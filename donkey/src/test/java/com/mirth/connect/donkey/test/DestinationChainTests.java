/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.test;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.channel.DestinationChain;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.FilterTransformerExecutor;
import com.mirth.connect.donkey.server.channel.FilterTransformerResult;
import com.mirth.connect.donkey.server.channel.MetaDataReplacer;
import com.mirth.connect.donkey.server.channel.components.FilterTransformerException;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.test.util.TestChannel;
import com.mirth.connect.donkey.test.util.TestConnectorProperties;
import com.mirth.connect.donkey.test.util.TestDataType;
import com.mirth.connect.donkey.test.util.TestDestinationConnector;
import com.mirth.connect.donkey.test.util.TestFilterTransformer;
import com.mirth.connect.donkey.test.util.TestPostProcessor;
import com.mirth.connect.donkey.test.util.TestPreProcessor;
import com.mirth.connect.donkey.test.util.TestResponseTransformer;
import com.mirth.connect.donkey.test.util.TestSourceConnector;
import com.mirth.connect.donkey.test.util.TestUtils;

public class DestinationChainTests {
    private static int TEST_SIZE = 10;
    private static String channelId = TestUtils.DEFAULT_CHANNEL_ID;
    private static String serverId = TestUtils.DEFAULT_SERVER_ID;
    private static String testMessage = TestUtils.TEST_HL7_MESSAGE;
    private Logger logger = Logger.getLogger(this.getClass());

    @BeforeClass
    final public static void beforeClass() throws StartException {
        Donkey.getInstance().startEngine(TestUtils.getDonkeyTestConfiguration());
    }

    @AfterClass
    final public static void afterClass() throws StartException {
        Donkey.getInstance().stopEngine();
    }

    /*
     * Create channel with two destination chains, two destination connectors
     * each
     * Set each destination connector's FilterTransformer to place values in the
     * connector, channel, and response maps
     * 
     * Send messages, and for each destination connector, assert that:
     * - The transformed data was stored
     * - The encoded data was stored
     * - The connector message maps were all updated correctly
     * - The connector message status was updated
     */
    @Test
    public final void testStoreData() throws Exception {
        int numChains = 2;
        int numDestinationsPerChain = 2;
        long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);

        TestChannel channel = new TestChannel();

        channel.setChannelId(channelId);
        channel.setServerId(serverId);
        channel.setEnabled(true);

        channel.setPreProcessor(new TestPreProcessor());
        channel.setPostProcessor(new TestPostProcessor());

        TestSourceConnector sourceConnector = (TestSourceConnector) TestUtils.createDefaultSourceConnector();
        sourceConnector.setRespondAfterProcessing(true);
        sourceConnector.setChannelId(channel.getChannelId());
        sourceConnector.setChannel(channel);
        sourceConnector.setMetaDataReplacer(new MetaDataReplacer());

        channel.setSourceConnector(sourceConnector);
        channel.setSourceFilterTransformer(TestUtils.createDefaultFilterTransformerExecutor());

        class TestFilterTransformer2 extends TestFilterTransformer {
            @Override
            public FilterTransformerResult doFilterTransform(ConnectorMessage message) throws FilterTransformerException {
                // Alter the connector message maps
                message.getConnectorMap().put("key", "value");
                message.getChannelMap().put("key", "value");
                message.getResponseMap().put("key", new Response(Status.SENT, "value"));
                return super.doFilterTransform(message);
            }
        }

        for (int i = 1; i <= numChains; i++) {
            DestinationChain chain = new DestinationChain();
            chain.setMetaDataReplacer(new MetaDataReplacer());
            chain.setChannelId(channel.getChannelId());
            chain.setMetaDataReplacer(sourceConnector.getMetaDataReplacer());
            chain.setMetaDataColumns(channel.getMetaDataColumns());

            for (int j = 1; j <= numDestinationsPerChain; j++) {
                int metaDataId = (i - 1) * numDestinationsPerChain + j;
                TestDestinationConnector destinationConnector = (TestDestinationConnector) TestUtils.createDestinationConnector(channel.getChannelId(), channel.getServerId(), new TestConnectorProperties(), TestUtils.DEFAULT_DESTINATION_NAME, new TestDataType(), new TestDataType(), new TestResponseTransformer(), metaDataId);
                destinationConnector.setChannelId(channel.getChannelId());

                FilterTransformerExecutor filterTransformerExecutor = new FilterTransformerExecutor(new TestDataType(), new TestDataType());
                filterTransformerExecutor.setFilterTransformer(new TestFilterTransformer2());

                chain.addDestination(metaDataId, filterTransformerExecutor, destinationConnector);
            }

            channel.addDestinationChain(chain);
        }

        channel.deploy();
        channel.start(null);

        if (ChannelController.getInstance().channelExists(channelId)) {
            ChannelController.getInstance().deleteAllMessages(channelId);
        }
        
