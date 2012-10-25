/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;

public class Response implements Serializable {
    private static final long serialVersionUID = 99766081218628503L;

    public enum Status {
        SUCCESS, FAILURE, FILTERED, QUEUED, UNKNOWN
    }

    private Status status = Status.UNKNOWN;
    private String message = new String();
    private Object payload = null;

    public Response(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public Response(Status status, String message, Object payload) {
        this.status = status;
        this.message = message;
        this.payload = payload;
    }

    public Response(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String toString() {
        StringBuilder response = new StringBuilder();
        response.append(getStatus().toString());
        response.append(": ");
        response.append(getMessage());

        if (getPayload() != null) {
            response.append(", payload = ");
            response.append(getPayload().toString());
        }

        return response.toString();
    }
}
