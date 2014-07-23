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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.donkey.model.DonkeyException;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.donkey.model.channel.DestinationConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.DestinationConnectorProperties;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.channel.DestinationChain;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.test.util.TestChannel;
import com.mirth.connect.donkey.test.util.TestConnectorProperties;
import com.mirth.connect.donkey.test.util.TestDestinationConnector;
import com.mirth.connect.donkey.test.util.TestDispatcher;
import com.mirth.connect.donkey.test.util.TestDispatcherProperties;
import com.mirth.connect.donkey.test.util.TestPostProcessor;
import com.mirth.connect.donkey.test.util.TestPreProcessor;
import com.mirth.connect.donkey.test.util.TestResponseTransformer;
import com.mirth.connect.donkey.test.util.TestSourceConnector;
import com.mirth.connect.donkey.test.util.TestUtils;
import com.mirth.connect.donkey.util.Serializer;

public class DestinationConnectorTests {
    private static int TEST_SIZE = 10;
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
     * Create a new channel with a TestDispatcher destination connector
     * Assert that:
     * - The destination connector has not been deployed
     * - The destination connector is not running
     * - The destination connector's queue thread is not running
     * 
     * Call onDeploy(), assert that:
     * - The destination connector has been successfully deployed
     * 
     * Call start(), assert that:
     * - The destination connector is running
     * - The destination connector's queue thread is running
     */
    @Test
    public final void testStart() throws Exception {
        TestUtils.initChannel(channelId);

        TestChannel channel = new TestChannel();

        channel.setChannelId(channelId);
        channel.setServerId(serverId);
        channel.setEnabled(true);

        channel.setPreProcessor(new TestPreProcessor());
        channel.setPostProcessor(new TestPostProcessor());

        TestSourceConnector sourceConnector = (TestSourceConnector) TestUtils.createDefaultSourceConnector();
        sourceConnector.setChannelId(channel.getChannelId());
        sourceConnector.setChannel(channel);
        channel.setSourceConnector(sourceConnector);
        channel.setSourceFilterTransformer(TestUtils.createDefaultFilterTransformerExecutor());

        TestDispatcher destinationConnector = new TestDispatcher();
        TestDispatcherProperties connectorProperties = new TestDispatcherProperties();
        connectorProperties.getDestinationConnectorProperties().setQueueEnabled(true);
        TestUtils.initDefaultDestinationConnector(destinationConnector, connectorProperties);
        destinationConnector.setChannelId(channelId);

        DestinationChain chain = new DestinationChain();
        chain.setChannelId(channelId);
        chain.setMetaDataReplacer(sourceConnector.getMetaDataReplacer());
        chain.setMetaDataColumns(channel.getMetaDataColumns());
        chain.addDestination(1, TestUtils.createDefaultFilterTransformerExecutor(), destinationConnector);
        channel.addDestinationChain(chain);

        // Assert that the destination connector has not been deployed
        assertFalse(destinationConnector.isDeployed());
        // Assert that the destination connector is not running
        assertFalse(destinationConnector.getCurrentState() != DeployedState.STOPPED);
        // Assert that the destination connector queue thread is not running
        assertFalse(destinationConnector.isQueueThreadRunning());

        destinationConnector.onDeploy();

        // Assert that the destination connector has been deployed
        assertTrue(destinationConnector.isDeployed());

        destinationConnector.start();
        Thread.sleep(1000);

        // Assert that the destination connector is running
        assertTrue(destinationConnector.getCurrentState() != DeployedState.STOPPED);
        // Assert that the destination connector queue thread is running
        assertTrue(destinationConnector.isQueueThreadRunning());

        destinationConnector.stop();
        destinationConnector.onUndeploy();
        ChannelController.getInstance().removeChannel(channel.getChannelId());
    }

