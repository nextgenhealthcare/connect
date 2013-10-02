package com.thoughtworks.xstream.converters.collections;

import java.util.concurrent.ConcurrentHashMap;

import com.thoughtworks.xstream.mapper.Mapper;

public class ConcurrentHashMapConverter extends MapConverter {

    public ConcurrentHashMapConverter(Mapper mapper) {
        super(mapper);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean canConvert(Class clazz) {
        return clazz.equals(ConcurrentHashMap.class);
    }
}
