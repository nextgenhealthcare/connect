package com.webreach.mirth.plugins.dashboardstatus;

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

import org.apache.log4j.Logger;

import com.webreach.mirth.connectors.doc.DocumentWriterProperties;
import com.webreach.mirth.connectors.email.EmailSenderProperties;
import com.webreach.mirth.connectors.file.FileWriterProperties;
import com.webreach.mirth.connectors.http.HTTPSenderProperties;
import com.webreach.mirth.connectors.jdbc.DatabaseWriterProperties;
import com.webreach.mirth.connectors.jms.JMSWriterProperties;
import com.webreach.mirth.connectors.mllp.LLPSenderProperties;
import com.webreach.mirth.connectors.soap.SOAPSenderProperties;
import com.webreach.mirth.connectors.tcp.TCPSenderProperties;
import com.webreach.mirth.connectors.vm.ChannelWriterProperties;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.converters.ObjectCloner;
import com.webreach.mirth.model.converters.ObjectClonerException;
import com.webreach.mirth.plugins.ServerPlugin;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.MonitoringController.ConnectorType;
import com.webreach.mirth.server.controllers.MonitoringController.Event;

public class DashboardConnectorStatusMonitor implements ServerPlugin
{
    private Logger logger = Logger.getLogger(this.getClass());
    private static final String UNKNOWN = "Unknown";
	private static final String BLACK = "black";
	private static final String IDLE = "Idle";
	private static final String RECEIVING = "Receiving";
	private static final String READING = "Reading";
	private static final String POLLING = "Polling";
	private static final String NOT_POLLING = "Not Polling";
    private static final String WRITING = "Writing";
    private static final String SENDING = "Sending";
    private static final String YELLOW = "yellow";
	private static final String GREEN = "green";
	private static final String RED = "red";
	private static final String WAITING = "Waiting";
	private static final String CONNECTED = "Connected";
	private static final String DISCONNECTED = "Disconnected";
    private static final String ATTEMPTING_TO_CONNECT = "Attempting to Connect";
	private static final String GET_STATES = "getStates";
    private static final String GET_CONNECTION_INFO_LOGS = "getConnectionInfoLogs";
    private static final String REMOVE_SESSIONID = "removeSessionId";
    private static final String CHANNELS_DEPLOYED = "channelsDeployed";
    private static final String NO_CHANNEL_SELECTED = "No Channel Selected";
    private HashMap<String, String[]> currentStates;
	private HashMap<String, Set<Socket>> socketSets;
    private ConcurrentHashMap<String, LinkedList<String[]>> connectorInfoLogs;
    private LinkedList<String[]> entireConnectorInfoLogs;
    private ConcurrentHashMap<String, HashMap<String, Long>> lastDisplayedLogIndexBySessionId = new ConcurrentHashMap<String, HashMap<String, Long>>();
    private static final int LOG_SIZE = 1000;        // maximum log size for each channel. and for entire logs.
    private static long logId = 1;
    private ConcurrentHashMap<String, Boolean> channelsDeployedFlagForEachClient = new ConcurrentHashMap<String, Boolean>();  // stores channelsJustBeenDeployed Flag for each session (client). this flag is used to signal clients to clear out all the Dashboard Monitoring Logs.


