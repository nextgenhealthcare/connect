/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.test.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.channel.MetaDataColumnException;
import com.mirth.connect.donkey.model.channel.MetaDataColumnType;
import com.mirth.connect.donkey.model.channel.ResponseConnectorProperties;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.DonkeyConfiguration;
import com.mirth.connect.donkey.server.PassthruEncryptor;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.channel.DestinationChain;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.FilterTransformerExecutor;
import com.mirth.connect.donkey.server.channel.MetaDataReplacer;
import com.mirth.connect.donkey.server.channel.ResponseSelector;
import com.mirth.connect.donkey.server.channel.ResponseTransformerExecutor;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.channel.StorageSettings;
import com.mirth.connect.donkey.server.channel.components.ResponseTransformer;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.server.controllers.MessageController;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.DonkeyDaoException;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;
import com.mirth.connect.donkey.server.data.buffered.BufferedDaoFactory;
import com.mirth.connect.donkey.server.data.passthru.DelayedStatisticsUpdater;
import com.mirth.connect.donkey.server.data.passthru.PassthruDaoFactory;
import com.mirth.connect.donkey.server.event.Event;
import com.mirth.connect.donkey.server.message.DataType;
import com.mirth.connect.donkey.server.queue.ConnectorMessageQueue;
import com.mirth.connect.donkey.server.queue.ConnectorMessageQueueDataSource;
import com.mirth.connect.donkey.util.ResourceUtil;
import com.mirth.connect.donkey.util.Serializer;

public class TestUtils {
    final public static String TEST_HL7_MESSAGE = "MSH|^~\\&|LABNET|Acme Labs|||20090601105700||ORU^R01|HMCDOOGAL-0088|D|2.2\rPID|1|8890088|8890088^^^72777||McDoogal^Hattie^||19350118|F||2106-3|100 Beach Drive^Apt. 5^Mission Viejo^CA^92691^US^H||(949) 555-0025|||||8890088^^^72|604422825\rPV1|1|R|C3E^C315^B||||2^HIBBARD^JULIUS^|5^ZIMMERMAN^JOE^|9^ZOIDBERG^JOHN^|CAR||||4|||2301^OBRIEN, KEVIN C|I|1783332658^1^1||||||||||||||||||||DISNEY CLINIC||N|||20090514205600\rORC|RE|928272608|056696716^LA||CM||||20090601105600||||  C3E|||^RESULT PERFORMED\rOBR|1|928272608|056696716^LA|1001520^K|||20090601101300|||MLH25|||HEMOLYZED/VP REDRAW|20090601102400||2301^OBRIEN, KEVIN C||||01123085310001100100152023509915823509915800000000101|0000915200932|20090601105600||LAB|F||^^^20090601084100^^ST~^^^^^ST\rOBX|1|NM|1001520^K||5.3|MMOL/L|3.5-5.5||||F|||20090601105600|IIM|IIM\r";
    final public static String TEST_HL7_ACK = "MSH|^~\\&|||LABNET|AcmeLabs|20090601105700||ACK|HMCDOOGAL-0088|D|2.2\rMSA|AA|HMCDOOGAL-0088\r";

    final private static String DONKEY_CONFIGURATION_FILE = "donkey-testing.properties";
    final private static String PERFORMANCE_LOG_FILE = null;

    final public static String DEFAULT_CHANNEL_ID = "testchannel";
    final public static String DEFAULT_SERVER_ID = "testserver";
    final public static String DEFAULT_RESPOND_FROM_NAME = ResponseConnectorProperties.RESPONSE_SOURCE_TRANSFORMED;
    final public static String DEFAULT_DESTINATION_NAME = "testdestination";
    final public static String DEFAULT_OUTBOUND_TEMPLATE = null;
    
    private static Logger logger = Logger.getLogger(TestUtils.class);

    public static DonkeyDaoFactory getDaoFactory() {
        return new BufferedDaoFactory(Donkey.getInstance().getDaoFactory());
    }
    
    public static void initChannel(String channelId) throws SQLException {
        ChannelController channelController = ChannelController.getInstance();
        
        if (channelController.channelExists(channelId)) {
            channelController.deleteAllMessages(channelId);
            TestUtils.deleteChannelStatistics(channelId);
        }
        
        ChannelController.getInstance().initChannelStorage(channelId);
    }
    
    public static TestChannel createDefaultChannel(String channelId, String serverId) throws SQLException {
        initChannel(channelId);

        TestChannel channel = new TestChannel();

        channel.setChannelId(channelId);
        channel.setServerId(serverId);
        channel.setEnabled(true);

        channel.setPreProcessor(new TestPreProcessor());
        channel.setPostProcessor(new TestPostProcessor());

        TestSourceConnector sourceConnector = (TestSourceConnector) TestUtils.createDefaultSourceConnector();
        sourceConnector.setChannel(channel);
        channel.setSourceConnector(sourceConnector);
        channel.setResponseSelector(new ResponseSelector(sourceConnector.getInboundDataType()));
        channel.setSourceFilterTransformer(TestUtils.createDefaultFilterTransformerExecutor());

        TestDestinationConnector destinationConnector = (TestDestinationConnector) TestUtils.createDefaultDestinationConnector();
        destinationConnector.setChannelId(channelId);
        destinationConnector.setMetaDataId(1);
        destinationConnector.setResponseTransformerExecutor(TestUtils.createDefaultResponseTransformerExecutor());

        DestinationChain chain = new DestinationChain();
        chain.setChannelId(channelId);
        chain.setMetaDataReplacer(sourceConnector.getMetaDataReplacer());
        chain.setMetaDataColumns(channel.getMetaDataColumns());
        chain.addDestination(1, TestUtils.createDefaultFilterTransformerExecutor(), destinationConnector);
        channel.addDestinationChain(chain);

        return channel;
    }

    public static TestChannel createDefaultChannel(String channelId, String serverId, int numChains, int numDestinationsPerChain) throws SQLException {
        return createDefaultChannel(channelId, serverId, true, numChains, numDestinationsPerChain, new StorageSettings());
    }

    public static TestChannel createDefaultChannel(String channelId, String serverId, Boolean respondAfterProcessing, int numChains, int numDestinationsPerChain) throws SQLException {
        return createDefaultChannel(channelId, serverId, respondAfterProcessing, numChains, numDestinationsPerChain, new StorageSettings());
    }

