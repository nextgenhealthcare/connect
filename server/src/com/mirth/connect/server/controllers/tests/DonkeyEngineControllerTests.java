/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers.tests;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.model.ServerEventContext;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;

public class DonkeyEngineControllerTests {
    final public static int TEST_SIZE = 100;

    @BeforeClass
    static public final void beforeClass() throws Exception {
//        SqlConfig.getInstance().start();
    }
    
    @Test
    public final void testStartEngine() throws Exception {
        ControllerFactory.getFactory().createEngineController().startEngine();
    }
    
    @Test
    public final void testStopEngine() throws Exception {
        ControllerFactory.getFactory().createEngineController().stopEngine();
    }
    

    @Test
    public final void testDeployChannels() throws Exception {
        List<String> channelIds = new ArrayList<String>();
        ServerEventContext context = null;

        // Start Donkey
        EngineController donkeyEngineController = ControllerFactory.getFactory().createEngineController();
        donkeyEngineController.startEngine();
        
        // Create Test Channel
//        TestUtils.createChannel(TestUtils.CHANNEL_ID, TestUtils.SERVER_ID, false, 1, 1);
        channelIds.add(TestUtils.CHANNEL_ID);
        
        // Deploy Channel
        donkeyEngineController.deployChannels(channelIds, context);
        
        // Send message to channel and assert received
        
    }
    
//    @Test
//    public final void testImportChannel() throws Exception {
//        List<String> channelIds = new ArrayList<String>();
//
//        // Start Donkey Engine
//        DonkeyEngineController donkeyEngineController = DonkeyEngineController.create();
//        donkeyEngineController.startEngine();
//
//        // Import Channel
//        donkeyEngineController.doImport("testHttpChannel.xml");
//        Channel testChannel = donkeyEngineController.getDonkeyChannels().get(0);
//        channelIds.add(testChannel.getChannelId());
//
//        // Deploy Channel
//        donkeyEngineController.deployChannels(channelIds, null);
//
//        assertEquals(1, testChannel.getDestinationChains().size());
//        assertEquals(1, testChannel.getDestinationChains().get(1).getDestinationConnectors().size());
//
//        // Send msg and Assert msg received
//        TestDestinationConnector destinationConnector = (TestDestinationConnector) testChannel.getDestinationChains().get(1).getDestinationConnectors().get(1);
//        ((TestSourceConnector) testChannel.getSourceConnector()).readTestMessage(TestUtils.TEST_HL7_MESSAGE);
//        Thread.sleep(100);
//        assertEquals(1, destinationConnector.getMessageIds().size());
//
//        // Undeploy Channel
//        donkeyEngineController.undeployChannels(channelIds, null);
//
//        // Send msg and Assert msg not received
//        ((TestSourceConnector) testChannel.getSourceConnector()).readTestMessage(TestUtils.TEST_HL7_MESSAGE);
//        Thread.sleep(100);
//        assertEquals(1, destinationConnector.getMessageIds().size());
//    }
}
