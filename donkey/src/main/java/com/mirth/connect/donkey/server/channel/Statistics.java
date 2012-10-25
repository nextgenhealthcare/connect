/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.mirth.connect.donkey.model.message.Status;

public class Statistics {
    private Map<String, Map<Integer, Map<Status, Long>>> stats = new HashMap<String, Map<Integer, Map<Status, Long>>>();

    public Map<String, Map<Integer, Map<Status, Long>>> getStats() {
        return stats;
    }

    public Map<Integer, Map<Status, Long>> getChannelStats(String channelId) {
        Map<Integer, Map<Status, Long>> channelStats = stats.get(channelId);

        if (channelStats == null) {
            channelStats = new HashMap<Integer, Map<Status, Long>>();
            stats.put(channelId, channelStats);
        }

        return channelStats;
    }

    public Map<Status, Long> getConnectorStats(String channelId, Integer metaDataId) {
        Map<Integer, Map<Status, Long>> channelStats = getChannelStats(channelId);
        Map<Status, Long> connectorStats = channelStats.get(metaDataId);

        if (connectorStats == null) {
            connectorStats = new HashMap<Status, Long>();
            connectorStats.put(Status.RECEIVED, 0L);
            connectorStats.put(Status.FILTERED, 0L);
            connectorStats.put(Status.TRANSFORMED, 0L);
            connectorStats.put(Status.PENDING, 0L);
            connectorStats.put(Status.SENT, 0L);
            connectorStats.put(Status.QUEUED, 0L);
            connectorStats.put(Status.ERROR, 0L);

            channelStats.put(metaDataId, connectorStats);
        }

        return connectorStats;
    }

    public void update(String channelId, int metaDataId, Status incrementStatus, Status decrementStatus) {
        Map<Status, Long> statsDiff = new HashMap<Status, Long>();
        statsDiff.put(incrementStatus, 1L);

        if (decrementStatus != null) {
            if (incrementStatus == decrementStatus) {
                statsDiff.put(decrementStatus, 0L);
            } else {
                statsDiff.put(decrementStatus, -1L);
            }
        }

        update(channelId, metaDataId, statsDiff);
    }

    public void update(String channelId, int metaDataId, Map<Status, Long> statsDiff) {
        Map<Status, Long> channelStats = getConnectorStats(channelId, null);
        Map<Status, Long> connectorStats = getConnectorStats(channelId, metaDataId);

        for (Entry<Status, Long> statsEntry : statsDiff.entrySet()) {
            Status status = statsEntry.getKey();
            Long count = statsEntry.getValue();

            connectorStats.put(status, connectorStats.get(status) + count);

            // update the channel statistics
            switch (status) {
            // update the following statuses based on the source connector
                case RECEIVED:
                    if (metaDataId == 0) {
                        channelStats.put(status, channelStats.get(status) + count);
                    }
                    break;

                // update the following statuses based on the source and destination connectors
                case FILTERED:
                case TRANSFORMED:
                case ERROR:
                    channelStats.put(status, channelStats.get(status) + count);
                    break;

                // update the following statuses based on the destination connectors
                case PENDING:
                case SENT:
                case QUEUED:
                    if (metaDataId > 0) {
                        channelStats.put(status, channelStats.get(status) + count);
                    }
                    break;
            }
        }
    }
}
