package com.webreach.mirth.plugins.dashboardstatus;

import java.net.Socket;
import java.util.*;
import java.util.List;
import java.sql.*;
import java.text.SimpleDateFormat;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.plugins.ServerPlugin;
import com.webreach.mirth.server.controllers.ChannelController;
import com.webreach.mirth.server.controllers.MonitoringController.ConnectorType;
import com.webreach.mirth.server.controllers.MonitoringController.Event;
import com.webreach.mirth.connectors.ftp.FTPWriterProperties;
import com.webreach.mirth.connectors.jdbc.DatabaseWriterProperties;
import com.webreach.mirth.connectors.file.FileWriterProperties;
import com.webreach.mirth.connectors.sftp.SFTPWriterProperties;
import com.webreach.mirth.connectors.jms.JMSWriterProperties;
import com.webreach.mirth.connectors.vm.ChannelWriterProperties;
import com.webreach.mirth.connectors.doc.DocumentWriterProperties;
import com.webreach.mirth.connectors.http.HTTPSenderProperties;
import com.webreach.mirth.connectors.email.EmailSenderProperties;
import com.webreach.mirth.connectors.tcp.TCPSenderProperties;
import com.webreach.mirth.connectors.mllp.LLPSenderProperties;
import com.webreach.mirth.connectors.soap.SOAPSenderProperties;
import org.apache.log4j.Logger;

public class DashboardConnectorStatusMonitor implements ServerPlugin
{
    private Logger logger = Logger.getLogger(this.getClass());
    private ChannelController channelController = ChannelController.getInstance();
    private static final String UNKNOWN = "Unknown";
	private static final String BLACK = "black";
	private static final String IDLE = "Idle";
	private static final String RECEIVING = "Receiving";
	private static final String READING = "Reading";
	private static final String POLLING = "Polling";
	private static final String NOT_POLLING = "Not Polling";
	private static final String YELLOW = "yellow";
	private static final String GREEN = "green";
	private static final String RED = "red";
	private static final String WAITING = "Waiting";
	private static final String CONNECTED = "Connected";
	private static final String DISCONNECTED = "Disconnected";
	private static final String GET_STATES = "getStates";
    private static final String GET_CONNECTION_INFO_LOGS = "getConnectionInfoLogs";
	private HashMap<String, String[]> currentStates;
	private HashMap<String, Set<Socket>> socketSets;
    private HashMap<String, LinkedList<String[]>> connectorInfoLogs;
    private static final int LOG_SIZE = 250;        // for each channel.
    
