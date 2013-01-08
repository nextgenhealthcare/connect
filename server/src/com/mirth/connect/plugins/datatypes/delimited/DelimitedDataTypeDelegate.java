package com.mirth.connect.plugins.datatypes.delimited;

import java.util.Map;

import com.mirth.connect.model.DataTypeDelegate;
import com.mirth.connect.model.converters.IXMLSerializer;

public class DelimitedDataTypeDelegate implements DataTypeDelegate {
    
    @Override
    public String getName() {
        return "DELIMITED";
    }
    
    @Override
    public IXMLSerializer getSerializer(Map<?, ?> properties) {
        return new DelimitedSerializer(properties);
    }

    @Override
    public Map<String, String> getDefaultProperties() {
        return DelimitedSerializer.getDefaultProperties();
    }

    @Override
    public boolean isBinary() {
        return false;
    }

    @Override
    public boolean isXml() {
        return false;
    }
}
