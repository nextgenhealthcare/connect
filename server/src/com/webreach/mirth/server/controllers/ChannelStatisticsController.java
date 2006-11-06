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
import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.truemesh.squiggle.MatchCriteria;
import com.truemesh.squiggle.SelectQuery;
import com.truemesh.squiggle.Table;
import com.webreach.mirth.model.ChannelStatistics;
import com.webreach.mirth.server.util.DatabaseConnection;
import com.webreach.mirth.server.util.DatabaseConnectionFactory;
import com.webreach.mirth.server.util.DatabaseUtil;
import com.webreach.mirth.server.util.JMXConnection;
import com.webreach.mirth.server.util.JMXConnectionFactory;

/**
 * The StatisticsContoller provides access to channel statistics.
 * 
 * @author GeraldB
 * 
 */
public class ChannelStatisticsController {
	private Logger logger = Logger.getLogger(this.getClass());

	public ChannelStatistics getStatistics(String channelId) throws ControllerException {
		ChannelStatistics currentStatistics = getStatisticsObject(channelId);
		
		// this is a fix for Mule's double counting of received messages
		int receivedCount = Double.valueOf(Math.ceil(currentStatistics.getReceivedCount() / 2)).intValue();
		currentStatistics.setReceivedCount(receivedCount);
		
		return currentStatistics;
	}
	
	/**
	 * Returns a Statistics object for the channel with the specified id.
	 * 
	 * @param channelId
	 * @return
	 * @throws ControllerException
	 */
	public ChannelStatistics getStatisticsObject(String channelId) throws ControllerException {
		logger.debug("retrieving statistics: channelId=" + channelId);

		DatabaseConnection dbConnection = null;
		ResultSet result = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();

			Table statistics = new Table("channel_statistics");
			SelectQuery select = new SelectQuery(statistics);

			select.addColumn(statistics, "received");
			select.addColumn(statistics, "sent");
			select.addColumn(statistics, "errors");
			select.addCriteria(new MatchCriteria(statistics, "channel_id", MatchCriteria.EQUALS, channelId));

			result = dbConnection.executeQuery(select.toString());

			ChannelStatistics channelStatistics = new ChannelStatistics();

			while (result.next()) {
				channelStatistics = new ChannelStatistics();
				channelStatistics.setReceivedCount(result.getInt("received"));
				channelStatistics.setSentCount(result.getInt("sent"));
				channelStatistics.setErrorCount(result.getInt("errors"));
			}

			return channelStatistics;
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(result);
			DatabaseUtil.close(dbConnection);
		}
	}

	public void createStatistics(String channelId) throws ControllerException {
		logger.debug("creating channel statistcs: channelId" + channelId);

		DatabaseConnection dbConnection = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			
			String insert = "insert into channel_statistics (channel_id, received, sent, errors) values (?, ?, ?, ?)";
			
			ArrayList<Object> parameters = new ArrayList<Object>();
			parameters.add(channelId);
			parameters.add(0);
			parameters.add(0);
			parameters.add(0);

			dbConnection.executeUpdate(insert, parameters);
		} catch (Exception e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(dbConnection);
		}
	}

	public void incReceivedCount(String channelId) throws ControllerException {
		logger.debug("incrementing received count: channelId=" + channelId);

		DatabaseConnection dbConnection = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();

			String statement = "update channel_statistics set received = ? where channel_id = ?";

			ChannelStatistics currentStatistics = getStatisticsObject(channelId);
			int received = currentStatistics.getReceivedCount() + 1;

			ArrayList<Object> parameters = new ArrayList<Object>();
			parameters.add(received);
			parameters.add(channelId);

			dbConnection.executeUpdate(statement, parameters);
		} catch (Exception e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(dbConnection);
		}
	}

	public void incSentCount(String channelId) throws ControllerException {
		logger.debug("incrementing sent count: channelId=" + channelId);

		DatabaseConnection dbConnection = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();

			String statement = "update channel_statistics set sent = ? where channel_id = ?";

			ChannelStatistics currentStatistics = getStatisticsObject(channelId);
			int sent = currentStatistics.getSentCount() + 1;
			
			ArrayList<Object> parameters = new ArrayList<Object>();
			parameters.add(sent);
			parameters.add(channelId);

			dbConnection.executeUpdate(statement, parameters);
		} catch (Exception e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(dbConnection);
		}
	}

	public void incErrorCount(String channelId) throws ControllerException {
		logger.debug("incrementing error: channelId=" + channelId);

		DatabaseConnection dbConnection = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();

			String statement = "update channel_statistics set errors = ? where channel_id = ?";

			ChannelStatistics currentStatistics = getStatisticsObject(channelId);
			int errors = currentStatistics.getErrorCount() + 1;

			ArrayList<Object> parameters = new ArrayList<Object>();
			parameters.add(errors);
			parameters.add(channelId);

			dbConnection.executeUpdate(statement, parameters);
		} catch (Exception e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(dbConnection);
		}
	}

	/**
	 * Clears all of the statistics for the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ControllerException
	 */
	public void clearStatistics(String channelId) throws ControllerException {
		logger.debug("clearing statistics: channelId=" + channelId);

		// clear the stats in Mule
		
		JMXConnection jmxConnection = null;

		try {
			jmxConnection = JMXConnectionFactory.createJMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "statistics");
			properties.put("name", channelId);
			jmxConnection.invokeOperation(properties, "clear", null, null);
		} catch (Exception e) {
			throw new ControllerException(e);
		} finally {
			jmxConnection.close();
		}
		
		// clear the stats in the database
		
		DatabaseConnection dbConnection = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();

			String statement = "update channel_statistics set received = ?, sent = ?, errors = ? where channel_id = ?";

			ArrayList<Object> parameters = new ArrayList<Object>();
			parameters.add(0);
			parameters.add(0);
			parameters.add(0);
			parameters.add(channelId);

			dbConnection.executeUpdate(statement, parameters);
		} catch (Exception e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(dbConnection);
		}
	}

	@Deprecated
	private int getSentCount(String channelId) {
		logger.debug("retrieving message sent count: channelId=" + channelId);

		try {
			return getStatistic(channelId, "TotalEventsSent");
		} catch (Exception e) {
			return -1;
		}
	}

	// This is a hack to address the fact that this statistic is incorrectly
	// incrememnted by 2 in Mule.
	@Deprecated
	private int getReceivedCount(String channelId) {
		logger.debug("retrieving message received count: " + channelId);

		JMXConnection jmxConnection = null;

		try {
			jmxConnection = JMXConnectionFactory.createJMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "statistics");
			properties.put("name", channelId);
			Double count = ((Long) jmxConnection.getAttribute(properties, "TotalEventsReceived")).doubleValue();
			return Double.valueOf(Math.ceil(count / 2)).intValue();
		} catch (Exception e) {
			return -1;
		} finally {
			jmxConnection.close();
		}
	}

	@Deprecated
	private int getErrorCount(String channelId) {
		logger.debug("retrieving error count: channelId=" + channelId);

		try {
			return getStatistic(channelId, "ExecutionErrors");
		} catch (Exception e) {
			return -1;
		}
	}

	@Deprecated
	private int getStatistic(String channelId, String statistic) throws ControllerException {
		JMXConnection jmxConnection = null;

		try {
			jmxConnection = JMXConnectionFactory.createJMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "statistics");
			properties.put("name", channelId);

			return ((Long) jmxConnection.getAttribute(properties, statistic)).intValue();
		} catch (Exception e) {
			throw new ControllerException("Could not retrieve statistic.");
		} finally {
			jmxConnection.close();
		}
	}

}
