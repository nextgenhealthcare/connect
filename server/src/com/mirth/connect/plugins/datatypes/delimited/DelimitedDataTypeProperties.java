package com.mirth.connect.plugins.datatypes.delimited;

import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.model.datatype.ResponseGenerationProperties;
import com.mirth.connect.model.datatype.ResponseValidationProperties;

public class DelimitedDataTypeProperties extends DataTypeProperties {
    
    private DelimitedSerializationProperties serializationProperties;
    private DelimitedDeserializationProperties deserializationProperties;
    private DelimitedBatchProperties batchProperties;
    
    public DelimitedDataTypeProperties() {
        serializationProperties = new DelimitedSerializationProperties();
        deserializationProperties = new DelimitedDeserializationProperties();
        batchProperties = new DelimitedBatchProperties();
    }
    
    
    @Override
    public DelimitedSerializationProperties getSerializationProperties() {
        return serializationProperties;
    }

    @Override
    public DelimitedDeserializationProperties getDeserializationProperties() {
        return deserializationProperties;
    }

    @Override
    public DelimitedBatchProperties getBatchProperties() {
        return batchProperties;
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
