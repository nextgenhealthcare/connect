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

package com.webreach.mirth.server.controllers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.ChannelSummary;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.util.ImportConverter;
import com.webreach.mirth.server.util.SqlConfig;
import com.webreach.mirth.util.PropertyVerifier;
import com.webreach.mirth.util.QueueUtil;

public class ChannelController {
	private Logger logger = Logger.getLogger(this.getClass());
	private SqlMapClient sqlMap = SqlConfig.getSqlMapInstance();
	private static HashMap<String, Channel> channelCache = new HashMap<String, Channel>();
	private static HashMap<String, String> channelIdLookup = new HashMap<String, String>();
	private ChannelStatisticsController statisticsController = ChannelStatisticsController.getInstance();
	private ExtensionController extensionController = ExtensionController.getInstance();
	private ConfigurationController configurationController = ConfigurationController.getInstance();

	private static ChannelController instance = null;

	private ChannelController() {

	}

	public static ChannelController getInstance() {
		synchronized (ChannelController.class) {
			if (instance == null) {
				instance = new ChannelController();
			}

			return instance;
		}
	}

	public void initialize() {
		try {
			updateChannelCache(getChannel(null));

			for (Channel channel : channelCache.values()) {
				if (!channel.getVersion().equals(configurationController.getServerVersion())) {
					Channel updatedChannel = ImportConverter.convertChannelObject(channel);
					PropertyVerifier.checkChannelProperties(updatedChannel);
					PropertyVerifier.checkConnectorProperties(updatedChannel, extensionController.getConnectorMetaData());
					updatedChannel.setVersion(configurationController.getServerVersion());
					updateChannel(updatedChannel, true);
				}
				if (!statisticsController.checkIfStatisticsExist(channel.getId())) {
					statisticsController.createStatistics(channel.getId());
				}
			}

			statisticsController.reloadLocalCache();
		} catch (Exception e) {
			logger.warn(e);
		}
	}

	public void updateChannelCache(List<Channel> channels) throws ControllerException {
		channelCache = new HashMap<String, Channel>();
		channelIdLookup = new HashMap<String, String>();

		Iterator<Channel> it = channels.iterator();

		while (it.hasNext()) {
			Channel channel = it.next();
			updateChannelInCache(channel);
		}
	}

	public static String getChannelId(String channelName) {
		return channelIdLookup.get(channelName.toUpperCase());
	}

	public static String getDestinationName(String id) {
		// String format: channelid_destination_index
		String destinationName = id; // if we can't parse the name, just use
		// the id
		String channelId = id.substring(0, id.indexOf('_'));
		String strIndex = id.substring(id.indexOf("destination_") + 12, id.indexOf("_connector"));
		int index = Integer.parseInt(strIndex) - 1;
		Channel channel = channelCache.get(channelId);

		if (channel != null) {
			if (index < channel.getDestinationConnectors().size())
				destinationName = channel.getDestinationConnectors().get(index).getName();
		}

		return destinationName;
	}
	
	public String getConnectorId(String channelId, String connectorName) throws Exception {
		Channel filterChannel = new Channel();
		filterChannel.setId(channelId);
		int index = 1;
		
		for (Connector connector : getChannel(filterChannel).get(0).getDestinationConnectors()) {
			if (connector.getName().equals(connectorName)) {
				return String.valueOf(index);
			} else {
				index++;
			}
		}
		
		throw new Exception("Connector name not found");
	}

