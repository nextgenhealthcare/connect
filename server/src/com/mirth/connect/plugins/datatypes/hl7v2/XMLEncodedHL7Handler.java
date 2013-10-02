/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.hl7v2;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
    private int rootLevel = -1;

    private int previousDelimeterCount = -1;
    private int previousDelimiterLength = 1;
    private String[] previousFieldNameArray;
    private String[] previousComponentNameArray;
    private String[] previousSubcomponentNameArray;

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

        String[] localNameArray = StringUtils.split(localName, ID_DELIMETER);

        if (rootLevel == -1) {
            rootLevel = localNameArray.length;
        }

        /*
         * Skip the root element, MSH.1, and MSH.2 since those don't have any
         * data that we care about.
         */
        if ((localNameArray.length == 1) && (localNameArray[0].equals(ER7Reader.MESSAGE_ROOT_ID))) {
            rootLevel = 0;
            return;
        } else if (localNameArray.length == 2) {
            if (isHeaderSegment(localNameArray[0])) {
                if ((localNameArray[1].length() == 1) && (localNameArray[1].charAt(0) == '1' || localNameArray[1].charAt(0) == '2')) {
                    previousFieldNameArray = localNameArray;
                    return;
                }
            }
        }

        /*
         * If the element that we've found is the same as the last, then we have
         * a repetition, so we remove the last separator that was added and
         * append to repetition separator.
         */
        if (ArrayUtils.isEquals(localNameArray, previousFieldNameArray)) {
            output.deleteCharAt(output.length() - 1);
            output.append(repetitionSeparator);
            return;
        }

        /*
         * To find the delimeter count we are splitting the element name by the
         * ID delimeter.
         */
        int currentDelimeterCount = localNameArray.length - 1;

        /*
         * MIRTH-2078: Don't add missing fields/components/subcomponents if the
         * current level was the starting level. This only pertains to partial
         * XML messages where the root is a field or component.
         */
        if (currentDelimeterCount == 1 && rootLevel <= 1) {
            /*
             * This will add missing fields if any (ex. between OBX.1 and
             * OBX.5).
             */
            int previousFieldId = 0;

            if (previousFieldNameArray != null) {
                previousFieldId = NumberUtils.toInt(previousFieldNameArray[1]);
            }

            int currentFieldId = NumberUtils.toInt(localNameArray[1]);

            for (int i = 1; i < (currentFieldId - previousFieldId); i++) {
                output.append(fieldSeparator);
            }

            previousFieldNameArray = localNameArray;
        } else if (currentDelimeterCount == 2 && rootLevel <= 2) {
            /*
             * This will add missing components if any (ex. between OBX.1.1 and
             * OBX.1.5).
             */
            int previousComponentId = 0;

            if (previousComponentNameArray != null) {
                previousComponentId = NumberUtils.toInt(previousComponentNameArray[2]);
            }

            int currentComponentId = NumberUtils.toInt(localNameArray[2]);

            for (int i = 1; i < (currentComponentId - previousComponentId); i++) {
                output.append(componentSeparator);
                previousDelimiterLength = componentSeparator.length();
            }

            previousComponentNameArray = localNameArray;
        } else if (currentDelimeterCount == 3 && rootLevel <= 3) {
            /*
             * This will add missing subcomponents if any (ex. between OBX.1.1.1
             * and OBX.1.1.5).
             */
            int previousSubcomponentId = 0;

            if (previousSubcomponentNameArray != null) {
                previousSubcomponentId = NumberUtils.toInt(previousSubcomponentNameArray[3]);
            }

            int currentSubcomponentId = NumberUtils.toInt(localNameArray[3]);

            for (int i = 1; i < (currentSubcomponentId - previousSubcomponentId); i++) {
                output.append(subcomponentSeparator);
                previousDelimiterLength = subcomponentSeparator.length();
            }

            previousSubcomponentNameArray = localNameArray;
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
            previousFieldNameArray = null;
        } else if (currentDelimeterCount == 1) {
            previousComponentNameArray = null;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        logger.trace("ending element: " + localName);
        inElement = false;

        String[] localNameArray = StringUtils.split(localName, ID_DELIMETER);

        /*
         * Once we see the closing of MSH.1 or MSH.2 tags, we know that the
         * separator characters have been added to the output buffer, so we can
         * grab them and set the local variables.
         */
        if ((localNameArray.length == 1) && (localNameArray[0].equals(ER7Reader.MESSAGE_ROOT_ID))) {
            return;
        } else if (localNameArray.length == 2) {
            if (isHeaderSegment(localNameArray[0])) {
                if ((localNameArray[1].length() == 1) && (localNameArray[1].charAt(0) == '1')) {
                    fieldSeparator = String.valueOf(output.charAt(output.length() - 1));
                    return;
                } else if ((localNameArray[1].length() == 1) && (localNameArray[1].charAt(0) == '2')) {
                    CharSequence separators = output.subSequence(4, output.length());
                    componentSeparator = String.valueOf(separators.charAt(0));
                    repetitionSeparator = String.valueOf(separators.charAt(1));
                    escapeCharacter = separators.length() > 2 ? String.valueOf(separators.charAt(2)) : "";
                    subcomponentSeparator = separators.length() > 3 ? String.valueOf(separators.charAt(3)) : "";
                }
            }
        }

        int currentDelimeterCount = localNameArray.length - 1;

        /*
         * We don't want to have tailing separators, so once we get to the last
         * element of a nested level, we delete the last character.
         */
        if (currentDelimeterCount > previousDelimeterCount) {
            previousDelimeterCount = currentDelimeterCount;
        } else if (currentDelimeterCount < previousDelimeterCount && previousDelimiterLength > 0) {
            output.deleteCharAt(output.length() - 1);
            previousDelimeterCount = currentDelimeterCount;
        }

        /*
         * The number of periods in the element tells us the level. So, MSH is
         * at level 0, MSH.3 is at level 1, MSH.3.1 at level 2, and so on. We
         * can use this to determine which seperator to append once the element
         * is closed.
         * 
         * MIRTH-2078: Only add the last character if the root delimiter is 0
         * (HL7Message) or the current element level is deeper than the root
         * level. This only pertains to partial XML messages where the root is a
         * field or component.
         */
        if (rootLevel == 0 || currentDelimeterCount >= rootLevel) {
            switch (currentDelimeterCount) {
                case 0:
                    output.append(segmentSeparator);
                    break;
                case 1:
                    output.append(fieldSeparator);
                    break;
                case 2:
                    output.append(componentSeparator);
                    previousDelimiterLength = componentSeparator.length();
                    break;
                case 3:
                    output.append(subcomponentSeparator);
                    previousDelimiterLength = subcomponentSeparator.length();
                    break;
                default:
                    break;
            }
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

    /**
     * This awesome piece of code returns true if the string is MSH|[B|F]HS
     * 
     * @param segmentName
     * @return
     */
    private boolean isHeaderSegment(String segmentName) {
        if (segmentName.length() == 3) {
            if (((segmentName.charAt(0) == 'M') && (segmentName.charAt(1) == 'S') && (segmentName.charAt(2) == 'H')) || ((segmentName.charAt(1) == 'H') && (segmentName.charAt(2) == 'S') && ((segmentName.charAt(0) == 'B') || (segmentName.charAt(0) == 'F')))) {
                return true;
            }
        }

        return false;
    }
}
