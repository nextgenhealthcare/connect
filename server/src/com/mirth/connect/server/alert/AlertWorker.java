/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.alert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.model.ServerEvent;
import com.mirth.connect.model.alert.AlertAction;
import com.mirth.connect.model.alert.AlertActionGroup;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.alert.AlertStatus;
import com.mirth.connect.plugins.ServerPlugin;
import com.mirth.connect.server.alert.action.Protocol;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.controllers.ExtensionController;
import com.mirth.connect.server.event.EventListener;
import com.mirth.connect.server.util.ServerSMTPConnectionFactory;
import com.mirth.connect.server.util.TemplateValueReplacer;

public abstract class AlertWorker extends EventListener implements AlertActionAcceptor {
    private static final String DEFAULT_SUBJECT = "Mirth Connect Alert";

    protected Logger logger = Logger.getLogger(this.getClass());
    protected ExecutorService actionExecutor = new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    protected Map<String, Alert> enabledAlerts = new ConcurrentHashMap<String, Alert>();
    protected EventController eventController = ControllerFactory.getFactory().createEventController();
    protected ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();

    private AlertController alertController;
    private String serverId = ControllerFactory.getFactory().createConfigurationController().getServerId();
    private List<AlertActionAcceptor> alertActionAcceptors = new ArrayList<AlertActionAcceptor>();

    public AlertWorker() {
        super();

        for (ServerPlugin serverPlugin : extensionController.getServerPlugins()) {
            if (serverPlugin instanceof AlertActionAcceptor) {
                alertActionAcceptors.add((AlertActionAcceptor) serverPlugin);
            }
        }
    }

    public void enableAlert(AlertModel alertModel) {
        Alert alert = new Alert(alertModel);
        enabledAlerts.put(alertModel.getId(), alert);

        alertEnabled(alert);
    }

    public void disableAlert(String alertId) {
        Alert alert = enabledAlerts.remove(alertId);

        if (alert != null) {
            alertDisabled(alert);
        }
    }

    public AlertStatus getAlertStatus(String alertId) {
        Alert alert = enabledAlerts.get(alertId);

        if (alert != null) {
            AlertStatus alertStatus = new AlertStatus();
            alertStatus.setAlertedCount(alert.getAlertedCount());
            return alertStatus;
        }

        return null;
    }

    public Alert getEnabledAlert(String alertId) {
        return enabledAlerts.get(alertId);
    }

    @Override
    public boolean acceptAlertAction(Alert alert, Map<String, Object> context) {
        for (AlertActionAcceptor acceptor : alertActionAcceptors) {
            if (!acceptor.acceptAlertAction(alert, context)) {
                return false;
            }
        }
        return true;
    }

    protected abstract void alertEnabled(Alert alert);

    protected abstract void alertDisabled(Alert alert);

    public abstract Class<?> getTriggerClass();

    protected abstract void triggerAction(Alert alert, Map<String, Object> context);

    protected class ActionTask implements Callable<Void> {

        private String alertId;
        private AlertActionGroup actionGroup;
        private Map<String, Object> context;
        private long taskCreatedNanoTime;

        public ActionTask(String alertId, AlertActionGroup actionGroup, Map<String, Object> context) {
            this.alertId = alertId;
            this.actionGroup = actionGroup;
            this.context = context;
            this.taskCreatedNanoTime = System.nanoTime();
        }

        @Override
        public Void call() throws Exception {
            Alert alert = enabledAlerts.get(alertId);

            if (alert != null && alert.getEnabledNanoTime() <= taskCreatedNanoTime) {
                TemplateValueReplacer replacer = new TemplateValueReplacer();

                String subject = null;
                String body = null;

                if (actionGroup.getSubject() != null) {
                    subject = replacer.replaceValues(actionGroup.getSubject(), context);
                }

                if (actionGroup.getTemplate() != null) {
                    body = replacer.replaceValues(actionGroup.getTemplate(), context);
                }

                if (StringUtils.isEmpty(subject)) {
                    subject = DEFAULT_SUBJECT;
                }

                Map<String, List<String>> protocolRecipients = new LinkedHashMap<String, List<String>>();

                for (AlertAction action : actionGroup.getActions()) {
                    String recipient = replacer.replaceValues(action.getRecipient(), context);

                    if (StringUtils.isNotBlank(recipient)) {
                        List<String> recipients = protocolRecipients.get(action.getProtocol());

                        if (recipients == null) {
                            recipients = new ArrayList<String>();
                            protocolRecipients.put(action.getProtocol(), recipients);
                        }

                        recipients.add(recipient);
                    }
                }

                if (alertController == null) {
                    alertController = ControllerFactory.getFactory().createAlertController();
                }

                Set<String> emailAddresses = new HashSet<>();

                for (Entry<String, List<String>> protocolEntry : protocolRecipients.entrySet()) {
                    String protocolName = protocolEntry.getKey();

                    try {
                        Protocol alertProtocol = alertController.getAlertActionProtocol(protocolName);

                        if (alertProtocol == null) {
                            logger.warn("Alert protocol '" + protocolName + "' is not currently installed, skipping.");
                        } else {
                            List<String> recipients = protocolEntry.getValue();
                            List<String> protocolEmailAddresses = alertProtocol.getEmailAddressesForDispatch(recipients);

                            alertProtocol.doCustomDispatch(recipients, subject, body);

                            if (CollectionUtils.isNotEmpty(protocolEmailAddresses)) {
                                emailAddresses.addAll(protocolEmailAddresses);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("An error occurred while attempting to dispatch '" + protocolName + "' alerts.", e);
                    }
                }

                if (!emailAddresses.isEmpty()) {
                    try {
                        ServerSMTPConnectionFactory.createSMTPConnection().send(StringUtils.join(emailAddresses, ","), null, subject, body);
                    } catch (Exception e) {
                        logger.error("Error sending alert email.", e);
                    }
                }

                // Dispatch a server event to notify that an alert was dispatched
                ServerEvent serverEvent = new ServerEvent(serverId, "Alert Dispatched");
                for (Entry<String, Object> entry : context.entrySet()) {
                    String value = entry.getValue().toString();
                    serverEvent.addAttribute(entry.getKey(), value);
                }
                eventController.dispatchEvent(serverEvent);
            }

            return null;
        }

    }

}
