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
import com.webreach.mirth.server.builders.ErrorBuilder;
import com.webreach.mirth.server.util.SMTPConnection;
import com.webreach.mirth.server.util.SMTPConnectionFactory;
import com.webreach.mirth.server.util.SqlConfig;
import com.webreach.mirth.server.util.TemplateEvaluator;

public class AlertController {
	private Logger logger = Logger.getLogger(this.getClass());
	private SqlMapClient sqlMap = SqlConfig.getSqlMapInstance();
	private ErrorBuilder errorBuilder = new ErrorBuilder();
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
		// remove all alerts
		removeAlert(null);

		for (Iterator iter = alerts.iterator(); iter.hasNext();) {
			Alert alert = (Alert) iter.next();
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

	public void removeAlert(Alert alert) throws ControllerException {
		logger.debug("removing alert: " + alert);

		try {
			sqlMap.delete("deleteAlert", alert);
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}

	public void sendAlerts(String channelId, String errorMessage) {
		try {
			List<Alert> alerts = getAlertByChannelId(channelId);

			for (Iterator iter = alerts.iterator(); iter.hasNext();) {
				Alert alert = (Alert) iter.next();

				if (alert.isEnabled() && isAlertCondition(alert.getExpression(), errorMessage)) {
					sentAlertEmails(alert.getEmails(), alert.getTemplate(), errorMessage);
				}
			}
		} catch (ControllerException e) {
			logger.error(e);
		}
	}
	public void sendAlerts(String channelId, String errorType, String customMessage, Throwable e) {
		String errorMessage = errorBuilder.getErrorString(errorType, customMessage, e);
		sendAlerts(channelId, errorMessage);
	}
	private boolean isAlertCondition(String expression, String errorMessage) {
		// TODO: is this accurate?
		return errorMessage.contains(expression);
	}

	private void sentAlertEmails(List<String> emails, String template, String errorMessage) throws ControllerException {
		try {
			Properties properties = (new ConfigurationController()).getServerProperties();
			String fromAddress = properties.getProperty("smtp.from");
			String toAddressList = generateEmailList(emails);

			TemplateEvaluator evaluator = new TemplateEvaluator();
			Map<String, Object> context = new HashMap<String, Object>();
			context.put("ERROR", errorMessage);
			context.put("SYSTIME", String.valueOf(System.currentTimeMillis()));
			String body = evaluator.evaluate(template, context);

			SMTPConnection connection = SMTPConnectionFactory.createSMTPConnection();
			connection.send(toAddressList, null, fromAddress, "Mirth Alert", body);
		} catch (Exception e) {
			throw new ControllerException(e);
		}
	}

	private String generateEmailList(List<String> emails) {
		StringBuilder emailAddressList = new StringBuilder();

		for (Iterator iter = emails.iterator(); iter.hasNext();) {
			String email = (String) iter.next();
			emailAddressList.append(email + ",");
		}

		return emailAddressList.toString();
	}
}
