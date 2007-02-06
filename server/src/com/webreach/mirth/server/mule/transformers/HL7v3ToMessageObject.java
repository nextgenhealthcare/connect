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


package com.webreach.mirth.server.mule.transformers;

import java.io.StringReader;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.controllers.ChannelController;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.util.StackTracePrinter;
import com.webreach.mirth.server.util.UUIDGenerator;

public class HL7v3ToMessageObject extends AbstractTransformer {
	private MessageObjectController messageObjectController = new MessageObjectController();

	@Override
	public Object doTransform(Object src) throws TransformerException {
		String rawData = (String) src;
		MessageObject messageObject = new MessageObject();
		messageObject.setId(UUIDGenerator.getUUID());
		messageObject.setRawData(rawData);
		messageObject.setRawDataProtocol(MessageObject.Protocol.HL7V3);
		messageObject.setTransformedDataProtocol(MessageObject.Protocol.XML);
		messageObject.setEncodedDataProtocol(MessageObject.Protocol.HL7V3);
		messageObject.setDateCreated(Calendar.getInstance());
		messageObject.setStatus(MessageObject.Status.RECEIVED);
		messageObject.setType("XML");
		messageObject.setChannelId(this.getEndpoint().getConnector().getName().substring(0, this.getEndpoint().getConnector().getName().indexOf('_')));
		messageObject.setConnectorName("Source");
    	DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();
			Document xmlDoc = docBuilder.parse(new InputSource(new StringReader(rawData)));
			messageObject.setSource(new String());
			messageObject.setType(xmlDoc.getDocumentElement().getNodeName());
			messageObject.setVersion("3.0");
		} catch (Exception e) {
			logger.warn("error transforming message", e);
			messageObject.setErrors(messageObject.getErrors() != null ? messageObject.getErrors() + '\n' : "" + StackTracePrinter.stackTraceToString(e));
			messageObject.setStatus(MessageObject.Status.ERROR);
			messageObjectController.updateMessage(messageObject);
			throw new TransformerException(this, e);
		}

		messageObject.setTransformedData(rawData);
		new MessageObjectController().updateMessage(messageObject);
		return messageObject;
	}
}
