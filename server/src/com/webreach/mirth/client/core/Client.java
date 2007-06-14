/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.client.core;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.log4j.Logger;

import com.webreach.mirth.model.Alert;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.ChannelStatistics;
import com.webreach.mirth.model.ChannelStatus;
import com.webreach.mirth.model.ChannelSummary;
import com.webreach.mirth.model.DriverInfo;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.ServerConfiguration;
import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.model.ConnectorMetaData;
import com.webreach.mirth.model.User;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.model.filters.MessageObjectFilter;
import com.webreach.mirth.model.filters.SystemEventFilter;
import com.webreach.mirth.model.ws.WSDefinition;

public class Client {
	private Logger logger = Logger.getLogger(this.getClass());
	private ObjectXMLSerializer serializer = new ObjectXMLSerializer();
	private ServerConnection serverConnection;

	public final static String USER_SERVLET = "/users";
	public final static String CHANNEL_SERVLET = "/channels";
	public final static String CONFIGURATION_SERVLET = "/configuration";
	public final static String CHANNEL_STATUS_SERVLET = "/channelstatus";
	public final static String CHANNEL_STATISTICS_SERVLET = "/channelstatistics";
	public final static String MESSAGE_SERVLET = "/messages";
	public final static String EVENT_SERVLET = "/events";
	public final static String ALERT_SERVLET = "/alerts";

	/**
	 * Instantiates a new Mirth client with a connection to the specified
	 * server.
	 * 
	 * @param address
	 */
	public Client(String address) {
		serverConnection = ServerConnectionFactory.createServerConnection(address);
	}

	/**
	 * Logs a user in to the Mirth server using the specified name and password.
	 * 
	 * @param username
	 * @param password
	 * @return
	 * @throws ClientException
	 */
	public synchronized boolean login(String username, String password, String version) throws ClientException {
		logger.debug("attempting to login user: username=" + username);
		NameValuePair[] params = { new NameValuePair("op", "login"), new NameValuePair("username", username), new NameValuePair("password", password), new NameValuePair("version", version) };
		return Boolean.valueOf(serverConnection.executePostMethod(USER_SERVLET, params)).booleanValue();	
	}

	/**
	 * Logs the user out of the server.
	 * 
	 * @throws ClientException
	 */
	public synchronized void logout() throws ClientException {
		logger.debug("logging out");
		NameValuePair[] params = { new NameValuePair("op", "logout") };
		serverConnection.executePostMethod(USER_SERVLET, params);
	}
	
    /**
     * Returns <code>true</code> if the user is logged in, <code>false</code>
     * otherwise.
     * 
     * @return
     * @throws ClientException
     */
    public String getServerId() throws ClientException {
        logger.debug("retrieving server's id");
        NameValuePair[] params = { new NameValuePair("op", "getServerId") };
        return serverConnection.executePostMethod(CONFIGURATION_SERVLET, params);
    }
    
	/**
	 * Returns <code>true</code> if the user is logged in, <code>false</code>
	 * otherwise.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public boolean isLoggedIn() throws ClientException {
		logger.debug("checking if logged in");
		NameValuePair[] params = { new NameValuePair("op", "isLoggedIn") };
		return Boolean.valueOf(serverConnection.executePostMethod(USER_SERVLET, params));
	}

	/**
	 * Returns a ServerConfiguration object which contains all of the channels,
	 * users, alerts and properties stored on the Mirth server.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public ServerConfiguration getServerConfiguration() throws ClientException {
		logger.debug("getting server configuration");
		NameValuePair[] params = { new NameValuePair("op", "getServerConfiguration") };
		return (ServerConfiguration) serializer.fromXML(serverConnection.executePostMethod(CONFIGURATION_SERVLET, params));
	}
	
	/**
	 * Sets a ServerConfiguration object which sets all of the channels, 
	 * alerts and properties stored on the Mirth server.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public synchronized void setServerConfiguration(ServerConfiguration serverConfiguration) throws ClientException {
		logger.debug("setting server configuration");
		NameValuePair[] params = { new NameValuePair("op", "setServerConfiguration"), new NameValuePair("data", serializer.toXML(serverConfiguration)) };
		serverConnection.executePostMethod(CONFIGURATION_SERVLET, params);
	}

	/**
	 * Returns a List of all channels.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public List<Channel> getChannel(Channel channel) throws ClientException {
		logger.debug("getting channel");
		NameValuePair[] params = { new NameValuePair("op", "getChannel"), new NameValuePair("channel", serializer.toXML(channel)) };
		return (List<Channel>) serializer.fromXML(serverConnection.executePostMethod(CHANNEL_SERVLET, params));
	}

	public List<ChannelSummary> getChannelSummary(Map<String, Integer> cachedChannels) throws ClientException {
		logger.debug("getting channel summary");
		NameValuePair[] params = { new NameValuePair("op", "getChannelSummary"), new NameValuePair("cachedChannels", serializer.toXML(cachedChannels)) };
		return (List<ChannelSummary>) serializer.fromXML(serverConnection.executePostMethod(CHANNEL_SERVLET, params));
	}

	/**
	 * Updates the specified channel.
	 * 
	 * @param channel
	 * @throws ClientException
	 */
	public synchronized boolean updateChannel(Channel channel, boolean override) throws ClientException {
		logger.debug("updating channel: channelId=" + channel.getId() + ", override=" + override);
		NameValuePair[] params = { new NameValuePair("op", "updateChannel"), new NameValuePair("channel", serializer.toXML(channel)), new NameValuePair("override", new Boolean(override).toString()) };
		return Boolean.valueOf(serverConnection.executePostMethod(CHANNEL_SERVLET, params)).booleanValue();
	}

