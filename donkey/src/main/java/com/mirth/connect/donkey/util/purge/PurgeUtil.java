package com.mirth.connect.donkey.util.purge;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.channel.Statistics;

public class PurgeUtil {
    /**
     * Counts the number of lines in this String.
     */
    public static Integer countLines(String data) {
        if (data == null) {
            return null;
        }
        return data.split("\r\n|\r|\n").length;
    }

    /**
     * Counts the number of characters in this String.
     */
    public static Integer countChars(String data) {
        if (data == null) {
            return null;
        }
        return data.length();
    }

    private static Map.Entry<?, ?> getPurgedEntry(Map.Entry<?, ?> originalEntry) {
        Object originalKey = originalEntry.getKey();
        Object originalValue = originalEntry.getValue();
        String key = StringUtils.uncapitalize(originalKey.toString());
        Object value = originalValue;

        if (originalValue instanceof String[]) {
            key += "Count";
            value = ArrayUtils.getLength(originalValue);
        } else if (originalValue instanceof ArrayList<?>) {
            key += "Count";
            value = ((ArrayList<Object>) originalValue).size();
        } else if (originalValue instanceof Map<?, ?>) {
            key += "Count";
            value = ((Map<Object, Object>) originalValue).size();
        } else {
            return null;
        }
        return new AbstractMap.SimpleEntry(key, value);
    }

    /**
     * Iterates through a Map and purges entries. If the entry is a collection, array, or map, the
     * size will be saved. All other entries are removed.
     */
    public static Map<?, ?> getPurgedMap(Map<?, ?> originalEntry) {
        Map<String, Object> purgedData = new HashMap<String, Object>();
        for (Map.Entry<?, ?> entry : originalEntry.entrySet()) {
            Map.Entry<?, ?> purgedEntry = getPurgedEntry(entry);
            // If data has been properly purged, add to purged data map.
            if (purgedEntry != null) {
                purgedData.put(purgedEntry.getKey().toString(), purgedEntry.getValue());
            }
        }
        return purgedData;
    }

    /**
     * If the value is numeric, returns original value. If the value is empty, returns an empty
     * string. Otherwise if the value uses Velocity replacement or is invalid, returns null. For
     * fields where the value must be numeric but we allow Strings so that Velocity can be used.
     */
    public static String getNumericValue(String data) {
        if (data == null) {
            return null;
        }
        String value = null;
        if (NumberUtils.isNumber(data)) {
            value = data;
        } else if (data.isEmpty()) {
            value = "";
        }
        return value;
    }

    /**
     * Iterates through a List and returns the purged properties for each item.
     */
    public static List<Map<String, Object>> purgeList(List<?> originalList) {
        List<Map<String, Object>> purgedList = new ArrayList<Map<String, Object>>();
        for (Object object : originalList) {
            if (object instanceof Purgable) {
                purgedList.add(((Purgable) object).getPurgedProperties());
            }
        }
        return purgedList;
    }
    
    public static Map<Status, Long> getMessageStatistics(String channelId, Integer metaDataId) {
        com.mirth.connect.donkey.server.controllers.ChannelController donkeyChannelController = com.mirth.connect.donkey.server.controllers.ChannelController.getInstance();
        Statistics totalStats = donkeyChannelController.getTotalStatistics();
        Map<Status, Long> lifetimeStats = new HashMap<Status, Long>();
        
        if (totalStats != null) {
            lifetimeStats = totalStats.getConnectorStats(channelId, metaDataId);
        }
        return lifetimeStats;
    }
}
