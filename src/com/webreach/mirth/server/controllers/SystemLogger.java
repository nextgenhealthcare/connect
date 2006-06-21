package com.webreach.mirth.server.controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
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
import com.webreach.mirth.server.util.DatabaseUtil;

public class SystemLogger {
	private Logger logger = Logger.getLogger(SystemLogger.class);
	private ObjectSerializer serializer = new ObjectSerializer();
	private DatabaseConnection dbConnection;

	/**
	 * Adds a new system event.
	 * 
	 * @param systemEvent
	 * @throws ControllerException
	 */
	public void logSystemEvent(SystemEvent systemEvent) {
		logger.debug("adding log event: " + systemEvent.getChannelId());

		try {
			dbConnection = new DatabaseConnection();
			StringBuilder insert = new StringBuilder();
			insert.append("INSERT INTO EVENTS (CHANNEL_ID, DATE_CREATED, EVENT, EVENT_LEVEL, DESCRIPTION, ATTRIBUTES) VALUES(");
			insert.append(systemEvent.getChannelId() + ", ");
			insert.append("'" + DatabaseUtil.getNow() + "', ");
			insert.append("'" + systemEvent.getEvent() + "', ");
			insert.append(systemEvent.getLevel() + ", ");
			insert.append("'" + systemEvent.getDescription() + "', ");
			insert.append("'" + serializer.toXML(systemEvent.getAttributes()) + "');");
			dbConnection.update(insert.toString());
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
		logger.debug("retrieving log event list");

		ResultSet result = null;

		try {
			dbConnection = new DatabaseConnection();

			Table events = new Table("events");
			SelectQuery select = new SelectQuery(events);

			select.addColumn(events, "id");
			select.addColumn(events, "channel_id");
			select.addColumn(events, "date_created");
			select.addColumn(events, "event");
			select.addColumn(events, "event_level");
			select.addColumn(events, "description");
			select.addColumn(events, "attributes");

			// filter on channelId
			if (filter.getChannelId() != -1) {
				select.addCriteria(new MatchCriteria(events, "channel_id", MatchCriteria.EQUALS, filter.getChannelId()));
			}

			// filter on min and max date
			if ((filter.getStartDate() != null) && (filter.getEndDate() != null)) {
				select.addCriteria(new MatchCriteria(events, "date_created", MatchCriteria.GREATEREQUAL, new Timestamp(filter.getStartDate().getTimeInMillis()).toString()));
				select.addCriteria(new MatchCriteria(events, "date_created", MatchCriteria.LESSEQUAL, new Timestamp(filter.getEndDate().getTimeInMillis()).toString()));
			}

			// filter on level
			if ((filter.getMinLevel() != -1) && (filter.getMaxLevel() != -1)) {
				select.addCriteria(new MatchCriteria(events, "level", MatchCriteria.GREATEREQUAL, filter.getMinLevel()));
				select.addCriteria(new MatchCriteria(events, "level", MatchCriteria.LESSEQUAL, filter.getMaxLevel()));
			}

			// filter on event
			if (filter.getEvent() != null) {
				select.addCriteria(new MatchCriteria(events, "event", MatchCriteria.LIKE, filter.getEvent()));
			}

			result = dbConnection.query(select.toString());
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

		try {
			dbConnection = new DatabaseConnection();
			StringBuilder statement = new StringBuilder();
			statement.append("delete from events;");
			dbConnection.update(statement.toString());
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
			systemEvent.setChannelId(result.getInt("channel_id"));
			systemEvent.setDate(result.getTimestamp("date_created"));
			systemEvent.setEvent(result.getString("event"));
			systemEvent.setLevel(result.getInt("event_level"));
			systemEvent.setDescription(result.getString("description"));
			systemEvent.setAttributes((Properties) serializer.fromXML(result.getString("attributes")));
			systemEvents.add(systemEvent);
		}

		return systemEvents;
	}
}
