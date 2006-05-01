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


package com.webreach.mirth.core.util;

import java.util.Hashtable;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;


public class JMXConnection {
	private JMXConnector jmxConnector;
	private MBeanServerConnection jmxConnection;
	private Properties mirthProperties;
	private String configurationId;
	private Logger logger = Logger.getLogger(JMXConnection.class);
	
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
			logger.debug("invoking mbean operation: " + operation);
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
			logger.debug("getting mbean attribute: " + attribute);
			return jmxConnection.getAttribute(new ObjectName(configurationId, properties), attribute);	
		} catch (Exception e) {
			throw new Exception(e);
		}
	}
}
