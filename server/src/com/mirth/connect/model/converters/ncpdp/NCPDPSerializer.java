/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters.ncpdp;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.mirth.connect.donkey.model.message.SerializerException;
import com.mirth.connect.donkey.model.message.XmlSerializer;
import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.model.converters.XMLPrettyPrinter;
import com.mirth.connect.model.ncpdp.NCPDPReference;
import com.mirth.connect.util.ErrorConstants;
import com.mirth.connect.util.ErrorMessageBuilder;

public class NCPDPSerializer implements IXMLSerializer {
    private String segmentDelimiter = "\u001E";
    private String groupDelimiter = "\u001D";
    private String fieldDelimiter = "\u001C";
    private boolean useStrictValidation = false;

    public NCPDPSerializer(Map<?, ?> properties) {
        if (properties != null) {
            if (properties.get("segmentDelimiter") != null) {
                String segDel = convertNonPrintableCharacters((String) properties.get("segmentDelimiter"));

                if (segDel.equals("0x1E")) {
                    this.segmentDelimiter = "\u001E";
                } else {
                    this.segmentDelimiter = segDel;
                }
            }

            if (properties.get("groupDelimiter") != null) {
                String grpDel = convertNonPrintableCharacters((String) properties.get("groupDelimiter"));

                if (grpDel.equals("0x1D")) {
                    this.groupDelimiter = "\u001D";
                } else {
                    this.groupDelimiter = grpDel;
                }
            }

            if (properties.get("fieldDelimiter") != null) {
                String fieldDel = convertNonPrintableCharacters((String) properties.get("fieldDelimiter"));

                if (fieldDel.equals("0x1C")) {
                    this.fieldDelimiter = "\u001C";
                } else {
                    this.fieldDelimiter = fieldDel;
                }
            }

            if (properties.get("useStrictValidation") != null) {
                this.useStrictValidation = Boolean.parseBoolean((String) properties.get("useStrictValidation"));
            }
        }
    }

    public static Map<String, String> getDefaultProperties() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("segmentDelimiter", "0x1E");
        properties.put("groupDelimiter", "0x1D");
        properties.put("fieldDelimiter", "0x1C");
        properties.put("useStrictValidation", "false");
        return properties;
    }
    
    @Override
    public boolean isTransformerRequired() {
        boolean transformerRequired = false;
        //TODO determine which properties are required for transformer
        if (!segmentDelimiter.equals("\u001E") || !groupDelimiter.equals("\u001D") || !fieldDelimiter.equals("\u001C") || useStrictValidation) {
            transformerRequired = true;
        }

        return transformerRequired;
    }
    
    @Override
    public String transformWithoutSerializing(String message, XmlSerializer outboundSerializer) {
        return message;
    }

    @Override
    public String fromXML(String source) throws SerializerException {
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
            NCPDPXMLHandler handler = new NCPDPXMLHandler(segmentDelimiter, groupDelimiter, fieldDelimiter, version);
            reader.setContentHandler(handler);

            if (useStrictValidation) {
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
            reader.parse(new InputSource(new StringReader(source.replaceAll(">\\s+<", "><"))));
            return handler.getOutput().toString();
        } catch (Exception e) {
            throw new SerializerException("Error converting XML to NCPDP message.", e, ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_500, "Error converting XML to NCPDP", e));
        }
    }

    @Override
    public String toXML(String source) throws SerializerException {
        try {
            NCPDPReader ncpdpReader = new NCPDPReader(segmentDelimiter, groupDelimiter, fieldDelimiter);
            StringWriter stringWriter = new StringWriter();
            XMLPrettyPrinter serializer = new XMLPrettyPrinter(stringWriter);
            ncpdpReader.setContentHandler(serializer);
            ncpdpReader.parse(new InputSource(new StringReader(source)));
            return stringWriter.toString();
        } catch (Exception e) {
            throw new SerializerException("Error converting NCPDP message to XML.", e, ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_500, "Error converting NCPDP to XML", e));
        }
    }

    @Override
    public Map<String, String> getMetadataFromDocument(Document document) {
        Map<String, String> metadata = new HashMap<String, String>();

        String serviceProviderId = StringUtils.EMPTY;

        if ((document != null) && (document.getElementsByTagName("ServiceProviderId") != null)) {
            Node sender = document.getElementsByTagName("ServiceProviderId").item(0);

            if (sender != null) {
                serviceProviderId = sender.getTextContent();
            }
        }

        String transactionCode = StringUtils.EMPTY;

        if ((document != null) && (document.getElementsByTagName("TransactionCode") != null)) {
            Node type = document.getElementsByTagName("TransactionCode").item(0);

            if (type != null) {
                transactionCode = NCPDPReference.getInstance().getTransactionName(type.getTextContent());
            }
        }

        String versionReleaseNumber = "5.1";

        if ((document != null) && (document.getElementsByTagName("VersionReleaseNumber") != null)) {
            Node versionNode = document.getElementsByTagName("VersionReleaseNumber").item(0);

            if (versionNode != null) {
                versionReleaseNumber = versionNode.getTextContent();
            }
        }

        metadata.put("version", versionReleaseNumber);
        metadata.put("type", transactionCode);
        metadata.put("source", serviceProviderId);
        return metadata;
    }

    private String convertNonPrintableCharacters(String delimiter) {
        return delimiter.replaceAll("\\\\r", "\r").replaceAll("\\\\n", "\n").replaceAll("\\\\t", "\t");
    }
}
