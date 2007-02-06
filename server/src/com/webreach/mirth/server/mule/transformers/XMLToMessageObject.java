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

import org.mule.transformers.AbstractTransformer;
import org.mule.umo.transformer.TransformerException;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.controllers.ChannelController;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.util.UUIDGenerator;

public class XMLToMessageObject extends AbstractTransformer {
	@Override
	public Object doTransform(Object src) throws TransformerException {
		String rawData = (String) src;
		MessageObject messageObject = new MessageObject();
		messageObject.setId(UUIDGenerator.getUUID());
		messageObject.setRawData(rawData);
		messageObject.setRawDataProtocol(MessageObject.Protocol.HL7V3);
		messageObject.setTransformedDataProtocol(MessageObject.Protocol.HL7V3);
		messageObject.setEncodedDataProtocol(MessageObject.Protocol.HL7V3);
		messageObject.setDateCreated(Calendar.getInstance());
		messageObject.setStatus(MessageObject.Status.RECEIVED);
		messageObject.setType("XML");
		messageObject.setChannelId(this.getEndpoint().getConnector().getName().substring(0, this.getEndpoint().getConnector().getName().indexOf('_')));
		messageObject.setConnectorName("Source");
		
		new MessageObjectController().updateMessage(messageObject);
		return messageObject;
	}
}
