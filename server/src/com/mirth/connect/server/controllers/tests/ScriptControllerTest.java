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

import junit.framework.Assert;
import junit.framework.TestCase;

import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.tools.ScriptRunner;

public class ScriptControllerTest extends TestCase {
    private ScriptController scriptController = ControllerFactory.getFactory().createScriptController();
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private static final String groupId = "TEST";

    protected void setUp() throws Exception {
        super.setUp();
        // clear all database tables
        ScriptRunner.runScript(new File("conf/" + ControllerTestSuite.database + "/" + ControllerTestSuite.database + "-database.sql"));
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testPutScript() throws ControllerException {

        String id = configurationController.generateGuid();
        String script = "return true;";
        scriptController.putScript(groupId, id, script);

        Assert.assertEquals(script, scriptController.getScript(groupId, id));
    }

    public void testGetScript() throws ControllerException {
        String id = configurationController.generateGuid();
        String script = "return true;";
        scriptController.putScript(groupId, id, script);

        Assert.assertEquals(script, scriptController.getScript(groupId, id));
    }

    public void testClearScripts() throws ControllerException {
        String id = configurationController.generateGuid();
        String script = "return true;";
        scriptController.putScript(groupId, id, script);
        scriptController.removeScripts(groupId);

        Assert.assertNull(scriptController.getScript(groupId, id));
    }

}