/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters;

import java.io.IOException;
import java.util.Stack;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.mirth.connect.model.ncpdp.NCPDPReference;

public class NCPDPReader extends SAXParser {
    private Logger logger = Logger.getLogger(this.getClass());

    private String segmentDelimeter;
    private String groupDelimeter;
    private String fieldDelimeter;
    private String version;

    public NCPDPReader(String segmentDelimeter, String groupDelimeter, String fieldDelimeter) {
        this.segmentDelimeter = segmentDelimeter;
        this.groupDelimeter = groupDelimeter;
        this.fieldDelimeter = fieldDelimeter;
    }

    @Override
    public void parse(InputSource input) throws SAXException, IOException {
        // convert the InputSource to a String and trim the whitespace
        String message = IOUtils.toString(input.getCharacterStream()).trim();

        ContentHandler contentHandler = getContentHandler();
        contentHandler.startDocument();

        if ((message == null) || (message.length() < 3)) {
            throw new SAXException("Unable to parse, message is null or too short: " + message);
        }

        // process header
        String header = parseHeader(message, contentHandler);

        // process body
        int indexOfGroup = message.indexOf(groupDelimeter);
        int indexOfSegment = message.indexOf(segmentDelimeter);
        int indexMessageBody = 0;

        if ((indexOfGroup == -1) || (indexOfSegment < indexOfGroup)) {
            indexMessageBody = indexOfSegment;
        } else {
            indexMessageBody = indexOfGroup;
        }

        String body = message.substring(indexMessageBody, message.length());

        boolean hasMoreSegments = true;
        boolean inGroup = false;
        boolean firstTrans = true;
        int groupCounter = 0;
        
        while (hasMoreSegments) {
            // get next segment or group
            indexOfGroup = body.indexOf(groupDelimeter);
            indexOfSegment = body.indexOf(segmentDelimeter);
            
            // Case: Next part is a group
            if (indexOfGroup > -1 && indexOfGroup < indexOfSegment) {
                // process last segment before group
                String segment = body.substring(0, indexOfGroup);
                parseSegment(segment, contentHandler);
                
                if (inGroup) {
                    contentHandler.endElement("", "TRANSACTION", "");
                }
                
                if (firstTrans) {
                    firstTrans = false;
                    contentHandler.startElement("", "TRANSACTIONS", "", null);
                }
                
                groupCounter++;
                // process a group
                AttributesImpl attr = new AttributesImpl();
                attr.addAttribute("", "counter", "counter", "", Integer.toString(groupCounter));
                contentHandler.startElement("", "TRANSACTION", "", attr);
                inGroup = true;
            }
            // Case: last segment
            else if (indexOfGroup == -1 && indexOfSegment == -1) {
                // process last segment
                parseSegment(body, contentHandler);
                hasMoreSegments = false;
            }
            // Case Next part is a segment
            else {
                // process a segment
                String segment = body.substring(0, indexOfSegment);
                parseSegment(segment, contentHandler);
            }
            // Remove processed segment from messageBody
            body = body.substring(indexOfSegment + segmentDelimeter.length());
        }
        
        // End group if we have started one
        if (inGroup) {
            contentHandler.endElement("", "TRANSACTION", "");
            contentHandler.endElement("", "TRANSACTIONS", "");
        }
        
        contentHandler.endElement("", header, "");
        contentHandler.endDocument();
    }

