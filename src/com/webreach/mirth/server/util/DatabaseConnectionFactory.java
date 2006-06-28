package com.webreach.mirth.server.util;

import java.sql.SQLException;
import java.util.Properties;

import com.webreach.mirth.util.PropertyLoader;


public class DatabaseConnectionFactory {
	private static boolean isDriverRegistered = false;
	private static Properties properties = PropertyLoader.loadProperties("mirth");
	
	public static DatabaseConnection createDatabaseConnection() throws SQLException {
		if (!isDriverRegistered) {
			try {
				Class.forName(properties.getProperty("database.driver"));
				isDriverRegistered = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		Properties info = new Properties();
		info.setProperty("user", "sa");
		info.setProperty("password", "");
		info.setProperty("shutdown", "true");

		return new DatabaseConnection(properties.getProperty("database.url"), info);
	}
}
