/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.util;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;

public class DefaultSerializer implements Serializer {
    @Override
    public String serialize(Serializable serializable) {
        byte[] bytes;
        try {
            bytes = SerializationUtils.serialize(serializable);
        } catch (SerializationException e) {
            /*
             * If the object to be serialized is a ConnectorMap or ChannelMap
             * and there was an error during serialization, then create a copy
             * of the map and convert all non-serializable values in the map to
             * strings. This is done so that users can still place
             * non-serializable objects into the maps (such as database
             * connection objects).
             */
            if (serializable instanceof Map) {
                bytes = serializeMap(serializable);
            } else {
                throw e;
            }
        }
        return Base64.encodeBase64String(bytes);
    }

    @Override
    public Object deserialize(String serializedObject) {
        if (serializedObject == null) {
            return null;
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(StringUtils.getBytesUsAscii(serializedObject));
        return SerializationUtils.deserialize(new Base64InputStream(bais));
    }

    /*
     * Returns the serialized bytes of an ConnectorMap or ChannelMap object,
     * making sure to convert all non-serializable values to strings.
     */
    private byte[] serializeMap(Serializable serializable) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.putAll((Map<String, Object>) serializable);
        convertMap(map);
        return SerializationUtils.serialize((Serializable) map);
    }

    /*
     * Iterates through the entry set of a map and replaces all non-serializable
     * values with String conversions of them.
     */
    private void convertMap(Map<String, Object> map) {
        for (String key : map.keySet()) {
            try {
                // Test whether the object is serializable
                SerializationUtils.serialize((Serializable) map.get(key));
            } catch (Exception e) {
                map.put(key, map.get(key).toString());
            }
        }
    }
}
