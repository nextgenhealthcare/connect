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

@SuppressWarnings("serial")
public class TcpReceiverProperties extends ConnectorProperties implements ListenerConnectorPropertiesInterface, ResponseConnectorPropertiesInterface {
    private ListenerConnectorProperties listenerConnectorProperties;
    private ResponseConnectorProperties responseConnectorProperties;

    public static final String PROTOCOL = "TCP";
    public static final String NAME = "TCP Listener";

    // TODO: Add max connections to the ListenerSettingsPanel?
    public static final int DEFAULT_MAX_CONNECTIONS = 256;

    private String timeout;
    private boolean frameEncodingIsHex;
    private String beginBytes;
    private String endBytes;
    private String bufferSize;
    private String maxConnections;
    private boolean keepConnectionOpen;
    private boolean ackOnNewConnection;
    private String ackIP;
    private String ackPort;
    private String charsetEncoding;
    private boolean dataTypeIsBase64;

    public TcpReceiverProperties() {
        listenerConnectorProperties = new ListenerConnectorProperties("8081");
        responseConnectorProperties = new ResponseConnectorProperties("None", new String[] { "None" });

        this.timeout = "5000";
        this.bufferSize = "65536";
        this.maxConnections = "256";
        this.keepConnectionOpen = false;
        this.frameEncodingIsHex = false;
        this.beginBytes = "";
        this.endBytes = "";
        this.ackIP = "";
        this.ackPort = "";
        this.ackOnNewConnection = false;
        this.charsetEncoding = CharsetUtils.DEFAULT_ENCODING;
        this.dataTypeIsBase64 = true;
    }

    @Override
    public ResponseConnectorProperties getResponseConnectorProperties() {
        return responseConnectorProperties;
    }

    @Override
    public ListenerConnectorProperties getListenerConnectorProperties() {
        return listenerConnectorProperties;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
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
