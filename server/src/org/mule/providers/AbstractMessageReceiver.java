/* 
 * $Header: /home/projects/mule/scm/mule/mule/src/java/org/mule/providers/AbstractMessageReceiver.java,v 1.36 2005/10/29 15:49:49 rossmason Exp $
 * $Revision: 1.36 $
 * $Date: 2005/10/29 15:49:49 $
 * ------------------------------------------------------------------------------------------------------
 * 
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 * 
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file. 
 *
 */

package org.mule.providers;

import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.config.ExceptionHelper;
import org.mule.config.ThreadingProfile;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleEvent;
import org.mule.impl.MuleMessage;
import org.mule.impl.MuleSession;
import org.mule.impl.RequestContext;
import org.mule.impl.ResponseOutputStream;
import org.mule.impl.internal.events.ConnectionEvent;
import org.mule.impl.internal.events.MessageEvent;
import org.mule.impl.internal.events.SecurityEvent;
import org.mule.transaction.TransactionCoordination;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.UMOSession;
import org.mule.umo.UMOTransaction;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.manager.UMOWorkManager;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageReceiver;
import org.mule.umo.provider.UniqueIdNotSupportedException;
import org.mule.umo.security.SecurityException;
import org.mule.umo.transformer.TransformerException;
import org.mule.umo.transformer.UMOTransformer;
import org.mule.util.concurrent.WaitableBoolean;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;

/**
 * <code>AbstractMessageReceiver</code> provides common methods for all
 * Message Receivers provided with Mule. A message receiver enables an endpoint
 * to receive a message from an external system.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.36 $
 */
public abstract class AbstractMessageReceiver implements UMOMessageReceiver {
	/**
	 * logger used by this class
	 */
	protected transient Log logger = LogFactory.getLog(getClass());

	/**
	 * The Component with which this receiver is associated with
	 */
	protected UMOComponent component = null;

	/**
	 * The endpoint descriptor which is associated with this receiver
	 */
	protected UMOEndpoint endpoint = null;

	private InternalMessageListener listener;
	/**
	 * the connector associated with this receiver
	 */
	protected AbstractConnector connector = null;

	protected boolean serverSide = true;

	protected AtomicBoolean disposing = new AtomicBoolean(false);

	protected WaitableBoolean connected = new WaitableBoolean(false);

	protected WaitableBoolean stopped = new WaitableBoolean(true);

	private AtomicBoolean connecting = new AtomicBoolean(false);

	/**
	 * Stores the endpointUri that this receiver listens on. This enpoint can be
	 * different to the endpointUri in the endpoint stored on the receiver as
	 * endpoint endpointUri may get rewritten if this endpointUri is a wildcard
	 * endpointUri such as jms.*
	 */
	private UMOEndpointURI endpointUri;

	private UMOWorkManager workManager;

	protected ConnectionStrategy connectionStrategy;

