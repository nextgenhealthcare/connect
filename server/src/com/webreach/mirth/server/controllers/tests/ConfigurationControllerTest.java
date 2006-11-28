package com.webreach.mirth.server.controllers.tests;

import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.webreach.mirth.model.Transport;
import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.tools.ScriptRunner;

public class ConfigurationControllerTest extends TestCase {
	private ConfigurationController configurationController = new ConfigurationController();

	protected void setUp() throws Exception {
		super.setUp();
		// clear all database tables
		ScriptRunner.runScript("database.sql");

		// initialize the configuration controller to cache encryption key
		configurationController.initialize();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetTransports() throws ControllerException {
		Transport sampleTransport = new Transport();
		sampleTransport.setName("FTP Reader");
		sampleTransport.setClassName("org.mule.providers.ftp.FtpConnector");
		sampleTransport.setProtocol("ftp");
		sampleTransport.setTransformers("ByteArrayToString");
		sampleTransport.setType(Transport.Type.LISTENER);
		sampleTransport.setInbound(true);
		sampleTransport.setOutbound(false);
		Map<String, Transport> testTransportList = configurationController.getTransports();

		Assert.assertTrue(testTransportList.containsValue(sampleTransport));
	}
}