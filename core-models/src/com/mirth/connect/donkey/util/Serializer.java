/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.util;

import java.util.List;

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
     * Deserializes a source XML string and returns a List of objects of the expectedListItemClass
     * type. If the source xml string represents a single object, then a list with that single
     * object will be returned.
     */
    public <T> List<T> deserializeList(String serializedObject, Class<T> expectedListItemClass);

    /**
     * Reads the class of a serialized object without deserializing it.
     * 
     * @return The class of the serialized object or null if the class could not be determined.
     */
    public Class<?> getClass(String serializedObject);
}
