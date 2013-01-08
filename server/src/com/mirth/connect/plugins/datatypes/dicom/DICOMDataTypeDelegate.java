package com.mirth.connect.plugins.datatypes.dicom;

import java.util.Map;

import com.mirth.connect.model.DataTypeDelegate;
import com.mirth.connect.model.converters.IXMLSerializer;

public class DICOMDataTypeDelegate implements DataTypeDelegate {

    @Override
    public IXMLSerializer getSerializer(Map<?, ?> properties) {
        return new DICOMSerializer(properties);
    }

    @Override
    public Map<String, String> getDefaultProperties() {
        return DICOMSerializer.getDefaultProperties();
    }
    
    @Override
    public boolean isBinary() {
        return true;
    }

    @Override
    public boolean isXml() {
        return false;
    }
}
