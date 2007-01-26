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
package org.mule.providers.tcp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.TemplateValueReplacer;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.provider.UMOConnector;
import org.mule.util.Utility;
import org.mule.util.queue.Queue;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.controllers.ChannelController;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.mule.util.BatchMessageProcessor;
import com.webreach.mirth.server.util.StackTracePrinter;
import com.webreach.mirth.server.util.UUIDGenerator;

/**
 * f <code>TcpMessageDispatcher</code> will send transformed mule events over
 * tcp.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @author <a href="mailto:tsuppari@yahoo.co.uk">P.Oikari</a>
 * 
 * @version $Revision: 1.12 $
 */

public class TcpMessageDispatcher extends AbstractMessageDispatcher {
	// ///////////////////////////////////////////////////////////////
	// keepSocketOpen option variables
	// ///////////////////////////////////////////////////////////////

	protected Socket connectedSocket = null;

	// ast:queue variables
	protected Queue queue = null;
	protected Queue errorQueue = null;

	// ///////////////////////////////////////////////////////////////
	/**
	 * logger used by this class
	 */
	protected static transient Log logger = LogFactory.getLog(TcpMessageDispatcher.class);

	private TcpConnector connector;

	private MessageObjectController messageObjectController = new MessageObjectController();
	private TemplateValueReplacer replacer = new TemplateValueReplacer();
	public TcpMessageDispatcher(TcpConnector connector) {
		super(connector);
		this.connector = connector;
	}

	// ast: set queues
	public void setQueues(UMOEndpoint endpoint) {
		// connect to the queues
		this.queue = null;
		this.errorQueue = null;

		if (connector.isUsePersistentQueues() && (endpoint != null)) {
			try {
				this.queue = connector.getQueue(endpoint);
				this.errorQueue = connector.getErrorQueue(endpoint);
			} catch (Exception e) {
				logger.error("Error setting queues to the endpoint" + e);
			}
		}
	}

	protected Socket initSocket(String endpoint) throws IOException, URISyntaxException {
		URI uri = new URI(endpoint);
		int port = uri.getPort();
		InetAddress inetAddress = InetAddress.getByName(uri.getHost());
		Socket socket = createSocket(port, inetAddress);
		socket.setReuseAddress(true);
		socket.setReceiveBufferSize(connector.getBufferSize());
		socket.setSendBufferSize(connector.getBufferSize());
		socket.setSoTimeout(connector.getSendTimeout());
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
		MessageObject messageObject = null;

		this.setQueues(event.getEndpoint());
		try {
			payload = event.getTransformedMessage();
			if (payload instanceof MessageObject){
				messageObject = (MessageObject) payload;
				if (messageObject.getStatus().equals(MessageObject.Status.REJECTED)){
					return;
				}
				if (messageObject.getCorrelationId() == null){
					//If we have no correlation id, this means this is the original message
					//so let's copy it and assign a new id and set the proper correlationid
					messageObject = messageObjectController.cloneMessageObjectForBroadcast(messageObject, this.getConnector().getName());
				}
				//we only want the encoded data
				//payload = messageObject.getEncodedData();
			}
		} catch (Exception et) {
			logger.error("Error in transformation " + et);
			payload = null;
			throw et;
		}

		// ast: now, the stuff for queueing (and re-try)
		if (messageObject == null)
			return;
		try {
			//The status should be queued, even if we are retrying
			if (messageObject != null) {
				messageObject.setStatus(MessageObject.Status.QUEUED);
				messageObjectController.updateMessage(messageObject);
			}
			if (queue != null) {
				try {

					queue.put(messageObject);
					return;
				} catch (Exception exq) {
					logger.error("Cant save payload to the queue\r\n\t " + exq);
					exceptionWriting = exq;
					success = false;
				}
			} else {
				int retryCount = -1;
				int maxRetries = connector.getMaxRetryCount();

				while (!success && !disposed && (retryCount < maxRetries)) {
					retryCount++;
					try {
						socket = initSocket(event.getEndpoint().getEndpointURI().getAddress());
						writeTemplatedData(socket, messageObject);
						success = true;
					} catch (Exception exs) {
						if (retryCount < maxRetries) {
							logger.warn("Can't connect to the endopint,waiting" + new Float(connector.getReconnectMillisecs() / 1000) + "seconds for reconnecting \r\n(" + exs + ")");
							try {
								Thread.sleep(connector.getReconnectMillisecs());
							} catch (Throwable t) {
								logger.error("Sending interrupption. Payload not sent");
								retryCount = maxRetries + 1;
								exceptionWriting = exs;
							}
						} else {
							logger.error("Can't connect to the endopint: payload not sent");
							exceptionWriting = exs;
							
						}
					}
				}
			}
		} catch (Exception exu) {
			logger.error("Unknown exception dispatching " + exu);
			exceptionWriting = exu;
		}
		if (!success) {
			if (messageObject != null) {
				messageObject.setStatus(MessageObject.Status.ERROR);
				messageObject.setErrors(messageObject.getErrors() != null ? messageObject.getErrors() + '\n' : "" +  "Can't connect to the endpoint\n" + StackTracePrinter.stackTraceToString(exceptionWriting));
				messageObjectController.updateMessage(messageObject);
				connector.incErrorStatistics(event.getComponent());
			}
		}
		if (success && (exceptionWriting == null)) {
			manageResponseAck(socket, event.getEndpoint(), messageObject);
		}
		if (socket != null && !socket.isClosed()) {
			socket.close();
		}
		// if (exceptionWriting!=null) throw exceptionWriting;
	}

