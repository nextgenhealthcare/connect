/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.delimited;

import com.mirth.connect.model.datatype.DataTypeProperties;

public class DelimitedDataTypeProperties extends DataTypeProperties {
    
    public DelimitedDataTypeProperties() {
        serializationProperties = new DelimitedSerializationProperties();
        deserializationProperties = new DelimitedDeserializationProperties();
        batchProperties = new DelimitedBatchProperties();
    }

}
