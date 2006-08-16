package com.webreach.mirth.server.controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import com.truemesh.squiggle.MatchCriteria;
import com.truemesh.squiggle.SelectQuery;
import com.truemesh.squiggle.Table;
import com.webreach.mirth.model.MessageEvent;
import com.webreach.mirth.model.filters.MessageEventFilter;
import com.webreach.mirth.server.util.DatabaseConnection;
import com.webreach.mirth.server.util.DatabaseConnectionFactory;
import com.webreach.mirth.server.util.DatabaseUtil;
import com.webreach.mirth.util.Encrypter;

/**
 * The MessageLogger is used to store messages as they are processes by a
 * channel.
 * 
 * @author GeraldB
 * 
 */
public class MessageLogger {
	private Logger logger = Logger.getLogger(this.getClass());
	private ConfigurationController configurationController = new ConfigurationController();

	/**
	 * Adds a new message to the database.
	 * 
	 * @param messageEvent
	 * @throws ControllerException
	 */
	public void logMessageEvent(MessageEvent messageEvent) {
		logger.debug("adding message event: channelId=" + messageEvent.getChannelId());

		DatabaseConnection dbConnection = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();

			// begin message data encryption
			Encrypter encrypter = new Encrypter(configurationController.getEncryptionKey());
			String encryptedMessageData = encrypter.encrypt(messageEvent.getMessage());
			// end message data encryption

			String insert = "insert into messages(channel_id, sending_facility, event, control_id, message, status) values (?, ?, ?, ?, ?, ?)";

			ArrayList<Object> parameters = new ArrayList<Object>();
			parameters.add(messageEvent.getChannelId());
			parameters.add(messageEvent.getSendingFacility());
			parameters.add(messageEvent.getEvent());
			parameters.add(messageEvent.getControlId());
			parameters.add(encryptedMessageData);
			parameters.add(messageEvent.getStatus().toString());

			dbConnection.executeUpdate(insert, parameters);
		} catch (Exception e) {
			logger.error("could not log message: channelId=" + messageEvent.getChannelId(), e);
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
		logger.debug("retrieving message event list: filter=" + filter.toString());

		DatabaseConnection dbConnection = null;
		ResultSet result = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();

			Table messages = new Table("messages");
			SelectQuery select = new SelectQuery(messages);

			select.addColumn(messages, "id");
			select.addColumn(messages, "channel_id");
			select.addColumn(messages, "date_created");
			select.addColumn(messages, "sending_facility");
			select.addColumn(messages, "event");
			select.addColumn(messages, "control_id");
			select.addColumn(messages, "message");
			select.addColumn(messages, "status");

			// filter on channelId
			if (filter.getChannelId() != -1) {
				select.addCriteria(new MatchCriteria(messages, "channel_id", MatchCriteria.EQUALS, filter.getChannelId()));
			}

			// filter on start and end date
			if ((filter.getStartDate() != null) && (filter.getEndDate() != null)) {
				String startDate = String.format("%1$tY-%1$tm-%1$td 00:00:00", filter.getStartDate());
				String endDate = String.format("%1$tY-%1$tm-%1$td 23:59:59", filter.getEndDate());

				select.addCriteria(new MatchCriteria(messages, "date_created", MatchCriteria.GREATEREQUAL, startDate));
				select.addCriteria(new MatchCriteria(messages, "date_created", MatchCriteria.LESSEQUAL, endDate));
			}

			// filter on sendingFacility
			if (filter.getSendingFacility() != null) {
				select.addCriteria(new MatchCriteria(messages, "sending_facility", MatchCriteria.LIKE, "%" + filter.getSendingFacility() + "%"));
			}

			// filter on event
			if (filter.getEvent() != null) {
				select.addCriteria(new MatchCriteria(messages, "event", MatchCriteria.LIKE, "%" + filter.getEvent() + "%"));
			}

			// filter on controlId
			if (filter.getControlId() != null) {
				select.addCriteria(new MatchCriteria(messages, "control_id", MatchCriteria.EQUALS, filter.getControlId()));
			}

			// filter on status
			if (filter.getStatus() != null) {
				select.addCriteria(new MatchCriteria(messages, "status", MatchCriteria.EQUALS, filter.getStatus().toString()));
			}

			result = dbConnection.executeQuery(select.toString());
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
		logger.debug("removing message event: messageEventId=" + messageEventId);

		DatabaseConnection dbConnection = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			StringBuilder statement = new StringBuilder();
			statement.append("delete from messages");
			statement.append(" where id = " + messageEventId + ";");
			dbConnection.executeUpdate(statement.toString());
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

		DatabaseConnection dbConnection = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			StringBuilder statement = new StringBuilder();
			statement.append("delete from messages");
			statement.append(" where channel_id = " + channelId + ";");
			dbConnection.executeUpdate(statement.toString());
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			dbConnection.close();
		}
	}

	private List<MessageEvent> getMessageEventList(ResultSet result) throws SQLException {
		ArrayList<MessageEvent> messageEvents = new ArrayList<MessageEvent>();
		Encrypter encrypter = new Encrypter(configurationController.getEncryptionKey());

		while (result.next()) {
			MessageEvent messageEvent = new MessageEvent();
			messageEvent.setId(result.getInt("id"));
			messageEvent.setChannelId(result.getInt("channel_id"));
			Calendar dateCreated = Calendar.getInstance();
			dateCreated.setTimeInMillis(result.getTimestamp("date_created").getTime());
			messageEvent.setDate(dateCreated);
			messageEvent.setSendingFacility(result.getString("sending_facility"));
			messageEvent.setEvent(result.getString("event"));
			messageEvent.setControlId(result.getString("control_id"));
			String messageData = encrypter.decrypt(result.getString("message"));
			messageEvent.setMessage(messageData);
			messageEvent.setStatus(MessageEvent.Status.valueOf(result.getString("status")));
			messageEvents.add(messageEvent);
		}

		return messageEvents;
	}
}
