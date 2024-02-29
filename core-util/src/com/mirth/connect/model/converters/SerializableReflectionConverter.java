/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.converters;

import java.io.Serializable;

import com.mirth.connect.donkey.util.xstream.SerializerException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * Same as the default reflection converter except that it rejects any object that isn't
 * Serializable.
 */
public class SerializableReflectionConverter extends ReflectionConverter {

    public SerializableReflectionConverter(Mapper mapper) {
        super(mapper, JVM.newReflectionProvider());
    }

    @Override
    public void marshal(Object original, HierarchicalStreamWriter writer, MarshallingContext context) {
        if (original == null || original instanceof Serializable) {
            super.marshal(original, writer, context);
        } else {
            throw new SerializerException("Object is not serializable: " + original.getClass().getName() + "@" + Integer.toHexString(original.hashCode()));
        }
    }
}
