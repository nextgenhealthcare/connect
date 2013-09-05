/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.hl7v2;

import java.util.LinkedHashMap;
import java.util.Map;

import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;
import com.mirth.connect.model.datatype.PropertyEditorType;
import com.mirth.connect.model.datatype.ResponseValidationProperties;

public class HL7v2ResponseValidationProperties extends ResponseValidationProperties {
    private String successfulACKCode = "AA";
    private String errorACKCode = "AE";
    private String rejectedACKCode = "AR";

    public HL7v2ResponseValidationProperties() {}

    public HL7v2ResponseValidationProperties(HL7v2ResponseValidationProperties properties) {
        this.successfulACKCode = properties.getSuccessfulACKCode();
        this.errorACKCode = properties.getErrorACKCode();
        this.rejectedACKCode = properties.getRejectedACKCode();
    }

    @Override
    public Map<String, DataTypePropertyDescriptor> getPropertyDescriptors() {
        Map<String, DataTypePropertyDescriptor> properties = new LinkedHashMap<String, DataTypePropertyDescriptor>();

        properties.put("successfulACKCode", new DataTypePropertyDescriptor(successfulACKCode, "Successful ACK Code", "The ACK code to expect when the message is accepted by the downstream system. By default, the message status will be set to SENT.", PropertyEditorType.STRING));
        properties.put("errorACKCode", new DataTypePropertyDescriptor(errorACKCode, "Error ACK Code", "The ACK code to expect when an error occurs on the downstream system. By default, the message status will be set to ERROR.", PropertyEditorType.STRING));
        properties.put("rejectedACKCode", new DataTypePropertyDescriptor(rejectedACKCode, "Rejected ACK Code", "The ACK code to expect when the message is rejected by the downstream system. By default, the message status will be set to ERROR.", PropertyEditorType.STRING));

        return properties;
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        if (properties != null) {
            if (properties.get("successfulACKCode") != null) {
                successfulACKCode = (String) properties.get("successfulACKCode");
            }
            if (properties.get("errorACKCode") != null) {
                errorACKCode = (String) properties.get("errorACKCode");
            }
            if (properties.get("rejectedACKCode") != null) {
                rejectedACKCode = (String) properties.get("rejectedACKCode");
            }
        }
    }

    public String getSuccessfulACKCode() {
        return successfulACKCode;
    }

    public void setSuccessfulACKCode(String successfulACKCode) {
        this.successfulACKCode = successfulACKCode;
    }

    public String getErrorACKCode() {
        return errorACKCode;
    }

    public void setErrorACKCode(String errorACKCode) {
        this.errorACKCode = errorACKCode;
    }

    public String getRejectedACKCode() {
        return rejectedACKCode;
    }

    public void setRejectedACKCode(String rejectedACKCode) {
        this.rejectedACKCode = rejectedACKCode;
    }
}
