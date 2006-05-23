package com.webreach.mirth.model.bind;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.webreach.mirth.model.Transport;

public class TransportMapUnmarshaller {
	private Logger logger = Logger.getLogger(TransportMapUnmarshaller.class);

	/**
	 * Returns a Map of Transport objects given an XML string representation.
	 * 
	 * @param source
	 * @return
	 * @throws UnmarshalException
	 */
	public Map<String, Transport> unmarshal(String source) throws UnmarshalException {
		logger.debug("unmarshalling transport map from string");

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
	 * Returns a Map of Transport objects given a Document representation.
	 * 
	 * @param document
	 * @return
	 * @throws UnmarshalException
	 */
	public Map<String, Transport> unmarshal(Document document) throws UnmarshalException {
		logger.debug("unmarshalling transport map from document");
		
		if ((document == null) || (!document.getDocumentElement().getTagName().equals("transports"))) {
			throw new UnmarshalException("Document is invalid.");
		}
		
		try {
			Map<String, Transport> transportMap = new HashMap<String, Transport>();
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			TransportUnmarshaller unmarshaller = new TransportUnmarshaller();

			for (int i = 0; i < document.getElementsByTagName("transport").getLength(); i++) {
				Document transportDocument = docBuilderFactory.newDocumentBuilder().newDocument();
				transportDocument.appendChild(transportDocument.importNode(document.getElementsByTagName("transport").item(i), true));
				Transport transport = unmarshaller.unmarshal(transportDocument);
				transportMap.put(transport.getName(), transport);
			}
			
			return transportMap;
		} catch (Exception e) {
			throw new UnmarshalException(e);
		}
	}
}