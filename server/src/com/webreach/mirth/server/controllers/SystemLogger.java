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
import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.model.filters.SystemEventFilter;

public class SystemLogger {
	private Logger logger = Logger.getLogger(this.getClass());
	private ObjectXMLSerializer serializer = new ObjectXMLSerializer();
	private SqlMapClient sqlMap = SqlConfig.getSqlMapInstance();

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
			return (List<SystemEvent>) sqlMap.queryForList("getEvent", filter);
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
}
