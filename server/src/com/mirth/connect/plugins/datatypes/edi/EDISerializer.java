/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.edi;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
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

public class EDISerializer implements IXMLSerializer {
    private Logger logger = Logger.getLogger(this.getClass());
    private EDISerializationProperties serializationProperties;

    private String serializationSegmentDelimiter = null;
    private String serializationElementDelimiter = null;
    private String serializationSubelementDelimiter = null;

    private static Pattern prettyPattern = Pattern.compile("</([^>]*)>\\s+<");

    public EDISerializer(SerializerProperties properties) {
        serializationProperties = (EDISerializationProperties) properties.getSerializationProperties();

        if (serializationProperties != null) {
            serializationElementDelimiter = StringUtil.unescape(serializationProperties.getElementDelimiter());
            serializationSubelementDelimiter = StringUtil.unescape(serializationProperties.getSubelementDelimiter());
            serializationSegmentDelimiter = StringUtil.unescape(serializationProperties.getSegmentDelimiter());
        }
    }

    @Override
    public boolean isSerializationRequired(boolean toXml) {
        boolean serializationRequired = false;

        return serializationRequired;
    }

    @Override
    public String transformWithoutSerializing(String message, XmlSerializer outboundSerializer) throws XmlSerializerException {
        return null;
    }

    @Override
    public String fromXML(String source) throws XmlSerializerException {
        XMLReader xr;
        try {
            xr = XMLReaderFactory.createXMLReader();
        } catch (SAXException e) {
            throw new XmlSerializerException("Error converting XML to EDI", e, ErrorMessageBuilder.buildErrorMessage(this.getClass().getSimpleName(), "Error converting XML to EDI", e));
        }
        EDIXMLHandler handler = new EDIXMLHandler();
        xr.setContentHandler(handler);
        xr.setErrorHandler(handler);
        try {
            //Parse, but first replace all spaces between brackets. This fixes pretty-printed XML we might receive
            xr.parse(new InputSource(new StringReader(prettyPattern.matcher(source).replaceAll("</$1><"))));
        } catch (Exception e) {
            throw new XmlSerializerException("Error converting XML to EDI", e, ErrorMessageBuilder.buildErrorMessage(this.getClass().getSimpleName(), "Error converting XML to EDI", e));
        }
        return handler.getOutput().toString();
    }

    @Override
    public String toXML(String source) throws XmlSerializerException {
        try {
            Delimiters delimiters = getDelimiters(source);
            EDIReader ediReader = new EDIReader(delimiters.segmentDelimiter, delimiters.elementDelimiter, delimiters.subelementDelimiter);
            StringWriter stringWriter = new StringWriter();
            XMLPrettyPrinter serializer = new XMLPrettyPrinter(stringWriter);
            serializer.setEncodeEntities(true);
            ediReader.setContentHandler(serializer);
            ediReader.parse(new InputSource(new StringReader(source)));
            return stringWriter.toString();
        } catch (Exception e) {
            throw new XmlSerializerException("Error converting EDI to XML", e, ErrorMessageBuilder.buildErrorMessage(this.getClass().getSimpleName(), "Error converting EDI to XML", e));
        }
    }

    private class Delimiters {
        public String elementDelimiter;
        public String subelementDelimiter;
        public String segmentDelimiter;
    }

    private Delimiters getDelimiters(String message) {
        Delimiters delimiters = new Delimiters();
        delimiters.elementDelimiter = serializationElementDelimiter;
        delimiters.subelementDelimiter = serializationSubelementDelimiter;
        delimiters.segmentDelimiter = serializationSegmentDelimiter;

        if (serializationProperties.isInferX12Delimiters()) {
            if (message.startsWith("ISA") && message.length() > 105) {
                delimiters.elementDelimiter = message.charAt(3) + "";
                delimiters.subelementDelimiter = message.charAt(104) + "";
                delimiters.segmentDelimiter = message.charAt(105) + "";
                // hack to handle newlines
                if (message.length() > 106 && message.charAt(106) == '\n') {
                    delimiters.segmentDelimiter += '\n';
                }
            }
        }

        return delimiters;
    }

    @Override
    public Map<String, Object> getMetaDataFromMessage(String message) {
        Map<String, Object> map = new HashMap<String, Object>();
        populateMetaData(message, map);
        return map;
    }

    @Override
    public void populateMetaData(String message, Map<String, Object> map) {
        try {
            Delimiters delimiters = getDelimiters(message);
            String source = null;
            String type = null;
            String version = null;
            int index = -1;

            do {
                index++;

                if (source == null && message.startsWith("ISA", index)) {
                    // Get the source from ISA.06.1
                    source = getElement(message, delimiters, index, 6);
                } else if ((source == null || version == null) && message.startsWith("GS", index)) {
                    // Get the source from GS.02.1 if we haven't already found it
                    if (source == null) {
                        source = getElement(message, delimiters, index, 2);
                    }

                    // Get the version from GS.08.1
                    version = getElement(message, delimiters, index, 8);
                } else if (type == null && message.startsWith("ST", index)) {
                    // Get the type from ST.01.1
                    type = getElement(message, delimiters, index, 1);
                }
            } while ((index = getDelimiterIndex(message, delimiters.segmentDelimiter, index)) != -1 && (source == null || type == null || version == null));

            if (source != null) {
                map.put(DefaultMetaData.SOURCE_VARIABLE_MAPPING, source);
            }
            if (type != null) {
                map.put(DefaultMetaData.TYPE_VARIABLE_MAPPING, type);
            }
            if (version != null) {
                map.put(DefaultMetaData.VERSION_VARIABLE_MAPPING, version);
            }
        } catch (Exception e) {
            logger.error("Error populating EDI/X12 metadata.", e);
        }
    }

    private int getDelimiterIndex(String message, String delimiters, int startIndex) {
        char[] delimitersArray = delimiters.toCharArray();
        while (startIndex < message.length()) {
            if (ArrayUtils.contains(delimitersArray, message.charAt(startIndex))) {
                return startIndex;
            }
            startIndex++;
        }
        return -1;
    }

    private boolean startsWithDelimiter(String message, String delimiters, int startIndex) {
        return ArrayUtils.contains(delimiters.toCharArray(), message.charAt(startIndex));
    }

    private String getElement(String message, Delimiters delimiters, int index, int elementNumber) {
        StringBuilder builder = new StringBuilder();
        boolean done = false;
        boolean found = false;
        int elementCount = 0;

        while (index < message.length() && !done) {
            if (startsWithDelimiter(message, delimiters.segmentDelimiter, index)) {
                done = true;
            } else if (startsWithDelimiter(message, delimiters.elementDelimiter, index)) {
                elementCount++;

                if (found) {
                    done = true;
                } else if (elementCount == elementNumber) {
                    found = true;
                }
            } else if (startsWithDelimiter(message, delimiters.subelementDelimiter, index)) {
                if (found) {
                    done = true;
                }
            } else if (found) {
                builder.append(message.charAt(index));
            }
            index++;
        }

        return found ? builder.toString() : null;
    }
}