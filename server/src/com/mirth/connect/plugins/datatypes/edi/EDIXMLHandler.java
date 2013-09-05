/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.edi;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class EDIXMLHandler extends DefaultHandler {
    private String segmentDelimiter;
    private String elementDelimiter;
    private String subelementDelimiter;

    private enum Location {
        DOCUMENT, SEGMENT, ELEMENT, SUBELEMENT
    };

    private Location currentLocation = Location.DOCUMENT;
    private boolean sawHeader = false;
    private boolean lastInSubelement = false;

    private String[] previousSegmentNameArray = null;
    private String[] previousElementNameArray = null;

    private StringBuilder output = new StringBuilder();

    public EDIXMLHandler() {
        super();
    }

    public void startDocument() {
        currentLocation = Location.DOCUMENT;
    }

    public void endDocument() {
        currentLocation = Location.DOCUMENT;
    }

    public void startElement(String uri, String name, String qName, Attributes atts) {
        if (sawHeader == false) {
            segmentDelimiter = atts.getValue("segmentDelimiter");
            elementDelimiter = atts.getValue("elementDelimiter");
            subelementDelimiter = atts.getValue("subelementDelimiter");
            
            if (segmentDelimiter == null) {
                segmentDelimiter = "~";
            }
            if (elementDelimiter == null) {
                elementDelimiter = "*";
            }
            if (subelementDelimiter == null) {
                subelementDelimiter = ":";
            }
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
                        output.append(elementDelimiter);
                    }

                    previousSegmentNameArray = currentNameArray;
                }
                
                output.append(elementDelimiter);
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
                        output.append(subelementDelimiter);
                    }

                    previousElementNameArray = currentNameArray;
                }
                
                if (lastInSubelement) {
                    output.append(subelementDelimiter);
                }

                currentLocation = Location.SUBELEMENT;
                lastInSubelement = true;
            }
        }
    }

    public void endElement(String uri, String name, String qName) {
        if (currentLocation.equals(Location.SEGMENT)) {
            output.append(segmentDelimiter);
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
