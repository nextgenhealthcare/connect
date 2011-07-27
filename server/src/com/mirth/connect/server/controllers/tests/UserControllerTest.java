/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.mirth.connect.model.LoginStatus;
import com.mirth.connect.model.User;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.UserController;
import com.mirth.connect.server.tools.ScriptRunner;

public class UserControllerTest extends TestCase {
    private UserController userController = ControllerFactory.getFactory().createUserController();
    private List<User> sampleUserList;

    protected void setUp() throws Exception {
        super.setUp();
        // clear all database tables
        ScriptRunner.runScript(new File("conf/" + ControllerTestSuite.database + "/" + ControllerTestSuite.database + "-database.sql"));
        sampleUserList = new ArrayList<User>();

        for (int i = 0; i < 10; i++) {
            User sampleUser = new User();
            sampleUser.setUsername("user" + i);
            sampleUser.setFirstName("User " + i);
            sampleUser.setLastName("User " + i);
            sampleUser.setEmail("user" + i + "@email.com");
            sampleUserList.add(sampleUser);
        }
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testUpdateUser() throws ControllerException {
        User sampleUser = sampleUserList.get(0);
        userController.updateUser(sampleUser);
        List<User> testUserList = userController.getUser(sampleUser);
        User testUser = testUserList.get(0);

        Assert.assertEquals(1, testUserList.size());
        Assert.assertEquals(sampleUser.getUsername(), testUser.getUsername());
    }

    public void testGetUser() throws ControllerException {
        insertSampleUsers();

        for (Iterator iter = sampleUserList.iterator(); iter.hasNext();) {
            User sampleUser = (User) iter.next();
            List<User> testUserList = userController.getUser(sampleUser);
            Assert.assertFalse(testUserList.isEmpty());
        }
    }

    public void testRemoveUser() throws ControllerException {
        insertSampleUsers();

        User sampleUser = sampleUserList.get(0);
        userController.removeUser(sampleUser, new Integer(1));
        List<User> testUserList = userController.getUser(null);

        Assert.assertFalse(testUserList.contains(sampleUser));
    }

    public void testAuthorizeUser() throws ControllerException {
        insertSampleUsers();

        assertTrue((userController.authorizeUser("user0", "password").getStatus() == LoginStatus.Status.SUCCESS));
    }

    public void testLoginUser() throws ControllerException {
        insertSampleUsers();

        User testUser = userController.getUser(null).get(0);
        userController.loginUser(testUser);
        assertTrue(userController.isUserLoggedIn(testUser));
    }

    public void testLogoutUser() throws ControllerException {
        insertSampleUsers();

        User testUser = userController.getUser(null).get(0);
        userController.logoutUser(testUser);
        assertFalse(userController.isUserLoggedIn(testUser));
    }

    public void insertSampleUsers() throws ControllerException {
        for (Iterator iter = sampleUserList.iterator(); iter.hasNext();) {
            User sampleUser = (User) iter.next();
            userController.updateUser(sampleUser);
            userController.updateUserPassword(sampleUser, "password");
        }
    }

    public void testGetUserPreferences() throws ControllerException {
        insertSampleUsers();

        User testUser = userController.getUser(null).get(0);
        userController.setUserPreference(testUser, "test.property", "Hello world!");
        Properties preferences = userController.getUserPreferences(testUser);
        assertFalse(preferences.isEmpty());
    }

    public void testSetUserPreference() throws ControllerException {
        insertSampleUsers();

        User testUser = userController.getUser(null).get(0);
        userController.setUserPreference(testUser, "test.property", "Hello world!");
    }

}
