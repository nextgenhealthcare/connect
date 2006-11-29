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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.webreach.mirth.model.ChannelStatistics;
import com.webreach.mirth.server.util.JMXConnection;
import com.webreach.mirth.server.util.JMXConnectionFactory;

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
		ChannelStatistics currentStatistics = getStatisticsObject(channelId);

		// this is a fix for Mule's double counting of received messages
		int receivedCount = Double.valueOf(Math.ceil(currentStatistics.getReceivedCount() / 2)).intValue();
		currentStatistics.setReceivedCount(receivedCount);

		return currentStatistics;
	}

	/**
	 * Returns a Statistics object for the channel with the specified id.
	 * 
	 * @param channelId
	 * @return
	 * @throws ControllerException
	 */
	private ChannelStatistics getStatisticsObject(String channelId) throws ControllerException {
		logger.debug("retrieving statistics: channelId=" + channelId);

		try {
			return (ChannelStatistics) sqlMap.queryForObject("getStatistic", channelId);
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}

	public void createStatistics(String channelId) throws ControllerException {
		logger.debug("creating channel statistcs: channelId" + channelId);

		try {
			sqlMap.insert("createStatistic", channelId);
		} catch (Exception e) {
			throw new ControllerException(e);
		}
	}

	public void incReceivedCount(String channelId) throws ControllerException {
		logger.debug("incrementing received count: channelId=" + channelId);

		try {
			Map parameterMap = new HashMap();
			parameterMap.put("statistic", "received");
			parameterMap.put("channelId", channelId);
			sqlMap.update("updateStatistic", parameterMap);
		} catch (Exception e) {
			throw new ControllerException(e);
		}
	}

	public void incSentCount(String channelId) throws ControllerException {
		logger.debug("incrementing sent count: channelId=" + channelId);

		try {
			Map parameterMap = new HashMap();
			parameterMap.put("statistic", "sent");
			parameterMap.put("channelId", channelId);
			sqlMap.update("updateStatistic", parameterMap);
		} catch (Exception e) {
			throw new ControllerException(e);
		}
	}

	public void incErrorCount(String channelId) throws ControllerException {
		logger.debug("incrementing error: channelId=" + channelId);

		try {
			Map parameterMap = new HashMap();
			parameterMap.put("statistic", "errors");
			parameterMap.put("channelId", channelId);
			sqlMap.update("updateStatistic", parameterMap);
		} catch (Exception e) {
			throw new ControllerException(e);
		}
	}

	/**
	 * Clears all of the statistics for the channel with the specified id.
	 * 
	 * @param channelId
	 * @throws ControllerException
	 */
	public void clearStatistics(String channelId) throws ControllerException {
		logger.debug("clearing statistics: channelId=" + channelId);

		// clear the stats in Mule
		JMXConnection jmxConnection = null;

		try {
			jmxConnection = JMXConnectionFactory.createJMXConnection();
			Hashtable<String, String> properties = new Hashtable<String, String>();
			properties.put("type", "statistics");
			properties.put("name", channelId);
			jmxConnection.invokeOperation(properties, "clear", null, null);
		} catch (Exception e) {
			throw new ControllerException(e);
		} finally {
			if (jmxConnection != null) {
				jmxConnection.close();	
			} else {
				logger.warn("could not close JMX connection");
			}
		}

		// clear the stats in the database
		try {
			sqlMap.update("clearStatistic", channelId);
		} catch (Exception e) {
			throw new ControllerException(e);
		}
	}
}