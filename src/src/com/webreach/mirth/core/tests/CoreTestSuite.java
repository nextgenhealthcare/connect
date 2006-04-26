package com.webreach.mirth.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class CoreTestSuite {

	public static void main(String[] args) {}

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for com.webreach.mirth.configuration.tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(UserTest.class);
		suite.addTestSuite(ChannelTest.class);
		suite.addTestSuite(ConnectorTest.class);
		suite.addTestSuite(MessageTest.class);
		suite.addTestSuite(ValidatorTest.class);
		suite.addTestSuite(LogTest.class);
		suite.addTestSuite(FilterTest.class);
		suite.addTestSuite(ConfigurationTest.class);
		//$JUnit-END$
		return suite;
	}

}
