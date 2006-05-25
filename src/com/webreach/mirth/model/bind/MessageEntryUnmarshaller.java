package com.webreach.mirth.model.bind;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Timestamp;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.model.MessageEntry;

public class MessageEntryUnmarshaller {
	private Logger logger = Logger.getLogger(MessageEntryUnmarshaller.class);

	/**
	 * Returns a MessageEntry object given an XML string representation.
	 * 
	 * @param source
	 * @return
	 * @throws UnmarshalException
	 */
	public MessageEntry unmarshal(String source) throws UnmarshalException {
		logger.debug("unmarshalling message entry from string");

		try {
			InputStream is = new ByteArrayInputStream(source.getBytes());
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			Document document = docBuilderFactory.newDocumentBuilder().parse(is);
			return unmarshal(document);
		} catch (UnmarshalException e) {
			throw e;
		} catch (Exception e) {
			throw new UnmarshalException("Could not parse source.", e);
		}
	}

	/**
	 * Returns a LogEntry object given a Document representation.
	 * 
	 * @param document
	 * @return
	 * @throws UnmarshalException
	 */
	public MessageEntry unmarshal(Document document) throws UnmarshalException {
		logger.debug("unmarshalling message entry from document");
		
		if ((document == null) || (!document.getDocumentElement().getTagName().equals("message-entry"))) {
			throw new UnmarshalException("Document is invalid.");
		}

		MessageEntry messageEntry = new MessageEntry();
		Element messageEntryElement = document.getDocumentElement();
		messageEntry.setId(Integer.valueOf(messageEntryElement.getAttribute("id")).intValue());
		messageEntry.setChannelId(Integer.valueOf(messageEntryElement.getAttribute("channelId")).intValue());
		messageEntry.setControlId(messageEntryElement.getAttribute("controlId"));
		messageEntry.setEvent(messageEntryElement.getAttribute("event"));
		messageEntry.setSendingFacility(messageEntryElement.getAttribute("sendingFacility"));
		messageEntry.setDate(Timestamp.valueOf(messageEntryElement.getAttribute("date")));
		messageEntry.setMessage(messageEntryElement.getElementsByTagName("message").item(0).getTextContent());
		return messageEntry;
	}
}