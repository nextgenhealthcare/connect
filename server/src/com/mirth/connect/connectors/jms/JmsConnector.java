/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.mirth.connect.connectors.jms;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.XAConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang.UnhandledException;
import org.mule.MuleManager;
import org.mule.MuleRuntimeException;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.internal.events.ConnectionEvent;
import org.mule.impl.internal.events.ConnectionEventListener;
import org.mule.providers.AbstractServiceEnabledConnector;
import org.mule.providers.ConnectException;
import org.mule.providers.FatalConnectException;
import org.mule.providers.ReplyToHandler;
import org.mule.providers.TemplateValueReplacer;
import org.mule.transaction.TransactionCoordination;
import org.mule.umo.TransactionException;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOTransaction;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.lifecycle.LifecycleException;
import org.mule.umo.manager.UMOServerEvent;
import org.mule.util.BeanUtils;
import org.mule.util.ClassHelper;

import com.mirth.connect.connectors.jms.xa.ConnectionFactoryWrapper;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;

/**
 * <code>JmsConnector</code> is a JMS 1.0.2b compliant connector that can be
 * used by a Mule endpoint. The connector supports all Jms functionality
 * including, topics and queues, durable subscribers, acknowledgement modes,
 * loacal transactions
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @author Guillaume Nodet
 * @version $Revision: 1.33 $
 */

public class JmsConnector extends AbstractServiceEnabledConnector implements ConnectionEventListener {
    private String connectionFactoryJndiName;
    private ConnectionFactory connectionFactory;
    private String connectionFactoryClass;
    private String jndiInitialFactory;
    private String jndiProviderUrl;
    private int acknowledgementMode = Session.AUTO_ACKNOWLEDGE;
    private String clientId;
    private boolean durable;
    private boolean noLocal;
    private boolean persistentDelivery;
    private Map jndiProviderProperties;
    private Map connectionFactoryProperties;
    private Connection connection;
    private String specification = JmsConstants.JMS_SPECIFICATION_102B;
    private JmsSupport jmsSupport;
    private Context jndiContext;
    private boolean jndiDestinations = false;
    private boolean forceJndiDestinations = false;
    public String username = null;
    public String password = null;
    private int maxRedelivery = 0;
    private String template;
    private String redeliveryHandler = DefaultRedeliveryHandler.class.getName();
    private String channelId;
    private String selector;
    private int frequency = 10000;
    private boolean recoverJmsConnections = true;

    private TemplateValueReplacer replacer = new TemplateValueReplacer();

    public JmsConnector() {
        receivers = new ConcurrentHashMap();
    }

