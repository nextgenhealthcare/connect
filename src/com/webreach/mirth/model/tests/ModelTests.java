package com.webreach.mirth.model.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ModelTests {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(ModelTests.suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for com.webreach.mirth.model.tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(ChannelTest.class);
		suite.addTestSuite(UserTest.class);
		suite.addTestSuite(MessageTest.class);
		suite.addTestSuite(FilterTest.class);
		suite.addTestSuite(ValidatorTest.class);
		suite.addTestSuite(LogTest.class);
		suite.addTestSuite(ConnectorTest.class);
		suite.addTestSuite(TransformerTest.class);
		//$JUnit-END$
		return suite;
	}

}
