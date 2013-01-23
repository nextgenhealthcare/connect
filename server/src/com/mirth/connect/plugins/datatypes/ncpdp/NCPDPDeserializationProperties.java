package com.mirth.connect.plugins.datatypes.ncpdp;

import java.util.LinkedHashMap;
import java.util.Map;

import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;
import com.mirth.connect.model.datatype.DeserializationProperties;
import com.mirth.connect.model.datatype.PropertyEditorType;

public class NCPDPDeserializationProperties implements DeserializationProperties {
    
    private String segmentDelimiter = "0x1E";
    private String groupDelimiter = "0x1D";
    private String fieldDelimiter = "0x1C";
    private boolean useStrictValidation = false;

    @Override
    public Map<String, DataTypePropertyDescriptor> getProperties() {
        Map<String, DataTypePropertyDescriptor> properties = new LinkedHashMap<String, DataTypePropertyDescriptor>();
        
        properties.put("fieldDelimiter", new DataTypePropertyDescriptor(fieldDelimiter, "Field Delimiter", "Characters that delimit the fields in the message.", PropertyEditorType.STRING));
        properties.put("groupDelimiter", new DataTypePropertyDescriptor(groupDelimiter, "Group Delimiter", "Characters that delimit the groups in the message.", PropertyEditorType.STRING));
        properties.put("segmentDelimiter", new DataTypePropertyDescriptor(segmentDelimiter, "Segment Delimiter", "Characters that delimit the segments in the message.", PropertyEditorType.STRING));
        properties.put("useStrictValidation", new DataTypePropertyDescriptor(useStrictValidation, "Use Strict Validation", "Validates the NCPDP message against a schema", PropertyEditorType.BOOLEAN));
        
        return properties;
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        if (properties != null) {
            if (properties.get("segmentDelimiter") != null) {
                this.segmentDelimiter = (String) properties.get("segmentDelimiter");
            }

            if (properties.get("groupDelimiter") != null) {
                this.segmentDelimiter = (String) properties.get("groupDelimiter");
            }

            if (properties.get("fieldDelimiter") != null) {
                this.segmentDelimiter = (String) properties.get("fieldDelimiter");
            }
            if (properties.get("useStrictValidation") != null) {
                this.useStrictValidation = (Boolean) properties.get("useStrictValidation");
            }
        }
    }
    
    public String getSegmentDelimiter() {
        return segmentDelimiter;
    }

    public void setSegmentDelimiter(String segmentDelimiter) {
        this.segmentDelimiter = segmentDelimiter;
    }

    public String getGroupDelimiter() {
        return groupDelimiter;
    }

    public void setGroupDelimiter(String groupDelimiter) {
        this.groupDelimiter = groupDelimiter;
    }

    public String getFieldDelimiter() {
        return fieldDelimiter;
    }

    public void setFieldDelimiter(String fieldDelimiter) {
        this.fieldDelimiter = fieldDelimiter;
    }

    public boolean isUseStrictValidation() {
        return useStrictValidation;
    }

    public void setUseStrictValidation(boolean useStrictValidation) {
        this.useStrictValidation = useStrictValidation;
    }
    
}
