package com.mirth.connect.donkey.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import com.mirth.connect.donkey.util.DonkeyElement.DonkeyElementException;

public class DonkeyElementTest {
	
	@Test
	public void testStringXmlConstructor() {
		try {
			DonkeyElement element = new DonkeyElement(validXml);
			assertEquals("bar", element.getChildElement("bar").getTextContent());
		} catch (DonkeyElementException e) {
			fail("Failed to construct DonkeyElement with valid XML. Exception: " + e.getMessage());
		}
	}
	
	@Test
	public void testStringXmlConstructorWithExternalDtd() {
		boolean exceptionCaught = false;
		try {
			new DonkeyElement(xmlWithExternalDtd);
		} catch (DonkeyElementException e) {
			assertEquals(XmlPullParserException.class, e.getCause().getClass());
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
