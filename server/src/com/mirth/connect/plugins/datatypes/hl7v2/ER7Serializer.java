/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.hl7v2;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.parser.XMLParser;
import ca.uhn.hl7v2.util.Terser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

import java.io.IOException;
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

import com.mirth.connect.donkey.model.message.MessageSerializer;
import com.mirth.connect.donkey.model.message.MessageSerializerException;
import com.mirth.connect.model.converters.IMessageSerializer;
import com.mirth.connect.model.converters.XMLPrettyPrinter;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.model.util.DefaultMetaData;
import com.mirth.connect.util.ErrorMessageBuilder;
import com.mirth.connect.util.StringUtil;

public class ER7Serializer implements IMessageSerializer {
    private Logger logger = Logger.getLogger(this.getClass());
    private PipeParser serializationPipeParser = null;
    private XMLParser serializationXmlParser = null;
    private PipeParser deserializationPipeParser = null;
    private XMLParser deserializationXmlParser = null;

    private boolean skipIntermediateDelimiter = false;
    private String serializationSegmentDelimiter = null;
    private String deserializationSegmentDelimiter = null;
    private HL7v2SerializationProperties serializationProperties;
    private HL7v2DeserializationProperties deserializationProperties;

    private static Pattern ampersandPattern = Pattern.compile("&amp;");
    private static Pattern prettyPattern1 = Pattern.compile("\\s*<([^/][^>]*)>");
    private static Pattern prettyPattern2 = Pattern.compile("<([^>]*/|/[^>]*)>\\s*");

    public ER7Serializer(SerializerProperties properties) {
        serializationProperties = (HL7v2SerializationProperties) properties.getSerializationProperties();
        deserializationProperties = (HL7v2DeserializationProperties) properties.getDeserializationProperties();

        if (serializationProperties != null) {
            serializationSegmentDelimiter = StringUtil.unescape(serializationProperties.getSegmentDelimiter());

            if (serializationSegmentDelimiter.equals("\r") || serializationSegmentDelimiter.equals("\n") || serializationSegmentDelimiter.equals("\r\n")) {
                skipIntermediateDelimiter = true;
            }

            if (serializationProperties.isUseStrictParser()) {
                serializationPipeParser = new PipeParser();
                serializationXmlParser = new DefaultXMLParser();

                // turn off strict validation if needed
                if (!serializationProperties.isUseStrictValidation()) {
                    serializationPipeParser.setValidationContext(new NoValidation());
                    serializationXmlParser.setValidationContext(new NoValidation());
                }

                serializationXmlParser.setKeepAsOriginalNodes(new String[] { "NTE.3", "OBX.5" });
            }
        }

        if (deserializationProperties != null) {
            deserializationSegmentDelimiter = StringUtil.unescape(deserializationProperties.getSegmentDelimiter());

            if (deserializationProperties.isUseStrictParser()) {
                deserializationPipeParser = new PipeParser();
                deserializationXmlParser = new DefaultXMLParser();

                // turn off strict validation if needed
                if (!deserializationProperties.isUseStrictValidation()) {
                    deserializationPipeParser.setValidationContext(new NoValidation());
                    deserializationXmlParser.setValidationContext(new NoValidation());
                }

                deserializationXmlParser.setKeepAsOriginalNodes(new String[] { "NTE.3", "OBX.5" });
            }
        }
    }

    public String getDeserializationSegmentDelimiter() {
        return deserializationSegmentDelimiter;
    }

    /**
     * Do the serializer properties require serialization
     * 
     * @return
     */
    @Override
    public boolean isSerializationRequired(boolean toXml) {
        boolean serializationRequired = false;

        if (toXml) {
            if (serializationProperties.isUseStrictParser()) {
                serializationRequired = true;
            }
        } else {
            if (deserializationProperties.isUseStrictParser()) {
                serializationRequired = true;
            }
        }

        return serializationRequired;
    }

