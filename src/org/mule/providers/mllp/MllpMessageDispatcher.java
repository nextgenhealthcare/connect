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
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.impl.MuleMessage;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.provider.UMOConnector;
import org.mule.util.Utility;

public class MllpMessageDispatcher extends AbstractMessageDispatcher {
	/**
	 * logger used by this class
	 */
	protected static transient Log logger = LogFactory.getLog(MllpMessageDispatcher.class);

	private MllpConnector connector;

	public MllpMessageDispatcher(MllpConnector connector) {
		super(connector);
		this.connector = connector;
	}

	protected Socket initSocket(String endpoint) throws IOException, URISyntaxException {
		URI uri = new URI(endpoint);
		int port = uri.getPort();
		InetAddress inetAddress = InetAddress.getByName(uri.getHost());
		Socket socket = createSocket(port, inetAddress);
		socket.setReuseAddress(true);
		socket.setReceiveBufferSize(connector.getBufferSize());
		socket.setSendBufferSize(connector.getBufferSize());
		socket.setSoTimeout(connector.getTimeout());
		return socket;
	}

	public void doDispatch(UMOEvent event) throws Exception {
		Socket socket = null;
		try {
			Object payload = event.getTransformedMessage();
			socket = initSocket(event.getEndpoint().getEndpointURI().getAddress());
			write(socket, payload);
		} finally {
			if (socket != null && !socket.isClosed()) {
				socket.close();
			}
		}
	}

	protected Socket createSocket(int port, InetAddress inetAddress) throws IOException {
		return new Socket(inetAddress, port);
	}

	protected void write(Socket socket, Object data) throws IOException {
		TcpProtocol protocol = connector.getTcpProtocol();
		byte[] binaryData;
		if (data instanceof String) {
			// NOTE: this is to send message with MLLP encoding
			String stringData = MllpConstants.START_OF_MESSAGE + data.toString() + MllpConstants.END_OF_MESSAGE;
			binaryData = stringData.toString().getBytes();

//			binaryData = data.toString().getBytes();
		} else if (data instanceof byte[]) {
			// NOTE: this is to send message with MLLP encoding
			byte[] temp1 = (byte[]) data;
			byte[] temp2 = new byte[temp1.length + 2];
			temp2[0] = MllpConstants.START_OF_MESSAGE;
			temp2[temp2.length - 1] = MllpConstants.END_OF_MESSAGE;
			System.arraycopy(temp1, 0, temp2, 1, temp1.length);
			binaryData = temp2;

//			binaryData = (byte[]) data;
		} else {
			binaryData = Utility.objectToByteArray(data);
		}
		BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
		protocol.write(bos, binaryData);
		bos.flush();
	}

	public UMOMessage doSend(UMOEvent event) throws Exception {
		Socket socket = null;
		try {
			Object payload = event.getTransformedMessage();
			socket = initSocket(event.getEndpoint().getEndpointURI().getAddress());

			write(socket, payload);
			// If we're doing sync receive try and read return info from socket
			if (useRemoteSync(event)) {
				try {
					byte[] result = receive(socket, event.getEndpoint().getRemoteSyncTimeout());
					if (result == null) {
						return null;
					}
					return (UMOMessage) new MuleMessage(connector.getMessageAdapter(result));
				} catch (SocketTimeoutException e) {
					// we dont necesarily expect to receive a resonse here
					logger.info("Socket timed out normally while doing a synchronous receive on endpointUri: "
							+ event.getEndpoint().getEndpointURI());
					return null;
				}
			} else {
				return event.getMessage();
			}
		} finally {
			if (socket != null && !socket.isClosed()) {
				socket.close();
			}
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
				logger.info("Socket timed out normally while doing a synchronous receive on endpointUri: "
						+ endpointUri);
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
	}
}
