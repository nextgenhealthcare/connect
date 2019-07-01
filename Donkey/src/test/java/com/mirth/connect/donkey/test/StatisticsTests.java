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
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.donkey.model.DonkeyException;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.channel.DestinationChainProvider;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.channel.FilterTransformerExecutor;
import com.mirth.connect.donkey.server.channel.FilterTransformerResult;
import com.mirth.connect.donkey.server.channel.components.FilterTransformerException;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;
import com.mirth.connect.donkey.server.queue.ConnectorMessageQueueDataSource;
import com.mirth.connect.donkey.server.queue.DestinationQueue;
import com.mirth.connect.donkey.test.util.TestChannel;
import com.mirth.connect.donkey.test.util.TestDataType;
import com.mirth.connect.donkey.test.util.TestDispatcher;
import com.mirth.connect.donkey.test.util.TestDispatcherProperties;
import com.mirth.connect.donkey.test.util.TestFilterTransformer;
import com.mirth.connect.donkey.test.util.TestPostProcessor;
import com.mirth.connect.donkey.test.util.TestPreProcessor;
import com.mirth.connect.donkey.test.util.TestResponseTransformer;
import com.mirth.connect.donkey.test.util.TestSourceConnector;
import com.mirth.connect.donkey.test.util.TestUtils;

public class StatisticsTests {
    private static long TEST_SIZE = 100;
    private static String channelId = TestUtils.DEFAULT_CHANNEL_ID;
    private static String serverId = TestUtils.DEFAULT_SERVER_ID;
    private static String testMessage = TestUtils.TEST_HL7_MESSAGE;
    private static DonkeyDaoFactory daoFactory;
    private Logger logger = Logger.getLogger(this.getClass());

    @BeforeClass
    final public static void beforeClass() throws StartException {
        Donkey.getInstance().startEngine(TestUtils.getDonkeyTestConfiguration());
        daoFactory = TestUtils.getDaoFactory();
    }

    @AfterClass
    final public static void afterClass() throws StartException {
        Donkey.getInstance().stopEngine();
    }

    /*
     * Create a "normal" channel with four destination connectors where all messages process through
     * the source connector with a final status of RECEIVED, and process through each destination
     * connector with a status of SENT
     * 
     * Send messages, assert that: - The source connector stats have TEST_SIZE for RECEIVED and
     * TRANSFORMED - The destination connector stats have TEST_SIZE for RECEIVED and SENT - The
     * aggregate stats are correct (RECEIVED/TRANSFORMED is the same as the source connector,
     * PENDING/SENT/QUEUED is the same as the destination connectors combined, FILTERED/ERROR is the
     * same as all connectors combined)
     */
    @Test
    public final void testStatistics1() throws Exception {
        TestChannel channel = (TestChannel) TestUtils.createDefaultChannel(channelId, serverId, true, 2, 2);

        channel.deploy();
        channel.start(null);

        // Send messages through the channel
        for (int i = 1; i <= TEST_SIZE; i++) {
            ((TestSourceConnector) channel.getSourceConnector()).readTestMessage(testMessage);
        }

        // Assert that the messages were sent
        assertEquals(TEST_SIZE, channel.getNumMessages());

        // Assert the source connector stats are correct

        statsEqual(channel.getChannelId(), 0, TEST_SIZE, 0l, TEST_SIZE, 0l, 0l, 0l);
        // Assert that destination connector stats are correct
        statsEqual(channel.getChannelId(), 1, TEST_SIZE, 0l, 0l, 0l, TEST_SIZE, 0l);
        statsEqual(channel.getChannelId(), 2, TEST_SIZE, 0l, 0l, 0l, TEST_SIZE, 0l);
        statsEqual(channel.getChannelId(), 3, TEST_SIZE, 0l, 0l, 0l, TEST_SIZE, 0l);
        statsEqual(channel.getChannelId(), 4, TEST_SIZE, 0l, 0l, 0l, TEST_SIZE, 0l);
        // Assert the aggregate stats are correct
        assertTrue(channelStatsCorrect());

        channel.stop();
        channel.undeploy();

        ChannelController.getInstance().removeChannel(channel.getChannelId());
    }

