/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

/**
 * This class represents a channel or destination response and is used to
 * retrieve details such as the response data, message status, and errors.
 */
public class Response {
    private com.mirth.connect.donkey.model.message.Response response;

    /**
     * Instantiates a new Response object.
     */
    public Response() {
        response = new com.mirth.connect.donkey.model.message.Response();
    }

    /**
     * Instantiates a new Response object.
     * 
     * @param message
     *            The actual response data.
     */
    public Response(String message) {
        response = new com.mirth.connect.donkey.model.message.Response(message);
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
        response = new com.mirth.connect.donkey.model.message.Response(status.toDonkeyStatus(), message);
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
        response = new com.mirth.connect.donkey.model.message.Response(status.toDonkeyStatus(), message, statusMessage);
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
        response = new com.mirth.connect.donkey.model.message.Response(status.toDonkeyStatus(), message, statusMessage, error);
    }

    /**
     * Instantiates a new Response object.
     * 
     * NOTE: This should be excluded from the public Javadoc.
     * 
     * @param response
     *            The underlying Donkey Response object to reference.
     */
    public Response(com.mirth.connect.donkey.model.message.Response response) {
        this.response = response;
    }

    /**
     * Returns the actual response data, as a string.
     */
    public String getMessage() {
        return response.getMessage();
    }

    /**
     * Sets the response data.
     * 
     * @param message
     *            The response data (String) to use.
     */
    public void setMessage(String message) {
        response.setMessage(message);
    }

    /**
     * Returns the Status (e.g. SENT, QUEUED) of this response.
     */
    public Status getStatus() {
        return Status.fromDonkeyStatus(response.getStatus());
    }

    /**
     * Sets the status of this response.
     * 
     * @param status
     *            The status (e.g. SENT, QUEUED) to use for this response.
     */
    public void setStatus(Status status) {
        response.setStatus(status.toDonkeyStatus());
    }

    /**
     * Returns the error string associated with this response, if it exists.
     */
    public String getError() {
        return response.getError();
    }

    /**
     * Sets the error string to be associated with this response.
     * 
     * @param error
     *            The error string to use.
     */
    public void setError(String error) {
        response.setError(error);
    }

    /**
     * Returns a brief message explaining the reason for the current status.
     */
    public String getStatusMessage() {
        return response.getStatusMessage();
    }

    /**
     * Sets the status message to use for this response.
     * 
     * @param statusMessage
     *            A brief message explaining the reason for the current status.
     */
    public void setStatusMessage(String statusMessage) {
        response.setStatusMessage(statusMessage);
    }

    com.mirth.connect.donkey.model.message.Response getDonkeyResponse() {
        return response;
    }

    /**
     * Indicates that the underlying Donkey Response objects are "equal".
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof Response && other != null) {
            return response.equals(((Response) other).getDonkeyResponse());
        }

        return false;
    }

    @Override
    public String toString() {
        return response.toString();
    }
}
