/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.mllp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/*
 * The purpose of this class is to extract the data from an XML/HL7 message 
 * without parsing the entire message
 * 
 */

public class HL7v2XMLQuickParser {

    private Logger logger = Logger.getLogger(this.getClass());

    private final String mshHedader = "MSH|^~\\&|";
    private final String fieldSeparator = "|";
    private final String componentSeparator = "^";

    private final String headerSegmentName = "MSH";
    private final Pattern headerSegmentNamePrecompiled = Pattern.compile("MSH\\s", Pattern.MULTILINE);
    private final String senderAppField = "MSH.3";
    private final Pattern senderAppFieldPrecompiled = Pattern.compile("MSH\\.3\\s", Pattern.MULTILINE);
    private final String senderPlaceField = "MSH.4";
    private final Pattern senderPlaceFieldPrecompiled = Pattern.compile("MSH\\.4\\s");
    private final String receiverAppField = "MSH.5";
    private final Pattern receiverAppFieldPrecompiled = Pattern.compile("MSH\\.5\\s");
    private final String receiverPlaceField = "MSH.6";
    private final Pattern receiverPlaceFieldPrecompiled = Pattern.compile("MSH\\.6\\s");
    private final String msg9Field = "MSH.9";
    private final Pattern msg9FieldPrecompiled = Pattern.compile("MSH\\.9\\s");
    private final String msgIdField = "MSH.10";
    private final Pattern msgIdPrecompiled = Pattern.compile("MSH\\.10\\s");
    private final String msgh11Field = "MSH.11";
    private final Pattern msgh11FieldPrecompiled = Pattern.compile("MSH\\.11\\s");
    private final String msgVersionField = "MSH.12";
    private final Pattern msgVersionFieldPrecompiled = Pattern.compile("MSH\\.12\\s");
    private final String procIdData = "PT.1";
    private final Pattern procIdDataPrecompiled = Pattern.compile("PT\\.1\\s");
    private final String procIdModeData = "PT.2";
    private final Pattern procIdModeDataPrecompiled = Pattern.compile("PT\\.2\\s");
    private final String msg2Data = "MSG.2";
    private final Pattern msg2DataPrecompiled = Pattern.compile("MSG\\.2\\s");
    private final String cm2Data = "CM.2";
    private final Pattern cm2DataPrecompiled = Pattern.compile("CM\\.2\\s");
    private final Pattern whiteSpacePattern = Pattern.compile("\\s");

    private static HL7v2XMLQuickParser instance = null;

    private HL7v2XMLQuickParser() {
        // ...
    }

    /**
     * @return The unique instance of this class.
     */
    public static HL7v2XMLQuickParser getInstance() {
        if (instance == null) {
            instance = new HL7v2XMLQuickParser();
        }
        return instance;
    }

    /*
     * Create an ER7 header with the data provider.
     */
    private String createER7MSH(String senderApp, String senderPlace, String receiverApp, String receiverPlace, String timest, String event, String processingType, String processingMode, String msgId, String version) {

        // MSH|^~\&|senderApp|senderPlace|receiverApp|receiverPlace|timest||msgType^event^msgStructure|msgId|processingId|version

        String msh = mshHedader;
        msh += senderApp + fieldSeparator;
        msh += senderPlace + fieldSeparator;
        msh += receiverApp + fieldSeparator;
        msh += receiverPlace + fieldSeparator;
        msh += timest + fieldSeparator + fieldSeparator;
        msh += "ACK" + componentSeparator + event + componentSeparator + "ACK" + fieldSeparator;
        msh += msgId + fieldSeparator;
        msh += processingType;
        if (processingMode.length() > 0) {
            msh += fieldSeparator + processingMode;
        }
        msh += fieldSeparator;
        msh += version + fieldSeparator;

        return msh;
    }

    /*
     * This method gets the content of an XML element. For example, with MSH
     * searches for <MSH>(...)</MSH>, first check for
     * <[...:]MSH>(...)</[...:]MSH> Then for <[...:]MSH (...)>(...)</[...:]MSH>
     */
    private String getElementContent(String xmlInput, String elementName, Pattern precompiledPattern) {
        String msgHeader = "";
        // First test with <[...:]MSH>(...)</[...:]MSH>.
        int mshStartPos = xmlInput.indexOf(elementName + ">");
        if (mshStartPos < 0)
            return "";
        msgHeader = xmlInput.substring(mshStartPos + elementName.length() + 1);
        int mshEndPos = msgHeader.indexOf(elementName + ">");
        if (mshEndPos < 0) {
            // Now, test with <[...:]MSH (...)>(...)</[...:]MSH>.
            mshEndPos = mshStartPos;
            msgHeader = xmlInput.substring(0, mshEndPos);
            Matcher m = precompiledPattern.matcher(msgHeader);
            if (!m.find())
                return "";
            mshStartPos = m.end();
            if (mshStartPos < 0)
                return "";
            msgHeader = msgHeader.substring(mshStartPos);
            mshStartPos = msgHeader.indexOf(">");
            if (mshStartPos < 0)
                return "";
            msgHeader = msgHeader.substring(mshStartPos + 1);
            mshEndPos = msgHeader.length() - 1;
        }
        while ((mshEndPos > 1) && (!msgHeader.substring(mshEndPos - 1, mshEndPos).equals("<")))
            mshEndPos--;
        if (mshEndPos <= 1)
            return "";
        msgHeader = msgHeader.substring(0, mshEndPos - 1);
        return msgHeader;
    }

