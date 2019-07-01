/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This class represents a channel or destination response and is used to retrieve details such as
 * the response data, message status, and errors.
 */
@XStreamAlias("response")
public class Response implements Serializable {
    private static final long serialVersionUID = 99766081218628503L;
    private Status status;
    private String message = new String();
    private String error = new String();
    private String statusMessage = new String();
    private transient boolean validate = false;

    /**
     * Instantiates a new Response object.
     */
    public Response() {
        this(new String());
    }

    /**
     * Instantiates a new Response object.
     * 
     * @param message
     *            The actual response data.
     */
    public Response(String message) {
        this(null, message);
    }

    /**
     * Instantiates a new Response object.
     * 
     * @param status
     *            The status (e.g. SENT, ERROR) of the response.
     * @param message
     *            The actual response data.
     */
    public Response(Status status, String message) {
        this(status, message, new String());
    }

    /**
     * Instantiates a new Response object.
     * 
     * @param status
     *            The status (e.g. SENT, ERROR) of the response.
     * @param message
     *            The actual response data.
     * @param statusMessage
     *            A brief message explaining the reason for the current status.
     */
    public Response(Status status, String message, String statusMessage) {
        this(status, message, statusMessage, new String());
    }

    /**
     * Instantiates a new Response object.
     * 
     * @param status
     *            The status (e.g. SENT, ERROR) of the response.
     * @param message
     *            The actual response data.
     * @param statusMessage
     *            A brief message explaining the reason for the current status.
     * @param error
     *            The error string associated with this response, if applicable.
     */
    public Response(Status status, String message, String statusMessage, String error) {
        this(status, message, statusMessage, error, false);
    }

    /**
     * Instantiates a new Response object.
     * 
     * @param status
     *            The status (e.g. SENT, ERROR) of the response.
     * @param message
     *            The actual response data.
     * @param statusMessage
     *            A brief message explaining the reason for the current status.
     * @param error
     *            The error string associated with this response, if applicable.
     */
    public Response(Status status, String message, String statusMessage, String error, boolean validate) {
        this.status = status;
        setMessage(message);
        setStatusMessage(statusMessage);
        setError(error);
        setValidate(validate);
    }

    /**
     * Returns the actual response data, as a string.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the response data.
     * 
     * @param message
     *            The response data (String) to use.
     */
    public void setMessage(String message) {
        /*
         * Message is not allowed to be null because it is stored in the database as a string and
         * would cause a null pointer exception in the serializer. This setter must be used to set
         * the message, even in the constructors.
         */
        this.message = message == null ? "" : message;
    }

    /**
     * Returns the Status (e.g. SENT, QUEUED) of this response.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the status of this response.
     * 
     * @param status
     *            The status (e.g. SENT, QUEUED) to use for this response.
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Returns the error string associated with this response, if it exists.
     */
    public String getError() {
        return error;
    }

    /**
     * Sets the error string to be associated with this response.
     * 
     * @param error
     *            The error string to use.
     */
    public void setError(String error) {
        this.error = error;
    }

    /**
     * Returns a brief message explaining the reason for the current status.
     */
    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * Sets the status message to use for this response.
     * 
     * @param statusMessage
     *            A brief message explaining the reason for the current status.
     */
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public boolean isValidate() {
        return validate;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
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

    /**
     * If necessary, modifies the status of this response in accordance with the messaging engine
     * and returns a string explaining why the change was made.
     * 
     * @param queueEnabled
     *            If false, the QUEUED status is considered invalid for this response, and should be
     *            modified.
     * @return A reason string if the status was changed, otherwise null.
     */
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
