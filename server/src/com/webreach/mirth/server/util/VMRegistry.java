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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webreach.mirth.connectors.vm.VMMessageReceiver;

public class VMRegistry {
	public Map<String, VMMessageReceiver> vmRegistry = new ConcurrentHashMap<String, VMMessageReceiver>();
	private static VMRegistry instance = null;

	private VMRegistry() {

	}

	public static VMRegistry getInstance() {
		synchronized (VMRegistry.class) {
			if (instance == null)
				instance = new VMRegistry();

			return instance;
		}
	}

	public boolean containsKey(String key) {
		return vmRegistry.containsKey(key);
	}

	public synchronized void remove(String key) {
		vmRegistry.remove(key);
	}

	public VMMessageReceiver get(String key) {
		return vmRegistry.get(key);
	}

	public synchronized void register(String key, VMMessageReceiver value) {
		vmRegistry.put(key, value);
	}

	public synchronized void rebuild() {
		vmRegistry = Collections.synchronizedMap(new HashMap<String, VMMessageReceiver>());
	}

}
