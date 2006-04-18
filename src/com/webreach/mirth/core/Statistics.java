package com.webreach.mirth.core;

import java.util.Hashtable;

import com.webreach.mirth.core.util.JMXConnection;

public class Statistics {
	private JMXConnection jmxConnection;
	private Channel channel;
	
	public Statistics(Channel channel) {
		this.channel = channel;
		jmxConnection = new JMXConnection();	
	}
	
	public int getSentCount() {
		return getStatistic(channel.getId(), "TotalEventsSent");
	}
	
	// This is a hack to address the fact that this statistic is incorrectly incrememnted by 2 in Mule.
	public int getReceivedCount() {
		Hashtable<String, String> properties = new Hashtable<String, String>();
		properties.put("type", "statistics");
		properties.put("name", String.valueOf(channel.getId()));
		Double count = ((Long) jmxConnection.getAttribute(properties, "TotalEventsReceived")).doubleValue();
		return Double.valueOf(Math.ceil(count / 2)).intValue();
	}
	
	public int getErrorCount() {
		return getStatistic(channel.getId(), "ExecutionErrors");
	}
	
	public int getQueueCount() {
		return getStatistic(channel.getId(), "QueuedEvents");
	}
	
	public void clear() {
		Hashtable<String, String> properties = new Hashtable<String, String>();
		properties.put("type", "statistics");
		properties.put("name", channel.getName());
		jmxConnection.invokeOperation(properties, "clear", null);	
	}
	
	private int getStatistic(int id, String statistic) throws RuntimeException {
		try {
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "statistics");
			properties.put("name", String.valueOf(id));

			return ((Long) jmxConnection.getAttribute(properties, statistic)).intValue();
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}
}
