package com.mirth.connect.plugins.datatypes.xml;

import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.model.datatype.SerializerProperties;

public class XMLDataTypeDelegate implements DataTypeDelegate {
    
    @Override
    public String getName() {
        return "XML";
    }
    
    @Override
    public IXMLSerializer getSerializer(SerializerProperties properties) {
        return new DefaultXMLSerializer(properties);
    }

    @Override
    public boolean isBinary() {
        return false;
    }
}
