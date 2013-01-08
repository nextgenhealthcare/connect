package com.mirth.connect.plugins.datatypes.hl7v2;

import java.util.Map;

import com.mirth.connect.model.DataTypeDelegate;
import com.mirth.connect.model.converters.IXMLSerializer;

public class HL7v2DataTypeDelegate implements DataTypeDelegate {

    @Override
    public String getName() {
        return "HL7V2";
    }
    
    @Override
    public IXMLSerializer getSerializer(Map<?, ?> properties) {
        return new ER7Serializer(properties);
    }

    @Override
    public Map<String, String> getDefaultProperties() {
        return ER7Serializer.getDefaultProperties();
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
