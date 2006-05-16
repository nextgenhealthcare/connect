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

import java.util.Iterator;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.Filter;
import com.webreach.mirth.model.Validator;

public class ChannelMarshaller {
	public static final String[] cDataElements = { "filter", "variable", "profile" };
	private Logger logger = Logger.getLogger(ChannelMarshaller.class);

	/**
	 * Returns a Document representation of a Channel.
	 * 
	 * @param channel
	 * @return
	 * @throws MarshalException
	 */
	public Document marshal(Channel channel) throws MarshalException {
		logger.debug("marshaling channel: " + channel.getName());

		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

			// channel (root)
			Element channelElement = document.createElement("channel");
			channelElement.setAttribute("id", String.valueOf(channel.getId()));
			channelElement.setAttribute("name", channel.getName());
			channelElement.setAttribute("description", channel.getDescription());

			// channel.enabled
			if (channel.isEnabled()) {
				channelElement.setAttribute("enabled", "true");
			} else {
				channelElement.setAttribute("enabled", "false");
			}

			channelElement.setAttribute("initial", channel.getInitialStatus().name());
			channelElement.setAttribute("direction", channel.getDirection().name());

			// source connector
			Element sourceConnectorElement = marshalConnector(document, "source", channel.getSourceConnector());
			channelElement.appendChild(sourceConnectorElement);

			// filter
			Element filterElement = marshalFilter(document, channel.getFilter());
			channelElement.appendChild(filterElement);

			// validator
			Element validatorElement = marshalValidator(document, channel.getValidator());
			channelElement.appendChild(validatorElement);

			// destination connector
			Element destinationsElement = document.createElement("destinations");

			for (Iterator iter = channel.getDestinationConnectors().iterator(); iter.hasNext();) {
				Connector destinationConnector = (Connector) iter.next();
				Element destinationConnectorElement = marshalConnector(document, "destination", destinationConnector);
				destinationsElement.appendChild(destinationConnectorElement);
			}

			channelElement.appendChild(destinationsElement);

			// finalize the document
			document.appendChild(channelElement);

			return document;
		} catch (MarshalException e) {
			throw e;
		} catch (Exception e) {
			throw new MarshalException(e);
		}
	}

	private Element marshalFilter(Document document, Filter filter) throws MarshalException {
		try {
			Element filterElement = document.createElement("filter");
			filterElement.setTextContent(filter.getScript());
			return filterElement;
		} catch (Exception e) {
			throw new MarshalException(e);
		}
	}

	private Element marshalValidator(Document document, Validator validator) throws MarshalException {
		try {
			Element validatorElement = document.createElement("validator");

			for (Iterator iter = validator.getProfiles().entrySet().iterator(); iter.hasNext();) {
				Entry profile = (Entry) iter.next();
				Element profileElement = document.createElement("profile");
				profileElement.setAttribute("name", profile.getKey().toString());
				profileElement.setTextContent(profile.getValue().toString());
				validatorElement.appendChild(profileElement);
			}

			return validatorElement;
		} catch (Exception e) {
			throw new MarshalException(e);
		}
	}

	private Element marshalConnector(Document document, String elementName, Connector connector) throws MarshalException {
		try {
			Element connectorElement = document.createElement(elementName);
			connectorElement.setAttribute("name", connector.getName());
			connectorElement.setAttribute("transport", connector.getTransport());

			// properties
			for (Iterator iter = connector.getProperties().entrySet().iterator(); iter.hasNext();) {
				Entry property = (Entry) iter.next();
				Element propertyElement = document.createElement("property");
				propertyElement.setAttribute("name", property.getKey().toString());
				propertyElement.setAttribute("value", property.getValue().toString());
				connectorElement.appendChild(propertyElement);
			}

			// transformer
			Element transformer = document.createElement("transformer");
			transformer.setAttribute("type", connector.getTransformer().getType().toString());
			transformer.setAttribute("language", connector.getTransformer().getLanguage().toString());

			// transformer.variables
			for (Iterator iter = connector.getTransformer().getVariables().entrySet().iterator(); iter.hasNext();) {
				Entry variable = (Entry) iter.next();
				Element scriptElement = document.createElement("variable");
				scriptElement.setAttribute("name", variable.getKey().toString());
				scriptElement.setTextContent(variable.getValue().toString());
				transformer.appendChild(scriptElement);
			}

			connectorElement.appendChild(transformer);

			return connectorElement;
		} catch (Exception e) {
			throw new MarshalException(e);
		}
	}
}
