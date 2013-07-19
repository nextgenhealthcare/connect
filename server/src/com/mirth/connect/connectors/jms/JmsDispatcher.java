/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jms;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.NamingException;

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
        jmsClient = new JmsClient(this, connectorProperties, getDestinationName());
        eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectorEventType.IDLE));
    }

    @Override
    public void onUndeploy() {}

    @Override
    public void onStart() throws StartException {
        jmsClient.start();
        
        try {
            producer = jmsClient.getSession().createProducer(null);
        } catch (JMSException e) {
            throw new StartException("Failed to create a JMS message producer", e);
        }

        eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectorEventType.CONNECTED));
    }

    @Override
    public void onStop() throws StopException {
        try {
            jmsClient.stop();
        } catch (Exception e) {
            throw new StopException("Failed to close JMS connection", e);
        }

        eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectorEventType.DISCONNECTED));
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
        eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectorEventType.SENDING));
        JmsDispatcherProperties jmsDispatcherProperties = (JmsDispatcherProperties) connectorProperties;

        /*
         * We synchronize with the client here because the client could be attempting to reconnect
         * to the broker on a different thread if we detected a disconnection.
         */
        synchronized (jmsClient) {
            Session session = jmsClient.getSession();
            
            try {
                return doSend(jmsDispatcherProperties, session);
            } catch (Exception e) {
                try {
                    if (jmsClient.beginReconnect(true)) {
                        return doSend(jmsDispatcherProperties, session);
                    } else {
                        throw e;
                    }
                } catch (Exception e1) {
                    logger.error("An error occurred in channel \"" + ChannelController.getInstance().getDeployedChannelById(getChannelId()).getName() + "\": " + e1.getMessage(), ExceptionUtils.getRootCause(e1));
                    eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.DESTINATION_CONNECTOR, getDestinationName(), e1.getMessage(), e1));
                    return new Response(Status.QUEUED, null, ErrorMessageBuilder.buildErrorResponse("Error occurred when attempting to send JMS message.", e1), ErrorMessageBuilder.buildErrorMessage(connectorProperties.getName(), e1.getMessage(), e1));
                }
            } finally {
                eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectorEventType.IDLE));
            }
        }
    }
    
    private Response doSend(JmsDispatcherProperties jmsDispatcherProperties, Session session) throws JMSException, NamingException {
        producer.send(jmsClient.getDestination(jmsDispatcherProperties.getDestinationName()), session.createTextMessage(jmsDispatcherProperties.getTemplate()));
        return new Response(Status.SENT, null, "Message sent successfully.");
    }
}
