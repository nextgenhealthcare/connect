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

import java.util.Calendar;

import org.apache.log4j.Logger;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.ER7Serializer;
import com.webreach.mirth.model.converters.HAPIMessageSerializer;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.util.StackTracePrinter;
import com.webreach.mirth.server.util.UUIDGenerator;

public class HL7ToMessageObject extends AbstractTransformer {
	private Logger logger = Logger.getLogger(this.getClass());
	private ER7Serializer xmlSerializer = new ER7Serializer();
	private HAPIMessageSerializer hapiSerializer = new HAPIMessageSerializer();
	private MessageObjectController messageObjectController = new MessageObjectController();

	public HL7ToMessageObject() {
		super();
		registerSourceType(String.class);
		setReturnClass(MessageObject.class);
	}

	@Override
	public Object doTransform(Object src) throws TransformerException {
		// trim removes any uneeded whitespace at the beginning and end of the
		// message
		String rawData = ((String) src).trim();
		MessageObject messageObject = new MessageObject();
		messageObject.setRawData(rawData);
		messageObject.setRawDataProtocol(MessageObject.Protocol.HL7);
		messageObject.setId(UUIDGenerator.getUUID());
		messageObject.setDateCreated(Calendar.getInstance());
		messageObject.setChannelId(this.getEndpoint().getConnector().getName().substring(0, this.getEndpoint().getConnector().getName().indexOf('_')));
		messageObject.setConnectorName("Source");
		messageObject.setTransformedDataProtocol(MessageObject.Protocol.XML);
		messageObject.setEncodedDataProtocol(MessageObject.Protocol.HL7);
		com.webreach.mirth.model.Channel channel = ChannelController.getChannelCache().get(messageObject.getChannelId());

		try {
			if (channel.getProperties().getProperty("recv_xml_encoded") != null && channel.getProperties().getProperty("recv_xml_encoded").equalsIgnoreCase("true")){
				messageObject.setSource(new String());
				messageObject.setType(new String());
				messageObject.setVersion(new String());
				messageObject.setTransformedData(rawData);
			}else{
				Message message = hapiSerializer.deserialize(rawData.replaceAll("\n", "\r"));
				Terser terser = new Terser(message);
				String sendingFacility = terser.get("/MSH-4-1");
				String event = terser.get("/MSH-9-1") + "-" + terser.get("/MSH-9-2");
				messageObject.setSource(sendingFacility);
				messageObject.setType(event);
				messageObject.setVersion(message.getVersion());
				messageObject.setTransformedData(xmlSerializer.toXML(rawData.replaceAll("\n", "\r")));
			}
		} catch (Exception e) {
			logger.warn("error transforming message", e);
			messageObject.setErrors(messageObject.getErrors() != null ? messageObject.getErrors() + '\n' : "" + StackTracePrinter.stackTraceToString(e));
			messageObject.setStatus(MessageObject.Status.ERROR);
			messageObjectController.updateMessage(messageObject);
			throw new TransformerException(this, e);
		}
		
		messageObject.setStatus(MessageObject.Status.RECEIVED);
		messageObjectController.updateMessage(messageObject);
		return messageObject;
	}
}
