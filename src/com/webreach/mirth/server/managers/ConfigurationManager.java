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

package com.webreach.mirth.server.managers;

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
import com.webreach.mirth.model.converters.DocumentSerializer;
import com.webreach.mirth.model.converters.ObjectSerializer;
import com.webreach.mirth.server.Command;
import com.webreach.mirth.server.CommandQueue;
import com.webreach.mirth.server.core.MuleConfigurationBuilder;
import com.webreach.mirth.server.core.util.DatabaseConnection;
import com.webreach.mirth.server.core.util.DatabaseUtil;
import com.webreach.mirth.server.core.util.PropertyLoader;

/**
 * The ConfigurationManager provides services for the Mirth configuration.
 * 
 * @author geraldb
 * 
 */
public class ConfigurationManager {
	private Logger logger = Logger.getLogger(ConfigurationManager.class);
	private DatabaseConnection dbConnection;
	private ObjectSerializer serializer = new ObjectSerializer();

	/**
	 * Returns a List containing the user with the specified <code>id</code>.
	 * If the <code>id</code> is <code>null</code>, all users are returned.
	 * 
	 * @param id
	 * @return
	 * @throws ManagerException
	 */
	public List<User> getUsers(Integer id) throws ManagerException {
		logger.debug("retrieving user list: id = " + id);

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
			return getUserList(result);
		} catch (SQLException e) {
			throw new ManagerException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}
	}

	/**
	 * Returns a List of User objects given a ResultSet.
	 * 
	 * @param result
	 * @return
	 * @throws SQLException
	 */
	private List<User> getUserList(ResultSet result) throws SQLException {
		ArrayList<User> users = new ArrayList<User>();

		while (result.next()) {
			User user = new User();
			user.setId(result.getInt("ID"));
			user.setUsername(result.getString("USERNAME"));
			user.setPassword(result.getString("PASSWORD"));
			users.add(user);
		}

		return users;
	}

	/**
	 * Updates the specified user.
	 * 
	 * @param user
	 * @throws ManagerException
	 */
	public void updateUser(User user) throws ManagerException {
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
			throw new ManagerException(e);
		} finally {
			dbConnection.close();
		}
	}

	/**
	 * Removes the user with the specified id.
	 * 
	 * @param userId
	 * @throws ManagerException
	 */
	public void removeUser(int userId) throws ManagerException {
		logger.debug("removing user: " + userId);

		try {
			dbConnection = new DatabaseConnection();
			StringBuffer statement = new StringBuffer();
			statement.append("DELETE FROM USERS");
			statement.append(" WHERE ID = " + userId + ";");
			dbConnection.update(statement.toString());
		} catch (SQLException e) {
			throw new ManagerException(e);
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
	 * @throws ManagerException
	 */
	public List<Channel> getChannels(Integer id) throws ManagerException {
		logger.debug("retrieving channel list: id = " + id);

		ResultSet result = null;

		try {
			dbConnection = new DatabaseConnection();
			StringBuffer query = new StringBuffer();
			query.append("SELECT ID, CHANNEL_DATA FROM CHANNELS");

			if (id != null) {
				query.append(" WHERE ID = " + id);
			}

			query.append(";");
			result = dbConnection.query(query.toString());
			return getChannelList(result);
		} catch (SQLException e) {
			throw new ManagerException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}
	}
	
	/**
	 * Returns a List of Channel objects given a ResultSet.
	 * 
	 * @param result
	 * @return
	 * @throws SQLException
	 */
	private List<Channel> getChannelList(ResultSet result) throws SQLException {
		ArrayList<Channel> channels = new ArrayList<Channel>();

		while (result.next()) {
			Channel channel = (Channel) serializer.fromXML(result.getString("CHANNEL_DATA"));
			channel.setId(result.getInt("ID"));
			channels.add(channel);
		}

		return channels;
	}

	/**
	 * Updates the specified channel.
	 * 
	 * @param channel
	 * @throws ManagerException
	 */
	public void updateChannel(Channel channel) throws ManagerException {
		try {
			dbConnection = new DatabaseConnection();
			StringBuffer statement = new StringBuffer();

			if (getChannels(channel.getId()).isEmpty()) {
				logger.debug("inserting channel: " + channel.getId());
				statement.append("INSERT INTO CHANNELS (ID, CHANNEL_NAME, CHANNEL_DATA) VALUES(");
				statement.append(channel.getId() + ",");
				statement.append("'" + channel.getName() + "',");
				statement.append("'" + serializer.toXML(channel) + "');");
			} else {
				logger.debug("updating channel: " + channel.getId());
				statement.append("UPDATE CHANNELS SET ");
				statement.append("CHANNEL_NAME = '" + channel.getName() + "', ");
				statement.append("CHANNEL_DATA = '" + serializer.toXML(channel) + "'");
				statement.append(" WHERE ID = " + channel.getId() + ";");
			}

			dbConnection.update(statement.toString());
		} catch (SQLException e) {
			throw new ManagerException(e);
		} finally {
			dbConnection.close();
		}
	}

	/**
	 * Removes the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ManagerException
	 */
	public void removeChannel(int channelId) throws ManagerException {
		logger.debug("removing channel: " + channelId);

		try {
			dbConnection = new DatabaseConnection();
			StringBuffer statement = new StringBuffer();
			statement.append("DELETE FROM CHANNELS");
			statement.append(" WHERE ID = " + channelId + ";");
			dbConnection.update(statement.toString());
		} catch (SQLException e) {
			throw new ManagerException(e);
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
	 * @throws ManagerException
	 */
	public Map<String, Transport> getTransports() throws ManagerException {
		logger.debug("retrieving transport list");

		ResultSet result = null;

		try {
			dbConnection = new DatabaseConnection();
			StringBuffer query = new StringBuffer();
			query.append("SELECT NAME, DISPLAY_NAME, CLASS_NAME, PROTOCOL, TRANSFORMERS FROM TRANSPORTS;");
			result = dbConnection.query(query.toString());
			return getTransportMap(result);
		} catch (SQLException e) {
			throw new ManagerException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}
	}
	
	/**
	 * Returns a Map of Transports given a ResultSet.
	 * 
	 * @param result
	 * @return
	 * @throws SQLException
	 */
	private Map<String, Transport> getTransportMap(ResultSet result) throws SQLException {
		Map<String, Transport> transports = new HashMap<String, Transport>();

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
	}

	public Properties getServerProperties() throws ManagerException {
		logger.debug("retrieving properties");

		Properties properties = PropertyLoader.loadProperties("mirth");

		if (properties == null) {
			throw new ManagerException("Could not load properties.");
		} else {
			return properties;
		}
	}

	public void updateServerProperties(Properties properties) throws ManagerException {
		logger.debug("updating properties");

		try {
			FileOutputStream fos = new FileOutputStream("mirth.properties");
			properties.store(fos, null);
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	public int getNextId() throws ManagerException {
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
			throw new ManagerException("Could not generate next unique identifier.", e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}

		return id;
	}

	/**
	 * Creates a new configuration and restarts the Mule engine.
	 * 
	 * @throws ManagerException
	 */
	public void deployChannels() throws ManagerException {
		logger.debug("deploying channels");

		try {
			CommandQueue queue = CommandQueue.getInstance();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			DocumentSerializer docSerializer = new DocumentSerializer();

			// instantiate a new configuration builder given the current channel
			// and transport list
			MuleConfigurationBuilder builder = new MuleConfigurationBuilder(getChannels(null), getTransports());
			// generate the new configuraton and serialize it to XML
			docSerializer.serialize(builder.getConfiguration(), MuleConfigurationBuilder.cDataElements, os);
			// add the newly generated configuration to the database
			addConfiguration(os.toString());
			// restart the mule engine which will grab the latest configuration
			// from the database
			queue.addCommand(new Command(Command.CMD_RESTART_MULE));
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/**
	 * Returns a File with the latest Mule configuration.
	 * 
	 * @return
	 * @throws ManagerException
	 */
	public File getConfiguration() throws ManagerException {
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

	/**
	 * Adds a new configuraiton to the database.
	 * 
	 * @param data
	 * @throws ManagerException
	 */
	private void addConfiguration(String data) throws ManagerException {
		logger.debug("adding configuration");

		try {
			dbConnection = new DatabaseConnection();
			StringBuffer insert = new StringBuffer();
			insert.append("INSERT INTO CONFIGURATIONS (DATE_CREATED, DATA) VALUES (");
			insert.append("'" + DatabaseUtil.getNowTimestamp() + "',");
			insert.append("'" + data + "');");
			dbConnection.update(insert.toString());
		} catch (Exception e) {
			throw new ManagerException(e);
		} finally {
			dbConnection.close();
		}
	}

}
