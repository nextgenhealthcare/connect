package com.thoughtworks.xstream.converters.collections;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class PropertiesConverter implements Converter {
    private class KeyComparator implements Comparator<Object> {
        @Override
        public int compare(Object obj1, Object obj2) {
            return (obj1.toString()).compareTo(obj2.toString());
        }
    }

    @SuppressWarnings("rawtypes")
    public boolean canConvert(Class clazz) {
        return clazz.equals(Properties.class);
    }

    /**
     * Sorts the Properties set by key and converts it to XML.
     */
    public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
        Properties properties = (Properties) value;

        List<Object> keys = Collections.list(properties.keys());
        Collections.sort(keys, new KeyComparator());

        for (Object key : keys) {
            writer.startNode("property");
            writer.addAttribute("name", key.toString());
            writer.setValue(properties.getProperty(key.toString()));
            writer.endNode();
        }
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Properties properties = new Properties();

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            properties.setProperty(reader.getAttribute("name"), reader.getValue());
            reader.moveUp();
        }

        return properties;
    }
}
