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

package com.webreach.mirth.server.services;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Transport;
import com.webreach.mirth.model.User;
import com.webreach.mirth.model.bind.ChannelMarshaller;
import com.webreach.mirth.model.bind.ChannelUnmarshaller;
import com.webreach.mirth.model.bind.MarshalException;
import com.webreach.mirth.model.bind.Serializer;
import com.webreach.mirth.model.bind.UnmarshalException;
import com.webreach.mirth.server.Command;
import com.webreach.mirth.server.CommandQueue;
import com.webreach.mirth.server.core.MuleConfigurationBuilder;
import com.webreach.mirth.server.core.util.DatabaseConnection;
import com.webreach.mirth.server.core.util.DatabaseUtil;
import com.webreach.mirth.server.core.util.PropertyLoader;

/**
 * The ConfigurationService provides services for the Mirth configuration.
 * 
 * @author geraldb
 * 
 */
public class ConfigurationService {
	private Logger logger = Logger.getLogger(ConfigurationService.class);
	private DatabaseConnection dbConnection;

	/**
	 * Returns a List containing the user with the specified <code>id</code>.
	 * If the <code>id</code> is <code>null</code>, all users are returned.
	 * 
	 * @param id
	 * @return
	 * @throws ServiceException
	 */
	public List<User> getUsers(Integer id) throws ServiceException {
		logger.debug("retrieving user list: id = " + id);

		ArrayList<User> users = new ArrayList<User>();
		ResultSet result = null;

		try {
			dbConnection = new DatabaseConnection();
			StringBuffer query = new StringBuffer();
			query.append("SELECT ID, USERNAME, PASSWORD FROM USERS");

			if (id != null) {
				query.append(" WHERE ID = " + id.toString());
			}

			query.append(";");
			result = dbConnection.query(query.toString());

			while (result.next()) {
				User user = new User();
				user.setId(result.getInt("ID"));
				user.setUsername(result.getString("USERNAME"));
				user.setPassword(result.getString("PASSWORD"));
				users.add(user);
			}

			return users;
		} catch (SQLException e) {
			throw new ServiceException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}
	}

	/**
	 * Updates the specified user.
	 * 
	 * @param user
	 * @throws ServiceException
	 */
	public void updateUser(User user) throws ServiceException {
		logger.debug("updating user: " + user.toString());

		try {
			dbConnection = new DatabaseConnection();
			StringBuffer statement = new StringBuffer();

			if (getUsers(user.getId()).isEmpty()) {
				statement.append("INSERT INTO USERS (ID, USERNAME, PASSWORD) VALUES(");
				statement.append("'" + user.getId() + "',");
				statement.append("'" + user.getUsername() + "',");
				statement.append("'" + user.getPassword() + "');");
			} else {
				statement.append("UPDATE USERS SET ");
				statement.append("USERNAME = '" + user.getUsername() + "',");
				statement.append("PASSWORD = '" + user.getPassword() + "'");
				statement.append(" WHERE ID = " + user.getId() + ";");
			}

			dbConnection.update(statement.toString());
		} catch (SQLException e) {
			throw new ServiceException(e);
		} finally {
			dbConnection.close();
		}
	}

	/**
	 * Returns a List containing the channel with the specified <code>id</code>.
	 * If the <code>id</code> is <code>null</code>, all channels are
	 * returned.
	 * 
	 * @param id
	 * @return
	 * @throws ServiceException
	 */
	public List<Channel> getChannels(Integer id) throws ServiceException {
		logger.debug("retrieving user list: id = " + id);

		ArrayList<Channel> channels = new ArrayList<Channel>();
		ResultSet result = null;
		ChannelUnmarshaller cu = new ChannelUnmarshaller();

		try {
			dbConnection = new DatabaseConnection();
			StringBuffer query = new StringBuffer();
			query.append("SELECT ID, CHANNEL_DATA FROM CHANNELS");

			if (id != null) {
				query.append(" WHERE ID = " + id);
			}

			query.append(";");
			result = dbConnection.query(query.toString());

			while (result.next()) {
				Channel channel = cu.unmarshal(result.getString("CHANNEL_DATA"));
				channel.setId(result.getInt("ID"));
				channels.add(channel);
			}

			return channels;
		} catch (SQLException e) {
			throw new ServiceException(e);
		} catch (UnmarshalException e) {
			throw new ServiceException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}
	}

	/**
	 * Updates the specified channel.
	 * 
	 * @param channel
	 * @throws ServiceException
	 */
	public void updateChannel(Channel channel) throws ServiceException {
		logger.debug("updating channel: " + channel.getId());

		ChannelMarshaller marshaller = new ChannelMarshaller();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Serializer serializer = new Serializer();

		try {
			dbConnection = new DatabaseConnection();
			StringBuffer statement = new StringBuffer();

			if (getChannels(channel.getId()).isEmpty()) {
				statement.append("INSERT INTO CHANNELS (ID, CHANNEL_NAME, CHANNEL_DATA) VALUES(");
				statement.append(channel.getId() + ",");
				statement.append("'" + channel.getName() + "'");
				serializer.serialize(marshaller.marshal(channel), ChannelMarshaller.cDataElements, os);
				statement.append("'" + os.toString() + "');");
			} else {
				statement.append("UPDATE CHANNELS SET");
				statement.append("CHANNEL_NAME = '" + channel.getName() + "'");
				serializer.serialize(marshaller.marshal(channel), ChannelMarshaller.cDataElements, os);
				statement.append("CHANNEL_DATA = '" + os.toString() + "'");
				statement.append(" WHERE ID = " + channel.getId() + ";");
			}

			dbConnection.update(statement.toString());
		} catch (SQLException e) {
			throw new ServiceException(e);
		} catch (MarshalException e) {
			throw new ServiceException(e);
		} finally {
			dbConnection.close();
		}
	}

