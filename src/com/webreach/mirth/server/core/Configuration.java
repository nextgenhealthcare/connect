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


package com.webreach.mirth.server.core;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.webreach.mirth.server.core.util.ChannelMarshaller;
import com.webreach.mirth.server.core.util.ChannelUnmarshaller;
import com.webreach.mirth.server.core.util.ConfigurationBuilderException;
import com.webreach.mirth.server.core.util.ConfigurationException;
import com.webreach.mirth.server.core.util.DatabaseConnection;
import com.webreach.mirth.server.core.util.DatabaseUtil;
import com.webreach.mirth.server.core.util.MarshalException;
import com.webreach.mirth.server.core.util.MuleConfigurationBuilder;
import com.webreach.mirth.server.core.util.PropertyLoader;
import com.webreach.mirth.server.core.util.UnmarshalException;


public class Configuration {
	private List<User> users;
	private Properties properties;
	private List<Transport> transports;
	private List<Channel> channels;
	private static Configuration instance = null;
	private DatabaseConnection dbConnection;
	private boolean initialized = false;
	private Logger logger = Logger.getLogger(Configuration.class);
	
	private Configuration() {}

	public static Configuration getInstance() {
		synchronized (Configuration.class) {
			if (instance == null)
				instance = new Configuration();

			return instance;
		}
	}
	
	public void initialize() throws ConfigurationException {
		logger.debug("initializing configuration");

		loadUsers();
		loadTransports();
		loadProperties();
		loadChannels();

		initialized = true;
	}
	
	public boolean isInitialized() {
		return initialized;
	}

	private void loadUsers() throws ConfigurationException {
		logger.debug("loading user list");
		
		users = new ArrayList<User>();
		ResultSet result = null;
		
		try {
			dbConnection = new DatabaseConnection();
			result = dbConnection.query("SELECT ID, USERNAME, PASSWORD FROM USERS;");
			
			while (result.next()) {
				User user = new User();
				user.setId(result.getInt("ID"));
				user.setUsername(result.getString("USERNAME"));
				user.setPassword(result.getString("PASSWORD"));
				users.add(user);
			}
		} catch (SQLException e) {
			throw new ConfigurationException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}
	}
	
	/**
	 * Returns a <code>List</code> of all Users.
	 * 
	 * @return
	 */
	public List<User> getUsers() throws ConfigurationException {
		if (!isInitialized()) {
			throw new ConfigurationException("Configuration must be initialized first."); 
		} 
		
		return users;
	}
	
	private void storeUsers() throws ConfigurationException {
		logger.debug("writing user list to database");
		
		try {
			// TODO: write the users list to the database
			dbConnection = new DatabaseConnection();
			dbConnection.update("DELETE FROM USERS;");
			
			for (Iterator iter = users.iterator(); iter.hasNext();) {
				User user = (User) iter.next();
				StringBuffer statement = new StringBuffer();
				statement.append("INSERT INTO USERS (ID, USERNAME, PASSWORD)");
				statement.append("'" + user.getId() + "',");
				statement.append("'" + user.getUsername() + "',");
				statement.append("'" + user.getPassword() + "');");
				dbConnection.update(statement.toString());
			}
		} catch (SQLException e) {
			throw new ConfigurationException(e);
		} finally {
			dbConnection.close();
		}
	}
	
	private void loadChannels() throws ConfigurationException {
		logger.debug("loading channel list");
		
		channels = new ArrayList<Channel>();
		ResultSet result = null;
		ChannelUnmarshaller cu = new ChannelUnmarshaller();
		
		try {
			dbConnection = new DatabaseConnection();
			// TODO: create schema for Channels table (attribute name for the XML code)
			result = dbConnection.query("SELECT ID, DATA FROM CHANNELS;");
			
			while (result.next()) {
				Channel channel = cu.unmarshal(result.getString("DATA"));
				channel.setId(result.getInt("ID"));
				channels.add(channel);
			}
		} catch (SQLException e) {
			throw new ConfigurationException(e);
		} catch (UnmarshalException e) {
			throw new ConfigurationException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}
	}
	
