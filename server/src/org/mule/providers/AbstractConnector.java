/*
 * $Header: /cvsroot/mule/mule/src/java/org/mule/providers/AbstractConnector.java,v 1.16 2003/12/11
 * 13:32:40 rossmason Exp $ $Revision: 1.35 $ $Date: 2005/11/01 17:03:31 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved. http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD style license a copy of
 * which has been included with this distribution in the LICENSE.txt file.
 *
 */

package org.mule.providers;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.MuleManager;
import org.mule.MuleRuntimeException;
import org.mule.config.ThreadingProfile;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.AlreadyInitialisedException;
import org.mule.impl.DefaultExceptionStrategy;
import org.mule.impl.internal.events.ConnectionEvent;
import org.mule.management.mbeans.EndpointService;
import org.mule.routing.filters.WildcardFilter;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.lifecycle.DisposeException;
import org.mule.umo.lifecycle.Initialisable;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.manager.UMOServerEvent;
import org.mule.umo.provider.ConnectorException;
import org.mule.umo.provider.UMOConnectable;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageDispatcher;
import org.mule.umo.provider.UMOMessageDispatcherFactory;
import org.mule.umo.provider.UMOMessageReceiver;
import org.mule.umo.transformer.UMOTransformer;
import org.mule.util.concurrent.WaitableBoolean;

import java.beans.ExceptionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <code>AbstractConnector</code> provides base functionality for all
 * connectors provided with Mule. Connectors are the mechanism used to connect
 * to external systems and protocols in order to send and receive data. <p/> The
 * <code>AbstractConnector</code> provides getter and setter methods for
 * endpoint name, transport name and protocol. It also provides methods to stop
 * and start connecotors and sets up a dispatcher threadpool which allows
 * deriving connectors the possibility to dispatch work to separate threads.
 * This functionality is controlled with the <i> doThreading</i> property on
 * the threadingProfiles for dispachers and receivers.
 *
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.35 $
 */
public abstract class AbstractConnector implements UMOConnector, ExceptionListener, UMOConnectable
{
    /**
     * logger used by this class
     */
    protected transient Log logger = LogFactory.getLog(getClass());

    /**
     * Specifies if the endpoint started
     */
    protected AtomicBoolean started = new AtomicBoolean(false);

    /**
     * True once the endpoint has been initialsed
     */
    protected AtomicBoolean initialised = new AtomicBoolean(false);

    /**
     * The name that identifies the endpoint
     */
    protected String name = null;

    /**
     * The exception strategy used by this connector
     */
    protected ExceptionListener exceptionListener = null;

    /**
     * Determines in the connector is alive and well
     */
    protected AtomicBoolean disposed = new AtomicBoolean(false);

    /**
     * Determines in connector has been told to dispose
     */
    protected AtomicBoolean disposing = new AtomicBoolean(false);

    /**
     * Factory used to create dispatchers for this connector
     */
    protected UMOMessageDispatcherFactory dispatcherFactory;

    /**
     * A pool of dispatchers for this connector, the pool is keyed on
     * endpointUri
     */
    protected ConcurrentHashMap dispatchers;

    /**
     * The collection of listeners on this connector. Keyed by entrypoint
     */
    protected ConcurrentHashMap receivers;

    /**
     * Defines the dispatcher threading model
     */
    private ThreadingProfile dispatcherThreadingProfile = null;

    /**
     * Defines the receiver threading model
     */
    private ThreadingProfile receiverThreadingProfile = null;

    /**
     * Determines whether dispatchers should be disposed straight away of
     * after every request or cached
     */
    protected boolean createDispatcherPerRequest = false;

    /**
     * For better throughput when using TransactedMessageReceivers. This will create an number
     * of receiver threads based on the ThreadingProfile configured fro the receiver. This property is
     * user by transports that support transactions, specifically MessageReceivers that extend the
     * TransactedPollingMessageReceiver.
     */
    protected boolean createMultipleTransactedReceivers = true;