    @Override
    public String transformWithoutSerializing(String message, MessageSerializer outboundSerializer) throws MessageSerializerException {
        try {
            boolean transformed = false;
            ER7Serializer serializer = (ER7Serializer) outboundSerializer;
            String outputSegmentDelimiter = serializer.getDeserializationSegmentDelimiter();

            if (serializationProperties.isConvertLineBreaks()) {
                if (skipIntermediateDelimiter) {
                    /*
                     * When convert line breaks is on and transform without serializing is called,
                     * ordinarily line breaks would be converted to the serialization delimiter,
                     * then the serialization delimiter would be converted to the deserialization
                     * delimiter. In this case, we can skip a step by simply converting line breaks
                     * to the deserialization delimiter if the serialization delimiter is also a
                     * line break.
                     */
                    return StringUtil.convertLineBreaks(message, outputSegmentDelimiter);
                }

                message = StringUtil.convertLineBreaks(message, serializationSegmentDelimiter);
                transformed = true;
            }

            if (!serializationSegmentDelimiter.equals(outputSegmentDelimiter)) {
                message = StringUtils.replace(message, serializationSegmentDelimiter, outputSegmentDelimiter);
                transformed = true;
            }

            if (transformed) {
                return message;
            }
        } catch (Exception e) {
            throw new MessageSerializerException("Error transforming ER7", e, ErrorMessageBuilder.buildErrorMessage(this.getClass().getSimpleName(), "Error transforming ER7", e));
        }

        return null;
    }

    /**
     * Returns an XML-encoded HL7 message given an ER7-encoded HL7 message.
     * 
     * @param source
     *            an ER7-encoded HL7 message.
     * @return
     */
    @Override
    public String toXML(String source) throws MessageSerializerException {
        try {
            if (serializationProperties.isConvertLineBreaks()) {
                source = StringUtil.convertLineBreaks(source, serializationSegmentDelimiter);
            }

            if (serializationProperties.isUseStrictParser()) {
                Message message = null;
                source = source.trim();

                if (source.length() > 0 && source.charAt(0) == '<') {
                    if (serializationProperties.isUseStrictValidation()) {
                        // If the message is XML and strict validation is needed, we'll need to create a message to be encoded.
                        message = serializationXmlParser.parse(source);
                    } else {
                        // If the message is XML and strict validation is not needed, we can use the source directly.
                    }
                } else {
                    message = serializationPipeParser.parse(source);
                }

                if (message != null) {
                    source = serializationXmlParser.encode(message);
                }

                if (serializationProperties.isStripNamespaces()) {
                    source = StringUtil.stripNamespaces(source);
                }

                return source;
            } else {
                ER7Reader er7Reader = new ER7Reader(serializationProperties.isHandleRepetitions(), serializationProperties.isHandleSubcomponents(), serializationSegmentDelimiter);
                StringWriter stringWriter = new StringWriter();
                XMLPrettyPrinter serializer = new XMLPrettyPrinter(stringWriter);
                serializer.setEncodeEntities(true);
                er7Reader.setContentHandler(serializer);
                er7Reader.parse(new InputSource(new StringReader(source)));
                return stringWriter.toString();
            }
        } catch (Exception e) {
            throw new MessageSerializerException("Error converting ER7 to XML", e, ErrorMessageBuilder.buildErrorMessage(this.getClass().getSimpleName(), "Error converting ER7 to XML", e));
        }
    }

