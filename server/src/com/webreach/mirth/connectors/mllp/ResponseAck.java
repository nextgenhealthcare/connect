package com.webreach.mirth.connectors.mllp;

import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.webreach.mirth.model.converters.SerializerFactory;

public class ResponseAck {
	String ackMessageString = "";
	String errorMessage = null;
	boolean responseType = false;
	String er7Ack = null;
	
	/**
	 * logger used by this class
	 */
	protected static transient Log logger = LogFactory.getLog(MllpMessageDispatcher.class);

	/** Creates a new instance of responseAck */
	public ResponseAck(String ackMessageString) {
        // for demo purposes, we just declare a literal message string
		this.ackMessageString = ackMessageString;
       
        try {
    		// If XML is being sent over MLLP, use the strict parser
    		// to convert the ack to ER7, then find the ack type.
        	if (ackMessageString.charAt(0) == '<') {
        		er7Ack = SerializerFactory.getHL7Serializer(true, false, false).fromXML(ackMessageString);
        	} else {
        		er7Ack = ackMessageString;        		
        	}
            logger.debug("ACK: "+ er7Ack);            
        } catch (Exception e) {
        	errorMessage = " Message is not a valid ACK";
        	errorMessage += "\n" + e + "\n" + ackMessageString;
        }          
    }

	public boolean getTypeOfAck() {
		if (er7Ack == null) {
			return false;
		}
		
		char segmentDelim = '\r';
		char fieldDelim = er7Ack.charAt(3); // Usually |
		
		Pattern segmentPattern = Pattern.compile(Pattern.quote(String.valueOf(segmentDelim)));
		Pattern fieldPattern = Pattern.compile(Pattern.quote(String.valueOf(fieldDelim)));

		String msa1 = "";
		String msa3 = "";
		String err1 = "";
		String[] segments = segmentPattern.split(er7Ack);
		for (String segment : segments) {
			String[] fields = fieldPattern.split(segment);
			
			if (fields[0].equals("MSA")) {
				if (fields.length > 1) {
					msa1 = fields[1];
				}
				if (fields.length > 3) {
					msa3 = fields[3];
				}
			} else if (fields[0].equals("ERR")) {
				if (fields.length > 1) {
					err1 = fields[1];
				}
			}
		}
		
		if (msa1.equals("AA") || msa1.equals("CA")) {
			responseType = true;
			errorMessage = "";
		} else if (msa1.equals("AR") || msa1.equals("CR")) {
			responseType = false;
			errorMessage = " [Application Reject]" + "\n" + msa3 + "\n" + err1;
		} else if (msa1.equals("AE") || msa1.equals("CE")) {
			responseType = false;
			errorMessage = " [Application Error]" + "\n" + msa3 + "\n" + err1;
		} else {
			responseType = false;
			errorMessage = "Unknown response type (" + msa1 + ") \n" + msa3 + "\n" + err1 + "\n\n" + ackMessageString;
		}
		
		return responseType;
	}

	public String getErrorDescription() {
		if (errorMessage == null)
			getTypeOfAck();
		return errorMessage;
	}
}