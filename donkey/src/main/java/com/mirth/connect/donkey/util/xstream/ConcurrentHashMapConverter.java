/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

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
