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
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.util.ErrorMessageBuilder;

public class DefaultAlertWorker extends AlertWorker {

    private static final int PATTERN_KEY = 0;

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

                boolean containsType = (errorEventTypes == null || errorEventTypes.contains(errorEvent.getType()));
                boolean eventSourceEnabled = (metaDataId == null ? alertChannels.isChannelEnabled(channelId) : alertChannels.isConnectorEnabled(channelId, metaDataId));

                if (containsType && eventSourceEnabled) {
                    boolean trigger = true;
                    String fullErrorMessage = ErrorMessageBuilder.buildErrorMessage(errorEvent.getType().toString(), errorEvent.getCustomMessage(), errorEvent.getThrowable());

                    if (StringUtils.isNotBlank(errorTrigger.getRegex())) {
                        Pattern pattern = (Pattern) alert.getProperties().get(PATTERN_KEY);

                        if (pattern == null) {
                            pattern = Pattern.compile(errorTrigger.getRegex());
                            alert.getProperties().put(PATTERN_KEY, pattern);
                        }

                        trigger = pattern.matcher(fullErrorMessage).find();
                    }

                    if (trigger) {
                        String channelName = "";

                        if (channelId != null) {
                            Channel channel = ControllerFactory.getFactory().createChannelController().getDeployedChannelById(channelId);

                            if (channel != null) {
                                channelName = channel.getName();
                            }
                        }

                        // Create and populate the context for template value replacement with trigger specific values
                        Map<String, Object> context = alert.createContext();

                        context.put("systemTime", String.valueOf(errorEvent.getNanoTime()));
                        context.put("channelId", channelId);
                        context.put("channelName", channelName);
                        context.put("connectorName", errorEvent.getConnectorName());
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
