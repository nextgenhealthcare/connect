package com.webreach.mirth.server.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;

public class EmbeddedDatabaseConnection {
	private Logger logger = Logger.getLogger(this.getClass());

	private Connection connection = null;

	private Statement statement = null;

	public ResultSet executeQuery(String driver, String address,
			String expression) {

		try {
			Class.forName(driver);
			connection = DriverManager.getConnection(address);
			statement = connection.createStatement();
			return statement.executeQuery(expression);
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

	public void executeUpdate(String driver, String address, String expression) {
		try {
			Class.forName(driver);
			connection = DriverManager.getConnection(address);
			statement = connection.createStatement();
			statement.executeUpdate(expression);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	public void close() {
		if (connection != null) {
			try {
				connection.close();
				DatabaseUtil.close(statement);
			} catch (Exception e) {
				logger.warn(e);
			}
		}
	}
}
