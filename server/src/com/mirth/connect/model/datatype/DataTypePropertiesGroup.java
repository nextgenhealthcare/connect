/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.datatype;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public abstract class DataTypePropertiesGroup implements Serializable {

    public abstract Map<String, DataTypePropertyDescriptor> getPropertyDescriptors();

    public final Map<String, Object> getProperties() {
        Map<String, Object> propertiesMap = new HashMap<String, Object>();
        Map<String, DataTypePropertyDescriptor> properties = getPropertyDescriptors();
        for (String key : properties.keySet()) {
            propertiesMap.put(key, properties.get(key).getValue());
        }
        return propertiesMap;
    }

    public abstract void setProperties(Map<String, Object> properties);

    @Override
    public boolean equals(Object object) {
        boolean isEqual = true;

        if (object instanceof DataTypePropertiesGroup) {
            DataTypePropertiesGroup propertiesGroup = (DataTypePropertiesGroup) object;
            Map<String, DataTypePropertyDescriptor> descriptorMap = propertiesGroup.getPropertyDescriptors();

            for (Entry<String, DataTypePropertyDescriptor> entry : getPropertyDescriptors().entrySet()) {
                if (!descriptorMap.containsKey(entry.getKey()) || !entry.getValue().equals(descriptorMap.get(entry.getKey()))) {
                    isEqual = false;
                }
            }
        } else {
            isEqual = false;
        }

        return isEqual;
    }
}
