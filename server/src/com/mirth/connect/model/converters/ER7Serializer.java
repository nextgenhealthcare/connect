/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
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
    private PipeParser pipeParser = null;
    private XMLParser xmlParser = null;
    private boolean useStrictParser = false;
    private boolean useStrictValidation = false;
    private boolean stripNamespaces = true;  // Used in JST for strict parser
    private boolean handleRepetitions = false;
    private boolean handleSubcomponents = false;
    private boolean convertLFtoCR = true;

    public ER7Serializer() {
        initializeParser();
    }

    public ER7Serializer(Map properties) {
        if (properties != null) {
            if (properties.get("useStrictParser") != null) {
                this.useStrictParser = Boolean.parseBoolean((String) properties.get("useStrictParser"));
            }

            if (properties.get("useStrictValidation") != null) {
                this.useStrictValidation = Boolean.parseBoolean((String) properties.get("useStrictValidation"));
            }

            if (properties.get("stripNamespaces") != null) {
                this.stripNamespaces = Boolean.parseBoolean((String) properties.get("stripNamespaces"));
            }

            if (properties.get("handleRepetitions") != null) {
                this.handleRepetitions = Boolean.parseBoolean((String) properties.get("handleRepetitions"));
            }

            if (properties.get("handleSubcomponents") != null) {
                this.handleSubcomponents = Boolean.parseBoolean((String) properties.get("handleSubcomponents"));
            }

            if (properties.get("convertLFtoCR") != null) {
                this.convertLFtoCR = Boolean.parseBoolean((String) properties.get("convertLFtoCR"));
            }
        }

        if (useStrictParser) {
            initializeParser();
        }
    }

    public static Map<String, String> getDefaultProperties() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("useStrictParser", "false");
        properties.put("useStrictValidation", "false");
        properties.put("stripNamespaces", "true");
        properties.put("handleRepetitions", "false");
        properties.put("convertLFtoCR", "true");
        properties.put("handleSubcomponents", "false");
        return properties;
    }

    private void initializeParser() {
        pipeParser = new PipeParser();
        xmlParser = new DefaultXMLParser();

        // turn off strict validation if needed
        if (!useStrictValidation) {
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
                ER7Reader er7Reader = new ER7Reader(handleRepetitions, handleSubcomponents, convertLFtoCR);
                StringWriter stringWriter = new StringWriter();
                XMLPrettyPrinter serializer = new XMLPrettyPrinter(stringWriter);
                serializer.setEncodeEntities(true);
                er7Reader.setContentHandler(serializer);
                er7Reader.parse(new InputSource(new StringReader(source)));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                baos.write(stringWriter.toString().getBytes());
                builder.append(baos.toString());
            } catch (Exception e) {
                logger.error(e.getClass().getName() + ":" + e.getMessage());
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
                /*
                 * The delimiters below need to come from the XML somehow. The
                 * ER7 handler should take care of it TODO: Ensure you get these
                 * elements from the XML
                 */

                String segmentSeparator = "\r";
                String fieldSeparator = getNodeValue(source, "<MSH.1>", "</MSH.1>");
                
                if (StringUtils.isEmpty(fieldSeparator)) {
                    fieldSeparator = "|";
                }
                
                String componentSeparator = "^";
                String repetitionSeparator = "~";
                String subcomponentSeparator = "&";
                String escapeCharacter = "\\";

                /*
                 * Our delimiters usually look like this:
                 * <MSH.2>^~\&amp;</MSH.2> We need to decode XML entities
                 */
                String separators = getNodeValue(source, "<MSH.2>", "</MSH.2>").replaceAll("&amp;", "&");

                if (separators.length() == 4) {
                    // usually ^
                    componentSeparator = separators.substring(0, 1);
                    // usually ~
                    repetitionSeparator = separators.substring(1, 2);
                    // usually \
                    escapeCharacter = separators.substring(2, 3);
                    // usually &
                    subcomponentSeparator = separators.substring(3, 4);
                }

                XMLEncodedHL7Handler handler = new XMLEncodedHL7Handler(segmentSeparator, fieldSeparator, componentSeparator, repetitionSeparator, escapeCharacter, subcomponentSeparator, true);
                XMLReader reader = XMLReaderFactory.createXMLReader();
                reader.setContentHandler(handler);
                reader.setErrorHandler(handler);

                /*
                 * Parse, but first replace all spaces between brackets. This
                 * fixes pretty-printed XML we might receive.
                 */
                reader.parse(new InputSource(new StringReader(source.replaceAll("\\s*<([^/][^>]*)>", "<$1>").replaceAll("<(/[^>]*)>\\s*", "<$1>"))));
                builder.append(handler.getOutput());
            } catch (Exception e) {
                String exceptionMessage = e.getClass().getName() + ":" + e.getMessage();
                logger.error(exceptionMessage);
                throw new SerializerException(e);
            }
        }

        return builder.toString();
    }

    public Map<String, String> getMetadataFromXML(String source) throws SerializerException {
        Map<String, String> metadata = new HashMap<String, String>();

        if (useStrictParser) {
            try {
                Message message = xmlParser.parse(source);
                Terser terser = new Terser(message);
                String sendingFacility = terser.get("/MSH-4-1");
                String event = terser.get("/MSH-9-1") + "-" + terser.get("/MSH-9-2");
                metadata.put("version", message.getVersion());
                metadata.put("type", event);
                metadata.put("source", sendingFacility);
            } catch (Exception e) {
                new SerializerException(e);
            }

            return metadata;
        } else {
            String event = getNodeValue(source, "<MSH.9.1>", "</MSH.9.1>");
            String subType = getNodeValue(source, "<MSH.9.2>", "</MSH.9.2>");

            if (!subType.equals("")) {
                event += "-" + subType;
            }

            if (event.equals("")) {
                event = "Unknown";
            }

            metadata.put("version", getNodeValue(source, "<MSH.12.1>", "</MSH.12.1>"));
            metadata.put("type", event);
            metadata.put("source", getNodeValue(source, "<MSH.4.1>", "</MSH.4.1>"));
            return metadata;
        }
    }

    public Map<String, String> getMetadataFromEncoded(String source) throws SerializerException {
        Map<String, String> metadata = new HashMap<String, String>();

        if (useStrictParser) {
            try {
                // XXX: This had a replaceAll("\n", "\r") before 1.7
                Message message = pipeParser.parse(source.trim());
                Terser terser = new Terser(message);
                metadata.put("version", message.getVersion());
                metadata.put("type", terser.get("/MSH-9-1") + "-" + terser.get("/MSH-9-2"));
                metadata.put("source", terser.get("/MSH-4-1"));
            } catch (Exception e) {
                new SerializerException(e);
            }

            return metadata;
        } else {
            // XXX: This had a replaceAll("\n", "\r") before 1.7
            source = source.trim();

            if ((source == null) || (source.length() < 3)) {
                logger.error("Unable to parse, message is null or too short: " + source);
                throw new SerializerException("Unable to parse, message is null or too short: " + source);
            } else if (source.substring(0, 3).equalsIgnoreCase("MSH")) {
                String segmentDelimeter = "\r";
                String fieldDelimeter = String.valueOf(source.charAt(3));
                String elementDelimeter = String.valueOf(source.charAt(4));
                String mshFields[] = source.split(segmentDelimeter)[0].split(Pattern.quote(fieldDelimeter));
                Pattern elementPattern = Pattern.compile(Pattern.quote(elementDelimeter));

                if (mshFields.length > 8) {
                    // MSH.9
                    String[] msh9 = elementPattern.split(mshFields[8]);
                    // MSH.9.1
                    String type = msh9[0];

                    if (msh9.length > 1) {
                        // MSH.9.2
                        type += "-" + msh9[1];
                    }

                    metadata.put("type", type);
                } else {
                    metadata.put("type", "Unknown");
                }

                if (mshFields.length > 3) {
                    // MSH.4.1
                    metadata.put("source", elementPattern.split(mshFields[3])[0]);
                } else {
                    metadata.put("source", "");
                }

                if (mshFields.length > 11) {
                    // MSH.12.1
                    metadata.put("version", elementPattern.split(mshFields[11])[0]);
                } else {
                    metadata.put("version", "");
                }                
            } else {
                metadata.put("type", "Unknown");
                metadata.put("source", "Unknown");
                metadata.put("version", "Unknown");
            }

            return metadata;
        }
    }

    public Map<String, String> getMetadataFromDocument(Document document) {
        Map<String, String> metadata = new HashMap<String, String>();

        if (useStrictParser) {
            try {
                DocumentSerializer serializer = new DocumentSerializer();
                String source = serializer.toXML(document);
                Message message = xmlParser.parse(source);
                Terser terser = new Terser(message);
                String sendingFacility = terser.get("/MSH-4-1");
                String event = terser.get("/MSH-9-1") + "-" + terser.get("/MSH-9-2");
                metadata.put("version", message.getVersion());
                metadata.put("type", event);
                metadata.put("source", sendingFacility);
                return metadata;
            } catch (Exception e) {
                logger.error(e.getMessage());
                return metadata;
            }
        } else {
            if (document.getElementsByTagName("MSH.4.1").getLength() > 0) {
                Node senderNode = document.getElementsByTagName("MSH.4.1").item(0);

                if ((senderNode != null) && (senderNode.getFirstChild() != null)) {
                    metadata.put("source", senderNode.getFirstChild().getTextContent());
                } else {
                    metadata.put("source", "");
                }
            }

            if (document.getElementsByTagName("MSH.9").getLength() > 0) {
                if (document.getElementsByTagName("MSH.9.1").getLength() > 0) {
                    Node typeNode = document.getElementsByTagName("MSH.9.1").item(0);

                    if (typeNode != null) {
                        String type = typeNode.getFirstChild().getNodeValue();

                        if (document.getElementsByTagName("MSH.9.2").getLength() > 0) {
                            Node subTypeNode = document.getElementsByTagName("MSH.9.2").item(0);
                            type += "-" + subTypeNode.getFirstChild().getTextContent();
                        }

                        metadata.put("type", type);
                    } else {
                        metadata.put("type", "Unknown");
                    }
                }
            }

            if (document.getElementsByTagName("MSH.12.1").getLength() > 0) {
                Node versionNode = document.getElementsByTagName("MSH.12.1").item(0);

                if (versionNode != null) {
                    metadata.put("version", versionNode.getFirstChild().getTextContent());
                } else {
                    metadata.put("version", "");
                }
            }

            return metadata;
        }
    }

    private String getNodeValue(String source, String startTag, String endTag) {
        int startIndex = -1;

        if ((startIndex = source.indexOf(startTag)) != -1) {
            return source.substring(startIndex + startTag.length(), source.indexOf(endTag, startIndex));
        } else {
            return "";
        }
    }
}