    public void updateStatus(String connectorId, ConnectorType type, Event event, Socket socket) {

		String stateImage = BLACK;
		String statusText = UNKNOWN;
        boolean updateStatus = false;

        switch (event) {
			case INITIALIZED:
				switch (type) {
					case LISTENER:
						stateImage = YELLOW;
						statusText = WAITING;
                        updateStatus = true;
                        break;
					case READER:
						stateImage = YELLOW;
						statusText = IDLE;
                        updateStatus = true;
                        break;
				}
                break;
			case CONNECTED:
				switch (type) {
					case LISTENER:
						if (socket != null){
							addConnectionToSocketSet(socket, connectorId);
							stateImage = GREEN;
							statusText = CONNECTED + " (" + getSocketSetCount(connectorId) + ")";
						} else {
							stateImage = GREEN;
							statusText = CONNECTED;
						}
                        updateStatus = true;
                        break;
					case READER:
						stateImage = GREEN;
						statusText = POLLING;
                        updateStatus = true;
                        break;
				}
                break;
			case DISCONNECTED:
				switch (type) {
					case LISTENER:
						if (socket != null){
							removeConnectionInSocketSet(socket, connectorId);
							int connectedSockets = getSocketSetCount(connectorId);
							if (connectedSockets == 0){
								stateImage = YELLOW;
								statusText = WAITING;
							}else{
								stateImage = GREEN;
								statusText = CONNECTED + " (" + connectedSockets + ")";
							}
						}else{
							clearSocketSet(connectorId);
							stateImage = RED;
							statusText = DISCONNECTED;
						}
                        updateStatus = true;
                        break;
					case READER:
						stateImage = RED;
						statusText = NOT_POLLING;
                        updateStatus = true;
                        break;
					case WRITER:
						stateImage = RED;
						statusText = DISCONNECTED;
                        updateStatus = true;
                        break;
					case SENDER:
                        stateImage = RED;
						statusText = DISCONNECTED;
                        updateStatus = true;
                        break;
                }
                break;
			case BUSY:
				switch (type) {
					case READER:
						stateImage = GREEN;
						statusText = READING;
                        updateStatus = true;
                        break;
					case LISTENER:
						stateImage = GREEN;
						statusText = RECEIVING;
                        updateStatus = true;
                        break;
                    case WRITER:
						stateImage = YELLOW;
						statusText = WRITING;
                        updateStatus = true;
                        break;
					case SENDER:
                        stateImage = YELLOW;
						statusText = SENDING;
                        updateStatus = true;
                        break;
                }
                break;
			case DONE:
				switch (type) {
					case READER:
						stateImage = YELLOW;
						statusText = IDLE;
                        updateStatus = true;
                        break;
					case LISTENER:
						if (socket != null) {
							stateImage = GREEN;
							statusText = CONNECTED + " (" + getSocketSetCount(connectorId) + ")";
						} else {
							stateImage = YELLOW;
							statusText = WAITING;
						}
                        updateStatus = true;
                        break;
                }
                break;
            case ATTEMPTING_TO_CONNECT:
                switch (type) {
                    case WRITER:
						stateImage = YELLOW;
						statusText = ATTEMPTING_TO_CONNECT;
                        updateStatus = true;
                        break;
					case SENDER:
                        stateImage = YELLOW;
						statusText = ATTEMPTING_TO_CONNECT;
                        updateStatus = true;
                        break;
                }
                break;
            default:
				break;
		}

        if (updateStatus) {

            Timestamp ts = new Timestamp(System.currentTimeMillis());
            SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

            String channelName = "";
            String connectorType = type.toString();     // this will be overwritten down below. If not, something's wrong.
            String information = "";

            // check 'connectorId' - contains destination_1_connector, etc.
            // connectorId consists of id_source_connector for sources, and id_destination_x_connector for destinations.
            // i.e. tokenCount will be 3 for sources and 4 for destinations.
            // Note that READER and LISTENER are sources, and WRITER and SENDER are destinations.
            StringTokenizer st = new StringTokenizer(connectorId, "_");
            String channelId = st.nextToken();
            int destinationIndex;

            LinkedList<String[]> channelLog = null;

            HashMap<String, Channel> channelsFromCache = ControllerFactory.getFactory().createChannelController().getChannelCache();    // HashMap(ChannelID, Channel)

            if (channelsFromCache.containsKey(channelId)) { //  redundant check as the channelId MUST exist in the channelCache. but just for a safety measure...

                Channel channel = channelsFromCache.get(channelId);

                channelName = channel.getName();

                // grab the channel's log from the HashMap, if not exist, create one.
                if (connectorInfoLogs.containsKey(channelName)) {
                    channelLog = connectorInfoLogs.get(channelName);
                } else {
                    channelLog = new LinkedList<String[]>();
                }

                Connector connector;

                switch (type) {
                    case READER:
                        connectorType = "Source: " + channel.getSourceConnector().getTransportName() + "  (" + channel.getSourceConnector().getTransformer().getInboundProtocol().toString() + " -> " + channel.getSourceConnector().getTransformer().getOutboundProtocol().toString() + ")";
                        break;
                    case LISTENER:
                        connectorType = "Source: " + channel.getSourceConnector().getTransportName() + "  (" + channel.getSourceConnector().getTransformer().getInboundProtocol().toString() + " -> " + channel.getSourceConnector().getTransformer().getOutboundProtocol().toString() + ")";
                        break;
                    case WRITER:
                        st.nextToken();
                        destinationIndex = Integer.valueOf(st.nextToken()) - 1;     // destinationId begins from 1, so subtract by 1 for the arrayIndex.
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
                        st.nextToken();
                        destinationIndex = Integer.valueOf(st.nextToken()) - 1;     // destinationId begins from 1, so subtract by 1 for the arrayIndex.
                        connector = channel.getDestinationConnectors().get(destinationIndex);
                        connectorType = "Destination: " + connector.getTransportName() + " - " + connector.getName();
                        if (connector.getTransportName().equals(HTTPSenderProperties.name)) {
                            // Destination - HTTP Sender.
                            information = HTTPSenderProperties.getInformation(connector.getProperties());
                        } else if (connector.getTransportName().equals(ChannelWriterProperties.name)) {
                            // Destination - Channel Writer.
                        	Channel cachedChannel = ControllerFactory.getFactory().createChannelController().getChannelCache().get(ChannelWriterProperties.getInformation(connector.getProperties()));
                        	if (cachedChannel == null) {
                        		information = "Target Channel: None";
                        	} else {
                        		information = "Target Channel: " + cachedChannel.getName();
                        	}
                        } else if (connector.getTransportName().equals(EmailSenderProperties.name)) {
                            // Destination - Email Sender.
                            information = EmailSenderProperties.getInformation(connector.getProperties());
                        } else if (connector.getTransportName().equals(TCPSenderProperties.name)) {
                            // Destination - TCP Sender.
                            // The useful info for TCP Sender - host:port will be taken care of by the socket below.
                        } else if (connector.getTransportName().equals(LLPSenderProperties.name)) {
                            // Destination - LLP Sender.
                            // The useful info for LLP Sender - host:port will be taken care of by the socket below.
                        } else if (connector.getTransportName().equals(SOAPSenderProperties.name)) {
                            // Destination - SOAP Sender.
                            // information = "";
                        }
                        break;
                }
            }

            if (socket != null) {

                String sendingIP = socket.getLocalAddress().toString() + ":" + socket.getLocalPort();
                String receivingIP = socket.getInetAddress().toString() + ":" + socket.getPort();

                // If addresses begin with a slash "/", remove it.
                if (sendingIP.startsWith("/")) {
                    sendingIP = sendingIP.substring(1);
                }
                if (receivingIP.startsWith("/")) {
                    receivingIP = receivingIP.substring(1);
                }

                information += "Sender: " + sendingIP + "  Receiver: " + receivingIP;
            }

            if (channelLog != null) {
                synchronized(this) {
                    if (channelLog.size() == LOG_SIZE) {
                        channelLog.removeLast();
                    }
                    channelLog.addFirst(new String[] { String.valueOf(logId), channelName, ft.format(ts), connectorType, event.toString(), information });

                    if (entireConnectorInfoLogs.size() == LOG_SIZE) {
                        entireConnectorInfoLogs.removeLast();
                    }
                    entireConnectorInfoLogs.addFirst(new String[] { String.valueOf(logId), channelName, ft.format(ts), connectorType, event.toString(), information });

                    logId++;

                    // put the channel log into the HashMap.
                    connectorInfoLogs.put(channelName, channelLog);
                }
            }

            this.currentStates.put(connectorId, new String[]{stateImage, statusText});
        }
    }

