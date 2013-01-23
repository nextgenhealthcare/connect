package com.mirth.connect.plugins.datatypes.hl7v2;

import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.model.datatype.SerializerProperties;

public class HL7v2DataTypeDelegate implements DataTypeDelegate {

    @Override
    public String getName() {
        return "HL7V2";
    }
    
    @Override
    public IXMLSerializer getSerializer(SerializerProperties properties) {
        return new ER7Serializer(properties);
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
