/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.ArrayUtils;

import com.mirth.connect.donkey.model.event.MessageEventType;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.event.EventDispatcher;
import com.mirth.connect.donkey.server.event.MessageEvent;

public class Statistics {

    public static final Status[] TRACKED_STATUSES = new Status[] { Status.RECEIVED,
            Status.FILTERED, Status.SENT, Status.ERROR };

    private Map<String, Map<Integer, Map<Status, AtomicLong>>> stats = new ConcurrentHashMap<String, Map<Integer, Map<Status, AtomicLong>>>();
    private EventDispatcher eventDispatcher;
    private boolean sendEvents;
    private boolean allowNegatives;

    public Statistics(boolean sendEvents) {
        this(sendEvents, false);
    }

    public Statistics(boolean sendEvents, boolean allowNegatives) {
        this.sendEvents = sendEvents;
        this.allowNegatives = allowNegatives;
    }
    
    public Map<String, Map<Integer, Map<Status, Long>>> getStats() {
        Map<String, Map<Integer, Map<Status, Long>>> stats = new HashMap<String, Map<Integer, Map<Status, Long>>>();

        for (Entry<String, Map<Integer, Map<Status, AtomicLong>>> channelEntry : this.stats.entrySet()) {
            Map<Integer, Map<Status, Long>> channelMap = new LinkedHashMap<Integer, Map<Status, Long>>();

            for (Entry<Integer, Map<Status, AtomicLong>> metaDataEntry : channelEntry.getValue().entrySet()) {
                Map<Status, Long> statusMap = new LinkedHashMap<Status, Long>();

                for (Entry<Status, AtomicLong> statusEntry : metaDataEntry.getValue().entrySet()) {
                    statusMap.put(statusEntry.getKey(), statusEntry.getValue().get());
                }

                channelMap.put(metaDataEntry.getKey(), statusMap);
            }

            stats.put(channelEntry.getKey(), channelMap);
        }

        return stats;
    }

    public Map<Integer, Map<Status, Long>> getChannelStats(String channelId) {
        Map<Integer, Map<Status, Long>> channelStats = new LinkedHashMap<Integer, Map<Status, Long>>();

        for (Entry<Integer, Map<Status, AtomicLong>> metaDataEntry : getChannelStatsMap(channelId).entrySet()) {
            Map<Status, Long> statusMap = new LinkedHashMap<Status, Long>();

            for (Entry<Status, AtomicLong> statusEntry : metaDataEntry.getValue().entrySet()) {
                statusMap.put(statusEntry.getKey(), statusEntry.getValue().get());
            }

            channelStats.put(metaDataEntry.getKey(), statusMap);
        }

        return channelStats;
    }

    public Map<Status, Long> getConnectorStats(String channelId, Integer metaDataId) {
        Map<Status, Long> statusMap = new LinkedHashMap<Status, Long>();

        for (Entry<Status, AtomicLong> statusEntry : getConnectorStatsMap(getChannelStatsMap(channelId), metaDataId).entrySet()) {
            statusMap.put(statusEntry.getKey(), statusEntry.getValue().get());
        }

        return statusMap;
    }

