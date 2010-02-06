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
import com.webreach.mirth.server.controllers.ScriptController;
import com.webreach.mirth.server.tools.ScriptRunner;

public class ScriptControllerTest extends TestCase {
	private ScriptController scriptController = ControllerFactory.getFactory().createScriptController();
	private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
	private static final String groupId = "TEST";
	
	protected void setUp() throws Exception {
		super.setUp();
		// clear all database tables
		ScriptRunner.runScript("derby-database.sql");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testPutScript() throws ControllerException {
	    
		String id = configurationController.getGuid();
		String script = "return true;";
		scriptController.putScript(groupId, id, script);

		Assert.assertEquals(script, scriptController.getScript(groupId, id));
	}

	public void testGetScript() throws ControllerException {
		String id = configurationController.getGuid();
		String script = "return true;";
		scriptController.putScript(groupId, id, script);

		Assert.assertEquals(script, scriptController.getScript(groupId, id));
	}
	
	public void testClearScripts() throws ControllerException {
		String id = configurationController.getGuid();
		String script = "return true;";
		scriptController.putScript(groupId, id, script);
		scriptController.removeAllScripts();
		
		Assert.assertNull(scriptController.getScript(groupId, id));
	}

}