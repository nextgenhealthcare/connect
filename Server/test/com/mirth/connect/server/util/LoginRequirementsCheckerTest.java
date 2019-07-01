/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Calendar;

import org.junit.Test;

import com.mirth.connect.model.LoginStrike;
import com.mirth.connect.model.PasswordRequirements;
import com.mirth.connect.model.User;
import com.mirth.connect.server.controllers.UserController;

public class LoginRequirementsCheckerTest {

    @Test
    public void testIncrementStrikes1() throws Exception {
        User user = new User();
        user.setId(1);
        PasswordRequirements passwordRequirements = mock(PasswordRequirements.class);
        UserController userController = mock(UserController.class);

        // Test non-null LoginStrike returned
        LoginStrike loginStrike = new LoginStrike(1, Calendar.getInstance());
        when(userController.incrementStrikes(user.getId())).thenReturn(loginStrike);
        LoginRequirementsChecker checker = new LoginRequirementsChecker(user, passwordRequirements, userController);
        checker.incrementStrikes();
        assertEquals((Integer) loginStrike.getLastStrikeCount(), user.getStrikeCount());
        assertEquals(loginStrike.getLastStrikeTime(), user.getLastStrikeTime());
    }

    @Test
    public void testIncrementStrikes2() throws Exception {
        User user = new User();
        user.setId(1);
        PasswordRequirements passwordRequirements = mock(PasswordRequirements.class);
        UserController userController = mock(UserController.class);

        // Test null LoginStrike returned
        when(userController.incrementStrikes(user.getId())).thenReturn(null);
        LoginRequirementsChecker checker = new LoginRequirementsChecker(user, passwordRequirements, userController);
        checker.incrementStrikes();
        assertNull(user.getStrikeCount());
        assertNull(user.getLastStrikeTime());
    }

    @Test
    public void testResetStrikes() throws Exception {
        User user = new User();
        user.setId(1);
        PasswordRequirements passwordRequirements = mock(PasswordRequirements.class);
        UserController userController = mock(UserController.class);

        LoginStrike loginStrike = new LoginStrike(0, null);
        when(userController.resetStrikes(user.getId())).thenReturn(loginStrike);
        LoginRequirementsChecker checker = new LoginRequirementsChecker(user, passwordRequirements, userController);
        checker.resetStrikes();
        assertEquals((Integer) loginStrike.getLastStrikeCount(), user.getStrikeCount());
        assertNull(user.getLastStrikeTime());
    }
}