    /*
     * Create a new channel with a TestDispatcher destination connector
     * Call onDeploy() and start(), assert that:
     * - The destination connector has been successfully deployed
     * - The destination connector is running
     * - The destination connector's queue thread is running
     * 
     * Call stop() and onUndeploy(), assert that:
     * - The destination connector has been successfully undeployed
     * - The destination connector is not running
     * - The destination connector's queue thread is not running
     * 
     * Do the same steps as above, except call stop(true) instead of stop()
     */
    @Test
    public final void testStop() throws Exception {
        ChannelController.getInstance().getLocalChannelId(channelId);

        TestChannel channel = new TestChannel();

        channel.setChannelId(channelId);
        channel.setServerId(serverId);
        channel.setEnabled(true);

        channel.setPreProcessor(new TestPreProcessor());
        channel.setPostProcessor(new TestPostProcessor());

        TestSourceConnector sourceConnector = (TestSourceConnector) TestUtils.createDefaultSourceConnector();
        sourceConnector.setChannelId(channel.getChannelId());
        sourceConnector.setChannel(channel);
        channel.setSourceConnector(sourceConnector);
        channel.setSourceFilterTransformer(TestUtils.createDefaultFilterTransformerExecutor());

        TestDispatcher destinationConnector = new TestDispatcher();
        TestDispatcherProperties connectorProperties = new TestDispatcherProperties();
        connectorProperties.getDestinationConnectorProperties().setQueueEnabled(true);
        TestUtils.initDefaultDestinationConnector(destinationConnector, connectorProperties);
        destinationConnector.setChannelId(channelId);

        DestinationChain chain = new DestinationChain();
        chain.setChannelId(channelId);
        chain.setMetaDataReplacer(sourceConnector.getMetaDataReplacer());
        chain.setMetaDataColumns(channel.getMetaDataColumns());
        chain.addDestination(1, TestUtils.createDefaultFilterTransformerExecutor(), destinationConnector);
        channel.addDestinationChain(chain);

        channel.deploy();
        channel.start();
        Thread.sleep(1000);

        // Assert that the destination connector has been deployed
        assertTrue(destinationConnector.isDeployed());
        // Assert that the destination connector is running
        assertTrue(destinationConnector.getCurrentState() != DeployedState.STOPPED);
        // Assert that the destination connector queue thread is running
        assertTrue(destinationConnector.isQueueThreadRunning());

        destinationConnector.stop();
        destinationConnector.onUndeploy();

        // Assert that the destination connector has been undeployed
        assertFalse(destinationConnector.isDeployed());
        // Assert that the destination connector is not running
        assertFalse(destinationConnector.getCurrentState() != DeployedState.STOPPED);
        // Assert that the destination connector queue thread is not running
        assertFalse(destinationConnector.isQueueThreadRunning());

        destinationConnector.onDeploy();
        destinationConnector.start();
        Thread.sleep(1000);

        // Assert that the destination connector has been deployed
        assertTrue(destinationConnector.isDeployed());
        // Assert that the destination connector is running
        assertTrue(destinationConnector.getCurrentState() != DeployedState.STOPPED);
        // Assert that the destination connector queue thread is running
        assertTrue(destinationConnector.isQueueThreadRunning());

        destinationConnector.halt();
        destinationConnector.onUndeploy();

        // Assert that the destination connector has been undeployed
        assertFalse(destinationConnector.isDeployed());
        // Assert that the destination connector is not running
        assertFalse(destinationConnector.getCurrentState() != DeployedState.STOPPED);
        // Assert that the destination connector queue thread is not running
        assertFalse(destinationConnector.isQueueThreadRunning());

        ChannelController.getInstance().removeChannel(channel.getChannelId());
    }

    /*
     * Create a new channel, where the destination connector is either a
     * TestDispatcher or a TestDestinationConnector, depending on whether it
     * should have its queueProperties null or not.
     * 
     * Send messages, assert that:
     * - If the queue properties is null, the queuing is disabled, queuing is
     * set to send first, or queuing is set to not regenerate the template, then
     * the sent content was stored
     * - If the queue properties is null, queuing is disabled, or queuing is set
     * to send first, then the send attempts is at least one
     * - If the queue properties is not null and queuing is enabled, then the
     * message was added to the destination connector queue
     * 
     * Repeat the above steps for all applicable combinations of queueNull,
     * queueEnabled, queueSendFirst, and queueRegenerate
     */
    
    // This test is all sorts of broken. It needs to be completely rewritten 
//    @Test
//    public final void testProcess() throws Exception {
//        // Test for null queueProperties
//        testProcess(true, false, false, false, 0);
//
//        // Tests for non-null queueProperties
//        testProcess(false, false, false, false, 0);
//        testProcess(false, false, false, true, 0);
//        testProcess(false, false, true, false, 0);
//        testProcess(false, false, true, true, 0);
//        testProcess(false, true, false, false, 0);
//        testProcess(false, true, false, true, 0);
//        testProcess(false, true, true, false, 0);
//        testProcess(false, true, true, true, 0);
//
//        // test retryOnFailure option
//        testProcess(false, false, false, false, 5);
//        testProcess(false, true, true, false, 5);
//    }

