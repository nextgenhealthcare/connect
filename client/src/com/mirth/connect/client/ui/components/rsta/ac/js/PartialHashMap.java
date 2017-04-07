/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta.ac.js;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class PartialHashMap<V> extends HashMap<String, List<V>> {

    private static final Pattern SPLIT_PATTERN = Pattern.compile("\\s+|-|(?=[A-Z0-9_][a-z])");

    private Map<String, String[]> splitMap = new HashMap<String, String[]>();

    @Override
    public void clear() {
        splitMap.clear();
        super.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(fixKey(key));
    }

    @Override
    public List<V> get(Object key) {
        return super.get(fixKey(key));
    }

    public List<V> getPartial(Object key) {
        String fixedKey = fixKey(key);

        if (StringUtils.isBlank(fixedKey)) {
            List<V> list = new ArrayList<V>();
            for (List<V> value : values()) {
                list.addAll(value);
            }
            return list;
        } else {
            List<V> list = new ArrayList<V>();

            for (Entry<String, List<V>> entry : entrySet()) {
                String entryKey = entry.getKey();

                if (entry.getKey().startsWith(fixedKey)) {
                    list.addAll(entry.getValue());
                } else {
                    String[] array = splitMap.get(entryKey);
                    if (array != null) {
                        for (String element : array) {
                            if (element.startsWith(fixedKey)) {
                                list.addAll(entry.getValue());
                                break;
                            }
                        }
                    }
                }
            }

            return list;
        }
    }

    public V put(String key, V value) {
        String fixedKey = fixKey(key);
        if (StringUtils.isNotBlank(fixedKey)) {
            if (!splitMap.containsKey(fixedKey)) {
                String[] splitArray = splitKey(key);
                if (splitArray.length > 0) {
                    splitMap.put(fixedKey, splitArray);
                }
            }

            List<V> list = super.get(fixedKey);
            if (list != null) {
                list.add(value);
            } else {
                super.put(fixedKey, new ArrayList<V>(Collections.singletonList(value)));
            }
            return null;
        } else {
            throw new RuntimeException("Key cannot be blank.");
        }
    }

    @Override
    public List<V> put(String key, List<V> value) {
        String fixedKey = fixKey(key);
        if (StringUtils.isNotBlank(fixedKey)) {
            if (!splitMap.containsKey(fixedKey)) {
                String[] splitArray = splitKey(key);
                if (splitArray.length > 0) {
                    splitMap.put(fixedKey, splitArray);
                }
            }

            return super.put(fixedKey, value);
        } else {
            throw new RuntimeException("Key cannot be blank.");
        }
    }

    @Override
    public List<V> remove(Object key) {
        String fixedKey = fixKey(key);
        splitMap.remove(fixedKey);
        return super.remove(fixedKey);
    }

    public void removeValue(Object key, Object value) {
        List<V> list = get(key);
        if (list != null) {
            list.remove(value);
            if (list.isEmpty()) {
                remove(key);
            }
        }
    }

    private String fixKey(Object key) {
        return (key != null ? String.valueOf(key) : "").toLowerCase().trim();
    }

    private String[] splitKey(String key) {
        List<String> list = new ArrayList<String>();

        Scanner scanner = new Scanner(key);
        scanner.useDelimiter(SPLIT_PATTERN);
        while (scanner.hasNext()) {
            list.add(scanner.next().toLowerCase());
        }
        scanner.close();

        return list.toArray(new String[list.size()]);
    }
}