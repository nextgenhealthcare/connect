/*
 * $Header: /home/projects/mule/scm/mule/providers/jms/src/java/org/mule/providers/jms/Jms102bSupport.java,v 1.7 2005/06/09 22:45:51 gnt Exp $
 * $Revision: 1.7 $
 * $Date: 2005/06/09 22:45:51 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
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
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;

/**
 * <code>Jms102bSupport</code> is a template class to provide an absstraction
 * to to the Jms 1.0.2b api specification.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.7 $
 */

public class Jms102bSupport extends Jms11Support
{
    public Jms102bSupport(JmsConnector connector,
                          Context context,
                          boolean jndiDestinations,
                          boolean forceJndiDestinations)
    {
        super(connector, context, jndiDestinations, forceJndiDestinations);
    }

    public Connection createConnection(ConnectionFactory connectionFactory, String username, String password)
            throws JMSException
    {
        if (connectionFactory == null) {
            throw new IllegalArgumentException("connectionFactory cannot be null");
        }
        if (connectionFactory instanceof QueueConnectionFactory) {
            return ((QueueConnectionFactory) connectionFactory).createQueueConnection(username, password);
        } else if (connectionFactory instanceof TopicConnectionFactory) {
            return ((TopicConnectionFactory) connectionFactory).createTopicConnection(username, password);
        } else {
            throw new IllegalArgumentException("Unsupported ConnectionFactory type: "
                    + connectionFactory.getClass().getName());
        }
    }

    public Connection createConnection(ConnectionFactory connectionFactory) throws JMSException
    {
        if (connectionFactory == null) {
            throw new IllegalArgumentException("connectionFactory cannot be null");
        }
        if (connectionFactory instanceof QueueConnectionFactory) {
            return ((QueueConnectionFactory) connectionFactory).createQueueConnection();
        } else if (connectionFactory instanceof TopicConnectionFactory) {
            return ((TopicConnectionFactory) connectionFactory).createTopicConnection();
        } else {
            throw new IllegalArgumentException("Unsupported ConnectionFactory type: "
                    + connectionFactory.getClass().getName());
        }
    }

    public Session createSession(Connection connection, boolean topic, boolean transacted, int ackMode, boolean noLocal)
            throws JMSException
    {
        if (topic) {
            if (connection instanceof TopicConnection) {
                return ((TopicConnection) connection).createTopicSession(noLocal, ackMode);
            }
        } else {
            if (connection instanceof QueueConnection) {
                return ((QueueConnection) connection).createQueueSession(transacted, ackMode);
            } 
        }
        throw new IllegalArgumentException("Unknown Jms connection: " + connection.getClass().getName());
    }

    public MessageConsumer createConsumer(Session session,
                                          Destination destination,
                                          String messageSelector,
                                          boolean noLocal,
                                          String durableName) throws JMSException
    {
        if (destination instanceof Queue) {
            if (session instanceof QueueSession) {
                if (messageSelector != null) {
                    return ((QueueSession) session).createReceiver((Queue) destination, messageSelector);
                } else {
                    return ((QueueSession) session).createReceiver((Queue) destination);
                }
            }
        } else {
            if (session instanceof TopicSession) {
                if (durableName == null) {
                    return ((TopicSession) session).createSubscriber((Topic) destination, messageSelector, noLocal);
                } else {
                    return ((TopicSession) session).createDurableSubscriber((Topic) destination,
                                                                            messageSelector,
                                                                            durableName,
                                                                            noLocal);
                }
            }
        }
        throw new IllegalArgumentException("Session and domain type do not match");
    }

    public MessageProducer createProducer(Session session, Destination dest) throws JMSException
    {
        if (dest instanceof Queue) {
            if (session instanceof QueueSession) {
                return ((QueueSession) session).createSender((Queue) dest);
            }
        } else {
            if (session instanceof TopicSession) {
                return ((TopicSession) session).createPublisher((Topic) dest);
            }
        }
        throw new IllegalArgumentException("Session and domain type do not match");
    }

    public void send(MessageProducer producer, Message message, boolean persistent, int priority, long ttl)
            throws JMSException
    {
        if (producer instanceof QueueSender) {
            ((QueueSender) producer).send(message,
                                          (persistent ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT),
                                          priority,
                                          ttl);
        } else {
            ((TopicPublisher) producer).publish(message, (persistent ? DeliveryMode.PERSISTENT
                    : DeliveryMode.NON_PERSISTENT), priority, ttl);
        }
    }

    public void send(MessageProducer producer,
                     Message message,
                     Destination dest,
                     boolean persistent,
                     int priority,
                     long ttl) throws JMSException
    {
        if (producer instanceof QueueSender) {
            ((QueueSender) producer).send((Queue) dest, message, (persistent ? DeliveryMode.PERSISTENT
                    : DeliveryMode.NON_PERSISTENT), priority, ttl);
        } else {
            ((TopicPublisher) producer).publish((Topic) dest, message, (persistent ? DeliveryMode.PERSISTENT
                    : DeliveryMode.NON_PERSISTENT), priority, ttl);
        }
    }
}
