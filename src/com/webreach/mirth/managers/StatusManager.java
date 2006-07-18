/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.webreach.mirth.MirthCommand;
import com.webreach.mirth.MirthCommandQueue;

/**
 * The <code>StatusManager</code> class contatains information about the
 * status of the Mirth service.
 * 
 * @author <a href="mailto:geraldb@webreachinc.com">Gerald Bortis</a>
 */
public class StatusManager {
	protected transient Log logger = LogFactory.getLog(StatusManager.class);

	private boolean initialized = false;
	private ConfigurationManager configurationManager = ConfigurationManager.getInstance();
	private ChangeManager changeManager = ChangeManager.getInstance();
	private MirthCommandQueue commandQueue = MirthCommandQueue.getInstance();

	// singleton pattern
	private static StatusManager instance = null;

	private StatusManager() {}

	public static StatusManager getInstance() {
		synchronized (StatusManager.class) {
			if (instance == null)
				instance = new StatusManager();

			return instance;
		}
	}

	/**
	 * Initializes the StatusManager.
	 * 
	 */
	public void initialize() {
		if (initialized)
			return;

		// initialization code

		initialized = true;
	}

	/**
	 * Returns <code>true</code> if StatusManager has been initialized.
	 * 
	 * @return <code>true</code> if StatusManager has been initialized.
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Returns a list of names of the deployed channels.
	 * 
	 * @return a list of names of the deployed channels.
	 */
	public ArrayList<String> getDeployedChannelNames() throws ManagerException {
		ArrayList<String> channelNames = new ArrayList<String>();

		try {
			Set beanObjectNames = StatusUtil.getMBeanNames();

			for (Iterator iter = beanObjectNames.iterator(); iter.hasNext();) {
				ObjectName objectName = (ObjectName) iter.next();

				// only add valid mirth channels
				// format: MirthConfiguration:type=statistics,name=*
				if ((objectName.getKeyProperty("type") != null)
						&& objectName.getKeyProperty("type").equals("statistics")
						&& (objectName.getKeyProperty("name") != null)
						&& !objectName.getKeyProperty("name").startsWith("_")
						&& !objectName.getKeyProperty("name").startsWith("Mirth-")
						&& (objectName.getKeyProperty("router") == null)) {
					channelNames.add(objectName.getKeyProperty("name"));
				}
			}

			return channelNames;
		} catch (Exception e) {
			// Instead of throwing an exception when channel names cannot be loaded, show no channels.
			return new ArrayList<String>();
		}
	}

	/**
	 * Returns <code>true</code> if a channel is not paused and not stopped,
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if a channel is not paused and not stopped,
	 *         <code>false</code> otherwise.
	 */
	public boolean isChannelRunning(String name) throws ManagerException {
		try {
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", name + "ComponentService");
			boolean paused = ((Boolean) StatusUtil.getMBeanAttribute(properties, "Paused")).booleanValue();
			boolean stopped = ((Boolean) StatusUtil.getMBeanAttribute(properties, "Stopped")).booleanValue();
			return (!paused && !stopped);
		} catch (Exception e) {
			logger.warn(e.getMessage());
			throw new ManagerException("Could not retrieve status for channel: " + name, e);
		}
	}

	/**
	 * Returns <code>true</code> if the Mule engine has been initialized,
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the Mule engine has been initialized,
	 *         <code>false</code> otherwise.
	 */
	public boolean isMuleInitialized() throws ManagerException {
		try {
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", "MuleService");
			return ((Boolean) StatusUtil.getMBeanAttribute(properties, "Initialised")).booleanValue();
		} catch (Exception e) {
			logger.warn(e.getMessage());
			throw new ManagerException("Could not retrieve Mule initlialization status.", e);
		}
	}

