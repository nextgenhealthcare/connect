/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.test;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage, null, channelMap), channel.getChannelId(), channel.getServerId()).getConnectorMessages().get(0);

                // Assert that the raw content was set
                assertTrue(sourceMessage.getRaw().getContent().equals(testMessage));

                // Assert that the channel map was set
                assertTrue(sourceMessage.getChannelMap().containsKey("key"));

                Connection connection = null;
                PreparedStatement statement = null;
                ResultSet result = null;
                
                // Assert that the Message was inserted into the database
                try {
                    connection = TestUtils.getConnection();
                    statement = connection.prepareStatement("SELECT * FROM d_m" + localChannelId + " WHERE id = ?");
                    statement.setLong(1, sourceMessage.getMessageId());
                    result = statement.executeQuery();
                    assertTrue(result.next());
                } finally {
                    TestUtils.close(result);
                    TestUtils.close(statement);
                    TestUtils.close(connection);
                }

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

    @Test
    public void testDeleteMessage() throws Exception {
        TestChannel channel = TestUtils.createDefaultChannel(channelId, serverId);
        channel.getSourceConnector().setRespondAfterProcessing(false);
        channel.getSourceQueue().setDataSource(new ConnectorMessageQueueDataSource(channelId, serverId, 0, Status.RECEIVED, false, TestUtils.getDaoFactory()));
        channel.getSourceQueue().updateSize();

        Message message = null;
        ConnectorMessage sourceMessage = null;
        DonkeyDao dao = null;
        
        try {
            dao = TestUtils.getDaoFactory().getDao();
            
            message = new Message();
            message.setMessageId(dao.getNextMessageId(channelId));
            message.setChannelId(channelId);
            message.setServerId(serverId);
            message.setReceivedDate(Calendar.getInstance());
            
            sourceMessage = new ConnectorMessage(channelId, message.getMessageId(), 0, serverId, message.getReceivedDate(), Status.RECEIVED);
            sourceMessage.setRaw(new MessageContent(channelId, message.getMessageId(), 0, ContentType.RAW, testMessage, null, false));
            message.getConnectorMessages().put(0, sourceMessage);
            
            dao.insertMessage(message);
            dao.insertConnectorMessage(sourceMessage, true, true);
            dao.insertMessageContent(sourceMessage.getRaw());
            dao.commit();
        } finally {
            TestUtils.close(dao);
        }
        
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
        MessageController.getInstance().deleteMessages(channelId, messages);
        channel.invalidateQueues();

        // assert that the message does not exist in the database
        TestUtils.assertMessageDoesNotExist(message);
        TestUtils.assertConnectorMessageDoesNotExist(sourceMessage);

        // assert that the message does not exist in the source queue's memory
        assertTrue(!channel.getSourceQueue().contains(sourceMessage));
    }
}
