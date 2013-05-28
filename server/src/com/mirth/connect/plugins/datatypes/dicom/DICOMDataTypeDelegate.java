/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.dicom;

import com.mirth.connect.donkey.model.message.SerializationType;
import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.model.datatype.DataTypeProperties;
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

    @Override
    public SerializationType getSerializationType() {
        return SerializationType.XML;
    }

    @Override
    public DataTypeProperties getDefaultProperties() {
        return new DICOMDataTypeProperties();
    }
}
