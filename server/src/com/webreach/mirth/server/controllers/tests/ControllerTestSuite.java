package com.webreach.mirth.server.controllers.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ControllerTestSuite {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(ControllerTestSuite.suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for com.webreach.mirth.server.controllers.tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(ScriptControllerTest.class);
		suite.addTestSuite(UserControllerTest.class);
		suite.addTestSuite(EventControllerTest.class);
		suite.addTestSuite(ConfigurationControllerTest.class);
		suite.addTestSuite(TemplateControllerTest.class);
		suite.addTestSuite(MessageObjectControllerTest.class);
		suite.addTestSuite(ChannelControllerTest.class);
		suite.addTestSuite(StatisticsControllerTest.class);
		//$JUnit-END$
		return suite;
	}

}
