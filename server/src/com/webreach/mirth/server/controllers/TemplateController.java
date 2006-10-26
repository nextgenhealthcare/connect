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

public class TemplateController {
	private Logger logger = Logger.getLogger(this.getClass());

	/**
	 * Returns the template with the specified id, null otherwise.
	 * 
	 * @param id
	 * @return
	 * @throws ControllerException
	 */
	public String getTemplate(String id) throws ControllerException {
		logger.debug("retrieving template: id=" + id);

		DatabaseConnection dbConnection = null;
		ResultSet result = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			Table templates = new Table("templates");
			SelectQuery select = new SelectQuery(templates);
			select.addColumn(templates, "template");
			select.addCriteria(new MatchCriteria(templates, "id", MatchCriteria.EQUALS, id));
			result = dbConnection.executeQuery(select.toString());
			String script = null;

			while (result.next()) {
				script = result.getString("template");
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
	 * Adds a template with the specified id to the database. If a template with the
	 * id already exists it will be overwritten.
	 * 
	 * @param id
	 * @param template
	 * @throws ControllerException
	 */
	public void putTemplate(String id, String template) throws ControllerException {
		logger.debug("adding template: id=" + id);
		DatabaseConnection dbConnection = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			String statement = null;
			ArrayList<Object> parameters = new ArrayList<Object>();

			if (getTemplate(id) == null) {
				statement = "insert into templates (id, template) values (?, ?)";
				parameters.add(id);
				parameters.add(template);
			} else {
				statement = "update templates set template = ? where id = ?";
				parameters.add(template);
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
