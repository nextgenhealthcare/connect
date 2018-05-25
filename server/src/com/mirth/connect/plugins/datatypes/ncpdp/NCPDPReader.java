/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.ncpdp;

import java.io.IOException;
import java.util.Stack;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

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
        int groupDelimeterIndex = message.indexOf(groupDelimeter);
        int segmentDelimeterIndex = message.indexOf(segmentDelimeter);
        int bodyIndex = 0;

        if ((groupDelimeterIndex == -1) || (segmentDelimeterIndex < groupDelimeterIndex)) {
            bodyIndex = segmentDelimeterIndex;
        } else {
            bodyIndex = groupDelimeterIndex;
        }

        String body = message.substring(bodyIndex, message.length());

        boolean hasMoreSegments = true;
        boolean inGroup = false;
        boolean firstTransaction = true;
        int groupCounter = 0;

        while (hasMoreSegments) {
            // get next segment or group
            groupDelimeterIndex = body.indexOf(groupDelimeter);
            segmentDelimeterIndex = body.indexOf(segmentDelimeter);

            if ((groupDelimeterIndex > -1) && (groupDelimeterIndex < segmentDelimeterIndex)) { // case: next part is a group
                // process last segment before group
                parseSegment(body.substring(0, groupDelimeterIndex), contentHandler);

                if (inGroup) {
                    contentHandler.endElement("", "TRANSACTION", "");
                }

                if (firstTransaction) {
                    firstTransaction = false;
                    contentHandler.startElement("", "TRANSACTIONS", "", null);
                }

                // process a group
                AttributesImpl attr = new AttributesImpl();
                attr.addAttribute("", "counter", "counter", "", Integer.toString(++groupCounter));
                contentHandler.startElement("", "TRANSACTION", "", attr);
                inGroup = true;
            } else if (groupDelimeterIndex == -1 && segmentDelimeterIndex == -1) { // case: last segment
                parseSegment(body, contentHandler);
                hasMoreSegments = false;
            } else { // case: next part is a segment
                String segment = body.substring(0, segmentDelimeterIndex);
                parseSegment(segment, contentHandler);
            }

            // remove processed segment from message body
            body = body.substring(segmentDelimeterIndex + segmentDelimeter.length());
        }

        // end group if we have started one
        if (inGroup) {
            contentHandler.endElement("", "TRANSACTION", "");
            contentHandler.endElement("", "TRANSACTIONS", "");
        }

        contentHandler.endElement("", header, "");
        contentHandler.endDocument();
    }

    private String parseHeader(String message, ContentHandler contentHandler) throws SAXException {
        String headerElementName = StringUtils.EMPTY;

        /*
         * The first segment is always the Transaction header so we will process it seperately.
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
        String segmentId = StringUtils.EMPTY;
        String subSegment = StringUtils.EMPTY;
        Stack<String> fieldStack = new Stack<String>();

        int fieldDelimeterIndex = segment.indexOf(fieldDelimeter);

        if (fieldDelimeterIndex == 0) {
            segment = segment.substring(fieldDelimeterIndex + fieldDelimeter.length());
            fieldDelimeterIndex = segment.indexOf(fieldDelimeter);
        }

        if (fieldDelimeterIndex == -1) {
            logger.warn("Empty segment with no field seperators. Make sure batch file processing is disabled.");
            hasMoreFields = false;
            segmentId = segment;
        } else {
            segmentId = segment.substring(0, fieldDelimeterIndex);
            subSegment = segment.substring(fieldDelimeterIndex + fieldDelimeter.length(), segment.length());
        }

        contentHandler.startElement("", NCPDPReference.getInstance().getSegment(segmentId, version), "", null);

        while (hasMoreFields) {
            fieldDelimeterIndex = subSegment.indexOf(fieldDelimeter);
            // not last field
            String field;

            if (fieldDelimeterIndex != -1) {
                field = subSegment.substring(0, subSegment.indexOf(fieldDelimeter));
                subSegment = subSegment.substring(fieldDelimeterIndex + fieldDelimeter.length());
            } else {
                field = subSegment;
                hasMoreFields = false;
            }

            String fieldId = field.substring(0, 2);
            String fieldDescription = NCPDPReference.getInstance().getDescription(fieldId, version);
            String fieldMessage = field.substring(2);

            if (inCount && !isRepeatingField(fieldDescription) && !fieldDescription.endsWith("Count")) {
                // if we are were in count field then end the element
                contentHandler.endElement("", fieldStack.pop(), "");

                if (fieldStack.size() == 0) {
                    inCount = false;
                }
            }

            if (fieldDescription.endsWith("Counter")) {
                if (inCounter) {
                    contentHandler.endElement("", fieldStack.pop(), "");
                }

                inCounter = true;
                AttributesImpl attr = new AttributesImpl();
                attr.addAttribute("", "counter", "counter", "", fieldMessage);
                contentHandler.startElement("", fieldDescription, "", attr);
                fieldStack.push(fieldDescription);
            } else if (fieldDescription.endsWith("Count")) {
                // count field, add complex element
                inCount = true;
                AttributesImpl attr = new AttributesImpl();
                attr.addAttribute("", fieldDescription, fieldDescription, "", fieldMessage);
                // start the repeating field element
                contentHandler.startElement("", fieldDescription, "", attr);
                fieldStack.push(fieldDescription);
            } else {
                contentHandler.startElement("", fieldDescription, "", null);
                contentHandler.characters(fieldMessage.toCharArray(), 0, fieldMessage.length());
                contentHandler.endElement("", fieldDescription, "");
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