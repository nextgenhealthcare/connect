package com.mirth.connect.connectors.jms;

import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.transport.Transport;
import org.apache.activemq.transport.TransportFilter;
import org.apache.activemq.transport.failover.FailoverTransport;
import org.apache.activemq.transport.tcp.TcpTransport;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

public class MirthActiveMQConnectionFactory implements ConnectionFactory {
    private ActiveMQConnectionFactory delegate;
    private int connectionTimeout = 10000;
    private Logger logger = Logger.getLogger(getClass());

    /**
     * @param connectionFactory
     *            An ActiveMQ connection factory
     * @param connectionProperties
     *            The connection properties that have already been run through the template value
     *            replacer
     */
    public MirthActiveMQConnectionFactory(ActiveMQConnectionFactory connectionFactory, Map<String, String> connectionProperties) {
        delegate = connectionFactory;

        // TODO this is an undocumented feature, users need a way to know that they can set 'connectionTimeout' in the connection properties table if the broker is an ActiveMQ broker
        if (connectionProperties.containsKey("connectionTimeout")) {
            connectionTimeout = NumberUtils.toInt(connectionProperties.get("connectionTimeout"));
        }
    }

    @Override
    public Connection createConnection() throws JMSException {
        return prepareConnection(delegate.createConnection());
    }

    @Override
    public Connection createConnection(String userName, String password) throws JMSException {
        return prepareConnection(delegate.createConnection(userName, password));
    }

    private Connection prepareConnection(Connection connection) {
        logger.debug("Preparing ActiveMQ connection");
        ActiveMQConnection activeMQConnection = (ActiveMQConnection) connection;

        if (connectionTimeout > 0) {
            setTransportConnectionTimeout(activeMQConnection.getTransport());
        }

        return connection;
    }

    private void setTransportConnectionTimeout(Transport transport) {
        if (transport instanceof TcpTransport) {
            logger.debug("Setting connection timeout for ActiveMQ TCP transport");
            ((TcpTransport) transport).setConnectionTimeout(connectionTimeout);
        } else if (transport instanceof FailoverTransport) {
            logger.debug("Setting connection timeout for ActiveMQ Failover transport");
            FailoverTransport failoverTransport = (FailoverTransport) transport;
            failoverTransport.setTimeout(connectionTimeout);
        } else if (transport instanceof TransportFilter) {
            setTransportConnectionTimeout(((TransportFilter) transport).getNext());
        } else {
            logger.debug("Could not set connection timeout, unrecognized transport type");
        }
    }
}
