package com.mirth.connect.model.datatype;

import com.mirth.connect.model.converters.IXMLSerializer;

public interface DataTypeDelegate {
    
    /**
     * Get the name of the data type
     */
    public String getName();
    
    /**
     * Get an instance of the data type's serializer with the given properties
     */
    public IXMLSerializer getSerializer(SerializerProperties properties);
    
    /** 
     * Indicates if the data type is in XML format
     */
    public boolean isXml();
    
    /**
     * Indicates if the data type is in binary format
     */
    public boolean isBinary();
}
