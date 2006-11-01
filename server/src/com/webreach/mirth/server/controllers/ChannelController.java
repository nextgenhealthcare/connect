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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.truemesh.squiggle.MatchCriteria;
import com.truemesh.squiggle.Order;
import com.truemesh.squiggle.SelectQuery;
import com.truemesh.squiggle.Table;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.server.util.DatabaseConnection;
import com.webreach.mirth.server.util.DatabaseConnectionFactory;
import com.webreach.mirth.server.util.DatabaseUtil;

/**
 * The ChannelController provides access to channels.
 * 
 * @author GeraldB
 * 
 */
public class ChannelController {
	private Logger logger = Logger.getLogger(this.getClass());
	private ObjectXMLSerializer serializer = new ObjectXMLSerializer();

	/**
	 * Returns a List containing the Channel with the specified
	 * <code>channelId</code>. If the <code>channelId</code> is
	 * <code>null</code>, all channels are returned.
	 * 
	 * @param channelId
	 *            the ID of Channel to be returned.
	 * @return a List containing the Channel with the specified
	 *         <code>channelId</code>, a List containing all channels
	 *         otherwise.
	 * @throws ControllerException
	 */
	public List<Channel> getChannels(String channelId) throws ControllerException {
		logger.debug("retrieving channel list: channelId=" + channelId);

		DatabaseConnection dbConnection = null;
		ResultSet result = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();

			Table channels = new Table("channels");
			SelectQuery select = new SelectQuery(channels);

			select.addColumn(channels, "id");
			select.addColumn(channels, "channel_data");

			if (channelId != null) {
				select.addCriteria(new MatchCriteria(channels, "id", MatchCriteria.EQUALS, channelId));
			}
			
			select.addOrder(channels, "date_created", Order.ASCENDING);

			result = dbConnection.executeQuery(select.toString());
			return getChannelList(result);
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(result);
			DatabaseUtil.close(dbConnection);
		}
	}

	/**
	 * Converts a ResultSet to a List of Channel objects.
	 * 
	 * @param result
	 *            the ResultSet to be converted.
	 * @return a List of Channel objects.
	 * @throws SQLException
	 */
	private List<Channel> getChannelList(ResultSet result) throws SQLException {
		ArrayList<Channel> channels = new ArrayList<Channel>();
		
		while (result.next()) {
			Channel channel = (Channel) serializer.fromXML(result.getString("channel_data"));
			channel.setId(result.getString("id"));
			channels.add(channel);
		}

		return channels;
	}

	/**
	 * If a Channel with the specified Channel's ID already exists, the Channel
	 * will be updated. Otherwise, the Channel will be added.
	 * 
	 * @param channel
	 *            Channel to be updated.
	 * @throws ControllerException
	 */
	public boolean updateChannel(Channel channel, boolean override) throws ControllerException {
		DatabaseConnection dbConnection = null;

		// if it's not a new channel, and its version is different from the one
		// in the database, and override is not enabled
		if ((channel.getRevision() > 0) && !getChannels(channel.getId()).isEmpty() && (getChannels(channel.getId()).get(0).getVersion() != channel.getVersion()) && !override) {
			return false;
		} else {
			channel.setVersion(channel.getVersion() + 1);
		}
		
		ConfigurationController configurationController = new ConfigurationController();
		channel.setVersion(configurationController.getVersion());

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();

			String statement = null;
			ArrayList<Object> parameters = new ArrayList<Object>();
			boolean isNewChannel = getChannels(channel.getId()).isEmpty();
			
			if (isNewChannel) {
				logger.debug("inserting channel: channelId=" + channel.getId());
				statement = "insert into channels (id, channel_name, channel_data) values (?, ?, ?)";
				parameters.add(channel.getId());
				parameters.add(channel.getName());
				parameters.add(serializer.toXML(channel));
			} else {
				logger.debug("updating channel: channelId=" + channel.getId());
				statement = "update channels set channel_name = ?, channel_data = ? where id = ?";
				parameters.add(channel.getName());
				parameters.add(serializer.toXML(channel));
				parameters.add(channel.getId());
			}

			dbConnection.executeUpdate(statement, parameters);
			
			// if it's a new channel, create its associated statistics row
			if (isNewChannel) {
				ChannelStatisticsController statisticsController = new ChannelStatisticsController();
				statisticsController.createStatistics(channel.getId());
			}
			
			return true;
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(dbConnection);
		}
	}

	/**
	 * Removes the channel with the specified ID.
	 * 
	 * @param channelId
	 *            ID of channel to be removed.
	 * @throws ControllerException
	 */
	public void removeChannel(String channelId) throws ControllerException {
		logger.debug("removing channel: channelId=" + channelId);
		DatabaseConnection dbConnection = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			
			String statement = "delete from channels where id = ?";
			ArrayList<Object> parameters = new ArrayList<Object>();
			parameters.add(channelId);
			
			dbConnection.executeUpdate(statement, parameters);
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(dbConnection);
		}
	}
}