	/**
	 * Removes the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ClientException
	 */
	public synchronized void removeChannel(Channel channel) throws ClientException {
		logger.debug("removing channel: channelId=" + channel.getId());
		NameValuePair[] params = { new NameValuePair("op", "removeChannel"), new NameValuePair("channel", serializer.toXML(channel)) };
		serverConnection.executePostMethod(CHANNEL_SERVLET, params);
	}

	/**
	 * Returns a List of all transports.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public Map<String, ConnectorMetaData> getConnectorMetaData() throws ClientException {
		logger.debug("retrieving transport list");
		NameValuePair[] params = { new NameValuePair("op", "getConnectorMetaData") };
		return (Map<String, ConnectorMetaData>) serializer.fromXML(serverConnection.executePostMethod(CONFIGURATION_SERVLET, params));
	}

	/**
	 * Returns a List of all of the encodings supported by the server
	 * 
	 * @return
	 * @throws ClientException
	 */
	// ast: The avaiable charset encodings depends on the JVM in which the
	// server is running
	public List<String> getAvaiableCharsetEncodings() throws ClientException {
		logger.debug("retrieving the server supported charset encoging list");
		NameValuePair[] params = { new NameValuePair("op", "getAvaiableCharsetEncodings") };
		return (List<String>) serializer.fromXML(serverConnection.executePostMethod(CONFIGURATION_SERVLET, params));
	}

	/**
	 * Returns a List of all users.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public List<User> getUser(User user) throws ClientException {
		logger.debug("getting user: " + user);
		NameValuePair[] params = { new NameValuePair("op", "getUser"), new NameValuePair("user", serializer.toXML(user)) };
		return (List<User>) serializer.fromXML(serverConnection.executePostMethod(USER_SERVLET, params));
	}

	/**
	 * Updates a specified user.
	 * 
	 * @param user
	 * @throws ClientException
	 */
	public synchronized void updateUser(User user, String password) throws ClientException {
		logger.debug("updating user: " + user);
		NameValuePair[] params = { new NameValuePair("op", "updateUser"), new NameValuePair("user", serializer.toXML(user)), new NameValuePair("password", password) };
		serverConnection.executePostMethod(USER_SERVLET, params);
	}

	/**
	 * Removes the user with the specified id.
	 * 
	 * @param userId
	 * @throws ClientException
	 */
	public synchronized void removeUser(User user) throws ClientException {
		logger.debug("removing user: " + user);
		NameValuePair[] params = { new NameValuePair("op", "removeUser"), new NameValuePair("user", serializer.toXML(user)) };
		serverConnection.executePostMethod(USER_SERVLET, params);
	}

