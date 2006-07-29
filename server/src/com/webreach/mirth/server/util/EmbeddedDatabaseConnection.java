package com.webreach.mirth.server.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;

public class EmbeddedDatabaseConnection {
	private Logger logger = Logger.getLogger(this.getClass());

	public ResultSet executeQuery(String driver, String address, String query) {
		Connection connection = null;
		Statement statement = null;
		
		try {
			Class.forName(driver);
			connection = DriverManager.getConnection(address);
			statement = connection.createStatement();
			return statement.executeQuery(query);
		} catch (Exception e) {
			logger.error(e);
			return null;
		} finally {
			DatabaseUtil.close(statement);
			
			try {
				connection.close();	
			} catch (Exception e) {
				logger.warn(e);
			}
		}
	}
	
	public void executeUpdate(String driver, String address, String query) {
		Connection connection = null;
		Statement statement = null;
		
		try {
			Class.forName(driver);
			connection = DriverManager.getConnection(address);
			statement = connection.createStatement();
			statement.executeUpdate(query);
		} catch (Exception e) {
			logger.error(e);
		} finally {
			DatabaseUtil.close(statement);
			
			try {
				connection.close();	
			} catch (Exception e) {
				logger.warn(e);
			}
		}
	}	
}
