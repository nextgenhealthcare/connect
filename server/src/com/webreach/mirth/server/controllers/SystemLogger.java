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
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.truemesh.squiggle.MatchCriteria;
import com.truemesh.squiggle.SelectQuery;
import com.truemesh.squiggle.Table;
import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.model.filters.SystemEventFilter;
import com.webreach.mirth.server.util.DatabaseConnection;
import com.webreach.mirth.server.util.DatabaseConnectionFactory;
import com.webreach.mirth.server.util.DatabaseUtil;

public class SystemLogger {
	private Logger logger = Logger.getLogger(this.getClass());
	private ObjectXMLSerializer serializer = new ObjectXMLSerializer();

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
			
			String insert = "insert into events (event, event_level, description, attributes) values(?, ?, ?, ?)";
			
			ArrayList<Object> parameters = new ArrayList<Object>();
			parameters.add(systemEvent.getEvent());
			parameters.add(systemEvent.getLevel().toString());
			parameters.add(systemEvent.getDescription());
			parameters.add(serializer.toXML(systemEvent.getAttributes()));
			
			dbConnection.executeUpdate(insert, parameters);
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

			// filter on status
			if (filter.getLevel() != null) {
				select.addCriteria(new MatchCriteria(events, "event_level", MatchCriteria.EQUALS, filter.getLevel().toString()));
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
			DatabaseUtil.close(dbConnection);
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
			DatabaseUtil.close(dbConnection);
		}
	}

	private List<SystemEvent> getSystemEventList(ResultSet result) throws SQLException {
		ArrayList<SystemEvent> systemEvents = new ArrayList<SystemEvent>();

		while (result.next()) {
			SystemEvent systemEvent = new SystemEvent(result.getString("event"));
			systemEvent.setId(result.getInt("id"));
			Calendar dateCreated = Calendar.getInstance();
			dateCreated.setTimeInMillis(result.getTimestamp("date_created").getTime());
			systemEvent.setDate(dateCreated);
			systemEvent.setLevel(SystemEvent.Level.valueOf(result.getString("event_level")));
			systemEvent.setDescription(result.getString("description"));
			systemEvent.setAttributes((Properties) serializer.fromXML(result.getString("attributes")));
			systemEvents.add(systemEvent);
		}

		return systemEvents;
	}
}
