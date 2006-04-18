package com.webreach.mirth.core.tests;

import com.webreach.mirth.core.Connector;
import com.webreach.mirth.core.Transformer;
import com.webreach.mirth.core.Transport;

import junit.framework.TestCase;

public class ConnectorTest extends TestCase {
	
	private Connector connector;

	protected void setUp() throws Exception {
		super.setUp();
		connector = new Connector();
		connector.setName("Test Connector");
		connector.setTransport(new Transport());
		connector.getProperties().put("test_property", "test_value");
		connector.setTransformer(new Transformer());
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
