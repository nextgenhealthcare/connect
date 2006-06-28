package com.webreach.mirth.server.util;

import java.util.Properties;

import com.webreach.mirth.util.PropertyLoader;

public class JMXConnectionFactory {
	private static Properties properties = PropertyLoader.loadProperties("mirth");
	
	public static JMXConnection createJMXConnection() throws Exception {
		return new JMXConnection(properties.getProperty("jmx.url"), properties.getProperty("configuration.id"));
	}
}
