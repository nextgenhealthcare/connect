package com.webreach.mirth.server.util;

import java.util.Properties;

import com.webreach.mirth.server.controllers.ConfigurationController;

public class SMTPConnectionFactory {
	public static SMTPConnection createSMTPConnection() throws Exception {
		Properties properties = (new ConfigurationController()).getServerProperties();
		String host = properties.getProperty("smtp.host");

		int port = 25;

		if (properties.getProperty("smtp.port") != null && !properties.getProperty("smtp.port").equals("")) {
			port = Integer.valueOf(properties.getProperty("smtp.port")).intValue();
		}

		String username = properties.getProperty("smtp.username");
		String password = properties.getProperty("smtp.password");
		
		return new SMTPConnection(host, port, username, password);
	}
}
