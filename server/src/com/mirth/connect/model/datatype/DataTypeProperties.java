package com.mirth.connect.model.datatype;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.SerializationUtils;

public abstract class DataTypeProperties implements Serializable {
	
	protected SerializationProperties serializationProperties;
	protected DeserializationProperties deserializationProperties;
	protected BatchProperties batchProperties;
	protected ResponseGenerationProperties responseGenerationProperties;
	protected ResponseValidationProperties responseValidationProperties;
    
    public SerializerProperties getSerializerProperties() {
        return new SerializerProperties(getSerializationProperties(), getDeserializationProperties(), getBatchProperties());
    }
    
    public SerializationProperties getSerializationProperties() {
    	return serializationProperties;
    }
    
    public DeserializationProperties getDeserializationProperties() {
    	return deserializationProperties;
    }
    
    public BatchProperties getBatchProperties() {
    	return batchProperties;
    }
    
    public ResponseGenerationProperties getResponseGenerationProperties() {
    	return responseGenerationProperties;
    }
    
    public ResponseValidationProperties getResponseValidationProperties() {
    	return responseValidationProperties;
    }
    
    public DataTypeProperties clone() {
    	return SerializationUtils.clone(this);
    }
}
