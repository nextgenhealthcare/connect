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
package com.webreach.mirth.server.mule.providers.mllp;

import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;

import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleMessage;
import org.mule.impl.ResponseOutputStream;
import org.mule.providers.AbstractMessageReceiver;
import org.mule.providers.ConnectException;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.Disposable;
import org.mule.umo.lifecycle.DisposeException;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageAdapter;

import ca.uhn.hl7v2.parser.EncodingCharacters;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

import com.webreach.mirth.model.MessageObject;
import org.mule.providers.tcp.protocols.*;

import com.webreach.mirth.server.mule.providers.mllp.protocols.LlpProtocol;
import com.webreach.mirth.server.mule.util.BatchMessageProcessor;
import com.webreach.mirth.server.util.StackTracePrinter;
import com.webreach.mirth.util.ACKGenerator;

/**
 * <code>TcpMessageReceiver</code> acts like a tcp server to receive socket
 * requests.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @author <a href="mailto:tsuppari@yahoo.co.uk">P.Oikari</a>
 * 
 * @version $Revision: 1.23 $
 */
public class MllpMessageReceiver extends AbstractMessageReceiver implements Work {
	protected ServerSocket serverSocket = null;
	private char END_MESSAGE = 0x1C;    // character indicating end of message
	private char START_MESSAGE = 0x0B;  // first character of a new message
	private char END_OF_RECORD = 0x0D; // character sent between messages
	private char END_OF_SEGMENT = 0x0D; // character sent between hl7 segments (usually same as end of record)
	private MllpConnector connector;
	public MllpMessageReceiver(UMOConnector connector, UMOComponent component,
			UMOEndpoint endpoint) throws InitialisationException {
		super(connector, component, endpoint);
		MllpConnector tcpConnector = (MllpConnector)connector;
		this.connector = tcpConnector;
		if (tcpConnector.getCharEncoding().equals("hex")){
			START_MESSAGE = (char)Integer.decode(tcpConnector.getMessageStart()).intValue();
			END_MESSAGE = (char)Integer.decode(tcpConnector.getMessageEnd()).intValue();
			END_OF_RECORD = (char)Integer.decode(tcpConnector.getRecordSeparator()).intValue();
			END_OF_SEGMENT = (char)Integer.decode(tcpConnector.getSegmentEnd()).intValue();
		}else{
			//TODO: Ensure this is unit-tested
			START_MESSAGE = tcpConnector.getMessageStart().charAt(0);
			END_MESSAGE = tcpConnector.getMessageEnd().charAt(0);
			END_OF_RECORD = tcpConnector.getRecordSeparator().charAt(0);
			END_OF_SEGMENT = tcpConnector.getSegmentEnd().charAt(0);
		}
	}

	public void doConnect() throws ConnectException {
		disposing.set(false);
		URI uri = endpoint.getEndpointURI().getUri();
		try {
			serverSocket = createSocket(uri);
		} catch (Exception e) {
			throw new org.mule.providers.ConnectException(new Message("tcp", 1,
					uri), e, this);
		}

		try {
			getWorkManager().scheduleWork(this, WorkManager.INDEFINITE, null,
					null);
		} catch (WorkException e) {
			throw new ConnectException(new Message(
					Messages.FAILED_TO_SCHEDULE_WORK), e, this);
		}
	}

