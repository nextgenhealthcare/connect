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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.channel.MetaDataColumnType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.Serializer;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.channel.DestinationChain;
import com.mirth.connect.donkey.server.channel.StorageSettings;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.server.controllers.MessageController;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;
import com.mirth.connect.donkey.server.data.jdbc.JdbcDaoFactory;
import com.mirth.connect.donkey.server.data.timed.TimedDaoFactory;
import com.mirth.connect.donkey.test.util.TestChannel;
import com.mirth.connect.donkey.test.util.TestSourceConnector;
import com.mirth.connect.donkey.test.util.TestUtils;
import com.mirth.connect.donkey.util.ActionTimer;

public class DonkeyDaoTests {
    private static int TEST_SIZE = 10;
    private static String channelId = TestUtils.DEFAULT_CHANNEL_ID;
    private static String serverId = TestUtils.DEFAULT_SERVER_ID;
    private static String testMessage = TestUtils.TEST_HL7_MESSAGE;
    private static Serializer serializer = Donkey.getInstance().getSerializer();
    private static DonkeyDaoFactory daoFactory;
    private static ActionTimer daoTimer = new ActionTimer();

    private Logger logger = Logger.getLogger(this.getClass());

    @BeforeClass
    final public static void beforeClass() throws StartException {
        Donkey donkey = Donkey.getInstance();
        donkey.startEngine(TestUtils.getDonkeyTestConfiguration());

        daoFactory = new TimedDaoFactory(donkey.getDaoFactory(), daoTimer);
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

    /*
     * Create and insert messages, assert that:
     * - The row was inserted correctly
     * - The server ID was inserted correctly
     * - The date_created column was initialized
     * - The processed column was initialized to false
     */
    @Test
    public void testInsertMessage() throws Exception {
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId);
        ChannelController.getInstance().deleteAllMessages(channel.getChannelId());
        long localChannelId = ChannelController.getInstance().getLocalChannelId(channel.getChannelId());

        DonkeyDao dao = daoFactory.getDao();

        try {
            logger.info("Testing DonkeyDao.insertMessage...");

            Message message = new Message();
            message.setChannelId(channel.getChannelId());
            message.setServerId(channel.getServerId());
            message.setDateCreated(Calendar.getInstance());
            message.setProcessed(false);

            for (int i = 1; i <= TEST_SIZE; i++) {
                message.setMessageId(MessageController.getInstance().getNextMessageId(channel.getChannelId()));
                dao.insertMessage(message);
                dao.commit();

                Connection connection = TestUtils.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM d_m" + localChannelId + " WHERE id = ?");
                statement.setLong(1, message.getMessageId());
                ResultSet result = statement.executeQuery();

                // Assert that the row was inserted
                assertTrue(result.next());

                // Assert that the server ID was inserted
                assertEquals(message.getServerId(), result.getString("server_id"));

                // Assert that the date_create column was initialized
                assertNotNull(result.getTimestamp("date_created"));

                // Assert that the processed column was initialized to false
                assertNotNull(result.getBoolean("processed"));
                assertFalse(result.getBoolean("processed"));

                result.close();
                connection.close();
            }

            System.out.println(daoTimer.getLog());
        } finally {
            dao.close();
            ChannelController.getInstance().removeChannel(channel.getChannelId());
        }
    }

    /*
     * Create and insert connector messages
     * For each connector message inserted, assert that:
     * - The metadata row was inserted correctly
     * - The date created was inserted correctly
     * - The status was inserted correctly
     * - The connector map was inserted correctly
     * - The channel map was inserted correctly
     * - The response map was inserted correctly
     * - The errors column was inserted correctly
     * - The send attempts column was initialized correctly
     */
    @Test
    public void testInsertConnectorMessage() throws Exception {
        ChannelController.getInstance().deleteAllMessages(channelId);
        TestUtils.createDefaultChannel(channelId, serverId);

        Map<String, Object> connectorMap = new HashMap<String, Object>();
        connectorMap.put("key", "value");

        Map<String, Object> channelMap = new HashMap<String, Object>();
        channelMap.put("key", "value");

        Map<String, Response> responseMap = new HashMap<String, Response>();
        responseMap.put("key", new Response(Status.SENT, "message"));

        DonkeyDao dao = daoFactory.getDao();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channelId, serverId, dao).getConnectorMessages().get(0);
            dao.commit();

            ConnectorMessage connectorMessage = new ConnectorMessage(channelId, sourceMessage.getMessageId(), 1, serverId, Calendar.getInstance(), Status.RECEIVED);

            connectorMessage.setConnectorMap(connectorMap);
            connectorMessage.setChannelMap(channelMap);
            connectorMessage.setResponseMap(responseMap);
            connectorMessage.setErrors("errors");

            double averageTime = 0;

            logger.info("Testing DonkeyDao.insertConnectorMessage...");

            daoTimer.reset();

            for (int i = 1; i <= TEST_SIZE; i++) {
                connectorMessage.setMetaDataId(i);
                dao.insertConnectorMessage(connectorMessage, true);
                dao.commit();

                long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);
                connection = TestUtils.getConnection();
                statement = connection.prepareStatement("SELECT * FROM d_mm" + localChannelId + " WHERE id = ? AND message_id = ?");
                statement.setLong(1, connectorMessage.getMetaDataId());
                statement.setLong(2, connectorMessage.getMessageId());
                result = statement.executeQuery();

                // Assert that the metadata row exists
                assertTrue(result.next());

                // Assert that the date created is correct
                assertEquals(result.getTimestamp("date_created").getTime(), connectorMessage.getDateCreated().getTimeInMillis());

                // Assert that the status is correct
                assertEquals(Status.fromChar(result.getString("status").charAt(0)), connectorMessage.getStatus());

                // Assert that the connector map is correct
                assertTrue(connectorMessage.getConnectorMap().equals((HashMap<String, Object>) serializer.deserialize(result.getString("connector_map"))));

                // Assert that the channel map is correct
                assertTrue(connectorMessage.getChannelMap().equals((HashMap<String, Object>) serializer.deserialize(result.getString("channel_map"))));

                // Assert that the response map is correct
                assertTrue(connectorMessage.getResponseMap().equals((HashMap<String, Response>) serializer.deserialize(result.getString("response_map"))));

                // Assert that the errors column is correct
                assertTrue(connectorMessage.getErrors().equals(serializer.deserialize(result.getString("errors"))));

                // Assert that the send attempts column is correct
                assertEquals(result.getInt("send_attempts"), 0);

