package com.webreach.mirth.server.controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.truemesh.squiggle.MatchCriteria;
import com.truemesh.squiggle.SelectQuery;
import com.truemesh.squiggle.Table;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.server.util.DatabaseConnection;
import com.webreach.mirth.server.util.DatabaseConnectionFactory;
import com.webreach.mirth.server.util.DatabaseUtil;
import com.webreach.mirth.util.PropertyLoader;

/**
 * The ChannelController provides access to channels.
 * 
 * @author GeraldB
 * 
 */
public class ChannelController {
	private Logger logger = Logger.getLogger(this.getClass());
	private ObjectXMLSerializer serializer = new ObjectXMLSerializer();
	private Properties versionProperties = PropertyLoader.loadProperties("version");

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
				select.addCriteria(new MatchCriteria(channels, "id", MatchCriteria.EQUALS, channelId.toString()));
			}

			result = dbConnection.executeQuery(select.toString());
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
	public boolean updateChannel(Channel channel, boolean override) throws ControllerException {
		DatabaseConnection dbConnection = null;

		// if it's not a new channel, and its version is different from the one
		// in the database, and override is not enabled
		if ((channel.getRevision() > 0) && !getChannels(channel.getId()).isEmpty() && (getChannels(channel.getId()).get(0).getVersion() != channel.getVersion()) && !override) {
			return false;
		} else {
			channel.setVersion(channel.getVersion() + 1);
		}
		
		String version = versionProperties.getProperty("mirth.version");
		channel.setVersion(version);

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();

			String statement = null;
			ArrayList<Object> parameters = new ArrayList<Object>();

			if (getChannels(channel.getId()).isEmpty()) {
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
			return true;
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
		logger.debug("removing channel: channelId=" + channelId);
		DatabaseConnection dbConnection = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			StringBuilder statement = new StringBuilder();
			statement.append("DELETE FROM CHANNELS");
			statement.append(" WHERE ID = " + channelId + ";");
			dbConnection.executeUpdate(statement.toString());
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			dbConnection.close();
		}
	}
}