	public void doDisconnect() throws ConnectException {
		// this will cause the server thread to quit
		disposing.set(true);
		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
		} catch (IOException e) {
			logger.warn("Failed to close server socket: " + e.getMessage(), e);
		}
	}

	protected ServerSocket createSocket(URI uri) throws Exception {
		String host = uri.getHost();
		int backlog = connector.getBacklog();
		if (host == null || host.length() == 0) {
			host = "localhost";
		}
		InetAddress inetAddress = InetAddress.getByName(host);
		if (inetAddress.equals(InetAddress.getLocalHost())
				|| inetAddress.isLoopbackAddress()
				|| host.trim().equals("localhost")) {
			return new ServerSocket(uri.getPort(), backlog);
		} else {
			return new ServerSocket(uri.getPort(), backlog, inetAddress);
		}
	}

	/**
	 * Obtain the serverSocket
	 */
	public ServerSocket getServerSocket() {
		return serverSocket;
	}

	public void run() {
		while (!disposing.get()) {
			if (connector.isStarted() && !disposing.get()) {
				Socket socket = null;
				try {
					socket = serverSocket.accept();
					logger.trace("Server socket Accepted on: "
							+ serverSocket.getLocalPort());
				} catch (java.io.InterruptedIOException iie) {
					logger.debug("Interupted IO doing serverSocket.accept: "
							+ iie.getMessage());
				} catch (Exception e) {
					if (!connector.isDisposed() && !disposing.get()) {
						logger.warn("Accept failed on socket: " + e, e);
						handleException(new ConnectException(e, this));
					}
				}
				if (socket != null) {
					try {
						Work work = createWork(socket);
						try {
							getWorkManager().scheduleWork(work,
									WorkManager.IMMEDIATE, null, null);
						} catch (WorkException e) {
							logger.error(
									"Tcp Server receiver Work was not processed: "
											+ e.getMessage(), e);
						}
					} catch (SocketException e) {
						handleException(e);
					}

				}
			}
		}
	}

	public void release() {
	}

	public void doDispose() {
		try {
			if (serverSocket != null && !serverSocket.isClosed())
				serverSocket.close();
			serverSocket = null;

		} catch (Exception e) {
			logger.error(new DisposeException(new Message("tcp", 2), e));
		}
		logger.info("Closed Tcp port");
	}

	protected Work createWork(Socket socket) throws SocketException {
		return new TcpWorker(socket);
	}

	protected class TcpWorker implements Work, Disposable {
		protected Socket socket = null;

		protected DataInputStream dataIn;

		protected DataOutputStream dataOut;

		protected AtomicBoolean closed = new AtomicBoolean(false);

		protected LlpProtocol protocol;

		public TcpWorker(Socket socket) {
			this.socket = socket;

			final MllpConnector tcpConnector = connector;
			this.protocol = tcpConnector.getLlpProtocol();
			tcpConnector.updateReceiveSocketsCount(true);
			try {
				socket.setReceiveBufferSize(tcpConnector.getBufferSize());
				socket.setSendBufferSize(tcpConnector.getBufferSize());
				socket.setSoTimeout(tcpConnector.getReceiveTimeout());
				socket.setTcpNoDelay(true);
				socket.setKeepAlive(tcpConnector.isKeepAlive());
			} catch (SocketException e) {
				logger.error("Failed to set Socket properties: "
						+ e.getMessage(), e);
			}

			logger.info("TCP connection from "
					+ socket.getRemoteSocketAddress().toString() + " on port "
					+ socket.getLocalPort());
		}

		public void release() {
			dispose();
		}

		public void dispose() {
			closed.set(true);
			try {
				if (socket != null && !socket.isClosed()) {
					logger.debug("Closing listener: "
							+ socket.getLocalSocketAddress().toString());
					// socket.shutdownInput();
					// socket.shutdownOutput();
					socket.close();
				}
			} catch (IOException e) {
				logger.error("Socket close failed with: " + e);
			} finally {
				connector.updateReceiveSocketsCount(false);
			}
		}

		/**
		 * Accept requests from a given TCP port
		 */
		public void run() {
			try {
				dataIn = new DataInputStream(new BufferedInputStream(socket
						.getInputStream()));
				dataOut = new DataOutputStream(new BufferedOutputStream(socket
						.getOutputStream()));

				while (!socket.isClosed() && !disposing.get()) {

					byte[] b;
					try {
						b = protocol.read(dataIn);
						// end of stream
						if (b == null) {
							break;
						}

						processData(b);
						dataOut.flush();
					} catch (SocketTimeoutException e) {
						if (!socket.getKeepAlive()) {
							break;
						}
					}
				}
			} catch (Exception e) {
				handleException(e);
			} finally {
				dispose();
			}
		}

		protected byte[] processData(byte[] data) throws Exception {
			String charset=connector.getCharsetEncoding();
			String str_data = new String(data,charset);
			BatchMessageProcessor batchProcessor = new BatchMessageProcessor();
			batchProcessor.setEndOfMessage((byte)END_MESSAGE);
			batchProcessor.setStartOfMessage((byte)START_MESSAGE);
			batchProcessor.setEndOfRecord((byte)END_OF_RECORD);
			Iterator<String> it = batchProcessor.processHL7Messages(str_data).iterator();
			UMOMessage returnMessage = null;
			OutputStream os;
			while (it.hasNext()) {
				data = (it.next()).getBytes(charset);
				UMOMessageAdapter adapter = connector.getMessageAdapter(new String(data,charset));
				os = new ResponseOutputStream(socket.getOutputStream(), socket);
				try{
					returnMessage = routeMessage(new MuleMessage(adapter), endpoint
							.isSynchronous(), os);
					//We need to check the message status
					if (returnMessage != null && returnMessage instanceof MuleMessage){
						Object payload = returnMessage.getPayload();
						if (payload instanceof MessageObject){
							MessageObject messageObjectResponse = (MessageObject)payload;
							String errorString = "";
							if (connector.isResponseFromTransformer()){
								if (connector.isAckOnNewConnection()){
									String endpointURI = connector.getAckIP() + ":" + connector.getAckPort();
									Socket socket = initSocket("mllp://" + endpointURI);
									BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
									protocol.write(bos, ((String)messageObjectResponse.getVariableMap().get(MessageObject.RESPONSE_VARIABLE)).getBytes(connector.getCharsetEncoding()));
									bos.flush();
									bos.close();
								}else{
									protocol.write(os,((String)messageObjectResponse.getVariableMap().get(MessageObject.RESPONSE_VARIABLE)).getBytes(connector.getCharsetEncoding()));
									
								}
							}else{
								//we only want the first line
								if (messageObjectResponse.getStatus().equals(MessageObject.Status.ERROR) && messageObjectResponse.getErrors() != null){
									if (messageObjectResponse.getErrors().indexOf('\n') > -1){
										errorString = messageObjectResponse.getErrors().substring(0,messageObjectResponse.getErrors().indexOf('\n'));
									}else{
										errorString = messageObjectResponse.getErrors();
									}
								}
								generateACK(new String(data,charset), os, messageObjectResponse.getStatus(), errorString);
							}
						}
					}else{
						generateACK(new String(data,charset), os, MessageObject.Status.RECEIVED, new String());		
					}
				}catch(Exception e){
					generateACK(new String(data,charset), os, MessageObject.Status.ERROR, e.getMessage());		
					throw e;
				}finally{
					if (os != null)
						os.close();
				}
			}
			//The return message is always the last message routed if in a batch
			//TODO: Check this for 1.2.1
			if (returnMessage != null) {
				return returnMessage.getPayloadAsBytes();
			} else {
				return null;
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
		protected Socket createSocket(int port, InetAddress inetAddress) throws IOException {
			return new Socket(inetAddress, port);
		}

		private void generateACK(String message, OutputStream os, MessageObject.Status status, String error)
				throws Exception, IOException {
			boolean errorOnly = false;
			boolean always = false;
			boolean successOnly = false;
			if (error == null){
				error = "";
			}
			//Check if we want to send ACKs at all.
			if (connector.getSendACK()) {
				//Check if we have to look at MSH15
				if (connector.isCheckMSH15()){
					//MSH15 Dictionary:
					//AL: Always
					//NE: Never
					//ER: Error / Reject condition
					//SU: Successful completion only
					PipeParser parser = new PipeParser();
					parser.setValidationContext(new NoValidation());
					ca.uhn.hl7v2.model.Message hl7message = parser.parse(message.trim());
					Terser terser = new Terser(hl7message);
					String msh15 =  terser.get("/MSH-15-1");
					if (msh15 != null && !msh15.equals("")){
						if (msh15.equalsIgnoreCase("AL")){
							always = true;
						}else if(msh15.equalsIgnoreCase("NE")){
							logger.debug("MSH15 is NE, Skipping ACK");
							return;
						}else if(msh15.equalsIgnoreCase("ER")){
							errorOnly = true;

						}else if(msh15.equalsIgnoreCase("SU")){
							successOnly = true;
						}
					}
				}
				String ackCode = "AA";
				String textMessage = "";
				if (status.equals(MessageObject.Status.ERROR)){
					if (successOnly){
						//we only send an ACK on success
						return;
					}
					ackCode = connector.getAckCodeError();
					String conError = connector.getAckMsgError();
					textMessage = conError.replaceFirst("\\$\\{ERROR\\}", error);
				}else if(status.equals(MessageObject.Status.REJECTED)){
					if (successOnly){
						return;
					}
					ackCode = connector.getAckCodeRejected();
					textMessage = connector.getAckMsgRejected();
				}else{
					if (errorOnly){
						return;
					}
					ackCode = connector.getAckCodeSuccessful();
					textMessage = connector.getAckMsgSuccessful();
				}
				String ACK = new ACKGenerator().generateAckResponse(message.trim(), ackCode, textMessage);

				logger.debug("Sending ACK: " + ACK);
				try{
					if (connector.isAckOnNewConnection()){
						String endpointURI = connector.getAckIP() + ":" + connector.getAckPort();
						Socket socket = initSocket("mllp://" + endpointURI);
						BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
						protocol.write(bos, ACK.getBytes((connector).getCharsetEncoding()));
						bos.flush();
					}else{
						protocol.write(os,ACK.getBytes((connector).getCharsetEncoding()));
					}
				}catch(Throwable t){
					logger.error("Can't write ACK to the sender\n" + StackTracePrinter.stackTraceToString(t));
				}
			}
		}
	}
}
