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

	public String getFilterScript(String channelId) throws ControllerException {
		logger.debug("retrieving filter script: channelId=" + channelId);

		DatabaseConnection dbConnection = null;
		ResultSet result = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			Table scripts = new Table("scripts");
			SelectQuery select = new SelectQuery(scripts);
			select.addColumn(scripts, "filter_script");
			select.addCriteria(new MatchCriteria(scripts, "channel_id", MatchCriteria.EQUALS, channelId));
			result = dbConnection.executeQuery(select.toString());
			String filterScript = null;

			while (result.next()) {
				filterScript = result.getString("filter_script");
			}

			return filterScript;
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(result);
			DatabaseUtil.close(dbConnection);
		}
	}

	public String getTransformerScript(String channelId) throws ControllerException {
		logger.debug("retrieving transformer script: channelId=" + channelId);

		DatabaseConnection dbConnection = null;
		ResultSet result = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			Table scripts = new Table("scripts");
			SelectQuery select = new SelectQuery(scripts);
			select.addColumn(scripts, "transformer_script");
			select.addCriteria(new MatchCriteria(scripts, "channel_id", MatchCriteria.EQUALS, channelId));
			result = dbConnection.executeQuery(select.toString());
			String transformerScript = null;

			while (result.next()) {
				transformerScript = result.getString("transformer_script");
			}

			return transformerScript;
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(result);
			DatabaseUtil.close(dbConnection);
		}
	}

	public void putScripts(String channelId, String filterScript, String transformerScript) throws ControllerException {
		logger.debug("adding filter and transformer scripts");
		DatabaseConnection dbConnection = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			String statement = null;
			ArrayList<Object> parameters = new ArrayList<Object>();

			if (getFilterScript(channelId) == null) {
				statement = "insert into scripts (channel_id, filter_script, transformer_script) values (?, ?, ?)";
				parameters.add(channelId);
				parameters.add(filterScript);
				parameters.add(transformerScript);
			} else {
				statement = "update scripts set filter_script = ?, transformer_script = ? where channel_id = ?";
				parameters.add(filterScript);
				parameters.add(transformerScript);
				parameters.add(channelId);
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
