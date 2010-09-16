/*
 * $Header: /home/projects/mule/scm/mule/mule/src/java/org/mule/MuleManager.java,v 1.51 2005/11/08 17:14:28 aperepel Exp $
 * $Revision: 1.51 $
 * $Date: 2005/11/08 17:14:28 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.config.ConfigurationException;
import org.mule.config.MuleConfiguration;
import org.mule.config.MuleProperties;
import org.mule.config.ThreadingProfile;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.container.MultiContainerContext;
import org.mule.impl.internal.admin.MuleAdminAgent;
import org.mule.impl.internal.events.AdminEvent;
import org.mule.impl.internal.events.AdminEventListener;
import org.mule.impl.internal.events.ComponentEvent;
import org.mule.impl.internal.events.ComponentEventListener;
import org.mule.impl.internal.events.ConnectionEvent;
import org.mule.impl.internal.events.ConnectionEventListener;
import org.mule.impl.internal.events.CustomEvent;
import org.mule.impl.internal.events.CustomEventListener;
import org.mule.impl.internal.events.ManagementEvent;
import org.mule.impl.internal.events.ManagementEventListener;
import org.mule.impl.internal.events.ManagerEvent;
import org.mule.impl.internal.events.ManagerEventListener;
import org.mule.impl.internal.events.MessageEvent;
import org.mule.impl.internal.events.MessageEventListener;
import org.mule.impl.internal.events.ModelEvent;
import org.mule.impl.internal.events.ModelEventListener;
import org.mule.impl.internal.events.SecurityEvent;
import org.mule.impl.internal.events.SecurityEventListener;
import org.mule.impl.internal.events.ServerEventManager;
import org.mule.impl.model.seda.SedaModel;
import org.mule.impl.security.MuleSecurityManager;
import org.mule.impl.work.MuleWorkManager;
import org.mule.management.stats.AllStatistics;
import org.mule.umo.UMOException;
import org.mule.umo.UMOInterceptorStack;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.FatalException;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.manager.UMOAgent;
import org.mule.umo.manager.UMOContainerContext;
import org.mule.umo.manager.UMOManager;
import org.mule.umo.manager.UMOServerEvent;
import org.mule.umo.manager.UMOServerEventListener;
import org.mule.umo.manager.UMOWorkManager;
import org.mule.umo.model.UMOModel;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.security.UMOSecurityManager;
import org.mule.umo.transformer.UMOTransformer;
import org.mule.util.PropertiesHelper;
import org.mule.util.SpiHelper;
import org.mule.util.StringMessageHelper;
import org.mule.util.Utility;
import org.mule.util.queue.CachingPersistenceStrategy;
import org.mule.util.queue.QueueManager;
import org.mule.util.queue.QueuePersistenceStrategy;
import org.mule.util.queue.TransactionalQueueManager;

import com.mirth.connect.server.Mirth;

import javax.transaction.TransactionManager;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * <code>MuleManager</code> maintains and provides services for a Mule
 * instance.
 *
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.51 $
 */
public class MuleManager implements UMOManager
{
    /**
     * singleton instance
     */
    private static UMOManager instance = null;

    /**
     * Default configuration
     */
    private static MuleConfiguration config = new MuleConfiguration();

    /**
     * Connectors registry
     */
    private Map connectors = new HashMap();

    /**
     * Endpoints registry
     */
    private Map endpointIdentifiers = new HashMap();

    /**
     * Holds any application scoped environment properties set in the config
     */
    private Map applicationProps = new HashMap();

    /**
     * Holds any registered agents
     */
    private Map agents = new LinkedHashMap();

    /**
     * Holds a list of global endpoints accessible to any client code
     */
    private Map endpoints = new HashMap();

    /**
     * The model being used
     */
    private UMOModel model;

    /**
     * the unique id for this manager
     */
    private String id = null;

    /**
     * The transaction Manager to use for global transactions
     */
    private TransactionManager transactionManager = null;

    /**
     * Collection for transformers registered in this component
     */
    private HashMap transformers = new HashMap();

    /**
     * True once the Mule Manager is initialised
     */
    private AtomicBoolean initialised = new AtomicBoolean(false);
    private AtomicBoolean initialising = new AtomicBoolean(false);

    /**
     * Determines of the MuleManager has been started
     */
    private AtomicBoolean started = new AtomicBoolean(false);

    /**
     * Determines in the manager is in the process of starting
     */
    private AtomicBoolean starting = new AtomicBoolean(false);

