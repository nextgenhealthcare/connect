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
import java.util.Calendar;
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
    UUID uuid = UUID.randomUUID();

    protected void setUp() throws Exception {
        super.setUp();
        // clear all database tables
//        ScriptRunner.runScript(new File("dbconf/" + ControllerTestSuite.database + "/" + ControllerTestSuite.database + "-database.sql"));
        sampleDebugStatsList = new ArrayList<DebugUsage>();
        int minCount = 0;
        int maxCount = 100;
        
        // is this three to mimic advanced clustering?
//        for (int i = 0; i < 3; i++) {
//            DebugUsage sampleDebugUsage = new DebugUsage();
//            UUID uuid = UUID.randomUUID();
//            Random random = new Random();
//            
//            sampleDebugUsage.setServerId(uuid.toString());
//            sampleDebugUsage.setDuppCount(random.nextInt(maxCount - minCount + 1) + minCount);
//            sampleDebugUsage.setAttachBatchCount(random.nextInt(maxCount - minCount + 1) + minCount);
//            sampleDebugUsage.setSourceFilterTransCount(random.nextInt(maxCount - minCount + 1) + minCount);
//            sampleDebugUsage.setResponseCount(random.nextInt(maxCount - minCount + 1) + minCount);
//            sampleDebugUsage.setInvocationCount(random.nextInt(maxCount - minCount + 1) + minCount);
//            
//            sampleDebugUsage.setLastSent();
//            sampleDebugStatsList.add(sampleDebugUsage);
//        }
    }

//    protected void tearDown() throws Exception {
//        super.tearDown();
//    }

//    public void testUpdateUser() throws ControllerException {
//        User sampleUser = sampleUserList.get(0);
//        userController.updateUser(sampleUser);
//        User testUser = userController.getUser(sampleUser.getId(), sampleUser.getUsername());
//
//        Assert.assertNotNull(testUser);
//        Assert.assertEquals(sampleUser.getUsername(), testUser.getUsername());
//    }

    public void testGetDebugUsageIsNull() throws ControllerException {
        DebugUsage testDebugUsage = debugUsageController.getDebugUsage(uuid.toString());
        Assert.assertEquals(testDebugUsage, null);
    }
    
    public void TestInsertDebugUsage() throws ControllerException {
        DebugUsage testDebugUsage = debugUsageController.getDebugUsage(uuid.toString());
        testDebugUsage.setAttachBatchCount(1);
        testDebugUsage.setDestinationFilterTransCount(1);
        testDebugUsage.setInvocationCount(1);
        debugUsageController.upsertDebugUsage(testDebugUsage);
        DebugUsage testGetDebugUsage = debugUsageController.getDebugUsage(uuid.toString());

        Assert.assertEquals((int) testGetDebugUsage.getDuppCount(), 0);
        Assert.assertEquals((int) testGetDebugUsage.getAttachBatchCount(), 1);
        Assert.assertEquals((int) testGetDebugUsage.getSourceConnectorCount(), 0);
        Assert.assertEquals((int) testGetDebugUsage.getSourceFilterTransCount(), 0);
        Assert.assertEquals((int) testGetDebugUsage.getDestinationFilterTransCount(), 1);
        Assert.assertEquals((int) testGetDebugUsage.getDestinationConnectorCount(), 0);
        Assert.assertEquals((int) testGetDebugUsage.getResponseCount(), 0);
        Assert.assertEquals((int) testGetDebugUsage.getInvocationCount(), 1);
        Assert.assertEquals((Calendar) testGetDebugUsage.getLastSent(), null);
        
    }  
    
    public void TestUpdateDebugUsage() throws ControllerException {
        DebugUsage testDebugUsage = debugUsageController.getDebugUsage(uuid.toString());
        testDebugUsage.setAttachBatchCount(1);
        testDebugUsage.setSourceConnectorCount(1);
        testDebugUsage.setInvocationCount(1);
        debugUsageController.upsertDebugUsage(testDebugUsage);
        DebugUsage testGetDebugUsage = debugUsageController.getDebugUsage(uuid.toString());

        Assert.assertEquals((int) testGetDebugUsage.getDuppCount(), 0);
        Assert.assertEquals((int) testGetDebugUsage.getAttachBatchCount(), 2);
        Assert.assertEquals((int) testGetDebugUsage.getSourceConnectorCount(), 1);
        Assert.assertEquals((int) testGetDebugUsage.getSourceFilterTransCount(), 0);
        Assert.assertEquals((int) testGetDebugUsage.getDestinationFilterTransCount(), 1);
        Assert.assertEquals((int) testGetDebugUsage.getDestinationConnectorCount(), 0);
        Assert.assertEquals((int) testGetDebugUsage.getResponseCount(), 0);
        Assert.assertEquals((int) testGetDebugUsage.getInvocationCount(), 2);
        Assert.assertEquals((Calendar) testGetDebugUsage.getLastSent(), null);
        
    } 
    
    public void TestResetDebugUsage() throws ControllerException {
        debugUsageController.resetDebugUsage(uuid.toString());
        DebugUsage testGetDebugUsage = debugUsageController.getDebugUsage(uuid.toString());

        Assert.assertEquals((int) testGetDebugUsage.getDuppCount(), 0);
        Assert.assertEquals((int) testGetDebugUsage.getAttachBatchCount(), 0);
        Assert.assertEquals((int) testGetDebugUsage.getSourceConnectorCount(), 0);
        Assert.assertEquals((int) testGetDebugUsage.getSourceFilterTransCount(), 0);
        Assert.assertEquals((int) testGetDebugUsage.getDestinationFilterTransCount(), 0);
        Assert.assertEquals((int) testGetDebugUsage.getDestinationConnectorCount(), 0);
        Assert.assertEquals((int) testGetDebugUsage.getResponseCount(), 0);
        Assert.assertEquals((int) testGetDebugUsage.getInvocationCount(), 0);
        Assert.assertNotNull(testGetDebugUsage.getLastSent());
        
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
            debugUsageController.upsertDebugUsage(debugUsage);
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
