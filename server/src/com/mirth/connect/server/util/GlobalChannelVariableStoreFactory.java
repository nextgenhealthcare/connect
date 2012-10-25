/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalChannelVariableStoreFactory {
	public Map<String, GlobalChannelVariableStore> globalChannelVariableMap = new ConcurrentHashMap<String, GlobalChannelVariableStore>();
	private static GlobalChannelVariableStoreFactory instance = null;

	private GlobalChannelVariableStoreFactory() {

	}

	public static GlobalChannelVariableStoreFactory getInstance() {
		synchronized (GlobalChannelVariableStoreFactory.class) {
			if (instance == null)
				instance = new GlobalChannelVariableStoreFactory();

			return instance;
		}
	}
	
	public synchronized GlobalChannelVariableStore get(String channelId) {
	    if (!globalChannelVariableMap.containsKey(channelId)) {
	        globalChannelVariableMap.put(channelId, new GlobalChannelVariableStore());
	        
	    }
	    return globalChannelVariableMap.get(channelId);
	}
	
}
