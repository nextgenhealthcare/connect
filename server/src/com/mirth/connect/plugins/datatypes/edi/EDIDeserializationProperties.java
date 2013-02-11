package com.mirth.connect.plugins.datatypes.edi;

import java.util.LinkedHashMap;
import java.util.Map;

import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;
import com.mirth.connect.model.datatype.DeserializationProperties;
import com.mirth.connect.model.datatype.PropertyEditorType;

public class EDIDeserializationProperties extends DeserializationProperties {
    private String segmentDelimiter = "~";
    private String elementDelimiter = "*";
    private String subelementDelimiter = ":";
    
    @Override
    public Map<String, DataTypePropertyDescriptor> getProperties() {
        Map<String, DataTypePropertyDescriptor> properties = new LinkedHashMap<String, DataTypePropertyDescriptor>();
        
        properties.put("elementDelimiter", new DataTypePropertyDescriptor(elementDelimiter, "Element Delimiter", "Characters that delimit the segments in the message.", PropertyEditorType.STRING));
        properties.put("segmentDelimiter", new DataTypePropertyDescriptor(segmentDelimiter, "Segment Delimiter", "Characters that delimit the elements in the message.", PropertyEditorType.STRING));
        properties.put("subelementDelimiter", new DataTypePropertyDescriptor(subelementDelimiter, "Subelement Delimiter", "Characters that delimit the subelements in the message.", PropertyEditorType.STRING));
        
        return properties;
    }
    
    @Override
    public void setProperties(Map<String, Object> properties) {
        if (properties != null) {
            if (properties.get("segmentDelimiter") != null) {
                this.segmentDelimiter = (String) properties.get("segmentDelimiter");
            }
            if (properties.get("elementDelimiter") != null) {
                this.elementDelimiter = (String) properties.get("elementDelimiter");
            }
            if (properties.get("subelementDelimiter") != null) {
                this.subelementDelimiter = (String) properties.get("subelementDelimiter");
            }
        }
    }
    
    public String getSegmentDelimiter() {
        return segmentDelimiter;
    }

    public void setSegmentDelimiter(String segmentDelimiter) {
        this.segmentDelimiter = segmentDelimiter;
    }

    public String getElementDelimiter() {
        return elementDelimiter;
    }

    public void setElementDelimiter(String elementDelimiter) {
        this.elementDelimiter = elementDelimiter;
    }

    public String getSubelementDelimiter() {
        return subelementDelimiter;
    }

    public void setSubelementDelimiter(String subelementDelimiter) {
        this.subelementDelimiter = subelementDelimiter;
    }
}
