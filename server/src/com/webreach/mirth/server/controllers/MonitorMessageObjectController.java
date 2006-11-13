package com.webreach.mirth.server.controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.truemesh.squiggle.MatchCriteria;
import com.truemesh.squiggle.Order;
import com.truemesh.squiggle.SelectQuery;
import com.truemesh.squiggle.Table;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.MonitorMessageObject;
import com.webreach.mirth.server.util.DatabaseConnection;
import com.webreach.mirth.server.util.DatabaseConnectionFactory;
import com.webreach.mirth.server.util.DatabaseUtil;
import com.webreach.mirth.server.util.sql.Delete;
import com.webreach.mirth.util.PropertyLoader;

public class MonitorMessageObjectController {
	private Logger logger = Logger.getLogger(this.getClass());
	private Properties mirthProperties = PropertyLoader.loadProperties("mirth");

	public void addMonitorMessage(MessageObject messageObject) {
		MessageObject.Status status = messageObject.getStatus();

		if ((mirthProperties.getProperty("enable_monitor") != null) && mirthProperties.getProperty("enable_monitor").equalsIgnoreCase("true")) {
			if (status.equals(MessageObject.Status.SENT) || status.equals(MessageObject.Status.REJECTED) || status.equals(MessageObject.Status.ERROR)) {
				logger.debug("adding monitor message: id=" + messageObject.getId());
				DatabaseConnection dbConnection = null;

				try {
					dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
					String insert = "insert into monitor_messages (id, channel_id, date_created, status, version, type, subtype) values (?, ?, ?, ?, ?, ?, ?)";
					ArrayList<Object> parameters = new ArrayList<Object>();
					
					parameters.add(messageObject.getId());
					parameters.add(messageObject.getChannelId());
					parameters.add(messageObject.getDateCreated());
					parameters.add(messageObject.getStatus());
					parameters.add(messageObject.getVersion());
					parameters.add(messageObject.getRawDataProtocol());
					// parameters.add(messageObject.getRawDataProtocolType());

					dbConnection.executeUpdate(insert, parameters);
				} catch (Exception e) {
					logger.error("could not add monitor message: id=" + messageObject.getId(), e);
				}
			}
		}
	}
	
	public List<MonitorMessageObject> getMonitorMessages(int lastId, int range) throws ControllerException {
		logger.debug("retrieving monitor messages: lastId=" + lastId + ", range=" + range);

		DatabaseConnection dbConnection = null;
		ResultSet result = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			Table monitorMessages = new Table("monitor_messages");
			SelectQuery select = new SelectQuery(monitorMessages);
			select.addColumn(monitorMessages, "id");
			select.addColumn(monitorMessages, "channel_id");
			select.addColumn(monitorMessages, "date_created");
			select.addColumn(monitorMessages, "version");
			select.addColumn(monitorMessages, "status");
			select.addColumn(monitorMessages, "type");
			select.addColumn(monitorMessages, "sub_type");
			select.addOrder(monitorMessages, "id", Order.ASCENDING);
			select.addCriteria(new MatchCriteria(monitorMessages, "sequence_id", MatchCriteria.GREATEREQUAL, lastId));
			select.addCriteria(new MatchCriteria(monitorMessages, "sequence_id", MatchCriteria.LESS, lastId + range));
			result = dbConnection.executeQuery(select.toString());

			return getMonitorMessageList(result);
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(result);
			DatabaseUtil.close(dbConnection);
		}
	}
	
	private List<MonitorMessageObject> getMonitorMessageList(ResultSet result) throws SQLException {
		List<MonitorMessageObject> monitorMessageObjects = new ArrayList<MonitorMessageObject>();
		
		while (result.next()) {
			MonitorMessageObject monitorMessageObject = new MonitorMessageObject();
			monitorMessageObject.setId(result.getString("id"));
			monitorMessageObject.setChannelId(result.getString("channel_id"));
			
			Calendar dateCreated = Calendar.getInstance();
			dateCreated.setTimeInMillis(result.getTimestamp("date_created").getTime());
			monitorMessageObject.setDateCreated(dateCreated);
			
			monitorMessageObject.setVersion(result.getString("version"));
			monitorMessageObject.setStatus(MessageObject.Status.valueOf(result.getString("status")));
			monitorMessageObject.setType(result.getString("type"));
			monitorMessageObject.setSubType(result.getString("sub_type"));
			monitorMessageObjects.add(monitorMessageObject);
		}
		
		return monitorMessageObjects;
	}
	
	public void acknowledge(int lastId) throws ControllerException {
		logger.debug("removing monitor messages: lastId=" + lastId);

		DatabaseConnection dbConnection = null;
		ResultSet result = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			Delete delete = new Delete("monitor_messages");
			delete.addCriteria("id <= last_id");
			dbConnection.executeUpdate(delete.toString());
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(result);
			DatabaseUtil.close(dbConnection);
		}
	}
}
