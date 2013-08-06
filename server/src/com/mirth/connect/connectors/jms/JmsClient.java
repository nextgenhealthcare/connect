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
import javax.naming.NamingException;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.ChannelState;
import com.mirth.connect.donkey.model.event.ConnectorEventType;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
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
    private JmsReceiverProperties connectorProperties;
    private String connectorName;
    private Connection connection;
    private Session session;
    private Context initialContext;
    private Destination destination;
    private String destinationName;
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private Thread reconnectThread;
    private AtomicBoolean connected = new AtomicBoolean(false);
    private AtomicBoolean attemptingReconnect = new AtomicBoolean(false);
    private int intervalMillis;
    private Logger logger = Logger.getLogger(getClass());

    public JmsClient(final Connector connector, JmsReceiverProperties connectorProperties, String connectorName) {
        this.connector = connector;
        this.connectorProperties = connectorProperties;
        this.connectorName = connectorName;
        this.intervalMillis = NumberUtils.toInt(replacer.replaceValues(connectorProperties.getReconnectIntervalMillis(), connector.getChannelId()));
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
    public void start() throws StartException {
        final String channelId = connector.getChannelId();
        Map<String, String> connectionProperties = replacer.replaceValues(connectorProperties.getConnectionProperties(), channelId);
        ConnectionFactory connectionFactory = null;

        if (connectorProperties.isUseJndi()) {
            try {
                connectionFactory = lookupConnectionFactoryWithJndi();
            } catch (Exception e) {
                throw new StartException("Failed to obtain the connection factory via JNDI", e);
            }
        } else {
            String className = replacer.replaceValues(connectorProperties.getConnectionFactoryClass(), channelId);

            try {
                connectionFactory = (ConnectionFactory) Class.forName(className).newInstance();
            } catch (Exception e) {
                throw new StartException("Failed to instantiate ConnectionFactory class: " + className, e);
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
        } catch (JMSException e) {
            try {
                stop();
            } catch (Exception e1) {
            }

            throw new StartException("Failed to establish a JMS connection", e);
        }

        connected.set(true);
    }

    /**
     * Closes/stops the JMS connection.
     */
    public void stop() throws StopException {
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException e) {
                throw new StopException("Failed to close the JMS connection", e);
            }
        }

        if (initialContext != null) {
            try {
                initialContext.close();
            } catch (NamingException e) {
                logger.error("Failed to close the initial context", e);
            }

            initialContext = null;
        }
        
        connected.set(false);
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
    public synchronized Destination getDestination(String destinationName) throws JMSException, NamingException {
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
     * Whenever an exception occurs, we want to try to reconnect to the JMS broker.
     */
    @Override
    public void onException(JMSException e) {
        /*
         * If reconnecting is true, then we return because we don't want to create an infinite loop
         * if the reconnect attempt itself throws an exception.
         */
        if (attemptingReconnect.get()) {
            return;
        }

        beginReconnect(false);
    }
    
    /**
     * Begin reconnect attempts if we've detected that the connect to the JMS broker is down.
     * 
     * @param force If true, forces a reconnect attempt right now and returns the result.
     * @return The result of the force reconnect attempt.
     */
    public synchronized boolean beginReconnect(boolean force) {
        if (connected.get()) {
            eventController.dispatchEvent(new ConnectorEvent(connector.getChannelId(), connector.getMetaDataId(), connectorName, ConnectorEventType.DISCONNECTED));
            connected.set(false);
        }

        if (intervalMillis == 0) {
            doReconnect();
            return connected.get();
        } else {
            if (reconnectThread == null || !reconnectThread.isAlive()) {
                reconnectThread = new ReconnectThread();
                reconnectThread.start();
            }

            if (force) {
                reconnectThread.interrupt();
                
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
                
                return connected.get();
            } else {
                return false;
            }
        }
    }
    
    private class ReconnectThread extends Thread {
        @Override
        public void run() {
            while (!connected.get() && connector.getCurrentState() == ChannelState.STARTED) {
                try {
                    if (!connected.get()) {
                        doReconnect();
                    }

                    Thread.sleep(intervalMillis);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private synchronized void doReconnect() {
        try {
            attemptingReconnect.set(true);
            logger.debug("attempting to reconnect");

            try {
                stop();
            } catch (StopException e1) {
            }

            connector.onStart();
            logger.debug("reconnect successful");
        } catch (StartException e) {
            reportError("Failed to reconnect", e);
        } finally {
            attemptingReconnect.set(false);

            /*
             * Notify beginReconnect() that we just attempted to reconnect
             */
            notify();
        }
    }
    
    private void reportError(String errorMessage, Exception e) {
        String channelId = connector.getChannelId();
        logger.error(errorMessage + " (channel: " + ChannelController.getInstance().getDeployedChannelById(channelId).getName() + ")", e);
        eventController.dispatchEvent(new ErrorEvent(channelId, connector.getMetaDataId(), ErrorEventType.DESTINATION_CONNECTOR, connectorName, connectorProperties.getName(), null, e.getCause()));
    }
}
