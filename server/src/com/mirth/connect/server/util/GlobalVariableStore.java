/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class GlobalVariableStore {
    public Map<String, Object> globalVariableMap = new ConcurrentHashMap<String, Object>();
    public Map<String, SyncObject> globalVariableSyncMap = new ConcurrentHashMap<String, SyncObject>();
    private static GlobalVariableStore instance = null;

    private GlobalVariableStore() {

    }

    public static GlobalVariableStore getInstance() {
        synchronized (GlobalVariableStore.class) {
            if (instance == null)
                instance = new GlobalVariableStore();

            return instance;
        }
    }

    public boolean containsKey(String key) {
        return globalVariableMap.containsKey(key);
    }

    public synchronized void remove(String key) {
        globalVariableMap.remove(key);
    }

    public Object get(String key) {
        return globalVariableMap.get(key);
    }

    public synchronized void put(String key, Object value) {
        globalVariableMap.put(key, value);
    }

    public synchronized void putAll(Map<String, Object> map) {
        globalVariableMap.putAll(map);
    }

    public Map<String, Object> getVariables() {
        return Collections.unmodifiableMap(globalVariableMap);
    }

    public synchronized void clear() {
        globalVariableMap.clear();
    }

    @Override
    public String toString() {
        return globalVariableMap.toString();
    }

    // ##### SYNC METHODS #####

    public void lock(String key) {
        globalVariableSyncMap.get(key).getLock().lock();
    }

    public void unlock(String key) {
        globalVariableSyncMap.get(key).getLock().unlock();
    }

    public boolean containsKeySync(String key) {
        return globalVariableSyncMap.containsKey(key);
    }

    public void removeSync(String key) {
        globalVariableSyncMap.remove(key);
    }

    public Object getSync(String key) {
        lock(key);
        Object value = globalVariableSyncMap.get(key).getValue();
        unlock(key);
        return value;
    }

    public void putSync(String key, Object value) {
        boolean exists = true;
        synchronized (globalVariableSyncMap) {
            if (!containsKeySync(key)) {
                exists = false;
                globalVariableSyncMap.put(key, new SyncObject(value, new ReentrantLock()));
            }
        }

        if (exists) {
            lock(key);
            globalVariableSyncMap.get(key).setValue(value);
            unlock(key);
        }
    }

    public void putAllSync(Map<String, Object> map) {
        for (Entry<String, Object> entry : map.entrySet()) {
            putSync(entry.getKey(), entry.getValue());
        }
    }

    public void clearSync() {
        globalVariableSyncMap.clear();
    }
}
