package com.mirth.connect.plugins.datatypes.xml;

import com.mirth.connect.model.datatype.BatchProperties;
import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.model.datatype.DeserializationProperties;
import com.mirth.connect.model.datatype.ResponseGenerationProperties;
import com.mirth.connect.model.datatype.ResponseValidationProperties;

public class XMLDataTypeProperties extends DataTypeProperties {

    private XMLSerializationProperties serializationProperties;
    
    public XMLDataTypeProperties() {
        serializationProperties = new XMLSerializationProperties();
    }

    @Override
    public XMLSerializationProperties getSerializationProperties() {
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
