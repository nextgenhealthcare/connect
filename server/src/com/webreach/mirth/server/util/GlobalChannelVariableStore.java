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

public class GlobalChannelVariableStore {
	public Map<String, Object> globalChannelVariableMap = new ConcurrentHashMap<String, Object>();

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
}
