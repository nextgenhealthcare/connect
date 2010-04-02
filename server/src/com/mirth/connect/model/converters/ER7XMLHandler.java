/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class ER7XMLHandler extends DefaultHandler {
    private enum Location {
        MESSAGE, SEGMENT, FIELD, COMPONENT, SUBCOMPONENT
    };

    private Logger logger = Logger.getLogger(this.getClass());
    private static final String ID_DELIMETER = "\\.";
    private String segmentSeparator;
    private String fieldSeparator;
    private String repetitionSeparator;
    private String componentSeparator;
    private String subcomponentSeparator;
    private Location currentLocation = Location.MESSAGE;
    private boolean lastComponentInField = false;
    private boolean enteredHeader1 = false;
    private boolean enteredHeader2 = false;
    private boolean foundHeader1 = false;
    private boolean foundHeader2 = false;
    private String lastSegment = new String();
    private String lastField = new String();
    private StringBuilder output = new StringBuilder();
    private boolean encodeEntities = false;

    public ER7XMLHandler(String segmentSeparator, String fieldSeparator, String componentSeparator, String repetitionSeparator, String escapeCharacter, String subcomponentSeparator, boolean encodeEntities) {
        super();
        this.segmentSeparator = segmentSeparator;
        this.fieldSeparator = fieldSeparator;
        this.componentSeparator = componentSeparator;
        this.repetitionSeparator = repetitionSeparator;
        this.subcomponentSeparator = subcomponentSeparator;
        this.encodeEntities = encodeEntities;
        logger.trace("initialized ER7-to-XML handler: fieldSeparator=" + fieldSeparator + ", componentSeparator=" + componentSeparator + ", repetitionSeparator=" + repetitionSeparator + ", escapeCharacter=" + escapeCharacter + ", subcomponentSeparator=" + subcomponentSeparator);
    }

    public StringBuilder getOutput() {
        return output;
    }

    @Override
    public void startDocument() {
        currentLocation = Location.MESSAGE;
    }

    @Override
    public void endDocument() {
        currentLocation = Location.MESSAGE;
    }

    @Override
    public void startElement(String uri, String elementName, String qName, Attributes attributes) {
        logger.trace("starting element: " + elementName);

        // skip the root element
        if (elementName.equals(ER7Reader.MESSAGE_ROOT_ID)) {
            return;
        }

        if (!foundHeader2 && (elementName.equals("MSH.2") || elementName.equals("BHS.2") || elementName.equals("FHS.2"))) {
            enteredHeader2 = true;
            foundHeader2 = true;
        }
        
        if (!foundHeader1 && (elementName.equals("MSH.1") || elementName.equals("BHS.1") || elementName.equals("FHS.1"))) {
            enteredHeader1 = true;
            foundHeader1 = true;
            lastComponentInField = false;
        } else if (currentLocation.equals(Location.MESSAGE)) {
            output.append(elementName);
            currentLocation = Location.SEGMENT;
            lastComponentInField = false;
        } else if (currentLocation.equals(Location.SEGMENT)) {
            if (lastSegment.equals(elementName)) {
                output.append(repetitionSeparator);
            } else {
                // add any missing fields
                if (!enteredHeader2) {
                    int lastFieldId = 0;

                    if (lastSegment.length() > 0) {
                        lastFieldId = Integer.parseInt(lastSegment.split(ID_DELIMETER)[1]);
                    }

                    // get the second part, the id
                    int currentFieldId = Integer.parseInt(elementName.split(ID_DELIMETER)[1]);
                    int difference = currentFieldId - lastFieldId;

                    for (int i = 1; i < difference; i++) {
                        output.append(fieldSeparator);
                    }
                }

                output.append(fieldSeparator);
                lastSegment = elementName;
            }

            currentLocation = Location.FIELD;
            lastComponentInField = false;
        } else if (currentLocation.equals(Location.FIELD)) {
            if (lastComponentInField) {
                output.append(componentSeparator);
            }

            // add any missing components
            int lastFieldId = 0;

            if (lastField.length() > 0) {
                lastFieldId = Integer.parseInt(lastField.split(ID_DELIMETER)[2]);
            }

            int currentFieldId = Integer.parseInt(elementName.split(ID_DELIMETER)[2]);
            int difference = currentFieldId - lastFieldId;

            for (int i = 1; i < difference; i++) {
                output.append(componentSeparator);
            }

            lastField = elementName;
            currentLocation = Location.COMPONENT;
            lastComponentInField = true;
        } else if (currentLocation.equals(Location.COMPONENT)) {

        }
    }
    
    @Override
    public void endElement(String uri, String elementName, String qName) {
        logger.trace("ending element: " + elementName);

        if (foundHeader2 && (elementName.equals("MSH.2") || elementName.equals("BHS.2") || elementName.equals("FHS.2"))) {
            enteredHeader2 = false;
            foundHeader2 = false;
        }

        if (foundHeader1 && (elementName.equals("MSH.1") || elementName.equals("BHS.1") || elementName.equals("FHS.1"))) {
            enteredHeader1 = false;
            foundHeader1 = false;
            output.deleteCharAt(output.length() - 1);
        } else if (currentLocation.equals(Location.SEGMENT)) {
            output.append(segmentSeparator);
            currentLocation = Location.MESSAGE;
            lastSegment = "";
        } else if (currentLocation.equals(Location.FIELD)) {
            currentLocation = Location.SEGMENT;
            lastField = "";
        } else if (currentLocation.equals(Location.COMPONENT)) {
            currentLocation = Location.FIELD;
        }
    }

    @Override
    public void characters(char ch[], int start, int length) {
        if (enteredHeader1) {
            fieldSeparator = String.valueOf(ch[start]);
            enteredHeader1 = false;
        } else if (enteredHeader2) {
            componentSeparator = String.valueOf(ch[start]);
            repetitionSeparator = String.valueOf(ch[start + 1]);
            enteredHeader2 = false;
        }

        output.append(ch, start, length);
        logger.trace("characters: " + new String(ch, start, length));
    }
}
