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
			JsonXmlUtil.xmlToJson(VALID_XML);
		} catch (Exception e) {
			fail("Failed to convert valid XML to JSON. Exception: " + e.getMessage());
		}
	}
	
	@Test
	public void testXmlToJsonWithExternalDTD() {
		boolean exceptionCaught = false;
		try {
			JsonXmlUtil.xmlToJson(XML_WITH_EXTERNAL_DTD);
		} catch (Exception e) {
			assertEquals(TransformerException.class, e.getClass());
			exceptionCaught = true;
		}
		assertTrue(exceptionCaught);
	}
	
	@Test
	public void testJsonToXml() throws Exception {
		assertEquals(EXPECTED_XML_FROM_JSON_1, JsonXmlUtil.jsonToXml(JSON_1));
	}
	
	@Test
	public void testJsonWithAttrsAtEndToXml() throws Exception {
		assertEquals(EXPECTED_XML_FROM_JSON_1, JsonXmlUtil.jsonToXml(JSON_1_WITH_ATTRS_AT_END));
	}
	
	private static String VALID_XML =  "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\r\n"
			+ "<foo><bar>bar</bar></foo>";
	
	private static String XML_WITH_EXTERNAL_DTD =  "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\r\n"
			+ "<!DOCTYPE foo [\r\n"
			+ "<!ELEMENT foo ANY >\r\n"
			+ "<!ENTITY xxe SYSTEM \"file:///dev/random\" >]><foo>&xxe;</foo>";
	
	private static String JSON_1 = "{\n" + 
			"	\"key1\": {\n" + 
			"		\"@attr1\": \"some attribute\",\n" + 
			"		\"@attr2\": \"some attribute 2\",\n" + 
			"		\"prop1\": \"some property\"\n" + 
			"	}\n" + 
			"}";
	
	private static String JSON_1_WITH_ATTRS_AT_END = "{\n" + 
			"	\"key1\": {\n" + 
			"		\"@attr1\": \"some attribute\",\n" + 
			"		\"prop1\": \"some property\"\n" + 
			"		\"@attr2\": \"some attribute 2\",\n" + 
			"	}\n" + 
			"}";
	
	private static String EXPECTED_XML_FROM_JSON_1 = "<?xml version='1.0' encoding='UTF-8'?><key1 attr1=\"some attribute\" attr2=\"some attribute 2\"><prop1>some property</prop1></key1>";
}
