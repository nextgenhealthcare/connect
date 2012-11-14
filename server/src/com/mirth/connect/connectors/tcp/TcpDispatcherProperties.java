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
import com.mirth.connect.util.TcpUtil;

@SuppressWarnings("serial")
public class TcpDispatcherProperties extends ConnectorProperties implements QueueConnectorPropertiesInterface {

    private QueueConnectorProperties queueConnectorProperties;

    public static final String PROTOCOL = "TCP";
    public static final String NAME = "TCP Sender";

    private String host;
    private String port;
    private String sendTimeout;
    private String bufferSize;
    private boolean keepConnectionOpen;
    private String startOfMessageBytes;
    private String endOfMessageBytes;
    private String responseTimeout;
    private boolean ignoreResponse;
    private boolean processHL7ACK;
    private boolean dataTypeBinary;
    private String charsetEncoding;
    private String template;

    public TcpDispatcherProperties() {
        queueConnectorProperties = new QueueConnectorProperties();

        this.host = "127.0.0.1";
        this.port = "8081";
        this.sendTimeout = "5000";
        this.bufferSize = "65536";
        this.keepConnectionOpen = false;
        this.startOfMessageBytes = TcpUtil.DEFAULT_LLP_START_BYTES;
        this.endOfMessageBytes = TcpUtil.DEFAULT_LLP_END_BYTES;
        this.responseTimeout = "5000";
        this.ignoreResponse = false;
        this.processHL7ACK = true;
        this.dataTypeBinary = false;
        this.charsetEncoding = CharsetUtils.DEFAULT_ENCODING;
        this.template = "${message.encodedData}";
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

    public boolean isProcessHL7ACK() {
        return processHL7ACK;
    }

    public void setProcessHL7ACK(boolean processHL7ACK) {
        this.processHL7ACK = processHL7ACK;
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

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
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
