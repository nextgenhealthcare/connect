package com.webreach.mirth.core;

import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.webreach.mirth.core.util.DatabaseConnection;
import com.webreach.mirth.core.util.DatabaseUtil;
import com.webreach.mirth.core.util.PropertyLoader;


public class Configuration {
	private List<User> users;
	private Properties properties;
	private List<Transport> transports;
	private List<Channel> channels;
	private static Configuration instance = null;
	private DatabaseConnection dbConnection;

	private Configuration() {
		
	}

	public static Configuration getInstance() {
		synchronized (Configuration.class) {
			if (instance == null)
				instance = new Configuration();

			return instance;
		}
	}
	
	public void initialize() {
		loadUsers();
		loadTransports();
		loadProperties();
		loadChannels();
	}

	private void loadUsers() {
		users = new ArrayList<User>();
		ResultSet result = null;
		
		try {
			dbConnection = new DatabaseConnection();
			result = dbConnection.query("SELECT id, username, password FROM Users;");
			
			while (result.next()) {
				User user = new User();
				user.setId(result.getInt("id"));
				user.setUsername(result.getString("username"));
				user.setPassword(result.getString("password"));
				users.add(user);
			}
		} catch (SQLException e) {
			e.printStackTrace();
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
	public List<User> getUsers() {
		return users;
	}
	
	private void storeUsers() {
		// TODO: write the users list to the database
		dbConnection = new DatabaseConnection();
		dbConnection.update("DELETE FROM Users;");
		
		for (Iterator iter = users.iterator(); iter.hasNext();) {
			User user = (User) iter.next();
			StringBuffer statement = new StringBuffer();

			if (user.getId() == -1) {
				statement.append("INSERT");	
			} else {
				statement.append("UPDATE");
			}
		}
		
		dbConnection.close();
	}
	
	private void loadChannels() {
		channels = new ArrayList<Channel>();
		ResultSet result = null;
		
		try {
			dbConnection = new DatabaseConnection();
			// TODO: create schema for Channels table (attribute name for the XML code)
			result = dbConnection.query("SELECT id, xml FROM Channels;");
			
			while (result.next()) {
				Channel channel = marshallChannel(result.getString("xml"));
				channel.setId(result.getInt("id"));
				channels.add(channel);
			}
		} catch (SQLException e) {
			e.printStackTrace();
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
	public List<Channel> getChannels() {
		return channels;
	}
	
	private void storeChannels() {
		// TODO: write the channels list to the database
	}
	
	private void loadTransports() {
		transports = new ArrayList<Transport>();
		ResultSet result = null;
		
		try {
			dbConnection = new DatabaseConnection();
			result = dbConnection.query("SELECT name, displayname, classname, protocol, transformers FROM Transports");
			
			while (result.next()) {
				Transport transport = new Transport();
				transport.setName(result.getString("name"));
				transport.setDisplayName(result.getString("displayname"));
				transport.setClassName(result.getString("classname"));
				transport.setProtocol(result.getString("protocol"));
				transport.setTransformers(result.getString("transformers"));
				transports.add(transport);
			}
		} catch (SQLException e) {
			e.printStackTrace();
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
	public List<Transport> getTransports() {
		return transports;
	}
	
	private void storeTransports() {
		// TODO: write the transports list to the database
	}
	
	private void loadProperties() {
		properties = PropertyLoader.loadProperties("mirth");
	}
	
	/**
	 * Returns a <code>Properties</code> list.
	 * 
	 * @return
	 */
	public Properties getProperties() {
		return properties;
	}
	
	private void storeProperties() {
		try {
			FileOutputStream fos = new FileOutputStream("mirth.properties");
			properties.store(fos, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Stores all configuration information to the database.
	 * 
	 */
	public void store() {
		storeUsers();
		storeTransports();
		storeProperties();
		storeChannels();
	}
	
	public String getMuleConfiguration() {
		// TODO: iterate through channels list and create descriptors
		return null;
	}
	
	private Channel marshallChannel(String xml) {
		// TODO: XML -> DOM -> Channel
		return null;
	}
	
	/**
	 * Returns the next available id if avaiable, otherwise returns -1.
	 * 
	 * @return
	 */
	public int getNextId() {
		dbConnection = new DatabaseConnection();
		ResultSet result = null;
		int id = -1;
		
		try {
			result = dbConnection.query("SELECT NEXT VALUE FOR SEQ_CONFIGURATION FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES WHERE SEQUENCE_NAME='SEQ_CONFIGURATION';");
			result.next();
			id = result.getInt(1);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}
		
		return id;
	}
}
