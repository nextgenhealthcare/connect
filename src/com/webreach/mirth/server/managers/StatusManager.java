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

public class StatusManager {
	private Logger logger = Logger.getLogger(StatusManager.class);
	private JMXConnection jmxConnection = null;
	private ConfigurationManager configurationManager = new ConfigurationManager();

	/**
	 * Starts the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ManagerException
	 */
	public void startChannel(int channelId) throws ManagerException {
		logger.debug("starting channel: " + channelId);
		
		try {
			jmxConnection = new JMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", channelId + "ComponentService");
			jmxConnection.invokeOperation(properties, "start", null);
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/**
	 * Stops the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ManagerException
	 */
	public void stopChannel(int channelId) throws ManagerException {
		logger.debug("stopping channel: " + channelId);
		
		try {
			jmxConnection = new JMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", channelId + "ComponentService");
			jmxConnection.invokeOperation(properties, "stop", null);
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/**
	 * Pauses the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ManagerException
	 */
	public void pauseChannel(int channelId) throws ManagerException {
		logger.debug("pausing channel: " + channelId);
		
		try {
			jmxConnection = new JMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", channelId + "ComponentService");
			jmxConnection.invokeOperation(properties, "pause", null);
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}

	/**
	 * Resumes the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ManagerException
	 */
	public void resumeChannel(int channelId) throws ManagerException {
		logger.debug("resuming channel: " + channelId);
		
		try {
			jmxConnection = new JMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", channelId + "ComponentService");
			jmxConnection.invokeOperation(properties, "resume", null);
		} catch (Exception e) {
			throw new ManagerException(e);
		}
	}
	
	/**
	 * Returns a list of ChannelStatus objects representing the running channels. 
	 * 
	 * @return
	 * @throws ManagerException
	 */
	public List<Status> getStatusList() {
		logger.debug("retrieving channel status list");
		List<Status> channelStatusList = new ArrayList<Status>();

		try {
			ArrayList<String> deployedChannelIdList = (ArrayList<String>) getDeployedIds();
			
			for (Iterator iter = deployedChannelIdList.iterator(); iter.hasNext();) {
				String channelId = (String) iter.next();
				Status channelStatus = new Status();
				channelStatus.setState(getState(Integer.valueOf(channelId).intValue()));
				channelStatus.setId(Integer.valueOf(channelId).intValue());
				channelStatus.setName(configurationManager.getChannels(Integer.valueOf(channelId).intValue()).get(0).getName());
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
	 * @throws ManagerException
	 */
	private List<String> getDeployedIds() throws ManagerException {
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
			throw new ManagerException(e);
		}
	}
	
	/**
	 * Returns the state of a channel with the specified id.
	 * 
	 * @param channelId
	 * @return
	 * @throws ManagerException
	 */
	private Status.State getState(int channelId) throws ManagerException {
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
			throw new ManagerException(e);
		}
	}
}
