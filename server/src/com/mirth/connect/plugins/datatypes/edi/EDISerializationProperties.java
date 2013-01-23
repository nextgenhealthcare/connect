package com.mirth.connect.plugins.datatypes.edi;

import java.util.LinkedHashMap;
import java.util.Map;

import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;
import com.mirth.connect.model.datatype.PropertyEditorType;
import com.mirth.connect.model.datatype.SerializationProperties;

public class EDISerializationProperties implements SerializationProperties {
    private String segmentDelimiter = "~";
    private String elementDelimiter = "*";
    private String subelementDelimiter = ":";
    private boolean inferX12Delimiters = true;
    
    public EDISerializationProperties() {
        
    }
    
    public EDISerializationProperties(EDISerializationProperties properties) {
        this.segmentDelimiter = properties.getSegmentDelimiter();
        this.elementDelimiter = properties.getElementDelimiter();
        this.subelementDelimiter = properties.getSubelementDelimiter();
        this.inferX12Delimiters = properties.isInferX12Delimiters();
    }
    
    @Override
    public Map<String, DataTypePropertyDescriptor> getProperties() {
        Map<String, DataTypePropertyDescriptor> properties = new LinkedHashMap<String, DataTypePropertyDescriptor>();
        
        properties.put("elementDelimiter", new DataTypePropertyDescriptor(elementDelimiter, "Element Delimiter", "Characters that delimit the segments in the message.", PropertyEditorType.STRING));
        properties.put("segmentDelimiter", new DataTypePropertyDescriptor(segmentDelimiter, "Segment Delimiter", "Characters that delimit the elements in the message.", PropertyEditorType.STRING));
        properties.put("subelementDelimiter", new DataTypePropertyDescriptor(subelementDelimiter, "Subelement Delimiter", "Characters that delimit the subelements in the message.", PropertyEditorType.STRING));
        properties.put("inferX12Delimiters", new DataTypePropertyDescriptor(inferX12Delimiters, "Infer X12 Delimiters", "Infer the standard X12 delimiters", PropertyEditorType.BOOLEAN));
        
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
            if (properties.get("inferX12Delimiters") != null) {
                this.inferX12Delimiters = ((Boolean) properties.get("inferX12Delimiters"));
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

    public boolean isInferX12Delimiters() {
        return inferX12Delimiters;
    }

    public void setInferX12Delimiters(boolean inferX12Delimiters) {
        this.inferX12Delimiters = inferX12Delimiters;
    }
}
