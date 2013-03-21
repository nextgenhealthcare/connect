package com.mirth.connect.plugins.datatypes.dicom;

import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.model.datatype.SerializerProperties;

public class DICOMDataTypeDelegate implements DataTypeDelegate {
    
    @Override
    public String getName() {
        return "DICOM";
    }

    @Override
    public IXMLSerializer getSerializer(SerializerProperties properties) {
        return new DICOMSerializer(properties);
    }
    
    @Override
    public boolean isBinary() {
        return true;
    }
}
