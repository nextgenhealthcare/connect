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
            if (responseData.trim().startsWith("<")) {
                // XML response received
                try {
                    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new CharArrayReader(responseData.toCharArray())));
                    String ackCode = XPathFactory.newInstance().newXPath().compile("//MSA.1/text()").evaluate(doc).trim();
                    if (Arrays.asList(errorACKCodes).contains(ackCode) || Arrays.asList(rejectedACKCodes).contains(ackCode)) {
                        handleNACK(response);
                    } else if (Arrays.asList(successfulACKCodes).contains(ackCode)) {
                        response.setStatus(Status.SENT);
                    }
                } catch (Exception e) {
                    response.setStatus(Status.QUEUED);
                    response.setStatusMessage("Error validating response: " + e.getMessage());
                    response.setError(ErrorMessageBuilder.buildErrorMessage(this.getClass().getSimpleName(), response.getStatusMessage(), e));
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

                    if (startsWithAny(responseData, errorACKCodes, index) || startsWithAny(responseData, rejectedACKCodes, index)) {
                        handleNACK(response);
                    } else if (startsWithAny(responseData, successfulACKCodes, index)) {
                        response.setStatus(Status.SENT);
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

    private void handleNACK(Response response) {
        response.setStatus(Status.ERROR);
        response.setStatusMessage("NACK sent from receiver.");
        response.setError("NACK sent from receiver: " + response.getMessage());
    }
}
