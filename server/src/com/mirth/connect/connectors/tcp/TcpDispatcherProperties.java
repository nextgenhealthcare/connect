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
import com.mirth.connect.donkey.model.channel.QueueConnectorProperties;
import com.mirth.connect.donkey.model.channel.QueueConnectorPropertiesInterface;
import com.mirth.connect.util.CharsetUtils;

@SuppressWarnings("serial")
public class TcpDispatcherProperties extends ConnectorProperties implements QueueConnectorPropertiesInterface {

    private QueueConnectorProperties queueConnectorProperties;

    public static final String PROTOCOL = "TCP";
    public static final String NAME = "TCP Sender";

    private String host;
    private String port;
    private boolean frameEncodingIsHex;
    private String beginBytes;
    private String endBytes;
    private String template;
    private String sendTimeout;
    private String bufferSize;
    private boolean keepConnectionOpen;
    private String maxRetryCount;
    private String reconnectInterval;
    private String responseTimeout;
    private boolean ignoreResponse;
    private String charsetEncoding;
    private boolean dataTypeIsBase64;

    public TcpDispatcherProperties() {
        queueConnectorProperties = new QueueConnectorProperties();

        this.host = "127.0.0.1";
        this.port = "8081";
        this.template = "${message.encodedData}";
        this.sendTimeout = "5000";
        this.bufferSize = "65536";
        this.keepConnectionOpen = false;
        this.frameEncodingIsHex = false;
        this.beginBytes = "";
        this.endBytes = "";
        this.maxRetryCount = "2";
        this.reconnectInterval = "10000";
        this.responseTimeout = "5000";
        this.ignoreResponse = false;
        this.charsetEncoding = CharsetUtils.DEFAULT_ENCODING;
        this.dataTypeIsBase64 = true;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public boolean isFrameEncodingHex() {
        return frameEncodingIsHex;
    }

    public void setFrameEncodingIsHex(boolean frameEncodingIsHex) {
        this.frameEncodingIsHex = frameEncodingIsHex;
    }

    public String getBeginBytes() {
        return beginBytes;
    }

    public void setBeginBytes(String beginBytes) {
        this.beginBytes = beginBytes;
    }

    public String getEndBytes() {
        return endBytes;
    }

    public void setEndBytes(String endBytes) {
        this.endBytes = endBytes;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getSendTimeout() {
        return sendTimeout;
    }

    public void setSendTimeout(String sendTimeout) {
        this.sendTimeout = sendTimeout;
    }

    public String getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(String bufferSize) {
        this.bufferSize = bufferSize;
    }

    public boolean isKeepConnectionOpen() {
        return keepConnectionOpen;
    }

    public void setKeepConnectionOpen(boolean keepConnectionOpen) {
        this.keepConnectionOpen = keepConnectionOpen;
    }

    public String getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(String maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public String getReconnectInterval() {
        return reconnectInterval;
    }

    public void setReconnectInterval(String reconnectInterval) {
        this.reconnectInterval = reconnectInterval;
    }

    public String getResponseTimeout() {
        return responseTimeout;
    }

    public void setResponseTimeout(String responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    public boolean isIgnoreResponse() {
        return ignoreResponse;
    }

    public void setIgnoreResponse(boolean ignoreResponse) {
        this.ignoreResponse = ignoreResponse;
    }

    public String getCharsetEncoding() {
        return charsetEncoding;
    }

    public void setCharsetEncoding(String charsetEncoding) {
        this.charsetEncoding = charsetEncoding;
    }

    public boolean isDataTypeBase64() {
        return dataTypeIsBase64;
    }

    public void setDataTypeIsBase64(boolean dataTypeIsBase64) {
        this.dataTypeIsBase64 = dataTypeIsBase64;
    }

    public void setQueueConnectorProperties(QueueConnectorProperties queueConnectorProperties) {
        this.queueConnectorProperties = queueConnectorProperties;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public QueueConnectorProperties getQueueConnectorProperties() {
        return queueConnectorProperties;
    }
}
