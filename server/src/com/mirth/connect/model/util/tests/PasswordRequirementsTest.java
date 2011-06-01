/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.util.tests;

import static junit.framework.Assert.*;
import junit.framework.TestCase;

import com.mirth.connect.model.PasswordRequirements;
import com.mirth.connect.model.util.PasswordRequirementsChecker;
import com.mirth.connect.server.controllers.ControllerException;

public class PasswordRequirementsTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMinLength() throws ControllerException {
        PasswordRequirements req = new PasswordRequirements(0, 0, 0, 0, 10);
        assertNotNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("test", req));
        assertNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("testtesttest", req));
        
    }

    public void testMinUpper() throws ControllerException {
        PasswordRequirements req = new PasswordRequirements(1, 0, 0, 0, 0);
        assertNotNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("test", req));
        assertNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("aTest", req));
        

        PasswordRequirements req2 = new PasswordRequirements(-1, 0, 0, 0, 0);
        assertNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("test", req2));
        assertNotNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("TESt", req2));
    }

    public void testMinLower() throws ControllerException {
        PasswordRequirements req = new PasswordRequirements(0, 1, 0, 0, 0);
        assertNotNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("TEST", req));
        assertNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("TESt", req));
        
        PasswordRequirements req2 = new PasswordRequirements(0, -1, 0, 0, 0);
        assertNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("TEST", req2));
        assertNotNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("TESt", req2));
        
        assertNotNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("testBLAH", new PasswordRequirements(0, 5, 0, 0, 0)));
        assertNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("testtBLAH", new PasswordRequirements(0, 5, 0, 0, 0)));
    }

    public void testMinNumeric() throws ControllerException {
        PasswordRequirements req = new PasswordRequirements(0, 0, 1, 0, 0);
        assertNotNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("TEST", req));
        assertNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("TEST9", req));
        
        PasswordRequirements req2 = new PasswordRequirements(0, 0, -1, 0, 0);
        assertNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("TEST", req2));
        assertNotNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("TESt9", req2));
    }

    public void testMinSpecial() throws ControllerException {
        PasswordRequirements req = new PasswordRequirements(0, 0, 0, 1, 0);
        assertNotNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("TEST", req));
        assertNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("TEST$TEST", req));
        
        PasswordRequirements req2 = new PasswordRequirements(0, 0, 0, -1, 0);
        assertNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("TEST", req2));
        assertNotNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("TESt$", req2));
    }

    public void testAllConditions() throws ControllerException {
        PasswordRequirements req = new PasswordRequirements(1, 1, 1, 1, 15);
        assertNotNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("test", req));
        assertNull(PasswordRequirementsChecker.getInstance().doesPasswordMeetRequirements("Th1$isAtestTEST*#", req));
    }
}