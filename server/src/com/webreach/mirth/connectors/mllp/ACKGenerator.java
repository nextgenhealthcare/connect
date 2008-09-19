package com.webreach.mirth.connectors.mllp;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.webreach.mirth.server.util.DateUtil;

//Supports ACKS from 2.1-2.4
//2.5 is supported but the advanced fields in ERR and SFT are not supported
public class ACKGenerator {
	private final String DEFAULTDATEFORMAT = "yyyyMMddHHmmss";
	private Logger logger = Logger.getLogger(this.getClass());

	public String generateAckResponse(String message, String acknowledgementCode, String textMessage) throws Exception {
		return generateAckResponse(message, acknowledgementCode, textMessage, DEFAULTDATEFORMAT, new String());
	}

	public String generateAckResponse(String message, String acknowledgementCode, String textMessage, String dateFormat, String errorMessage) throws Exception {
		if (message == null || message.length() < 9) {
			logger.error("Unable to parse, message is null or too short: " + message);
			throw new Exception("Unable to parse, message is null or too short: " + message);
		}

		char segmentDelim = '\r';
		char fieldDelim = message.charAt(3); // Usually |
		char elementDelim = message.charAt(4); // Usually ^
		StringBuilder ackBuilder = new StringBuilder();
		ackBuilder.append(message.substring(0, 9));
		String timestamp = DateUtil.getCurrentDate(dateFormat);
		if (textMessage != null && textMessage.length() > 0) {
			textMessage = fieldDelim + textMessage;
		} else {
			textMessage = new String();
		}
		if (errorMessage != null && errorMessage.length() > 0) {
			errorMessage = segmentDelim + "ERR" + fieldDelim + errorMessage;
		} else {
			errorMessage = new String();
		}

		// Handle single line messages without any segment delimiters
		int firstSegmentDelim = message.indexOf(String.valueOf(segmentDelim));
		String mshString;
		if (firstSegmentDelim != -1) {
			mshString = message.substring(0, firstSegmentDelim);
		} else {
			mshString = message;
		}

		Pattern fieldPattern = Pattern.compile(Pattern.quote(String.valueOf(fieldDelim)));
		Pattern elementPattern = Pattern.compile(Pattern.quote(String.valueOf(elementDelim)));

		String[] mshFields = fieldPattern.split(mshString);
		int mshFieldsLength = mshFields.length;

		String sendingApplication = ""; // MSH.3.1
		String sendingFacility = ""; // MSH.4.1
		String receivingApplication = ""; // MSH.5.1
		String receivingFacility = ""; // MSH.6.1
		String originalid = ""; // MSH.10.1
		String procid = ""; // MSH.11.1
		String procidmode = ""; // // MSH.11.2
		String version = ""; // MSH.12.1

		if (mshFieldsLength > 2) {
			sendingApplication = elementPattern.split(mshFields[2])[0]; // MSH.3.1
			
			if (mshFieldsLength > 3) {
				sendingFacility = elementPattern.split(mshFields[3])[0]; // MSH.4.1
				
				if (mshFieldsLength > 4) {
					receivingApplication = elementPattern.split(mshFields[4])[0]; // MSH.5.1
					
					if (mshFieldsLength > 5) {
						receivingFacility = elementPattern.split(mshFields[5])[0]; // MSH.6.1
						
						if (mshFieldsLength > 9) {
							originalid = elementPattern.split(mshFields[9])[0]; // MSH.10.1
							
							if (mshFieldsLength > 10) {
								String[] msh11 = elementPattern.split(mshFields[10]); // MSH.11
								procid = msh11[0]; // MSH.11.1
								
								if (msh11.length > 1) {
									procidmode = msh11[1]; // MSH.11.2
								}
								
								if (mshFieldsLength > 11) {
									version = elementPattern.split(mshFields[11])[0]; // MSH.12.1
								} 
							}
						}
					} 
				}
			} 
		}
		
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
		if (procidmode != null && procidmode.length() > 0) {
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
		ackBuilder.append(segmentDelim); // MIRTH-494
		// MSH|^~\\&|{sendapp}|{sendfac}|{recapp}|{recfac}|{timestamp}||ACK|{timestamp}|P|{version}\rMSA|{code}|{originalid}{textmessage}\r
		return ackBuilder.toString();
	}
}
