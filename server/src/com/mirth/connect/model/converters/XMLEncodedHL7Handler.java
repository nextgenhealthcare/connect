/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLEncodedHL7Handler extends DefaultHandler {
    private Logger logger = Logger.getLogger(this.getClass());
    private static final String ID_DELIMETER = ".";
    private String segmentSeparator;
    private String fieldSeparator;
    private String repetitionSeparator;
    private String escapeCharacter;
    private String componentSeparator;
    private String subcomponentSeparator;
    private boolean encodeEntities = false;
    private boolean inElement = false;

    private int previousDelimeterCount = -1;
    private String previousFieldName;

    private StringBuilder output = new StringBuilder();

    public XMLEncodedHL7Handler(String segmentSeparator, String fieldSeparator, String componentSeparator, String repetitionSeparator, String escapeCharacter, String subcomponentSeparator, boolean encodeEntities) {
        super();
        this.segmentSeparator = segmentSeparator;
        this.fieldSeparator = fieldSeparator;
        this.componentSeparator = componentSeparator;
        this.repetitionSeparator = repetitionSeparator;
        this.escapeCharacter = escapeCharacter;
        this.subcomponentSeparator = subcomponentSeparator;
        this.encodeEntities = encodeEntities;
        logger.trace("initialized ER7-to-XML handler: fieldSeparator=" + fieldSeparator + ", componentSeparator=" + componentSeparator + ", repetitionSeparator=" + repetitionSeparator + ", escapeCharacter=" + escapeCharacter + ", subcomponentSeparator=" + subcomponentSeparator);
    }

    public StringBuilder getOutput() {
        return output;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        logger.trace("starting element: " + localName);
        inElement = true;

        /*
         * Skip the root element, MSH.1, and MSH.2 since those don't have any
         * data that we care about.
         */
        if (localName.equals(ER7Reader.MESSAGE_ROOT_ID)) {
            return;
        } else if (localName.equals("MSH.1") || localName.equals("BHS.1") || localName.equals("FHS.1")) {
            return;
        } else if (localName.equals("MSH.2") || localName.equals("BHS.2") || localName.equals("FHS.2")) {
            return;
        }

        /*
         * If the element that we've found is the same as the last, then we have
         * a repetition, so we remove the last separator that was added and
         * append to repetition separator.
         */
        if (localName.equals(previousFieldName)) {
            output.deleteCharAt(output.length() - 1);
            output.append(repetitionSeparator);
            return;
        }

        int currentDelimeterCount = StringUtils.countMatches(localName, ID_DELIMETER);

        if (currentDelimeterCount == 1) {
            previousFieldName = localName;
        }

        /*
         * If we have an element with no periods, then we know its the name of
         * the segment, so write it to the output buffer followed by the field
         * separator.
         */
        if (currentDelimeterCount == 0) {
            output.append(localName);
            output.append(fieldSeparator);

            /*
             * Also set previousFieldName to null so that multiple segments in a
             * row with only one field don't trigger a repetition character.
             * (i.e. NTE|1<CR>NTE|2)
             */
            previousFieldName = null;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        logger.trace("ending element: " + localName);
        inElement = false;

        /*
         * Once we see the closing of MSH.1 or MSH.2 tags, we know that the
         * separator characters have been added to the output buffer, so we can
         * grab them and set the local variables.
         */
        if (localName.equals(ER7Reader.MESSAGE_ROOT_ID)) {
            return;
        } else if (localName.equals("MSH.1") || localName.equals("BHS.1") || localName.equals("FHS.1")) {
            fieldSeparator = String.valueOf(output.charAt(output.length() - 1));
            return;
        } else if (localName.equals("MSH.2") || localName.equals("BHS.2") || localName.equals("FHS.2")) {
            CharSequence separators = output.subSequence(output.length() - 4, output.length());
            componentSeparator = String.valueOf(separators.charAt(0));
            repetitionSeparator = String.valueOf(separators.charAt(1));
            escapeCharacter = String.valueOf(separators.charAt(2));
            subcomponentSeparator = String.valueOf(separators.charAt(3));
        }

        int currentDelimeterCount = StringUtils.countMatches(localName, ID_DELIMETER);

        /*
         * We don't want to have tailing separators, so once we get to the last
         * element of a nested level, we delete the last character.
         */
        if (currentDelimeterCount > previousDelimeterCount) {
            previousDelimeterCount = currentDelimeterCount;
        } else if (currentDelimeterCount < previousDelimeterCount) {
            output.deleteCharAt(output.length() - 1);
            previousDelimeterCount = currentDelimeterCount;
        }

        /*
         * The number of periods in the element tells us the level. So, MSH is
         * at level 0, MSH.3 is at level 1, MSH.3.1 at level 2, and so on. We
         * can use this to determine which seperator to append once the element
         * is closed.
         */
        switch (currentDelimeterCount) {
            case 0:
                output.append(segmentSeparator);
                break;
            case 1:
                output.append(fieldSeparator);
                break;
            case 2:
                output.append(componentSeparator);
                break;
            case 3:
                output.append(subcomponentSeparator);
                break;
            default:
                break;
        }
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        String str = new String(ch, start, length);

        /*
         * Write the substring to the output buffer, unless it is the field
         * separators (to avoid MSH.1. being written out).
         */
        if (inElement && !str.equals(fieldSeparator)) {
            logger.trace("writing output: " + str);
            output.append(str);
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        // Receive notification of ignorable whitespace in element content.
        logger.trace("found ignorable whitespace: length=" + length);
    }
}
