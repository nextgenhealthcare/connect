/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.server.controllers.tests;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.TemplateController;
import com.webreach.mirth.server.tools.ScriptRunner;

public class TemplateControllerTest extends TestCase {
	private TemplateController templateController = ControllerFactory.getFactory().createTemplateController();
	private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();

	protected void setUp() throws Exception {
		super.setUp();
		// clear all database tables
		ScriptRunner.runScript("derby-database.sql");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testPutTemplate() throws ControllerException {
		String id = configurationController.getGuid();
		String template = "<sample><test>hello world</test></sample>";
		templateController.putTemplate(id, template);

		Assert.assertEquals(template, templateController.getTemplate(id));
	}

	public void testGetTemplate() throws ControllerException {
		String id = configurationController.getGuid();
		String template = "<sample><test>hello world</test></sample>";
		templateController.putTemplate(id, template);

		Assert.assertEquals(template, templateController.getTemplate(id));
	}
	
	public void testClearTemplates() throws ControllerException {
		String id = configurationController.getGuid();
		String template = "<sample><test>hello world</test></sample>";
		templateController.putTemplate(id, template);
		templateController.clearTemplates();
		
		Assert.assertNull(templateController.getTemplate(id));
	}

}