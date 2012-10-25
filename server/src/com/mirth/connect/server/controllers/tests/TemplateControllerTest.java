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
import com.mirth.connect.server.controllers.TemplateController;
import com.mirth.connect.server.tools.ScriptRunner;

public class TemplateControllerTest extends TestCase {
    private TemplateController templateController = ControllerFactory.getFactory().createTemplateController();
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private static final String GROUP_ID = "TEST";

    protected void setUp() throws Exception {
        super.setUp();
        // clear all database tables
        ScriptRunner.runScript(new File("conf/" + ControllerTestSuite.database + "/" + ControllerTestSuite.database + "-database.sql"));
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testPutTemplate() throws ControllerException {
        String id = configurationController.generateGuid();
        String template = "<sample><test>hello world</test></sample>";
        templateController.putTemplate(GROUP_ID, id, template);

        Assert.assertEquals(template, templateController.getTemplate(GROUP_ID, id));
    }

    public void testGetTemplate() throws ControllerException {
        String id = configurationController.generateGuid();
        String template = "<sample><test>hello world</test></sample>";
        templateController.putTemplate(GROUP_ID, id, template);

        Assert.assertEquals(template, templateController.getTemplate(GROUP_ID, id));
    }

    public void testClearTemplates() throws ControllerException {
        String id = configurationController.generateGuid();
        String template = "<sample><test>hello world</test></sample>";
        templateController.putTemplate(GROUP_ID, id, template);
        templateController.removeTemplates(GROUP_ID);

        Assert.assertNull(templateController.getTemplate(GROUP_ID, id));
    }

}