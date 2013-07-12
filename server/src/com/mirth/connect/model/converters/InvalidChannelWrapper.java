/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters;

import com.mirth.connect.donkey.util.xstream.DonkeyMapperWrapper;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.InvalidChannel;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public class InvalidChannelWrapper implements DonkeyMapperWrapper {

    @Override
    public MapperWrapper wrapMapper(MapperWrapper next) {
        return new MapperWrapper(next) {
            @Override
            public String serializedClass(Class type) {
                if (type == InvalidChannel.class) {
                    return super.serializedClass(Channel.class);
                }
                return super.serializedClass(type);
            }
        };
    }
}