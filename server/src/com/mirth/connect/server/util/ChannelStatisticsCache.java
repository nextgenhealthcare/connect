/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.server.util;

import java.util.Map;

import com.webreach.mirth.model.ChannelStatistics;

public class ChannelStatisticsCache {
	private Map<String, ChannelStatistics> cache;

	// singleton pattern
	private static ChannelStatisticsCache instance = null;

	private ChannelStatisticsCache() { }

	public static ChannelStatisticsCache getInstance() {
		synchronized (ChannelStatisticsCache.class) {
			if (instance == null)
				instance = new ChannelStatisticsCache();

			return instance;
		}
	}

	public Map<String, ChannelStatistics> getCache() {
		return cache;
	}

	public void setCache(Map<String, ChannelStatistics> cache) {
		this.cache = cache;
	}
}
