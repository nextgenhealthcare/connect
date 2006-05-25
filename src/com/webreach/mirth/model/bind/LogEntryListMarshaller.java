package com.webreach.mirth.model.bind;

import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.model.LogEntry;

public class LogEntryListMarshaller {
	private Logger logger = Logger.getLogger(LogEntryListMarshaller.class);
	
	/**
	 * Returns a Document representation of a List of log entries.
	 * 
	 * @param logEntryList
	 * @return
	 * @throws MarshalException
	 */
	public Document marshal(List<LogEntry> logEntryList) throws MarshalException {
		logger.debug("marshaling log entry list");
		
		try {
			LogEntryMarshaller marshaller = new LogEntryMarshaller();
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element logEntryListElement = document.createElement("log-entries");
			
			for (Iterator iter = logEntryList.iterator(); iter.hasNext();) {
				LogEntry logEntry = (LogEntry) iter.next();
				logEntryListElement.appendChild(document.importNode(marshaller.marshal(logEntry).getDocumentElement(), true));
			}
			
			document.appendChild(logEntryListElement);
			return document;
		} catch (Exception e) {
			throw new MarshalException(e);
		}
	}
}
