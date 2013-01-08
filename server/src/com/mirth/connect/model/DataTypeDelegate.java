package com.mirth.connect.model;

import java.util.Map;

import com.mirth.connect.model.converters.IXMLSerializer;

public interface DataTypeDelegate {
    
    /**
     * Get an instance of the data type's serializer with the given properties
     */
    public IXMLSerializer getSerializer(Map<?, ?> properties);
    
    /**
     * Get the default properties of the data type
     */
    public Map<String, String> getDefaultProperties();
    
    /** 
     * Indicates if the data type is in XML format
     */
    public boolean isXml();
    
    /**
     * Indicates if the data type is in binary format
     */
    public boolean isBinary();
}
