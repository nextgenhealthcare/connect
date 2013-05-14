/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.mirth.connect.donkey.model.event.MessageEventType;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.event.EventDispatcher;
import com.mirth.connect.donkey.server.event.MessageEvent;

public class Statistics {
    private Map<String, Map<Integer, Map<Status, Long>>> stats = new LinkedHashMap<String, Map<Integer, Map<Status, Long>>>();
    private EventDispatcher eventDispatcher;
    private boolean sendEvents;
    
    public Statistics(boolean sendEvents) {
        this.sendEvents = sendEvents;
    }

    public Map<String, Map<Integer, Map<Status, Long>>> getStats() {
        return stats;
    }
    
    public void resetStats(String channelId, Integer metaDataId, Set<Status> statuses) {
        for (Status status : statuses) {
            getChannelStats(channelId).get(metaDataId).put(status, 0L);
            
            if (sendEvents && metaDataId != null) {
                MessageEventType type = MessageEventType.fromStatus(status);
                if (type != null && status != Status.QUEUED) {
                    // Dispatch a message event if the the status is in MessageEventType and the connector stat was updated
                    if (eventDispatcher == null) {
                        eventDispatcher = Donkey.getInstance().getEventDispatcher();
                    }
                    eventDispatcher.dispatchEvent(new MessageEvent(channelId, metaDataId, type, 0L, true));
                }
            }
        }
    }

    public Map<Integer, Map<Status, Long>> getChannelStats(String channelId) {
        Map<Integer, Map<Status, Long>> channelStats = stats.get(channelId);

        if (channelStats == null) {
            channelStats = new LinkedHashMap<Integer, Map<Status, Long>>();
            stats.put(channelId, channelStats);
        }

        return channelStats;
    }

    public Map<Status, Long> getConnectorStats(String channelId, Integer metaDataId) {
        Map<Integer, Map<Status, Long>> channelStats = getChannelStats(channelId);
        Map<Status, Long> connectorStats = channelStats.get(metaDataId);

        if (connectorStats == null) {
            connectorStats = new LinkedHashMap<Status, Long>();
            connectorStats.put(Status.RECEIVED, 0L);
            connectorStats.put(Status.FILTERED, 0L);
            connectorStats.put(Status.TRANSFORMED, 0L);
            connectorStats.put(Status.PENDING, 0L);
            connectorStats.put(Status.SENT, 0L);
            connectorStats.put(Status.ERROR, 0L);

            channelStats.put(metaDataId, connectorStats);
        }

        return connectorStats;
    }

    public void update(String channelId, int metaDataId, Status incrementStatus, Status decrementStatus) {
        Map<Status, Long> statsDiff = new LinkedHashMap<Status, Long>();
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
            Long diff = statsEntry.getValue();

            if (diff != 0) {
                Long connectorCount = null;
                // update the connector statistics
                if (status != Status.QUEUED) {
                    connectorCount = connectorStats.get(status) + diff;
                    connectorStats.put(status, connectorCount);
                }
    
                // update the channel statistics
                switch (status) {
                // update the following statuses based on the source connector
                    case RECEIVED:
                        if (metaDataId == 0) {
                            channelStats.put(status, channelStats.get(status) + diff);
                        }
                        break;
    
                    // update the following statuses based on the source and destination connectors
                    case FILTERED:
                    case TRANSFORMED:
                    case ERROR:
                        channelStats.put(status, channelStats.get(status) + diff);
                        break;
    
                    // update the following statuses based on the destination connectors
                    case PENDING:
                    case SENT:
                        if (metaDataId > 0) {
                            channelStats.put(status, channelStats.get(status) + diff);
                        }
                        break;
    
                    // Queued statistics are managed by the queue itself. Neither the channel nor connector should store them.
                    // This case is added here for readability.
                    case QUEUED:
                        break;
                }
                
                if (sendEvents && connectorCount != null) {
                    MessageEventType type = MessageEventType.fromStatus(status);
                    if (type != null) {
                        // Dispatch a message event if the the status is in MessageEventType and the connector stat was updated
                        if (eventDispatcher == null) {
                            eventDispatcher = Donkey.getInstance().getEventDispatcher();
                        }
                        eventDispatcher.dispatchEvent(new MessageEvent(channelId, metaDataId, type, connectorCount, diff <= 0));
                    }
                }
            }
        }
    }

    /**
     * Updates (increments/decrements) values from another Statistics object
     */
    public void update(Statistics statistics) {
        for (Entry<String, Map<Integer, Map<Status, Long>>> entry : statistics.getStats().entrySet()) {
            for (Entry<Integer, Map<Status, Long>> connectorEntry : entry.getValue().entrySet()) {
                Integer metaDataId = connectorEntry.getKey();

                if (metaDataId != null) {
                    update(entry.getKey(), metaDataId, connectorEntry.getValue());
                }
            }
        }
    }
}
