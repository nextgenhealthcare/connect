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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.truemesh.squiggle.MatchCriteria;
import com.truemesh.squiggle.SelectQuery;
import com.truemesh.squiggle.Table;
import com.webreach.mirth.server.util.DatabaseConnection;
import com.webreach.mirth.server.util.DatabaseConnectionFactory;
import com.webreach.mirth.server.util.DatabaseUtil;

public class ScriptController {
	private Logger logger = Logger.getLogger(this.getClass());

	/**
	 * Returns the script with the specified id, null otherwise.
	 * 
	 * @param id
	 * @return
	 * @throws ControllerException
	 */
	public String getScript(String id) throws ControllerException {
		logger.debug("retrieving script: id=" + id);

		DatabaseConnection dbConnection = null;
		ResultSet result = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			Table scripts = new Table("scripts");
			SelectQuery select = new SelectQuery(scripts);
			select.addColumn(scripts, "script");
			select.addCriteria(new MatchCriteria(scripts, "id", MatchCriteria.EQUALS, id));
			result = dbConnection.executeQuery(select.toString());
			String script = null;

			while (result.next()) {
				script = result.getString("script");
			}

			return script;
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(result);
			DatabaseUtil.close(dbConnection);
		}
	}

	/**
	 * Adds a script with the specified id to the database. If a script with the
	 * id already exists it will be overwritten.
	 * 
	 * @param id
	 * @param script
	 * @throws ControllerException
	 */
	public void putScript(String id, String script) throws ControllerException {
		logger.debug("adding script: id=" + id);
		DatabaseConnection dbConnection = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			String statement = null;
			ArrayList<Object> parameters = new ArrayList<Object>();

			if (getScript(id) == null) {
				statement = "insert into scripts (id, script) values (?, ?)";
				parameters.add(id);
				parameters.add(script);
			} else {
				statement = "update scripts set script = ? where id = ?";
				parameters.add(script);
				parameters.add(id);
			}

			dbConnection.executeUpdate(statement, parameters);
		} catch (SQLException e) {
			throw new ControllerException(e);
		} catch (ControllerException e) {
			throw e;
		} finally {
			DatabaseUtil.close(dbConnection);
		}
	}
}
