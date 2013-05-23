/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.mirth.connect.connectors.tcp;

import org.mule.config.i18n.Message;
import org.mule.impl.model.AbstractComponent;
import org.mule.management.stats.ComponentStatistics;
import org.mule.providers.QueueEnabledConnector;
import org.mule.umo.UMOComponent;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOMessageReceiver;

import com.mirth.connect.connectors.tcp.protocols.DefaultProtocol;
import com.mirth.connect.server.Constants;
import com.mirth.connect.util.CharsetUtils;

/**
 * <code>TcpConnector</code> can bind or sent to a given tcp port on a given
 * host.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @author <a href="mailto:tsuppari@yahoo.co.uk">P.Oikari</a>
 * 
 * @version $Revision: 1.11 $
 */
public class TcpConnector extends QueueEnabledConnector {
    public static final String PROPERTY_CHAR_ENCODING = "charEncoding";
    public static final String PROPERTY_START_OF_MESSAGE = "messageStart";
    public static final String PROPERTY_END_OF_MESSAGE = "messageEnd";
    public static final String PROPERTY_RECORD_SEPARATOR = "recordSeparator";
    public static final String PROPERTY_END_OF_SEGMENT = "segmentEnd";
    public static final String PROPERTY_TEMPLATE = "template";
    public static final String PROPERTY_CHECKMSH15 = "checkMSH15";
    public static final String PROPERTY_ACK_NEW_CONNECTION = "ackOnNewConnection";
    public static final String PROPERTY_ACK_NEW_CONNECTION_IP = "ackIP";
    public static final String PROPERTY_ACK_NEW_CONNECTION_PORT = "ackPort";
    public static final String PROPERTY_REPLY_CHANNEL_ID = "replyChannelId";
    public static final String PROPERTY_TRANSFORMER_ACK = "responseFromTransformer";
    public static final String PROPERTY_RECEIVE_BINARY = "binary";
    public static final String PROPERTY_RESPONSE_VALUE = "responseValue";

    private String template = "message.encodedData";
    private boolean checkMSH15 = false;
    private boolean ackOnNewConnection = false;
    private String ackIP = "";
    private String ackPort = "";
    private String replyChannelId = "";
    private boolean responseFromTransformer = false;
    private boolean binary = false;
    private String responseValue = "None";

    public static final int DEFAULT_SOCKET_TIMEOUT = 5000;
    public static final int DEFAULT_ACK_TIMEOUT = 5000;
    public static final int DEFAULT_BUFFER_SIZE = 64 * 1024;
    public static final long DEFAULT_POLLING_FREQUENCY = 10;
    public static final int DEFAULT_BACKLOG = 256;
    private int sendTimeout = DEFAULT_SOCKET_TIMEOUT;
    private int receiveTimeout = DEFAULT_SOCKET_TIMEOUT;
    private int bufferSize = DEFAULT_BUFFER_SIZE;
    private int backlog = DEFAULT_BACKLOG;
    private boolean sendACK = false;
    private TcpProtocol tcpProtocol;

    private UMOComponent component = null;
    private int ackTimeout = DEFAULT_ACK_TIMEOUT;
    private String charsetEncoding;

    // /////////////////////////////////////////////
    // Does this protocol have any connected sockets?
    // /////////////////////////////////////////////
    private boolean sendSocketValid = false;
    private int receiveSocketsCount = 0;

    // //////////////////////////////////////////////////////////////////////
    // Properties for 'keepSocketConnected' TcpMessageDispatcher
    // //////////////////////////////////////////////////////////////////////
    public static final int KEEP_RETRYING_INDEFINETLY = 100;
    public static final int DEFAULT_RETRY_TIMES = 100;
    private boolean keepSendSocketOpen = false;

    // -1 try to reconnect forever
    private int maxRetryCount = DEFAULT_RETRY_TIMES;
    private boolean keepAlive = true;
    private String channelId;

