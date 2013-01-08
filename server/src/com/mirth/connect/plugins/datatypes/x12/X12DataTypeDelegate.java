package com.mirth.connect.plugins.datatypes.x12;

import java.util.Map;

import com.mirth.connect.model.DataTypeDelegate;
import com.mirth.connect.model.converters.IXMLSerializer;

public class X12DataTypeDelegate implements DataTypeDelegate {

    @Override
    public IXMLSerializer getSerializer(Map<?, ?> properties) {
        return new X12Serializer(properties);
    }

    @Override
    public Map<String, String> getDefaultProperties() {
        return X12Serializer.getDefaultProperties();
    }

    @Override
    public boolean isXml() {
        return false;
    }

    @Override
    public boolean isBinary() {
        return false;
    }
}
