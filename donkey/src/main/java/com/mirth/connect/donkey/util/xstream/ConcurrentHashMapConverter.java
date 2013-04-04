package com.mirth.connect.donkey.util.xstream;

import java.util.concurrent.ConcurrentHashMap;

import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.mapper.Mapper;

public class ConcurrentHashMapConverter extends MapConverter {
    public ConcurrentHashMapConverter(Mapper mapper) {
        super(mapper);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean canConvert(Class clazz) {
        if (clazz.equals(ConcurrentHashMap.class)) {
            return true;
        }

        return false;
    }
}
