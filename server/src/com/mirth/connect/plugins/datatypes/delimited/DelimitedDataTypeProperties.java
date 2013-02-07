package com.mirth.connect.plugins.datatypes.delimited;

import com.mirth.connect.model.datatype.DataTypeProperties;

public class DelimitedDataTypeProperties extends DataTypeProperties {
    
    public DelimitedDataTypeProperties() {
        serializationProperties = new DelimitedSerializationProperties();
        deserializationProperties = new DelimitedDeserializationProperties();
        batchProperties = new DelimitedBatchProperties();
    }

}
