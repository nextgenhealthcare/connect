package com.webreach.mirth.model.converters.tests;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.Assert;

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

		DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = parser.newDocument();
		
		Element element = document.createElement("root");
		element.setTextContent("Hello\r\nworld!");
		document.appendChild(element);
		
		String actual = serializer.toXML(document);
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root>Hello&#xd;\nworld!</root>\n";
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void testPreserveSpace() throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.newDocument();
        Element root = document.createElement("root");
        document.appendChild(root);
        Element child = document.createElement("child");
        child.setTextContent("Hello\nworld!");
        root.appendChild(child);
        
        DocumentSerializer serializer = new DocumentSerializer();
        String actual = serializer.toXML(document);
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<root>\n<child>Hello\nworld!</child>\n</root>\n";
        Assert.assertEquals(expected, actual);
	}

	@Test
	public void testFromXML() {
		
	}

}
