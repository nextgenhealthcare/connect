package com.webreach.mirth.server.mule.util;

import java.util.HashMap;
import java.util.Map;

public class GlobalVariableStore {
	public Map globalVariableMap = new HashMap();
	private static GlobalVariableStore instance = null;

	private GlobalVariableStore() {

	}

	public static Map getInstance() {
		synchronized (GlobalVariableStore.class) {
			if (instance == null)
				instance = new GlobalVariableStore();

			return instance.globalVariableMap;
		}
	}
}
