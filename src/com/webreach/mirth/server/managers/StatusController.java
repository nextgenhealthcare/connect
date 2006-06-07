package com.webreach.mirth.server.managers;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.Status;
import com.webreach.mirth.server.core.util.JMXConnection;

public class StatusController {
	private Logger logger = Logger.getLogger(StatusController.class);
	private JMXConnection jmxConnection = null;

	/**
	 * Starts the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ControllerException
	 */
	public void startChannel(int channelId) throws ControllerException {
		logger.debug("starting channel: " + channelId);
		
		try {
			jmxConnection = new JMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", channelId + "ComponentService");
			jmxConnection.invokeOperation(properties, "start", null);
		} catch (Exception e) {
			throw new ControllerException(e);
		}
	}

	/**
	 * Stops the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ControllerException
	 */
	public void stopChannel(int channelId) throws ControllerException {
		logger.debug("stopping channel: " + channelId);
		
		try {
			jmxConnection = new JMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", channelId + "ComponentService");
			jmxConnection.invokeOperation(properties, "stop", null);
		} catch (Exception e) {
			throw new ControllerException(e);
		}
	}

	/**
	 * Pauses the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ControllerException
	 */
	public void pauseChannel(int channelId) throws ControllerException {
		logger.debug("pausing channel: " + channelId);
		
		try {
			jmxConnection = new JMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", channelId + "ComponentService");
			jmxConnection.invokeOperation(properties, "pause", null);
		} catch (Exception e) {
			throw new ControllerException(e);
		}
	}

	/**
	 * Resumes the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ControllerException
	 */
	public void resumeChannel(int channelId) throws ControllerException {
		logger.debug("resuming channel: " + channelId);
		
		try {
			jmxConnection = new JMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", channelId + "ComponentService");
			jmxConnection.invokeOperation(properties, "resume", null);
		} catch (Exception e) {
			throw new ControllerException(e);
		}
	}
	
	/**
	 * Returns a list of ChannelStatus objects representing the running channels. 
	 * 
	 * @return
	 * @throws ControllerException
	 */
	public List<Status> getStatusList() {
		logger.debug("retrieving channel status list");
		List<Status> channelStatusList = new ArrayList<Status>();
		ChannelController channelController = new ChannelController();

		try {
			ArrayList<String> deployedChannelIdList = (ArrayList<String>) getDeployedIds();
			
			for (Iterator iter = deployedChannelIdList.iterator(); iter.hasNext();) {
				String channelId = (String) iter.next();
				Status channelStatus = new Status();
				channelStatus.setId(Integer.valueOf(channelId).intValue());
				channelStatus.setName(channelController.getChannels(Integer.valueOf(channelId).intValue()).get(0).getName());
				channelStatus.setState(getState(Integer.valueOf(channelId).intValue()));
				channelStatusList.add(channelStatus);
			}
			
			return channelStatusList;
		} catch (Exception e) {
			logger.warn("Could not connect to server.", e);
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
		
		try {
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
			throw new ControllerException(e);
		}
	}
	
	/**
	 * Returns the state of a channel with the specified id.
	 * 
	 * @param channelId
	 * @return
	 * @throws ControllerException
	 */
	private Status.State getState(int channelId) throws ControllerException {
		logger.debug("retrieving channel state: " + channelId);
		
		try {
			jmxConnection = new JMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", channelId + "ComponentService");

			if ((Boolean) jmxConnection.getAttribute(properties, "Paused")) {
				return Status.State.PAUSED;
			} else if ((Boolean) jmxConnection.getAttribute(properties, "Stopped")) {
				return Status.State.STOPPED;
			} else {
				return Status.State.STARTED;
			}
		} catch (Exception e) {
			throw new ControllerException(e);
		}
	}
}
