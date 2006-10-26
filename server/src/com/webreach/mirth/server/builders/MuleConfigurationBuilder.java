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
import com.webreach.mirth.model.Transport;
import com.webreach.mirth.model.converters.DocumentSerializer;
import com.webreach.mirth.model.converters.ER7Serializer;
import com.webreach.mirth.server.controllers.ScriptController;
import com.webreach.mirth.server.controllers.TemplateController;
import com.webreach.mirth.server.util.UUIDGenerator;
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

	private ScriptController scriptController = new ScriptController();

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
			muleDescriptorElement.setAttribute("implementation", "com.webreach.mirth.server.mule.components.Channel");
			muleDescriptorElement.setAttribute("name", String.valueOf(channel.getId()));

			// default initial state is stopped if no state is found
			String initialState = "stopped";

			if (channel.getProperties().getProperty("initialState") != null) {
				initialState = channel.getProperties().getProperty("initialState");
			}

			muleDescriptorElement.setAttribute("initialState", initialState);

			// exception-strategy
			Element exceptionStrategyElement = document.createElement("exception-strategy");
			exceptionStrategyElement.setAttribute("className", "com.webreach.mirth.server.mule.ExceptionStrategy");
			muleDescriptorElement.appendChild(exceptionStrategyElement);

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

			// add the VM endpoint for reprocessing capabilities
			Element vmEndpointElement = document.createElement("endpoint");
			vmEndpointElement.setAttribute("address", "vm://" + channel.getId());

			// add the configured endpoint
			Element endpointElement = document.createElement("endpoint");
			endpointElement.setAttribute("address", getEndpointUri(channel.getSourceConnector()));

			String connectorReference = channel.getId() + "_source";

			// add the source connector
			addConnector(document, configurationElement, channel.getSourceConnector(), connectorReference + "_connector");
			endpointElement.setAttribute("connector", connectorReference + "_connector");

			StringBuilder endpointTransformers = new StringBuilder();
			StringBuilder vmTransformers = new StringBuilder();

			// 1. append the default transformers required by the transport (ex.
			// ByteArrayToString)
			Transport transport = transports.get(channel.getSourceConnector().getTransportName());
			endpointTransformers.append(transport.getTransformers() + " ");

			// 2. append the preprocessing transformer
			addPreprocessor(document, configurationElement, channel, connectorReference + "_preprocessor");
			endpointTransformers.append(connectorReference + "_preprocessor ");
			vmTransformers.append(connectorReference + "_preprocessor ");

			// 3. determine which transformer to use
			if (channel.getDirection().equals(Channel.Direction.OUTBOUND)) {
				endpointTransformers.append("XMLToMessageObject ");
				vmTransformers.append("XMLToMessageObject ");
			} else {
				if (channel.getProtocol().equals(Channel.Protocol.HL7)) {
					endpointTransformers.append("HL7ToMessageObject ");
					vmTransformers.append("HL7ToMessageObject ");
				} else if (channel.getProtocol().equals(Channel.Protocol.X12)) {
					endpointTransformers.append("X12ToMessageObject ");
					vmTransformers.append("X12ToMessageObject ");
				} else {
					endpointTransformers.append("XMLToMessageObject ");
					vmTransformers.append("XMLToMessageObject ");
				}
			}

			// 4. finally, append the JavaScriptTransformer that does the
			// mappings if it's BROADCAST
			if (channel.getMode().equals(Channel.Mode.BROADCAST)) {
				addTransformer(document, configurationElement, channel, channel.getSourceConnector(), connectorReference + "_transformer");
				endpointTransformers.append(connectorReference + "_transformer");
				vmTransformers.append(connectorReference + "_transformer");
			}

			// 5. add the transformer sequence as an attribute to the endpoint
			// if not empty
			if (!endpointTransformers.toString().trim().equals("")) {
				endpointElement.setAttribute("transformers", endpointTransformers.toString().trim());
				vmEndpointElement.setAttribute("transformers", vmTransformers.toString().trim());
			}

			inboundRouterElement.appendChild(vmEndpointElement);

			if (channel.getMode().equals(Channel.Mode.BROADCAST)) {
				Element routerElement = document.createElement("router");
				routerElement.setAttribute("className", "org.mule.routing.inbound.SelectiveConsumer");

				Element filterElement = document.createElement("filter");
				filterElement.setAttribute("className", "com.webreach.mirth.server.mule.filters.ValidMessageFilter");

				routerElement.appendChild(filterElement);
				inboundRouterElement.appendChild(routerElement);
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

				// if there are multiple endpoints, make them all synchronous to
				// ensure correct ordering of fired events
				if (channel.getDestinationConnectors().size() > 0) {
					endpointElement.setAttribute("synchronous", "true");
				}

				String connectorReference = channel.getId() + "_destination_" + String.valueOf(iterator.nextIndex());

				// add the destination connector
				addConnector(document, configurationElement, connector, connectorReference + "_connector");
				endpointElement.setAttribute("connector", connectorReference + "_connector");

				StringBuilder transformers = new StringBuilder();

				// 1. append the JavaScriptTransformer that does the mappings if
				// it's ROUTER
				if (channel.getMode().equals(Channel.Mode.ROUTER)) {
					addTransformer(document, configurationElement, channel, connector, connectorReference + "_transformer");
					transformers.append(connectorReference + "_transformer" + " ");
				}

				// 2. finally, append any transformers needed by the transport
				// (ie. StringToByteArray)
				Transport transport = transports.get(connector.getTransportName());
				transformers.append(transport.getTransformers());

				// 3. add the transformer sequence as an attribute to the
				// endpoint if not empty
				if (!transformers.toString().trim().equals("")) {
					endpointElement.setAttribute("transformers", transformers.toString().trim());
				}

				routerElement.appendChild(endpointElement);
			}

			// transaction support
			boolean transactional = ((channel.getProperties().get("transactional") != null) && channel.getProperties().get("transactional").toString().equalsIgnoreCase("true"));

			if (transactional) {
				// transports.get(connector.getTransportName()).getProtocol();
				String protocol = "jdbc";
				String factory = new String();
				String action = "BEGIN_OR_JOIN";

				if (protocol.equals("jdbc")) {
					factory = "org.mule.providers.jdbc.JdbcTransactionFactory";
				}

				Element transactionElement = document.createElement("transaction");
				transactionElement.setAttribute("action", action);
				transactionElement.setAttribute("factory", factory);
				routerElement.appendChild(transactionElement);
			}

			outboundRouterElement.appendChild(routerElement);
			return outboundRouterElement;
		} catch (Exception e) {
			throw new BuilderException(e);
		}
	}

	private void addTransformer(Document document, Element configurationElement, Channel channel, Connector connector, String name) throws BuilderException {
		try {
			Element transformersElement = (Element) configurationElement.getElementsByTagName("transformers").item(0);
			Element transformerElement = document.createElement("transformer");
			transformerElement.setAttribute("name", name);
			transformerElement.setAttribute("className", "com.webreach.mirth.server.mule.transformers.JavaScriptTransformer");

			Properties properties = new Properties();
			properties.put("channelId", channel.getId());
			properties.put("protocol", channel.getProtocol().toString());
			properties.put("direction", channel.getDirection().toString());
			properties.put("encryptData", channel.getProperties().get("encryptData"));
			properties.put("storeMessages", channel.getProperties().get("store_messages"));

			// if outbound, put the template in the templates table
			if (channel.getDirection().equals(Channel.Direction.OUTBOUND) && (connector.getTransformer().getTemplate() != null)) {
				TemplateController templateController = new TemplateController();
				ER7Serializer serializer = new ER7Serializer();
				String templateId = UUIDGenerator.getUUID();
				templateController.putTemplate(templateId, serializer.toXML(connector.getTransformer().getTemplate()));
				properties.put("templateId", templateId);
			}
			
			// put the filter script in the scripts table
			String filterScriptId = UUIDGenerator.getUUID();
			scriptController.putScript(filterScriptId, filterBuilder.getScript(connector.getFilter(), channel));
			properties.put("filterScriptId", filterScriptId);

			// put the transformer script in the scripts table
			String transformerScriptId = UUIDGenerator.getUUID();
			scriptController.putScript(transformerScriptId, transformerBuilder.getScript(connector.getTransformer(), channel));
			properties.put("transformerScriptId", transformerScriptId);

			if (channel.getMode().equals(Channel.Mode.ROUTER)) {
				properties.put("connectorName", connector.getName());
			} else {
				properties.put("connectorName", "Source");
			}

			transformerElement.appendChild(getProperties(document, properties, null));

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

			// exception-strategy
			Element exceptionStrategyElement = document.createElement("exception-strategy");
			exceptionStrategyElement.setAttribute("className", "com.webreach.mirth.server.mule.ExceptionStrategy");
			connectorElement.appendChild(exceptionStrategyElement);

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
				// the getProperties method could not be used since this is a
				// special case involving a map element for the queries
				if ((property.getValue() != null) && (!property.getValue().equals("")) && !nonConnectorProperties.contains(property.getKey())) {
					if (property.getKey().equals("template")) {
						Element textPropertyElement = document.createElement("text-property");
						textPropertyElement.setAttribute("name", "template");
						textPropertyElement.setTextContent(property.getValue().toString());
						propertiesElement.appendChild(textPropertyElement);
					} else {
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

	private void addPreprocessor(Document document, Element configurationElement, Channel channel, String name) throws BuilderException {
		try {
			Element transformersElement = (Element) configurationElement.getElementsByTagName("transformers").item(0);
			Element transformerElement = document.createElement("transformer");
			transformerElement.setAttribute("name", name);
			transformerElement.setAttribute("className", "com.webreach.mirth.server.mule.transformers.JavaScriptPreprocessor");
			Properties properties = new Properties();
			String preprocessingScriptId = UUIDGenerator.getUUID();
			scriptController.putScript(preprocessingScriptId, channel.getPreprocessingScript());
			properties.put("preprocessingScriptId", preprocessingScriptId);
			transformerElement.appendChild(getProperties(document, properties, null));
			transformersElement.appendChild(transformerElement);
		} catch (Exception e) {
			throw new BuilderException(e);
		}
	}

	/**
	 * Returns a properties element given a Properties object and a List of
	 * properties which should be text-property elements.
	 * 
	 * @param document
	 * @param properties
	 * @param textProperties
	 * @return
	 */
	private Element getProperties(Document document, Properties properties, List<String> textProperties) {
		Element propertiesElement = document.createElement("properties");

		for (Iterator iter = properties.entrySet().iterator(); iter.hasNext();) {
			Entry property = (Entry) iter.next();

			// only add non-null and non-empty properties to the list
			if ((property.getValue() != null) && (!property.getValue().equals(""))) {
				if ((textProperties != null) && textProperties.contains(property.getKey())) {
					Element textPropertyElement = document.createElement("text-property");
					textPropertyElement.setAttribute("name", property.getKey().toString());
					textPropertyElement.setTextContent(property.getValue().toString());
					propertiesElement.appendChild(textPropertyElement);
				} else {
					Element propertyElement = document.createElement("property");
					propertyElement.setAttribute("name", property.getKey().toString());
					propertyElement.setAttribute("value", property.getValue().toString());
					propertiesElement.appendChild(propertyElement);
				}
			}
		}

		return propertiesElement;
	}

	// Generate the endpoint URI for the specified connector.
	// The format is: protocol://host|hostname|emtpy:port
	private String getEndpointUri(Connector connector) {
		// TODO: This is a hack.
		if (connector.getProperties().getProperty("host") != null && connector.getProperties().getProperty("host").startsWith("axis:http")) {
			return connector.getProperties().getProperty("host");
		}
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
