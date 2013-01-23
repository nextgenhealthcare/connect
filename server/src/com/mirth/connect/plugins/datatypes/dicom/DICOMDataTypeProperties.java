package com.mirth.connect.plugins.datatypes.dicom;

import com.mirth.connect.model.datatype.BatchProperties;
import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.model.datatype.DeserializationProperties;
import com.mirth.connect.model.datatype.ResponseGenerationProperties;
import com.mirth.connect.model.datatype.ResponseValidationProperties;
import com.mirth.connect.model.datatype.SerializationProperties;

public class DICOMDataTypeProperties extends DataTypeProperties {

    @Override
    public SerializationProperties getSerializationProperties() {
        return null;
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
