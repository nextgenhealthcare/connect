/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class EDIXMLHandler extends DefaultHandler {
    private String segmentDelim;
    private String elementDelim;
    private String subelementDelim;

    private enum Location {
        DOCUMENT, SEGMENT, ELEMENT, SUBELEMENT
    };

    private Location currentLocation = Location.DOCUMENT;
    private boolean sawHeader = false;
    private boolean lastinSubelement = false;
    private StringBuilder output = new StringBuilder();

    public EDIXMLHandler(String segmentDelim, String elementDelim, String subelementDelim) {
        super();
        this.segmentDelim = segmentDelim;
        this.elementDelim = elementDelim;
        this.subelementDelim = subelementDelim;
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
                lastinSubelement = false;
            } else if (currentLocation.equals(Location.SEGMENT)) {
                output.append(elementDelim);
                currentLocation = Location.ELEMENT;
                lastinSubelement = false;
            } else if (currentLocation.equals(Location.ELEMENT)) {
                if (lastinSubelement) {
                    output.append(subelementDelim);
                }
                currentLocation = Location.SUBELEMENT;
                lastinSubelement = true;
            }
        }
    }

    public void endElement(String uri, String name, String qName) {
        if (currentLocation.equals(Location.SEGMENT)) {
            output.append(segmentDelim);
            currentLocation = Location.DOCUMENT;
        } else if (currentLocation.equals(Location.ELEMENT)) {

            currentLocation = Location.SEGMENT;
        } else if (currentLocation.equals(Location.SUBELEMENT)) {

            currentLocation = Location.ELEMENT;
        } else if (currentLocation.equals(Location.DOCUMENT)) {

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
