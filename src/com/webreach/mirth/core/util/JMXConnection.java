package com.webreach.mirth.core.util;

import java.util.Hashtable;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;


public class JMXConnection {
	private JMXConnector jmxConnector;
	private MBeanServerConnection jmxConnection;
	private Properties mirthProperties;
	private String configurationId;
	
	public JMXConnection() throws Exception {
		try {
			mirthProperties = PropertyLoader.loadProperties("mirth");
			configurationId = mirthProperties.getProperty("configuration.id");
			JMXServiceURL serviceURL = new JMXServiceURL(mirthProperties.getProperty("jmx.url"));
			jmxConnector = JMXConnectorFactory.connect(serviceURL);
			jmxConnection = jmxConnector.getMBeanServerConnection();
		} catch (Exception e) {
			throw new Exception(e);
		}
	}
	
	/**
	 * Invokes an operation on the MBean with the specified properties.
	 * 
	 * @param propertiesTable
	 *            the MBean name/value properties.
	 * @param operationName
	 *            the name of the operation.
	 * @return the result of the operation.
	 */
	public Object invokeOperation(Hashtable properties, String operation, Object[] params) throws Exception {
		try {
			return jmxConnection.invoke(new ObjectName(configurationId, properties), operation, params, null);	
		} catch (Exception e) {
			throw new Exception(e);
		}
	}
	
	/**
	 * Returns specified attribute from the MBean with the specified properties.
	 * 
	 * @param properties
	 * @param attribute
	 * @return
	 */
	public Object getAttribute(Hashtable properties, String attribute) throws Exception {
		try {
			return jmxConnection.getAttribute(new ObjectName(configurationId, properties), attribute);	
		} catch (Exception e) {
			throw new Exception(e);
		}
	}
}
