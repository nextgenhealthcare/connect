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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.webreach.mirth.model.*;
import com.webreach.mirth.model.converters.DocumentSerializer;
import com.webreach.mirth.model.converters.IXMLSerializer;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.controllers.ScriptController;
import com.webreach.mirth.server.controllers.TemplateController;
import com.webreach.mirth.server.mule.adaptors.AdaptorFactory;
import com.webreach.mirth.server.tools.ClassPathResource;
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
	private static final String JDBC_TRANSACTION_FACTORY_CLASS = "com.webreach.mirth.connectors.jdbc.JdbcTransactionFactory";
	private Logger logger = Logger.getLogger(this.getClass());
	private List<Channel> channels = null;
	private Map<String, ConnectorMetaData> transports = null;
	private JavaScriptBuilder scriptBuilder = new JavaScriptBuilder();
	private ScriptController scriptController = ScriptController.getInstance();

	public MuleConfigurationBuilder(List<Channel> channels, Map<String, ConnectorMetaData> transports) {
		this.channels = channels;
		this.transports = transports;
	}

	public String getConfiguration() throws BuilderException {
		DocumentSerializer docSerializer = new DocumentSerializer();
		return docSerializer.toXML(getConfigurationDocument());
	}

	private Document getConfigurationDocument() throws BuilderException {
		logger.debug("generating mule configuration");

		if ((channels == null) || (transports == null)) {
			throw new BuilderException("Invalid channel or transport list.");
		}

		try {
			Properties properties = PropertyLoader.loadProperties("mirth");
			File muleBootstrapFile = new File(ClassPathResource.getResourceURI(PropertyLoader.getProperty(properties, "mule.template")));

			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(muleBootstrapFile);
			Element muleConfigurationElement = document.getDocumentElement();

			// set the server address
			Element agentsElement = (Element) muleConfigurationElement.getElementsByTagName("agents").item(0);
			NodeList agents = (NodeList) agentsElement.getElementsByTagName("agent");
			String port = PropertyLoader.getProperty(properties, "jmx.port");

			for (int i = 0; i < agents.getLength(); i++) {
				Element agent = (Element) agents.item(i);
				Element agentProperties = (Element) agent.getElementsByTagName("properties").item(0);
				Element propertyElement = document.createElement("property");

				if (agent.getAttribute("name").toLowerCase().equals("rmi")) {
					propertyElement.setAttribute("name", "serverUri");
					propertyElement.setAttribute("value", "rmi://localhost:" + port);
				} else if (agent.getAttribute("name").toLowerCase().equals("jmx")) {
					propertyElement.setAttribute("name", "connectorServerUrl");
					propertyElement.setAttribute("value", "service:jmx:rmi:///jndi/rmi://localhost:" + port + "/server");
					
					// add JMX credentials
					String jmxPassword = PropertyLoader.getProperty(properties, "jmx.password");
					Element credentialsMapElement = document.createElement("map");
					credentialsMapElement.setAttribute("name", "credentials");
					Element credentialsPropertyElement = document.createElement("property");
					credentialsPropertyElement.setAttribute("name", "admin");
					credentialsPropertyElement.setAttribute("value", jmxPassword);
					credentialsMapElement.appendChild(credentialsPropertyElement);
					agentProperties.appendChild(credentialsMapElement);
				}

				agentProperties.appendChild(propertyElement);
			}

			// set the Mule working directory
			String muleQueue = PropertyLoader.getProperty(properties, "mule.queue");
			muleQueue = StringUtils.replace(muleQueue, "${mirthHomeDir}", ConfigurationController.mirthHomeDir);
			Element muleEnvironmentPropertiesElement = (Element) muleConfigurationElement.getElementsByTagName("mule-environment-properties").item(0);
			muleEnvironmentPropertiesElement.setAttribute("workingDirectory", muleQueue);

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

			// ast: Response router
			Element responseRouterElement = getResponseRouter(document, configurationElement, channel);

			if (responseRouterElement != null) {
				muleDescriptorElement.appendChild(responseRouterElement);
			}

			return muleDescriptorElement;
		} catch (Exception e) {
			throw new BuilderException(e);
		}
	}

	/*
	 * ast: Add the getResponseRouter elements for the queued connectors
	 * 
	 */
	private Element getResponseRouter(Document document, Element configurationElement, Channel channel) throws BuilderException {
		Element responseRouterElement = null;

		for (ListIterator iterator = channel.getDestinationConnectors().listIterator(); iterator.hasNext();) {
			Connector connector = (Connector) iterator.next();
			
			if (connector.isEnabled()) {
				String usePersistentQueues = connector.getProperties().getProperty("usePersistentQueues");
	
				if ((usePersistentQueues != null) && (usePersistentQueues.equals("1"))) {
					if (responseRouterElement == null) {
						responseRouterElement = document.createElement("response-router");
					}
	
					Element endpointElement = document.createElement("endpoint");
					endpointElement.setAttribute("address", getEndpointUri(connector));
					String connectorName = getConnectorNameForOutputRouter(getConnectorReferenceForOutputRouter(channel, String.valueOf(iterator.nextIndex())));
					endpointElement.setAttribute("connector", connectorName);
					responseRouterElement.appendChild(endpointElement);
				}
			}
		}

		return responseRouterElement;
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
			addConnector(document, configurationElement, channel.getSourceConnector(), connectorReference + "_connector", channel.getId());
			endpointElement.setAttribute("connector", connectorReference + "_connector");
			// if the channel is snychronous
			if ((channel.getProperties().get("synchronous")) != null && ((String) channel.getProperties().get("synchronous")).equalsIgnoreCase("true")) {
				endpointElement.setAttribute("synchronous", "true");
			}
			StringBuilder endpointTransformers = new StringBuilder();
			StringBuilder vmTransformers = new StringBuilder();

			// 1. append the default transformers required by the transport (ex.
			// ByteArrayToString)
			ConnectorMetaData transport = transports.get(channel.getSourceConnector().getTransportName());
			endpointTransformers.append(transport.getTransformers() + " ");

			// 2. append the preprocessing transformer
			addPreprocessor(document, configurationElement, channel, connectorReference + "_preprocessor");
			endpointTransformers.append(connectorReference + "_preprocessor ");
			vmTransformers.append(connectorReference + "_preprocessor ");

			// 3. finally, append the JavaScriptTransformer that does the
			// mappings
			addTransformer(document, configurationElement, channel, channel.getSourceConnector(), connectorReference + "_transformer");
			endpointTransformers.append(connectorReference + "_transformer");
			vmTransformers.append(connectorReference + "_transformer");

			// 4. add the transformer sequence as an attribute to the endpoint
			// if not empty
			if (!endpointTransformers.toString().trim().equals("")) {
				endpointElement.setAttribute("transformers", endpointTransformers.toString().trim());
				vmEndpointElement.setAttribute("transformers", vmTransformers.toString().trim());
			}

			inboundRouterElement.appendChild(vmEndpointElement);

			Element routerElement = document.createElement("router");
			routerElement.setAttribute("className", "org.mule.routing.inbound.SelectiveConsumer");

			Element filterElement = document.createElement("filter");
			filterElement.setAttribute("className", "com.webreach.mirth.server.mule.filters.ValidMessageFilter");

			routerElement.appendChild(filterElement);
			inboundRouterElement.appendChild(routerElement);

			// NOTE: If the user selected the Channel Reader connector, then we
			// don't to add it since there already exists a VM connector for
			// every channel.
			if (!channel.getSourceConnector().getTransportName().equals("Channel Reader")) {
				inboundRouterElement.appendChild(endpointElement);
			}

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
			boolean enableTransactions = false;

			for (ListIterator iterator = channel.getDestinationConnectors().listIterator(); iterator.hasNext();) {
				Connector connector = (Connector) iterator.next();

				if (connector.isEnabled()) {
					Element endpointElement = document.createElement("endpoint");
					endpointElement.setAttribute("address", getEndpointUri(connector));

					// if there are multiple endpoints, make them all
					// synchronous to
					// ensure correct ordering of fired events
					if (channel.getDestinationConnectors().size() > 0) {
						endpointElement.setAttribute("synchronous", "true");
						routerElement.setAttribute("synchronous", "true");
					}

					// ast: now, a funciont gets the connection reference string
					// String connectorReference = channel.getId() +
					// "_destination_"
					// + String.valueOf(iterator.nextIndex());
					String connectorReference = getConnectorReferenceForOutputRouter(channel, String.valueOf(iterator.nextIndex()));

					// add the destination connector
					// ast: changes to get the same name for the connector and
					String connectorName = getConnectorNameForOutputRouter(connectorReference);
					addConnector(document, configurationElement, connector, connectorName, channel.getId());
					endpointElement.setAttribute("connector", connectorName);

					StringBuilder transformers = new StringBuilder();

					// 1. append the JavaScriptTransformer that does the
					// mappings
					addTransformer(document, configurationElement, channel, connector, connectorReference + "_transformer");
					transformers.append(connectorReference + "_transformer" + " ");

					// 2. finally, append any transformers needed by the
					// transport
					// (ie. StringToByteArray)
					ConnectorMetaData transport = transports.get(connector.getTransportName());

					if (transport.getTransformers() != null) {
						transformers.append(transport.getTransformers());
					}

					// enable transactions for the outbount router only if it
					// has a
					// JDBC connector
					if (transport.getProtocol().equals("jdbc")) {
						enableTransactions = true;
					}

					// 3. add the transformer sequence as an attribute to the
					// endpoint if not empty
					if (!transformers.toString().trim().equals("")) {
						endpointElement.setAttribute("transformers", transformers.toString().trim());
					}

					routerElement.appendChild(endpointElement);
				}
			}

			// check for enabled transactions
			boolean transactional = ((channel.getProperties().get("transactional") != null) && channel.getProperties().get("transactional").toString().equalsIgnoreCase("true"));

			if (enableTransactions && transactional) {
				String protocol = "jdbc";
				String action = "BEGIN_OR_JOIN";
				String factory = new String();

				if (protocol.equals("jdbc")) {
					factory = JDBC_TRANSACTION_FACTORY_CLASS;
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

	// ast: to sincronize the name of the connector for the output router and
	// the response router
	public String getConnectorNameForOutputRouter(String coonnectorReference) {
		return coonnectorReference + "_connector";

	}

	public String getConnectorReferenceForOutputRouter(Channel channel, String value) {
		return channel.getId() + "_destination_" + value;
	}

	private void addTransformer(Document document, Element configurationElement, Channel channel, Connector connector, String name) throws BuilderException {
		try {
			Element transformersElement = (Element) configurationElement.getElementsByTagName("transformers").item(0);
			Element transformerElement = document.createElement("transformer");
			Transformer transformer = connector.getTransformer();
			transformerElement.setAttribute("name", name);
			transformerElement.setAttribute("className", "com.webreach.mirth.server.mule.transformers.JavaScriptTransformer");

			Properties properties = new Properties();
			properties.put("channelId", channel.getId());
			properties.put("inboundProtocol", transformer.getInboundProtocol());
			properties.put("outboundProtocol", transformer.getOutboundProtocol());
			properties.put("encryptData", channel.getProperties().get("encryptData"));
			properties.put("removeNamespace", channel.getProperties().get("removeNamespace"));
			properties.put("mode", connector.getMode().toString());

			// put the outbound template in the templates table
			if (transformer.getOutboundTemplate() != null) {
				TemplateController templateController = TemplateController.getInstance();
				IXMLSerializer<String> serializer = AdaptorFactory.getAdaptor(transformer.getOutboundProtocol()).getSerializer(transformer.getOutboundProperties());
				String templateId = UUIDGenerator.getUUID();

				if (transformer.getOutboundTemplate().length() > 0) {
                    if(transformer.getOutboundProtocol().equals(MessageObject.Protocol.DICOM)) {
                        templateController.putTemplate(templateId, transformer.getOutboundTemplate());        
                    }
                    else 
                        templateController.putTemplate(templateId, serializer.toXML(transformer.getOutboundTemplate()));
				}

				properties.put("templateId", templateId);
			}

			// put the script in the scripts table
			String scriptId = UUIDGenerator.getUUID();
			scriptController.putScript(scriptId, scriptBuilder.getScript(channel, connector.getFilter(), transformer));
			properties.put("scriptId", scriptId);
			properties.put("connectorName", connector.getName());

			Element propertiesElement = getProperties(document, properties, null);

			if (transformer.getInboundProperties() != null && transformer.getInboundProperties().size() > 0) {
				Element inboundPropertiesElement = getPropertiesMap(document, transformer.getInboundProperties(), null, "inboundProperties");
				propertiesElement.appendChild(inboundPropertiesElement);
			}

			if (transformer.getOutboundProperties() != null && transformer.getOutboundProperties().size() > 0) {
				Element outboundPropertiesElement = getPropertiesMap(document, transformer.getOutboundProperties(), null, "outboundProperties");
				propertiesElement.appendChild(outboundPropertiesElement);
			}
			transformerElement.appendChild(propertiesElement);
			transformersElement.appendChild(transformerElement);
		} catch (Exception e) {
			throw new BuilderException(e);
		}
	}

	private void addConnector(Document document, Element configurationElement, Connector connector, String name, String channelId) throws BuilderException {
		try {
			// get the transport associated with this class from the transport
			// map
			ConnectorMetaData transport = transports.get(connector.getTransportName());
			Element connectorElement = document.createElement("connector");
			connectorElement.setAttribute("name", name);
			connectorElement.setAttribute("className", transport.getServerClassName());

			// exception-strategy
			Element exceptionStrategyElement = document.createElement("exception-strategy");
			exceptionStrategyElement.setAttribute("className", "com.webreach.mirth.server.mule.ExceptionStrategy");
			connectorElement.appendChild(exceptionStrategyElement);

			// TODO: (maybe) create a ConnectorProperty object that has
			// name, value, and isMuleProperty attribute
			// then only add the properties that have isMuleProperty set to true
			Properties connectorProperties = connector.getProperties();
			// The connector needs it's channel id (so it doesn't have to parse
			// the name)
			// for alerts
			connectorProperties.put("channelId", channelId);
			Element propertiesElement = document.createElement("properties");
			Element mapElement = document.createElement("map");
			mapElement.setAttribute("name", "queries");

			for (Iterator iter = connectorProperties.entrySet().iterator(); iter.hasNext();) {
				Entry property = (Entry) iter.next();

				// list of all properties which should not be appended to the
				// connector
				ArrayList<String> nonConnectorProperties = new ArrayList<String>();
				nonConnectorProperties.add("host");
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
					} else if (property.getKey().equals("connectionFactoryProperties") || property.getKey().equals("requestVariables") || property.getKey().equals("headerVariables")) {
						ObjectXMLSerializer serializer = new ObjectXMLSerializer();
						Properties connectionFactoryProperties = (Properties) serializer.fromXML(property.getValue().toString());
						Element connectionFactoryPropertiesMapElement = getPropertiesMap(document, connectionFactoryProperties, null, property.getKey().toString());
						propertiesElement.appendChild(connectionFactoryPropertiesMapElement);
					} else {
						// script is a special property reserved for some
						// connectors
						if (property.getKey().equals("script") || property.getKey().equals("ackScript")) {
							// put the script in the scripts table
							String databaseScriptId = UUIDGenerator.getUUID();
							scriptController.putScript(databaseScriptId, property.getValue().toString());
							Element propertyElement = document.createElement("property");
							propertyElement.setAttribute("name", property.getKey() + "Id");
							propertyElement.setAttribute("value", databaseScriptId);
							propertiesElement.appendChild(propertyElement);
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
			}

			// if queries/statements/ack have been added to the queries map, add
			// the map to the connector properties
			if (mapElement.hasChildNodes()) {
				propertiesElement.appendChild(mapElement);
			}

			// add the inbound transformer's protocol as a connector properties
			Element protocolPropertyElement = document.createElement("property");
			protocolPropertyElement.setAttribute("name", "protocol");
			protocolPropertyElement.setAttribute("value", connector.getTransformer().getInboundProtocol().toString());
			propertiesElement.appendChild(protocolPropertyElement);

			if (connector.getMode().equals(Connector.Mode.SOURCE)) {
				
				// add the protocol properties to the connector
				Properties protocolProperties = connector.getTransformer().getInboundProperties();
				
				if (protocolProperties != null && protocolProperties.size() > 0) {
					Element protocolPropertiesElement = getPropertiesMap(document, protocolProperties, null, "protocolProperties");
					propertiesElement.appendChild(protocolPropertiesElement);
				}
			}
			
			// add the properties to the connector
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
	 * Returns a properties map element given a Properties object and a List of
	 * properties which should be text-property elements, as well as a name for
	 * the map
	 * 
	 * @param document
	 * @param properties
	 * @param textProperties
	 * @param name
	 * @return
	 */
	private Element getPropertiesMap(Document document, Properties properties, List<String> textProperties, String name) {
		Element propertiesElement = document.createElement("map");
		propertiesElement.setAttribute("name", name);
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
		if (connector.getProperties().getProperty("host") != null && (connector.getProperties().getProperty("host").startsWith("axis:") || connector.getProperties().getProperty("host").startsWith("http"))) {
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
