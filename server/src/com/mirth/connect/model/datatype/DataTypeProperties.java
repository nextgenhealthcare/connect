package com.mirth.connect.model.datatype;

import java.io.Serializable;

public abstract class DataTypeProperties implements Serializable {
    
    public SerializerProperties getSerializerProperties() {
        return new SerializerProperties(getSerializationProperties(), getDeserializationProperties(), getBatchProperties());
    }
    
    public abstract SerializationProperties getSerializationProperties();
    
    public abstract DeserializationProperties getDeserializationProperties();
    
    public abstract BatchProperties getBatchProperties();
    
    public abstract ResponseGenerationProperties getResponseGenerationProperties();
    
    public abstract ResponseValidationProperties getResponseValidationProperties();
}
