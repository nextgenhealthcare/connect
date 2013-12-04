/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.tcp;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.DispatcherConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.QueueConnectorProperties;
import com.mirth.connect.model.transmission.TransmissionModeProperties;
import com.mirth.connect.model.transmission.framemode.FrameModeProperties;
import com.mirth.connect.util.CharsetUtils;
import com.mirth.connect.util.TcpUtil;

@SuppressWarnings("serial")
public class TcpDispatcherProperties extends ConnectorProperties implements DispatcherConnectorPropertiesInterface {

    private QueueConnectorProperties queueConnectorProperties;

    public static final String PROTOCOL = "TCP";
    public static final String NAME = "TCP Sender";

    private TransmissionModeProperties transmissionModeProperties;
    private String remoteAddress;
    private String remotePort;
    private boolean overrideLocalBinding;
    private String localAddress;
    private String localPort;
    private String sendTimeout;
    private String bufferSize;
    private boolean keepConnectionOpen;
    private String responseTimeout;
    private boolean ignoreResponse;
    private boolean queueOnResponseTimeout;
    private boolean processHL7ACK;
    private boolean dataTypeBinary;
    private String charsetEncoding;
    private String template;

    public TcpDispatcherProperties() {
        queueConnectorProperties = new QueueConnectorProperties();

        FrameModeProperties frameModeProperties = new FrameModeProperties("MLLP");
        frameModeProperties.setStartOfMessageBytes(TcpUtil.DEFAULT_LLP_START_BYTES);
        frameModeProperties.setEndOfMessageBytes(TcpUtil.DEFAULT_LLP_END_BYTES);
        this.transmissionModeProperties = frameModeProperties;

        this.remoteAddress = "127.0.0.1";
        this.remotePort = "6660";
        this.overrideLocalBinding = false;
        this.localAddress = "0.0.0.0";
        this.localPort = "0";
        this.sendTimeout = "5000";
        this.bufferSize = "65536";
        this.keepConnectionOpen = false;
        this.responseTimeout = "5000";
        this.ignoreResponse = false;
        this.queueOnResponseTimeout = true;
        this.processHL7ACK = true;
        this.dataTypeBinary = false;
        this.charsetEncoding = CharsetUtils.DEFAULT_ENCODING;
        this.template = "${message.encodedData}";
    }

    public TcpDispatcherProperties(TcpDispatcherProperties props) {
        queueConnectorProperties = new QueueConnectorProperties(props.getQueueConnectorProperties());

        transmissionModeProperties = props.getTransmissionModeProperties();

        remoteAddress = props.getRemoteAddress();
        remotePort = props.getRemotePort();
        overrideLocalBinding = props.isOverrideLocalBinding();
        localAddress = props.getLocalAddress();
        localPort = props.getLocalPort();
        sendTimeout = props.getSendTimeout();
        bufferSize = props.getBufferSize();
        keepConnectionOpen = props.isKeepConnectionOpen();
        responseTimeout = props.getResponseTimeout();
        ignoreResponse = props.isIgnoreResponse();
        queueOnResponseTimeout = props.isQueueOnResponseTimeout();
        processHL7ACK = props.isProcessHL7ACK();
        dataTypeBinary = props.isDataTypeBinary();
        charsetEncoding = props.getCharsetEncoding();
        template = props.getTemplate();
    }

    public TransmissionModeProperties getTransmissionModeProperties() {
        return transmissionModeProperties;
    }

    public void setTransmissionModeProperties(TransmissionModeProperties transmissionModeProperties) {
        this.transmissionModeProperties = transmissionModeProperties;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(String remotePort) {
        this.remotePort = remotePort;
    }

    public boolean isOverrideLocalBinding() {
        return overrideLocalBinding;
    }

    public void setOverrideLocalBinding(boolean overrideLocalBinding) {
        this.overrideLocalBinding = overrideLocalBinding;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public String getLocalPort() {
        return localPort;
    }

    public void setLocalPort(String localPort) {
        this.localPort = localPort;
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

    public boolean isQueueOnResponseTimeout() {
        return queueOnResponseTimeout;
    }

    public void setQueueOnResponseTimeout(boolean queueOnResponseTimeout) {
        this.queueOnResponseTimeout = queueOnResponseTimeout;
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
        StringBuilder builder = new StringBuilder();
        String newLine = "\n";

        builder.append("REMOTE ADDRESS: ");
        builder.append(remoteAddress + ":" + remotePort);
        builder.append(newLine);

        if (overrideLocalBinding) {
            builder.append("LOCAL ADDRESS: ");
            builder.append(localAddress + ":" + localPort);
            builder.append(newLine);
        }

        builder.append(newLine);
        builder.append("[CONTENT]");
        builder.append(newLine);
        builder.append(template);
        return builder.toString();
    }

    @Override
    public QueueConnectorProperties getQueueConnectorProperties() {
        return queueConnectorProperties;
    }

    @Override
    public ConnectorProperties clone() {
        return new TcpDispatcherProperties(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
}
