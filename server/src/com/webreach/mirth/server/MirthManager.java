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

package com.webreach.mirth.server;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.ChannelStatistics;
import com.webreach.mirth.model.ChannelStatus;
import com.webreach.mirth.model.ConnectorMetaData;
import com.webreach.mirth.model.DriverInfo;
import com.webreach.mirth.model.User;
import com.webreach.mirth.model.filters.MessageObjectFilter;
import com.webreach.mirth.server.controllers.ChannelController;
import com.webreach.mirth.server.controllers.ChannelStatisticsController;
import com.webreach.mirth.server.controllers.ChannelStatusController;
import com.webreach.mirth.server.controllers.ConfigurationController;
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.ExtensionController;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.controllers.EventController;
import com.webreach.mirth.server.controllers.UserController;

public class MirthManager {
	private ChannelController channelController = ControllerFactory.getFactory().createChannelController();
	private ChannelStatusController channelStatusController = ControllerFactory.getFactory().createChannelStatusController();
	private ChannelStatisticsController channelStatisticsController = ControllerFactory.getFactory().createChannelStatisticsController();
	private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
	private MessageObjectController messageObjectController = ControllerFactory.getFactory().createMessageObjectController();
	private EventController systemLogger = ControllerFactory.getFactory().createEventController();
    private ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
	private UserController userController = ControllerFactory.getFactory().createUserController();

	/**
	 * Returns a List of all channels.
	 * 
	 * @return
	 * @throws MirthException
	 */
	public List<Channel> getChannel(Channel channel) throws MirthException {
		try {
			return channelController.getChannel(channel);
		} catch (ControllerException e) {
			throw new MirthException(e);
		}
	}

	/**
	 * Updates the specified channel.
	 * 
	 * @param channel
	 * @throws MirthException
	 */
	public boolean updateChannel(Channel channel, boolean override) throws MirthException {
		try {
			return channelController.updateChannel(channel, override);
		} catch (ControllerException e) {
			throw new MirthException(e);
		}
	}

	/**
	 * Removes the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws MirthException
	 */
	public void removeChannel(Channel channel) throws MirthException {
		try {
			channelController.removeChannel(channel);
		} catch (ControllerException e) {
			throw new MirthException(e);
		}
	}

	/**
	 * Returns a List of all transports.
	 * 
	 * @return
	 * @throws MirthException
	 */
	public Map<String, ConnectorMetaData> getTransports() throws MirthException {
		try {
			return extensionController.getConnectorMetaData();
		} catch (ControllerException e) {
			throw new MirthException(e);
		}
	}

	/**
	 * Returns a List of users.
	 * 
	 * @return
	 * @throws MirthException
	 */
	public List<User> getUser(User user) throws MirthException {
		try {
			return userController.getUser(user);
		} catch (ControllerException e) {
			throw new MirthException(e);
		}
	}

	/**
	 * Updates a specified user.
	 * 
	 * @param user
	 * @throws MirthException
	 */
	public void updateUser(User user, String password) throws MirthException {
		try {
			userController.updateUser(user, password);
		} catch (ControllerException e) {
			throw new MirthException(e);
		}
	}

	/**
	 * Removes the user.
	 * 
	 * @param user
	 * @throws MirthException
	 */
	public void removeUser(User user) throws MirthException {
		try {
			userController.removeUser(user);
		} catch (ControllerException e) {
			throw new MirthException(e);
		}
	}

	/**
	 * Returns a Properties object with all server configuration properties.
	 * 
	 * @return
	 * @throws MirthException
	 */
	public Properties getServerProperties() throws MirthException {
		try {
			return configurationController.getServerProperties();
		} catch (ControllerException e) {
			throw new MirthException(e);
		}
	}

	/**
	 * Updates the server configuration properties.
	 * 
	 * @param properties
	 * @throws MirthException
	 */
	public void setServerProperties(Properties properties) throws MirthException {
		try {
			configurationController.setServerProperties(properties);
		} catch (ControllerException e) {
			throw new MirthException(e);
		}
	}

	/**
	 * Deploys all channels.
	 * 
	 * @throws MirthException
	 */
	public void deployChannels() throws MirthException {
		try {
			configurationController.deployChannels();
		} catch (ControllerException e) {
			throw new MirthException(e);
		}
	}

	/**
	 * Starts the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws MirthException
	 */
	public void startChannel(String channelId) throws MirthException {
		try {
			channelStatusController.startChannel(channelId);
		} catch (ControllerException e) {
			throw new MirthException(e);
		}
	}

	/**
	 * Stops the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws MirthException
	 */
	public void stopChannel(String channelId) throws MirthException {
		try {
			channelStatusController.stopChannel(channelId);
		} catch (ControllerException e) {
			throw new MirthException(e);
		}
	}

	/**
	 * Pauses the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws MirthException
	 */
	public void pauseChannel(String channelId) throws MirthException {
		try {
			channelStatusController.pauseChannel(channelId);
		} catch (ControllerException e) {
			throw new MirthException(e);
		}
	}

	/**
	 * Resumes the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws MirthException
	 */
	public void resumeChannel(String channelId) throws MirthException {
		try {
			channelStatusController.resumeChannel(channelId);
		} catch (ControllerException e) {
			throw new MirthException(e);
		}
	}

	/**
	 * Returns the Statistics for the channel with the specified id.
	 * 
	 * @return
	 * @throws MirthException
	 */
	public ChannelStatistics getStatistics(String channelId) throws MirthException {
		return channelStatisticsController.getStatistics(channelId);
	}

	/**
	 * Clears the system event list.
	 * 
	 * @throws MirthException
	 */
	public void clearSystemEvents() throws MirthException {
		try {
			systemLogger.clearSystemEvents();
		} catch (ControllerException e) {
			throw new MirthException(e);
		}
	}

	/**
	 * Removes the message event with the specified id.
	 * 
	 * @param messageId
	 * @throws MirthException
	 */
	public void removeMessages(MessageObjectFilter filter) throws MirthException {
		try {
			messageObjectController.removeMessages(filter);
		} catch (ControllerException e) {
			throw new MirthException(e);
		}
	}

	/**
	 * Clears the message list for the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws MirthException
	 */
	public void clearMessages(String channelId) throws MirthException {
		try {
			messageObjectController.clearMessages(channelId);
		} catch (ControllerException e) {
			throw new MirthException(e);
		}
	}

	/**
	 * Returns the channel status list.
	 * 
	 * @return
	 * @throws MirthException
	 */
	public List<ChannelStatus> getChannelStatusList() {
		return channelStatusController.getChannelStatusList();
	}

	/**
	 * Returns the database driver list.
	 * 
	 * @return
	 * @throws MirthException
	 */
	public List<DriverInfo> getDatabaseDrivers() throws MirthException {
		try {
			return configurationController.getDatabaseDrivers();
		} catch (ControllerException e) {
			throw new MirthException(e);
		}
	}

	/**
	 * Returns the version of the Mirth server.
	 * 
	 * @return
	 * @throws MirthException
	 */
	public String getVersion() {
		return configurationController.getServerVersion();
	}

	/**
	 * Returns the build date of the Mirth server.
	 * 
	 * @return
	 * @throws MirthException
	 */
	public String getBuildDate() {
		return configurationController.getBuildDate();
	}
}
