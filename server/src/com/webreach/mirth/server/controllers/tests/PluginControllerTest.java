/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.server.controllers.tests;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.webreach.mirth.model.ConnectorMetaData;
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.ExtensionController;
import com.webreach.mirth.server.tools.ScriptRunner;

public class PluginControllerTest extends TestCase {
	private ExtensionController pluginController = ControllerFactory.getFactory().createExtensionController();

	protected void setUp() throws Exception {
		super.setUp();
		// clear all database tables
		ScriptRunner.runScript("derby-database.sql");
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
		sampleConnector.setType(ConnectorMetaData.Type.SOURCE);
		Map<String, ConnectorMetaData> testTransportList = pluginController.getConnectorMetaData();

		Assert.assertTrue(testTransportList.containsValue(sampleConnector));
	}
	
	public void testGetConnectorLibraries() throws ControllerException {
		List<String> libraries = pluginController.getClientLibraries();
		
		for (Iterator iter = libraries.iterator(); iter.hasNext();) {
			String library = (String) iter.next();
			System.out.println(library);
		}
	}
}