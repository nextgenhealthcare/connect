package com.webreach.mirth.model.bind;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PropertiesUnmarshaller {
	private Logger logger = Logger.getLogger(PropertiesUnmarshaller.class);

	/**
	 * Returns a Properties object given an XML string representation.
	 * 
	 * @param source
	 * @return
	 * @throws UnmarshalException
	 */
	public Properties unmarshal(String source) throws UnmarshalException {
		logger.debug("unmarshalling properties from string");

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
	 * Returns a Properties object given a Document representation.
	 * 
	 * @param document
	 * @return
	 * @throws UnmarshalException
	 */
	public Properties unmarshal(Document document) throws UnmarshalException {
		logger.debug("unmarshalling properties from document");
		
		if ((document == null) || (!document.getDocumentElement().getTagName().equals("properties"))) {
			throw new UnmarshalException("Document is invalid.");
		}

		Properties properties = new Properties();
		Element propertiesElement = document.getDocumentElement();
		
		for (int i = 0; i < propertiesElement.getElementsByTagName("property").getLength(); i++) {
			String key = propertiesElement.getElementsByTagName("property").item(i).getAttributes().getNamedItem("name").getNodeValue();
			String value = propertiesElement.getElementsByTagName("property").item(i).getAttributes().getNamedItem("value").getNodeValue();
			properties.put(key, value);
		}

		return properties;
	}
}