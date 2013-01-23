package com.mirth.connect.plugins.datatypes.hl7v2;

import com.mirth.connect.model.datatype.BatchProperties;
import com.mirth.connect.model.datatype.DataTypeProperties;

public class HL7v2DataTypeProperties extends DataTypeProperties {
    
    private HL7v2SerializationProperties serializationProperties;
    private HL7v2DeserializationProperties deserializationProperties;
    private HL7v2ResponseGenerationProperties responseGenerationProperties;
    private HL7v2ResponseValidationProperties responseValidationProperties;
    
    public HL7v2DataTypeProperties() {
        serializationProperties = new HL7v2SerializationProperties();
        deserializationProperties = new HL7v2DeserializationProperties();
        responseGenerationProperties = new HL7v2ResponseGenerationProperties();
        responseValidationProperties = new HL7v2ResponseValidationProperties();
    }
    
    @Override
    public HL7v2SerializationProperties getSerializationProperties() {
        return serializationProperties;
    }

    @Override
    public HL7v2DeserializationProperties getDeserializationProperties() {
        return deserializationProperties;
    }

    @Override
    public BatchProperties getBatchProperties() {
        return null;
    }

    @Override
    public HL7v2ResponseGenerationProperties getResponseGenerationProperties() {
        return responseGenerationProperties;
    }

    @Override
    public HL7v2ResponseValidationProperties getResponseValidationProperties() {
        return responseValidationProperties;
    }
    
}
