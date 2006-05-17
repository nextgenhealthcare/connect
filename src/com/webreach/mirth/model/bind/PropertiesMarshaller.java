package com.webreach.mirth.model.bind;

import java.util.Iterator;
import java.util.Properties;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PropertiesMarshaller {
	public static final String[] cDataElements = null;
	private Logger logger = Logger.getLogger(PropertiesMarshaller.class);
	
	/**
	 * Returns a Document representation of a Properties object.
	 * 
	 * @param transport
	 * @return
	 * @throws MarshalException
	 */
	public Document marshal(Properties properties) throws MarshalException {
		logger.debug("marshalling properties");
		
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element propertiesElement = document.createElement("properties");
			
			for (Iterator iter = properties.entrySet().iterator(); iter.hasNext();) {
				Entry entry = (Entry) iter.next();
				Element propertyElement = document.createElement("property");
				propertyElement.setAttribute("key", entry.getKey().toString());
				propertyElement.setAttribute("value", entry.getValue().toString());
				propertiesElement.appendChild(propertyElement);
			}
			
			document.appendChild(propertiesElement);
			return document;
		} catch (Exception e) {
			throw new MarshalException(e);
		}
	}
}