	private synchronized void addConnectionToSocketSet(Socket socket, String connectorId) {
		if (socket != null){
			Set<Socket> socketSet = socketSets.get(connectorId);
			if (socketSet == null){
				socketSet = new HashSet<Socket>();
				socketSets.put(connectorId, socketSet);
			}
			socketSet.add(socket);
		}
	}
	
	private synchronized void removeConnectionInSocketSet(Socket socket, String connectorId) {
		if (socket != null){
			Set socketSet = socketSets.get(connectorId);
			if (socketSet != null){
				socketSet.remove(socket);
			}
		}
	}
	
	private synchronized void clearSocketSet(String connectorId) {
		socketSets.remove(connectorId);
	}
	
	private int getSocketSetCount(String connectorId){
		Set socketSet = socketSets.get(connectorId);
		if (socketSet == null){
			return 0;
		}else{
			return socketSet.size();
		}
	}

	public Properties getDefaultProperties() {
		return null;
	}

	public void init(Properties properties) {
		initialize();
	}

	private void initialize() {
		this.socketSets = new HashMap<String, Set<Socket>>();
		this.currentStates = new HashMap<String, String[]>();
        this.connectorInfoLogs = new ConcurrentHashMap<String, LinkedList<String[]>>();
        this.entireConnectorInfoLogs = new LinkedList<String[]>();
        this.channelsDeployedFlagForEachClient.clear();
    }

