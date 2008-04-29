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
