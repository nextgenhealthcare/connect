/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers.tests;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import com.mirth.connect.connectors.tests.TestAutoResponder;
import com.mirth.connect.connectors.tests.TestDestinationConnector;
import com.mirth.connect.connectors.tests.TestResponseTransformer;
import com.mirth.connect.connectors.tests.TestSerializer;
import com.mirth.connect.connectors.tests.TestSourceConnector;
import com.mirth.connect.connectors.vm.VmDispatcherProperties;
import com.mirth.connect.connectors.vm.VmReceiverProperties;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.channel.ChannelLock;
import com.mirth.connect.donkey.server.channel.DestinationChain;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.FilterTransformerExecutor;
import com.mirth.connect.donkey.server.channel.MetaDataReplacer;
import com.mirth.connect.donkey.server.channel.ResponseSelector;
import com.mirth.connect.donkey.server.channel.ResponseTransformerExecutor;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.buffered.BufferedDaoFactory;
import com.mirth.connect.donkey.server.message.DataType;
import com.mirth.connect.donkey.server.queue.ConnectorMessageQueue;
import com.mirth.connect.donkey.server.queue.ConnectorMessageQueueDataSource;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.Connector.Mode;
import com.mirth.connect.model.Filter;
import com.mirth.connect.model.Rule;
import com.mirth.connect.model.ServerEventContext;
import com.mirth.connect.model.Transformer;
import com.mirth.connect.plugins.datatypes.hl7v2.HL7v2DataTypeProperties;
import com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseValidationProperties;
import com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseValidator;
import com.mirth.connect.plugins.datatypes.hl7v2.HL7v2SerializationProperties;
import com.mirth.connect.server.Mirth;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.util.ResourceUtil;

public class TestUtils {
    final public static String TEST_HL7_MESSAGE = "MSH|^~\\&|LABNET|Acme Labs|||20090601105700||ORU^R01|HMCDOOGAL-0088|D|2.2\nPID|1|8890088|8890088^^^72777||McDoogal^Hattie^||19350118|F||2106-3|100 Beach Drive^Apt. 5^Mission Viejo^CA^92691^US^H||(949) 555-0025|||||8890088^^^72|604422825\nPV1|1|R|C3E^C315^B||||2^HIBBARD^JULIUS^|5^ZIMMERMAN^JOE^|9^ZOIDBERG^JOHN^|CAR||||4|||2301^OBRIEN, KEVIN C|I|1783332658^1^1||||||||||||||||||||DISNEY CLINIC||N|||20090514205600\nORC|RE|928272608|056696716^LA||CM||||20090601105600||||  C3E|||^RESULT PERFORMED\nOBR|1|928272608|056696716^LA|1001520^K|||20090601101300|||MLH25|||HEMOLYZED/VP REDRAW|20090601102400||2301^OBRIEN, KEVIN C||||01123085310001100100152023509915823509915800000000101|0000915200932|20090601105600||LAB|F||^^^20090601084100^^ST~^^^^^ST\nOBX|1|NM|1001520^K||5.3|MMOL/L|3.5-5.5||||F|||20090601105600|IIM|IIM";
    final public static String CHANNEL_ID = "newtestchannel";
    final public static String SERVER_ID = "testserver";

    private static Logger logger = Logger.getLogger(TestUtils.class);

    public static void startMirthServer() throws InterruptedException {
        startMirthServer(1000);
    }

