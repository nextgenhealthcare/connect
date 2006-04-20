package com.webreach.mirth.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.webreach.mirth.core.Message;
import com.webreach.mirth.core.handlers.MessageSearchCriteria;
import com.webreach.mirth.core.util.DatabaseConnection;
import com.webreach.mirth.core.util.DatabaseUtil;

public class MessageDAO {
	final private String table = "MESSAGE";
	final private String fields = "project_id, name," + "project_manager_id, start_date, end_date, " + " started, completed, accepted, acceptedDate," + " customer_id, description, status";

	public List executeSelect(MessageSearchCriteria criteria) throws SQLException {
		List list = null;
		DatabaseConnection dbConnection = new DatabaseConnection();
		ResultSet result = null;

		StringBuffer selectStatement = new StringBuffer();
		selectStatement.append("SELECT " + fields + " FROM " + table + "WHERE TRUE AND");

		if (criteria.getChannelName() != null) {
			selectStatement.append(" AND CHANNEL='" + criteria.getChannelName() + "'");
		}
		
		if ((criteria.getMinId() > -1) && (criteria.getMaxId() > - 1)) {
			selectStatement.append(" AND ID >= " + criteria.getMinId() + " AND ID <= " + criteria.getMaxId());
		}
		
		selectStatement.append(";");
		
		try {
			result = dbConnection.query(selectStatement.toString());
			list = prepareResult(result);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}

		return list;
	}

	private List prepareResult(ResultSet result) throws SQLException {
		ArrayList list = new ArrayList();

		while (result.next()) {
			Message message = new Message();
			message.setId(result.getInt("id"));
			message.setEvent(result.getString("event"));
			message.setControlId(result.getString("controlid"));
			message.setSendingFacility(result.getString("sendingfacility"));
			message.setDate(result.getString("date"));
			message.setMessage(result.getString("message"));
			message.setSize(result.getInt("size"));
			list.add(message);
		}

		return list;
	}
	
	public void insert(Message message) {
		DatabaseConnection dbConnection = new DatabaseConnection();
		StringBuffer insert = new StringBuffer();

		insert.append("INSERT INTO " + table + " () VALUES (");
		insert.append(message.getEvent() + ", ");
		insert.append(message.getSendingFacility() + ", ");
		insert.append(message.getControlId() + ", ");
		insert.append(message.getDate() + ", ");
		insert.append(message.getMessage() + ", ");
		insert.append(message.getSize() + ");");
		
		try {
			dbConnection.update(insert.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbConnection.close();
		}
	}
}