	/**
	 * Returns a <code>List</code> containing all Channels.
	 * 
	 * @return
	 */
	public List<Channel> getChannels() throws ConfigurationException {
		if (!isInitialized()) {
			throw new ConfigurationException("Configuration must be initialized first.");
		}
		
		return channels;
	}
	
	private void storeChannels() throws ConfigurationException {
		logger.debug("writing channel list to database");
		
		try {
			dbConnection = new DatabaseConnection();
			
			for (Iterator iter = channels.iterator(); iter.hasNext();) {
				Channel channel = (Channel) iter.next();
				StringBuffer insert = new StringBuffer();
				insert.append("INSERT INTO CHANNELS (ID, NAME, DATA) VALUES(");
				insert.append(channel.getId() + ",");
				insert.append("'" + channel.getName() + "'");
				
				ChannelMarshaller cm = new ChannelMarshaller();
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				cm.marshal(channel, outputStream);
				insert.append("'" + outputStream.toString() + "');");

				dbConnection.update(insert.toString());
			}
		} catch (SQLException e) {
			throw new ConfigurationException(e);
		} catch (MarshalException e) {
			throw new ConfigurationException(e);
		} finally {
			dbConnection.close();
		}
	}
	
	private void loadTransports() throws ConfigurationException {
		transports = new ArrayList<Transport>();
		ResultSet result = null;
		
		try {
			dbConnection = new DatabaseConnection();
			result = dbConnection.query("SELECT NAME, DISPLAY_NAME, CLASS_NAME, PROTOCOL, TRANSFORMERS FROM TRANSPORTS;");
			
			while (result.next()) {
				Transport transport = new Transport();
				transport.setName(result.getString("NAME"));
				transport.setDisplayName(result.getString("DISPLAY_NAME"));
				transport.setClassName(result.getString("CLASS_NAME"));
				transport.setProtocol(result.getString("PROTOCOL"));
				transport.setTransformers(result.getString("TRANSFORMERS"));
				transports.add(transport);
			}
		} catch (SQLException e) {
			throw new ConfigurationException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}
	}
	
	/**
	 * Returns a <code>List</code> containing all Transports.
	 * 
	 * @return
	 */
	public List<Transport> getTransports() throws ConfigurationException {
		if (!isInitialized()) {
			throw new ConfigurationException("Configuration must be initialized first.");
		}
		
		return transports;
	}
	
	private void storeTransports() throws ConfigurationException {
		logger.debug("writing transport list to database");
		
		// TODO: write the transports list to the database
	}
	
	private void loadProperties() throws ConfigurationException {
		properties = PropertyLoader.loadProperties("mirth");
		
		if (properties == null) {
			throw new ConfigurationException("Could not load properties.");
		}
	}
	
	/**
	 * Returns a <code>Properties</code> list.
	 * 
	 * @return
	 */
	public Properties getProperties() throws ConfigurationException {
		if (!isInitialized()) {
			throw new ConfigurationException("Configuration must be initialized first.");
		}
		
		return properties;
	}
	
	private void storeProperties() throws ConfigurationException {
		logger.debug("writing properties to file");
		
		try {
			FileOutputStream fos = new FileOutputStream("mirth.properties");
			properties.store(fos, null);
		} catch (Exception e) {
			throw new ConfigurationException(e);
		}
	}
	
	/**
	 * Stores all configuration information to the database.
	 * 
	 */
	public void store() throws ConfigurationException {
		logger.debug("storing configuration information");

		storeUsers();
		storeTransports();
		storeProperties();
		storeChannels();
	}
	
	public String getMuleConfiguration() throws ConfigurationException {
		try {
			MuleConfigurationBuilder builder = new MuleConfigurationBuilder(getChannels(), getTransports());
			return builder.getConfiguration();
		} catch (ConfigurationException e) {
			throw e;
		} catch (ConfigurationBuilderException e) {
			throw new ConfigurationException("Could not generate Mule configuration.", e);
		}
	}
	
	/**
	 * Returns the next available id if avaiable, otherwise returns -1.
	 * 
	 * @return
	 */
	public int getNextId() throws RuntimeException {
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
			throw new RuntimeException("Could not generate next unique ID.", e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}
		
		return id;
	}
}
