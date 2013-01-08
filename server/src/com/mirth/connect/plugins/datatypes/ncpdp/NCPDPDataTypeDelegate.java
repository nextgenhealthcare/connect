package com.mirth.connect.plugins.datatypes.ncpdp;

import java.util.Map;

import com.mirth.connect.model.DataTypeDelegate;
import com.mirth.connect.model.converters.IXMLSerializer;

public class NCPDPDataTypeDelegate implements DataTypeDelegate {

    @Override
    public IXMLSerializer getSerializer(Map<?, ?> properties) {
        return new NCPDPSerializer(properties);
    }

    @Override
    public Map<String, String> getDefaultProperties() {
        return NCPDPSerializer.getDefaultProperties();
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
