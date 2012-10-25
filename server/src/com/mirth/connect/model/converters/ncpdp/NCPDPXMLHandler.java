/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters.ncpdp;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import com.mirth.connect.model.ncpdp.NCPDPReference;

public class NCPDPXMLHandler extends DefaultHandler {
    private enum Location {
        DOCUMENT, SEGMENT, GROUP, FIELD, REPETITION
    }

    private String segmentDelimeter;
    private String groupDelimeter;
    private String fieldDelimeter;
    private String version;

    private Location currentLocation = Location.DOCUMENT;
    private boolean sawHeader = false;
    private boolean inTransactionHeader = false;
    private boolean inGroup = false;
    private StringBuilder output = new StringBuilder();
    private NCPDPReference reference = NCPDPReference.getInstance();

    public NCPDPXMLHandler(String segmentDelimeter, String groupDelimeter, String fieldDelimeter, String version) {
        super();
        this.segmentDelimeter = segmentDelimeter;
        this.groupDelimeter = groupDelimeter;
        this.fieldDelimeter = fieldDelimeter;
        this.version = version;
    }

    @Override
    public void startDocument() {
        currentLocation = Location.DOCUMENT;
        inGroup = false;
        inTransactionHeader = false;
    }

    @Override
    public void endDocument() {
        currentLocation = Location.DOCUMENT;
        inGroup = false;
        inTransactionHeader = false;
    }

    @Override
    public void startElement(String uri, String name, String qName, Attributes atts) {
        if (!sawHeader) {
            sawHeader = true;
        } else {
            if (!inTransactionHeader && name.startsWith("TransactionHeader")) {
                inTransactionHeader = true;
            }

            if (currentLocation.equals(Location.DOCUMENT)) {
                // dont output tag names or delimitors when in trans header
                if (name.equals("TRANSACTIONS")) {
                    currentLocation = Location.DOCUMENT;
                } else if (name.equals("TRANSACTION")) {
                    output.append(groupDelimeter);
                    currentLocation = Location.GROUP;
                } else {
                    currentLocation = Location.SEGMENT;
                    
                    if (!inTransactionHeader) {
                        output.append(segmentDelimeter);
                        output.append(fieldDelimeter);
                        output.append(reference.getSegmentIdByName(name, version));
                        
                        if (atts != null && atts.getLength() > 0) {
                            for (int i = 0; i < atts.getLength(); i++) {
                                output.append(fieldDelimeter);
                                String localName = atts.getLocalName(i);
                                String value = atts.getValue(i);
                                output.append(reference.getCodeByName(localName, version));
                                output.append(value);
                            }
                        }
                    }
                }
            } else if (currentLocation.equals(Location.GROUP)) {
                // output the segment delimitor and segment name
                output.append(segmentDelimeter);
                output.append(fieldDelimeter);
                output.append(reference.getSegmentIdByName(name, version));
                
                if (atts != null && atts.getLength() > 0) {
                    for (int i = 0; i < atts.getLength(); i++) {
                        output.append(fieldDelimeter);
                        String localName = atts.getLocalName(i);
                        String value = atts.getValue(i);
                        output.append(reference.getCodeByName(localName, version));
                        output.append(value);
                    }
                }
                
                inGroup = true;
                currentLocation = Location.SEGMENT;
            } else if (currentLocation.equals(Location.SEGMENT)) {
                // dont output tag names or delimitors when in trans header
                currentLocation = Location.FIELD;
                
                if (!inTransactionHeader) {
                    output.append(fieldDelimeter);
                    if (isCounterOrCountField(name)) {
                        output.append(reference.getCodeByName(name, version));
                        output.append(atts.getValue(0));
                        currentLocation = Location.SEGMENT;
                    } else {
                        output.append(reference.getCodeByName(name, version));
                        
                        if (atts != null && atts.getLength() > 0) {
                            for (int i = 0; i < atts.getLength(); i++) {
                                output.append(fieldDelimeter);
                                String localName = atts.getLocalName(i);
                                String value = atts.getValue(i);
                                output.append(reference.getCodeByName(localName, version));
                                output.append(value);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void endElement(String uri, String name, String qName) {
        if (currentLocation.equals(Location.SEGMENT)) {
            if (isCounterOrCountField(name)) {
                currentLocation = Location.SEGMENT;
            } else if (inGroup) {
                currentLocation = Location.GROUP;
            } else {
                currentLocation = Location.DOCUMENT;
            }

            inTransactionHeader = false;
        } else if (currentLocation.equals(Location.GROUP)) {
            currentLocation = Location.DOCUMENT;
            inGroup = false;
        } else if (currentLocation.equals(Location.FIELD)) {
            currentLocation = Location.SEGMENT;
        } else if (currentLocation.equals(Location.DOCUMENT)) {
            // do nothing if we are closing the document
        }
    }

    @Override
    public void characters(char ch[], int start, int length) {
        output.append(ch, start, length);
    }

    public StringBuilder getOutput() {
        return output;
    }

    private boolean isCounterOrCountField(String fieldDescription) {
        return (fieldDescription.endsWith("Counter") || fieldDescription.endsWith("Count"));
    }
}
