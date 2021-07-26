package com.mirth.connect.model.converters;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class DocumentSerializerTest {
	private static DocumentSerializer serializer;
	private static String dummyXml;
	
	@BeforeClass
	public static void setupClass() throws Exception {
		serializer = new DocumentSerializer();
		dummyXml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\r\n"
				+ "<!DOCTYPE foo [\r\n"
				+ "<!ELEMENT foo ANY >\r\n"
				+ "<!ENTITY xxe SYSTEM \"file:///dev/random\" >]><foo>&xxe;</foo>";
	}
	
	@Test
	public void testGetSecureTransformerFactory() throws Exception {
		DocumentBuilderFactory trfactory = DocumentSerializer.getSecureTransformerFactory();
		boolean doctypeFlag = trfactory.getFeature("http://apache.org/xml/features/disallow-doctype-decl");
		assertNotNull(trfactory);
		assertTrue(doctypeFlag);
    	boolean flag = trfactory.isExpandEntityReferences();
    	assertTrue(flag);
	}

	
	@Test
	public void testFromXML() {
		Document doc = serializer.fromXML(dummyXml);
		assertNull(doc);
	}
	
	@Test
	public void testToXMLWithExternalDTD() throws Exception {
		String xml = FileUtils.readFileToString(new File("tests/test-xxe-example.xml"), "UTF-8");

		// The above code should produce empty or null output due to encountering the external DTD
		boolean exceptionCaught = false;
		try {
			Document document = DocumentSerializer.getSecureTransformerFactory().newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
			serializer.toXML(document);
		}catch(Exception e) {
			exceptionCaught = true;
		}
		assertTrue(exceptionCaught);
	}
	
	@Test
	public void testFromXMLWithExternalDTD() throws Exception {
		String xml = FileUtils.readFileToString(new File("tests/test-xxe-example.xml"), "UTF-8");
		Document document = serializer.fromXML(xml);
		
		// The above code should produce empty or null output due to encountering the external DTD
		assertNull(document);
	}
	
	@Test
	public void testValidFromXML() throws Exception {
		String xml = FileUtils.readFileToString(new File("tests/test-xxe-example-valid.xml"), "UTF-8");
		Document document = serializer.fromXML(xml);
		
		// The above code should produce empty or null output due to encountering the external DTD
		assertNotNull(document);
	}
	
	// Util method for debugging purposes
	@SuppressWarnings("unused")
	private static void printDocument(Document doc) throws IOException, TransformerException {
	    TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer = tf.newTransformer();
	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

	    transformer.transform(new DOMSource(doc), 
	         new StreamResult(new OutputStreamWriter(System.out, "UTF-8")));
	}	
}