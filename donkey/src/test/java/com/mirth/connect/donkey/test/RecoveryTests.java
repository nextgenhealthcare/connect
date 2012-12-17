/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;
import com.mirth.connect.donkey.server.data.buffered.BufferedDaoFactory;
import com.mirth.connect.donkey.server.data.timed.TimedDaoFactory;
import com.mirth.connect.donkey.test.util.TestChannel;
import com.mirth.connect.donkey.test.util.TestDestinationConnector;
import com.mirth.connect.donkey.test.util.TestSourceConnector;
import com.mirth.connect.donkey.test.util.TestUtils;
import com.mirth.connect.donkey.util.ActionTimer;

public class RecoveryTests {
    private static int TEST_SIZE = 10;
    private static String channelId = TestUtils.DEFAULT_CHANNEL_ID;
    private static String serverId = TestUtils.DEFAULT_SERVER_ID;
    private static RawMessage testMessage = new RawMessage(TestUtils.TEST_HL7_MESSAGE);
    private static ActionTimer daoTimer = new ActionTimer();
    private static DonkeyDaoFactory daoFactory;
    private Logger logger = Logger.getLogger(this.getClass());

    @BeforeClass
    final public static void beforeClass() throws StartException {
        Donkey donkey = Donkey.getInstance();
        donkey.startEngine(TestUtils.getDonkeyTestConfiguration());

        daoFactory = new BufferedDaoFactory(new TimedDaoFactory(donkey.getDaoFactory(), daoTimer));
        donkey.setDaoFactory(daoFactory);
    }

    @AfterClass
    final public static void afterClass() throws StartException {
        Donkey.getInstance().stopEngine();
    }

    @Before
    final public void before() {
        daoTimer.reset();
    }

    @Test
    public final void testSourceRecovery() throws Exception {
        final int testSize = TEST_SIZE;

        TestChannel channel = TestUtils.createDefaultChannel(channelId, serverId);
        
        List<Long> messageIds = new ArrayList<Long>();
        
        // Add a bunch of RECEIVED source connector messages
        for (int i = 0; i < testSize; i++) {
            messageIds.add(TestUtils.createAndStoreNewMessage(testMessage, channelId, serverId, daoFactory).getMessageId());
        }

        channel.deploy();
        channel.start();
        channel.processUnfinishedMessages();
        channel.stop();
        channel.undeploy();

        TestDestinationConnector testDestinationConnector = (TestDestinationConnector) channel.getDestinationChains().get(0).getDestinationConnectors().get(1);

        List<Long> receivedMessageIds = testDestinationConnector.getMessageIds();

        // Test that the number of recovered messages matches the number of messages sent to the destination connector
        assertEquals(testSize, receivedMessageIds.size());

        // Test that the messages are recovered in the same order that they were sent
        for (int i = 0; i < testSize; i++) {
            assertEquals(messageIds.get(i), receivedMessageIds.get(i));
        }
    }

    @Test
    public final void testDestinationReceivedRecovery() throws Exception {
        final int testSize = TEST_SIZE;
        List<Long> messageIds = new ArrayList<Long>();

        // Channel should have two chains with destination connector metaDataIds 1, 2 and 3, 4
        TestChannel channel = TestUtils.createDefaultChannel(channelId, serverId, true, 2, 2);

        for (int i = 0; i < testSize; i++) {
            ConnectorMessage message = TestUtils.createAndStoreNewMessage(testMessage, channelId, serverId, daoFactory).getConnectorMessages().get(0);
            long messageId = message.getMessageId();
            message.setEncoded(message.getRaw());
            message.getEncoded().setContentType(ContentType.ENCODED);
            message.setStatus(Status.TRANSFORMED);
            
            DonkeyDao dao = null;
            
            try {
                dao = daoFactory.getDao();
                dao.storeMessageContent(message.getEncoded());
                dao.updateStatus(message, Status.RECEIVED);
    
                // create incomplete messages for destinations 1 and 4
                createDestinationMessage(dao, message, 1, Status.RECEIVED);
                createDestinationMessage(dao, message, 3, Status.SENT);
                createDestinationMessage(dao, message, 4, Status.RECEIVED);
                
                dao.commit();
            } finally {
                TestUtils.close(dao);
            }

            messageIds.add(messageId);
        }

        channel.deploy();
        channel.start();
        List<Message> recoveredMessages = channel.processUnfinishedMessages();
        channel.stop();
        channel.undeploy();

        TestDestinationConnector testDestinationConnector = (TestDestinationConnector) channel.getDestinationChains().get(0).getDestinationConnectors().get(1);
        List<Long> receivedMessageIds = testDestinationConnector.getMessageIds();

        // test that the correct number of messages were sent from the destination connector and were recovered by the channel
        assertEquals(testSize, receivedMessageIds.size());
        assertEquals(testSize, recoveredMessages.size());

        for (int i = 0; i < testSize; i++) {
            // test that the sent and recovered messages were processed in the correct order
            assertEquals(messageIds.get(i), receivedMessageIds.get(i));
            assertEquals(messageIds.get(i), (Long) recoveredMessages.get(i).getMessageId());
        }
    }

