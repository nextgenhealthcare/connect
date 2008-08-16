/*
 * $Header: /home/projects/mule/scm/mule/providers/tcp/src/java/org/mule/providers/tcp/TcpMessageDispatcher.java,v 1.12 2005/11/05 12:23:27 aperepel Exp $
 * $Revision: 1.12 $
 * $Date: 2005/11/05 12:23:27 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package com.webreach.mirth.connectors.tcp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.QueueEnabledMessageDispatcher;
import org.mule.providers.TemplateValueReplacer;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.provider.UMOConnector;
import org.mule.util.queue.Queue;

import sun.misc.BASE64Decoder;

import com.webreach.mirth.connectors.mllp.MllpConnector;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.QueuedMessage;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.controllers.AlertController;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.controllers.MonitoringController;
import com.webreach.mirth.server.controllers.MonitoringController.ConnectorType;
import com.webreach.mirth.server.controllers.MonitoringController.Event;
import com.webreach.mirth.server.util.VMRouter;

/**
 * f <code>TcpMessageDispatcher</code> will send transformed mule events over
 * tcp.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @author <a href="mailto:tsuppari@yahoo.co.uk">P.Oikari</a>
 * 
 * @version $Revision: 1.12 $
 */

public class TcpMessageDispatcher extends AbstractMessageDispatcher implements QueueEnabledMessageDispatcher{
	// ///////////////////////////////////////////////////////////////
	// keepSocketOpen option variables
	// ///////////////////////////////////////////////////////////////

	protected Map<String, Socket> connectedSockets = new HashMap<String, Socket>();

	// ast:queue variables
	protected Queue queue = null;

	protected Queue errorQueue = null;

	// ///////////////////////////////////////////////////////////////
	/**
	 * logger used by this class
	 */
	protected static transient Log logger = LogFactory.getLog(TcpMessageDispatcher.class);

	private TcpConnector connector;
	private MessageObjectController messageObjectController = MessageObjectController.getInstance();
	private TemplateValueReplacer replacer = new TemplateValueReplacer();
	private AlertController alertController = AlertController.getInstance();
	private MonitoringController monitoringController = MonitoringController.getInstance();
	private ConnectorType connectorType = ConnectorType.SENDER;
	public TcpMessageDispatcher(TcpConnector connector) {
		super(connector);
		this.connector = connector;
		monitoringController.updateStatus(connector, connectorType, Event.INITIALIZED);
	}

