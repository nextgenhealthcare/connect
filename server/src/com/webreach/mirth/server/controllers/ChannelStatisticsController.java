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
import java.util.Map;

import org.apache.log4j.Logger;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.webreach.mirth.model.ChannelStatistics;
import com.webreach.mirth.server.util.ChannelStatisticsCache;
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
	private static ChannelStatisticsCache statsCache;
    private ConfigurationController configurationController = new ConfigurationController();

	public void initialize() {
		logger.debug("initialzing statistics controller");
		
		statsCache = ChannelStatisticsCache.getInstance();
		
		try {
            Map parameterMap = new HashMap();
            parameterMap.put("serverId", configurationController.getServerId());
			statsCache.setCache((HashMap<String, ChannelStatistics>) sqlMap.queryForMap("getStatistics", parameterMap, "channelId"));
		} catch (SQLException e) {
			logger.error("Could not initialize channel statistics.");
		}
	}
	
    public void createStatistics(String channelId)
    {
        Map parameterMap = new HashMap();
        parameterMap.put("serverId", configurationController.getServerId());
        parameterMap.put("channelId", channelId);
        
        try{
            sqlMap.insert("createStatistics", parameterMap);
        } catch (SQLException e) {
            logger.warn("could not update statistics");
        }
    }
    
    public boolean checkIfStatisticsExist(String channelId)
    {
        try {
            
            Map parameterMap = new HashMap();
            parameterMap.put("serverId", configurationController.getServerId());
            parameterMap.put("channelId", channelId);
            
            Map<String, ChannelStatistics> tempStats = (HashMap<String, ChannelStatistics>) sqlMap.queryForMap("getStatistics", parameterMap, "channelId");
            if(tempStats != null && tempStats.size() > 0)
                return true;
        } catch (SQLException e) {
            logger.error("Could not get channel statistics.");
        }
        return false;
    }
    
	public ChannelStatistics getStatistics(String channelId) {
		return statsCache.getCache().get(channelId);
	}

	public void incrementReceivedCount(String channelId) {
		statsCache.getCache().get(channelId).setReceived(statsCache.getCache().get(channelId).getReceived() + 1);
		updateStatistics(channelId);
	}

	public void incrementSentCount(String channelId) {
		statsCache.getCache().get(channelId).setSent(statsCache.getCache().get(channelId).getSent() + 1);
		updateStatistics(channelId);
	}

	public void incrementFilteredCount(String channelId) {
		statsCache.getCache().get(channelId).setFiltered(statsCache.getCache().get(channelId).getFiltered() + 1);
		updateStatistics(channelId);
	}

	public void incrementErrorCount(String channelId) {
		statsCache.getCache().get(channelId).setError(statsCache.getCache().get(channelId).getError() + 1);
		updateStatistics(channelId);
	}

	public void incrementQueuedCount(String channelId) {
		statsCache.getCache().get(channelId).setQueued(statsCache.getCache().get(channelId).getQueued() + 1);
		updateStatistics(channelId);
	}

	public void decrementQueuedCount(String channelId) {
		statsCache.getCache().get(channelId).setQueued(statsCache.getCache().get(channelId).getQueued() - 1);
		updateStatistics(channelId);
	}

	private void updateStatistics(String channelId) {
		try {
			sqlMap.update("updateStatistics", statsCache.getCache().get(channelId));
		} catch (SQLException e) {
			logger.warn("could not update statistics");
		}
	}
	
	public void clearStatistics(String channelId, boolean received, boolean filtered, boolean queued, boolean sent, boolean errored) throws ControllerException {
        if(received)
            statsCache.getCache().get(channelId).setReceived(0);
        if(filtered)
            statsCache.getCache().get(channelId).setFiltered(0);
        if(queued)
            statsCache.getCache().get(channelId).setQueued(0);
        if(sent)
            statsCache.getCache().get(channelId).setSent(0);
        if(errored)
            statsCache.getCache().get(channelId).setError(0);
		
        updateStatistics(channelId) ;
	}
}