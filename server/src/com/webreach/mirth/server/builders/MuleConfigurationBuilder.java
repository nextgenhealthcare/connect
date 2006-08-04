/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.server.builders;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.Transformer;
import com.webreach.mirth.model.Transport;
import com.webreach.mirth.model.converters.DocumentSerializer;
import com.webreach.mirth.util.PropertyLoader;

/**
 * A MuleConfigurationBuilder is used to generate Mule configurations based on
 * the current internal Channel configuration.
 * 
 * @author geraldb
 * 
 */
public class MuleConfigurationBuilder {
	public static final String[] cDataElements = null;
	private Logger logger = Logger.getLogger(this.getClass());

	private List<Channel> channels = null;
	private Map<String, Transport> transports = null;

	private JavaScriptFilterBuilder filterBuilder = new JavaScriptFilterBuilder();
	private JavaScriptTransformerBuilder transformerBuilder = new JavaScriptTransformerBuilder();

	public MuleConfigurationBuilder(List<Channel> channels, Map<String, Transport> transports) {
		this.channels = channels;
		this.transports = transports;
	}

	public String getConfiguration() throws BuilderException {
		DocumentSerializer docSerializer = new DocumentSerializer(cDataElements);
		return docSerializer.toXML(getConfigurationDocument());
	}

	private Document getConfigurationDocument() throws BuilderException {
		logger.debug("generating mule configuration");

		if ((channels == null) || (transports == null)) {
			throw new BuilderException("Invalid channel or transport list.");
		}

		try {
			Properties properties = PropertyLoader.loadProperties("mirth");
			File muleBootstrapFile = new File(properties.getProperty("mule.boot"));

			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(muleBootstrapFile);
			Element muleConfigurationElement = document.getDocumentElement();
			Element modelElement = (Element) muleConfigurationElement.getElementsByTagName("model").item(0);

			// create descriptors
			for (Iterator iter = channels.iterator(); iter.hasNext();) {
				Channel channel = (Channel) iter.next();

				if (channel.isEnabled()) {
					modelElement.appendChild(getDescriptor(document, muleConfigurationElement, channel));
				}
			}

			return document;
		} catch (BuilderException be) {
			throw be;
		} catch (Exception e) {
			throw new BuilderException(e);
		}
	}

	private Element getDescriptor(Document document, Element configurationElement, Channel channel) throws BuilderException {
		try {
			Element muleDescriptorElement = document.createElement("mule-descriptor");
			muleDescriptorElement.setAttribute("implementation", "com.webreach.mirth.server.mule.components.ChannelComponent");	
			muleDescriptorElement.setAttribute("name", String.valueOf(channel.getId()));
			
			// default initial state is stopped if no state is found
			String initialState = "stopped";
			
			if (channel.getProperties().getProperty("initialState") != null) {
				initialState = channel.getProperties().getProperty("initialState");
			}
			
			muleDescriptorElement.setAttribute("initialState", initialState);

			// inbound-router
			muleDescriptorElement.appendChild(getInboundRouter(document, configurationElement, channel));

			// outbound-router
			muleDescriptorElement.appendChild(getOutboundRouter(document, configurationElement, channel));

			return muleDescriptorElement;
		} catch (Exception e) {
			throw new BuilderException(e);
		}
	}

