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


package com.webreach.mirth.server.util;

import com.webreach.mirth.model.MessageObject.Protocol;

//Wrapper for the LLP ack generator
//Made so that ACKs can be generated from JS
public class ACKGenerator {
	private final String DEFAULTDATEFORMAT = "yyyyMMddHHmmss";
	
	/**
	 * This method defaults the protocol to HL7v2, along with the dateFormat to "yyyyMMddHHmmss" and the errorMessage to ""
	 */
	public String generateAckResponse(String message, String acknowledgementCode, String textMessage) throws Exception {
		return new com.webreach.mirth.connectors.mllp.ACKGenerator().generateAckResponse(message, Protocol.HL7V2, acknowledgementCode, textMessage, DEFAULTDATEFORMAT, new String());
	}
	public String generateAckResponse(String message, Protocol protocol, String acknowledgementCode, String textMessage, String dateFormat, String errorMessage) throws Exception{
		return new com.webreach.mirth.connectors.mllp.ACKGenerator().generateAckResponse(message, protocol, acknowledgementCode, textMessage, dateFormat, errorMessage);
	}
}
