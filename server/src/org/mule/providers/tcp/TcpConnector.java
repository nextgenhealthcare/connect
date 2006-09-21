/*
 * $Header: /home/projects/mule/scm/mule/providers/tcp/src/java/org/mule/providers/tcp/TcpConnector.java,v 1.11 2005/11/05 12:23:27 aperepel Exp $
 * $Revision: 1.11 $
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

import org.mule.config.i18n.Message;
import org.mule.providers.AbstractServiceEnabledConnector;
import org.mule.providers.tcp.protocols.DefaultProtocol;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.util.ClassHelper;

/**
 * <code>TcpConnector</code> can bind or sent to a given tcp port on a given
 * host.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @author <a href="mailto:tsuppari@yahoo.co.uk">P.Oikari</a>
 * 
 * @version $Revision: 1.11 $
 */
public class TcpConnector extends AbstractServiceEnabledConnector {
	// customer properties
	public static final String PROPERTY_CHAR_ENCODING = "charEncoding";
	public static final String PROPERTY_START_OF_MESSAGE = "messageStart";
	public static final String PROPERTY_END_OF_MESSAGE = "messageEnd";
	public static final String PROPERTY_RECORD_SEPARATOR = "recordSeparator";
	public static final String PROPERTY_END_OF_SEGMENT = "segmentEnd";

	// custom properties
	private String charEncoding = "hex";
	private String messageStart = "0x1C";
	private String messageEnd = "0x0B";
	private String recordSeparator = "0x0D";
	private String segmentEnd = "0x0D";

	public static final int DEFAULT_SOCKET_TIMEOUT = 5000;

	public static final int DEFAULT_BUFFER_SIZE = 64 * 1024;

	public static final long DEFAULT_POLLING_FREQUENCY = 10;

	public static final int DEFAULT_BACKLOG = 256;

	private int sendTimeout = DEFAULT_SOCKET_TIMEOUT;

	private int receiveTimeout = DEFAULT_SOCKET_TIMEOUT;

	private int bufferSize = DEFAULT_BUFFER_SIZE;

	private int backlog = DEFAULT_BACKLOG;

	private boolean sendACK = false;

	private String tcpProtocolClassName = DefaultProtocol.class.getName();

	private TcpProtocol tcpProtocol;

	// /////////////////////////////////////////////
	// Does this protocol have any connected sockets?
	// /////////////////////////////////////////////
	private boolean sendSocketValid = false;

	private int receiveSocketsCount = 0;

	// //////////////////////////////////////////////////////////////////////
	// Properties for 'keepSocketConnected' TcpMessageDispatcher
	// //////////////////////////////////////////////////////////////////////
	public static final int KEEP_RETRYING_INDEFINETLY = 0;

	private boolean keepSendSocketOpen = false;

	// Time to sleep between reconnects in msecs
	private int reconnectMillisecs = 10000;

	// -1 try to reconnect forever
	private int maxRetryCount = KEEP_RETRYING_INDEFINETLY;

	private boolean keepAlive = true;

	public boolean isKeepSendSocketOpen() {
		return keepSendSocketOpen;
	}

	public void setKeepSendSocketOpen(boolean keepSendSocketOpen) {
		this.keepSendSocketOpen = keepSendSocketOpen;
	}

	public int getReconnectMillisecs() {
		return reconnectMillisecs;
	}

	public void setReconnectMillisecs(int reconnectMillisecs) {
		this.reconnectMillisecs = reconnectMillisecs;
	}

	public int getMaxRetryCount() {
		return maxRetryCount;
	}

	public void setMaxRetryCount(int maxRetryCount) {
		// Dont set negative numbers
		if (maxRetryCount >= KEEP_RETRYING_INDEFINETLY) {
			this.maxRetryCount = maxRetryCount;
		}
	}

	// //////////////////////////////////////////////////////////////////////
	public void doInitialise() throws InitialisationException {
		super.doInitialise();
		if (tcpProtocol == null) {
			try {
				tcpProtocol = (TcpProtocol) ClassHelper.instanciateClass(tcpProtocolClassName, null);
				tcpProtocol.setTcpConnector(this);
			} catch (Exception e) {
				throw new InitialisationException(new Message("tcp", 3), e);
			}
		}
	}

	public String getProtocol() {
		return "TCP";
	}

	/**
	 * A shorthand property setting timeout for both SEND and RECEIVE sockets.
	 */
	public void setTimeout(int timeout) {
		setSendTimeout(timeout);
		setReceiveTimeout(timeout);
	}

	public int getSendTimeout() {
		return this.sendTimeout;
	}

	public boolean getSendACK() {
		return sendACK;
	}

	public void setSendACK(boolean ack) {
		sendACK = ack;
	}

	public void setSendTimeout(int timeout) {
		if (timeout < 0) {
			timeout = DEFAULT_SOCKET_TIMEOUT;
		}
		this.sendTimeout = timeout;
	}

	// ////////////////////////////////////////////
	// New independednt Socket timeout for receiveSocket
	// ////////////////////////////////////////////
	public int getReceiveTimeout() {
		return receiveTimeout;
	}

	public void setReceiveTimeout(int timeout) {
		if (timeout < 0)
			timeout = DEFAULT_SOCKET_TIMEOUT;
		this.receiveTimeout = timeout;
	}

	public boolean isSendSocketValid() {
		return sendSocketValid;
	}

	public void setSendSocketValid(boolean validity) {
		this.sendSocketValid = validity;
	}

	public boolean hasReceiveSockets() {
		return receiveSocketsCount > 0;
	}

	/**
	 * Update the number of receive sockets.
	 * 
	 * @param addSocket
	 *            increase the number if true, decrement otherwise
	 */
	public synchronized void updateReceiveSocketsCount(boolean addSocket) {
		if (addSocket) {
			this.receiveSocketsCount++;
		} else {
			this.receiveSocketsCount--;
		}
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		if (bufferSize < 1)
			bufferSize = DEFAULT_BUFFER_SIZE;
		this.bufferSize = bufferSize;
	}

	public int getBacklog() {
		return backlog;
	}

	public void setBacklog(int backlog) {
		this.backlog = backlog;
	}

	public TcpProtocol getTcpProtocol() {
		return tcpProtocol;
	}

	public void setTcpProtocol(TcpProtocol tcpProtocol) {
		this.tcpProtocol = tcpProtocol;
	}

	public String getTcpProtocolClassName() {
		return tcpProtocolClassName;
	}

	public void setTcpProtocolClassName(String protocolClassName) {
		this.tcpProtocolClassName = protocolClassName;
	}

	public boolean isRemoteSyncEnabled() {
		return true;
	}

	public String getCharEncoding() {
		return this.charEncoding;
	}

	public void setCharEncoding(String charEncoding) {
		this.charEncoding = charEncoding;
	}

	public String getMessageEnd() {
		return this.messageEnd;
	}

	public void setMessageEnd(String messageEnd) {
		this.messageEnd = messageEnd;
	}

	public String getMessageStart() {
		return this.messageStart;
	}

	public void setMessageStart(String messageStart) {
		this.messageStart = messageStart;
	}

	public String getRecordSeparator() {
		return this.recordSeparator;
	}

	public void setRecordSeparator(String recordSeparator) {
		this.recordSeparator = recordSeparator;
	}

	public String getSegmentEnd() {
		return this.segmentEnd;
	}

	public void setSegmentEnd(String segmentEnd) {
		this.segmentEnd = segmentEnd;
	}

	public char stringToChar(String source) {
		return source.charAt(0);
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}
}