	protected Socket createSocket(int port, InetAddress inetAddress) throws IOException {
		return new Socket(inetAddress, port);
	}

	protected void write(Socket socket, Object data) throws IOException {

		TcpProtocol protocol = connector.getTcpProtocol();
		byte[] binaryData;

		if (data instanceof String) {
			//ast: encode using the selected encoding
			binaryData = ((String)data).getBytes(connector.getCharsetEncoding());
		} else if (data instanceof byte[]) {
			binaryData = (byte[]) data;
		} else if (data instanceof MessageObject) {
			MessageObject messageObject = (MessageObject) data;

			if (messageObject.getStatus().equals(MessageObject.Status.REJECTED)) {
				logger.warn("message marked as rejected");
				return;
			}
			//ast: encode using the selected encoding
			binaryData = messageObject.getEncodedData().getBytes(connector.getCharsetEncoding());
		} else {
			binaryData = Utility.objectToByteArray(data);
		}

		BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
		protocol.write(bos, binaryData);
		bos.flush();
		
		// update the message status to sent
		if (data instanceof MessageObject) {
			MessageObject messageObject = (MessageObject) data;
			messageObject.setStatus(MessageObject.Status.SENT);
			messageObjectController.updateMessage(messageObject);
		}

	}

	// ast: split the doSend code into three functions: sendPayload (sending)
	// and doTheRemoteSyncStuff (for remote-sync)

	public UMOMessage doSend(UMOEvent event) throws Exception {
		Object data = null;
		MessageObject messageObject = null;
		this.setQueues(event.getEndpoint());
		try {
			data = event.getTransformedMessage();
			if (!connector.isKeepSendSocketOpen())
				doDispose();
		} catch (Exception e) {
			logger.error("Error at transformation: " + e);
			throw e;
		}
		if (data == null)
			return null;
		if (data instanceof MessageObject) {
			messageObject = (MessageObject) data;
			if (messageObject.getStatus().equals(MessageObject.Status.REJECTED)){
				return null;
			}
			if (messageObject.getCorrelationId() == null){
				//If we have no correlation id, this means this is the original message
				//so let's copy it and assign a new id and set the proper correlationid
				MessageObject clone = (MessageObject) messageObject.clone();
				clone.setId(UUIDGenerator.getUUID());
				clone.setDateCreated(Calendar.getInstance());
				clone.setCorrelationId(messageObject.getId());
				messageObject = clone;
			}
			//We don't want to send the actual MO, just the encoded data
			//data = messageObject.getEncodedData();
		}
		

		try {
			if (queue != null) {
				queue.put(messageObject);
			} else {
				sendPayload(messageObject, event.getEndpoint());
			}
		} catch (Exception e) {
			logger.error("Error sending: " + e);
			if (messageObject != null) {
				messageObject.setStatus(MessageObject.Status.ERROR);
				messageObject.setErrors(messageObject.getErrors() != null ? messageObject.getErrors() + '\n' : "" +  "Can't connect to the endpoint " + e);
				messageObjectController.updateMessage(messageObject);
			}
			throw e;
		}
		// update the message status to sent
		if (messageObject != null) {
			messageObject.setStatus(MessageObject.Status.SENT);
			messageObjectController.updateMessage(messageObject);
		}

		if (useRemoteSync(event)) {
			return doTheRemoteSyncStuff(connectedSocket, event.getEndpoint());
		} else {
			return event.getMessage();
		}

	}

