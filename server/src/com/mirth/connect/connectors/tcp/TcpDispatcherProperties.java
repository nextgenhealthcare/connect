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
import com.mirth.connect.donkey.model.channel.DestinationConnectorProperties;
import com.mirth.connect.donkey.model.channel.DestinationConnectorPropertiesInterface;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.purge.PurgeUtil;
import com.mirth.connect.model.transmission.TransmissionModeProperties;
import com.mirth.connect.model.transmission.framemode.FrameModeProperties;
import com.mirth.connect.util.CharsetUtils;
import com.mirth.connect.util.TcpUtil;

@SuppressWarnings("serial")
public class TcpDispatcherProperties extends ConnectorProperties implements DestinationConnectorPropertiesInterface {

    private DestinationConnectorProperties destinationConnectorProperties;

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
    private boolean checkRemoteHost;
    private String responseTimeout;
    private boolean ignoreResponse;
    private boolean queueOnResponseTimeout;
    private boolean dataTypeBinary;
    private String charsetEncoding;
    private String template;

    public TcpDispatcherProperties() {
        destinationConnectorProperties = new DestinationConnectorProperties(true);

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
        this.checkRemoteHost = false;
        this.responseTimeout = "5000";
        this.ignoreResponse = false;
        this.queueOnResponseTimeout = true;
        this.dataTypeBinary = false;
        this.charsetEncoding = CharsetUtils.DEFAULT_ENCODING;
        this.template = "${message.encodedData}";
    }

    public TcpDispatcherProperties(TcpDispatcherProperties props) {
        super(props);
        destinationConnectorProperties = new DestinationConnectorProperties(props.getDestinationConnectorProperties());

        transmissionModeProperties = props.getTransmissionModeProperties();

        remoteAddress = props.getRemoteAddress();
        remotePort = props.getRemotePort();
        overrideLocalBinding = props.isOverrideLocalBinding();
        localAddress = props.getLocalAddress();
        localPort = props.getLocalPort();
        sendTimeout = props.getSendTimeout();
        bufferSize = props.getBufferSize();
        keepConnectionOpen = props.isKeepConnectionOpen();
        checkRemoteHost = props.isCheckRemoteHost();
        responseTimeout = props.getResponseTimeout();
        ignoreResponse = props.isIgnoreResponse();
        queueOnResponseTimeout = props.isQueueOnResponseTimeout();
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

    public boolean isCheckRemoteHost() {
        return checkRemoteHost;
    }

    public void setCheckRemoteHost(boolean checkRemoteHost) {
        this.checkRemoteHost = checkRemoteHost;
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
    public DestinationConnectorProperties getDestinationConnectorProperties() {
        return destinationConnectorProperties;
    }

    @Override
    public ConnectorProperties clone() {
        return new TcpDispatcherProperties(this);
    }

    @Override
    public boolean canValidateResponse() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {
        super.migrate3_1_0(element);

        element.addChildElementIfNotExists("checkRemoteHost", "true");

        DonkeyElement processHL7ACKElement = element.removeChild("processHL7ACK");
        DonkeyElement destinationPropertiesElement = element.getChildElement("destinationConnectorProperties");
        if (processHL7ACKElement != null && destinationPropertiesElement != null) {
            destinationPropertiesElement.addChildElement("validateResponse", processHL7ACKElement.getTextContent());
        }
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = super.getPurgedProperties();
        purgedProperties.put("destinationConnectorProperties", destinationConnectorProperties.getPurgedProperties());
        purgedProperties.put("transmissionModeProperties", transmissionModeProperties.getPurgedProperties());
        purgedProperties.put("overrideLocalBinding", overrideLocalBinding);
        purgedProperties.put("sendTimeout", PurgeUtil.getNumericValue(sendTimeout));
        purgedProperties.put("bufferSize", PurgeUtil.getNumericValue(bufferSize));
        purgedProperties.put("keepConnectionOpen", keepConnectionOpen);
        purgedProperties.put("checkRemoteHost", checkRemoteHost);
        purgedProperties.put("responseTimeout", PurgeUtil.getNumericValue(responseTimeout));
        purgedProperties.put("ignoreResponse", ignoreResponse);
        purgedProperties.put("queueOnResponseTimeout", queueOnResponseTimeout);
        purgedProperties.put("charsetEncoding", charsetEncoding);
        purgedProperties.put("templateLines", PurgeUtil.countLines(template));
        return purgedProperties;
    }
}
