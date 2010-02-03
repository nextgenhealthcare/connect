/*
 * Copyright (C) 2003, 2004, 2005 Joe Walnes.
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 */

package com.thoughtworks.xstream.converters.collections;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.core.util.Fields;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Special converter for java.util.Properties that stores
 * properties in a more compact form than java.util.Map.
 * <p/>
 * <p>Because all entries of a Properties instance
 * are Strings, a single element is used for each property
 * with two attributes; one for key and one for value.</p>
 * <p>Additionally, default properties are also serialized, if they are present.</p>
 *
 * @author Joe Walnes
 * @author Kevin Ring
 */
public class PropertiesConverter implements Converter {

    private final static Field defaultsField = Fields.find(Properties.class, "defaults");

    public boolean canConvert(Class type) {
        return Properties.class == type;
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Properties properties = (Properties) source;
        for (Iterator iterator = properties.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            writer.startNode("property");
            writer.addAttribute("name", entry.getKey().toString());
          //  writer.addAttribute("value", entry.getValue().toString());
            writer.setValue(entry.getValue().toString());
            writer.endNode();
        }
        Properties defaults = (Properties) Fields.read(defaultsField, properties);
        if (defaults != null) {
            writer.startNode("defaults");
            marshal(defaults, writer, context);
            writer.endNode();
        }
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Properties properties = new Properties();
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            if (reader.getNodeName().equals("defaults")) {
                Properties defaults = (Properties) unmarshal(reader, context);
                Fields.write(defaultsField, properties, defaults);
            } else {
                String name = reader.getAttribute("name");
                String value = reader.getAttribute("value");
                
                if ((value == null) || (value.length() == 0)){
                	value = reader.getValue();
                }
                
                properties.setProperty(name, value);
            }
            reader.moveUp();
        }
        return properties;
    }

}