	// ast: sendPayload is called from the doSend method, or from
	// MessageResponseQueued
	public boolean sendPayload(MessageObject data, UMOEndpoint endpoint) throws Exception {
		Boolean result = false;
		Exception sendException = null;
		if (this.queue == null)
			this.queue = connector.getQueue(endpoint);
		try {
			if (!connector.isKeepSendSocketOpen()) {
				try {
					connectedSocket = initSocket(endpoint.getEndpointURI().getAddress());
				} catch (Throwable tnf) {
					connectedSocket = null;
				}
			}
			// reconnect(endpoint, connector.getMaxRetryCount());
			result = reconnect(endpoint, 1);
			if (!result)
				return result;
			try {
				//Send the templated data
				writeTemplatedData(connectedSocket, data);
				result = true;
				// If we're doing sync receive try and read return info from
				// socket
			} catch (IOException e) {
				logger.warn("Write raised exception: '" + e.getMessage() + "' attempting to reconnect.");
				doDispose();
				try {
					if (reconnect(endpoint, connector.getMaxRetryCount())) {
						write(connectedSocket, data);
						result = true;
					}
				} catch (Exception ers) {
					logger.warn("Write raised exception: '" + e.getMessage() + "' ceasing reconnecting.");
					sendException = ers;
				}
			}
		} catch (Exception e) {
			logger.warn("Write raised exception: '" + e.getMessage() + "' desisting reconnecting.");
			sendException = e;
		}
		if ((result == false) || (sendException != null)) {
			if (sendException != null)
				throw sendException;
			return result;
		}

		// If we have reached this point, the conections has been fine
		manageResponseAck(connectedSocket, endpoint, data);
		if (!connector.isKeepSendSocketOpen()) {
			doDispose();
		}
		return result;
	}

	private void writeTemplatedData(Socket socket, MessageObject data) throws IOException {
		if (connector.getTemplate() != ""){
			String template = replacer.replaceValues(connector.getTemplate(), data, "llp");
			write(socket, template);
		}else{
			write(socket, data.getEncodedData());
		}
	}

	// ast: for sync
	public UMOMessage doTheRemoteSyncStuff(UMOEndpoint endpoint) {
		return doTheRemoteSyncStuff(connectedSocket, endpoint);
	}

	public void manageResponseAck(Socket socket, UMOEndpoint endpoint, MessageObject messageObject) {

		int maxTime = connector.getAckTimeout();
		if (maxTime <= 0){
			messageObject.setStatus(MessageObject.Status.SENT);
			messageObjectController.updateMessage(messageObject);
		}
		byte[] theAck = getAck(socket, endpoint);

		if (theAck == null) {
			if (messageObject != null) {
				// NACK
				messageObject.setStatus(MessageObject.Status.ERROR);
				messageObject.setErrors(messageObject.getErrors() != null ? messageObject.getErrors() + '\n' : "" + "Timeout waiting for the ACK");
				messageObjectController.updateMessage(messageObject);
				connector.incErrorStatistics();
			}
			return;
		}
		String ackString = null;
		try {
			ackString = processResponseData(theAck);

		} catch (Throwable t) {
			logger.error("Error processing the Ack" + t);
		}
		if (ackString == null) {
			if (messageObject != null) {
				// //NACK
				messageObject.setStatus(MessageObject.Status.ERROR);
				messageObject.setErrors(messageObject.getErrors() != null ? messageObject.getErrors() + '\n' : "" +  "ACK message violates LLP protocol");
				messageObjectController.updateMessage(messageObject);
				connector.incErrorStatistics();
			}
			return;
		}
		ResponseAck rack = new ResponseAck(ackString);

		if (rack.getTypeOfAck()) {// Ack Ok
			if (messageObject != null) {
				messageObject.setStatus(MessageObject.Status.SENT);
				messageObjectController.updateMessage(messageObject);
			}
		} else {
			if (messageObject != null) {
				// NACK
				messageObject.setStatus(MessageObject.Status.ERROR);
				messageObject.setErrors(messageObject.getErrors() != null ? messageObject.getErrors() + '\n' : "" +   "NACK sent from the receiver:\n" + rack.getErrorDescription());
				messageObjectController.updateMessage(messageObject);
				connector.incErrorStatistics();
			}
		}

	}

