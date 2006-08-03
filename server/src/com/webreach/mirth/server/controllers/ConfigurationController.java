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

package com.webreach.mirth.server.controllers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.truemesh.squiggle.SelectQuery;
import com.truemesh.squiggle.Table;
import com.webreach.mirth.model.DriverInfo;
import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.model.Transport;
import com.webreach.mirth.model.converters.ObjectStringSerializer;
import com.webreach.mirth.server.Command;
import com.webreach.mirth.server.CommandQueue;
import com.webreach.mirth.server.builders.MuleConfigurationBuilder;
import com.webreach.mirth.server.util.DatabaseConnection;
import com.webreach.mirth.server.util.DatabaseConnectionFactory;
import com.webreach.mirth.server.util.DatabaseUtil;
import com.webreach.mirth.util.Encrypter;
import com.webreach.mirth.util.PropertyLoader;

/**
 * The ConfigurationController provides access to the Mirth configuration.
 * 
 * @author geraldb
 * 
 */
public class ConfigurationController {
	private static final String CONF_FOLDER = "conf/";
	private Logger logger = Logger.getLogger(this.getClass());
	private SystemLogger systemLogger = new SystemLogger();
	private static File serverPropertiesFile = new File("server.properties");

	public Map<String, Transport> getTransports() throws ControllerException {
		logger.debug("retrieving transport list");

		DatabaseConnection dbConnection = null;
		ResultSet result = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();

			Table transports = new Table("transports");
			SelectQuery select = new SelectQuery(transports);

			select.addColumn(transports, "name");
			select.addColumn(transports, "class_name");
			select.addColumn(transports, "protocol");
			select.addColumn(transports, "transformers");
			select.addColumn(transports, "type");
			select.addColumn(transports, "inbound");
			select.addColumn(transports, "outbound");

			result = dbConnection.executeQuery(select.toString());
			return getTransportMap(result);
		} catch (SQLException e) {
			throw new ControllerException(e);
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
			transport.setName(result.getString("name"));
			transport.setClassName(result.getString("class_name"));
			transport.setProtocol(result.getString("protocol"));
			transport.setTransformers(result.getString("transformers"));
			transport.setType(Transport.Type.valueOf(result.getString("type")));
			transport.setInbound(result.getBoolean("inbound"));
			transport.setOutbound(result.getBoolean("outbound"));
			transports.put(transport.getName(), transport);
		}

