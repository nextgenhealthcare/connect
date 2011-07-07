/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class GlobalChannelVariableStore {
    public Map<String, Object> globalChannelVariableMap = new ConcurrentHashMap<String, Object>();
    public Map<String, SyncObject> globalChannelVariableSyncMap = new ConcurrentHashMap<String, SyncObject>();

    public boolean containsKey(String key) {
        return globalChannelVariableMap.containsKey(key);
    }

    public synchronized void remove(String key) {
        globalChannelVariableMap.remove(key);
    }

    public Object get(String key) {
        return globalChannelVariableMap.get(key);
    }

    public synchronized void put(String key, Object value) {
        globalChannelVariableMap.put(key, value);
    }

    public synchronized void putAll(Map<String, Object> map) {
        globalChannelVariableMap.putAll(map);
    }

    public Map<String, Object> getVariables() {
        return Collections.unmodifiableMap(globalChannelVariableMap);
    }

    public synchronized void clear() {
        globalChannelVariableMap.clear();
    }

    @Override
    public String toString() {
        return globalChannelVariableMap.toString();
    }

    // ##### SYNC METHODS #####

    public void lock(String key) {
        globalChannelVariableSyncMap.get(key).getLock().lock();
    }

    public void unlock(String key) {
        globalChannelVariableSyncMap.get(key).getLock().unlock();
    }

    public boolean containsKeySync(String key) {
        return globalChannelVariableSyncMap.containsKey(key);
    }

    public void removeSync(String key) {
        globalChannelVariableSyncMap.remove(key);
    }

    public Object getSync(String key) {
        lock(key);
        Object value = globalChannelVariableSyncMap.get(key).getValue();
        unlock(key);
        return value;
    }

    public void putSync(String key, Object value) {
        boolean exists = true;
        synchronized (globalChannelVariableSyncMap) {
            if (!containsKeySync(key)) {
                exists = false;
                globalChannelVariableSyncMap.put(key, new SyncObject(value, new ReentrantLock()));
            }
        }

        if (exists) {
            lock(key);
            globalChannelVariableSyncMap.get(key).setValue(value);
            unlock(key);
        }
    }

    public void putAllSync(Map<String, Object> map) {
        for (Entry<String, Object> entry : map.entrySet()) {
            putSync(entry.getKey(), entry.getValue());
        }
    }

    public void clearSync() {
        globalChannelVariableSyncMap.clear();
    }
}