	public synchronized Object invoke(String method, Object object, String sessionId) {
		if (method.equals(GET_STATES)) {
			return this.currentStates;
		} else if (method.equals(GET_CONNECTION_INFO_LOGS)) {

            String channelName;
            LinkedList<String[]> channelLog;

            if (object == null) {
                // object is null - no channel is selected.  return the latest entire log entries of all channels combined.  ONLY new entries.
                channelName = NO_CHANNEL_SELECTED;
                channelLog = entireConnectorInfoLogs;
            } else {
                // object is not null - a channel is selected.  return the latest (LOG_SIZE) of that particular channel.
                channelName = object.toString();
                // return only the newly added log entries for the client with matching sessionId.
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

                    // FYI, channelLog.size() will never be larger than LOG_SIZE = 1000.
                    for (String[] aChannelLog : channelLog) {
                        if (lastDisplayedLogId < Long.parseLong(aChannelLog[0])) {
                            newChannelLogEntries.addLast(aChannelLog);
                        }
                    }

                    if (newChannelLogEntries.size() > 0) {
                        // put the lastDisplayedLogId into the HashMap. index 0 is the most recent entry, and index0 of that entry contains the logId.
                        lastDisplayedLogIdByChannel.put(channelName, Long.parseLong(newChannelLogEntries.get(0)[0]));
                        lastDisplayedLogIndexBySessionId.put(sessionId, lastDisplayedLogIdByChannel);
                    }

                    try {
                        return ObjectCloner.deepCopy(newChannelLogEntries);
                    } catch (ObjectClonerException oce) {
                        logger.error("Error: DashboardConnectorStatusMonitor.java", oce);
                    }

                } else {
                    // new channel viewing on an already open client.
                    // -> all log entries are new. display them all.
                    // put the lastDisplayedLogId into the HashMap.  index0 is the most recent entry, and index0 of that entry object contains the logId.
                    if (channelLog.size() > 0) {
                        lastDisplayedLogIdByChannel.put(channelName, Long.parseLong(channelLog.get(0)[0]));
                        lastDisplayedLogIndexBySessionId.put(sessionId, lastDisplayedLogIdByChannel);
                    }

                    try {
                        return ObjectCloner.deepCopy(channelLog);
                    } catch (ObjectClonerException oce) {
                        logger.error("Error: DashboardConnectorStatusMonitor.java", oce);
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
                    // no log exist at all. put the currentLogId-1, which is the very latest logId.
                    lastDisplayedLogIdByChannel.put(channelName, logId-1);
                }

                lastDisplayedLogIndexBySessionId.put(sessionId, lastDisplayedLogIdByChannel);
                try {
                    return ObjectCloner.deepCopy(channelLog);
                } catch (ObjectClonerException oce) {
                    logger.error("Error: DashboardConnectorStatusMonitor.java", oce);
                }
            }

        } else if (method.equals(CHANNELS_DEPLOYED)) {
            
            if (channelsDeployedFlagForEachClient.containsKey(sessionId)) {
                // sessionId found. no (re)deploy occurred.
                return false;
            } else {
                // no sessionId found, which means channels have just been (re)deployed - clear out all clients' Dashboard Connector Logs.
                channelsDeployedFlagForEachClient.put(sessionId, true);
                return true;
            }

        } else if (method.equals(REMOVE_SESSIONID)) {
            // client shut down, or user logged out -> remove everything involving this sessionId.
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

    public void onDeploy() {
		initialize();        
    }

	public void start() {

	}

	public void stop() {

    }

	public void update(Properties properties) {

	}

}
