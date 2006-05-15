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


package com.webreach.mirth.managers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Database {
	protected transient Log logger = LogFactory.getLog(Database.class);
	private Connection connection;
	
	public Database(String name) throws Exception {
		Class.forName("org.hsqldb.jdbcDriver");
		connection = DriverManager.getConnection("jdbc:hsqldb:file:database/" + name, "sa", "");
	}

	public void shutdown() throws SQLException {
		logger.debug("shutting down database");

		Statement statement = connection.createStatement();
		statement.execute("SHUTDOWN");
		connection.close();
	}

	public synchronized ResultSet query(String expression) throws SQLException {
		logger.debug("excuting query: " + expression);

		Statement statement = connection.createStatement();
		ResultSet result = statement.executeQuery(expression);
		statement.close();

		return result;
	}

	public synchronized void update(String expression) throws SQLException {
		logger.debug("excuting update: " + expression);

		Statement statement = connection.createStatement();
		int rowCount = statement.executeUpdate(expression);

		if (rowCount == -1) {
			throw new SQLException();
		}

		statement.close();
	}
	public synchronized void update(String expression, ArrayList parameters) throws SQLException {
		logger.debug("excuting update: " + expression);
		
		PreparedStatement statement = connection.prepareStatement(expression);
		ListIterator it = parameters.listIterator();
		while (it.hasNext()){
			statement.setObject(it.nextIndex(), it.next());
		}
		int rowCount = statement.executeUpdate(expression);

		if (rowCount == -1) {
			throw new SQLException();
		}

		statement.close();
	}
	public synchronized void updateString(String expression, ArrayList<String> parameters) throws SQLException {
		logger.debug("excuting update: " + expression);
		
		PreparedStatement statement = connection.prepareStatement(expression);
		Iterator<String> it = parameters.iterator();
		int i = 1;
		while (it.hasNext()){
			statement.setString(i, it.next());
			i++;
		}
		int rowCount = statement.executeUpdate();

		if (rowCount == -1) {
			throw new SQLException();
		}

		statement.close();
	}
}
