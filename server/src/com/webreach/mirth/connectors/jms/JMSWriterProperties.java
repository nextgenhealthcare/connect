/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.connectors.jms;

import java.util.Properties;

import com.webreach.mirth.model.ComponentProperties;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;

public class JMSWriterProperties implements ComponentProperties
{
	public static final String name = "JMS Writer";
	
    public static final String DATATYPE = "DataType";
    public static final String JMS_SPECIFICATION = "specification";
    public static final String JMS_USERNAME = "username";
    public static final String JMS_PASSWORD = "password";
    public static final String JMS_QUEUE = "host";
    public static final String JMS_URL = "jndiProviderUrl";
    public static final String JMS_USE_JNDI = "useJndi";
    public static final String JMS_INITIAL_FACTORY = "jndiInitialFactory";
    public static final String JMS_CONNECTION_FACTORY_JNDI = "connectionFactoryJndiName";
    public static final String JMS_CONNECTION_FACTORY_CLASS = "connectionFactoryClass";
    public static final String JMS_ACK_MODE = "acknowledgementMode";
    public static final String JMS_ADDITIONAL_PROPERTIES = "connectionFactoryProperties";
    public static final String JMS_TEMPLATE = "template";
    
    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(JMS_SPECIFICATION, "1.1");
        properties.put(JMS_USERNAME, "");
        properties.put(JMS_PASSWORD, "");
        properties.put(JMS_URL, "");
        properties.put(JMS_QUEUE, "");
        properties.put(JMS_INITIAL_FACTORY, "");
        properties.put(JMS_CONNECTION_FACTORY_JNDI, "");
        properties.put(JMS_CONNECTION_FACTORY_CLASS, "");
        properties.put(JMS_USE_JNDI, "0");
        properties.put(JMS_ACK_MODE, "1");
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        properties.put(JMS_ADDITIONAL_PROPERTIES, serializer.toXML(new Properties()));
        properties.put(JMS_TEMPLATE, "${message.encodedData}");
        return properties;
    }

    public static String getInformation(Properties properties) {
        return "URL: " + properties.getProperty(JMS_URL);
    }
}