    private static TestChannel createDefaultChannel(String channelId, String serverId, Boolean respondAfterProcessing, int numChains, int numDestinationsPerChain, StorageSettings storageSettings) throws SQLException {
        initChannel(channelId);
        
        TestChannel channel = new TestChannel();

        channel.setChannelId(channelId);
        channel.setServerId(serverId);
        channel.setEnabled(true);
        channel.setStorageSettings(storageSettings);

        if (storageSettings.isEnabled()) {
            channel.setDaoFactory(new BufferedDaoFactory(Donkey.getInstance().getDaoFactory()));
        } else {
            channel.setDaoFactory(new PassthruDaoFactory(new DelayedStatisticsUpdater(Donkey.getInstance().getDaoFactory())));
        }

        channel.setPreProcessor(new TestPreProcessor());
        channel.setPostProcessor(new TestPostProcessor());

        TestSourceConnector sourceConnector = (TestSourceConnector) TestUtils.createDefaultSourceConnector();
        sourceConnector.setRespondAfterProcessing(respondAfterProcessing);
        sourceConnector.setChannel(channel);

        channel.setSourceConnector(sourceConnector);
        channel.setSourceFilterTransformer(TestUtils.createDefaultFilterTransformerExecutor());

        for (int i = 1; i <= numChains; i++) {
            DestinationChain chain = new DestinationChain();
            chain.setMetaDataReplacer(new MetaDataReplacer());
            chain.setChannelId(channelId);
            chain.setMetaDataReplacer(sourceConnector.getMetaDataReplacer());
            chain.setMetaDataColumns(channel.getMetaDataColumns());

            for (int j = 1; j <= numDestinationsPerChain; j++) {
                int metaDataId = (i - 1) * numDestinationsPerChain + j;
                TestDestinationConnector destinationConnector = (TestDestinationConnector) TestUtils.createDestinationConnector(channel.getChannelId(), new TestConnectorProperties(), TestUtils.DEFAULT_DESTINATION_NAME, new TestDataType(), new TestDataType(), new TestResponseTransformer(), metaDataId);
                destinationConnector.setChannelId(channelId);
                chain.addDestination(metaDataId, TestUtils.createDefaultFilterTransformerExecutor(), destinationConnector);
            }

            channel.addDestinationChain(chain);
        }

        return channel;
    }

    public static SourceConnector createDefaultSourceConnector() {
        return createSourceConnector(new TestConnectorProperties(), new TestDataType(), new TestDataType());
    }

    public static SourceConnector createSourceConnector(ConnectorProperties connectorProperties, DataType inboundDataType, DataType outboundDataType) {
        SourceConnector sourceConnector = new TestSourceConnector();

        sourceConnector.setConnectorProperties(connectorProperties);
        sourceConnector.setInboundDataType(inboundDataType);
        sourceConnector.setOutboundDataType(outboundDataType);
        sourceConnector.setMetaDataReplacer(new MetaDataReplacer());

        return sourceConnector;
    }

    public static DestinationConnector createDefaultDestinationConnector() {
        return createDestinationConnector(DEFAULT_CHANNEL_ID, new TestConnectorProperties(), DEFAULT_DESTINATION_NAME, new TestDataType(), new TestDataType(), new TestResponseTransformer(), 1);
    }

    public static DestinationConnector createDestinationConnector(String channelId, ConnectorProperties connectorProperties, String name, DataType inboundDataType, DataType outboundDataType, ResponseTransformer responseTransformer, Integer metaDataId) {
        DestinationConnector destinationConnector = new TestDestinationConnector();
        initDestinationConnector(destinationConnector, channelId, connectorProperties, name, inboundDataType, outboundDataType, responseTransformer, metaDataId);
        return destinationConnector;
    }

    public static void initDefaultDestinationConnector(DestinationConnector destinationConnector, ConnectorProperties connectorProperties) {
        initDestinationConnector(destinationConnector, DEFAULT_CHANNEL_ID, connectorProperties, DEFAULT_DESTINATION_NAME, new TestDataType(), new TestDataType(), new TestResponseTransformer(), 1);
    }

    public static void initDestinationConnector(DestinationConnector destinationConnector, String channelId, ConnectorProperties connectorProperties, String name, DataType inboundDataType, DataType outboundDataType, ResponseTransformer responseTransformer, Integer metaDataId) {
        destinationConnector.setChannelId(channelId);
        destinationConnector.setConnectorProperties(connectorProperties);
        destinationConnector.setDestinationName(name);
        destinationConnector.setInboundDataType(inboundDataType);
        destinationConnector.setOutboundDataType(outboundDataType);
        destinationConnector.setResponseTransformerExecutor(createDefaultResponseTransformerExecutor());
        destinationConnector.setMetaDataId(metaDataId);

        ConnectorMessageQueue destinationConnectorQueue = new ConnectorMessageQueue();
        destinationConnectorQueue.setDataSource(new ConnectorMessageQueueDataSource(channelId, metaDataId, Status.QUEUED, false, getDaoFactory(), new PassthruEncryptor()));
        destinationConnector.setQueue(destinationConnectorQueue);
    }

    public static FilterTransformerExecutor createDefaultFilterTransformerExecutor() {
        FilterTransformerExecutor filterTransformerExecutor = new FilterTransformerExecutor(new TestDataType(), new TestDataType());
        filterTransformerExecutor.setFilterTransformer(new TestFilterTransformer());
        return filterTransformerExecutor;
    }
    
    public static ResponseTransformerExecutor createDefaultResponseTransformerExecutor() {
    	ResponseTransformerExecutor responseTransformerExecutor = new ResponseTransformerExecutor(new TestDataType(), new TestDataType());
    	responseTransformerExecutor.setResponseTransformer(new TestResponseTransformer());
        return responseTransformerExecutor;
    }

    public static Connection getConnection() {
        Properties properties = Donkey.getInstance().getConfiguration().getDatabaseProperties();
        Connection connection = null;

        try {
            String driver = properties.getProperty("database.driver");

            if (driver != null) {
                Class.forName(driver);
            }

            connection = DriverManager.getConnection(properties.getProperty("database.url"), properties.getProperty("database.username"), properties.getProperty("database.password"));
            connection.setAutoCommit(false);
        } catch (Exception e) {
            throw new DonkeyDaoException("Failed to establish JDBC connection", e);
        }

        return connection;
    }

    public static void assertChannelExists(String channelId) throws SQLException {
        assertChannelExists(channelId, false);
    }

