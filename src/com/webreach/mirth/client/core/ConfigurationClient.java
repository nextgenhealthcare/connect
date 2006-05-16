package com.webreach.mirth.client.core;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.User;
import com.webreach.mirth.model.bind.ChannelListUnmarshaller;
import com.webreach.mirth.model.bind.ChannelMarshaller;
import com.webreach.mirth.model.bind.Serializer;
import com.webreach.mirth.model.bind.UserListUnmarshaller;
import com.webreach.mirth.model.bind.UserMarshaller;

public class ConfigurationClient {
	private PostMethod post = null;
	private HttpClient client = new HttpClient();
	
	public ConfigurationClient(String serverURL) {
		post = new PostMethod(serverURL);
	}

	/**
	 * Returns a List of all channels.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public List<Channel> getChannels() throws ClientException {
		try {
			NameValuePair[] data = { new NameValuePair("method", "getChannels") };
			post.setRequestBody(data);

			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}

			ChannelListUnmarshaller channelListUnmarshaller = new ChannelListUnmarshaller();
			return channelListUnmarshaller.unmarshal(post.getResponseBodyAsString());
		} catch (Exception e) {
			throw new ClientException(e);
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
		try {
			ChannelMarshaller marshaller = new ChannelMarshaller();
			Serializer serializer = new Serializer();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			serializer.serialize(marshaller.marshal(channel), ChannelMarshaller.cDataElements, os);
			NameValuePair[] data = { new NameValuePair("method", "updateChannel"), new NameValuePair("body", os.toString()) };
			post.setRequestBody(data);

			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}
		} catch (Exception e) {
			throw new ClientException(e);
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
		try {
			NameValuePair[] data = { new NameValuePair("method", "getUsers") };
			post.setRequestBody(data);

			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}

			UserListUnmarshaller userListUnmarshaller = new UserListUnmarshaller();
			return userListUnmarshaller.unmarshal(post.getResponseBodyAsString());
		} catch (Exception e) {
			throw new ClientException(e);
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
		try {
			UserMarshaller marshaller = new UserMarshaller();
			Serializer serializer = new Serializer();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			serializer.serialize(marshaller.marshal(user), UserMarshaller.cDataElements, os);
			NameValuePair[] data = { new NameValuePair("method", "updateUser"), new NameValuePair("body", os.toString()) };
			post.setRequestBody(data);

			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}
		} catch (Exception e) {
			throw new ClientException(e);
		} finally {
			post.releaseConnection();
		}
	}

	/**
	 * Returns the latest configuration identifier.
	 * 
	 * @return
	 * @throws ClientException
	 */
	public int getNextId() throws ClientException {
		try {
			NameValuePair[] data = { new NameValuePair("method", "getNextId") };
			post.setRequestBody(data);

			int statusCode = client.executeMethod(post);

			if (statusCode != HttpStatus.SC_OK) {
				throw new ClientException("method failed: " + post.getStatusLine());
			}

			return Integer.parseInt(post.getResponseBodyAsString().trim());
		} catch (Exception e) {
			throw new ClientException(e);
		} finally {
			post.releaseConnection();
		}
	}

}
