package com.webreach.mirth.server.services;

import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.webreach.mirth.server.core.util.JMXConnection;

public class StatisticsService {
	private static StatisticsService instance = null;
	private boolean initialized = false;
	private Logger logger = Logger.getLogger(StatisticsService.class);
	private JMXConnection jmxConnection = null;
	
	private StatisticsService() {}

	public static StatisticsService getInstance() {
		synchronized (StatusService.class) {
			if (instance == null)
				instance = new StatisticsService();

			return instance;
		}
	}
	
	public void initialize() {
		logger.debug("initializing configuration");
		
		try {
			jmxConnection = new JMXConnection();
			initialized = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean isInitialized() {
		return initialized;
	}

	public int getChannelSentCount(int id) {
		try {
			jmxConnection = new JMXConnection();
			return getStatistic(id, "TotalEventsSent");
		} catch (Exception e) {
			return -1;
		}
	}

	// This is a hack to address the fact that this statistic is incorrectly
	// incrememnted by 2 in Mule.
	public int getChannelReceivedCount(int id) {
		try {
			jmxConnection = new JMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "statistics");
			properties.put("name", String.valueOf(id));
			Double count = ((Long) jmxConnection.getAttribute(properties, "TotalEventsReceived")).doubleValue();
			return Double.valueOf(Math.ceil(count / 2)).intValue();
		} catch (Exception e) {
			return -1;
		}
	}

	public int getChannelErrorCount(int id) {
		try {
			jmxConnection = new JMXConnection();
			return getStatistic(id, "ExecutionErrors");
		} catch (Exception e) {
			return -1;
		}
	}

	public int getChannelQueueCount(int id) {
		try {
			jmxConnection = new JMXConnection();
			return getStatistic(id, "QueuedEvents");
		} catch (Exception e) {
			return -1;
		}
	}

	public void clearChannelStatistics(int id) {
		try {
			jmxConnection = new JMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "statistics");
			properties.put("name", String.valueOf(id));
			jmxConnection.invokeOperation(properties, "clear", null);
		} catch (Exception e) {
			// TODO: do something
			// throw new StatisticsException();
		}
	}

	private int getStatistic(int id, String statistic) throws Exception {
		try {
			jmxConnection = new JMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "statistics");
			properties.put("name", String.valueOf(id));

			return ((Long) jmxConnection.getAttribute(properties, statistic)).intValue();
		} catch (Exception e) {
			throw new Exception();
		}
	}

}