	/**
	 * Returns a true if the password matches the pasword stored in the database for the specified user.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public boolean authorizeUser(User user, String password) throws ClientException {
		logger.debug("authorizing user: " + user);
		NameValuePair[] params = { new NameValuePair("op", "authorizeUser"), new NameValuePair("user", serializer.toXML(user)), new NameValuePair("password", password) };
		return Boolean.valueOf(serverConnection.executePostMethod(USER_SERVLET, params));
	}

	/**
	 * Returns a true if the specified user is logged in to the server.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public boolean isUserLoggedIn(User user) throws ClientException {
		logger.debug("checking if user logged in: " + user);
		NameValuePair[] params = { new NameValuePair("op", "isUserLoggedIn"), new NameValuePair("user", serializer.toXML(user)) };
		return Boolean.valueOf(serverConnection.executePostMethod(USER_SERVLET, params));
	}

	/**
	 * Returns a List of all alerts.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public List<Alert> getAlert(Alert alert) throws ClientException {
		logger.debug("getting alert: " + alert);
		NameValuePair[] params = { new NameValuePair("op", "getAlert"), new NameValuePair("alert", serializer.toXML(alert)) };
		return (List<Alert>) serializer.fromXML(serverConnection.executePostMethod(ALERT_SERVLET, params));
	}

	/**
	 * Updates a specified alert.
	 * 
	 * @param alert
	 * @throws ClientException
	 */
	public synchronized void updateAlert(Alert alert) throws ClientException {
		logger.debug("updating alert: " + alert);
		NameValuePair[] params = { new NameValuePair("op", "updateAlert"), new NameValuePair("alert", serializer.toXML(alert)) };
		serverConnection.executePostMethod(ALERT_SERVLET, params);
	}

	/**
	 * Updates a list of alerts.
	 * 
	 * @param alert
	 * @throws ClientException
	 */
	public synchronized void updateAlerts(List<Alert> alerts) throws ClientException {
		logger.debug("updating alerts: " + alerts);
		NameValuePair[] params = { new NameValuePair("op", "updateAlerts"), new NameValuePair("alerts", serializer.toXML(alerts)) };
		serverConnection.executePostMethod(ALERT_SERVLET, params);
	}

	/**
	 * Removes the alert with the specified id.
	 * 
	 * @param alertId
	 * @throws ClientException
	 */
	public synchronized void removeAlert(Alert alert) throws ClientException {
		logger.debug("removing alert: " + alert);
		NameValuePair[] params = { new NameValuePair("op", "removeAlert"), new NameValuePair("alert", serializer.toXML(alert)) };
		serverConnection.executePostMethod(ALERT_SERVLET, params);
	}

	/**
	 * Returns a Properties object with all server configuration properties.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public Properties getServerProperties() throws ClientException {
		logger.debug("retrieving server properties");
		NameValuePair[] params = { new NameValuePair("op", "getServerProperties") };
		return (Properties) serializer.fromXML(serverConnection.executePostMethod(CONFIGURATION_SERVLET, params));
	}

	/**
	 * Updates the server configuration properties.
	 * 
	 * @param properties
	 * @throws ClientException
	 */
	public synchronized void setServerProperties(Properties properties) throws ClientException {
		logger.debug("updating server properties");
		NameValuePair[] params = { new NameValuePair("op", "setServerProperties"), new NameValuePair("data", serializer.toXML(properties)) };
		serverConnection.executePostMethod(CONFIGURATION_SERVLET, params);
	}

	/**
	 * Returns a globaly unique id.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public String getGuid() throws ClientException {
		logger.debug("retrieving next guid");
		NameValuePair[] params = { new NameValuePair("op", "getGuid") };
		return serverConnection.executePostMethod(CONFIGURATION_SERVLET, params);
	}

	/**
	 * Deploys all channels.
	 * 
	 * @throws ClientException
	 */
	public synchronized void deployChannels() throws ClientException {
		logger.debug("deploying channels");
		NameValuePair[] params = { new NameValuePair("op", "deployChannels") };
		serverConnection.executePostMethod(CONFIGURATION_SERVLET, params);
	}

	/**
	 * Starts the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ClientException
	 */
	public synchronized void startChannel(String channelId) throws ClientException {
		logger.debug("starting channel: channelId=" + channelId);
		NameValuePair[] params = { new NameValuePair("op", "startChannel"), new NameValuePair("id", channelId) };
		serverConnection.executePostMethod(CHANNEL_STATUS_SERVLET, params);
	}

