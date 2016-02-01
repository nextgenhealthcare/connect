/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.httpauth;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpHeader;

public class AuthenticationResult {

    public enum AuthStatus {
        CHALLENGED, SUCCESS, FAILURE;
    }

    private AuthStatus status;
    private String username = "";
    private String realm = "";
    private Map<String, List<String>> responseHeaders = new LinkedHashMap<String, List<String>>();

    public AuthenticationResult(AuthStatus status) {
        setStatus(status);
    }

    public AuthenticationResult(com.mirth.connect.plugins.httpauth.userutil.AuthenticationResult result) {
        switch (result.getStatus()) {
            case CHALLENGED:
                setStatus(AuthStatus.CHALLENGED);
                break;
            case SUCCESS:
                setStatus(AuthStatus.SUCCESS);
                break;
            case FAILURE:
                setStatus(AuthStatus.FAILURE);
                break;
            default:
                setStatus(null);
        }
        setUsername(result.getUsername());
        setRealm(result.getRealm());
        setResponseHeaders(result.getResponseHeaders());
    }

    public AuthStatus getStatus() {
        return status;
    }

    public void setStatus(AuthStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null.");
        }
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = StringUtils.trimToEmpty(username);
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = StringUtils.trimToEmpty(realm);
    }

    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(Map<String, List<String>> responseHeaders) {
        if (responseHeaders == null) {
            responseHeaders = new LinkedHashMap<String, List<String>>();
        }
        this.responseHeaders = responseHeaders;
    }

    public void addResponseHeader(String key, String value) {
        List<String> list = responseHeaders.get(key);
        if (list == null) {
            list = new ArrayList<String>();
            responseHeaders.put(key, list);
        }
        list.add(value);
    }

    public static AuthenticationResult Challenged(String authenticateHeader) {
        AuthenticationResult result = new AuthenticationResult(AuthStatus.CHALLENGED);
        result.addResponseHeader(HttpHeader.WWW_AUTHENTICATE.asString(), StringUtils.trimToEmpty(authenticateHeader));
        return result;
    }

    public static AuthenticationResult Success() {
        return new AuthenticationResult(AuthStatus.SUCCESS);
    }

    public static AuthenticationResult Success(String username, String realm) {
        AuthenticationResult result = new AuthenticationResult(AuthStatus.SUCCESS);
        result.setUsername(username);
        result.setRealm(realm);
        return result;
    }

    public static AuthenticationResult Failure() {
        return new AuthenticationResult(AuthStatus.FAILURE);
    }
}