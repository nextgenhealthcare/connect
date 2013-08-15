/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.dashboardstatus;

import java.awt.Color;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.event.ConnectorEventType;
import com.mirth.connect.donkey.model.event.Event;
import com.mirth.connect.donkey.server.event.ConnectorCountEvent;
import com.mirth.connect.donkey.server.event.ConnectorEvent;
import com.mirth.connect.donkey.server.event.EventType;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.event.EventListener;

public class DashboardConnectorEventListener extends EventListener {
    private Logger logger = Logger.getLogger(this.getClass());

    private static final int MAX_LOG_SIZE = 1000;
    private static long logId = 1;
    private Map<String, Object[]> connectorStateMap = new ConcurrentHashMap<String, Object[]>();
    private Map<String, AtomicInteger> connectorCountMap = new ConcurrentHashMap<String, AtomicInteger>();
    private Map<String, Integer> maxConnectionMap = new ConcurrentHashMap<String, Integer>();
    private Map<String, LinkedList<String[]>> connectorInfoLogs = new ConcurrentHashMap<String, LinkedList<String[]>>();
    private LinkedList<String[]> entireConnectorInfoLogs = new LinkedList<String[]>();
    private Map<String, Map<String, Long>> lastDisplayedLogIndexBySessionId = new ConcurrentHashMap<String, Map<String, Long>>();
    private Map<String, Boolean> channelsDeployedFlagForEachClient = new ConcurrentHashMap<String, Boolean>();

    @Override
    protected void onShutdown() {

    }

    @Override
    public Set<EventType> getEventTypes() {
        Set<EventType> EventTypes = new HashSet<EventType>();

        EventTypes.add(EventType.CONNECTOR);

        return EventTypes;
    }

