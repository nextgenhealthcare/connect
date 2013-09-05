/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.hl7v2;

import java.util.LinkedHashMap;
import java.util.Map;

import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;
import com.mirth.connect.model.datatype.PropertyEditorType;
import com.mirth.connect.model.datatype.ResponseGenerationProperties;

public class HL7v2ResponseGenerationProperties extends ResponseGenerationProperties {
    private String segmentDelimiter = "\\r";
    private String successfulACKCode = "AA";
    private String successfulACKMessage = "";
    private String errorACKCode = "AE";
    private String errorACKMessage = "An Error Occured Processing Message.";
    private String rejectedACKCode = "AR";
    private String rejectedACKMessage = "Message Rejected.";
    private boolean msh15ACKAccept = false;

    public HL7v2ResponseGenerationProperties() {

    }

    public HL7v2ResponseGenerationProperties(HL7v2ResponseGenerationProperties properties) {
        this.segmentDelimiter = properties.getSegmentDelimiter();
        this.successfulACKCode = properties.getSuccessfulACKCode();
        this.successfulACKMessage = properties.getSuccessfulACKMessage();
        this.errorACKCode = properties.getErrorACKCode();
        this.errorACKMessage = properties.getErrorACKMessage();
        this.rejectedACKCode = properties.getRejectedACKCode();
        this.rejectedACKMessage = properties.getRejectedACKMessage();
        this.msh15ACKAccept = properties.isMsh15ACKAccept();
    }

    @Override
    public Map<String, DataTypePropertyDescriptor> getPropertyDescriptors() {
        Map<String, DataTypePropertyDescriptor> properties = new LinkedHashMap<String, DataTypePropertyDescriptor>();

        properties.put("segmentDelimiter", new DataTypePropertyDescriptor(segmentDelimiter, "Segment Delimiter", "This is the delimiter character(s) that will be used after each segment. This option has no effect unless an \"Auto-generate\" item has been selected in the response settings.", PropertyEditorType.STRING));
        properties.put("successfulACKCode", new DataTypePropertyDescriptor(successfulACKCode, "Successful ACK Code", "The ACK code to respond with when the message processes successfully. This option has no effect unless an \"Auto-generate\" item has been selected in the response settings.", PropertyEditorType.STRING));
        properties.put("successfulACKMessage", new DataTypePropertyDescriptor(successfulACKMessage, "Successful ACK Message", "The ACK message to respond with when the message processes successfully. This option has no effect unless an \"Auto-generate\" item has been selected in the response settings.", PropertyEditorType.STRING));
        properties.put("errorACKCode", new DataTypePropertyDescriptor(errorACKCode, "Error ACK Code", "The ACK code to respond with when an error occurs during message processing. This option has no effect unless an \"Auto-generate\" item has been selected in the response settings.", PropertyEditorType.STRING));
        properties.put("errorACKMessage", new DataTypePropertyDescriptor(errorACKMessage, "Error ACK Message", "The ACK message to respond with when an error occurs during message processing. This option has no effect unless an \"Auto-generate\" item has been selected in the response settings.", PropertyEditorType.STRING));
        properties.put("rejectedACKCode", new DataTypePropertyDescriptor(rejectedACKCode, "Rejected ACK Code", "The ACK code to respond with when the message is filtered. This option has no effect unless an \"Auto-generate\" item has been selected in the response settings.", PropertyEditorType.STRING));
        properties.put("rejectedACKMessage", new DataTypePropertyDescriptor(rejectedACKMessage, "Rejected ACK Message", "The ACK message to respond with when the message is filtered. This option has no effect unless an \"Auto-generate\" item has been selected in the response settings.", PropertyEditorType.STRING));
        properties.put("msh15ACKAccept", new DataTypePropertyDescriptor(msh15ACKAccept, "MSH-15 ACK Accept", "This setting determines if Mirth should check the MSH-15 field of an incoming message to control the acknowledgment conditions. The MSH-15 field specifies if a message should be always acknowledged, never acknowledged, or only acknowledged on error. This option has no effect unless an \"Auto-generate\" item has been selected in the response settings.", PropertyEditorType.BOOLEAN));

        return properties;
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        if (properties != null) {
            if (properties.get("segmentDelimiter") != null) {
                segmentDelimiter = (String) properties.get("segmentDelimiter");
            }
            if (properties.get("successfulACKCode") != null) {
                successfulACKCode = (String) properties.get("successfulACKCode");
            }
            if (properties.get("successfulACKMessage") != null) {
                successfulACKMessage = (String) properties.get("successfulACKMessage");
            }
            if (properties.get("errorACKCode") != null) {
                errorACKCode = (String) properties.get("errorACKCode");
            }
            if (properties.get("errorACKMessage") != null) {
                errorACKMessage = (String) properties.get("errorACKMessage");
            }
            if (properties.get("rejectedACKCode") != null) {
                rejectedACKCode = (String) properties.get("rejectedACKCode");
            }
            if (properties.get("rejectedACKMessage") != null) {
                rejectedACKMessage = (String) properties.get("rejectedACKMessage");
            }
            if (properties.get("msh15ACKAccept") != null) {
                msh15ACKAccept = (Boolean) properties.get("msh15ACKAccept");
            }
        }
    }

    public String getSegmentDelimiter() {
        return segmentDelimiter;
    }

    public void setOutputSegmentDelimiter(String segmentDelimiter) {
        this.segmentDelimiter = segmentDelimiter;
    }

    public String getSuccessfulACKCode() {
        return successfulACKCode;
    }

    public void setSuccessfulACKCode(String successfulACKCode) {
        this.successfulACKCode = successfulACKCode;
    }

    public String getSuccessfulACKMessage() {
        return successfulACKMessage;
    }

    public void setSuccessfulACKMessage(String successfulACKMessage) {
        this.successfulACKMessage = successfulACKMessage;
    }

    public String getErrorACKCode() {
        return errorACKCode;
    }

    public void setErrorACKCode(String errorACKCode) {
        this.errorACKCode = errorACKCode;
    }

    public String getErrorACKMessage() {
        return errorACKMessage;
    }

    public void setErrorACKMessage(String errorACKMessage) {
        this.errorACKMessage = errorACKMessage;
    }

    public String getRejectedACKCode() {
        return rejectedACKCode;
    }

    public void setRejectedACKCode(String rejectedACKCode) {
        this.rejectedACKCode = rejectedACKCode;
    }

    public String getRejectedACKMessage() {
        return rejectedACKMessage;
    }

    public void setRejectedACKMessage(String rejectedACKMessage) {
        this.rejectedACKMessage = rejectedACKMessage;
    }

    public boolean isMsh15ACKAccept() {
        return msh15ACKAccept;
    }

    public void setMsh15ACKAccept(boolean msh15ackAccept) {
        msh15ACKAccept = msh15ackAccept;
    }
}
