package com.webreach.mirth.core.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;


public class DatabaseConnection {
	private Properties mirthProperties;
	private Connection connection;
	
	public DatabaseConnection() throws RuntimeException {
		try {
			mirthProperties = PropertyLoader.loadProperties("mirth");
			Class.forName(mirthProperties.getProperty("database.driver"));
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}
	
	private Connection getConnection() throws RuntimeException {
		try {
			return DriverManager.getConnection(mirthProperties.getProperty("database.url"), "sa", "");	
		} catch (SQLException e) {
			throw new RuntimeException();
		}
	}

	public synchronized ResultSet query(String expression) throws RuntimeException {
		Statement statement = null;
		ResultSet result = null;
		
		try {
			connection = getConnection();
			statement = connection.createStatement();
			result = statement.executeQuery(expression);

			return result;
		} catch (SQLException e) {
			throw new RuntimeException();
		} finally {
			DatabaseUtil.close(statement);
		}
	}

	public synchronized int update(String expression) throws RuntimeException {
		Statement statement = null;
		
		try {
			connection = getConnection();
			statement = connection.createStatement();
			int rowCount = statement.executeUpdate(expression);
			statement.close();

			return rowCount;
		} catch (SQLException e) {
			throw new RuntimeException();
		} finally {
			DatabaseUtil.close(statement);
		}
	}
	
	public void close() throws RuntimeException {
		if (connection == null) {
			throw new RuntimeException("Database connection is not open.");
		}
		
		Statement statement = null;
		
		try {
			statement = connection.createStatement();
			statement.execute("SHUTDOWN");
			connection.close();
		} catch (SQLException e) {
			throw new RuntimeException();
		} finally {
			DatabaseUtil.close(statement);
		}
	}
}