    @Override
    protected void processEvent(Event event) {
        if (event instanceof ConnectorEvent) {
            ConnectorEvent connectorEvent = (ConnectorEvent) event;
            String channelId = connectorEvent.getChannelId();
            Integer metaDataId = connectorEvent.getMetaDataId();
            String information = connectorEvent.getMessage();
            Timestamp timestamp = new Timestamp(event.getDateTime());

            String connectorId = channelId + "_" + metaDataId;

            ConnectorEventType eventType = connectorEvent.getState();

            ConnectorEventType connectorState = eventType;
            Integer connectorCount = null;
            Integer maximum = null;

            if (event instanceof ConnectorCountEvent) {
                ConnectorCountEvent connectorCountEvent = (ConnectorCountEvent) connectorEvent;

                maximum = connectorCountEvent.getMaximum();
                Boolean increment = connectorCountEvent.isIncrement();

                if (maximum != null) {
                    maxConnectionMap.put(connectorId, maximum);
                } else {
                    maximum = maxConnectionMap.get(connectorId);
                }

                AtomicInteger count = connectorCountMap.get(connectorId);

                if (count == null) {
                    count = new AtomicInteger();
                    connectorCountMap.put(connectorId, count);
                }

                if (increment != null) {
                    if (increment) {
                        count.incrementAndGet();
                    } else {
                        count.decrementAndGet();
                    }
                }

                connectorCount = count.get();

                if (connectorCount == 0) {
                    connectorState = ConnectorEventType.IDLE;
                } else {
                    connectorState = ConnectorEventType.CONNECTED;
                }
            }

            String stateString = null;
            if (connectorState.isState()) {
                Color color = getColor(connectorState);
                stateString = connectorState.toString();
                if (connectorCount != null) {
                    if (maximum != null && connectorCount.equals(maximum)) {
                        stateString += " <font color='red'>(" + connectorCount + ")</font>";
                    } else if (connectorCount > 0) {
                        stateString += " (" + connectorCount + ")";
                    }
                }

                connectorStateMap.put(connectorId, new Object[] { color, stateString });
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
            String channelName = "";
            String connectorType = "";

            LinkedList<String[]> channelLog = null;

            Channel channel = ControllerFactory.getFactory().createChannelController().getDeployedChannelById(channelId);

            if (channel != null) {
                channelName = channel.getName();
                // grab the channel's log from the HashMap, if not exist, create
                // one.
                if (connectorInfoLogs.containsKey(channelId)) {
                    channelLog = connectorInfoLogs.get(channelId);
                } else {
                    channelLog = new LinkedList<String[]>();
                }

                if (metaDataId == 0) {
                    connectorType = "Source: " + channel.getSourceConnector().getTransportName() + "  (" + channel.getSourceConnector().getTransformer().getInboundDataType().toString() + " -> " + channel.getSourceConnector().getTransformer().getOutboundDataType().toString() + ")";
                } else {
                    Connector connector = getConnectorFromMetaDataId(channel.getDestinationConnectors(), metaDataId);
                    connectorType = "Destination: " + connector.getTransportName() + " - " + connector.getName();
                }
            }

            if (channelLog != null) {
                synchronized (this) {
                    if (channelLog.size() == MAX_LOG_SIZE) {
                        channelLog.removeLast();
                    }
                    channelLog.addFirst(new String[] { String.valueOf(logId), channelName,
                            dateFormat.format(timestamp), connectorType,
                            ((ConnectorEvent) event).getState().toString(), information, channelId,
                            Integer.toString(metaDataId) });

                    if (entireConnectorInfoLogs.size() == MAX_LOG_SIZE) {
                        entireConnectorInfoLogs.removeLast();
                    }
                    entireConnectorInfoLogs.addFirst(new String[] { String.valueOf(logId),
                            channelName, dateFormat.format(timestamp), connectorType,
                            ((ConnectorEvent) event).getState().toString(), information, channelId,
                            Integer.toString(metaDataId) });

                    logId++;

                    // put the channel log into the HashMap.
                    connectorInfoLogs.put(channelId, channelLog);
                }
            }

        }
    }

    private Connector getConnectorFromMetaDataId(List<Connector> connectors, int metaDataId) {
        for (Connector connector : connectors) {
            if (connector.getMetaDataId() == metaDataId) {
                return connector;
            }
        }

        return null;
    }

    public Map<String, Object[]> getConnectorStateMap() {
        return new HashMap<String, Object[]>(connectorStateMap);
    }

    public synchronized LinkedList<String[]> getChannelLog(Object object, String sessionId) {
        String channelName;
        LinkedList<String[]> channelLog;

        if (object == null) {
            /*
             * object is null - no channel is selected. return the latest
             * entire log entries of all channels combined. ONLY new
             * entries.
             */
            channelName = "No Channel Selected";
            channelLog = entireConnectorInfoLogs;
        } else {
            // object is not null - a channel is selected. return the latest
            // (LOG_SIZE) of that particular channel.
            channelName = object.toString();
            // return only the newly added log entries for the client with
            // matching sessionId.
            channelLog = connectorInfoLogs.get(channelName);

            if (channelLog == null) {
                channelLog = new LinkedList<String[]>();
                connectorInfoLogs.put(channelName, channelLog);
            }
        }

        Map<String, Long> lastDisplayedLogIdByChannel;

        if (lastDisplayedLogIndexBySessionId.containsKey(sessionId)) {
            // client exist with the sessionId.
            lastDisplayedLogIdByChannel = lastDisplayedLogIndexBySessionId.get(sessionId);

            if (lastDisplayedLogIdByChannel.containsKey(channelName)) {
                // existing channel on an already open client.
                // -> only display new log entries.
                long lastDisplayedLogId = lastDisplayedLogIdByChannel.get(channelName);
                LinkedList<String[]> newChannelLogEntries = new LinkedList<String[]>();

                // FYI, channelLog.size() will never be larger than LOG_SIZE
                // = 1000.
                for (String[] aChannelLog : channelLog) {
                    if (lastDisplayedLogId < Long.parseLong(aChannelLog[0])) {
                        newChannelLogEntries.addLast(aChannelLog);
                    }
                }

                if (newChannelLogEntries.size() > 0) {
                    /*
                     * put the lastDisplayedLogId into the HashMap. index 0
                     * is the most recent entry, and index0 of that entry
                     * contains the logId.
                     */
                    lastDisplayedLogIdByChannel.put(channelName, Long.parseLong(newChannelLogEntries.get(0)[0]));
                    lastDisplayedLogIndexBySessionId.put(sessionId, lastDisplayedLogIdByChannel);
                }

                try {
                    return SerializationUtils.clone(newChannelLogEntries);
                } catch (SerializationException e) {
                    logger.error(e);
                }
            } else {
                /*
                 * new channel viewing on an already open client. -> all log
                 * entries are new. display them all. put the
                 * lastDisplayedLogId into the HashMap. index0 is the most
                 * recent entry, and index0 of that entry object contains
                 * the logId.
                 */
                if (channelLog.size() > 0) {
                    lastDisplayedLogIdByChannel.put(channelName, Long.parseLong(channelLog.get(0)[0]));
                    lastDisplayedLogIndexBySessionId.put(sessionId, lastDisplayedLogIdByChannel);
                }

                try {
                    return SerializationUtils.clone(channelLog);
                } catch (SerializationException e) {
                    logger.error(e);
                }
            }

        } else {
            // brand new client.
            // thus also new channel viewing.
            // -> all log entries are new. display them all.
            lastDisplayedLogIdByChannel = new HashMap<String, Long>();

            if (channelLog.size() > 0) {
                lastDisplayedLogIdByChannel.put(channelName, Long.parseLong(channelLog.get(0)[0]));
            } else {
                // no log exist at all. put the currentLogId-1, which is the
                // very latest logId.
                lastDisplayedLogIdByChannel.put(channelName, logId - 1);
            }

            lastDisplayedLogIndexBySessionId.put(sessionId, lastDisplayedLogIdByChannel);

            try {
                return SerializationUtils.clone(channelLog);
            } catch (SerializationException e) {
                logger.error(e);
            }
        }

        return null;
    }

    public Object channelDeployed(String sessionId) {
        if (channelsDeployedFlagForEachClient.containsKey(sessionId)) {
            // sessionId found. no (re)deploy occurred.
            return false;
        } else {
            // no sessionId found, which means channels have just been
            // (re)deployed - clear out all clients' Dashboard Connector
            // Logs.
            channelsDeployedFlagForEachClient.put(sessionId, true);
            return true;
        }
    }

    public Object removeSession(String sessionId) {
        // client shut down, or user logged out -> remove everything
        // involving this sessionId.
        if (lastDisplayedLogIndexBySessionId.containsKey(sessionId)) {
            lastDisplayedLogIndexBySessionId.remove(sessionId);
        }

        if (channelsDeployedFlagForEachClient.containsKey(sessionId)) {
            channelsDeployedFlagForEachClient.remove(sessionId);
        }

        return null;
    }

    public Color getColor(ConnectorEventType type) {
        switch (type) {
            case IDLE:
            case CONNECTING:
            case WAITING_FOR_RESPONSE:
                return Color.yellow;

            case WRITING:
            case SENDING:
            case READING:
            case RECEIVING:
            case POLLING:
            case CONNECTED:
                return Color.green;

            case DISCONNECTED:
                return Color.red;

            case INFO:
                return Color.blue;

            default:
                return Color.black;
        }
    }

}
