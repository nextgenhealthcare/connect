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
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.model.filters.SystemEventFilter;
import com.webreach.mirth.server.util.SqlConfig;

public class SystemLogger {
	private Logger logger = Logger.getLogger(this.getClass());
	private SqlMapClient sqlMap = SqlConfig.getSqlMapInstance();

	private static SystemLogger instance = null;
	
	private SystemLogger() {
		
	}
	
	public static SystemLogger getInstance() {
		synchronized (SystemLogger.class) {
			if (instance == null) {
				instance = new SystemLogger();
			}
			
			return instance;
		}
	}   
	
	/**
	 * Adds a new system event.
	 * 
	 * @param systemEvent
	 * @throws ControllerException
	 */
	public void logSystemEvent(SystemEvent systemEvent) {
		logger.debug("adding log event: " + systemEvent);

		try {
			sqlMap.insert("insertEvent", systemEvent);
		} catch (Exception e) {
			logger.error("could not log system event", e);
		}
	}

	/**
	 * Returns a List of all system events.
	 * 
	 * @param channelId
	 * @return
	 * @throws ControllerException
	 */
	public List<SystemEvent> getSystemEvents(SystemEventFilter filter) throws ControllerException {
		logger.debug("retrieving log event list: " + filter);

		try {
			return (List<SystemEvent>) sqlMap.queryForList("getEvent", getFilterMap(filter));
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}

	/**
	 * Clears the sysem event list.
	 * 
	 */
	public void clearSystemEvents() throws ControllerException {
		logger.debug("clearing system event list");

		try {
			sqlMap.delete("deleteEvent");
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}
	
	private Map getFilterMap(SystemEventFilter filter) {
		Map parameterMap = new HashMap();
		parameterMap.put("event", filter.getEvent());
		parameterMap.put("level", filter.getLevel());
		
		if (filter.getStartDate() != null) {
			parameterMap.put("startDate", String.format("%1$tY-%1$tm-%1$td 00:00:00", filter.getStartDate()));	
		}
		
		if (filter.getEndDate() != null) {
			parameterMap.put("endDate", String.format("%1$tY-%1$tm-%1$td 23:59:59", filter.getEndDate()));	
		}

		return parameterMap;
	}
}
