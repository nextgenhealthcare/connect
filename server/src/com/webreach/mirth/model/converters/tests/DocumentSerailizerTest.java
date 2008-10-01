package com.webreach.mirth.model.converters.tests;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.webreach.mirth.model.converters.DocumentSerializer;

public class DocumentSerailizerTest {

	@Before
	public void setUp() throws Exception {}

	@After
	public void tearDown() throws Exception {}

	@Test
	public void testToXML() throws Exception {
		DocumentSerializer serializer = new DocumentSerializer();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = factory.newDocumentBuilder();
		Document document = parser.newDocument();
		
		Element element = document.createElement("root");
		element.setTextContent("Hello\r\nworld!");
		document.appendChild(element);
		
		String xml = serializer.toXML(document);
		System.out.println(xml);
	}

	@Test
	public void testFromXML() {
		
	}

}
