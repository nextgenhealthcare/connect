/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.util;

public interface Serializer {
    /**
     * Serializes an object.
     */
    public String serialize(Object object);

    /**
     * Deserializes an object of the expectedClass type from the serializedObject string.
     */
    public <T> T deserialize(String serializedObject, Class<T> expectedClass);

    /**
     * Reads the class of a serialized object without deserializing it.
     * 
     * @return The class of the serialized object or null if the class could not be determined.
     */
    public Class<?> getClass(String serializedObject);
}
