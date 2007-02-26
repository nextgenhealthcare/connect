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
import com.webreach.mirth.model.Alert;
import com.webreach.mirth.server.util.SqlConfig;

public class AlertController {
	private Logger logger = Logger.getLogger(this.getClass());
	private SqlMapClient sqlMap = SqlConfig.getSqlMapInstance();

	public List<Alert> getAlert(Alert alert) throws ControllerException {
		logger.debug("getting alert: " + alert);

		try {
			return sqlMap.queryForList("getAlert", alert);
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}

	public void updateAlert(Alert alert) throws ControllerException {
		try {
			if (alert.getId() == null) {
				logger.debug("adding alert: " + alert);
				sqlMap.insert("insertAlert", alert);
			} else {
				logger.debug("updating alert: " + alert);
				sqlMap.update("updateAlert", alert);
			}
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}

	public void removeUser(Alert alert) throws ControllerException {
		logger.debug("removing alert: " + alert);

		try {
			sqlMap.delete("deleteAlert", alert);
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}
}
