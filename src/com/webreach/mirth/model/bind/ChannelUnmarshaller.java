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


package com.webreach.mirth.model.bind;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.Filter;
import com.webreach.mirth.model.Transformer;
import com.webreach.mirth.model.Validator;

public class ChannelUnmarshaller {
	private Logger logger = Logger.getLogger(ChannelUnmarshaller.class);
	
	/**
	 * Returns a Channel object given a XML string representation.
	 * 
	 * @param source
	 * @return
	 * @throws UnmarshalException
	 */
	public Channel unmarshal(String source) throws UnmarshalException {
		logger.debug("unmarshalling channel from string");
		
		try {
			InputStream is = new ByteArrayInputStream(source.getBytes());
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			docBuilderFactory.setNamespaceAware(true);
			Document document = docBuilderFactory.newDocumentBuilder().parse(is);
			return unmarshal(document);
		} catch (UnmarshalException e) {
			throw e;
		} catch (Exception e) {
			throw new UnmarshalException("Could not parse source.", e);
		}
	}
	
	/**
	 * Returns a Channel object given a Document representation.
	 * 
	 * @param document
	 * @return
	 * @throws UnmarshalException
	 */
	public Channel unmarshal(Document document) throws UnmarshalException {
		logger.debug("unmarshalling channel from document");
		
		if ((document == null) || (!document.getDocumentElement().getTagName().equals("channel"))) {
			throw new UnmarshalException("Document is invalid.");
		}
		
		try {
			Channel channel = new Channel();
			
			// channel (root)
			Element channelElement = document.getDocumentElement();
			channel.setId(Integer.parseInt(channelElement.getAttribute("id")));
			channel.setName(channelElement.getAttribute("name"));
			channel.setDescription(channelElement.getAttribute("description"));
			
			// channel.enabled
			if (channelElement.getAttribute("enabled").equals("true")) {
				channel.setEnabled(true);
			} else {
				channel.setEnabled(false);
			}

			channel.setDirection(Channel.Direction.valueOf(channelElement.getAttribute("direction")));
			channel.setInitialStatus(Channel.Status.valueOf(channelElement.getAttribute("initial")));
			
			// source connector
			Element sourceConnectorElement = (Element) channelElement.getElementsByTagName("source").item(0);
			Connector sourceConnector = unmarshalConnector(sourceConnectorElement);
			channel.setSourceConnector(sourceConnector);
			
			// filter
			Element filterElement = (Element) channelElement.getElementsByTagName("filter").item(0);
			Filter filter = unmarshalFilter(filterElement);
			channel.setFilter(filter);
			
			// validator
			Element validatorElement = (Element) channelElement.getElementsByTagName("validator").item(0);
			Validator validator = unmarshalValidator(validatorElement);
			channel.setValidator(validator);
			
			// destination connectors
			Element destinationConnectorsElement = (Element) channelElement.getElementsByTagName("destinations").item(0);
			
			for (int i = 0; i < destinationConnectorsElement.getElementsByTagName("destination").getLength(); i++) {
				Element destinationConnectorElement = (Element) destinationConnectorsElement.getElementsByTagName("destination").item(i);
				Connector destinationConnector = unmarshalConnector(destinationConnectorElement);
				channel.getDestinationConnectors().add(destinationConnector);
			}
			
			// finalize the channel
			return channel;
		} catch (UnmarshalException e) {
			throw e;
		}
	}
	
	private Filter unmarshalFilter(Element filterElement) throws UnmarshalException {
		Filter filter = new Filter();
		filter.setScript(filterElement.getNodeValue());
		return filter;
	}
	
	private Validator unmarshalValidator(Element validatorElement) throws UnmarshalException {
		Validator validator = new Validator();
		
		for (int i = 0; i < validatorElement.getElementsByTagName("profile").getLength(); i++) {
			String key = validatorElement.getElementsByTagName("profile").item(i).getAttributes().getNamedItem("name").getNodeValue();
			String value = validatorElement.getElementsByTagName("profile").item(i).getNodeValue();
			validator.getProfiles().put(key, value);
		}
		
		return validator;
	}
	
	private Connector unmarshalConnector(Element connectorElement) throws UnmarshalException {
		Connector connector = new Connector();
		connector.setName(connectorElement.getAttributes().getNamedItem("name").getNodeValue());
		connector.setTransport(connectorElement.getAttributes().getNamedItem("transport").getNodeValue());

		// properties
		for (int i = 0; i < connectorElement.getElementsByTagName("property").getLength(); i++) {
			String key = connectorElement.getElementsByTagName("property").item(i).getAttributes().getNamedItem("name").getNodeValue();
			String value = connectorElement.getElementsByTagName("property").item(i).getAttributes().getNamedItem("value").getNodeValue();
			connector.getProperties().put(key, value);
		}

		// transformer
		Element transformerElement = (Element) connectorElement.getElementsByTagName("transformer").item(0);
		Transformer transformer = new Transformer();
		transformer.setType(Transformer.Type.valueOf(transformerElement.getAttribute("type")));
		transformer.setLanguage(Transformer.Language.valueOf(transformerElement.getAttribute("language")));
		
		// transformer.variables
		for (int i = 0; i < transformerElement.getElementsByTagName("variable").getLength(); i++) {
			String key = transformerElement.getElementsByTagName("variable").item(i).getAttributes().getNamedItem("name").getNodeValue();
			String value = transformerElement.getElementsByTagName("variable").item(i).getNodeValue();
			transformer.getVariables().put(key, value);
		}
		
		connector.setTransformer(transformer);
		
		return connector;
	}
}
