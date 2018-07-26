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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.donkey.server.data.timed.TimedDaoFactory;
import com.mirth.connect.donkey.test.util.TestSourceConnector;
import com.mirth.connect.donkey.test.util.TestUtils;
import com.mirth.connect.donkey.util.ActionTimer;

public class ChannelControllerTests {
    private static int TEST_SIZE = 10;
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
     * Remove the channel corresponding to channelId, if applicable
     * Call getLocalChannelId, assert that:
     * - The channel row was inserted
     * - All channel message tables were created
     * 
     * Call getLocalChannelId again, assert that:
     * - The ID returned is the same one returned previously
     */
    @Test
    final public void testGetLocalChannelId() throws Exception {
        try {
            logger.info("Testing ChannelController.getLocalChannelId...");

            for (int i = 1; i <= TEST_SIZE; i++) {
                ChannelController.getInstance().removeChannel(channelId);
                long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);

                // Assert that the channel was created
                TestUtils.assertChannelExists(channelId, true);

                // Assert that subsequent calls return the same local channel ID
                assertEquals(localChannelId, (long) ChannelController.getInstance().getLocalChannelId(channelId));
            }

            System.out.println(daoTimer.getLog());
        } finally {
            ChannelController.getInstance().removeChannel(channelId);
        }
    }

    /*
     * Remove the channel corresponding to channelId (if applicable), assert:
     * - The channel statistics in the database are equal to the ones returned
     * from getStatistics
     * 
     * Create a new channel (use channelId), then assert:
     * - The channel statistics in the database are equal to the ones returned
     * from getStatistics
     * 
     * Send messages through the channel, and after each message, assert:
     * - The channel statistics in the database are equal to the ones returned
     * from getStatistics
     */
    @Test
    public final void testGetTotals() throws Exception {
        Channel channel = null;

        try {
            logger.info("Testing ChannelController.getTotals...");

            ChannelController.getInstance().removeChannel(channelId);
            assertEquals(TestUtils.getChannelStatistics(channelId), ChannelController.getInstance().getStatistics().getChannelStats(channelId));

            channel = TestUtils.createDefaultChannel(channelId, serverId);
            channel.deploy();
            channel.start(null);

            assertEquals(TestUtils.getChannelStatistics(channel.getChannelId()), ChannelController.getInstance().getStatistics().getChannelStats(channelId));

            for (int i = 1; i <= TEST_SIZE; i++) {
                ((TestSourceConnector) channel.getSourceConnector()).readTestMessage(testMessage);

                assertEquals(TestUtils.getChannelStatistics(channel.getChannelId()), ChannelController.getInstance().getStatistics().getChannelStats(channelId));
            }

            System.out.println(daoTimer.getLog());
        } finally {
            if (channel != null) {
                channel.stop();
                channel.undeploy();
                ChannelController.getInstance().removeChannel(channel.getChannelId());
            }
        }
    }

    /*
     * Create a new default channel
     * Insert some random initial source/destination/aggregate statistics
     * Start up the channel, send messages, and after each message assert:
     * - The statistics returned from getStatistics is equal to the difference
     * between the statistics in the database and the initial statistics
     */
    @Test
    public final void testGetStatistics() throws Exception {
        Channel channel = TestUtils.createDefaultChannel(channelId, serverId);

        try {
            logger.info("Testing ChannelController.getStatistics...");

            // Insert some initial statistics
            Connection connection = null;
            PreparedStatement statement = null;
            try {
                long localChannelId = ChannelController.getInstance().getLocalChannelId(channelId);
                connection = TestUtils.getConnection();
                statement = connection.prepareStatement("DELETE FROM d_ms" + localChannelId);
                statement.executeUpdate();
                statement.close();
                
                for (Integer metaDataId : new Integer[] { null, 0, 1 }) {
                    statement = connection.prepareStatement("INSERT INTO d_ms" + localChannelId + " (metadata_id, received, filtered, transformed, pending, sent, error) VALUES (?,?,?,?,?,?,?)");
                    if (metaDataId != null) {
                        statement.setInt(1, metaDataId);
                    } else {
                        statement.setNull(1, Types.INTEGER);
                    }
                    for (int i = 2; i <= 7; i++) {
                        statement.setInt(i, (int) (Math.random() * 100));
                    }
                    statement.executeUpdate();
                    statement.close();
                }
                connection.commit();
            } finally {
                TestUtils.close(statement);
                TestUtils.close(connection);
            }

            // Get the initial statistics
            Map<Integer, Map<Status, Long>> initialStats = TestUtils.getChannelStatistics(channel.getChannelId());

            channel.deploy();
            channel.start(null);

            // Send messages
            for (int i = 1; i <= TEST_SIZE; i++) {
                ((TestSourceConnector) channel.getSourceConnector()).readTestMessage(testMessage);

                Map<Integer, Map<Status, Long>> dbStats = TestUtils.getChannelStatistics(channel.getChannelId());
                Map<Integer, Map<Status, Long>> vmStats = ChannelController.getInstance().getStatistics().getChannelStats(channel.getChannelId());
                Map<Integer, Map<Status, Long>> subtractedStats = subtractStats(dbStats, initialStats);

                // Assert that getStatistics returns the difference between the current database statistics and the initial statistics
                assertEquals(subtractedStats, vmStats);
            }

            System.out.println(daoTimer.getLog());
        } finally {
            channel.stop();
            channel.undeploy();
            ChannelController.getInstance().removeChannel(channel.getChannelId());
        }
    }

    private Map<Integer, Map<Status, Long>> subtractStats(Map<Integer, Map<Status, Long>> minuend, Map<Integer, Map<Status, Long>> subtrahend) {
        Map<Integer, Map<Status, Long>> stats = new HashMap<Integer, Map<Status, Long>>();

        for (Integer metaDataId : joinSets(minuend.keySet(), subtrahend.keySet())) {
            Map<Status, Long> connectorStats = new HashMap<Status, Long>();

            for (Status status : Status.values()) {
            	if (status != Status.QUEUED) {
	                connectorStats.put(status, 0L);
	                if (minuend.containsKey(metaDataId) && minuend.get(metaDataId).containsKey(status)) {
	                    connectorStats.put(status, minuend.get(metaDataId).get(status));
	                }
	                if (subtrahend.containsKey(metaDataId) && subtrahend.get(metaDataId).containsKey(status)) {
	                    connectorStats.put(status, connectorStats.get(status) - subtrahend.get(metaDataId).get(status));
	                }
            	}
            }

            stats.put(metaDataId, connectorStats);
        }

        return stats;
    }

    private Set<Integer> joinSets(Set<Integer> set1, Set<Integer> set2) {
        Set<Integer> joinedSet = new HashSet<Integer>();
        joinedSet.addAll(set1);
        joinedSet.addAll(set2);
        return joinedSet;
    }
}
