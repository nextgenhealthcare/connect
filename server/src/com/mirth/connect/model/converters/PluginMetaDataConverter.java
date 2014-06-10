/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.converters;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.model.PluginMetaData;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.DocumentReader;
import com.thoughtworks.xstream.mapper.Mapper;

public class PluginMetaDataConverter extends ReflectionConverter {

    public PluginMetaDataConverter(Mapper mapper) {
        super(mapper, JVM.newReflectionProvider());
    }

    @Override
    public boolean canConvert(Class type) {
        return type == PluginMetaData.class;
    }

    @Override
    public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
        super.marshal(value, writer, context);
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        DonkeyElement pluginMetaDataElement = new DonkeyElement((Element) ((DocumentReader) reader).getCurrent());

        DonkeyElement serverClassesElement = pluginMetaDataElement.getChildElement("serverClasses");

        if (serverClassesElement != null) {
            for (DonkeyElement childElement : serverClassesElement.getChildElements()) {
                if (childElement.getNodeName().equals("string")) {
                    convertStringToPluginClass(childElement);
                }
            }
        }

        DonkeyElement clientClassesElement = pluginMetaDataElement.getChildElement("clientClasses");

        if (clientClassesElement != null) {
            for (DonkeyElement childElement : clientClassesElement.getChildElements()) {
                if (childElement.getNodeName().equals("string")) {
                    convertStringToPluginClass(childElement);
                }
            }
        }

        return super.unmarshal(reader, context);
    }

    private void convertStringToPluginClass(DonkeyElement stringElement) {
        // Extract class metadata
        String className = stringElement.getTextContent();
        String weight = stringElement.getAttribute("weight");

        // Clear node attributes and value
        stringElement.setTextContent(null);
        stringElement.removeAttribute("weight");

        // Update node name
        stringElement.setNodeName("pluginClass");
        // Add the plugin class name
        stringElement.addChildElement("name", className);
        // Add the plugin weight if it exists.
        if (StringUtils.isNotBlank(weight)) {
            stringElement.addChildElement("weight", weight);
        }
    }
}
