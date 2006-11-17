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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.mule.extras.client.MuleClient;
import org.mule.umo.UMOException;

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
import com.webreach.mirth.server.util.sql.Delete;
import com.webreach.mirth.util.Encrypter;

public class MessageObjectController {
	private Logger logger = Logger.getLogger(this.getClass());
	private ConfigurationController configurationController = new ConfigurationController();
	private MonitorMessageObjectController monitorController = new MonitorMessageObjectController();
	private Table messages = new Table("messages");

	public void updateMessage(MessageObject messageObject) {
		logger.debug("updating message: channelId=" + messageObject.getChannelId());
		monitorController.addMonitorMessage(messageObject);

		DatabaseConnection dbConnection = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();

			ObjectXMLSerializer serializer = new ObjectXMLSerializer();
			Encrypter encrypter = new Encrypter(configurationController.getEncryptionKey());

			String statement = null;
			ArrayList<Object> parameters = new ArrayList<Object>();

			String rawData = new String();
			String transformedData = new String();
			String encodedData = new String();

			if (messageObject.isEncrypted()) {
				if (messageObject.getRawData() != null) {
					rawData = encrypter.encrypt(messageObject.getRawData());
				}

				if (messageObject.getTransformedData() != null) {
					transformedData = encrypter.encrypt(messageObject.getTransformedData());
				}

				if (messageObject.getEncodedData() != null) {
					encodedData = encrypter.encrypt(messageObject.getEncodedData());
				}
			} else {
				rawData = messageObject.getRawData();
				transformedData = messageObject.getTransformedData();
				encodedData = messageObject.getEncodedData();
			}

			MessageObjectFilter filter = new MessageObjectFilter();
			filter.setId(messageObject.getId());

			if (getMessageCount(filter) == 0) {
				logger.debug("inserting message: id=" + messageObject.getId());
				statement = "insert into messages (id, channel_id, date_created, version, encrypted, status, raw_data, raw_data_protocol, transformed_data, transformed_data_protocol, encoded_data, encoded_data_protocol, variable_map, connector_name, errors) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

				parameters.add(messageObject.getId());
				parameters.add(messageObject.getChannelId());
				parameters.add(new Timestamp(messageObject.getDateCreated().getTimeInMillis()));
				parameters.add(messageObject.getVersion());
				parameters.add(messageObject.isEncrypted());
				parameters.add(messageObject.getStatus());
				parameters.add(rawData);
				parameters.add(messageObject.getRawDataProtocol());
				parameters.add(transformedData);
				parameters.add(messageObject.getTransformedDataProtocol());
				parameters.add(encodedData);
				parameters.add(messageObject.getEncodedDataProtocol());

				// convert the values in the variable map to strings
				Map variableMap = messageObject.getVariableMap();

				for (Iterator iter = variableMap.entrySet().iterator(); iter.hasNext();) {
					Entry entry = (Entry) iter.next();
					entry.setValue(entry.getValue().toString());
				}

				parameters.add(serializer.toXML(variableMap));
				parameters.add(messageObject.getConnectorName());
				parameters.add(messageObject.getErrors());
			} else {
				logger.debug("updating message: id=" + messageObject.getId());
				statement = "update messages set status = ?, raw_data = ?, transformed_data = ?, encoded_data = ?, variable_map = ?, errors = ? where id = ?";

				parameters.add(messageObject.getStatus());
				parameters.add(rawData);
				parameters.add(transformedData);
				parameters.add(encodedData);

				// convert the values in the variable map to strings
				Map variableMap = messageObject.getVariableMap();

				for (Iterator iter = variableMap.entrySet().iterator(); iter.hasNext();) {
					Entry entry = (Entry) iter.next();
					entry.setValue(entry.getValue().toString());
				}

				parameters.add(serializer.toXML(variableMap));
				parameters.add(messageObject.getErrors());
				parameters.add(messageObject.getId());
			}

			dbConnection.executeUpdate(statement, parameters);
		} catch (Exception e) {
			logger.error("could not log message: id=" + messageObject.getId(), e);
		}
	}

	public List<MessageObject> getMessagesByPage(int page, int pageSize) throws ControllerException {
		logger.debug("retrieving messages by page: page=" + page);

		DatabaseConnection dbConnection = null;
		ResultSet result = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();

			StringBuilder query = new StringBuilder();
			query.append("select messages.* from messages_temp, messages where messages_temp.id = messages.sequence_id");
			
			if ((page != -1) && (pageSize != -1)) {
				int first = page * pageSize;
				int last = first + pageSize;
				query.append(" and messages_temp.sequence_order between " + first + " and " + last + ";");
			}
			
			result = dbConnection.executeQuery(query.toString());
			return getMessageList(result);
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(result);
			DatabaseUtil.close(dbConnection);
		}
	}
	
	public void createMessagesTempTable(MessageObjectFilter filter) throws ControllerException {
		logger.debug("creating temporary message table: filter=" + filter.toString());

		DatabaseConnection dbConnection = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			dbConnection.executeUpdate("drop table messages_temp if exists;");
			dbConnection.executeUpdate("create table messages_temp(id integer, sequence_order integer generated by default as identity);");
			dbConnection.executeUpdate("create index index_messages_temp on messages_temp(sequence_order, id);");
			
			StringBuilder insert = new StringBuilder();
			insert.append("insert into messages_temp(id) ");
			
			// add the select
			SelectQuery select = new SelectQuery(messages);
			select.addColumn(messages, "sequence_id");
			addFilterCriteria(select, filter);
			select.addOrder(messages, "date_created", Order.DESCENDING);
			insert.append(select.toString());

			dbConnection.executeUpdate(insert.toString());
			// FIXME: why is this shutdown required?
			dbConnection.executeUpdate("shutdown");
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(dbConnection);
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
			DatabaseUtil.close(dbConnection);
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

		// filter on connector_name
		if (filter.getConnectorName() != null) {
			select.addCriteria(new MatchCriteria(messages, "connector_name", MatchCriteria.EQUALS, filter.getConnectorName()));
		}

	}

	public void removeMessages(MessageObjectFilter filter) throws ControllerException {
		logger.debug("removing messages: filter=" + filter.toString());

		DatabaseConnection dbConnection = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();

			Delete delete = new Delete("messages");
			ArrayList<Object> parameters = new ArrayList<Object>();

			// filter on id
			if (filter.getId() != null) {
				delete.addCriteria("id = ?");
				parameters.add(filter.getId());
			}

			// filter on channelId
			if (filter.getChannelId() != null) {
				delete.addCriteria("channel_id = ?");
				parameters.add(filter.getChannelId());
			}

			// filter on start and end date
			if ((filter.getStartDate() != null) && (filter.getEndDate() != null)) {
				String startDate = String.format("%1$tY-%1$tm-%1$td 00:00:00", filter.getStartDate());
				String endDate = String.format("%1$tY-%1$tm-%1$td 23:59:59", filter.getEndDate());

				delete.addCriteria("(date_created >= ? and date_created <= ?)");

				parameters.add(startDate);
				parameters.add(endDate);
			}

			// filter on end date only
			if ((filter.getStartDate() == null) && (filter.getEndDate() != null)) {
				String endDate = String.format("%1$tY-%1$tm-%1$td 23:59:59", filter.getEndDate());
				delete.addCriteria("date_created <= ?");
				parameters.add(endDate);
			}

			// filter on status
			if (filter.getStatus() != null) {
				delete.addCriteria("status = ?");
				parameters.add(filter.getStatus().toString());
			}

			// filter on connector name
			if (filter.getConnectorName() != null) {
				delete.addCriteria("connector_name = ?");
				parameters.add(filter.getConnectorName());
			}

			dbConnection.executeUpdate(delete.toString(), parameters);
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(dbConnection);
		}
	}

	public void clearMessages(String channelId) throws ControllerException {
		logger.debug("clearing messages: " + channelId);

		DatabaseConnection dbConnection = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();

			String statement = "delete from messages where channel_id = ?";
			ArrayList<Object> parameters = new ArrayList<Object>();
			parameters.add(channelId);

			dbConnection.executeUpdate(statement, parameters);
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(dbConnection);
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
			messageObject.setVersion(result.getString("version"));
			messageObject.setEncrypted(result.getBoolean("encrypted"));

			String rawData = new String();
			String transformedData = new String();
			String encodedData = new String();

			if (messageObject.isEncrypted()) {
				if (result.getString("raw_data") != null) {
					rawData = encrypter.decrypt(result.getString("raw_data"));
				}

				if (result.getString("transformed_data") != null) {
					transformedData = encrypter.decrypt(result.getString("transformed_data"));
				}

				if (result.getString("encoded_data") != null) {
					encodedData = encrypter.decrypt(result.getString("encoded_data"));
				}
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
			messageObject.setConnectorName(result.getString("connector_name"));
			messageObject.setErrors(result.getString("errors"));

			messageObjects.add(messageObject);
		}

		return messageObjects;
	}

	public void reprocessMessages(MessageObjectFilter filter) throws ControllerException {
		createMessagesTempTable(filter);
		List<MessageObject> messages = getMessagesByPage(-1, -1);

		try {
			MuleClient client = new MuleClient();

			for (Iterator iter = messages.iterator(); iter.hasNext();) {
				MessageObject message = (MessageObject) iter.next();
				client.dispatch("vm://" + message.getChannelId(), message.getRawData(), null);
			}
		} catch (UMOException e) {
			throw new ControllerException("could not reprocess message", e);
		}
	}
}
