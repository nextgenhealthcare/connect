/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

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