	private Element getInboundRouter(Document document, Element configurationElement, Channel channel) throws BuilderException {
		try {
			Element inboundRouterElement = document.createElement("inbound-router");
			Element endpointElement = document.createElement("endpoint");
			endpointElement.setAttribute("address", getEndpointUri(channel.getSourceConnector()));

			String connectorReference = String.valueOf(channel.getId()) + "_source";

			// add the source connector
			addConnector(document, configurationElement, channel.getSourceConnector(), connectorReference);
			endpointElement.setAttribute("connector", connectorReference);

			StringBuilder transformers = new StringBuilder();

			// 1. append the default transformers required by the transport (ex.
			// ByteArrayToString)
			Transport transport = transports.get(channel.getSourceConnector().getTransportName());
			transformers.append(transport.getTransformers() + " ");

			// 2. if it's an inbound channel and the messages aren't
			// pre-encoded, append the HL7StringToXMLString transformer
			if (channel.getDirection().equals(Channel.Direction.INBOUND) && !channel.getProperties().get("recv_xml_encoded").equals("true")) {
				transformers.append("ER7toXML ");
			}

			// 3. finally, append the JavaScriptTransformer that does the
			// mappings if it's BROADCAST
			if (channel.getMode().equals(Channel.Mode.BROADCAST)) {
				addTransformer(document, configurationElement, channel, channel.getSourceConnector().getTransformer(), connectorReference);
				transformers.append(connectorReference);
			}

			// 4. add the transformer sequence as an attribute to the endpoint
			// if not empty
			if (!transformers.toString().trim().equals("")) {
				endpointElement.setAttribute("transformers", transformers.toString().trim());
			}

			if (channel.getMode().equals(Channel.Mode.BROADCAST)) {
				Element filterElement = document.createElement("filter");
				filterElement.setAttribute("className", "com.webreach.mirth.server.mule.filters.JavaScriptFilter");

				// add the filter script properties
				Properties properties = new Properties();
				properties.put("script", filterBuilder.getScript(channel.getSourceConnector().getFilter()));
				filterElement.appendChild(getProperties(document, properties));

				endpointElement.appendChild(filterElement);
			}
			
			inboundRouterElement.appendChild(endpointElement);
			return inboundRouterElement;
		} catch (Exception e) {
			throw new BuilderException(e);
		}
	}

	private Element getOutboundRouter(Document document, Element configurationElement, Channel channel) throws BuilderException {
		try {
			Element outboundRouterElement = document.createElement("outbound-router");

			Element routerElement = document.createElement("router");
			routerElement.setAttribute("className", "org.mule.routing.outbound.FilteringMulticastingRouter");

			for (ListIterator iterator = channel.getDestinationConnectors().listIterator(); iterator.hasNext();) {
				Connector connector = (Connector) iterator.next();

				Element endpointElement = document.createElement("endpoint");
				endpointElement.setAttribute("address", getEndpointUri(connector));

				String connectorReference = String.valueOf(channel.getId()) + "_destination_" + String.valueOf(iterator.nextIndex());

				// add the destination connector
				addConnector(document, configurationElement, connector, connectorReference);
				endpointElement.setAttribute("connector", connectorReference);

				StringBuilder transformers = new StringBuilder();

				// 1. append the JavaScriptTransformer that does the mappings if
				// it's ROUTER
				if (channel.getMode().equals(Channel.Mode.ROUTER)) {
					addTransformer(document, configurationElement, channel, connector.getTransformer(), connectorReference);
					transformers.append(connectorReference + " ");
				}

				// 2. convert the XML message in the map to an ER7 message
				if (channel.getDirection().equals(Channel.Direction.OUTBOUND)) {
					//transformers.append("XMLtoER7 ");
					//CL - moved to JavascriptTransformer
				}

				// 3. finally, append any transformers needed by the transport
				// (ie. StringToByteArray)
				Transport transport = transports.get(connector.getTransportName());
				transformers.append(transport.getTransformers());

				// 4. add the transformer sequence as an attribute to the
				// endpoint if not empty
				if (!transformers.toString().trim().equals("")) {
					endpointElement.setAttribute("transformers", transformers.toString().trim());
				}

				if (channel.getMode().equals(Channel.Mode.ROUTER)) {
					// add the filter
					Element filterElement = document.createElement("filter");
					filterElement.setAttribute("className", "com.webreach.mirth.server.mule.filters.JavaScriptFilter");

					// add the filter script properties
					Properties properties = new Properties();
					properties.put("script", filterBuilder.getScript(connector.getFilter()));
					filterElement.appendChild(getProperties(document, properties));

					endpointElement.appendChild(filterElement);
				}

				routerElement.appendChild(endpointElement);
			}

			outboundRouterElement.appendChild(routerElement);
			return outboundRouterElement;
		} catch (Exception e) {
			throw new BuilderException(e);
		}
	}

