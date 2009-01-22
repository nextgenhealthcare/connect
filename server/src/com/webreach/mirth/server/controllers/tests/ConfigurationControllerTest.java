package com.webreach.mirth.server.controllers.tests;

import junit.framework.TestCase;

import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.tools.ScriptRunner;

public class ConfigurationControllerTest extends TestCase {
	private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();

	protected void setUp() throws Exception {
		super.setUp();
		// clear all database tables
		ScriptRunner.runScript("derby-database.sql");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
}