/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.mirth.connect.donkey.model.message.Status;

public class StatisticsTest {

    
    Statistics statisticNoNegativeValues;
    Statistics statisticAllowNegatives;
    final String CHANNEL_ID = "StatisticsTest";
    final int CONNECTOR1_ID = 1;
    final int CONNECTOR2_ID = 2;
    final Status KEY = Status.ERROR;
    
    @Before
    public void setup() {
        statisticNoNegativeValues = new Statistics(false); // default statistics object does not allow negative values
        statisticAllowNegatives = new Statistics(false, true);
    }
    
    @Test
    public void decrement_IfAtZero_ShouldStayAtZero() {
        statisticNoNegativeValues.update(CHANNEL_ID, CONNECTOR1_ID, null, KEY);

        // confirm that neither the connector stats nor aggregate stat are negative
        assertExistsAndEq(statisticNoNegativeValues.getConnectorStats(CHANNEL_ID, CONNECTOR1_ID), KEY, 0L);
        assertExistsAndEq(statisticNoNegativeValues.getConnectorStats(CHANNEL_ID, null), KEY, 0L);
    }

    @Test
    public void decrement_IfAtZero_ShouldBeMinusOne() {
        statisticAllowNegatives.update(CHANNEL_ID, CONNECTOR1_ID, null, KEY);

        // confirm that neither the connector stats nor aggregate stat are negative
        assertExistsAndEq(statisticAllowNegatives.getConnectorStats(CHANNEL_ID, CONNECTOR1_ID), KEY, -1L);
        assertExistsAndEq(statisticAllowNegatives.getConnectorStats(CHANNEL_ID, null), KEY, -1L);
    }
    
    @Test
    public void incrementTwice_IfAtZero_ShouldBeTwo() {
        statisticNoNegativeValues.update(CHANNEL_ID, CONNECTOR1_ID, KEY, null);
        statisticNoNegativeValues.update(CHANNEL_ID, CONNECTOR1_ID, KEY, null);
        statisticAllowNegatives.update(CHANNEL_ID, CONNECTOR1_ID, KEY, null);
        statisticAllowNegatives.update(CHANNEL_ID, CONNECTOR1_ID, KEY, null);

        // confirm that both the connector stats and aggregate stat are at 2
        assertExistsAndEq(statisticNoNegativeValues.getConnectorStats(CHANNEL_ID, CONNECTOR1_ID), KEY, 2L);
        assertExistsAndEq(statisticNoNegativeValues.getConnectorStats(CHANNEL_ID, null), KEY, 2L);
        assertExistsAndEq(statisticAllowNegatives.getConnectorStats(CHANNEL_ID, CONNECTOR1_ID), KEY, 2L);
        assertExistsAndEq(statisticAllowNegatives.getConnectorStats(CHANNEL_ID, null), KEY, 2L);
    }

    @Test
    public void incrementTwiceDecrementOnce_IfStartAtZero_ShouldBeOne() {
        statisticNoNegativeValues.update(CHANNEL_ID, CONNECTOR1_ID, KEY, null);
        statisticNoNegativeValues.update(CHANNEL_ID, CONNECTOR1_ID, KEY, null);
        statisticNoNegativeValues.update(CHANNEL_ID, CONNECTOR1_ID, null, KEY);
        statisticAllowNegatives.update(CHANNEL_ID, CONNECTOR1_ID, KEY, null);
        statisticAllowNegatives.update(CHANNEL_ID, CONNECTOR1_ID, KEY, null);
        statisticAllowNegatives.update(CHANNEL_ID, CONNECTOR1_ID, null, KEY);

        // confirm that both the connector stats and aggregate stat are at 1
        assertExistsAndEq(statisticNoNegativeValues.getConnectorStats(CHANNEL_ID, CONNECTOR1_ID), KEY, 1L);
        assertExistsAndEq(statisticNoNegativeValues.getConnectorStats(CHANNEL_ID, null), KEY, 1L);
        assertExistsAndEq(statisticAllowNegatives.getConnectorStats(CHANNEL_ID, CONNECTOR1_ID), KEY, 1L);
        assertExistsAndEq(statisticAllowNegatives.getConnectorStats(CHANNEL_ID, null), KEY, 1L);
    }

    @Test
    public void decrementThenIncrement_IfStartAtZero_ShouldBeOne() {
        statisticNoNegativeValues.update(CHANNEL_ID, CONNECTOR1_ID, null, KEY);
        statisticNoNegativeValues.update(CHANNEL_ID, CONNECTOR1_ID, KEY, null);

        // confirm that both the connector stats and aggregate stat are at 1
        assertExistsAndEq(statisticNoNegativeValues.getConnectorStats(CHANNEL_ID, CONNECTOR1_ID), KEY, 1L);
        assertExistsAndEq(statisticNoNegativeValues.getConnectorStats(CHANNEL_ID, null), KEY, 1L);
    }

