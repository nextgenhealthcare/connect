package com.mirth.connect.plugins.datatypes.hl7v3;

import com.mirth.connect.model.datatype.BatchProperties;
import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.model.datatype.DeserializationProperties;
import com.mirth.connect.model.datatype.ResponseGenerationProperties;
import com.mirth.connect.model.datatype.ResponseValidationProperties;

public class HL7V3DataTypeProperties extends DataTypeProperties {
    
    private HL7V3SerializationProperties serializationProperties;
    
    public HL7V3DataTypeProperties() {
        serializationProperties = new HL7V3SerializationProperties();
    }

    @Override
    public HL7V3SerializationProperties getSerializationProperties() {
        return serializationProperties;
    }

    @Override
    public DeserializationProperties getDeserializationProperties() {
        return null;
    }

    @Override
    public BatchProperties getBatchProperties() {
        return null;
    }

    @Override
    public ResponseGenerationProperties getResponseGenerationProperties() {
        return null;
    }

    @Override
    public ResponseValidationProperties getResponseValidationProperties() {
        return null;
    }

}
