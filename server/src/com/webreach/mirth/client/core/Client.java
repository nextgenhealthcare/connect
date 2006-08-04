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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.log4j.Logger;

import com.webreach.mirth.client.core.ssl.EasySSLProtocolSocketFactory;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.ChannelStatus;
import com.webreach.mirth.model.DriverInfo;
import com.webreach.mirth.model.MessageEvent;
import com.webreach.mirth.model.ChannelStatistics;
import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.model.Transport;
import com.webreach.mirth.model.User;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.model.filters.MessageEventFilter;
import com.webreach.mirth.model.filters.SystemEventFilter;

public class Client {
	private Logger logger = Logger.getLogger(this.getClass());
	private String serverAddress = null;
	private HttpClient client = null;
	private ObjectXMLSerializer serializer = new ObjectXMLSerializer();

	private final static String USER_SERVLET = "/users";
	private final static String CHANNEL_SERVLET = "/channels";
	private final static String CONFIGURATION_SERVLET = "/configuration";
	private final static String CHANNEL_STATUS_SERVLET = "/channelstatus";
	private final static String STATISTICS_SERVLET = "/statistics";
	private final static String LOGGER_SERVLET = "/logger";

	/**
	 * Instantiates a new Mirth client with a connection to the specified
	 * server.
	 * 
	 * @param serverAddress
	 */
	public Client(String serverAddress) {
		this.serverAddress = serverAddress;
		client = new HttpClient();
		Protocol mirthHttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 8443);
		Protocol.registerProtocol("https", mirthHttps);
	}

	/**
	 * Executes a POST method on a servlet with a set of parameters.
	 * 
	 * @param servletName The name of the servlet.
	 * @param params An array of NameValuePair objects.
	 * @return
	 * @throws ClientException
	 */
	private String executePostMethod(String servletName, NameValuePair[] params) throws ClientException {
		PostMethod post = null;
		
		try {
			post = new PostMethod(serverAddress + servletName);
			post.setRequestBody(params);

			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}

			return post.getResponseBodyAsString();
		} catch (Exception e) {
			throw new ClientException(e);
		} finally {
			post.releaseConnection();
		}
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
		return Boolean.valueOf(executePostMethod(USER_SERVLET, params)).booleanValue();
	}

	/**
	 * Logs the user out of the server.
	 * 
	 * @throws ClientException
	 */
	public synchronized void logout() throws ClientException {
		logger.debug("logging out");
		NameValuePair[] params = { new NameValuePair("op", "logout") };
		executePostMethod(USER_SERVLET, params);
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
		return Boolean.valueOf(executePostMethod(USER_SERVLET, params));
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
		return (List<Channel>) serializer.fromXML(executePostMethod(CHANNEL_SERVLET, params));
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
		return Boolean.valueOf(executePostMethod(CHANNEL_SERVLET, params)).booleanValue();
	}

	/**
	 * Removes the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ClientException
	 */
	public synchronized void removeChannel(int channelId) throws ClientException {
		logger.debug("removing channel: channelId=" + channelId);
		NameValuePair[] params = { new NameValuePair("op", "removeChannel"), new NameValuePair("data", String.valueOf(channelId)) };
		executePostMethod(CHANNEL_SERVLET, params);
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
		return (Map<String, Transport>) serializer.fromXML(executePostMethod(CONFIGURATION_SERVLET, params));
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
		return (List<User>) serializer.fromXML(executePostMethod(USER_SERVLET, params));
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
		executePostMethod(USER_SERVLET, params);
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
		executePostMethod(USER_SERVLET, params);
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
		return (Properties) serializer.fromXML(executePostMethod(CONFIGURATION_SERVLET, params));
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
		executePostMethod(CONFIGURATION_SERVLET, params);
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
		return Integer.parseInt(executePostMethod(CONFIGURATION_SERVLET, params));
	}

	/**
	 * Deploys all channels.
	 * 
	 * @throws ClientException
	 */
	public synchronized void deployChannels() throws ClientException {
		logger.debug("deploying channels");
		NameValuePair[] params = { new NameValuePair("op", "deployChannels") };
		executePostMethod(CONFIGURATION_SERVLET, params);
	}

	/**
	 * Starts the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ClientException
	 */
	public synchronized void startChannel(int channelId) throws ClientException {
		logger.debug("starting channel: channelId=" + channelId);
		NameValuePair[] params = { new NameValuePair("op", "startChannel"), new NameValuePair("id", String.valueOf(channelId)) };
		executePostMethod(CHANNEL_STATUS_SERVLET, params);
	}

	/**
	 * Stops the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ClientException
	 */
	public synchronized void stopChannel(int channelId) throws ClientException {
		logger.debug("stopping channel: channelId=" + channelId);
		NameValuePair[] params = { new NameValuePair("op", "stopChannel"), new NameValuePair("id", String.valueOf(channelId)) };
		executePostMethod(CHANNEL_STATUS_SERVLET, params);	}

	/**
	 * Pauses the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ClientException
	 */
	public synchronized void pauseChannel(int channelId) throws ClientException {
		logger.debug("pausing channel: channelId=" + channelId);
		NameValuePair[] params = { new NameValuePair("op", "pauseChannel"), new NameValuePair("id", String.valueOf(channelId)) };
		executePostMethod(CHANNEL_STATUS_SERVLET, params);	}

	/**
	 * Resumes the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ClientException
	 */
	public synchronized void resumeChannel(int channelId) throws ClientException {
		logger.debug("resuming channel: channelId=" + channelId);
		NameValuePair[] params = { new NameValuePair("op", "resumeChannel"), new NameValuePair("id", String.valueOf(channelId)) };
		executePostMethod(CHANNEL_STATUS_SERVLET, params);	}

	/**
	 * Returns the Statistics for the channel with the specified id.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public synchronized ChannelStatistics getStatistics(int channelId) throws ClientException {
		logger.debug("retrieving channel statistics: channelId=" + channelId);
		NameValuePair[] params = { new NameValuePair("op", "getStatistics"), new NameValuePair("id", String.valueOf(channelId)) };
		return (ChannelStatistics) serializer.fromXML(executePostMethod(STATISTICS_SERVLET, params));
	}

	/**
	 * Clears the statistics for the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ClientException
	 */
	public synchronized void clearStatistics(int channelId) throws ClientException {
		logger.debug("clearing channel statistics: channelId=" + channelId);
		NameValuePair[] params = { new NameValuePair("op", "clearStistics"), new NameValuePair("id", String.valueOf(channelId)) };
		executePostMethod(STATISTICS_SERVLET, params);
	}

	public synchronized List<SystemEvent> getSystemEvents(SystemEventFilter filter) throws ClientException {
		logger.debug("retrieving log event list");
		NameValuePair[] params = { new NameValuePair("op", "getSystemEvents"), new NameValuePair("filter", serializer.toXML(filter)) };
		return (List<SystemEvent>) serializer.fromXML(executePostMethod(LOGGER_SERVLET, params));
	}

	/**
	 * Clears the system event list.
	 * 
	 * @throws ClientException
	 */
	public synchronized void clearSystemEvents() throws ClientException {
		logger.debug("clearing system events");
		NameValuePair[] params = { new NameValuePair("op", "clearSystemEvents") };
		executePostMethod(LOGGER_SERVLET, params);
	}

	/**
	 * Removes the message event with the specified id.
	 * 
	 * @param messageEventId
	 * @throws ClientException
	 */
	public synchronized void removeMessageEvent(int messageEventId) throws ClientException {
		logger.debug("removing message event: " + messageEventId);
		NameValuePair[] params = { new NameValuePair("op", "removeMessageEvent"), new NameValuePair("data", String.valueOf(messageEventId)) };
		executePostMethod(LOGGER_SERVLET, params);
	}

	/**
	 * Clears the message list for the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ClientException
	 */
	public synchronized void clearMessageEvents(int channelId) throws ClientException {
		logger.debug("clearing message events: " + channelId);
		NameValuePair[] params = { new NameValuePair("op", "clearMessageEvents"), new NameValuePair("data", String.valueOf(channelId)) };
		executePostMethod(LOGGER_SERVLET, params);
	}

	/**
	 * Returns a list of message events based on the specified filter.
	 * 
	 * @param filter
	 * @return
	 * @throws ClientException
	 */
	public synchronized List<MessageEvent> getMessageEvents(MessageEventFilter filter) throws ClientException {
		logger.debug("retrieving message event list");
		NameValuePair[] params = { new NameValuePair("op", "getMessageEvents"), new NameValuePair("filter", serializer.toXML(filter)) };
		return (List<MessageEvent>) serializer.fromXML(executePostMethod(LOGGER_SERVLET, params));
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
		return (List<ChannelStatus>) serializer.fromXML(executePostMethod(CHANNEL_STATUS_SERVLET, params));
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
		return (List<DriverInfo>) serializer.fromXML(executePostMethod(CONFIGURATION_SERVLET, params));
	}

}
