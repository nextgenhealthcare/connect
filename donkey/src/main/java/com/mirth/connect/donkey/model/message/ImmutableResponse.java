/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message;

/**
 * This class represents a destination response and is used to retrieve details
 * such as the response data, message status, and errors.
 */
public class ImmutableResponse {
    private Response response;

    /**
     * Instantiates a new ImmutableResponse.
     * 
     * @param response
     *            The Response object that this object will reference for
     *            retrieving data.
     */
    public ImmutableResponse(Response response) {
        this.response = response;
    }

    /**
     * Returns the actual response data, as a string.
     */
    public String getMessage() {
        return response.getMessage();
    }

    /**
     * Returns the Status (e.g. SENT, QUEUED) of this response, which will be
     * used to set the status of the corresponding connector message.
     */
    public Status getNewMessageStatus() {
        return response.getStatus();
    }

    /**
     * Returns the error string associated with this response, if it exists.
     */
    public String getError() {
        return response.getError();
    }

    /**
     * Returns a brief message explaining the reason for the current status.
     */
    public String getStatusMessage() {
        return response.getStatusMessage();
    }
}
