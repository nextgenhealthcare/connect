/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.httpauth;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import com.mirth.connect.userutil.MessageHeaders;
import com.mirth.connect.userutil.MessageParameters;

public class RequestInfo {

    private String remoteAddress;
    private int remotePort;
    private String localAddress;
    private int localPort;
    private String protocol;
    private String method;
    private String requestURI;
    private Map<String, List<String>> headers = new CaseInsensitiveMap<String, List<String>>();
    private Map<String, List<String>> queryParameters = new CaseInsensitiveMap<String, List<String>>();
    private EntityProvider entityProvider;
    private Map<String, Object> extraProperties = new HashMap<String, Object>();

    public RequestInfo(String remoteAddress, int remotePort, String localAddress, int localPort, String protocol, String method, String requestURI, Map<String, List<String>> headers, Map<String, List<String>> queryParameters, EntityProvider entityProvider) {
        this(remoteAddress, remotePort, localAddress, localPort, protocol, method, requestURI, headers, queryParameters, entityProvider, null);
    }

    public RequestInfo(String remoteAddress, int remotePort, String localAddress, int localPort, String protocol, String method, String requestURI, Map<String, List<String>> headers, Map<String, List<String>> queryParameters, EntityProvider entityProvider, Map<String, Object> extraProperties) {
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
        this.localAddress = localAddress;
        this.localPort = localPort;
        this.protocol = protocol;
        this.method = method;
        this.requestURI = requestURI;
        setHeaders(headers);
        setQueryParameters(queryParameters);
        this.entityProvider = entityProvider;
        this.extraProperties = extraProperties;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public int getLocalPort() {
        return localPort;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getMethod() {
        return method;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public Map<String, List<String>> getHeaders() {
        Map<String, List<String>> headers = new CaseInsensitiveMap<String, List<String>>();
        for (Entry<String, List<String>> entry : this.headers.entrySet()) {
            headers.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
        return Collections.unmodifiableMap(headers);
    }

    private void setHeaders(Map<String, List<String>> headers) {
        if (headers == null) {
            headers = new HashMap<String, List<String>>();
        }
        this.headers = new CaseInsensitiveMap<String, List<String>>(headers);
    }

    public Map<String, List<String>> getQueryParameters() {
        Map<String, List<String>> queryParameters = new CaseInsensitiveMap<String, List<String>>();
        for (Entry<String, List<String>> entry : this.queryParameters.entrySet()) {
            queryParameters.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
        }
        return Collections.unmodifiableMap(queryParameters);
    }

    private void setQueryParameters(Map<String, List<String>> queryParameters) {
        if (queryParameters == null) {
            queryParameters = new HashMap<String, List<String>>();
        }
        this.queryParameters = new CaseInsensitiveMap<String, List<String>>(queryParameters);
    }

    public EntityProvider getEntityProvider() {
        return entityProvider;
    }

    public interface EntityProvider {
        public static final String ATTRIBUTE_NAME = "Cached Entity";

        public byte[] getEntity() throws IOException;
    }

    public Map<String, Object> getExtraProperties() {
        return extraProperties;
    }

    public void setExtraProperties(Map<String, Object> extraProperties) {
        this.extraProperties = extraProperties;
    }

    public void populateMap(Map<String, Object> map) {
        map.put("remoteAddress", remoteAddress);
        map.put("remotePort", remotePort);
        map.put("localAddress", localAddress);
        map.put("localPort", localPort);
        map.put("protocol", protocol);
        map.put("method", method);
        map.put("uri", requestURI);
        map.put("headers", new MessageHeaders(headers));
        map.put("parameters", new MessageParameters(queryParameters));

        if (MapUtils.isNotEmpty(extraProperties)) {
            map.putAll(extraProperties);
        }
    }
}