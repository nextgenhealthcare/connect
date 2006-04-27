package com.webreach.mirth.core;

import java.util.Hashtable;

import com.webreach.mirth.core.util.JMXConnection;

public class Statistics {
	private JMXConnection jmxConnection;
	private Channel channel;

	public Statistics(Channel channel) {
		this.channel = channel;
	}

	public int getSentCount() {
		try {
			jmxConnection = new JMXConnection();
			return getStatistic(channel.getId(), "TotalEventsSent");
		} catch (Exception e) {
			return -1;
		}
	}

	// This is a hack to address the fact that this statistic is incorrectly
	// incrememnted by 2 in Mule.
	public int getReceivedCount() {
		try {
			jmxConnection = new JMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "statistics");
			properties.put("name", String.valueOf(channel.getId()));
			Double count = ((Long) jmxConnection.getAttribute(properties, "TotalEventsReceived")).doubleValue();
			return Double.valueOf(Math.ceil(count / 2)).intValue();
		} catch (Exception e) {
			return -1;
		}
	}

	public int getErrorCount() {
		try {
			jmxConnection = new JMXConnection();
			return getStatistic(channel.getId(), "ExecutionErrors");
		} catch (Exception e) {
			return -1;
		}
	}

	public int getQueueCount() {
		try {
			jmxConnection = new JMXConnection();
			return getStatistic(channel.getId(), "QueuedEvents");
		} catch (Exception e) {
			return -1;
		}
	}

	public void clear() {
		try {
			jmxConnection = new JMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "statistics");
			properties.put("name", channel.getName());
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
