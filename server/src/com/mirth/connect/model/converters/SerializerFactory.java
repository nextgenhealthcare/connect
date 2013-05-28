/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters;

import java.util.Map;

import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.plugins.DataTypeServerPlugin;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ExtensionController;

public class SerializerFactory {

    private static ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();

    public static IXMLSerializer getSerializer(String dataType) {
        return getSerializer(dataType, null, null);
    }

    public static IXMLSerializer getSerializer(String dataType, Map<String, Object> serializationPropertiesMap, Map<String, Object> deserializationPropertiesMap) {
        DataTypeServerPlugin plugin = extensionController.getDataTypePlugins().get(dataType);
        if (plugin != null) {
            if (serializationPropertiesMap == null) {
                serializationPropertiesMap = getDefaultSerializationProperties(dataType);
            }
            if (deserializationPropertiesMap == null) {
                deserializationPropertiesMap = getDefaultDeserializationProperties(dataType);
            }

            SerializerProperties properties = plugin.getDefaultProperties().getSerializerProperties();
            properties.getSerializationProperties().setProperties(serializationPropertiesMap);
            properties.getDeserializationProperties().setProperties(deserializationPropertiesMap);

            return plugin.getSerializer(properties);
        } else {
            return null;
        }
    }

    public static Map<String, Object> getDefaultSerializationProperties(String dataType) {
        DataTypeServerPlugin plugin = extensionController.getDataTypePlugins().get(dataType);
        if (plugin != null) {
            return plugin.getDefaultProperties().getSerializationProperties().getProperties();
        } else {
            return null;
        }
    }

    public static Map<String, Object> getDefaultDeserializationProperties(String dataType) {
        DataTypeServerPlugin plugin = extensionController.getDataTypePlugins().get(dataType);
        if (plugin != null) {
            return plugin.getDefaultProperties().getDeserializationProperties().getProperties();
        } else {
            return null;
        }
    }
}