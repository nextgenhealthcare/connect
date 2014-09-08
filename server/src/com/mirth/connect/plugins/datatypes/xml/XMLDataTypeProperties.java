/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.xml;

import java.util.HashMap;
import java.util.Map;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.model.datatype.DataTypeProperties;

public class XMLDataTypeProperties extends DataTypeProperties {

    public XMLDataTypeProperties() {
        serializationProperties = new XMLSerializationProperties();
        batchProperties = new XMLBatchProperties();
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {
        DonkeyElement batchElement = element.addChildElement("batchProperties");

        batchElement.setAttribute("class", "com.mirth.connect.plugins.datatypes.xml.XMLBatchProperties");
        batchElement.addChildElement("splitType", "Element_Name");
        batchElement.addChildElement("elementName");
        batchElement.addChildElement("level", "0");
        batchElement.addChildElement("query");
        batchElement.addChildElement("batchScript");
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("serializationProperties", serializationProperties.getPurgedProperties());
        purgedProperties.put("batchProperties", batchProperties.getPurgedProperties());
        return purgedProperties;
    }
}
