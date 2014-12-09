/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.delimited;

import java.util.HashMap;
import java.util.Map;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.model.datatype.DataTypeProperties;

public class DelimitedDataTypeProperties extends DataTypeProperties {

    public DelimitedDataTypeProperties() {
        serializationProperties = new DelimitedSerializationProperties();
        deserializationProperties = new DelimitedDeserializationProperties();
        batchProperties = new DelimitedBatchProperties();
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {}

    @Override
    public void migrate3_2_0(DonkeyElement element) {}

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("serializationProperties", serializationProperties.getPurgedProperties());
        purgedProperties.put("deserializationProperties", deserializationProperties.getPurgedProperties());
        purgedProperties.put("batchProperties", batchProperties.getPurgedProperties());
        return purgedProperties;
    }
}