    public boolean isEmpty() {
        if (!stats.isEmpty()) {
            for (Map<Integer, Map<Status, AtomicLong>> channelMap : stats.values()) {
                for (Map<Status, AtomicLong> statusMap : channelMap.values()) {
                    for (AtomicLong diff : statusMap.values()) {
                        if (diff.get() != 0) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    public void update(Map<String, Map<Integer, Map<Status, Long>>> stats) {
        for (Entry<String, Map<Integer, Map<Status, Long>>> channelEntry : stats.entrySet()) {
            for (Entry<Integer, Map<Status, Long>> connectorEntry : channelEntry.getValue().entrySet()) {
                Integer metaDataId = connectorEntry.getKey();

                if (metaDataId != null) {
                    update(channelEntry.getKey(), metaDataId, connectorEntry.getValue());
                }
            }
        }
    }

    public void update(String channelId, int metaDataId, Status incrementStatus, Status decrementStatus) {
        // If no net change will be done then return immediately
        if (incrementStatus == decrementStatus) {
            return;
        }

        Map<Status, Long> statsDiff = new LinkedHashMap<Status, Long>();
        statsDiff.put(incrementStatus, 1L);

        if (decrementStatus != null) {
            statsDiff.put(decrementStatus, -1L);
        }

        update(channelId, metaDataId, statsDiff);
    }

    public void update(String channelId, int metaDataId, Map<Status, Long> statsDiff) {
        Map<Integer, Map<Status, AtomicLong>> channelStats = getChannelStatsMap(channelId);
        Map<Status, AtomicLong> aggregateStats = getConnectorStatsMap(channelStats, null);
        Map<Status, AtomicLong> connectorStats = getConnectorStatsMap(channelStats, metaDataId);

        for (Entry<Status, Long> statsEntry : statsDiff.entrySet()) {
            Long diff = statsEntry.getValue();

            if (ArrayUtils.contains(TRACKED_STATUSES, statsEntry.getKey()) && diff != 0) {
                Status status = statsEntry.getKey();

                AtomicLong statValue = connectorStats.get(status);
                Long connectorCount = updateStat(statValue, diff);
                
                // update the channel statistics
                switch (status) {
                // update the following statuses based on the source connector
                    case RECEIVED:
                        if (metaDataId == 0) {
                            updateStat(aggregateStats.get(status), diff);
                        }
                        break;

                    // update the following statuses based on the source and destination connectors
                    case FILTERED:
                    case ERROR:
                        updateStat(aggregateStats.get(status), diff);
                        break;

                    // update the following statuses based on the destination connectors
                    case SENT:
                        if (metaDataId > 0) {
                            updateStat(aggregateStats.get(status), diff);
                        }
                        break;

                    default:
                        break;
                }

                if (sendEvents) {
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

    private long updateStat(AtomicLong stat, Long diff) {
        // stats values can not go below zero. If we are decrementing, synchronize so threads don't decrement below zero accidentally
        if (!allowNegatives && diff < 0) {
            synchronized (stat) {
                long connectorDiff = diff;
                long statValueL =stat.get();
                // if the resulting value < 0, floor the resulting value at zero.
                if (statValueL + diff < 0L) {
                    connectorDiff = -statValueL;
                }
                return stat.addAndGet(connectorDiff);
            }
        } else {    // else just do an add like usual. Atomic Long will make sure multiple threads increment correctly.
            return stat.addAndGet(diff);
        }
    }
    
    public void overwrite(String channelId, Integer metaDataId, Map<Status, Long> stats) {
        Map<Status, AtomicLong> connectorStats = getConnectorStatsMap(getChannelStatsMap(channelId), metaDataId);
        for (Entry<Status, Long> entry : stats.entrySet()) {
            connectorStats.get(entry.getKey()).set(entry.getValue());
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

    public void resetStats(String channelId, Integer metaDataId, Set<Status> statuses) {
        for (Status status : statuses) {
            if (ArrayUtils.contains(TRACKED_STATUSES, status)) {
                Map<Status, AtomicLong> connectorStats = getConnectorStatsMap(getChannelStatsMap(channelId), metaDataId);
                connectorStats.get(status).set(0L);

                if (sendEvents && metaDataId != null) {
                    MessageEventType type = MessageEventType.fromStatus(status);

                    if (type != null) {
                        // Dispatch a message event if the the status is in MessageEventType and the connector stat was updated
                        if (eventDispatcher == null) {
                            eventDispatcher = Donkey.getInstance().getEventDispatcher();
                        }

                        eventDispatcher.dispatchEvent(new MessageEvent(channelId, metaDataId, type, 0L, true));
                    }
                }
            }
        }
    }

    public void remove(String channelId) {
        synchronized (stats) {
            Map<Integer, Map<Status, AtomicLong>> channelStats = stats.get(channelId);

            if (channelStats != null) {
                synchronized (channelStats) {
                    stats.remove(channelId);
                }
            }
        }
    }

    public void clear() {
        synchronized (stats) {
            stats.clear();
        }
    }

    private Map<Integer, Map<Status, AtomicLong>> getChannelStatsMap(String channelId) {
        Map<Integer, Map<Status, AtomicLong>> channelStats = stats.get(channelId);

        if (channelStats == null) {
            synchronized (stats) {
                channelStats = stats.get(channelId);

                if (channelStats == null) {
                    channelStats = new LinkedHashMap<Integer, Map<Status, AtomicLong>>();
                    stats.put(channelId, channelStats);
                }
            }
        }

        return channelStats;
    }

    private Map<Status, AtomicLong> getConnectorStatsMap(Map<Integer, Map<Status, AtomicLong>> channelStats, Integer metaDataId) {
        Map<Status, AtomicLong> connectorStats = channelStats.get(metaDataId);

        if (connectorStats == null) {
            synchronized (channelStats) {
                connectorStats = channelStats.get(metaDataId);

                if (connectorStats == null) {
                    connectorStats = new LinkedHashMap<Status, AtomicLong>();
                    connectorStats.put(Status.RECEIVED, new AtomicLong(0L));
                    connectorStats.put(Status.FILTERED, new AtomicLong(0L));
                    connectorStats.put(Status.SENT, new AtomicLong(0L));
                    connectorStats.put(Status.ERROR, new AtomicLong(0L));

                    channelStats.put(metaDataId, connectorStats);
                }
            }
        }

        return connectorStats;
    }

    public static Set<Status> getTrackedStatuses() {
        return new HashSet<Status>(Arrays.asList(TRACKED_STATUSES));
    }
}