    /*
     * Create a channel where all messages are filtered on the source connector
     * 
     * Send messages, assert that: - The source connector stats have TEST_SIZE for RECEIVED and
     * FILTERED - The destination connector stats are all zeroes - The aggregate stats are correct
     * (RECEIVED/TRANSFORMED is the same as the source connector, PENDING/SENT/QUEUED is the same as
     * the destination connectors combined, FILTERED/ERROR is the same as all connectors combined)
     */
    @Test
    public final void testStatistics2() throws Exception {
        TestChannel channel = (TestChannel) TestUtils.createDefaultChannel(channelId, serverId, true, 2, 2);

        TestFilterTransformer filterTransformer = new TestFilterTransformer() {
            @Override
            public FilterTransformerResult doFilterTransform(ConnectorMessage message) throws FilterTransformerException {
                super.doFilterTransform(message);
                return new FilterTransformerResult(true, null);
            }
        };
        channel.getSourceConnector().getFilterTransformerExecutor().setFilterTransformer(filterTransformer);

        channel.deploy();
        channel.start(null);

        // Send messages through the channel
        for (int i = 1; i <= TEST_SIZE; i++) {
            ((TestSourceConnector) channel.getSourceConnector()).readTestMessage(testMessage);
        }

        // Assert that the messages were sent
        assertEquals(TEST_SIZE, channel.getNumMessages());

        // Assert the source connector stats are correct
        statsEqual(channel.getChannelId(), 0, TEST_SIZE, TEST_SIZE, 0L, 0L, 0L, 0L);
        // Assert that destination connector stats are correct
        statsEqual(channel.getChannelId(), 1, 0L, 0L, 0L, 0L, 0L, 0L);
        statsEqual(channel.getChannelId(), 2, 0L, 0L, 0L, 0L, 0L, 0L);
        statsEqual(channel.getChannelId(), 3, 0L, 0L, 0L, 0L, 0L, 0L);
        statsEqual(channel.getChannelId(), 4, 0L, 0L, 0L, 0L, 0L, 0L);
        // Assert the aggregate stats are correct
        assertTrue(channelStatsCorrect());

        channel.stop();
        channel.undeploy();

        ChannelController.getInstance().removeChannel(channel.getChannelId());
    }

