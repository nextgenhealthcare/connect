/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.server.util;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;

/**
 * A JMXConnection provides a connection to the Mule MBean server.
 * 
 * @author <a href="mailto:geraldb@webreachinc.com">Gerald Bortis</a>
 * 
 */
public class JMXConnection {
	private Logger logger = Logger.getLogger(this.getClass());
	private JMXConnector jmxConnector;
	private MBeanServerConnection jmxConnection;
	private String domain;

	public JMXConnection(String address, String domain, Map<String, ?> environment) throws Exception {
		this.domain = domain;
		JMXServiceURL url = new JMXServiceURL(address);
		jmxConnector = JMXConnectorFactory.connect(url, environment);
		jmxConnection = jmxConnector.getMBeanServerConnection();
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
	public Object invokeOperation(Hashtable<String, String> properties, String operation, Object[] params, String[] signature) throws Exception {
		logger.debug("invoking mbean operation: " + operation);
		return jmxConnection.invoke(getObjectName(properties), operation, params, signature);
	}

	/**
	 * Returns specified attribute from the MBean with the specified properties.
	 * 
	 * @param properties
	 * @param attribute
	 * @return
	 */
	public Object getAttribute(Hashtable<String, String> properties, String attribute) throws Exception {
		logger.debug("getting mbean attribute: " + attribute);
		return jmxConnection.getAttribute(getObjectName(properties), attribute);
	}

	public Set<ObjectName> getMBeanNames() throws Exception {
		logger.debug("getting mbean names");
		return jmxConnection.queryNames(null, null);
	}

	public void close() {
		try {
			if (jmxConnector != null) {
				jmxConnector.close();
			} else {
				logger.warn("could not close jmx connection");
			}
		} catch (Exception e) {
			logger.warn(e.getMessage());
		}
	}

	private ObjectName getObjectName(Hashtable<String, String> properties) throws Exception {
		ObjectName objectName = null;
		if (properties == null) {
			objectName = (ObjectName) jmxConnection.queryNames(new ObjectName(domain), null).toArray()[0];
		} else {
			objectName = new ObjectName(domain, properties);
		}
		return objectName;
	}
}