    /**
     * Determines in the manager is in the process of stopping.
     */
    private AtomicBoolean stopping = new AtomicBoolean(false);

    /**
     * Determines if the manager has been disposed
     */
    private AtomicBoolean disposed = new AtomicBoolean(false);

    /**
     * Holds a reference to the deamon running the Manager if any
     */
    private static MuleServer server = null;

    /**
     * Maintains a reference to any interceptor stacks configured on the manager
     */
    private HashMap interceptorsMap = new HashMap();

    /**
     * the date in milliseconds from when the server was started
     */
    private long startDate = 0;

    /**
     * stats used for management
     */
    private AllStatistics stats = new AllStatistics();

    /**
     * Manages all Server event eventManager
     */
    private ServerEventManager eventManager = null;

    private MultiContainerContext containerContext = null;

    private UMOSecurityManager securityManager;

    /**
     * The queue manager to use for component queues and vm connector
     */
    private QueueManager queueManager;

    private UMOWorkManager workManager;

    /**
     * logger used by this class
     */
    private static transient Log logger = LogFactory.getLog(MuleManager.class);

    private ShutdownContext shutdownContext = new ShutdownContext(true, null);

    /**
     * Default Constructor
     */
    private MuleManager()
    {
        if (config == null) {
            config = new MuleConfiguration();
        }
        containerContext = new MultiContainerContext();
        securityManager = new MuleSecurityManager();
        // Runtime.getRuntime().addShutdownHook(new ShutdownThread());
        Mirth.addShutdownHook(new ShutdownThread());
    }

    /**
     * ObjectFactory method to create the singleton MuleManager instance
     */
    protected static synchronized UMOManager createInstance()
    {
        Class clazz = SpiHelper.findService(UMOManager.class, MuleManager.class.getName(), MuleManager.class);
        Object obj;
        try {
            obj = clazz.newInstance();
        } catch (Exception e) {
            throw new MuleRuntimeException(new Message(Messages.FAILED_TO_CREATE_MANAGER_INSTANCE_X, clazz.getName()),
                                           e);
        }

        MuleManager.setInstance((UMOManager) obj);

        // HACK hit the model, so it's created and initialized
        MuleManager.getInstance().getModel();

        return MuleManager.getInstance();
    }

    /**
     * Getter method for the current singleton MuleManager
     *
     * @return the current singleton MuleManager
     */
    public static UMOManager getInstance()
    {
        if (instance == null) {
            instance = createInstance();
        }
        return instance;
    }

    /**
     * A static method to determine if there is an instance of the MuleManager.
     * This should be used instead of <code>
     * if(MuleManager.getInstance()!=null)
     * </code>
     * because getInstance never returns a null. If an istance is not available
     * one is created. This method queries the instance directly.
     *
     * @return true if the manager is instanciated
     */
    public static boolean isInstanciated()
    {
        return (instance != null);
    }

    /**
     * Sets the current singleton MuleManager
     */
    public static synchronized void setInstance(UMOManager manager)
    {
        instance = manager;
        if (instance == null) {
            config = new MuleConfiguration();
        }
    }

    /**
     * Gets all statisitcs for this instance
     *
     * @return all statisitcs for this instance
     */
    public AllStatistics getStatistics()
    {
        return stats;
    }

    /**
     * Sets statistics on this instance
     *
     * @param stat
     */
    public void setStatistics(AllStatistics stat)
    {
        this.stats = stat;
    }

    /**
     * @return the MuleConfiguration for this MuleManager. This object is
     *         immutable once the manager has initialised.
     */
    public static MuleConfiguration getConfiguration()
    {
        return config;

    }

    /**
     * Sets the configuration for the <code>MuleManager</code>.
     *
     * @param config the configuration object
     * @throws IllegalAccessError if the <code>MuleManager</code> has already
     *             been initialised.
     */
    public static void setConfiguration(MuleConfiguration config)
    {
        if (config == null) {
            throw new IllegalArgumentException(new Message(Messages.X_IS_NULL, "MuleConfiguration object").getMessage());
        }
        MuleManager.config = config;
    }

    // Implementation methods
    // -------------------------------------------------------------------------