        for (int i = 1; i <= TEST_SIZE; i++) {
            DispatchResult messageResponse = ((TestSourceConnector) channel.getSourceConnector()).readTestMessage(testMessage);

            for (DestinationChain chain : channel.getDestinationChains()) {
                for (int metaDataId : chain.getDestinationConnectors().keySet()) {
                    Connection connection = null;
                    PreparedStatement statement = null;
                    ResultSet result = null;
                    
                    try {
                        connection = TestUtils.getConnection();
                        
                        // Assert that the transformed data was stored
                        statement = connection.prepareStatement("SELECT * FROM d_mc" + localChannelId + " WHERE message_id = ? AND metadata_id = ? AND content_type = ?");
                        statement.setLong(1, messageResponse.getMessageId());
                        statement.setInt(2, metaDataId);
                        statement.setInt(3, ContentType.TRANSFORMED.getContentTypeCode());
                        result = statement.executeQuery();
                        assertTrue(result.next());
                        TestUtils.close(result);
    
                        // Assert that the encoded data was stored
                        statement.setInt(3, ContentType.ENCODED.getContentTypeCode());
                        result = statement.executeQuery();
                        assertTrue(result.next());
                    } finally {
                        TestUtils.close(result);
                        TestUtils.close(statement);
                        TestUtils.close(connection);
                    }

                    // Assert that the connector message maps were updated
                    Map<String, Object> connectorMap = TestUtils.getConnectorMap(channel.getChannelId(), messageResponse.getMessageId(), metaDataId);
                    Map<String, Object> channelMap = TestUtils.getChannelMap(channel.getChannelId(), messageResponse.getMessageId(), metaDataId);
                    Map<String, Object> responseMap = TestUtils.getResponseMap(channel.getChannelId(), messageResponse.getMessageId(), metaDataId);
                    assertTrue(connectorMap.get("key").equals("value"));
                    assertTrue(channelMap.get("key").equals("value"));
                    assertTrue(responseMap.get("key").equals(new Response(Status.SENT, "value")));

                    // Assert that the connector message status was updated
                    TestUtils.assertConnectorMessageStatusEquals(channel.getChannelId(), messageResponse.getMessageId(), metaDataId, Status.SENT);
                }
            }
        }

        channel.stop();
        channel.undeploy();
        ChannelController.getInstance().removeChannel(channel.getChannelId());
    }

    /*
     * Create channel with two destination chains, two destination connectors
     * each
     * Set the source FilterTransformer to place values in the channel and
     * response maps
     * 
     * Send messages, and for each destination connector, assert that:
     * - The connector message was stored
     * - The channel and response maps were updated correctly
     * - If the destination connector isn't the first one in the chain, the raw
     * data was stored
     * - If the destination connector is the first one in the chain, the source
     * encoded data was stored
     */
    @Test
    public final void testCreateNextMessage() throws Exception {
        long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId, true, 2, 2);

        channel.getSourceFilterTransformer().setFilterTransformer(new TestFilterTransformer() {
            @Override
            public FilterTransformerResult doFilterTransform(ConnectorMessage message) throws FilterTransformerException {
                // Alter the channel and response maps
                message.getChannelMap().put("key", "value");
                message.getResponseMap().put("key", new Response(Status.SENT, "value"));
                return new FilterTransformerResult(false, null);
            }
        });

        channel.deploy();
        channel.start(null);
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            connection = TestUtils.getConnection();
            
            for (int i = 1; i <= TEST_SIZE; i++) {
                DispatchResult messageResponse = ((TestSourceConnector) channel.getSourceConnector()).readTestMessage(testMessage);

                for (DestinationChain chain : channel.getDestinationChains()) {
                    for (int metaDataId : chain.getEnabledMetaDataIds()) {
                        // Assert that the connector message was stored
                        statement = connection.prepareStatement("SELECT * FROM d_mm" + localChannelId + " WHERE message_id = ? AND id = ?");
                        statement.setLong(1, messageResponse.getMessageId());
                        statement.setInt(2, metaDataId);
                        result = statement.executeQuery();
                        assertTrue(result.next());
                        result.close();
                        statement.close();

                        // Assert that the channel and response maps were updated
                        Map<String, Object> channelMap = TestUtils.getChannelMap(channel.getChannelId(), messageResponse.getMessageId(), metaDataId);
                        Map<String, Object> responseMap = TestUtils.getResponseMap(channel.getChannelId(), messageResponse.getMessageId(), metaDataId);
                        assertTrue(channelMap.get("key").equals("value"));
                        assertTrue(responseMap.get("key").equals(new Response(Status.SENT, "value")));

                        // Assert that the raw data was stored
                        statement = connection.prepareStatement("SELECT * FROM d_mc" + localChannelId + " WHERE message_id = ? AND metadata_id = 0 AND content_type = ?");
                        statement.setLong(1, messageResponse.getMessageId());
                        statement.setInt(2, ContentType.ENCODED.getContentTypeCode());
                        result = statement.executeQuery();
                        assertTrue(result.next());
                        result.close();
                        statement.close();
                    }
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
}
