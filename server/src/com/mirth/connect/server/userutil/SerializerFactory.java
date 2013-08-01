/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.util.Map;

import org.apache.log4j.Logger;

import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.plugins.DataTypeServerPlugin;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ExtensionController;

public class SerializerFactory {
    private static Logger logger = Logger.getLogger(SerializerFactory.class);
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

            if (properties.getSerializationProperties() != null) {
                properties.getSerializationProperties().setProperties(serializationPropertiesMap);
            }
            if (properties.getDeserializationProperties() != null) {
                properties.getDeserializationProperties().setProperties(deserializationPropertiesMap);
            }

            return plugin.getSerializer(properties);
        } else {
            return null;
        }
    }

    public static Map<String, Object> getDefaultSerializationProperties(String dataType) {
        DataTypeServerPlugin plugin = extensionController.getDataTypePlugins().get(dataType);
        if (plugin != null && plugin.getDefaultProperties().getSerializationProperties() != null) {
            return plugin.getDefaultProperties().getSerializationProperties().getProperties();
        } else {
            return null;
        }
    }

    public static Map<String, Object> getDefaultDeserializationProperties(String dataType) {
        DataTypeServerPlugin plugin = extensionController.getDataTypePlugins().get(dataType);
        if (plugin != null && plugin.getDefaultProperties().getDeserializationProperties() != null) {
            return plugin.getDefaultProperties().getDeserializationProperties().getProperties();
        } else {
            return null;
        }
    }

    @Deprecated
    // TODO: Remove in 3.1
    private static IXMLSerializer getHL7SerializerQuietly(boolean useStrictParser, boolean useStrictValidation, boolean handleRepetitions, boolean convertLFtoCR, boolean handleSubcomponents) {
        String dataType = "HL7V2";
        Map<String, Object> serializationPropertiesMap = getDefaultSerializationProperties(dataType);
        serializationPropertiesMap.put("handleRepetitions", handleRepetitions);
        serializationPropertiesMap.put("handleSubcomponents", handleSubcomponents);
        serializationPropertiesMap.put("useStrictParser", useStrictParser);
        serializationPropertiesMap.put("useStrictValidation", useStrictValidation);
        serializationPropertiesMap.put("segmentDelimiter", "\\r");
        serializationPropertiesMap.put("convertLineBreaks", convertLFtoCR);

        Map<String, Object> deserializationPropertiesMap = getDefaultDeserializationProperties(dataType);
        deserializationPropertiesMap.put("useStrictParser", useStrictParser);
        deserializationPropertiesMap.put("useStrictValidation", useStrictValidation);

        return getSerializer(dataType, serializationPropertiesMap, deserializationPropertiesMap);
    }

    @Deprecated
    // TODO: Remove in 3.1
    public static IXMLSerializer getHL7Serializer(boolean useStrictParser, boolean useStrictValidation, boolean handleRepetitions, boolean convertLFtoCR, boolean handleSubcomponents) {
        logger.error("The getHL7Serializer(useStrictParser, useStrictValidation, handleRepetitions, convertLFtoCR, handleSubcomponents) method is deprecated and will soon be removed. Please use the \"Convert HL7 v2.x\" templates from the References tab instead. Look at the tooltips to see the available property keys.");
        return getHL7SerializerQuietly(useStrictParser, useStrictValidation, handleRepetitions, convertLFtoCR, handleSubcomponents);
    }

    @Deprecated
    // TODO: Remove in 3.1
    public static IXMLSerializer getHL7Serializer(boolean useStrictParser, boolean useStrictValidation, boolean handleRepetitions, boolean convertLFtoCR) {
        logger.error("The getHL7Serializer(useStrictParser, useStrictValidation, handleRepetitions, convertLFtoCR) method is deprecated and will soon be removed. Please use the \"Convert HL7 v2.x\" templates from the References tab instead. Look at the tooltips to see the available property keys.");
        return getHL7SerializerQuietly(useStrictParser, useStrictValidation, handleRepetitions, convertLFtoCR, false);
    }

    @Deprecated
    // TODO: Remove in 3.1
    public static IXMLSerializer getHL7Serializer(boolean useStrictParser, boolean useStrictValidation, boolean handleRepetitions) {
        logger.error("The getHL7Serializer(useStrictParser, useStrictValidation, handleRepetitions) method is deprecated and will soon be removed. Please use the \"Convert HL7 v2.x\" templates from the References tab instead. Look at the tooltips to see the available property keys.");
        return getHL7SerializerQuietly(useStrictParser, useStrictValidation, handleRepetitions, true, false);
    }

    @Deprecated
    // TODO: Remove in 3.1
    public static IXMLSerializer getHL7Serializer(boolean useStrictParser, boolean useStrictValidation) {
        logger.error("The getHL7Serializer(useStrictParser, useStrictValidation) method is deprecated and will soon be removed. Please use the \"Convert HL7 v2.x\" templates from the References tab instead. Look at the tooltips to see the available property keys.");
        return getHL7SerializerQuietly(useStrictParser, useStrictValidation, false, true, false);
    }

    @Deprecated
    // TODO: Remove in 3.1
    public static IXMLSerializer getHL7Serializer() {
        logger.error("The getHL7Serializer() method is deprecated and will soon be removed. Please use the \"Convert HL7 v2.x\" templates from the References tab instead. Look at the tooltips to see the available property keys.");
        return getHL7SerializerQuietly(false, false, false, true, false);
    }

    @Deprecated
    // TODO: Remove in 3.1
    public static IXMLSerializer getX12Serializer(boolean inferDelimiters) {
        logger.error("The getX12Serializer(inferDelimiters) method is deprecated and will soon be removed. Please use the \"Convert EDI / X12\" templates from the References tab instead. Look at the tooltips to see the available property keys.");

        String dataType = "EDI/X12";
        Map<String, Object> serializationPropertiesMap = getDefaultSerializationProperties(dataType);
        serializationPropertiesMap.put("inferX12Delimiters", inferDelimiters);

        return getSerializer(dataType, serializationPropertiesMap, null);
    }

    @Deprecated
    // TODO: Remove in 3.1
    public static IXMLSerializer getEDISerializer(String segmentDelim, String elementDelim, String subelementDelim) {
        logger.error("The getEDISerializer(segmentDelim, elementDelim, subelementDelim) method is deprecated and will soon be removed. Please use the \"Convert EDI / X12\" templates from the References tab instead. Look at the tooltips to see the available property keys.");

        String dataType = "EDI/X12";
        Map<String, Object> serializationPropertiesMap = getDefaultSerializationProperties(dataType);
        serializationPropertiesMap.put("segmentDelimiter", segmentDelim);
        serializationPropertiesMap.put("elementDelimiter", elementDelim);
        serializationPropertiesMap.put("subelementDelimiter", subelementDelim);
        serializationPropertiesMap.put("inferX12Delimiters", false);

        return getSerializer(dataType, serializationPropertiesMap, null);
    }

    @Deprecated
    // TODO: Remove in 3.1
    private static IXMLSerializer getNCPDPSerializerQuietly(String segmentDelim, String groupDelim, String fieldDelim, boolean useStrictValidation) {
        String dataType = "NCPDP";
        Map<String, Object> serializationPropertiesMap = getDefaultSerializationProperties(dataType);
        serializationPropertiesMap.put("segmentDelimiter", segmentDelim);
        serializationPropertiesMap.put("groupDelimiter", groupDelim);
        serializationPropertiesMap.put("fieldDelimiter", fieldDelim);

        Map<String, Object> deserializationPropertiesMap = getDefaultDeserializationProperties(dataType);
        deserializationPropertiesMap.put("segmentDelimiter", segmentDelim);
        deserializationPropertiesMap.put("groupDelimiter", groupDelim);
        deserializationPropertiesMap.put("fieldDelimiter", fieldDelim);
        deserializationPropertiesMap.put("useStrictValidation", useStrictValidation);

        return getSerializer(dataType, serializationPropertiesMap, deserializationPropertiesMap);
    }

    @Deprecated
    // TODO: Remove in 3.1
    public static IXMLSerializer getNCPDPSerializer(String segmentDelim, String groupDelim, String fieldDelim, boolean useStrictValidation) {
        logger.error("The getNCPDPSerializer(segmentDelim, groupDelim, fieldDelim, useStrictValidation) method is deprecated and will soon be removed. Please use the \"Convert NCPDP\" templates from the References tab instead. Look at the tooltips to see the available property keys.");
        return getNCPDPSerializerQuietly(segmentDelim, groupDelim, fieldDelim, useStrictValidation);
    }

    @Deprecated
    // TODO: Remove in 3.1
    public static IXMLSerializer getNCPDPSerializer(String segmentDelim, String groupDelim, String fieldDelim) {
        logger.error("The getNCPDPSerializer(segmentDelim, groupDelim, fieldDelim) method is deprecated and will soon be removed. Please use the \"Convert NCPDP\" templates from the References tab instead. Look at the tooltips to see the available property keys.");
        return getNCPDPSerializerQuietly(segmentDelim, groupDelim, fieldDelim, false);
    }
}