    private void testProcess(boolean queueNull, boolean queueEnabled, boolean queueSendFirst, boolean queueRegenerate, int retryCount) throws Exception {
        ChannelController.getInstance().getLocalChannelId(channelId);

        TestChannel channel = new TestChannel();

        channel.setChannelId(channelId);
        channel.setServerId(serverId);
        channel.setEnabled(true);

        channel.setPreProcessor(new TestPreProcessor());
        channel.setPostProcessor(new TestPostProcessor());

        TestSourceConnector sourceConnector = (TestSourceConnector) TestUtils.createDefaultSourceConnector();
        sourceConnector.setChannelId(channel.getChannelId());
        sourceConnector.setChannel(channel);
        channel.setSourceConnector(sourceConnector);
        channel.setSourceFilterTransformer(TestUtils.createDefaultFilterTransformerExecutor());

        DestinationConnector destinationConnector = null;
        ConnectorProperties connectorProperties = null;
        DestinationConnectorProperties dispatcherConnectorProperties = null;

        if (queueNull) {
            destinationConnector = new TestDestinationConnector();
            connectorProperties = new TestConnectorProperties();
        } else {
            destinationConnector = new TestDispatcher();
            connectorProperties = new TestDispatcherProperties();

            dispatcherConnectorProperties = ((TestDispatcherProperties) connectorProperties).getDestinationConnectorProperties();
            dispatcherConnectorProperties.setQueueEnabled(queueEnabled);
            dispatcherConnectorProperties.setSendFirst(queueSendFirst);
            dispatcherConnectorProperties.setRegenerateTemplate(queueRegenerate);
            dispatcherConnectorProperties.setRetryIntervalMillis(100);

            if (retryCount > 0) {
                dispatcherConnectorProperties.setRetryCount(retryCount);
                ((TestDispatcher) destinationConnector).setReturnStatus(Status.QUEUED);
            }
        }

        TestUtils.initDefaultDestinationConnector(destinationConnector, connectorProperties);
        destinationConnector.setChannelId(channelId);

        DestinationChain chain = new DestinationChain();
        chain.setChannelId(channelId);
        chain.setMetaDataReplacer(sourceConnector.getMetaDataReplacer());
        chain.setMetaDataColumns(channel.getMetaDataColumns());
        chain.addDestination(1, TestUtils.createDefaultFilterTransformerExecutor(), destinationConnector);
        channel.addDestinationChain(chain);

        channel.deploy();
        channel.start();
        ChannelController.getInstance().deleteAllMessages(channel.getChannelId());

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            connection = TestUtils.getConnection();
            
            for (int i = 1; i <= TEST_SIZE; i++) {
                DispatchResult dispatchResult = sourceConnector.readTestMessage(testMessage);
                sourceConnector.finishDispatch(dispatchResult);
                
                if (retryCount > 0) {
                    // assert that the connector attempted to send the message the correct number of times
                    assertEquals(new Integer(dispatcherConnectorProperties.getRetryCount() + 1), TestUtils.getSendAttempts(channel.getChannelId(), dispatchResult.getMessageId()));
                } else {
                    if (queueNull || !queueEnabled || queueSendFirst) {
                        // Assert that the sent content was stored
                        long localChannelId = ChannelController.getInstance().getLocalChannelId(channel.getChannelId());
                        statement = connection.prepareStatement("SELECT * FROM d_mc" + localChannelId + " WHERE message_id = ? AND metadata_id = ? AND content_type = ?");
                        statement.setLong(1, dispatchResult.getMessageId());
                        statement.setInt(2, 1);
                        statement.setInt(3, ContentType.SENT.getContentTypeCode());
                        result = statement.executeQuery();
                        assertTrue(result.next());
                        result.close();
                        statement.close();

                        // Assert that the send attempts is at least one
                        assertTrue(TestUtils.getSendAttempts(channel.getChannelId(), dispatchResult.getMessageId()) > 0);
                    }
                }

                if (!queueNull && queueEnabled) {
                    // Assert that the message was placed in the destination connector queue
                    assertEquals(i, destinationConnector.getQueue().size());
                }
            }
        } finally {
            TestUtils.close(result);
            TestUtils.close(statement);
            TestUtils.close(connection);
        }

