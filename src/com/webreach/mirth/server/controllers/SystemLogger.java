package com.webreach.mirth.server.controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.truemesh.squiggle.MatchCriteria;
import com.truemesh.squiggle.SelectQuery;
import com.truemesh.squiggle.Table;
import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.model.converters.ObjectSerializer;
import com.webreach.mirth.model.filters.SystemEventFilter;
import com.webreach.mirth.server.util.DatabaseConnection;
import com.webreach.mirth.server.util.DatabaseConnectionFactory;
import com.webreach.mirth.server.util.DatabaseUtil;

public class SystemLogger {
	private Logger logger = Logger.getLogger(SystemLogger.class);
	private ObjectSerializer serializer = new ObjectSerializer();

	/**
	 * Adds a new system event.
	 * 
	 * @param systemEvent
	 * @throws ControllerException
	 */
	public void logSystemEvent(SystemEvent systemEvent) {
		logger.debug("adding log event: " + systemEvent);

		DatabaseConnection dbConnection = null;
		
		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			StringBuilder insert = new StringBuilder();
			insert.append("INSERT INTO EVENTS (EVENT, EVENT_LEVEL, DESCRIPTION, ATTRIBUTES) VALUES(");
			insert.append("'" + systemEvent.getEvent() + "', ");
			insert.append(systemEvent.getLevel() + ", ");
			insert.append("'" + systemEvent.getDescription() + "', ");
			insert.append("'" + serializer.toXML(systemEvent.getAttributes()) + "');");
			dbConnection.executeUpdate(insert.toString());
		} catch (Exception e) {
			logger.error("could not log system event", e);
		}
	}

	/**
	 * Returns a List of all system events.
	 * 
	 * @param channelId
	 * @return
	 * @throws ControllerException
	 */
	public List<SystemEvent> getSystemEvents(SystemEventFilter filter) throws ControllerException {
		logger.debug("retrieving log event list: " + filter);

		DatabaseConnection dbConnection = null;
		ResultSet result = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();

			Table events = new Table("events");
			SelectQuery select = new SelectQuery(events);

			select.addColumn(events, "id");
			select.addColumn(events, "date_created");
			select.addColumn(events, "event");
			select.addColumn(events, "event_level");
			select.addColumn(events, "description");
			select.addColumn(events, "attributes");

			// filter on start and end date
			if ((filter.getStartDate() != null) && (filter.getEndDate() != null)) {
				String startDate = String.format("%1$tY-%1$tm-%1$td 00:00:00", filter.getStartDate());
				String endDate = String.format("%1$tY-%1$tm-%1$td 23:59:59", filter.getEndDate());
				
				select.addCriteria(new MatchCriteria(events, "date_created", MatchCriteria.GREATEREQUAL, startDate));
				select.addCriteria(new MatchCriteria(events, "date_created", MatchCriteria.LESSEQUAL, endDate));
			}

			// filter on level
			if ((filter.getMinLevel() != -1) && (filter.getMaxLevel() != -1)) {
				select.addCriteria(new MatchCriteria(events, "level", MatchCriteria.GREATEREQUAL, filter.getMinLevel()));
				select.addCriteria(new MatchCriteria(events, "level", MatchCriteria.LESSEQUAL, filter.getMaxLevel()));
			}

			// filter on event
			if (filter.getEvent() != null) {
				select.addCriteria(new MatchCriteria(events, "LCASE", "event", MatchCriteria.LIKE, "%" + filter.getEvent().toLowerCase() + "%"));
			}

			result = dbConnection.executeQuery(select.toString());
			return getSystemEventList(result);
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}
	}

	/**
	 * Clears the sysem event list.
	 * 
	 */
	public void clearSystemEvents() throws ControllerException {
		logger.debug("clearing system event list");

		DatabaseConnection dbConnection = null;
		
		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			StringBuilder statement = new StringBuilder();
			statement.append("delete from events;");
			dbConnection.executeUpdate(statement.toString());
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			dbConnection.close();
		}
	}

	private List<SystemEvent> getSystemEventList(ResultSet result) throws SQLException {
		ArrayList<SystemEvent> systemEvents = new ArrayList<SystemEvent>();

		while (result.next()) {
			SystemEvent systemEvent = new SystemEvent();
			systemEvent.setId(result.getInt("id"));
			Calendar dateCreated = Calendar.getInstance();
			dateCreated.setTimeInMillis(result.getTimestamp("date_created").getTime());
			systemEvent.setDate(dateCreated);
			systemEvent.setEvent(result.getString("event"));
			systemEvent.setLevel(result.getInt("event_level"));
			systemEvent.setDescription(result.getString("description"));
			systemEvent.setAttributes((Properties) serializer.fromXML(result.getString("attributes")));
			systemEvents.add(systemEvent);
		}

		return systemEvents;
	}
}
