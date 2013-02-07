package com.mirth.connect.plugins.datatypes.hl7v3;

import com.mirth.connect.model.datatype.DataTypeProperties;

public class HL7V3DataTypeProperties extends DataTypeProperties {
    
    public HL7V3DataTypeProperties() {
        serializationProperties = new HL7V3SerializationProperties();
    }

}
