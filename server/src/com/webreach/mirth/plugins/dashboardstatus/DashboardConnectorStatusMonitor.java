package com.webreach.mirth.plugins.dashboardstatus;

import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.plugins.ConnectorStatusPlugin;
import com.webreach.mirth.plugins.ServerPlugin;
import com.webreach.mirth.server.controllers.ChannelController;
import com.webreach.mirth.server.controllers.MonitoringController.ConnectorType;
import com.webreach.mirth.server.controllers.MonitoringController.Event;

public class DashboardConnectorStatusMonitor implements ServerPlugin{
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
	private HashMap<String, String[]> currentStates;
	private HashMap<String, Set<Socket>> socketSets;

	public void updateStatus(String connectorName, ConnectorType type, Event event, Socket socket) {
		// TODO Auto-generated method stub
		String connectorId = connectorName;
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
						stateImage = GREEN;
						statusText = POLLING;
						break;
				}
				updateState = true;
				break;
			case CONNECTED:
				switch (type){
					case LISTENER:
						addConnectionToSocketSet(socket, connectorId);
						stateImage = GREEN;
						statusText = CONNECTED + " (" + getSocketSetCount(connectorId) + ")";
						break;
					case READER:
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
						stateImage = GREEN;
						statusText = CONNECTED + " (" + getSocketSetCount(connectorId) + ")";
						updateState = true;
						break;
				}
				break;
			default:
				break;
		
		}
		if (updateState){
			this.currentStates.put(connectorId, new String[]{stateImage, statusText});
		}
	}

	private void addConnectionToSocketSet(Socket socket, String connectorId) {
		if (socket != null){
			Set<Socket> socketSet = socketSets.get(connectorId);
			if (socketSet == null){
				socketSet = new HashSet<Socket>();
				socketSets.put(connectorId, socketSet);
			}
			socketSet.add(socket);
		}
	}
	
	private void removeConnectionInSocketSet(Socket socket, String connectorId) {
		if (socket != null){
			Set socketSet = socketSets.get(connectorId);
			if (socketSet != null){
				socketSet.remove(socket);
			}
		}
	}
	private void clearSocketSet(String connectorId) {
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
	}

	public Object invoke(String method, Object object) {
		if (method.equals(GET_STATES)){
			return this.currentStates;
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
