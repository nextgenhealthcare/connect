package com.mirth.connect.plugins.datatypes.ncpdp;

import com.mirth.connect.model.datatype.BatchProperties;
import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.model.datatype.ResponseGenerationProperties;
import com.mirth.connect.model.datatype.ResponseValidationProperties;

public class NCPDPDataTypeProperties extends DataTypeProperties {
    
    private NCPDPSerializationProperties serializationProperties;
    private NCPDPDeserializationProperties deserializationProperties;
    
    public NCPDPDataTypeProperties() {
        serializationProperties = new NCPDPSerializationProperties();
        deserializationProperties = new NCPDPDeserializationProperties();
    }

    @Override
    public NCPDPSerializationProperties getSerializationProperties() {
        return serializationProperties;
    }

    @Override
    public NCPDPDeserializationProperties getDeserializationProperties() {
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
