package com.webreach.mirth.model.converters;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class X12ContentHandler extends DefaultHandler {
	
	public StringBuffer xmlMapping = new StringBuffer();
	public void startDocument() throws SAXException {
		xmlMapping.setLength(0);
	}
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		xmlMapping.append(ch, start, length);
	}
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		xmlMapping.append("<").append(localName).append(">");
	}
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		xmlMapping.append("</").append(localName).append(">");
	}
}
