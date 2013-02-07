package com.mirth.connect.plugins.datatypes.hl7v2;

import com.mirth.connect.model.datatype.DataTypeProperties;

public class HL7v2DataTypeProperties extends DataTypeProperties {
    
    public HL7v2DataTypeProperties() {
        serializationProperties = new HL7v2SerializationProperties();
        deserializationProperties = new HL7v2DeserializationProperties();
        responseGenerationProperties = new HL7v2ResponseGenerationProperties();
        responseValidationProperties = new HL7v2ResponseValidationProperties();
    }
    
}
