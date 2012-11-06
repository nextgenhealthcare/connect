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
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.server.controllers.MessageController;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.timed.TimedDaoFactory;
import com.mirth.connect.donkey.server.queue.ConnectorMessageQueueDataSource;
import com.mirth.connect.donkey.test.util.TestChannel;
import com.mirth.connect.donkey.test.util.TestSourceConnector;
import com.mirth.connect.donkey.test.util.TestUtils;
import com.mirth.connect.donkey.util.ActionTimer;

public class MessageControllerTests {
    private static int TEST_SIZE = 20;
    private static String channelId = TestUtils.DEFAULT_CHANNEL_ID;
    private static String serverId = TestUtils.DEFAULT_SERVER_ID;
    private static String testMessage = TestUtils.TEST_HL7_MESSAGE;
    private static ActionTimer daoTimer = new ActionTimer();
    private Logger logger = Logger.getLogger(this.getClass());

    @BeforeClass
    final public static void beforeClass() throws StartException {
        Donkey donkey = Donkey.getInstance();
        donkey.startEngine(TestUtils.getDonkeyTestConfiguration());
        donkey.setDaoFactory(new TimedDaoFactory(donkey.getDaoFactory(), daoTimer));
    }

    @AfterClass
    final public static void afterClass() throws StartException {
        Donkey.getInstance().stopEngine();
    }

    @Before
    final public void before() {
        daoTimer.reset();
    }

    /*
     * Deploy two new channels
     * Send messages, alternating between channel1 and channel2
     * After each dispatch, assert that:
     * - The maximum message ID from both channel message tables (plus one) is
     * equal to the ID returned by getNextMessageId
     */
    @Test
    public final void testGetNextMessageId() throws Exception {
        Channel channel1 = TestUtils.createDefaultChannel(channelId + "1", serverId);
        Channel channel2 = TestUtils.createDefaultChannel(channelId + "2", serverId);

        try {
            logger.info("Testing MessageController.getNextMessageId...");

            channel1.deploy();
            channel1.start();
            channel2.deploy();
            channel2.start();

            // Send messages
            for (int i = 1; i <= TEST_SIZE; i++) {
                ((TestSourceConnector) channel1.getSourceConnector()).readTestMessage(testMessage);

                // Assert that the next message ID matches the one returned from the message controller
                assertNextMessageIdCorrect(channel1.getChannelId());

                ((TestSourceConnector) channel2.getSourceConnector()).readTestMessage(testMessage);

                // Assert that the next message ID matches the one returned from the message controller
                assertNextMessageIdCorrect(channel2.getChannelId());
            }

            System.out.println(daoTimer.getLog());
        } finally {
            channel1.stop();
            channel1.undeploy();
            channel2.stop();
            channel2.undeploy();
            ChannelController.getInstance().removeChannel(channel1.getChannelId());
            ChannelController.getInstance().removeChannel(channel2.getChannelId());
        }
    }

    private void assertNextMessageIdCorrect(String channelId) throws SQLException {
        Connection connection = TestUtils.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT MAX(id) FROM d_m" + ChannelController.getInstance().getLocalChannelId(channelId));
        ResultSet result = statement.executeQuery();
        result.next();
        Long maxId = result.getLong(1);
        connection.close();
        assertEquals(new Long(maxId + 1), Long.valueOf(MessageController.getInstance().getNextMessageId(channelId)));
    }

    /*
     * Create source connector messages
     * For each message created, assert that:
     * - The raw content was set
     * - The channel map was set
     * - The Message was inserted into the database
     * - The source ConnectorMessage was inserted into the database
     * - The source raw MessageContent was inserted into the database
     */
    @Test
    public void testCreateNewMessage() throws Exception {
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId);
        ChannelController.getInstance().deleteAllMessages(channel.getChannelId());
        long localChannelId = ChannelController.getInstance().getLocalChannelId(channel.getChannelId());

        Map<String, Object> channelMap = new HashMap<String, Object>();
        channelMap.put("key", "value");

        try {
            logger.info("Testing MessageController.createNewMessage...");

            for (int i = 1; i <= TEST_SIZE; i++) {
                DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage, null, channelMap), channel.getChannelId(), channel.getServerId(), dao).getConnectorMessages().get(0);
                dao.commit();
                dao.close();

                // Assert that the raw content was set
                assertTrue(sourceMessage.getRaw().getContent().equals(testMessage));

                // Assert that the channel map was set
                assertTrue(sourceMessage.getChannelMap().containsKey("key"));

                // Assert that the Message was inserted into the database
                Connection connection = TestUtils.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM d_m" + localChannelId + " WHERE id = ?");
                statement.setLong(1, sourceMessage.getMessageId());
                ResultSet result = statement.executeQuery();
                assertTrue(result.next());

                result.close();
                connection.close();

                // Assert that the source ConnectorMessage was inserted into the database
                TestUtils.assertConnectorMessageExists(sourceMessage, false);