    public static void startMirthServer(int sleepMillis) throws InterruptedException {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                new Mirth().run();
            }
        });

        while (ConfigurationController.getInstance().getStatus() != ConfigurationController.STATUS_OK) {
            Thread.sleep(sleepMillis);
        }
    }

    public static void runChannelTest(Channel channel, final String testMessage, final int testSize) throws Exception {
        channel.deploy();
        channel.start();

        SourceConnector sourceConnector = channel.getSourceConnector();
        RawMessage rawMessage = new RawMessage(testMessage);

        for (int i = 0; i < testSize; i++) {
            DispatchResult dispatchResult = null;

            try {
                dispatchResult = sourceConnector.dispatchRawMessage(rawMessage);

                if (dispatchResult.getChannelException() != null) {
                    throw dispatchResult.getChannelException();
                }
            } finally {
                sourceConnector.finishDispatch(dispatchResult);
            }
        }

        channel.stop();
        channel.undeploy();
    }

    public static com.mirth.connect.model.Channel createTestChannelModel(String channelId, String channelName) {
        Transformer transformer = new Transformer();
        transformer.setInboundDataType("HL7V2");
        transformer.setInboundProperties(new HL7v2DataTypeProperties());
        transformer.setOutboundDataType("HL7V2");
        transformer.setOutboundProperties(new HL7v2DataTypeProperties());

        Filter filter = new Filter();
        filter.setRules(new ArrayList<Rule>());

        Connector sourceConnector = new Connector();
        sourceConnector.setEnabled(true);
        sourceConnector.setMetaDataId(0);
        sourceConnector.setMode(Mode.SOURCE);
        sourceConnector.setName("source");
        sourceConnector.setProperties(new VmReceiverProperties());
        sourceConnector.setTransportName(sourceConnector.getProperties().getName());
        sourceConnector.setWaitForPrevious(true);
        sourceConnector.setTransformer(transformer);
        sourceConnector.setFilter(filter);

        transformer = new Transformer();
        transformer.setInboundDataType("HL7V2");
        transformer.setInboundProperties(new HL7v2DataTypeProperties());
        transformer.setOutboundDataType("HL7V2");
        transformer.setOutboundProperties(new HL7v2DataTypeProperties());

        filter = new Filter();
        filter.setRules(new ArrayList<Rule>());

        Connector destinationConnector = new Connector();
        destinationConnector.setEnabled(true);
        destinationConnector.setMetaDataId(1);
        destinationConnector.setMode(Mode.DESTINATION);
        destinationConnector.setName("destination");
        destinationConnector.setProperties(new VmDispatcherProperties());
        destinationConnector.setTransportName(destinationConnector.getProperties().getName());
        destinationConnector.setTransformer(transformer);
        destinationConnector.setFilter(filter);

        transformer = new Transformer();
        transformer.setInboundDataType("HL7V2");
        transformer.setInboundProperties(new HL7v2DataTypeProperties());
        transformer.setOutboundDataType("HL7V2");
        transformer.setOutboundProperties(new HL7v2DataTypeProperties());

        destinationConnector.setResponseTransformer(transformer);

        com.mirth.connect.model.Channel channel = new com.mirth.connect.model.Channel();
        channel.setId(channelId);
        channel.setEnabled(true);
        channel.setLastModified(Calendar.getInstance());
        channel.setName(channelName);
        channel.setRevision(0);
        channel.setSourceConnector(sourceConnector);
        channel.addDestination(destinationConnector);

        return channel;
    }

    public static Channel createChannel(String channelId, String serverId, SourceConnector sourceConnector, DestinationConnector destinationConnector) {
        Channel channel = new Channel();
        channel.lock(ChannelLock.DEBUG);
        channel.setChannelId(channelId);
        channel.setServerId(serverId);
        channel.setEnabled(true);
        channel.setSourceConnector(sourceConnector);
        channel.setPreProcessor(new TestPreProcessor());
        channel.setPostProcessor(new TestPostProcessor());

        FilterTransformerExecutor filterTransformer = new FilterTransformerExecutor(new DataType("XML", new TestSerializer(), new TestAutoResponder()), new DataType("XML", new TestSerializer(), new TestAutoResponder()));
        filterTransformer.setFilterTransformer(new TestFilterTransformer());
        channel.setSourceFilterTransformer(filterTransformer);

        sourceConnector.setChannelId(channel.getChannelId());
        sourceConnector.setChannel(channel);

        DestinationChain chain = new DestinationChain();
        filterTransformer = new FilterTransformerExecutor(new DataType("XML", new TestSerializer(), new TestAutoResponder()), new DataType("XML", new TestSerializer(), new TestAutoResponder()));
        filterTransformer.setFilterTransformer(new TestFilterTransformer());
        chain.addDestination(1, filterTransformer, destinationConnector);
        channel.getDestinationChains().add(chain);
        destinationConnector.setChannelId(channelId);

        ResponseTransformerExecutor responseTransformerExecutor = new ResponseTransformerExecutor(new DataType("XML", new TestSerializer(), new TestAutoResponder()), new DataType("XML", new TestSerializer(), new TestAutoResponder()));
        responseTransformerExecutor.setResponseTransformer(new TestResponseTransformer());
        destinationConnector.setResponseTransformerExecutor(responseTransformerExecutor);

        ConnectorMessageQueue queue = new ConnectorMessageQueue();
        queue.setDataSource(new ConnectorMessageQueueDataSource(channelId, serverId, 1, Status.QUEUED, false, Donkey.getInstance().getDaoFactory()));
        destinationConnector.setQueue(queue);

        return channel;
    }

    public static Channel createChannel(String channelId, String serverId, boolean respondAfterProcessing, int numChains, int destinationsPerChain) {
        // create channel
        Channel channel = new Channel();
        channel.lock(ChannelLock.DEBUG);
        channel.setChannelId(channelId);
        channel.setServerId(serverId);
        channel.setEnabled(true);
        channel.setDaoFactory(new BufferedDaoFactory(Donkey.getInstance().getDaoFactory()));
        channel.setPreProcessor(new TestPreProcessor());
        channel.setPostProcessor(new TestPostProcessor());

        DataType dataType = new DataType("HL7V2", new TestSerializer(), new TestAutoResponder());

        FilterTransformerExecutor filterTransformer = new FilterTransformerExecutor(dataType, dataType);
        filterTransformer.setFilterTransformer(new TestFilterTransformer());
        channel.setSourceFilterTransformer(filterTransformer);

        // create source connector
        TestSourceConnector sourceConnector = new TestSourceConnector();
        sourceConnector.setChannelId(channel.getChannelId());
        sourceConnector.setChannel(channel);
        sourceConnector.setRespondAfterProcessing(respondAfterProcessing);
        sourceConnector.setMetaDataReplacer(new MetaDataReplacer());
        sourceConnector.setInboundDataType(dataType);

        channel.setSourceConnector(sourceConnector);
        channel.setResponseSelector(new ResponseSelector(sourceConnector.getInboundDataType()));
        channel.getResponseSelector().setRespondFromName("destination1");

        int metaDataId = 1;

        // create destination chains
        for (int i = 0; i < numChains; i++) {
            DestinationChain chain = new DestinationChain();
            chain.setChannelId(channelId);
            chain.setMetaDataReplacer(sourceConnector.getMetaDataReplacer());

            channel.getDestinationChains().add(chain);

            for (int j = 0; j < destinationsPerChain; j++) {
                ConnectorMessageQueue queue = new ConnectorMessageQueue();
                queue.setDataSource(new ConnectorMessageQueueDataSource(channelId, serverId, metaDataId, Status.QUEUED, false, Donkey.getInstance().getDaoFactory()));

                TestDestinationConnector testDestinationConnector = new TestDestinationConnector();
                testDestinationConnector.setChannelId(channelId);
                testDestinationConnector.setDestinationName("destination" + metaDataId);
                testDestinationConnector.setEnabled(true);
                testDestinationConnector.setQueue(queue);
                testDestinationConnector.setResponseValidator(new HL7v2ResponseValidator(new HL7v2SerializationProperties(), new HL7v2ResponseValidationProperties()));

                ResponseTransformerExecutor responseTransformerExecutor = new ResponseTransformerExecutor(dataType, dataType);
                responseTransformerExecutor.setResponseTransformer(new TestResponseTransformer());
                testDestinationConnector.setResponseTransformerExecutor(responseTransformerExecutor);

                filterTransformer = new FilterTransformerExecutor(dataType, dataType);
                filterTransformer.setFilterTransformer(new TestFilterTransformer());
                chain.addDestination(metaDataId++, filterTransformer, testDestinationConnector);
            }
        }

        return channel;
    }

    public static void deployTestChannel(com.mirth.connect.model.Channel channel) throws Exception {
        com.mirth.connect.server.controllers.ChannelController.getInstance().updateChannel(channel, ServerEventContext.SYSTEM_USER_EVENT_CONTEXT, true);
        Set<String> channelIds = new LinkedHashSet<String>();
        channelIds.add(channel.getId());
        ControllerFactory.getFactory().createEngineController().deployChannels(channelIds, ServerEventContext.SYSTEM_USER_EVENT_CONTEXT);
    }

    public static Properties getSqlProperties() {
        PropertiesConfiguration mirthProperties = new PropertiesConfiguration();

        try {
            InputStream is = ResourceUtil.getResourceStream(SqlSession.class, "mirth.properties");
            mirthProperties.setDelimiterParsingDisabled(true);
            mirthProperties.load(is);
            IOUtils.closeQuietly(is);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        Properties donkeyProperties = new Properties();
        donkeyProperties.setProperty("database.driver", mirthProperties.getString("database.driver"));
        donkeyProperties.setProperty("database.url", mirthProperties.getString("database.url"));
        donkeyProperties.setProperty("database.username", mirthProperties.getString("database.username"));

        if (mirthProperties.containsKey("database.password")) {
            donkeyProperties.setProperty("database.password", mirthProperties.getString("database.password"));
        }

        return donkeyProperties;
    }

    public static Connection getConnection() throws Exception {
        Properties configuration = Donkey.getInstance().getConfiguration().getDatabaseProperties();
        String driver = configuration.getProperty("database.driver");

        if (driver != null) {
            Class.forName(driver);
        }

        Connection connection = DriverManager.getConnection(configuration.getProperty("database.url"), configuration.getProperty("database.username"), configuration.getProperty("database.password"));
        connection.setAutoCommit(false);
        return connection;
    }

    public static boolean channelExists(String channelId) throws Exception {
        boolean exists = false;
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM channels WHERE channel_id = ?");
        statement.setString(1, channelId);
        ResultSet result = statement.executeQuery();

        if (result.next()) {
            exists = true;
        }

        result.close();
        connection.close();
        return exists;
    }

    public static String getPerformanceText(String testName, int testSize, long milliseconds) {
        double seconds = ((double) milliseconds) / 1000.0;
        return StringUtils.rightPad(testName, 50) + ((int) (testSize / seconds)) + " messages/second";
    }

    public static Message createMessage(RawMessage rawMessage, String channelId, String serverId, long messageId) {
        Message message = new Message();
        message.setMessageId(messageId);
        message.setChannelId(channelId);
        message.setServerId(serverId);
        message.setReceivedDate(Calendar.getInstance());

        ConnectorMessage sourceMessage = new ConnectorMessage(channelId, message.getMessageId(), 0, serverId, message.getReceivedDate(), Status.RECEIVED);
        sourceMessage.setRaw(new MessageContent(channelId, message.getMessageId(), 0, ContentType.RAW, rawMessage.getRawData(), null, false));

        if (rawMessage.getSourceMap() != null) {
            sourceMessage.setSourceMap(rawMessage.getSourceMap());
        }

        message.getConnectorMessages().put(0, sourceMessage);

        return message;
    }

    public static Message createAndStoreNewMessage(RawMessage rawMessage, String channelId, String serverId, DonkeyDao dao) {
        Message message = createMessage(rawMessage, channelId, serverId, dao.getNextMessageId(channelId));
        ConnectorMessage sourceMessage = message.getConnectorMessages().get(0);

        dao.insertMessage(message);
        dao.insertConnectorMessage(sourceMessage, true, true);
        dao.insertMessageContent(sourceMessage.getRaw());
        return message;
    }

    @SuppressWarnings("unchecked")
    public static List<Long> getMessageIds(String channelId) throws Exception {
        return (List<Long>) selectColumn("SELECT id FROM d_m" + ChannelController.getInstance().getLocalChannelId(channelId));
    }

    public static int getNumMessages(String channelId) throws Exception {
        return getNumMessages(channelId, false);
    }

    public static int getNumMessages(String channelId, boolean onlyCountMessagesWithContent) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);

            StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM d_m" + localChannelId + " m");

            if (onlyCountMessagesWithContent) {
                query.append(" WHERE EXISTS (SELECT 1 FROM d_mc" + localChannelId + " WHERE message_id = m.id)");
            }

            connection = getConnection();
            statement = connection.prepareStatement(query.toString());
            result = statement.executeQuery();
            result.next();
            return result.getInt(1);
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

    public static void deleteAllMessages(String channelId) throws Exception {
        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();

        try {
            dao.deleteAllMessages(channelId);
            dao.commit();
        } finally {
            dao.close();
        }

        fixMessageIdSequence(channelId);
    }

    public static void createTestMessages(String channelId, Message templateMessage, int count) throws Exception {
        DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();

        try {
            for (int i = 0; i < count; i++) {
                insertCompleteMessage(templateMessage, dao);
            }

            dao.commit();
        } finally {
            dao.close();
        }
    }

    public static void createTestMessagesFast(String channelId, int power) throws Exception {
        createTestMessagesFast(channelId, TestUtils.createMessage(new RawMessage(TestUtils.TEST_HL7_MESSAGE), channelId, "testserver", 1), power);
    }

    public static void createTestMessagesFast(String channelId, Message templateMessage, int power) throws Exception {
        long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);
        deleteAllMessages(channelId);
        createTestMessages(channelId, templateMessage, 1);

        Connection connection = null;
        PreparedStatement messageStatement = null;
        PreparedStatement metaDataStatement = null;
        PreparedStatement contentStatement = null;
        long idOffset = templateMessage.getMessageId();

        logger.debug("Replicating messages in channel \"" + channelId + "\"");

        try {
            connection = getConnection();
            messageStatement = connection.prepareStatement("INSERT INTO d_m" + localChannelId + " (id, server_id, received_date, processed) SELECT id + ?, server_id, received_date, processed FROM d_m" + localChannelId);
            metaDataStatement = connection.prepareStatement("INSERT INTO d_mm" + localChannelId + " (id, server_id, message_id, chain_id, received_date, status, order_id) SELECT id, server_id, message_id + ?, chain_id, received_date, status, order_id FROM d_mm" + localChannelId);
            contentStatement = connection.prepareStatement("INSERT INTO d_mc" + localChannelId + " (metadata_id, message_id, content_type, content, is_encrypted, data_type) SELECT metadata_id, message_id + ?, content_type, content, is_encrypted, data_type FROM d_mc" + localChannelId);

            for (int i = 0; i < power; i++) {
                messageStatement.setLong(1, idOffset);
                metaDataStatement.setLong(1, idOffset);
                contentStatement.setLong(1, idOffset);

                messageStatement.executeUpdate();
                metaDataStatement.executeUpdate();
                contentStatement.executeUpdate();

                idOffset *= 2;

                connection.commit();
                logger.debug("# of messages in channel \"" + channelId + "\" is now " + getNumMessages(channelId));
            }
        } finally {
            close(messageStatement);
            close(metaDataStatement);
            close(contentStatement);
            close(connection);
        }

        fixMessageIdSequence(channelId);
        logger.debug("Finished replicating messages in channel \"" + channelId + "\"");
    }

    public static void fixMessageIdSequence(String channelId) throws Exception {
        Connection connection = null;
        long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);
        String database = (String) Donkey.getInstance().getConfiguration().getDatabaseProperties().get("database");
        Long maxId = null;

        if (database.equals("derby") || database.equals("mysql") || database.equals("sqlserver")) {
            Statement statement = null;
            ResultSet result = null;

            try {
                connection = getConnection();
                statement = connection.createStatement();
                result = statement.executeQuery("SELECT MAX(id) FROM d_m" + localChannelId);
                result.next();
                maxId = result.getLong(1) + 1;
                close(result);
                statement.execute("DELETE FROM d_message_sequences WHERE local_channel_id = " + localChannelId);
                statement.execute("INSERT INTO d_message_sequences (local_channel_id) VALUES (" + localChannelId + ")");
                connection.commit();
            } finally {
                close(result);
                close(statement);
                close(connection);
            }
        }

        logger.debug("Message ID sequence updated to: " + maxId);
    }

    private static void insertCompleteMessage(Message message, DonkeyDao dao) {
        dao.insertMessage(message);

        if (message.isProcessed()) {
            dao.markAsProcessed(message.getChannelId(), message.getMessageId());
        }

        for (ConnectorMessage connectorMessage : message.getConnectorMessages().values()) {
            dao.insertConnectorMessage(connectorMessage, true, true);

            for (ContentType contentType : ContentType.getMessageTypes()) {
                MessageContent messageContent = connectorMessage.getMessageContent(contentType);

                if (messageContent != null) {
                    dao.insertMessageContent(messageContent);
                }
            }
        }
    }

    public static double getPerSecondRate(long size, long millis, Integer precision) {
        double rate = ((double) size) / ((double) millis / 1000d);

        if (precision != null) {
            rate = Precision.round(rate, 2);
        }

        return rate;
    }

    public static List<?> selectColumn(String query) {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            List<Object> col = new ArrayList<Object>();

            while (resultSet.next()) {
                col.add(resultSet.getObject(1));
            }

            return col;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(connection);
        }
    }
}
