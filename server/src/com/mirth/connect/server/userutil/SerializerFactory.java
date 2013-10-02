/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.util.Map;

import org.apache.log4j.Logger;

import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.plugins.DataTypeServerPlugin;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ExtensionController;

/**
 * Used to create a serializer for a specific data type for conversion to and from XML.
 */
public class SerializerFactory {
    private static Logger logger = Logger.getLogger(SerializerFactory.class);
    private static ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();

    private SerializerFactory() {}

    /**
     * Returns a serializer (with toXML and fromXML methods) for a given data type. Any
     * serialization or deserialization properties will be left as the default values.
     * 
     * @param dataType
     *            The plugin point (e.g. "HL7V2") of the data type to create the serializer for.
     * @return The instantiated IXMLSerializer object.
     */
    public static IXMLSerializer getSerializer(String dataType) {
        return getSerializer(dataType, null, null);
    }

    /**
     * Returns a serializer (with toXML and fromXML methods) for a given data type and properties.
     * 
     * @param dataType
     *            The plugin point (e.g. "HL7V2") of the data type to create the serializer for.
     * @param serializationPropertiesMap
     *            A Map of properties used to customize how serialization from the data type to XML
     *            is performed.
     * @param deserializationPropertiesMap
     *            A Map of properties used to customize how deserialization from XML to the data
     *            type is performed.
     * @return The instantiated IXMLSerializer object.
     */
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

    /**
     * Returns a map of default properties used to customize how serialization from the data type to
     * XML is performed.
     * 
     * @param dataType
     *            The plugin point (e.g. "HL7V2") of the data type to get default properties for.
     * @return The map of default serialization properties.
     */
    public static Map<String, Object> getDefaultSerializationProperties(String dataType) {
        DataTypeServerPlugin plugin = extensionController.getDataTypePlugins().get(dataType);
        if (plugin != null && plugin.getDefaultProperties().getSerializationProperties() != null) {
            return plugin.getDefaultProperties().getSerializationProperties().getProperties();
        } else {
            return null;
        }
    }

    /**
     * Returns a map of default properties used to customize how deserialization from XML to the
     * data type is performed.
     * 
     * @param dataType
     *            The plugin point (e.g. "HL7V2") of the data type to get default properties for.
     * @return The map of default deserialization properties.
     */
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

        // The old serializer never stripped namespaces, so this must be set to false to match
        serializationPropertiesMap.put("stripNamespaces", false);

        Map<String, Object> deserializationPropertiesMap = getDefaultDeserializationProperties(dataType);
        deserializationPropertiesMap.put("useStrictParser", useStrictParser);
        deserializationPropertiesMap.put("useStrictValidation", useStrictValidation);

