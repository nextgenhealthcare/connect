package com.mirth.connect.donkey.util.xstream;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * The purpose of this xstream converter is to ensure that when the same HashMap is serialized
 * multiple times, the order of the items in the map remains consistent in the serialized output.
 */
public class HashMapConverter extends MapConverter {
    public HashMapConverter(Mapper mapper) {
        super(mapper);
    }

    @Override
    public boolean canConvert(Class clazz) {
        return (clazz.equals(HashMap.class));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext context) {
        super.marshal(new TreeMap<Object, Object>((Map<Object, Object>) object), writer, context);
    }
}
