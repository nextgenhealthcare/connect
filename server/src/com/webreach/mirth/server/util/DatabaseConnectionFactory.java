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

package com.webreach.mirth.server.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.webreach.mirth.util.PropertyLoader;

public class DatabaseConnectionFactory {
	public static DatabaseConnection createDatabaseConnection() throws SQLException {
		Properties properties = PropertyLoader.loadProperties("mirth");

		try {
			Class.forName(PropertyLoader.getProperty(properties, "database.driver"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		Properties info = new Properties();
		info.setProperty("user", PropertyLoader.getProperty(properties, "database.user"));
		info.setProperty("password", PropertyLoader.getProperty(properties, "database.password"));
		return new DatabaseConnection(PropertyLoader.getProperty(properties, "database.url"), info);
	}

	public static DatabaseConnection createDatabaseConnection(String driver, String address, String username, String password) throws SQLException {
		try {
			Class.forName(driver);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Properties info = new Properties();
		info.setProperty("user", username);
		info.setProperty("password", password);
		
		// this property should only be set if it's for embedded database
		info.setProperty("shutdown", "true");

		return new DatabaseConnection(address, info);
	}
	
	public static DatabaseConnection createDatabaseConnection(String driver, String address) throws SQLException {
		try {
			Class.forName(driver);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Properties info = new Properties();
		
		// this property should only be set if it's for embedded database
		info.setProperty("shutdown", "true");

		return new DatabaseConnection(address, info);
	}

	public static Connection createConnection(String driver, String address, String username, String password) throws SQLException {
		try {
			Class.forName(driver);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Properties info = new Properties();
		info.setProperty("user", username);
		info.setProperty("password", password);
		
		// this property should only be set if it's for embedded database
		info.setProperty("shutdown", "true");
		
		return DriverManager.getConnection(address, info);
	}
	//Initializes the specified Driver. This is used for JS function that can't call
	// "Class.forName"
	public static void initializeDriver(String driver) throws Exception {
		Class.forName(driver);
	}
}