    @Test
    public final void testDestinationPendingRecovery() throws Exception {
        final int testSize = TEST_SIZE;

        List<Long> messageIds = new ArrayList<Long>();

        // channel should have two chains with metaDataIds 1, 2 and 3, 4
        TestChannel channel = TestUtils.createDefaultChannel(channelId, serverId, true, 2, 2);
        long localChannelId = ChannelController.getInstance().getLocalChannelId(channel.getChannelId());
        
        for (int i = 0; i < testSize; i++) {
            ConnectorMessage message = TestUtils.createAndStoreNewMessage(testMessage, channelId, serverId, daoFactory).getConnectorMessages().get(0);
            long messageId = message.getMessageId();
            message.setEncoded(message.getRaw());
            message.getEncoded().setContentType(ContentType.ENCODED);
            message.setStatus(Status.TRANSFORMED);
            
            DonkeyDao dao = null;
            
            try {
                dao = daoFactory.getDao();
                dao.storeMessageContent(message.getEncoded());
                dao.updateStatus(message, Status.RECEIVED);
    
                // create incomplete messages for destinations 1 and 4
                createDestinationMessage(dao, message, 1, Status.RECEIVED);
                createDestinationMessage(dao, message, 3, Status.SENT);
                createDestinationMessage(dao, message, 4, Status.PENDING);
                
                dao.commit();
            } finally {
                TestUtils.close(dao);
            }

            messageIds.add(messageId);
        }

        // test that destination 1 has testSize SENT messages
        assertEquals((Integer) testSize, getMessageCount(localChannelId, 1, Status.RECEIVED));

        // test that destination 3 has testSize SENT messages
        assertEquals((Integer) testSize, getMessageCount(localChannelId, 3, Status.SENT));

        // test that destination 4 has testSize PENDING messages
        assertEquals((Integer) testSize, getMessageCount(localChannelId, 4, Status.PENDING));

        channel.deploy();
        channel.start();
        List<Message> recoveredMessages = channel.processUnfinishedMessages();
        channel.stop();
        channel.undeploy();

        TestSourceConnector testSourceConnector = (TestSourceConnector) channel.getSourceConnector();

        List<DispatchResult> recoveredResponses = testSourceConnector.getRecoveredDispatchResults();

        // test that the correct number of messages were sent from the destination connector and were recovered by the channel
        assertEquals(testSize, recoveredResponses.size());
        assertEquals(testSize, recoveredMessages.size());

        for (int i = 0; i < testSize; i++) {
            // test that the sent and recovered messages were processed in the correct order
            assertEquals(messageIds.get(i), (Long) recoveredResponses.get(i).getMessageId());
            assertEquals(messageIds.get(i), (Long) recoveredMessages.get(i).getMessageId());
        }

        // test that each destination has testSize SENT messages
        assertEquals((Integer) testSize, getMessageCount(localChannelId, 1, Status.SENT));
        assertEquals((Integer) testSize, getMessageCount(localChannelId, 2, Status.SENT));
        assertEquals((Integer) testSize, getMessageCount(localChannelId, 3, Status.SENT));
        assertEquals((Integer) testSize, getMessageCount(localChannelId, 4, Status.SENT));
    }

