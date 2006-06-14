package com.webreach.mirth.server.controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.truemesh.squiggle.MatchCriteria;
import com.truemesh.squiggle.SelectQuery;
import com.truemesh.squiggle.Table;
import com.webreach.mirth.model.MessageEvent;
import com.webreach.mirth.server.controllers.filters.MessageEventFilter;
import com.webreach.mirth.server.util.DatabaseConnection;
import com.webreach.mirth.server.util.DatabaseUtil;

/**
 * The MessageLogger is used to store messages as they are processes by a channel.
 * 
 * @author GeraldB
 *
 */
public class MessageLogger {
	private Logger logger = Logger.getLogger(MessageLogger.class);
	private DatabaseConnection dbConnection;
	
	/**
	 * Adds a new message to the database.
	 * 
	 * @param messageEvent
	 * @throws ControllerException
	 */
	public void logMessageEvent(MessageEvent messageEvent) {
		logger.debug("adding message event: " + messageEvent.getChannelId());
		
		try {
			dbConnection = new DatabaseConnection();	
			StringBuilder insert = new StringBuilder();
			insert.append("INSERT INTO MESSAGES (CHANNEL_ID, DATE_CREATED, SENDING_FACILITY, EVENT, CONTROL_ID, MESSAGE) VALUES(");
			insert.append(messageEvent.getChannelId() + ", ");
			insert.append("'" + DatabaseUtil.getNowTimestamp() + "', ");
			insert.append("'" + messageEvent.getSendingFacility() + "', ");
			insert.append("'" + messageEvent.getEvent() + "', ");
			insert.append("'" + messageEvent.getControlId() + "', ");
			insert.append("'" + messageEvent.getMessage() + "');");
			dbConnection.update(insert.toString());
		} catch (Exception e) {
			logger.error("could not log message: channel id = " + messageEvent.getChannelId(), e);
		}
	}
	
	/**
	 * Returns a List of all messages.
	 * 
	 * @param channelId
	 * @return
	 * @throws ControllerException
	 */
	public List<MessageEvent> getMessageEvents(MessageEventFilter filter) throws ControllerException {
		logger.debug("retrieving message event list");
		
		ResultSet result = null;
		
		try {
			dbConnection = new DatabaseConnection();
			
			Table messages = new Table("messages");
			SelectQuery select = new SelectQuery(messages);
			
			select.addColumn(messages, "id");
			select.addColumn(messages, "channel_id");
			select.addColumn(messages, "date_created");
			select.addColumn(messages, "sending_facility");
			select.addColumn(messages, "event");
			select.addColumn(messages, "control_id");
			select.addColumn(messages, "message");
			
			// filter on id
			if (filter.getId() != -1) {
				select.addCriteria(new MatchCriteria(messages, "id", MatchCriteria.EQUALS, filter.getId()));
			}
			
			// filter on channelId
			if (filter.getChannelId() != -1) {
				select.addCriteria(new MatchCriteria(messages, "channel_id", MatchCriteria.EQUALS, filter.getChannelId()));
			}
			
			// filter on min and max date
			if ((filter.getMinDate() != null) && (filter.getMaxDate() != null)) {
				select.addCriteria(new MatchCriteria(messages, "date_created", MatchCriteria.GREATEREQUAL, filter.getMinDate().toString()));
				select.addCriteria(new MatchCriteria(messages, "date_created", MatchCriteria.LESSEQUAL, filter.getMaxDate().toString()));
			}

			// filter on sendingFacility
			if (filter.getId() != -1) {
				select.addCriteria(new MatchCriteria(messages, "sending_facility", MatchCriteria.EQUALS, filter.getSendingFacility()));
			}
			
			// filter on event
			if (filter.getEvent() != null) {
				select.addCriteria(new MatchCriteria(messages, "event", MatchCriteria.EQUALS, filter.getEvent()));
			}

			// filter on controlId
			if (filter.getEvent() != null) {
				select.addCriteria(new MatchCriteria(messages, "control_id", MatchCriteria.EQUALS, filter.getControlId()));
			}

			result = dbConnection.query(select.toString());
			return getMessageEventList(result);
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}		
	}
	
	/**
	 * Removes the message with the specified id.
	 * 
	 * @param messageEventId
	 */
	public void removeMessageEvent(int messageEventId) throws ControllerException {
		logger.debug("removing message event: " + messageEventId);
		
		try {
			dbConnection = new DatabaseConnection();
			StringBuilder statement = new StringBuilder();
			statement.append("delete from messages");
			statement.append(" where id = " + messageEventId + ";");
			dbConnection.update(statement.toString());
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			dbConnection.close();
		}
	}
	
	/**
	 * Clears the message list for the channel with the specified id.
	 * 
	 * @param channelId
	 */
	public void clearMessageEvents(int channelId) throws ControllerException {
		logger.debug("clearing message events: " + channelId);

		try {
			dbConnection = new DatabaseConnection();
			StringBuilder statement = new StringBuilder();
			statement.append("delete from messages");
			statement.append(" where channel_id = " + channelId + ";");
			dbConnection.update(statement.toString());
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			dbConnection.close();
		}
	}

	private List<MessageEvent> getMessageEventList(ResultSet result) throws SQLException {
		ArrayList<MessageEvent> messageEvents = new ArrayList<MessageEvent>();
		
		while (result.next()) {
			MessageEvent messageEvent = new MessageEvent();
			messageEvent.setId(result.getInt("id"));
			messageEvent.setChannelId(result.getInt("channel_id"));
			messageEvent.setDate(result.getTimestamp("date_created"));
			messageEvent.setSendingFacility(result.getString("sending_facility"));
			messageEvent.setEvent(result.getString("event"));
			messageEvent.setControlId(result.getString("control_id"));
			messageEvent.setMessage(result.getString("message"));
			messageEvents.add(messageEvent);
		}

		return messageEvents;
	}
}
