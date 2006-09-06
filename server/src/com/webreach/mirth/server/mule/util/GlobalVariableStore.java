package com.webreach.mirth.server.mule.util;

import java.util.HashMap;
import java.util.Map;


public class GlobalVariableStore {
	public Map<String, String> globalVariableMap = new HashMap<String, String>();
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
	
	public boolean containsKey(Object key){
		return globalVariableMap.containsKey(key);
	}
	public void put(String key, String value) {
		globalVariableMap.put(key, value);
	}
	
	public String get(String key) {
		return globalVariableMap.get(key);
	}
}