	/**
	 * Stops the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ClientException
	 */
	public synchronized void stopChannel(String channelId) throws ClientException {
		logger.debug("stopping channel: channelId=" + channelId);
		NameValuePair[] params = { new NameValuePair("op", "stopChannel"), new NameValuePair("id", channelId) };
		serverConnection.executePostMethod(CHANNEL_STATUS_SERVLET, params);
	}

	/**
	 * Pauses the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ClientException
	 */
	public synchronized void pauseChannel(String channelId) throws ClientException {
		logger.debug("pausing channel: channelId=" + channelId);
		NameValuePair[] params = { new NameValuePair("op", "pauseChannel"), new NameValuePair("id", channelId) };
		serverConnection.executePostMethod(CHANNEL_STATUS_SERVLET, params);
	}

	/**
	 * Resumes the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ClientException
	 */
	public synchronized void resumeChannel(String channelId) throws ClientException {
		logger.debug("resuming channel: channelId=" + channelId);
		NameValuePair[] params = { new NameValuePair("op", "resumeChannel"), new NameValuePair("id", channelId) };
		serverConnection.executePostMethod(CHANNEL_STATUS_SERVLET, params);
	}

	/**
	 * Returns the Statistics for the channel with the specified id.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public ChannelStatistics getStatistics(String channelId) throws ClientException {
		logger.debug("retrieving channel statistics: channelId=" + channelId);
		NameValuePair[] params = { new NameValuePair("op", "getStatistics"), new NameValuePair("id", channelId) };
		return (ChannelStatistics) serializer.fromXML(serverConnection.executePostMethod(CHANNEL_STATISTICS_SERVLET, params));
	}

	/**
	 * Clears the Statistics for the channel with the specified id.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public void clearStatistics(String channelId, boolean received, boolean filtered, boolean queued, boolean sent, boolean error) throws ClientException {
		logger.debug("clearing channel statistics: channelId=" + channelId);
		NameValuePair[] params = { new NameValuePair("op", "clearStatistics"), new NameValuePair("id", channelId), new NameValuePair("deleteReceived", new Boolean(received).toString()),
                new NameValuePair("deleteFiltered", new Boolean(filtered).toString()), new NameValuePair("deleteQueued", new Boolean(queued).toString()), new NameValuePair("deleteSent", new Boolean(sent).toString()),
                new NameValuePair("deleteErrored", new Boolean(error).toString())};
		serverConnection.executePostMethod(CHANNEL_STATISTICS_SERVLET, params);
	}

	/**
	 * Returns a list of system events.
	 * 
	 * @param filter
	 * @return
	 * @throws ClientException
	 */
	public List<SystemEvent> getSystemEvents(SystemEventFilter filter) throws ClientException {
		logger.debug("retrieving log event list");
		NameValuePair[] params = { new NameValuePair("op", "getSystemEvents"), new NameValuePair("filter", serializer.toXML(filter)) };
		return (List<SystemEvent>) serializer.fromXML(serverConnection.executePostMethod(EVENT_SERVLET, params));
	}

	/**
	 * Clears the system event list.
	 * 
	 * @throws ClientException
	 */
	public void clearSystemEvents() throws ClientException {
		logger.debug("clearing system events");
		NameValuePair[] params = { new NameValuePair("op", "clearSystemEvents") };
		serverConnection.executePostMethod(EVENT_SERVLET, params);
	}

	public void removeMessages(MessageObjectFilter filter) throws ClientException {
		logger.debug("removing messages");
		NameValuePair[] params = { new NameValuePair("op", "removeMessages"), new NameValuePair("filter", serializer.toXML(filter)) };
		serverConnection.executePostMethod(MESSAGE_SERVLET, params);
	}

	public void reprocessMessages(MessageObjectFilter filter) throws ClientException {
		logger.debug("reprocessing messages");
		NameValuePair[] params = { new NameValuePair("op", "reprocessMessages"), new NameValuePair("filter", serializer.toXML(filter)) };
		serverConnection.executePostMethod(MESSAGE_SERVLET, params);
	}
    
	public void processMessage(MessageObject message) throws ClientException {
            logger.debug("processing message");
            NameValuePair[] params = { new NameValuePair("op", "processMessage"), new NameValuePair("message", serializer.toXML(message)) };
            serverConnection.executePostMethod(MESSAGE_SERVLET, params);
    }
    
