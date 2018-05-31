/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.edi;

import java.util.HashMap;
import java.util.Map;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.model.datatype.DataTypeProperties;

public class EDIDataTypeProperties extends DataTypeProperties {

    public EDIDataTypeProperties() {
        serializationProperties = new EDISerializationProperties();
        batchProperties = new EDIBatchProperties();
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {
        DonkeyElement batchElement = element.addChildElementIfNotExists("batchProperties");

        batchElement.setAttribute("class", "com.mirth.connect.plugins.datatypes.edi.EDIBatchProperties");
        batchElement.addChildElementIfNotExists("splitType", "JavaScript");
        batchElement.addChildElementIfNotExists("batchScript");
    }

    @Override
    public void migrate3_2_0(DonkeyElement element) {}

    @Override
    public void migrate3_3_0(DonkeyElement element) {}

    @Override
    public void migrate3_4_0(DonkeyElement element) {}

    @Override
    public void migrate3_5_0(DonkeyElement element) {}

    @Override
    public void migrate3_6_0(DonkeyElement element) {}
    
    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("serializationProperties", serializationProperties.getPurgedProperties());
        purgedProperties.put("batchProperties", batchProperties.getPurgedProperties());
        return purgedProperties;
    }
}
