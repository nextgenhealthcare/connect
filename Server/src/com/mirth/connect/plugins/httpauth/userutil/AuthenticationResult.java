/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.httpauth.userutil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * This class represents the result of an HTTP authentication attempt, used to accept or reject
 * requests coming into HTTP-based source connectors.
 */
public class AuthenticationResult {

    private AuthStatus status;
    private String username;
    private String realm;
    private Map<String, List<String>> responseHeaders = new LinkedHashMap<String, List<String>>();

    /**
     * Instantiates a new AuthenticationResult object.
     * 
     * @param status
     *            The accept/reject status to use.
     */
    public AuthenticationResult(AuthStatus status) {
        setStatus(status);
    }

    /**
     * Returns the accept/reject status of the authentication attempt.
     * 
     * @return The accept/reject status of the authentication attempt.
     */
    public AuthStatus getStatus() {
        return status;
    }

    /**
     * Sets the accept/reject status of the authentication attempt.
     * 
     * @param status
     *            The accept/reject status to use.
     */
    public void setStatus(AuthStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null.");
        }
        this.status = status;
    }

    /**
     * Returns the username that the request has been authenticated with.
     * 
     * @return The username that the request has been authenticated with.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username that the request has been authenticated with.
     * 
     * @param username
     *            The username that the request has been authenticated with.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the realm that the request has been authenticated with.
     * 
     * @return The realm that the request has been authenticated with.
     */
    public String getRealm() {
        return realm;
    }

    /**
     * Sets the realm that the request has been authenticated with.
     * 
     * @param realm
     *            The realm that the request has been authenticated with.
     */
    public void setRealm(String realm) {
        this.realm = realm;
    }

    /**
     * Returns the map of HTTP headers to be sent along with the authentication response.
     * 
     * @return The map of HTTP headers to be sent along with the authentication response.
     */
    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

    /**
     * Sets the map of HTTP headers to be sent along with the authentication response.
     * 
     * @param responseHeaders
     *            The map of HTTP headers to be sent along with the authentication response.
     */
    public void setResponseHeaders(Map<String, List<String>> responseHeaders) {
        if (responseHeaders == null) {
            responseHeaders = new LinkedHashMap<String, List<String>>();
        }
        this.responseHeaders = responseHeaders;
    }

    /**
     * Adds a new response header to be sent along with the authentication response.
     * 
     * @param key
     *            The name of the header.
     * @param value
     *            The value of the header.
     */
    public void addResponseHeader(String key, String value) {
        List<String> list = responseHeaders.get(key);
        if (list == null) {
            list = new ArrayList<String>();
            responseHeaders.put(key, list);
        }
        list.add(value);
    }

    /**
     * Convenience method to create a new AuthenticationResult with the CHALLENGED status.
     * 
     * @param authenticateHeader
     *            The value to include in the WWW-Authenticate response header.
     * @return The created AuthenticationResult object.
     */
    public static AuthenticationResult Challenged(String authenticateHeader) {
        AuthenticationResult result = new AuthenticationResult(AuthStatus.CHALLENGED);
        result.addResponseHeader("WWW-Authenticate", StringUtils.trimToEmpty(authenticateHeader));
        return result;
    }

    /**
     * Convenience method to create a new AuthenticationResult with the SUCCESS status.
     * 
     * @return The created AuthenticationResult object.
     */
    public static AuthenticationResult Success() {
        return new AuthenticationResult(AuthStatus.SUCCESS);
    }

    /**
     * Convenience method to create a new AuthenticationResult with the SUCCESS status.
     * 
     * @param username
     *            The username that the request has been authenticated with.
     * @param realm
     *            The realm that the request has been authenticated with.
     * @return The created AuthenticationResult object.
     */
    public static AuthenticationResult Success(String username, String realm) {
        AuthenticationResult result = new AuthenticationResult(AuthStatus.SUCCESS);
        result.setUsername(username);
        result.setRealm(realm);
        return result;
    }

    /**
     * Convenience method to create a new AuthenticationResult with the FAILURE status.
     * 
     * @return The created AuthenticationResult object.
     */
    public static AuthenticationResult Failure() {
        return new AuthenticationResult(AuthStatus.FAILURE);
    }
}