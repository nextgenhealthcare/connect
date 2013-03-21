package com.mirth.connect.plugins.datatypes.hl7v3;

import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.model.datatype.SerializerProperties;

public class HL7V3DataTypeDelegate implements DataTypeDelegate {

    @Override
    public String getName() {
        return "HL7V3";
    }
    
    @Override
    public IXMLSerializer getSerializer(SerializerProperties properties) {
        return new HL7V3Serializer(properties);
    }

    @Override
    public boolean isBinary() {
        return false;
    }
}
