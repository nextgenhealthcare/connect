/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.edi;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class EDIReader extends SAXParser {
    private Logger logger = Logger.getLogger(this.getClass());

    private String segmentDelim;

    private String elementDelim;

    private String subelementDelim;

    public EDIReader(String segmentDelim, String elementDelim, String subelementDelim) {
        this.segmentDelim = segmentDelim;
        this.elementDelim = elementDelim;
        this.subelementDelim = subelementDelim;
        return;
    }

    public void parse(InputSource input) throws SAXException, IOException {
        // Read the data from the InputSource
        BufferedReader in = new BufferedReader(input.getCharacterStream());
        String nextLine = "";
        StringBuffer sb = new StringBuffer();
        while ((nextLine = in.readLine()) != null) {
            sb.append(nextLine);
            sb.append("\n");
        }
        String message = sb.toString();
        message = message.trim();
        // fire SAX events
        String documentHead = "";
        ContentHandler contentHandler = getContentHandler();
        contentHandler.startDocument();
        // First tokenize the segments
        if (message == null || message.length() < 3) {
            logger.error("Unable to parse, message is null or too short: " + message);
            throw new SAXException("Unable to parse, message is null or too short: " + message);
        }

        // Tokenize the segments first
        StringTokenizer segmentTokenizer = new StringTokenizer(message, segmentDelim);
        int segmentCounter = 0;

        while (segmentTokenizer.hasMoreTokens()) {
            String segment = segmentTokenizer.nextToken();
            // loop through each segment and pull out the elements
            StringTokenizer elementTokenizer = new StringTokenizer(segment, elementDelim, true);

            if (elementTokenizer.hasMoreTokens()) {
                // Our XML element is named after the first element
                String segmentID = elementTokenizer.nextToken().trim();
                // check if we have EDI or X12
                if (segmentCounter == 0) {
                    if (segmentID.equals("ISA")) {
                        documentHead = "X12Transaction";
                    } else {
                        documentHead = "EDIMessage";
                    }
                    contentHandler.startElement("", documentHead, "", null);
                }
                contentHandler.startElement("", segmentID, "", null);

                int fieldID = 0;
                String field = "00";
                int subelementID = 1;
                boolean lastsegElement = false;
                boolean lastsegSubelement = true;
                while (elementTokenizer.hasMoreTokens()) {
                    // Codes should be in the form ISA.01, etc
                    field = fieldID < 10 ? "0" + fieldID : "" + fieldID;
                    // Go through each element and add as new child under
                    // the segment element
                    String element = elementTokenizer.nextToken();
                    // System.out.println("EL:" + element);
                    // The naming is SEG.<field number>
                    if (element.equals(elementDelim)) {
                        if (lastsegElement) {
                            contentHandler.startElement("", segmentID + "." + field, "", null);
                            contentHandler.endElement("", segmentID + "." + field, "");
                        }
                        fieldID++;
                        lastsegElement = true;
                    } else {
                        lastsegElement = false;

                        if (element.indexOf(subelementDelim) > -1) {
                            contentHandler.startElement("", segmentID + "." + field, "", null);
                            // check if we have sub-elements, if so add them
                            StringTokenizer subelementTokenizer = new StringTokenizer(element, subelementDelim, true);
                            subelementID = 1;
                            lastsegSubelement = true;
                            while (subelementTokenizer.hasMoreTokens()) {
                                String subelement = subelementTokenizer.nextToken();
                                if (subelement.equals(subelementDelim)) {
                                    String subelementName = segmentID + "." + field + "." + subelementID;
                                    if (lastsegSubelement) {
                                        contentHandler.startElement("", subelementName, "", null);
                                        contentHandler.characters("".toCharArray(), 0, 0);
                                        contentHandler.endElement("", subelementName, "");
                                    }
                                    subelementID++;
                                    lastsegSubelement = true;
                                } else {

                                    String subelementName = segmentID + "." + field + "." + subelementID;
                                    lastsegSubelement = false;
                                    // The naming is SEG.<field
                                    // number>.<element number>
                                    contentHandler.startElement("", subelementName, "", null);
                                    contentHandler.characters(subelement.toCharArray(), 0, subelement.length());
                                    contentHandler.endElement("", subelementName, "");

                                }
                            }
                            String subelementName = segmentID + "." + (field) + "." + subelementID;
                            if (lastsegSubelement) {
                                contentHandler.startElement("", subelementName, "", null);
                                contentHandler.characters("".toCharArray(), 0, 0);
                                contentHandler.endElement("", subelementName, "");
                            }
                            contentHandler.endElement("", segmentID + "." + (field), null);
                        } else {
                            contentHandler.startElement("", segmentID + "." + field, "", null);
                            contentHandler.startElement("", segmentID + "." + field + ".1", "", null);

                            // Set the text contents to the value
                            contentHandler.characters(element.toCharArray(), 0, element.length());
                            contentHandler.endElement("", segmentID + "." + (field) + ".1", null);
                            contentHandler.endElement("", segmentID + "." + (field), null);

                        }
                    }

                }
                if (lastsegElement) {
                    // Set the field id here so we don't get dupe fields like
                    // SE.01 and SE.01 when we have SE**~
                    field = fieldID < 10 ? "0" + fieldID : "" + fieldID;
                    contentHandler.startElement("", segmentID + "." + field, "", null);
                    contentHandler.endElement("", segmentID + "." + field, "");
                }
                contentHandler.endElement("", segmentID, "");

            } else {
                throw new SAXException("Could not find elements in segment: " + segment);
            }
            segmentCounter++;
        }
        contentHandler.endElement("", documentHead, "");
        contentHandler.endDocument();
    }

}
