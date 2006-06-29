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
import java.util.Iterator;
import java.util.List;
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
	private Logger logger = Logger.getLogger(MuleConfigurationBuilder.class);

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
		return docSerializer.serialize(getConfigurationDocument());
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
		} catch (Exception e) {
			throw new BuilderException(e);
		}
	}

	private Element getDescriptor(Document document, Element configurationElement, Channel channel) throws BuilderException {
		try {
			Element muleDescriptorElement = document.createElement("mule-descriptor");
			muleDescriptorElement.setAttribute("implementation", "com.webreach.mirth.server.mule.components.ChannelComponent");
			muleDescriptorElement.setAttribute("name", String.valueOf(channel.getId()));
			muleDescriptorElement.setAttribute("initialState", "stopped");

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

			// add the transformer for the connector
			Transport transport = transports.get(channel.getSourceConnector().getTransportName());
			addTransformer(document, configurationElement, channel.getSourceConnector().getTransformer(), connectorReference);
			// prepend the necessary transformers required by this transport to
			// turn it into proper format for the transformer
			endpointElement.setAttribute("transformers", (transport.getTransformers() + " " + connectorReference).trim());

			Element routerElement = document.createElement("router");
			routerElement.setAttribute("className", "org.mule.routing.inbound.SelectiveConsumer");

			Element filterElement = document.createElement("filter");
			filterElement.setAttribute("className", "com.webreach.mirth.server.mule.filters.JavaScriptFilter");

			// add the filter script properties
			Properties properties = new Properties();
			properties.put("script", filterBuilder.getScript(channel.getSourceConnector().getFilter()));
			filterElement.appendChild(getProperties(document, properties));

			routerElement.appendChild(filterElement);
			inboundRouterElement.appendChild(endpointElement);
			inboundRouterElement.appendChild(routerElement);

			return inboundRouterElement;
		} catch (Exception e) {
			throw new BuilderException(e);
		}
	}

	private Element getOutboundRouter(Document document, Element configurationElement, Channel channel) throws BuilderException {
		try {
			Element outboundRouterElement = document.createElement("outbound-router");
			int connectorIndex = 0;

			for (Iterator iter = channel.getDestinationConnectors().iterator(); iter.hasNext();) {
				Connector connector = (Connector) iter.next();

				Element routerElement = document.createElement("router");
				routerElement.setAttribute("className", "org.mule.routing.outbound.FilteringOutboundRouter");

				Element endpointElement = document.createElement("endpoint");
				endpointElement.setAttribute("address", getEndpointUri(connector));

				String connectorReference = String.valueOf(channel.getId()) + "_destination_" + String.valueOf(connectorIndex);

				// add the destination connector
				addConnector(document, configurationElement, connector, connectorReference);
				endpointElement.setAttribute("connector", connectorReference);

				// add the transformer for this destination connector
				Transport transport = transports.get(connector.getTransportName());
				addTransformer(document, configurationElement, connector.getTransformer(), connectorReference);
				endpointElement.setAttribute("transformers", (transport.getTransformers() + " " + connectorReference).trim());

				routerElement.appendChild(endpointElement);

				// add the filter
				Element filterElement = document.createElement("filter");
				filterElement.setAttribute("className", "com.webreach.mirth.server.mule.filters.JavaScriptFilter");

				// add the filter script properties
				Properties properties = new Properties();
				properties.put("script", filterBuilder.getScript(connector.getFilter()));
				filterElement.appendChild(getProperties(document, properties));

				routerElement.appendChild(filterElement);

				outboundRouterElement.appendChild(routerElement);
				connectorIndex++;
			}

			return outboundRouterElement;
		} catch (Exception e) {
			throw new BuilderException(e);
		}
	}

	private void addTransformer(Document document, Element configurationElement, Transformer transformer, String name) throws BuilderException {
		try {
			Element transformersElement = (Element) configurationElement.getElementsByTagName("transformers").item(0);
			Element transformerElement = document.createElement("transformer");
			transformerElement.setAttribute("name", name);
			transformerElement.setAttribute("className", "com.webreach.mirth.server.mule.transformers.JavaScriptTransformer");

			// add the transformer script properties
			Properties properties = new Properties();
			properties.put("script", transformerBuilder.getScript(transformer));
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
			// TODO: handle the case where a queries map is added
			
//			connectorElement.appendChild(getProperties(document, connector.getProperties()));

			// insert the connector before the tranformers element to maintain sequence
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
			Element propertyElement = document.createElement("property");
			propertyElement.setAttribute("name", property.getKey().toString());
			propertyElement.setAttribute("value", property.getValue().toString());
			propertiesElement.appendChild(propertyElement);
		}

		return propertiesElement;
	}
	
	private String getEndpointUri(Connector connector) {
		StringBuilder builder = new StringBuilder();
		builder.append(transports.get(connector.getTransportName()).getProtocol());
		builder.append("://");
		builder.append(connector.getProperties().getProperty("address"));
		
		if (connector.getProperties().getProperty("port") != null) {
			builder.append(":");
			builder.append(connector.getProperties().getProperty("port"));
		}

		return builder.toString();
	}
}
