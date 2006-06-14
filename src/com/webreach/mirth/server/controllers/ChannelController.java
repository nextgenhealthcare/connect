package com.webreach.mirth.server.controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.truemesh.squiggle.MatchCriteria;
import com.truemesh.squiggle.SelectQuery;
import com.truemesh.squiggle.Table;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.converters.ObjectSerializer;
import com.webreach.mirth.server.util.DatabaseConnection;
import com.webreach.mirth.server.util.DatabaseUtil;

/**
 * The ChannelController provides access to channels.
 * 
 * @author GeraldB
 * 
 */
public class ChannelController {
	private Logger logger = Logger.getLogger(ChannelController.class);
	private DatabaseConnection dbConnection;
	private ObjectSerializer serializer = new ObjectSerializer();

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
	public List<Channel> getChannels(Integer channelId) throws ControllerException {
		logger.debug("retrieving channel list: channel id = " + channelId);

		ResultSet result = null;

		try {
			dbConnection = new DatabaseConnection();

			Table channels = new Table("channels");
			SelectQuery select = new SelectQuery(channels);

			select.addColumn(channels, "id");
			select.addColumn(channels, "channel_data");

			if (channelId != null) {
				select.addCriteria(new MatchCriteria(channels, "id", MatchCriteria.EQUALS, channelId.toString()));
			}

			result = dbConnection.query(select.toString());
			return getChannelList(result);
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
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
			channel.setId(result.getInt("id"));
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
	public void updateChannel(Channel channel) throws ControllerException {
		try {
			dbConnection = new DatabaseConnection();
			StringBuilder statement = new StringBuilder();

			if (getChannels(channel.getId()).isEmpty()) {
				logger.debug("inserting channel: " + channel.getId());
				statement.append("INSERT INTO CHANNELS (ID, CHANNEL_NAME, CHANNEL_DATA) VALUES(");
				statement.append(channel.getId() + ",");
				statement.append("'" + channel.getName() + "',");
				statement.append("'" + serializer.toXML(channel) + "');");
			} else {
				logger.debug("updating channel: " + channel.getId());
				statement.append("UPDATE CHANNELS SET ");
				statement.append("CHANNEL_NAME = '" + channel.getName() + "', ");
				statement.append("CHANNEL_DATA = '" + serializer.toXML(channel) + "'");
				statement.append(" WHERE ID = " + channel.getId() + ";");
			}

			dbConnection.update(statement.toString());
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			dbConnection.close();
		}
	}

	/**
	 * Removes the channel with the specified ID.
	 * 
	 * @param channelId
	 *            ID of channel to be removed.
	 * @throws ControllerException
	 */
	public void removeChannel(int channelId) throws ControllerException {
		logger.debug("removing channel: " + channelId);

		try {
			dbConnection = new DatabaseConnection();
			StringBuilder statement = new StringBuilder();
			statement.append("DELETE FROM CHANNELS");
			statement.append(" WHERE ID = " + channelId + ";");
			dbConnection.update(statement.toString());
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			dbConnection.close();
		}
	}
}