    private String getFirstChildContent(String xmlInput, int startPos) {
        // Search the content of the first child
        if (startPos < 0)
            startPos = xmlInput.indexOf("<");
        if (startPos != 0) {
            if (startPos < 0)
                return xmlInput;
            String content = xmlInput.substring(0, startPos);
            if (content.trim().length() > 0)
                return content;
        }
        int endPos = xmlInput.indexOf(">");
        if (endPos <= 0)
            return "";
        if (xmlInput.substring(endPos - 1, endPos + 1).equals("/>"))
            return "";
        String elementName = xmlInput.substring(startPos + 1, endPos);
        Matcher m = whiteSpacePattern.matcher(elementName);
        if (m.find()) {
            elementName = elementName.substring(0, m.start());
        }
        // Now, change the start position to the end of the element name
        startPos = endPos + 1;

        endPos = xmlInput.lastIndexOf("</" + elementName + ">");
        if (endPos < 0) {
            logger.error("Error parsing XML as an HL7 document: can't find " + "</" + elementName + "> in the string " + xmlInput);
            return "";
        }
        xmlInput = xmlInput.substring(startPos, endPos);
        startPos = xmlInput.indexOf("<");
        if (startPos < 0)
            return xmlInput;
        return getFirstChildContent(xmlInput, startPos);
    }

    public HL7v2HeaderElements processXMLString(String xmlInput) {
        HL7v2HeaderElements msh = new HL7v2XMLQuickParser.HL7v2HeaderElements();
        try {
            String mshHeader = getElementContent(xmlInput, headerSegmentName, headerSegmentNamePrecompiled);

            msh.setSendingApplication(getFirstChildContent(getElementContent(mshHeader, senderAppField, senderAppFieldPrecompiled), -1).trim());
            msh.setSendingFacility(getFirstChildContent(getElementContent(mshHeader, senderPlaceField, senderPlaceFieldPrecompiled), -1).trim());
            msh.setReceivingApplication(getFirstChildContent(getElementContent(mshHeader, receiverAppField, receiverAppFieldPrecompiled), -1).trim());
            msh.setReceivingFacility(getFirstChildContent(getElementContent(mshHeader, receiverPlaceField, receiverPlaceFieldPrecompiled), -1).trim());
            msh.setOriginalId(getFirstChildContent(getElementContent(mshHeader, msgIdField, msgIdPrecompiled), -1).trim());
            msh.setVersion(getFirstChildContent(getElementContent(mshHeader, msgVersionField, msgVersionFieldPrecompiled), -1).trim());

            String msh9 = getElementContent(mshHeader, msg9Field, msg9FieldPrecompiled).trim();
            if (msh9.indexOf(msg2Data) > 0)
                msh.setOriginalEvent(getElementContent(msh9, msg2Data, msg2DataPrecompiled).trim()); // Ver
                                                                                                     // 2.5
            else if (msh9.indexOf(cm2Data) > 0)
                msh.setOriginalEvent(getElementContent(msh9, cm2Data, cm2DataPrecompiled).trim()); // Ver
                                                                                                   // 2.4

            String msh11 = getElementContent(mshHeader, msgh11Field, msgh11FieldPrecompiled);
            msh.setProcId(getElementContent(msh11, procIdData, procIdDataPrecompiled).trim());
            msh.setProcIdMode(getElementContent(msh11, procIdModeData, procIdModeDataPrecompiled).trim());
        } catch (Throwable t) {
            logger.error("Error extracting HL7 from XML in the string " + t + " input string: " + xmlInput);
        }
        return msh;

    }

    public class HL7v2HeaderElements {
        String sendingApplication = "Unknown"; // MSH.3.1
        String sendingFacility = "Unknown"; // MSH.4.1
        String receivingApplication = "MIRTH"; // MSH.5.1
        String receivingFacility = "MIRTHPLACE"; // MSH.6.1
        String originalEvent = "ACK"; // MSH.9.2 (MSH.9/MSGH.2)
        String originalId = "123456789"; // MSH.10.1
        String procId = "P"; // MSH.11.1
        String procIdMode = ""; // // MSH.11.2
        String version = "2.4"; // MSH.12.1

        public String toString() {
            return createER7MSH(sendingApplication, sendingFacility, receivingApplication, receivingFacility, "2001111111111", originalEvent, procId, procIdMode, originalId, version);
        }

        public String getSendingApplication() {
            return sendingApplication;
        }

        public void setSendingApplication(String sendingApplication) {
            this.sendingApplication = sendingApplication;
        }

        public String getSendingFacility() {
            return sendingFacility;
        }

        public void setSendingFacility(String sendingFacility) {
            this.sendingFacility = sendingFacility;
        }

        public String getReceivingApplication() {
            return receivingApplication;
        }

        public void setReceivingApplication(String receivingApplication) {
            this.receivingApplication = receivingApplication;
        }

        public String getReceivingFacility() {
            return receivingFacility;
        }

        public void setReceivingFacility(String receivingFacility) {
            this.receivingFacility = receivingFacility;
        }

        public String getOriginalEvent() {
            return originalEvent;
        }

        public void setOriginalEvent(String originalEvent) {
            this.originalEvent = originalEvent;
        }

        public String getOriginalId() {
            return originalId;
        }

        public void setOriginalId(String originalId) {
            this.originalId = originalId;
        }

        public String getProcId() {
            return procId;
        }

        public void setProcId(String procId) {
            this.procId = procId;
        }

        public String getProcIdMode() {
            return procIdMode;
        }

        public void setProcIdMode(String procIdMode) {
            this.procIdMode = procIdMode;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }
}