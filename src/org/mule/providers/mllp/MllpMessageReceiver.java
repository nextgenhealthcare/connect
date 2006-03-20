/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package org.mule.providers.mllp;

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
import java.net.URI;

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
import org.mule.umo.transformer.UMOTransformer;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedBoolean;

public class MllpMessageReceiver extends AbstractMessageReceiver implements Work {
	protected ServerSocket serverSocket = null;
	protected UMOTransformer responseTransformer = null;

	public MllpMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint) throws InitialisationException {
		super(connector, component, endpoint);
		responseTransformer = getResponseTransformer();
	}

	protected UMOTransformer getResponseTransformer() throws InitialisationException {
		UMOTransformer transformer = component.getDescriptor().getResponseTransformer();
		if (transformer == null) {
			return connector.getDefaultResponseTransformer();
		}
		return transformer;
	}

	public void doConnect() throws ConnectException {
		disposing.set(false);
		URI uri = endpoint.getEndpointURI().getUri();
		try {
			serverSocket = createSocket(uri);
		} catch (Exception e) {
			throw new org.mule.providers.ConnectException(new Message("mllp", 1, uri), e, this);
		}

		try {
			getWorkManager().scheduleWork(this, WorkManager.INDEFINITE, null, null);
		} catch (WorkException e) {
			throw new ConnectException(new Message(Messages.FAILED_TO_SCHEDULE_WORK), e, this);
		}
	}

	public void doDisconnect() throws ConnectException {
		// this will cause the server thread to quit
		disposing.set(true);
		try {
			serverSocket.close();
		} catch (IOException e) {
			logger.warn("Failed to close server socket: " + e.getMessage(), e);
		}
	}

	protected ServerSocket createSocket(URI uri) throws Exception {
		String host = uri.getHost();
		InetAddress inetAddress = null;
		int backlog = ((MllpConnector) connector).getBacklog();
		if (host == null || host.length() == 0) {
			host = "localhost";
		}
		inetAddress = InetAddress.getByName(host);
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
					MllpConnector connector = (MllpConnector) this.connector;
					socket.setReceiveBufferSize(connector.getBufferSize());
					socket.setSendBufferSize(connector.getBufferSize());
					socket.setSoTimeout(connector.getTimeout());
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
					Work work = null;
					try {
						work = createWork(socket);
						try {
							getWorkManager().scheduleWork(work, WorkManager.INDEFINITE, null, null);
						} catch (WorkException e) {
							logger.error("Tcp Server receiver Work was not processed: "
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
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
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
		protected SynchronizedBoolean closed = new SynchronizedBoolean(false);
		protected TcpProtocol protocol;

		public TcpWorker(Socket socket) {
			this.socket = socket;
			this.protocol = ((MllpConnector) connector).getTcpProtocol();
		}

		public void release() {
			dispose();
		}

		public void dispose() {
			closed.set(true);
			try {
				if (socket != null) {
					logger.debug("Closing listener: " + socket.getInetAddress());
					socket.shutdownInput();
					socket.shutdownOutput();
					socket.close();
				}
			} catch (IOException e) {
				logger.error("Socket close failed with: " + e);
			}
		}

		/**
		 * Accept requests from a given TCP port
		 */
		public void run() {
			try {
				dataIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
				dataOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

				while (!socket.isClosed() && !disposing.get()) {

					byte[] b = protocol.read(dataIn);
					// end of stream
					if (b == null) {
						break;
					}

					byte[] result = processData(b);
					if (result != null) {
						protocol.write(dataOut, result);
					}
					dataOut.flush();
				}
			} catch (Exception e) {
				handleException(e);
			} finally {
				dispose();
			}
		}

		// NOTE: main work is done here
		protected byte[] processData(byte[] data) throws Exception {
			// Check the data for the SOB/EOR chars to delim. HL7 messages
			String source = new String(data);
			source = source.replaceAll(MllpConstants.START_OF_MESSAGE + "", "");
			String[] messages = source.split(MllpConstants.END_OF_MESSAGE + "");
			
			for (int i = 0; i < messages.length; i++) {
				UMOMessageAdapter adapter = connector.getMessageAdapter(messages[i].getBytes());
				routeMessage(new MuleMessage(adapter));

			}

			return null;
		}

		protected byte[] sendUMOMessage(byte[] data) throws Exception {
			UMOMessageAdapter adapter = connector.getMessageAdapter(data);
			OutputStream os = new ResponseOutputStream(socket.getOutputStream(), socket);
			UMOMessage returnMessage = routeMessage(new MuleMessage(adapter), endpoint.isSynchronous(), os);
			if (returnMessage != null) {
				if (responseTransformer != null) {
					Object response = responseTransformer.transform(returnMessage.getPayload());
					if (response instanceof byte[]) {
						return (byte[]) response;
					} else {
						return response.toString().getBytes();
					}
				} else {
					return returnMessage.getPayloadAsBytes();
				}
			} else {
				return null;
			}

		}

	}
}
