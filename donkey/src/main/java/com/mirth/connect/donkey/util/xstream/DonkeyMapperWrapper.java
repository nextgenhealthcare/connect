/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.util.xstream;

import com.thoughtworks.xstream.mapper.MapperWrapper;

public interface DonkeyMapperWrapper {

    public MapperWrapper wrapMapper(MapperWrapper next);
}
