/*
 * $Header: /home/projects/mule/scm/mule/providers/jms/src/java/org/mule/providers/jms/xa/ConnectionFactoryWrapper.java,v 1.4 2005/06/03 01:20:34 gnt Exp $
 * $Revision: 1.4 $
 * $Date: 2005/06/03 01:20:34 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package com.webreach.mirth.connectors.jms.xa;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XAQueueConnection;
import javax.jms.XAQueueConnectionFactory;
import javax.jms.XAQueueSession;
import javax.jms.XASession;
import javax.jms.XATopicConnection;
import javax.jms.XATopicConnectionFactory;
import javax.jms.XATopicSession;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Guillaume Nodet
 * @version $Revision: 1.4 $
 */
public class ConnectionFactoryWrapper implements ConnectionFactory, QueueConnectionFactory, TopicConnectionFactory
{

    protected Object factory;
    protected TransactionManager tm;
    protected static transient Log logger = LogFactory.getLog(ConnectionFactoryWrapper.class);

    public ConnectionFactoryWrapper(Object factory, TransactionManager tm)
    {
        this.factory = factory;
        this.tm = tm;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.ConnectionFactory#createConnection()
     */
    public Connection createConnection() throws JMSException
    {
        XAConnection xac = ((XAConnectionFactory) factory).createXAConnection();
        Connection proxy = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                                                               new Class[] { Connection.class },
                                                               new ConnectionInvocationHandler(xac));
        return proxy;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.ConnectionFactory#createConnection(java.lang.String,
     *      java.lang.String)
     */
    public Connection createConnection(String username, String password) throws JMSException
    {
        XAConnection xac = ((XAConnectionFactory) factory).createXAConnection(username, password);
        Connection proxy = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                                                               new Class[] { Connection.class },
                                                               new ConnectionInvocationHandler(xac));
        return proxy;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.QueueConnectionFactory#createQueueConnection()
     */
    public QueueConnection createQueueConnection() throws JMSException
    {
        XAQueueConnection xaqc = ((XAQueueConnectionFactory) factory).createXAQueueConnection();
        QueueConnection proxy = (QueueConnection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                                                                         new Class[] { QueueConnection.class },
                                                                         new ConnectionInvocationHandler(xaqc));
        return proxy;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.QueueConnectionFactory#createQueueConnection(java.lang.String,
     *      java.lang.String)
     */
    public QueueConnection createQueueConnection(String username, String password) throws JMSException
    {
        XAQueueConnection xaqc = ((XAQueueConnectionFactory) factory).createXAQueueConnection(username, password);
        QueueConnection proxy = (QueueConnection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                                                                         new Class[] { QueueConnection.class },
                                                                         new ConnectionInvocationHandler(xaqc));
        return proxy;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.TopicConnectionFactory#createTopicConnection()
     */
    public TopicConnection createTopicConnection() throws JMSException
    {
        XATopicConnection xatc = ((XATopicConnectionFactory) factory).createXATopicConnection();
        TopicConnection proxy = (TopicConnection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                                                                         new Class[] { TopicConnection.class },
                                                                         new ConnectionInvocationHandler(xatc));
        return proxy;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.TopicConnectionFactory#createTopicConnection(java.lang.String,
     *      java.lang.String)
     */
    public TopicConnection createTopicConnection(String username, String password) throws JMSException
    {
        XATopicConnection xatc = ((XATopicConnectionFactory) factory).createXATopicConnection(username, password);
        TopicConnection proxy = (TopicConnection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                                                                         new Class[] { TopicConnection.class },
                                                                         new ConnectionInvocationHandler(xatc));
        return proxy;
    }

    protected class ConnectionInvocationHandler implements InvocationHandler
    {

        private Object xac;

        public ConnectionInvocationHandler(Object xac)
        {
            this.xac = xac;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
         *      java.lang.reflect.Method, java.lang.Object[])
         */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            if (logger.isDebugEnabled()) {
                logger.debug("Invoking " + method);
            }
            if (method.getName().equals("createSession")) {
                XASession xas = ((XAConnection) xac).createXASession();
                return (Session) Proxy.newProxyInstance(Session.class.getClassLoader(),
                                                        new Class[] { Session.class },
                                                        new SessionInvocationHandler(xas.getSession(),
                                                                                     xas.getXAResource()));
            } else if (method.getName().equals("createQueueSession")) {
                XAQueueSession xaqs = ((XAQueueConnection) xac).createXAQueueSession();
                return (Session) Proxy.newProxyInstance(Session.class.getClassLoader(),
                                                        new Class[] { QueueSession.class },
                                                        new SessionInvocationHandler(xaqs.getQueueSession(),
                                                                                     xaqs.getXAResource()));
            } else if (method.getName().equals("createTopicSession")) {
                XATopicSession xats = ((XATopicConnection) xac).createXATopicSession();
                return (Session) Proxy.newProxyInstance(Session.class.getClassLoader(),
                                                        new Class[] { TopicSession.class },
                                                        new SessionInvocationHandler(xats.getTopicSession(),
                                                                                     xats.getXAResource()));
            } else {
                return method.invoke(xac, args);
            }
        }

        protected class SessionInvocationHandler implements InvocationHandler
        {

            private Object session;
            private Object xares;
            private Transaction tx;

            public SessionInvocationHandler(Object session, Object xares)
            {
                this.session = session;
                this.xares = xares;
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
             *      java.lang.reflect.Method, java.lang.Object[])
             */
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
            {
                if (logger.isDebugEnabled()) {
                    logger.debug("Invoking " + method);
                }
                Object result = method.invoke(session, args);
                if (result instanceof MessageConsumer) {
                    result = Proxy.newProxyInstance(Session.class.getClassLoader(),
                                                    new Class[] { MessageConsumer.class },
                                                    new ConsumerProducerInvocationHandler(result));
                } else if (result instanceof MessageProducer) {
                    result = Proxy.newProxyInstance(Session.class.getClassLoader(),
                                                    new Class[] { MessageProducer.class },
                                                    new ConsumerProducerInvocationHandler(result));
                }
                return result;
            }

            protected void enlist() throws Exception
            {
                if (logger.isDebugEnabled()) {
                    logger.debug("Enlistment request: " + this);
                }
                if (tx == null && tm != null) {
                    tx = tm.getTransaction();
                    if (tx != null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Enlisting resource in xa transaction: " + xares);
                        }
                        XAResource xares = (XAResource) Proxy.newProxyInstance(XAResource.class.getClassLoader(),
                                                                               new Class[] { XAResource.class },
                                                                               new XAResourceInvocationHandler());
                        tx.enlistResource(xares);
                    }
                }
            }

            protected class XAResourceInvocationHandler implements InvocationHandler
            {

                /*
                 * (non-Javadoc)
                 * 
                 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
                 *      java.lang.reflect.Method, java.lang.Object[])
                 */
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                {
                    try {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Invoking " + method);
                        }
                        if (method.getName().equals("end")) {
                            tx = null;
                        }
                        return method.invoke(xares, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                }
            }

            protected class ConsumerProducerInvocationHandler implements InvocationHandler
            {

                private Object target;

                public ConsumerProducerInvocationHandler(Object target)
                {
                    this.target = target;
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
                 *      java.lang.reflect.Method, java.lang.Object[])
                 */
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Invoking " + method);
                    }
                    if (!method.getName().equals("close")) {
                        enlist();
                    }
                    return method.invoke(target, args);
                }
            }

        }
    }

}
