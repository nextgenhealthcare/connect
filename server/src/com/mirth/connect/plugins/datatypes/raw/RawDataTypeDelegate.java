package com.mirth.connect.plugins.datatypes.raw;

import com.mirth.connect.donkey.model.message.SerializationType;
import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.model.datatype.SerializerProperties;

public class RawDataTypeDelegate implements DataTypeDelegate {
    
    @Override
    public String getName() {
        return "RAW";
    }
    
    @Override
    public IXMLSerializer getSerializer(SerializerProperties properties) {
        return new RawSerializer(properties);
    }

    @Override
    public boolean isBinary() {
        return false;
    }
    
    @Override
    public SerializationType getSerializationType() {
        return SerializationType.RAW;
    }
}
