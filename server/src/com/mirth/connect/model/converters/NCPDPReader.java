/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.mirth.connect.model.ncpdp.NCPDPReference;

public class NCPDPReader extends SAXParser {
    private Logger logger = Logger.getLogger(this.getClass());

    private String segmentDelim;
    private String groupDelim;
    private String fieldDelim;

    public NCPDPReader(String segmentDelim, String groupDelim, String fieldDelim) {
        this.segmentDelim = segmentDelim;
        this.groupDelim = groupDelim;
        this.fieldDelim = fieldDelim;
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

        // Process header
        documentHead = readHeader(message, contentHandler);
        // Process message body
        int indexOfGroup = message.indexOf(groupDelim);
        int indexOfSegment = message.indexOf(segmentDelim);
        int indexMessageBody;
        if (indexOfGroup == -1 || indexOfSegment < indexOfGroup) {
            indexMessageBody = indexOfSegment;
        } else {
            indexMessageBody = indexOfGroup;
        }
        String messageBody = message.substring(indexMessageBody, message.length());

        boolean hasMoreSegments = true;
        boolean inGroup = false;
        boolean firstTrans = true;
        int groupIterator = 0;
        while (hasMoreSegments) {
            // get next segment or group
            indexOfGroup = messageBody.indexOf(groupDelim);
            indexOfSegment = messageBody.indexOf(segmentDelim);
            // Case: Next part is a group
            if (indexOfGroup > -1 && indexOfGroup < indexOfSegment) {
                // process last segment before group
                String segment = messageBody.substring(0, indexOfGroup);
                readSegment(segment, contentHandler);
                if (inGroup) {
                    contentHandler.endElement("", "TRANSACTION", "");
                }
                if (firstTrans) {
                    firstTrans = false;
                    contentHandler.startElement("", "TRANSACTIONS", "", null);
                }
                groupIterator++;
                // process a group
                AttributesImpl attr = new AttributesImpl();
                attr.addAttribute("", "counter", "counter", "", Integer.toString(groupIterator));
                contentHandler.startElement("", "TRANSACTION", "", attr);
                inGroup = true;

            }
            // Case: last segment
            else if (indexOfGroup == -1 && indexOfSegment == -1) {
                // process last segment
                readSegment(messageBody, contentHandler);
                hasMoreSegments = false;
            }
            // Case Next part is a segment
            else {
                // process a segment
                String segment = messageBody.substring(0, indexOfSegment);
                readSegment(segment, contentHandler);
            }
            // Remove processed segment from messageBody
            messageBody = messageBody.substring(indexOfSegment + segmentDelim.length());
        }
        // End group if we have started one
        if (inGroup) {
            contentHandler.endElement("", "TRANSACTION", "");
            contentHandler.endElement("", "TRANSACTIONS", "");
        }
        contentHandler.endElement("", documentHead, "");
        contentHandler.endDocument();
    }

    private String readHeader(String message, ContentHandler contentHandler) throws SAXException {
        int position;
        String docHead = "";
        // First Segment is always Transaction header. Process seperately
        if (message.indexOf(segmentDelim) != -1) {
            position = message.indexOf(segmentDelim);
            String header = message.substring(0, position);
            String docAttr = "";// " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"ncpdp.xsd\"";
            if (header.length() > 40) {
                String transaction = NCPDPReference.getInstance().getTransactionName(header.substring(8, 10));
                docHead = "NCPDP_51_" + transaction + "_Request";
                contentHandler.startElement("", docHead + docAttr, "", null);
                contentHandler.startElement("", "TransactionHeaderRequest", "", null);
                contentHandler.startElement("", "BinNumber", "", null);
                contentHandler.characters(header.toCharArray(), 0, 6);
                contentHandler.endElement("", "BinNumber", "");
                contentHandler.startElement("", "VersionReleaseNumber", "", null);
                contentHandler.characters(header.toCharArray(), 6, 2);
                contentHandler.endElement("", "VersionReleaseNumber", "");
                contentHandler.startElement("", "TransactionCode", "", null);
                contentHandler.characters(header.toCharArray(), 8, 2);
                contentHandler.endElement("", "TransactionCode", "");
                contentHandler.startElement("", "ProcessorControlNumber", "", null);
                contentHandler.characters(header.toCharArray(), 10, 10);
                contentHandler.endElement("", "ProcessorControlNumber", "");
                contentHandler.startElement("", "TransactionCount", "", null);
                contentHandler.characters(header.toCharArray(), 20, 1);
                contentHandler.endElement("", "TransactionCount", "");
                contentHandler.startElement("", "ServiceProviderIdQualifier", "", null);
                contentHandler.characters(header.toCharArray(), 21, 2);
                contentHandler.endElement("", "ServiceProviderIdQualifier", "");
                contentHandler.startElement("", "ServiceProviderId", "", null);
                contentHandler.characters(header.toCharArray(), 23, 15);
                contentHandler.endElement("", "ServiceProviderId", "");
                contentHandler.startElement("", "DateOfService", "", null);
                contentHandler.characters(header.toCharArray(), 38, 8);
                contentHandler.endElement("", "DateOfService", "");
                contentHandler.startElement("", "SoftwareVendorCertificationId", "", null);
                contentHandler.characters(header.toCharArray(), 46, 10);
                contentHandler.endElement("", "SoftwareVendorCertificationId", "");
                contentHandler.endElement("", "TransactionHeaderRequest", "");
            } else {
                String transaction = NCPDPReference.getInstance().getTransactionName(header.substring(2, 4));
                docHead = "NCPDP_51_" + transaction + "_Response";
                contentHandler.startElement("", docHead + docAttr, "", null);
                contentHandler.startElement("", "TransactionHeaderResponse", "", null);
                contentHandler.startElement("", "VersionReleaseNumber", "", null);
                contentHandler.characters(header.toCharArray(), 0, 2);
                contentHandler.endElement("", "VersionReleaseNumber", "");
                contentHandler.startElement("", "TransactionCode", "", null);
                contentHandler.characters(header.toCharArray(), 2, 2);
                contentHandler.endElement("", "TransactionCode", "");
                contentHandler.startElement("", "TransactionCount", "", null);
                contentHandler.characters(header.toCharArray(), 4, 1);
                contentHandler.endElement("", "TransactionCount", "");
                contentHandler.startElement("", "HeaderResponseStatus", "", null);
                contentHandler.characters(header.toCharArray(), 5, 1);
                contentHandler.endElement("", "HeaderResponseStatus", "");
                contentHandler.startElement("", "ServiceProviderIdQualifier", "", null);
                contentHandler.characters(header.toCharArray(), 6, 2);
                contentHandler.endElement("", "ServiceProviderIdQualifier", "");
                contentHandler.startElement("", "ServiceProviderId", "", null);
                contentHandler.characters(header.toCharArray(), 8, 15);
                contentHandler.endElement("", "ServiceProviderId", "");
                contentHandler.startElement("", "DateOfService", "", null);
                contentHandler.characters(header.toCharArray(), 23, 8);
                contentHandler.endElement("", "DateOfService", "");
                contentHandler.endElement("", "TransactionHeaderResponse", "");
            }
        }
        return docHead;
    }