    /**
     * The service descriptor can define a default inbound transformer to be
     * used on an endpoint if no other is set
     */
    protected UMOTransformer defaultInboundTransformer = null;

    /**
     * The service descriptor can define a default outbound transformer to be
     * used on an endpoint if no other is set
     */
    protected UMOTransformer defaultOutboundTransformer = null;

    /**
     * For some connectors such as http, a response transformer is required or
     * where a replyTo needs a trnasformer
     */
    protected UMOTransformer defaultResponseTransformer = null;

    private ConnectionStrategy connectionStrategy;

    protected WaitableBoolean connected = new WaitableBoolean(false);

    protected WaitableBoolean connecting = new WaitableBoolean(false);

    /** keeps arecord of whether the connecter should be started once it is reconnected */
    protected WaitableBoolean startedBeforeDisconnect = new WaitableBoolean(false);

    /** Whether to fire message events for every message that is sent or received from this connector */
    private boolean enableMessageEvents = false;

    private List supportedProtocols;

    public AbstractConnector()
    {
        // make sure we always have an exception strategy
        exceptionListener = new DefaultExceptionStrategy();
        dispatchers = new ConcurrentHashMap();
        receivers = new ConcurrentHashMap();
        connectionStrategy = MuleManager.getConfiguration().getConnectionStrategy();
        enableMessageEvents = MuleManager.getConfiguration().isEnableMessageEvents();
        supportedProtocols = new ArrayList();
        //Always add the default protocol
        supportedProtocols.add(getProtocol().toLowerCase());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.UMOConnector#getName()
     */
    public String getName()
    {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.UMOConnector#setName(java.lang.String)
     */
    public void setName(String newName)
    {
        if (newName == null) {
            throw new IllegalArgumentException("Connector name cannot be null");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Set UMOConnector name to: " + newName);
        }
        name = newName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.providers.UMOConnector#create(java.util.HashMap)
     */
    public final synchronized void initialise() throws InitialisationException
    {
        if (initialised.get()) {
            throw new AlreadyInitialisedException("Connector '" + getName() + "'", this);
        }
        if (logger.isInfoEnabled()) {
            logger.info("Initialising " + getClass().getName());
        }
        if (exceptionListener instanceof Initialisable) {
            ((Initialisable) exceptionListener).initialise();
        }

        doInitialise();
        initialised.set(true);
    }

    public abstract String getProtocol();

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.provider.UMOConnector#start()
     */
    public final void startConnector() throws UMOException
    {
        if (isDisposed()) {
            throw new ConnectorException(new Message(Messages.CANT_START_DISPOSED_CONNECTOR), this);
        }
        if (!started.get()) {
            if(!isConnected()) {
                getConnectionStrategy().connect(this);
            }
            if (logger.isInfoEnabled()) {
                logger.info("Starting Connector: " + getClass().getName());
            }
            doStart();
            for (Iterator iterator = receivers.values().iterator(); iterator.hasNext();) {
                AbstractMessageReceiver amr = (AbstractMessageReceiver) iterator.next();
                if (logger.isDebugEnabled()) {
                    logger.debug("Starting receiver on endpoint: " + amr.getEndpoint().getEndpointURI());
                }
                amr.start();
            }
            started.set(true);
            if (logger.isInfoEnabled()) {
                logger.info("Connector: " + getClass().getName() + " has been started");
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.provider.UMOConnector#isStarted()
     */
    public boolean isStarted()
    {
        return started.get();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.provider.UMOConnector#stop()
     */
    public final void stopConnector() throws UMOException
    {
        if (isDisposed()) {
            return; // throw new MuleException("Cannot stop a connector once it
            // has been disposing");
        }
        if (started.get()) {
            if (logger.isInfoEnabled()) {
                logger.info("Stopping Connector: " + getClass().getName());
            }
            doStop();
            for (Iterator iterator = receivers.values().iterator(); iterator.hasNext();) {
            	UMOMessageReceiver mr = (UMOMessageReceiver) iterator.next();
                if (logger.isDebugEnabled()) {
                    logger.debug("Stopping receiver on endpoint: " + mr.getEndpoint().getEndpointURI());
                }
                mr.stop();
            }
            started.set(false);
            if(isConnected()) {
                try {
                    disconnect();
                } catch (Exception e) {
                    logger.error("Failed to disconnect: " + e.getMessage(), e);
                }
            }
            if (logger.isInfoEnabled()) {
                logger.info("Connector " + getClass().getName() + " has been stopped");
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.provider.UMOConnector#shutdown()
     */
    public final synchronized void dispose()
    {
        disposing.set(true);
        if (logger.isInfoEnabled()) {
            logger.info("Disposing Connector: " + getClass().getName());
            logger.debug("Disposing Receivers");
        }
        disposeReceivers();
        disposeDispatchers();

        doDispose();
        disposed.set(true);

        if (logger.isInfoEnabled()) {
            logger.info("Connector " + getClass().getName() + " has been disposed.");
        }

        receivers = null;
        dispatchers = null;
    }

    protected void disposeReceivers() {
        if (receivers != null) {
            Map.Entry entry;
            for (Iterator iterator = receivers.entrySet().iterator(); iterator.hasNext();) {
                entry = (Map.Entry) iterator.next();
                try {
                    destroyReceiver(((UMOMessageReceiver) entry.getValue()), null);
                } catch (Exception e) {
                    logger.error("Failed to destroy receiver: " + e.getMessage(), e);
                }
                receivers.remove(entry.getKey());

            }
            logger.debug("Receivers Disposed");
        }
    }

    protected void disposeDispatchers()
    {
        if (dispatchers != null) {
            // Map.Entry entry;
            logger.debug("Disposing Dispatchers");
            for (Iterator iterator = dispatchers.values().iterator(); iterator.hasNext();) {
                UMOMessageDispatcher umoMessageDispatcher = (UMOMessageDispatcher) iterator.next();
                umoMessageDispatcher.dispose();
            }
            dispatchers.clear();
            logger.debug("Dispatchers Disposed");
        }
    }

    /*
    * (non-Javadoc)
    *
    * @see org.mule.umo.provider.UMOConnector#isAlive()
    */
    public boolean isDisposed()
    {
        return disposed.get();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.provider.UMOConnector#handleException(java.lang.Object,
     *      java.lang.Throwable)
     */
    public void handleException(Exception exception)
    {
        if (exceptionListener == null) {
            throw new MuleRuntimeException(new Message(Messages.EXCEPTION_ON_CONNECTOR_X_NO_EXCEPTION_LISTENER,
                                                       getName()), exception);
        } else {
            exceptionListener.exceptionThrown(exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.util.ExceptionListener#onException(java.lang.Throwable)
     */
    public void exceptionThrown(Exception e)
    {
        handleException(e);
    }

    /**
     * @return the ExceptionStrategy for this endpoint
     * @see ExceptionListener
     */
    public ExceptionListener getExceptionListener()
    {
        return exceptionListener;
    }

    /**
     * @param listener the ExceptionStrategy to use with this endpoint
     * @see ExceptionListener
     */
    public void setExceptionListener(ExceptionListener listener)
    {
        exceptionListener = listener;
    }

    /**
     * @return Returns the dispatcherFactory.
     */
    public UMOMessageDispatcherFactory getDispatcherFactory()
    {
        return dispatcherFactory;
    }

    /**
     * @param dispatcerFactory The dispatcherFactory to set.
     */
    public void setDispatcherFactory(UMOMessageDispatcherFactory dispatcerFactory)
    {
        this.dispatcherFactory = dispatcerFactory;
    }

    public synchronized UMOMessageDispatcher getDispatcher(String endpoint) throws UMOException
    {
        checkDisposed();
        UMOMessageDispatcher dispatcher = null;
        if (endpoint == null || "".equals(endpoint)) {
            endpoint = "ANY";
        }
        if ("ANY".equals(endpoint) && dispatchers.size() > 0) {
            Map.Entry entry;
            for (Iterator iterator = dispatchers.entrySet().iterator(); iterator.hasNext();) {
                entry = (Map.Entry) iterator.next();
                if (((UMOMessageDispatcher) entry.getValue()).isDisposed()) {
                    dispatchers.remove(entry.getKey());
                } else {
                    dispatcher = (UMOMessageDispatcher) entry.getValue();
                    break;
                }
            }
        } else {
            if (dispatchers == null) {
                throw new NullPointerException("Dispatchers are null for connector: " + name);
            }
            dispatcher = (UMOMessageDispatcher) dispatchers.get(endpoint);
            if (dispatcher != null && dispatcher.isDisposed()) {
                dispatchers.values().remove(dispatcher);
                dispatcher = null;
            }
        }

        if (dispatcher == null) {
            dispatcher = createDispatcher();
            dispatchers.put(endpoint, dispatcher);
        }
        return dispatcher;
    }

    protected void checkDisposed() throws DisposeException
    {
        if (isDisposed()) {
            throw new DisposeException(new Message(Messages.CANT_START_DISPOSED_CONNECTOR), this);
        }
    }

    protected UMOMessageDispatcher createDispatcher() throws UMOException
    {
        if (dispatcherFactory == null) {
            throw new ConnectorException(new Message(Messages.CONNECTOR_NOT_STARTED, name), this);
        }
        UMOMessageDispatcher dispatcher = dispatcherFactory.create(this);
        return dispatcher;
    }

    public UMOMessageReceiver registerListener(UMOComponent component, UMOEndpoint endpoint) throws Exception
    {
        if (endpoint == null || component == null) {
            throw new IllegalArgumentException("The endpoint and component cannot be null when registering a listener");
        }

        UMOEndpointURI endpointUri = endpoint.getEndpointURI();
        if (endpointUri == null) {
            throw new ConnectorException(new Message(Messages.ENDPOINT_NULL_FOR_LISTENER), this);
        }
        logger.info("registering listener: " + component.getDescriptor().getName() + " on endpointUri: "
                + endpointUri.toString());

        UMOMessageReceiver receiver = getReceiver(component, endpoint);
        if (receiver != null) {
            throw new ConnectorException(new Message(Messages.LISTENER_ALREADY_REGISTERED, endpointUri), this);
        } else {
            receiver = createReceiver(component, endpoint);
            receivers.put(getReceiverKey(component, endpoint), receiver);
        }
        if (started.get() && endpoint.getInitialState().equals(UMOEndpoint.INITIAL_STATE_STARTED)) {
            ((AbstractMessageReceiver) receiver).start();
        }
        return receiver;
    }

    /**
     * The method determines the key used to store the receiver against.
     * 
     * @param component the component for which the endpoint is being registered
     * @param endpoint the endpoint being registered for the component
     * @return the key to store the newly created receiver against
     */
    protected Object getReceiverKey(UMOComponent component, UMOEndpoint endpoint)
    {
        if (endpoint.getEndpointURI().getFilterAddress() != null) {
            return endpoint.getEndpointURI().getFilterAddress();
        } else {
            return endpoint.getEndpointURI().getAddress();
        }
    }

    public final void unregisterListener(UMOComponent component, UMOEndpoint endpoint) throws Exception
    {
        if (endpoint == null || component == null || endpoint.getEndpointURI() == null) {
            throw new IllegalArgumentException("The endpoint and component and endpointUri cannot be null when you unregister a listener");
        }

        UMOEndpointURI endpointUri = endpoint.getEndpointURI();
        if (logger.isInfoEnabled()) {
            logger.info("removing listener on endpointUri: " + endpointUri);
        }

        UMOMessageReceiver receiver = (UMOMessageReceiver) receivers.remove(getReceiverKey(component, endpoint));
        if (receiver != null) {
            destroyReceiver(receiver, endpoint);
            receiver.dispose();
        }
    }
    
    public void startDispatchers(UMOComponent component, UMOEndpoint endpoint) throws UMOException { 
    	
    }
    
    public void stopDispatchers(UMOComponent component, UMOEndpoint endpoint) throws UMOException { 
    	
    }

    public ThreadingProfile getDispatcherThreadingProfile()
    {
        if (dispatcherThreadingProfile == null) {
            dispatcherThreadingProfile = MuleManager.getConfiguration().getMessageDispatcherThreadingProfile();

        }
        return dispatcherThreadingProfile;
    }

    public void setDispatcherThreadingProfile(ThreadingProfile dispatcherThreadingProfile)
    {
        this.dispatcherThreadingProfile = dispatcherThreadingProfile;
    }

    public ThreadingProfile getReceiverThreadingProfile()
    {
        if (receiverThreadingProfile == null) {
            receiverThreadingProfile = MuleManager.getConfiguration().getMessageReceiverThreadingProfile();
        }
        return receiverThreadingProfile;
    }

    public void setReceiverThreadingProfile(ThreadingProfile receiverThreadingProfile)
    {
        this.receiverThreadingProfile = receiverThreadingProfile;
    }

    public abstract UMOMessageReceiver createReceiver(UMOComponent component, UMOEndpoint endpoint) throws Exception;

    public void destroyReceiver(UMOMessageReceiver receiver, UMOEndpoint endpoint) throws Exception
    {
        receiver.dispose();
    }

    /**
     * Template method to perform any work when starting the connectoe
     * 
     * @throws UMOException if the method fails
     */
    protected void doStart() throws UMOException
    {
    }

    /**
     * Template method to perform any work when stopping the connectoe
     * 
     * @throws UMOException if the method fails
     */
    protected void doStop() throws UMOException
    {
    }

    /**
     * Template method to perform any work when destroying the connectoe
     * 
     */
    protected void doDispose()
    {
        try {
            stopConnector();
        } catch (UMOException e) {
            logger.warn("Failed to stop during shutdown: " + e.getMessage(), e);
        }
    }

    public void doInitialise() throws InitialisationException
    {
    }

    public UMOTransformer getDefaultInboundTransformer()
    {
        if (defaultInboundTransformer != null) {
            try {
                return (UMOTransformer) defaultInboundTransformer.clone();
            } catch (CloneNotSupportedException e) {
                logger.error("Failed to clone default Inbound transformer");
                return null;
            }
        } else {
            return null;
        }
    }

    public void setDefaultInboundTransformer(UMOTransformer defaultInboundTransformer)
    {
        this.defaultInboundTransformer = defaultInboundTransformer;
    }

    public UMOTransformer getDefaultResponseTransformer()
    {
        if (defaultResponseTransformer != null) {
            try {
                return (UMOTransformer) defaultResponseTransformer.clone();
            } catch (CloneNotSupportedException e) {
                logger.error("Failed to clone default Outbound transformer");
                return null;
            }
        } else {
            return null;
        }
    }

    public UMOTransformer getDefaultOutboundTransformer()
    {
        if (defaultOutboundTransformer != null) {
            try {
                return (UMOTransformer) defaultOutboundTransformer.clone();
            } catch (CloneNotSupportedException e) {
                logger.error("Failed to clone default Outbound transformer");
                return null;
            }
        } else {
            return null;
        }
    }

    public void setDefaultOutboundTransformer(UMOTransformer defaultOutboundTransformer)
    {
        this.defaultOutboundTransformer = defaultOutboundTransformer;
    }

    public void setDefaultResponseTransformer(UMOTransformer defaultResponseTransformer)
    {
        this.defaultResponseTransformer = defaultResponseTransformer;
    }

    public ReplyToHandler getReplyToHandler()
    {
        return new DefaultReplyToHandler(defaultResponseTransformer);
    }

    public Map getDispatchers()
    {
        return dispatchers;
    }

    /**
     * Fires a server event to all registered
     * {@link org.mule.impl.internal.events.CustomEventListener} eventManager.
     * 
     * @param event the event to fire. This must be of type
     *            {@link org.mule.impl.internal.events.CustomEvent} otherwise an
     *            exception will be thrown.
     * @throws UnsupportedOperationException if the event fired is not a
     *             {@link org.mule.impl.internal.events.CustomEvent}
     */
    public void fireEvent(UMOServerEvent event)
    {
        MuleManager.getInstance().fireEvent(event);
        // if(event instanceof CustomEvent) {
        // if(eventManager!=null) {
        // eventManager.fireEvent(event);
        // } else if(logger.isDebugEnabled()) {
        // logger.debug("Event Manager is not enabled, ignoring event: " +
        // event);
        // }
        // } else {
        // throw new UnsupportedOperationException(new
        // Message(Messages.ONLY_CUSTOM_EVENTS_CAN_BE_FIRED).getMessage());
        // }
    }

    public ConnectionStrategy getConnectionStrategy()
    {
        // not happy with this but each receiver needs its own instance
        // of the connection strategy and using a factory just introduces extra
        // implementation
        try {
            return (ConnectionStrategy) BeanUtils.cloneBean(connectionStrategy);
        } catch (Exception e) {
            throw new MuleRuntimeException(new Message(Messages.FAILED_TO_CLONE_X, "connectionStrategy"), e);
        }
    }

    public void setConnectionStrategy(ConnectionStrategy connectionStrategy)
    {
        this.connectionStrategy = connectionStrategy;
    }

    public List getEndpointMBeans()
    {
        // for now only return receiver endpoints as those are the ones we can
        // control
        // in terms of connecting/disconnecting
        List beans = new ArrayList(receivers.size());
        for (Iterator iterator = receivers.values().iterator(); iterator.hasNext();) {
            UMOMessageReceiver receiver = (UMOMessageReceiver) iterator.next();
            beans.add(new EndpointService(receiver));
        }
        return beans;
    }

    public boolean isDisposing() {
        return disposing.get();
    }

    public boolean isRemoteSyncEnabled() {
        return false;
    }

    public AbstractMessageReceiver getReceiver(UMOComponent component, UMOEndpoint endpoint) {
        return (AbstractMessageReceiver) receivers.get(getReceiverKey(component, endpoint));
    }

    public Map getReceivers() {
        return Collections.unmodifiableMap(receivers);
    }

    public AbstractMessageReceiver getReceiver(String key) {
        return (AbstractMessageReceiver) receivers.get(key);
    }

    public AbstractMessageReceiver[] getReceivers(String wildcardExpression) {

        List temp = new ArrayList();
        WildcardFilter filter = new WildcardFilter(wildcardExpression);
        for (Iterator iterator = receivers.keySet().iterator(); iterator.hasNext();) {
            Object o =  iterator.next();
            if(filter.accept(o)) {
                temp.add(receivers.get(o));
            }
        }
        AbstractMessageReceiver[] result = new AbstractMessageReceiver[temp.size()];
        return (AbstractMessageReceiver[])temp.toArray(result);
    }

    public void connect() throws Exception {
        if (connected.get()) {
            return;
        }

        if (connecting.commit(false, true)) {
            connectionStrategy.connect(this);
            logger.info("Connected: " + getConnectionDescription());
            return;
        }

        try {
            doConnect();
            fireEvent(new ConnectionEvent(this, getConnectEventId(), ConnectionEvent.CONNECTION_CONNECTED));
        } catch (Exception e) {
            fireEvent(new ConnectionEvent(this, getConnectEventId(), ConnectionEvent.CONNECTION_FAILED));
            if (e instanceof ConnectException) {
                throw (ConnectException) e;
            } else {
                throw new ConnectException(e, this);
            }
        }
        connected.set(true);
        connecting.set(false);
        if(startedBeforeDisconnect.get()) {
            startConnector();
        }
    }

    public void disconnect() throws Exception {
        startedBeforeDisconnect.set(isStarted());
        fireEvent(new ConnectionEvent(this, getConnectEventId(), ConnectionEvent.CONNECTION_DISCONNECTED));
        connected.set(false);
        doDisconnect();
        stopConnector();
        logger.info("Disconnected: " + getConnectionDescription());
    }

    public String getConnectionDescription() {
        return toString();
    }

    public boolean isConnected() {
       return connected.get();
    }

    /**
     * Template method where any connections should be made for the connector
     * @throws Exception
     */
    public void doConnect() throws Exception {

    }

    /**
     * Template method where any connected resources used by the connector should
     * be disconnected
     * @throws Exception
     */
    public void doDisconnect() throws Exception {

    }

    /**
     * The resource id used when firing ConnectEvents from this connector
     * @return the  resource id used when firing ConnectEvents from this connector
     */
    protected String getConnectEventId()
    {
        return getName();
    }

    /**
     * controls whether dispatchers or cached or created per request
     * Note that if an exception occurs in the Dispatcher it is automatically disposed
     * of and a new one is created for the next request.  This allows dispatchers to recover
     * from loss of connection and other faults.
     * @param createDispatcherPerRequest whether a new dispatcher is
     * created for every request or not
     */
    public void setCreateDispatcherPerRequest(boolean createDispatcherPerRequest) {
        this.createDispatcherPerRequest = createDispatcherPerRequest;
    }

    /**
     * controls whether dispatchers or cached or created per request
     * Note that if an exception occurs in the Dispatcher it is automatically disposed
     * of and a new one is created for the next request.  This allows dispatchers to recover
     * from loss of connection and other faults.
     * @return true if a anew dispatcher is created for every request
     */
    public boolean isCreateDispatcherPerRequest() {
        return createDispatcherPerRequest;
    }

    /**
     * For better throughput when using TransactedMessageReceivers. This will create an number
     * of receiver threads based on the ThreadingProfile configured fro the receiver. This property is
     * user by transports that support transactions, specifically MessageReceivers that extend the
     * TransactedPollingMessageReceiver.
     * @return true if multiple receiver threads will be created for receivers on this connection
     */
    public boolean isCreateMultipleTransactedReceivers() {
        return createMultipleTransactedReceivers;
    }

    /**
     * For better throughput when using TransactedMessageReceivers. This will create an number
     * of receiver threads based on the ThreadingProfile configured fro the receiver. This property is
     * user by transports that support transactions, specifically MessageReceivers that extend the
     * TransactedPollingMessageReceiver.
     * @param createMultipleTransactedReceivers true if multiple receiver threads will be created for
     * receivers on this connection
     */
    public void setCreateMultipleTransactedReceivers(boolean createMultipleTransactedReceivers) {
        this.createMultipleTransactedReceivers = createMultipleTransactedReceivers;
    }

    /**
     * Whether to fire message events for every message that is sent or received from this connector
     * @return
     */
    public boolean isEnableMessageEvents() {
        return enableMessageEvents;
    }

    /**
     * Whether to fire message events for every message that is sent or received from this connector
     * @param enableMessageEvents
     */
    public void setEnableMessageEvents(boolean enableMessageEvents) {
        this.enableMessageEvents = enableMessageEvents;
    }

    /**
     * Registers other protocols 'understood' by this connector.
     * These must contain scheme meta info. Any protocol registered
     * must begin with the protocol of this connector, i.e. If the connector is axis
     * the protocol for jms over axis will be axis:jms.  Here, 'axis' is the
     * scheme meta info and 'jms' is the protocol.
     * If the protocol argument does not start with the connector's protocol, it
     * will be appended.
     * @param protocol the supported protocol to register
     */
    public void registerSupportedProtocol(String protocol) {
        protocol = protocol.toLowerCase();
        if(protocol.startsWith(getProtocol().toLowerCase())) {
            supportedProtocols.add(protocol);
        } else {
            supportedProtocols.add(getProtocol().toLowerCase() + ":" + protocol);
        }
    }

    /**
     * @return true if the protocol is supported by this connector.
     */
    public boolean supportsProtocol(String protocol) {
    	return supportedProtocols.contains(protocol.toLowerCase());
    }
}
