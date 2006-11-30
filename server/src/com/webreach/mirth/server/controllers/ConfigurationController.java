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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.webreach.mirth.model.Configuration;
import com.webreach.mirth.model.DriverInfo;
import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.model.Transport;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.server.Command;
import com.webreach.mirth.server.CommandQueue;
import com.webreach.mirth.server.builders.MuleConfigurationBuilder;
import com.webreach.mirth.util.Encrypter;
import com.webreach.mirth.util.PropertyLoader;

/**
 * The ConfigurationController provides access to the Mirth configuration.
 * 
 * @author geraldb
 * 
 */
public class ConfigurationController {
	private Logger logger = Logger.getLogger(this.getClass());
	private SystemLogger systemLogger = new SystemLogger();
	private static File serverPropertiesFile = new File("server.properties");
	private static Properties versionProperties = PropertyLoader.loadProperties("version");
	private static SecretKey encryptionKey = null;
	private final String CONF_FOLDER = "conf/";
	private SqlMapClient sqlMap = SqlConfig.getSqlMapInstance();

	public void initialize() {
		try {
			loadEncryptionKey();
		} catch (ControllerException e) {
			logger.error("could not initialize configuration settings", e);
		}
	}

	public Map<String, Transport> getTransports() throws ControllerException {
		logger.debug("retrieving transport list");

		try {
			return (Map<String, Transport>) sqlMap.queryForMap("getTransports", null, "name");
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
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

	public String getGuid() throws ControllerException {
		return UUID.randomUUID().toString();
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
			MuleConfigurationBuilder builder = new MuleConfigurationBuilder(channelController.getChannel(null), getTransports());
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

		try {
			Configuration latestConfiguration = (Configuration) sqlMap.queryForObject("getLatestConfiguration");

			if (latestConfiguration != null) {
				logger.debug("using configuration " + latestConfiguration.getId() + " created on " + latestConfiguration.getDateCreated());
				BufferedWriter out = new BufferedWriter(new FileWriter(properties.getProperty("mule.config")));
				out.write(latestConfiguration.getData());
				out.close();
				return new File(properties.getProperty("mule.config"));
			}

			logger.debug("no configuration found, using default boot file");
			return new File(properties.getProperty("mule.boot"));
		} catch (Exception e) {
			logger.error("Could not retrieve latest configuration.", e);
			return null;
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

		try {
			sqlMap.delete("deleteLatestConfiguration");
		} catch (SQLException e) {
			logger.warn("Could not delete latest configuration.", e);
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

		try {
			sqlMap.insert("insertConfiguration", data);
		} catch (Exception e) {
			throw new ControllerException(e);
		}
	}

	public SecretKey getEncryptionKey() {
		return encryptionKey;
	}

	public void loadEncryptionKey() throws ControllerException {
		logger.debug("loading encryption key");

		ObjectXMLSerializer serializer = new ObjectXMLSerializer();

		try {
			String data = (String) sqlMap.queryForObject("getKey");
			boolean isKeyFound = false;

			if (data != null) {
				logger.debug("encryption key found");
				encryptionKey = (SecretKey) serializer.fromXML(data);
				isKeyFound = true;
			}

			if (!isKeyFound) {
				logger.debug("no key found, creating new encryption key");
				encryptionKey = KeyGenerator.getInstance(Encrypter.DES_ALGORITHM).generateKey();
				sqlMap.insert("insertKey", serializer.toXML(encryptionKey));
			}
		} catch (Exception e) {
			throw new ControllerException("error loading encryption key", e);
		}
	}

	public List<DriverInfo> getDatabaseDrivers() throws ControllerException {
		logger.debug("retrieving database driver list");
		File driversFile = new File(CONF_FOLDER + "dbdrivers.xml");

		if (driversFile.exists()) {
			try {
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
			} catch (Exception e) {
				throw new ControllerException("Error during loading of database drivers file: " + driversFile.getAbsolutePath(), e);
			}
		} else {
			throw new ControllerException("Could not locate database drivers file: " + driversFile.getAbsolutePath());
		}
	}

	public String getVersion() {
		return versionProperties.getProperty("mirth.version");
	}

	public String getBuildDate() {
		return versionProperties.getProperty("mirth.date");
	}
}
