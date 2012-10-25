/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers.tests;

import java.io.File;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ExtensionController;
import com.mirth.connect.server.tools.ScriptRunner;

public class PluginControllerTest extends TestCase {
    private ExtensionController pluginController = ControllerFactory.getFactory().createExtensionController();

    protected void setUp() throws Exception {
        super.setUp();
        // clear all database tables
        ScriptRunner.runScript(new File("conf/" + ControllerTestSuite.database + "/" + ControllerTestSuite.database + "-database.sql"));
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetConnectorMetaData() throws ControllerException {
        ConnectorMetaData sampleConnector = new ConnectorMetaData();
        sampleConnector.setName("FTP Reader");
        sampleConnector.setServerClassName("com.mirth.connect.server.mule.providers.ftp.FtpConnector");
        sampleConnector.setProtocol("ftp");
        sampleConnector.setTransformers("ByteArrayToString");
        sampleConnector.setType(ConnectorMetaData.Type.SOURCE);
        Map<String, ConnectorMetaData> testTransportList = pluginController.getConnectorMetaData();

        Assert.assertTrue(testTransportList.containsValue(sampleConnector));
    }
}