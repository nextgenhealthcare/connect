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
import org.mozilla.javascript.NativeObject;

import com.mirth.connect.donkey.util.xstream.DonkeyMapperWrapper;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.InvalidChannel;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public class MirthMapperWrapper implements DonkeyMapperWrapper {

    @Override
    public MapperWrapper wrapMapper(MapperWrapper next) {
        return new MapperWrapper(next) {
            @Override
            public String serializedClass(Class type) {
                if (type == InvalidChannel.class) {
                    return super.serializedClass(Channel.class);
                } else if (type == NativeObject.class || type == NativeArray.class || type == NativeDate.class || type == NativeBoolean.class) {
                    return super.serializedClass(String.class);
                }
                return super.serializedClass(type);
            }
        };
    }
}