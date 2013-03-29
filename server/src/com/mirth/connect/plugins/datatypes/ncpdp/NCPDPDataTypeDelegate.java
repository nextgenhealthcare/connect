package com.mirth.connect.plugins.datatypes.ncpdp;

import com.mirth.connect.donkey.model.message.SerializationType;
import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.model.datatype.SerializerProperties;

public class NCPDPDataTypeDelegate implements DataTypeDelegate {

    @Override
    public String getName() {
        return "NCPDP";
    }
    
    @Override
    public IXMLSerializer getSerializer(SerializerProperties properties) {
        return new NCPDPSerializer(properties);
    }

    @Override
    public boolean isBinary() {
        return false;
    }
    
    @Override
    public SerializationType getSerializationType() {
        return SerializationType.XML;
    }
}
