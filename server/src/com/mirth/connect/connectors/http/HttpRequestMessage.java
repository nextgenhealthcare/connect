/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.util.Map;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.http.entity.ContentType;

public class HttpRequestMessage {
    private String method;
    private Object content;
    private Map<String, String> headers;
    private CaseInsensitiveMap caseInsensitiveHeaders;
    private Map<String, Object> parameters;
    private ContentType contentType;
    private String remoteAddress;
    private String queryString;
    private String requestUrl;
    private String contextPath;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getCaseInsensitiveHeaders() {
        return caseInsensitiveHeaders;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
        caseInsensitiveHeaders = new CaseInsensitiveMap(headers);
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }
}
