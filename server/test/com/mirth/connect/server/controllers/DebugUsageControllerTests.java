/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.model.DebugUsage;
import com.mirth.connect.server.tools.ScriptRunner;

import junit.framework.Assert;
import junit.framework.TestCase;

public class DebugUsageControllerTests extends TestCase {
    private DebugUsageController debugUsageController = ControllerFactory.getFactory().createDebugUsageController();
    private List<DebugUsage> sampleDebugStatsList;

    protected void setUp() throws Exception {
        super.setUp();
        // clear all database tables
        ScriptRunner.runScript(new File("conf/" + ControllerTestSuite.database + "/" + ControllerTestSuite.database + "-database.sql"));
        sampleDebugStatsList = new ArrayList<DebugUsage>();
        int minCount = 0;
        int maxCount = 100;
        
        for (int i = 0; i < 3; i++) {
            DebugUsage sampleDebugUsage = new DebugUsage();
            UUID uuid = UUID.randomUUID();
            Random random = new Random();
            
            sampleDebugUsage.setServerId(uuid.toString());

            sampleDebugUsage.setInvocationCount(random.nextInt(maxCount - minCount + 1) + minCount);
            sampleDebugUsage.setPostprocessorCount(random.nextInt(maxCount - minCount + 1) + minCount);
            sampleDebugUsage.setPreprocessorCount(random.nextInt(maxCount - minCount + 1) + minCount);
            sampleDebugUsage.setDeployCount(random.nextInt(maxCount - minCount + 1) + minCount);
            sampleDebugUsage.setUndeployCount(random.nextInt(maxCount - minCount + 1) + minCount);
            
//            sampleDebugUsage.setLastSent();
            sampleDebugStatsList.add(sampleDebugUsage);
        }
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

//    public void testUpdateUser() throws ControllerException {
//        User sampleUser = sampleUserList.get(0);
//        userController.updateUser(sampleUser);
//        User testUser = userController.getUser(sampleUser.getId(), sampleUser.getUsername());
//
//        Assert.assertNotNull(testUser);
//        Assert.assertEquals(sampleUser.getUsername(), testUser.getUsername());
//    }

    public void testGetDebugUsage() throws ControllerException {
        
        insertSampleDebugStats();

        for (Iterator<DebugUsage> iter = sampleDebugStatsList.iterator(); iter.hasNext();) {
            DebugUsage sampleDebugUsage = (DebugUsage) iter.next();
            DebugUsage testDebugUsage = debugUsageController.getDebugUsage(sampleDebugUsage.getServerId());
            Assert.assertNotNull(testDebugUsage);
        }
    }

//    public void testRemoveUser() throws ControllerException {
//        insertSampleUsers();
//
//        User sampleUser = sampleUserList.get(0);
//        userController.removeUser(sampleUser.getId(), new Integer(1));
//        List<User> testUserList = userController.getAllUsers();
//
//        Assert.assertFalse(testUserList.contains(sampleUser));
//    }

//    public void testAuthorizeUser() throws ControllerException {
//        insertSampleUsers();
//
//        assertTrue((userController.authorizeUser("user0", "password").getStatus() == LoginStatus.Status.SUCCESS));
//    }

//    public void testLoginUser() throws ControllerException {
//        insertSampleUsers();
//
//        User testUser = userController.getAllUsers().get(0);
//        userController.loginUser(testUser);
//        assertTrue(userController.isUserLoggedIn(testUser.getId()));
//    }
//
//    public void testLogoutUser() throws ControllerException {
//        insertSampleUsers();
//
//        User testUser = userController.getAllUsers().get(0);
//        userController.logoutUser(testUser);
//        assertFalse(userController.isUserLoggedIn(testUser.getId()));
//    }
//
    public void insertSampleDebugStats() throws ControllerException {
        for (Iterator iter = sampleDebugStatsList.iterator(); iter.hasNext();) {
            DebugUsage debugUsage = (DebugUsage) iter.next();
            debugUsageController.insertOrUpdatePersistedDebugUsageStats(debugUsage);
//            DebugUsage validDebugUsage = debugUsageController.getDebugUsage(debugUsage.getServerId());
        }
    }
//
//    public void testGetUserPreferences() throws ControllerException {
//        insertSampleUsers();
//
//        User testUser = userController.getAllUsers().get(0);
//        userController.setUserPreference(testUser.getId(), "test.property", "Hello world!");
//        Properties preferences = userController.getUserPreferences(testUser.getId(), null);
//        assertFalse(preferences.isEmpty());
//    }
//
//    public void testSetUserPreference() throws ControllerException {
//        insertSampleUsers();
//
//        User testUser = userController.getAllUsers().get(0);
//        userController.setUserPreference(testUser.getId(), "test.property", "Hello world!");
//    }

}
