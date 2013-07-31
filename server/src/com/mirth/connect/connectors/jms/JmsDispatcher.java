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
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
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
import com.mirth.connect.util.BeanUtil;
import com.mirth.connect.util.ErrorMessageBuilder;

public class JmsDispatcher extends DestinationConnector {
    private JmsDispatcherProperties connectorProperties;
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private Logger logger = Logger.getLogger(getClass());

    private Map<String, JmsConnection> jmsConnections = new ConcurrentHashMap<String, JmsConnection>();
    private static final int maxConnections = 1000;

    @Override
    public void onDeploy() throws DeployException {
        connectorProperties = (JmsDispatcherProperties) getConnectorProperties();
        eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectorEventType.IDLE));
    }

    @Override
    public void onUndeploy() {}

    @Override
    public void onStart() throws StartException {}

    @Override
    public void onStop() throws StopException {
        StopException firstCause = null;

        // Close the JMS connections
        for (String connectorKey : jmsConnections.keySet().toArray(new String[jmsConnections.size()])) {
            try {
                closeJmsConnection(connectorKey);
            } catch (Exception e) {
                if (firstCause == null) {
                    firstCause = new StopException("Error closing JMS connection (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", firstCause);
                }
            }
        }

        if (firstCause != null) {
            throw new StopException("Error closing JMS connection (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", firstCause);
        }
    }

    @Override
    public void onHalt() throws HaltException {
        HaltException firstCause = null;

        // Close the JMS connections
        for (String connectorKey : jmsConnections.keySet().toArray(new String[jmsConnections.size()])) {
            try {
                closeJmsConnection(connectorKey);
            } catch (Exception e) {
                if (firstCause == null) {
                    firstCause = new HaltException("Error closing JMS connection (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", firstCause);
                }
            }
        }

        if (firstCause != null) {
            throw new HaltException("Error closing JMS connection (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", firstCause);
        }
    }

    @Override
    public void replaceConnectorProperties(ConnectorProperties connectorProperties, ConnectorMessage message) {
        JmsDispatcherProperties jmsDispatcherProperties = (JmsDispatcherProperties) connectorProperties;
        jmsDispatcherProperties.setTemplate(replacer.replaceValues(jmsDispatcherProperties.getTemplate(), message));
        jmsDispatcherProperties.setDestinationName(replacer.replaceValues(jmsDispatcherProperties.getDestinationName(), message));
        jmsDispatcherProperties.setConnectionProperties(replacer.replaceValuesInMap(jmsDispatcherProperties.getConnectionProperties(), message));
        jmsDispatcherProperties.setUsername(replacer.replaceValues(jmsDispatcherProperties.getUsername(), message));
        jmsDispatcherProperties.setPassword(replacer.replaceValues(jmsDispatcherProperties.getPassword(), message));
        jmsDispatcherProperties.setClientId(replacer.replaceValues(jmsDispatcherProperties.getClientId(), message));

        if (jmsDispatcherProperties.isUseJndi()) {
            jmsDispatcherProperties.setJndiProviderUrl(replacer.replaceValues(jmsDispatcherProperties.getJndiProviderUrl(), message));
            jmsDispatcherProperties.setJndiInitialContextFactory(replacer.replaceValues(jmsDispatcherProperties.getJndiInitialContextFactory(), message));
            jmsDispatcherProperties.setJndiConnectionFactoryName(replacer.replaceValues(jmsDispatcherProperties.getJndiConnectionFactoryName(), message));
        } else {
            jmsDispatcherProperties.setConnectionFactoryClass(replacer.replaceValues(jmsDispatcherProperties.getConnectionFactoryClass(), message));
        }
    }

    @Override
    public Response send(ConnectorProperties connectorProperties, ConnectorMessage connectorMessage) throws InterruptedException {
        eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectorEventType.SENDING));
        JmsDispatcherProperties jmsDispatcherProperties = (JmsDispatcherProperties) connectorProperties;

        String responseError = null;
        String responseStatusMessage = "Message sent successfully.";
        Status responseStatus = Status.SENT; // Always set the status to QUEUED

        // Only one connection is allowed to be created per message so keep track of whether a connection was created.
        boolean connectionCreated = false;

        long dispatcherId = getDispatcherId();
        String connectionKey = getConnectionKey(jmsDispatcherProperties);

        // Retrieve the connection from the cache
        JmsConnection jmsConnection = jmsConnections.get(connectionKey);

        try {
            try {
                if (jmsConnection == null) {
                    /*
                     * If the connection was not in the cache, create it and indicate that a
                     * connection was created for this message.
                     */
                    connectionCreated = true;
                    jmsConnection = getJmsConnection(jmsDispatcherProperties, connectionKey, dispatcherId, false);
                }

                // Retrieve the session for this dispatcherId 
                JmsSession jmsSession = getJmsSession(jmsConnection, dispatcherId);

                /*
                 * Get the destination, create the text message, and send it.
                 */
                jmsSession.getProducer().send(getDestination(jmsDispatcherProperties, jmsSession, jmsConnection.getInitialContext()), jmsSession.getSession().createTextMessage(jmsDispatcherProperties.getTemplate()));
            } catch (Exception e) {
                if (!connectionCreated) {
                    /*
                     * If a connection was not already created for this attempt, create a new
                     * connection and attempt to send the message again. This would typically occur
                     * if a connection was lost prior to the message being sent.
                     */
                    try {
                        jmsConnection = getJmsConnection(jmsDispatcherProperties, connectionKey, dispatcherId, true);

                        JmsSession jmsSession = getJmsSession(jmsConnection, dispatcherId);

                        jmsSession.getProducer().send(getDestination(jmsDispatcherProperties, jmsSession, jmsConnection.getInitialContext()), jmsSession.getSession().createTextMessage(jmsDispatcherProperties.getTemplate()));
                    } catch (Exception e2) {
                        // If the message fails to send again, throw the exception which will set the response status to ERROR
                        throw e2;
                    }
                } else {
                    // Otherwise throw the exception which will set the response status to ERROR
                    throw e;
                }
            }
        } catch (Exception e) {
            logger.error("An error occurred in channel \"" + ChannelController.getInstance().getDeployedChannelById(getChannelId()).getName() + "\": " + e.getMessage(), ExceptionUtils.getRootCause(e));
            eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.DESTINATION_CONNECTOR, getDestinationName(), "Error occurred when attempting to send JMS message.", e));
            responseStatus = Status.QUEUED;
            responseStatusMessage = ErrorMessageBuilder.buildErrorResponse("Error occurred when attempting to send JMS message.", e);
            responseError = ErrorMessageBuilder.buildErrorMessage(connectorProperties.getName(), "Error occurred when attempting to send JMS message.", e);
        } finally {
            eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectorEventType.IDLE));
        }

        return new Response(responseStatus, null, responseStatusMessage, responseError);
    }

    /**
     * Create a connection key based off the dispatcher properties which can be used to store and
     * identify a connection.
     */
    private String getConnectionKey(JmsDispatcherProperties jmsDispatcherProperties) {
        char delimiter = ':';
        StringBuilder builder = new StringBuilder();

        builder.append(jmsDispatcherProperties.isUseJndi());

        if (jmsDispatcherProperties.isUseJndi()) {
            builder.append(delimiter);
            builder.append(jmsDispatcherProperties.getJndiProviderUrl());

            builder.append(delimiter);
            builder.append(jmsDispatcherProperties.getJndiInitialContextFactory());

            builder.append(delimiter);
            builder.append(jmsDispatcherProperties.getJndiConnectionFactoryName());
        } else {

            builder.append(delimiter);
            builder.append(jmsDispatcherProperties.getConnectionFactoryClass());

            builder.append(delimiter);
            builder.append(jmsDispatcherProperties.isTopic());
        }

        for (String value : jmsDispatcherProperties.getConnectionProperties().values()) {
            builder.append(value);
        }

        builder.append(delimiter);
        builder.append(jmsDispatcherProperties.getUsername());

        builder.append(delimiter);
        builder.append(jmsDispatcherProperties.getPassword());

        builder.append(delimiter);
        builder.append(jmsDispatcherProperties.getClientId());

        return builder.toString();
    }

    /**
     * Get the JmsConnection from the cache if one exists, otherwise a new one will be created. This
     * method is synchronized otherwise multiple threads may try to create the same connection
     * simultaneously. Only one thread is allowed to create a connection at a time. Subsequent
     * threads will then retrieve the connection that was already created.
     */
    private synchronized JmsConnection getJmsConnection(JmsDispatcherProperties jmsDispatcherProperties, String connectionKey, Long dispatcherId, boolean replace) throws Exception {
        // If the connection needs to be replaced, clean up the old connection and remove it from the cache.
        if (replace) {
            closeJmsConnectionQuietly(connectionKey);
        }

        JmsConnection jmsConnection = jmsConnections.get(connectionKey);

        if (jmsConnection == null) {
            if (jmsConnections.size() >= maxConnections) {
                throw new Exception("Cannot create new connection. Maximum number (" + maxConnections + ") of cached connections reached.");
            }
            
            Context initialContext = null;
            ConnectionFactory connectionFactory = null;
            Connection connection = null;

            Map<String, String> connectionProperties = jmsDispatcherProperties.getConnectionProperties();
            if (jmsDispatcherProperties.isUseJndi()) {
                Hashtable<String, Object> env = new Hashtable<String, Object>();
                env.put(Context.PROVIDER_URL, jmsDispatcherProperties.getJndiProviderUrl());
                env.put(Context.INITIAL_CONTEXT_FACTORY, jmsDispatcherProperties.getJndiInitialContextFactory());
                env.put(Context.SECURITY_PRINCIPAL, jmsDispatcherProperties.getUsername());
                env.put(Context.SECURITY_CREDENTIALS, jmsDispatcherProperties.getPassword());

                initialContext = new InitialContext(env);

                String connectionFactoryName = jmsDispatcherProperties.getJndiConnectionFactoryName();
                connectionFactory = (ConnectionFactory) initialContext.lookup(connectionFactoryName);
            } else {
                String className = jmsDispatcherProperties.getConnectionFactoryClass();

                connectionFactory = (ConnectionFactory) Class.forName(className).newInstance();
            }

            BeanUtil.setProperties(connectionFactory, connectionProperties);

            try {
                logger.debug("Creating JMS connection and session");
                connection = connectionFactory.createConnection(jmsDispatcherProperties.getUsername(), jmsDispatcherProperties.getPassword());
                String clientId = jmsDispatcherProperties.getClientId();

                if (!clientId.isEmpty()) {
                    connection.setClientID(clientId);
                }

                logger.debug("Starting JMS connection");
                connection.start();
            } catch (JMSException e) {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Exception e1) {
                    logger.debug("Failed to close JMS connection.", e);
                }

                try {
                    if (initialContext != null) {
                        initialContext.close();
                    }
                } catch (Exception e1) {
                    logger.debug("Failed to close initial context.", e);
                }

                throw e;
            }

            // Create the new JmsConnection and add it to the cache.
            jmsConnection = new JmsConnection(connection, initialContext);
            jmsConnections.put(connectionKey, jmsConnection);
        }

        return jmsConnection;
    }

    /**
     * Retrieve the dispatcherId specific JmsSession from the cache. If the JmsSession does not
     * exist, create a new one from the connection.
     */
    private JmsSession getJmsSession(JmsConnection jmsConnection, Long dispatcherId) throws Exception {
        Map<Long, JmsSession> jmsSessions = jmsConnection.getJmsSessions();
        JmsSession jmsSession = jmsSessions.get(dispatcherId);

        if (jmsSession == null) {
            Session session = jmsConnection.getConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(null);

            jmsSession = new JmsSession(session, producer);
            jmsSessions.put(dispatcherId, jmsSession);
        }

        return jmsSession;
    }

    private void closeJmsConnectionQuietly(String connectionKey) {
        try {
            closeJmsConnection(connectionKey);
        } catch (Exception e) {
            logger.debug("Error closing JMS connection on channel " + getChannelId(), e);
        }
    }

    private void closeJmsConnection(String connectionKey) throws Exception {
        JmsConnection jmsConnection = jmsConnections.get(connectionKey);

        if (jmsConnection != null) {
            try {
                Exception firstException = null;

                Connection connection = jmsConnection.getConnection();
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (JMSException e) {
                        firstException = e;
                        logger.debug("Failed to close the JMS connection", e);
                    }
                }

                Context initialContext = jmsConnection.getInitialContext();
                if (initialContext != null) {
                    try {
                        initialContext.close();
                    } catch (NamingException e) {
                        if (firstException == null) {
                            firstException = e;
                        }
                        logger.debug("Failed to close the initial context", e);
                    }

                    initialContext = null;
                }

                if (firstException != null) {
                    throw firstException;
                }
            } finally {
                jmsConnections.remove(connectionKey);
            }
        }
    }

    public Destination getDestination(JmsDispatcherProperties jmsDispatcherProperties, JmsSession jmsSession, Context initialContext) throws Exception {
        try {
            String destinationName = jmsDispatcherProperties.getDestinationName();
            if (destinationName.equals(jmsSession.getDestinationName())) {
                /*
                 * Only lookup/create a new destination object if the destination name has changed
                 * since the
                 * last time this method was called.
                 */
                return jmsSession.getDestination();
            }

            jmsSession.setDestinationName(destinationName);

            if (jmsDispatcherProperties.isUseJndi()) {
                synchronized (initialContext) {
                    jmsSession.setDestination((Destination) initialContext.lookup(destinationName));
                }
            } else if (jmsDispatcherProperties.isTopic()) {
                jmsSession.setDestination(jmsSession.getSession().createTopic(destinationName));
                logger.debug("Connected to topic: " + destinationName);
            } else {
                jmsSession.setDestination(jmsSession.getSession().createQueue(destinationName));
                logger.debug("Connected to queue: " + destinationName);
            }

            return jmsSession.getDestination();
        } catch (Exception e) {
            jmsSession.setDestination(null);
            jmsSession.setDestinationName(null);

            throw e;
        }
    }

    private class JmsConnection {
        private Connection connection;
        private Context initialContext;
        private Map<Long, JmsSession> jmsSessions = new ConcurrentHashMap<Long, JmsSession>();

        public JmsConnection(Connection connection, Context initialContext) {
            this.connection = connection;
            this.initialContext = initialContext;
        }

        public Connection getConnection() {
            return connection;
        }

        public Context getInitialContext() {
            return initialContext;
        }

        public Map<Long, JmsSession> getJmsSessions() {
            return jmsSessions;
        }
    }

    private class JmsSession {
        private Session session;
        private MessageProducer producer;
        private String destinationName;
        private Destination destination;

        public JmsSession(Session session, MessageProducer producer) {
            this.session = session;
            this.producer = producer;
        }

        public Session getSession() {
            return session;
        }

        public MessageProducer getProducer() {
            return producer;
        }

        public String getDestinationName() {
            return destinationName;
        }

        public void setDestinationName(String destinationName) {
            this.destinationName = destinationName;
        }

        public Destination getDestination() {
            return destination;
        }

        public void setDestination(Destination destination) {
            this.destination = destination;
        }
    }
}
