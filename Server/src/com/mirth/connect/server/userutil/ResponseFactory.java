/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import com.mirth.connect.userutil.Response;
import com.mirth.connect.userutil.Status;

/**
 * Provides methods to create Response objects.
 */
public class ResponseFactory {

    private ResponseFactory() {}

    /**
     * Returns a Response representing a successfully sent message.
     * 
     * @param message
     *            The response data to store.
     * @return The instantiated Response object.
     */
    public static Response getSentResponse(String message) {
        return new Response(Status.SENT, message);
    }

    /**
     * Returns a Response representing a erred message.
     * 
     * @param message
     *            The response data to store.
     * @return The instantiated Response object.
     */
    public static Response getErrorResponse(String message) {
        return new Response(Status.ERROR, message);
    }

    /**
     * Returns a Response representing a filtered message.
     * 
     * @param message
     *            The response data to store.
     * @return The instantiated Response object.
     */
    public static Response getFilteredResponse(String message) {
        return new Response(Status.FILTERED, message);
    }

    /**
     * Returns a Response representing a queued message.
     * 
     * @param message
     *            The response data to store.
     * @return The instantiated Response object.
     */
    public static Response getQueuedResponse(String message) {
        return new Response(Status.QUEUED, message);
    }
}
