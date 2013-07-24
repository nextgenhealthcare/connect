/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.ncpdp;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.mirth.connect.donkey.model.message.XmlSerializer;
import com.mirth.connect.donkey.model.message.XmlSerializerException;
import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.model.converters.XMLPrettyPrinter;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.model.util.DefaultMetaData;
import com.mirth.connect.util.ErrorMessageBuilder;
import com.mirth.connect.util.StringUtil;

public class NCPDPSerializer implements IXMLSerializer {
    private NCPDPSerializationProperties serializationProperties;
    private NCPDPDeserializationProperties deserializationProperties;
    private Logger logger = Logger.getLogger(getClass());

    private String serializationSegmentDelimiter = null;
    private String serializationGroupDelimiter = null;
    private String serializationFieldDelimiter = null;
    private String deserializationSegmentDelimiter = null;
    private String deserializationGroupDelimiter = null;
    private String deserializationFieldDelimiter = null;

    private static Pattern prettyPattern = Pattern.compile(">\\s+<");

    public NCPDPSerializer(SerializerProperties properties) {
        serializationProperties = (NCPDPSerializationProperties) properties.getSerializationProperties();
        deserializationProperties = (NCPDPDeserializationProperties) properties.getDeserializationProperties();

        if (serializationProperties != null) {
            serializationSegmentDelimiter = StringUtil.unescape(serializationProperties.getSegmentDelimiter());
            serializationGroupDelimiter = StringUtil.unescape(serializationProperties.getGroupDelimiter());
            serializationFieldDelimiter = StringUtil.unescape(serializationProperties.getFieldDelimiter());
        }

        if (deserializationProperties != null) {
            deserializationSegmentDelimiter = StringUtil.unescape(deserializationProperties.getSegmentDelimiter());
            deserializationGroupDelimiter = StringUtil.unescape(deserializationProperties.getGroupDelimiter());
            deserializationFieldDelimiter = StringUtil.unescape(deserializationProperties.getFieldDelimiter());
        }
    }

    public String getDeserializationSegmentDelimiter() {
        return deserializationSegmentDelimiter;
    }

    public String getDeserializationGroupDelimiter() {
        return deserializationGroupDelimiter;
    }

    public String getDeserializationFieldDelimiter() {
        return deserializationFieldDelimiter;
    }

    @Override
    public boolean isSerializationRequired(boolean toXml) {
        boolean serializationRequired = false;

        if (toXml) {
            // No serialization properties require serializing
        } else {
            if (deserializationProperties.isUseStrictValidation()) {
                serializationRequired = true;
            }
        }

        return serializationRequired;
    }

    @Override
    public String transformWithoutSerializing(String message, XmlSerializer outboundSerializer) throws XmlSerializerException {
        try {
            boolean transformed = false;

            NCPDPSerializer serializer = (NCPDPSerializer) outboundSerializer;

            String outputSegmentDelimiter = serializer.getDeserializationSegmentDelimiter();
            String outputGroupDelimiter = serializer.getDeserializationGroupDelimiter();
            String outputFieldDelimiter = serializer.getDeserializationFieldDelimiter();

            if (!serializationSegmentDelimiter.equals(outputSegmentDelimiter)) {
                message = StringUtils.replace(message, serializationSegmentDelimiter, outputSegmentDelimiter);
                transformed = true;
            }

            if (!serializationGroupDelimiter.equals(outputGroupDelimiter)) {
                message = StringUtils.replace(message, serializationGroupDelimiter, outputGroupDelimiter);
                transformed = true;
            }

            if (!serializationFieldDelimiter.equals(outputFieldDelimiter)) {
                message = StringUtils.replace(message, serializationFieldDelimiter, outputFieldDelimiter);
                transformed = true;
            }

            if (transformed) {
                return message;
            }
        } catch (Exception e) {
            throw new XmlSerializerException("Error transforming NCPDP", e, ErrorMessageBuilder.buildErrorMessage(this.getClass().getSimpleName(), "Error transforming NCPDP", e));
        }

        return null;
    }

