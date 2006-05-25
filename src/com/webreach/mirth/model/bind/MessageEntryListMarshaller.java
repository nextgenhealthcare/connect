package com.webreach.mirth.model.bind;

import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.model.MessageEntry;

public class MessageEntryListMarshaller {
	private Logger logger = Logger.getLogger(MessageEntryListMarshaller.class);
	
	/**
	 * Returns a Document representation of a List of message entries.
	 * 
	 * @param messageEntryList
	 * @return
	 * @throws MarshalException
	 */
	public Document marshal(List<MessageEntry> messageEntryList) throws MarshalException {
		logger.debug("marshaling message entry list");
		
		try {
			MessageEntryMarshaller marshaller = new MessageEntryMarshaller();
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element messageEntryListElement = document.createElement("message-entries");
			
			for (Iterator iter = messageEntryList.iterator(); iter.hasNext();) {
				MessageEntry messageEntry = (MessageEntry) iter.next();
				messageEntryListElement.appendChild(document.importNode(marshaller.marshal(messageEntry).getDocumentElement(), true));
			}
			
			document.appendChild(messageEntryListElement);
			return document;
		} catch (Exception e) {
			throw new MarshalException(e);
		}
	}
}
