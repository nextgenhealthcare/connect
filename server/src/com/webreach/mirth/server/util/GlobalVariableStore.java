/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


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
