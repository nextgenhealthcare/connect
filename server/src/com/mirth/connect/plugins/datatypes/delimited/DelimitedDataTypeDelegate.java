package com.mirth.connect.plugins.datatypes.delimited;

import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.model.datatype.SerializerProperties;

public class DelimitedDataTypeDelegate implements DataTypeDelegate {
    
    @Override
    public String getName() {
        return "DELIMITED";
    }
    
    @Override
    public IXMLSerializer getSerializer(SerializerProperties properties) {
        return new DelimitedSerializer(properties);
    }

    @Override
    public boolean isBinary() {
        return false;
    }

    @Override
    public boolean isXml() {
        return false;
    }
}