    public String getChannelId() {
        return this.channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public boolean isKeepSendSocketOpen() {
        return keepSendSocketOpen;
    }

    public void setKeepSendSocketOpen(boolean keepSendSocketOpen) {
        this.keepSendSocketOpen = keepSendSocketOpen;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(int maxRetryCount) {
        // Dont set negative numbers
        if (maxRetryCount >= KEEP_RETRYING_INDEFINETLY) {
            this.maxRetryCount = maxRetryCount;
        } else if (maxRetryCount < 0) {
            this.maxRetryCount = 0;
        } else {
            this.maxRetryCount = maxRetryCount;
        }
    }

    // //////////////////////////////////////////////////////////////////////
    public void doInitialise() throws InitialisationException {
        super.doInitialise();
        if (tcpProtocol == null) {
            try {
                tcpProtocol = new DefaultProtocol();
                tcpProtocol.setTcpConnector(this);

            } catch (Exception e) {
                throw new InitialisationException(new Message("tcp", 3), e);
            }
        }

        if (isUsePersistentQueues()) {
            setConnectorErrorCode(Constants.ERROR_411);
            setDispatcher(new TcpMessageDispatcher(this));
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

    public void setTcProtocol(TcpProtocol tcpProtocol) {
        this.tcpProtocol = tcpProtocol;
    }

    public boolean isRemoteSyncEnabled() {
        return true;
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

    public void setCharsetEncoding(String charsetEncoding) {
        this.charsetEncoding = CharsetUtils.getEncoding(charsetEncoding, System.getProperty("ca.uhn.hl7v2.llp.charset"));
    }

    public String getCharsetEncoding() {
        return this.charsetEncoding;
    }

    public void setAckTimeout(int timeout) {
        if (timeout < 0) {
            timeout = DEFAULT_ACK_TIMEOUT;
        }
        this.ackTimeout = timeout;
    }

    public int getAckTimeout() {
        return (ackTimeout);
    }

    /*
     * Overload method to avoid error startting the channel after an stop
     * (non-Javadoc)
     * 
     * @seeorg.mule.providers.AbstractConnector#registerListener(org.mule.umo.
     * UMOComponent, org.mule.umo.endpoint.UMOEndpoint)
     */
    public UMOMessageReceiver registerListener(UMOComponent component, UMOEndpoint endpoint) throws Exception {
        UMOMessageReceiver r = null;
        this.component = component;
        try {
            r = super.registerListener(component, endpoint);
        } catch (org.mule.umo.provider.ConnectorException e) {
            logger.warn("Trying to reconnect a listener: this is not an error with this kind of router");
        }
        return r;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.mule.umo.provider.UMOConnector#registerListener(org.mule.umo.UMOSession
     * , org.mule.umo.endpoint.UMOEndpoint)
     */
    public UMOMessageReceiver createReceiver(UMOComponent component, UMOEndpoint endpoint) throws Exception {
        this.component = component;
        return super.createReceiver(component, endpoint);
    }

    public void incErrorStatistics() {
        incErrorStatistics(component);
    }

    public void incErrorStatistics(UMOComponent umoComponent) {
        ComponentStatistics statistics = null;

        if (umoComponent != null)
            component = umoComponent;

        if (component == null) {
            return;
        }

        if (!(component instanceof AbstractComponent)) {
            return;
        }

        try {
            statistics = ((AbstractComponent) component).getStatistics();
            if (statistics == null) {
                return;
            }
            statistics.incExecutionError();
        } catch (Throwable t) {
            logger.error("Error setting statistics ");
        }
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public boolean isCheckMSH15() {
        return checkMSH15;
    }

    public void setCheckMSH15(boolean checkMSH15) {
        this.checkMSH15 = checkMSH15;
    }

    public boolean isAckOnNewConnection() {
        return ackOnNewConnection;
    }

    public void setAckOnNewConnection(boolean ackOnNewConnection) {
        this.ackOnNewConnection = ackOnNewConnection;
    }

    public String getAckIP() {
        return ackIP;
    }

    public void setAckIP(String ackIP) {
        this.ackIP = ackIP;
    }

    public String getAckPort() {
        return ackPort;
    }

    public void setAckPort(String ackPort) {
        this.ackPort = ackPort;
    }

    public String getReplyChannelId() {
        return replyChannelId;
    }

    public void setReplyChannelId(String replyChannelId) {
        this.replyChannelId = replyChannelId;
    }

    public boolean isResponseFromTransformer() {
        return !getResponseValue().equalsIgnoreCase("None");
    }

    public void setResponseFromTransformer(boolean responseFromTransformer) {
        this.responseFromTransformer = responseFromTransformer;
    }

    public boolean isBinary() {
        return binary;
    }

    public void setBinary(boolean receiveBinary) {
        this.binary = receiveBinary;
    }

    public String getResponseValue() {
        return responseValue;
    }

    public void setResponseValue(String responseValue) {
        this.responseValue = responseValue;
    }
}
