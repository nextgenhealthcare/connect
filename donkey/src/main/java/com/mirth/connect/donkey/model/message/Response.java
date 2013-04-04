/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("response")
public class Response implements Serializable {
    private static final long serialVersionUID = 99766081218628503L;
    private Status status;
    private String message = new String();
    private String error = new String();
    private String statusMessage = new String();

    public Response() {
        this(new String());
    }

    public Response(String message) {
        this(null, message);
    }

    public Response(Status status, String message) {
        this(status, message, new String());
    }

    public Response(Status status, String message, String statusMessage) {
        this(status, message, statusMessage, new String());
    }

    public Response(Status status, String message, String statusMessage, String error) {
        this.status = status;
        setMessage(message);
        setStatusMessage(statusMessage);
        setError(error);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        /*
         * message is not allowed to be null because it is stored in the database as a string and
         * would cause a null pointer exception in the serializer. This setter must be used to set
         * the message, even in the constructors.
         */
        this.message = message == null ? "" : message;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Response) {
            Response otherResponse = (Response) other;
            if (status == otherResponse.getStatus() && message.equals(otherResponse.getMessage()) && statusMessage.equals(otherResponse.getStatusMessage()) && error.equals(otherResponse.getError())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(status);
        if (StringUtils.isNotEmpty(statusMessage)) {
            builder.append(": ");
            builder.append(statusMessage);
        }

        return builder.toString();
    }

    public String fixStatus(boolean queueEnabled) {
        Status status = getStatus();

        if (status != Status.ERROR && status != Status.SENT && status != Status.QUEUED) {
            // If the response is invalid for a final destination status, change the status to ERROR
            setStatus(Status.ERROR);
            return "Invalid response status: " + status + ". Status updated to ERROR.";
        } else if (!queueEnabled && status == Status.QUEUED) {
            // If the status is QUEUED and queuing is disabled, change the status to ERROR
            setStatus(Status.ERROR);
            return "Invalid response status. Cannot set status to QUEUED while queuing is disabled. Status updated to ERROR.";
        }
        
        return null;
    }
}
