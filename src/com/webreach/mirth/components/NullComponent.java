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


package com.webreach.mirth.components;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.umo.UMOEventContext;
import org.mule.umo.lifecycle.Callable;

import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.parser.GenericParser;

import com.webreach.mirth.managers.MessageManager;
import com.webreach.mirth.managers.types.MirthMessage;

public class NullComponent implements Callable {
	protected static transient Log logger = LogFactory.getLog(NullComponent.class);

	private MessageManager messageManager = MessageManager.getInstance();

	public Object onCall(UMOEventContext eventContext) throws Exception {
		GenericParser parser = new GenericParser();
		ca.uhn.hl7v2.model.Message msg = parser.parse(eventContext.getMessageAsString());
		Segment segment = (Segment) msg.get("MSH");
		System.out.println(">>>>>>>>>>>> " + segment.numFields());

		MirthMessage message = new MirthMessage();
		message.setContent(eventContext.getMessageAsString());
		message.setChannel(eventContext.getComponentDescriptor().getName());
		message.setSize(String.valueOf((eventContext.getTransformedMessageAsString().getBytes().length)));
//		message.setSource(((Type) segment.getField(3, 1)));
//		message.setMessageControlId(((Type) segment.getField(4, 1)));
		messageManager.addMessage(message);

		return eventContext.getTransformedMessage();
	}
}
