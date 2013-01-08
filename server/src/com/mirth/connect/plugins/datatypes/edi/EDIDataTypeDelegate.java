package com.mirth.connect.plugins.datatypes.edi;

import java.util.Map;

import com.mirth.connect.model.DataTypeDelegate;
import com.mirth.connect.model.converters.IXMLSerializer;

public class EDIDataTypeDelegate implements DataTypeDelegate {
    @Override
    public IXMLSerializer getSerializer(Map<?, ?> properties) {
        return new EDISerializer(properties);
    }

    @Override
    public Map<String, String> getDefaultProperties() {
        return EDISerializer.getDefaultProperties();
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
