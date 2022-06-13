/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.mirth.connect.util.PropertyLoader;

public class ControllerTestSuite {

    public static String database = PropertyLoader.getProperty(PropertyLoader.loadProperties("mirth"), "database");

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ControllerTestSuite.suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for com.mirth.connect.server.controllers.tests");
        // $JUnit-BEGIN$

        suite.addTestSuite(ScriptControllerTests.class);
        suite.addTestSuite(UserControllerTests.class);
        suite.addTestSuite(ConfigurationControllerTests.class);
        suite.addTestSuite(ChannelControllerTests.class);
        suite.addTestSuite(StatisticsControllerTests.class);
        // $JUnit-END$
        return suite;
    }

}
