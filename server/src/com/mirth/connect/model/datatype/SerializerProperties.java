package com.mirth.connect.model.datatype;

import java.io.Serializable;

public class SerializerProperties implements Serializable {

    private SerializationProperties serializationProperties;
    private DeserializationProperties deserializationProperties;
    private BatchProperties batchProperties;

    public SerializerProperties(SerializationProperties serializationProperties, DeserializationProperties deserializationProperties, BatchProperties batchProperties) {
        this.serializationProperties = serializationProperties;
        this.deserializationProperties = deserializationProperties;
        this.batchProperties = batchProperties;
    }

    public SerializationProperties getSerializationProperties() {
        return serializationProperties;
    }

    public void setSerializationProperties(SerializationProperties serializationProperties) {
        this.serializationProperties = serializationProperties;
    }

    public DeserializationProperties getDeserializationProperties() {
        return deserializationProperties;
    }

    public void setDeserializationProperties(DeserializationProperties deserializationProperties) {
        this.deserializationProperties = deserializationProperties;
    }

    public BatchProperties getBatchProperties() {
        return batchProperties;
    }

    public void setBatchProperties(BatchProperties batchProperties) {
        this.batchProperties = batchProperties;
    }


}
