package com.webreach.mirth.model.bind;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.model.Transport;

public class TransportMarshaller {
	private Logger logger = Logger.getLogger(TransportMarshaller.class);
	
	/**
	 * Returns a Document representation of a Transport object.
	 * 
	 * @param transport
	 * @return
	 * @throws MarshalException
	 */
	public Document marshal(Transport transport) throws MarshalException {
		logger.debug("marshalling transport: " + transport.toString());
		
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			
			Element transportElement = document.createElement("transport");
			transportElement.setAttribute("name", transport.getName());
			transportElement.setAttribute("displayName", transport.getDisplayName());
			transportElement.setAttribute("className", transport.getProtocol());
			transportElement.setAttribute("protocol", transport.getProtocol());
			transportElement.setAttribute("transformers", transport.getTransformers());
			
			document.appendChild(transportElement);
			
			return document;
		} catch (Exception e) {
			throw new MarshalException(e);
		}
	}
}
