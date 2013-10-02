/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.xml;

import java.util.LinkedHashMap;
import java.util.Map;

import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;
import com.mirth.connect.model.datatype.PropertyEditorType;
import com.mirth.connect.model.datatype.SerializationProperties;

public class XMLSerializationProperties extends SerializationProperties {

    private boolean stripNamespaces = true;

    @Override
    public Map<String, DataTypePropertyDescriptor> getPropertyDescriptors() {
        Map<String, DataTypePropertyDescriptor> properties = new LinkedHashMap<String, DataTypePropertyDescriptor>();

        properties.put("stripNamespaces", new DataTypePropertyDescriptor(stripNamespaces, "Strip Namespaces", "Strips namespace definitions from the transformed XML message.  Will not remove namespace prefixes.  If you do not strip namespaces your default xml namespace will be set to the incoming data namespace.  If your outbound template namespace is different, you will have to set \"default xml namespace = 'namespace';\" via JavaScript before template mappings.", PropertyEditorType.BOOLEAN));

        return properties;
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        if (properties != null) {
            if (properties.get("stripNamespaces") != null) {
                this.stripNamespaces = (Boolean) properties.get("stripNamespaces");
            }
        }
    }

    public boolean isStripNamespaces() {
        return stripNamespaces;
    }

    public void setStripNamespaces(boolean stripNamespaces) {
        this.stripNamespaces = stripNamespaces;
    }

}
