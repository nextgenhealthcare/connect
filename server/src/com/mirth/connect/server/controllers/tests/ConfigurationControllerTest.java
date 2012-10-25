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

import junit.framework.TestCase;

import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.tools.ScriptRunner;

public class ConfigurationControllerTest extends TestCase {
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();

    protected void setUp() throws Exception {
        super.setUp();
        // clear all database tables
        // ScriptRunner.runScript("derby-database.sql");
        ScriptRunner.runScript(new File("conf/" + ControllerTestSuite.database + "/" + ControllerTestSuite.database + "-database.sql"));
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}