package com.webreach.mirth.server.controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.truemesh.squiggle.MatchCriteria;
import com.truemesh.squiggle.SelectQuery;
import com.truemesh.squiggle.Table;
import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.server.controllers.filters.SystemEventFilter;
import com.webreach.mirth.server.util.DatabaseConnection;
import com.webreach.mirth.server.util.DatabaseUtil;

public class SystemLogger {
	private Logger logger = Logger.getLogger(SystemLogger.class);
	private DatabaseConnection dbConnection;
	
	/**
	 * Adds a new system event.
	 * 
	 * @param systemEvent
	 * @throws ControllerException
	 */
	public void logSystemEvent(SystemEvent systemEvent) throws ControllerException {
		logger.debug("adding log event: " + systemEvent.getChannelId());
		
		try {
			dbConnection = new DatabaseConnection();	
			StringBuilder insert = new StringBuilder();
			insert.append("INSERT INTO EVENTS (CHANNEL_ID, DATE_CREATED, EVENT, EVENT_LEVEL) VALUES(");
			insert.append(systemEvent.getChannelId() + ", ");
			insert.append("'" + DatabaseUtil.getNowTimestamp() + "', ");
			insert.append("'" + systemEvent.getDescription() + "', ");
			insert.append(systemEvent.getLevel() + ";");
			dbConnection.update(insert.toString());
		} catch (Exception e) {
			throw new ControllerException("Could not add system event for channel " + systemEvent.getChannelId(), e);
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

			// filter on id
			if (filter.getId() != -1) {
				select.addCriteria(new MatchCriteria(events, "id", MatchCriteria.EQUALS, filter.getId()));
			}

			// filter on channelId
			if (filter.getChannelId() != -1) {
				select.addCriteria(new MatchCriteria(events, "channel_id", MatchCriteria.EQUALS, filter.getChannelId()));
			}

			// filter on min and max date
			if ((filter.getMinDate() != null) && (filter.getMaxDate() != null)) {
				select.addCriteria(new MatchCriteria(events, "date_created", MatchCriteria.GREATEREQUAL, filter.getMinDate().toString()));
				select.addCriteria(new MatchCriteria(events, "date_created", MatchCriteria.LESSEQUAL, filter.getMaxDate().toString()));
			}
			
			// filter on level
			if ((filter.getMinLevel() != -1) && (filter.getMaxLevel() != -1)) {
				select.addCriteria(new MatchCriteria(events, "level", MatchCriteria.GREATEREQUAL, filter.getMinLevel()));
				select.addCriteria(new MatchCriteria(events, "level", MatchCriteria.LESSEQUAL, filter.getMaxLevel()));
			}

			// filter on event
			if (filter.getEvent() != null) {
				select.addCriteria(new MatchCriteria(events, "event", MatchCriteria.EQUALS, filter.getEvent()));
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
	
	private List<SystemEvent> getSystemEventList(ResultSet result) throws SQLException {
		ArrayList<SystemEvent> systemEvents = new ArrayList<SystemEvent>();

		while (result.next()) {
			SystemEvent systemEvent = new SystemEvent();
			systemEvent.setId(result.getInt("ID"));
			systemEvent.setChannelId(result.getInt("CHANNEL_ID"));
			systemEvent.setDate(result.getTimestamp("DATE_CREATED"));
			systemEvent.setDescription(result.getString("EVENT"));
			systemEvent.setLevel(result.getInt("EVENT_LEVEL"));
			systemEvents.add(systemEvent);
		}

		return systemEvents;
	}
}
