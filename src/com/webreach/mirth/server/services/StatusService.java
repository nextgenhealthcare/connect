package com.webreach.mirth.server.services;

import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.Channel.Status;
import com.webreach.mirth.server.core.util.ConfigurationException;
import com.webreach.mirth.server.core.util.JMXConnection;

public class StatusService {
	private static StatusService instance = null;
	private boolean initialized = false;
	private Logger logger = Logger.getLogger(StatusService.class);
	private JMXConnection jmxConnection = null;

	private StatusService() {}

	public static StatusService getInstance() {
		synchronized (StatusService.class) {
			if (instance == null)
				instance = new StatusService();

			return instance;
		}
	}

	public void initialize() throws ConfigurationException {
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

	public void startChannel(int id) {
		try {
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", id + "ComponentService");
			jmxConnection.invokeOperation(properties, "start", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stopChannel(int id) {
		try {
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", id + "ComponentService");
			jmxConnection.invokeOperation(properties, "stop", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void pause(int id) {
		try {
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", id + "ComponentService");
			jmxConnection.invokeOperation(properties, "pause", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void resume(int id) {
		try {
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", id + "ComponentService");
			jmxConnection.invokeOperation(properties, "resume", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Status getChannelStatus(int id) {
		try {
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", id + "ComponentService");

			if ((Boolean) jmxConnection.getAttribute(properties, "Paused")) {
				return Status.PAUSED;
			} else if ((Boolean) jmxConnection.getAttribute(properties, "Stopped")) {
				return Status.STOPPED;
			} else {
				return Status.STARTED;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Status.STOPPED;
		}
	}

}
