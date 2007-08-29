package com.webreach.mirth.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ServerConfiguration implements Serializable {
	private String date;
	private List<Channel> channels;
	private List<User> users;
	private List<Alert> alerts;
	private Properties properties;
	private Map<String, String> globalScripts;
	public List<Alert> getAlerts() {
		return this.alerts;
	}

	public void setAlerts(List<Alert> alerts) {
		this.alerts = alerts;
	}

	public List<Channel> getChannels() {
		return this.channels;
	}

	public void setChannels(List<Channel> channels) {
		this.channels = channels;
	}

	public Properties getProperties() {
		return this.properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public List<User> getUsers() {
		return this.users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public Map<String, String> getGlobalScripts() {
		return globalScripts;
	}

	public void setGlobalScripts(Map<String, String> globalScripts) {
		this.globalScripts = globalScripts;
	}
}
