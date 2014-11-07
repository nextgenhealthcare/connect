/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.hl7v2;

import java.io.CharArrayReader;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.message.ResponseValidator;
import com.mirth.connect.model.datatype.ResponseValidationProperties;
import com.mirth.connect.model.datatype.SerializationProperties;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.ErrorMessageBuilder;
import com.mirth.connect.util.StringUtil;

public class HL7v2ResponseValidator implements ResponseValidator {

    private HL7v2SerializationProperties serializationProperties;
    private HL7v2ResponseValidationProperties responseValidationProperties;
    private String serializationSegmentDelimiter;
    private TemplateValueReplacer replacer = new TemplateValueReplacer();

    public HL7v2ResponseValidator(SerializationProperties serializationProperties, ResponseValidationProperties responseValidationProperties) {
        this.serializationProperties = (HL7v2SerializationProperties) serializationProperties;
        this.responseValidationProperties = (HL7v2ResponseValidationProperties) responseValidationProperties;
        serializationSegmentDelimiter = StringUtil.unescape(this.serializationProperties.getSegmentDelimiter());
    }

    @Override
    public Response validate(Response response, ConnectorMessage connectorMessage) {
        HL7v2ResponseValidationProperties responseValidationProperties = getReplacedResponseValidationProperties(connectorMessage);
        String[] successfulACKCodes = StringUtils.split(responseValidationProperties.getSuccessfulACKCode(), ',');
        String[] errorACKCodes = StringUtils.split(responseValidationProperties.getErrorACKCode(), ',');
        String[] rejectedACKCodes = StringUtils.split(responseValidationProperties.getRejectedACKCode(), ',');

        String responseData = response.getMessage();

        if (StringUtils.isNotBlank(responseData)) {
            try {
                if (responseData.trim().startsWith("<")) {
                    // XML response received
                    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new CharArrayReader(responseData.toCharArray())));
                    String ackCode = XPathFactory.newInstance().newXPath().compile("//MSA.1/text()").evaluate(doc).trim();

                    boolean rejected = Arrays.asList(rejectedACKCodes).contains(ackCode);
                    boolean error = rejected || Arrays.asList(errorACKCodes).contains(ackCode);

                    if (error || rejected) {
                        String msa3 = StringUtils.trim(XPathFactory.newInstance().newXPath().compile("//MSA.3/text()").evaluate(doc));
                        String err1 = StringUtils.trim(XPathFactory.newInstance().newXPath().compile("//ERR.1/text()").evaluate(doc));
                        handleNACK(response, rejected, msa3, err1);
                    } else if (Arrays.asList(successfulACKCodes).contains(ackCode)) {
                        response.setStatus(Status.SENT);
                    }
                } else {
                    // ER7 response received
                    if (serializationProperties.isConvertLineBreaks()) {
                        responseData = StringUtil.convertLineBreaks(responseData, serializationSegmentDelimiter);
                    }

                    int index = -1;
                    boolean valid = true;

                    // Attempt to find the MSA segment using the segment delimiters in the serialization properties
                    if ((index = responseData.indexOf(serializationSegmentDelimiter + "MSA")) >= 0) {
                        // MSA found; add the length of the segment delimiter, MSA, and field separator to get to the index of MSA.1
                        index += serializationSegmentDelimiter.length() + 4;

                        if (index < responseData.length()) {
                            boolean rejected = startsWithAny(responseData, rejectedACKCodes, index);
                            boolean error = rejected || startsWithAny(responseData, errorACKCodes, index);

                            if (error || rejected) {
                                String msa3 = null;
                                String err1 = null;
                                char fieldSeparator = responseData.charAt(index - 1);

                                // Index of MSA.2
                                index = responseData.indexOf(fieldSeparator, index);
                                if (index >= 0) {
                                    // Index of MSA.3
                                    index = responseData.indexOf(fieldSeparator, index + 1);
                                    if (index >= 0) {
                                        // Find the next index of either the field separator or segment delimiter, and then the resulting substring
                                        String tempSegment = StringUtils.substring(responseData, index + 1);
                                        index = StringUtils.indexOfAny(tempSegment, fieldSeparator + serializationSegmentDelimiter);

                                        if (index >= 0) {
                                            msa3 = StringUtils.substring(tempSegment, 0, index);
                                        } else {
                                            msa3 = StringUtils.substring(tempSegment, 0);
                                        }
                                    }
                                }

                                if ((index = responseData.indexOf(serializationSegmentDelimiter + "ERR")) >= 0) {
                                    // ERR found; add the length of the segment delimiter, ERR, and field separator to get to the index of ERR.1
                                    index += serializationSegmentDelimiter.length() + 4;
                                    // Find the next index of either the field separator or segment delimiter, and then the resulting substring
                                    String tempSegment = StringUtils.substring(responseData, index);
                                    index = StringUtils.indexOfAny(tempSegment, fieldSeparator + serializationSegmentDelimiter);

                                    if (index >= 0) {
                                        err1 = StringUtils.substring(tempSegment, 0, index);
                                    } else {
                                        err1 = StringUtils.substring(tempSegment, 0);
                                    }
                                }

                                handleNACK(response, rejected, msa3, err1);
                            } else if (startsWithAny(responseData, successfulACKCodes, index)) {
                                response.setStatus(Status.SENT);
                            } else {
                                valid = false;
                            }
                        } else {
                            valid = false;
                        }
                    } else {
                        valid = false;
                    }

                    if (!valid) {
                        response.setStatus(Status.QUEUED);
                        response.setStatusMessage("Invalid HL7 v2.x acknowledgement received.");
                        response.setError(response.getStatusMessage());
                    }
                }
            } catch (Exception e) {
                response.setStatus(Status.QUEUED);
                response.setStatusMessage("Error validating response: " + e.getMessage());
                response.setError(ErrorMessageBuilder.buildErrorMessage(this.getClass().getSimpleName(), response.getStatusMessage(), e));
            }
        } else {
            response.setStatus(Status.QUEUED);
            response.setStatusMessage("Empty or blank response received.");
            response.setError(response.getStatusMessage());
        }

        return response;
    }

    private boolean startsWithAny(String str, String[] prefixes, int toffset) {
        for (String prefix : prefixes) {
            if (str.startsWith(prefix, toffset)) {
                return true;
            }
        }
        return false;
    }

    private HL7v2ResponseValidationProperties getReplacedResponseValidationProperties(ConnectorMessage connectorMessage) {
        HL7v2ResponseValidationProperties props = new HL7v2ResponseValidationProperties(responseValidationProperties);

        props.setSuccessfulACKCode(replacer.replaceValues(props.getSuccessfulACKCode(), connectorMessage));
        props.setErrorACKCode(replacer.replaceValues(props.getErrorACKCode(), connectorMessage));
        props.setRejectedACKCode(replacer.replaceValues(props.getRejectedACKCode(), connectorMessage));

        return props;
    }

    private void handleNACK(Response response, boolean rejected, String msa3, String err1) {
        response.setStatus(Status.ERROR);

        StringBuilder nackMessage = new StringBuilder("NACK sent from receiver. (");
        nackMessage.append(rejected ? "Rejected)" : "Error)");

        if (StringUtils.isNotEmpty(msa3)) {
            nackMessage.append('\n');
            nackMessage.append(msa3);
        }
        if (StringUtils.isNotEmpty(err1)) {
            nackMessage.append('\n');
            nackMessage.append(err1);
        }

        response.setStatusMessage(nackMessage.toString());

        nackMessage.append("\n\n");
        nackMessage.append(response.getMessage());
        response.setError(nackMessage.toString());
    }
}
