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

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.ChannelStatistics;
import com.webreach.mirth.model.ChannelStatus;
import com.webreach.mirth.model.DriverInfo;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.model.Transport;
import com.webreach.mirth.model.User;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.model.filters.MessageObjectFilter;
import com.webreach.mirth.model.filters.SystemEventFilter;

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
	public synchronized boolean login(String username, String password) throws ClientException {
		logger.debug("attempting to login user: username=" + username);
		NameValuePair[] params = { new NameValuePair("op", "login"), new NameValuePair("username", username), new NameValuePair("password", password) };
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
	public synchronized boolean isLoggedIn() throws ClientException {
		logger.debug("checking if logged in");
		NameValuePair[] params = { new NameValuePair("op", "isLoggedIn") };
		return Boolean.valueOf(serverConnection.executePostMethod(USER_SERVLET, params));
	}

	/**
	 * Returns a List of all channels.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public synchronized List<Channel> getChannels() throws ClientException {
		logger.debug("retrieving channel list");
		NameValuePair[] params = { new NameValuePair("op", "getChannels") };
		return (List<Channel>) serializer.fromXML(serverConnection.executePostMethod(CHANNEL_SERVLET, params));
	}

	/**
	 * Updates the specified channel.
	 * 
	 * @param channel
	 * @throws ClientException
	 */
	public synchronized boolean updateChannel(Channel channel, boolean override) throws ClientException {
		logger.debug("updating channel: channelId=" + channel.getId() + ", override=" + override);
		NameValuePair[] params = { new NameValuePair("op", "updateChannel"), new NameValuePair("data", serializer.toXML(channel)), new NameValuePair("override", new Boolean(override).toString()) };
		return Boolean.valueOf(serverConnection.executePostMethod(CHANNEL_SERVLET, params)).booleanValue();
	}

	/**
	 * Removes the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ClientException
	 */
	public synchronized void removeChannel(String channelId) throws ClientException {
		logger.debug("removing channel: channelId=" + channelId);
		NameValuePair[] params = { new NameValuePair("op", "removeChannel"), new NameValuePair("data", channelId) };
		serverConnection.executePostMethod(CHANNEL_SERVLET, params);
	}

	/**
	 * Returns a List of all transports.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public synchronized Map<String, Transport> getTransports() throws ClientException {
		logger.debug("retrieving transport list");
		NameValuePair[] params = { new NameValuePair("op", "getTransports") };
		return (Map<String, Transport>) serializer.fromXML(serverConnection.executePostMethod(CONFIGURATION_SERVLET, params));
	}

	/**
	 * Returns a List of all users.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public synchronized List<User> getUsers() throws ClientException {
		logger.debug("retrieving user list");
		NameValuePair[] params = { new NameValuePair("op", "getUsers") };
		return (List<User>) serializer.fromXML(serverConnection.executePostMethod(USER_SERVLET, params));
	}

	/**
	 * Updates a specified user.
	 * 
	 * @param user
	 * @throws ClientException
	 */
	public synchronized void updateUser(User user) throws ClientException {
		logger.debug("updating user: user id = " + user.toString());
		NameValuePair[] params = { new NameValuePair("op", "updateUser"), new NameValuePair("data", serializer.toXML(user)) };
		serverConnection.executePostMethod(USER_SERVLET, params);
	}

	/**
	 * Removes the user with the specified id.
	 * 
	 * @param userId
	 * @throws ClientException
	 */
	public synchronized void removeUser(int userId) throws ClientException {
		logger.debug("removing user: user id = " + userId);
		NameValuePair[] params = { new NameValuePair("op", "removeUser"), new NameValuePair("data", String.valueOf(userId)) };
		serverConnection.executePostMethod(USER_SERVLET, params);
	}

	/**
	 * Returns a Properties object with all server configuration properties.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public synchronized Properties getServerProperties() throws ClientException {
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
	public synchronized void updateServerProperties(Properties properties) throws ClientException {
		logger.debug("updating server properties");
		NameValuePair[] params = { new NameValuePair("op", "updateServerProperties"), new NameValuePair("data", serializer.toXML(properties)) };
		serverConnection.executePostMethod(CONFIGURATION_SERVLET, params);
	}

	/**
	 * Returns the latest configuration id.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public synchronized int getNextId() throws ClientException {
		logger.debug("retrieving next id");
		NameValuePair[] params = { new NameValuePair("op", "getNextId") };
		return Integer.parseInt(serverConnection.executePostMethod(CONFIGURATION_SERVLET, params));
	}

	/**
	 * Returns a globaly unique id.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public synchronized String getGuid() throws ClientException {
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
		serverConnection.executePostMethod(CHANNEL_STATUS_SERVLET, params);	}

	/**
	 * Pauses the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ClientException
	 */
	public synchronized void pauseChannel(String channelId) throws ClientException {
		logger.debug("pausing channel: channelId=" + channelId);
		NameValuePair[] params = { new NameValuePair("op", "pauseChannel"), new NameValuePair("id", channelId) };
		serverConnection.executePostMethod(CHANNEL_STATUS_SERVLET, params);	}

	/**
	 * Resumes the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ClientException
	 */
	public synchronized void resumeChannel(String channelId) throws ClientException {
		logger.debug("resuming channel: channelId=" + channelId);
		NameValuePair[] params = { new NameValuePair("op", "resumeChannel"), new NameValuePair("id", channelId) };
		serverConnection.executePostMethod(CHANNEL_STATUS_SERVLET, params);	}

	/**
	 * Returns the Statistics for the channel with the specified id.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public synchronized ChannelStatistics getStatistics(String channelId) throws ClientException {
		logger.debug("retrieving channel statistics: channelId=" + channelId);
		NameValuePair[] params = { new NameValuePair("op", "getStatistics"), new NameValuePair("id", channelId) };
		return (ChannelStatistics) serializer.fromXML(serverConnection.executePostMethod(CHANNEL_STATISTICS_SERVLET, params));
	}

	/**
	 * Clears the statistics for the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ClientException
	 */
	public synchronized void clearStatistics(String channelId) throws ClientException {
		logger.debug("clearing channel statistics: channelId=" + channelId);
		NameValuePair[] params = { new NameValuePair("op", "clearStatistics"), new NameValuePair("id", channelId) };
		serverConnection.executePostMethod(CHANNEL_STATISTICS_SERVLET, params);
	}

	/**
	 * Returns a list of system events.
	 * 
	 * @param filter
	 * @return
	 * @throws ClientException
	 */
	public synchronized List<SystemEvent> getSystemEvents(SystemEventFilter filter) throws ClientException {
		logger.debug("retrieving log event list");
		NameValuePair[] params = { new NameValuePair("op", "getSystemEvents"), new NameValuePair("filter", serializer.toXML(filter)) };
		return (List<SystemEvent>) serializer.fromXML(serverConnection.executePostMethod(EVENT_SERVLET, params));
	}

	/**
	 * Clears the system event list.
	 * 
	 * @throws ClientException
	 */
	public synchronized void clearSystemEvents() throws ClientException {
		logger.debug("clearing system events");
		NameValuePair[] params = { new NameValuePair("op", "clearSystemEvents") };
		serverConnection.executePostMethod(EVENT_SERVLET, params);
	}

	/**
	 * Removes the message event with the specified id.
	 * 
	 * @param messageEventId
	 * @throws ClientException
	 */
	public synchronized void removeMessages(MessageObjectFilter filter) throws ClientException {
		logger.debug("removing messages");
		NameValuePair[] params = { new NameValuePair("op", "removeMessages"), new NameValuePair("filter", serializer.toXML(filter)) };
		serverConnection.executePostMethod(MESSAGE_SERVLET, params);
	}

	/**
	 * Clears the message list for the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ClientException
	 */
	public synchronized void clearMessages(String channelId) throws ClientException {
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
	public synchronized List<MessageObject> getMessageEvents(MessageObjectFilter filter) throws ClientException {
		logger.debug("retrieving messages");
		NameValuePair[] params = { new NameValuePair("op", "getMessages"), new NameValuePair("filter", serializer.toXML(filter)) };
		return (List<MessageObject>) serializer.fromXML(serverConnection.executePostMethod(MESSAGE_SERVLET, params));
	}

	public MessageListHandler getMessageListHandler(MessageObjectFilter filter) {
		return new MessageListHandler(filter, serverConnection);
	}
	
	/**
	 * Returns the channel status list.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public synchronized List<ChannelStatus> getChannelStatusList() throws ClientException {
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
	public synchronized List<DriverInfo> getDatabaseDrivers() throws ClientException {
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
	public synchronized String getVersion() throws ClientException {
		logger.debug("retrieving version");
		NameValuePair[] params = { new NameValuePair("op", "getVersion") };
		return serverConnection.executePostMethod(CONFIGURATION_SERVLET, params);
	}

	/**
	 * Returns the build date of the Mirth server.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public synchronized String getBuildDate() throws ClientException {
		logger.debug("retrieving build date");
		NameValuePair[] params = { new NameValuePair("op", "getBuildDate") };
		return serverConnection.executePostMethod(CONFIGURATION_SERVLET, params);
	}
}
