package com.webreach.mirth.model.bind;

import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.model.Transport;

public class TransportListMarshaller {
	private Logger logger = Logger.getLogger(TransportListMarshaller.class);
	
	/**
	 * Returns a Document representation of a List of Transport objects.
	 * 
	 * @param transportList
	 * @return
	 * @throws MarshalException
	 */
	public Document marshal(List<Transport> transportList) throws MarshalException {
		logger.debug("marshalling transport list");
		
		try {
			TransportMarshaller transportMarshaller = new TransportMarshaller();
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element transportListElement = document.createElement("transports");
			
			for (Iterator iter = transportList.iterator(); iter.hasNext();) {
				Transport transport = (Transport) iter.next();
				transportListElement.appendChild(document.importNode(transportMarshaller.marshal(transport).getDocumentElement(), false));
			}
			
			document.appendChild(transportListElement);
			return document;
		} catch (Exception e) {
			throw new MarshalException(e);
		}
	}
}
