/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.dashboardstatus;

import java.net.Socket;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.connectors.doc.DocumentWriterProperties;
import com.mirth.connect.connectors.email.EmailSenderProperties;
import com.mirth.connect.connectors.file.FileWriterProperties;
import com.mirth.connect.connectors.http.HttpSenderProperties;
import com.mirth.connect.connectors.jdbc.DatabaseWriterProperties;
import com.mirth.connect.connectors.jms.JMSWriterProperties;
import com.mirth.connect.connectors.mllp.LLPSenderProperties;
import com.mirth.connect.connectors.tcp.TCPSenderProperties;
import com.mirth.connect.connectors.vm.ChannelWriterProperties;
import com.mirth.connect.connectors.ws.WebServiceSenderProperties;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.ExtensionPermission;
import com.mirth.connect.plugins.ServicePlugin;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;

public class DashboardConnectorStatusMonitor implements ServicePlugin {
    private Logger logger = Logger.getLogger(this.getClass());

    private static final String PLUGIN_NAME = "Dashboard Connector Status Monitor";

    private static final String COLOR_BLACK = "black";
    private static final String COLOR_YELLOW = "yellow";
    private static final String COLOR_GREEN = "green";
    private static final String COLOR_RED = "red";

    private static final String STATE_UNKNOWN = "Unknown";
    private static final String STATE_IDLE = "Idle";
    private static final String STATE_RECEIVING = "Receiving";
    private static final String STATE_READING = "Reading";
    private static final String STATE_POLLING = "Polling";
    private static final String STATE_NOT_POLLING = "Not Polling";
    private static final String STATE_WRITING = "Writing";
    private static final String STATE_SENDING = "Sending";
    private static final String STATE_WAITING = "Waiting";
    private static final String STATE_CONNECTED = "Connected";
    private static final String STATE_DISCONNECTED = "Disconnected";
    private static final String STATE_ATTEMPTING = "Attempting to Connect";
    private static final String STATE_NO_SELECTION = "No Channel Selected";

    private static final String METHOD_GET_STATES = "getStates";
    private static final String METHOD_GET_CONNECTION_INFO_LOGS = "getConnectionInfoLogs";
    private static final String METHOD_REMOVE_SESSIONID = "removeSessionId";
    private static final String METHOD_CHANNELS_DEPLOYED = "channelsDeployed";

    private HashMap<String, String[]> connectorStateMap;
    private HashMap<String, Set<Socket>> socketSetMap;
    private ConcurrentHashMap<String, LinkedList<String[]>> connectorInfoLogs;
    private LinkedList<String[]> entireConnectorInfoLogs;
    private ConcurrentHashMap<String, HashMap<String, Long>> lastDisplayedLogIndexBySessionId = new ConcurrentHashMap<String, HashMap<String, Long>>();
    // maximum log size for each channel. and for entire logs.
    private static final int MAX_LOG_SIZE = 1000;
    private static long logId = 1;
    // stores channelsJustBeenDeployedFlag for each session (client). this flag
    // is used to signal clients to clear out all the Dashboard Monitoring Logs.
    private ConcurrentHashMap<String, Boolean> channelsDeployedFlagForEachClient = new ConcurrentHashMap<String, Boolean>();

    @Override
    public void init(Properties properties) {
        socketSetMap = new HashMap<String, Set<Socket>>();
        connectorStateMap = new HashMap<String, String[]>();
        connectorInfoLogs = new ConcurrentHashMap<String, LinkedList<String[]>>();
        entireConnectorInfoLogs = new LinkedList<String[]>();

        channelsDeployedFlagForEachClient.clear();
    }

    @Override
    public void onDeploy() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void update(Properties properties) {

    }

