package com.mirth.connect.plugins.datatypes.edi;

import com.mirth.connect.model.datatype.DataTypeProperties;

public class EDIDataTypeProperties extends DataTypeProperties {

    public EDIDataTypeProperties() {
        serializationProperties = new EDISerializationProperties();
        deserializationProperties = new EDIDeserializationProperties();
    }
    
}
