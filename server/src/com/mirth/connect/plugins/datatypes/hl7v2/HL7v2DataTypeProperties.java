/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.hl7v2;

import java.util.Map;

import com.mirth.connect.util.StringUtil;

public class HL7v2DataTypeProperties {

    private boolean handleRepetitions = false;
    private boolean handleSubcomponents = false;
    private boolean useStrictParser = false;
    private boolean useStrictValidation = false;
    private boolean stripNamespaces = true;
    private String inputSegmentDelimiter = "\r\n|\r|\n";
    private String outputSegmentDelimiter = "\r";
    private String successfulACKCode = "AA";
    private String successfulACKMessage = "";
    private String errorACKCode = "AE";
    private String errorACKMessage = "An Error Occured Processing Message.";
    private String rejectedACKCode = "AR";
    private String rejectedACKMessage = "Message Rejected.";
    private boolean msh15ACKAccept = false;

    public HL7v2DataTypeProperties() {}

    public HL7v2DataTypeProperties(Map<?, ?> properties) {
        if (properties.get("handleRepetitions") != null) {
            handleRepetitions = Boolean.valueOf((String) properties.get("handleRepetitions"));
        }
        if (properties.get("handleSubcomponents") != null) {
            handleSubcomponents = Boolean.valueOf((String) properties.get("handleSubcomponents"));
        }
        if (properties.get("useStrictParser") != null) {
            useStrictParser = Boolean.valueOf((String) properties.get("useStrictParser"));
        }
        if (properties.get("useStrictValidation") != null) {
            useStrictValidation = Boolean.valueOf((String) properties.get("useStrictValidation"));
        }
        if (properties.get("stripNamespaces") != null) {
            stripNamespaces = Boolean.valueOf((String) properties.get("stripNamespaces"));
        }
        if (properties.get("inputSegmentDelimiter") != null) {
            this.inputSegmentDelimiter = StringUtil.unescape((String) properties.get("inputSegmentDelimiter"));
        }
        if (properties.get("outputSegmentDelimiter") != null) {
            this.outputSegmentDelimiter = StringUtil.unescape((String) properties.get("outputSegmentDelimiter"));
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
            msh15ACKAccept = Boolean.valueOf((String) properties.get("msh15ACKAccept"));
        }
    }

    public boolean isHandleRepetitions() {
        return handleRepetitions;
    }

    public void setHandleRepetitions(boolean handleRepetitions) {
        this.handleRepetitions = handleRepetitions;
    }

    public boolean isHandleSubcomponents() {
        return handleSubcomponents;
    }

    public void setHandleSubcomponents(boolean handleSubcomponents) {
        this.handleSubcomponents = handleSubcomponents;
    }

    public boolean isUseStrictParser() {
        return useStrictParser;
    }

    public void setUseStrictParser(boolean useStrictParser) {
        this.useStrictParser = useStrictParser;
    }

    public boolean isUseStrictValidation() {
        return useStrictValidation;
    }

    public void setUseStrictValidation(boolean useStrictValidation) {
        this.useStrictValidation = useStrictValidation;
    }

    public boolean isStripNamespaces() {
        return stripNamespaces;
    }

    public void setStripNamespaces(boolean stripNamespaces) {
        this.stripNamespaces = stripNamespaces;
    }

    public String getInputSegmentDelimiter() {
        return inputSegmentDelimiter;
    }

    public void setInputSegmentDelimiters(String inputSegmentDelimiter) {
        this.inputSegmentDelimiter = inputSegmentDelimiter;
    }
    
    public String getOutputSegmentDelimiter() {
        return outputSegmentDelimiter;
    }

    public void setOutputSegmentDelimiters(String outputSegmentDelimiter) {
        this.outputSegmentDelimiter = outputSegmentDelimiter;
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