        channel.stop();
        channel.undeploy();
        ChannelController.getInstance().removeChannel(channel.getChannelId());
    }

    /*
     * Create channel where the response transformer blocks the thread
     * Send messages in asynchronous thread (so that the response transformer is
     * waiting), assert that:
     * - The destination connector response content was stored
     * - The message status was updated to PENDING in the database
     * 
     * Then allow the response transformer to finish, join the thread, and
     * assert:
     * - The response transformer was successfully run
     * - The message status was updated to SENT in the database
     */
    @Test
    public final void testAfterSend() throws Exception {
        ChannelController.getInstance().getLocalChannelId(channelId);

        final TestChannel channel = new TestChannel();

        channel.setChannelId(channelId);
        channel.setServerId(serverId);
        channel.setEnabled(true);

        channel.setPreProcessor(new TestPreProcessor());
        channel.setPostProcessor(new TestPostProcessor());

        final TestSourceConnector sourceConnector = (TestSourceConnector) TestUtils.createDefaultSourceConnector();
        sourceConnector.setChannelId(channel.getChannelId());
        sourceConnector.setChannel(channel);
        channel.setSourceConnector(sourceConnector);
        channel.setSourceFilterTransformer(TestUtils.createDefaultFilterTransformerExecutor());

        final ConnectorProperties connectorProperties = new TestDispatcherProperties();
        ((TestDispatcherProperties) connectorProperties).getDestinationConnectorProperties().setQueueEnabled(true);
        ((TestDispatcherProperties) connectorProperties).getDestinationConnectorProperties().setSendFirst(true);
        ((TestDispatcherProperties) connectorProperties).getDestinationConnectorProperties().setRegenerateTemplate(true);

        final DestinationConnector destinationConnector = new TestDispatcher();
        TestUtils.initDefaultDestinationConnector(destinationConnector, connectorProperties);
        destinationConnector.setChannelId(channelId);
        ((TestDispatcher) destinationConnector).setReturnStatus(Status.SENT);

        class BlockingTestResponseTransformer extends TestResponseTransformer {
            public volatile boolean waiting = true;

            @Override
            public String doTransform(Response response, ConnectorMessage connectorMessage) throws DonkeyException, InterruptedException {
                while (waiting) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return super.doTransform(response, connectorMessage);
            }
        }
        final BlockingTestResponseTransformer responseTransformer = new BlockingTestResponseTransformer();
        
        destinationConnector.setResponseTransformerExecutor(TestUtils.createDefaultResponseTransformerExecutor());
        destinationConnector.getResponseTransformerExecutor().setResponseTransformer(responseTransformer);

        DestinationChain chain = new DestinationChain();
        chain.setChannelId(channelId);
        chain.setMetaDataReplacer(sourceConnector.getMetaDataReplacer());
        chain.setMetaDataColumns(channel.getMetaDataColumns());
        chain.addDestination(1, TestUtils.createDefaultFilterTransformerExecutor(), destinationConnector);
        channel.addDestinationChain(chain);

        if (ChannelController.getInstance().channelExists(channelId)) {
            ChannelController.getInstance().deleteAllMessages(channelId);
        }
        
        channel.deploy();
        channel.start();

        class TempClass {
            public long messageId;
        }
        final TempClass tempClass = new TempClass();

        for (int i = 1; i <= TEST_SIZE; i++) {
            responseTransformer.waiting = true;

            Thread thread = new Thread() {
                @Override
                public void run() {
                    ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId()).getConnectorMessages().get(0);
                    tempClass.messageId = sourceMessage.getMessageId();

                    try {
                        channel.process(sourceMessage, false);
                    } catch (InterruptedException e) {
                        throw new AssertionError(e);
                    }
                }
            };
            thread.start();

            Thread.sleep(100);
            // Assert that the response content was stored
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet result = null;
            
            try {
                connection = TestUtils.getConnection();
                long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);
                statement = connection.prepareStatement("SELECT * FROM d_mc" + localChannelId + " WHERE message_id = ? AND metadata_id = ? AND content_type = ?");
                statement.setLong(1, tempClass.messageId);
                statement.setInt(2, 1);
                statement.setInt(3, ContentType.SENT.getContentTypeCode());
                result = statement.executeQuery();
                assertTrue(result.next());
                result.close();
                statement.close();
    
                // Assert that the message status was updated to PENDING
                statement = connection.prepareStatement("SELECT * FROM d_mm" + localChannelId + " WHERE message_id = ? AND id = ? AND status = ?");
                statement.setLong(1, tempClass.messageId);
                statement.setInt(2, 1);
                statement.setString(3, String.valueOf(Status.PENDING.getStatusCode()));
                result = statement.executeQuery();
                assertTrue(result.next());
                result.close();
                statement.close();
    
                responseTransformer.waiting = false;
                thread.join();
    
                // Assert that the response transformer was run
                assertTrue(responseTransformer.isTransformed());
    
                // Assert that the message status was updated to SENT
                statement = connection.prepareStatement("SELECT * FROM d_mm" + localChannelId + " WHERE message_id = ? AND id = ? AND status = ?");
                statement.setLong(1, tempClass.messageId);
                statement.setInt(2, 1);
                statement.setString(3, String.valueOf(Status.SENT.getStatusCode()));
                result = statement.executeQuery();
                assertTrue(result.next());
                result.close();
                statement.close();
            } finally {
                TestUtils.close(result);
                TestUtils.close(statement);
                TestUtils.close(connection);
            }
        }

        channel.stop();
        channel.undeploy();
        //ChannelController.getInstance().removeChannel(channel.getChannelId());
    }

    /*
     * Create new channel where the response transformer changes the message and
     * status of the Response object
     * If the response status was changed to QUEUED and queuing is not enabled,
     * or if the status was changed to something invalid
     * (RECEIVED/TRANSFORMED/PENDING), then assume that it was changed to ERROR
     * 
     * Send messages, assert that:
     * - The processed response was stored
     * - The destination entry in the response map was overwritten
     * - The connector message status was changed based on the response status
     * 
     * Do the above steps for all statuses
     */
    @Test
    public final void testRunResponseTransformer() throws Exception {
        for (Status status : Status.values()) {
            testRunResponseTransformer(status);
        }
    }

    private void testRunResponseTransformer(Status responseStatus) throws Exception {
        final Response testResponse = new Response(responseStatus, TestUtils.TEST_HL7_ACK);
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId);

        Response finalResponse = new Response(testResponse.getStatus(), testResponse.getMessage());
        if (finalResponse.getStatus() != Status.ERROR && finalResponse.getStatus() != Status.SENT && finalResponse.getStatus() != Status.QUEUED) {
            // If the response is invalid for a final destination finalResponse.getStatus(), change the status to ERROR
            finalResponse.setStatus(Status.ERROR);
        } else if (channel.getDestinationConnector(1).getConnectorProperties() instanceof DestinationConnectorPropertiesInterface) {
            // If the destination connector isn't queuing, and the response status is QUEUED, then it should have changed to ERROR
            DestinationConnectorProperties destinationConnectorProperties = ((DestinationConnectorPropertiesInterface) channel.getDestinationConnector(1).getConnectorProperties()).getDestinationConnectorProperties();
            if ((destinationConnectorProperties == null || !destinationConnectorProperties.isQueueEnabled()) && finalResponse.getStatus() == Status.QUEUED) {
                finalResponse.setStatus(Status.ERROR);
            }
        } else if (finalResponse.getStatus() == Status.QUEUED) {
            // If the destination connector isn't queuing, and the response status is QUEUED, then it should have changed to ERROR
            finalResponse.setStatus(Status.ERROR);
        }

        class TestResponseTransformer2 extends TestResponseTransformer {
            @Override
            public String doTransform(Response response, ConnectorMessage connectorMessage) throws DonkeyException, InterruptedException {
                response.setMessage(testResponse.getMessage());
                response.setStatus(testResponse.getStatus());
                connectorMessage.getResponseTransformed().setContent(testResponse.getMessage());
                return super.doTransform(response, connectorMessage);
            }
        }
        channel.getDestinationConnector(1).getResponseTransformerExecutor().setResponseTransformer(new TestResponseTransformer2());

        //ChannelController.getInstance().deleteAllMessages(channel.getChannelId());
        channel.deploy();
        channel.start();

        for (int i = 1; i <= TEST_SIZE; i++) {
            DispatchResult messageResponse = ((TestSourceConnector) channel.getSourceConnector()).readTestMessage(testMessage);
            Serializer serializer = Donkey.getInstance().getSerializer();
            String responseString = serializer.serialize(finalResponse);
            
            // Assert that the processed response was stored
            MessageContent messageContent = new MessageContent(channel.getChannelId(), messageResponse.getMessageId(), 1, ContentType.PROCESSED_RESPONSE, responseString, null, false);
            TestUtils.assertMessageContentExists(messageContent);

            // Assert that the entry in the response map was overwritten
            Map<String, Object> responseMap = TestUtils.getResponseMap(channel.getChannelId(), messageResponse.getMessageId(), 1);
            assertTrue(responseMap.get("d1").equals(finalResponse));

            // Assert that the message status was changed
            TestUtils.assertConnectorMessageStatusEquals(channel.getChannelId(), messageResponse.getMessageId(), 1, finalResponse.getStatus());
        }

        channel.stop();
        channel.undeploy();
        //ChannelController.getInstance().removeChannel(channel.getChannelId());
    }
}
