package com.mirth.connect.plugins.datatypes.ncpdp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXParseException;

import com.mirth.connect.donkey.model.message.MessageSerializerException;
import com.mirth.connect.model.datatype.SerializerProperties;

public class NCPDPSerializerTest {

	private NCPDPSerializer serializer;
	
	@Before
	public void setup() {
		serializer = new NCPDPSerializer(new SerializerProperties(new NCPDPSerializationProperties(), new NCPDPDeserializationProperties(), null));
	}

	@Test
	public void testFromXML() {
		try {
			serializer.fromXML(validXml);
		} catch (MessageSerializerException e) {
			fail("Failed to parse valid XML. Exception: " + e.getMessage());
		}
	}
	
	@Test
	public void testFromXMLWithExternalDtd() {
		boolean exceptionCaught = false;
		try {
			serializer.fromXML(xmlWithExternalDtd);
		} catch (MessageSerializerException e) {
			assertEquals(SAXParseException.class, e.getCause().getClass());
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
