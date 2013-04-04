/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.util;

import java.io.Reader;
import java.io.Writer;

public interface Serializer {
    public String serialize(Object serializableObject);
    
    public void serialize(Object serializableObject, Writer writer);

    public Object deserialize(String serializedObject);
    
    public Object deserialize(Reader reader);
}