                // Assert that the source raw MessageContent was inserted into the database
                TestUtils.assertMessageContentExists(sourceMessage.getRaw());
            }

            System.out.println(daoTimer.getLog());
        } finally {
            ChannelController.getInstance().removeChannel(channel.getChannelId());
        }
    }

    /*
     * Create new Message objects, each with their own discrete connector
     * messages and message content, etc.
     * 
     * Import each message, and assert that:
     * - The Message row was inserted (d_m#)
     * - Each ConnectorMessage row was inserted (d_mm#)
     * - Each MessageContent row was inserted (d_mc#)
     */
    @Test
    public void testImportMessage() throws Exception {
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId);
        ChannelController.getInstance().deleteAllMessages(channel.getChannelId());

        Message message = new Message();

        message.setChannelId(channel.getChannelId());
        message.setMessageId((long) 1);
        message.setDateCreated(Calendar.getInstance());
        message.setProcessed(false);
        message.setServerId(serverId);

        Map<String, Object> connectorMap = new HashMap<String, Object>();
        connectorMap.put("key", "value");

        Map<String, Object> channelMap = new HashMap<String, Object>();
        channelMap.put("key", "value");

        Map<String, Response> responseMap = new HashMap<String, Response>();
        responseMap.put("key", new Response(Status.SENT, "message"));

        try {
            logger.info("Testing MessageController.importMessage...");

            for (int i = 1; i <= 10; i++) {
                ConnectorMessage connectorMessage = new ConnectorMessage(message.getChannelId(), message.getMessageId(), i, message.getServerId(), Calendar.getInstance(), Status.RECEIVED);
                connectorMessage.setConnectorMap(connectorMap);
                connectorMessage.setChannelMap(channelMap);
                connectorMessage.setResponseMap(responseMap);
                connectorMessage.setErrors("errors");

                message.getConnectorMessages().put(i, connectorMessage);
            }

            for (int i = 1; i <= TEST_SIZE; i++) {
                message.setMessageId((long) i);
                for (ConnectorMessage connectorMessage : message.getConnectorMessages().values()) {
                    connectorMessage.setMessageId(message.getMessageId());
                    connectorMessage.setContent(new MessageContent(channel.getChannelId(), message.getMessageId(), connectorMessage.getMetaDataId(), ContentType.RAW, testMessage, false));
                    connectorMessage.setContent(new MessageContent(channel.getChannelId(), message.getMessageId(), connectorMessage.getMetaDataId(), ContentType.PROCESSED_RAW, testMessage, false));
                    connectorMessage.setContent(new MessageContent(channel.getChannelId(), message.getMessageId(), connectorMessage.getMetaDataId(), ContentType.TRANSFORMED, testMessage, false));
                    connectorMessage.setContent(new MessageContent(channel.getChannelId(), message.getMessageId(), connectorMessage.getMetaDataId(), ContentType.ENCODED, testMessage, false));
                    connectorMessage.setContent(new MessageContent(channel.getChannelId(), message.getMessageId(), connectorMessage.getMetaDataId(), ContentType.SENT, testMessage, false));
                    connectorMessage.setContent(new MessageContent(channel.getChannelId(), message.getMessageId(), connectorMessage.getMetaDataId(), ContentType.RESPONSE, testMessage, false));
                    connectorMessage.setContent(new MessageContent(channel.getChannelId(), message.getMessageId(), connectorMessage.getMetaDataId(), ContentType.PROCESSED_RESPONSE, testMessage, false));
                }

                MessageController.getInstance().importMessage(message);

                // Assert that the message (and recursively all connector messages and content) exists in the database
                TestUtils.assertMessageExists(message, true);
            }

            System.out.println(daoTimer.getLog());
        } finally {
            ChannelController.getInstance().removeChannel(channel.getChannelId());
        }
    }

    @Test
    public void testDeleteMessage() throws Exception {
        TestChannel channel = TestUtils.createDefaultChannel(channelId, serverId);
        channel.getSourceConnector().setWaitForDestinations(false);
        channel.getSourceQueue().setDataSource(new ConnectorMessageQueueDataSource(channelId, 0, Status.RECEIVED, Donkey.getInstance().getDaoFactory()));
        channel.getSourceQueue().updateSize();

        Message message = MessageController.getInstance().createNewMessage(channelId, serverId);
        ConnectorMessage sourceMessage = new ConnectorMessage(channelId, message.getMessageId(), 0, serverId, message.getDateCreated(), Status.RECEIVED);
        sourceMessage.setRaw(new MessageContent(channelId, message.getMessageId(), 0, ContentType.RAW, testMessage, false));
        message.getConnectorMessages().put(0, sourceMessage);

        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();
        dao.insertMessage(message);
        dao.insertConnectorMessage(sourceMessage, true);
        dao.insertMessageContent(sourceMessage.getRaw());
        dao.commit();
        dao.close();

        // put the message in the source queue
        channel.queue(sourceMessage);

        // assert that the message exists in the database
        TestUtils.assertMessageExists(message, true);
        TestUtils.assertConnectorMessageExists(sourceMessage, true);

        // assert that the message exists in the source queue's memory
        assertTrue(channel.getSourceQueue().contains(sourceMessage));

        // delete the message
        Map<Long, Set<Integer>> messages = new HashMap<Long, Set<Integer>>();
        messages.put(message.getMessageId(), null);
        MessageController.getInstance().deleteMessages(channelId, messages, false);
        channel.invalidateQueues();

        // assert that the message does not exist in the database
        TestUtils.assertMessageDoesNotExist(message);
        TestUtils.assertConnectorMessageDoesNotExist(sourceMessage);

        // assert that the message does not exist in the source queue's memory
        assertTrue(!channel.getSourceQueue().contains(sourceMessage));
    }
}
