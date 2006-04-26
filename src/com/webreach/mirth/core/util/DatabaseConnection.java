package com.webreach.mirth.core.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;


public class DatabaseConnection {
	private Properties mirthProperties;
	private Connection connection;
	private Logger logger = Logger.getLogger(DatabaseConnection.class);
	
	public DatabaseConnection() throws RuntimeException {
		try {
			mirthProperties = PropertyLoader.loadProperties("mirth");
			Class.forName(mirthProperties.getProperty("database.driver"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private Connection getConnection() throws SQLException {
		try {
			return DriverManager.getConnection(mirthProperties.getProperty("database.url"), "sa", "");	
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized ResultSet query(String expression) throws SQLException {
		Statement statement = null;
		ResultSet result = null;
		
		try {
			connection = getConnection();
			statement = connection.createStatement();
			logger.debug("executing query: " + expression);
			result = statement.executeQuery(expression);

			return result;
		} catch (SQLException e) {
			throw e;
		} finally {
			DatabaseUtil.close(statement);
		}
	}

	public synchronized int update(String expression) throws SQLException {
		Statement statement = null;
		
		try {
			connection = getConnection();
			statement = connection.createStatement();
			logger.debug("executing update: " + expression);
			int rowCount = statement.executeUpdate(expression);
			statement.close();

			return rowCount;
		} catch (SQLException e) {
			throw e;
		} finally {
			DatabaseUtil.close(statement);
		}
	}
	
	public void close() throws RuntimeException {
		if (connection == null) {
			logger.warn("database connection cannot be closed");
		}
		
		Statement statement = null;
		
		try {
			statement = connection.createStatement();
			statement.execute("SHUTDOWN");
			logger.warn("closing database connection");
			connection.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			DatabaseUtil.close(statement);
		}
	}
}
