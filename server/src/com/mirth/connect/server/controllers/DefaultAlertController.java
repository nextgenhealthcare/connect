/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.tools.generic.DateTool;
import org.mule.providers.TemplateValueReplacer;

import com.mirth.connect.model.Alert;
import com.mirth.connect.model.Channel;
import com.mirth.connect.server.builders.ErrorMessageBuilder;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.SMTPConnection;
import com.mirth.connect.server.util.SMTPConnectionFactory;
import com.mirth.connect.server.util.SqlConfig;
import com.mirth.connect.util.PropertyLoader;

public class DefaultAlertController extends AlertController {
    private Logger logger = Logger.getLogger(this.getClass());
    private ChannelStatisticsController statisticsController = ControllerFactory.getFactory().createChannelStatisticsController();
    private ErrorMessageBuilder errorBuilder = new ErrorMessageBuilder();

    private static DefaultAlertController instance = null;

    private DefaultAlertController() {

    }

    public static AlertController create() {
        synchronized (DefaultAlertController.class) {
            if (instance == null) {
                instance = new DefaultAlertController();
            }

            return instance;
        }
    }

    public List<Alert> getAlert(Alert alert) throws ControllerException {
        logger.debug("getting alert: " + alert);

        try {
            List<Alert> alerts = SqlConfig.getSqlMapClient().queryForList("Alert.getAlert", alert);

            for (Alert currentAlert : alerts) {
                List<String> channelIds = SqlConfig.getSqlMapClient().queryForList("Alert.getChannelIdsByAlertId", currentAlert.getId());
                currentAlert.setChannels(channelIds);

                List<String> emails = SqlConfig.getSqlMapClient().queryForList("Alert.getEmailsByAlertId", currentAlert.getId());
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
            List<Alert> alerts = SqlConfig.getSqlMapClient().queryForList("Alert.getAlertByChannelId", channelId);

            for (Alert currentAlert : alerts) {
                List<String> channelIds = SqlConfig.getSqlMapClient().queryForList("Alert.getChannelIdsByAlertId", currentAlert.getId());
                currentAlert.setChannels(channelIds);

                List<String> emails = SqlConfig.getSqlMapClient().queryForList("Alert.getEmailsByAlertId", currentAlert.getId());
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

        for (Alert alert : alerts) {
            insertAlert(alert);
        }
    }

    private void insertAlert(Alert alert) throws ControllerException {
        try {
            Alert alertFilter = new Alert();
            alertFilter.setId(alert.getId());

            try {
                SqlConfig.getSqlMapClient().startTransaction();

                logger.debug("adding alert: " + alert);
                SqlConfig.getSqlMapClient().insert("Alert.insertAlert", alert);

                logger.debug("adding channel alerts");

                for (String channelId : alert.getChannels()) {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("alertId", alert.getId());
                    params.put("channelId", channelId);
                    SqlConfig.getSqlMapClient().insert("Alert.insertChannelAlert", params);
                }

                logger.debug("adding alert emails");

                for (String email : alert.getEmails()) {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("alertId", alert.getId());
                    params.put("email", email);
                    SqlConfig.getSqlMapClient().insert("Alert.insertAlertEmail", params);
                }

                SqlConfig.getSqlMapClient().commitTransaction();
            } finally {
                SqlConfig.getSqlMapClient().endTransaction();
            }
        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    public void removeAlert(Alert alert) throws ControllerException {
        logger.debug("removing alert: " + alert);

        try {
            SqlConfig.getSqlMapClient().delete("Alert.deleteAlert", alert);

            if (DatabaseUtil.statementExists("Alert.vacuumAlertTable")) {
                SqlConfig.getSqlMapClient().update("Alert.vacuumAlertTable");
            }

        } catch (SQLException e) {
            throw new ControllerException(e);
        }
    }

    public void sendAlerts(String channelId, String errorType, String customMessage, Throwable e) {
        String fullErrorMessage = errorBuilder.buildErrorMessage(errorType, customMessage, e);
        String shortErrorMessage = (e == null) ? "No exception message." : e.getMessage();

        try {
            for (Alert alert : getAlertByChannelId(channelId)) {
                if (alert.isEnabled() && isAlertableError(alert.getExpression(), fullErrorMessage)) {
                    statisticsController.incrementAlertedCount(channelId);
                    sendAlertEmails(alert.getSubject(), alert.getEmails(), alert.getTemplate(), fullErrorMessage, shortErrorMessage, channelId);
                }
            }
        } catch (ControllerException ce) {
            logger.error(ce);
        }
    }

    private boolean isAlertableError(String expression, String errorMessage) {
        if ((expression != null) && (expression.length() > 0)) {
            return Pattern.compile(expression).matcher(errorMessage).find();
        } else {
            return false;
        }
    }

    private void sendAlertEmails(String subject, List<String> emails, String template, String fullErrorMessage, String shortErrorMessage, String channelId) throws ControllerException {
        TemplateValueReplacer replacer = new TemplateValueReplacer();
        Properties properties = ControllerFactory.getFactory().createConfigurationController().getServerProperties();
        final String fromAddress = PropertyLoader.getProperty(properties, "smtp.from");
        final String toAddresses = generateEmailList(emails);

        Map<String, Object> context = new HashMap<String, Object>();
        
        String channelName = "";
        
        if (channelId != null) {
            Channel channel = ControllerFactory.getFactory().createChannelController().getDeployedChannelById(channelId);
            
            if (channel != null) {
                channelName = channel.getName();
            }
        }
        
        context.put("channelName", channelName);
        context.put("error", fullErrorMessage);
        context.put("errorMessage", shortErrorMessage);
        context.put("systemTime", String.valueOf(System.currentTimeMillis()));
        context.put("date", new DateTool());
        
        // no longer shown on UI
        context.put("ERROR", fullErrorMessage);
        context.put("SYSTIME", String.valueOf(System.currentTimeMillis()));
        
        if (subject != null) {
            subject = replacer.replaceValues(subject, context);
        }
        
        // if there is no subject, set it to the default value
        if ((subject == null) || (subject.length() == 0)) {
            subject = "Mirth Connect Alert";
        }

        String body = fullErrorMessage;

        if (template != null) {
            body = replacer.replaceValues(template, context);
        }

        final String finalSubject = subject;
        final String finalBody = body;

        new Thread(new Runnable() {
            public void run() {
                try {
                    SMTPConnection connection = SMTPConnectionFactory.createSMTPConnection();
                    connection.send(toAddresses, null, fromAddress, finalSubject, finalBody);
                } catch (Exception e) {
                    logger.error("Could not send alert email.", e);
                }
            }
        }).start();
    }

    private String generateEmailList(List<String> emails) {
        StringBuilder builder = new StringBuilder();

        for (String email : emails) {
            builder.append(email + ",");
        }

        return builder.toString();
    }
}
