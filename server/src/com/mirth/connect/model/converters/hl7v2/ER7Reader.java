/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters.hl7v2;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ER7Reader extends SAXParser {
    private Logger logger = Logger.getLogger(this.getClass());
    private boolean handleRepetitions = false;
    private boolean handleSubcomponents = false;
    private boolean convertLFtoCR = false;

    private static final char[] EMPTY_CHAR_ARRAY = "".toCharArray();
    private static final String DEFAULT_SEGMENT_TERMINATOR = "\r";
    private static final String DEFAULT_FIELD_SEPARATOR = "|";
    private static final String DEFAULT_COMPONENT_SEPARATOR = "^";
    private static final String DEFAULT_REPETITION_SEPARATOR = "~";
    private static final String DEFAULT_ESCAPE_CHARACTER = "\\";
    private static final String DEFAULT_SUBCOMPONENT_TERMINATOR = "&";
    public static final String MESSAGE_ROOT_ID = "HL7Message";

    public ER7Reader(boolean handleRepetitions, boolean handleSubcomponents, boolean convertLFtoCR) {
        this.handleRepetitions = handleRepetitions;
        this.handleSubcomponents = handleSubcomponents;
        this.convertLFtoCR = convertLFtoCR;
    }

    private String getMessageFromSource(InputSource source) throws IOException {
        BufferedReader reader = new BufferedReader(source.getCharacterStream());
        StringBuilder builder = new StringBuilder();
        String line = "";

        if (convertLFtoCR) {
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                // TODO: This should be configurable
                builder.append("\r");
            }
        } else {
            int c;

            while ((c = reader.read()) != -1) {
                builder.append((char) c);
            }
        }

        return builder.toString().trim();
    }

    public void parse(InputSource source) throws SAXException, IOException {
        String message = getMessageFromSource(source);
        ContentHandler contentHandler = getContentHandler();
        contentHandler.startDocument();

        // first tokenize the segments
        if ((message == null) || (message.length() < 3)) {
            throw new SAXException("Unable to parse message. It is NULL or too short. " + message);
        }

        // usually |
        String segmentTerminator = DEFAULT_SEGMENT_TERMINATOR;
        String fieldSeparator = DEFAULT_FIELD_SEPARATOR;
        String componentSeparator = DEFAULT_COMPONENT_SEPARATOR;
        String repetitionSeparator = DEFAULT_REPETITION_SEPARATOR;
        String escapeCharacter = DEFAULT_ESCAPE_CHARACTER;
        String subcomponentSeparator = DEFAULT_SUBCOMPONENT_TERMINATOR;

        // if we have a header, grab the actual separators from the message
        if (message.substring(0, 3).equalsIgnoreCase("MSH")) {
            fieldSeparator = new String(new char[] { message.charAt(3) });

            int nextDelimeter = message.indexOf(message.charAt(3), 4);

            if (nextDelimeter > 4) {
                // usually ^
                componentSeparator = new String(new char[] { message.charAt(4) });
            }

            if (nextDelimeter > 5) {
                // usually ~
                repetitionSeparator = new String(new char[] { message.charAt(5) });
            }

            if (nextDelimeter > 6) {
                // usually \
                escapeCharacter = new String(new char[] { message.charAt(6) });
            }

            if (nextDelimeter > 7) {
                // usually &
                subcomponentSeparator = new String(new char[] { message.charAt(7) });
            }
        }

        // replace the special case of ^~& with ^~\& (MIRTH-1544)
        if ("^~&|".equals(message.substring(4, 8))) {
            escapeCharacter = "\\";
            subcomponentSeparator = "&";
            repetitionSeparator = "~";
            componentSeparator = "^";
        }

        // tokenize the segments first
        StringTokenizer segmentTokenizer = new StringTokenizer(message, segmentTerminator);
        String documentHead = handleSegments("", contentHandler, fieldSeparator, componentSeparator, subcomponentSeparator, repetitionSeparator, escapeCharacter, segmentTokenizer);
        contentHandler.endElement("", documentHead, "");
        contentHandler.endDocument();
    }

    private String handleSegments(String documentHead, ContentHandler contentHandler, String fieldSeparator, String componentSeparator, String subcomponentSeparator, String repetitionSeparator, String escapeCharacter, StringTokenizer segmentTokenizer) throws SAXException {
        int segmentCount = 0;

        while (segmentTokenizer.hasMoreTokens()) {
            String segment = segmentTokenizer.nextToken();
            logger.trace("handling segment: " + segment);
            // loop through each segment and pull out the fields
            StringTokenizer fieldTokenizer = new StringTokenizer(segment, fieldSeparator, true);

            if (fieldTokenizer.hasMoreTokens()) {
                // the XML element is named after the first field
                String segmentId = fieldTokenizer.nextToken().trim();

                if (segmentCount == 0) {
                    documentHead = MESSAGE_ROOT_ID;
                    contentHandler.startElement("", documentHead, "", null);
                }

                contentHandler.startElement("", segmentId, "", null);
                handleFieldOrRepetitions(contentHandler, fieldSeparator, componentSeparator, subcomponentSeparator, repetitionSeparator, escapeCharacter, segmentId, fieldTokenizer);
                contentHandler.endElement("", segmentId, "");
            } else {
                throw new SAXException("Could not find fields in segment: " + segment);
            }

            segmentCount++;
        }

        return documentHead;
    }

    private void handleFieldOrRepetitions(ContentHandler contentHandler, String fieldSeparator, String componentSeparator, String subcomponentSeparator, String repetitionSeparator, String escapeCharacter, String segmentId, StringTokenizer fieldTokenizer) throws SAXException {
        int fieldId = 0;
        boolean atLastField = false;

        while (fieldTokenizer.hasMoreTokens()) {
            boolean enteredHeader = false;

            /*
             * Go through each element and add as new child under the segment
             * element
             */
            String field = fieldTokenizer.nextToken();

            // the naming is SEG.<field#>
            if (field.equals(fieldSeparator)) {
                if (atLastField) {
                    contentHandler.startElement("", segmentId + "." + fieldId, "", null);
                    contentHandler.endElement("", segmentId + "." + fieldId, "");
                }

                fieldId++;
                atLastField = true;
            } else {
                logger.trace("handling field or repetition: " + field);
                atLastField = false;

                // batch support
                if (segmentId.equals("MSH") || segmentId.equals("FHS") || segmentId.equals("BHS")) {
                    enteredHeader = true;
                }

                if (enteredHeader && (fieldId == 1)) {
                    contentHandler.startElement("", segmentId + "." + fieldId, "", null);
                    contentHandler.characters(fieldSeparator.toCharArray(), 0, 1);
                    contentHandler.endElement("", segmentId + "." + (fieldId), null);
                    fieldId++;
                    contentHandler.startElement("", segmentId + "." + fieldId, "", null);
                    char[] specialCharacters = new char[] { componentSeparator.charAt(0), repetitionSeparator.charAt(0), escapeCharacter.charAt(0), subcomponentSeparator.charAt(0) };
                    contentHandler.characters(specialCharacters, 0, specialCharacters.length);
                    contentHandler.endElement("", segmentId + "." + (fieldId), null);
                } else if (enteredHeader && (fieldId == 2)) {
                    // do nothing
                } else {
                    if (handleRepetitions) {
                        handleFieldRepetitions(contentHandler, componentSeparator, repetitionSeparator, subcomponentSeparator, segmentId, fieldId, field);
                    } else {
                        handleField(contentHandler, componentSeparator, subcomponentSeparator, segmentId, fieldId, field);
                    }
                }
            }
        }

        if (atLastField) {
            contentHandler.startElement("", segmentId + "." + fieldId, "", null);
            contentHandler.endElement("", segmentId + "." + fieldId, "");
        }
    }

    private void handleFieldRepetitions(ContentHandler contentHandler, String componentSeparator, String repetitionSeparator, String subcomponentSeparator, String segmentId, int fieldId, String field) throws SAXException {
        StringTokenizer fieldRepetitionTokenizer = new StringTokenizer(field, repetitionSeparator, true);
        boolean atLastRepetition = true;

        while (fieldRepetitionTokenizer.hasMoreTokens()) {
            field = fieldRepetitionTokenizer.nextToken();

            if (field.equals(repetitionSeparator)) {
                // check for ~~
                if (atLastRepetition) {
                    contentHandler.startElement("", segmentId + "." + fieldId, "", null);
                    contentHandler.characters(EMPTY_CHAR_ARRAY, 0, 0);
                    contentHandler.endElement("", segmentId + "." + fieldId, "");
                }

                atLastRepetition = true;
            } else {
                logger.trace("handling repetition: " + field);
                atLastRepetition = false;
                handleField(contentHandler, componentSeparator, subcomponentSeparator, segmentId, fieldId, field);
            }
        }

        if (atLastRepetition) {
            contentHandler.startElement("", segmentId + "." + fieldId, "", null);
            contentHandler.characters(EMPTY_CHAR_ARRAY, 0, 0);
            contentHandler.endElement("", segmentId + "." + fieldId, "");
        }
    }

    private void handleField(ContentHandler contentHandler, String componentSeparator, String subcomponentSeparator, String segmentId, int fieldId, String field) throws SAXException {
        if ((field.indexOf(componentSeparator) > -1) || (handleSubcomponents && (field.indexOf(subcomponentSeparator) > -1))) {
            contentHandler.startElement("", segmentId + "." + fieldId, "", null);
            StringTokenizer componentTokenizer = new StringTokenizer(field, componentSeparator, true);
            handleComponents(contentHandler, componentSeparator, subcomponentSeparator, segmentId, fieldId, 1, componentTokenizer);
            contentHandler.endElement("", segmentId + "." + fieldId, null);
        } else {
            logger.trace("handling field: " + field);
            contentHandler.startElement("", segmentId + "." + fieldId, "", null);
            contentHandler.startElement("", segmentId + "." + fieldId + ".1", "", null);
            contentHandler.characters(field.toCharArray(), 0, field.length());
            contentHandler.endElement("", segmentId + "." + fieldId + ".1", null);
            contentHandler.endElement("", segmentId + "." + fieldId, null);
        }
    }

    private void handleComponents(ContentHandler contentHandler, String componentSeparator, String subcomponentSeparator, String segmentId, int fieldId, int componentId, StringTokenizer componentTokenizer) throws SAXException {
        boolean atLastComponent = true;

        while (componentTokenizer.hasMoreTokens()) {
            String component = componentTokenizer.nextToken();

            if (component.equals(componentSeparator)) {
                if (atLastComponent) {
                    contentHandler.startElement("", segmentId + "." + fieldId + "." + componentId, "", null);
                    contentHandler.characters(EMPTY_CHAR_ARRAY, 0, 0);
                    contentHandler.endElement("", segmentId + "." + fieldId + "." + componentId, "");
                }

                componentId++;
                atLastComponent = true;
            } else {
                atLastComponent = false;
                handleComponent(contentHandler, subcomponentSeparator, segmentId, fieldId, componentId, component);
            }
        }

        if (atLastComponent) {
            contentHandler.startElement("", segmentId + "." + fieldId + "." + componentId, "", null);
            contentHandler.characters(EMPTY_CHAR_ARRAY, 0, 0);
            contentHandler.endElement("", segmentId + "." + fieldId + "." + componentId, "");
        }
    }

    private void handleComponent(ContentHandler contentHandler, String subcomponentSeparator, String segmentId, int fieldId, int componentId, String component) throws SAXException {
        if (handleSubcomponents && (component.indexOf(subcomponentSeparator) > -1)) {
            contentHandler.startElement("", segmentId + "." + fieldId + "." + componentId, "", null);
            // check if we have subcomponents, if so add them
            StringTokenizer subcomponentTokenizer = new StringTokenizer(component, subcomponentSeparator, true);
            handleSubcomponents(contentHandler, subcomponentSeparator, segmentId, fieldId, componentId, 1, subcomponentTokenizer);
            contentHandler.endElement("", segmentId + "." + fieldId + "." + componentId, null);
        } else {
            logger.trace("handling component: " + component);
            // the naming is SEG.<field#>.<component#>
            contentHandler.startElement("", segmentId + "." + fieldId + "." + componentId, "", null);
            contentHandler.characters(component.toCharArray(), 0, component.length());
            contentHandler.endElement("", segmentId + "." + fieldId + "." + componentId, "");
        }
    }

    private void handleSubcomponents(ContentHandler contentHandler, String subcomponentSeparator, String segmentId, int fieldId, int componentId, int subcomponentId, StringTokenizer subcomponentTokenizer) throws SAXException {
        boolean atLastSubcomponent = true;

        while (subcomponentTokenizer.hasMoreTokens()) {
            String subcomponent = subcomponentTokenizer.nextToken();

            if (subcomponent.equals(subcomponentSeparator)) {
                if (atLastSubcomponent) {
                    contentHandler.startElement("", segmentId + "." + fieldId + "." + componentId + "." + subcomponentId, "", null);
                    contentHandler.characters(EMPTY_CHAR_ARRAY, 0, 0);
                    contentHandler.endElement("", segmentId + "." + fieldId + "." + componentId + "." + subcomponentId, "");
                }

                subcomponentId++;
                atLastSubcomponent = true;
            } else {
                logger.trace("handling subcomponent: " + subcomponentId);
                atLastSubcomponent = false;
                // the naming is SEG.<field#>.<component#>.<subcomponent#>
                contentHandler.startElement("", segmentId + "." + fieldId + "." + componentId + "." + subcomponentId, "", null);
                contentHandler.characters(subcomponent.toCharArray(), 0, subcomponent.length());
                contentHandler.endElement("", segmentId + "." + fieldId + "." + componentId + "." + subcomponentId, "");
            }
        }

        if (atLastSubcomponent) {
            contentHandler.startElement("", segmentId + "." + fieldId + "." + componentId + "." + subcomponentId, "", null);
            contentHandler.characters(EMPTY_CHAR_ARRAY, 0, 0);
            contentHandler.endElement("", segmentId + "." + fieldId + "." + componentId + "." + subcomponentId, "");
        }
    }
}
