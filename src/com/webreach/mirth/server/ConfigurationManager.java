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


package com.webreach.mirth.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.webreach.mirth.server.core.util.DatabaseConnection;
import com.webreach.mirth.server.core.util.DatabaseUtil;
import com.webreach.mirth.server.core.util.PropertyLoader;

public class ConfigurationManager {
	private Logger logger = Logger.getLogger(ConfigurationManager.class);
	private Properties mirthProperties;	
	
	// singleton pattern
	private static ConfigurationManager instance = null;

	private ConfigurationManager() {}

	public static ConfigurationManager getInstance() {
		synchronized (ConfigurationManager.class) {
			if (instance == null)
				instance = new ConfigurationManager();

			return instance;
		}
	}

	public File getLatestConfiguration() {
		DatabaseConnection dbConnection = new DatabaseConnection();
		mirthProperties = PropertyLoader.loadProperties("mirth");
		ResultSet result = null;
		String data;
		
		try {
			dbConnection = new DatabaseConnection();
			result = dbConnection.query("SELECT * FROM CONFIGURATIONS WHERE DATE_CREATED = MAX(DATE_CREATED);");
			
			if (result.next()) {
				logger.debug("using configuration " + result.getInt("ID") + "[" + result.getTimestamp("DATE_CREATED").toString() + "]");
				data = result.getString("DATA");
				BufferedWriter out = new BufferedWriter(new FileWriter(mirthProperties.getProperty("mule.config")));
		        out.write(data);
		        out.close();
		        return new File(mirthProperties.getProperty("mule.config"));
			} else {
				logger.debug("no configuration found, using default boot file");
				return new File(mirthProperties.getProperty("mule.boot"));
			}
		} catch (Exception e) {
			return null;
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}
	}
}
