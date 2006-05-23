package com.webreach.mirth.model.bind;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.model.Transport;

public class TransportMapMarshaller {
	private Logger logger = Logger.getLogger(TransportMapMarshaller.class);
	
	/**
	 * Returns a Document representation of a Map of Transport objects.
	 * 
	 * @param transportMap
	 * @return
	 * @throws MarshalException
	 */
	public Document marshal(Map<String, Transport> transportMap) throws MarshalException {
		logger.debug("marshalling transport list");
		
		try {
			TransportMarshaller transportMarshaller = new TransportMarshaller();
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element transportListElement = document.createElement("transports");
			
			for (Iterator iter = transportMap.entrySet().iterator(); iter.hasNext();) {
				Entry entry = (Entry) iter.next();
				Transport transport = (Transport) entry.getValue();
				transportListElement.appendChild(document.importNode(transportMarshaller.marshal(transport).getDocumentElement(), true));
			}
			
			document.appendChild(transportListElement);
			return document;
		} catch (Exception e) {
			throw new MarshalException(e);
		}
	}
}