	/**
	 * Returns HTML stats table for the specified channel.
	 * 
	 */
	public String getAllChannelStats() throws ManagerException {
		try {
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "statistics");
			return (String) StatusUtil.invokeMBeanOperation(properties, "printHtmlSummary", null);
		} catch (Exception e) {
			logger.warn(e.getMessage());
			throw new ManagerException("Could not retrieve all channel stats.", e);
		}
	}

	/**
	 * Returns a map of channel statistics.
	 * 
	 * @param channel
	 *            the name of the channel.
	 * @return a map of channel statistics.
	 */
	public HashMap<String, String> getChannelStats(String channel) {
		HashMap<String, String> channelStats = new HashMap<String, String>();
		channelStats.put("Total Messages Sent", Integer.toString(getChannelSentMessageCount(channel)));
		channelStats.put("Total Messages Received", Integer.toString(getChannelReceivedMessageCount(channel)));
		
		channelStats.put("Current Queue Size", Integer.toString(getQueuedMessageCount(channel)));
		channelStats.put("Average Queue Size", Integer.toString(getStatistic(channel, "AverageQueueSize")));
		channelStats.put("Maximum Queue Size", Integer.toString(getStatistic(channel, "MaxQueueSize")));
		
		channelStats.put("Execution Errors", Integer.toString(getChannelErrorCount(channel)));
		channelStats.put("Fatal Errors", Integer.toString(getStatistic(channel, "FatalErrors")));
		
		channelStats.put("Maximum Execution Time", Integer.toString(getStatistic(channel, "MaxExecutionTime")));
		channelStats.put("Minimum Execution Time", Integer.toString(getStatistic(channel, "MinExecutionTime")));
		channelStats.put("Average Execution Time", Integer.toString(getStatistic(channel, "AverageExecutionTime")));
		channelStats.put("Total Execution Time", Integer.toString(getStatistic(channel, "TotalExecutionTime")));
		
		return channelStats;
	}

	/**
	 * Returns the sent message count for the specified channel.
	 * 
	 * @param name
	 *            the name of the channel.
	 * @return the sent message count for the specified channel.
	 */
	public int getChannelSentMessageCount(String channel) {
		return getStatistic(channel, "TotalEventsSent");
	}

	/**
	 * Returns the recieved message count for the specified channel.
	 * 
	 * @param name
	 *            the name of the channel.
	 * @return the recieved message count for the specified channel.
	 */
	public int getChannelReceivedMessageCount(String name) throws ManagerException {
		try {
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "statistics");
			properties.put("name", name);
			
			// NOTE: counter hack to prevent incrementing count by 2
			Double count = ((Long) StatusUtil.getMBeanAttribute(properties, "TotalEventsReceived")).doubleValue();
			return Double.valueOf(Math.ceil(count / 2)).intValue();
			
//			return ((Long) StatusUtil.getMBeanAttribute(properties, "TotalEventsReceived")).intValue();
		} catch (Exception e) {
			logger.warn(e.getMessage());
			throw new ManagerException("Could not retrieve channel received message count.", e);
		}
	}

	/**
	 * Returns the execution error count for the specified channel.
	 * 
	 * @param name
	 *            the name of the channel.
	 * @return the execution error count for the specified channel.
	 */
	public int getChannelErrorCount(String channel) throws ManagerException {
		return getStatistic(channel, "ExecutionErrors");
	}

	/**
	 * Returns the execution error count for the specified channel.
	 * 
	 * @param name
	 *            the name of the channel.
	 * @return the execution error count for the specified channel.
	 */
	public int getQueuedMessageCount(String channel) throws ManagerException {
		return getStatistic(channel, "QueuedEvents");
	}

	private int getStatistic(String channel, String statistic) {
		try {
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "statistics");
			properties.put("name", channel);
			return ((Long) StatusUtil.getMBeanAttribute(properties, statistic)).intValue();
		} catch (Exception e) {
			logger.warn(e.getMessage());
			throw new ManagerException("Could not retrieve error count for channel: " + channel, e);
		}
	}
	
	/**
	 * Clears the stats table for the specified channel.
	 * 
	 * @param name
	 *            the name of the channel.
	 */
	public void clearChannelStats(String name) throws ManagerException {
		try {
			logger.debug("clearing stats for channel: " + name);

			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "statistics");
			properties.put("name", name);

			StatusUtil.invokeMBeanOperation(properties, "clear", null);
		} catch (Exception e) {
			logger.warn(e.getMessage());
			throw new ManagerException("Error clearning stats for channel: " + name, e);
		}
	}

	/**
	 * Updates the Mule configuration file and restarts the Mule engine.
	 * 
	 */
	public void deployChannels() throws ManagerException {
		try {
			logger.debug("deploying all channels");
			configurationManager.marshallMule();
			commandQueue.addCommand(new MirthCommand(MirthCommand.CMD_RESTART_MULE, ConfigurationManager.MULE_CONFIG_FILE));
			changeManager.resetChannels();
			changeManager.setConfigurationChanged(false);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new ManagerException("Error deploying channels. Please check logs.");
		}
	}

	/**
	 * Pauses the specified channel.
	 * 
	 * @param name
	 *            the name of the channel.
	 */
	public void pauseChannel(String name) throws ManagerException {
		try {
			logger.debug("pausing channel: " + name);
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", name + "ComponentService");
			StatusUtil.invokeMBeanOperation(properties, "pause", null);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new ManagerException("Error pausing channel: " + name, e);
		}
	}

	/**
	 * Resumes the specified channel.
	 * 
	 * @param name
	 *            the name of the channel.
	 */
	public void resumeChannel(String name) throws ManagerException {
		try {
			logger.debug("resuming channel: " + name);
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", name + "ComponentService");
			StatusUtil.invokeMBeanOperation(properties, "resume", null);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new ManagerException("Error resuming channel: " + name, e);
		}
	}
	
	/**
	 * Stops the specified channel.
	 * 
	 * @param name
	 *            the name of the channel.
	 */
	public void stopChannel(String name) throws ManagerException {
		try {
			logger.debug("stopping channel: " + name);
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", name + "ComponentService");
			StatusUtil.invokeMBeanOperation(properties, "stop", null);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new ManagerException("Error stopping channel: " + name, e);
		}
	}
	
	/**
	 * Starts the specified channel.
	 * 
	 * @param name
	 *            the name of the channel.
	 */
	public void startChannel(String name) throws ManagerException {
		try {
			logger.debug("starting channel: " + name);
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "control");
			properties.put("name", name + "ComponentService");
			StatusUtil.invokeMBeanOperation(properties, "start", null);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new ManagerException("Error starting channel: " + name, e);
		}
	}
}
