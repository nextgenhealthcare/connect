/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.model.converters;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class ER7XMLHandler extends DefaultHandler {
    private enum Location {
        DOCUMENT, SEGMENT, ELEMENT, SUBELEMENT
    };

    private String segmentDelimeter;
    private String fieldDelimeter;
    private String componentDelimeter;
    private String repetitionSeparator;
    private Location currentLocation = Location.DOCUMENT;
    private boolean sawHeader = false;
    private boolean lastInSubelement = false;
    private boolean enteredMSH1 = false;
    private boolean enteredMSH2 = false;
    private boolean foundMSH1 = false;
    private boolean foundMSH2 = false;
    private String lastSegment = new String();
    private String lastElement = new String();
    private StringBuilder output = new StringBuilder();
    private boolean encodeEntities = false;

    public ER7XMLHandler(String segmentDelimeter, String fieldDelimeter, String componentDelimeter, String subcomponentDelimeter, String repetitionSeparator, String escapeCharacter, boolean encodeEntities) {
        super();
        this.segmentDelimeter = segmentDelimeter;
        this.fieldDelimeter = fieldDelimeter;
        this.componentDelimeter = componentDelimeter;
        this.repetitionSeparator = repetitionSeparator;
        this.encodeEntities = encodeEntities;
    }

    public StringBuilder getOutput() {
        return output;
    }

    public void startDocument() {
        currentLocation = Location.DOCUMENT;
    }

    public void endDocument() {
        currentLocation = Location.DOCUMENT;
    }

    public void startElement(String uri, String name, String qName, Attributes attributes) {
        if (sawHeader == false) {
            sawHeader = true;
        } else {
            if (!foundMSH2 && (name.equals("MSH.2") || name.equals("BHS.2") || name.equals("FHS.2"))) {
                enteredMSH2 = true;
                foundMSH2 = true;
            }
            
            if (!foundMSH1 && (name.equals("MSH.1") || name.equals("BHS.1") || name.equals("FHS.1"))) {
                lastInSubelement = false;
                enteredMSH1 = true;
                foundMSH1 = true;
            } else if (currentLocation.equals(Location.DOCUMENT)) {
                output.append(name);
                currentLocation = Location.SEGMENT;
                lastInSubelement = false;
            } else if (currentLocation.equals(Location.SEGMENT)) {
                if (lastSegment.equals(name)) {
                    output.append(repetitionSeparator);
                } else {
                    // handle any missing fields
                    if (!enteredMSH2) {
                        int lastFieldId = 0;
                        if (lastSegment.length() > 0) {
                            lastFieldId = Integer.parseInt(lastSegment.split("\\.")[1]);
                        }

                        // get the second part, the id
                        int currentFieldId = Integer.parseInt(name.split("\\.")[1]);
                        int difference = currentFieldId - lastFieldId;

                        for (int i = 1; i < difference; i++) {
                            output.append(fieldDelimeter);
                        }
                    }

                    output.append(fieldDelimeter);
                    lastSegment = name;
                }

                currentLocation = Location.ELEMENT;
                lastInSubelement = false;
            } else if (currentLocation.equals(Location.ELEMENT)) {
                if (lastInSubelement) {
                    output.append(componentDelimeter);
                }

                // handle any missing elements
                int lastFieldId = 0;

                if (lastElement.length() > 0) {
                    lastFieldId = Integer.parseInt(lastElement.split("\\.")[2]);
                }

                int currentFieldId = Integer.parseInt(name.split("\\.")[2]);
                int difference = currentFieldId - lastFieldId;

                for (int i = 1; i < difference; i++) {
                    output.append(componentDelimeter);
                }

                lastElement = name;
                currentLocation = Location.SUBELEMENT;
                lastInSubelement = true;
            }
        }
    }

    public void endElement(String uri, String name, String qName) {
        if (foundMSH2 && (name.equals("MSH.2") || name.equals("BHS.2") || name.equals("FHS.2"))) {
            enteredMSH2 = false;
            foundMSH2 = false;
        }

        if (foundMSH1 && (name.equals("MSH.1") || name.equals("BHS.1") || name.equals("FHS.1"))) {
            enteredMSH1 = false;
            foundMSH1 = false;
            output.deleteCharAt(output.length() - 1);
        } else if (currentLocation.equals(Location.SEGMENT)) {
            output.append(segmentDelimeter);
            currentLocation = Location.DOCUMENT;
            lastSegment = "";
        } else if (currentLocation.equals(Location.ELEMENT)) {
            lastElement = "";
            currentLocation = Location.SEGMENT;
        } else if (currentLocation.equals(Location.SUBELEMENT)) {
            currentLocation = Location.ELEMENT;
        } else if (currentLocation.equals(Location.DOCUMENT)) {
            // do nothing
        }
    }

    public void characters(char ch[], int start, int length) {
        if (enteredMSH1) {
            fieldDelimeter = ch[start] + "";
            enteredMSH1 = false;
        } else if (enteredMSH2) {
            componentDelimeter = ch[start] + "";
            enteredMSH2 = false;
        }

        output.append(ch, start, length);
    }
}
