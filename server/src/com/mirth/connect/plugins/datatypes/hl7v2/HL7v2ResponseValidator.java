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
import com.mirth.connect.plugins.datatypes.hl7v2.HL7v2ResponseValidationProperties.OriginalMessageControlId;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.ErrorMessageBuilder;
import com.mirth.connect.util.StringUtil;

public class HL7v2ResponseValidator implements ResponseValidator {

    private HL7v2SerializationProperties serializationProperties;
    private HL7v2ResponseValidationProperties responseValidationProperties;
    private String serializationSegmentDelimiter;
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private static int MESSAGE_CONTROL_ID_FIELD = 10;

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
        boolean validateMessageControlId = responseValidationProperties.isValidateMessageControlId();

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
                        if (validateMessageControlId) {
                            String msa2 = StringUtils.trim(XPathFactory.newInstance().newXPath().compile("//MSA.2/text()").evaluate(doc));
                            String originalControlID = getOriginalControlId(connectorMessage);

                            if (!StringUtils.equals(msa2, originalControlID)) {
                                handleInvalidControlId(response, originalControlID, msa2);
                            } else {
                                response.setStatus(Status.SENT);
                            }
                        } else {
                            response.setStatus(Status.SENT);
                        }
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

                            char fieldSeparator = responseData.charAt(index - 1);
                            if (error || rejected) {
                                String msa3 = null;
                                String err1 = null;

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
                                if (validateMessageControlId) {
                                    String msa2 = "";
                                    index = responseData.indexOf(fieldSeparator, index);

                                    if (index >= 0) {
                                        String tempSegment = StringUtils.substring(responseData, index + 1);
                                        index = StringUtils.indexOfAny(tempSegment, fieldSeparator + serializationSegmentDelimiter);

                                        if (index >= 0) {
                                            msa2 = StringUtils.substring(tempSegment, 0, index);
                                        } else {
                                            msa2 = StringUtils.substring(tempSegment, 0);
                                        }
                                    }
                                    String originalControlID = getOriginalControlId(connectorMessage);

                                    if (!StringUtils.equals(msa2, originalControlID)) {
                                        handleInvalidControlId(response, originalControlID, msa2);
                                    } else {
                                        response.setStatus(Status.SENT);
                                    }
                                } else {
                                    response.setStatus(Status.SENT);
                                }
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

    private String getOriginalControlId(ConnectorMessage connectorMessage) throws Exception {
        String controlId = "";
        String originalMessage = "";

        if (responseValidationProperties.getOriginalMessageControlId().equals(OriginalMessageControlId.Destination_Encoded)) {
            originalMessage = connectorMessage.getEncoded().getContent();
        } else if (responseValidationProperties.getOriginalMessageControlId().equals(OriginalMessageControlId.Map_Variable)) {
            String originalIdMapVariable = responseValidationProperties.getOriginalIdMapVariable();
            if (StringUtils.isEmpty(originalIdMapVariable)) {
                throw new Exception("Map variable for original control Id not set.");
            }

            Object value = null;
            if (connectorMessage.getConnectorMap().containsKey(originalIdMapVariable)) {
                value = connectorMessage.getConnectorMap().get(originalIdMapVariable);
            } else {
                value = connectorMessage.getChannelMap().get(originalIdMapVariable);
            }

            if (value == null) {
                throw new Exception("Map variable for original control Id not set.");
            }

            controlId = value.toString();

            return controlId;
        }

        if (originalMessage.startsWith("<")) {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new CharArrayReader(originalMessage.toCharArray())));
            controlId = XPathFactory.newInstance().newXPath().compile("//MSH.10.1/text()").evaluate(doc).trim();
        } else {
            int index;

            if ((index = originalMessage.indexOf("MSH")) >= 0) {
                index += 4;
                char fieldSeparator = originalMessage.charAt(index - 1);
                int iteration = 2;
                int segmentDelimeterIndex = originalMessage.indexOf(serializationSegmentDelimiter, index);

                while (iteration < MESSAGE_CONTROL_ID_FIELD) {
                    index = originalMessage.indexOf(fieldSeparator, index + 1);

                    if ((segmentDelimeterIndex >= 0 && segmentDelimeterIndex < index) || index == -1) {
                        return "";
                    }

                    iteration++;
                }

                String tempSegment = StringUtils.substring(originalMessage, index + 1);
                index = StringUtils.indexOfAny(tempSegment, fieldSeparator + serializationSegmentDelimiter);

                if (index >= 0) {
                    controlId = StringUtils.substring(tempSegment, 0, index);
                } else {
                    controlId = StringUtils.substring(tempSegment, 0);
                }
            }
        }

        return controlId;
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

    private void handleInvalidControlId(Response response, String originalControlID, String msa2) {
        response.setStatus(Status.ERROR);

        String statusMessage = "Message control Ids do not match.";
        response.setStatusMessage(statusMessage);
        response.setError(statusMessage + "\nExpected: " + originalControlID + "\nActual: " + msa2);
    }
}