    /*
     * Create a channel where each destination results in a different status (FILTERED, SENT,
     * QUEUED, ERROR)
     * 
     * Send messages, assert that: - The source connector stats have TEST_SIZE for RECEIVED and
     * TRANSFORMED - The first destination connector stats have TEST_SIZE for FILTERED - The second
     * destination connector stats have TEST_SIZE for SENT - The third destination connector stats
     * have TEST_SIZE for QUEUED - The fourth destination connector stats have TEST_SIZE for ERROR -
     * The aggregate stats are correct (RECEIVED/TRANSFORMED is the same as the source connector,
     * PENDING/SENT/QUEUED is the same as the destination connectors combined, FILTERED/ERROR is the
     * same as all connectors combined)
     */
    @Test
    public final void testStatistics3() throws Exception {
        TestUtils.initChannel(channelId);

        TestChannel channel = new TestChannel();

        channel.setChannelId(channelId);
        channel.setServerId(serverId);

        channel.setPreProcessor(new TestPreProcessor());
        channel.setPostProcessor(new TestPostProcessor());

        TestSourceConnector sourceConnector = (TestSourceConnector) TestUtils.createDefaultSourceConnector();
        sourceConnector.setChannelId(channel.getChannelId());
        sourceConnector.setChannel(channel);
        channel.setSourceConnector(sourceConnector);
        channel.getSourceConnector().setFilterTransformerExecutor(TestUtils.createDefaultFilterTransformerExecutor());

        DestinationChainProvider chain = new DestinationChainProvider();
        chain.setChannelId(channel.getChannelId());

        for (int i = 1; i <= 4; i++) {
            DestinationConnector destinationConnector = new TestDispatcher();
            destinationConnector.setChannelId(channel.getChannelId());

            TestDispatcherProperties connectorProperties = new TestDispatcherProperties();
            if (i == 3) {
                connectorProperties.getDestinationConnectorProperties().setQueueEnabled(true);
                connectorProperties.getDestinationConnectorProperties().setSendFirst(false);
            } else {
                connectorProperties.getDestinationConnectorProperties().setQueueEnabled(false);
            }

            destinationConnector.setConnectorProperties(connectorProperties);
            destinationConnector.setDestinationName(TestUtils.DEFAULT_DESTINATION_NAME);
            destinationConnector.setInboundDataType(new TestDataType());
            destinationConnector.setOutboundDataType(new TestDataType());
            destinationConnector.setResponseTransformerExecutor(TestUtils.createDefaultResponseTransformerExecutor());

            DestinationQueue destinationConnectorQueue = new DestinationQueue(connectorProperties.getDestinationConnectorProperties().getThreadAssignmentVariable(), connectorProperties.getDestinationConnectorProperties().getThreadCount(), connectorProperties.getDestinationConnectorProperties().isRegenerateTemplate(), destinationConnector.getSerializer(), destinationConnector.getMessageMaps());
            destinationConnectorQueue.setDataSource(new ConnectorMessageQueueDataSource(channel.getChannelId(), channel.getServerId(), i, Status.QUEUED, false, daoFactory));
            destinationConnectorQueue.updateSize();
            destinationConnector.setQueue(destinationConnectorQueue);

            FilterTransformerExecutor filterTransformerExecutor = TestUtils.createDefaultFilterTransformerExecutor();

            switch (i) {
                case 1:
                    ((TestFilterTransformer) filterTransformerExecutor.getFilterTransformer()).setFiltered(true);
                    break;
                case 2:
                    ((TestDispatcher) destinationConnector).setReturnStatus(Status.SENT);
                    break;
                case 3:
                    ((TestDispatcher) destinationConnector).setReturnStatus(Status.QUEUED);
                    break;
                case 4:
                    ((TestDispatcher) destinationConnector).setReturnStatus(Status.ERROR);
                    break;
            }

            destinationConnector.setMetaDataReplacer(sourceConnector.getMetaDataReplacer());
            destinationConnector.setMetaDataColumns(channel.getMetaDataColumns());
            destinationConnector.setFilterTransformerExecutor(filterTransformerExecutor);

            chain.addDestination(i, destinationConnector);
        }

        channel.addDestinationChainProvider(chain);

        ChannelController.getInstance().deleteAllMessages(channel.getChannelId());
        TestUtils.deleteChannelStatistics(channel.getChannelId());
        channel.deploy();
        channel.start(null);

        // Send messages through the channel
        for (int i = 1; i <= TEST_SIZE; i++) {
            ((TestSourceConnector) channel.getSourceConnector()).readTestMessage(testMessage);
        }

        // Assert that the messages were sent
        assertEquals(TEST_SIZE, channel.getNumMessages());

        // Assert the source connector stats are correct
        statsEqual(channel.getChannelId(), 0, TEST_SIZE, 0L, TEST_SIZE, 0L, 0L, 0L);
        // Assert that destination connector stats are correct
        statsEqual(channel.getChannelId(), 1, TEST_SIZE, TEST_SIZE, 0L, 0L, 0L, 0L);
        statsEqual(channel.getChannelId(), 2, TEST_SIZE, 0L, 0L, 0L, TEST_SIZE, 0L);
        statsEqual(channel.getChannelId(), 3, TEST_SIZE, 0L, 0L, 0L, 0L, 0L);
        statsEqual(channel.getChannelId(), 4, TEST_SIZE, 0L, 0L, 0L, 0L, TEST_SIZE);
        // Assert the aggregate stats are correct
        assertTrue(channelStatsCorrect());

        channel.stop();
        channel.undeploy();

        ChannelController.getInstance().removeChannel(channel.getChannelId());
    }