    /*
     * Start up new channel, send messages that do not get marked as processed,
     * add each message ID to a list, and assert that:
     * - Each message in the list has a processed flag of false in the database
     * 
     * Call processUnfinishedMessages, then call storeMessageResponse for each
     * recovered response, and assert that:
     * - Each message in the list has a processed flag of true in the database
     */
    @Test
    public final void testProcessedFalseRecovery() throws Exception {
        TestChannel channel = TestUtils.createDefaultChannel(channelId, serverId, true, 2, 2);
        List<Long> messageIds = new ArrayList<Long>();

        try {
            logger.info("Testing recovery on processed = false...");

            channel.deploy();
            channel.start();

            for (int i = 1; i <= TEST_SIZE; i++) {
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(testMessage, channel.getChannelId(), channel.getServerId(), daoFactory).getConnectorMessages().get(0);
                Message finalMessage = channel.process(sourceMessage, false);
                messageIds.add(finalMessage.getMessageId());
            }

            channel.stop();
            channel.undeploy();

            for (Long messageId : messageIds) {
                assertFalse(TestUtils.isMessageProcessed(channel.getChannelId(), messageId));
            }

            channel.deploy();
            channel.start();
            channel.processUnfinishedMessages();
            
            for (DispatchResult dispatchResult : ((TestSourceConnector) channel.getSourceConnector()).getRecoveredDispatchResults()) {
                channel.getSourceConnector().finishDispatch(dispatchResult);
            }

            for (Long messageId : messageIds) {
                assertTrue(TestUtils.isMessageProcessed(channel.getChannelId(), messageId));
            }

            System.out.println(daoTimer.getLog());
        } finally {
            channel.stop();
            channel.undeploy();
            ChannelController.getInstance().removeChannel(channel.getChannelId());
        }
    }

    private void createDestinationMessage(DonkeyDao dao, ConnectorMessage sourceMessage, int metaDataId, Status status) {
        final String responseContent = "SENT:test response";

        ConnectorMessage destinationMessage = new ConnectorMessage(sourceMessage.getChannelId(), sourceMessage.getMessageId(), metaDataId, sourceMessage.getServerId(), Calendar.getInstance(), status);
        destinationMessage.setChannelMap(sourceMessage.getChannelMap());
        dao.insertConnectorMessage(destinationMessage, true);

        destinationMessage.setContent(new MessageContent(sourceMessage.getChannelId(), sourceMessage.getMessageId(), metaDataId, ContentType.RAW, sourceMessage.getEncoded().getContent(), null, null));
        destinationMessage.setContent(new MessageContent(sourceMessage.getChannelId(), sourceMessage.getMessageId(), metaDataId, ContentType.PROCESSED_RAW, sourceMessage.getEncoded().getContent(), null, null));

        dao.storeMessageContent(destinationMessage.getRaw());
        dao.storeMessageContent(destinationMessage.getProcessedRaw());

        if (status == Status.PENDING || status == Status.SENT) {
            destinationMessage.setContent(new MessageContent(sourceMessage.getChannelId(), sourceMessage.getMessageId(), metaDataId, ContentType.TRANSFORMED, sourceMessage.getEncoded().getContent(), null, null));
            destinationMessage.setContent(new MessageContent(sourceMessage.getChannelId(), sourceMessage.getMessageId(), metaDataId, ContentType.ENCODED, sourceMessage.getEncoded().getContent(), null, null));
            destinationMessage.setContent(new MessageContent(sourceMessage.getChannelId(), sourceMessage.getMessageId(), metaDataId, ContentType.SENT, sourceMessage.getEncoded().getContent(), null, null));
            destinationMessage.setContent(new MessageContent(sourceMessage.getChannelId(), sourceMessage.getMessageId(), metaDataId, ContentType.RESPONSE, responseContent, null, null));

            dao.storeMessageContent(destinationMessage.getTransformed());
            dao.storeMessageContent(destinationMessage.getEncoded());
            dao.storeMessageContent(destinationMessage.getSent());
            dao.storeMessageContent(destinationMessage.getResponse());

            if (status == Status.SENT) {
                destinationMessage.setContent(new MessageContent(sourceMessage.getChannelId(), sourceMessage.getMessageId(), metaDataId, ContentType.PROCESSED_RESPONSE, responseContent, null, null));
                dao.storeMessageContent(destinationMessage.getProcessedResponse());
            }
        }
    }

    private Integer getMessageCount(long localChannelId, int metaDataId, Status status) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        
        try {
            connection = TestUtils.getConnection();
            statement = connection.prepareStatement("SELECT COUNT(*) FROM d_mm" + localChannelId + " WHERE status = ? AND id = ?");
            statement.setString(1, Character.toString(status.getStatusCode()));
            statement.setInt(2, metaDataId);
            result = statement.executeQuery();
            result.next();
            int count = result.getInt(1);
            result.close();
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            TestUtils.close(result);
            TestUtils.close(statement);
            TestUtils.close(connection);
        }
    }
}
