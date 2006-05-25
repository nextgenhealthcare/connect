package com.webreach.mirth.model.bind;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.webreach.mirth.model.LogEntry;

public class LogEntryListUnmarshaller {
	private Logger logger = Logger.getLogger(LogEntryListUnmarshaller.class);

	/**
	 * Returns a List of LogEntry objects given a XML string representation.
	 * 
	 * @param source
	 * @return
	 * @throws UnmarshalException
	 */
	public List<LogEntry> unmarshal(String source) throws UnmarshalException {
		logger.debug("unmarshalling log entry list from string");

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
	 * Returns a List of LogEntry objects given a Document representation.
	 * 
	 * @param document
	 * @return
	 * @throws UnmarshalException
	 */
	public List<LogEntry> unmarshal(Document document) throws UnmarshalException {
		logger.debug("unmarshalling log entry list from document");
		
		if ((document == null) || (!document.getDocumentElement().getTagName().equals("log-entries"))) {
			throw new UnmarshalException("Document is invalid.");
		}
		
		try {
			List<LogEntry> logEntryList = new ArrayList<LogEntry>();
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			LogEntryUnmarshaller unmarshaller = new LogEntryUnmarshaller();

			for (int i = 0; i < document.getElementsByTagName("log-entry").getLength(); i++) {
				Document messageEntryDocument = docBuilderFactory.newDocumentBuilder().newDocument();
				messageEntryDocument.appendChild(messageEntryDocument.importNode(document.getElementsByTagName("log-entry").item(i), true));
				logEntryList.add(unmarshaller.unmarshal(messageEntryDocument));
			}
			
			return logEntryList;
		} catch (Exception e) {
			throw new UnmarshalException(e);
		}
	}
}