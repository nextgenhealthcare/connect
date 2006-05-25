package com.webreach.mirth.model.bind;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.model.MessageEntry;

public class MessageEntryMarshaller {
	public static final String[] cDataElements = null;
	private Logger logger = Logger.getLogger(MessageEntryMarshaller.class);
	
	/**
	 * Returns a Document representation of a MessageEntry object.
	 * 
	 * @param messageEntry
	 * @return
	 * @throws MarshalException
	 */
	public Document marshal(MessageEntry messageEntry) throws MarshalException {
		logger.debug("marshalling message entry");
		
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element messageEntryElement = document.createElement("message-entry");
			messageEntryElement.setAttribute("id", String.valueOf(messageEntry.getId()));
			messageEntryElement.setAttribute("channelId", String.valueOf(messageEntry.getChannelId()));
			messageEntryElement.setAttribute("controlId", messageEntry.getControlId());
			messageEntryElement.setAttribute("event", messageEntry.getEvent());
			messageEntryElement.setAttribute("date", messageEntry.getDate().toString());
			messageEntryElement.setAttribute("sendingFacility", messageEntry.getSendingFacility());
			Element messageElement = document.createElement("message");
			messageElement.setTextContent(messageEntry.getMessage());
			messageEntryElement.appendChild(messageElement);
			document.appendChild(messageEntryElement);
			return document;
		} catch (Exception e) {
			throw new MarshalException(e);
		}
	}
}
