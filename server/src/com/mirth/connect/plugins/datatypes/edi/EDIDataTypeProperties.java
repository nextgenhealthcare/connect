package com.mirth.connect.plugins.datatypes.edi;

import com.mirth.connect.model.datatype.BatchProperties;
import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.model.datatype.ResponseGenerationProperties;
import com.mirth.connect.model.datatype.ResponseValidationProperties;

public class EDIDataTypeProperties extends DataTypeProperties {

    private EDISerializationProperties serializationProperties;
    private EDIDeserializationProperties deserializationProperties;
    
    public EDIDataTypeProperties() {
        serializationProperties = new EDISerializationProperties();
        deserializationProperties = new EDIDeserializationProperties();
    }
    
    @Override
    public EDISerializationProperties getSerializationProperties() {
        return serializationProperties;
    }

    @Override
    public EDIDeserializationProperties getDeserializationProperties() {
        return deserializationProperties;
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
