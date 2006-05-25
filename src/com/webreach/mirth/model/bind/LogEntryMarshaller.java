package com.webreach.mirth.model.bind;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.model.LogEntry;

public class LogEntryMarshaller {
	public static final String[] cDataElements = null;
	private Logger logger = Logger.getLogger(LogEntryMarshaller.class);
	
	/**
	 * Returns a Document representation of a LogEntry object.
	 * 
	 * @param logEntry
	 * @return
	 * @throws MarshalException
	 */
	public Document marshal(LogEntry logEntry) throws MarshalException {
		logger.debug("marshalling log entry");
		
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element logEntryElement = document.createElement("log-entry");
			logEntryElement.setAttribute("id", String.valueOf(logEntry.getId()));
			logEntryElement.setAttribute("channelId", String.valueOf(logEntry.getChannelId()));
			logEntryElement.setAttribute("event", logEntry.getEvent());
			logEntryElement.setAttribute("date", logEntry.getDate().toString());
			logEntryElement.setAttribute("level", String.valueOf(logEntry.getLevel()));
			document.appendChild(logEntryElement);
			return document;
		} catch (Exception e) {
			throw new MarshalException(e);
		}
	}
}
