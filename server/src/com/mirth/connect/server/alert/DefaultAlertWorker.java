package com.mirth.connect.server.alert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;

import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.donkey.server.event.Event;
import com.mirth.connect.donkey.server.event.EventType;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.alert.AlertAction;
import com.mirth.connect.model.alert.AlertActionGroup;
import com.mirth.connect.model.alert.DefaultTrigger;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.util.SMTPConnectionFactory;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.server.util.VMRouter;
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
    public Class<?> getTriggerClass() {
        return DefaultTrigger.class;
    }

    @Override
    protected Callable<?> getWorkerTask() {
        return new AlertWorkerTask();
    }
    
    @Override
    protected void triggerAction(Alert alert) {
        for (AlertActionGroup actionGroup : alert.getModel().getActionGroups()) {
            if (CollectionUtils.isNotEmpty(actionGroup.getActions())) {
                actionExecutor.submit(new ActionTask(actionGroup, alert.getContext()));
            }
        }
    }

    private class AlertWorkerTask implements Callable<Void> {

        @Override
        public Void call() throws Exception {
            while (!Thread.currentThread().isInterrupted()) {
                Event event = queue.take();

                if (event instanceof ErrorEvent) {
                    ErrorEvent errorEvent = (ErrorEvent) event;

                    for (Alert alert : enabledAlerts.values()) {
                        DefaultTrigger errorTrigger = (DefaultTrigger) alert.getModel().getTrigger();

                        Set<ErrorEventType> errorAlertTypes = errorTrigger.getErrorAlertTypes();

                        if (errorAlertTypes.contains(errorEvent.getType())) {
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
                                Map<String, Object> context = alert.getContext();
                                String channelId = errorEvent.getChannelId();
                                String channelName = "";
                                
                                if (channelId != null) {
                                    Channel channel = ControllerFactory.getFactory().createChannelController().getDeployedChannelById(channelId);
                                    
                                    if (channel != null) {
                                        channelName = channel.getName();
                                    }
                                }
                                
                                context.put("systemTime", String.valueOf(errorEvent.getDate()));
                                context.put("serverId", ConfigurationController.getInstance().getServerId());
                                context.put("channelId", channelId);
                                context.put("channelName", channelName);
                                context.put("error", fullErrorMessage);
                                context.put("errorMessage", (errorEvent.getThrowable() == null) ? "No exception message." : errorEvent.getThrowable().getMessage());
                                
                                triggerAction(alert);
                            }
                        }
                    }
                }
            }

            return null;
        }

    }
    
    private class ActionTask implements Callable<Void> {
        
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
            
            for (AlertAction action: actionGroup.getActions()) {
                switch (action.getProtocol()) {
                    case EMAIL:
                        emails.add(action.getRecipient());
                        break;
    
                    case CHANNEL:
                        channels.add(action.getRecipient());
                        break;
                }
            }
            
            if (!emails.isEmpty()) {
                try {
                    SMTPConnectionFactory.createSMTPConnection().send(StringUtils.join(emails, ","), null, subject, body);
                } catch (ControllerException e) {
                    logger.error("Could not load default SMTP settings.", e);
                } catch (EmailException e) {
                    logger.error("Error sending alert email.", e);
                }
            }

            VMRouter router = new VMRouter();
            if (!channels.isEmpty()) {
                for (String channelName : channels) {
                    router.routeMessage(channelName, body);
                }
            }

            return null;
        }
        
    }

}
