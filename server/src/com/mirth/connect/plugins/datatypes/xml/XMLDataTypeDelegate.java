package com.mirth.connect.plugins.datatypes.xml;

import java.util.Map;

import com.mirth.connect.model.DataTypeDelegate;
import com.mirth.connect.model.converters.IXMLSerializer;

public class XMLDataTypeDelegate implements DataTypeDelegate {
    
    @Override
    public IXMLSerializer getSerializer(Map<?, ?> properties) {
        return new DefaultXMLSerializer(properties);
    }

    @Override
    public Map<String, String> getDefaultProperties() {
        return DefaultXMLSerializer.getDefaultProperties();
    }

    @Override
    public boolean isXml() {
        return true;
    }

    @Override
    public boolean isBinary() {
        return false;
    }
}
