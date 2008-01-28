package com.webreach.mirth.connectors.mllp;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.converters.SerializerFactory;
import com.webreach.mirth.server.util.DateUtil;
//Supports ACKS from 2.1-2.4
//2.5 is supported but the advanced fields in ERR and SFT are not supported
public class ACKGenerator {
	private final String DEFAULTDATEFORMAT = "yyyyMMddHHmmss";
	private Logger logger = Logger.getLogger(this.getClass());
	public String generateAckResponse(String message, String acknowledgementCode, String textMessage) throws Exception {
		return generateAckResponse(message, acknowledgementCode, textMessage, DEFAULTDATEFORMAT, new String());
	}
	
	public String generateAckResponse(String message, String acknowledgementCode, String textMessage, String dateFormat, String errorMessage) throws Exception{
		if (message == null || message.length() < 3) {
			logger.error("Unable to parse, message is null or too short: " + message);
			throw new Exception("Unable to parse, message is null or too short: " + message);
		}
		String segmentDelim = "\r";
		char fieldDelim = message.charAt(3); // Usually |
		char elementDelim = message.charAt(4); // Usually ^
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
		String procid = getXMLValue(originalXML, "<MSH.11.1>", "</MSH.11.1>");
		String procidmode = getXMLValue(originalXML, "<MSH.11.2>", "</MSH.11.2>");
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
		ackBuilder.append(procid);
		if (procidmode != null && procidmode.length() > 0 )
		{		
			ackBuilder.append(elementDelim);
			ackBuilder.append(procidmode);
		}
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
		ackBuilder.append(segmentDelim); //MIRTH-494
		//MSH|^~\\&|{sendapp}|{sendfac}|{recapp}|{recfac}|{timestamp}||ACK|{timestamp}|P|{version}\rMSA|{code}|{originalid}{textmessage}\r
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
