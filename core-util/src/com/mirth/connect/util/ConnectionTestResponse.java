/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

public class ConnectionTestResponse {
    public enum Type {
        SUCCESS, TIME_OUT, FAILURE
    }

    private Type type;
    private String message;
    private String connectionInfo;

    public ConnectionTestResponse(Type type, String message) {
        this(type, message, null);
    }

    public ConnectionTestResponse(Type type, String message, String connectionInfo) {
        this.type = type;
        this.message = message;
        this.connectionInfo = connectionInfo;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getConnectionInfo() {
        return connectionInfo;
    }

    public void setConnectionInfo(String connectionInfo) {
        this.connectionInfo = connectionInfo;
    }
}