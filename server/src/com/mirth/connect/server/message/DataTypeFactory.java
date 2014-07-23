/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.message;

import com.mirth.connect.donkey.model.message.SerializationType;
import com.mirth.connect.donkey.model.message.XmlSerializer;
import com.mirth.connect.donkey.server.message.AutoResponder;
import com.mirth.connect.donkey.server.message.DataType;
import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.plugins.DataTypeServerPlugin;
import com.mirth.connect.server.controllers.ExtensionController;

public class DataTypeFactory {

    public static DataType getDataType(String dataType, DataTypeProperties dataTypeProperties) {
        // Get the data type plugin
        DataTypeServerPlugin dataTypePlugin = ExtensionController.getInstance().getDataTypePlugins().get(dataType);

        // Create the serializer
        SerializerProperties serializerProperties = dataTypeProperties.getSerializerProperties();
        XmlSerializer serializer = dataTypePlugin.getSerializer(serializerProperties);

        // Create the autoresponder
        AutoResponder autoResponder = dataTypePlugin.getAutoResponder(serializerProperties.getSerializationProperties(), dataTypeProperties.getResponseGenerationProperties());
        if (autoResponder == null) {
            autoResponder = new DefaultAutoResponder();
        }

        // Get the serialization type
        SerializationType serializationType = dataTypePlugin.getSerializationType();

        // Return the data type
        return new DataType(dataType, serializer, autoResponder, serializationType);
    }
}
