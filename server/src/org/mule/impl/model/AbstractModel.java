/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package org.mule.impl.model;

import java.beans.ExceptionListener;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.MuleManager;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.DefaultComponentExceptionStrategy;
import org.mule.impl.DefaultLifecycleAdapterFactory;
import org.mule.impl.ImmutableMuleDescriptor;
import org.mule.impl.MuleDescriptor;
import org.mule.impl.MuleSession;
import org.mule.impl.endpoint.MuleEndpoint;
import org.mule.impl.internal.events.ModelEvent;
import org.mule.model.DynamicEntryPointResolver;
import org.mule.providers.QueueEnabledConnector;
import org.mule.routing.outbound.AbstractOutboundRouter;
import org.mule.routing.outbound.FilteringMulticastingRouter;
import org.mule.transaction.TransactionCoordination;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMODescriptor;
import org.mule.umo.UMOException;
import org.mule.umo.UMOSession;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.Initialisable;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.lifecycle.UMOLifecycleAdapterFactory;
import org.mule.umo.manager.UMOServerEvent;
import org.mule.umo.model.ModelException;
import org.mule.umo.model.UMOEntryPointResolver;
import org.mule.umo.model.UMOModel;
import org.mule.umo.routing.UMOOutboundRouter;
import org.mule.umo.routing.UMORouter;

import com.mirth.connect.model.Event;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;

/**
 * <code>MuleModel</code> is the default implementation of the UMOModel. The
 * model encapsulates and manages the runtime behaviour of a Mule Server
 * instance. It is responsible for maintaining the UMOs instances and their
 * configuration.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1116 $
 */
public abstract class AbstractModel implements UMOModel {
	/**
	 * logger used by this class
	 */
	protected transient Log logger = LogFactory.getLog(getClass());

	private String name;
	private UMOEntryPointResolver entryPointResolver;
	private UMOLifecycleAdapterFactory lifecycleAdapterFactory;

	private ConcurrentHashMap components;

	/**
	 * Collection for mule descriptors registered in this Manager
	 */
	protected ConcurrentHashMap descriptors;

	private AtomicBoolean initialised = new AtomicBoolean(false);

	private AtomicBoolean started = new AtomicBoolean(false);

	private ExceptionListener exceptionListener;

