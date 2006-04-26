package com.webreach.mirth.core.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.webreach.mirth.core.Message;
import com.webreach.mirth.core.util.DatabaseConnection;
import com.webreach.mirth.core.util.DatabaseUtil;
import com.webreach.mirth.core.util.ValueListHandler;

public class MessageListHandler extends ValueListHandler {
	private String query;

	public MessageListHandler(String query) {
		this.query = query;
		executeSearch();
	}

	public void executeSearch() {
		DatabaseConnection dbConnection = new DatabaseConnection();
		ResultSet result = null;
		
		try {
			result = dbConnection.query(query);
			setList(prepareResult(result));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}
	}
	
	private List prepareResult(ResultSet result) throws SQLException {
		ArrayList<Message> list = new ArrayList<Message>();

		while (result.next()) {
			Message message = new Message();
			message.setId(result.getInt("ID"));
			message.setEvent(result.getString("EVENT"));
			message.setControlId(result.getString("CONTROL_ID"));
			message.setSendingFacility(result.getString("SENDING_FACILITY"));
			message.setDate(result.getTimestamp("DATE_CREATED"));
			message.setMessage(result.getString("MESSAGE"));
			message.setSize(result.getString("MESSAGE").getBytes().length);
			list.add(message);
		}

		return list;
	}	
}