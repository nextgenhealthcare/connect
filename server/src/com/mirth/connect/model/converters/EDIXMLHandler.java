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
import org.apache.commons.lang.math.NumberUtils;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class EDIXMLHandler extends DefaultHandler {
    private String segmentDelimeter;
    private String elementDelimeter;
    private String subelementDelimeter;

    private enum Location {
        DOCUMENT, SEGMENT, ELEMENT, SUBELEMENT
    };

    private Location currentLocation = Location.DOCUMENT;
    private boolean sawHeader = false;
    private boolean lastInSubelement = false;

    private String[] previousSegmentNameArray = null;
    private String[] previousElementNameArray = null;

    private StringBuilder output = new StringBuilder();

    public EDIXMLHandler(String segmentDelimeter, String elementDelimeter, String subelementDelimeter) {
        super();
        this.segmentDelimeter = segmentDelimeter;
        this.elementDelimeter = elementDelimeter;
        this.subelementDelimeter = subelementDelimeter;
    }

    public void startDocument() {
        currentLocation = Location.DOCUMENT;
    }

    public void endDocument() {
        currentLocation = Location.DOCUMENT;
    }

    public void startElement(String uri, String name, String qName, Attributes atts) {
        if (sawHeader == false) {
            sawHeader = true;
        } else {
            if (currentLocation.equals(Location.DOCUMENT)) {
                output.append(name);
                currentLocation = Location.SEGMENT;
                lastInSubelement = false;
                
                previousSegmentNameArray = null;
            } else if (currentLocation.equals(Location.SEGMENT)) {
                String[] currentNameArray = StringUtils.split(name, ".");
                int currentDelimeterCount = currentNameArray.length - 1;

                if (currentDelimeterCount == 1) {
                    int previousId = 0;
                    
                    if (previousSegmentNameArray != null) {
                        previousId = NumberUtils.toInt(previousSegmentNameArray[1]);
                    }
                    
                    int currentId = NumberUtils.toInt(currentNameArray[1]);

                    for (int i = 1; i < (currentId - previousId); i++) {
                        output.append(elementDelimeter);
                    }

                    previousSegmentNameArray = currentNameArray;
                }
                
                output.append(elementDelimeter);
                currentLocation = Location.ELEMENT;
                lastInSubelement = false;
                
                previousElementNameArray = null;
            } else if (currentLocation.equals(Location.ELEMENT)) {
                String[] currentNameArray = StringUtils.split(name, ".");
                int currentDelimeterCount = currentNameArray.length - 1;

                if (currentDelimeterCount == 2) {
                    int previousId = 0;
                    
                    if (previousElementNameArray != null) {
                        previousId = NumberUtils.toInt(previousElementNameArray[2]);
                    }
                    
                    int currentId = NumberUtils.toInt(currentNameArray[2]);

                    for (int i = 1; i < (currentId - previousId); i++) {
                        output.append(subelementDelimeter);
                    }

                    previousElementNameArray = currentNameArray;
                }
                
                if (lastInSubelement) {
                    output.append(subelementDelimeter);
                }

                currentLocation = Location.SUBELEMENT;
                lastInSubelement = true;
            }
        }
    }

    public void endElement(String uri, String name, String qName) {
        if (currentLocation.equals(Location.SEGMENT)) {
            output.append(segmentDelimeter);
            currentLocation = Location.DOCUMENT;
        } else if (currentLocation.equals(Location.ELEMENT)) {
            currentLocation = Location.SEGMENT;
        } else if (currentLocation.equals(Location.SUBELEMENT)) {
            currentLocation = Location.ELEMENT;
        }
    }

    public void characters(char ch[], int start, int length) {
        output.append(ch, start, length);
    }

    public StringBuilder getOutput() {
        return output;
    }

    public void setOutput(StringBuilder output) {
        this.output = output;
    }
}
