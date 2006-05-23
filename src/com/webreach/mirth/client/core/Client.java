package com.webreach.mirth.client.core;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Statistics;
import com.webreach.mirth.model.Transport;
import com.webreach.mirth.model.User;
import com.webreach.mirth.model.Channel.Status;
import com.webreach.mirth.model.bind.ChannelListUnmarshaller;
import com.webreach.mirth.model.bind.ChannelMarshaller;
import com.webreach.mirth.model.bind.PropertiesMarshaller;
import com.webreach.mirth.model.bind.PropertiesUnmarshaller;
import com.webreach.mirth.model.bind.Serializer;
import com.webreach.mirth.model.bind.StatisticsUnmarshaller;
import com.webreach.mirth.model.bind.TransportMapUnmarshaller;
import com.webreach.mirth.model.bind.UserListUnmarshaller;
import com.webreach.mirth.model.bind.UserMarshaller;

public class Client {
	private Logger logger = Logger.getLogger(Client.class);
	private String serverURL = null;
	private HttpClient client = new HttpClient();
	private PostMethod post = null;

	private final String AUTHENTICATION_SERVLET = "/authentication";
	private final String CONFIGURATION_SERVLET = "/configuration";
	private final String STATUS_SERVLET = "/status";
	private final String STATISTICS_SERVLET = "/statistics";

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
		NameValuePair[] data = { new NameValuePair("method", "getChannels") };
		post.setRequestBody(data);

		try {
			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}

			ChannelListUnmarshaller unmarshaller = new ChannelListUnmarshaller();
			return unmarshaller.unmarshal(post.getResponseBodyAsString());
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
		ChannelMarshaller marshaller = new ChannelMarshaller();
		Serializer serializer = new Serializer();
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			serializer.serialize(marshaller.marshal(channel), ChannelMarshaller.cDataElements, os);
			NameValuePair[] data = { new NameValuePair("method", "updateChannel"), new NameValuePair("body", os.toString()) };
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
		NameValuePair[] data = { new NameValuePair("method", "getTransports") };
		post.setRequestBody(data);

		try {
			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}

			TransportMapUnmarshaller unmarshaller = new TransportMapUnmarshaller();
			return unmarshaller.unmarshal(post.getResponseBodyAsString());
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
		NameValuePair[] data = { new NameValuePair("method", "getUsers") };
		post.setRequestBody(data);

		try {
			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}

			UserListUnmarshaller unmarshaller = new UserListUnmarshaller();
			return unmarshaller.unmarshal(post.getResponseBodyAsString());
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
		UserMarshaller marshaller = new UserMarshaller();
		Serializer serializer = new Serializer();
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			serializer.serialize(marshaller.marshal(user), UserMarshaller.cDataElements, os);
			NameValuePair[] data = { new NameValuePair("method", "updateUser"), new NameValuePair("body", os.toString()) };
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
	 * Returns a Properties object with all configuration properties.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public Properties getProperties() throws ClientException {
		logger.debug("retrieving properties");
		post = new PostMethod(serverURL + CONFIGURATION_SERVLET);
		NameValuePair[] data = { new NameValuePair("method", "getProperties") };
		post.setRequestBody(data);

		try {
			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}

			PropertiesUnmarshaller unmarshaller = new PropertiesUnmarshaller();
			return unmarshaller.unmarshal(post.getResponseBodyAsString());
		} catch (Exception e) {
			throw new ClientException("Could not connect to server.", e);
		} finally {
			post.releaseConnection();
		}
	}

	/**
	 * Updates the configuration properties.
	 * 
	 * @param properties
	 * @throws ClientException
	 */
	public void updateProperties(Properties properties) throws ClientException {
		logger.debug("updating properties");
		post = new PostMethod(serverURL + CONFIGURATION_SERVLET);
		PropertiesMarshaller marshaller = new PropertiesMarshaller();
		Serializer serializer = new Serializer();
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			serializer.serialize(marshaller.marshal(properties), PropertiesMarshaller.cDataElements, os);
			NameValuePair[] data = { new NameValuePair("method", "updateProperties"), new NameValuePair("body", os.toString()) };
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
		NameValuePair[] data = { new NameValuePair("method", "getNextId") };
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
	 * Starts the channel with the specified id.
	 * 
	 * @param id
	 * @throws ClientException
	 */
	public void startChannel(int id) throws ClientException {
		logger.debug("starting channel: " + id);
		post = new PostMethod(serverURL + STATUS_SERVLET);
		NameValuePair[] data = { new NameValuePair("op", "startChannel"), new NameValuePair("id", String.valueOf(id)) };
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
	 * @param id
	 * @throws ClientException
	 */
	public void stopChannel(int id) throws ClientException {
		logger.debug("stopping channel: " + id);
		post = new PostMethod(serverURL + STATUS_SERVLET);
		NameValuePair[] data = { new NameValuePair("op", "stopChannel"), new NameValuePair("id", String.valueOf(id)) };
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
	 * @param id
	 * @throws ClientException
	 */
	public void resumeChannel(int id) throws ClientException {
		logger.debug("resuming channel: " + id);
		post = new PostMethod(serverURL + STATUS_SERVLET);
		NameValuePair[] data = { new NameValuePair("op", "resumeChannel"), new NameValuePair("id", String.valueOf(id)) };
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
	 * Returns the status of the channel with the specified id.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public Status getChannelStatus(int id) throws ClientException {
		logger.debug("retrieving channel status: " + id);
		post = new PostMethod(serverURL + STATUS_SERVLET);
		NameValuePair[] data = { new NameValuePair("op", "getChannelStatus"), new NameValuePair("id", String.valueOf(id)) };
		post.setRequestBody(data);

		try {
			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("operation failed: " + post.getStatusLine());
			}

			return Status.valueOf(post.getResponseBodyAsString().trim());
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
	public Statistics getChannelStatistics(int id) throws ClientException {
		logger.debug("retrieving channel statistics: " + id);
		post = new PostMethod(serverURL + STATISTICS_SERVLET);
		NameValuePair[] data = { new NameValuePair("op", "getChannelStatistics"), new NameValuePair("id", String.valueOf(id)) };
		post.setRequestBody(data);

		try {
			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("operation failed: " + post.getStatusLine());
			}

			StatisticsUnmarshaller unmarshaller = new StatisticsUnmarshaller();
			return unmarshaller.unmarshal(post.getResponseBodyAsString().trim());
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
	public void clearChannelStatistics(int id) throws ClientException {
		logger.debug("clearing channel statistics: " + id);
		post = new PostMethod(serverURL + STATISTICS_SERVLET);
		NameValuePair[] data = { new NameValuePair("op", "clearChannelStistics"), new NameValuePair("id", String.valueOf(id)) };
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

}