    private void readSegment(String segment, ContentHandler contentHandler) throws SAXException {
        if (segment == null || segment.equals("")) {
            return;
        }
        boolean inCounter = false;
        boolean inCount = false;
        boolean hasMoreFields = true;
        String segmentId = "";
        Stack<String> a = new Stack<String>();
        String subSegment = "";

        int indexOfField = segment.indexOf(fieldDelim);
        if (indexOfField == 0) {
            segment = segment.substring(indexOfField + fieldDelim.length());
            indexOfField = segment.indexOf(fieldDelim);
        }
        if (indexOfField == -1) {
            logger.debug("Empty Segment, No field Seperators. Make sure batch file processing is disabled");
            hasMoreFields = false;
            segmentId = segment;
        } else {
            segmentId = segment.substring(0, indexOfField);
            subSegment = segment.substring(indexOfField + fieldDelim.length(), segment.length());
        }
        //
        contentHandler.startElement("", NCPDPReference.getInstance().getSegment(segmentId), "", null);
        while (hasMoreFields) {
            indexOfField = subSegment.indexOf(fieldDelim);
            // not last field
            String field;
            if (indexOfField != -1) {
                field = subSegment.substring(0, subSegment.indexOf(fieldDelim));
                subSegment = subSegment.substring(indexOfField + fieldDelim.length());
            } else {
                field = subSegment;
                hasMoreFields = false;
            }
            String fieldId = field.substring(0, 2);
            String fieldDesc = NCPDPReference.getInstance().getDescription(fieldId);
            String fieldMessage = field.substring(2);
            if (inCount && !isRepeatingField(fieldDesc) && !isCountField(fieldDesc)) {
                // if we are were in count field, end element
                contentHandler.endElement("", a.pop(), "");
                if (a.size() == 0) {
                    inCount = false;
                }
            }
            if (isCounterField(fieldDesc)) {
                if (inCounter) {
                    contentHandler.endElement("", a.pop(), "");
                }
                inCounter = true;
                AttributesImpl attr = new AttributesImpl();
                attr.addAttribute("", "counter", "counter", "", fieldMessage);
                contentHandler.startElement("", fieldDesc, "", attr);
                a.push(fieldDesc);
            } else if (isCountField(fieldDesc)) {
                // Count field. Add complex element
                inCount = true;
                AttributesImpl attr = new AttributesImpl();
                attr.addAttribute("", fieldDesc, fieldDesc, "", fieldMessage);
                // Start repeating field element
                contentHandler.startElement("", fieldDesc, "", attr);
                a.push(fieldDesc);
            } else {
                contentHandler.startElement("", fieldDesc, "", null);
                contentHandler.characters(fieldMessage.toCharArray(), 0, fieldMessage.length());
                contentHandler.endElement("", fieldDesc, "");
            }
        }
        while (a.size() > 0) {
            // close remaining count and counters
            contentHandler.endElement("", a.pop(), "");
        }
        contentHandler.endElement("", NCPDPReference.getInstance().getSegment(segmentId), "");
    }

    private boolean isCounterField(String fieldDesc) {
        if (fieldDesc.endsWith("Counter")) {
            return true;
        }
        return false;
    }

    private boolean isCountField(String fieldDesc) {
        if (fieldDesc.endsWith("Count")) {
            return true;
        }
        return false;
    }

    private boolean isRepeatingField(String fieldDesc) {
        return NCPDPReference.getInstance().isRepeatingField(fieldDesc);
    }
}