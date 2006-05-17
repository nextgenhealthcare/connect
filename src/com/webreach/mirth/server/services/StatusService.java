package com.webreach.mirth.server.services;

import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.Channel.Status;
import com.webreach.mirth.server.core.util.JMXConnection;

public class StatusService {
	private Logger logger = Logger.getLogger(StatusService.class);
	private JMXConnection jmxConnection = null;

	/**
	 * Starts the channel with the specified id.
	 * 
	 * @param id
	 * @throws ServiceException
	 */
	public void startChannel(int id) throws ServiceException {
		logger.debug("starting channel: " + id);
		
		try {
			jmxConnection = new JMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", id + "ComponentService");
			jmxConnection.invokeOperation(properties, "start", null);
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	/**
	 * Stops the channel with the specified id.
	 * 
	 * @param id
	 * @throws ServiceException
	 */
	public void stopChannel(int id) throws ServiceException {
		logger.debug("stopping channel: " + id);
		
		try {
			jmxConnection = new JMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", id + "ComponentService");
			jmxConnection.invokeOperation(properties, "stop", null);
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	/**
	 * Pauses the channel with the specified id.
	 * 
	 * @param id
	 * @throws ServiceException
	 */
	public void pauseChannel(int id) throws ServiceException {
		logger.debug("pausing channel: " + id);
		
		try {
			jmxConnection = new JMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", id + "ComponentService");
			jmxConnection.invokeOperation(properties, "pause", null);
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	/**
	 * Resumes the channel with the specified id.
	 * 
	 * @param id
	 * @throws ServiceException
	 */
	public void resumeChannel(int id) throws ServiceException {
		logger.debug("resuming channel: " + id);
		
		try {
			jmxConnection = new JMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", id + "ComponentService");
			jmxConnection.invokeOperation(properties, "resume", null);
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	/**
	 * Returns the status of the channel with the specified id.
	 * 
	 * @param id
	 * @return
	 * @throws ServiceException
	 */
	public Status getChannelStatus(int id) throws ServiceException {
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
			throw new ServiceException(e);
		}
	}

}
