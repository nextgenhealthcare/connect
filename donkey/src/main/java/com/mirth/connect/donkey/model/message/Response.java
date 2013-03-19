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

public class Response implements Serializable {
    private static final long serialVersionUID = 99766081218628503L;

    public static Response fromString(String response) {
        if (response == null) {
            return null;
        }

        int index = response.indexOf(":");

        if (index == -1) {
            return null;
        }

        String statusString = StringUtils.trim(response.substring(0, index));
        String message = StringUtils.trim(response.substring(index + 2));
        Status newMessageStatus = null;

        if (statusString.equals("SENT")) {
            newMessageStatus = Status.SENT;
        } else if (statusString.equals("ERROR")) {
            newMessageStatus = Status.ERROR;
        } else if (statusString.equals("FILTERED")) {
            newMessageStatus = Status.FILTERED;
        } else if (statusString.equals("QUEUED")) {
            newMessageStatus = Status.QUEUED;
        } else {
            return null;
        }

        return new Response(newMessageStatus, message);
    }

    private Status newMessageStatus;
    private String message = new String();
    private String error = new String();

    public Response() {}

    public Response(String message) {
        setMessage(message);
    }

    public Response(Status newMessageStatus, String message) {
        this.newMessageStatus = newMessageStatus;
        setMessage(message);
    }

    public Response(Status newMessageStatus, String message, String error) {
        this.newMessageStatus = newMessageStatus;
        setMessage(message);
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

    public Status getNewMessageStatus() {
        return newMessageStatus;
    }

    public void setNewMessageStatus(Status newMessageStatus) {
        this.newMessageStatus = newMessageStatus;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String toString() {
        StringBuilder response = new StringBuilder();
        response.append(getNewMessageStatus().toString());
        response.append(": ");
        response.append(getMessage());

        return response.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Response) {
            Response otherResponse = (Response) other;
            if (newMessageStatus == otherResponse.getNewMessageStatus() && message.equals(otherResponse.getMessage())) {
                return true;
            }
        }

        return false;
    }

    public void fixStatus(boolean queueEnabled) {
        Status status = getNewMessageStatus();

        if (status != Status.ERROR && status != Status.SENT && status != Status.QUEUED) {
            // If the response is invalid for a final destination status, change the status to ERROR
            setNewMessageStatus(Status.ERROR);
            setError("Invalid response status: " + status + ". Status updated to ERROR.");
        } else if (!queueEnabled && status == Status.QUEUED) {
            // If the status is QUEUED and queuing is disabled, change the status to ERROR
            setNewMessageStatus(Status.ERROR);
            setError("Invalid response status. Cannot set status to QUEUED while queuing is disabled. Status updated to ERROR.");
        }
    }
}
