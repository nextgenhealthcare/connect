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

	public ChannelStatistics getStatistics(int channelId) throws ControllerException {
		updateStatistics(channelId);
		return getStatisticsObject(channelId);
	}
	
	/**
	 * Returns a Statistics object for the channel with the specified id.
	 * 
	 * @param channelId
	 * @return
	 * @throws ControllerException
	 */
	public ChannelStatistics getStatisticsObject(int channelId) throws ControllerException {
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
			dbConnection.close();
		}
	}

	public void createStatistics(int channelId) throws ControllerException {
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
			dbConnection.close();
		}
	}

	public void updateStatistics(int channelId) throws ControllerException {
		logger.debug("updating channel statistics: channelId=" + channelId);

		DatabaseConnection dbConnection = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();

			String statement = "update channel_statistics set received = ?, sent = ?, errors = ? where channel_id = ?";

			ChannelStatistics channelStatistics = getStatisticsObject(channelId);
			int received = channelStatistics.getReceivedCount() + getReceivedCount(channelId);
			int sent = channelStatistics.getSentCount() + getSentCount(channelId);
			int errors = channelStatistics.getErrorCount() + getErrorCount(channelId);

			ArrayList<Object> parameters = new ArrayList<Object>();
			parameters.add(received);
			parameters.add(sent);
			parameters.add(errors);
			parameters.add(channelId);

			dbConnection.executeUpdate(statement, parameters);
		} catch (Exception e) {
			throw new ControllerException(e);
		} finally {
			dbConnection.close();
		}
	}

	/**
	 * Clears all of the statistics for the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ControllerException
	 */
	public void clearStatistics(int channelId) throws ControllerException {
		logger.debug("clearing statistics: channelId=" + channelId);

		// clear the stats in Mule
		
		JMXConnection jmxConnection = null;

		try {
			jmxConnection = JMXConnectionFactory.createJMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "statistics");
			properties.put("name", String.valueOf(channelId));
			jmxConnection.invokeOperation(properties, "clear", null);
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
			dbConnection.close();
		}
	}

	private int getSentCount(int channelId) {
		logger.debug("retrieving message sent count: channelId=" + channelId);

		try {
			return getStatistic(channelId, "TotalEventsSent");
		} catch (Exception e) {
			return -1;
		}
	}

	// This is a hack to address the fact that this statistic is incorrectly
	// incrememnted by 2 in Mule.
	private int getReceivedCount(int channelId) {
		logger.debug("retrieving message received count: " + channelId);

		JMXConnection jmxConnection = null;

		try {
			jmxConnection = JMXConnectionFactory.createJMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "statistics");
			properties.put("name", String.valueOf(channelId));
			Double count = ((Long) jmxConnection.getAttribute(properties, "TotalEventsReceived")).doubleValue();
			return Double.valueOf(Math.ceil(count / 2)).intValue();
		} catch (Exception e) {
			return -1;
		} finally {
			jmxConnection.close();
		}
	}

	private int getErrorCount(int channelId) {
		logger.debug("retrieving error count: channelId=" + channelId);

		try {
			return getStatistic(channelId, "ExecutionErrors");
		} catch (Exception e) {
			return -1;
		}
	}

	private int getStatistic(int channelId, String statistic) throws ControllerException {
		JMXConnection jmxConnection = null;

		try {
			jmxConnection = JMXConnectionFactory.createJMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "statistics");
			properties.put("name", String.valueOf(channelId));

			return ((Long) jmxConnection.getAttribute(properties, statistic)).intValue();
		} catch (Exception e) {
			throw new ControllerException("Could not retrieve statistic.");
		} finally {
			jmxConnection.close();
		}
	}

}
