package com.mirth.connect.plugins.dashboardstatus;

import java.awt.Color;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;
import com.mirth.connect.donkey.model.event.Event;
import com.mirth.connect.donkey.server.event.ConnectionStatusEvent;
import com.mirth.connect.donkey.server.event.ConnectorCountEvent;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;

public class DefaultConnectionLogController extends ConnectionStatusLogController {

    private Logger logger = Logger.getLogger(this.getClass());

    private static final int MAX_LOG_SIZE = 1000;
    private static long logId = 1;
    private Map<String, Object[]> connectorStateMap = new ConcurrentHashMap<String, Object[]>();
    private Map<String, ConnectionStatusEventType> connectorStateTypeMap = new ConcurrentHashMap<String, ConnectionStatusEventType>();
    private Map<String, AtomicInteger> connectorCountMap = new ConcurrentHashMap<String, AtomicInteger>();
    private Map<String, Integer> maxConnectionMap = new ConcurrentHashMap<String, Integer>();
    private Map<String, LinkedList<ConnectionLogItem>> connectorInfoLogs = new ConcurrentHashMap<>();
    private LinkedList<ConnectionLogItem> entireConnectorInfoLogs = new LinkedList<>();

    @Override
    public void processEvent(Event event) {

        if (event instanceof ConnectionStatusEvent) {
            ConnectionStatusEvent connectionStatusEvent = (ConnectionStatusEvent) event;
            String channelId = connectionStatusEvent.getChannelId();
            Integer metaDataId = connectionStatusEvent.getMetaDataId();
            String information = connectionStatusEvent.getMessage();
            Timestamp timestamp = new Timestamp(event.getDateTime());

            String connectorId = channelId + "_" + metaDataId;

            ConnectionStatusEventType eventType = connectionStatusEvent.getState();
            Integer connectorCount = null;
            Integer maximum = null;

            if (event instanceof ConnectorCountEvent) {
                ConnectorCountEvent connectorCountEvent = (ConnectorCountEvent) connectionStatusEvent;

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
                    eventType = ConnectionStatusEventType.IDLE;
                } else {
                    eventType = ConnectionStatusEventType.CONNECTED;
                }
            }

            String stateString = null;
            if (eventType.isState()) {
                Color color = getColor(eventType);
                stateString = eventType.toString();
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

            LinkedList<ConnectionLogItem> channelLog = null;

            Channel channel = ControllerFactory.getFactory().createChannelController().getDeployedChannelById(channelId);

            if (channel != null) {
                channelName = channel.getName();
                // grab the channel's log from the HashMap, if not exist, create
                // one.
                if (connectorInfoLogs.containsKey(channelId)) {
                    channelLog = connectorInfoLogs.get(channelId);
                } else {
                    channelLog = new LinkedList<>();
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

                    ConnectionLogItem connectionLogItem = new ConnectionLogItem(logId, null, channelId, metaDataId.longValue(), dateFormat.format(timestamp), channelName, connectorType, ((ConnectionStatusEvent) event).getState().toString(), information);
                    channelLog.addFirst(connectionLogItem);

                    if (entireConnectorInfoLogs.size() == MAX_LOG_SIZE) {
                        entireConnectorInfoLogs.removeLast();
                    }
                    entireConnectorInfoLogs.addFirst(connectionLogItem);

                    logId++;

                    // put the channel log into the HashMap.
                    connectorInfoLogs.put(channelId, channelLog);
                }
            }

            if (eventType.isState()) {
                connectorStateTypeMap.put(connectorId, eventType);
            }
        }
    }

    @Override
    public synchronized LinkedList<ConnectionLogItem> getChannelLog(String serverId, String channelId, int fetchSize, Long lastLogId) {
        LinkedList<ConnectionLogItem> channelLog;

        if (channelId == null) {
            /*
             * object is null - no channel is selected. return the latest entire log entries of all
             * channels combined. ONLY new entries.
             */
            channelId = "No Channel Selected";
            channelLog = entireConnectorInfoLogs;
        } else {
            // object is not null - a channel is selected. return the latest
            // (LOG_SIZE) of that particular channel.
            // return only the newly added log entries for the client with
            // matching sessionId.
            channelLog = connectorInfoLogs.get(channelId);

            if (channelLog == null) {
                channelLog = new LinkedList<>();
                connectorInfoLogs.put(channelId, channelLog);
            }
        }

        if (lastLogId != null) {
            LinkedList<ConnectionLogItem> newChannelLogEntries = new LinkedList<>();

            // FYI, channelLog.size() will never be larger than LOG_SIZE
            // = 1000.
            for (ConnectionLogItem aChannelLog : channelLog) {
                if (lastLogId < aChannelLog.getLogId()) {
                    newChannelLogEntries.addLast(aChannelLog);
                }
            }

            try {
                return SerializationUtils.clone(newChannelLogEntries);
            } catch (SerializationException e) {
                logger.error(e);
            }
        } else {
            /*
             * new channel viewing on an already open client. -> all log entries are new. display
             * them all. put the lastDisplayedLogId into the HashMap. index0 is the most recent
             * entry, and index0 of that entry object contains the logId.
             */
            try {
                return SerializationUtils.clone(channelLog);
            } catch (SerializationException e) {
                logger.error(e);
            }
        }

        return null;
    }

    /**
     * Get connector states. Does not use serverId, but is provided for subclasses to use in
     * Clustering.
     * 
     */
    @Override
    public Map<String, Object[]> getConnectorStateMap(String serverId) {
        return new HashMap<String, Object[]>(connectorStateMap);
    }

    @Override
    public Map<String, Map<String, List<ConnectionStateItem>>> getConnectionStatesForServer(String serverId) {
        // serverId is unused for default implementation, as it has no knowledge of clustering. It will use the server id for this server. The Clustering version of this interface will need serverId
        String thisServerId = ConfigurationController.getInstance().getServerId();
        Map<String, List<ConnectionStateItem>> buildMap = new HashMap<>();
        // connectorId is ${channelId} + "_" + ${metadataId}
        for (String connectorId : connectorStateMap.keySet()) {
            String channelId = connectorId.substring(0, connectorId.lastIndexOf('_'));
            String metadataId = connectorId.substring(connectorId.lastIndexOf('_') + 1);
            int connectorCount = connectorCountMap.get(connectorId).get();
            ConnectionStateItem stateItem = new ConnectionStateItem(thisServerId, channelId, metadataId, connectorStateTypeMap.get(connectorId), connectorCount, maxConnectionMap.get(connectorId));

            if (buildMap.containsKey(channelId)) {
                buildMap.get(channelId).add(stateItem);

            } else {
                List<ConnectionStateItem> list = new ArrayList<ConnectionStateItem>();
                list.add(stateItem);
                buildMap.put(channelId, list);
            }

        }
        Map<String, Map<String, List<ConnectionStateItem>>> toReturn = new HashMap<>();
        toReturn.put(thisServerId, buildMap);
        return toReturn;
    }

}
