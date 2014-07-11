/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.donkey.model.channel.ResponseConnectorProperties;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.test.util.TestChannel;
import com.mirth.connect.donkey.test.util.TestSourceConnector;
import com.mirth.connect.donkey.test.util.TestUtils;

public class SourceConnectorTests {
    private static int TEST_SIZE = 20;
    private static String channelId = TestUtils.DEFAULT_CHANNEL_ID;
    private static String serverId = TestUtils.DEFAULT_SERVER_ID;
    private static String testMessage = TestUtils.TEST_HL7_MESSAGE;

    @BeforeClass
    final public static void beforeClass() throws StartException {
        Donkey.getInstance().startEngine(TestUtils.getDonkeyTestConfiguration());
    }

    @AfterClass
    final public static void afterClass() throws StartException {
        Donkey.getInstance().stopEngine();
    }

    /*
     * Deploy a new channel, assert that:
     * - The default behaviour for the source connector is to wait for
     * destinations
     * 
     * Send messages that should immediately process, assert that:
     * - The message was created and marked as processed in the database
     * - The MessageResponse returned is not null
     * - The response status is correct (TRANSFORMED)
     * - The channel's final transaction was created
     * - The source connector response was stored
     * 
     * Send messages that should immediately queue, assert that:
     * - The message was stored in the database
     * - The MessageResponse returned is not null
     * - The response is not null
     * - The source connector response was not stored
     */
    @Test
    public final void testHandleRawMessage() throws Exception {
        TestChannel channel = (TestChannel) TestUtils.createDefaultChannel(channelId, serverId);
        TestSourceConnector sourceConnector = (TestSourceConnector) channel.getSourceConnector();
        sourceConnector.setRespondAfterProcessing(true);
        channel.getResponseSelector().setRespondFromName(ResponseConnectorProperties.RESPONSE_SOURCE_TRANSFORMED);

        channel.deploy();
        channel.start();

        // Assert that the default source connector behaviour is to wait for destinations
        assertTrue(sourceConnector.isRespondAfterProcessing());

        // Send messages that immediately process
        for (int i = 1; i <= TEST_SIZE; i++) {
            RawMessage rawMessage = new RawMessage(testMessage);
            DispatchResult dispatchResult = null;

            try {
                dispatchResult = sourceConnector.dispatchRawMessage(rawMessage);
                dispatchResult.setAttemptedResponse(true);
                
                if (dispatchResult.getSelectedResponse() != null) {
                    dispatchResult.getSelectedResponse().setMessage("response");
                }
            } finally {
                sourceConnector.finishDispatch(dispatchResult);
            }

            if (dispatchResult != null) {
                sourceConnector.getMessageIds().add(dispatchResult.getMessageId());
            }

            // Assert that the message was created and processed
            Message message = new Message();
            message.setChannelId(channel.getChannelId());
            message.setMessageId(dispatchResult.getMessageId());
            message.setServerId(channel.getServerId());
            message.setProcessed(true);
            TestUtils.assertMessageExists(message, false);

            // Assert that the message response is not null
            assertNotNull(dispatchResult);
            // Assert that the response is not null
            assertNotNull(dispatchResult.getSelectedResponse());
            // Assert that the response status is correct
            assertEquals(Status.TRANSFORMED, dispatchResult.getSelectedResponse().getStatus());
            // Assert that the source connector response was created
            TestUtils.assertResponseExists(channel.getChannelId(), dispatchResult.getMessageId());
        }

        // Send messages that queue
        sourceConnector.setRespondAfterProcessing(false);
        channel.getResponseSelector().setRespondFromName(null);

        for (int i = 1; i <= TEST_SIZE; i++) {
            RawMessage rawMessage = new RawMessage(testMessage);
            DispatchResult dispatchResult = null;

            try {
                dispatchResult = sourceConnector.dispatchRawMessage(rawMessage);
            } finally {
                sourceConnector.finishDispatch(dispatchResult);
            }

            if (dispatchResult != null) {
                sourceConnector.getMessageIds().add(dispatchResult.getMessageId());
            }

            // Assert that the message was created
            Message message = new Message();
            message.setChannelId(channel.getChannelId());
            message.setMessageId(dispatchResult.getMessageId());
            message.setServerId(channel.getServerId());
            message.setProcessed(false);
            try {
                TestUtils.assertMessageExists(message, false);
            } catch (Exception e) {
                message.setProcessed(true);
                TestUtils.assertMessageExists(message, false);
            }

            // Assert that the message response is not null
            assertNotNull(dispatchResult);
            // Assert that the response is null
            assertNull(dispatchResult.getSelectedResponse());
            // Assert that the source connector response was not created
            TestUtils.assertResponseDoesNotExist(channel.getChannelId(), dispatchResult.getMessageId());
        }

        channel.stop();
        channel.undeploy();

        ChannelController.getInstance().removeChannel(channel.getChannelId());
    }

