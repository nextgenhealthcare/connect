/*
 * responseAck.java
 *
 * Created on 14 de noviembre de 2006, 22:53
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.connectors.mllp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.webreach.mirth.model.converters.ER7Serializer;

/**
 * 
 * @author ast
 */
public class ResponseAck {
	String ackMessageString = "";
	String errorMessage = null;
	boolean responseType = false;
	String xmlAck = null;
	/**
	 * logger used by this class
	 */
	protected static transient Log logger = LogFactory.getLog(MllpMessageDispatcher.class);

	/** Creates a new instance of responseAck */
	public ResponseAck(String ackMessageString) {
        // for demo purposes, we just declare a literal message string
        this.ackMessageString = ackMessageString;
        ER7Serializer serializer = new ER7Serializer();
       
        try {        
            xmlAck = serializer.toXML(ackMessageString);            
            logger.debug("ACK: "+ xmlAck);            
        } catch (Exception e) {
                errorMessage=" Message is not a valid ACK";
                errorMessage+="\n"+e+"\n"+ackMessageString;
        }          
    }
	private String getXMLValue(String source, String startTag, String endTag) {
		String returnValue = "";
		int startLoc = -1;
		if ((startLoc = source.indexOf(startTag)) != -1) {
			returnValue = source.substring(startLoc + startTag.length(), source.indexOf(endTag, startLoc));
		}
		return returnValue;
	}
	public boolean getTypeOfAck() {
		if (xmlAck == null) {
			return false;
		}
		try {

			String msa1 = getXMLValue(xmlAck, "<MSA.1.1>", "</MSA.1.1>");
			if (msa1.length() == 0){
				errorMessage = " Message is not a valid ACK";
				return false;
			}
			String msa3 = getXMLValue(xmlAck, "<MSA.3.1>", "</MSA.3.1>");

			String errorSegment = getXMLValue(xmlAck, "<ERR.1.1>", "</ERR.1.1>");
	
			if (msa1.equals("AA") || msa1.equals("CA")) {
				responseType = true;
				errorMessage = "";
			} else if (msa1.equals("AR") || msa1.equals("CR")) {
				responseType = false;
				errorMessage = " [Application Reject]" + "\n" + msa3 + "\n" + errorSegment;
			} else if (msa1.equals("AE") || msa1.equals("CE")) {
				responseType = false;
				errorMessage = " [Application Error]" + "\n" + msa3 + "\n" + errorSegment;
			} else {
				responseType = false;
				errorMessage = "Unknown response type" + "\n" + msa3 + "\n" + errorSegment + "\n\n" + ackMessageString;
			}
		} catch (Throwable t) {
			errorMessage = "Exception reviewing the ACK\n" + t;
			responseType = false;
		}
		return responseType;
	}

	public String getErrorDescription() {
		if (errorMessage == null)
			getTypeOfAck();
		return errorMessage;
	}
}