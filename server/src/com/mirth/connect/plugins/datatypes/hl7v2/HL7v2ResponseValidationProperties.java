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
    private boolean msh15ACKAccept = false;
    
    @Override
    public Map<String, DataTypePropertyDescriptor> getProperties() {
        Map<String, DataTypePropertyDescriptor> properties = new LinkedHashMap<String, DataTypePropertyDescriptor>();
        
        properties.put("successfulACKCode", new DataTypePropertyDescriptor(successfulACKCode, "Successful ACK Code", "The ACK code to respond with when the message processes successfully. This option has no effect unless an \"Auto Respond\" item has been selected in the response settings.", PropertyEditorType.STRING));
        properties.put("errorACKCode", new DataTypePropertyDescriptor(errorACKCode, "Error ACK Code", "The ACK code to respond with when an error occurs during message processing. This option has no effect unless an \"Auto Respond\" item has been selected in the response settings.", PropertyEditorType.STRING));
        properties.put("rejectedACKCode", new DataTypePropertyDescriptor(rejectedACKCode, "Rejected ACK Code", "The ACK code to respond with when the message is filtered. This option has no effect unless an \"Auto Respond\" item has been selected in the response settings.", PropertyEditorType.STRING));
        properties.put("msh15ACKAccept", new DataTypePropertyDescriptor(msh15ACKAccept, "MSH-15 ACK Accept", "This setting determines if Mirth should check the MSH-15 field of an incoming message to control the acknowledgment conditions. The MSH-15 field specifies if a message should be always acknowledged, never acknowledged, or only acknowledged on error. This option has no effect unless an \"Auto Respond\" item has been selected in the response settings.", PropertyEditorType.BOOLEAN));
        
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
            if (properties.get("msh15ACKAccept") != null) {
                msh15ACKAccept = (Boolean) properties.get("msh15ACKAccept");
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
    public boolean isMsh15ACKAccept() {
        return msh15ACKAccept;
    }
    public void setMsh15ACKAccept(boolean msh15ackAccept) {
        msh15ACKAccept = msh15ackAccept;
    }
}