    public void updateStatus(String connectorId, ConnectorType type, Event event, Socket socket) {

        // TODO Auto-generated method stub
		String stateImage = BLACK;
		String statusText = UNKNOWN;
		boolean updateState = false;
		switch (event){
			case INITIALIZED:
				switch (type){
					case LISTENER:
						stateImage = YELLOW;
						statusText = WAITING;
						break;
					case READER:
						stateImage = YELLOW;
						statusText = IDLE;
						break;
				}
				updateState = true;
				break;
			case CONNECTED:
				switch (type){
					case LISTENER:
						if (socket != null){
							addConnectionToSocketSet(socket, connectorId);
							stateImage = GREEN;
							statusText = CONNECTED + " (" + getSocketSetCount(connectorId) + ")";
						} else {
							stateImage = GREEN;
							statusText = CONNECTED;
						}
						break;
					case READER:
						stateImage = GREEN;
						statusText = POLLING;
						break;
				}
				updateState = true;
				break;
			case DISCONNECTED:
				switch (type){
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
						break;
					case READER:
						stateImage = RED;
						statusText = NOT_POLLING;
						break;
				}
				updateState = true;
				break;
			case BUSY:
				switch (type){
					case READER:
						stateImage = GREEN;
						statusText = READING;
						updateState = true;
						break;
					case LISTENER:
						stateImage = GREEN;
						statusText = RECEIVING;
						updateState = true;
						break;
				}
				break;
			case DONE:
				switch (type){
					case READER:
						stateImage = YELLOW;
						statusText = IDLE;
						updateState = true;
						break;
					case LISTENER:
						if (socket != null){
							stateImage = GREEN;
							statusText = CONNECTED + " (" + getSocketSetCount(connectorId) + ")";
							updateState = true;
							break;
						} else {
							stateImage = YELLOW;
							statusText = WAITING;
							updateState = true;
							break;
						}
				}
				break;
			default:
				break;
		
		}

        if (updateState) {

            Timestamp ts = new Timestamp(System.currentTimeMillis());
            SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
            
            String channelName = "";
            String connectorType = type.toString();     // this will be overwritten down below. If not, something's wrong.
            String information = "";
            StringTokenizer st;
            int destinationIndex = 0;
            LinkedList<String[]> channelLog = null;
            
            try
            {
                List<Channel> channels = channelController.getChannel(null);
                Connector connector;

                for (Iterator iter = channels.iterator(); iter.hasNext();) {

                    Channel channel = (Channel) iter.next();

                    if (connectorId.indexOf(channel.getId()) >= 0) {
                        channelName = channel.getName();

                        // grab the channel's log from the HashMap, if not exist, create one.
                        if (connectorInfoLogs.containsKey(channelName)) {
                            channelLog = connectorInfoLogs.get(channelName);
                            // remove now-old-outdated log. The new updated log will be put into the HashMap.
                            connectorInfoLogs.remove(channelName);
                        } else {
                            channelLog = new LinkedList<String[]>();
                        }

                        // check 'connectorId' - contains destination_1_connector, etc.
                        // connectorId consists of id_source_connector for sources, and id_destination_x_connector for destinations.
                        // i.e. tokenCount will be 3 for sources and 4 for destinations.
                        // Note that READER and LISTENER are sources, and WRITER and SENDER are destinations.

                        switch (type) {
                            case READER:
                                connectorType = "Source: " + channel.getSourceConnector().getTransportName() + "  (" + channel.getSourceConnector().getTransformer().getInboundProtocol().toString() + " -> " + channel.getSourceConnector().getTransformer().getOutboundProtocol().toString() + ")";
                                break;
                            case LISTENER:
                                connectorType = "Source: " + channel.getSourceConnector().getTransportName() + "  (" + channel.getSourceConnector().getTransformer().getInboundProtocol().toString() + " -> " + channel.getSourceConnector().getTransformer().getOutboundProtocol().toString() + ")";
                                break;
                            case WRITER:
                                st = new StringTokenizer(connectorId, "_");
                                st.nextToken(); st.nextToken();
                                destinationIndex = Integer.valueOf(st.nextToken()) - 1;     // destinationId begins from 1, so subtract by 1 for the arrayIndex.
                                connector = channel.getDestinationConnectors().get(destinationIndex);
                                connectorType = "Destination: " + connector.getTransportName() + " - " + connector.getName();

                                if (connector.getTransportName().equals(FileWriterProperties.name)) {
                                    // Destination - File Writer.
                                    information = "Result written to: " + connector.getProperties().getProperty(FileWriterProperties.FILE_DIRECTORY) + "/" + connector.getProperties().getProperty(FileWriterProperties.FILE_NAME);
                                } else if (connector.getTransportName().equals(DatabaseWriterProperties.name)) {
                                    // Destination - Database Writer.
                                    information = "URL: " + connector.getProperties().getProperty(DatabaseWriterProperties.DATABASE_URL);
                                } else if (connector.getTransportName().equals(FTPWriterProperties.name)) {
                                    // Destination - FTP Writer.
                                    information = "Result written to: " + connector.getProperties().getProperty(FTPWriterProperties.FTP_URL) + "/" + connector.getProperties().getProperty(FTPWriterProperties.FTP_OUTPUT_PATTERN);
                                    if (connector.getProperties().getProperty(FTPWriterProperties.FTP_FILE_TYPE).equals("0")) {
                                        information += "   File Type: ASCII";
                                    } else {
                                        information += "   File Type: Binary";
                                    }
                                } else if (connector.getTransportName().equals(SFTPWriterProperties.name)) {
                                    // Destination - SFTP Writer.
                                    information = "Result written to: " + connector.getProperties().getProperty(SFTPWriterProperties.SFTP_ADDRESS) + "/" + connector.getProperties().getProperty(SFTPWriterProperties.SFTP_OUTPUT_PATTERN);
                                    if (connector.getProperties().getProperty(SFTPWriterProperties.SFTP_BINARY).equals("0")) {
                                        information += "   File Type: ASCII";
                                    } else {
                                        information += "   File Type: Binary";
                                    }
                                } else if (connector.getTransportName().equals(JMSWriterProperties.name)) {
                                    // Destination - JMS Writer.


                                    //TO-DO: Need to implement JMS Writer properties.
//                                    information = "";


                                    
                                } else if (connector.getTransportName().equals(ChannelWriterProperties.name)) {
                                    // Destination - Channel Writer.
                                    information = "Target Channel: " + connector.getProperties().getProperty(ChannelWriterProperties.CHANNEL_NAME);
                                } else if (connector.getTransportName().equals(DocumentWriterProperties.name)) {
                                    // Destination - Document Writer.
                                    if (connector.getProperties().getProperty(DocumentWriterProperties.DOCUMENT_PASSWORD_PROTECTED).equals("0")) {
                                        information = connector.getProperties().getProperty(DocumentWriterProperties.DOCUMENT_TYPE) + " Document Type Result written to: " +
                                                      connector.getProperties().getProperty(DocumentWriterProperties.FILE_DIRECTORY) + "/" + connector.getProperties().getProperty(DocumentWriterProperties.FILE_NAME);
                                    } else {
                                        information = "Encrypted " + connector.getProperties().getProperty(DocumentWriterProperties.DOCUMENT_TYPE) + " Document Type Result written to: " +
                                                      connector.getProperties().getProperty(DocumentWriterProperties.FILE_DIRECTORY) + "/" + connector.getProperties().getProperty(DocumentWriterProperties.FILE_NAME);
                                    }
                                }
                                break;
                            case SENDER:
                                st = new StringTokenizer(connectorId, "_");
                                st.nextToken(); st.nextToken();
                                destinationIndex = Integer.valueOf(st.nextToken()) - 1;     // destination begins from 1, so for arrayIndex subtract by 1.
                                connector = channel.getDestinationConnectors().get(destinationIndex);
                                connectorType = "Destination: " + connector.getTransportName() + " - " + connector.getName();

                                if (connector.getTransportName().equals(HTTPSenderProperties.name)) {
                                    // Destination - HTTP Sender.
                                    information = "Host: " + connector.getProperties().getProperty(HTTPSenderProperties.HTTP_URL) + "   Method: " + connector.getProperties().getProperty(HTTPSenderProperties.HTTP_METHOD);
                                } else if (connector.getTransportName().equals(EmailSenderProperties.name)) {
                                    // Destination - Email Sender.
                                    information = "From: " + connector.getProperties().getProperty(EmailSenderProperties.EMAIL_FROM) +
                                                  "   To: " + connector.getProperties().getProperty(EmailSenderProperties.EMAIL_TO) +
                                                  "   SMTP Info: " + connector.getProperties().getProperty(EmailSenderProperties.EMAIL_ADDRESS) + ":" + connector.getProperties().getProperty(EmailSenderProperties.EMAIL_PORT);
                                } else if (connector.getTransportName().equals(TCPSenderProperties.name)) {
                                    // Destination - TCP Sender.
//                                    information = "Host: " + connector.getProperties().getProperty("host") + ":" + connector.getProperties().getProperty("port");
                                } else if (connector.getTransportName().equals(LLPSenderProperties.name)) {
                                    // Destination - LLP Sender.
//                                    information = "Host: " + connector.getProperties().getProperty("host") + ":" + connector.getProperties().getProperty("port");
                                } else if (connector.getTransportName().equals(SOAPSenderProperties.name)) {
                                    // Destination - SOAP Sender.


                                    //TO-DO: Need to implement SOAP Sender properties.
//                                    information = "";

                                    
                                }
                                break;
                        }
                        break;      // break out of the for-loop.
                    }
                }
            }
            catch (Exception e)
            {
                logger.error("Error: DashboardConnectorStatusMonitor.java", e);
            }

            if (socket != null) {

                String listenerIP = socket.getLocalAddress().toString() + ":" + socket.getLocalPort();
                String senderIP = socket.getInetAddress().toString() + ":" + socket.getPort();
                
                // If addresses begin with a slash "/", remove it.
                if (listenerIP.startsWith("/")) {
                    listenerIP = listenerIP.substring(1);
                }
                if (senderIP.startsWith("/")) {
                    senderIP = senderIP.substring(1);
                }

                information += "Listener: " + listenerIP + "  Sender: " + senderIP;
            }

            if (channelLog != null) {
                if (channelLog.size() == LOG_SIZE) {
                    channelLog.removeLast();
                }
                channelLog.addFirst (new String[] { channelName, ft.format(ts), connectorType, event.toString(), information });

                // put the channel log into the HashMap.
                connectorInfoLogs.put(channelName, channelLog);
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
		// TODO Auto-generated method stub
		return null;
	}

	public void init(Properties properties) {
		// TODO Auto-generated method stub
		initialize();
	}

	private void initialize() {
		this.socketSets = new HashMap<String, Set<Socket>>();
		this.currentStates = new HashMap<String, String[]>();
        this.connectorInfoLogs = new HashMap<String, LinkedList<String[]>>();
    }

	public Object invoke(String method, Object object) {
		if (method.equals(GET_STATES)) {
			return this.currentStates;
		} else if (method.equals(GET_CONNECTION_INFO_LOGS)) {
            return connectorInfoLogs.get(object.toString());
        }
        return null;
	}

    public void onDeploy() {
		initialize();
	}

	public void start() {
		// TODO Auto-generated method stub
		
	}

	public void stop() {
		// TODO Auto-generated method stub
		
	}

	public void update(Properties properties) {
		// TODO Auto-generated method stub
		
	}

}
