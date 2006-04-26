package com.webreach.mirth.core.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.webreach.mirth.core.Log;
import com.webreach.mirth.core.util.DatabaseConnection;
import com.webreach.mirth.core.util.DatabaseUtil;
import com.webreach.mirth.core.util.ValueListHandler;

public class LogListHandler extends ValueListHandler {
	private String query;

	public LogListHandler(String query) {
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
		ArrayList<Log> list = new ArrayList<Log>();

		while (result.next()) {
			Log log = new Log();
			log.setId(result.getInt("ID"));
			log.setDate(result.getTimestamp("DATE_CREATED"));
			log.setLevel(result.getInt("LEVEL"));
			log.setEvent(result.getString("EVENT"));
			list.add(log);
		}

		return list;
	}	
}