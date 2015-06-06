package com.mirth.connect.plugins.datatypes.json;

import com.mirth.connect.donkey.model.message.SerializationType;
import com.mirth.connect.model.converters.IMessageSerializer;
import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.model.datatype.DataTypeProperties;
import com.mirth.connect.model.datatype.SerializerProperties;

public class JSONDataTypeDelegate implements DataTypeDelegate {

    @Override
    public String getName() {
        return "JSON";
    }

    @Override
    public IMessageSerializer getSerializer(SerializerProperties properties) {
        return new JSONSerializer(properties);
    }

    @Override
    public boolean isBinary() {
        return false;
    }

    @Override
    public SerializationType getSerializationType() {
        return SerializationType.JSON;
    }

    @Override
    public DataTypeProperties getDefaultProperties() {
        return new JSONDataTypeProperties();
    }

}
