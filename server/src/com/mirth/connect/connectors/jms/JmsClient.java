/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jms;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.ChannelState;
import com.mirth.connect.donkey.model.event.ConnectorEventType;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.server.channel.Connector;
import com.mirth.connect.donkey.server.event.ConnectorEvent;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.BeanUtil;

/**
 * Represents the client connection to a JMS broker, used by both the JMS receiver and dispatcher
 * connectors
 */
public class JmsClient implements ExceptionListener {
    private Connector connector;
    private JmsConnectorProperties connectorProperties;
    private String connectorName;
    private Connection connection;
    private Session session;
    private Context initialContext;
    private Destination destination;
    private String destinationName;
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private AtomicBoolean attemptingReconnect = new AtomicBoolean(false);
    private Logger logger = Logger.getLogger(getClass());

    public JmsClient(final Connector connector, JmsConnectorProperties connectorProperties, String connectorName) {
        this.connector = connector;
        this.connectorProperties = connectorProperties;
        this.connectorName = connectorName;
    }

    private ConnectionFactory lookupConnectionFactoryWithJndi() throws Exception {
        String channelId = connector.getChannelId();

        Hashtable<String, Object> env = new Hashtable<String, Object>();
        env.put(Context.PROVIDER_URL, replacer.replaceValues(connectorProperties.getJndiProviderUrl(), channelId));
        env.put(Context.INITIAL_CONTEXT_FACTORY, replacer.replaceValues(connectorProperties.getJndiInitialContextFactory(), channelId));
        env.put(Context.SECURITY_PRINCIPAL, replacer.replaceValues(connectorProperties.getUsername(), channelId));
        env.put(Context.SECURITY_CREDENTIALS, replacer.replaceValues(connectorProperties.getPassword(), channelId));

        initialContext = new InitialContext(env);
        String connectionFactoryName = replacer.replaceValues(connectorProperties.getJndiConnectionFactoryName(), channelId);
        return (ConnectionFactory) initialContext.lookup(connectionFactoryName);
    }

    /**
     * Starts a JMS connection and session.
     */
    public void start() throws Exception {
        final String channelId = connector.getChannelId();
        Map<String, String> connectionProperties = replacer.replaceValues(connectorProperties.getConnectionProperties(), channelId);
        ConnectionFactory connectionFactory = null;

        if (connectorProperties.isUseJndi()) {
            connectionFactory = lookupConnectionFactoryWithJndi();
        } else {
            String className = replacer.replaceValues(connectorProperties.getConnectionFactoryClass(), channelId);

            try {
                connectionFactory = (ConnectionFactory) Class.forName(className).newInstance();
            } catch (Exception e) {
                throw new Exception("Failed to instantiate ConnectionFactory class: " + className, e);
            }
        }

        BeanUtil.setProperties(connectionFactory, connectionProperties);

        try {
            logger.debug("Creating JMS connection and session");
            connection = connectionFactory.createConnection(replacer.replaceValues(connectorProperties.getUsername(), channelId), replacer.replaceValues(connectorProperties.getPassword(), channelId));
            String clientId = replacer.replaceValues(connectorProperties.getClientId(), channelId);

            if (!clientId.isEmpty()) {
                connection.setClientID(clientId);
            }

            connection.setExceptionListener(this);

            logger.debug("Starting JMS connection");
            connection.start();

            logger.debug("Creating JMS session");
            session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            logger.debug("JMS session created");
        } catch (Exception e) {
            try {
                stop();
            } catch (Exception e1) {
            }

            throw e;
        }
    }

    /**
     * Closes/stops the JMS connection.
     */
    public void stop() throws Exception {
        if (connection != null) {
            connection.close();
        }

        if (initialContext != null) {
            initialContext.close();
            initialContext = null;
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public Session getSession() {
        return session;
    }

    /*
     * This method is synchronized in case a queued destination is running in "attempt first" mode.
     * The queue thread and the destination's "attempt first" thread could potentially execute this
     * method at the same time.
     */
    public synchronized Destination getDestination(String destinationName) throws Exception {
        /*
         * Only lookup/create a new destination object if the destination name has changed since the
         * last time this method was called.
         */
        if (destinationName.equals(this.destinationName)) {
            return destination;
        }

        this.destinationName = destinationName;

        if (connectorProperties.isUseJndi()) {
            destination = (Destination) initialContext.lookup(destinationName);
        } else if (connectorProperties.isTopic()) {
            destination = session.createTopic(destinationName);
            logger.debug("Connected to topic: " + destinationName);
        } else {
            destination = session.createQueue(destinationName);
            logger.debug("Connected to queue: " + destinationName);
        }

        return destination;
    }

    /*
     * The exception listener detects if the JMS connection encounters a problem. When that happens,
     * attempt to reconnect every X milliseconds or until the connector is stopped/undeployed.
     */
    @Override
    public void onException(JMSException e) {
        if (!attemptingReconnect.get()) {
            attemptingReconnect.set(true);

            try {
                int reconnectIntervalMillis = NumberUtils.toInt(replacer.replaceValues(connectorProperties.getReconnectIntervalMillis(), connector.getChannelId()));

                if (reconnectIntervalMillis > 0 && connector.getCurrentState() == ChannelState.STARTED) {
                    Exception exception;
                    reportError("A connection error occurred, attempting to reconnect", e);

                    try {
                        connector.onStop();
                    } catch (Exception e1) {
                        logger.error("Failed to close connection", e1);
                        eventController.dispatchEvent(new ConnectorEvent(connector.getChannelId(), connector.getMetaDataId(), connectorName, ConnectorEventType.DISCONNECTED));
                    }

                    do {
                        exception = null;

                        try {
                            connector.onStart();
                        } catch (Exception e1) {
                            reportError("Failed to reconnect, retyring in " + reconnectIntervalMillis + " milliseconds", e1);
                            exception = e1;

                            try {
                                Thread.sleep(reconnectIntervalMillis);
                            } catch (InterruptedException e2) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    } while (exception != null && connector.getCurrentState() == ChannelState.STARTED);

                    if (exception == null) {
                        logger.debug("Reconnected successfully");
                    } else {
                        logger.debug("Halted reconnect attempt, channel is no longer running");
                    }
                } else {
                    reportError("A connection error occurred.", e);
                }
            } finally {
                attemptingReconnect.set(false);
            }
        }
    }

    private void reportError(String errorMessage, Exception e) {
        String channelId = connector.getChannelId();
        logger.error(errorMessage + " (channel: " + ChannelController.getInstance().getDeployedChannelById(channelId).getName() + ")", e);
        eventController.dispatchEvent(new ErrorEvent(channelId, connector.getMetaDataId(), ErrorEventType.DESTINATION_CONNECTOR, connectorName, null, e.getCause()));
    }
}