    /*
     * Create a channel where the destination connector sends messages as usual, but both the
     * dispatcher's send method and the response transformer's doTransform method stall the
     * processing thread for a specified amount of time. This way, we can check at specific
     * intervals whether the message statistics in the database have been updated to QUEUED,
     * PENDING, and the final return status.
     * 
     * Send messages, and for each message: Wait a specified amount of time, then assert: - The
     * destination connector stats have 1 for QUEUED, the total number of messages for RECEIVED, and
     * the remainder for the final return status.
     * 
     * Wait a specified amount of time, then assert: - The destination connector stats have 1 for
     * PENDING, the total number of messages for RECEIVED, and the remainder for the final return
     * status.
     * 
     * Wait a specified amount of time, then assert: - The source connector stats have the total
     * number of messages for RECEIVED and TRANSFORMED - The destination connector stats have the
     * total number of messages for RECEIVED and the final return status. - The aggregate stats are
     * correct
     * 
     * After all messages finish processing, assert: - The source connector stats have testSize for
     * RECEIVED and TRANSFORMED - The destination connector stats have testSize for RECEIVED and the
     * final return status - The aggregate stats are correct (RECEIVED/TRANSFORMED is the same as
     * the source connector, PENDING/SENT/QUEUED is the same as the destination connectors combined,
     * FILTERED/ERROR is the same as all connectors combined)
     */
    @Test
    public final void testStatistics4() throws Exception {
        final int waitTime = 1000;
        long testSize = 5;
        Status returnStatus = Status.SENT;

        TestUtils.initChannel(channelId);

        TestChannel channel = new TestChannel();

        channel.setChannelId(channelId);
        channel.setServerId(serverId);

        channel.setPreProcessor(new TestPreProcessor());
        channel.setPostProcessor(new TestPostProcessor());

        TestSourceConnector sourceConnector = (TestSourceConnector) TestUtils.createDefaultSourceConnector();
        sourceConnector.setChannelId(channel.getChannelId());
        sourceConnector.setChannel(channel);
        channel.setSourceConnector(sourceConnector);
        channel.getSourceConnector().setFilterTransformerExecutor(TestUtils.createDefaultFilterTransformerExecutor());

        DestinationChainProvider chain = new DestinationChainProvider();
        chain.setChannelId(channel.getChannelId());

        class BlockingTestDispatcher extends TestDispatcher {
            public volatile boolean waiting = true;

            @Override
            public Response send(ConnectorProperties connectorProperties, ConnectorMessage message) {
                while (waiting) {
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return super.send(connectorProperties, message);
            }
        }

        // Create a destination connector that stalls the processing thread a specified amount of time during the send method
        BlockingTestDispatcher destinationConnector = new BlockingTestDispatcher();
        destinationConnector.setChannelId(channel.getChannelId());

        destinationConnector.setMetaDataReplacer(sourceConnector.getMetaDataReplacer());
        destinationConnector.setMetaDataColumns(channel.getMetaDataColumns());
        destinationConnector.setFilterTransformerExecutor(TestUtils.createDefaultFilterTransformerExecutor());

        TestDispatcherProperties connectorProperties = new TestDispatcherProperties();
        connectorProperties.getDestinationConnectorProperties().setQueueEnabled(true);
        connectorProperties.getDestinationConnectorProperties().setSendFirst(false);

        destinationConnector.setConnectorProperties(connectorProperties);
        destinationConnector.setDestinationName(TestUtils.DEFAULT_DESTINATION_NAME);
        destinationConnector.setInboundDataType(new TestDataType());
        destinationConnector.setOutboundDataType(new TestDataType());
        destinationConnector.setResponseTransformerExecutor(TestUtils.createDefaultResponseTransformerExecutor());

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("localChannelId", ChannelController.getInstance().getLocalChannelId(channel.getChannelId()));
        params.put("channelId", channel.getChannelId());
        params.put("metaDataId", 1);
        params.put("status", Status.QUEUED);

        DestinationQueue destinationConnectorQueue = new DestinationQueue(connectorProperties.getDestinationConnectorProperties().getThreadAssignmentVariable(), connectorProperties.getDestinationConnectorProperties().getThreadCount(), connectorProperties.getDestinationConnectorProperties().isRegenerateTemplate(), destinationConnector.getSerializer(), destinationConnector.getMessageMaps());
        destinationConnectorQueue.setDataSource(new ConnectorMessageQueueDataSource(channel.getChannelId(), channel.getServerId(), 1, Status.QUEUED, false, daoFactory));
        destinationConnector.setQueue(destinationConnectorQueue);

        ((TestDispatcher) destinationConnector).setReturnStatus(returnStatus);

        class BlockingTestResponseTransformer extends TestResponseTransformer {
            public volatile boolean waiting = true;

            @Override
            public String doTransform(Response response, ConnectorMessage connectorMessage) throws DonkeyException, InterruptedException {
                while (waiting) {
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return super.doTransform(response, connectorMessage);
            }
        }

        // Create a response transformer that stalls the processing thread a specified amount of time
        BlockingTestResponseTransformer responseTransformer = new BlockingTestResponseTransformer();
        destinationConnector.getResponseTransformerExecutor().setResponseTransformer(responseTransformer);

        chain.addDestination(1, destinationConnector);

        channel.addDestinationChainProvider(chain);

        ChannelController.getInstance().deleteAllMessages(channel.getChannelId());
        TestUtils.deleteChannelStatistics(channel.getChannelId());
        channel.deploy();
        channel.start(null);

        Map<Status, Long> stats;

        logger.info(String.format("%-140s", "Testing destination connector statistics changing: RECEIVED->QUEUED->PENDING->SENT"));
        logger.info(String.format("%-140s", "Test size: " + testSize));
        logger.info(String.format("%-140s", ""));

        logger.info(String.format("%-140s", "Destination Connector Stats"));
        logger.info(String.format("%-140s", "---------------------------"));

        // Send messages through the channel
        for (long i = 1; i <= testSize; i++) {
            destinationConnector.waiting = true;
            responseTransformer.waiting = true;

            logger.info(String.format("%-140s", "Sending Message #" + i + "..."));
            ((TestSourceConnector) channel.getSourceConnector()).readTestMessage(testMessage);

            logger.info(String.format("%-140s", String.format("%-50s", "   - After sending (should be QUEUED): ") + (stats = ChannelController.getInstance().getStatistics().getConnectorStats(channelId, 1))));

            // Assert that destination connector stats are correct
            assertDestinationStatsCorrect(stats, i, returnStatus, Status.QUEUED);

            destinationConnector.waiting = false;
            logger.info(String.format("%-140s", "   - Waiting " + waitTime + " ms..."));
            Thread.sleep(waitTime);

            logger.info(String.format("%-140s", String.format("%-50s", "   - After first wait (should be PENDING):   ") + (stats = ChannelController.getInstance().getStatistics().getConnectorStats(channelId, 1))));

            // Assert that destination connector stats are correct
            assertDestinationStatsCorrect(stats, i, returnStatus, Status.PENDING);

            responseTransformer.waiting = false;
            logger.info(String.format("%-140s", "   - Waiting " + waitTime + " ms..."));
            Thread.sleep(waitTime);

            logger.info(String.format("%-140s", String.format("%-50s", "   - After second wait (should be " + returnStatus + "): ") + (stats = ChannelController.getInstance().getStatistics().getConnectorStats(channelId, 1))));

            // Assert the source connector stats are correct
            statsEqual(channel.getChannelId(), 0, i, 0L, i, 0L, 0L, 0L);

            // Assert that destination connector stats are correct
            assertDestinationStatsCorrect(stats, i, returnStatus, returnStatus);

            // Assert the aggregate stats are correct
            assertTrue(channelStatsCorrect());
        }

        // Assert that the messages were sent
        assertEquals(testSize, channel.getNumMessages());

        // Assert the source connector stats are correct
        statsEqual(channel.getChannelId(), 0, testSize, 0L, testSize, 0L, 0L, 0L);
        // Assert that destination connector stats are correct
        destinationStatsCorrect(channel.getChannelId(), 1, testSize, returnStatus, returnStatus);
        // Assert the aggregate stats are correct
        assertTrue(channelStatsCorrect());

        channel.stop();
        channel.undeploy();

        ChannelController.getInstance().removeChannel(channel.getChannelId());
    }

    private void statsEqual(String channelId, Integer metaDataId, Long received, Long filtered, Long transformed, Long pending, Long sent, Long error) {
        assertStatsEqual(ChannelController.getInstance().getStatistics().getConnectorStats(channelId, metaDataId), received, filtered, transformed, pending, sent, error);
    }

    private void assertStatsEqual(Map<Status, Long> stats, Long received, Long filtered, Long transformed, Long pending, Long sent, Long error) {
        assertEquals(received, stats.get(Status.RECEIVED));
        assertEquals(filtered, stats.get(Status.FILTERED));
        assertEquals(transformed, stats.get(Status.TRANSFORMED));
        assertEquals(pending, stats.get(Status.PENDING));
        assertEquals(sent, stats.get(Status.SENT));
        assertEquals(error, stats.get(Status.ERROR));
    }

    /*
     * Gets the aggregate channel statistics from storage and checks whether they are correct with
     * respect to each individual connector. Although the TRANSFORMED status is updated in the
     * channel stats along with both source and destination connectors (in ChannelStatistics), the
     * destination connector message status is never updated in the database as TRANSFORMED after it
     * passes through the filter/transformer. So here, we only check the aggregate TRANSFORMED
     * statistics with respect to the source connector.
     */
    private boolean channelStatsCorrect() {
        Map<Integer, Map<Status, Long>> stats = ChannelController.getInstance().getStatistics().getChannelStats(channelId);
        Map<Status, Long> channelStats = createStatsMap();

        for (Integer metaDataId : stats.keySet()) {
            if (metaDataId != null) {
                for (Status status : stats.get(metaDataId).keySet()) {
                    switch (status) {
                        // Aggregate RECEIVED and TRANSFORMED stats should be the same as the source connector
                        case RECEIVED:
                        case TRANSFORMED:
                            if (metaDataId == 0) {
                                channelStats.put(status, channelStats.get(status) + stats.get(metaDataId).get(status));
                            }
                            break;

                        // Aggregate FILTERED and ERROR stats should be the same as all connectors combined
                        case FILTERED:
                        case ERROR:
                            channelStats.put(status, channelStats.get(status) + stats.get(metaDataId).get(status));
                            break;

                        // Aggregate PENDING, SENT, and QUEUED stats should be the same as the destination connector
                        case PENDING:
                        case SENT:
                        case QUEUED:
                            if (metaDataId > 0) {
                                channelStats.put(status, channelStats.get(status) + stats.get(metaDataId).get(status));
                            }
                            break;
                    }
                }
            }
        }

        return channelStats.equals(stats.get(null));
    }

    private void destinationStatsCorrect(String channelId, int metaDataId, long numMessages, Status returnStatus, Status currentStatus) {
        assertDestinationStatsCorrect(ChannelController.getInstance().getStatistics().getConnectorStats(channelId, metaDataId), numMessages, returnStatus, currentStatus);
    }

    private void assertDestinationStatsCorrect(Map<Status, Long> stats, long numMessages, Status returnStatus, Status currentStatus) {
        long pending = 0;
        long remainder = numMessages;
        switch (currentStatus) {
            case PENDING:
                pending = 1;
                remainder--;
                break;
            case QUEUED:
                remainder--;
                break;
            default:
                break;
        }

        switch (returnStatus) {
            case FILTERED:
                assertStatsEqual(stats, numMessages, remainder, 0L, pending, 0L, 0L);
                break;

            case SENT:
                assertStatsEqual(stats, numMessages, 0L, 0L, pending, remainder, 0L);
                break;

            case QUEUED:
                assertStatsEqual(stats, numMessages, 0L, 0L, pending, 0L, 0L);
                break;

            case ERROR:
                assertStatsEqual(stats, numMessages, 0L, 0L, pending, 0L, remainder);
                break;
        }
    }

    private Map<Status, Long> createStatsMap() {
        Map<Status, Long> connectorStats = new HashMap<Status, Long>();
        connectorStats.put(Status.RECEIVED, 0L);
        connectorStats.put(Status.FILTERED, 0L);
        connectorStats.put(Status.TRANSFORMED, 0L);
        connectorStats.put(Status.PENDING, 0L);
        connectorStats.put(Status.SENT, 0L);
        connectorStats.put(Status.ERROR, 0L);

        return connectorStats;
    }
}