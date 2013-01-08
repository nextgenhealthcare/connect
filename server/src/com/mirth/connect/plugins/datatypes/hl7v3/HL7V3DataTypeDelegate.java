package com.mirth.connect.plugins.datatypes.hl7v3;

import java.util.Map;

import com.mirth.connect.model.DataTypeDelegate;
import com.mirth.connect.model.converters.IXMLSerializer;

public class HL7V3DataTypeDelegate implements DataTypeDelegate {

    @Override
    public IXMLSerializer getSerializer(Map<?, ?> properties) {
        return new HL7V3Serializer(properties);
    }

    @Override
    public Map<String, String> getDefaultProperties() {
        return HL7V3Serializer.getDefaultProperties();
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
