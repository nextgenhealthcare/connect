/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.mllp;

import org.mule.config.i18n.Message;
import org.mule.impl.model.AbstractComponent;
import org.mule.management.stats.ComponentStatistics;
import org.mule.providers.QueueEnabledConnector;
import org.mule.umo.UMOComponent;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.ConnectorException;
import org.mule.umo.provider.UMOMessageReceiver;

import com.mirth.connect.connectors.mllp.protocols.LlpProtocol;
import com.mirth.connect.server.Constants;
import com.mirth.connect.util.CharsetUtils;

public class MllpConnector extends QueueEnabledConnector {
    // custom properties
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
    public static final String PROPERTY_WAIT_FOR_EOM_CHAR = "waitForEndOfMessageCharacter";
    public static final String PROPERTY_TRANSFORMER_ACK = "responseFromTransformer";
    public static final String PROPERTY_RESPONSE_VALUE = "responseValue";
    public static final String PROPERTY_USE_STRICT_LLP = "useStrictLLP";

    // custom properties
    private boolean serverMode = true;
    private String charEncoding = "hex";
    private String messageStart = "0x0B";
    private String messageEnd = "0x1C";
    private String recordSeparator = "0x0D";
    private String segmentEnd = "0x0D";
    private String template = "message.encodedData";
    private boolean checkMSH15 = false;
    private boolean ackOnNewConnection = false;
    private String ackIP = "";
    private String ackPort = "";
    private String replyChannelId = "";
    private boolean responseFromTransformer = false;
    private String responseValue = "None";
    private String channelId;
    private boolean waitForEndOfMessageCharacter = false;
    private boolean useStrictLLP = true;

    // ack properties
    public static final String PROPERTY_ACKCODE_SUCCESSFUL = "ackCodeSuccessful";
    public static final String PROPERTY_ACKMSG_SUCCESSFUL = "ackMsgSuccessful";

    public static final String PROPERTY_ACKCODE_ERROR = "ackCodeError";
    public static final String PROPERTY_ACKMSG_ERROR = "ackMsgError";

    public static final String PROPERTY_ACKCODE_REJECTED = "ackCodeRejected";
    public static final String PROPERTY_ACKMSG_REJECTED = "ackMsgRejected";

    private String ackCodeSuccessful = "AA";
    private String ackMsgSuccessful = "";
    private String ackCodeError = "AE";
    private String ackMsgError = "Error Processing Message";
    private String ackCodeRejected = "AR";
    private String ackMsgRejected = "Message Rejected";

    public static final int DEFAULT_RECONNECT_INTERVAL = 5000;
    public static final int DEFAULT_SOCKET_TIMEOUT = 5000;
    public static final int DEFAULT_ACK_TIMEOUT = 5000;
    public static final int DEFAULT_BUFFER_SIZE = 64 * 1024;
    public static final int DEFAULT_BACKLOG = 256;
    private int reconnectInterval = DEFAULT_RECONNECT_INTERVAL;
    private int sendTimeout = DEFAULT_SOCKET_TIMEOUT;
    private int receiveTimeout = DEFAULT_SOCKET_TIMEOUT;
    private int bufferSize = DEFAULT_BUFFER_SIZE;
    private int backlog = DEFAULT_BACKLOG;
    private boolean sendACK = false;
    private LlpProtocol llpProtocol;

    public static final long DEFAULT_POLL_FREQUENCY = 1000;
    public static final long STARTUP_DELAY = 1000;
    private long frequency = DEFAULT_POLL_FREQUENCY;

    private UMOComponent component = null;
    private int ackTimeout = DEFAULT_ACK_TIMEOUT;
    
    private String charsetEncoding;

    // Does this protocol have any connected sockets?
    private boolean sendSocketValid = false;
    private int receiveSocketsCount = 0;

    // Properties for 'keepSocketConnected' TcpMessageDispatcher
    public static final int KEEP_RETRYING_INDEFINETLY = 100;
    public static final int DEFAULT_RETRY_TIMES = 100;
    private boolean keepSendSocketOpen = false;

    // -1 try to reconnect forever
    private int maxRetryCount = DEFAULT_RETRY_TIMES;
    private boolean keepAlive = true;
    private boolean processBatchFiles = true;
    private boolean processHl7AckResponse = true;
    private boolean queueAckTimeout = true;

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

    public void doInitialise() throws InitialisationException {
        super.doInitialise();

        if (llpProtocol == null) {
            try {
                llpProtocol = new LlpProtocol();
                llpProtocol.setTcpConnector(this);
                if (isUseStrictLLP()) {
                    llpProtocol.setUseLLP(true);
                } else {
                    llpProtocol.setUseLLP(false);
                }
            } catch (Exception e) {
                throw new InitialisationException(new Message("mllp", 3), e);
            }
        }

        if (isUsePersistentQueues()) {
            setConnectorErrorCode(Constants.ERROR_408);
            setDispatcher(new MllpMessageDispatcher(this));
        }
    }

