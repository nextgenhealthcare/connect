/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.alert;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.event.Event;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.donkey.server.event.EventType;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.alert.AlertActionGroup;
import com.mirth.connect.model.alert.AlertChannels;
import com.mirth.connect.model.alert.DefaultTrigger;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.util.ErrorMessageBuilder;

public class DefaultAlertWorker extends AlertWorker {

    private enum Keys {
        PATTERN
    };

    private ChannelController channelController = ControllerFactory.getFactory().createChannelController();

    @Override
    public Set<EventType> getEventTypes() {
        Set<EventType> eventTypes = new HashSet<EventType>();

        eventTypes.add(EventType.ERROR);

        return eventTypes;
    }

    @Override
    protected void onShutdown() {

    }

    @Override
    protected void alertEnabled(Alert alert) {

    }

    @Override
    protected void alertDisabled(Alert alert) {

    }

    @Override
    public Class<?> getTriggerClass() {
        return DefaultTrigger.class;
    }

    @Override
    protected void triggerAction(Alert alert, Map<String, Object> context) {
        for (AlertActionGroup actionGroup : alert.getModel().getActionGroups()) {
            if (CollectionUtils.isNotEmpty(actionGroup.getActions())) {
                actionExecutor.submit(new ActionTask(actionGroup, context));
                alert.incrementAlertedCount();
            }
        }
    }

    @Override
    protected void processEvent(Event event) {
        if (event instanceof ErrorEvent) {
            ErrorEvent errorEvent = (ErrorEvent) event;
            String channelId = errorEvent.getChannelId();
            Integer metaDataId = errorEvent.getMetaDataId();

            for (Alert alert : enabledAlerts.values()) {
                DefaultTrigger errorTrigger = (DefaultTrigger) alert.getModel().getTrigger();

                Set<ErrorEventType> errorEventTypes = errorTrigger.getErrorEventTypes();

                AlertChannels alertChannels = errorTrigger.getAlertChannels();

                boolean containsType = (errorEventTypes.contains(errorEvent.getType()) || errorEventTypes.contains(ErrorEventType.ANY));
                boolean eventSourceEnabled = (metaDataId == null ? alertChannels.isChannelEnabled(channelId) : alertChannels.isConnectorEnabled(channelId, metaDataId));

                /*
                 * Check that this alert is listening for event's type, and check if this alert is
                 * active for the channel that dispatched the event
                 */
                if (containsType && eventSourceEnabled) {
                    boolean trigger = true;
                    String errorSource = errorEvent.getType().toString();
                    if (errorEvent.getConnectorType() != null) {
                        errorSource += " (" + errorEvent.getConnectorType() + ")";
                    }

                    String fullErrorMessage = ErrorMessageBuilder.buildErrorMessage(errorSource, errorEvent.getCustomMessage(), errorEvent.getThrowable());

                    // If a regex is provided, check that it matches the full error message
                    if (StringUtils.isNotBlank(errorTrigger.getRegex())) {
                        Pattern pattern = (Pattern) alert.getProperties().get(Keys.PATTERN);

                        if (pattern == null) {
                            pattern = Pattern.compile(errorTrigger.getRegex());
                            alert.getProperties().put(Keys.PATTERN, pattern);
                        }

                        trigger = pattern.matcher(fullErrorMessage).find();
                    }

                    if (trigger) {
                        String channelName = "";

                        if (channelId != null) {
                            Channel channel = channelController.getDeployedChannelById(channelId);

                            if (channel != null) {
                                channelName = channel.getName();
                            }
                        }

                        // Create and populate the context for template value replacement with trigger specific values
                        Map<String, Object> context = alert.createContext();

                        context.put("systemTime", String.valueOf(errorEvent.getDateTime()));
                        context.put("channelId", channelId);
                        context.put("channelName", channelName);
                        context.put("connectorName", errorEvent.getConnectorName());
                        context.put("connectorType", errorEvent.getConnectorType());
                        context.put("error", fullErrorMessage);
                        context.put("errorMessage", (errorEvent.getThrowable() == null) ? "No exception message." : errorEvent.getThrowable().getMessage());
                        context.put("errorType", errorEvent.getType());

                        triggerAction(alert, context);
                    }
                }
            }
        }
    }
}
