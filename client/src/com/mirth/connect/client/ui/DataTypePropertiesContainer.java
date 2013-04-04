package com.mirth.connect.client.ui;

import com.mirth.connect.model.datatype.DataTypeProperties;

public class DataTypePropertiesContainer {
    private DataTypeProperties properties;
    private TransformerType type;

    public DataTypePropertiesContainer(DataTypeProperties properties, TransformerType type) {
        this.properties = properties;
        this.type = type;
    }

    public DataTypeProperties getProperties() {
        return properties;
    }

    public void setProperties(DataTypeProperties properties) {
        this.properties = properties;
    }

    public TransformerType getType() {
        return type;
    }

    public void setType(TransformerType type) {
        this.type = type;
    }
}
