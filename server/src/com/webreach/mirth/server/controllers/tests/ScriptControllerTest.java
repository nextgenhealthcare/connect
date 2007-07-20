package com.webreach.mirth.server.controllers.tests;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.controllers.ScriptController;
import com.webreach.mirth.server.tools.ScriptRunner;

public class ScriptControllerTest extends TestCase {
	private ScriptController scriptController = ScriptController.getInstance();
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

	public void testPutScript() throws ControllerException {
		String id = configurationController.getGuid();
		String script = "return true;";
		scriptController.putScript(id, script);

		Assert.assertEquals(script, scriptController.getScript(id));
	}

	public void testGetScript() throws ControllerException {
		String id = configurationController.getGuid();
		String script = "return true;";
		scriptController.putScript(id, script);

		Assert.assertEquals(script, scriptController.getScript(id));
	}
	
	public void testClearScripts() throws ControllerException {
		String id = configurationController.getGuid();
		String script = "return true;";
		scriptController.putScript(id, script);
		scriptController.clearScripts();
		
		Assert.assertNull(scriptController.getScript(id));
	}

}