                result.close();
                connection.close();
            }

            averageTime = daoTimer.getTotalTime() / TEST_SIZE;
            logger.info("Success, average transaction time: " + averageTime + " ms");
        } finally {
            DbUtils.close(result);
            DbUtils.close(statement);
            DbUtils.close(connection);
            dao.close();
            ChannelController.getInstance().removeChannel(channelId);
        }
    }

    /*
     * Create new channel and connector messages
     * Insert content for the messages and assert that:
     * - Each MessageContent row was inserted correctly
     * 
     * For raw content type, assert that:
     * - An exception was thrown due to a FK constraint
     */
    @Test
    public final void testInsertMessageContent() throws Exception {
        ChannelController.getInstance().deleteAllMessages(channelId);
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId);

        DonkeyDao dao = daoFactory.getDao();

        try {
            logger.info("Testing DonkeyDao.insertMessageContent...");

            for (int i = 1; i <= TEST_SIZE; i++) {
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), dao).getConnectorMessages().get(0);
                dao.commit();

                for (ContentType contentType : ContentType.values()) {
                    MessageContent messageContent = new MessageContent(channel.getChannelId(), sourceMessage.getMessageId(), sourceMessage.getMetaDataId(), contentType, testMessage, null);
                    sourceMessage.setContent(messageContent);

                    if (contentType != ContentType.RAW) {
                        dao.insertMessageContent(messageContent);
                        dao.commit();
                        // Assert that the content was inserted
                        TestUtils.assertMessageContentExists(messageContent);
                    } else {
                        Exception e = null;
                        try {
                            dao.insertMessageContent(messageContent);
                        } catch (Exception e2) {
                            dao.rollback();
                            e = e2;
                        }
                        // Assert that an exception was thrown
                        assertNotNull(e);
                    }
                }
            }

            System.out.println(daoTimer.getLog());
        } finally {
            dao.close();
        }

        ChannelController.getInstance().removeChannel(channel.getChannelId());
    }

    /*
     * Create new channel and message
     * Insert attachments for the message, assert that:
     * - The attachment was inserted correctly
     */
    @Test
    public final void testInsertMessageAttachment() throws Exception {
        ChannelController.getInstance().deleteAllMessages(channelId);
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId);
        DonkeyDao dao = daoFactory.getDao();

        try {
            logger.info("Testing DonkeyDao.insertMessageAttachment...");

            ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), dao).getConnectorMessages().get(0);
            dao.commit();

            for (int i = 1; i <= TEST_SIZE; i++) {
                Attachment attachment = new Attachment("attachment" + i, testMessage.getBytes(), "text/plain");

                dao.insertMessageAttachment(channel.getChannelId(), sourceMessage.getMessageId(), attachment);
                dao.commit();

                // Assert that the attachment was inserted
                TestUtils.assertAttachmentExists(channel.getChannelId(), sourceMessage.getMessageId(), attachment);
            }

            System.out.println(daoTimer.getLog());
        } finally {
            dao.close();
            ChannelController.getInstance().removeChannel(channel.getChannelId());
        }
    }

    /*
     * Create a new channel, and create metadata columns for it
     * Deploy the channel, assert that:
     * - The columns were successfully added to the table
     * 
     * Create source connector messages
     * Insert source/destination metadata for each message, assert that:
     * - Each of the metadata columns was inserted successfully
     */
    @Test
    public final void testInsertMetaData() throws Exception {
        ChannelController.getInstance().deleteAllMessages(channelId);
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId);

        for (MetaDataColumnType columnType : MetaDataColumnType.values()) {
            channel.getMetaDataColumns().add(new MetaDataColumn(columnType.toString().toLowerCase() + "test", columnType, null));
        }

        channel.deploy();

        // Assert that the columns were added successfully
        assertTrue(channel.getMetaDataColumns().equals(TestUtils.getExistingMetaDataColumns(channel.getChannelId())));

        Map<String, Object> sourceMap = new HashMap<String, Object>();
        sourceMap.put(MetaDataColumnType.BOOLEAN.toString().toLowerCase() + "test", true);
        sourceMap.put(MetaDataColumnType.LONG.toString().toLowerCase() + "test", 1);
        sourceMap.put(MetaDataColumnType.DOUBLE.toString().toLowerCase() + "test", 1.0);
        sourceMap.put(MetaDataColumnType.STRING.toString().toLowerCase() + "test", "testing");
        sourceMap.put(MetaDataColumnType.DATE.toString().toLowerCase() + "test", Calendar.getInstance());
        sourceMap.put(MetaDataColumnType.TIME.toString().toLowerCase() + "test", Calendar.getInstance());
        sourceMap.put(MetaDataColumnType.TIMESTAMP.toString().toLowerCase() + "test", Calendar.getInstance());

        Map<String, Object> destinationMap = new HashMap<String, Object>();
        destinationMap.put(MetaDataColumnType.BOOLEAN.toString().toLowerCase() + "test", false);
        destinationMap.put(MetaDataColumnType.LONG.toString().toLowerCase() + "test", 1);
        destinationMap.put(MetaDataColumnType.STRING.toString().toLowerCase() + "test", "");
        destinationMap.put(MetaDataColumnType.DATE.toString().toLowerCase() + "test", Calendar.getInstance());
        destinationMap.put(MetaDataColumnType.TIMESTAMP.toString().toLowerCase() + "test", Calendar.getInstance());

        logger.info("Testing DonkeyDao.insertMetaData...");

        DonkeyDao dao = daoFactory.getDao();

        try {
            for (int i = 1; i <= TEST_SIZE; i++) {
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), dao).getConnectorMessages().get(0);
                dao.commit();

                sourceMessage.setMetaDataMap(sourceMap);
                dao.insertMetaData(sourceMessage, channel.getMetaDataColumns());
                dao.commit();

                ConnectorMessage destinationMessage = TestUtils.createAndStoreDestinationConnectorMessage(dao, channel.getChannelId(), channel.getServerId(), sourceMessage.getMessageId(), 1, testMessage, Status.RECEIVED);
                destinationMessage.setMetaDataMap(destinationMap);
                dao.insertMetaData(destinationMessage, channel.getMetaDataColumns());
                dao.commit();

                // Assert the custom metadata was inserted correctly
                TestUtils.compareMetaDataMaps(channel.getMetaDataColumns(), sourceMap, TestUtils.getCustomMetaData(channel.getChannelId(), sourceMessage.getMessageId(), 0));
                TestUtils.compareMetaDataMaps(channel.getMetaDataColumns(), destinationMap, TestUtils.getCustomMetaData(channel.getChannelId(), sourceMessage.getMessageId(), 1));
            }

            System.out.println(daoTimer.getLog());
        } finally {
            dao.close();
            channel.undeploy();
            ChannelController.getInstance().removeChannel(channel.getChannelId());
        }
    }

    /*
     * Create new channel and connector messages
     * Insert content for the messages and assert that:
     * - Each MessageContent row was inserted correctly
     * 
     * For raw content type, assert that:
     * - The content was updated correctly
     */
    @Test
    public final void testStoreMessageContent() throws Exception {
        ChannelController.getInstance().deleteAllMessages(channelId);
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId);

        DonkeyDao dao = daoFactory.getDao();

        try {
            logger.info("Testing DonkeyDao.storeMessageContent...");

            for (int i = 1; i <= TEST_SIZE; i++) {
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), dao).getConnectorMessages().get(0);
                dao.commit();

                for (ContentType contentType : ContentType.values()) {
                    MessageContent messageContent = new MessageContent(channel.getChannelId(), sourceMessage.getMessageId(), sourceMessage.getMetaDataId(), contentType, testMessage, null);
                    sourceMessage.setContent(messageContent);
                    dao.storeMessageContent(messageContent);
                    dao.commit();
                    // Assert that the content was inserted
                    TestUtils.assertMessageContentExists(messageContent);
                }
            }

            System.out.println(daoTimer.getLog());
        } finally {
            dao.close();
            ChannelController.getInstance().removeChannel(channel.getChannelId());
        }
    }

    /*
     * Create a new channel, and create connector messages
     * For each message, update the status to ERROR and assert that:
     * - The status was updated in the database
     * 
     * Update the status to TRANSFORMED with a send attempt of 1 and assert
     * that:
     * - The status was updated in the database
     * - The send attempts were updated correctly
     */
    @Test
    public final void testUpdateStatus() throws Exception {
        ChannelController.getInstance().deleteAllMessages(channelId);
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId);

        DonkeyDao dao = daoFactory.getDao();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            logger.info("Testing DonkeyDao.updateStatus...");

            for (int i = 1; i <= TEST_SIZE; i++) {
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), dao).getConnectorMessages().get(0);
                dao.commit();

                sourceMessage.setStatus(Status.ERROR);
                dao.updateStatus(sourceMessage, Status.RECEIVED);
                dao.commit();

                // Assert that the status was updated
                TestUtils.assertConnectorMessageExists(sourceMessage, false);

                sourceMessage.setStatus(Status.TRANSFORMED);
                sourceMessage.setSendAttempts(1);
                dao.updateStatus(sourceMessage, Status.ERROR);
                dao.commit();

                // Assert that the status was updated
                TestUtils.assertConnectorMessageExists(sourceMessage, false);

                // Assert that the send attempts were updated
                long localChannelId = ChannelController.getInstance().getLocalChannelId(channel.getChannelId());
                connection = TestUtils.getConnection();
                statement = connection.prepareStatement("SELECT send_attempts FROM d_mm" + localChannelId + " WHERE message_id = ? AND id = ?");
                statement.setLong(1, sourceMessage.getMessageId());
                statement.setLong(2, sourceMessage.getMetaDataId());
                result = statement.executeQuery();
                result.next();
                assertEquals(1, result.getInt("send_attempts"));
                result.close();
                connection.close();
            }

            System.out.println(daoTimer.getLog());
        } finally {
            DbUtils.close(result);
            DbUtils.close(statement);
            DbUtils.close(connection);
            dao.close();
            ChannelController.getInstance().removeChannel(channel.getChannelId());
        }
    }

    /*
     * Create a new channel, and source connector messages
     * For each message, set the connector, channel, and response map
     * Update the maps, and assert that:
     * - The inserted connector map is equal to the one on the message object
     * - The inserted channel map is equal to the one on the message object
     * - The inserted response map is equal to the one on the message object
     * 
     * For non-serializable values in the connector or channel map, instead of
     * asserting equality, assert that:
     * - The inserted value is equal to the string representation of the value
     * in the map on the message object
     */
    @Test
    public final void testUpdateMaps() throws Exception {
        ChannelController.getInstance().deleteAllMessages(channelId);
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId);
        Connection connection = TestUtils.getConnection();

        Map<String, Object> connectorMap = new HashMap<String, Object>();
        connectorMap.put("key1", "teststring");
        connectorMap.put("key2", Calendar.getInstance());
        connectorMap.put("key3", connection);

        Map<String, Object> channelMap = new HashMap<String, Object>();
        channelMap.put("key1", 12345);
        channelMap.put("key2", null);
        channelMap.put("key3", connection);

        Map<String, Response> responseMap = new HashMap<String, Response>();
        responseMap.put("key1", new Response("message"));
        responseMap.put("key2", new Response(Status.FILTERED, "message"));

        DonkeyDao dao = daoFactory.getDao();

        try {
            logger.info("Testing DonkeyDao.updateMaps...");

            for (int i = 1; i <= TEST_SIZE; i++) {
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), dao).getConnectorMessages().get(0);
                dao.commit();

                sourceMessage.setConnectorMap(connectorMap);
                sourceMessage.setChannelMap(channelMap);
                sourceMessage.setResponseMap(responseMap);
                dao.updateMaps(sourceMessage);
                dao.commit();

                Map<String, Object> insertedConnectorMap = TestUtils.getConnectorMap(channel.getChannelId(), sourceMessage.getMessageId(), sourceMessage.getMetaDataId());
                Map<String, Object> insertedChannelMap = TestUtils.getChannelMap(channel.getChannelId(), sourceMessage.getMessageId(), sourceMessage.getMetaDataId());
                Map<String, Response> insertedResponseMap = TestUtils.getResponseMap(channel.getChannelId(), sourceMessage.getMessageId(), sourceMessage.getMetaDataId());

                // Assert that both connector maps have all the same keys
                assertTrue(connectorMap.keySet().equals(insertedConnectorMap.keySet()));
                for (String key : connectorMap.keySet()) {
                    try {
                        // Test that the object is serializable
                        SerializationUtils.serialize((Serializable) connectorMap.get(key));
                        // Assert that the objects are equal
                        assertTrue(connectorMap.get(key).equals(insertedConnectorMap.get(key)));
                    } catch (Exception e) {
                        // If the object is not serializable, assert that the inserted value is the String representation
                        assertTrue(connectorMap.get(key).toString().equals(insertedConnectorMap.get(key)));
                    }
                }

                // Assert that both channel maps have all the same keys
                assertTrue(channelMap.keySet().equals(insertedChannelMap.keySet()));
                for (String key : channelMap.keySet()) {
                    try {
                        // Test that the object is serializable
                        SerializationUtils.serialize((Serializable) channelMap.get(key));
                        // Assert that the objects are equal
                        assertEquals(channelMap.get(key), insertedChannelMap.get(key));
                    } catch (Exception e) {
                        // If the object is not serializable, assert that the inserted value is the String representation
                        assertEquals(channelMap.get(key).toString(), insertedChannelMap.get(key));
                    }
                }

                // Assert that both response maps are equal
                assertTrue(responseMap.equals(insertedResponseMap));

                // Assert that both response maps are equal
                assertTrue(responseMap.equals(insertedResponseMap));

                // Assert that both response maps are equal
                assertTrue(responseMap.equals(insertedResponseMap));

                // Assert that both response maps are equal
                assertTrue(responseMap.equals(insertedResponseMap));
            }

            System.out.println(daoTimer.getLog());
        } finally {
            dao.close();
            connection.close();
            ChannelController.getInstance().removeChannel(channel.getChannelId());
        }
    }

    /*
     * Create a new channel, and source connector messages
     * For each message, set the response map
     * Update the maps, and assert that:
     * - The inserted response map is equal to the one on the message object
     */
    @Test
    public final void testUpdateResponseMap() throws Exception {
        ChannelController.getInstance().deleteAllMessages(channelId);
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId);

        Map<String, Response> responseMap = new HashMap<String, Response>();
        responseMap.put("key1", new Response("message"));
        responseMap.put("key2", new Response(Status.FILTERED, "message"));

        DonkeyDao dao = daoFactory.getDao();

        try {
            logger.info("Testing DonkeyDao.updateResponseMap...");

            for (int i = 1; i <= TEST_SIZE; i++) {
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), dao).getConnectorMessages().get(0);
                dao.commit();

                sourceMessage.setResponseMap(responseMap);
                dao.updateResponseMap(sourceMessage);
                dao.commit();

                Map<String, Response> insertedResponseMap = TestUtils.getResponseMap(channel.getChannelId(), sourceMessage.getMessageId(), sourceMessage.getMetaDataId());

                // Assert that both response maps are equal
                assertTrue(responseMap.equals(insertedResponseMap));
            }

            System.out.println(daoTimer.getLog());
        } finally {
            dao.close();
            ChannelController.getInstance().removeChannel(channel.getChannelId());
        }
    }

    /*
     * Create a new channel, and source connector messages
     * For each message, assert that:
     * - The message processed flag is false in the database
     * 
     * Then update the message as processed and assert that:
     * - The message processed flag is now true
     */
    @Test
    public final void testMarkAsProcessed() throws Exception {
        ChannelController.getInstance().deleteAllMessages(channelId);
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId);

        DonkeyDao dao = daoFactory.getDao();

        try {
            logger.info("Testing DonkeyDao.markAsProcessed...");

            for (int i = 1; i <= TEST_SIZE; i++) {
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), dao).getConnectorMessages().get(0);
                dao.commit();

                // Assert that the message is initially not processed
                assertFalse(TestUtils.isMessageProcessed(channel.getChannelId(), sourceMessage.getMessageId()));

                dao.markAsProcessed(channel.getChannelId(), sourceMessage.getMessageId());
                dao.commit();

                // Assert that the message is now processed
                assertTrue(TestUtils.isMessageProcessed(channel.getChannelId(), sourceMessage.getMessageId()));
            }

            System.out.println(daoTimer.getLog());
        } finally {
            dao.close();
            ChannelController.getInstance().removeChannel(channel.getChannelId());
        }
    }

    /*
     * Create a new channel
     * Process messages directly through Channel.process, assert that:
     * - Each message was inserted successfully
     * - TEST_SIZE messages were added to the testing list
     * - The channel statistics were updated correctly
     * 
     * Iterate through each message in the list, assert that:
     * - Each message, connector message, and message content row was deleted
     * - The channel statistics were not deleted
     * 
     * Do the same thing as above, except this time delete the channel
     * statistics as well, assert that:
     * - The channel statistics were deleted
     */
    @Test
    public void testDeleteMessage() throws Exception {
        TestChannel channel = TestUtils.createDefaultChannel(channelId, serverId);

        channel.deploy();
        channel.start();

        DonkeyDao dao = daoFactory.getDao();

        try {
            logger.info("Testing DonkeyDao.deleteMessage...");

            // Test deleting messages without deleting statistics
            testDeleteMessage(channel, false, dao);

            dao.close();
            dao = daoFactory.getDao();

            // Test deleting messages with deleting statistics
            testDeleteMessage(channel, true, dao);

            System.out.println(daoTimer.getLog());
        } finally {
            dao.close();
            channel.stop();
            channel.undeploy();
            ChannelController.getInstance().removeChannel(channel.getChannelId());
        }
    }

    private void testDeleteMessage(TestChannel channel, boolean deleteStatistics, DonkeyDao dao) throws Exception {
        List<Message> messages = new ArrayList<Message>();
        ChannelController.getInstance().deleteAllMessages(channel.getChannelId());
        TestUtils.deleteChannelStatistics(channel.getChannelId());

        // Process a bunch of messages through the channel
        for (int i = 1; i <= TEST_SIZE; i++) {
            ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), dao).getConnectorMessages().get(0);
            dao.commit();

            // Bypass the source connector so we can retrieve the Message object
            Message message = channel.process(sourceMessage, true);

            // Assert that each message was successfully created
            TestUtils.assertMessageExists(message, true);
            messages.add(message);
        }

        // Assert that TEST_SIZE messages were added
        assertEquals(TEST_SIZE, messages.size());

        Map<Integer, Map<Status, Long>> channelStats = ChannelController.getInstance().getStatistics().getChannelStats(channelId);

        try {
            // Assert that the statistics were updated, ignore the RECEIVED status for the source/aggregate
            assertNotNull(channelStats);
            assertNotNull(channelStats.get(null));
            assertNotNull(channelStats.get(null).get(Status.TRANSFORMED));
            assertNotNull(channelStats.get(null).get(Status.SENT));
            assertNotNull(channelStats.get(0));
            assertNotNull(channelStats.get(0).get(Status.TRANSFORMED));
            assertNotNull(channelStats.get(1));
            assertNotNull(channelStats.get(1).get(Status.RECEIVED));
            assertNotNull(channelStats.get(1).get(Status.SENT));

            assertEquals(TEST_SIZE, channelStats.get(null).get(Status.TRANSFORMED).intValue());
            assertEquals(TEST_SIZE, channelStats.get(null).get(Status.SENT).intValue());
            assertEquals(TEST_SIZE, channelStats.get(0).get(Status.TRANSFORMED).intValue());
            assertEquals(TEST_SIZE, channelStats.get(1).get(Status.RECEIVED).intValue());
            assertEquals(TEST_SIZE, channelStats.get(1).get(Status.SENT).intValue());
        } catch (AssertionError e) {
            for (Entry<Integer, Map<Status, Long>> entry : channelStats.entrySet()) {
                System.out.printf("metaDataId %-5s: %s\n", entry.getKey(), entry.getValue());
            }

            throw e;
        }

        // Delete all the messages that were processed
        for (Message message : messages) {
            dao.deleteMessage(message.getChannelId(), message.getMessageId(), deleteStatistics);
            dao.commit();

            // Assert that each message was successfully deleted
            for (ConnectorMessage connectorMessage : message.getConnectorMessages().values()) {
                for (ContentType contentType : ContentType.values()) {
                    if (connectorMessage.getContent(contentType) != null) {
                        // Assert that each content row was deleted
                        TestUtils.assertMessageContentDoesNotExist(connectorMessage.getContent(contentType));
                    }
                }

                // Assert that each metadata row was deleted
                TestUtils.assertConnectorMessageDoesNotExist(connectorMessage);
            }

            // Assert that the message row itself was deleted
            TestUtils.assertMessageDoesNotExist(message);
        }

        if (deleteStatistics) {
            // Assert that the statistics were decremented
            channelStats = ChannelController.getInstance().getStatistics().getChannelStats(channelId);
            assertEquals(0, channelStats.get(null).get(Status.RECEIVED).intValue());
            assertEquals(0, channelStats.get(null).get(Status.TRANSFORMED).intValue());
            assertEquals(0, channelStats.get(null).get(Status.SENT).intValue());
            assertEquals(0, channelStats.get(0).get(Status.RECEIVED).intValue());
            assertEquals(0, channelStats.get(0).get(Status.TRANSFORMED).intValue());
            assertEquals(0, channelStats.get(1).get(Status.RECEIVED).intValue());
            assertEquals(0, channelStats.get(1).get(Status.SENT).intValue());
        } else {
            // Assert that the statistics were not deleted, ignore the RECEIVED status for the source/aggregate
            channelStats = ChannelController.getInstance().getStatistics().getChannelStats(channelId);
            assertEquals(TEST_SIZE, channelStats.get(null).get(Status.TRANSFORMED).intValue());
            assertEquals(TEST_SIZE, channelStats.get(null).get(Status.SENT).intValue());
            assertEquals(TEST_SIZE, channelStats.get(0).get(Status.TRANSFORMED).intValue());
            assertEquals(TEST_SIZE, channelStats.get(1).get(Status.RECEIVED).intValue());
            assertEquals(TEST_SIZE, channelStats.get(1).get(Status.SENT).intValue());
        }
    }

    /*
     * Deploy a new channel, process messages
     * For each message, assert that:
     * - Each connector message and content was inserted
     * 
     * Then delete the connector messages and assert:
     * - Each connector message and content was deleted
     */
    @Test
    public final void testDeleteConnectorMessages() throws Exception {
        TestChannel channel = TestUtils.createDefaultChannel(channelId, serverId);

        channel.deploy();
        channel.start();
        ChannelController.getInstance().deleteAllMessages(channelId);

        DonkeyDao dao = daoFactory.getDao();

        try {
            logger.info("Testing DonkeyDao.deleteConnectorMessages...");

            for (int i = 1; i <= TEST_SIZE; i++) {
                Message message = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), dao);
                dao.commit();
                channel.process(message.getConnectorMessages().get(0), true);

                for (ConnectorMessage connectorMessage : message.getConnectorMessages().values()) {
                    TestUtils.assertConnectorMessageExists(connectorMessage, true);
                }

                dao.deleteConnectorMessages(channel.getChannelId(), message.getMessageId(), new ArrayList<Integer>(message.getConnectorMessages().keySet()), false);
                dao.commit();

                for (ConnectorMessage connectorMessage : message.getConnectorMessages().values()) {
                    for (ContentType contentType : ContentType.values()) {
                        MessageContent messageContent = connectorMessage.getContent(contentType);
                        if (messageContent != null) {
                            TestUtils.assertMessageContentDoesNotExist(messageContent);
                        }
                    }
                    TestUtils.assertConnectorMessageDoesNotExist(connectorMessage);
                }
            }

            System.out.println(daoTimer.getLog());
        } finally {
            dao.close();
            channel.stop();
            channel.undeploy();
            ChannelController.getInstance().removeChannel(channel.getChannelId());
        }
    }

    /*
     * Deploy a new channel, process messages
     * Delete all messages for the channel and assert:
     * - The message table was truncated
     * - The message metadata table was truncated
     * - The message content table was truncated
     * - The message custom metadata table was truncated
     * - The message attachment table was truncated
     */
    @Test
    public final void testDeleteAllMessages() throws Exception {
        TestChannel channel = TestUtils.createDefaultChannel(channelId, serverId);

        channel.deploy();
        channel.start();

        DonkeyDao dao = daoFactory.getDao();

        try {
            logger.info("Testing DonkeyDao.deleteAllMessages...");

            for (int i = 1; i <= TEST_SIZE; i++) {
                ((TestSourceConnector) channel.getSourceConnector()).readTestMessage(testMessage);
            }

            dao.deleteAllMessages(channel.getChannelId());
            dao.commit();

            // Assert that all the message tables have been truncated
            long localChannelId = ChannelController.getInstance().getLocalChannelId(channel.getChannelId());
            Connection connection = TestUtils.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM d_m" + localChannelId);
            ResultSet result = statement.executeQuery();
            assertFalse(result.next());
            statement = connection.prepareStatement("SELECT * FROM d_mm" + localChannelId);
            result = statement.executeQuery();
            assertFalse(result.next());
            statement = connection.prepareStatement("SELECT * FROM d_mc" + localChannelId);
            result = statement.executeQuery();
            assertFalse(result.next());
            statement = connection.prepareStatement("SELECT * FROM d_mcm" + localChannelId);
            result = statement.executeQuery();
            assertFalse(result.next());
            statement = connection.prepareStatement("SELECT * FROM d_ma" + localChannelId);
            result = statement.executeQuery();
            assertFalse(result.next());
            result.close();
            connection.close();

            System.out.println(daoTimer.getLog());
        } finally {
            dao.close();
            channel.stop();
            channel.undeploy();
            ChannelController.getInstance().removeChannel(channel.getChannelId());
        }
    }

    /*
     * Insert a bunch of new channels into the database
     * Select all channels from the database, and store them in a map
     * Call getLocalChannelIds(), assert:
     * - The map returned from getLocalChannelIds() matches the one previously
     * stored
     */
    @Test
    public final void testGetLocalChannelIds() throws Exception {
        Map<String, Long> localChannelIds = new HashMap<String, Long>();

        DonkeyDao dao = daoFactory.getDao();

        try {
            logger.info("Testing DonkeyDao.getLocalChannelIds...");

            // Insert the new channel entries
            Connection connection = TestUtils.getConnection();
            for (int i = 1; i <= TEST_SIZE; i++) {
                String tempChannelId = "getLocalChannelIds test " + i;
                Long nextId = dao.selectMaxLocalChannelId();
                if (nextId == null) {
                    nextId = Long.valueOf(1);
                }
                localChannelIds.put(tempChannelId, ++nextId);

                PreparedStatement statement = connection.prepareStatement("INSERT INTO d_channels (channel_id, local_channel_id) VALUES (?, ?)");
                statement.setString(1, tempChannelId);
                statement.setLong(2, localChannelIds.get(tempChannelId));
                statement.executeUpdate();
                connection.commit();
            }

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM d_channels");
            ResultSet result = statement.executeQuery();
            Map<String, Long> databaseLocalChannelIds = new HashMap<String, Long>();
            while (result.next()) {
                databaseLocalChannelIds.put(result.getString("channel_id"), result.getLong("local_channel_id"));
            }
            connection.close();

            // Assert that all the channels returned by getLocalChannelIds match the ones in the database
            assertEquals(databaseLocalChannelIds, dao.getLocalChannelIds());

            System.out.println(daoTimer.getLog());
        } finally {
            dao.close();
            Connection connection = TestUtils.getConnection();
            for (String channelId : localChannelIds.keySet()) {
                PreparedStatement statement = connection.prepareStatement("DELETE FROM d_channels WHERE channel_id = ? and local_channel_id = ?");
                statement.setString(1, channelId);
                statement.setLong(2, localChannelIds.get(channelId));
                statement.executeUpdate();
            }
            connection.commit();
            connection.close();
        }
    }

    /*
     * Use createChannel to create some new channels; assert:
     * - The channel ID and local channel ID were inserted
     * - The message table was created
     * - The message metadata table was created
     * - The message content table was created
     * - The message custom metadata table was created
     * - The message attachment table was created
     * - The message statistics table was created
     */
    @Test
    public final void testCreateChannel() throws Exception {
        List<String> channelIds = new ArrayList<String>();

        DonkeyDao dao = daoFactory.getDao();

        try {
            logger.info("Testing DonkeyDao.createChannel...");

            // Create new channels
            for (int i = 1; i <= TEST_SIZE; i++) {
                String tempChannelId = "createChannel test " + i;
                channelIds.add(tempChannelId);
                Long nextId = dao.selectMaxLocalChannelId();
                if (nextId == null) {
                    nextId = Long.valueOf(1);
                }
                nextId++;

                dao.removeChannel(tempChannelId);
                dao.commit();

                dao.createChannel(tempChannelId, nextId);
                dao.commit();

                Connection connection = TestUtils.getConnection();

                // Assert that the channel was inserted
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM d_channels WHERE channel_id = ? and local_channel_id = ?");
                statement.setString(1, tempChannelId);
                statement.setLong(2, nextId);
                ResultSet result = statement.executeQuery();
                assertTrue(result.next());

                // Assert that the channel message tables were created
                assertTrue(TestUtils.channelMessageTablesExist(connection, nextId));

                connection.close();
            }

            System.out.println(daoTimer.getLog());
        } finally {
            try {
                for (String channelId : channelIds) {
                    dao.removeChannel(channelId);
                    dao.commit();
                }
            } finally {
                dao.close();
            }
        }
    }

    /*
     * Create some new channels, remove the channels using removeChannel, and
     * assert that:
     * - The channel ID and local channel ID were deleted
     * - The message table was dropped
     * - The message metadata table was dropped
     * - The message content table was dropped
     * - The message custom metadata table was dropped
     * - The message attachment table was dropped
     * - The message statistics table was dropped
     */
    @Test
    public final void testRemoveChannel() throws Exception {
        Map<String, Long> localChannelIds = new HashMap<String, Long>();

        DonkeyDao dao = daoFactory.getDao();

        try {
            logger.info("Testing DonkeyDao.removeChannel...");

            // Create new channels
            for (int i = 1; i <= TEST_SIZE; i++) {
                String tempChannelId = "createChannel test " + i;
                Long nextId = dao.selectMaxLocalChannelId();
                if (nextId == null) {
                    nextId = Long.valueOf(1);
                }
                localChannelIds.put(tempChannelId, ++nextId);

                dao.createChannel(tempChannelId, localChannelIds.get(tempChannelId));
                dao.commit();
                ChannelController.getInstance().getLocalChannelIds().put(tempChannelId, nextId);
            }

            // Create new channels
            for (String channelId : localChannelIds.keySet()) {
                dao.removeChannel(channelId);
                dao.commit();

                Connection connection = TestUtils.getConnection();

                // Assert that the channel was deleted
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM d_channels WHERE channel_id = ? and local_channel_id = ?");
                statement.setString(1, channelId);
                statement.setLong(2, localChannelIds.get(channelId));
                ResultSet result = statement.executeQuery();
                assertFalse(result.next());

                // Assert that the channel message tables were dropped
                boolean messageTableExists = false;
                boolean messageMetaDataTableExists = false;
                boolean messageContentTableExists = false;
                boolean messageCustomMetaDataTableExists = false;
                boolean messageAttachmentTableExists = false;
                boolean messageStatisticsTableExists = false;
                result = connection.getMetaData().getTables(null, null, "d_m%", null);
                while (result.next()) {
                    String name = result.getString("TABLE_NAME").toLowerCase();
                    if (name.equals("d_m" + localChannelIds.get(channelId))) {
                        messageTableExists = true;
                    } else if (name.equals("d_mm" + localChannelIds.get(channelId))) {
                        messageMetaDataTableExists = true;
                    } else if (name.equals("d_mc" + localChannelIds.get(channelId))) {
                        messageContentTableExists = true;
                    } else if (name.equals("d_mcm" + localChannelIds.get(channelId))) {
                        messageCustomMetaDataTableExists = true;
                    } else if (name.equals("d_ma" + localChannelIds.get(channelId))) {
                        messageAttachmentTableExists = true;
                    } else if (name.equals("d_ms" + localChannelIds.get(channelId))) {
                        messageStatisticsTableExists = true;
                    }
                }
                assertFalse(messageTableExists);
                assertFalse(messageMetaDataTableExists);
                assertFalse(messageContentTableExists);
                assertFalse(messageCustomMetaDataTableExists);
                assertFalse(messageAttachmentTableExists);
                assertFalse(messageStatisticsTableExists);

                connection.close();
            }

            System.out.println(daoTimer.getLog());
        } finally {
            dao.close();
        }
    }

    /*
     * Select the max local channel ID manually, assert that:
     * - The ID matches the one returned from selectMaxLocalChannelId
     * 
     * Then create new channels, for each one select the max local channel ID
     * manually, then assert that:
     * - The ID matches the one returned from selectMaxLocalChannelId
     */
    @Test
    public final void testSelectMaxLocalChannelId() throws Exception {
        Map<String, Long> localChannelIds = new HashMap<String, Long>();

        DonkeyDao dao = daoFactory.getDao();

        try {
            logger.info("Testing DonkeyDao.selectMaxLocalChannelId...");

            // Assert that the maximum local channel ID matches the one returned from the DAO
            Connection connection = TestUtils.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT MAX(local_channel_id) FROM d_channels");
            ResultSet result = statement.executeQuery();
            result.next();
            Long maxId = result.getLong(1);
            connection.close();
            assertEquals(maxId, dao.selectMaxLocalChannelId());

            // Create new channels
            for (int i = 1; i <= TEST_SIZE; i++) {
                String tempChannelId = "selectMaxLocalChannelId test " + i;
                ChannelController.getInstance().getLocalChannelId(tempChannelId);

                // Assert that the maximum local channel ID matches the one returned from the DAO
                connection = TestUtils.getConnection();
                statement = connection.prepareStatement("SELECT MAX(local_channel_id) FROM d_channels");
                result = statement.executeQuery();
                result.next();
                maxId = result.getLong(1);
                connection.close();
                assertEquals(maxId, dao.selectMaxLocalChannelId());
            }

            System.out.println(daoTimer.getLog());
        } finally {
            for (String channelId : localChannelIds.keySet()) {
                ChannelController.getInstance().removeChannel(channelId);
            }
            dao.close();
        }
    }

    /*
     * Start up a new channel, assert that:
     * - The channel statistics in the database are the same as the ones
     * returned from getChannelStatistics
     * 
     * Then send messages, and after each one assert:
     * - The channel statistics in the database are the same as the ones
     * returned from getChannelStatistics
     */
    @Test
    public final void testGetChannelStatistics() throws Exception {
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId);
        channel.deploy();
        channel.start();

        DonkeyDao dao = daoFactory.getDao();

        try {
            logger.info("Testing DonkeyDao.getChannelStatistics...");

            // Assert that the statistics are correct
            assertEquals(TestUtils.getChannelStatistics(channel.getChannelId()), ChannelController.getInstance().getStatistics().getChannelStats(channel.getChannelId()));

            for (int i = 1; i <= TEST_SIZE; i++) {
                ((TestSourceConnector) channel.getSourceConnector()).readTestMessage(testMessage);

                // Assert that the statistics are correct
                assertEquals(TestUtils.getChannelStatistics(channel.getChannelId()), ChannelController.getInstance().getStatistics().getChannelStats(channel.getChannelId()));
            }

            System.out.println(daoTimer.getLog());
        } finally {
            dao.close();
            channel.stop();
            channel.undeploy();
            ChannelController.getInstance().removeChannel(channel.getChannelId());
        }
    }

    /*
     * Create a new channel, select the max message ID manually, assert that:
     * - The ID matches the one returned from selectMaxMessageId
     * 
     * Then send messages, and for each one select the max message ID manually,
     * then assert that:
     * - The ID matches the one returned from selectMaxMessageId
     */
    @Test
    public final void testGetMaxMessageId() throws Exception {
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId);

        DonkeyDao dao = daoFactory.getDao();

        try {
            logger.info("Testing DonkeyDao.getMaxMessageId...");

            long localChannelId = ChannelController.getInstance().getLocalChannelId(channel.getChannelId());
            channel.deploy();
            channel.start();

            // Assert that the maximum local channel ID matches the one returned from the DAO
            Connection connection = TestUtils.getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT MAX(id) FROM d_m" + localChannelId);
            ResultSet result = statement.executeQuery();
            result.next();
            Long maxId = result.getLong(1);
            connection.close();
            assertEquals(maxId, new Long(dao.getMaxMessageId(channel.getChannelId())));

            // Send messages
            for (int i = 1; i <= TEST_SIZE; i++) {
                ((TestSourceConnector) channel.getSourceConnector()).readTestMessage(testMessage);

                // Assert that the maximum local channel ID matches the one returned from the DAO
                connection = TestUtils.getConnection();
                statement = connection.prepareStatement("SELECT MAX(id) FROM d_m" + localChannelId);
                result = statement.executeQuery();
                result.next();
                maxId = result.getLong(1);
                connection.close();
                assertEquals(maxId, new Long(dao.getMaxMessageId(channel.getChannelId())));
            }

            System.out.println(daoTimer.getLog());
        } finally {
            dao.close();
            channel.stop();
            channel.undeploy();
            ChannelController.getInstance().removeChannel(channel.getChannelId());
        }
    }

    /*
     * Deploy new channel, send messages, and catch the Message objects returned
     * from the process method
     * 
     * Retrieve the connector messages for the message from the database using
     * getConnectorMessage, then for each connector message in the processed
     * Message object, assert that:
     * - The connector message (and all content attached to it) is equal to the
     * one retrieved from the database by the DAO
     */
    @Test
    public final void testGetConnectorMessages() throws Exception {
        TestChannel channel = TestUtils.createDefaultChannel(channelId, serverId);

        int limit = (int) Math.ceil(TEST_SIZE / 3.0);

        List<ConnectorMessage> sourceMessages;
        List<ConnectorMessage> destinationMessages;
        List<ConnectorMessage> databaseSourceMessages;
        List<ConnectorMessage> databaseDestinationMessages;

        DonkeyDao dao = daoFactory.getDao();

        try {
            logger.info("Testing DonkeyDao.getConnectorMessages...");

            channel.deploy();
            channel.start();
            ChannelController.getInstance().deleteAllMessages(channel.getChannelId());

            // Test selecting connector messages by message ID
            for (int i = 1; i <= TEST_SIZE; i++) {
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), dao).getConnectorMessages().get(0);
                dao.commit();
                Message processedMessage = channel.process(sourceMessage, true);

                // Assert that each connector message is equal
                for (ConnectorMessage connectorMessage : dao.getConnectorMessages(channel.getChannelId(), processedMessage.getMessageId()).values()) {
                    TestUtils.assertConnectorMessagesEqual(processedMessage.getConnectorMessages().get(connectorMessage.getMetaDataId()), connectorMessage);
                }
            }

            // Test selecting source connector messages by metadata ID and status
            sourceMessages = new ArrayList<ConnectorMessage>();
            for (int i = 1; i <= TEST_SIZE; i++) {
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), dao).getConnectorMessages().get(0);
                dao.commit();
                sourceMessages.add(sourceMessage);
            }
            databaseSourceMessages = dao.getConnectorMessages(channel.getChannelId(), 0, Status.RECEIVED);
            // Assert the connector message lists are equal
            TestUtils.assertConnectorMessageListsEqual(sourceMessages, databaseSourceMessages);

            dao.close();
            dao = daoFactory.getDao();
            ChannelController.getInstance().deleteAllMessages(channel.getChannelId());

            // Test selecting source and destination connector messages by metadata ID and status
            sourceMessages = new ArrayList<ConnectorMessage>();
            destinationMessages = new ArrayList<ConnectorMessage>();
            for (int i = 1; i <= TEST_SIZE; i++) {
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), dao).getConnectorMessages().get(0);
                dao.commit();
                Message processedMessage = channel.process(sourceMessage, true);
                sourceMessages.add(processedMessage.getConnectorMessages().get(0));
                destinationMessages.add(processedMessage.getConnectorMessages().get(1));
            }
            databaseSourceMessages = dao.getConnectorMessages(channel.getChannelId(), 0, Status.TRANSFORMED);
            databaseDestinationMessages = dao.getConnectorMessages(channel.getChannelId(), 1, Status.SENT);
            // Assert the connector message lists are equal
            TestUtils.assertConnectorMessageListsEqual(sourceMessages, databaseSourceMessages);
            TestUtils.assertConnectorMessageListsEqual(destinationMessages, databaseDestinationMessages);

            dao.close();
            dao = daoFactory.getDao();
            ChannelController.getInstance().deleteAllMessages(channel.getChannelId());

            // Test selecting source connector messages by metadata ID, status, offset, and limit
            sourceMessages = new ArrayList<ConnectorMessage>();
            for (int i = 1; i <= TEST_SIZE; i++) {
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), dao).getConnectorMessages().get(0);
                dao.commit();
                sourceMessages.add(sourceMessage);
            }
            for (int i = 0; i <= Math.ceil((double) sourceMessages.size() / limit) - 1; i++) {
                int offset = i * limit;
                databaseSourceMessages = dao.getConnectorMessages(channel.getChannelId(), 0, Status.RECEIVED, offset, limit, null, null);
                // Assert the connector message lists are equal
                TestUtils.assertConnectorMessageListsEqual(sourceMessages.subList(offset, Math.min(offset + limit, sourceMessages.size())), databaseSourceMessages);
            }

            dao.close();
            dao = daoFactory.getDao();
            ChannelController.getInstance().deleteAllMessages(channel.getChannelId());

            // Test selecting source and destination connector messages by metadata ID, status, offset, and limit
            sourceMessages = new ArrayList<ConnectorMessage>();
            destinationMessages = new ArrayList<ConnectorMessage>();
            for (int i = 1; i <= TEST_SIZE; i++) {
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), dao).getConnectorMessages().get(0);
                dao.commit();
                Message processedMessage = channel.process(sourceMessage, true);
                sourceMessages.add(processedMessage.getConnectorMessages().get(0));
                destinationMessages.add(processedMessage.getConnectorMessages().get(1));
            }
            for (int i = 0; i <= Math.ceil((double) sourceMessages.size() / limit) - 1; i++) {
                int offset = i * limit;
                databaseSourceMessages = dao.getConnectorMessages(channel.getChannelId(), 0, Status.TRANSFORMED, offset, limit, null, null);
                databaseDestinationMessages = dao.getConnectorMessages(channel.getChannelId(), 1, Status.SENT, offset, limit, null, null);
                // Assert the connector message lists are equal
                TestUtils.assertConnectorMessageListsEqual(sourceMessages.subList(offset, Math.min(offset + limit, sourceMessages.size())), databaseSourceMessages);
                TestUtils.assertConnectorMessageListsEqual(destinationMessages.subList(offset, Math.min(offset + limit, destinationMessages.size())), databaseDestinationMessages);
            }

            dao.close();
            dao = daoFactory.getDao();
            ChannelController.getInstance().deleteAllMessages(channel.getChannelId());

            System.out.println(daoTimer.getLog());
        } finally {
            dao.close();
            channel.stop();
            channel.undeploy();
            ChannelController.getInstance().removeChannel(channel.getChannelId());
        }
    }

    /*
     * Start up a new channel and send messages
     * Get the connector message count for all channel messages, and assert:
     */
    @Test
    public final void testGetConnectorMessageCount() throws Exception {
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId, 2, 2);

        DonkeyDao dao = daoFactory.getDao();

        try {
            logger.info("Testing DonkeyDao.getConnectorMessageCount...");

            channel.deploy();
            channel.start();

            dao.close();
            dao = daoFactory.getDao();
            ChannelController.getInstance().deleteAllMessages(channel.getChannelId());
            for (int i = 1; i <= TEST_SIZE; i++) {
                ((TestSourceConnector) channel.getSourceConnector()).readTestMessage(testMessage);

                // Test selecting all source connector messages with a specific metadata ID and status
                assertEquals(i, dao.getConnectorMessageCount(channel.getChannelId(), 0, Status.TRANSFORMED));
                assertEquals(0, dao.getConnectorMessageCount(channel.getChannelId(), 0, Status.SENT));

                for (DestinationChain chain : channel.getDestinationChains()) {
                    for (Integer metaDataId : chain.getEnabledMetaDataIds()) {
                        // Test selecting all destination connector messages with a specific metadata ID and status
                        assertEquals(0, dao.getConnectorMessageCount(channel.getChannelId(), metaDataId, Status.TRANSFORMED));
                        assertEquals(i, dao.getConnectorMessageCount(channel.getChannelId(), metaDataId, Status.SENT));
                    }
                }
            }

            System.out.println(daoTimer.getLog());
        } finally {
            dao.close();
            channel.stop();
            channel.undeploy();
            ChannelController.getInstance().removeChannel(channel.getChannelId());
        }
    }

    /*
     * Start new channel, send messages, catch each processed Message object and
     * add it to a list
     * Call getUnfinishedMessages to get a second list of Message objects, and
     * assert that:
     * - Each Message object in the list, along with all subsequent
     * ConnectorMessage and MessageContent objects are equal
     */
    @Test
    public final void testGetUnfinishedMessages() throws Exception {
        TestChannel channel = TestUtils.createDefaultChannel(channelId, serverId);

        List<Message> messages = new ArrayList<Message>();

        DonkeyDao dao = daoFactory.getDao();

        try {
            logger.info("Testing DonkeyDao.getUnfinishedMessages...");

            ChannelController.getInstance().deleteAllMessages(channel.getChannelId());
            channel.deploy();
            channel.start();

            for (int i = 1; i <= TEST_SIZE; i++) {
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), dao).getConnectorMessages().get(0);
                dao.commit();
                Message processedMessage = channel.process(sourceMessage, false);
                messages.add(processedMessage);
            }

            List<Message> databaseMessages = dao.getUnfinishedMessages(channel.getChannelId(), channel.getServerId());
            assertEquals(messages.size(), databaseMessages.size());
            for (int i = 0; i <= messages.size() - 1; i++) {
                TestUtils.assertMessagesEqual(messages.get(i), databaseMessages.get(i));
            }

            System.out.println(daoTimer.getLog());
        } finally {
            dao.close();
            channel.stop();
            channel.undeploy();
            ChannelController.getInstance().removeChannel(channel.getChannelId());
        }
    }

    /*
     * Create a list of metadata columns and add the list to the channel's
     * metadata columns
     * Deploy the channel, and assert that:
     * - The list of metadata columns matches the one returned by
     * getMetaDataColumns
     */
    @Test
    public final void testGetMetaDataColumns() throws Exception {
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId);

        List<MetaDataColumn> metaDataColumns = new ArrayList<MetaDataColumn>();
        for (MetaDataColumnType type : MetaDataColumnType.values()) {
            metaDataColumns.add(new MetaDataColumn(type.toString().toLowerCase() + "column", type, null));
        }
        channel.setMetaDataColumns(metaDataColumns);

        DonkeyDao dao = daoFactory.getDao();

        try {
            logger.info("Testing DonkeyDao.getMetaDataColumns...");

            ChannelController.getInstance().deleteAllMessages(channel.getChannelId());
            channel.deploy();

            assertEquals(metaDataColumns, dao.getMetaDataColumns(channel.getChannelId()));

            System.out.println(daoTimer.getLog());
        } finally {
            dao.close();
            channel.undeploy();
            ChannelController.getInstance().removeChannel(channel.getChannelId());
        }
    }

    /*
     * Deploy a new channel, manually add metadata columns using
     * addMetaDataColumn, and assert that:
     * - All the columns were successfully added
     */
    @Test
    public final void testAddMetaDataColumn() throws Exception {
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId);

        List<MetaDataColumn> metaDataColumns = new ArrayList<MetaDataColumn>();

        DonkeyDao dao = daoFactory.getDao();

        try {
            logger.info("Testing DonkeyDao.addMetaDataColumn...");

            ChannelController.getInstance().deleteAllMessages(channel.getChannelId());
            channel.deploy();

            for (int i = 1; i <= TEST_SIZE; i++) {
                for (MetaDataColumnType type : MetaDataColumnType.values()) {
                    MetaDataColumn metaDataColumn = new MetaDataColumn(type.toString().toLowerCase() + "column" + i, type, null);
                    dao.addMetaDataColumn(channel.getChannelId(), metaDataColumn);
                    metaDataColumns.add(metaDataColumn);
                }
                dao.commit();

                // Assert that the columns were added
                assertEquals(metaDataColumns, TestUtils.getExistingMetaDataColumns(channel.getChannelId()));
            }

            System.out.println(daoTimer.getLog());
        } finally {
            dao.close();
            channel.undeploy();
            ChannelController.getInstance().removeChannel(channel.getChannelId());
        }
    }

    /*
     * Deploy a new channel, add metadata columns, then use removeMetaDataColumn
     * to delete all the columns added
     * Get the list of existing metadata columns in the database, and assert:
     * - All the columns previously added were successfully removed
     */
    @Test
    public final void testRemoveMetaDataColumn() throws Exception {
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId);

        List<MetaDataColumn> metaDataColumns = new ArrayList<MetaDataColumn>();

        DonkeyDao dao = daoFactory.getDao();

        try {
            logger.info("Testing DonkeyDao.addMetaDataColumn...");

            ChannelController.getInstance().deleteAllMessages(channel.getChannelId());
            channel.deploy();

            for (int i = 1; i <= TEST_SIZE; i++) {
                for (MetaDataColumnType type : MetaDataColumnType.values()) {
                    MetaDataColumn metaDataColumn = new MetaDataColumn(type.toString().toLowerCase() + "column" + i, type, null);
                    dao.addMetaDataColumn(channel.getChannelId(), metaDataColumn);
                    metaDataColumns.add(metaDataColumn);
                }
            }
            dao.commit();

            // Remove the columns
            for (MetaDataColumn metaDataColumn : metaDataColumns) {
                dao.removeMetaDataColumn(channel.getChannelId(), metaDataColumn.getName());
            }
            dao.commit();

            List<MetaDataColumn> databaseMetaDataColumns = TestUtils.getExistingMetaDataColumns(channel.getChannelId());

            // Assert that the columns in the database do not contain any of the columns previously added
            for (MetaDataColumn metaDataColumn : metaDataColumns) {
                assertFalse(databaseMetaDataColumns.contains(metaDataColumn));
            }

            System.out.println(daoTimer.getLog());
        } finally {
            dao.close();
            channel.undeploy();
            ChannelController.getInstance().removeChannel(channel.getChannelId());
        }
    }
    
    /**
     * Sends messages through 5 channels with (maxConnections * 2) asynchronous destinations for 10 seconds and
     * verifies that the number of connection caches created is not greater than the maximum number
     * of connections in the connection pool
     * 
     * @throws Exception
     */
    @Test
    public final void testJdbcDaoStatementCache() throws Exception {
        DonkeyDaoFactory daoFactory = ((TimedDaoFactory) Donkey.getInstance().getDaoFactory()).getDelegateFactory();
        
        if (!(daoFactory instanceof JdbcDaoFactory)) {
            System.out.println("Skipping testJdbcDaoStatementCache() because the current DonkeyDaoFactory is not an instance of JdbcDaoFactory");
            return;
        }
        
        Integer maxConnections = Integer.parseInt(Donkey.getInstance().getConfiguration().getDatabaseProperties().getProperty("database.max-connections"));
        
        JdbcDaoFactory jdbcDaoFactory = (JdbcDaoFactory) daoFactory;
        
        TestUtils.runChannelTest(testMessage, channelId, serverId, "testJdbcDaoStatementCache", 5, maxConnections * 2, 1, true, null, 10000, null, new StorageSettings());
        
        assertTrue(jdbcDaoFactory.getStatementSources().size() + " connection caches were created, but the max # of connections is " + maxConnections, jdbcDaoFactory.getStatementSources().size() <= maxConnections);
    }

//    @Test
//    public final void testHalt() throws Exception {
//        TestChannel channel = TestUtils.createDefaultChannel(channelId, serverId);
//        DonkeyDao dao = daoFactory.getDao();
//
//        try {
//            channel.deploy();
//            channel.start();
//            
//            final ConnectorMessage connectorMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), dao).getConnectorMessages().get(0);
//            dao.commit();
//            
//            ExecutorService executor = Executors.newSingleThreadExecutor();
//            Future<?> future = executor.submit(new Runnable() {
//                @Override
//                public void run() {
//                    DonkeyDao dao = daoFactory.getDao();
//                    InterruptedException exception = null;
//                    
//                    try {
//                        dao.updateMaps(connectorMessage);
//                    } catch (InterruptedException e) {
//                        exception = e;
//                    }
//                    
//                    assertNotNull(exception);
//                    System.out.println("got here");
//                }
//            });
//            
//            future.cancel(true);
//            assertTrue(future.isCancelled());
//        } finally {
//            dao.close();
//            channel.stop();
//            channel.undeploy();
//            ChannelController.getInstance().removeChannel(channel.getChannelId());
//        }
//    }
}
