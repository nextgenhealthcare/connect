/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.uima;

import java.util.Properties;

import com.mirth.connect.model.QueuedSenderProperties;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

public class UimaSenderProperties extends QueuedSenderProperties {
    public static final String name = "Apache UIMA Sender";
    public static final String DATATYPE = "DataType";
    
    public static final String UIMA_HOST = "host";

    public static final String UIMA_TEMPLATE = "template";
    public static final String UIMA_PIPELINE = "pipeline";
    public static final String UIMA_META_TIMEOUT = "metaTimeout";
    public static final String UIMA_CAS_PROCESS_TIMEOUT = "casProcessTimeout";
    public static final String UIMA_CPC_TIMEOUT = "cpcTimeout";
    public static final String UIMA_SERIALIZATION_STRATEGY = "serializationStrategy";
    public static final String UIMA_CAS_POOL_SIZE = "casPoolSize";
    public static final String UIMA_JMS_URL = "jmsUrl";
    public static final String UIMA_SUCCESS_RESPONSE_CHANNEL_ID = "successResponseChannelId";
    public static final String UIMA_ERROR_RESPONSE_CHANNEL_ID = "errorResponseChannelId";
    public static final String UIMA_EXTRA_PROPERTIES = "dispatcherParameters";

    public Properties getDefaults() {
        Properties properties = super.getDefaults();

        ObjectXMLSerializer serializer = new ObjectXMLSerializer();

        properties.put(DATATYPE, name);
        properties.put(UIMA_HOST, "sink");
        
        properties.put(UIMA_PIPELINE, "");
        properties.put(UIMA_JMS_URL, "");
        properties.put(UIMA_SERIALIZATION_STRATEGY, "xmi");
        properties.put(UIMA_CPC_TIMEOUT, "0");
        properties.put(UIMA_CAS_PROCESS_TIMEOUT, "0");
        properties.put(UIMA_META_TIMEOUT, "60000");
        properties.put(UIMA_CAS_POOL_SIZE, "2");

        properties.put(UIMA_EXTRA_PROPERTIES, serializer.toXML(new Properties()));
        properties.put(UIMA_SUCCESS_RESPONSE_CHANNEL_ID, "sink");
        properties.put(UIMA_ERROR_RESPONSE_CHANNEL_ID, "sink");
        properties.put(UIMA_TEMPLATE, "");

        return properties;
    }

    public static String getInformation(Properties properties) {
        return "Host: " + properties.getProperty(UIMA_JMS_URL) + "   Pipeline: " + properties.getProperty(UIMA_PIPELINE);
    }
}
