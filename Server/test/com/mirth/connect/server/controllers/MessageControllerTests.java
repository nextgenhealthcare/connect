/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.model.filters.MessageFilter;

public class MessageControllerTests {
    private Logger logger = Logger.getLogger(getClass());

    @BeforeClass
    public final static void init() throws Exception {
        TestUtils.startMirthServer();
    }

    /**
     * Simulates messages being reprocessed while simultaneously running the message browser search,
     * in order to see if a database deadlock occurs.
     * 
     * @throws Exception
     */
    @Test
    public final void testConcurrentReprocessAndSearch() throws Exception {
        final int testSize = 1000;
        final int pageSize = 20;
        final int searchCount = 1000;
        final MessageController messageController = MessageController.getInstance();
        final MessageFilter filter = new MessageFilter();
        final String channelId = "sqlserverdeadlocktest";
        ExecutorService executor = Executors.newSingleThreadExecutor();
        EngineController engineController = ControllerFactory.getFactory().createEngineController();

        // create and deploy test channel
        TestUtils.deployTestChannel(TestUtils.createTestChannelModel(channelId, "test channel"));

        // fill the channel with test messages
        logger.info("Dispatching " + testSize + " messages into the test channel");

        for (int i = 0; i < testSize; i++) {
            engineController.dispatchRawMessage(channelId, new RawMessage(TestUtils.TEST_HL7_MESSAGE), false, true);
        }

        // start re-processing messages
        Future<Void> future = executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                logger.info("Reprocessing messages");

                try {
                    messageController.reprocessMessages(channelId, filter, false, null);
                } catch (Exception e) {
                    logger.error(e);
                }

                logger.info("Reprocessing completed");
                return null;
            }
        });

        // repeatedly run the message browser search
        com.mirth.connect.donkey.server.channel.Channel donkeyChannel = new com.mirth.connect.donkey.server.channel.Channel();
        donkeyChannel.setChannelId(channelId);

        logger.info("Running select queries");

        for (int i = 0; i < searchCount; i++) {
            messageController.getMessages(filter, donkeyChannel.getChannelId(), false, 0, pageSize);
        }

        logger.info("Select queries completed");

        future.get();
        executor.shutdown();
    }
}
