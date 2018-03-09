/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.message;

import com.mirth.connect.donkey.model.message.MessageSerializer;
import com.mirth.connect.donkey.model.message.SerializationType;
import com.mirth.connect.donkey.server.message.AutoResponder;
import com.mirth.connect.donkey.server.message.DataType;
import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.plugins.DataTypeServerPlugin;
import com.mirth.connect.server.controllers.ExtensionController;

public class DataTypeFactory {

    public static DataType getDataType(String dataType, DataTypeProperties dataTypeProperties, boolean inbound) {
        // Get the data type plugin
        DataTypeServerPlugin dataTypePlugin = ExtensionController.getInstance().getDataTypePlugins().get(dataType);

        // Create the serializer
        SerializerProperties serializerProperties = dataTypeProperties.getSerializerProperties();
        MessageSerializer serializer = dataTypePlugin.getSerializer(serializerProperties);

        // Create the autoresponder
        AutoResponder autoResponder = dataTypePlugin.getAutoResponder(serializerProperties.getSerializationProperties(), dataTypeProperties.getResponseGenerationProperties());
        if (autoResponder == null) {
            autoResponder = new DefaultAutoResponder();
        }

        // Get the serialization types
        SerializationType serializationType = getSerializationType(dataTypePlugin, dataTypeProperties, inbound);
        SerializationType templateSerializationType = getSerializationType(dataTypePlugin, dataTypeProperties, true);

        // Return the data type
        return new DataType(dataType, serializer, autoResponder, serializationType, templateSerializationType);
    }

    public static SerializationType getSerializationType(String dataType, DataTypeProperties dataTypeProperties, boolean useSerializationProperties) {
        DataTypeServerPlugin dataTypePlugin = ExtensionController.getInstance().getDataTypePlugins().get(dataType);
        return getSerializationType(dataTypePlugin, dataTypeProperties, useSerializationProperties);
    }

    public static SerializationType getSerializationType(DataTypeServerPlugin dataTypePlugin, DataTypeProperties dataTypeProperties, boolean useSerializationProperties) {
        SerializationType serializationType = null;

        /*
         * Attempt to get the serialization type from the Serialization properties (if inbound) or
         * Deserialization properties (if outbound). If the properties don't exist or the returned
         * serialization type is null, use the data type default serialization type.
         */
        if (dataTypeProperties != null) {
            if (useSerializationProperties) {
                if (dataTypeProperties.getSerializationProperties() != null) {
                    serializationType = dataTypeProperties.getSerializationProperties().getSerializationType();
                }
            } else if (dataTypeProperties.getDeserializationProperties() != null) {
                serializationType = dataTypeProperties.getDeserializationProperties().getSerializationType();
            }
        }

        if (serializationType == null) {
            serializationType = dataTypePlugin.getDefaultSerializationType();
        }

        return serializationType;
    }
}
