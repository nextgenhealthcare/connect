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

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.mirth.connect.donkey.model.message.XmlSerializer;
import com.mirth.connect.donkey.model.message.XmlSerializerException;
import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.model.converters.XMLPrettyPrinter;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.util.ErrorConstants;
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
            throw new XmlSerializerException(e.getMessage(), e, ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_500, "Error converting XML to EDI", e));
        }
        EDIXMLHandler handler = new EDIXMLHandler();
        xr.setContentHandler(handler);
        xr.setErrorHandler(handler);
        try {
            //Parse, but first replace all spaces between brackets. This fixes pretty-printed XML we might receive
            xr.parse(new InputSource(new StringReader(prettyPattern.matcher(source).replaceAll("</$1><"))));
        } catch (Exception e) {
            throw new XmlSerializerException(e.getMessage(), e, ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_500, "Error converting XML to EDI", e));
        }
        return handler.getOutput().toString();
    }

    @Override
    public String toXML(String source) throws XmlSerializerException {
        try {
            String elementDelimiter = serializationElementDelimiter;
            String subelementDelimiter = serializationSubelementDelimiter;
            String segmentDelimiter = serializationSegmentDelimiter;

            if (serializationProperties.isInferX12Delimiters()) {
                String x12message = source;
                if (x12message.startsWith("ISA")) {
                    elementDelimiter = x12message.charAt(3) + "";
                    subelementDelimiter = x12message.charAt(104) + "";
                    segmentDelimiter = x12message.charAt(105) + "";
                    // hack to handle newlines
                    if (x12message.charAt(106) == '\n') {
                        segmentDelimiter = serializationProperties.getSegmentDelimiter() + x12message.charAt(106);
                    }
                }
            }

            EDIReader ediReader = new EDIReader(segmentDelimiter, elementDelimiter, subelementDelimiter);
            StringWriter stringWriter = new StringWriter();
            XMLPrettyPrinter serializer = new XMLPrettyPrinter(stringWriter);
            serializer.setEncodeEntities(true);
            ediReader.setContentHandler(serializer);
            ediReader.parse(new InputSource(new StringReader(source)));
            return stringWriter.toString();
        } catch (Exception e) {
            //TODO is this supposed to throw a SerializerException?
            logger.error("Error converting EDI message to XML.", e);
        }

        return new String();
    }

    @Override
    public Map<String, String> getMetadataFromDocument(Document document) {
        Map<String, String> map = new HashMap<String, String>();
        String sendingFacility = "";
        if (document.getElementsByTagName("ISA.06.1") != null) {
            Node sender = document.getElementsByTagName("ISA.06.1").item(0);
            if (sender != null) {
                sendingFacility = sender.getTextContent();
            }
        }
        if (sendingFacility == null && document.getElementsByTagName("GS.02.1") != null) {
            Node sender = document.getElementsByTagName("GS.02.1").item(0);
            if (sender != null) {
                sendingFacility = sender.getTextContent();
            }
        }
        String event = document.getDocumentElement().getNodeName();
        if (document.getElementsByTagName("ST.01.1") != null) {
            Node type = document.getElementsByTagName("ST.01.1").item(0);
            if (type != null) {
                event = type.getTextContent();
            }
        }
        String version = "";
        if (document.getElementsByTagName("GS.08.1") != null) {
            Node versionNode = document.getElementsByTagName("GS.08.1").item(0);
            if (versionNode != null) {
                version = versionNode.getTextContent();
            }
        }

        map.put("version", version);
        map.put("type", event);
        map.put("source", sendingFacility);
        return map;
    }

}
