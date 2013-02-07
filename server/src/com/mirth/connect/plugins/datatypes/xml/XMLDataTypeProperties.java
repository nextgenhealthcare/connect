package com.mirth.connect.plugins.datatypes.xml;

import com.mirth.connect.model.datatype.DataTypeProperties;

public class XMLDataTypeProperties extends DataTypeProperties {

    public XMLDataTypeProperties() {
        serializationProperties = new XMLSerializationProperties();
    }

}
