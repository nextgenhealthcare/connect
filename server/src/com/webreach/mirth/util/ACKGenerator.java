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


package com.webreach.mirth.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.webreach.mirth.model.converters.ER7Serializer;
import com.webreach.mirth.model.converters.SerializerException;
import com.webreach.mirth.model.converters.SerializerFactory;
import com.webreach.mirth.server.util.DateUtil;
import com.webreach.mirth.server.util.FileUtil;

import ca.uhn.hl7v2.app.DefaultApplication;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.parser.PipeParser;
//Supports ACKS from 2.1-2.4
//2.5 is supported but the advanced fields in ERR and SFT are not supported
public class ACKGenerator {
	public static void main(String[] args) {
		try {
			String hl7 = FileUtil.read("c:\\orm.txt");
			String ack = new ACKGenerator().generateAckResponse(hl7, "AA", "");
			System.out.println(ack);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private final String DEFAULTDATEFORMAT = "yyyyMMddHHmmss";
	private Logger logger = Logger.getLogger(this.getClass());
	public String generateAckResponse(String message, String acknowledgementCode, String textMessage) throws Exception {
		return generateAckResponse(message, acknowledgementCode, textMessage, DEFAULTDATEFORMAT, new String());
	}
	
	private String generateAckResponse(String message, String acknowledgementCode, String textMessage, String dateFormat, String errorMessage) throws Exception{
		if (message == null || message.length() < 3) {
			logger.error("Unable to parse, message is null or too short: " + message);
			throw new Exception("Unable to parse, message is null or too short: " + message);
		}
		String segmentDelim = "\r";
		char fieldDelim = message.charAt(3); // Usually |
		StringBuilder ackBuilder = new StringBuilder();
		ackBuilder.append(message.substring(0, 9));
		String originalXML;
		originalXML = SerializerFactory.getHL7Serializer(false, false).toXML(message);			
	    String timestamp = DateUtil.getCurrentDate(dateFormat);
	    if (textMessage != null && textMessage.length() > 0){
	    	textMessage = "|" + textMessage;
	    }else{
	    	textMessage = new String();
	    }
	    if (errorMessage != null && errorMessage.length() > 0){
	    	errorMessage = segmentDelim + "ERR|" + errorMessage;
	    }else{
	    	errorMessage = new String();
	    }
	    String sendingApplication = getXMLValue(originalXML, "<MSH.3.1>", "</MSH.3.1>");
	    String sendingFacility = getXMLValue(originalXML, "<MSH.4.1>", "</MSH.4.1>");
	    String receivingApplication = getXMLValue(originalXML, "<MSH.5.1>", "</MSH.5.1>");
	    String receivingFacility = getXMLValue(originalXML, "<MSH.6.1>", "</MSH.6.1>");
	    String originalid = getXMLValue(originalXML, "<MSH.10.1>", "</MSH.10.1>");
		String version = getXMLValue(originalXML, "<MSH.12.1>", "</MSH.12.1>");
		ackBuilder.append(receivingApplication);
		ackBuilder.append(fieldDelim);
		ackBuilder.append(receivingFacility);
		ackBuilder.append(fieldDelim);
		ackBuilder.append(sendingApplication);
		ackBuilder.append(fieldDelim);
		ackBuilder.append(sendingFacility);
		ackBuilder.append(fieldDelim);
		ackBuilder.append(timestamp);
		ackBuilder.append(fieldDelim);
		ackBuilder.append(fieldDelim);
		ackBuilder.append("ACK");
		ackBuilder.append(fieldDelim);
		ackBuilder.append(timestamp);
		ackBuilder.append(fieldDelim);
		ackBuilder.append("P");
		ackBuilder.append(fieldDelim);
		ackBuilder.append(version);
		ackBuilder.append(segmentDelim);
		ackBuilder.append("MSA");
		ackBuilder.append(fieldDelim);
		ackBuilder.append(acknowledgementCode);
		ackBuilder.append(fieldDelim);
		ackBuilder.append(originalid);
		ackBuilder.append(textMessage);
		ackBuilder.append(errorMessage);
		//MSH|^~\\&|{sendapp}|{sendfac}|{recapp}|{recfac}|{timestamp}||ACK|{timestamp}|P|{version}\r MSA|{code}|{originalid}{textmessage}
		return ackBuilder.toString();
	}
	private String getXMLValue(String source, String startTag, String endTag) {
		String returnValue = "";
		int startLoc = -1;
		if ((startLoc = source.indexOf(startTag)) != -1) {
			returnValue = source.substring(startLoc + startTag.length(), source.indexOf(endTag, startLoc));
		}
		return returnValue;
	}
}