	private void addTransformer(Document document, Element configurationElement, Channel channel, Transformer transformer, String name) throws BuilderException {
		try {
			Element transformersElement = (Element) configurationElement.getElementsByTagName("transformers").item(0);
			Element transformerElement = document.createElement("transformer");
			transformerElement.setAttribute("name", name);
			transformerElement.setAttribute("className", "com.webreach.mirth.server.mule.transformers.JavaScriptTransformer");

			// add the transformer script properties
			Properties properties = new Properties();
			properties.put("script", transformerBuilder.getScript(transformer, channel));	
			transformerElement.appendChild(getProperties(document, properties));

			transformersElement.appendChild(transformerElement);
		} catch (Exception e) {
			throw new BuilderException(e);
		}
	}

	private void addConnector(Document document, Element configurationElement, Connector connector, String name) throws BuilderException {
		try {
			// get the transport associated with this class from the transport
			// map
			Transport transport = transports.get(connector.getTransportName());
			Element connectorElement = document.createElement("connector");
			connectorElement.setAttribute("name", name);
			connectorElement.setAttribute("className", transport.getClassName());

			// TODO: (maybe) create a ConnectorProperty object that has
			// name, value, and isMuleProperty attribute
			// then only add the properties that have isMuleProperty set to true
			Properties connectorProperties = connector.getProperties();
			Element propertiesElement = document.createElement("properties");
			Element mapElement = document.createElement("map");
			mapElement.setAttribute("name", "queries");

			for (Iterator iter = connectorProperties.entrySet().iterator(); iter.hasNext();) {
				Entry property = (Entry) iter.next();

				// list of all properties which should not be appended to the
				// connector
				ArrayList<String> nonConnectorProperties = new ArrayList<String>();
				nonConnectorProperties.add("host");
				nonConnectorProperties.add("hostname");
				nonConnectorProperties.add("port");
				nonConnectorProperties.add("DataType");

				// only add non-null, non-empty, non-Mule properties to the list
				if ((property.getValue() != null) && (!property.getValue().equals("")) && !nonConnectorProperties.contains(property.getKey())) {
					Element propertyElement = document.createElement("property");
					propertyElement.setAttribute("name", property.getKey().toString());
					propertyElement.setAttribute("value", property.getValue().toString());
					
					if (property.getKey().equals("query") || property.getKey().equals("statement") || property.getKey().equals("ack")) {
						mapElement.appendChild(propertyElement);
					} else {
						propertiesElement.appendChild(propertyElement);
					}
				}
			}

			// if queries/statements/ack have been added to the queries map, add
			// the map to the connector properties
			if (mapElement.hasChildNodes()) {
				propertiesElement.appendChild(mapElement);
			}

			connectorElement.appendChild(propertiesElement);

			// insert the connector before the tranformers element to maintain
			// sequence
			Element transformersElement = (Element) configurationElement.getElementsByTagName("transformers").item(0);
			configurationElement.insertBefore(connectorElement, transformersElement);
		} catch (Exception e) {
			throw new BuilderException(e);
		}
	}

	private Element getProperties(Document document, Properties properties) {
		Element propertiesElement = document.createElement("properties");

		for (Iterator iter = properties.entrySet().iterator(); iter.hasNext();) {
			Entry property = (Entry) iter.next();

			// only add non-null and non-empty properties to the list
			if ((property.getValue() != null) && (!property.getValue().equals(""))) {
				Element propertyElement = document.createElement("property");
				propertyElement.setAttribute("name", property.getKey().toString());
				propertyElement.setAttribute("value", property.getValue().toString());
				propertiesElement.appendChild(propertyElement);
			}
		}

		return propertiesElement;
	}

	// Generate the endpoint URI for the specified connector.
	// The format is: protocol://host|hostname|emtpy:port
	private String getEndpointUri(Connector connector) {
		StringBuilder builder = new StringBuilder();
		builder.append(transports.get(connector.getTransportName()).getProtocol());
		builder.append("://");

		if (connector.getProperties().getProperty("host") != null) {
			builder.append(connector.getProperties().getProperty("host"));
		} else if (connector.getProperties().getProperty("hostname") != null) {
			builder.append(connector.getProperties().getProperty("hostname"));
		}

		if (connector.getProperties().getProperty("port") != null) {
			builder.append(":");
			builder.append(connector.getProperties().getProperty("port"));
		}

		return builder.toString();
	}
}