	/**
	 * Default constructor
	 */
	public AbstractModel() {
		// Always set default entrypoint resolver, lifecycle and compoenent
		// resolver and exceptionstrategy.
		entryPointResolver = new DynamicEntryPointResolver();
		lifecycleAdapterFactory = new DefaultLifecycleAdapterFactory();
		components = new ConcurrentHashMap();
		descriptors = new ConcurrentHashMap();
		exceptionListener = new DefaultComponentExceptionStrategy();
		name = "mule";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.UMOModel#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.UMOModel#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.model.UMOModel#getEntryPointResolver()
	 */
	public UMOEntryPointResolver getEntryPointResolver() {
		return entryPointResolver;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.model.UMOModel#setEntryPointResolver(org.mule.umo.model.UMOEntryPointResolver)
	 */
	public void setEntryPointResolver(UMOEntryPointResolver entryPointResolver) {
		this.entryPointResolver = entryPointResolver;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.UMOModel#isUMORegistered(java.lang.String)
	 */
	public boolean isComponentRegistered(String name) {
		return (components.get(name) != null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.UMOModel#registerUMO(org.mule.umo.UMODescriptor)
	 */
	public UMOComponent registerComponent(UMODescriptor descriptor) throws UMOException {
		if (descriptor == null) {
			throw new ModelException(new Message(Messages.X_IS_NULL, "UMO Descriptor"));
		}

		// Set the es if one wasn't set in the configuration
		if (descriptor.getExceptionListener() == null) {
			descriptor.setExceptionListener(exceptionListener);
		}

		// detect duplicate descriptor declarations
		if (descriptors.get(descriptor.getName()) != null) {
			throw new ModelException(new Message(Messages.DESCRIPTOR_X_ALREADY_EXISTS, descriptor.getName()));
		}

		UMOComponent component = (UMOComponent) components.get(descriptor.getName());

		if (component == null) {
			component = createComponent(descriptor);
			descriptors.put(descriptor.getName(), descriptor);
			components.put(descriptor.getName(), component);
		}

		logger.debug("Added Mule UMO: " + descriptor.getName());

		if (initialised.get()) {
			logger.info("Initialising component: " + descriptor.getName());
			component.initialise();
		}
		
        if (started.get()) {
            // MIRTH-1430, MIRTH-1427: We don't want the channel to start if the initial state is stopped, even on a re-deploy.
            if (!MuleDescriptor.INITIAL_STATE_STOPPED.equals(descriptor.getInitialState())) {
                logger.info("Starting component: " + descriptor.getName() + "...");

                try {
                    registerListeners(component);
                    component.start();
                } catch (Throwable t) {
                    // MIRTH-1952: If the channel errors on startup we should unregister it
                    logger.error("Error encountered starting channel: " + descriptor.getName(), t);
                    unregisterListeners(component);
                    component.stop();
                }
            }
        }
		
		return component;
	}

	public void unregisterComponent(UMODescriptor descriptor) throws UMOException {
		if (descriptor == null) {
			throw new ModelException(new Message(Messages.X_IS_NULL, "UMO Descriptor"));
		}

		if (!isComponentRegistered(descriptor.getName())) {
			throw new ModelException(new Message(Messages.COMPONENT_X_NOT_REGISTERED, descriptor.getName()));
		}
		UMOComponent component = (UMOComponent) components.remove(descriptor.getName());

		if (component != null) {
			component.stop();
			unregisterListeners(component);
			// MIRTH-1427, MIRTH-1447: Stop the dispatchers and their queue threads
			stopDispatchers(component);
			descriptors.remove(descriptor.getName());
			component.dispose();
			logger.info("The component: " + descriptor.getName() + " has been unregistered and disposing");
		}
	}

	protected void registerListeners(UMOComponent component) throws UMOException {
		UMOEndpoint endpoint;
		List endpoints = new ArrayList();
		endpoints.addAll(component.getDescriptor().getInboundRouter().getEndpoints());
		if (component.getDescriptor().getInboundEndpoint() != null) {
			endpoints.add(component.getDescriptor().getInboundEndpoint());
		}
		// Add response endpoints if any
		if (component.getDescriptor().getResponseRouter() != null && component.getDescriptor().getResponseRouter().getEndpoints() != null) {
			endpoints.addAll(component.getDescriptor().getResponseRouter().getEndpoints());
		}

		for (Iterator it = endpoints.iterator(); it.hasNext();) {
			endpoint = (UMOEndpoint) it.next();
			try {
				endpoint.getConnector().registerListener(component, endpoint);
			} catch (UMOException e) {
				throw e;
			} catch (Exception e) {
				throw new ModelException(new Message(Messages.FAILED_TO_REGISTER_X_ON_ENDPOINT_X, component.getDescriptor().getName(), endpoint.getEndpointURI()), e);
			}
		}
        
        // MIRTH-1427: Start any queue threads if destinations are QueueEnabledConnectors
        if (component.getDescriptor().getOutboundRouter().getRouters() != null) {
            List outboundRouters = component.getDescriptor().getOutboundRouter().getRouters();
            
            for (int ori = 0; ori < outboundRouters.size(); ori++) {
                UMOOutboundRouter outRouter = (UMOOutboundRouter) outboundRouters.get(ori);
                
                for (int epi = 0; epi < outRouter.getEndpoints().size(); epi++) {                   
                    MuleEndpoint muleEP = (MuleEndpoint) outRouter.getEndpoints().get(epi);
                    
                    if (muleEP.getConnector() instanceof QueueEnabledConnector) {
                        QueueEnabledConnector queueEnabledConnector = (QueueEnabledConnector) muleEP.getConnector();
                        
                        if (queueEnabledConnector.isUsePersistentQueues()) {
                            queueEnabledConnector.startQueueThread();
                        }
                    }
                }
            }
        }
	}

	protected void unregisterListeners(UMOComponent component) throws UMOException {
		UMOEndpoint endpoint;
		List endpoints = component.getDescriptor().getInboundRouter().getEndpoints();
		if (component.getDescriptor().getInboundEndpoint() != null) {
			endpoints.add(component.getDescriptor().getInboundEndpoint());
		}

		for (Iterator it = endpoints.iterator(); it.hasNext();) {
			endpoint = (UMOEndpoint) it.next();
			try {
				endpoint.getConnector().unregisterListener(component, endpoint);
			} catch (Exception e) {
			    // rather than throwing the UMOException or ModelException,
			    // log an error so listeners continue to be cleaned up.
			    logger.error("Error unregistering listener. This may occur when cleaning up a failed deploy.", new ModelException(new Message(Messages.FAILED_TO_UNREGISTER_X_ON_ENDPOINT_X, component.getDescriptor().getName(), endpoint.getEndpointURI()), e));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.model.UMOModel#getLifecycleAdapterFactory()
	 */
	public UMOLifecycleAdapterFactory getLifecycleAdapterFactory() {
		return lifecycleAdapterFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.model.UMOModel#setLifecycleAdapterFactory(org.mule.umo.lifecycle.UMOLifecycleAdapterFactory)
	 */
	public void setLifecycleAdapterFactory(UMOLifecycleAdapterFactory lifecycleAdapterFactory) {
		this.lifecycleAdapterFactory = lifecycleAdapterFactory;
	}

	/**
	 * Destroys any current components
	 * 
	 */
	public void dispose() {
		fireEvent(new ModelEvent(this, ModelEvent.MODEL_DISPOSING));
		UMOComponent temp = null;
		Object key = null;
		for (Iterator i = components.keySet().iterator(); i.hasNext();) {
			try {
				key = i.next();
				temp = (UMOComponent) components.get(key);
				try {
					temp.dispose();
				} catch (Exception e1) {
					logger.warn("Failed to dispose component: " + e1.getMessage());
				}
				logger.info(temp + " has been destroyed successfully");
			} catch (ConcurrentModificationException e) {
				logger.warn("cannot dispose calling component");
				// return;
			}
		}
		components.clear();
		descriptors.clear();
		components = null;
		descriptors = null;
		fireEvent(new ModelEvent(this, ModelEvent.MODEL_DISPOSED));
	}

	/**
	 * Returns a valid component for the given Mule name
	 * 
	 * @param muleName
	 *            the Name of the Mule for which the component is required
	 * @return a component for the specified name
	 */
	public UMOSession getComponentSession(String muleName) {
		UMOComponent component = (UMOComponent) components.get(muleName);
		if (component == null) {
			logger.warn("Component: " + muleName + " not found returning null session");
			return null;
		} else {
			return new MuleSession(component, TransactionCoordination.getInstance().getTransaction());
		}
	}

	/**
	 * Stops any registered components
	 * 
	 * @throws UMOException
	 *             if a Component fails tcomponent
	 */
	public void stop() throws UMOException {
		fireEvent(new ModelEvent(this, ModelEvent.MODEL_STOPPING));
		for (Iterator i = components.values().iterator(); i.hasNext();) {
			UMOComponent temp = (UMOComponent) i.next();
			temp.stop();
			logger.info("Component " + temp + " has been stopped successfully");
		}
		fireEvent(new ModelEvent(this, ModelEvent.MODEL_STOPPED));
	}

	/**
	 * Starts all registered components
	 * 
	 * @throws UMOException
	 *             if any of the components fail to start
	 */
	public void start() throws UMOException {

		// ast: avoid the launch of exceptions during the startup of components

		if (!initialised.get()) {
			initialise();
		}

		if (!started.get()) {
			fireEvent(new ModelEvent(this, ModelEvent.MODEL_STARTING));

			for (Iterator i = components.values().iterator(); i.hasNext();) {
				UMOComponent temp = (UMOComponent) i.next();

				try {
					
					if (temp.getDescriptor().getInitialState().equals(ImmutableMuleDescriptor.INITIAL_STATE_STARTED)) {
						registerListeners(temp);
						temp.start();
						logger.info("Component " + temp + " has been started successfully");
					} else if (temp.getDescriptor().getInitialState().equals(ImmutableMuleDescriptor.INITIAL_STATE_PAUSED)) {
						//registerListeners(temp);
						//registerListeners(temp);
						temp.start();
						temp.pause();
						logger.info("Component " + temp + " has an initial state of 'paused'");
					} else {
						//temp.start();
						//temp.stop();
						logger.info("Component " + temp + " has an initial state of 'stopped'");
					}
				} catch (Exception e) {
					try {
						unregisterListeners(temp);
						temp.stop();
					} catch (Throwable t) {
						logger.warn("Error stopping a connector after an error " + t);
					}
					logger.error("Error starting component [" + temp + "] \n" + e);

					EventController eventController = ControllerFactory.getFactory().createEventController();
					Event event = new Event("Error starting the channel due to a problem at one of the endpoints " + temp.getDescriptor().getName());
					event.setLevel(Event.Level.ERROR);
					event.getAttributes().put(Event.ATTR_EXCEPTION, ExceptionUtils.getStackTrace(e));
					eventController.addEvent(event);
				}

			}
			started.set(true);
			fireEvent(new ModelEvent(this, ModelEvent.MODEL_STARTED));
		} else {
			logger.debug("Model already started");
		}
	}
	
	public void startDispatchers(UMOComponent component) throws UMOException{ 
		List<UMORouter> routers = component.getDescriptor().getOutboundRouter().getRouters(); 
		for(UMORouter router : routers) {				
			for (Iterator it = ((FilteringMulticastingRouter)router).getEndpoints().iterator(); it.hasNext();) {
				UMOEndpoint endpoint = (UMOEndpoint) it.next();
				try {
					endpoint.getConnector().startDispatchers(component, endpoint);
				} catch (UMOException e) {
					throw e;
				} catch (Exception e) {
					throw new ModelException(new Message(Messages.FAILED_TO_UNREGISTER_X_ON_ENDPOINT_X, component.getDescriptor().getName(), endpoint.getEndpointURI()), e);
				}
			}
		}
	}
	
	public void stopDispatchers(UMOComponent component) throws UMOException{ 
		List<UMORouter> routers = component.getDescriptor().getOutboundRouter().getRouters(); 
		for(UMORouter router : routers) {				
			for (Iterator it = ((AbstractOutboundRouter)router).getEndpoints().iterator(); it.hasNext();) {
				UMOEndpoint endpoint = (UMOEndpoint) it.next();
				try {
					endpoint.getConnector().stopDispatchers(component, endpoint);
				} catch (Exception e) {
				 // rather than throwing the UMOException or ModelException,
	                // log an error so dispatchers continue to be cleaned up.
				    logger.error("Error stopping dispatcher. This may occur when cleaning up a failed deploy.", new ModelException(new Message(Messages.FAILED_TO_UNREGISTER_X_ON_ENDPOINT_X, component.getDescriptor().getName(), endpoint.getEndpointURI()), e));
				}
			}
		}
	}
	
	/**
	 * Stops a single Mule Component. This can be useful when stopping and
	 * starting some Mule UMOs while letting others continue.
	 * 
	 * @param name
	 *            the name of the Mule UMO to stop
	 * @throws UMOException
	 *             if the MuleUMO is not registered
	 */
	public void stopComponent(String name) throws UMOException {
		UMOComponent component = (UMOComponent) components.get(name);
		
		if (component == null) {
			throw new ModelException(new Message(Messages.COMPONENT_X_NOT_REGISTERED, name));
		} else {
			unregisterListeners(component);
			stopDispatchers(component);
			component.stop();
			logger.info("mule " + name + " has been stopped successfully");
		}
	}

	/**
	 * Starts a single Mule Component. This can be useful when stopping and
	 * starting some Mule UMOs while letting others continue
	 * 
	 * @param name
	 *            the name of the Mule UMO to start
	 * @throws UMOException
	 *             if the MuleUMO is not registered or the component failed to
	 *             start
	 */
	public void startComponent(String name) throws UMOException {

		UMOComponent component = (UMOComponent) components.get(name);
		if (component == null) {
			throw new ModelException(new Message(Messages.COMPONENT_X_NOT_REGISTERED, name));
		} else {
			try {
				registerListeners(component);
				startDispatchers(component);
				component.start();
				logger.info("Mule " + component.toString() + " has been started successfully");
			} catch (Exception e) {
				logger.error("Error starting component [" + name + "] \n" + e);
				try {
					unregisterListeners(component);
					stopDispatchers(component);
					component.stop();
				} catch (Throwable t) {
					logger.warn("Error stopping a connector after an error " + t);
				}

				EventController eventController = ControllerFactory.getFactory().createEventController();
				Event event = new Event("Error starting the channel due to a problem at one of the endpoints " + component.getDescriptor().getName());
				event.setLevel(Event.Level.ERROR);
				event.getAttributes().put(Event.ATTR_EXCEPTION, ExceptionUtils.getStackTrace(e));
				eventController.addEvent(event);
			}
		}
	}

	/**
	 * Pauses event processing for a single Mule Component. Unlike
	 * stopComponent(), a paused component will still consume messages from the
	 * underlying transport, but those messages will be queued until the
	 * component is resumed. <p/> In order to persist these queued messages you
	 * can set the 'recoverableMode' property on the Muleconfiguration to true.
	 * this causes all internal queues to store their state.
	 * 
	 * @param name
	 *            the name of the Mule UMO to stop
	 * @throws org.mule.umo.UMOException
	 *             if the MuleUMO is not registered or the component failed to
	 *             pause.
	 * @see org.mule.config.MuleConfiguration
	 */
	public void pauseComponent(String name) throws UMOException {
		UMOComponent component = (UMOComponent) components.get(name);
		if (component == null) {
			throw new ModelException(new Message(Messages.COMPONENT_X_NOT_REGISTERED, name));
		} else {
			component.pause();
			logger.info("Mule Component " + name + " has been paused successfully");
		}
	}

	/**
	 * Resumes a single Mule Component that has been paused. If the component is
	 * not paused nothing is executed.
	 * 
	 * @param name
	 *            the name of the Mule UMO to resume
	 * @throws org.mule.umo.UMOException
	 *             if the MuleUMO is not registered or the component failed to
	 *             resume
	 */
	public void resumeComponent(String name) throws UMOException {
		UMOComponent component = (UMOComponent) components.get(name);
		if (component == null) {
			throw new ModelException(new Message(Messages.COMPONENT_X_NOT_REGISTERED, name));
		} else {
			component.resume();
			logger.info("Mule Component " + name + " has been resumed successfully");
		}
	}

	public void setComponents(List descriptors) throws UMOException {
		for (Iterator iterator = descriptors.iterator(); iterator.hasNext();) {
			registerComponent((UMODescriptor) iterator.next());
		}
	}

	public void initialise() throws InitialisationException {
		if (!initialised.get()) {
			fireEvent(new ModelEvent(this, ModelEvent.MODEL_INITIALISING));

			if (exceptionListener instanceof Initialisable) {
				((Initialisable) exceptionListener).initialise();
			}
			UMOComponent temp = null;
			for (Iterator i = components.values().iterator(); i.hasNext();) {
				temp = (UMOComponent) i.next();
				temp.initialise();

				logger.info("Component " + temp.getDescriptor().getName() + " has been started successfully");
			}
			initialised.set(true);
			fireEvent(new ModelEvent(this, ModelEvent.MODEL_INITIALISED));
		} else {
			logger.debug("Model already initialised");
		}
	}

	public ExceptionListener getExceptionListener() {
		return exceptionListener;
	}

	public void setExceptionListener(ExceptionListener exceptionListener) {
		this.exceptionListener = exceptionListener;
	}

	public UMODescriptor getDescriptor(String name) {
		return (UMODescriptor) descriptors.get(name);
	}

	/**
	 * Gets an iterator of all component names registered in the model
	 * 
	 * @return an iterator of all component names
	 */
	public Iterator getComponentNames() {
		return components.keySet().iterator();
	}

	void fireEvent(UMOServerEvent event) {
		MuleManager.getInstance().fireEvent(event);
	}

	protected abstract UMOComponent createComponent(UMODescriptor descriptor);
}
