package com.webreach.mirth.server.managers;

import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.Channel.Status;
import com.webreach.mirth.server.core.util.JMXConnection;

public class StatusManager {
	private Logger logger = Logger.getLogger(StatusManager.class);
	private JMXConnection jmxConnection = null;

	/**
	 * Starts the channel with the specified id.
	 * 
	 * @param id
	 * @throws ManagerException
	 */
	public void startChannel(int id) throws ManagerException {
		logger.debug("starting channel: " + id);
		
		try {
			jmxConnection = new JMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", id + "ComponentService");
			jmxConnection.invokeOperation(properties, "start", null);
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/**
	 * Stops the channel with the specified id.
	 * 
	 * @param id
	 * @throws ManagerException
	 */
	public void stopChannel(int id) throws ManagerException {
		logger.debug("stopping channel: " + id);
		
		try {
			jmxConnection = new JMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", id + "ComponentService");
			jmxConnection.invokeOperation(properties, "stop", null);
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/**
	 * Pauses the channel with the specified id.
	 * 
	 * @param id
	 * @throws ManagerException
	 */
	public void pauseChannel(int id) throws ManagerException {
		logger.debug("pausing channel: " + id);
		
		try {
			jmxConnection = new JMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", id + "ComponentService");
			jmxConnection.invokeOperation(properties, "pause", null);
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/**
	 * Resumes the channel with the specified id.
	 * 
	 * @param id
	 * @throws ManagerException
	 */
	public void resumeChannel(int id) throws ManagerException {
		logger.debug("resuming channel: " + id);
		
		try {
			jmxConnection = new JMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", id + "ComponentService");
			jmxConnection.invokeOperation(properties, "resume", null);
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/**
	 * Returns the status of the channel with the specified id.
	 * 
	 * @param id
	 * @return
	 * @throws ManagerException
	 */
	public Status getChannelStatus(int id) throws ManagerException {
		logger.debug("retrieving channel status: " + id);
		
		try {
			jmxConnection = new JMXConnection();
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
			throw new ManagerException(e);
		}
	}

}
