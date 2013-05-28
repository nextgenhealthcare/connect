/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jms;

import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.event.ConnectorEventType;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.HaltException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.event.ConnectorEvent;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.ErrorConstants;
import com.mirth.connect.util.ErrorMessageBuilder;

public class JmsDispatcher extends DestinationConnector {
    private JmsClient jmsClient;
    private JmsDispatcherProperties connectorProperties;
    private MessageProducer producer;
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private Logger logger = Logger.getLogger(getClass());

    @Override
    public void onDeploy() throws DeployException {
        connectorProperties = (JmsDispatcherProperties) getConnectorProperties();
        jmsClient = new JmsClient(this, connectorProperties);
        eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), ConnectorEventType.IDLE));
    }

    @Override
    public void onUndeploy() {}

    @Override
    public void onStart() throws StartException {
        try {
            jmsClient.start();
            producer = jmsClient.getSession().createProducer(null);
        } catch (Exception e) {
            throw new StartException("Failed to establish JMS connection", e);
        }

        eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), ConnectorEventType.CONNECTED));
    }

    @Override
    public void onStop() throws StopException {
        try {
            jmsClient.stop();
        } catch (Exception e) {
            throw new StopException("Failed to close JMS connection", e);
        }

        eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), ConnectorEventType.DISCONNECTED));
    }

    @Override
    public void onHalt() throws HaltException {
        try {
            onStop();
        } catch (StopException e) {
            throw new HaltException(e);
        }
    }

    @Override
    public void replaceConnectorProperties(ConnectorProperties connectorProperties, ConnectorMessage message) {
        JmsDispatcherProperties jmsDispatcherProperties = (JmsDispatcherProperties) connectorProperties;
        jmsDispatcherProperties.setTemplate(replacer.replaceValues(jmsDispatcherProperties.getTemplate(), message));
        jmsDispatcherProperties.setDestinationName(replacer.replaceValues(jmsDispatcherProperties.getDestinationName(), message));
    }

    @Override
    public Response send(ConnectorProperties connectorProperties, ConnectorMessage connectorMessage) throws InterruptedException {
        eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), ConnectorEventType.SENDING));
        JmsDispatcherProperties jmsDispatcherProperties = (JmsDispatcherProperties) connectorProperties;
        Session session = jmsClient.getSession();

        try {
            producer.send(jmsClient.getDestination(jmsDispatcherProperties.getDestinationName()), session.createTextMessage(jmsDispatcherProperties.getTemplate()));
            return new Response(Status.SENT, null, "Message sent successfully.");
        } catch (Exception e) {
            logger.error("An error occurred in channel \"" + ChannelController.getInstance().getDeployedChannelById(getChannelId()).getName() + "\": " + e.getMessage(), ExceptionUtils.getRootCause(e));
            eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.DESTINATION_CONNECTOR, connectorProperties.getName(), e.getMessage(), e));
            return new Response(Status.QUEUED, null, ErrorMessageBuilder.buildErrorResponse("Error occurred when attempting to send JMS message.", e), ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_407, e.getMessage(), e));
        } finally {
            eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), ConnectorEventType.IDLE));
        }
    }
}
