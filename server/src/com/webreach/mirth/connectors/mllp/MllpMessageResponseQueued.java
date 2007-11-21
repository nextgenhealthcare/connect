/*
 * $Header: /home/projects/mule/scm/mule/providers/tcp/src/java/org/mule/providers/tcp/TcpMessageReceiver.java,v 1.23 2005/11/05 12:23:27 aperepel Exp $
 * $Revision: 1.23 $
 * $Date: 2005/11/05 12:23:27 $
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
/*
 *ast: Class created to create an outbound queue
 * */
package com.webreach.mirth.connectors.mllp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.providers.ConnectException;
import org.mule.providers.PollingMessageReceiver;
import org.mule.umo.UMOComponent;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.util.queue.Queue;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.controllers.MessageObjectController;

/**
 * <code>TcpMessageReceiver</code> acts like a tcp server to receive socket
 * requests.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @author <a href="mailto:tsuppari@yahoo.co.uk">P.Oikari</a>
 * 
 * @version $Revision: 1.23 $
 */
public class MllpMessageResponseQueued extends PollingMessageReceiver {

	public static final long DEFAULT_POLL_FREQUENCY = 1000;
	public static final long STARTUP_DELAY = 1000;
	private long frequency = DEFAULT_POLL_FREQUENCY;
	private long pollMaxTime = 10000;
	protected Queue queue = null;

	protected static transient Log logger = LogFactory.getLog(MllpMessageDispatcher.class);
	protected MllpMessageDispatcher auxDispatcher;

	public MllpMessageResponseQueued(MllpConnector connector, UMOComponent component, UMOEndpoint endpoint, Long frecuency) throws InitialisationException {
		super(connector, component, endpoint, frecuency);
		this.connector = connector;
		this.auxDispatcher = new MllpMessageDispatcher(connector);
		this.queue = connector.getQueue(endpoint);
	}

	public void doConnect() throws ConnectException {
		disposing.set(false);
	}

	public void doDisconnect() throws ConnectException {
		// this will cause the server thread to quit
		disposing.set(true);
		auxDispatcher.doDispose();
	}

	public void doDispose() {
		try {
			doDisconnect();
		} catch (Throwable t) {
			logger.error("Error disconnecting: " + t);
		}
	}

	/*public void doStart() throws UMOException {

		super.doStart();
		// System.out.println("[TcpMessageDispatcherQueued] doStart() ");

	}

	public void doStop() throws UMOException {

		super.doStop();
		// System.out.println("[TcpMessageDispatcherQueued] doStop() ");

	}*/

	public int getQueueSize() {
		if (queue != null)
			return queue.size();
		else
			return 0;
	}

	public void poll() throws Exception {
		Boolean open = true;
		if (queue.size() == 0)
			return;
		// If the endopoint is active, try to send without waiting for another
		// pool()
		while ((queue.size() > 0) && open) {
			MessageObject thePayload= null;
			try {
				thePayload = (MessageObject)queue.peek();
				if (auxDispatcher.sendPayload(thePayload, endpoint)) {
					queue.poll(pollMaxTime);
					open = true;
					// UMOMessage
					// um=auxDispatcher.doTheRemoteSyncStuff(endpoint);
					// if (um!=null) System.out.println("MENSAJE RECIBIDO \r\n
					// ["+um.getPayloadAsString()+"]");
				}else{
					MessageObjectController.getInstance().resetQueuedStatus(thePayload);
				}
			} catch (Throwable t) {
				if (thePayload != null){
					MessageObjectController.getInstance().resetQueuedStatus(thePayload);
				}
				logger.debug("Conection error [" + t + "] " + " at " + endpoint.getEndpointURI() + " queue size " + new Integer(queue.size()).toString());
				open = false;
			}
		}
	}

}