    public String getProtocol() {
        return "MLLP";
    }

    // a shorthand property setting timeout for both SEND and RECEIVE sockets.
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

    // new independednt Socket timeout for receiveSocket
    public int getReceiveTimeout() {
        return receiveTimeout;
    }

    public void setReceiveTimeout(int timeout) {
        if (timeout < 0) {
            timeout = DEFAULT_SOCKET_TIMEOUT;
        }

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
            receiveSocketsCount++;
        } else {
            receiveSocketsCount--;
        }
    }

    public boolean isWaitForEndOfMessageCharacter() {
        return waitForEndOfMessageCharacter;
    }

    public void setWaitForEndOfMessageCharacter(boolean waitForEndOfMessageCharacter) {
        this.waitForEndOfMessageCharacter = waitForEndOfMessageCharacter;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        if (bufferSize < 1) {
            bufferSize = DEFAULT_BUFFER_SIZE;
        }

        this.bufferSize = bufferSize;
    }

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public LlpProtocol getLlpProtocol() {
        return llpProtocol;
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

    public void setCharsetEncoding(String charsetEncoding) {
        this.charsetEncoding = CharsetUtils.getEncoding(charsetEncoding, System.getProperty("ca.uhn.hl7v2.llp.charset"));
    }

    public String getCharsetEncoding() {
        return charsetEncoding;
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
        UMOMessageReceiver receiver = null;
        this.component = component;

        try {
            receiver = super.registerListener(component, endpoint);
        } catch (ConnectorException e) {
            logger.warn("Trying to reconnect a listener: this is not an error with this kind of router");
        }

        return receiver;
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

        if (umoComponent != null) {
            component = umoComponent;
        }

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

    public String getAckCodeError() {
        return ackCodeError;
    }

    public void setAckCodeError(String ackCodeError) {
        this.ackCodeError = ackCodeError;
    }

    public String getAckCodeRejected() {
        return ackCodeRejected;
    }

    public void setAckCodeRejected(String ackCodeRejected) {
        this.ackCodeRejected = ackCodeRejected;
    }

    public String getAckCodeSuccessful() {
        return ackCodeSuccessful;
    }

    public void setAckCodeSuccessful(String ackCodeSuccessful) {
        this.ackCodeSuccessful = ackCodeSuccessful;
    }

    public String getAckMsgError() {
        return ackMsgError;
    }

    public void setAckMsgError(String ackMsgError) {
        this.ackMsgError = ackMsgError;
    }

    public String getAckMsgRejected() {
        return ackMsgRejected;
    }

    public void setAckMsgRejected(String ackMsgRejected) {
        this.ackMsgRejected = ackMsgRejected;
    }

    public String getAckMsgSuccessful() {
        return ackMsgSuccessful;
    }

    public void setAckMsgSuccessful(String ackMsgSuccessful) {
        this.ackMsgSuccessful = ackMsgSuccessful;
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
        return responseFromTransformer;
    }

    public void setResponseFromTransformer(boolean responseFromTransformer) {
        this.responseFromTransformer = responseFromTransformer;
    }

    public String getResponseValue() {
        return responseValue;
    }

    public void setResponseValue(String responseValue) {
        this.responseValue = responseValue;
    }

    public boolean isUseStrictLLP() {
        return useStrictLLP;
    }

    public void setUseStrictLLP(boolean useStrictLLP) {
        this.useStrictLLP = useStrictLLP;
    }

    public boolean isServerMode() {
        return serverMode;
    }

    public void setServerMode(boolean serverMode) {
        this.serverMode = serverMode;
    }

    public int getReconnectInterval() {
        return reconnectInterval;
    }

    public void setReconnectInterval(int reconnectInterval) {
        this.reconnectInterval = reconnectInterval;
    }

    public boolean isProcessBatchFiles() {
        return processBatchFiles;
    }

    public void setProcessBatchFiles(boolean processBatchFiles) {
        this.processBatchFiles = processBatchFiles;
    }

    public boolean isProcessHl7AckResponse() {
        return processHl7AckResponse;
    }

    public void setProcessHl7AckResponse(boolean processHl7AckResponse) {
        this.processHl7AckResponse = processHl7AckResponse;
    }

    public void setQueueAckTimeout(boolean queueAckTimeout) {
        this.queueAckTimeout = queueAckTimeout;
    }

    public boolean isQueueAckTimeout() {
        return queueAckTimeout;
    }

    public long getFrequency() {
        return frequency;
    }

    public void setFrequency(long frequency) {
        this.frequency = frequency;
    }
}
