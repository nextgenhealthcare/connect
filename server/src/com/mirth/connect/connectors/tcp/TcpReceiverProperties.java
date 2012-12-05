/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.tcp;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.ListenerConnectorProperties;
import com.mirth.connect.donkey.model.channel.ListenerConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.ResponseConnectorProperties;
import com.mirth.connect.donkey.model.channel.ResponseConnectorPropertiesInterface;
import com.mirth.connect.util.CharsetUtils;
import com.mirth.connect.util.TcpUtil;

@SuppressWarnings("serial")
public class TcpReceiverProperties extends ConnectorProperties implements ListenerConnectorPropertiesInterface, ResponseConnectorPropertiesInterface {
    private ListenerConnectorProperties listenerConnectorProperties;
    private ResponseConnectorProperties responseConnectorProperties;

    public static final String PROTOCOL = "TCP";
    public static final String NAME = "TCP Listener";
    public static final int SAME_CONNECTION = 0;
    public static final int NEW_CONNECTION = 1;
    public static final int NEW_CONNECTION_ON_RECOVERY = 2;

    private boolean serverMode;
    private String reconnectInterval;
    private String receiveTimeout;
    private String bufferSize;
    private String maxConnections;
    private boolean keepConnectionOpen;
    private String startOfMessageBytes;
    private String endOfMessageBytes;
    private boolean processBatch;
    private boolean dataTypeBinary;
    private String charsetEncoding;
    private int respondOnNewConnection;
    private String responseAddress;
    private String responsePort;

    public TcpReceiverProperties() {
        listenerConnectorProperties = new ListenerConnectorProperties("6661");
        responseConnectorProperties = new ResponseConnectorProperties();
        responseConnectorProperties.setResponseVariable(ResponseConnectorProperties.RESPONSE_AUTO_BEFORE);

        this.serverMode = true;
        this.reconnectInterval = "5000";
        this.receiveTimeout = "5000";
        this.bufferSize = "65536";
        this.maxConnections = "10";
        this.keepConnectionOpen = false;
        this.startOfMessageBytes = TcpUtil.DEFAULT_LLP_START_BYTES;
        this.endOfMessageBytes = TcpUtil.DEFAULT_LLP_END_BYTES;
        this.processBatch = false;
        this.dataTypeBinary = false;
        this.charsetEncoding = CharsetUtils.DEFAULT_ENCODING;
        this.respondOnNewConnection = SAME_CONNECTION;
        this.responseAddress = "";
        this.responsePort = "";
    }

    @Override
    public ResponseConnectorProperties getResponseConnectorProperties() {
        return responseConnectorProperties;
    }

    @Override
    public ListenerConnectorProperties getListenerConnectorProperties() {
        return listenerConnectorProperties;
    }

    public boolean isServerMode() {
        return serverMode;
    }

    public void setServerMode(boolean serverMode) {
        this.serverMode = serverMode;
    }

    public String getReconnectInterval() {
        return reconnectInterval;
    }

    public void setReconnectInterval(String reconnectInterval) {
        this.reconnectInterval = reconnectInterval;
    }

    public String getReceiveTimeout() {
        return receiveTimeout;
    }

    public void setReceiveTimeout(String receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    public String getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(String bufferSize) {
        this.bufferSize = bufferSize;
    }

    public String getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(String maxConnections) {
        this.maxConnections = maxConnections;
    }

    public boolean isKeepConnectionOpen() {
        return keepConnectionOpen;
    }

    public void setKeepConnectionOpen(boolean keepConnectionOpen) {
        this.keepConnectionOpen = keepConnectionOpen;
    }

    public String getStartOfMessageBytes() {
        return startOfMessageBytes;
    }

    public void setStartOfMessageBytes(String startOfMessageBytes) {
        this.startOfMessageBytes = startOfMessageBytes;
    }

    public String getEndOfMessageBytes() {
        return endOfMessageBytes;
    }

    public void setEndOfMessageBytes(String endOfMessageBytes) {
        this.endOfMessageBytes = endOfMessageBytes;
    }

    public boolean isProcessBatch() {
        return processBatch;
    }

    public void setProcessBatch(boolean processBatch) {
        this.processBatch = processBatch;
    }

    public boolean isDataTypeBinary() {
        return dataTypeBinary;
    }

    public void setDataTypeBinary(boolean dataTypeBinary) {
        this.dataTypeBinary = dataTypeBinary;
    }

    public String getCharsetEncoding() {
        return charsetEncoding;
    }

    public void setCharsetEncoding(String charsetEncoding) {
        this.charsetEncoding = charsetEncoding;
    }

    public int getRespondOnNewConnection() {
        return respondOnNewConnection;
    }

    public void setRespondOnNewConnection(int respondOnNewConnection) {
        this.respondOnNewConnection = respondOnNewConnection;
    }

    public String getResponseAddress() {
        return responseAddress;
    }

    public void setResponseAddress(String responseAddress) {
        this.responseAddress = responseAddress;
    }

    public String getResponsePort() {
        return responsePort;
    }

    public void setResponsePort(String responsePort) {
        this.responsePort = responsePort;
    }

    public void setListenerConnectorProperties(ListenerConnectorProperties listenerConnectorProperties) {
        this.listenerConnectorProperties = listenerConnectorProperties;
    }

    public void setResponseConnectorProperties(ResponseConnectorProperties responseConnectorProperties) {
        this.responseConnectorProperties = responseConnectorProperties;
    }

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toFormattedString() {
        return null;
    }
}
