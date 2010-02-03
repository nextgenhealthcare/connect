/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.server.util;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalVariableStore {
	public Map<String, Object> globalVariableMap = new ConcurrentHashMap<String, Object>();
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
}
