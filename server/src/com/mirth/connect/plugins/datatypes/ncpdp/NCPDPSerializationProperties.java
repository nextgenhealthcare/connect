package com.mirth.connect.plugins.datatypes.ncpdp;

import java.util.LinkedHashMap;
import java.util.Map;

import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;
import com.mirth.connect.model.datatype.PropertyEditorType;
import com.mirth.connect.model.datatype.SerializationProperties;

public class NCPDPSerializationProperties implements SerializationProperties {
    
    private String segmentDelimiter = "0x1E";
    private String groupDelimiter = "0x1D";
    private String fieldDelimiter = "0x1C";

    @Override
    public Map<String, DataTypePropertyDescriptor> getProperties() {
        Map<String, DataTypePropertyDescriptor> properties = new LinkedHashMap<String, DataTypePropertyDescriptor>();
        
        properties.put("fieldDelimiter", new DataTypePropertyDescriptor(fieldDelimiter, "Field Delimiter", "Characters that delimit the fields in the message.", PropertyEditorType.STRING));
        properties.put("groupDelimiter", new DataTypePropertyDescriptor(groupDelimiter, "Group Delimiter", "Characters that delimit the groups in the message.", PropertyEditorType.STRING));
        properties.put("segmentDelimiter", new DataTypePropertyDescriptor(segmentDelimiter, "Segment Delimiter", "Characters that delimit the segments in the message.", PropertyEditorType.STRING));
        
        return properties;
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        if (properties != null) {
            if (properties.get("segmentDelimiter") != null) {
                this.segmentDelimiter = (String) properties.get("segmentDelimiter");
            }

            if (properties.get("groupDelimiter") != null) {
                this.groupDelimiter = (String) properties.get("groupDelimiter");
            }

            if (properties.get("fieldDelimiter") != null) {
                this.fieldDelimiter = (String) properties.get("fieldDelimiter");
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
}
