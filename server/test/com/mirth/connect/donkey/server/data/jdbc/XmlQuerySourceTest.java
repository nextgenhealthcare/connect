package com.mirth.connect.donkey.server.data.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXParseException;

import com.mirth.connect.donkey.server.data.jdbc.XmlQuerySource.XmlQuerySourceException;

public class XmlQuerySourceTest {
	
	private XmlQuerySource querySource;
	
	@Before
	public void setup() {
		querySource = new XmlQuerySource();
	}
	
	@Test
	public void testLoad() {
		try {
			querySource.load("tests/test-xxe-query-example-valid.xml");
		} catch (XmlQuerySourceException e) {
			fail("Failed to load valid XML file. Exception: " + e.getMessage());
		}
		assertEquals("query1", querySource.getQuery("id1"));
	}
	
	@Test
	public void testLoadWithExternalDTD() {
		boolean exceptionCaught = false;
		try {
			querySource.load("tests/test-xxe-example.xml");
		} catch (XmlQuerySourceException e) {
			assertEquals(SAXParseException.class, e.getCause().getClass());
			exceptionCaught = true;
		}
		assertTrue(exceptionCaught);
	}
}
