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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.channel.DestinationChain;
import com.mirth.connect.donkey.server.channel.StorageSettings;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;
import com.mirth.connect.donkey.server.data.buffered.BufferedDaoFactory;
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
    private static DonkeyDaoFactory daoFactory;
    private static ActionTimer daoTimer = new ActionTimer();

    private Logger logger = Logger.getLogger(this.getClass());

    @BeforeClass
    final public static void beforeClass() throws StartException {
        Donkey donkey = Donkey.getInstance();
        donkey.startEngine(TestUtils.getDonkeyTestConfiguration());

        daoFactory = new BufferedDaoFactory(new TimedDaoFactory(donkey.getDaoFactory(), daoTimer));
        donkey.setDaoFactory(daoFactory);
        
        ChannelController.getInstance().initChannelStorage(channelId);
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
    public void testNextMessageId() throws Exception {
        DonkeyDao dao = null;

        try {
            dao = daoFactory.getDao();
            
            long id1 = dao.getNextMessageId(channelId);
            long id2 = dao.getNextMessageId(channelId);
            long id3 = dao.getNextMessageId(channelId);
            
            logger.debug("id1: " + id1);
            logger.debug("id2: " + id2);
            logger.debug("id3: " + id3);
            
            assertEquals(id1 + 1, id2);
            assertEquals(id2 + 1, id3);
        } finally {
            TestUtils.close(dao);
        }
    }

    /*
     * Create and insert messages, assert that:
     * - The row was inserted correctly
     * - The server ID was inserted correctly
     * - The received_date column was initialized
     * - The processed column was initialized to false
     */
    @Test
    public void testInsertMessage() throws Exception {
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId);
        long localChannelId = ChannelController.getInstance().getLocalChannelId(channel.getChannelId());

        logger.info("Testing DonkeyDao.insertMessage...");

        Message message = new Message();
        message.setChannelId(channel.getChannelId());
        message.setServerId(channel.getServerId());
        message.setReceivedDate(Calendar.getInstance());
        message.setProcessed(false);

        for (int i = 1; i <= TEST_SIZE; i++) {
            DonkeyDao dao = daoFactory.getDao();
            message.setMessageId(dao.getNextMessageId(channel.getChannelId()));
            
            try {
                dao.insertMessage(message);
                dao.commit();
            } finally {
                dao.close();
            }

            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet result = null;
            
            try {
                connection = TestUtils.getConnection();
                statement = connection.prepareStatement("SELECT * FROM d_m" + localChannelId + " WHERE id = ?");
                statement.setLong(1, message.getMessageId());
                result = statement.executeQuery();

                // Assert that the row was inserted
                assertTrue(result.next());

                // Assert that the server ID was inserted
                assertEquals(message.getServerId(), result.getString("server_id"));

                // Assert that the date_create column was initialized
                assertNotNull(result.getTimestamp("received_date"));

                // Assert that the processed column was initialized to false
                assertNotNull(result.getBoolean("processed"));
                assertFalse(result.getBoolean("processed"));
            } finally {
                TestUtils.close(result);
                TestUtils.close(statement);
                TestUtils.close(connection);
            }
        }

        System.out.println(daoTimer.getLog());
    }

    /*
     * Create and insert connector messages
     * For each connector message inserted, assert that:
     * - The metadata row was inserted correctly
     * - The received dated was inserted correctly
     * - The status was inserted correctly
     * - The connector map was inserted correctly
     * - The channel map was inserted correctly
     * - The response map was inserted correctly
     * - The errors column was inserted correctly
     * - The send attempts column was initialized correctly
     */
    @Test
    public void testInsertConnectorMessage() throws Exception {
        TestUtils.createDefaultChannel(channelId, serverId);

        Map<String, Object> connectorMap = new HashMap<String, Object>();
        connectorMap.put("key", "value");

        Map<String, Object> channelMap = new HashMap<String, Object>();
        channelMap.put("key", "value");

        Map<String, Object> responseMap = new HashMap<String, Object>();
        responseMap.put("key", new Response(Status.SENT, "message"));

        DonkeyDao dao = null;
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channelId, serverId, daoFactory).getConnectorMessages().get(0);
            ConnectorMessage connectorMessage = new ConnectorMessage(channelId, sourceMessage.getMessageId(), 1, serverId, Calendar.getInstance(), Status.RECEIVED);

            connectorMessage.setConnectorMap(connectorMap);
            connectorMessage.setChannelMap(channelMap);
            connectorMessage.setResponseMap(responseMap);
            connectorMessage.setProcessingError("errors");

            double averageTime = 0;

            logger.info("Testing DonkeyDao.insertConnectorMessage...");

            long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);
            
            daoTimer.reset();

            for (int i = 1; i <= TEST_SIZE; i++) {
                logger.debug("testInsertConnectorMessage #" + i);
                connectorMessage.setMetaDataId(i);
                
                try {
                    dao = daoFactory.getDao();
                    dao.insertConnectorMessage(connectorMessage, true);
                    dao.commit();
                } finally {
                    TestUtils.close(dao);
                }
                
                connection = TestUtils.getConnection();
                statement = connection.prepareStatement("SELECT * FROM d_mm" + localChannelId + " WHERE id = ? AND message_id = ?");
                statement.setLong(1, connectorMessage.getMetaDataId());
                statement.setLong(2, connectorMessage.getMessageId());
                result = statement.executeQuery();

                // Assert that the metadata row exists
                assertTrue(result.next());

                // Assert that the received date is correct
                try {
                    assertEquals(result.getTimestamp("received_date").getTime(), connectorMessage.getReceivedDate().getTimeInMillis());
                } catch (AssertionError e) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS");
                    logger.error("database received_date: " + format.format(result.getTimestamp("received_date").getTime()) + ", connector message received_date: " + format.format(connectorMessage.getReceivedDate().getTimeInMillis()));
                    throw e;
                }

                // Assert that the status is correct
                assertEquals(Status.fromChar(result.getString("status").charAt(0)), connectorMessage.getStatus());

                // Assert that the connector map is correct
                assertTrue(connectorMessage.getConnectorMap().equals(TestUtils.getConnectorMap(channelId, connectorMessage.getMessageId(), connectorMessage.getMetaDataId())));

                // Assert that the channel map is correct
                assertTrue(connectorMessage.getChannelMap().equals(TestUtils.getChannelMap(channelId, connectorMessage.getMessageId(), connectorMessage.getMetaDataId())));

                // Assert that the response map is correct
                assertTrue(connectorMessage.getResponseMap().equals(TestUtils.getResponseMap(channelId, connectorMessage.getMessageId(), connectorMessage.getMetaDataId())));

                // Assert that the errors column is correct
                assertTrue(connectorMessage.getProcessingError().equals(TestUtils.getErrorFromMessageContent(TestUtils.getMessageContent(channelId, connectorMessage.getMessageId(), connectorMessage.getMetaDataId(), ContentType.PROCESSING_ERROR))));

                // Assert that the send attempts column is correct
                assertEquals(result.getInt("send_attempts"), 0);

                TestUtils.close(result);
                connection.close();
            }

            averageTime = daoTimer.getTotalTime() / TEST_SIZE;
            logger.info("Success, average transaction time: " + averageTime + " ms");
        } finally {
            TestUtils.close(result);
            TestUtils.close(statement);
            TestUtils.close(connection);
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
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId);

        DonkeyDao dao = null;

        try {
            dao = daoFactory.getDao();
            logger.info("Testing DonkeyDao.insertMessageContent...");

            for (int i = 1; i <= TEST_SIZE; i++) {
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), daoFactory).getConnectorMessages().get(0);
                TestUtils.assertMessageContentExists(sourceMessage.getRaw());
                
                for (ContentType contentType : ContentType.getMessageTypes()) {
                    MessageContent messageContent = new MessageContent(channel.getChannelId(), sourceMessage.getMessageId(), sourceMessage.getMetaDataId(), contentType, testMessage, null, false);
                    sourceMessage.setMessageContent(messageContent);

                    if (contentType != ContentType.RAW) {
                        dao.insertMessageContent(messageContent);
                        dao.commit();
                        // Assert that the content was inserted
                        TestUtils.assertMessageContentExists(messageContent);
                    } else {
                        Exception e = null;
                        try {
                            dao.insertMessageContent(messageContent);
                            dao.commit();
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
    }

    /*
     * Create new channel and message
     * Insert attachments for the message, assert that:
     * - The attachment was inserted correctly
     */
    @Test
    public final void testInsertMessageAttachment() throws Exception {
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId);
        DonkeyDao dao = daoFactory.getDao();

        try {
            logger.info("Testing DonkeyDao.insertMessageAttachment...");

            ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), daoFactory).getConnectorMessages().get(0);

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
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId);

        for (MetaDataColumnType columnType : MetaDataColumnType.values()) {
            channel.getMetaDataColumns().add(new MetaDataColumn(columnType.toString() + "test", columnType, null));
        }

        channel.deploy();

        // Assert that the columns were added successfully
        List<MetaDataColumn> existingColumns = TestUtils.getExistingMetaDataColumns(channel.getChannelId());
        List<MetaDataColumn> channelColumns = channel.getMetaDataColumns();
        
        assertEquals(channelColumns.size(), existingColumns.size());
        
        for (MetaDataColumn metaDataColumn : channelColumns) {
            assertTrue(metaDataColumn.getName(), existingColumns.contains(metaDataColumn));
        }

        Map<String, Object> sourceMap = new HashMap<String, Object>();
        sourceMap.put(MetaDataColumnType.BOOLEAN.toString() + "test", true);
        sourceMap.put(MetaDataColumnType.NUMBER.toString() + "test", 1);
        sourceMap.put(MetaDataColumnType.STRING.toString() + "test", "testing");
        sourceMap.put(MetaDataColumnType.TIMESTAMP.toString() + "test", Calendar.getInstance());

        Map<String, Object> destinationMap = new HashMap<String, Object>();
        destinationMap.put(MetaDataColumnType.BOOLEAN.toString() + "test", false);
        destinationMap.put(MetaDataColumnType.NUMBER.toString() + "test", 1);
        destinationMap.put(MetaDataColumnType.STRING.toString() + "test", "");
        destinationMap.put(MetaDataColumnType.TIMESTAMP.toString() + "test", Calendar.getInstance());

        logger.info("Testing DonkeyDao.insertMetaData...");

        DonkeyDao dao = daoFactory.getDao();

        try {
            for (int i = 1; i <= TEST_SIZE; i++) {
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), daoFactory).getConnectorMessages().get(0);

                sourceMessage.setMetaDataMap(sourceMap);
                dao.insertMetaData(sourceMessage, channel.getMetaDataColumns());
                dao.commit();

                ConnectorMessage destinationMessage = TestUtils.createAndStoreDestinationConnectorMessage(daoFactory, channel.getChannelId(), channel.getServerId(), sourceMessage.getMessageId(), 1, testMessage, Status.RECEIVED);
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
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId);

        DonkeyDao dao = daoFactory.getDao();

        try {
            logger.info("Testing DonkeyDao.storeMessageContent...");

            for (int i = 1; i <= TEST_SIZE; i++) {
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), daoFactory).getConnectorMessages().get(0);

                for (ContentType contentType : ContentType.getMessageTypes()) {
                    MessageContent messageContent = new MessageContent(channel.getChannelId(), sourceMessage.getMessageId(), sourceMessage.getMetaDataId(), contentType, testMessage, null, false);
                    sourceMessage.setMessageContent(messageContent);
                    dao.storeMessageContent(messageContent);
                    dao.commit();
                    // Assert that the content was inserted
                    TestUtils.assertMessageContentExists(messageContent);
                }
            }

            System.out.println(daoTimer.getLog());
        } finally {
            dao.close();
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
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId);

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            logger.info("Testing DonkeyDao.updateStatus...");

            for (int i = 1; i <= TEST_SIZE; i++) {
                DonkeyDao dao = null;
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), daoFactory).getConnectorMessages().get(0);

                sourceMessage.setStatus(Status.ERROR);
                
                try {
                    dao = daoFactory.getDao();
                    dao.updateStatus(sourceMessage, Status.RECEIVED);
                    dao.commit();
                } finally {
                    TestUtils.close(dao);
                }

                // Assert that the status was updated
                TestUtils.assertConnectorMessageExists(sourceMessage, false);

                sourceMessage.setStatus(Status.TRANSFORMED);
                sourceMessage.setSendAttempts(1);
                
                try {
                    dao = daoFactory.getDao();
                    dao.updateStatus(sourceMessage, Status.ERROR);
                    dao.commit();
                } finally {
                    TestUtils.close(dao);
                }

                // Assert that the status was updated
                TestUtils.assertConnectorMessageExists(sourceMessage, false);

                // Assert that the send attempts were updated
                long localChannelId = ChannelController.getInstance().getLocalChannelId(channel.getChannelId());
                connection = TestUtils.getConnection();
                statement = connection.prepareStatement("SELECT send_attempts FROM d_mm" + localChannelId + " WHERE message_id = ? AND id = ?");
                statement.setLong(1, sourceMessage.getMessageId());
                statement.setLong(2, sourceMessage.getMetaDataId());
                result = statement.executeQuery();
                assertTrue(result.next());
                assertEquals(1, result.getInt("send_attempts"));
                result.close();
                statement.close();
                connection.close();
            }

            System.out.println(daoTimer.getLog());
        } finally {
            TestUtils.close(result);
            TestUtils.close(statement);
            TestUtils.close(connection);
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
        Connection connection = null;
        
        try {
            Channel channel = TestUtils.createDefaultChannel(channelId, serverId);
            connection = TestUtils.getConnection();
    
            Map<String, Object> connectorMap = new HashMap<String, Object>();
            connectorMap.put("key1", "teststring");
            connectorMap.put("key2", Calendar.getInstance());
            connectorMap.put("key3", connection);
    
            Map<String, Object> channelMap = new HashMap<String, Object>();
            channelMap.put("key1", 12345);
            channelMap.put("key2", null);
            channelMap.put("key3", connection);
    
            Map<String, Object> responseMap = new HashMap<String, Object>();
            responseMap.put("key1", new Response("message"));
            responseMap.put("key2", new Response(Status.FILTERED, "message"));
    
            
            logger.info("Testing DonkeyDao.updateMaps...");

            for (int i = 1; i <= TEST_SIZE; i++) {
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), daoFactory).getConnectorMessages().get(0);

                sourceMessage.setConnectorMap(connectorMap);
                sourceMessage.setChannelMap(channelMap);
                sourceMessage.setResponseMap(responseMap);
                
                DonkeyDao dao = null;
                
                try {
                    dao = daoFactory.getDao();
                    dao.updateMaps(sourceMessage);
                    dao.commit();
                } finally {
                    TestUtils.close(dao);
                }

                Map<String, Object> insertedConnectorMap = TestUtils.getConnectorMap(channel.getChannelId(), sourceMessage.getMessageId(), sourceMessage.getMetaDataId());
                Map<String, Object> insertedChannelMap = TestUtils.getChannelMap(channel.getChannelId(), sourceMessage.getMessageId(), sourceMessage.getMetaDataId());
                Map<String, Object> insertedResponseMap = TestUtils.getResponseMap(channel.getChannelId(), sourceMessage.getMessageId(), sourceMessage.getMetaDataId());
                
                // Assert that both connector maps have all the same keys
                assertTrue(connectorMap.keySet().equals(insertedConnectorMap.keySet()));
                
                for (String key : connectorMap.keySet()) {
                    // assert that the inserted value is the String representation
                    Object value = connectorMap.get(key);
                    Object value2 = insertedConnectorMap.get(key).toString();
                    assertEquals(key, (value == null) ? "" : value.toString(), (value2 == null) ? "" : value2.toString());
                }

                // Assert that both channel maps have all the same keys
                assertTrue(channelMap.keySet().equals(insertedChannelMap.keySet()));
                
                for (String key : channelMap.keySet()) {
                    // assert that the inserted value is the String representation
                    Object value = channelMap.get(key);
                    Object value2 = insertedChannelMap.get(key);
                    assertEquals(key, (value == null) ? "" : value.toString(), (value2 == null) ? "" : value2.toString());
                }
                
                // Assert that both response maps have all the same keys
                assertTrue(responseMap.keySet().equals(insertedResponseMap.keySet()));
                
                for (String key : responseMap.keySet()) {
                    // assert that the inserted value is the String representation
                    Object value = responseMap.get(key);
                    Object value2 = insertedResponseMap.get(key);
                    assertEquals(key, (value == null) ? "" : value.toString(), (value2 == null) ? "" : value2.toString());
                }
            }

            System.out.println(daoTimer.getLog());
        } finally {
            TestUtils.close(connection);
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
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId);

        Map<String, Object> responseMap = new HashMap<String, Object>();
        responseMap.put("key1", new Response("message"));
        responseMap.put("key2", new Response(Status.FILTERED, "message"));

        DonkeyDao dao = daoFactory.getDao();

        try {
            logger.info("Testing DonkeyDao.updateResponseMap...");

            for (int i = 1; i <= TEST_SIZE; i++) {
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), daoFactory).getConnectorMessages().get(0);

                sourceMessage.setResponseMap(responseMap);
                dao.updateResponseMap(sourceMessage);
                dao.commit();

                Map<String, Object> insertedResponseMap = TestUtils.getResponseMap(channel.getChannelId(), sourceMessage.getMessageId(), sourceMessage.getMetaDataId());

                // Assert that both response maps are equal
                assertTrue(responseMap.equals(insertedResponseMap));
            }

            System.out.println(daoTimer.getLog());
        } finally {
            dao.close();
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
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId);

        DonkeyDao dao = daoFactory.getDao();

        try {
            logger.info("Testing DonkeyDao.markAsProcessed...");

            for (int i = 1; i <= TEST_SIZE; i++) {
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), daoFactory).getConnectorMessages().get(0);

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
    public void testDeleteMessage() throws Exception { // FIXME
        TestChannel channel = TestUtils.createDefaultChannel(channelId, serverId);

        channel.deploy();
        channel.start();

        DonkeyDao dao = daoFactory.getDao();

        try {
            logger.info("Testing DonkeyDao.deleteMessage...");

            // Test deleting messages without deleting statistics
            testDeleteMessage(channel, false, dao);

            // Test deleting messages with deleting statistics
            testDeleteMessage(channel, true, dao);
        } finally {
            dao.close();
            channel.stop();
            channel.undeploy();
        }
    }

    private void testDeleteMessage(TestChannel channel, boolean deleteStatistics, DonkeyDao dao) throws Exception {
        List<Message> messages = new ArrayList<Message>();
        TestUtils.deleteChannelStatistics(channel.getChannelId());

        // Process a bunch of messages through the channel
        for (int i = 1; i <= TEST_SIZE; i++) {
            ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), daoFactory).getConnectorMessages().get(0);

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
                for (ContentType contentType : ContentType.getMessageTypes()) {
                    if (connectorMessage.getMessageContent(contentType) != null) {
                        // Assert that each content row was deleted
                        TestUtils.assertMessageContentDoesNotExist(connectorMessage.getMessageContent(contentType));
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

        try {
            logger.info("Testing DonkeyDao.deleteConnectorMessages...");

            for (int i = 1; i <= TEST_SIZE; i++) {
                Message message = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), daoFactory);
                channel.process(message.getConnectorMessages().get(0), true);

                for (ConnectorMessage connectorMessage : message.getConnectorMessages().values()) {
                    TestUtils.assertConnectorMessageExists(connectorMessage, true);
                }

                DonkeyDao dao = null;
                
                try {
                    dao = daoFactory.getDao();
                    dao.deleteConnectorMessages(channel.getChannelId(), message.getMessageId(), new ArrayList<Integer>(message.getConnectorMessages().keySet()), false);
                    dao.commit();
                } finally {
                    TestUtils.close(dao);
                }

                for (ConnectorMessage connectorMessage : message.getConnectorMessages().values()) {
                    for (ContentType contentType : ContentType.getMessageTypes()) {
                        MessageContent messageContent = connectorMessage.getMessageContent(contentType);
                        if (messageContent != null) {
                            TestUtils.assertMessageContentDoesNotExist(messageContent);
                        }
                    }
                    TestUtils.assertConnectorMessageDoesNotExist(connectorMessage);
                }
            }

            System.out.println(daoTimer.getLog());
        } finally {
            channel.stop();
            channel.undeploy();
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
        
        logger.info("Testing DonkeyDao.deleteAllMessages...");

        for (int i = 1; i <= TEST_SIZE; i++) {
            ((TestSourceConnector) channel.getSourceConnector()).readTestMessage(testMessage);
        }

        DonkeyDao dao = null;
        
        try {
            dao = daoFactory.getDao();
            dao.deleteAllMessages(channel.getChannelId());
            dao.commit();
        } finally {
            TestUtils.close(dao);
        }
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            // Assert that all the message tables have been truncated
            long localChannelId = ChannelController.getInstance().getLocalChannelId(channel.getChannelId());
            connection = TestUtils.getConnection();
            
            statement = connection.prepareStatement("SELECT * FROM d_m" + localChannelId);
            result = statement.executeQuery();
            assertFalse(result.next());
            result.close();
            statement.close();
            
            statement = connection.prepareStatement("SELECT * FROM d_mm" + localChannelId);
            result = statement.executeQuery();
            assertFalse(result.next());
            result.close();
            statement.close();
            
            statement = connection.prepareStatement("SELECT * FROM d_mc" + localChannelId);
            result = statement.executeQuery();
            assertFalse(result.next());
            result.close();
            statement.close();
            
            statement = connection.prepareStatement("SELECT * FROM d_mcm" + localChannelId);
            result = statement.executeQuery();
            assertFalse(result.next());
            result.close();
            statement.close();
            
            statement = connection.prepareStatement("SELECT * FROM d_ma" + localChannelId);
            result = statement.executeQuery();
            assertFalse(result.next());
            result.close();
            statement.close();

            System.out.println(daoTimer.getLog());
        } finally {
            TestUtils.close(result);
            TestUtils.close(statement);
            TestUtils.close(connection);
            
            channel.stop();
            channel.undeploy();
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
        
        try {
            logger.info("Testing DonkeyDao.getLocalChannelIds...");

            // Insert the new channel entries
            for (int i = 1; i <= TEST_SIZE; i++) {
                String tempChannelId = "getLocalChannelIds test " + i;
                Long nextId = null;
                DonkeyDao dao = null;
                
                try {
                    dao = daoFactory.getDao();
                    nextId = dao.selectMaxLocalChannelId();
                } finally {
                    TestUtils.close(dao);
                }
                
                if (nextId == null) {
                    nextId = Long.valueOf(1);
                }
                
                localChannelIds.put(tempChannelId, ++nextId);

                Connection connection = null;
                PreparedStatement statement = null;
                
                try {
                    connection = TestUtils.getConnection();
                    statement = connection.prepareStatement("INSERT INTO d_channels (channel_id, local_channel_id) VALUES (?, ?)");
                    statement.setString(1, tempChannelId);
                    statement.setLong(2, localChannelIds.get(tempChannelId));
                    statement.executeUpdate();
                    connection.commit();
                } finally {
                    TestUtils.close(statement);
                    TestUtils.close(connection);
                }
            }

            Map<String, Long> databaseLocalChannelIds = new HashMap<String, Long>();
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet result = null;
            
            try {
                connection = TestUtils.getConnection();
                statement = connection.prepareStatement("SELECT * FROM d_channels");
                result = statement.executeQuery();
                
                while (result.next()) {
                    databaseLocalChannelIds.put(result.getString("channel_id"), result.getLong("local_channel_id"));
                }
            } finally {
                TestUtils.close(result);
                TestUtils.close(statement);
                TestUtils.close(connection);
            }
            
            DonkeyDao dao = null;
            
            try {
                dao = daoFactory.getDao();
                
                // Assert that all the channels returned by getLocalChannelIds match the ones in the database
                assertEquals(databaseLocalChannelIds, dao.getLocalChannelIds());
            } finally {
                TestUtils.close(dao);
            }

            System.out.println(daoTimer.getLog());
        } finally {
            Connection connection = null;
            PreparedStatement statement = null;
            
            try {
                connection = TestUtils.getConnection();
                
                for (String channelId : localChannelIds.keySet()) {
                    statement = connection.prepareStatement("DELETE FROM d_channels WHERE channel_id = ? AND local_channel_id = ?");
                    statement.setString(1, channelId);
                    statement.setLong(2, localChannelIds.get(channelId));
                    statement.executeUpdate();
                    statement.close();
                }
                
                connection.commit();
            } finally {
                TestUtils.close(statement);
                TestUtils.close(connection);
            }
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
        logger.info("Testing DonkeyDao.createChannel...");

        // Create new channels
        for (int i = 1; i <= TEST_SIZE; i++) {
            String tempChannelId = "createChannel test " + i;
            DonkeyDao dao = null;
            Long nextId = null;
            
            try {
                dao = daoFactory.getDao();
                nextId = dao.selectMaxLocalChannelId();

                if (nextId == null) {
                    nextId = Long.valueOf(1);
                }
                
                nextId++;

                dao.removeChannel(tempChannelId);
                dao.commit();

                dao.createChannel(tempChannelId, nextId);
                dao.commit();
            } finally {
                TestUtils.close(dao);
            }
            
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet result = null;

            try {
                connection = TestUtils.getConnection();
                
                // Assert that the channel was inserted
                statement = connection.prepareStatement("SELECT * FROM d_channels WHERE channel_id = ? AND local_channel_id = ?");
                statement.setString(1, tempChannelId);
                statement.setLong(2, nextId);
                result = statement.executeQuery();
                assertTrue(result.next());

                // Assert that the channel message tables were created
                TestUtils.assertChannelMessageTablesExist(connection, nextId);
            } finally {
                TestUtils.close(result);
                TestUtils.close(statement);
                TestUtils.close(connection);
            }
        }

        System.out.println(daoTimer.getLog());
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
        logger.info("Testing DonkeyDao.removeChannel...");
        
        DonkeyDao dao = null;
        
        try {
            dao = daoFactory.getDao();
            localChannelIds = dao.getLocalChannelIds();
            
            // Create new channels
            for (int i = 1; i <= TEST_SIZE; i++) {
                String tempChannelId = "testRemoveChannel test " + i;
                Long nextId = dao.selectMaxLocalChannelId();
                if (nextId == null) {
                    nextId = Long.valueOf(1);
                }
                
                if (!localChannelIds.containsKey(tempChannelId)) {
                    localChannelIds.put(tempChannelId, ++nextId);
                    dao.createChannel(tempChannelId, localChannelIds.get(tempChannelId));
                    dao.commit();
                }
            }
        } finally {
            TestUtils.close(dao);
        }

        // Create new channels
        for (String channelId : localChannelIds.keySet()) {
            try {
                dao = daoFactory.getDao();
                dao.removeChannel(channelId);
                dao.commit();
            } finally {
                TestUtils.close(dao);
            }
            
            // Assert that the channel message tables were dropped
            boolean messageTableExists = false;
            boolean messageMetaDataTableExists = false;
            boolean messageContentTableExists = false;
            boolean messageCustomMetaDataTableExists = false;
            boolean messageAttachmentTableExists = false;
            boolean messageStatisticsTableExists = false;
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet result = null;
            
            try {
                connection = TestUtils.getConnection();
                
                // Assert that the channel was deleted
                statement = connection.prepareStatement("SELECT * FROM d_channels WHERE channel_id = ? AND local_channel_id = ?");
                statement.setString(1, channelId);
                statement.setLong(2, localChannelIds.get(channelId));
                result = statement.executeQuery();
                assertFalse(result.next());
                TestUtils.close(result);
                TestUtils.close(statement);
                
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
            } finally {
                TestUtils.close(result);
                TestUtils.close(statement);
                TestUtils.close(connection);
            }
            
            assertFalse(messageTableExists);
            assertFalse(messageMetaDataTableExists);
            assertFalse(messageContentTableExists);
            assertFalse(messageCustomMetaDataTableExists);
            assertFalse(messageAttachmentTableExists);
            assertFalse(messageStatisticsTableExists);
        }

        System.out.println(daoTimer.getLog());
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

        try {
            logger.info("Testing DonkeyDao.selectMaxLocalChannelId...");
            
            Long maxId = null;
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet result = null;
            
            // Assert that the maximum local channel ID matches the one returned from the DAO
            try {
                connection = TestUtils.getConnection();
                statement = connection.prepareStatement("SELECT MAX(local_channel_id) FROM d_channels");
                result = statement.executeQuery();
                result.next();
                maxId = result.getLong(1);
            } finally {
                TestUtils.close(result);
                TestUtils.close(statement);
                TestUtils.close(connection);
            }
            
            DonkeyDao dao = null;
            
            try {
                dao = daoFactory.getDao();
                assertEquals(maxId, dao.selectMaxLocalChannelId());
            } finally {
                TestUtils.close(dao);
            }

            // Create new channels
            for (int i = 1; i <= TEST_SIZE; i++) {
                String tempChannelId = "selectMaxLocalChannelId test " + i;
                ChannelController.getInstance().getLocalChannelId(tempChannelId);

                try {
                    connection = TestUtils.getConnection();
                    
                    // Assert that the maximum local channel ID matches the one returned from the DAO
                    statement = connection.prepareStatement("SELECT MAX(local_channel_id) FROM d_channels");
                    result = statement.executeQuery();
                    result.next();
                    maxId = result.getLong(1);
                } finally {
                    TestUtils.close(result);
                    TestUtils.close(statement);
                    TestUtils.close(connection);
                }
                
                try {
                    dao = daoFactory.getDao();
                    assertEquals(maxId, dao.selectMaxLocalChannelId());
                } finally {
                    TestUtils.close(dao);
                }
            }

            System.out.println(daoTimer.getLog());
        } finally {
            for (String channelId : localChannelIds.keySet()) {
                ChannelController.getInstance().removeChannel(channelId);
            }
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
            channel.stop();
            channel.undeploy();
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

        try {
            logger.info("Testing DonkeyDao.getMaxMessageId...");

            long localChannelId = ChannelController.getInstance().getLocalChannelId(channel.getChannelId());
            channel.deploy();
            channel.start();

            Long maxId = null;
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet result = null;
            
            try {
                // Assert that the maximum local channel ID matches the one returned from the DAO
                connection = TestUtils.getConnection();
                statement = connection.prepareStatement("SELECT MAX(id) FROM d_m" + localChannelId);
                result = statement.executeQuery();
                result.next();
                maxId = result.getLong(1);
            } finally {
                TestUtils.close(result);
                TestUtils.close(statement);
                TestUtils.close(connection);
            }
            
            DonkeyDao dao = null;
            
            try {
                dao = daoFactory.getDao();
                assertEquals(maxId, new Long(dao.getMaxMessageId(channel.getChannelId())));
            } finally {
                TestUtils.close(dao);
            }

            // Send messages
            for (int i = 1; i <= TEST_SIZE; i++) {
                ((TestSourceConnector) channel.getSourceConnector()).readTestMessage(testMessage);

                try {
                    // Assert that the maximum local channel ID matches the one returned from the DAO
                    connection = TestUtils.getConnection();
                    statement = connection.prepareStatement("SELECT MAX(id) FROM d_m" + localChannelId);
                    result = statement.executeQuery();
                    result.next();
                    maxId = result.getLong(1);
                } finally {
                    TestUtils.close(result);
                    TestUtils.close(statement);
                    TestUtils.close(connection);
                }
                
                try {
                    dao = daoFactory.getDao();
                    assertEquals(maxId, new Long(dao.getMaxMessageId(channel.getChannelId())));
                } finally {
                    TestUtils.close(dao);
                }
            }

            System.out.println(daoTimer.getLog());
        } finally {
            channel.stop();
            channel.undeploy();
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

        try {
            logger.info("Testing DonkeyDao.getConnectorMessages...");
            DonkeyDao dao = null;
            
            channel.deploy();
            channel.start();

            // Test selecting connector messages by message ID
            for (int i = 1; i <= TEST_SIZE; i++) {
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), daoFactory).getConnectorMessages().get(0);
                Message processedMessage = channel.process(sourceMessage, true);

                // Assert that each connector message is equal
                Map<Integer, ConnectorMessage> connectorMessages;
                
                try {
                    dao = daoFactory.getDao();
                    connectorMessages = dao.getConnectorMessages(channel.getChannelId(), processedMessage.getMessageId());
                } finally {
                    TestUtils.close(dao);
                }
                
                for (ConnectorMessage connectorMessage : connectorMessages.values()) {
                    TestUtils.assertConnectorMessagesEqual(processedMessage.getConnectorMessages().get(connectorMessage.getMetaDataId()), connectorMessage);
                }
            }

            // Test selecting source connector messages by metadata ID and status
            sourceMessages = new ArrayList<ConnectorMessage>();
            
            for (int i = 1; i <= TEST_SIZE; i++) {
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), daoFactory).getConnectorMessages().get(0);
                sourceMessages.add(sourceMessage);
            }
            
            try {
                dao = daoFactory.getDao();
                databaseSourceMessages = dao.getConnectorMessages(channel.getChannelId(), channel.getServerId(), 0, Status.RECEIVED);
            } finally {
                TestUtils.close(dao);
            }
            
            // Assert the connector message lists are equal
            TestUtils.assertConnectorMessageListsEqual(sourceMessages, databaseSourceMessages);

            ChannelController.getInstance().deleteAllMessages(channel.getChannelId());

            // Test selecting source and destination connector messages by metadata ID and status
            sourceMessages = new ArrayList<ConnectorMessage>();
            destinationMessages = new ArrayList<ConnectorMessage>();
            
            for (int i = 1; i <= TEST_SIZE; i++) {
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), daoFactory).getConnectorMessages().get(0);
                Message processedMessage = channel.process(sourceMessage, true);
                sourceMessages.add(processedMessage.getConnectorMessages().get(0));
                destinationMessages.add(processedMessage.getConnectorMessages().get(1));
            }
            
            try {
                dao = daoFactory.getDao();
                databaseSourceMessages = dao.getConnectorMessages(channel.getChannelId(), channel.getServerId(), 0, Status.TRANSFORMED);
                databaseDestinationMessages = dao.getConnectorMessages(channel.getChannelId(), channel.getServerId(), 1, Status.SENT);
            } finally {
                TestUtils.close(dao);
            }
            
            // Assert the connector message lists are equal
            TestUtils.assertConnectorMessageListsEqual(sourceMessages, databaseSourceMessages);
            TestUtils.assertConnectorMessageListsEqual(destinationMessages, databaseDestinationMessages);

            ChannelController.getInstance().deleteAllMessages(channel.getChannelId());

            // Test selecting source connector messages by metadata ID, status, offset, and limit
            sourceMessages = new ArrayList<ConnectorMessage>();
            
            for (int i = 1; i <= TEST_SIZE; i++) {
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), daoFactory).getConnectorMessages().get(0);
                sourceMessages.add(sourceMessage);
            }
            
            for (int i = 0; i <= Math.ceil((double) sourceMessages.size() / limit) - 1; i++) {
                int offset = i * limit;
                
                try {
                    dao = daoFactory.getDao();
                    databaseSourceMessages = dao.getConnectorMessages(channel.getChannelId(), channel.getServerId(), 0, Status.RECEIVED, offset, limit, null, null);
                } finally {
                    TestUtils.close(dao);
                }
                
                // Assert the connector message lists are equal
                TestUtils.assertConnectorMessageListsEqual(sourceMessages.subList(offset, Math.min(offset + limit, sourceMessages.size())), databaseSourceMessages);
            }

            ChannelController.getInstance().deleteAllMessages(channel.getChannelId());

            // Test selecting source and destination connector messages by metadata ID, status, offset, and limit
            sourceMessages = new ArrayList<ConnectorMessage>();
            destinationMessages = new ArrayList<ConnectorMessage>();
            
            for (int i = 1; i <= TEST_SIZE; i++) {
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), daoFactory).getConnectorMessages().get(0);
                Message processedMessage = channel.process(sourceMessage, true);
                sourceMessages.add(processedMessage.getConnectorMessages().get(0));
                destinationMessages.add(processedMessage.getConnectorMessages().get(1));
            }
            
            for (int i = 0; i <= Math.ceil((double) sourceMessages.size() / limit) - 1; i++) {
                int offset = i * limit;
                
                try {
                    dao = daoFactory.getDao();
                    databaseSourceMessages = dao.getConnectorMessages(channel.getChannelId(), channel.getServerId(), 0, Status.TRANSFORMED, offset, limit, null, null);
                    databaseDestinationMessages = dao.getConnectorMessages(channel.getChannelId(), channel.getServerId(), 1, Status.SENT, offset, limit, null, null);
                } finally {
                    TestUtils.close(dao);
                }
                
                // Assert the connector message lists are equal
                TestUtils.assertConnectorMessageListsEqual(sourceMessages.subList(offset, Math.min(offset + limit, sourceMessages.size())), databaseSourceMessages);
                TestUtils.assertConnectorMessageListsEqual(destinationMessages.subList(offset, Math.min(offset + limit, destinationMessages.size())), databaseDestinationMessages);
            }

            ChannelController.getInstance().deleteAllMessages(channel.getChannelId());
            System.out.println(daoTimer.getLog());
        } finally {
            channel.stop();
            channel.undeploy();
        }
    }

    /*
     * Start up a new channel and send messages
     * Get the connector message count for all channel messages, and assert:
     */
    @Test
    public final void testGetConnectorMessageCount() throws Exception {
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId, true, 1, 3);

        try {
            logger.info("Testing DonkeyDao.getConnectorMessageCount...");

            channel.deploy();
            channel.start();
            
            DonkeyDao dao = null;

            for (int i = 1; i <= TEST_SIZE; i++) {
                ((TestSourceConnector) channel.getSourceConnector()).readTestMessage(testMessage);

                try {
                    dao = daoFactory.getDao();
                    
                    // Test selecting all source connector messages with a specific metadata ID and status
                    assertEquals(i, dao.getConnectorMessageCount(channel.getChannelId(), channel.getServerId(), 0, Status.TRANSFORMED));
                    assertEquals(0, dao.getConnectorMessageCount(channel.getChannelId(), channel.getServerId(), 0, Status.SENT));
                } finally {
                    TestUtils.close(dao);
                }

                for (DestinationChain chain : channel.getDestinationChains()) {
                    for (Integer metaDataId : chain.getEnabledMetaDataIds()) {
                        
                        try {
                            dao = daoFactory.getDao();
                            
                            // Test selecting all destination connector messages with a specific metadata ID and status
                            assertEquals(0, dao.getConnectorMessageCount(channel.getChannelId(), channel.getServerId(), metaDataId, Status.TRANSFORMED));
                            assertEquals(i, dao.getConnectorMessageCount(channel.getChannelId(), channel.getServerId(), metaDataId, Status.SENT));
                        } finally {
                            TestUtils.close(dao);
                        }
                    }
                }
            }

            System.out.println(daoTimer.getLog());
        } finally {
            channel.stop();
            channel.undeploy();
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

        try {
            logger.info("Testing DonkeyDao.getUnfinishedMessages...");

            channel.deploy();
            channel.start();

            for (int i = 1; i <= TEST_SIZE; i++) {
                ConnectorMessage sourceMessage = TestUtils.createAndStoreNewMessage(new RawMessage(testMessage), channel.getChannelId(), channel.getServerId(), daoFactory).getConnectorMessages().get(0);
                Message processedMessage = channel.process(sourceMessage, false);
                
                // Since the message is never marked as finished, the response map is never updated in the DB.
                processedMessage.getConnectorMessages().get(0).getResponseMap().clear();
                
                messages.add(processedMessage);
            }

            List<Message> databaseMessages;
            DonkeyDao dao = null;
            
            try {
                dao = daoFactory.getDao();
                databaseMessages = dao.getUnfinishedMessages(channel.getChannelId(), channel.getServerId());
            } finally {
                TestUtils.close(dao);
            }
            
            assertEquals(messages.size(), databaseMessages.size());
            
            for (int i = 0; i <= messages.size() - 1; i++) {
                TestUtils.assertMessagesEqual(messages.get(i), databaseMessages.get(i));
            }

            System.out.println(daoTimer.getLog());
        } finally {
            channel.stop();
            channel.undeploy();
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
            metaDataColumns.add(new MetaDataColumn(type.toString() + "column", type, null));
        }
        channel.setMetaDataColumns(metaDataColumns);

        try {
            logger.info("Testing DonkeyDao.getMetaDataColumns...");

            channel.deploy();
            
            List<MetaDataColumn> daoMetaDataColumns;
            DonkeyDao dao = null;
            
            try {
                dao = daoFactory.getDao();
                daoMetaDataColumns = dao.getMetaDataColumns(channel.getChannelId());
            } finally {
                TestUtils.close(dao);
            }
            
            assertEquals(metaDataColumns.size(), daoMetaDataColumns.size());
            
            for (MetaDataColumn column : daoMetaDataColumns) {
                assertTrue(column.getName(), metaDataColumns.contains(column));
            }

            System.out.println(daoTimer.getLog());
        } finally {
            channel.undeploy();
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

        try {
            logger.info("Testing DonkeyDao.addMetaDataColumn...");

            channel.deploy();

            for (int i = 1; i <= TEST_SIZE; i++) {
                DonkeyDao dao = null;
                
                try {
                    dao = daoFactory.getDao();
                    
                    for (MetaDataColumnType type : MetaDataColumnType.values()) {
                        MetaDataColumn metaDataColumn = new MetaDataColumn(type.toString() + "column" + i, type, null);
                        dao.addMetaDataColumn(channel.getChannelId(), metaDataColumn);
                        metaDataColumns.add(metaDataColumn);
                    }
                    
                    logger.debug("Adding metadata column set " + i);
                    dao.commit();
                } finally {
                    TestUtils.close(dao);
                }

                // Assert that the columns were added
                assertEquals(metaDataColumns, TestUtils.getExistingMetaDataColumns(channel.getChannelId()));
            }

            System.out.println(daoTimer.getLog());
        } finally {
            channel.undeploy();
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

        try {
            logger.info("Testing DonkeyDao.addMetaDataColumn...");

            channel.deploy();
            DonkeyDao dao = null;
            
            try {
                dao = daoFactory.getDao();
                
                for (int i = 1; i <= TEST_SIZE; i++) {
                    for (MetaDataColumnType type : MetaDataColumnType.values()) {
                        MetaDataColumn metaDataColumn = new MetaDataColumn(type.toString() + "column" + i, type, null);
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
            } finally {
                TestUtils.close(dao);
            }

            List<MetaDataColumn> databaseMetaDataColumns = TestUtils.getExistingMetaDataColumns(channel.getChannelId());

            // Assert that the columns in the database do not contain any of the columns previously added
            for (MetaDataColumn metaDataColumn : metaDataColumns) {
                assertFalse(databaseMetaDataColumns.contains(metaDataColumn));
            }

            System.out.println(daoTimer.getLog());
        } finally {
            channel.undeploy();
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
        DonkeyDaoFactory daoFactory = ((TimedDaoFactory)((BufferedDaoFactory) Donkey.getInstance().getDaoFactory()).getDelegateFactory()).getDelegateFactory();
        
        if (!(daoFactory instanceof JdbcDaoFactory)) {
            System.out.println("Skipping testJdbcDaoStatementCache() because the current DonkeyDaoFactory is not an instance of JdbcDaoFactory");
            return;
        }
        
        if (Donkey.getInstance().getConfiguration().getDatabaseProperties().get("database").equals("derby")) {
            System.out.println("Skipping testJdbcDaoStatementCache(), not applicable for Derby");
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
//        }
//    }
}