    public void updateStatus(String connectorId, ConnectorType type, Event event, Socket socket) {
        String stateImage = COLOR_BLACK;
        String stateText = STATE_UNKNOWN;
        boolean updateStatus = true;

        switch (event) {
            case INITIALIZED:
                switch (type) {
                    case LISTENER:
                        stateImage = COLOR_YELLOW;
                        stateText = STATE_WAITING;
                        break;
                    case READER:
                        stateImage = COLOR_YELLOW;
                        stateText = STATE_IDLE;
                        break;
                }
                break;
            case CONNECTED:
                switch (type) {
                    case LISTENER:
                        if (socket != null) {
                            addConnectionToSocketSet(socket, connectorId);
                            stateImage = COLOR_GREEN;
                            stateText = STATE_CONNECTED + " (" + getSocketSetCount(connectorId) + ")";
                        } else {
                            stateImage = COLOR_GREEN;
                            stateText = STATE_CONNECTED;
                        }
                        break;
                    case READER:
                        stateImage = COLOR_GREEN;
                        stateText = STATE_POLLING;
                        break;
                }
                break;
            case DISCONNECTED:
                switch (type) {
                    case LISTENER:
                        if (socket != null) {
                            removeConnectionInSocketSet(socket, connectorId);
                            int connectedSockets = getSocketSetCount(connectorId);
                            if (connectedSockets == 0) {
                                stateImage = COLOR_YELLOW;
                                stateText = STATE_WAITING;
                            } else {
                                stateImage = COLOR_GREEN;
                                stateText = STATE_CONNECTED + " (" + connectedSockets + ")";
                            }
                        } else {
                            clearSocketSet(connectorId);
                            stateImage = COLOR_RED;
                            stateText = STATE_DISCONNECTED;
                        }
                        break;
                    case READER:
                        stateImage = COLOR_RED;
                        stateText = STATE_NOT_POLLING;
                        break;
                    case WRITER:
                        stateImage = COLOR_RED;
                        stateText = STATE_DISCONNECTED;
                        break;
                    case SENDER:
                        stateImage = COLOR_RED;
                        stateText = STATE_DISCONNECTED;
                        break;
                }
                break;
            case BUSY:
                switch (type) {
                    case READER:
                        stateImage = COLOR_GREEN;
                        stateText = STATE_READING;
                        break;
                    case LISTENER:
                        stateImage = COLOR_GREEN;
                        stateText = STATE_RECEIVING;
                        break;
                    case WRITER:
                        stateImage = COLOR_YELLOW;
                        stateText = STATE_WRITING;
                        break;
                    case SENDER:
                        stateImage = COLOR_YELLOW;
                        stateText = STATE_SENDING;
                        break;
                }
                break;
            case DONE:
                switch (type) {
                    case READER:
                        stateImage = COLOR_YELLOW;
                        stateText = STATE_IDLE;
                        break;
                    case LISTENER:
                        if (socket != null) {
                            stateImage = COLOR_GREEN;
                            stateText = STATE_CONNECTED + " (" + getSocketSetCount(connectorId) + ")";
                        } else {
                            stateImage = COLOR_YELLOW;
                            stateText = STATE_WAITING;
                        }
                        break;
                }
                break;
            case ATTEMPTING:
                switch (type) {
                    case WRITER:
                        stateImage = COLOR_YELLOW;
                        stateText = STATE_ATTEMPTING;
                        break;
                    case SENDER:
                        stateImage = COLOR_YELLOW;
                        stateText = STATE_ATTEMPTING;
                        break;
                }
                break;
            default:
                updateStatus = false;
                break;
        }

        if (updateStatus) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

            String channelName = "";
            // this will be overwritten down below. If not, something's wrong.
            String connectorType = type.toString();
            String information = "";

            /*
             * check 'connectorId' - contains destination_1_connector, etc.
             * connectorId consists of id_source_connector for sources, and
             * id_destination_x_connector for destinations. i.e. tokenCount will
             * be 3 for sources and 4 for destinations. Note that READER and
             * LISTENER are sources, and WRITER and SENDER are destinations.
             */
            StringTokenizer tokenizer = new StringTokenizer(connectorId, "_");
            String channelId = tokenizer.nextToken();
            int destinationIndex;
            LinkedList<String[]> channelLog = null;

            Channel channel = ControllerFactory.getFactory().createChannelController().getDeployedChannelById(channelId);

            if (channel != null) {
                channelName = channel.getName();
                // grab the channel's log from the HashMap, if not exist, create
                // one.
                if (connectorInfoLogs.containsKey(channelName)) {
                    channelLog = connectorInfoLogs.get(channelName);
                } else {
                    channelLog = new LinkedList<String[]>();
                }

                Connector connector = null;

                switch (type) {
                    case READER:
                        connectorType = "Source: " + channel.getSourceConnector().getTransportName() + "  (" + channel.getSourceConnector().getTransformer().getInboundProtocol().toString() + " -> " + channel.getSourceConnector().getTransformer().getOutboundProtocol().toString() + ")";
                        break;
                    case LISTENER:
                        connectorType = "Source: " + channel.getSourceConnector().getTransportName() + "  (" + channel.getSourceConnector().getTransformer().getInboundProtocol().toString() + " -> " + channel.getSourceConnector().getTransformer().getOutboundProtocol().toString() + ")";
                        break;
                    case WRITER:
                        tokenizer.nextToken();
                        // destinationId begins from 1, so subtract by 1 for the
                        // arrayIndex.
                        destinationIndex = Integer.valueOf(tokenizer.nextToken()) - 1;
                        connector = channel.getDestinationConnectors().get(destinationIndex);
                        connectorType = "Destination: " + connector.getTransportName() + " - " + connector.getName();

                        if (connector.getTransportName().equals(FileWriterProperties.name)) {
                            // Destination - File Writer.
                            switch (event) {
                                case BUSY:
                                    information = FileWriterProperties.getInformation(connector.getProperties());
                                    break;
                            }
                        } else if (connector.getTransportName().equals(DatabaseWriterProperties.name)) {
                            // Destination - Database Writer.
                            information = DatabaseWriterProperties.getInformation(connector.getProperties());
                        } else if (connector.getTransportName().equals(JMSWriterProperties.name)) {
                            // Destination - JMS Writer.
                            information = JMSWriterProperties.getInformation(connector.getProperties());
                        } else if (connector.getTransportName().equals(DocumentWriterProperties.name)) {
                            // Destination - Document Writer.
                            information = DocumentWriterProperties.getInformation(connector.getProperties());
                        }
                        break;
                    case SENDER:
                        tokenizer.nextToken();
                        // destinationId begins from 1, so subtract by 1 for the
                        // arrayIndex.
                        destinationIndex = Integer.valueOf(tokenizer.nextToken()) - 1;
                        connector = channel.getDestinationConnectors().get(destinationIndex);
                        connectorType = "Destination: " + connector.getTransportName() + " - " + connector.getName();

                        if (connector.getTransportName().equals(HttpSenderProperties.name)) {
                            // Destination - HTTP Sender.
                            information = HttpSenderProperties.getInformation(connector.getProperties());
                        } else if (connector.getTransportName().equals(ChannelWriterProperties.name)) {
                            // Destination - Channel Writer.
                            Channel targetChannel = ControllerFactory.getFactory().createChannelController().getDeployedChannelById(ChannelWriterProperties.getInformation(connector.getProperties()));

                            if (targetChannel == null) {
                                information = "Target Channel: None";
                            } else {
                                information = "Target Channel: " + targetChannel.getName();
                            }
                        } else if (connector.getTransportName().equals(EmailSenderProperties.name)) {
                            // Destination - Email Sender.
                            information = EmailSenderProperties.getInformation(connector.getProperties());
                        } else if (connector.getTransportName().equals(TCPSenderProperties.name)) {
                            // Destination - TCP Sender.
                            // The useful info for TCP Sender - host:port will
                            // be taken care of by the socket below.
                        } else if (connector.getTransportName().equals(LLPSenderProperties.name)) {
                            // Destination - LLP Sender.
                            // The useful info for LLP Sender - host:port will
                            // be taken care of by the socket below.
                        } else if (connector.getTransportName().equals(WebServiceSenderProperties.name)) {
                            // Destination - Web Service Sender.
                            // information = "";
                        }
                        break;
                }
            }

            if (socket != null) {
                String sendingAddress = socket.getLocalAddress().toString() + ":" + socket.getLocalPort();
                String receivingAddress = socket.getInetAddress().toString() + ":" + socket.getPort();

                // If addresses begin with a slash "/", remove it.
                if (sendingAddress.startsWith("/")) {
                    sendingAddress = sendingAddress.substring(1);
                }

                if (receivingAddress.startsWith("/")) {
                    receivingAddress = receivingAddress.substring(1);
                }

                information += "Sender: " + sendingAddress + "  Receiver: " + receivingAddress;
            }

            if (channelLog != null) {
                synchronized (this) {
                    if (channelLog.size() == MAX_LOG_SIZE) {
                        channelLog.removeLast();
                    }
                    channelLog.addFirst(new String[] { String.valueOf(logId), channelName, dateFormat.format(timestamp), connectorType, event.toString(), information });

                    if (entireConnectorInfoLogs.size() == MAX_LOG_SIZE) {
                        entireConnectorInfoLogs.removeLast();
                    }
                    entireConnectorInfoLogs.addFirst(new String[] { String.valueOf(logId), channelName, dateFormat.format(timestamp), connectorType, event.toString(), information });

                    logId++;

                    // put the channel log into the HashMap.
                    connectorInfoLogs.put(channelName, channelLog);
                }
            }

            connectorStateMap.put(connectorId, new String[] { stateImage, stateText });
        }
    }

    private synchronized void addConnectionToSocketSet(Socket socket, String connectorId) {
        if (socket != null) {
            Set<Socket> socketSet = socketSetMap.get(connectorId);

            if (socketSet == null) {
                socketSet = new HashSet<Socket>();
                socketSetMap.put(connectorId, socketSet);
            }

            socketSet.add(socket);
        }
    }

    private synchronized void removeConnectionInSocketSet(Socket socket, String connectorId) {
        if (socket != null) {
            Set<Socket> socketSet = socketSetMap.get(connectorId);

            if (socketSet != null) {
                socketSet.remove(socket);
            }
        }
    }

    private synchronized void clearSocketSet(String connectorId) {
        socketSetMap.remove(connectorId);
    }

    private int getSocketSetCount(String connectorId) {
        Set<Socket> socketSet = socketSetMap.get(connectorId);

        if (socketSet == null) {
            return 0;
        } else {
            return socketSet.size();
        }
    }

    public Properties getDefaultProperties() {
        return new Properties();
    }

    public synchronized Object invoke(String method, Object object, String sessionId) {
        if (method.equals(METHOD_GET_STATES)) {
            return connectorStateMap;
        } else if (method.equals(METHOD_GET_CONNECTION_INFO_LOGS)) {
            String channelName;
            LinkedList<String[]> channelLog;

            if (object == null) {
                /*
                 * object is null - no channel is selected. return the latest
                 * entire log entries of all channels combined. ONLY new
                 * entries.
                 */
                channelName = STATE_NO_SELECTION;
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

            HashMap<String, Long> lastDisplayedLogIdByChannel;

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

        } else if (method.equals(METHOD_CHANNELS_DEPLOYED)) {
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
        } else if (method.equals(METHOD_REMOVE_SESSIONID)) {
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

        return null;
    }

    @Override
    public ExtensionPermission[] getExtensionPermissions() {
        ExtensionPermission viewPermission = new ExtensionPermission(PLUGIN_NAME, "View Connection Status", "Displays the connection status and history of the selected channel on the Dashboard.", new String[] { METHOD_GET_STATES, METHOD_GET_CONNECTION_INFO_LOGS }, new String[] { });
        
        return new ExtensionPermission[] { viewPermission };
    }
}