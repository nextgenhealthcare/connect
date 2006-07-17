package com.webreach.mirth.server.controllers;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.ChannelStatus;
import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.server.util.JMXConnection;
import com.webreach.mirth.server.util.JMXConnectionFactory;

public class ChannelStatusController {
	private Logger logger = Logger.getLogger(this.getClass());
	private SystemLogger systemLogger = new SystemLogger();
	private	ChannelController channelController = new ChannelController();

	/**
	 * Starts the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ControllerException
	 */
	public void startChannel(int channelId) throws ControllerException {
		logger.debug("starting channel: " + channelId);
		
		JMXConnection jmxConnection = null;
		
		try {
			jmxConnection = JMXConnectionFactory.createJMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", "ModelService");
		
			String[] params = {String.valueOf(channelId)};
			jmxConnection.invokeOperation(properties, "startComponent", params);
		} catch (Exception e) {
			throw new ControllerException(e);
		} finally {
			jmxConnection.close();
		}

		SystemEvent systemEvent = new SystemEvent("Channel started.");
		systemEvent.getAttributes().put("channelId", channelId);
		systemLogger.logSystemEvent(systemEvent);
	}

	/**
	 * Stops the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ControllerException
	 */
	public void stopChannel(int channelId) throws ControllerException {
		logger.debug("stopping channel: channelId=" + channelId);
		
		// if paused, must be resumed before stopped
		if (getState(channelId).equals(ChannelStatus.State.PAUSED)) {
			logger.debug("channel is paused, must resume before stopping");
			resumeChannel(channelId);
		}
		
		JMXConnection jmxConnection = null;
		
		try {
			jmxConnection = JMXConnectionFactory.createJMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", channelId + "ComponentService");
			jmxConnection.invokeOperation(properties, "stop", null);
		} catch (Exception e) {
			throw new ControllerException(e);
		} finally {
			jmxConnection.close();
		}

		SystemEvent systemEvent = new SystemEvent("Channel stopped.");
		systemEvent.getAttributes().put("channelId", channelId);
		systemLogger.logSystemEvent(systemEvent);
	}

	/**
	 * Pauses the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ControllerException
	 */
	public void pauseChannel(int channelId) throws ControllerException {
		logger.debug("pausing channel: channelId=" + channelId);
		
		JMXConnection jmxConnection = null;
		
		try {
			jmxConnection = JMXConnectionFactory.createJMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", channelId + "ComponentService");
			jmxConnection.invokeOperation(properties, "pause", null);
		} catch (Exception e) {
			throw new ControllerException(e);
		} finally {
			jmxConnection.close();
		}

		SystemEvent systemEvent = new SystemEvent("Channel paused.");
		systemEvent.getAttributes().put("channelId", channelId);
		systemLogger.logSystemEvent(systemEvent);
	}

	/**
	 * Resumes the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ControllerException
	 */
	public void resumeChannel(int channelId) throws ControllerException {
		logger.debug("resuming channel: channelId=" + channelId);
		
		JMXConnection jmxConnection = null;
		
		try {
			jmxConnection = JMXConnectionFactory.createJMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", channelId + "ComponentService");
			jmxConnection.invokeOperation(properties, "resume", null);
		} catch (Exception e) {
			throw new ControllerException(e);
		} finally {
			jmxConnection.close();
		}

		SystemEvent systemEvent = new SystemEvent("Channel resumed.");
		systemEvent.getAttributes().put("channelId", channelId);
		systemLogger.logSystemEvent(systemEvent);
	}
	
	/**
	 * Returns a list of ChannelStatus objects representing the running channels. 
	 * 
	 * @return
	 * @throws ControllerException
	 */
	public List<ChannelStatus> getChannelStatusList() {
		logger.debug("retrieving channel status list");
		List<ChannelStatus> channelStatusList = new ArrayList<ChannelStatus>();

		try {
			ArrayList<String> deployedChannelIdList = (ArrayList<String>) getDeployedIds();
			
			for (Iterator iter = deployedChannelIdList.iterator(); iter.hasNext();) {
				String channelId = (String) iter.next();
				ChannelStatus channelStatus = new ChannelStatus();
				channelStatus.setChannelId(Integer.valueOf(channelId).intValue());
				
				// check if the channel is running but has been removed from the channel list
				if (channelController.getChannels(Integer.valueOf(channelId).intValue()).size() != 0) {
					channelStatus.setName(channelController.getChannels(Integer.valueOf(channelId).intValue()).get(0).getName());	
				} else {
					channelStatus.setName("Channel has been deleted.");
				}
				
				channelStatus.setState(getState(Integer.valueOf(channelId).intValue()));
				channelStatusList.add(channelStatus);
			}
			
			return channelStatusList;
		} catch (Exception e) {
			logger.warn("could not retrieve channel status list", e);
			// returns an empty list
			return channelStatusList;
		}
	}
	
	/**
	 * Returns a list of the ids of all running channels.
	 * 
	 * @return
	 * @throws ControllerException
	 */
	private List<String> getDeployedIds() throws ControllerException {
		logger.debug("retrieving deployed channel id list");
		List<String> deployedChannelIdList = new ArrayList<String>();
		
		JMXConnection jmxConnection = null;
		
		try {
			jmxConnection = JMXConnectionFactory.createJMXConnection();
			Set beanObjectNames = jmxConnection.getMBeanNames();

			for (Iterator iter = beanObjectNames.iterator(); iter.hasNext();) {
				ObjectName objectName = (ObjectName) iter.next();

				// only add valid mirth channels
				// format: MirthConfiguration:type=statistics,name=*
				if ((objectName.getKeyProperty("type") != null)
						&& objectName.getKeyProperty("type").equals("statistics")
						&& (objectName.getKeyProperty("name") != null)
						&& !objectName.getKeyProperty("name").startsWith("_")
						&& (objectName.getKeyProperty("router") == null)) {
					deployedChannelIdList.add(objectName.getKeyProperty("name"));
				}
			}

			return deployedChannelIdList;
		} catch (Exception e) {
			throw new ControllerException(e.getMessage());
		} finally {
			jmxConnection.close();
		}
	}
	
	/**
	 * Returns the state of a channel with the specified id.
	 * 
	 * @param channelId
	 * @return
	 * @throws ControllerException
	 */
	private ChannelStatus.State getState(int channelId) throws ControllerException {
		logger.debug("retrieving channel state: channelId=" + channelId);
		
		JMXConnection jmxConnection = null;
		
		try {
			jmxConnection = JMXConnectionFactory.createJMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", channelId + "ComponentService");

			if ((Boolean) jmxConnection.getAttribute(properties, "Paused")) {
				return ChannelStatus.State.PAUSED;
			} else if ((Boolean) jmxConnection.getAttribute(properties, "Stopped")) {
				return ChannelStatus.State.STOPPED;
			} else {
				return ChannelStatus.State.STARTED;
			}
		} catch (Exception e) {
			throw new ControllerException(e);
		} finally {
			jmxConnection.close();
		}
	}
}
