/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.hl7v2;

import com.mirth.connect.donkey.model.message.SerializationType;
import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.model.datatype.DataTypeDelegate;
import com.mirth.connect.model.datatype.SerializerProperties;

public class HL7v2DataTypeDelegate implements DataTypeDelegate {

    @Override
    public String getName() {
        return "HL7V2";
    }
    
    @Override
    public IXMLSerializer getSerializer(SerializerProperties properties) {
        return new ER7Serializer(properties);
    }

    @Override
    public boolean isBinary() {
        return false;
    }
    
    @Override
    public SerializationType getSerializationType() {
        return SerializationType.XML;
    }
}
