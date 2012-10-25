/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.util.LinkedHashMap;
import java.util.Map;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.QueueConnectorProperties;
import com.mirth.connect.donkey.model.channel.QueueConnectorPropertiesInterface;

public class HttpDispatcherProperties extends ConnectorProperties implements QueueConnectorPropertiesInterface {

    private QueueConnectorProperties queueConnectorProperties;

    private String host;
    private String method;
    private Map<String, String> headers;
    private Map<String, String> parameters;
    private boolean includeHeadersInResponse;
    private boolean multipart;
    private boolean useAuthentication;
    private String authenticationType;
    private String username;
    private String password;
    private String content;
    private String contentType;
    private String charset;
    private String socketTimeout;

    public HttpDispatcherProperties() {
        queueConnectorProperties = new QueueConnectorProperties();

        this.host = "";
        this.method = "post";
        this.headers = new LinkedHashMap<String, String>();
        this.parameters = new LinkedHashMap<String, String>();
        this.includeHeadersInResponse = false;
        this.multipart = false;
        this.useAuthentication = false;
        this.authenticationType = "Basic";
        this.username = "";
        this.password = "";
        this.content = "";
        this.contentType = "text/plain";
        this.charset = "UTF-8";
        this.socketTimeout = "30000";
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public boolean isIncludeHeadersInResponse() {
        return includeHeadersInResponse;
    }

    public void setIncludeHeadersInResponse(boolean includeHeadersInResponse) {
        this.includeHeadersInResponse = includeHeadersInResponse;
    }

    public boolean isMultipart() {
        return multipart;
    }

    public void setMultipart(boolean multipart) {
        this.multipart = multipart;
    }

    public boolean isUseAuthentication() {
        return useAuthentication;
    }

    public void setUseAuthentication(boolean useAuthentication) {
        this.useAuthentication = useAuthentication;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(String socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    @Override
    public String getProtocol() {
        return "HTTP";
    }

    @Override
    public String getName() {
        return "HTTP Sender";
    }

    @Override
    public String toFormattedString() {
        return null;
    }

    @Override
    public QueueConnectorProperties getQueueConnectorProperties() {
        return queueConnectorProperties;
    }
}
