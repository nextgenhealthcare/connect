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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.webreach.mirth.model.Alert;
import com.webreach.mirth.server.util.SMTPConnection;
import com.webreach.mirth.server.util.SMTPConnectionFactory;
import com.webreach.mirth.server.util.SqlConfig;

public class AlertController {
	private Logger logger = Logger.getLogger(this.getClass());
	private SqlMapClient sqlMap = SqlConfig.getSqlMapInstance();

	public List<Alert> getAlert(Alert alert) throws ControllerException {
		logger.debug("getting alert: " + alert);

		try {
			List<Alert> alerts = sqlMap.queryForList("getAlert", alert);

			for (Iterator iter = alerts.iterator(); iter.hasNext();) {
				Alert currentAlert = (Alert) iter.next();

				List<String> channelIds = sqlMap.queryForList("getChannelIdsByAlertId", currentAlert.getId());
				currentAlert.setChannels(channelIds);

				List<String> emails = sqlMap.queryForList("getEmailsByAlertId", currentAlert.getId());
				currentAlert.setEmails(emails);
			}

			return alerts;
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}

	public List<Alert> getAlertByChannelId(String channelId) throws ControllerException {
		logger.debug("getting alert by channel id: " + channelId);

		try {
			List<Alert> alerts = sqlMap.queryForList("getAlertByChannelId", channelId);

			for (Iterator iter = alerts.iterator(); iter.hasNext();) {
				Alert currentAlert = (Alert) iter.next();

				List<String> channelIds = sqlMap.queryForList("getChannelIdsByAlertId", currentAlert.getId());
				currentAlert.setChannels(channelIds);

				List<String> emails = sqlMap.queryForList("getEmailsByAlertId", currentAlert.getId());
				currentAlert.setEmails(emails);
			}

			return alerts;
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}

	public void updateAlerts(List<Alert> alerts) throws ControllerException {
		for (Iterator iter = alerts.iterator(); iter.hasNext();) {
			Alert alert = (Alert) iter.next();
			removeAlertById(alert.getId());
			insertAlert(alert);
		}
	}

	private void insertAlert(Alert alert) throws ControllerException {
		try {
			Alert alertFilter = new Alert();
			alertFilter.setId(alert.getId());

			try {
				sqlMap.startTransaction();

				// insert the alert and its properties
				logger.debug("adding alert: " + alert);
				sqlMap.insert("insertAlert", alert);

				// insert the channel ID list
				logger.debug("adding channel alerts");

				List<String> channelIds = alert.getChannels();

				for (Iterator iter = channelIds.iterator(); iter.hasNext();) {
					String channelId = (String) iter.next();
					Map params = new HashMap();
					params.put("alertId", alert.getId());
					params.put("channelId", channelId);
					sqlMap.insert("insertChannelAlert", params);
				}

				// insert the email address list
				logger.debug("adding alert emails");

				List<String> emails = alert.getEmails();

				for (Iterator iter = emails.iterator(); iter.hasNext();) {
					String email = (String) iter.next();
					Map params = new HashMap();
					params.put("alertId", alert.getId());
					params.put("email", email);
					sqlMap.insert("insertAlertEmail", params);
				}

				sqlMap.commitTransaction();
			} finally {
				sqlMap.endTransaction();
			}
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}

	public void removeAlertById(String alertId) throws ControllerException {
		logger.debug("removing alert by id: " + alertId);

		try {
			sqlMap.delete("deleteAlertById", alertId);
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}

	public void sendAlerts(String channelId, String message) {
		try {
			List<Alert> alerts = getAlertByChannelId(channelId);

			if (!alerts.isEmpty()) {
				for (Iterator iter = alerts.iterator(); iter.hasNext();) {
					Alert alert = (Alert) iter.next();

					if (isAlertCondition(alert.getExpression(), message)) {
						sentAlertEmails(alert.getTemplate(), alert.getEmails());
					}
				}
			}
		} catch (ControllerException e) {
			logger.error(e);
		}
	}

	private boolean isAlertCondition(String expression, String message) {
		// TODO: is this accurate?
		return message.contains(expression);
	}

	private void sentAlertEmails(String template, List<String> emails) throws ControllerException {
		try {
			Properties properties = (new ConfigurationController()).getServerProperties();
			String fromAddress = properties.getProperty("smtp.from");
			String toAddressList = generateEmailList(emails);
			SMTPConnection connection = SMTPConnectionFactory.createSMTPConnection();
			connection.send(toAddressList, null, fromAddress, "Mirth Alert", template);
		} catch (Exception e) {
			throw new ControllerException(e);
		}
	}

	private String generateEmailList(List<String> emails) {
		StringBuilder emailList = new StringBuilder();

		for (Iterator iter = emails.iterator(); iter.hasNext();) {
			String email = (String) iter.next();
			emailList.append(email + ";");
		}

		return emailList.toString();
	}
}
