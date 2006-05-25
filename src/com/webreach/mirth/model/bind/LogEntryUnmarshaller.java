package com.webreach.mirth.model.bind;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Timestamp;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.model.LogEntry;

public class LogEntryUnmarshaller {
	private Logger logger = Logger.getLogger(LogEntryUnmarshaller.class);

	/**
	 * Returns a LogEntry object given an XML string representation.
	 * 
	 * @param source
	 * @return
	 * @throws UnmarshalException
	 */
	public LogEntry unmarshal(String source) throws UnmarshalException {
		logger.debug("unmarshalling log entry from string");

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
	public LogEntry unmarshal(Document document) throws UnmarshalException {
		logger.debug("unmarshalling log entry from document");
		
		if ((document == null) || (!document.getDocumentElement().getTagName().equals("log-entry"))) {
			throw new UnmarshalException("Document is invalid.");
		}

		LogEntry logEntry = new LogEntry();
		Element logEntryElement = document.getDocumentElement();
		logEntry.setId(Integer.valueOf(logEntryElement.getAttribute("id")).intValue());
		logEntry.setChannelId(Integer.valueOf(logEntryElement.getAttribute("channelId")).intValue());
		logEntry.setEvent(logEntryElement.getAttribute("event"));
		logEntry.setDate(Timestamp.valueOf(logEntryElement.getAttribute("date")));
		logEntry.setLevel(Integer.valueOf(logEntryElement.getAttribute("level")).intValue());
		return logEntry;
	}
}