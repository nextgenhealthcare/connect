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
import java.util.List;

import org.apache.log4j.Logger;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.webreach.mirth.model.Channel;

public class ChannelController {
	private Logger logger = Logger.getLogger(this.getClass());
	private SqlMapClient sqlMap = SqlConfig.getSqlMapInstance();

	public List<Channel> getChannel(Channel channel) throws ControllerException {
		logger.debug("getting channel");

		try {
			return sqlMap.queryForList("getChannel", channel);
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}

	public boolean updateChannel(Channel channel, boolean override) throws ControllerException {
		// if it's not a new channel, and its version is different from the one
		// in the database, and override is not enabled
		if ((channel.getRevision() > 0) && !getChannel(channel).isEmpty() && (getChannel(channel).get(0).getVersion() != channel.getVersion()) && !override) {
			return false;
		} else {
			channel.setVersion(channel.getVersion() + 1);
		}

		ConfigurationController configurationController = new ConfigurationController();
		channel.setVersion(configurationController.getVersion());

		try {
			if (getChannel(channel).isEmpty()) {
				logger.debug("adding channel");
				sqlMap.insert("insertChannel", channel);

				ChannelStatisticsController statisticsController = new ChannelStatisticsController();
				statisticsController.createStatistics(channel.getId());
			} else {
				logger.debug("updating channel");
				sqlMap.update("updateChannel", channel);
			}
			
			return true;
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}

	public void removeChannel(Channel channel) throws ControllerException {
		logger.debug("removing channel");

		try {
			sqlMap.delete("deleteChannel", channel);
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}
}
