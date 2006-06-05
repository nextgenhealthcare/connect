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
import com.webreach.mirth.model.Status;
import com.webreach.mirth.model.LogEvent;
import com.webreach.mirth.model.MessageEvent;
import com.webreach.mirth.model.Statistics;
import com.webreach.mirth.model.Transport;
import com.webreach.mirth.model.User;
import com.webreach.mirth.model.converters.ObjectSerializer;

public class Client {
	private Logger logger = Logger.getLogger(Client.class);
	private String serverURL = null;
	private HttpClient client = new HttpClient();
	private PostMethod post = null;
	private ObjectSerializer serializer = new ObjectSerializer();

	private final String AUTHENTICATION_SERVLET = "/authentication";
	private final String CONFIGURATION_SERVLET = "/configuration";
	private final String STATUS_SERVLET = "/status";
	private final String STATISTICS_SERVLET = "/statistics";
	private final String ENTRY_SERVLET = "/entry";

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
	 * Logs a user in to the Mirth server using the specified name and password.
	 * 
	 * @param username
	 * @param password
	 * @return
	 * @throws ClientException
	 */
	public boolean login(String username, String password) throws ClientException {
		logger.debug("attempting to login user: " + username);
		post = new PostMethod(serverURL + AUTHENTICATION_SERVLET);
		NameValuePair[] data = { new NameValuePair("op", "login"), new NameValuePair("username", username), new NameValuePair("password", password) };
		post.setRequestBody(data);

		try {
			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}

			return Boolean.valueOf(post.getResponseBodyAsString()).booleanValue();
		} catch (Exception e) {
			throw new ClientException("Could not connect to server.", e);
		} finally {
			post.releaseConnection();
		}
	}

	/**
	 * Logs the user out of the server.
	 * 
	 * @throws ClientException
	 */
	public void logout() throws ClientException {
		logger.debug("logging out");
		post = new PostMethod(serverURL + AUTHENTICATION_SERVLET);
		NameValuePair[] data = { new NameValuePair("op", "logout") };
		post.setRequestBody(data);

		try {
			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}
		} catch (Exception e) {
			throw new ClientException("Could not connect to server.", e);
		} finally {
			post.releaseConnection();
		}
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
		post = new PostMethod(serverURL + AUTHENTICATION_SERVLET);
		NameValuePair[] data = { new NameValuePair("op", "isLoggedIn") };
		post.setRequestBody(data);

		try {
			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}

			return Boolean.valueOf(post.getResponseBodyAsString()).booleanValue();
		} catch (Exception e) {
			throw new ClientException("Could not connect to server.", e);
		} finally {
			post.releaseConnection();
		}
	}

	/**
	 * Returns a List of all channels.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public List<Channel> getChannels() throws ClientException {
		logger.debug("retrieving channel list");
		post = new PostMethod(serverURL + CONFIGURATION_SERVLET);
		NameValuePair[] data = { new NameValuePair("op", "getChannels") };
		post.setRequestBody(data);

		try {
			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}

			return (List<Channel>) serializer.fromXML(post.getResponseBodyAsString());
		} catch (Exception e) {
			throw new ClientException("Could not connect to server.", e);
		} finally {
			post.releaseConnection();
		}
	}

	/**
	 * Updates the specified channel.
	 * 
	 * @param channel
	 * @throws ClientException
	 */
	public void updateChannel(Channel channel) throws ClientException {
		logger.debug("updating channel: " + channel.getId());
		post = new PostMethod(serverURL + CONFIGURATION_SERVLET);

		try {
			NameValuePair[] data = { new NameValuePair("op", "updateChannel"), new NameValuePair("data", serializer.toXML(channel)) };
			post.setRequestBody(data);

			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}
		} catch (Exception e) {
			throw new ClientException("Could not connect to server.", e);
		} finally {
			post.releaseConnection();
		}
	}

	/**
	 * Removes the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ClientException
	 */
	public void removeChannel(int channelId) throws ClientException {
		logger.debug("removing channel: " + channelId);
		post = new PostMethod(serverURL + CONFIGURATION_SERVLET);

		try {
			NameValuePair[] data = { new NameValuePair("op", "removeChannel"), new NameValuePair("data", String.valueOf(channelId)) };
			post.setRequestBody(data);

			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}
		} catch (Exception e) {
			throw new ClientException("Could not connect to server.", e);
		} finally {
			post.releaseConnection();
		}
	}
	
	/**
	 * Returns a List of all transports.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public Map<String, Transport> getTransports() throws ClientException {
		logger.debug("retrieving transport list");
		post = new PostMethod(serverURL + CONFIGURATION_SERVLET);
		NameValuePair[] data = { new NameValuePair("op", "getTransports") };
		post.setRequestBody(data);

		try {
			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}

			return (Map<String, Transport>) serializer.fromXML(post.getResponseBodyAsString());
		} catch (Exception e) {
			throw new ClientException("Could not connect to server.", e);
		} finally {
			post.releaseConnection();
		}
	}

	/**
	 * Returns a List of all users.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public List<User> getUsers() throws ClientException {
		logger.debug("retrieving user list");
		post = new PostMethod(serverURL + CONFIGURATION_SERVLET);
		NameValuePair[] data = { new NameValuePair("op", "getUsers") };
		post.setRequestBody(data);

		try {
			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}

			return (List<User>) serializer.fromXML(post.getResponseBodyAsString());
		} catch (Exception e) {
			throw new ClientException("Could not connect to server.", e);
		} finally {
			post.releaseConnection();
		}
	}

	/**
	 * Updates a specified user.
	 * 
	 * @param user
	 * @throws ClientException
	 */
	public void updateUser(User user) throws ClientException {
		logger.debug("updating user: " + user.toString());
		post = new PostMethod(serverURL + CONFIGURATION_SERVLET);

		try {
			NameValuePair[] data = { new NameValuePair("op", "updateUser"), new NameValuePair("data", serializer.toXML(user)) };
			post.setRequestBody(data);

			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}
		} catch (Exception e) {
			throw new ClientException("Could not connect to server.", e);
		} finally {
			post.releaseConnection();
		}
	}

	/**
	 * Removes the user with the specified id.
	 * 
	 * @param userId
	 * @throws ClientException
	 */
	public void removeUser(int userId) throws ClientException {
		logger.debug("removing user: " + userId);
		post = new PostMethod(serverURL + CONFIGURATION_SERVLET);

		try {
			NameValuePair[] data = { new NameValuePair("op", "removeUser"), new NameValuePair("data", String.valueOf(userId)) };
			post.setRequestBody(data);

			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}
		} catch (Exception e) {
			throw new ClientException("Could not connect to server.", e);
		} finally {
			post.releaseConnection();
		}
	}

	/**
	 * Returns a Properties object with all server configuration properties.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public Properties getServerProperties() throws ClientException {
		logger.debug("retrieving server properties");
		post = new PostMethod(serverURL + CONFIGURATION_SERVLET);
		NameValuePair[] data = { new NameValuePair("op", "getServerProperties") };
		post.setRequestBody(data);

		try {
			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}

			return (Properties) serializer.fromXML(post.getResponseBodyAsString());
		} catch (Exception e) {
			throw new ClientException("Could not connect to server.", e);
		} finally {
			post.releaseConnection();
		}
	}

	/**
	 * Updates the server configuration properties.
	 * 
	 * @param properties
	 * @throws ClientException
	 */
	public void updateServerProperties(Properties properties) throws ClientException {
		logger.debug("updating server properties");
		post = new PostMethod(serverURL + CONFIGURATION_SERVLET);

		try {
			NameValuePair[] data = { new NameValuePair("op", "updateServerProperties"), new NameValuePair("data", serializer.toXML(properties)) };
			post.setRequestBody(data);

			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}
		} catch (Exception e) {
			throw new ClientException("Could not connect to server.", e);
		} finally {
			post.releaseConnection();
		}
	}

	/**
	 * Returns the latest configuration id.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public int getNextId() throws ClientException {
		logger.debug("retrieving next id");
		post = new PostMethod(serverURL + CONFIGURATION_SERVLET);
		NameValuePair[] data = { new NameValuePair("op", "getNextId") };
		post.setRequestBody(data);

		try {
			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}

			return Integer.parseInt(post.getResponseBodyAsString().trim());
		} catch (Exception e) {
			throw new ClientException("Could not connect to server.", e);
		} finally {
			post.releaseConnection();
		}
	}
	
	/**
	 * Deploys all channels.
	 * 
	 * @throws ClientException
	 */
	public void deployChannels() throws ClientException {
		logger.debug("deploying channels");
		post = new PostMethod(serverURL + CONFIGURATION_SERVLET);
		NameValuePair[] data = { new NameValuePair("op", "deployChannels") };
		post.setRequestBody(data);

		try {
			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("operation failed: " + post.getStatusLine());
			}
		} catch (Exception e) {
			throw new ClientException("Could not connect to server.", e);
		} finally {
			post.releaseConnection();
		}
	}

	/**
	 * Starts the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ClientException
	 */
	public void startChannel(int channelId) throws ClientException {
		logger.debug("starting channel: " + channelId);
		post = new PostMethod(serverURL + STATUS_SERVLET);
		NameValuePair[] data = { new NameValuePair("op", "startChannel"), new NameValuePair("id", String.valueOf(channelId)) };
		post.setRequestBody(data);

		try {
			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("operation failed: " + post.getStatusLine());
			}
		} catch (Exception e) {
			throw new ClientException("Could not connect to server.", e);
		} finally {
			post.releaseConnection();
		}
	}

	/**
	 * Stops the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ClientException
	 */
	public void stopChannel(int channelId) throws ClientException {
		logger.debug("stopping channel: " + channelId);
		post = new PostMethod(serverURL + STATUS_SERVLET);
		NameValuePair[] data = { new NameValuePair("op", "stopChannel"), new NameValuePair("id", String.valueOf(channelId)) };
		post.setRequestBody(data);

		try {
			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("operation failed: " + post.getStatusLine());
			}
		} catch (Exception e) {
			throw new ClientException("Could not connect to server.", e);
		} finally {
			post.releaseConnection();
		}
	}

	/**
	 * Pauses the channel with the specified id.
	 * 
	 * @param id
	 * @throws ClientException
	 */
	public void pauseChannel(int id) throws ClientException {
		logger.debug("pausing channel: " + id);
		post = new PostMethod(serverURL + STATUS_SERVLET);
		NameValuePair[] data = { new NameValuePair("op", "pauseChannel"), new NameValuePair("id", String.valueOf(id)) };
		post.setRequestBody(data);

		try {
			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("operation failed: " + post.getStatusLine());
			}
		} catch (Exception e) {
			throw new ClientException("Could not connect to server.", e);
		} finally {
			post.releaseConnection();
		}
	}

	/**
	 * Resumes the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ClientException
	 */
	public void resumeChannel(int channelId) throws ClientException {
		logger.debug("resuming channel: " + channelId);
		post = new PostMethod(serverURL + STATUS_SERVLET);
		NameValuePair[] data = { new NameValuePair("op", "resumeChannel"), new NameValuePair("id", String.valueOf(channelId)) };
		post.setRequestBody(data);

		try {
			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("operation failed: " + post.getStatusLine());
			}
		} catch (Exception e) {
			throw new ClientException("Could not connect to server.", e);
		} finally {
			post.releaseConnection();
		}
	}

	/**
	 * Returns the statistics for the channel with the specified id.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public Statistics getStatistics(int channelId) throws ClientException {
		logger.debug("retrieving channel statistics: " + channelId);
		post = new PostMethod(serverURL + STATISTICS_SERVLET);
		NameValuePair[] data = { new NameValuePair("op", "getStatistics"), new NameValuePair("id", String.valueOf(channelId)) };
		post.setRequestBody(data);

		try {
			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("operation failed: " + post.getStatusLine());
			}

			return (Statistics) serializer.fromXML(post.getResponseBodyAsString().trim());
		} catch (Exception e) {
			throw new ClientException("Could not connect to server.", e);
		} finally {
			post.releaseConnection();
		}
	}

	/**
	 * Clears the statistics for the channel with the specified id.
	 * 
	 * @param id
	 * @throws ClientException
	 */
	public void clearStatistics(int id) throws ClientException {
		logger.debug("clearing channel statistics: " + id);
		post = new PostMethod(serverURL + STATISTICS_SERVLET);
		NameValuePair[] data = { new NameValuePair("op", "clearStistics"), new NameValuePair("id", String.valueOf(id)) };
		post.setRequestBody(data);

		try {
			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("operation failed: " + post.getStatusLine());
			}
		} catch (Exception e) {
			throw new ClientException("Could not connect to server.", e);
		} finally {
			post.releaseConnection();
		}
	}

	public List<LogEvent> getLogEvents(int channelId) throws ClientException {
		logger.debug("retrieving log event list");
		post = new PostMethod(serverURL + ENTRY_SERVLET);
		NameValuePair[] data = { new NameValuePair("op", "getLogEvents") };
		post.setRequestBody(data);

		try {
			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}

			return (List<LogEvent>) serializer.fromXML(post.getResponseBodyAsString());
		} catch (Exception e) {
			throw new ClientException("Could not connect to server.", e);
		} finally {
			post.releaseConnection();
		}

	}

	public List<MessageEvent> getMessageEvents(int channelId) throws ClientException {
		logger.debug("retrieving message event list");
		post = new PostMethod(serverURL + ENTRY_SERVLET);
		NameValuePair[] data = { new NameValuePair("op", "getMessageEvents") };
		post.setRequestBody(data);

		try {
			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}

			return (List<MessageEvent>) serializer.fromXML(post.getResponseBodyAsString());
		} catch (Exception e) {
			throw new ClientException("Could not connect to server.", e);
		} finally {
			post.releaseConnection();
		}
	}
	
	public List<Status> getStatusList() throws ClientException {
		logger.debug("retrieving channel status list");
		post = new PostMethod(serverURL + STATUS_SERVLET);
		NameValuePair[] data = { new NameValuePair("op", "getStatusList") };
		post.setRequestBody(data);

		try {
			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}

			return (List<Status>) serializer.fromXML(post.getResponseBodyAsString());
		} catch (Exception e) {
			throw new ClientException("Could not connect to server.", e);
		}
	}
}