    public void importMessage(MessageObject message) throws ClientException {
        logger.debug("importing message");
        NameValuePair[] params = { new NameValuePair("op", "importMessage"), new NameValuePair("message", serializer.toXML(message)) };
        serverConnection.executePostMethod(MESSAGE_SERVLET, params);
    }

	/**
	 * Clears the message list for the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ClientException
	 */
	public void clearMessages(String channelId) throws ClientException {
		logger.debug("clearing messages: " + channelId);
		NameValuePair[] params = { new NameValuePair("op", "clearMessages"), new NameValuePair("data", channelId) };
		serverConnection.executePostMethod(MESSAGE_SERVLET, params);
	}

	/**
	 * Returns a list of message events based on the specified filter.
	 * 
	 * @param filter
	 * @return
	 * @throws ClientException
	 */
	public List<MessageObject> getMessageEvents(MessageObjectFilter filter) throws ClientException {
		logger.debug("retrieving messages");
		NameValuePair[] params = { new NameValuePair("op", "getMessages"), new NameValuePair("filter", serializer.toXML(filter)) };
		return (List<MessageObject>) serializer.fromXML(serverConnection.executePostMethod(MESSAGE_SERVLET, params));
	}

	public MessageListHandler getMessageListHandler(MessageObjectFilter filter, int pageSize, boolean newInstance) throws ClientException {
		return new MessageListHandler(filter, pageSize, (newInstance ? (System.currentTimeMillis() + "") : null), serverConnection);
	}

	/**
	 * Returns the channel status list.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public List<ChannelStatus> getChannelStatusList() throws ClientException {
		logger.debug("retrieving channel status list");
		NameValuePair[] params = { new NameValuePair("op", "getChannelStatusList") };
		return (List<ChannelStatus>) serializer.fromXML(serverConnection.executePostMethod(CHANNEL_STATUS_SERVLET, params));
	}

	/**
	 * Returns the database driver list.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public List<DriverInfo> getDatabaseDrivers() throws ClientException {
		logger.debug("retrieving database driver list");
		NameValuePair[] params = { new NameValuePair("op", "getDatabaseDrivers") };
		return (List<DriverInfo>) serializer.fromXML(serverConnection.executePostMethod(CONFIGURATION_SERVLET, params));
	}

	/**
	 * Returns the version of the Mirth server.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public String getVersion() throws ClientException {
		logger.debug("retrieving version");
		NameValuePair[] params = { new NameValuePair("op", "getVersion") };
		return serverConnection.executePostMethod(CONFIGURATION_SERVLET, params);
	}
	
	/**
	 * Returns the status of the Mirth server.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public synchronized int getStatus() throws ClientException {
		logger.debug("retrieving status");
		NameValuePair[] params = { new NameValuePair("op", "getStatus") };
		return Integer.valueOf(serverConnection.executePostMethod(CONFIGURATION_SERVLET, params));
	}

	/**
	 * Returns the build date of the Mirth server.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public String getBuildDate() throws ClientException {
		logger.debug("retrieving build date");
		NameValuePair[] params = { new NameValuePair("op", "getBuildDate") };
		return serverConnection.executePostMethod(CONFIGURATION_SERVLET, params);
	}

	/**
	 * Submits an error message to the Mirth Project.
	 * 
	 * @param message
	 */
	public void submitError(String message) {
		Error error = new Error();
		error.setJavaVersion(System.getProperty("java.version"));
		error.setOsArchitecture(System.getProperty("os.arch"));
		error.setOsName(System.getProperty("os.name"));
		error.setOsVersion(System.getProperty("os.version"));
		error.setStackTrace(message);
		error.setDate(new ErrorDate());

		ServerConnection errorServerConnection = new ServerConnection("http://www.mirthproject.org:8083/errors");

		try {
			errorServerConnection.executePostMethod("/create", error.getAsParams());
		} catch (ClientException e) {
			logger.error("could not submit error", e);
		}
	}

	public WSDefinition getWebServiceDefinition(String address) throws ClientException {
		logger.debug("retrieving web service definition for address: " + address);
		NameValuePair[] params = { new NameValuePair("op", "getWebServiceDefinition"), new NameValuePair("address", address) };
		return (WSDefinition) serializer.fromXML(serverConnection.executePostMethod(CONFIGURATION_SERVLET, params));
	}
}
