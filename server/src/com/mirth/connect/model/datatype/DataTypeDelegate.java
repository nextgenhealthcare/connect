/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.datatype;

import com.mirth.connect.donkey.model.message.SerializationType;
import com.mirth.connect.model.converters.IXMLSerializer;

public interface DataTypeDelegate {

    /**
     * Get the name of the data type
     */
    public String getName();

    /**
     * Get an instance of the data type's serializer with the given properties
     */
    public IXMLSerializer getSerializer(SerializerProperties properties);

    /**
     * Indicates if the data type is in binary format
     */
    public boolean isBinary();

    /**
     * Get the serialization type
     */
    public SerializationType getSerializationType();

    /**
     * Get the default properties of the data type. Must not return null.
     */
    public abstract DataTypeProperties getDefaultProperties();
}