    /**
     * Returns an ER7-encoded HL7 message given an XML-encoded HL7 message.
     * 
     * @param source
     *            a XML-encoded HL7 message.
     * @return
     */
    @Override
    public String fromXML(String source) throws MessageSerializerException {
        try {
            if (deserializationProperties.isUseStrictParser()) {
                return deserializationPipeParser.encode(deserializationXmlParser.parse(source));
            } else {
                /*
                 * The delimiters below need to come from the XML somehow. The ER7 handler should
                 * take care of it TODO: Ensure you get these elements from the XML
                 */

                String fieldSeparator = getNodeValue(source, "<MSH.1>", "</MSH.1>");

                if (StringUtils.isEmpty(fieldSeparator)) {
                    fieldSeparator = "|";
                }

                String componentSeparator = "^";
                String repetitionSeparator = "~";
                String subcomponentSeparator = "&";
                String escapeCharacter = "\\";

                /*
                 * Our delimiters usually look like this: <MSH.2>^~\&amp;</MSH.2> We need to decode
                 * XML entities
                 */
                String separators = ampersandPattern.matcher(getNodeValue(source, "<MSH.2>", "</MSH.2>")).replaceAll("&");

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

                XMLEncodedHL7Handler handler = new XMLEncodedHL7Handler(deserializationSegmentDelimiter, fieldSeparator, componentSeparator, repetitionSeparator, escapeCharacter, subcomponentSeparator, true);
                XMLReader reader = XMLReaderFactory.createXMLReader();
                reader.setContentHandler(handler);
                reader.setErrorHandler(handler);

                /*
                 * Parse, but first replace all spaces between brackets. This fixes pretty-printed
                 * XML we might receive.
                 */
                reader.parse(new InputSource(new StringReader(prettyPattern2.matcher(prettyPattern1.matcher(source).replaceAll("<$1>")).replaceAll("<$1>"))));
                return handler.getOutput().toString();
            }
        } catch (Exception e) {
            throw new MessageSerializerException("Error converting XML to ER7", e, ErrorMessageBuilder.buildErrorMessage(this.getClass().getSimpleName(), "Error converting XML to ER7", e));
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

    @Override
    public Map<String, Object> getMetaDataFromMessage(String message) {
        Map<String, Object> map = new HashMap<String, Object>();
        populateMetaData(message, map);
        return map;
    }

    @Override
    public void populateMetaData(String message, Map<String, Object> map) {
        try {
            if (serializationProperties.isUseStrictParser()) {
                Message hapiMessage = serializationPipeParser.parse(message.trim());
                Terser terser = new Terser(hapiMessage);

                try {
                    map.put(DefaultMetaData.SOURCE_VARIABLE_MAPPING, (Object) terser.get("/MSH-4-1"));
                } catch (HL7Exception e) {
                    logger.error("Error populating ER7 metadata.", e);
                }

                try {
                    map.put(DefaultMetaData.TYPE_VARIABLE_MAPPING, terser.get("/MSH-9-1") + "-" + terser.get("/MSH-9-2"));
                } catch (HL7Exception e) {
                    logger.error("Error populating ER7 metadata.", e);
                }

                map.put(DefaultMetaData.VERSION_VARIABLE_MAPPING, hapiMessage.getVersion());
            } else {
                int index = 0;

                // Skip leading whitespace
                while (index < message.length() && message.charAt(index) <= ' ') {
                    index++;
                }

                // Get the index of the first segment delimiter
                int segmentDelimiterIndex = -1;
                if (serializationProperties.isConvertLineBreaks()) {
                    // If we're converting line breaks, check for CR, LF, and the serialization segment delimiter
                    int tempIndex = index;
                    while (segmentDelimiterIndex == -1 && tempIndex < message.length()) {
                        if (message.startsWith("\r", tempIndex) || message.startsWith("\n", tempIndex) || (!skipIntermediateDelimiter && message.startsWith(serializationSegmentDelimiter, tempIndex))) {
                            segmentDelimiterIndex = tempIndex;
                        }
                        tempIndex++;
                    }
                } else {
                    segmentDelimiterIndex = message.indexOf(serializationSegmentDelimiter, index);
                }

                if (segmentDelimiterIndex == -1) {
                    segmentDelimiterIndex = message.length();
                }

                // Return if the message doesn't start with MSH, FHS, or BHS
                boolean mshFound = false;
                if (message.startsWith("MSH", index)) {
                    mshFound = true;
                }
                if (!mshFound && !message.startsWith("FHS", index) && !message.startsWith("BHS", index)) {
                    return;
                }
                index += 3;

                if (index >= segmentDelimiterIndex || index >= message.length()) {
                    return;
                }

                int fieldSeparator = message.charAt(index++);
                int[] encodingCharacters = new int[] { -1, -1, -1, -1 };
                int c;

                // Attempt to find the encoding characters
                while (index < segmentDelimiterIndex && index < message.length() && (c = message.charAt(index)) != fieldSeparator) {
                    for (int i = 0; i < encodingCharacters.length; i++) {
                        if (encodingCharacters[i] == -1) {
                            encodingCharacters[i] = c;
                            break;
                        }
                    }
                    index++;
                }

                if (index >= segmentDelimiterIndex || index >= message.length()) {
                    return;
                }

                // At the beginning of third field, skip to fourth field
                index = message.indexOf(fieldSeparator, index + 1);
                if (index == -1 || index >= segmentDelimiterIndex || index >= message.length()) {
                    return;
                }

                // Get the source (fourth field)
                map.put(DefaultMetaData.SOURCE_VARIABLE_MAPPING, getComponent(message, index + 1, fieldSeparator, encodingCharacters, segmentDelimiterIndex, false));

                // Skip fields 4-8
                for (int i = 4; i <= 8; i++) {
                    index = message.indexOf(fieldSeparator, index + 1);
                    if (index == -1 || index >= segmentDelimiterIndex) {
                        return;
                    }
                }

                // Get the type and trigger (ninth field)
                map.put(DefaultMetaData.TYPE_VARIABLE_MAPPING, getComponent(message, index + 1, fieldSeparator, encodingCharacters, segmentDelimiterIndex, true));

                // Don't get the version for batches
                if (mshFound) {
                    // Skip fields 9-11
                    for (int i = 9; i <= 11; i++) {
                        index = message.indexOf(fieldSeparator, index + 1);
                        if (index == -1 || index >= segmentDelimiterIndex) {
                            return;
                        }
                    }

                    // Get the version (twelfth field)
                    map.put(DefaultMetaData.VERSION_VARIABLE_MAPPING, getComponent(message, index + 1, fieldSeparator, encodingCharacters, segmentDelimiterIndex, false));
                }
            }
        } catch (Exception e) {
            logger.error("Error populating ER7 metadata.", e);
        }
    }

    private String getComponent(String message, int index, int fieldSeparator, int[] encodingCharacters, int segmentDelimiterIndex, boolean combineSecond) throws IOException {
        StringBuilder result = new StringBuilder();
        boolean resultEnd = false;
        int c = -1;

        int componentSeparator = encodingCharacters[0];
        int repetitionMarker = encodingCharacters[1];
        int subcomponentSeparator = encodingCharacters[3];

        // Keep iterating until the reader is done, or until a field/component separator or repetition marker is found
        while (index < segmentDelimiterIndex && index < message.length() && (c = message.charAt(index)) != fieldSeparator && c != componentSeparator && (!serializationProperties.isHandleRepetitions() || c != repetitionMarker)) {
            if (serializationProperties.isHandleSubcomponents() && c == subcomponentSeparator) {
                resultEnd = true;
            } else if (!resultEnd) {
                result.append((char) c);
            }
            index++;
        }

        // If combining the second component and the previous iteration was stopped by a component separator
        if (combineSecond && c == componentSeparator) {
            boolean secondFound = false;
            index++;
            while (index < segmentDelimiterIndex && index < message.length() && (c = message.charAt(index)) != fieldSeparator && c != componentSeparator && (!serializationProperties.isHandleRepetitions() || c != repetitionMarker) && (!serializationProperties.isHandleSubcomponents() || c != subcomponentSeparator)) {
                if (!secondFound) {
                    result.append('-');
                    secondFound = true;
                }
                result.append((char) c);
                index++;
            }
        }

        return result.toString();
    }

    @Override
    public String toJSON(String message) throws MessageSerializerException {
        return null;
    }

    @Override
    public String fromJSON(String message) throws MessageSerializerException {
        return null;
    }
}