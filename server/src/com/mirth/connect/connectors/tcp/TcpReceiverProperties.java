/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.tcp;

import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.ListenerConnectorProperties;
import com.mirth.connect.donkey.model.channel.ListenerConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.ResponseConnectorProperties;
import com.mirth.connect.donkey.model.channel.ResponseConnectorPropertiesInterface;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.purge.PurgeUtil;
import com.mirth.connect.model.transmission.TransmissionModeProperties;
import com.mirth.connect.model.transmission.framemode.FrameModeProperties;
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

    private TransmissionModeProperties transmissionModeProperties;
    private boolean serverMode;
    private String remoteAddress;
    private String remotePort;
    private boolean overrideLocalBinding;
    private String reconnectInterval;
    private String receiveTimeout;
    private String bufferSize;
    private String maxConnections;
    private boolean keepConnectionOpen;
    private boolean dataTypeBinary;
    private String charsetEncoding;
    private int respondOnNewConnection;
    private String responseAddress;
    private String responsePort;

    public TcpReceiverProperties() {
        listenerConnectorProperties = new ListenerConnectorProperties("6661");
        responseConnectorProperties = new ResponseConnectorProperties(ResponseConnectorProperties.RESPONSE_SOURCE_TRANSFORMED);

        FrameModeProperties frameModeProperties = new FrameModeProperties("MLLP");
        frameModeProperties.setStartOfMessageBytes(TcpUtil.DEFAULT_LLP_START_BYTES);
        frameModeProperties.setEndOfMessageBytes(TcpUtil.DEFAULT_LLP_END_BYTES);
        this.transmissionModeProperties = frameModeProperties;

        this.serverMode = true;
        this.remoteAddress = "";
        this.remotePort = "";
        this.overrideLocalBinding = false;
        this.reconnectInterval = "5000";
        this.receiveTimeout = "0";
        this.bufferSize = "65536";
        this.maxConnections = "10";
        this.keepConnectionOpen = true;
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

    public TransmissionModeProperties getTransmissionModeProperties() {
        return transmissionModeProperties;
    }

    public void setTransmissionModeProperties(TransmissionModeProperties transmissionModeProperties) {
        this.transmissionModeProperties = transmissionModeProperties;
    }

    public boolean isServerMode() {
        return serverMode;
    }

    public void setServerMode(boolean serverMode) {
        this.serverMode = serverMode;
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

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {
        String remoteAddress = "";
        String remotePort = "";

        // If client mode is enabled, get the remote address/port from the listener settings
        if (!Boolean.parseBoolean(element.getChildElement("serverMode").getTextContent())) {
            DonkeyElement listenerConnectorProperties = element.getChildElement("listenerConnectorProperties");
            DonkeyElement host = listenerConnectorProperties.getChildElement("host");
            DonkeyElement port = listenerConnectorProperties.getChildElement("port");

            remoteAddress = host.getTextContent();
            remotePort = port.getTextContent();

            // Set the local address/port to the defaults since the channel will be using the remote ones instead
            host.setTextContent("0.0.0.0");
            port.setTextContent("0");
        }

        element.addChildElement("remoteAddress", remoteAddress);
        element.addChildElement("remotePort", remotePort);
        element.addChildElement("overrideLocalBinding", "false");
    }

    @Override
    public void migrate3_1_0(DonkeyElement element) {}

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = super.getPurgedProperties();
        purgedProperties.put("responseConnectorProperties", responseConnectorProperties.getPurgedProperties());
        purgedProperties.put("transmissionModeProperties", transmissionModeProperties.getPurgedProperties());
        purgedProperties.put("serverMode", serverMode);
        purgedProperties.put("overrideLocalBinding", overrideLocalBinding);
        purgedProperties.put("reconnectInterval", PurgeUtil.getNumericValue(reconnectInterval));
        purgedProperties.put("receiveTimeout", PurgeUtil.getNumericValue(receiveTimeout));
        purgedProperties.put("bufferSize", PurgeUtil.getNumericValue(bufferSize));
        purgedProperties.put("maxConnections", PurgeUtil.getNumericValue(maxConnections));
        purgedProperties.put("keepConnectionOpen", keepConnectionOpen);
        purgedProperties.put("dataTypeBinary", dataTypeBinary);
        purgedProperties.put("charsetEncoding", charsetEncoding);
        purgedProperties.put("respondOnNewConnection", respondOnNewConnection);
        return purgedProperties;
    }
}