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

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;
import com.mirth.connect.model.datatype.PropertyEditorType;
import com.mirth.connect.model.datatype.ResponseValidationProperties;

public class HL7v2ResponseValidationProperties extends ResponseValidationProperties {
    private String successfulACKCode = "AA,CA";
    private String errorACKCode = "AE,CE";
    private String rejectedACKCode = "AR,CR";
    private boolean validateMessageControlId = true;
    private OriginalMessageControlId originalMessageControlId = OriginalMessageControlId.values()[0];
    private String originalIdMapVariable = "";

    public enum OriginalMessageControlId {
        Destination_Encoded, Map_Variable;

        @Override
        public String toString() {
            return super.toString().replace('_', ' ');
        }
    };

    public HL7v2ResponseValidationProperties() {}

    public HL7v2ResponseValidationProperties(HL7v2ResponseValidationProperties properties) {
        this.successfulACKCode = properties.getSuccessfulACKCode();
        this.errorACKCode = properties.getErrorACKCode();
        this.rejectedACKCode = properties.getRejectedACKCode();
        this.validateMessageControlId = properties.isValidateMessageControlId();
        this.originalMessageControlId = properties.getOriginalMessageControlId();
        this.originalIdMapVariable = properties.getOriginalIdMapVariable();
    }

    @Override
    public Map<String, DataTypePropertyDescriptor> getPropertyDescriptors() {
        Map<String, DataTypePropertyDescriptor> properties = new LinkedHashMap<String, DataTypePropertyDescriptor>();

        properties.put("successfulACKCode", new DataTypePropertyDescriptor(successfulACKCode, "Successful ACK Codes", "The ACK code(s) to expect when the message is accepted by the downstream system. By default, the message status will be set to SENT. Specify multiple codes with a list of comma separated values.", PropertyEditorType.STRING));
        properties.put("errorACKCode", new DataTypePropertyDescriptor(errorACKCode, "Error ACK Codes", "The ACK code(s) to expect when an error occurs on the downstream system. By default, the message status will be set to ERROR. Specify multiple codes with a list of comma separated values.", PropertyEditorType.STRING));
        properties.put("rejectedACKCode", new DataTypePropertyDescriptor(rejectedACKCode, "Rejected ACK Codes", "The ACK code(s) to expect when the message is rejected by the downstream system. By default, the message status will be set to ERROR. Specify multiple codes with a list of comma separated values.", PropertyEditorType.STRING));
        properties.put("validateMessageControlId", new DataTypePropertyDescriptor(validateMessageControlId, "Validate Message Control Id", "Select this option to validate the Message Control Id (MSA-2) returned from the response.", PropertyEditorType.BOOLEAN));
        properties.put("originalMessageControlId", new DataTypePropertyDescriptor(originalMessageControlId, "Original Message Control Id", "Select the source of the original Message Control Id used to validate the response. If Destination Encoded is selected, the Id will be extracted from the MSH-10 field of the destination's encoded content. If Map Variable is selected, the Id will be retrieved from the destination's connector map or the channel map.", PropertyEditorType.OPTION, OriginalMessageControlId.values()));
        properties.put("originalIdMapVariable", new DataTypePropertyDescriptor(originalIdMapVariable, "Original Id Map Variable", "This field must be populated if the Original Message Control Id is set to Map Variable. The Id will be read from this variable in the destination's connector map or the channel map. ", PropertyEditorType.STRING));

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
            if (properties.get("validateMessageControlId") != null) {
                validateMessageControlId = (Boolean) properties.get("validateMessageControlId");
            }
            if (properties.get("originalMessageControlId") != null) {
                originalMessageControlId = (OriginalMessageControlId) properties.get("originalMessageControlId");
            }
            if (properties.get("originalIdMapVariable") != null) {
                originalIdMapVariable = (String) properties.get("originalIdMapVariable");
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

    public boolean isValidateMessageControlId() {
        return validateMessageControlId;
    }

    public void setValidateMessageControlId(boolean validateMessageControlId) {
        this.validateMessageControlId = validateMessageControlId;
    }

    public OriginalMessageControlId getOriginalMessageControlId() {
        return originalMessageControlId;
    }

    public void setOriginalMessageControlId(OriginalMessageControlId originalMessageControlId) {
        this.originalMessageControlId = originalMessageControlId;
    }

    public String getOriginalIdMapVariable() {
        return originalIdMapVariable;
    }

    public void setOriginalIdMapVariable(String originalIdMapVariable) {
        this.originalIdMapVariable = originalIdMapVariable;
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {}

    @Override
    public void migrate3_2_0(DonkeyElement element) {
        element.addChildElementIfNotExists("validateMessageControlId", "false");
        element.addChildElementIfNotExists("originalMessageControlId", "Destination_Encoded");
        element.addChildElementIfNotExists("originalIdMapVariable", "");
    }

    @Override
    public void migrate3_3_0(DonkeyElement element) {}

    @Override
    public void migrate3_4_0(DonkeyElement element) {}

    @Override
    public void migrate3_5_0(DonkeyElement element) {}

    @Override
    public void migrate3_6_0(DonkeyElement element) {}
    
    @Override
    public Map<String, Object> getPurgedProperties() {
        return null;
    }
}