	/**
	 * Returns a List containing the transport with the specified
	 * <code>id</code>. If the <code>id</code> is <code>null</code>, all
	 * transports are returned.
	 * 
	 * @return
	 * @throws ServiceException
	 */
	public Map<String, Transport> getTransports() throws ServiceException {
		logger.debug("retrieving transport list");

		Map<String, Transport> transports = new HashMap<String, Transport>();
		ResultSet result = null;

		try {
			dbConnection = new DatabaseConnection();
			StringBuffer query = new StringBuffer();
			query.append("SELECT NAME, DISPLAY_NAME, CLASS_NAME, PROTOCOL, TRANSFORMERS FROM TRANSPORTS;");
			result = dbConnection.query(query.toString());

			while (result.next()) {
				Transport transport = new Transport();
				transport.setName(result.getString("NAME"));
				transport.setDisplayName(result.getString("DISPLAY_NAME"));
				transport.setClassName(result.getString("CLASS_NAME"));
				transport.setProtocol(result.getString("PROTOCOL"));
				transport.setTransformers(result.getString("TRANSFORMERS"));
				transports.put(transport.getName(), transport);
			}

			return transports;
		} catch (SQLException e) {
			throw new ServiceException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}
	}

	public Properties getProperties() throws ServiceException {
		logger.debug("retrieving properties");

		Properties properties = PropertyLoader.loadProperties("mirth");

		if (properties == null) {
			throw new ServiceException("Could not load properties.");
		} else {
			return properties;
		}
	}

	public void updateProperties(Properties properties) throws ServiceException {
		logger.debug("updating properties");

		try {
			FileOutputStream fos = new FileOutputStream("mirth.properties");
			properties.store(fos, null);
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	public int getNextId() throws ServiceException {
		logger.debug("retrieving next id");

		dbConnection = new DatabaseConnection();
		ResultSet result = null;
		int id = -1;

		try {
			result = dbConnection.query("SELECT NEXT VALUE FOR SEQ_CONFIGURATION FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES WHERE SEQUENCE_NAME='SEQ_CONFIGURATION';");
			result.next();

			if (result.getInt(1) > 0) {
				id = result.getInt(1);
			}
		} catch (SQLException e) {
			throw new ServiceException("Could not generate next unique identifier.", e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}

		return id;
	}

	/**
	 * Creates a new configuration and restarts the Mule engine.
	 * 
	 * @throws ServiceException
	 */
	public void deployChannels() throws ServiceException {
		logger.debug("deploying channels");

		try {
			CommandQueue queue = CommandQueue.getInstance();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			Serializer serializer = new Serializer();

			// instantiate a new configuration builder given the current channel
			// and transport list
			MuleConfigurationBuilder builder = new MuleConfigurationBuilder(getChannels(null), getTransports());
			// generate the new configuraton and serialize it to XML
			serializer.serialize(builder.getConfiguration(), MuleConfigurationBuilder.cDataElements, os);
			// add the newly generated configuration to the database
			addConfiguration(os.toString());
			// restart the mule engine which will grab the latest configuration
			// from the database
			queue.addCommand(new Command(Command.CMD_RESTART_MULE));
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	/**
	 * Returns a File with the latest Mule configuration.
	 * 
	 * @return
	 * @throws ServiceException
	 */
	public File getConfiguration() throws ServiceException {
		logger.debug("retrieving configuration");

		Properties properties = PropertyLoader.loadProperties("mirth");
		ResultSet result = null;
		try {
			dbConnection = new DatabaseConnection();
			result = dbConnection.query("SELECT ID, DATE_CREATED, DATA FROM CONFIGURATIONS WHERE DATE_CREATED IN (SELECT MAX(DATE_CREATED) FROM CONFIGURATIONS);");

			while (result.next()) {
				logger.debug("using configuration ID" + result.getInt("ID") + " created @ " + result.getTimestamp("DATE_CREATED").toString());
				String data = result.getString("DATA");
				BufferedWriter out = new BufferedWriter(new FileWriter(properties.getProperty("mule.config")));
				out.write(data);
				out.close();
				return new File(properties.getProperty("mule.config"));
			}

			logger.debug("no configuration found, using default boot file");
			return new File(properties.getProperty("mule.boot"));
		} catch (Exception e) {
			return null;
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}
	}

	private void addConfiguration(String data) throws ServiceException {
		logger.debug("adding configuration");
		
		try {
			dbConnection = new DatabaseConnection();
			StringBuffer insert = new StringBuffer();
			insert.append("INSERT INTO CONFIGURATIONS (DATE_CREATED, DATA) VALUES (");
			insert.append("'" + DatabaseUtil.getNowTimestamp() + "',");
			insert.append("'" + data + "');");
			dbConnection.update(insert.toString());
		} catch (Exception e) {
			throw new ServiceException(e);
		} finally {
			dbConnection.close();
		}
	}

}