	protected Socket initSocket(String endpoint) throws IOException, URISyntaxException {
		if (connectedSockets.get(endpoint) != null){
			monitoringController.updateStatus(connector, connectorType, Event.DISCONNECTED, connectedSockets.get(endpoint));
		}
		URI uri = new URI(endpoint);
		int port = uri.getPort();
		InetAddress inetAddress = InetAddress.getByName(uri.getHost());
		InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, port);
		Socket socket = new Socket();
		createSocket(socket, inetSocketAddress);
		socket.setReuseAddress(true);
		socket.setReceiveBufferSize(connector.getBufferSize());
		socket.setSendBufferSize(connector.getBufferSize());
		socket.setSoTimeout(connector.getSendTimeout());
		connectedSockets.put(endpoint, socket);
		monitoringController.updateStatus(connector, connectorType, Event.CONNECTED, socket);
		return socket;
	}

	// ast:Code changes to allow queues
	/*
	 * As doSend is never called, all the changes are made to the doSispatch
	 * method
	 */
	public void doDispatch(UMOEvent event) throws Exception {
		Socket socket = null;
		Object payload = null;
		boolean success = false;
		Exception exceptionWriting = null;
		String exceptionMessage = "";
		String endpointUri = event.getEndpoint().getEndpointURI().toString();
		MessageObject messageObject = messageObjectController.getMessageObjectFromEvent(event);
		if (messageObject == null) {
			return;
		}

		String host = replacer.replaceURLValues(endpointUri, messageObject);
		
		try {
			if (queue != null) {
				connector.putMessageInQueue(event.getEndpoint().getEndpointURI(), messageObject);
				return;
			} else {
				int retryCount = -1;
				int maxRetries = connector.getMaxRetryCount();
				while (!success && !disposed && (retryCount < maxRetries)) {

                    monitoringController.updateStatus(connector, connectorType, Event.ATTEMPTING_TO_CONNECT, socket);

                    if (maxRetries != TcpConnector.KEEP_RETRYING_INDEFINETLY) {
						retryCount++;
					}
					try {
						if (!connector.isKeepSendSocketOpen()) {
							socket = initSocket(host);
							writeTemplatedData(socket, messageObject);
							success = true;
						} else {
							socket = connectedSockets.get(host);
							if (socket != null && !socket.isClosed()) {
								try{
									writeTemplatedData(socket, messageObject);
									success = true;
								}catch (Exception e){
									//if the connection was lost, try creating it again
									doDispose(socket);
									socket = initSocket(host);
									writeTemplatedData(socket, messageObject);
									success = true;
								}
							} else {
								socket = initSocket(host);
								writeTemplatedData(socket, messageObject);
								success = true;
							}
						}
					} catch (Exception exs) {
						if (retryCount < maxRetries) {
							if (socket != null){
								doDispose(socket);
							}
							logger.warn("Can't connect to the endopint,waiting" + new Float(connector.getReconnectMillisecs() / 1000) + "seconds for reconnecting \r\n(" + exs + ")");
							try {
								Thread.sleep(connector.getReconnectMillisecs());
							} catch (Throwable t) {
								exceptionMessage = "Unable to send message. Too many retries";
								logger.error("Sending interrupption. Payload not sent");
								retryCount = maxRetries + 1;
								exceptionWriting = exs;
							}
						} else {
							exceptionMessage = "Unable to connect to destination";
							logger.error("Can't connect to the endopint: payload not sent");
							exceptionWriting = exs;

						}
					}
				}
			}
		} catch (Exception exu) {
			exceptionMessage = exu.getMessage();
			alertController.sendAlerts(((TcpConnector) connector).getChannelId(), Constants.ERROR_411, null, exu);
			logger.error("Unknown exception dispatching " + exu);
			exceptionWriting = exu;
		} finally {

		}
		if (!success) {
			messageObjectController.setError(messageObject, Constants.ERROR_411, exceptionMessage, exceptionWriting);
			alertController.sendAlerts(((TcpConnector) connector).getChannelId(), Constants.ERROR_411, exceptionMessage, exceptionWriting);
		}
		if (success && (exceptionWriting == null)) {
			manageResponseAck(socket, endpointUri, messageObject);
			if (!connector.isKeepSendSocketOpen()) {
				monitoringController.updateStatus(connector, connectorType, Event.DISCONNECTED, socket);
				doDispose();
			}
		}
	}


	protected void createSocket(Socket socket, InetSocketAddress inetAddress) throws IOException {
		socket.connect(inetAddress, connector.getReconnectMillisecs());
	}
	//TODO: Remove these two write methods after 1.7 beta
	/* Deprecated? 1.7
	protected void write(Socket socket, byte[] data) throws IOException {
		TcpProtocol protocol = connector.getTcpProtocol();
		BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
		protocol.write(bos, data);
		bos.flush();
	}

	protected void write(Socket socket, MessageObject messageObject) throws Exception {
		byte[] data = messageObject.getEncodedData().getBytes(connector.getCharsetEncoding());
		TcpProtocol protocol = connector.getTcpProtocol();
		BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
		protocol.write(bos, data);
		bos.flush();
	}
*/
	protected void write(Socket socket, String data) throws Exception {
		byte[] buffer = null;
		//When working with binary data the template has to be base64 encoded
		if (connector.isBinary()) {
			BASE64Decoder base64 = new BASE64Decoder();
			buffer = base64.decodeBuffer(data);
		} else {
			buffer = data.getBytes(connector.getCharsetEncoding());
		}
		TcpProtocol protocol = connector.getTcpProtocol();
		BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
		protocol.write(bos, buffer);
		bos.flush();
	}

	// ast: split the doSend code into three functions: sendPayload (sending)
	// and doTheRemoteSyncStuff (for remote-sync)

	public UMOMessage doSend(UMOEvent event) throws Exception {
		doDispatch(event);
		return event.getMessage();
	}


	// ast: sendPayload is called from the doSend method, or from
	// MessageResponseQueued
	public boolean sendPayload(QueuedMessage thePayload) throws Exception {
		Boolean result = false;
		Exception sendException = null;
		Socket socket = null;
		String host = replacer.replaceURLValues(thePayload.getEndpointUri().toString(), thePayload.getMessageObject());

		try {
			if (!connector.isKeepSendSocketOpen()) {
				socket = initSocket(host);
				writeTemplatedData(socket, thePayload.getMessageObject());
				result = true;
			} else {
				socket = connectedSockets.get(host);
				if (socket != null && !socket.isClosed()) {
					writeTemplatedData(socket, thePayload.getMessageObject());
					result = true;
				} else {
					socket = initSocket(host);
					writeTemplatedData(socket, thePayload.getMessageObject());
					result = true;
				}
			}
		} catch (IOException e) {
			logger.warn("Write raised exception: '" + e.getMessage() + "' attempting to reconnect.");
			try {
				if (socket != null) {
					monitoringController.updateStatus(connector, connectorType, Event.DISCONNECTED, socket);
					socket.close();
					connectedSockets.values().remove(socket);
				}
				if (reconnect(host, connector.getMaxRetryCount())) {
					socket = connectedSockets.get(host);
					if (socket != null) {
						writeTemplatedData(socket, thePayload.getMessageObject());
						result = true;
					}
				}
			} catch (Exception ers) {
				logger.warn("Write raised exception: '" + e.getMessage() + "' ceasing reconnecting.");
				sendException = ers;
			}
		} catch (Exception e) {
			sendException = e;
		} finally {

		}

		if ((result == false) || (sendException != null)) {
			if (sendException != null) {
				messageObjectController.setError(thePayload.getMessageObject(), Constants.ERROR_408, "Socket write exception", sendException);
				throw sendException;
			}
			return result;
		}
		// If we have reached this point, the conections has been fine
		manageResponseAck(socket, thePayload.getEndpointUri().toString(), thePayload.getMessageObject());
		if (!connector.isKeepSendSocketOpen()) {
			doDispose(socket);
		}
		return result;
	}

	private void writeTemplatedData(Socket socket, MessageObject data) throws Exception {
		monitoringController.updateStatus(connector, connectorType, Event.BUSY, socket);
		if (!connector.getTemplate().equals("")) {
			String template = replacer.replaceValues(connector.getTemplate(), data);
			write(socket, template);
		} else {
			write(socket, data.getEncodedData());
		}
		monitoringController.updateStatus(connector, connectorType, Event.DONE, socket);
	}


	public void manageResponseAck(Socket socket, String endpointUri, MessageObject messageObject) {
		int maxTime = connector.getAckTimeout();
		if (maxTime <= 0) { // TODO: Either make a UI setting to "not check for
			// ACK" or document this
			// We aren't waiting for an ACK
			messageObjectController.setSuccess(messageObject, "Message successfully sent");
			return;
		}
		byte[] theAck = getAck(socket, endpointUri);

		if (theAck == null) {
			// NACK
			messageObjectController.setSuccess(messageObject, "Empty Response");
			return;
		}
		try {
			String ackString = new String(theAck, connector.getCharsetEncoding());
			if (connector.getReplyChannelId() != null & !connector.getReplyChannelId().equals("") && !connector.getReplyChannelId().equals("sink")) {
				// reply back to channel
				VMRouter router = new VMRouter();
				router.routeMessageByChannelId(connector.getReplyChannelId(), ackString, true, true);
			}
			messageObjectController.setSuccess(messageObject, ackString);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
			messageObjectController.setError(messageObject, Constants.ERROR_411, "Error setting encoding: " + connector.getCharsetEncoding(), e);
            alertController.sendAlerts(((TcpConnector) connector).getChannelId(), Constants.ERROR_411, "Error setting encoding: " + connector.getCharsetEncoding(), e);
		}
	}

	public byte[] getAck(Socket socket, String endpointUri) {
		int maxTime = connector.getAckTimeout();
		if (maxTime == 0)
			return null;
		try {
			byte[] result = receive(socket, maxTime);
			if (result == null) {
				return null;
			}
			return result;
		} catch (SocketTimeoutException e) {
			// we don't necessarily expect to receive a response here
			logger.warn("Socket timed out normally while doing a synchronous receive on endpointUri: " + endpointUri);
			return null;
		} catch (Exception ex) {
			logger.info("Socket error while doing a synchronous receive on endpointUri: " + endpointUri);
			return null;
		}
	}

	protected byte[] receive(Socket socket, int timeout) throws IOException {
		DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		if (timeout >= 0) {
			socket.setSoTimeout(timeout);
		}
		return connector.getTcpProtocol().read(dis);
	}

	public UMOMessage receive(UMOEndpointURI endpointUri, long timeout) throws Exception {
		Socket socket = null;
		try {
			socket = initSocket(endpointUri.getAddress());
			try {
				byte[] result = receive(socket, (int) timeout);
				if (result == null) {
					return null;
				}
				UMOMessage message = new MuleMessage(connector.getMessageAdapter(result));
				return message;
			} catch (SocketTimeoutException e) {
				// we dont necesarily expect to receive a resonse here
				logger.info("Socket timed out normally while doing a synchronous receive on endpointUri: " + endpointUri);
				return null;
			}
		} finally {
			if (socket != null && !socket.isClosed()) {
				socket.close();
			}
		}
	}

	public Object getDelegateSession() throws UMOException {
		return null;
	}

	public UMOConnector getConnector() {
		return connector;
	}
	
	public void doDispose() {
		for (Socket connectedSocket : connectedSockets.values()) {
			if (null != connectedSocket && !connectedSocket.isClosed()) {
				try {
					connectedSocket.close();
					connectedSockets.values().remove(connectedSocket);
					connectedSocket = null;
				} catch (IOException e) {
					logger.debug("ConnectedSocked close raised exception. Reason: " + e.getMessage());
				}
			}
		}
	}

	public void doDispose(Socket socket) {
		monitoringController.updateStatus(connector, connectorType, Event.DISCONNECTED, socket);
		if (null != socket && !socket.isClosed()) {
			try {
				socket.close();
				connectedSockets.values().remove(socket);
				socket = null;
			} catch (IOException e) {
				logger.debug("ConnectedSocked close raised exception. Reason: " + e.getMessage());
			}
		}

	}

