package com.webreach.mirth.client.core;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.ChannelStatus;
import com.webreach.mirth.model.MessageEvent;
import com.webreach.mirth.model.Statistics;
import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.model.Transport;
import com.webreach.mirth.model.User;
import com.webreach.mirth.model.converters.ObjectSerializer;
import com.webreach.mirth.server.controllers.filters.MessageEventFilter;
import com.webreach.mirth.server.controllers.filters.SystemEventFilter;

public class Client {
	private Logger logger = Logger.getLogger(Client.class);
	private String serverURL = null;
	private HttpClient client = new HttpClient();
	private ObjectSerializer serializer = new ObjectSerializer();

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
	 * @param serverURL
	 */
	public Client(String serverURL) {
		this.serverURL = serverURL;
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
			post = new PostMethod(serverURL + servletName);
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
	public boolean login(String username, String password) throws ClientException {
		logger.debug("attempting to login user: " + username);
		NameValuePair[] params = { new NameValuePair("op", "login"), new NameValuePair("username", username), new NameValuePair("password", password) };
		return Boolean.valueOf(executePostMethod(USER_SERVLET, params)).booleanValue();
	}

	/**
	 * Logs the user out of the server.
	 * 
	 * @throws ClientException
	 */
	public void logout() throws ClientException {
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
	public boolean isLoggedIn() throws ClientException {
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
	public List<Channel> getChannels() throws ClientException {
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
	public void updateChannel(Channel channel) throws ClientException {
		logger.debug("updating channel: " + channel.getId());
		NameValuePair[] params = { new NameValuePair("op", "updateChannel"), new NameValuePair("data", serializer.toXML(channel)) };
		executePostMethod(CHANNEL_SERVLET, params);
	}

	/**
	 * Removes the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ClientException
	 */
	public void removeChannel(int channelId) throws ClientException {
		logger.debug("removing channel: " + channelId);
		NameValuePair[] params = { new NameValuePair("op", "removeChannel"), new NameValuePair("data", String.valueOf(channelId)) };
		executePostMethod(CHANNEL_SERVLET, params);
	}

	/**
	 * Returns a List of all transports.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public Map<String, Transport> getTransports() throws ClientException {
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
	public List<User> getUsers() throws ClientException {
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
	public void updateUser(User user) throws ClientException {
		logger.debug("updating user: " + user.toString());
		NameValuePair[] params = { new NameValuePair("op", "updateUser"), new NameValuePair("data", serializer.toXML(user)) };
		executePostMethod(USER_SERVLET, params);
	}

	/**
	 * Removes the user with the specified id.
	 * 
	 * @param userId
	 * @throws ClientException
	 */
	public void removeUser(int userId) throws ClientException {
		logger.debug("removing user: " + userId);
		NameValuePair[] params = { new NameValuePair("op", "removeUser"), new NameValuePair("data", String.valueOf(userId)) };
		executePostMethod(USER_SERVLET, params);
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
		return (Properties) serializer.fromXML(executePostMethod(CONFIGURATION_SERVLET, params));
	}

	/**
	 * Updates the server configuration properties.
	 * 
	 * @param properties
	 * @throws ClientException
	 */
	public void updateServerProperties(Properties properties) throws ClientException {
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
	public int getNextId() throws ClientException {
		logger.debug("retrieving next id");
		NameValuePair[] params = { new NameValuePair("op", "getNextId") };
		return Integer.parseInt(executePostMethod(CONFIGURATION_SERVLET, params));
	}

	/**
	 * Deploys all channels.
	 * 
	 * @throws ClientException
	 */
	public void deployChannels() throws ClientException {
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
	public void startChannel(int channelId) throws ClientException {
		logger.debug("starting channel: " + channelId);
		NameValuePair[] params = { new NameValuePair("op", "startChannel"), new NameValuePair("id", String.valueOf(channelId)) };
		executePostMethod(CHANNEL_STATUS_SERVLET, params);
	}

	/**
	 * Stops the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ClientException
	 */
	public void stopChannel(int channelId) throws ClientException {
		logger.debug("stopping channel: " + channelId);
		NameValuePair[] params = { new NameValuePair("op", "stopChannel"), new NameValuePair("id", String.valueOf(channelId)) };
		executePostMethod(CHANNEL_STATUS_SERVLET, params);	}

	/**
	 * Pauses the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ClientException
	 */
	public void pauseChannel(int channelId) throws ClientException {
		logger.debug("pausing channel: " + channelId);
		NameValuePair[] params = { new NameValuePair("op", "pauseChannel"), new NameValuePair("id", String.valueOf(channelId)) };
		executePostMethod(CHANNEL_STATUS_SERVLET, params);	}

	/**
	 * Resumes the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ClientException
	 */
	public void resumeChannel(int channelId) throws ClientException {
		logger.debug("resuming channel: " + channelId);
		NameValuePair[] params = { new NameValuePair("op", "resumeChannel"), new NameValuePair("id", String.valueOf(channelId)) };
		executePostMethod(CHANNEL_STATUS_SERVLET, params);	}

	/**
	 * Returns the statistics for the channel with the specified id.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public Statistics getStatistics(int channelId) throws ClientException {
		logger.debug("retrieving channel statistics: " + channelId);
		NameValuePair[] params = { new NameValuePair("op", "getStatistics"), new NameValuePair("id", String.valueOf(channelId)) };
		return (Statistics) serializer.fromXML(executePostMethod(STATISTICS_SERVLET, params));
	}

	/**
	 * Clears the statistics for the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ClientException
	 */
	public void clearStatistics(int channelId) throws ClientException {
		logger.debug("clearing channel statistics: " + channelId);
		NameValuePair[] params = { new NameValuePair("op", "clearStistics"), new NameValuePair("id", String.valueOf(channelId)) };
		executePostMethod(STATISTICS_SERVLET, params);
	}

	public List<SystemEvent> getSystemEvents(SystemEventFilter filter) throws ClientException {
		logger.debug("retrieving log event list");
		NameValuePair[] params = { new NameValuePair("op", "getSystemEvents"), new NameValuePair("filter", serializer.toXML(filter)) };
		return (List<SystemEvent>) serializer.fromXML(executePostMethod(LOGGER_SERVLET, params));
	}

	/**
	 * Clears the system event list.
	 * 
	 * @throws ClientException
	 */
	public void clearSystemEvents() throws ClientException {
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
	public void removeMessageEvent(int messageEventId) throws ClientException {
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
	public void clearMessageEvents(int channelId) throws ClientException {
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
	public List<MessageEvent> getMessageEvents(MessageEventFilter filter) throws ClientException {
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
	public List<ChannelStatus> getChannelStatusList() throws ClientException {
		logger.debug("retrieving channel status list");
		NameValuePair[] params = { new NameValuePair("op", "getChannelStatusList") };
		return (List<ChannelStatus>) serializer.fromXML(executePostMethod(CHANNEL_STATUS_SERVLET, params));
	}
}
