package com.mirth.connect.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.xml.transform.TransformerException;

import org.junit.Test;

public class JsonXmlUtilTest {
	
	@Test
	public void testXmlToJson() {
		try {
			JsonXmlUtil.xmlToJson(validXml);
		} catch (Exception e) {
			fail("Failed to convert valid XML to JSON. Exception: " + e.getMessage());
		}
	}
	
	@Test
	public void testXmlToJsonWithExternalDTD() {
		boolean exceptionCaught = false;
		try {
			JsonXmlUtil.xmlToJson(xmlWithExternalDtd);
		} catch (Exception e) {
			assertEquals(TransformerException.class, e.getClass());
			exceptionCaught = true;
		}
		assertTrue(exceptionCaught);
	}
	
	private static String validXml =  "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\r\n"
			+ "<foo><bar>bar</bar></foo>";
	
	private static String xmlWithExternalDtd =  "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\r\n"
			+ "<!DOCTYPE foo [\r\n"
			+ "<!ELEMENT foo ANY >\r\n"
			+ "<!ENTITY xxe SYSTEM \"file:///dev/random\" >]><foo>&xxe;</foo>";
}
