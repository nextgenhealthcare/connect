/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.server.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class CoreTestSuite {

	public static void main(String[] args) {}

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for com.webreach.mirth.core.tests");
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