    public static void assertChannelExists(String channelId, boolean checkMessageTables) throws SQLException {
        boolean channelExists = false;
        long localChannelId = 0;
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement("SELECT local_channel_id FROM d_channels WHERE channel_id = ?");
            statement.setString(1, channelId);
            result = statement.executeQuery();

            if (result.next()) {
                channelExists = true;
                localChannelId = result.getLong("local_channel_id");
            }
            
            close(result);
            close(statement);

            if (checkMessageTables) {
                assertChannelMessageTablesExist(connection, localChannelId);
            }
            
            assertTrue(channelExists);
        } finally {
            close(result);
            close(statement);
            close(connection);
        }
    }
    
    public static void assertChannelDoesNotExist(String channelId) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement("SELECT local_channel_id FROM d_channels WHERE channel_id = ?");
            statement.setString(1, channelId);
            result = statement.executeQuery();
            assertFalse(result.next());
        } finally {
            close(result);
            close(statement);
            close(connection);
        }
    }

    public static void assertChannelMessageTablesExist(Connection connection, long localChannelId) throws SQLException {
        boolean messageTableExists = false;
        boolean messageMetaDataTableExists = false;
        boolean messageContentTableExists = false;
        boolean messageCustomMetaDataTableExists = false;
        boolean messageAttachmentTableExists = false;
        boolean messageStatisticsTableExists = false;
        ResultSet result = null;

        try {
            result = connection.getMetaData().getTables(null, null, "d_m%", null);
            
            if (!result.next()) {
                result = connection.getMetaData().getTables(null, null, "D_M%", null);
                
                if (!result.next()) {
                    throw new AssertionError();
                }
            }
            
            do {
                String name = result.getString("table_name").toLowerCase();
                
                if (name.equals("d_m" + localChannelId)) {
                    messageTableExists = true;
                } else if (name.equals("d_mm" + localChannelId)) {
                    messageMetaDataTableExists = true;
                } else if (name.equals("d_mc" + localChannelId)) {
                    messageContentTableExists = true;
                } else if (name.equals("d_mcm" + localChannelId)) {
                    messageCustomMetaDataTableExists = true;
                } else if (name.equals("d_ma" + localChannelId)) {
                    messageAttachmentTableExists = true;
                } else if (name.equals("d_ms" + localChannelId)) {
                    messageStatisticsTableExists = true;
                }
            } while (result.next());
            
            assertTrue(messageTableExists);
            assertTrue(messageMetaDataTableExists);
            assertTrue(messageContentTableExists);
            assertTrue(messageCustomMetaDataTableExists);
            assertTrue(messageAttachmentTableExists);
            assertTrue(messageStatisticsTableExists);
        } finally {
            close(result);
        }
    }

    public static void assertMessageExists(Message message, boolean deepSearch) throws SQLException {
        long localChannelId = ChannelController.getInstance().getLocalChannelId(message.getChannelId());

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement("SELECT * FROM d_m" + localChannelId + " WHERE id = ?");
            statement.setLong(1, message.getMessageId());
            result = statement.executeQuery();

            if (result.next()) {
                String serverId = result.getString("server_id");
                Boolean processed = result.getBoolean("processed");
                close(result);
                close(statement);

                assertTrue(testEquality(serverId, message.getServerId()));
                assertTrue(testEquality(processed, message.isProcessed()));

                if (deepSearch) {
                    for (ConnectorMessage connectorMessage : message.getConnectorMessages().values()) {
                        assertConnectorMessageExists(connectorMessage, deepSearch, connection);
                    }
                }
            } else {
                throw new AssertionError();
            }
        } finally {
            close(result);
            close(statement);
            close(connection);
        }
    }

    public static void assertMessageDoesNotExist(Message message) throws SQLException {
        long localChannelId = ChannelController.getInstance().getLocalChannelId(message.getChannelId());

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement("SELECT * FROM d_m" + localChannelId + " WHERE id = ?");
            statement.setLong(1, message.getMessageId());
            result = statement.executeQuery();
            assertFalse(result.next());
        } finally {
            close(result);
            close(statement);
            close(connection);
        }
    }

    public static void assertMessagesEqual(Message message1, Message message2) {
        if (message1 == null && message2 == null) {
            return;
        }
        assertEquals(message1.getChannelId(), message2.getChannelId());
        assertEquals(message1.getServerId(), message2.getServerId());
        assertEquals(message1.getMessageId(), message2.getMessageId());
        assertEquals(message1.getDateCreated(), message2.getDateCreated());
        assertEquals(message1.getConnectorMessages().keySet(), message2.getConnectorMessages().keySet());
        for (Integer metaDataId : message1.getConnectorMessages().keySet()) {
            assertConnectorMessagesEqual(message1.getConnectorMessages().get(metaDataId), message2.getConnectorMessages().get(metaDataId));
        }
    }

    public static void assertConnectorMessageExists(ConnectorMessage connectorMessage, boolean deepSearch) throws SQLException {
        Connection connection = null;

        try {
            connection = getConnection();
            assertConnectorMessageExists(connectorMessage, deepSearch, connection);
        } finally {
            close(connection);
        }
    }

    public static void assertConnectorMessageExists(ConnectorMessage connectorMessage, boolean deepSearch, Connection connection) throws SQLException {
        long localChannelId = ChannelController.getInstance().getLocalChannelId(connectorMessage.getChannelId());
        Serializer serializer = Donkey.getInstance().getSerializer();
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            statement = connection.prepareStatement("SELECT * FROM d_mm" + localChannelId + " WHERE message_id = ? AND id = ?");
            statement.setLong(1, connectorMessage.getMessageId());
            statement.setLong(2, connectorMessage.getMetaDataId());
            result = statement.executeQuery();
            
            if (!result.next()) {
                throw new AssertionError();
            }

            Calendar dateCreated = Calendar.getInstance();
            dateCreated.setTimeInMillis(result.getTimestamp("date_created").getTime());
            Status status = Status.fromChar(result.getString("status").charAt(0));
            Map<String, Object> connectorMap = (Map<String, Object>) serializer.deserialize(result.getString("connector_map"));
            Map<String, Object> channelMap = (Map<String, Object>) serializer.deserialize(result.getString("channel_map"));
            Map<String, Object> responseMap = (Map<String, Object>) serializer.deserialize(result.getString("response_map"));
            String errors = (String) serializer.deserialize(result.getString("errors"));
            
            assertTrue(testEquality(dateCreated, connectorMessage.getDateCreated()));
            assertTrue(testEquality(status, connectorMessage.getStatus()));
            assertTrue(testEquality(connectorMap, connectorMessage.getConnectorMap()));
            assertTrue(testEquality(channelMap, connectorMessage.getChannelMap()));
            assertTrue(testEquality(responseMap, connectorMessage.getResponseMap()));
            assertTrue(testEquality(errors, connectorMessage.getErrors()));
        } finally {
            close(result);
            close(statement);
        }

        if (deepSearch) {
            for (ContentType contentType : ContentType.values()) {
                // Even though raw content exists on the destination connector message, it won't be stored in the database
                if (contentType != ContentType.RAW || connectorMessage.getMetaDataId() == 0) {
                    MessageContent messageContent = connectorMessage.getContent(contentType);

                    if (messageContent != null) {
                        assertMessageContentExists(messageContent);
                    }
                }
            }
        }
    }

    public static void assertConnectorMessageDoesNotExist(ConnectorMessage message) throws SQLException {
        long localChannelId = ChannelController.getInstance().getLocalChannelId(message.getChannelId());

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement("SELECT * FROM d_mm" + localChannelId + " WHERE message_id = ? AND id = ?");
            statement.setLong(1, message.getMessageId());
            statement.setInt(2, message.getMetaDataId());
            result = statement.executeQuery();
            assertFalse(result.next());
        } finally {
            close(result);
            close(statement);
            close(connection);
        }
    }

    public static void assertConnectorMessageStatusEquals(String channelId, long messageId, int metaDataId, Status status) throws SQLException {
        long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        
        try {
            connection = getConnection();
            statement = connection.prepareStatement("SELECT status FROM d_mm" + localChannelId + " WHERE message_id = ? AND id = ?");
            statement.setLong(1, messageId);
            statement.setInt(2, metaDataId);
            result = statement.executeQuery();
            result.next();
            assertEquals(status, Status.fromChar(result.getString("status").charAt(0)));
        } finally {
            close(result);
            close(statement);
            close(connection);
        }
    }

    public static void assertConnectorMessageListsEqual(List<ConnectorMessage> list1, List<ConnectorMessage> list2) {
        assertEquals(list1.size(), list2.size());
        for (int i = 0; i <= list1.size() - 1; i++) {
            assertConnectorMessagesEqual(list1.get(i), list2.get(i));
        }
    }

    public static void assertConnectorMessagesEqual(ConnectorMessage connectorMessage1, ConnectorMessage connectorMessage2) {
        if (connectorMessage1 == null && connectorMessage2 == null) {
            return;
        }
        assertEquals(connectorMessage1.getMessageId(), connectorMessage2.getMessageId());
        assertEquals(connectorMessage1.getMetaDataId(), connectorMessage2.getMetaDataId());
        assertEquals(connectorMessage1.getChannelId(), connectorMessage2.getChannelId());
        assertEquals(connectorMessage1.getConnectorName(), connectorMessage2.getConnectorName());
        assertEquals(connectorMessage1.getServerId(), connectorMessage2.getServerId());
        assertEquals(connectorMessage1.getDateCreated(), connectorMessage2.getDateCreated());
        assertEquals(connectorMessage1.getStatus(), connectorMessage2.getStatus());
        assertMessageContentsEqual(connectorMessage1.getRaw(), connectorMessage2.getRaw());
        assertMessageContentsEqual(connectorMessage1.getProcessedRaw(), connectorMessage2.getProcessedRaw());
        assertMessageContentsEqual(connectorMessage1.getTransformed(), connectorMessage2.getTransformed());
        assertMessageContentsEqual(connectorMessage1.getEncoded(), connectorMessage2.getEncoded());
        assertMessageContentsEqual(connectorMessage1.getSent(), connectorMessage2.getSent());
        assertMessageContentsEqual(connectorMessage1.getResponse(), connectorMessage2.getResponse());
        assertMessageContentsEqual(connectorMessage1.getResponseTransformed(), connectorMessage2.getResponseTransformed());
        assertMessageContentsEqual(connectorMessage1.getProcessedResponse(), connectorMessage2.getProcessedResponse());
        assertEquals(connectorMessage1.getConnectorMap(), connectorMessage2.getConnectorMap());
        assertEquals(connectorMessage1.getChannelMap(), connectorMessage2.getChannelMap());
        assertEquals(connectorMessage1.getResponseMap(), connectorMessage2.getResponseMap());
        assertEquals(connectorMessage1.getMetaDataMap(), connectorMessage2.getMetaDataMap());
        assertEquals(connectorMessage1.getErrors(), connectorMessage2.getErrors());
        assertEquals(connectorMessage1.getSendAttempts(), connectorMessage2.getSendAttempts());
    }

    public static void assertMessageContentExists(MessageContent content) throws SQLException {
        long localChannelId = ChannelController.getInstance().getLocalChannelId(content.getChannelId());

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement("SELECT * FROM d_mc" + localChannelId + " WHERE message_id = ? AND metadata_id = ? AND content_type = ?");
            statement.setLong(1, content.getMessageId());
            statement.setLong(2, content.getMetaDataId());
            statement.setString(3, String.valueOf(content.getContentType().getContentTypeCode()));
            result = statement.executeQuery();

            if (result.next()) {
                if (result.getBoolean("is_encrypted")) {
                    assertTrue(testEquality(result.getString("content"), content.getEncryptedContent()));
                } else {
                    Serializer serializer = Donkey.getInstance().getSerializer();
                    Response queryObject = (Response) serializer.deserialize(result.getString("content"));
                    Response contentObject = (Response) serializer.deserialize(content.getContent());
                    assertTrue(testEquality(queryObject, contentObject));
                }
            } else {
                throw new AssertionError();
            }
        } finally {
            close(result);
            close(statement);
            close(connection);
        }
    }
    
    public static void close(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.rollback();
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void close(Statement statement) {
        try {
            DbUtils.close(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void close(PreparedStatement preparedStatement) {
        try {
            DbUtils.close(preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void close(ResultSet resultSet) {
        try {
            DbUtils.close(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void close(DonkeyDao dao) {
        if (dao != null && !dao.isClosed()) {
            dao.close();
        }
    }

    public static void assertMessageContentDoesNotExist(MessageContent content) throws SQLException {
        long localChannelId = ChannelController.getInstance().getLocalChannelId(content.getChannelId());

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement("SELECT * FROM d_mc" + localChannelId + " WHERE message_id = ? AND metadata_id = ? AND content_type = ?");
            statement.setLong(1, content.getMessageId());
            statement.setInt(2, content.getMetaDataId());
            statement.setString(3, String.valueOf(content.getContentType().getContentTypeCode()));
            result = statement.executeQuery();
            assertFalse(result.next());
        } finally {
            close(result);
            close(statement);
            close(connection);
        }
    }

    public static void assertMessageContentsEqual(MessageContent messageContent1, MessageContent messageContent2) {
        if (messageContent1 == null && messageContent2 == null) {
            return;
        }
        assertEquals(messageContent1.getChannelId(), messageContent2.getChannelId());
        assertEquals(messageContent1.getMessageId(), messageContent2.getMessageId());
        assertEquals(messageContent1.getMetaDataId(), messageContent2.getMetaDataId());
        assertEquals(messageContent1.getContentType(), messageContent2.getContentType());
        assertEquals(messageContent1.getContent(), messageContent2.getContent());
        assertEquals(messageContent1.getEncryptedContent(), messageContent2.getEncryptedContent());
    }

    public static void assertAttachmentExists(String channelId, long messageId, Attachment attachment) throws SQLException {
        long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        
        try {
            connection = getConnection();
            statement = connection.prepareStatement("SELECT * FROM d_ma" + localChannelId + " WHERE message_id = ? AND id = ?");
            statement.setLong(1, messageId);
            statement.setString(2, attachment.getId());
            result = statement.executeQuery();
            assertTrue(result.next());
            byte[] content = result.getBytes("content");
            String type = result.getString("type");
            assertTrue(Arrays.equals(content, attachment.getContent()));
            assertTrue(testEquality(type, attachment.getType()));
        } finally {
            close(result);
            close(statement);
            close(connection);
        }
    }

    public static void assertEventExists(Event event) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        
        try {
            connection = getConnection();
            statement = connection.prepareStatement("SELECT * FROM d_events WHERE event_type = ? AND channel_id = ? AND metadata_id = ? AND message_id = ? AND message_status = ? AND event_date = ?");
            statement.setInt(1, event.getEventType().getEventCode());
            statement.setString(2, event.getChannelId());
            statement.setInt(3, event.getMetaDataId());
            statement.setLong(4, event.getMessageId());
            statement.setString(5, String.valueOf(event.getMessageStatus().getStatusCode()));
            statement.setTimestamp(6, new Timestamp(event.getEventDate().getTimeInMillis()));
            result = statement.executeQuery();
            assertTrue(result.next());
        } finally {
            close(result);
            close(statement);
            close(connection);
        }
    }

    public static void assertResponseExists(String channelId, long messageId) throws SQLException {
        long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        
        // Assert that the source connector response was created
        try {
            connection = TestUtils.getConnection();
            statement = connection.prepareStatement("SELECT * FROM d_mc" + localChannelId + " WHERE message_id = ? AND metadata_id = ? AND content_type = ?");
            statement.setLong(1, messageId);
            statement.setInt(2, 0);
            statement.setString(3, String.valueOf(ContentType.SENT.getContentTypeCode()));
            result = statement.executeQuery();
            assertTrue(result.next());
        } finally {
            close(result);
            close(statement);
            close(connection);
        }
    }

    public static void assertResponseDoesNotExist(String channelId, long messageId) throws SQLException {
        long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        
        // Assert that the source connector response was created
        try {
            connection = TestUtils.getConnection();
            statement = connection.prepareStatement("SELECT * FROM d_mc" + localChannelId + " WHERE message_id = ? AND metadata_id = ? AND content_type = ?");
            statement.setLong(1, messageId);
            statement.setInt(2, 0);
            statement.setString(3, String.valueOf(ContentType.SENT.getContentTypeCode()));
            result = statement.executeQuery();
            assertFalse(result.next());
        } finally {
            close(result);
            close(statement);
            close(connection);
        }
    }

    private static boolean testEquality(Object object1, Object object2) {
        if (object1 != null && object2 != null) {
            return object1.equals(object2);
        } else if (object1 == null && object2 == null) {
            return true;
        } else {
            return false;
        }
    }

    public static List<MetaDataColumn> getExistingMetaDataColumns(String channelId) throws SQLException {
        long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);
        List<MetaDataColumn> metaDataColumns = new ArrayList<MetaDataColumn>();
        Connection connection = null;
        ResultSet columns = null;
        
        try {
            connection = getConnection();
            columns = connection.getMetaData().getColumns(connection.getCatalog(), null, "d_mcm" + localChannelId, null);
            
            if (!columns.next()) {
                columns = connection.getMetaData().getColumns(connection.getCatalog(), null, "D_MCM" + localChannelId, null);
                
                if (!columns.next()) {
                    return metaDataColumns;
                }
            }

            do {
                String name = columns.getString("COLUMN_NAME").toUpperCase();
                
                if (!name.equals("METADATA_ID") && !name.equals("MESSAGE_ID")) {
                    int type = columns.getInt("DATA_TYPE");
                    
                    if (type == Types.VARCHAR || type == Types.NVARCHAR || type == Types.LONGVARCHAR || type == Types.LONGNVARCHAR) {
                        metaDataColumns.add(new MetaDataColumn(name, MetaDataColumnType.STRING, null));
                    } else if (type == Types.BIGINT || type == Types.INTEGER || type == Types.SMALLINT || type == Types.TINYINT) {
                        metaDataColumns.add(new MetaDataColumn(name, MetaDataColumnType.LONG, null));
                    } else if (type == Types.DOUBLE || type == Types.FLOAT || type == Types.DECIMAL || type == Types.REAL || type == Types.NUMERIC) {
                        metaDataColumns.add(new MetaDataColumn(name, MetaDataColumnType.DOUBLE, null));
                    } else if (type == Types.BOOLEAN || type == Types.BIT) {
                        metaDataColumns.add(new MetaDataColumn(name, MetaDataColumnType.BOOLEAN, null));
                    } else if (type == Types.DATE) {
                        metaDataColumns.add(new MetaDataColumn(name, MetaDataColumnType.DATE, null));
                    } else if (type == Types.TIME) {
                        metaDataColumns.add(new MetaDataColumn(name, MetaDataColumnType.TIME, null));
                    } else if (type == Types.TIMESTAMP) {
                        metaDataColumns.add(new MetaDataColumn(name, MetaDataColumnType.TIMESTAMP, null));
                    }
                }
            } while (columns.next());
        } finally {
            close(columns);
            close(connection);
        }

        return metaDataColumns;
    }

    public static Map<String, Object> getCustomMetaData(String channelId, long messageId, int metaDataId) throws SQLException {
        Map<String, Object> map = new HashMap<String, Object>();
        List<MetaDataColumn> columns = getExistingMetaDataColumns(channelId);

        if (columns.size() > 0) {
            long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet result = null;

            try {
                connection = getConnection();
                statement = connection.prepareStatement("SELECT * FROM d_mcm" + localChannelId + " WHERE message_id = ? AND metadata_id = ?");
                statement.setLong(1, messageId);
                statement.setInt(2, metaDataId);
                result = statement.executeQuery();
                result.next();

                for (MetaDataColumn column : columns) {
                    result.getObject(column.getName());

                    if (!result.wasNull()) {
                        // @formatter:off
                        switch (column.getType()) {
                            case BOOLEAN: map.put(column.getName(), result.getBoolean(column.getName())); break;
                            case LONG: map.put(column.getName(), result.getLong(column.getName())); break;
                            case DOUBLE: map.put(column.getName(), result.getDouble(column.getName())); break;
                            case STRING: map.put(column.getName(), result.getString(column.getName())); break;
                            case DATE: case TIME: case TIMESTAMP:
                                Calendar calendar = Calendar.getInstance();
                                long millis = 0;
                                
                                switch (column.getType()) {
                                    case DATE: millis = result.getDate(column.getName()).getTime(); break;
                                    case TIME: millis = result.getTime(column.getName()).getTime(); break;
                                    case TIMESTAMP: millis = result.getTimestamp(column.getName()).getTime(); break;
                                }
                                
                                calendar.setTimeInMillis(millis);
                                map.put(column.getName(), calendar);
                                break;
                        }
                        // @formatter:on
                    }
                }
            } finally {
                close(result);
                close(statement);
                close(connection);
            }
        }

        return map;
    }

    public static void compareMetaDataMaps(List<MetaDataColumn> columns, Map<String, Object> map1, Map<String, Object> map2) {
        for (MetaDataColumn column : columns) {
            String name = column.getName();

            if (map1.containsKey(name) && map2.containsKey(name)) {
                compareMetaData(column.getType(), map1.get(name), map2.get(name));
            } else if (map1.containsKey(name) || map2.containsKey(name)) {
                throw new AssertionError();
            }
        }
    }

    private static void compareMetaData(MetaDataColumnType type, Object value1, Object value2) {
        if (value1 == null && value2 == null) {
            return;
        } else if (value1 == null || value2 == null) {
            throw new AssertionError();
        }

        String date1;
        String date2;

        switch (type) {
            case BOOLEAN:
            case DOUBLE:
            case LONG:
            case STRING:
                try {
                    assertEquals(type.toString(), type.castMetaDataFromString(value1.toString()), type.castMetaDataFromString(value2.toString()));
                } catch (MetaDataColumnException e) {
                    throw new AssertionError();
                }
                break;

            case TIMESTAMP:
                assertEquals(type.toString(), ((Calendar) value1), (Calendar) value2);
                break;

            case DATE:
                date1 = new SimpleDateFormat("yyyyMMdd").format(((Calendar) value1).getTimeInMillis());
                date2 = new SimpleDateFormat("yyyyMMdd").format(((Calendar) value2).getTimeInMillis());
                assertEquals(type.toString(), date1, date2);
                break;

            case TIME:
                date1 = new SimpleDateFormat("HHmmss").format(((Calendar) value1).getTimeInMillis());
                date2 = new SimpleDateFormat("HHmmss").format(((Calendar) value2).getTimeInMillis());
                assertEquals(type.toString(), date1, date2);
                break;

            default:
                throw new AssertionError();
        }
    }

    public static Map<String, Object> getConnectorMap(String channelId, long messageId, int metaDataId) throws SQLException {
        long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);
        Serializer serializer = Donkey.getInstance().getSerializer();
        String connectorMap;
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement("SELECT connector_map FROM d_mm" + localChannelId + " WHERE message_id = ? AND id = ?");
            statement.setLong(1, messageId);
            statement.setInt(2, metaDataId);
            result = statement.executeQuery();
            result.next();
            connectorMap = result.getString("connector_map");
        } finally {
            close(result);
            close(statement);
            close(connection);
        }
        
        return (Map<String, Object>) serializer.deserialize(connectorMap);
    }

    public static Map<String, Object> getChannelMap(String channelId, long messageId, int metaDataId) throws SQLException {
        long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);
        Serializer serializer = Donkey.getInstance().getSerializer();
        String channelMap;
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement("SELECT channel_map FROM d_mm" + localChannelId + " WHERE message_id = ? AND id = ?");
            statement.setLong(1, messageId);
            statement.setInt(2, metaDataId);
            result = statement.executeQuery();
            result.next();
            channelMap = result.getString("channel_map");
        } finally {
            close(result);
            close(statement);
            close(connection);
        }
        
        return (Map<String, Object>) serializer.deserialize(channelMap);
    }

    public static Map<String, Object> getResponseMap(String channelId, long messageId, int metaDataId) throws SQLException {
        long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);
        Serializer serializer = Donkey.getInstance().getSerializer();
        String responseMap;
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement("SELECT response_map FROM d_mm" + localChannelId + " WHERE message_id = ? AND id = ?");
            statement.setLong(1, messageId);
            statement.setInt(2, metaDataId);
            result = statement.executeQuery();
            result.next();
            responseMap = result.getString("response_map");
        } finally {
            close(result);
            close(statement);
            close(connection);
        }
        
        return (Map<String, Object>) serializer.deserialize(responseMap);
    }

    public static boolean isMessageProcessed(String channelId, long messageId) throws SQLException {
        long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement("SELECT processed FROM d_m" + localChannelId + " WHERE id = ?");
            statement.setLong(1, messageId);
            result = statement.executeQuery();
            result.next();
            return result.getBoolean("processed");
        } finally {
            close(result);
            close(statement);
            close(connection);
        }
    }

    public static Map<Integer, Map<Status, Long>> getChannelStatistics(String channelId) throws SQLException {
        long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);
        Map<Integer, Map<Status, Long>> stats = new HashMap<Integer, Map<Status, Long>>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        
        try {
            connection = getConnection();
            statement = connection.prepareStatement("SELECT * FROM d_ms" + localChannelId);
            result = statement.executeQuery();
            
            while (result.next()) {
                Map<Status, Long> connectorStats = new HashMap<Status, Long>();
                connectorStats.put(Status.RECEIVED, result.getLong("received"));
                connectorStats.put(Status.FILTERED, result.getLong("filtered"));
                connectorStats.put(Status.TRANSFORMED, result.getLong("transformed"));
                connectorStats.put(Status.PENDING, result.getLong("pending"));
                connectorStats.put(Status.SENT, result.getLong("sent"));
                connectorStats.put(Status.ERROR, result.getLong("error"));
                Integer metaDataId = result.getInt("metadata_id");
                
                if (result.wasNull()) {
                    metaDataId = null;
                }
                
                stats.put(metaDataId, connectorStats);
            }
        } finally {
            close(result);
            close(statement);
            close(connection);
        }
        
        return stats;
    }

    public static void deleteChannelStatistics(String channelId) throws SQLException {
        long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement("DELETE FROM d_ms" + localChannelId);
            statement.executeUpdate();
            connection.commit();
        } finally {
            close(statement);
            close(connection);
        }

        ChannelController.getInstance().getStatistics().getStats().remove(channelId);
    }

    public static String getPerformanceText(int numDestinations, long milliseconds, List<Long> times) throws IOException {
        double seconds = ((double) milliseconds) / 1000d;
        double speed = ((double) times.size()) / seconds;

        long sum = 0;
        Writer writer = null;

        if (PERFORMANCE_LOG_FILE != null) {
            writer = new BufferedWriter(new FileWriter(new File(PERFORMANCE_LOG_FILE)));
        }

        for (Long time : times) {
            sum += time;

            if (writer != null) {
                writer.append(time + "\n");
            }
        }

        if (writer != null) {
            writer.flush();
        }

        Collections.sort(times);

        StringBuilder stringBuilder = new StringBuilder();
        final int padding = 17;

        stringBuilder.append(StringUtils.rightPad("Messages Sent:", padding) + times.size() + "\n");
        stringBuilder.append(StringUtils.rightPad("Inbound:", padding) + Precision.round(speed, 2) + " messages/second\n");
        stringBuilder.append(StringUtils.rightPad("Outbound:", padding) + Precision.round(speed * numDestinations, 2) + " messages/second\n");

        if (times.size() > 0) {
            stringBuilder.append(StringUtils.rightPad("Lowest Time:", padding) + times.get(0) + "ms\n");
            stringBuilder.append(StringUtils.rightPad("Highest Time:", padding) + times.get(times.size() - 1) + "ms\n");
            stringBuilder.append(StringUtils.rightPad("Median Time:", padding) + times.get(times.size() / 2) + "ms\n");
            stringBuilder.append(StringUtils.rightPad("Average Time:", padding) + Precision.round((double) sum / (double) times.size(), 2) + "ms\n");
            stringBuilder.append(StringUtils.rightPad("Total Send Time:", padding) + sum + "ms\n");
            stringBuilder.append(StringUtils.rightPad("Total Test Time:", padding) + milliseconds + "ms\n");
        }

        return stringBuilder.toString();
    }

    public static Message createAndStoreNewMessage(RawMessage rawMessage, String channelId, String serverId) {
        return createAndStoreNewMessage(rawMessage, channelId, serverId, getDaoFactory());
    }
    
    public static Message createAndStoreNewMessage(RawMessage rawMessage, String channelId, String serverId, DonkeyDaoFactory daoFactory) {
        Message message = MessageController.getInstance().createNewMessage(channelId, serverId);
        ConnectorMessage sourceMessage = new ConnectorMessage(channelId, message.getMessageId(), 0, serverId, message.getDateCreated(), Status.RECEIVED);
        sourceMessage.setRaw(new MessageContent(channelId, message.getMessageId(), 0, ContentType.RAW, rawMessage.getRawData(), null, null));

        if (rawMessage.getChannelMap() != null) {
            sourceMessage.setChannelMap(rawMessage.getChannelMap());
        }

        message.getConnectorMessages().put(0, sourceMessage);

        DonkeyDao dao = null;
        
        try {
            dao = daoFactory.getDao();
            dao.insertMessage(message);
            dao.insertConnectorMessage(sourceMessage, true);
            dao.insertMessageContent(sourceMessage.getRaw());
            dao.commit();
        } finally {
            close(dao);
        }
        
        return message;
    }

    public static ConnectorMessage createAndStoreDestinationConnectorMessage(DonkeyDaoFactory daoFactory, String channelId, String serverId, long messageId, int metaDataId, String rawContent, Status status) {
        ConnectorMessage connectorMessage = new ConnectorMessage(channelId, messageId, metaDataId, serverId, Calendar.getInstance(), status);
        connectorMessage.setRaw(new MessageContent(channelId, messageId, metaDataId, ContentType.RAW, rawContent, null, null));

        DonkeyDao dao = null;
        
        try {
            dao = daoFactory.getDao();
            dao.insertConnectorMessage(connectorMessage, false);
            dao.insertMessageContent(connectorMessage.getRaw());
            dao.commit();
        } finally {
            close(dao);
        }

        return connectorMessage;
    }

    public static Integer getSendAttempts(String channelId, long messageId) throws SQLException {
        long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement("SELECT send_attempts FROM d_mm" + localChannelId + " WHERE message_id = ? AND id = ?");
            statement.setLong(1, messageId);
            statement.setInt(2, 1);
            result = statement.executeQuery();

            if (result.next()) {
                return result.getInt(1);
            }

            return null;
        } finally {
            close(result);
            close(statement);
            close(connection);
        }
    }

    public static DonkeyConfiguration getDonkeyTestConfiguration() {
        try {
            Properties databaseProperties = new Properties();
            InputStream is = ResourceUtil.getResourceStream(Donkey.class, DONKEY_CONFIGURATION_FILE);
            databaseProperties.load(is);
            IOUtils.closeQuietly(is);
            return new DonkeyConfiguration(new File(".").getAbsolutePath(), databaseProperties);
        } catch (Exception e) {
            throw new DonkeyDaoException("Failed to read configuration file", e);
        }
    }

    public static void runChannelTest(String testMessage, String channelId, String serverId, final String testName, final int numChannels, final int numChains, final int numDestinations, final boolean respondAfterProcessing, final Integer testSize, final Integer testMillis, final Integer warmupMillis, StorageSettings storageSettings) throws Exception {
        Channel[] channels = new Channel[numChannels];

        for (int i = 0; i < numChannels; i++) {
            channels[i] = TestUtils.createDefaultChannel(channelId + i, serverId, respondAfterProcessing, numChains, numDestinations, storageSettings);
        }

        runChannelTest(testMessage, testName, testSize, testMillis, warmupMillis, channels);
    }
    
    public static String getDatabaseType() {
        return (String) Donkey.getInstance().getConfiguration().getDatabaseProperties().get("database");
    }

    public static void runChannelTest(String testMessage, final String testName, final Integer testSize, Integer testMillis, Integer warmupMillis, Channel[] channels) throws Exception {
        TestSourceConnector[] sourceConnectors = new TestSourceConnector[channels.length];
        List<List<Long>> sentMessageIds = new ArrayList<List<Long>>();
        boolean isPostgres = getDatabaseType().equals("postgres");

        for (int i = 0; i < channels.length; i++) {
            ChannelController.getInstance().deleteAllMessages(channels[i].getChannelId());
            long localChannelId = ChannelController.getInstance().getLocalChannelId(channels[i].getChannelId());

            if (isPostgres) {
                System.out.print("Vacuuming tables for channel: " + channels[i].getChannelId() + "...");
                Connection connection = null;
                Statement statement = null;
                
                try {
                    connection = getConnection();
                    connection.setAutoCommit(true);
                    statement = connection.createStatement();
                    statement.execute("VACUUM ANALYZE d_m" + localChannelId);
                    statement.execute("VACUUM ANALYZE d_mm" + localChannelId);
                    statement.execute("VACUUM ANALYZE d_mc" + localChannelId);
                    statement.execute("VACUUM ANALYZE d_mcm" + localChannelId);
                    statement.execute("VACUUM ANALYZE d_ms" + localChannelId);
                    statement.execute("VACUUM ANALYZE d_ma" + localChannelId);
                } finally {
                    close(statement);
                    
                    if (connection != null && !connection.isClosed()) {
                        connection.close();
                    }
                }
                
                System.out.println("done");
            }

            sourceConnectors[i] = (TestSourceConnector) channels[i].getSourceConnector();
            sentMessageIds.add(new ArrayList<Long>());
        }

        for (Channel channel : channels) {
            channel.deploy();
            channel.start();
        }

        List<Long> times = new ArrayList<Long>();
        long testStartTime = System.currentTimeMillis();
        long duration = 0;

        if (testMillis != null) {
            if (warmupMillis == null) {
                warmupMillis = 0;
            }

            testMillis += warmupMillis;
            long testBeginTime = testStartTime + warmupMillis;
            long testEndTime = testStartTime + testMillis;

            while (System.currentTimeMillis() < testEndTime) {
                for (int j = 0; j < channels.length; j++) {
                    logger.debug("Sending message");
                    long msgStartTime = System.currentTimeMillis();
                    sentMessageIds.get(j).add(sourceConnectors[j].readTestMessage(testMessage).getMessageId());
                    long totalTime = System.currentTimeMillis() - msgStartTime;

                    if (System.currentTimeMillis() > testBeginTime) {
                        times.add(totalTime);
                    }
                }
            }

            for (Channel channel : channels) {
                channel.stop();
            }
            
            for (Channel channel : channels) {
                channel.undeploy();
            }

            duration = System.currentTimeMillis() - testBeginTime;
        } else {
            for (int i = 0; i < testSize; i++) {
                for (int j = 0; j < channels.length; j++) {
                    logger.debug("Sending message");
                    long msgStartTime = System.currentTimeMillis();
                    sentMessageIds.get(j).add(sourceConnectors[j].readTestMessage(testMessage).getMessageId());
                    long totalTime = System.currentTimeMillis() - msgStartTime;
                    times.add(totalTime);
                }
            }

            for (Channel channel : channels) {
                channel.processSourceQueue(0);
                channel.stop();
            }
            
            for (Channel channel : channels) {
                channel.undeploy();
            }

            duration = System.currentTimeMillis() - testStartTime;
        }

        if (testName != null) {
            System.out.println(testName);
        }

        System.out.println(TestUtils.getPerformanceText(channels[0].getDestinationCount(), duration, times));

        long size = 0;

        for (Channel channel : channels) {
            size += TestUtils.getChannelStorageSize(channel.getChannelId());
        }

        System.out.println("Total Storage Used: " + Precision.round((((float) size) / 1048576f), 2) + " MB");

        for (int i = 0; i < channels.length; i++) {
            List<Long> channelSentMessageIds = sentMessageIds.get(i);
            assertTrue(channelSentMessageIds.size() > 0);

            for (DestinationChain chain : channels[i].getDestinationChains()) {
                for (DestinationConnector destinationConnector : chain.getDestinationConnectors().values()) {
                    List<Long> receivedMessageIds = ((TestDestinationConnector) destinationConnector).getMessageIds();

                    // test that the number of messages processed by this destination connector
                    // is equal to the number of messages sent to the channel
                    assertEquals(channelSentMessageIds.size(), receivedMessageIds.size());

                    for (int j = 0; j < channelSentMessageIds.size(); j++) {
                        assertTrue(channelSentMessageIds.get(j) > 0);

                        // test that the messages were processed by the destination in the correct order
                        assertEquals(channelSentMessageIds.get(j), receivedMessageIds.get(j));
                    }
                }
            }
        }
    }

    public static void showContent(String testMessage, String channelId, String serverId, StorageSettings storageSettings) throws Exception {
        Channel channel = createDefaultChannel(channelId, serverId, true, 1, 1, storageSettings);
        channel.deploy();
        channel.start();

        DispatchResult dispatchResult = null;

        try {
            dispatchResult = channel.getSourceConnector().dispatchRawMessage(new RawMessage(testMessage));
            channel.getSourceConnector().finishDispatch(dispatchResult);
        } finally {
            channel.stop();
            channel.undeploy();
        }

        long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement("SELECT * FROM d_mc" + localChannelId + " WHERE message_id = ?");
            statement.setLong(1, dispatchResult.getMessageId());
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String content = resultSet.getString("content");
                ContentType contentType = ContentType.fromChar(resultSet.getString("content_type").charAt(0));
                System.out.printf("%-20s%-5d%s\n", contentType.name(), resultSet.getInt("metadata_id"), content.substring(0, Math.min(10, content.length())));
            }
        } finally {
            close(connection);
            close(statement);
            close(resultSet);
        }
        
        System.out.println();
    }
    
    public static long getChannelStorageSize(String channelId) throws Exception {
        long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);
        return getTableSize("d_m" + localChannelId) + getTableSize("d_mm" + localChannelId) + getTableSize("d_mc" + localChannelId) + getTableSize("d_mcm" + localChannelId) + getTableSize("d_ma" + localChannelId) + getTableSize("d_ms" + localChannelId);
    }

    private static long getTableSize(String tableName) throws Exception {
        if (!getDatabaseType().equals("postgres")) {
            return 0;
        }
        
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT pg_total_relation_size('" + tableName + "')");

            if (!resultSet.next()) {
                throw new Exception("Failed to determine size of table: " + tableName);
            }

            return resultSet.getLong(1);
        } finally {
            close(resultSet);
            close(statement);
            close(connection);
        }
    }
    
    public static Message createTestProcessedMessage(String channelId, String serverId, long messageId, String content) {
        Calendar dateCreated = Calendar.getInstance();

        Message message = new Message();
        message.setMessageId(messageId);
        message.setChannelId(channelId);
        message.setServerId(serverId);
        message.setDateCreated(dateCreated);
        message.setProcessed(true);

        ConnectorMessage sourceMessage = new ConnectorMessage(channelId, message.getMessageId(), 0, serverId, message.getDateCreated(), Status.TRANSFORMED);
        message.getConnectorMessages().put(0, sourceMessage);

        ConnectorMessage destinationMessage = new ConnectorMessage(channelId, message.getMessageId(), 1, serverId, message.getDateCreated(), Status.SENT);
        message.getConnectorMessages().put(1, destinationMessage);

        sourceMessage.setRaw(new MessageContent(channelId, message.getMessageId(), 0, ContentType.RAW, content, null, null));
        destinationMessage.setRaw(new MessageContent(channelId, message.getMessageId(), 1, ContentType.RAW, content, null, null));
        
        return message;
    }
    
    public static void insertCompleteMessage(Message message, DonkeyDao dao) {
        dao.insertMessage(message);

        if (message.isProcessed()) {
            dao.markAsProcessed(message.getChannelId(), message.getMessageId());
        }

        for (ConnectorMessage connectorMessage : message.getConnectorMessages().values()) {
            dao.insertConnectorMessage(connectorMessage, true);

            for (ContentType contentType : ContentType.values()) {
                MessageContent messageContent = connectorMessage.getContent(contentType);

                if (messageContent != null) {
                    dao.insertMessageContent(messageContent);
                }
            }
        }
    }

    public enum MessageStorageMode {
        DEVELOPMENT(5), PRODUCTION(4), RAW(3), METADATA(2), DISABLED(1);

        private int value;

        private MessageStorageMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static MessageStorageMode fromInt(int value) {
            switch (value) {
                case 1:
                    return DISABLED;
                case 2:
                    return METADATA;
                case 3:
                    return RAW;
                case 4:
                    return PRODUCTION;
                case 5:
                    return DEVELOPMENT;
            }

            return null;
        }
    }

    public static StorageSettings getStorageSettings(MessageStorageMode messageStorageMode) {
        StorageSettings storageSettings = new StorageSettings();

        // we assume that all storage settings are enabled by default
        switch (messageStorageMode) {
            case PRODUCTION:
                storageSettings.setStoreProcessedRaw(false);
                storageSettings.setStoreTransformed(false);
                storageSettings.setStoreProcessedResponse(false);
                break;

            case RAW:
                storageSettings.setMessageRecoveryEnabled(false);
                storageSettings.setDurable(false);
                storageSettings.setStoreCustomMetaData(false);
                storageSettings.setStoreMaps(false);
                storageSettings.setStoreProcessedRaw(false);
                storageSettings.setStoreTransformed(false);
                storageSettings.setStoreSourceEncoded(false);
                storageSettings.setStoreDestinationEncoded(false);
                storageSettings.setStoreSent(false);
                storageSettings.setStoreProcessedResponse(false);
                storageSettings.setStoreResponse(false);
                storageSettings.setStoreSentResponse(false);
                break;

            case METADATA:
                storageSettings.setMessageRecoveryEnabled(false);
                storageSettings.setDurable(false);
                storageSettings.setRawDurable(false);
                storageSettings.setStoreAttachments(false);
                storageSettings.setStoreCustomMetaData(false);
                storageSettings.setStoreMaps(false);
                storageSettings.setStoreRaw(false);
                storageSettings.setStoreProcessedRaw(false);
                storageSettings.setStoreTransformed(false);
                storageSettings.setStoreSourceEncoded(false);
                storageSettings.setStoreDestinationEncoded(false);
                storageSettings.setStoreSent(false);
                storageSettings.setStoreProcessedResponse(false);
                storageSettings.setStoreResponse(false);
                storageSettings.setStoreSentResponse(false);
                break;

            case DISABLED:
                storageSettings.setEnabled(false);
                storageSettings.setMessageRecoveryEnabled(false);
                storageSettings.setDurable(false);
                storageSettings.setRawDurable(false);
                storageSettings.setStoreAttachments(false);
                storageSettings.setStoreCustomMetaData(false);
                storageSettings.setStoreMaps(false);
                storageSettings.setStoreRaw(false);
                storageSettings.setStoreProcessedRaw(false);
                storageSettings.setStoreTransformed(false);
                storageSettings.setStoreSourceEncoded(false);
                storageSettings.setStoreDestinationEncoded(false);
                storageSettings.setStoreSent(false);
                storageSettings.setStoreProcessedResponse(false);
                storageSettings.setStoreResponse(false);
                storageSettings.setStoreSentResponse(false);
                break;
        }

        return storageSettings;
    }
}
