package com.webreach.mirth.model.bind;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.webreach.mirth.model.MessageEntry;

public class MessageEntryListUnmarshaller {
	private Logger logger = Logger.getLogger(MessageEntryListUnmarshaller.class);

	/**
	 * Returns a List of MessageEntry objects given a XML string representation.
	 * 
	 * @param source
	 * @return
	 * @throws UnmarshalException
	 */
	public List<MessageEntry> unmarshal(String source) throws UnmarshalException {
		logger.debug("unmarshalling message entry list from string");

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
	 * Returns a List of MessageEntry objects given a Document representation.
	 * 
	 * @param document
	 * @return
	 * @throws UnmarshalException
	 */
	public List<MessageEntry> unmarshal(Document document) throws UnmarshalException {
		logger.debug("unmarshalling message entry list from document");
		
		if ((document == null) || (!document.getDocumentElement().getTagName().equals("message-entries"))) {
			throw new UnmarshalException("Document is invalid.");
		}
		
		try {
			List<MessageEntry> messageEntryList = new ArrayList<MessageEntry>();
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			MessageEntryUnmarshaller unmarshaller = new MessageEntryUnmarshaller();

			for (int i = 0; i < document.getElementsByTagName("message-entry").getLength(); i++) {
				Document messageEntryDocument = docBuilderFactory.newDocumentBuilder().newDocument();
				messageEntryDocument.appendChild(messageEntryDocument.importNode(document.getElementsByTagName("message-entry").item(i), true));
				messageEntryList.add(unmarshaller.unmarshal(messageEntryDocument));
			}
			
			return messageEntryList;
		} catch (Exception e) {
			throw new UnmarshalException(e);
		}
	}
}