	/**
	 * Creates the Message Receiver
	 * 
	 * @param connector
	 *            the endpoint that created this listener
	 * @param component
	 *            the component to associate with the receiver. When data is
	 *            recieved the component <code>dispatchEvent</code> or
	 *            <code>sendEvent</code> is used to dispatch the data to the
	 *            relivant UMO.
	 * @param endpoint
	 *            the provider contains the endpointUri on which the receiver
	 *            will listen on. The endpointUri can be anything and is
	 *            specific to the receiver implementation i.e. an email address,
	 *            a directory, a jms destination or port address.
	 * @see UMOComponent
	 * @see UMOEndpoint
	 */
	public AbstractMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint) throws InitialisationException {
		setConnector(connector);
		setComponent(component);
		setEndpoint(endpoint);
		listener = new DefaultInternalMessageListener();
		endpointUri = endpoint.getEndpointURI();
		if (connector instanceof AbstractConnector) {
			ThreadingProfile tp = ((AbstractConnector) connector).getReceiverThreadingProfile();
			if (serverSide) {
				tp.setThreadPriority(Thread.NORM_PRIORITY + 2);
			}
			workManager = tp.createWorkManager(connector.getName() + "." + endpoint.getName() + ".receiver");
			try {
				workManager.start();
			} catch (UMOException e) {
				throw new InitialisationException(e, this);
			}
		}

		connectionStrategy = this.connector.getConnectionStrategy();
		// if(connectionStrategy instanceof AbstractConnectionStrategy) {
		// ((AbstractConnectionStrategy)connectionStrategy).setDoThreading(
		// this.connector.getReceiverThreadingProfile().isDoThreading());
		// }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.provider.UMOMessageReceiver#getEndpointName()
	 */
	public UMOEndpoint getEndpoint() {
		return endpoint;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.provider.UMOMessageReceiver#getExceptionListener()
	 */
	public void handleException(Exception exception) {
		if (exception instanceof ConnectException) {
			logger.info("Exception caught is a ConnectException, disconnecting receiver and invoking ReconnectStrategy");
			try {
				disconnect();
			} catch (Exception e) {
				connector.getExceptionListener().exceptionThrown(e);
				setExceptionCode(exception);
			}
		}
		connector.getExceptionListener().exceptionThrown(exception);
		setExceptionCode(exception);
		if (exception instanceof ConnectException) {
			try {
				connectionStrategy.connect(this);
			} catch (UMOException e) {
				connector.getExceptionListener().exceptionThrown(e);
				setExceptionCode(exception);
			}
		}
	}

	public void setExceptionCode(Exception exception) {
		String propName = ExceptionHelper.getErrorCodePropertyName(connector.getProtocol());
		// If we dont find a error code property we can assume there are not
		// error code mappings for this connector
		if (propName != null) {
			UMOEvent event = RequestContext.getEvent();
			if (event != null) {
				UMOMessage message = event.getMessage();
				String code = ExceptionHelper.getErrorMapping(connector.getProtocol(), exception.getClass());
				if (logger.isDebugEnabled()) {
					logger.debug("Setting error code for: " + connector.getProtocol() + ", " + propName + "=" + code);
				}
				message.setProperty(propName, code);
				RequestContext.rewriteEvent(message);
			}
		}
	}

	public UMOConnector getConnector() {
		return connector;
	}

	public void setConnector(UMOConnector connector) {
		if (connector != null) {
			if (connector instanceof AbstractConnector) {
				this.connector = (AbstractConnector) connector;
			} else {
				throw new IllegalArgumentException(new Message(Messages.PROPERTY_X_IS_NOT_SUPPORTED_TYPE_X_IT_IS_TYPE_X, "connector", AbstractConnector.class.getName(), connector.getClass().getName()).getMessage());
			}
		} else {
			throw new NullPointerException(new Message(Messages.X_IS_NULL, "connector").getMessage());
		}
	}

	public UMOComponent getComponent() {
		return component;
	}

	public final UMOMessage routeMessage(UMOMessage message) throws UMOException {
		return routeMessage(message, (endpoint.isSynchronous() || TransactionCoordination.getInstance().getTransaction() != null));
	}

	public final UMOMessage routeMessage(UMOMessage message, boolean synchronous) throws UMOException {
		UMOTransaction tx = TransactionCoordination.getInstance().getTransaction();
		return routeMessage(message, tx, tx != null || synchronous, null);
	}

	public final UMOMessage routeMessage(UMOMessage message, UMOTransaction trans, boolean synchronous) throws UMOException {
		return routeMessage(message, trans, synchronous, null);
	}

	public final UMOMessage routeMessage(UMOMessage message, OutputStream outputStream) throws UMOException {
		return routeMessage(message, endpoint.isSynchronous(), outputStream);
	}

	public final UMOMessage routeMessage(UMOMessage message, boolean synchronous, OutputStream outputStream) throws UMOException {
		UMOTransaction tx = TransactionCoordination.getInstance().getTransaction();
		return routeMessage(message, tx, tx != null || synchronous, outputStream);
	}

	public final UMOMessage routeMessage(UMOMessage message, UMOTransaction trans, boolean synchronous, OutputStream outputStream) throws UMOException {

		if (connector.isEnableMessageEvents()) {
			connector.fireEvent(new MessageEvent(message, endpoint, component.getDescriptor().getName(), MessageEvent.MESSAGE_RECEIVED));
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Message Received from: " + endpoint.getEndpointURI());
			logger.debug(message);
		}
		if (logger.isTraceEnabled()) {
			try {
				logger.trace("Message Payload: \n" + message.getPayloadAsString());
			} catch (Exception e) {
				// ignore
			}
		}

		// Apply the endpoint filter if one is configured
		if (endpoint.getFilter() != null) {
			if (!endpoint.getFilter().accept(message)) {
				handleUnacceptedFilter(message);
				return null;
			}
		}
		return listener.onMessage(message, trans, synchronous, outputStream);
	}

	protected UMOMessage handleUnacceptedFilter(UMOMessage message) {
		String messageId = null;
		try {
			messageId = message.getUniqueId();
		} catch (UniqueIdNotSupportedException e) {
			messageId = "'Current Message (no unique id)'";
		}
		logger.debug("Message " + messageId + " failed to pass filter on endpoint: " + endpoint + ". Message is being ignored");

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.provider.UMOMessageReceiver#setEndpoint(org.mule.umo.endpoint.UMOEndpoint)
	 */
	public void setEndpoint(UMOEndpoint endpoint) {
		if (endpoint == null) {
			throw new IllegalArgumentException("Provider cannot be null");
		}
		this.endpoint = endpoint;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.provider.UMOMessageReceiver#setSession(org.mule.umo.UMOSession)
	 */
	public void setComponent(UMOComponent component) {
		if (component == null) {
			throw new IllegalArgumentException("Component cannot be null");
		}
		this.component = component;
	}

	public final void dispose() {
		stop();
		disposing.set(true);
		doDispose();
		workManager.dispose();
	}

	/**
	 * Template method to dispose any resources associated with this receiver.
	 * There is not need to dispose the connector as this is already done by the
	 * framework
	 */
	protected void doDispose() {}

	public UMOEndpointURI getEndpointURI() {
		return endpointUri;
	}

	public boolean isServerSide() {
		return serverSide;
	}

	public void setServerSide(boolean serverSide) {
		this.serverSide = serverSide;
	}

	protected UMOWorkManager getWorkManager() {
		return workManager;
	}

	protected void setWorkManager(UMOWorkManager workManager) {
		this.workManager = workManager;
	}

	public void connect() throws Exception {
		if (connected.get()) {
			return;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Attempting to connect to: " + endpoint.getEndpointURI());
		}
		if (connecting.compareAndSet(false, true)) {

			connectionStrategy.connect(this);

			logger.info("Successfully connected to: " + endpoint.getEndpointURI());
			return;
		}

		try {
			doConnect();
			connector.fireEvent(new ConnectionEvent(this, getConnectEventId(), ConnectionEvent.CONNECTION_CONNECTED));
		} catch (Exception e) {
			connector.fireEvent(new ConnectionEvent(this, getConnectEventId(), ConnectionEvent.CONNECTION_FAILED));
			if (e instanceof ConnectException) {
				throw (ConnectException) e;
			} else {
				throw new ConnectException(e, this);
			}
		}
		connected.set(true);
		connecting.set(false);
	}

	public void disconnect() throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Disconnecting from: " + endpoint.getEndpointURI());
		}
		connector.fireEvent(new ConnectionEvent(this, getConnectEventId(), ConnectionEvent.CONNECTION_DISCONNECTED));
		connected.set(false);
		doDisconnect();
		logger.info("Disconnected from: " + endpoint.getEndpointURI());
	}

	public String getConnectionDescription() {
		return endpoint.getEndpointURI().toString();
	}

	public void start() throws UMOException {
		// ast: changes to allow a start without problems
		UMOException startException = null;
		if (stopped.commit(true, false)) {
			if (!connected.get()) {
				ConnectionStrategy oce = connectionStrategy;
				connectionStrategy = new org.mule.providers.SingleAttemptConnectionStrategy();
				try {
					connectionStrategy.connect(this);
				} catch (UMOException t) {
					startException = t;
				}
				connectionStrategy = oce;
			}
			if (startException == null)
				doStart();
			else
				throw startException;
		}
	}

	public void stop() {
		if (stopped.commit(false, true)) {
			try {
				doStop();
			} catch (UMOException e) {
				logger.error(e.getMessage(), e);
			}
			// try {
			// if(connected.get()) disconnect();
			// } catch (Exception e) {
			// logger.error(e.getMessage(), e);
			// }
		}
	}

	public abstract void doConnect() throws Exception;

	public abstract void doDisconnect() throws Exception;

	public void doStart() throws UMOException {

	}

	public void doStop() throws UMOException {

	}

	public boolean isConnected() {
		return connected.get();
	}

	public InternalMessageListener getListener() {
		return listener;
	}

	public void setListener(InternalMessageListener listener) {
		this.listener = listener;
	}

	private class DefaultInternalMessageListener implements InternalMessageListener {

		public UMOMessage onMessage(UMOMessage message, UMOTransaction trans, boolean synchronous, OutputStream outputStream) throws UMOException {

			UMOMessage resultMessage = null;
			ResponseOutputStream ros = null;
			if (outputStream != null) {
				if (outputStream instanceof ResponseOutputStream) {
					ros = (ResponseOutputStream) outputStream;
				} else {
					ros = new ResponseOutputStream(outputStream);
				}
			}
			UMOSession session = new MuleSession(component, trans);
			UMOEvent muleEvent = new MuleEvent(message, endpoint, session, synchronous, ros);
			RequestContext.setEvent(muleEvent);

			// Apply Security filter if one is set
			boolean authorised = false;
			if (endpoint.getSecurityFilter() != null) {
				try {
					endpoint.getSecurityFilter().authenticate(muleEvent);
					authorised = true;
				} catch (SecurityException e) {
					logger.warn("Request was made but was not authenticated: " + e.getMessage(), e);
					connector.fireEvent(new SecurityEvent(e, SecurityEvent.SECURITY_AUTHENTICATION_FAILED));
					handleException(e);
					resultMessage = message;
				}
			} else {
				authorised = true;
			}

			if (authorised) {
				// the security filter may update the payload so we need to get
				// the
				// latest event again
				muleEvent = RequestContext.getEvent();

				// This is a replyTo event for a current request
				if (UMOEndpoint.ENDPOINT_TYPE_RESPONSE.equals(endpoint.getType())) {
					component.getDescriptor().getResponseRouter().route(muleEvent);
					return null;
				} else {
					resultMessage = component.getDescriptor().getInboundRouter().route(muleEvent);
				}
			}
			return applyResponseTransformer(resultMessage);
		}
	}

	protected String getConnectEventId() {
		return connector.getName() + ".receiver (" + endpoint.getEndpointURI() + ")";
	}

	protected UMOMessage applyResponseTransformer(UMOMessage returnMessage) throws TransformerException {
		UMOTransformer transformer = endpoint.getResponseTransformer();
		if (transformer == null)
			transformer = component.getDescriptor().getResponseTransformer();
		if (transformer == null)
			return returnMessage;

		if ((returnMessage == null)) {
			if (transformer.isAcceptNull()) {
				returnMessage = new MuleMessage(new NullPayload(), RequestContext.getProperties());
			} else {
				return null;
			}
		}

		if (transformer.isSourceTypeSupported(returnMessage.getPayload().getClass())) {
			//MULE-549 
			//Fixes bug where SOAP result doesn't include proper Content-Type - CL
			RequestContext.rewriteEvent(returnMessage);
			Object result = transformer.transform(returnMessage.getPayload());
			if (result instanceof UMOMessage) {
				returnMessage = (UMOMessage) result;
			} else {
				returnMessage = new MuleMessage(result, returnMessage.getProperties());
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Response transformer: " + transformer + " doesn't support the result payload: " + returnMessage.getPayload().getClass());
			}
		}
		return returnMessage;
	}
}
