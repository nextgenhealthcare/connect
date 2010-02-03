/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.webreach.mirth.connectors.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.NamingException;

/**
 * <code>Jms11Support</code> is a template class to provide an absstraction to
 * to the Jms 1.1 api specification.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.9 $
 */

public class Jms11Support implements JmsSupport {
    protected Context context;
    protected boolean jndiDestinations = false;
    protected boolean forceJndiDestinations = false;
    protected JmsConnector connector;

    public Jms11Support(JmsConnector connector, Context context, boolean jndiDestinations, boolean forceJndiDestinations) {
        this.connector = connector;
        this.context = context;
        this.jndiDestinations = jndiDestinations;
        this.forceJndiDestinations = forceJndiDestinations;
    }

    public Connection createConnection(ConnectionFactory connectionFactory, String username, String password) throws JMSException {
        if (connectionFactory == null) {
            throw new IllegalArgumentException("connectionFactory cannot be null");
        }
        return connectionFactory.createConnection(username, password);
    }

    public Connection createConnection(ConnectionFactory connectionFactory) throws JMSException {
        if (connectionFactory == null) {
            throw new IllegalArgumentException("connectionFactory cannot be null");
        }
        return connectionFactory.createConnection();
    }

    public Session createSession(Connection connection, boolean topic, boolean transacted, int ackMode, boolean noLocal) throws JMSException {
        return connection.createSession(transacted, (transacted ? Session.SESSION_TRANSACTED : ackMode));
    }

    public MessageProducer createProducer(Session session, Destination destination) throws JMSException {
        return session.createProducer(destination);
    }

    public MessageConsumer createConsumer(Session session, Destination destination) throws JMSException {
        return createConsumer(session, destination, null, false, null);
    }

    public MessageConsumer createConsumer(Session session, Destination destination, String messageSelector, boolean noLocal, String durableName) throws JMSException {
        if (durableName == null) {
            if (destination instanceof Topic) {
                return session.createConsumer(destination, messageSelector, noLocal);
            } else {
                return session.createConsumer(destination, messageSelector);
            }
        } else if (destination instanceof Topic) {
            return session.createDurableSubscriber((Topic) destination, durableName, messageSelector, noLocal);
        } else {
            throw new JMSException("A durable subscriber name was set but the destination was not a Topic");
        }
    }

    public Destination createDestination(Session session, String name, boolean topic) throws JMSException {
        if (session == null) {
            throw new IllegalArgumentException("Session cannot be null when creating a destination");
        }
        if (name == null) {
            throw new IllegalArgumentException("Destination name cannot be null when creating a destination");
        }

        if (jndiDestinations) {
            if (context == null) {
                throw new IllegalArgumentException("Jndi Context name cannot be null when looking up a destination");
            }
            Destination dest = getJndiDestination(name);
            if (dest != null) {
                return dest;
            } else if (forceJndiDestinations) {
                throw new JMSException("JNDI destination not found with name: " + name);
            }
        }

        if (!topic) {
            if (session instanceof QueueSession) {
                return ((QueueSession) session).createQueue(name);
            } else {
                return session.createQueue(name);
            }
        } else {
            if (session instanceof TopicSession) {
                return ((TopicSession) session).createTopic(name);
            } else {
                return session.createTopic(name);
            }
        }
    }

    protected Destination getJndiDestination(String name) throws JMSException {
        Object temp = null;
        try {
            temp = context.lookup(name);
        } catch (NamingException e) {
            throw new JMSException("Failed to look up destination: " + e.getMessage());
        }
        if (temp != null) {
            if (temp instanceof Destination) {
                return (Destination) temp;
            } else if (forceJndiDestinations) {
                throw new JMSException("JNDI destination not found with name: " + name);
            }
        }
        return null;
    }

    public Destination createTemporaryDestination(Session session, boolean topic) throws JMSException {
        if (session == null) {
            throw new IllegalArgumentException("Session cannot be null when creating a destination");
        }

        if (!topic) {
            if (session instanceof QueueSession) {
                return ((QueueSession) session).createTemporaryQueue();
            } else {
                return session.createTemporaryQueue();
            }
        } else {
            if (session instanceof TopicSession) {
                return ((TopicSession) session).createTemporaryTopic();
            } else {
                return session.createTemporaryTopic();
            }
        }
    }

    public void send(MessageProducer producer, Message message) throws JMSException {
        send(producer, message, connector.isPersistentDelivery(), Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
    }

    public void send(MessageProducer producer, Message message, Destination dest) throws JMSException {
        send(producer, message, dest, connector.isPersistentDelivery(), Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
    }

    public void send(MessageProducer producer, Message message, boolean persistent, int priority, long ttl) throws JMSException {
        producer.send(message, (persistent ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT), priority, ttl);
    }

    public void send(MessageProducer producer, Message message, Destination dest, boolean persistent, int priority, long ttl) throws JMSException {
        producer.send(dest, message, (persistent ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT), priority, ttl);
    }
}