    private String parseHeader(String message, ContentHandler contentHandler) throws SAXException {
        String headerElementName = null;
        
        /*
         * The first segment is always the Transaction header so we will process
         * it seperately.
         */
        if (message.indexOf(segmentDelimeter) != -1) {
            String header = message.substring(0, message.indexOf(segmentDelimeter));
            
            // handle a request (requests have a longer header than responses)
            if (header.length() > 40) {
                String transactionName = NCPDPReference.getInstance().getTransactionName(header.substring(8, 10));
                version = header.substring(6, 8);
                headerElementName = "NCPDP_" + version + "_" + transactionName + "_Request";
                
                contentHandler.startElement("", headerElementName, "", null);
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
            } else { // handle a response
                String transaction = NCPDPReference.getInstance().getTransactionName(header.substring(2, 4));
                version = header.substring(0, 2);
                headerElementName = "NCPDP_" + version + "_" + transaction + "_Response";
                
                contentHandler.startElement("", headerElementName, "", null);
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

        return headerElementName;
    }

    private void parseSegment(String segment, ContentHandler contentHandler) throws SAXException {
        if (StringUtils.isBlank(segment)) {
            return;
        }

        boolean inCounter = false;
        boolean inCount = false;
        boolean hasMoreFields = true;
        String segmentId = "";
        String subSegment = "";
        Stack<String> fieldStack = new Stack<String>();

        int fieldIndex = segment.indexOf(fieldDelimeter);

        if (fieldIndex == 0) {
            segment = segment.substring(fieldIndex + fieldDelimeter.length());
            fieldIndex = segment.indexOf(fieldDelimeter);
        }

        if (fieldIndex == -1) {
            logger.warn("Empty segment with no field seperators. Make sure batch file processing is disabled.");
            hasMoreFields = false;
            segmentId = segment;
        } else {
            segmentId = segment.substring(0, fieldIndex);
            subSegment = segment.substring(fieldIndex + fieldDelimeter.length(), segment.length());
        }

        contentHandler.startElement("", NCPDPReference.getInstance().getSegment(segmentId, version), "", null);

        while (hasMoreFields) {
            fieldIndex = subSegment.indexOf(fieldDelimeter);
            // not last field
            String field;
            
            if (fieldIndex != -1) {
                field = subSegment.substring(0, subSegment.indexOf(fieldDelimeter));
                subSegment = subSegment.substring(fieldIndex + fieldDelimeter.length());
            } else {
                field = subSegment;
                hasMoreFields = false;
            }
            
            String fieldId = field.substring(0, 2);
            String fieldDesc = NCPDPReference.getInstance().getDescription(fieldId, version);
            String fieldMessage = field.substring(2);
            
            if (inCount && !isRepeatingField(fieldDesc) && !fieldDesc.endsWith("Count")) {
                // if we are were in count field, end element
                contentHandler.endElement("", fieldStack.pop(), "");
                if (fieldStack.size() == 0) {
                    inCount = false;
                }
            }
            
            if (fieldDesc.endsWith("Counter")) {
                if (inCounter) {
                    contentHandler.endElement("", fieldStack.pop(), "");
                }
                
                inCounter = true;
                AttributesImpl attr = new AttributesImpl();
                attr.addAttribute("", "counter", "counter", "", fieldMessage);
                contentHandler.startElement("", fieldDesc, "", attr);
                fieldStack.push(fieldDesc);
            } else if (fieldDesc.endsWith("Count")) {
                // Count field. Add complex element
                inCount = true;
                AttributesImpl attr = new AttributesImpl();
                attr.addAttribute("", fieldDesc, fieldDesc, "", fieldMessage);
                // Start repeating field element
                contentHandler.startElement("", fieldDesc, "", attr);
                fieldStack.push(fieldDesc);
            } else {
                contentHandler.startElement("", fieldDesc, "", null);
                contentHandler.characters(fieldMessage.toCharArray(), 0, fieldMessage.length());
                contentHandler.endElement("", fieldDesc, "");
            }
        }

        while (fieldStack.size() > 0) {
            // close remaining count and counters
            contentHandler.endElement("", fieldStack.pop(), "");
        }

        contentHandler.endElement("", NCPDPReference.getInstance().getSegment(segmentId, version), "");
    }

    private boolean isRepeatingField(String fieldDescription) {
        return NCPDPReference.getInstance().isRepeatingField(fieldDescription, version);
    }
}