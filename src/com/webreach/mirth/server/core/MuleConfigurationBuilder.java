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

package com.webreach.mirth.server.core;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.Transport;
import com.webreach.mirth.model.bind.PropertiesMarshaller;
import com.webreach.mirth.server.core.util.PropertyLoader;

/**
 * A MuleConfigurationBuilder is used to generate Mule configuration files based
 * on the current internal Channel configuration.
 * 
 * @author geraldb
 * 
 */
public class MuleConfigurationBuilder {
	public static final String[] cDataElements = null;
	private Logger logger = Logger.getLogger(PropertiesMarshaller.class);

	private List<Channel> channels = null;
	private Map<String, Transport> transports = null;

	public MuleConfigurationBuilder(List<Channel> channels, Map<String, Transport> transports) {
		this.channels = channels;
		this.transports = transports;
	}

	public Document getConfiguration() throws ConfigurationBuilderException {
		logger.debug("generating mule configuration");

		if ((channels == null) || (transports == null)) {
			throw new ConfigurationBuilderException();
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
				modelElement.appendChild(getDescriptor(document, muleConfigurationElement, channel));
			}

			return document;
		} catch (Exception e) {
			throw new ConfigurationBuilderException(e);
		}
	}

	private Element getDescriptor(Document document, Element configurationElement, Channel channel) throws ConfigurationBuilderException {
		try {
			Element muleDescriptorElement = document.createElement("mule-descriptor");
			muleDescriptorElement.setAttribute("implementation", "com.webreach.mirth.mule.components.Channel");
			muleDescriptorElement.setAttribute("name", "channel_" + String.valueOf(channel.getId()));
			muleDescriptorElement.setAttribute("initialState", channel.getInitialStatus().toString());

			// inbound-router
			muleDescriptorElement.appendChild(getInboundRouter(document, configurationElement, channel));
			 
			// outbound-router
			muleDescriptorElement.appendChild(getOutboundRouter(document, configurationElement, channel));

			return muleDescriptorElement;
		} catch (Exception e) {
			throw new ConfigurationBuilderException(e);
		}
	}

	private Element getInboundRouter(Document document, Element configurationElement, Channel channel) throws ConfigurationBuilderException {
		try {
			Element inboundRouterElement = document.createElement("inbound-router");
			Element endpointElement = document.createElement("endpoint");
			endpointElement.setAttribute("address", channel.getSourceConnector().getProperties().getProperty("address"));
			
			String connectorReference = String.valueOf("channel_" + channel.getId()) + "_source";
			
			// add the source connector
			addConnector(document, configurationElement, channel.getSourceConnector(), connectorReference);
			endpointElement.setAttribute("connector", connectorReference);
			
			// add the transformer for the connector
			addTransformer(document, configurationElement, channel.getSourceConnector(), connectorReference);
			endpointElement.setAttribute("transformers", connectorReference);
			
			Element routerElement = document.createElement("router");
			routerElement.setAttribute("className", "org.mule.routing.inbound.SelectiveConsumer");

			Element filterElement = document.createElement("filter");
			filterElement.setAttribute("className", null);

			Element propertiesElement = document.createElement("properties");
			Element propertyElement = document.createElement("property");
			propertyElement.setAttribute("name", "script");
			propertyElement.setAttribute("value", channel.getFilter().getScript());
			propertiesElement.appendChild(propertyElement);

			filterElement.appendChild(propertiesElement);
			routerElement.appendChild(filterElement);
			inboundRouterElement.appendChild(routerElement);

			return inboundRouterElement;
		} catch (Exception e) {
			throw new ConfigurationBuilderException(e);
		}
	}
	
	private Element getOutboundRouter(Document document, Element configurationElement, Channel channel) throws ConfigurationBuilderException {
		try {
			Element outboundRouterElement = document.createElement("outbound-router");
			Element routerElement = document.createElement("router");
			routerElement.setAttribute("className", "org.mule.routing.outbound.MulticastingRouter");

			int connectorIndex = 0;
			
			for (Iterator iter = channel.getDestinationConnectors().iterator(); iter.hasNext();) {
				Connector connector = (Connector) iter.next();
				Element endpointElement = document.createElement("endpoint");
				endpointElement.setAttribute("address", connector.getProperties().getProperty("address"));
				
				String connectorReference = String.valueOf("channel_" + channel.getId()) + "_destination_" + String.valueOf(connectorIndex);
				
				// add the destination connector
				addConnector(document, configurationElement, connector, connectorReference);
				endpointElement.setAttribute("connector", connectorReference);
				
				// add the transformer for this destination connector
				addTransformer(document, configurationElement, connector, connectorReference);
				endpointElement.setAttribute("transformer", connectorReference);
				
				routerElement.appendChild(endpointElement);
				connectorIndex++;
			}

			outboundRouterElement.appendChild(routerElement);
			return outboundRouterElement;
		} catch (Exception e) {
			throw new ConfigurationBuilderException(e);
		}
	}
	
	private void addTransformer(Document document, Element configurationElement, Connector connector, String name) throws ConfigurationBuilderException {
		try {
			Element transformersElement = (Element) configurationElement.getElementsByTagName("transformers").item(0);
			Element transformerElement = document.createElement("transformer");
			transformerElement.setAttribute("name", name);
			transformerElement.setAttribute("className", "com.webreach.mirth.mule.transformers.Transformer");
			
			// create a new properties object for storing the transformer properties
			PropertiesMarshaller marshaller = new PropertiesMarshaller();
			Properties properties = new Properties();
			properties.put("script", connector.getTransformer().getScript());
			properties.put("language", connector.getTransformer().getLanguage().toString());
			properties.put("type", connector.getTransformer().getType().toString());
			transformerElement.appendChild(document.importNode(marshaller.marshal(properties).getDocumentElement(), true));
			
			transformersElement.appendChild(transformerElement);
		} catch (Exception e) {
			throw new ConfigurationBuilderException(e);
		}
	}

	private void addConnector(Document document, Element configurationElement, Connector connector, String name) throws ConfigurationBuilderException {
		try {
			PropertiesMarshaller marshaller = new PropertiesMarshaller();
			// get the transport associated with this class from the transport map
			Transport transport = transports.get(connector.getTransportName());
			Element connectorElement = document.createElement("connector");
			connectorElement.setAttribute("className", transport.getClassName());
			connectorElement.setAttribute("name", name);
			
			// TODO: create a ConnectorProperty object that has
			// name, value, and isMuleProperty attribute
			// then only add the properties that have isMuleProperty set to true
			
			// TODO: handle the case where a queries map is added
			connectorElement.appendChild(document.importNode(marshaller.marshal(connector.getProperties()).getDocumentElement(), true));
			configurationElement.appendChild(connectorElement);
		} catch (Exception e) {
			throw new ConfigurationBuilderException(e);
		}
	}
}
