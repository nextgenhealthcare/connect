/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.converters;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeBoolean;
import org.mozilla.javascript.NativeDate;
import org.mozilla.javascript.NativeJSON;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

public class JavaScriptObjectConverter extends ReflectionConverter {

    public JavaScriptObjectConverter(Mapper mapper) {
        super(mapper, JVM.newReflectionProvider());
    }

    @Override
    public boolean canConvert(Class type) {
        return type == NativeObject.class || type == NativeArray.class || type == NativeDate.class || type == NativeBoolean.class;
    }

    @Override
    public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
        try {
            if (value instanceof NativeDate) {
                // We don't want the quotes around the date, so call toISOString directly
                context.convertAnother(NativeDate.js_toISOString(((NativeDate) value).getJSTimeValue()));
            } else {
                context.convertAnother(NativeJSON.stringify(null, (Scriptable) value, value, null, null).toString());
            }
        } catch (Exception e) {
            super.marshal(value, writer, context);
        }
    }
}