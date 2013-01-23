package com.mirth.connect.plugins.datatypes.edi;

import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.model.datatype.SerializerProperties;

public class EDIDataTypeDelegate implements DataTypeDelegate {
    
    @Override
    public String getName() {
        return "EDI/X12";
    }
    
    @Override
    public IXMLSerializer getSerializer(SerializerProperties properties) {
        return new EDISerializer(properties);
    }

    @Override
    public boolean isXml() {
        return false;
    }

    @Override
    public boolean isBinary() {
        return false;
    }
}