    @Override
    public String fromXML(String source) throws XmlSerializerException {
        /*
         * Need to determine the version by looking at the raw message.
         * The transaction header will contain the version ("51" for 5.1 and
         * "D0" for D.0)
         */
        String version = "D0";

        if (source.indexOf("D0") == -1) {
            version = "51";
        } else if (source.indexOf("51") == -1) {
            version = "D0";
        } else if (source.indexOf("51") < source.indexOf("D0")) {
            version = "51";
        }

        try {
            XMLReader reader = XMLReaderFactory.createXMLReader();
            NCPDPXMLHandler handler = new NCPDPXMLHandler(deserializationSegmentDelimiter, deserializationGroupDelimiter, deserializationFieldDelimiter, version);
            reader.setContentHandler(handler);

            if (deserializationProperties.isUseStrictValidation()) {
                reader.setFeature("http://xml.org/sax/features/validation", true);
                reader.setFeature("http://apache.org/xml/features/validation/schema", true);
                reader.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
                reader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
                reader.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation", "ncpdp" + version + ".xsd");
                reader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", "/ncpdp" + version + ".xsd");
            }

            /*
             * Parse, but first replace all spaces between brackets. This fixes
             * pretty-printed XML we might receive
             */
            reader.parse(new InputSource(new StringReader(prettyPattern.matcher(source).replaceAll("><"))));
            return handler.getOutput().toString();
        } catch (Exception e) {
            throw new XmlSerializerException("Error converting XML to NCPDP message.", e, ErrorMessageBuilder.buildErrorMessage(this.getClass().getSimpleName(), "Error converting XML to NCPDP", e));
        }
    }

    @Override
    public String toXML(String source) throws XmlSerializerException {
        try {
            NCPDPReader ncpdpReader = new NCPDPReader(serializationSegmentDelimiter, serializationGroupDelimiter, serializationFieldDelimiter);
            StringWriter stringWriter = new StringWriter();
            XMLPrettyPrinter serializer = new XMLPrettyPrinter(stringWriter);
            ncpdpReader.setContentHandler(serializer);
            ncpdpReader.parse(new InputSource(new StringReader(source)));
            return stringWriter.toString();
        } catch (Exception e) {
            throw new XmlSerializerException("Error converting NCPDP message to XML.", e, ErrorMessageBuilder.buildErrorMessage(this.getClass().getSimpleName(), "Error converting NCPDP to XML", e));
        }
    }

    @Override
    public Map<String, Object> getMetaDataFromMessage(String message) {
        Map<String, Object> map = new HashMap<String, Object>();
        populateMetaData(message, map);
        if (!map.containsKey(DefaultMetaData.VERSION_VARIABLE_MAPPING)) {
            // Put default version value
            map.put(DefaultMetaData.VERSION_VARIABLE_MAPPING, "5.1");
        }
        return map;
    }

    @Override
    public void populateMetaData(String message, Map<String, Object> map) {
        try {
            int segmentDelimiterIndex = message.indexOf(serializationSegmentDelimiter);
            if (segmentDelimiterIndex == -1) {
                return;
            }

            int versionPos = 6;
            int typePos = 8;
            int sourcePos = 23;

            if (segmentDelimiterIndex <= 40) {
                // Handle a response (requests have a longer header than responses)
                versionPos = 0;
                typePos = 2;
                sourcePos = 8;
            }

            if (versionPos + 2 <= message.length()) {
                map.put(DefaultMetaData.VERSION_VARIABLE_MAPPING, message.substring(versionPos, versionPos + 2));
            }
            if (typePos + 2 <= message.length()) {
                map.put(DefaultMetaData.TYPE_VARIABLE_MAPPING, NCPDPReference.getInstance().getTransactionName(message.substring(typePos, typePos + 2)));
            }
            if (sourcePos + 15 <= message.length()) {
                map.put(DefaultMetaData.SOURCE_VARIABLE_MAPPING, message.substring(sourcePos, sourcePos + 15));
            }
        } catch (Exception e) {
            logger.error("Error populating NCPDP metadata.", e);
        }
    }
}