    @Test
    public void decrementThenIncrement_IfStartAtZero_ShouldBeZero() {
        statisticAllowNegatives.update(CHANNEL_ID, CONNECTOR1_ID, null, KEY);
        statisticAllowNegatives.update(CHANNEL_ID, CONNECTOR1_ID, KEY, null);

        // confirm that both the connector stats and aggregate stat are at 1
        assertExistsAndEq(statisticAllowNegatives.getConnectorStats(CHANNEL_ID, CONNECTOR1_ID), KEY, 0L);
        assertExistsAndEq(statisticAllowNegatives.getConnectorStats(CHANNEL_ID, null), KEY, 0L);
    }

    @Test
    public void incrementSeparateConnectors_AggregateShouldBeSum() {
        statisticNoNegativeValues.update(CHANNEL_ID, CONNECTOR1_ID, KEY, null);
        statisticNoNegativeValues.update(CHANNEL_ID, CONNECTOR2_ID, KEY, null);
        statisticAllowNegatives.update(CHANNEL_ID, CONNECTOR1_ID, KEY, null);
        statisticAllowNegatives.update(CHANNEL_ID, CONNECTOR2_ID, KEY, null);

        // confirm that the two connector stats are at 1 and aggregate is at 2
        assertExistsAndEq(statisticNoNegativeValues.getConnectorStats(CHANNEL_ID, CONNECTOR1_ID), KEY, 1L);
        assertExistsAndEq(statisticNoNegativeValues.getConnectorStats(CHANNEL_ID, CONNECTOR2_ID), KEY, 1L);
        assertExistsAndEq(statisticNoNegativeValues.getConnectorStats(CHANNEL_ID, null), KEY, 2L);
        assertExistsAndEq(statisticAllowNegatives.getConnectorStats(CHANNEL_ID, CONNECTOR1_ID), KEY, 1L);
        assertExistsAndEq(statisticAllowNegatives.getConnectorStats(CHANNEL_ID, CONNECTOR2_ID), KEY, 1L);
        assertExistsAndEq(statisticAllowNegatives.getConnectorStats(CHANNEL_ID, null), KEY, 2L);
    }
    
    @Test
    public void decrementFive_IfStartAt4_ShouldBeZero() {
        Map<Status, Long> diffPlus4 = new HashMap<>();
        diffPlus4.put(KEY, 4L);
        statisticNoNegativeValues.update(CHANNEL_ID, CONNECTOR1_ID, diffPlus4);
        // confirm that both the connector stats and aggregate stat are at 4
        assertExistsAndEq(statisticNoNegativeValues.getConnectorStats(CHANNEL_ID, CONNECTOR1_ID), KEY, 4L);
        assertExistsAndEq(statisticNoNegativeValues.getConnectorStats(CHANNEL_ID, null), KEY, 4L);
        
        Map<Status, Long> diffMinus5 = new HashMap<>();
        diffMinus5.put(KEY, -5L);
        statisticNoNegativeValues.update(CHANNEL_ID, CONNECTOR1_ID, diffMinus5);

        // confirm that both the connector stats and aggregate stat are at 0
        assertExistsAndEq(statisticNoNegativeValues.getConnectorStats(CHANNEL_ID, CONNECTOR1_ID), KEY, 0L);
        assertExistsAndEq(statisticNoNegativeValues.getConnectorStats(CHANNEL_ID, null), KEY, 0L);
    }

    @Test
    public void decrementFive_IfStartAt4_ShouldBeMinusOne() {
        Map<Status, Long> diffPlus4 = new HashMap<>();
        diffPlus4.put(KEY, 4L);
        statisticAllowNegatives.update(CHANNEL_ID, CONNECTOR1_ID, diffPlus4);
        // confirm that both the connector stats and aggregate stat are at 4
        assertExistsAndEq(statisticAllowNegatives.getConnectorStats(CHANNEL_ID, CONNECTOR1_ID), KEY, 4L);
        assertExistsAndEq(statisticAllowNegatives.getConnectorStats(CHANNEL_ID, null), KEY, 4L);
        
        Map<Status, Long> diffMinus5 = new HashMap<>();
        diffMinus5.put(KEY, -5L);
        statisticAllowNegatives.update(CHANNEL_ID, CONNECTOR1_ID, diffMinus5);

        // confirm that both the connector stats and aggregate stat are at 0
        assertExistsAndEq(statisticAllowNegatives.getConnectorStats(CHANNEL_ID, CONNECTOR1_ID), KEY, -1L);
        assertExistsAndEq(statisticAllowNegatives.getConnectorStats(CHANNEL_ID, null), KEY, -1L);
    }
    
    private void assertExistsAndEq(final Map<Status, Long> STATS, final Status KEY, final long EXPECTED) {
        assertNotNull(STATS.get(KEY));
        assertEquals(EXPECTED, STATS.get(KEY).longValue());
    }
}