	public byte[] getAck(Socket socket, UMOEndpoint endpoint) {

		int maxTime = endpoint.getRemoteSyncTimeout();
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
			logger.warn("Socket timed out normally while doing a synchronous receive on endpointUri: " + endpoint.getEndpointURI());
			return null;
		} catch (Exception ex) {
			logger.info("Socket error while doing a synchronous receive on endpointUri: " + endpoint.getEndpointURI());
			return null;
		}
	}

	// ast:for syncronous
	public UMOMessage doTheRemoteSyncStuff(Socket socket, UMOEndpoint endpoint) {
		try {
			byte[] result = receive(socket, endpoint.getRemoteSyncTimeout());
			if (result == null) {
				return null;
			}
			return new MuleMessage(connector.getMessageAdapter(result));
		} catch (SocketTimeoutException e) {
			// we don't necessarily expect to receive a response here
			logger.info("Socket timed out normally while doing a synchronous receive on endpointUri: " + endpoint.getEndpointURI());
			return null;
		} catch (Exception ex) {
			logger.info("Socket error while doing a synchronous receive on endpointUri: " + endpoint.getEndpointURI());
			return null;
		}
	}

	// Function similar to the Receiver
	protected String processResponseData(byte[] data) throws Exception {

		char START_MESSAGE, END_MESSAGE, END_OF_RECORD, END_OF_SEGMENT;

		if (connector.getCharEncoding().equals("hex")) {
			START_MESSAGE = (char) Integer.decode(connector.getMessageStart()).intValue();
			END_MESSAGE = (char) Integer.decode(connector.getMessageEnd()).intValue();
			END_OF_RECORD = (char) Integer.decode(connector.getRecordSeparator()).intValue();
			END_OF_SEGMENT = (char) Integer.decode(connector.getSegmentEnd()).intValue();
		} else {
			START_MESSAGE = connector.getMessageStart().charAt(0);
			END_MESSAGE = connector.getMessageEnd().charAt(0);
			END_OF_RECORD = connector.getRecordSeparator().charAt(0);
			END_OF_SEGMENT = connector.getSegmentEnd().charAt(0);
		}

		// The next lines, removes any '\n' (decimal code 10 or 0x0A) in the
		// message
		byte[] bites = data;
		int destPointer = 0, srcPointer = 0;
		for (destPointer = 0, srcPointer = 0; srcPointer < bites.length; srcPointer++) {
			if (bites[srcPointer] != 10) {
				bites[destPointer] = bites[srcPointer];
				destPointer++;
			}
		}
		data = bites;
                //ast: use the user's encoding
		String str_data = new String(data, 0, destPointer,connector.getCharsetEncoding());

		BatchMessageProcessor batchProcessor = new BatchMessageProcessor();
		batchProcessor.setEndOfMessage((byte) END_MESSAGE);
		batchProcessor.setStartOfMessage((byte) START_MESSAGE);
		batchProcessor.setEndOfRecord((byte) END_OF_RECORD);
		Iterator<String> it = batchProcessor.processHL7Messages(str_data).iterator();
		UMOMessage returnMessage = null;
		if (it.hasNext()) {
			data = (it.next()).getBytes();
			return new String(data);
		}
		return null;
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
		if (null != connectedSocket && !connectedSocket.isClosed()) {
			try {
				connectedSocket.close();

				connectedSocket = null;
			} catch (IOException e) {
				logger.warn("ConnectedSocked close raised exception. Reason: " + e.getMessage());
			}
		}
	}

	// ///////////////////////////////////////////////////////////////
	// New keepSocketOpen option methods by P.Oikari
	// ///////////////////////////////////////////////////////////////	
	public boolean reconnect(UMOEndpoint endpoint, int maxRetries) throws Exception {
		if (null != connectedSocket) {
			// We already have a connected socket
			return true;
		}

		boolean success = false;

		int retryCount = -1;

		while (!success && !disposed && (retryCount < maxRetries)) {
			try {
				// ast: we now work with the endpoint
				connectedSocket = initSocket(endpoint.getEndpointURI().getAddress());

				success = true;

				connector.setSendSocketValid(true);
			} catch (Exception e) {
				success = false;

				connector.setSendSocketValid(false);

				if (maxRetries != TcpConnector.KEEP_RETRYING_INDEFINETLY) {
					retryCount++;
				}
				// ast: we now work with the endpoint
				logger.warn("run() warning at host: '" + endpoint.getEndpointURI().getAddress() + "'. Reason: " + e.getMessage());

				if (retryCount < maxRetries) {
					try {
						Thread.sleep(connector.getReconnectMillisecs());
					} catch (Exception ex) {
						logger.warn("SocketConnector threadsleep interrupted. Reason: " + ex.getMessage());
					}
				} else {
					throw e;
				}
			}
		}

		return (success);
	}
}
