package com.mirth.connect.donkey.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

public class MapUtil {
    private static Logger logger = Logger.getLogger(MapUtil.class);

    public static String serializeMap(Serializer serializer, Map<String, Object> map) {
        /*
         * Try to serialize the entire map and if it fails, then find the map
         * values that failed to
         * serialize and convert them into their string representation before
         * attempting to
         * serialize again.
         */
        try {
            return serializer.serialize(map);
        } catch (Exception e) {
            Map<String, Object> newMap = new HashMap<String, Object>();

            for (Entry<String, Object> entry : map.entrySet()) {
                Object value = entry.getValue();

                try {
                    serializer.serialize(value);
                    newMap.put(entry.getKey(), value);
                } catch (Exception e2) {
                    logger.error("Non-serializable value found in map, converting value to string with key: " + entry.getKey());
                    newMap.put(entry.getKey(), (value == null) ? "" : value.toString());
                }
            }

            return serializer.serialize(newMap);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> deserializeMap(Serializer serializer, String serializedMap) {
        return (Map<String, Object>) serializer.deserialize(serializedMap, Map.class);
    }
}
