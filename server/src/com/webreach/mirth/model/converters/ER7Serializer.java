/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.model.converters;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.parser.XMLParser;
import ca.uhn.hl7v2.util.Terser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

public class ER7Serializer implements IXMLSerializer<String> {
    private Logger logger = Logger.getLogger(this.getClass());
    private PipeParser pipeParser;
    private XMLParser xmlParser;
    private boolean useStrictParser = false;
    private boolean useStrictValidation = false;
    private boolean stripNamespaces = true;
    private boolean handleRepetitions = false;
    private boolean convertLFtoCR = true;

    public static Map<String, String> getDefaultProperties() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("useStrictParser", "false");
        map.put("useStrictValidation", "false");
        map.put("stripNamespaces", "true");
        map.put("handleRepetitions", "false");
        map.put("convertLFtoCR", "true");
        return map;
    }

    public ER7Serializer(Map<String, String> properties) {
        if (properties != null && properties.get("useStrictParser") != null) {
            this.useStrictParser = Boolean.parseBoolean(properties.get("useStrictParser"));
        }
        if (properties != null && properties.get("useStrictValidation") != null) {
            this.useStrictValidation = Boolean.parseBoolean(properties.get("useStrictValidation"));
        }
        if (properties != null && properties.get("stripNamespaces") != null) {
            this.stripNamespaces = Boolean.parseBoolean(properties.get("stripNamespaces"));
        }
        if (properties != null && properties.get("handleRepetitions") != null) {
            this.handleRepetitions = Boolean.parseBoolean(properties.get("handleRepetitions"));
        }
        if (properties != null && properties.get("convertLFtoCR") != null) {
            this.convertLFtoCR = Boolean.parseBoolean(properties.get("convertLFtoCR"));
        }
        if (useStrictParser) {
            initializeHapiParser();
        }
    }

    public ER7Serializer() {
        initializeHapiParser();
    }

    private void initializeHapiParser() {
        pipeParser = new PipeParser();
        xmlParser = new DefaultXMLParser();
        // Turn off strict validation if needed
        if (!this.useStrictValidation) {
            pipeParser.setValidationContext(new NoValidation());
            xmlParser.setValidationContext(new NoValidation());
        }

        xmlParser.setKeepAsOriginalNodes(new String[] { "NTE.3", "OBX.5" });
    }

    /**
     * Returns an XML-encoded HL7 message given an ER7-enconded HL7 message.
     * 
     * @param source
     *            an ER7-encoded HL7 message.
     * @return
     */
    public String toXML(String source) throws SerializerException {
        StringBuilder builder = new StringBuilder();
        if (useStrictParser) {
            try {
                builder.append(xmlParser.encode(pipeParser.parse(source.trim())));
            } catch (HL7Exception e) {
                throw new SerializerException(e);
            }
        } else {
            try {

                ER7Reader er7Reader = new ER7Reader(handleRepetitions, convertLFtoCR);
                StringWriter stringWriter = new StringWriter();
                XMLPrettyPrinter serializer = new XMLPrettyPrinter(stringWriter);
                serializer.setEncodeEntities(true);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                try {
                    er7Reader.setContentHandler(serializer);
                    er7Reader.parse(new InputSource(new StringReader(source)));
                    os.write(stringWriter.toString().getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                builder.append(os.toString());

            } catch (Exception e) {
                String exceptionMessage = e.getClass().getName() + ":" + e.getMessage();
                logger.error(exceptionMessage);
            }
        }
        return builder.toString();
    }

    /**
     * Returns an ER7-encoded HL7 message given an XML-encoded HL7 message.
     * 
     * @param source
     *            a XML-encoded HL7 message.
     * @return
     */
    public String fromXML(String source) throws SerializerException {
        StringBuilder builder = new StringBuilder();
        if (useStrictParser) {
            try {
                builder.append(pipeParser.encode(xmlParser.parse(source)));
            } catch (HL7Exception e) {
                throw new SerializerException(e);
            }
        } else {

            try {
                // The delimiters below need to come from the XML somehow...the
                // ER7 handler should take care of it
                // TODO: Ensure you get these elements from the XML
                String segmentDelimiter = getXMLValue(source, "<MSH.1>", "</MSH.1>"); // usually
                // |
                String fieldDelimiter = "^";
                String repetitionDelimiter = "~";
                String escapeSequence = "\\";
                String subcomponentDelimiter = "&";
                // Our delimiters usually look like this:
                // <MSH.2>^~\&amp;</MSH.2>
                // We need to decode XML entities
                String otherDelimiters = getXMLValue(source, "<MSH.2>", "</MSH.2>").replaceAll("&amp;", "&");
                if (otherDelimiters.length() == 4) {
                    fieldDelimiter = otherDelimiters.substring(0, 1); // usually
                    // ^
                    repetitionDelimiter = otherDelimiters.substring(1, 2); // usually
                    // ~
                    escapeSequence = otherDelimiters.substring(2, 3); // usually
                    // \
                    subcomponentDelimiter = otherDelimiters.substring(3, 4); // usually
                    // &
                }
                // String fieldDelimiter =
                ER7XMLHandler handler = new ER7XMLHandler("\r", segmentDelimiter, fieldDelimiter, subcomponentDelimiter, repetitionDelimiter, escapeSequence, true);
                XMLReader xr = XMLReaderFactory.createXMLReader();
                xr.setContentHandler(handler);
                xr.setErrorHandler(handler);
                // Parse, but first replace all spaces between brackets. This
                // fixes pretty-printed XML we might receive
                xr.parse(new InputSource(new StringReader(source.replaceAll("\\s*<([^/][^>]*)>", "<$1>").replaceAll("<(/[^>]*)>\\s*", "<$1>"))));
                builder.append(handler.getOutput());
            } catch (Exception e) {
                String exceptionMessage = e.getClass().getName() + ":" + e.getMessage();
                logger.error(exceptionMessage);
                throw new SerializerException(e);
            }
        }
        return builder.toString();
    }

    public Map<String, String> getMetadataFromXML(String xmlSource) throws SerializerException {
        Map<String, String> map = new HashMap<String, String>();

        if (useStrictParser) {
            try {
                Message message = xmlParser.parse(xmlSource);
                Terser terser = new Terser(message);
                String sendingFacility = terser.get("/MSH-4-1");
                String event = terser.get("/MSH-9-1") + "-" + terser.get("/MSH-9-2");
                map.put("version", message.getVersion());
                map.put("type", event);
                map.put("source", sendingFacility);
            } catch (Exception e) {
                new SerializerException(e);
            }
            return map;
        } else {
            String sendingFacility = getXMLValue(xmlSource, "<MSH.4.1>", "</MSH.4.1>");
            String event = getXMLValue(xmlSource, "<MSH.9.1>", "</MSH.9.1>");
            String subType = getXMLValue(xmlSource, "<MSH.9.2>", "</MSH.9.2>");
            if (!subType.equals("")) {
                event += "-" + subType;
            }
            if (event.equals("")) {
                event = "Unknown";
            }
            String version = getXMLValue(xmlSource, "<MSH.12.1>", "</MSH.12.1>");
            map.put("version", version);
            map.put("type", event);
            map.put("source", sendingFacility);
            return map;
        }
    }

    public Map<String, String> getMetadataFromEncoded(String source) throws SerializerException {
        Map<String, String> map = new HashMap<String, String>();

        if (useStrictParser) {
            try {
                Message message = pipeParser.parse(source.trim()); // this had
                // a
                // replaceAll("\n",
                // "\r")
                // before
                // 1.7
                Terser terser = new Terser(message);
                String sendingFacility = terser.get("/MSH-4-1");
                String event = terser.get("/MSH-9-1") + "-" + terser.get("/MSH-9-2");
                map.put("version", message.getVersion());
                map.put("type", event);
                map.put("source", sendingFacility);
            } catch (Exception e) {
                new SerializerException(e);
            }
            return map;
        } else {
            source = source.trim(); // this had a replaceAll("\n", "\r") before
            // 1.7
            if (source == null || source.length() < 3) {
                logger.error("Unable to parse, message is null or too short: " + source);
                throw new SerializerException("Unable to parse, message is null or too short: " + source);
            }

            String segmentDelim = "\r";
            char fieldDelim = source.charAt(3);
            char elementDelim = source.charAt(4);
            String mshFields[] = source.trim().split(segmentDelim)[0].split(Pattern.quote(String.valueOf(fieldDelim)));
            Pattern elementPattern = Pattern.compile(Pattern.quote(String.valueOf(elementDelim)));
            int mshFieldsLength = mshFields.length;

            String event = "";

            if (mshFieldsLength > 8) {
                String[] msh9 = elementPattern.split(mshFields[8]); // MSH.9
                event = msh9[0]; // MSH.9.1

                if (msh9.length > 1) {
                    event = event + "-" + msh9[1]; // MSH.9.2
                }
            }

            if (event.equals(""))
                event = "Unknown";

            String sendingFacility = "";
            if (mshFieldsLength > 3) {
                sendingFacility = elementPattern.split(mshFields[3])[0]; // MSH.4.1
            }

            String version = "";
            if (mshFieldsLength > 11) {
                version = elementPattern.split(mshFields[11])[0]; // MSH.12.1
            }

            map.put("version", version);
            map.put("type", event);
            map.put("source", sendingFacility);
            return map;
        }
    }

    public String getXMLValue(String source, String startTag, String endTag) {
        String returnValue = "";
        int startLoc = -1;
        if ((startLoc = source.indexOf(startTag)) != -1) {
            returnValue = source.substring(startLoc + startTag.length(), source.indexOf(endTag, startLoc));
        }
        return returnValue;
    }

    public Map<String, String> getMetadataFromDocument(Document document) {
        Map<String, String> map = new HashMap<String, String>();
        if (useStrictParser) {
            try {
                DocumentSerializer serializer = new DocumentSerializer();
                String source = serializer.toXML(document);
                Message message = xmlParser.parse(source);
                Terser terser = new Terser(message);
                String sendingFacility = terser.get("/MSH-4-1");
                String event = terser.get("/MSH-9-1") + "-" + terser.get("/MSH-9-2");
                map.put("version", message.getVersion());
                map.put("type", event);
                map.put("source", sendingFacility);
                return map;
            } catch (Exception e) {
                logger.error(e.getMessage());
                return map;
            }
        } else {

            String sendingFacility = "";
            if (document.getElementsByTagName("MSH.4.1").getLength() > 0) {
                Node sender = document.getElementsByTagName("MSH.4.1").item(0);
                if (sender != null && sender.getFirstChild() != null) {
                    sendingFacility = sender.getFirstChild().getTextContent();
                }
            }
            String event = "Unknown";
            if (document.getElementsByTagName("MSH.9").getLength() > 0) {
                if (document.getElementsByTagName("MSH.9.1").getLength() > 0) {
                    Node type = document.getElementsByTagName("MSH.9.1").item(0);
                    if (type != null) {
                        event = type.getFirstChild().getNodeValue();
                        if (document.getElementsByTagName("MSH.9.2").getLength() > 0) {
                            Node subtype = document.getElementsByTagName("MSH.9.2").item(0);
                            event += "-" + subtype.getFirstChild().getTextContent();
                        }
                    }
                }
            }
            String version = "";
            if (document.getElementsByTagName("MSH.12.1").getLength() > 0) {
                Node versionNode = document.getElementsByTagName("MSH.12.1").item(0);
                if (versionNode != null) {
                    version = versionNode.getFirstChild().getTextContent();
                }
            }
            map.put("version", version);
            map.put("type", event);
            map.put("source", sendingFacility);
            return map;
        }
    }
}
