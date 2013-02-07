package com.mirth.connect.plugins.datatypes.ncpdp;

import com.mirth.connect.model.datatype.DataTypeProperties;

public class NCPDPDataTypeProperties extends DataTypeProperties {
    
    public NCPDPDataTypeProperties() {
        serializationProperties = new NCPDPSerializationProperties();
        deserializationProperties = new NCPDPDeserializationProperties();
    }

}
