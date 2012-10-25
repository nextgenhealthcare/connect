/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.util;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

// TODO: Rewrite to support ConnectorProperties and new Channel properties
public class PropertyVerifier
{
    /** A method to compare two properties file to check if they are the same. */
    public static boolean compareProps(ConnectorProperties p1, ConnectorProperties p2) {
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        return serializer.toXML(p1).equals(serializer.toXML(p2));
    }
    
//    /** A method to compare two properties file to check if they are the same. */
//    public static boolean compareProps(ConnectorProperties p1, ConnectorProperties p2)
//    {
//        Enumeration<?> propertyKeys = p1.propertyNames();
//        while (propertyKeys.hasMoreElements())
//        {
//            String key = (String) propertyKeys.nextElement();
//            // System.out.println(key + " " + p1.getProperty(key) + " " +
//            // p2.getProperty(key));
//            if (p1.getProperty(key) == null)
//            {
//                if (p2.getProperty(key) != null)
//                    return false;
//            }
//            else if (!p1.getProperty(key).equals(p2.getProperty(key)))
//                return false;
//        }
//        return true;
//    }

//    /** A method to add default properties to a channel. */
//    public static void checkChannelProperties(Channel channel)
//    {
//        fixMissingOrInvalidProperties(new ChannelProperties().getDefaults(), channel.getProperties());
//    }
    
//    /** A method to add default connector properties to a channel. */
//    public static void checkConnectorProperties(Channel channel, Map<String, ConnectorMetaData> connectorData)
//    {
//        PropertyVerifier.checkPropertyValidity(channel.getSourceConnector(), connectorData);
//    
//        List<Connector> destinations = channel.getDestinationConnectors();
//        for (int i = 0; i < destinations.size(); i++)
//        {
//            PropertyVerifier.checkPropertyValidity(destinations.get(i), connectorData);
//        }
//    }
    
//    /** A method to add default connector properties to a connector. */
//    public static void checkConnectorProperties(Connector connector, Map<String, ConnectorMetaData> connectorData)
//    {
//        PropertyVerifier.checkPropertyValidity(connector, connectorData);
//    }
    
//    /**
//     * Gets the default properties for a connector, and fixes invalid/missing properties
//     */
//    private static void checkPropertyValidity(Connector connector, Map<String, ConnectorMetaData> connectorData)
//    {
//        
//        Properties properties = connector.getProperties();
//        Properties propertiesDefaults = null;
//        
//        try
//        {
//            propertiesDefaults = ((ComponentProperties)Class.forName(connectorData.get(connector.getTransportName()).getSharedClassName()).newInstance()).getDefaults();
//            fixMissingOrInvalidProperties(propertiesDefaults, properties);
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }        
//    }
    
//    /**
//     * Checks for properties that are new or not used and adds or removes them from a Properties object.
//     */    
//    private static void fixMissingOrInvalidProperties(Properties propertiesDefaults, Properties properties)
//    {
//        Enumeration<?> propertyKeys;
//        propertyKeys = properties.propertyNames();
//        while (propertyKeys.hasMoreElements())
//        {
//            String key = (String) propertyKeys.nextElement();
//            if (propertiesDefaults.getProperty(key) == null)
//            {
//                properties.remove(key);
//            }
//        }
//
//        propertyKeys = propertiesDefaults.propertyNames();
//        while (propertyKeys.hasMoreElements())
//        {
//            String key = (String) propertyKeys.nextElement();
//            if (properties.getProperty(key) == null)
//            {
//                if (propertiesDefaults.getProperty(key) != null)
//                    properties.put(key, propertiesDefaults.getProperty(key));
//            }
//        }
//    }
}
