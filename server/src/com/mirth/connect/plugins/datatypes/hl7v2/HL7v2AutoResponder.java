/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.hl7v2;

import java.io.CharArrayReader;
import java.io.Reader;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.message.AutoResponder;
import com.mirth.connect.model.datatype.ResponseGenerationProperties;
import com.mirth.connect.model.datatype.SerializationProperties;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.StringUtil;

public class HL7v2AutoResponder implements AutoResponder {

    private Logger logger = Logger.getLogger(getClass());
    private HL7v2SerializationProperties serializationProperties;
    private HL7v2ResponseGenerationProperties generationProperties;
    private TemplateValueReplacer replacer = new TemplateValueReplacer();

    private String serializationSegmentDelimiter = null;
    private String deserializationSegmentDelimiter = null;

    public HL7v2AutoResponder() {
        this(null, null);
    }

    public HL7v2AutoResponder(SerializationProperties serializationProperties, ResponseGenerationProperties generationProperties) {
        this.serializationProperties = (HL7v2SerializationProperties) serializationProperties;
        this.generationProperties = (HL7v2ResponseGenerationProperties) generationProperties;

        if (this.serializationProperties != null) {
            serializationSegmentDelimiter = StringUtil.unescape(this.serializationProperties.getSegmentDelimiter());
        }
        if (this.generationProperties != null) {
            deserializationSegmentDelimiter = StringUtil.unescape(this.generationProperties.getSegmentDelimiter());
        }
    }

    @Override
    public Response getResponse(Status status, String message, ConnectorMessage connectorMessage) {
        HL7v2ResponseGenerationProperties hl7Properties = getReplacedHL7Properties(connectorMessage);
        return generateACK(status, message, hl7Properties);
    }

    private HL7v2ResponseGenerationProperties getReplacedHL7Properties(ConnectorMessage connectorMessage) {
        // Create a new properties object
        HL7v2ResponseGenerationProperties hl7v2Properties = new HL7v2ResponseGenerationProperties(generationProperties);

        // Replace ACK properties
        hl7v2Properties.setSuccessfulACKCode(replacer.replaceValues(hl7v2Properties.getSuccessfulACKCode(), connectorMessage));
        hl7v2Properties.setSuccessfulACKMessage(replacer.replaceValues(hl7v2Properties.getSuccessfulACKMessage(), connectorMessage));
        hl7v2Properties.setErrorACKCode(replacer.replaceValues(hl7v2Properties.getErrorACKCode(), connectorMessage));
        hl7v2Properties.setErrorACKMessage(replacer.replaceValues(hl7v2Properties.getErrorACKMessage(), connectorMessage));
        hl7v2Properties.setRejectedACKCode(replacer.replaceValues(hl7v2Properties.getRejectedACKCode(), connectorMessage));
        hl7v2Properties.setRejectedACKMessage(replacer.replaceValues(hl7v2Properties.getRejectedACKMessage(), connectorMessage));

        return hl7v2Properties;
    }

    private Response generateACK(Status status, String hl7Message, HL7v2ResponseGenerationProperties hl7v2Properties) {
        boolean errorOnly = false;
        boolean always = false;
        boolean successOnly = false;

        hl7Message = hl7Message.trim();
        boolean isXML = hl7Message.charAt(0) == '<';

        String ACK = null;
        String statusMessage = null;
        String error = null;

        try {
            // Check if we have to look at MSH15
            if (hl7v2Properties.isMsh15ACKAccept()) {
                // MSH15 Dictionary:
                // AL: Always
                // NE: Never
                // ER: Error / Reject condition
                // SU: Successful completion only

                String msh15 = "";

                // Check if the message is ER7 or XML
                if (isXML) { // XML form
                    XPath xpath = XPathFactory.newInstance().newXPath();
                    XPathExpression msh15Query = xpath.compile("//MSH.15/text()");
                    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = domFactory.newDocumentBuilder();
                    Reader reader = new CharArrayReader(hl7Message.toCharArray());
                    Document doc = builder.parse(new InputSource(reader));
                    msh15 = msh15Query.evaluate(doc);
                } else { // ER7
                    char fieldDelim = hl7Message.charAt(3); // Usually |
                    char componentDelim = hl7Message.charAt(4); // Usually ^

                    Pattern fieldPattern = Pattern.compile(Pattern.quote(String.valueOf(fieldDelim)));
                    Pattern componentPattern = Pattern.compile(Pattern.quote(String.valueOf(componentDelim)));

                    String mshString = hl7Message.split(serializationSegmentDelimiter)[0];
                    String[] mshFields = fieldPattern.split(mshString);

                    if (mshFields.length > 14) {
                        msh15 = componentPattern.split(mshFields[14])[0]; // MSH.15.1
                    }
                }

                if (msh15 != null && !msh15.equals("")) {
                    if (msh15.equalsIgnoreCase("AL")) {
                        always = true;
                    } else if (msh15.equalsIgnoreCase("NE")) {
                        logger.debug("MSH15 is NE, Skipping ACK");
                        return null;
                    } else if (msh15.equalsIgnoreCase("ER")) {
                        errorOnly = true;
                    } else if (msh15.equalsIgnoreCase("SU")) {
                        successOnly = true;
                    }
                }
            }

            String ackCode = "AA";
            String ackMessage = "";
            boolean nack = false;

            if (status == Status.ERROR) {
                if (successOnly) {
                    // we only send an ACK on success
                    return null;
                }
                ackCode = hl7v2Properties.getErrorACKCode();
                ackMessage = hl7v2Properties.getErrorACKMessage();
                nack = true;
            } else if (status == Status.FILTERED) {
                if (successOnly) {
                    return null;
                }
                ackCode = hl7v2Properties.getRejectedACKCode();
                ackMessage = hl7v2Properties.getRejectedACKMessage();
                nack = true;
            } else {
                if (errorOnly) {
                    return null;
                }
                ackCode = hl7v2Properties.getSuccessfulACKCode();
                ackMessage = hl7v2Properties.getSuccessfulACKMessage();
            }

            ACK = new ACKGenerator().generateAckResponse(hl7Message, isXML, ackCode, ackMessage, "yyyyMMddHHmmss", new String(), deserializationSegmentDelimiter);
            statusMessage = "HL7v2 " + (nack ? "N" : "") + "ACK successfully generated.";
            logger.debug("HL7v2 " + (nack ? "N" : "") + "ACK successfully generated: " + ACK);
        } catch (Exception e) {
            logger.warn("Error generating HL7v2 ACK.", e);
            statusMessage = "Error generating HL7v2 ACK.";
            error = e.getMessage();
        }

        return new Response(status, ACK, statusMessage, error);
    }
}