    /**
     * Destroys the MuleManager and all resources it maintains
     */
    public synchronized void dispose()
    {
        if (disposed.get()) {
            return;
        }
        try {
            if (started.get()) {
                stop();
            }
        } catch (UMOException e) {
            logger.error("Failed to stop manager: " + e.getMessage(), e);
        }
        disposed.set(true);
        disposeConnectors();

        if (model != null) {
            model.dispose();
        }
        disposeAgents();

        transformers.clear();
        endpoints.clear();
        endpointIdentifiers.clear();
        containerContext.dispose();
        containerContext = null;
        // props.clear();
        fireSystemEvent(new ManagerEvent(this, ManagerEvent.MANAGER_DISPOSED));

        transformers = null;
        endpoints = null;
        endpointIdentifiers = null;
        // props = null;
        initialised.set(false);
        if (eventManager != null) {
            eventManager.dispose();
        }
        if (workManager != null) {
            workManager.dispose();
        }

        if(!config.isEmbedded()) {
            if (logger.isInfoEnabled()) {
                logger.info(getEndSplash());
            } else {
                System.out.println(getEndSplash());
            }
        }

        config = new MuleConfiguration();
        instance = null;
    }

    /**
     * Destroys all connectors
     */
    private synchronized void disposeConnectors()
    {
        fireSystemEvent(new ManagerEvent(this, ManagerEvent.MANAGER_DISPOSING_CONNECTORS));
        for (Iterator iterator = connectors.values().iterator(); iterator.hasNext();) {
            UMOConnector c = (UMOConnector) iterator.next();
            c.dispose();
        }
        fireSystemEvent(new ManagerEvent(this, ManagerEvent.MANAGER_DISPOSED_CONNECTORS));
    }

    /**
     * {@inheritDoc}
     */
    public Object getProperty(Object key)
    {
        return applicationProps.get(key);
    }

    /**
     * {@inheritDoc}
     */
    public Map getProperties()
    {
        return applicationProps;
    }

    /**
     * {@inheritDoc}
     */
    public TransactionManager getTransactionManager()
    {
        return transactionManager;
    }

    /**
     * {@inheritDoc}
     */
    public UMOConnector lookupConnector(String name)
    {
        return (UMOConnector) connectors.get(name);
    }

    /**
     * {@inheritDoc}
     */
    public String lookupEndpointIdentifier(String logicalName, String defaultName)
    {
        String name = (String) endpointIdentifiers.get(logicalName);
        if (name == null) {
            return defaultName;
        }
        return name;
    }