		return transports;
	}

	public Properties getServerProperties() throws ControllerException {
		logger.debug("retrieving properties");

		FileInputStream fileInputStream = null;
		
		try {
			serverPropertiesFile.createNewFile();
			fileInputStream = new FileInputStream(serverPropertiesFile);
			Properties properties = new Properties();
			properties.load(fileInputStream);
			return properties;
		} catch (Exception e) {
			throw new ControllerException(e);
		} finally {
			try {
				fileInputStream.close();
			} catch (IOException e) {
				logger.warn(e);
			}
		}
	}

	public void updateServerProperties(Properties properties) throws ControllerException {
		logger.debug("updating server properties");

		FileOutputStream fileOuputStream = null;
		
		try {
			fileOuputStream = new FileOutputStream(serverPropertiesFile);
			properties.store(fileOuputStream, null);
		} catch (Exception e) {
			throw new ControllerException(e);
		} finally {
			try {
				fileOuputStream.flush();
				fileOuputStream.close();
			} catch (IOException e) {
				logger.warn(e);
			}
		}
	}

	public int getNextId() throws ControllerException {
		logger.debug("retrieving next id");

		DatabaseConnection dbConnection = null;
		ResultSet result = null;
		int id = -1;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			result = dbConnection.executeQuery("SELECT NEXT VALUE FOR SEQ_CONFIGURATION FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES WHERE SEQUENCE_NAME='SEQ_CONFIGURATION';");
			result.next();

			if (result.getInt(1) > 0) {
				id = result.getInt(1);
			}
		} catch (SQLException e) {
			throw new ControllerException("Could not generate next unique identifier.", e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}

		return id;
	}

	/**
	 * Creates a new configuration and restarts the Mule engine.
	 * 
	 * @throws ControllerException
	 */
	public void deployChannels() throws ControllerException {
		logger.debug("deploying channels");

		try {
			ChannelController channelController = new ChannelController();
			CommandQueue queue = CommandQueue.getInstance();

			// instantiate a new configuration builder given the current channel
			// and transport list
			MuleConfigurationBuilder builder = new MuleConfigurationBuilder(channelController.getChannels(null), getTransports());
			// add the newly generated configuration to the database
			addConfiguration(builder.getConfiguration());
			// restart the mule engine which will grab the latest configuration
			// from the database
			queue.addCommand(new Command(Command.Operation.RESTART));
		} catch (Exception e) {
			throw new ControllerException(e);
		}

		systemLogger.logSystemEvent(new SystemEvent("Channels deployed."));
	}

	/**
	 * Returns a File with the latest Mule configuration.
	 * 
	 * @return
	 * @throws ControllerException
	 */
	public File getLatestConfiguration() throws ControllerException {
		logger.debug("retrieving latest configuration");

		Properties properties = PropertyLoader.loadProperties("mirth");
		DatabaseConnection dbConnection = null;
		ResultSet result = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			result = dbConnection.executeQuery("SELECT id, date_created, data FROM configurations WHERE date_created IN (SELECT MAX(date_created) FROM configurations);");

			while (result.next()) {
				logger.debug("using configuration " + result.getInt("id") + " created on " + result.getTimestamp("date_created").toString());
				String data = result.getString("data");
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
	 * Removes the most recent configuration from the database. This is used to
	 * revert to the last good configuration.
	 * 
	 * @throws ControllerException
	 */
	public void deleteLatestConfiguration() {
		logger.debug("deleting most recent configuration");
		DatabaseConnection dbConnection = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			StringBuilder statement = new StringBuilder();
			statement.append("DELETE FROM configurations");
			statement.append(" WHERE date_created IN (SELECT MAX(date_created) FROM configurations);");
			dbConnection.executeUpdate(statement.toString());
		} catch (SQLException e) {
			logger.warn("Could not delete latest configuration.", e);
		} finally {
			dbConnection.close();
		}
	}

	/**
	 * Adds a new configuraiton to the database.
	 * 
	 * @param data
	 * @throws ControllerException
	 */
	private void addConfiguration(String data) throws ControllerException {
		logger.debug("adding configuration");

		DatabaseConnection dbConnection = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();

			String insert = "insert into configurations (data) values (?)";
			ArrayList<String> parameters = new ArrayList<String>();
			parameters.add(data);

			dbConnection.executeUpdate(insert, parameters);
		} catch (Exception e) {
			throw new ControllerException(e);
		} finally {
			dbConnection.close();
		}
	}

	public SecretKey getEncryptionKey() throws ControllerException {
		logger.debug("retrieving encryption key");

		DatabaseConnection dbConnection = null;
		ResultSet result = null;
		ObjectStringSerializer serializer = new ObjectStringSerializer();

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			
			Table keys = new Table("keys");
			SelectQuery select = new SelectQuery(keys);
			select.addColumn(keys, "data");
			result = dbConnection.executeQuery(select.toString());

			while (result.next()) {
				logger.debug("encryption key found");
				return (SecretKey) serializer.deserialize(result.getString("data"));
			}
			
			logger.debug("creating new encryption key");
			SecretKey key = KeyGenerator.getInstance(Encrypter.DES_ALGORITHM).generateKey();
			StringBuilder insert = new StringBuilder();
			insert.append("insert into keys (data) values(");
			insert.append("'" + serializer.serialize(key) + "'");
			insert.append(");");
			dbConnection.executeUpdate(insert.toString());
			return key;
		} catch (Exception e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}
	}
	
	public List<DriverInfo> getDatabaseDrivers() throws Exception {
		logger.debug("retrieving database driver list");
		File driversFile = new File(CONF_FOLDER + "dbdrivers.xml");

		if (driversFile.exists()) {
			ArrayList<DriverInfo> drivers = new ArrayList<DriverInfo>();
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(driversFile);
			Element driversElement = document.getDocumentElement();

			for (int i = 0; i < driversElement.getElementsByTagName("driver").getLength(); i++) {
				Element driverElement = (Element) driversElement.getElementsByTagName("driver").item(i);
				DriverInfo driver = new DriverInfo(driverElement.getAttribute("name"), driverElement.getAttribute("class"));
				logger.debug("found database driver: " + driver);
				drivers.add(driver);
			}

			return drivers;
		} else {
			throw new Exception("Could not locate database drivers file: " + driversFile.getAbsolutePath());
		}
	}
}
