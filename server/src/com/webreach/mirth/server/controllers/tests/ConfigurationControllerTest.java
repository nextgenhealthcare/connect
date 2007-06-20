package com.webreach.mirth.server.controllers.tests;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.webreach.mirth.model.ConnectorMetaData;
import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.tools.ScriptRunner;

public class ConfigurationControllerTest extends TestCase {
	private ConfigurationController configurationController = ConfigurationController.getInstance();

	protected void setUp() throws Exception {
		super.setUp();
		// clear all database tables
		ScriptRunner.runScript("derby-database.sql");

		// initialize the configuration controller to cache encryption key
		configurationController.initialize();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetConnectorMetaData() throws ControllerException {
		ConnectorMetaData sampleConnector = new ConnectorMetaData();
		sampleConnector.setName("FTP Reader");
		sampleConnector.setServerClassName("com.webreach.mirth.server.mule.providers.ftp.FtpConnector");
		sampleConnector.setProtocol("ftp");
		sampleConnector.setTransformers("ByteArrayToString");
		sampleConnector.setType(ConnectorMetaData.Type.LISTENER);
		Map<String, ConnectorMetaData> testTransportList = configurationController.getConnectorMetaData();

		Assert.assertTrue(testTransportList.containsValue(sampleConnector));
	}
	
	public void testGetConnectorLibraries() throws ControllerException {
		List<String> libraries = configurationController.getConnectorLibraries();
		
		for (Iterator iter = libraries.iterator(); iter.hasNext();) {
			String library = (String) iter.next();
			System.out.println(library);
		}
	}
}