//	 ///////////////////////////////////////////////////////////////
	// New keepSocketOpen option methods by P.Oikari
	// ///////////////////////////////////////////////////////////////
	public boolean reconnect(String endpoint, int maxRetries) throws Exception {
		if (connectedSockets.containsKey(endpoint) && !connectedSockets.get(endpoint).isClosed()) {
			// We already have a connected socket
			return true;
		}

		boolean success = false;

		int retryCount = -1;

		while (!success && !disposed && (retryCount < maxRetries)) {
			try {
				// ast: we now work with the endpoint
				initSocket(endpoint);
				success = true;
				connector.setSendSocketValid(true);
			} catch (Exception e) {
				success = false;

				connector.setSendSocketValid(false);

				if (maxRetries != MllpConnector.KEEP_RETRYING_INDEFINETLY) {
					retryCount++;
				}
				// ast: we now work with the endpoint
				logger.debug("run() warning at host: '" + endpoint + "'. Reason: " + e.getMessage());

				if (retryCount < maxRetries) {
					try {
						Thread.sleep(connector.getReconnectMillisecs());
					} catch (Exception ex) {
						logger.debug("SocketConnector threadsleep interrupted. Reason: " + ex.getMessage());
					}
				} else {
					throw e;
				}
			}
		}

		return (success);
	}
}
