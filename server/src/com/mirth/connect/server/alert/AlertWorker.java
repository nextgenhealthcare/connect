/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.alert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ServerEvent;
import com.mirth.connect.model.alert.AlertAction;
import com.mirth.connect.model.alert.AlertActionGroup;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.alert.AlertStatus;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.event.EventListener;
import com.mirth.connect.server.util.ServerSMTPConnectionFactory;
import com.mirth.connect.server.util.TemplateValueReplacer;

public abstract class AlertWorker extends EventListener {

    protected Logger logger = Logger.getLogger(this.getClass());
    protected ExecutorService actionExecutor = new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    protected Map<String, Alert> enabledAlerts = new ConcurrentHashMap<String, Alert>();
    protected EventController eventController = ControllerFactory.getFactory().createEventController();

    private EngineController engineController = ControllerFactory.getFactory().createEngineController();
    private ChannelController channelController = ControllerFactory.getFactory().createChannelController();

    public AlertWorker() {
        super();
    }

    public void enableAlert(AlertModel alertModel) {
        Alert alert = new Alert(alertModel);
        enabledAlerts.put(alertModel.getId(), alert);

        alertEnabled(alert);
    }

    public void disableAlert(AlertModel alertModel) {
        Alert alert = enabledAlerts.remove(alertModel.getId());

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

    protected abstract void alertEnabled(Alert alert);

    protected abstract void alertDisabled(Alert alert);

    public abstract Class<?> getTriggerClass();

    protected abstract void triggerAction(Alert alert, Map<String, Object> context);

    protected class ActionTask implements Callable<Void> {

        private AlertActionGroup actionGroup;
        private Map<String, Object> context;

        public ActionTask(AlertActionGroup actionGroup, Map<String, Object> context) {
            this.actionGroup = actionGroup;
            this.context = context;
        }

        @Override
        public Void call() throws Exception {
            TemplateValueReplacer replacer = new TemplateValueReplacer();

            String subject = null;
            String body = null;

            if (actionGroup.getSubject() != null) {
                subject = replacer.replaceValues(actionGroup.getSubject(), context);
            }

            if (actionGroup.getTemplate() != null) {
                body = replacer.replaceValues(actionGroup.getTemplate(), context);
            }

            List<String> emails = new ArrayList<String>();
            List<String> channels = new ArrayList<String>();

            // Split the recipients into separate lists for emails and channels
            for (AlertAction action : actionGroup.getActions()) {
                switch (action.getProtocol()) {
                    case EMAIL:
                        emails.add(action.getRecipient());
                        break;

                    case CHANNEL:
                        channels.add(action.getRecipient());
                        break;
                }
            }

            // Send the alert emails
            if (!emails.isEmpty()) {
                try {
                    ServerSMTPConnectionFactory.createSMTPConnection().send(StringUtils.join(emails, ","), null, subject, body);
                } catch (ControllerException e) {
                    logger.error("Could not load default SMTP settings.", e);
                } catch (EmailException e) {
                    logger.error("Error sending alert email.", e);
                }
            }

            // Route the alert message to the specified channels
            for (String channelName : channels) {
                Channel channel = channelController.getDeployedChannelByName(channelName);
                if (channel != null) {
                    engineController.dispatchRawMessage(channel.getId(), new RawMessage(body));
                }
            }

            // Dispatch a server event to notify that an alert was dispatched
            ServerEvent serverEvent = new ServerEvent("Alert Dispatched");
            for (Entry<String, Object> entry : context.entrySet()) {
                String value = entry.getValue().toString();
                serverEvent.addAttribute(entry.getKey(), value);
            }
            eventController.dispatchEvent(serverEvent);

            return null;
        }

    }

}
