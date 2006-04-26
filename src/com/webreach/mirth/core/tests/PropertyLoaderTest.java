package com.webreach.mirth.core.tests;

import java.util.Properties;

import com.webreach.mirth.core.util.PropertyLoader;

import junit.framework.TestCase;

public class PropertyLoaderTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testLoadProperties() {
		Properties properties = PropertyLoader.loadProperties("mirth");
		
		assertEquals(properties.getProperty("configuration.id"), "MirthConfiguration");
		assertEquals(properties.getProperty("jmx.url"), "service:jmx:rmi:///jndi/rmi://localhost:1099/server");
	}

}