	public List<Channel> getChannel(Channel channel) throws ControllerException {
		logger.debug("getting channel");

		try {
			return sqlMap.queryForList("getChannel", channel);
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}

	public List<Channel> getEnabledChannels() throws ControllerException {
		List<Channel> channels = getChannel(null);

		for (int i = 0; i < channels.size(); i++) {
			if (!channels.get(i).isEnabled())
				channels.remove(i);
		}

		return channels;
	}

	public List<ChannelSummary> getChannelSummary(Map<String, Integer> cachedChannels) throws ControllerException {
		logger.debug("getting channel summary");
		List<ChannelSummary> channelSummaries = new ArrayList<ChannelSummary>();

		try {
			Map<String, Integer> serverChannels = sqlMap.queryForMap("getChannelRevision", null, "id", "revision");

			/*
			 * Iterate through the cached channel list and check if a channel
			 * with the id exists on the server. If it does, and the revision
			 * numbers aren't equal, then add the channel to the updated list.
			 * Otherwise, if the channel is not found, add it to the deleted
			 * list.
			 * 
			 */
			for (Iterator iter = cachedChannels.keySet().iterator(); iter.hasNext();) {
				String cachedChannelId = (String) iter.next();
				boolean channelExistsOnServer = false;

				// iterate through all of the channels on the server
				for (Iterator iterator = serverChannels.entrySet().iterator(); iterator.hasNext() && !channelExistsOnServer;) {
					Entry entry = (Entry) iterator.next();
					String id = (String) entry.getKey();
					Integer revision = (Integer) entry.getValue();

					// if the channel with the cached id exists
					if (id.equals(cachedChannelId)) {
						// and the revision numbers aren't equal, add it as
						// updated
						if (!revision.equals(cachedChannels.get(cachedChannelId))) {
							ChannelSummary summary = new ChannelSummary();
							summary.setId(id);
							channelSummaries.add(summary);
						}

						channelExistsOnServer = true;
					}
				}

				// if a channel with the id is never found on the server, add it
				// as deleted
				if (!channelExistsOnServer) {
					ChannelSummary summary = new ChannelSummary();
					summary.setId(cachedChannelId);
					summary.setDeleted(true);
					channelSummaries.add(summary);
				}
			}

			/*
			 * Iterate through the server channel list, check if every id exists
			 * in the cached channel list. If it doesn't, add it to the summary
			 * list as added.
			 * 
			 */
			for (Iterator iter = serverChannels.entrySet().iterator(); iter.hasNext();) {
				Entry entry = (Entry) iter.next();
				String id = (String) entry.getKey();

				if (!cachedChannels.keySet().contains(id)) {
					ChannelSummary summary = new ChannelSummary();
					summary.setId(id);
					summary.setAdded(true);
					channelSummaries.add(summary);
				}
			}

			return channelSummaries;
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}

	public boolean updateChannel(Channel channel, boolean override) throws ControllerException {
		// if it's not a new channel, and its version is different from the one
		// in the database, and override is not enabled
		if ((channel.getRevision() > 0) && !getChannel(channel).isEmpty() && (!getChannel(channel).get(0).getVersion().equals(channel.getVersion())) && !override) {
			return false;
		} else {
			channel.setRevision(channel.getRevision() + 1);
		}

		ConfigurationController configurationController = ConfigurationController.getInstance();
		channel.setVersion(configurationController.getServerVersion());

		try {

			updateChannelInCache(channel);

			Channel channelFilter = new Channel();
			channelFilter.setId(channel.getId());

			if (getChannel(channelFilter).isEmpty()) {
				logger.debug("adding channel");
				sqlMap.insert("insertChannel", channel);
			} else {
				logger.debug("updating channel");
				sqlMap.update("updateChannel", channel);
			}
			return true;
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}

	/**
	 * Removes a channel. If the channel is NULL, then all channels are removed.
	 * 
	 * @param channel
	 * @throws ControllerException
	 */
	public void removeChannel(Channel channel) throws ControllerException {
		logger.debug("removing channel");

		try {
			if (channel != null) {
				QueueUtil.getInstance().removeAllQueuesForChannel(channel);
				removeChannelFromCache(channel.getId());
			} else {
				QueueUtil.getInstance().removeAllQueues();
				clearChannelCache();
			}

			sqlMap.delete("deleteChannel", channel);
		} catch (Exception e) {
			throw new ControllerException(e);
		}
	}

	public static HashMap<String, Channel> getChannelCache() {
		return channelCache;
	}

	private static void removeChannelFromCache(String channelId) {
		channelCache.remove(channelId);
		channelIdLookup.remove(channelId);
	}

	private static void clearChannelCache() {
		channelCache.clear();
		channelIdLookup.clear();
	}

	private static void updateChannelInCache(Channel channel) {
		channelCache.put(channel.getId(), channel);
		channelIdLookup.put(channel.getName().toUpperCase(), channel.getId());
	}

	public static void setChannelCache(HashMap<String, Channel> channelCache) {
		ChannelController.channelCache = channelCache;
	}
}
