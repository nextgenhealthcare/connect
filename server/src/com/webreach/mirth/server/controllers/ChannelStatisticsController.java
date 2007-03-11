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

import org.apache.log4j.Logger;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.webreach.mirth.model.ChannelStatistics;
import com.webreach.mirth.server.util.SqlConfig;

/**
 * The StatisticsContoller provides access to channel statistics.
 * 
 * @author GeraldB
 * 
 */
public class ChannelStatisticsController {
	private Logger logger = Logger.getLogger(this.getClass());
	private SqlMapClient sqlMap = SqlConfig.getSqlMapInstance();

	public ChannelStatistics getStatistics(String channelId) throws ControllerException {
		ChannelStatistics statistics = new ChannelStatistics();
		statistics.setChannelId(channelId);
		statistics.setReceivedCount(getReceivedCount(channelId));
		statistics.setSentCount(getSentCount(channelId));
		statistics.setErrorCount(getErrorCount(channelId));
		statistics.setFilteredCount(getFilteredCount(channelId));
		return statistics;
	}

	public Integer getReceivedCount(String channelId) throws ControllerException {
		try {
			return (Integer) sqlMap.queryForObject("getReceivedCount", channelId);
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}

	public Integer getSentCount(String channelId) throws ControllerException {
		try {
			return (Integer) sqlMap.queryForObject("getSentCount", channelId);
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}

	public Integer getErrorCount(String channelId) throws ControllerException {
		try {
			return (Integer) sqlMap.queryForObject("getErrorCount", channelId);
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}

	public Integer getFilteredCount(String channelId) throws ControllerException {
		try {
			return (Integer) sqlMap.queryForObject("getFilteredCount", channelId);
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}
}