        return getSerializer(dataType, serializationPropertiesMap, deserializationPropertiesMap);
    }

    /**
     * Returns an HL7 v2.x serializer.
     * 
     * @param useStrictParser
     *            If true, messages will be parsed based upon strict HL7 specifications.
     * @param useStrictValidation
     *            If true, messages will be validated using HL7 specifications (applies to Strict
     *            Parser only).
     * @param handleRepetitions
     *            If true, field repetitions will be parsed (applies to Non-Strict Parser only).
     * @param convertLFtoCR
     *            If true, line feeds (\n) will be converted to carriage returns (\r) automatically
     *            (applies to Non-Strict Parser only).
     * @param handleSubcomponents
     *            If true, subcomponents will be parsed (applies to Non-Strict Parser only).
     * @return The instantiated IXMLSerializer object.
     * 
     * @deprecated This method is deprecated and will soon be removed. Please use
     *             getSerializer(dataType, serializationPropertiesMap, deserializationPropertiesMap)
     *             instead. The new method will now strip namespaces by default unless the
     *             'stripNamespaces' property is set to false.
     */
    // TODO: Remove in 3.1
    public static IXMLSerializer getHL7Serializer(boolean useStrictParser, boolean useStrictValidation, boolean handleRepetitions, boolean convertLFtoCR, boolean handleSubcomponents) {
        logger.error("The getHL7Serializer(useStrictParser, useStrictValidation, handleRepetitions, convertLFtoCR, handleSubcomponents) method is deprecated and will soon be removed. Please use the \"Convert HL7 v2.x\" templates from the References tab instead. Look at the tooltips to see the available property keys. The new method will strip namespaces by default unless the 'stripNamespaces' property is set to false.");
        return getHL7SerializerQuietly(useStrictParser, useStrictValidation, handleRepetitions, convertLFtoCR, handleSubcomponents);
    }

    /**
     * Returns an HL7 v2.x serializer.
     * 
     * @param useStrictParser
     *            If true, messages will be parsed based upon strict HL7 specifications.
     * @param useStrictValidation
     *            If true, messages will be validated using HL7 specifications (applies to Strict
     *            Parser only).
     * @param handleRepetitions
     *            If true, field repetitions will be parsed (applies to Non-Strict Parser only).
     * @param convertLFtoCR
     *            If true, line feeds (\n) will be converted to carriage returns (\r) automatically
     *            (applies to Non-Strict Parser only).
     * @return The instantiated IXMLSerializer object.
     * 
     * @deprecated This method is deprecated and will soon be removed. Please use
     *             getSerializer(dataType, serializationPropertiesMap, deserializationPropertiesMap)
     *             instead. The new method will now strip namespaces by default unless the
     *             'stripNamespaces' property is set to false.
     */
    // TODO: Remove in 3.1
    public static IXMLSerializer getHL7Serializer(boolean useStrictParser, boolean useStrictValidation, boolean handleRepetitions, boolean convertLFtoCR) {
        logger.error("The getHL7Serializer(useStrictParser, useStrictValidation, handleRepetitions, convertLFtoCR) method is deprecated and will soon be removed. Please use the \"Convert HL7 v2.x\" templates from the References tab instead. Look at the tooltips to see the available property keys. The new method will strip namespaces by default unless the 'stripNamespaces' property is set to false.");
        return getHL7SerializerQuietly(useStrictParser, useStrictValidation, handleRepetitions, convertLFtoCR, false);
    }

    /**
     * Returns an HL7 v2.x serializer.
     * 
     * @param useStrictParser
     *            If true, messages will be parsed based upon strict HL7 specifications.
     * @param useStrictValidation
     *            If true, messages will be validated using HL7 specifications (applies to Strict
     *            Parser only).
     * @param handleRepetitions
     *            If true, field repetitions will be parsed (applies to Non-Strict Parser only).
     * @return The instantiated IXMLSerializer object.
     * 
     * @deprecated This method is deprecated and will soon be removed. Please use
     *             getSerializer(dataType, serializationPropertiesMap, deserializationPropertiesMap)
     *             instead. The new method will now strip namespaces by default unless the
     *             'stripNamespaces' property is set to false.
     */
    // TODO: Remove in 3.1
    public static IXMLSerializer getHL7Serializer(boolean useStrictParser, boolean useStrictValidation, boolean handleRepetitions) {
        logger.error("The getHL7Serializer(useStrictParser, useStrictValidation, handleRepetitions) method is deprecated and will soon be removed. Please use the \"Convert HL7 v2.x\" templates from the References tab instead. Look at the tooltips to see the available property keys. The new method will strip namespaces by default unless the 'stripNamespaces' property is set to false.");
        return getHL7SerializerQuietly(useStrictParser, useStrictValidation, handleRepetitions, true, false);
    }

    /**
     * Returns an HL7 v2.x serializer.
     * 
     * @param useStrictParser
     *            If true, messages will be parsed based upon strict HL7 specifications.
     * @param useStrictValidation
     *            If true, messages will be validated using HL7 specifications (applies to Strict
     *            Parser only).
     * @return The instantiated IXMLSerializer object.
     * 
     * @deprecated This method is deprecated and will soon be removed. Please use
     *             getSerializer(dataType, serializationPropertiesMap, deserializationPropertiesMap)
     *             instead. The new method will now strip namespaces by default unless the
     *             'stripNamespaces' property is set to false.
     */
    // TODO: Remove in 3.1
    public static IXMLSerializer getHL7Serializer(boolean useStrictParser, boolean useStrictValidation) {
        logger.error("The getHL7Serializer(useStrictParser, useStrictValidation) method is deprecated and will soon be removed. Please use the \"Convert HL7 v2.x\" templates from the References tab instead. Look at the tooltips to see the available property keys. The new method will strip namespaces by default unless the 'stripNamespaces' property is set to false.");
        return getHL7SerializerQuietly(useStrictParser, useStrictValidation, false, true, false);
    }

    /**
     * Returns an HL7 v2.x serializer.
     * 
     * @return The instantiated IXMLSerializer object.
     * 
     * @deprecated This method is deprecated and will soon be removed. Please use
     *             getSerializer(dataType, serializationPropertiesMap, deserializationPropertiesMap)
     *             instead. The new method will now strip namespaces by default unless the
     *             'stripNamespaces' property is set to false.
     */
    // TODO: Remove in 3.1
    public static IXMLSerializer getHL7Serializer() {
        logger.error("The getHL7Serializer() method is deprecated and will soon be removed. Please use the \"Convert HL7 v2.x\" templates from the References tab instead. Look at the tooltips to see the available property keys. The new method will strip namespaces by default unless the 'stripNamespaces' property is set to false.");
        return getHL7SerializerQuietly(false, false, false, true, false);
    }

    /**
     * Returns an EDI / X12 serializer.
     * 
     * @param inferDelimiters
     *            This property only applies to X12 messages. If checked, the delimiters are
     *            inferred from the incoming message and the delimiter properties will not be used.
     * @return The instantiated IXMLSerializer object.
     * 
     * @deprecated This method is deprecated and will soon be removed. Please use
     *             getSerializer(dataType, serializationPropertiesMap, deserializationPropertiesMap)
     *             instead.
     */
    // TODO: Remove in 3.1
    public static IXMLSerializer getX12Serializer(boolean inferDelimiters) {
        logger.error("The getX12Serializer(inferDelimiters) method is deprecated and will soon be removed. Please use the \"Convert EDI / X12\" templates from the References tab instead. Look at the tooltips to see the available property keys.");

        String dataType = "EDI/X12";
        Map<String, Object> serializationPropertiesMap = getDefaultSerializationProperties(dataType);
        serializationPropertiesMap.put("inferX12Delimiters", inferDelimiters);

        return getSerializer(dataType, serializationPropertiesMap, null);
    }

    /**
     * Returns an EDI / X12 serializer.
     * 
     * @param segmentDelim
     *            Characters that delimit the segments in the message.
     * @param elementDelim
     *            Characters that delimit the elements in the message.
     * @param subelementDelim
     *            Characters that delimit the subelements in the message.
     * @return The instantiated IXMLSerializer object.
     * 
     * @deprecated This method is deprecated and will soon be removed. Please use
     *             getSerializer(dataType, serializationPropertiesMap, deserializationPropertiesMap)
     *             instead.
     */
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

    /**
     * Returns an NCPDP serializer.
     * 
     * @param segmentDelim
     *            Characters that delimit the segments in the message.
     * @param groupDelim
     *            Characters that delimit the groups in the message.
     * @param fieldDelim
     *            Characters that delimit the fields in the message.
     * @param useStrictValidation
     *            Validates the NCPDP message against a schema.
     * @return The instantiated IXMLSerializer object.
     * 
     * @deprecated This method is deprecated and will soon be removed. Please use
     *             getSerializer(dataType, serializationPropertiesMap, deserializationPropertiesMap)
     *             instead.
     */
    // TODO: Remove in 3.1
    public static IXMLSerializer getNCPDPSerializer(String segmentDelim, String groupDelim, String fieldDelim, boolean useStrictValidation) {
        logger.error("The getNCPDPSerializer(segmentDelim, groupDelim, fieldDelim, useStrictValidation) method is deprecated and will soon be removed. Please use the \"Convert NCPDP\" templates from the References tab instead. Look at the tooltips to see the available property keys.");
        return getNCPDPSerializerQuietly(segmentDelim, groupDelim, fieldDelim, useStrictValidation);
    }

    /**
     * Returns an NCPDP serializer.
     * 
     * @param segmentDelim
     *            Characters that delimit the segments in the message.
     * @param groupDelim
     *            Characters that delimit the groups in the message.
     * @param fieldDelim
     *            Characters that delimit the fields in the message.
     * @return The instantiated IXMLSerializer object.
     * 
     * @deprecated This method is deprecated and will soon be removed. Please use
     *             getSerializer(dataType, serializationPropertiesMap, deserializationPropertiesMap)
     *             instead.
     */
    // TODO: Remove in 3.1
    public static IXMLSerializer getNCPDPSerializer(String segmentDelim, String groupDelim, String fieldDelim) {
        logger.error("The getNCPDPSerializer(segmentDelim, groupDelim, fieldDelim) method is deprecated and will soon be removed. Please use the \"Convert NCPDP\" templates from the References tab instead. Look at the tooltips to see the available property keys.");
        return getNCPDPSerializerQuietly(segmentDelim, groupDelim, fieldDelim, false);
    }
}