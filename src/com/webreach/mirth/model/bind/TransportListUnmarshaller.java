package com.webreach.mirth.model.bind;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.webreach.mirth.model.Transport;
import com.webreach.mirth.model.User;

public class TransportListUnmarshaller {
	private Logger logger = Logger.getLogger(TransportListUnmarshaller.class);

	/**
	 * Returns a List of Transport objects given an XML string representation.
	 * 
	 * @param source
	 * @return
	 * @throws UnmarshalException
	 */
	public List<Transport> unmarshal(String source) throws UnmarshalException {
		logger.debug("unmarshalling transport list from string");

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
	 * Returns a List of Transport objects given a Document representation.
	 * 
	 * @param document
	 * @return
	 * @throws UnmarshalException
	 */
	public List<Transport> unmarshal(Document document) throws UnmarshalException {
		logger.debug("unmarshalling transport list from document");
		
		if ((document == null) || (!document.getDocumentElement().getTagName().equals("transports"))) {
			throw new UnmarshalException("Document is invalid.");
		}
		
		try {
			List<Transport> transportList = new ArrayList<Transport>();
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			TransportUnmarshaller unmarshaller = new TransportUnmarshaller();

			for (int i = 0; i < document.getElementsByTagName("transport").getLength(); i++) {
				Document transportDocument = docBuilderFactory.newDocumentBuilder().newDocument();
				transportDocument.appendChild(transportDocument.importNode(document.getElementsByTagName("transport").item(i), false));
				transportList.add(unmarshaller.unmarshal(transportDocument));
			}
			
			return transportList;
		} catch (Exception e) {
			throw new UnmarshalException(e);
		}
	}
}