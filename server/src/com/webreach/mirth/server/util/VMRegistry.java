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
