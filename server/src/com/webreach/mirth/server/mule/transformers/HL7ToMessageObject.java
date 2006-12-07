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

import org.apache.log4j.Logger;
import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.ER7Serializer;
import com.webreach.mirth.model.converters.HAPIMessageSerializer;
import com.webreach.mirth.model.converters.SerializerException;
import com.webreach.mirth.server.util.StackTracePrinter;

public class HL7ToMessageObject extends AbstractTransformer {
	private Logger logger = Logger.getLogger(this.getClass());
	private ER7Serializer xmlSerializer = new ER7Serializer();
	private HAPIMessageSerializer hapiSerializer = new HAPIMessageSerializer();

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

		try {
			messageObject.setVersion(hapiSerializer.deserialize(rawData).getVersion());
			messageObject.setTransformedData(xmlSerializer.toXML(rawData));
		} catch (SerializerException e) {
			logger.warn("error transforming message", e);
			messageObject.setErrors(StackTracePrinter.stackTraceToString(e));
		}

		messageObject.setTransformedDataProtocol(MessageObject.Protocol.XML);
		messageObject.setEncodedDataProtocol(MessageObject.Protocol.HL7);
		messageObject.setStatus(MessageObject.Status.RECEIVED);
		return messageObject;
	}
}
