/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util.test;

import junit.framework.TestCase;

import com.mirth.connect.model.PasswordRequirements;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.util.PasswordRequirementsChecker;

public class PasswordRequirementsTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMinLength() throws ControllerException {
        PasswordRequirements req = new PasswordRequirements(10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        assertNotNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements(null, "test", req));
        assertNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements(null, "testtesttest", req));

    }

    public void testMinUpper() throws ControllerException {
        PasswordRequirements req = new PasswordRequirements(0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        assertNotNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements(null, "test", req));
        assertNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements(null, "aTest", req));

        PasswordRequirements req2 = new PasswordRequirements(0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        assertNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements(null, "test", req2));
        assertNotNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements(null, "TESt", req2));
    }

    public void testMinLower() throws ControllerException {
        PasswordRequirements req = new PasswordRequirements(0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0);
        assertNotNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements(null, "TEST", req));
        assertNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements(null, "TESt", req));

        PasswordRequirements req2 = new PasswordRequirements(0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0);
        assertNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements(null, "TEST", req2));
        assertNotNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements(null, "TESt", req2));

        assertNotNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements(null, "testBLAH", new PasswordRequirements(0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0)));
        assertNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements(null, "testtBLAH", new PasswordRequirements(0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0)));
    }

    public void testMinNumeric() throws ControllerException {
        PasswordRequirements req = new PasswordRequirements(0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0);
        assertNotNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements(null, "TEST", req));
        assertNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements(null, "TEST9", req));

        PasswordRequirements req2 = new PasswordRequirements(0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0);
        assertNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements(null, "TEST", req2));
        assertNotNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements(null, "TESt9", req2));
    }

    public void testMinSpecial() throws ControllerException {
        PasswordRequirements req = new PasswordRequirements(0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0);
        assertNotNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements(null, "TEST", req));
        assertNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements(null, "TEST$TEST", req));

        PasswordRequirements req2 = new PasswordRequirements(0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0);
        assertNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements(null, "TEST", req2));
        assertNotNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements(null, "TESt$", req2));
    }

    public void testAllConditions() throws ControllerException {
        PasswordRequirements req = new PasswordRequirements(15, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0);
        assertNotNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements(null, "test", req));
        assertNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements(null, "Th1$isAtestTEST*#", req));
    }
}