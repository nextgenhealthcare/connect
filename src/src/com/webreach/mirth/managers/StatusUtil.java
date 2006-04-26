/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.managers;

import java.util.Hashtable;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StatusUtil {
	protected static transient Log logger = LogFactory.getLog(StatusUtil.class);
	private static ConfigurationManager configurationManager = ConfigurationManager.getInstance();

	private StatusUtil() {};

	/**
	 * Returns a new connection to the Mule MBean server.
	 * 
	 * @return a new connection to the Mule MBean server.
	 */
	private static MBeanServerConnection getMBeanServerConnection() throws Exception {
		JMXServiceURL serviceURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:1099/server");
		JMXConnector connector = JMXConnectorFactory.connect(serviceURL);
		return connector.getMBeanServerConnection();
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
	public static Object invokeMBeanOperation(Hashtable properties, String operationName, Object[] params) throws Exception {
		logger.debug("invoking MBean operation: " + operationName);
		MBeanServerConnection beanConnection = getMBeanServerConnection();
		return beanConnection.invoke(new ObjectName(configurationManager.getConfigurationId(), properties), operationName, params, null);
	}

	/**
	 * Returns specified attribute from the MBean with the specified properties.
	 * 
	 * @param properties
	 * @param attribute
	 * @return
	 */
	public static Object getMBeanAttribute(Hashtable properties, String attribute) throws Exception {
		MBeanServerConnection beanConnection = getMBeanServerConnection();
		return beanConnection.getAttribute(new ObjectName(configurationManager.getConfigurationId(), properties), attribute);
	}

	/**
	 * Returns a set of all MBean ObjectNames.
	 * 
	 * @return
	 */
	public static Set getMBeanNames() throws Exception {
		MBeanServerConnection beanConnection = getMBeanServerConnection();
		return beanConnection.queryNames(null, null);
	}

	public static String cleanChannelName(String name) {
		return name.substring(name.indexOf(ConfigurationManager.ID_NAME_DELIMETER) + 1, name.length());
	}
}