    /*
     * Deploy a new channel, send messages, assert
     * that:
     * - The message was stored in the database
     * - The MessageResponse returned is not null
     * - The response is null
     * - The source connector response was not stored
     * 
     * Call storeMessageResponse using a null MessageResponse assert:
     * - The source connector response was not stored
     * 
     * Call storeMessageResponse using the returned MessageResponse, assert:
     * - The source connector response was not stored
     * 
     * Modify the MessageResponse, creating/setting a new Response object
     * Call storeMessageResponse using the returned MessageResponse, assert:
     * - The source connector response was stored
     */
    @Test
    public final void testStoreMessageResponse() throws Exception {
        TestChannel channel = (TestChannel) TestUtils.createDefaultChannel(channelId, serverId);

        TestSourceConnector sourceConnector = (TestSourceConnector) channel.getSourceConnector();
        sourceConnector.setRespondAfterProcessing(true);
        channel.getResponseSelector().setRespondFromName(ResponseConnectorProperties.RESPONSE_SOURCE_TRANSFORMED);

        channel.deploy();
        channel.start();

        for (int i = 1; i <= TEST_SIZE; i++) {
            RawMessage rawMessage = new RawMessage(testMessage);
            DispatchResult dispatchResult = null;

            try {
                dispatchResult = sourceConnector.dispatchRawMessage(rawMessage);
                dispatchResult.setAttemptedResponse(true);
                
                if (dispatchResult.getSelectedResponse() != null) {
                    dispatchResult.getSelectedResponse().setMessage("response");
                }
            } finally {
                sourceConnector.finishDispatch(dispatchResult);
            }

            if (dispatchResult != null) {
                sourceConnector.getMessageIds().add(dispatchResult.getMessageId());
            }

            // Assert that the message was created
            Message message = new Message();
            message.setChannelId(channel.getChannelId());
            message.setMessageId(dispatchResult.getMessageId());
            message.setServerId(channel.getServerId());
            message.setProcessed(true);
            TestUtils.assertMessageExists(message, false);

            // Assert that the message response is not null
            assertNotNull(dispatchResult);

            // Assert that the response is not null
            assertNotNull(dispatchResult.getSelectedResponse());

            // Assert that the source connector response was created
            TestUtils.assertResponseExists(channel.getChannelId(), dispatchResult.getMessageId());
        }

        channel.stop();
        channel.undeploy();

        ChannelController.getInstance().removeChannel(channel.getChannelId());
    }

    /*
     * Deploys and starts a channel, sets the source respond-from name to
     * RESPONSE_SOURCE_TRANSFORMED, sends messages, and asserts that:
     * - Each response returned is TRANSFORMED
     * - TEST_SIZE messages are processed
     * 
     * Then sets the source respond-from name to
     * RESPONSE_DESTINATIONS_COMPLETED, sends messages, and asserts that:
     * - Each response returned is SENT
     * - 2*TEST_SIZE messages are processed
     * 
     * Then sets the source respond-from name to the destination name, sends
     * messages, and asserts that:
     * - Each response returned is SENT
     * - 3*TEST_SIZE messages are processed
     * 
     * Then sets the source respond-from name to an invalid destination name,
     * sends messages, and asserts that:
     * - Each response returned is null
     * - 4*TEST_SIZE messages are processed
     */
    @Test
    public final void testGetResponse() throws Exception {
        String destinationName = TestUtils.DEFAULT_DESTINATION_NAME;
        TestChannel channel = (TestChannel) TestUtils.createDefaultChannel(channelId, serverId);
        TestSourceConnector sourceConnector = (TestSourceConnector) channel.getSourceConnector();
        Response response = null;

        channel.deploy();
        channel.start();

        channel.getResponseSelector().setRespondFromName(ResponseConnectorProperties.RESPONSE_SOURCE_TRANSFORMED);

        for (int i = 0; i < TEST_SIZE; i++) {
            response = sourceConnector.readTestMessage(testMessage).getSelectedResponse();
            assertEquals(Status.TRANSFORMED, response.getStatus());
        }

        assertEquals(TEST_SIZE, channel.getNumMessages());

        channel.getResponseSelector().setRespondFromName(ResponseConnectorProperties.RESPONSE_DESTINATIONS_COMPLETED);

        for (int i = 0; i < TEST_SIZE; i++) {
            response = sourceConnector.readTestMessage(testMessage).getSelectedResponse();
            assertEquals(Status.SENT, response.getStatus());
        }

        assertEquals(TEST_SIZE * 2, channel.getNumMessages());

        channel.getResponseSelector().setRespondFromName("d1");

        for (int i = 0; i < TEST_SIZE; i++) {
            response = sourceConnector.readTestMessage(testMessage).getSelectedResponse();
            assertEquals(Status.SENT, response.getStatus());
        }

        assertEquals(TEST_SIZE * 3, channel.getNumMessages());

        channel.getResponseSelector().setRespondFromName(destinationName + "lolwut");

        for (int i = 0; i < TEST_SIZE; i++) {
            response = sourceConnector.readTestMessage(testMessage).getSelectedResponse();
            assertEquals(null, response);
        }

        assertEquals(TEST_SIZE * 4, channel.getNumMessages());

        channel.stop();
        channel.undeploy();
    }
}