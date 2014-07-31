/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.ListenerConnectorProperties;
import com.mirth.connect.donkey.model.channel.ListenerConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.SourceConnectorProperties;
import com.mirth.connect.donkey.model.channel.SourceConnectorPropertiesInterface;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.purge.PurgeUtil;

public class HttpReceiverProperties extends ConnectorProperties implements ListenerConnectorPropertiesInterface, SourceConnectorPropertiesInterface {
    private ListenerConnectorProperties listenerConnectorProperties;
    private SourceConnectorProperties sourceConnectorProperties;

    private boolean xmlBody;
    private boolean parseMultipart;
    private boolean includeMetadata;
    private String responseContentType;
    private boolean responseDataTypeBinary;
    private String responseStatusCode;
    private Map<String, String> responseHeaders;
    private String charset;
    private String contextPath;
    private String timeout;
    private List<HttpStaticResource> staticResources;

    public HttpReceiverProperties() {
        listenerConnectorProperties = new ListenerConnectorProperties("80");
        sourceConnectorProperties = new SourceConnectorProperties();

        this.xmlBody = false;
        this.parseMultipart = true;
        this.includeMetadata = false;
        this.responseContentType = "text/plain";
        this.responseDataTypeBinary = false;
        this.responseStatusCode = "";
        this.responseHeaders = new LinkedHashMap<String, String>();
        this.charset = "UTF-8";
        this.contextPath = "";
        this.timeout = "0";
        this.staticResources = new ArrayList<HttpStaticResource>();
    }

    public boolean isXmlBody() {
        return xmlBody;
    }

    public void setXmlBody(boolean xmlBody) {
        this.xmlBody = xmlBody;
    }

    public boolean isParseMultipart() {
        return parseMultipart;
    }

    public void setParseMultipart(boolean parseMultipart) {
        this.parseMultipart = parseMultipart;
    }

    public boolean isIncludeMetadata() {
        return includeMetadata;
    }

    public void setIncludeMetadata(boolean includeMetadata) {
        this.includeMetadata = includeMetadata;
    }

    public String getResponseContentType() {
        return responseContentType;
    }

    public void setResponseContentType(String responseContentType) {
        this.responseContentType = responseContentType;
    }

    public boolean isResponseDataTypeBinary() {
        return responseDataTypeBinary;
    }

    public void setResponseDataTypeBinary(boolean responseDataTypeBinary) {
        this.responseDataTypeBinary = responseDataTypeBinary;
    }

    public String getResponseStatusCode() {
        return responseStatusCode;
    }

    public void setResponseStatusCode(String responseStatusCode) {
        this.responseStatusCode = responseStatusCode;
    }

    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(Map<String, String> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public List<HttpStaticResource> getStaticResources() {
        return staticResources;
    }

    public void setStaticResources(List<HttpStaticResource> staticResources) {
        this.staticResources = staticResources;
    }

    @Override
    public String getProtocol() {
        return "HTTP";
    }

    @Override
    public String getName() {
        return "HTTP Listener";
    }

    @Override
    public String toFormattedString() {
        return null;
    }

    @Override
    public ListenerConnectorProperties getListenerConnectorProperties() {
        return listenerConnectorProperties;
    }

    @Override
    public SourceConnectorProperties getSourceConnectorProperties() {
        return sourceConnectorProperties;
    }

    @Override
    public boolean canBatch() {
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
        boolean xmlBody = !Boolean.parseBoolean(element.removeChild("bodyOnly").getTextContent());
        element.addChildElement("xmlBody", Boolean.toString(xmlBody));
        element.addChildElement("parseMultipart", Boolean.toString(!xmlBody));
        element.addChildElement("includeMetadata", Boolean.toString(xmlBody));

        element.addChildElement("responseDataTypeBinary", "false");

        element.addChildElement("staticResources");
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = super.getPurgedProperties();
        purgedProperties.put("sourceConnectorProperties", sourceConnectorProperties.getPurgedProperties());
        purgedProperties.put("responseContentType", responseContentType);
        purgedProperties.put("responseDataTypeBinary", responseDataTypeBinary);
        purgedProperties.put("responseHeaderChars", responseHeaders.size());
        purgedProperties.put("charset", charset);
        purgedProperties.put("timeout", PurgeUtil.getNumericValue(timeout));
        return purgedProperties;
    }
}