    public String getChannelId() {
        return this.channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.UMOConnector#create(java.util.HashMap)
     */
    public void doInitialise() throws InitialisationException {
        super.doInitialise();
        MuleManager.getInstance().registerListener(this, getName());
        try {
            // If we have a connection factory, there is no need to initialise
            // the JndiContext
            if ((connectionFactory == null && connectionFactoryClass == null) || (connectionFactory != null && jndiInitialFactory != null)) {
                initJndiContext();
                jndiDestinations = true;
                forceJndiDestinations = true;
            } else {
                // Set these to false so that the jndiContext
                // will not be used by the JmsSupport classes
                jndiDestinations = false;
                forceJndiDestinations = false;
            }

            if (JmsConstants.JMS_SPECIFICATION_102B.equals(specification)) {
                jmsSupport = new Jms102bSupport(this, jndiContext, jndiDestinations, forceJndiDestinations);
            } else {
                jmsSupport = new Jms11Support(this, jndiContext, jndiDestinations, forceJndiDestinations);
            }

        } catch (Exception e) {
            throw new InitialisationException(new Message(Messages.FAILED_TO_CREATE_X, "Jms Connector"), e, this);
        }
    }

    protected void initJndiContext() throws NamingException, InitialisationException {
        if (jndiContext == null) {
            Hashtable props = new Hashtable();

            if (jndiInitialFactory != null) {
                props.put(Context.INITIAL_CONTEXT_FACTORY, jndiInitialFactory);
            } else if (jndiProviderProperties == null || !jndiProviderProperties.containsKey(Context.INITIAL_CONTEXT_FACTORY)) {
                throw new InitialisationException(new Message(Messages.X_IS_NULL, "jndiInitialFactory"), this);
            }

            if (jndiProviderUrl != null) {
                props.put(Context.PROVIDER_URL, replacer.replaceValues(jndiProviderUrl));
            }
            
            if (jndiProviderProperties != null) {
                props.putAll(jndiProviderProperties);
            }
            jndiContext = new InitialContext(props);
        }
    }

    protected void setConnection(Connection connection) {
        this.connection = connection;
    }

    protected ConnectionFactory createConnectionFactory() throws InitialisationException, NamingException {

        // If JNDI is being used, connectionFactoryClass will be null.
        if (connectionFactoryClass != null) {
            try {
                ConnectionFactory connectionFactory = (ConnectionFactory) Class.forName(connectionFactoryClass).newInstance();
                if (connectionFactory != null && connectionFactoryProperties != null && !connectionFactoryProperties.isEmpty()) {
                    // replace connection factory property values
                    for (Iterator<Entry<String, String>> iterator = connectionFactoryProperties.entrySet().iterator(); iterator.hasNext();) {
                        Entry<String, String> entry = iterator.next();
                        entry.setValue(replacer.replaceValues(entry.getValue(), channelId));
                    }

                    // apply connection factory properties
                    BeanUtils.populateWithoutFail(connectionFactory, connectionFactoryProperties, true);
                }
                return connectionFactory;
            } catch (Exception e) {
                throw new InitialisationException(e, this);
            }
        } else {
            Object temp = jndiContext.lookup(connectionFactoryJndiName);

            if (temp instanceof ConnectionFactory) {
                return (ConnectionFactory) temp;
            } else {
                throw new InitialisationException(new Message(Messages.JNDI_RESOURCE_X_NOT_FOUND, connectionFactoryJndiName), this);
            }
        }
    }

    protected Connection createConnection() throws NamingException, JMSException, InitialisationException {
        Connection connection = null;

        if (connectionFactory == null) {
            connectionFactory = createConnectionFactory();
        }

        if (connectionFactory != null && connectionFactory instanceof XAConnectionFactory) {
            if (MuleManager.getInstance().getTransactionManager() != null) {
                connectionFactory = new ConnectionFactoryWrapper(connectionFactory, MuleManager.getInstance().getTransactionManager());
            }
        }

        if (username != null) {
            connection = jmsSupport.createConnection(connectionFactory, username, password);
        } else {
            connection = jmsSupport.createConnection(connectionFactory);
        }

        if (clientId != null) {
            connection.setClientID(getClientId());
        }

        if (recoverJmsConnections && connection != null) {
            connection.setExceptionListener(new ExceptionListener() {
                public void onException(JMSException jmsException) {
                    logger.debug("About to recycle myself due to remote JMS connection shutdown.");
                    final JmsConnector jmsConnector = JmsConnector.this;
                    try {
                        jmsConnector.doStop();
                        jmsConnector.initialised.set(false);
                    } catch (UMOException e) {
                        logger.warn(e.getMessage(), e);
                    }

                    try {
                        jmsConnector.doConnect();
                        jmsConnector.doInitialise();
                        jmsConnector.doStart();
                    } catch (FatalConnectException fcex) {
                        logger.fatal("Failed to reconnect to JMS server. I'm giving up.");
                    } catch (UMOException umoex) {
                        throw new UnhandledException("Failed to recover a connector.", umoex);
                    }
                }
            });
        }

        return connection;
    }

    public void doConnect() throws ConnectException {
        try {
            connection = createConnection();
            if (started.get()) {
                connection.start();
            }
        } catch (Exception e) {
             throw new ConnectException(e, this);
        }
    }

    public void doDisconnect() throws ConnectException {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (Exception e) {
            throw new ConnectException(e, this);
        } finally {
            connectionFactory = null;
            connection = null;
        }
    }

    protected Object getReceiverKey(UMOComponent component, UMOEndpoint endpoint) {
        return component.getDescriptor().getName() + "~" + endpoint.getEndpointURI().getAddress();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.mule.providers.TransactionEnabledConnector#getSessionFactory(org.
     * mule.umo.endpoint.UMOEndpoint)
     */
    public Object getSessionFactory(UMOEndpoint endpoint) {
        if (endpoint.getTransactionConfig() != null && endpoint.getTransactionConfig().getFactory() instanceof JmsClientAcknowledgeTransactionFactory) {
            throw new MuleRuntimeException(new org.mule.config.i18n.Message("jms", 9));
        } else {
            return connection;
        }
    }

    public Session getCurrentSession() {
        UMOTransaction tx = TransactionCoordination.getInstance().getTransaction();
        if (tx != null) {
            if (tx.hasResource(connection)) {
                return (Session) tx.getResource(connection);
            }
        }
        return null;
    }

    public Session getSession(boolean transacted, boolean topic) throws JMSException {
        if (!isConnected()) {
            throw new JMSException("Not connected");
        }
        UMOTransaction tx = TransactionCoordination.getInstance().getTransaction();
        Session session = getCurrentSession();
        if (session != null) {
            logger.debug("Retrieving jms session from current transaction");
            return session;
        }
        logger.debug("Retrieving new jms session from connection");
        session = jmsSupport.createSession(connection, topic, transacted || tx != null, acknowledgementMode, noLocal);
        if (tx != null) {
            logger.debug("Binding session to current transaction");
            try {
                tx.bindResource(connection, session);
            } catch (TransactionException e) {
                throw new RuntimeException("Could not bind session to current transaction", e);
            }
        }
        return session;
    }

    public void doStart() throws UMOException {
        if (connection != null) {
            try {
                connection.start();
            } catch (JMSException e) {
                throw new LifecycleException(new Message(Messages.FAILED_TO_START_X, "Jms Connection"), e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.UMOConnector#getProtocol()
     */
    public String getProtocol() {
        return "jms";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.AbstractConnector#doDispose()
     */
    protected void doDispose() {
        super.doDispose();
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException e) {
                logger.error("Jms connector failed to dispose properly: ", e);
            }
            connection = null;
        }
        if (jndiContext != null) {
            try {
                jndiContext.close();
            } catch (NamingException e) {
                logger.error("Jms connector failed to dispose properly: ", e);
            }
        }
        jndiContext = null;
    }

    /**
     * @return Returns the acknowledgeMode.
     */
    public int getAcknowledgementMode() {
        return acknowledgementMode;
    }

    /**
     * @param acknowledgementMode
     *            The acknowledgementMode to set.
     */
    public void setAcknowledgementMode(int acknowledgementMode) {
        this.acknowledgementMode = acknowledgementMode;
    }

    /**
     * @return Returns the connectionFactoryJndiName.
     */
    public String getConnectionFactoryJndiName() {
        return connectionFactoryJndiName;
    }

    /**
     * @param connectionFactoryJndiName
     *            The connectionFactoryJndiName to set.
     */
    public void setConnectionFactoryJndiName(String connectionFactoryJndiName) {
        this.connectionFactoryJndiName = connectionFactoryJndiName;
    }

    /**
     * @return Returns the durable.
     */
    public boolean isDurable() {
        return durable;
    }

    /**
     * @param durable
     *            The durable to set.
     */
    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    /**
     * @return Returns the noLocal.
     */
    public boolean isNoLocal() {
        return noLocal;
    }

    /**
     * @param noLocal
     *            The noLocal to set.
     */
    public void setNoLocal(boolean noLocal) {
        this.noLocal = noLocal;
    }

    /**
     * @return Returns the persistentDelivery.
     */
    public boolean isPersistentDelivery() {
        return persistentDelivery;
    }

    /**
     * @param persistentDelivery
     *            The persistentDelivery to set.
     */
    public void setPersistentDelivery(boolean persistentDelivery) {
        this.persistentDelivery = persistentDelivery;
    }

    /**
     * @return Returns the JNDI providerProperties.
     * @since 1.1
     */
    public Map getJndiProviderProperties() {
        return jndiProviderProperties;
    }

    /**
     * @param jndiProviderProperties
     *            The JNDI providerProperties to set.
     * @since 1.1
     */
    public void setJndiProviderProperties(final Map jndiProviderProperties) {
        this.jndiProviderProperties = jndiProviderProperties;
    }

    /**
     * @return Returns underlying connection factory properties.
     */
    public Map getConnectionFactoryProperties() {
        return connectionFactoryProperties;
    }

    /**
     * @param connectionFactoryProperties
     *            properties to be set on the underlying ConnectionFactory.
     */
    public void setConnectionFactoryProperties(final Map connectionFactoryProperties) {
        this.connectionFactoryProperties = connectionFactoryProperties;
    }

    public String getJndiInitialFactory() {
        return jndiInitialFactory;
    }

    public void setJndiInitialFactory(String jndiInitialFactory) {
        this.jndiInitialFactory = jndiInitialFactory;
    }

    public String getJndiProviderUrl() {
        return jndiProviderUrl;
    }

    public void setJndiProviderUrl(String jndiProviderUrl) {
        this.jndiProviderUrl = jndiProviderUrl;
    }

    public Session getSession(UMOEndpoint endpoint) throws Exception {
        String resourceInfo = endpoint.getEndpointURI().getResourceInfo();
        boolean topic = (resourceInfo != null && "topic".equalsIgnoreCase(resourceInfo));
        return getSession(endpoint.getTransactionConfig().isTransacted(), topic);
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public String getConnectionFactoryClass() {
        return connectionFactoryClass;
    }

    public void setConnectionFactoryClass(String connectionFactoryClass) {
        this.connectionFactoryClass = connectionFactoryClass;
    }

    public JmsSupport getJmsSupport() {
        return jmsSupport;
    }

    public void setJmsSupport(JmsSupport jmsSupport) {
        this.jmsSupport = jmsSupport;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public boolean isJndiDestinations() {
        return jndiDestinations;
    }

    public void setJndiDestinations(boolean jndiDestinations) {
        this.jndiDestinations = jndiDestinations;
    }

    public boolean isForceJndiDestinations() {
        return forceJndiDestinations;
    }

    public void setForceJndiDestinations(boolean forceJndiDestinations) {
        this.forceJndiDestinations = forceJndiDestinations;
    }

    public Context getJndiContext() {
        return jndiContext;
    }

    public void setJndiContext(Context jndiContext) {
        this.jndiContext = jndiContext;
    }
    
    public void setSelector(String selector) {
        this.selector = selector;
    }
    
    public String getSelector() {
        return this.selector;
    }

    protected RedeliveryHandler createRedeliveryHandler() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException, ClassNotFoundException {
        if (redeliveryHandler != null) {
            return (RedeliveryHandler) ClassHelper.instanciateClass(redeliveryHandler, ClassHelper.NO_ARGS);
        } else {
            return new DefaultRedeliveryHandler();
        }
    }

    public ReplyToHandler getReplyToHandler() {
        return new JmsReplyToHandler(this, defaultOutboundTransformer);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return Returns the connection.
     */
    public Connection getConnection() {
        return connection;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public int getMaxRedelivery() {
        return maxRedelivery;
    }

    public void setMaxRedelivery(int maxRedelivery) {
        this.maxRedelivery = maxRedelivery;
    }

    public String getRedeliveryHandler() {
        return redeliveryHandler;
    }

    public void setRedeliveryHandler(String redeliveryHandler) {
        this.redeliveryHandler = redeliveryHandler;
    }

    public boolean isRemoteSyncEnabled() {
        return true;
    }

    public void onEvent(UMOServerEvent event) {
        if (event.getAction() == ConnectionEvent.CONNECTION_DISCONNECTED) {
            // Remove all dispatchers as any cached session will be invalidated
            disposeDispatchers();
        }
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
}
