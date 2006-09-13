package com.webreach.mirth.server.controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.truemesh.squiggle.MatchCriteria;
import com.truemesh.squiggle.Order;
import com.truemesh.squiggle.SelectQuery;
import com.truemesh.squiggle.Table;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.model.filters.MessageObjectFilter;
import com.webreach.mirth.server.util.DatabaseConnection;
import com.webreach.mirth.server.util.DatabaseConnectionFactory;
import com.webreach.mirth.server.util.DatabaseUtil;
import com.webreach.mirth.util.Encrypter;

public class MessageObjectController {
	private Logger logger = Logger.getLogger(this.getClass());
	private ConfigurationController configurationController = new ConfigurationController();
	private	Table messages = new Table("messages");

	public void updateMessage(MessageObject messageObject) {
		logger.debug("updating message: channelId=" + messageObject.getChannelId());

		DatabaseConnection dbConnection = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();

			ObjectXMLSerializer serializer = new ObjectXMLSerializer();
			Encrypter encrypter = new Encrypter(configurationController.getEncryptionKey());

			String statement = null;
			ArrayList<Object> parameters = new ArrayList<Object>();

			String rawData;
			String transformedData;
			String encodedData;

			if (messageObject.isEncrypted()) {
				rawData = encrypter.encrypt(messageObject.getRawData());
				transformedData = encrypter.encrypt(messageObject.getTransformedData());
				encodedData = encrypter.encrypt(messageObject.getEncodedData());
			} else {
				rawData = messageObject.getRawData();
				transformedData = messageObject.getTransformedData();
				encodedData = messageObject.getEncodedData();
			}

			MessageObjectFilter filter = new MessageObjectFilter();
			filter.setId(messageObject.getId());
			
			if (getMessageCount(filter) == 0) {
				logger.debug("inserting message: id=" + messageObject.getId());
				statement = "insert into messages (id, channel_id, date_created, status, raw_data, raw_data_protocol, transformed_data, transformed_data_protocol, encoded_data, encoded_data_protocol variable_map) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

				parameters.add(messageObject.getId());
				parameters.add(messageObject.getChannelId());
				parameters.add(messageObject.getDateCreated());
				parameters.add(messageObject.getStatus());
				parameters.add(rawData);
				parameters.add(messageObject.getRawDataProtocol());
				parameters.add(transformedData);
				parameters.add(messageObject.getTransformedDataProtocol());
				parameters.add(encodedData);
				parameters.add(messageObject.getEncodedDataProtocol());
				parameters.add(serializer.toXML(messageObject.getVariableMap()));
				parameters.add(messageObject.getDestination());
			} else {
				logger.debug("updating message: id=" + messageObject.getId());
				statement = "updated messages set status = ?, raw_data = ?, transformed_data = ?, encoded_data = ?, variable_map = ? where id = ?";

				parameters.add(messageObject.getStatus());
				parameters.add(rawData);
				parameters.add(transformedData);
				parameters.add(encodedData);
				parameters.add(serializer.toXML(messageObject.getVariableMap()));
				parameters.add(messageObject.getId());
			}
			
			dbConnection.executeUpdate(statement, parameters);
		} catch (Exception e) {
			logger.error("could not log message: id=" + messageObject.getId(), e);
		}
	}

	public List<MessageObject> getMessages(MessageObjectFilter filter) throws ControllerException {
		logger.debug("retrieving message list: filter=" + filter.toString());

		DatabaseConnection dbConnection = null;
		ResultSet result = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();

			SelectQuery select = new SelectQuery(messages);
			
			select.addColumn(messages, "id");
			select.addColumn(messages, "channel_id");
			select.addColumn(messages, "date_created");
			select.addColumn(messages, "status");
			select.addColumn(messages, "raw_data");
			select.addColumn(messages, "raw_data_protocol");
			select.addColumn(messages, "transformed_data");
			select.addColumn(messages, "transformed_data_protocol");
			select.addColumn(messages, "encoded_data");
			select.addColumn(messages, "encoded_data_protocol");
			select.addColumn(messages, "variable_map");
			addFilterCriteria(select, filter);
			select.addOrder(messages, "date_created", Order.DESCENDING);
			
			String query = select.toString();
			
			if ((filter.getPage() != -1) && (filter.getPageSize() != -1)) {
				int limit = filter.getPageSize();
				int offset = filter.getPageSize() * filter.getPage();
				
				if (offset > 0)
					query += " LIMIT " + limit + " OFFSET " + offset;
				else 
					query += " LIMIT " + limit;
			}
			
			result = dbConnection.executeQuery(query.toString());
			return getMessageList(result);
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}
	}

	public int getMessageCount(MessageObjectFilter filter) throws ControllerException {
		logger.debug("retrieving message count: filter=" + filter.toString());

		DatabaseConnection dbConnection = null;
		ResultSet result = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			SelectQuery select = new SelectQuery(messages);
			select.addColumn(messages, "count", "id");
			addFilterCriteria(select, filter);			
			result = dbConnection.executeQuery(select.toString());
			
			while (result.next()) {
				return result.getInt(1);	
			}
			
			return -1;
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}
	}

	private void addFilterCriteria(SelectQuery select, MessageObjectFilter filter) {
		// filter on id
		if (filter.getId() != null) {
			select.addCriteria(new MatchCriteria(messages, "id", MatchCriteria.EQUALS, filter.getId()));
		}
		
		// filter on channelId
		if (filter.getChannelId() != null) {
			select.addCriteria(new MatchCriteria(messages, "channel_id", MatchCriteria.EQUALS, filter.getChannelId()));
		}

		// filter on start and end date
		if ((filter.getStartDate() != null) && (filter.getEndDate() != null)) {
			String startDate = String.format("%1$tY-%1$tm-%1$td 00:00:00", filter.getStartDate());
			String endDate = String.format("%1$tY-%1$tm-%1$td 23:59:59", filter.getEndDate());

			select.addCriteria(new MatchCriteria(messages, "date_created", MatchCriteria.GREATEREQUAL, startDate));
			select.addCriteria(new MatchCriteria(messages, "date_created", MatchCriteria.LESSEQUAL, endDate));
		}

		// filter on status
		if (filter.getStatus() != null) {
			select.addCriteria(new MatchCriteria(messages, "status", MatchCriteria.EQUALS, filter.getStatus().toString()));
		}
	}
	
	public void removeMessage(String messageObjectId) throws ControllerException {
		logger.debug("removing message: id=" + messageObjectId);

		DatabaseConnection dbConnection = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			StringBuilder statement = new StringBuilder();
			statement.append("delete from messages");
			statement.append(" where id = '" + messageObjectId + "';");
			dbConnection.executeUpdate(statement.toString());
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			dbConnection.close();
		}
	}

	public void clearMessages(String channelId) throws ControllerException {
		logger.debug("clearing message events: " + channelId);

		DatabaseConnection dbConnection = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			StringBuilder statement = new StringBuilder();
			statement.append("delete from messages");
			statement.append(" where channel_id = '" + channelId + "';");
			dbConnection.executeUpdate(statement.toString());
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			dbConnection.close();
		}
	}

	private List<MessageObject> getMessageList(ResultSet result) throws SQLException {
		ArrayList<MessageObject> messageObjects = new ArrayList<MessageObject>();
		ObjectXMLSerializer serializer = new ObjectXMLSerializer();
		Encrypter encrypter = new Encrypter(configurationController.getEncryptionKey());

		while (result.next()) {
			MessageObject messageObject = new MessageObject();
			messageObject.setId(result.getString("id"));
			messageObject.setChannelId(result.getString("channel_id"));
			messageObject.setStatus(MessageObject.Status.valueOf(result.getString("status")));
			
			Calendar dateCreated = Calendar.getInstance();
			dateCreated.setTimeInMillis(result.getTimestamp("date_created").getTime());
			messageObject.setDateCreated(dateCreated);

			String rawData;
			String transformedData;
			String encodedData;

			if (messageObject.isEncrypted()) {
				rawData = encrypter.decrypt(result.getString("raw_data"));
				transformedData = encrypter.decrypt(result.getString("transformed_data"));
				encodedData = encrypter.decrypt(result.getString("encoded_data"));
			} else {
				rawData = result.getString("raw_data");
				transformedData = result.getString("transformed_data");
				encodedData = result.getString("encoded_data");
			}
			
			messageObject.setRawData(rawData);
			messageObject.setRawDataProtocol(MessageObject.Protocol.valueOf(result.getString("raw_data_protocol")));
			messageObject.setTransformedData(transformedData);
			messageObject.setTransformedDataProtocol(MessageObject.Protocol.valueOf(result.getString("transformed_data_protocol")));
			messageObject.setEncodedData(encodedData);
			messageObject.setEncodedDataProtocol(MessageObject.Protocol.valueOf(result.getString("encoded_data_protocol")));
			messageObject.setVariableMap((Map) serializer.fromXML(result.getString("variable_map")));
			
			messageObjects.add(messageObject);
		}

		return messageObjects;
	}
}
