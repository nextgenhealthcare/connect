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


package com.webreach.mirth.core.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;


public class DatabaseConnection {
	private Properties mirthProperties;
	private Connection connection;
	private Logger logger = Logger.getLogger(DatabaseConnection.class);
	
	public DatabaseConnection() throws RuntimeException {
		try {
			mirthProperties = PropertyLoader.loadProperties("mirth");
			Class.forName(mirthProperties.getProperty("database.driver"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private Connection getConnection() throws SQLException {
		try {
			return DriverManager.getConnection(mirthProperties.getProperty("database.url"), "sa", "");	
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized ResultSet query(String expression) throws SQLException {
		Statement statement = null;
		ResultSet result = null;
		
		try {
			connection = getConnection();
			statement = connection.createStatement();
			logger.debug("executing query: " + expression);
			result = statement.executeQuery(expression);

			return result;
		} catch (SQLException e) {
			throw e;
		} finally {
			DatabaseUtil.close(statement);
		}
	}

	public synchronized int update(String expression) throws SQLException {
		Statement statement = null;
		
		try {
			connection = getConnection();
			statement = connection.createStatement();
			logger.debug("executing update: " + expression);
			int rowCount = statement.executeUpdate(expression);
			statement.close();

			return rowCount;
		} catch (SQLException e) {
			throw e;
		} finally {
			DatabaseUtil.close(statement);
		}
	}
	
	public void close() throws RuntimeException {
		if (connection == null) {
			logger.warn("database connection cannot be closed");
		}
		
		Statement statement = null;
		
		try {
			statement = connection.createStatement();
			statement.execute("SHUTDOWN");
			logger.debug("closing database connection");
			connection.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			DatabaseUtil.close(statement);
		}
	}
}
