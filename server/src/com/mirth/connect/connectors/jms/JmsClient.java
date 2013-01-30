package com.mirth.connect.connectors.jms;

import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.ChannelState;
import com.mirth.connect.donkey.server.channel.Connector;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.ErrorConstants;

/**
 * Represents the client connection to a JMS broker, used by both the JMS receiver and dispatcher
 * connectors
 */
public class JmsClient {
    private Connector connector;
    private ConnectorType connectorType;
    private JmsConnectorProperties connectorProperties;
    private Connection connection;
    private Session session;
    private Context initialContext;
    private Destination destination;
    private String destinationName;
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private Logger logger = Logger.getLogger(getClass());

    public JmsClient(final Connector connector, final ConnectorType connectorType, JmsConnectorProperties connectorProperties) {
        this.connector = connector;
        this.connectorType = connectorType;
        this.connectorProperties = connectorProperties;
    }

    private ConnectionFactory getConnectionFactory() throws Exception {
        ConnectionFactory connectionFactory;
        String channelId = connector.getChannelId();

        if (connectorProperties.isUseJndi()) {
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.PROVIDER_URL, replacer.replaceValues(connectorProperties.getJndiProviderUrl(), channelId));
            env.put(Context.INITIAL_CONTEXT_FACTORY, replacer.replaceValues(connectorProperties.getJndiInitialContextFactory(), channelId));

            initialContext = new InitialContext(env);
            String connectionFactoryName = replacer.replaceValues(connectorProperties.getJndiConnectionFactoryName(), channelId);
            connectionFactory = (ConnectionFactory) initialContext.lookup(connectionFactoryName);
        } else {
            String className = replacer.replaceValues(connectorProperties.getConnectionFactoryClass(), channelId);
            logger.debug("Instantiating connection factory: " + className);

            try {
                connectionFactory = (ConnectionFactory) Class.forName(className).newInstance();
            } catch (Exception e) {
                throw new Exception("Failed to instantiate ConnectionFactory class: " + className, e);
            }
        }

        /*
         * Take the Property/Value entries that the user provided in the connector UI and use
         * BeanUtils to set them on the ConnectionFactory.
         */
        Map<String, String> properties = connectorProperties.getConnectionProperties();

        for (Entry<String, String> entry : properties.entrySet()) {
            entry.setValue(replacer.replaceValues(entry.getValue(), channelId));

            try {
                BeanUtils.setProperty(connectionFactory, entry.getKey(), entry.getValue());
            } catch (Exception e) {
                logger.error("Failed to set ConnectionFactory property '" + entry.getKey() + "'", e);
            }
        }

        /*
         * Use a ConnectionFactory wrapper for recognized JMS implementations, in order to set
         * implementation-specific features (like connection timeout).
         */
        if (connectionFactory instanceof ActiveMQConnectionFactory) {
            connectionFactory = new MirthActiveMQConnectionFactory((ActiveMQConnectionFactory) connectionFactory, properties);
        }

        return connectionFactory;
    }

    /**
     * Starts a JMS connection and session.
     */
    public void start() throws Exception {
        ConnectionFactory connectionFactory = getConnectionFactory();
        final String channelId = connector.getChannelId();
        final int metaDataId = connector.getMetaDataId();

        try {
            logger.debug("Creating JMS connection and session");
            connection = connectionFactory.createConnection(replacer.replaceValues(connectorProperties.getUsername(), channelId), replacer.replaceValues(connectorProperties.getPassword(), channelId));

            String clientId = replacer.replaceValues(connectorProperties.getClientId(), channelId);

            if (!clientId.isEmpty()) {
                connection.setClientID(clientId);
            }

            /*
             * Set an exception listener that will detect if the JMS connection encounters a
             * problem. When that happens, attempt to reconnect every X milliseconds or until the
             * connector is stopped/undeployed.
             */
            connection.setExceptionListener(new ExceptionListener() {
                @Override
                public void onException(JMSException e) {
                    monitoringController.updateStatus(channelId, metaDataId, connectorType, Event.DISCONNECTED);
                    int reconnectIntervalMillis = NumberUtils.toInt(replacer.replaceValues(connectorProperties.getReconnectIntervalMillis(), channelId));

                    if (reconnectIntervalMillis > 0 && connector.getCurrentState() == ChannelState.STARTED) {
                        Exception exception;
                        reportError("A connection error occurred, attempting to reconnect", e);

                        do {
                            exception = null;

                            try {
                                connector.onStop();
                            } catch (Exception e1) {
                                logger.error("Failed to close connection", e1);
                            }

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
                }
            });

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

    private void reportError(String errorMessage, Exception e) {
        String channelId = connector.getChannelId();
        logger.error(errorMessage + " (channel: " + ChannelController.getInstance().getDeployedChannelById(channelId).getName() + ")", e);
        alertController.sendAlerts(channelId, ErrorConstants.ERROR_407, null, e.getCause());
    }
}