    /**
     * {@inheritDoc}
     */
    public UMOEndpoint lookupEndpoint(String logicalName)
    {
        UMOEndpoint endpoint = (UMOEndpoint) endpoints.get(logicalName);
        if (endpoint != null) {
            return (UMOEndpoint) endpoint.clone();
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public UMOTransformer lookupTransformer(String name)
    {
        UMOTransformer trans = (UMOTransformer) transformers.get(name);
        if (trans != null) {
            try {
                return (UMOTransformer) trans.clone();
            } catch (Exception e) {
                throw new MuleRuntimeException(new Message(Messages.FAILED_TO_CLONE_X, "Transformer: "
                        + trans.getName()), e);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void registerConnector(UMOConnector connector) throws UMOException
    {
        connectors.put(connector.getName(), connector);
        if (initialised.get() || initialising.get()) {
            connector.initialise();
        }
        if ((started.get() || starting.get()) && !connector.isStarted()) {
            connector.startConnector();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void unregisterConnector(String connectorName) throws UMOException
    {
        UMOConnector c = (UMOConnector) connectors.remove(connectorName);
        if (c != null) {
            c.dispose();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void registerEndpointIdentifier(String logicalName, String endpoint)
    {
        // Set the name of the endpoint is it is a Url. This helps when viewing
        // Jmx stats
        // if(MuleEndpointURI.isMuleUri(endpoint)) {
        // if(endpoint.indexOf(UMOEndpointURI.PROPERTY_ENDPOINT_NAME) == -1) {
        // String endpointName = logicalName + "(" + endpoint.substring(0,
        // endpoint.indexOf(":")) + ")";
        // if(endpoint.indexOf('?') == -1) {
        // endpoint += "?" + UMOEndpointURI.PROPERTY_ENDPOINT_NAME + "=" +
        // endpointName;
        // } else {
        // endpoint += "&" + UMOEndpointURI.PROPERTY_ENDPOINT_NAME + "=" +
        // endpointName;
        // }
        // }
        // }
        endpointIdentifiers.put(logicalName, endpoint);
    }

    /**
     * {@inheritDoc}
     */
    public void unregisterEndpointIdentifier(String logicalName)
    {
        endpointIdentifiers.remove(logicalName);
    }

    /**
     * {@inheritDoc}
     */
    public void registerEndpoint(UMOEndpoint endpoint)
    {
        endpoints.put(endpoint.getName(), endpoint);
    }

    /**
     * {@inheritDoc}
     */
    public void unregisterEndpoint(String endpointName)
    {
        UMOEndpoint p = (UMOEndpoint) endpoints.get(endpointName);
        if (p != null) {
            endpoints.remove(p);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void registerTransformer(UMOTransformer transformer) throws InitialisationException
    {
        transformer.initialise();
        transformers.put(transformer.getName(), transformer);
        logger.info("Transformer " + transformer.getName() + " has been initialised successfully");
    }

    /**
     * {@inheritDoc}
     */
    public void unregisterTransformer(String transformerName)
    {
        transformers.remove(transformerName);
    }

    /**
     * {@inheritDoc}
     */
    public void setProperty(Object key, Object value)
    {
        applicationProps.put(key, value);
    }

    public void addProperties(Map props) {
        applicationProps.putAll(props);
    }

    /**
     * {@inheritDoc}
     */
    public void setTransactionManager(TransactionManager newManager) throws UMOException
    {
        if (transactionManager != null) {
            throw new ConfigurationException(new Message(Messages.TX_MANAGER_ALREADY_SET));
        }
        transactionManager = newManager;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void initialise() throws UMOException
    {
        validateEncoding();

        if (!initialised.get()) {
            initialising.set(true);
            startDate = System.currentTimeMillis();
            // if no work manager has been set create a default one
            if (workManager == null) {
                ThreadingProfile tp = config.getDefaultThreadingProfile();
                logger.debug("Creating default work manager using default threading profile: " + tp);
                workManager = new MuleWorkManager(tp, "UMOManager");
                workManager.start();
            }

            // create the event manager
            eventManager = new ServerEventManager(workManager);
            eventManager.registerEventType(ManagerEvent.class, ManagerEventListener.class);
            eventManager.registerEventType(ModelEvent.class, ModelEventListener.class);
            eventManager.registerEventType(ComponentEvent.class, ComponentEventListener.class);
            eventManager.registerEventType(SecurityEvent.class, SecurityEventListener.class);
            eventManager.registerEventType(ManagementEvent.class, ManagementEventListener.class);
            eventManager.registerEventType(AdminEvent.class, AdminEventListener.class);
            eventManager.registerEventType(CustomEvent.class, CustomEventListener.class);
            eventManager.registerEventType(ConnectionEvent.class, ConnectionEventListener.class);
            if(config.isEnableMessageEvents()) {
                eventManager.registerEventType(MessageEvent.class, MessageEventListener.class);
            }

            fireSystemEvent(new ManagerEvent(this, ManagerEvent.MANAGER_INITIALISNG));
            if (id == null) {
                logger.warn("No unique id has been set on this manager");
            }
            try {
                if (securityManager != null) {
                    securityManager.initialise();
                }
                if (queueManager == null) {
                    try {
                        TransactionalQueueManager queueMgr = new TransactionalQueueManager();
                        QueuePersistenceStrategy ps = new CachingPersistenceStrategy(getConfiguration().getPersistenceStrategy());
                        queueMgr.setPersistenceStrategy(ps);
                        queueManager = queueMgr;
                    } catch (Exception e) {
                        throw new InitialisationException(new Message(Messages.INITIALISATION_FAILURE_X, "QueueManager"),
                                                          e);
                    }
                }

                initialiseConnectors();
                initialiseEndpoints();
                initialiseAgents();
                if(model!=null) model.initialise();

            } finally {
                initialised.set(true);
                initialising.set(false);
                fireSystemEvent(new ManagerEvent(this, ManagerEvent.MANAGER_INITIALISED));
            }
        }
    }

    protected void validateEncoding() throws FatalException
    {
        String encoding = System.getProperty(MuleProperties.MULE_ENCODING_SYSTEM_PROPERTY);
        if(encoding==null) {
            encoding = config.getEncoding();
            System.setProperty(MuleProperties.MULE_ENCODING_SYSTEM_PROPERTY, encoding);
        } else {
            config.setEncoding(encoding);
        }
        //Check we have a valid and supported encoding
        if(!Charset.isSupported(config.getEncoding())) {
            throw new FatalException(new Message(Messages.PROPERTY_X_HAS_INVALID_VALUE_X, "encoding", config.getEncoding()), this);
        }
    }

    protected void registerAdminAgent() throws UMOException {
        // Allows users to disable all server components and connections
        // this can be useful for testing
        boolean disable = PropertiesHelper.getBooleanProperty(System.getProperties(),
                                                              MuleProperties.DISABLE_SERVER_CONNECTIONS,
                                                              false);

        // if endpointUri is null do not setup server components
        if (config.getServerUrl() == null || "".equals(config.getServerUrl().trim())) {
            logger.info("Server endpointUri is null, not registering Mule Admin agent");
            disable = true;
        }

        if (!disable) {
            unregisterAgent(MuleAdminAgent.AGENT_NAME);
            registerAgent(new MuleAdminAgent());

        }
    }

    protected void initialiseEndpoints() throws InitialisationException
    {
        UMOEndpoint ep;
        for (Iterator iterator = this.endpoints.values().iterator(); iterator.hasNext();) {
            ep = (UMOEndpoint) iterator.next();
            ep.initialise();
            // the connector has been created for this endpoint so lets
            // set the create connector to 0 so that every time this endpoint
            // is referenced we don't create another connector
            ep.setCreateConnector(0);
        }
    }

    /**
     * Start the <code>MuleManager</code>. This will start the connectors and
     * sessions.
     *
     * @throws UMOException if the the connectors or components fail to start
     */
    public synchronized void start() throws UMOException
    {
        initialise();

        if (!started.get()) {
            starting.set(true);
            fireSystemEvent(new ManagerEvent(this, ManagerEvent.MANAGER_STARTING));
            registerAdminAgent();
            if (queueManager != null) { queueManager.start(); }
            startConnectors();
            startAgents();
            if(model!=null) model.start();
            started.set(true);
            starting.set(false);
            if(!config.isEmbedded()) {
                if (logger.isInfoEnabled()) {
                    logger.info(getStartSplash());
                } else {
                    System.out.println(getStartSplash());
                }
            }
            fireSystemEvent(new ManagerEvent(this, ManagerEvent.MANAGER_STARTED));
        }
    }

    /**
     * Start the <code>MuleManager</code>. This will start the connectors and
     * sessions.
     *
     * @param serverUrl the server Url for this instance
     * @throws UMOException if the the connectors or components fail to start
     */
    public void start(String serverUrl) throws UMOException
    {
        // this.createClientListener = createRequestListener;
        config.setServerUrl(serverUrl);
        start();
    }

    /**
     * Starts the connectors
     *
     * @throws MuleException if the connectors fail to start
     */
    private void startConnectors() throws UMOException
    {
        for (Iterator iterator = connectors.values().iterator(); iterator.hasNext();) {
            UMOConnector c = (UMOConnector) iterator.next();
            c.startConnector();
        }
        logger.info("Connectors have been started successfully");
    }

    private void initialiseConnectors() throws InitialisationException
    {
        for (Iterator iterator = connectors.values().iterator(); iterator.hasNext();) {
            UMOConnector c = (UMOConnector) iterator.next();
            c.initialise();
        }
        logger.info("Connectors have been initialised successfully");
    }

    /**
     * Stops the <code>MuleManager</code> which stops all sessions and
     * connectors
     *
     * @throws UMOException if either any of the sessions or connectors fail to
     *             stop
     */
    public synchronized void stop() throws UMOException
    {
        started.set(false);
        stopping.set(true);
        fireSystemEvent(new ManagerEvent(this, ManagerEvent.MANAGER_STOPPING));

        stopConnectors();
        stopAgents();

        if (queueManager != null)
        {
            queueManager.stop();
        }

        logger.debug("Stopping model...");
        if (model != null)
        {
            model.stop();
        }

        stopping.set(false);
        fireSystemEvent(new ManagerEvent(this, ManagerEvent.MANAGER_STOPPED));
    }

    /**
     * Stops the connectors
     *
     * @throws MuleException if any of the connectors fail to stop
     */
    private void stopConnectors() throws UMOException
    {
        logger.debug("Stopping connectors...");
        for (Iterator iterator = connectors.values().iterator(); iterator.hasNext();) {
            UMOConnector c = (UMOConnector) iterator.next();
            c.stopConnector();
        }
        logger.info("Connectors have been stopped successfully");
    }

    /**
     * If the <code>MuleManager</code> was started from the
     * <code>MuleServer</code> daemon then this will be called by the Server
     *
     * @param server a reference to the <code>MuleServer</code>.
     */
    void setServer(MuleServer server)
    {
        MuleManager.server = server;
    }

    /**
     * Shuts down the whole server tring to shut down all resources cleanly on
     * the way
     *
     * @param e an exception that caused the <code>shutdown()</code> method to
     *            be called. If e is null the shutdown message will just display
     *            a time when the server was shutdown. Otherwise the exception
     *            information will also be displayed.
     */
    public void shutdown(Throwable e, boolean aggressive)
    {
        shutdownContext = new ShutdownContext(aggressive, e);
        System.exit(0);
    }

    /**
     * {@inheritDoc}
     */
    public UMOModel getModel()
    {
        //todo in version two we must not assume the model
        if(model==null) {
            model = new SedaModel();
        }
        return model;
    }

    /**
     * {@inheritDoc}
     */
    public void setModel(UMOModel model) throws UMOException {
        this.model = model;
        if(initialised.get()) {
            model.initialise();
        }

         if(started.get()) {
            model.start();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void registerInterceptorStack(String name, UMOInterceptorStack stack)
    {
        interceptorsMap.put(name, stack);
    }

    /**
     * {@inheritDoc}
     */
    public UMOInterceptorStack lookupInterceptorStack(String name)
    {
        return (UMOInterceptorStack) interceptorsMap.get(name);
    }

    /**
     * {@inheritDoc}
     */
    public Map getConnectors()
    {
        return Collections.unmodifiableMap(connectors);
    }

    /**
     * {@inheritDoc}
     */
    public Map getEndpointIdentifiers()
    {
        return Collections.unmodifiableMap(endpointIdentifiers);
    }

    /**
     * {@inheritDoc}
     */
    public Map getEndpoints()
    {
        return Collections.unmodifiableMap(endpoints);
    }

    /**
     * {@inheritDoc}
     */
    public Map getTransformers()
    {
        return Collections.unmodifiableMap(transformers);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isStarted()
    {
        return started.get();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isInitialised()
    {
        return initialised.get();
    }

    /**
     * Determines if the server is currently initialising
     * 
     * @return true if if the server is currently initialising, false otherwise
     */
    public boolean isInitialising()
    {
        return initialising.get();
    }

    /**
     * Determines in the manager is in the process of stopping.
     */
    public boolean isStopping()
    {
        return stopping.get();
    }

    /**
     * {@inheritDoc}
     */
    public long getStartDate()
    {
        return startDate;
    }

    /**
     * Returns a formatted string that is a summary of the configuration of the
     * server. This is the brock of information that gets displayed when the
     * server starts
     * 
     * @return a string summary of the server information
     */
    protected String getStartSplash()
    {
        String notset = new Message(Messages.NOT_SET).getMessage();

        List message = new ArrayList();
        Manifest mf = config.getManifest();
        Map att = mf.getMainAttributes();
        if (att.values().size() > 0) {
            message.add(PropertiesHelper.getStringProperty(att, new Attributes.Name("Specification-Title"), notset)
                    + " " + new Message(Messages.VERSION).getMessage() + " "
                    + PropertiesHelper.getStringProperty(att, new Attributes.Name("Implementation-Version"), notset));

            message.add(PropertiesHelper.getStringProperty(att, new Attributes.Name("Specification-Vendor"), notset));
            message.add(PropertiesHelper.getStringProperty(att, new Attributes.Name("Implementation-Vendor"), notset));
        } else {
            message.add(new Message(Messages.VERSION_INFO_NOT_SET).getMessage());
        }
        message.add(" ");
        message.add(new Message(Messages.SERVER_STARTED_AT_X, new Date(getStartDate()).toString()).getMessage());
        message.add("JDK: " + System.getProperty("java.version") + " (" + System.getProperty("java.vm.info") + ")");
        message.add("OS: " + System.getProperty("os.name") + " (" + System.getProperty("os.version") + ", " + System.getProperty("os.arch") + ")");
        message.add(" ");
        if (agents.size() == 0) {
            message.add(new Message(Messages.AGENTS_RUNNING).getMessage() + " "
                    + new Message(Messages.NONE).getMessage());
        } else {
            message.add(new Message(Messages.AGENTS_RUNNING).getMessage());
            UMOAgent umoAgent;
            for (Iterator iterator = agents.values().iterator(); iterator.hasNext();) {
                umoAgent = (UMOAgent) iterator.next();
                message.add("  " + umoAgent.getDescription());
            }
        }
        return StringMessageHelper.getBoilerPlate(message, '*', 70);
    }

    private String getEndSplash()
    {
        List message = new ArrayList(2);
        long currentTime = System.currentTimeMillis();
        message.add(new Message(Messages.SHUTDOWN_NORMALLY_ON_X, new Date().toString()).getMessage());
        long duration = 10;
        if (startDate > 0) {
            duration = currentTime - startDate;
        }
        message.add(new Message(Messages.SERVER_WAS_UP_FOR_X, Utility.getFormattedDuration(duration)).getMessage());

        return StringMessageHelper.getBoilerPlate(message, '*', 80);
    }

    /**
     * {@inheritDoc}
     */
    public void registerAgent(UMOAgent agent) throws UMOException
    {
        agents.put(agent.getName(), agent);
        agent.registered();
        if (initialised.get() || initialising.get()) {
            agent.initialise();
        }
        if ((started.get() || starting.get())) {
            agent.start();
        }
    }

    /**
     * {@inheritDoc}
     */
    public UMOAgent unregisterAgent(String name) throws UMOException
    {
        if(name==null) return null;
        UMOAgent agent = (UMOAgent)agents.remove(name);
        if(agent!=null) {
            agent.dispose();
            agent.unregistered();
        }
        return agent;
    }

    /**
     * Initialises all registered agents
     * 
     * @throws InitialisationException
     */
    protected void initialiseAgents() throws InitialisationException
    {
        UMOAgent umoAgent;
        logger.info("Initialising agents...");
        for (Iterator iterator = agents.values().iterator(); iterator.hasNext();) {
            umoAgent = (UMOAgent) iterator.next();
            logger.debug("Initialising agent: " + umoAgent.getName());
            umoAgent.initialise();
        }
        logger.info("Agents Successfully Initialised");
    }

    /**
     * {@inheritDoc}
     */
    protected void startAgents() throws UMOException
    {
        UMOAgent umoAgent;
        logger.info("Starting agents...");
        for (Iterator iterator = agents.values().iterator(); iterator.hasNext();) {
            umoAgent = (UMOAgent) iterator.next();
            logger.info("Starting agent: " + umoAgent.getDescription());
            umoAgent.start();

        }
        logger.info("Agents Successfully Started");
    }

    /**
     * {@inheritDoc}
     */
    protected void stopAgents() throws UMOException
    {
        logger.info("Stopping agents...");
        for (Iterator iterator = agents.values().iterator(); iterator.hasNext();) {
            UMOAgent umoAgent = (UMOAgent) iterator.next();
            logger.debug("Stopping agent: " + umoAgent.getName());
            umoAgent.stop();
        }
        logger.info("Agents Successfully Stopped");
    }

    /**
     * {@inheritDoc}
     */
    protected void disposeAgents()
    {
        UMOAgent umoAgent;
        logger.info("disposing agents...");
        for (Iterator iterator = agents.values().iterator(); iterator.hasNext();) {
            umoAgent = (UMOAgent) iterator.next();
            logger.debug("Disposing agent: " + umoAgent.getName());
            umoAgent.dispose();
        }
        logger.info("Agents Successfully Disposed");
    }

    /**
     * associates a Dependency Injector container or Jndi container with Mule. This can be used to
     * integrate container managed resources with Mule resources
     * 
     * @param container a Container context to use. By default, there is a
     *            default Mule container <code>MuleContainerContext</code>
     *            that will assume that the reference key for an oblect is a
     *            classname and will try to instanciate it.
     */
    public void setContainerContext(UMOContainerContext container) throws UMOException
    {
        if (container == null) {
            if (containerContext != null) {
                containerContext.dispose();
            }
            containerContext = new MultiContainerContext();
        } else {
            container.initialise();
            containerContext.addContainer(container);
        }
    }

    /**
     * associates a Dependency Injector container with Mule. This can be used to
     * integrate container managed resources with Mule resources
     * 
     * @return the container associated with the Manager
     */
    public UMOContainerContext getContainerContext()
    {
        return containerContext;
    }

    /**
     * {@inheritDoc}
     */
    public void registerListener(UMOServerEventListener l)
    {
        registerListener(l, null);
    }

    public void registerListener(UMOServerEventListener l, String resourceIdentifier)
    {
        if (eventManager == null) {
            throw new MuleRuntimeException(new Message(Messages.SERVER_EVENT_MANAGER_NOT_ENABLED));
        }
        eventManager.registerListener(l, resourceIdentifier);
    }

    /**
     * {@inheritDoc}
     */
    public void unregisterListener(UMOServerEventListener l)
    {
        if (eventManager != null) {
            eventManager.unregisterListener(l);
        }
    }

    /**
     * Fires a mule 'system' event. These are events that are fired because
     * something within the Mule instance happened such as the Model started or
     * the server is being disposed.
     * 
     * @param e the event that occurred
     */
    protected void fireSystemEvent(UMOServerEvent e)
    {
        if (eventManager != null) {
            eventManager.fireEvent(e);
        } else if (logger.isDebugEnabled()) {
            logger.debug("Event Manager is not enabled, ignoring event: " + e);
        }
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
        // if(event instanceof CustomEvent) {
        if (eventManager != null) {
            eventManager.fireEvent(event);
        } else if (logger.isDebugEnabled()) {
            logger.debug("Event Manager is not enabled, ignoring event: " + event);
        }
        // } else {
        // throw new UnsupportedOperationException(new
        // Message(Messages.ONLY_CUSTOM_EVENTS_CAN_BE_FIRED).getMessage());
        // }
    }

    public void setId(String id)
    {
        if (this.id == null) {
            this.id = id;
        }
    }

    public String getId()
    {
        return id;
    }

    /**
     * Sets the security manager used by this Mule instance to authenticate and
     * authorise incoming and outgoing event traffic and service invocations
     * 
     * @param securityManager the security manager used by this Mule instance to
     *            authenticate and authorise incoming and outgoing event traffic
     *            and service invocations
     */
    public void setSecurityManager(UMOSecurityManager securityManager) throws InitialisationException
    {
        this.securityManager = securityManager;
        if (securityManager != null && isInitialised()) {
            this.securityManager.initialise();
        }
    }

    /**
     * Gets the security manager used by this Mule instance to authenticate and
     * authorise incoming and outgoing event traffic and service invocations
     * 
     * @return he security manager used by this Mule instance to authenticate
     *         and authorise incoming and outgoing event traffic and service
     *         invocations
     */
    public UMOSecurityManager getSecurityManager()
    {
        return securityManager;
    }

    /**
     * Obtains a workManager instance that can be used to schedule work in a
     * thread pool. This will be used primarially by UMOAgents wanting to
     * schedule work. This work Manager must <b>never</b> be used by provider
     * implementations as they have their own workManager accible on the
     * connector.
     * 
     * If a workManager has not been set by the time the
     * <code>initialise()</code> method has been called a default
     * <code>MuleWorkManager</code> will be created using the
     * <i>DefaultThreadingProfile</i> on the <code>MuleConfiguration</code>
     * object.
     * 
     * @return a workManager instance used by the current MuleManager
     * @see org.mule.config.ThreadingProfile
     * @see MuleConfiguration
     */
    public UMOWorkManager getWorkManager()
    {
        return workManager;
    }

    /**
     * Obtains a workManager instance that can be used to schedule work in a
     * thread pool. This will be used primarially by UMOAgents wanting to
     * schedule work. This work Manager must <b>never</b> be used by provider
     * implementations as they have their own workManager accible on the
     * connector.
     * 
     * If a workManager has not been set by the time the
     * <code>initialise()</code> method has been called a default
     * <code>MuleWorkManager</code> will be created using the
     * <i>DefaultThreadingProfile</i> on the <code>MuleConfiguration</code>
     * object.
     * 
     * @param workManager the workManager instance used by the current
     *            MuleManager
     * @throws IllegalStateException if the workManager has already been set.
     * 
     * @see org.mule.config.ThreadingProfile
     * @see MuleConfiguration
     * @see MuleWorkManager
     */
    public void setWorkManager(UMOWorkManager workManager)
    {
        if (this.workManager != null) {
            throw new IllegalStateException(new Message(Messages.CANT_SET_X_ONCE_IT_HAS_BEEN_SET, "workManager").getMessage());
        }
        this.workManager = workManager;
    }

    public QueueManager getQueueManager()
    {
        return queueManager;
    }

    public void setQueueManager(QueueManager queueManager)
    {
        this.queueManager = queueManager;
    }

    /**
     * The shutdown thread used by the server when its main thread is terminated
     */
    private class ShutdownThread extends Thread
    {
        Throwable t;
        boolean aggressive = true;

        public ShutdownThread()
        {
            super();
            this.t = shutdownContext.getException();
            this.aggressive = shutdownContext.isAggressive();
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Runnable#run()
         */
        public void run()
        {
            dispose();
            if (!aggressive) {
                // FIX need to check if there are any outstanding
                // operations to be done?
            }

            if (server != null) {
                if (t != null) {
                    server.shutdown(t);
                } else {
                    server.shutdown();
                }
            }
        }
    }

    private class ShutdownContext
    {
        private boolean aggressive = false;
        private Throwable exception = null;

        public ShutdownContext(boolean aggressive, Throwable exception) {
            this.aggressive = aggressive;
            this.exception = exception;
        }

        public boolean isAggressive() {
            return aggressive;
        }

        public Throwable getException() {
            return exception;
        }
    }
}
