/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;

public class ResponseFactory {
    private static Logger logger = Logger.getLogger(ResponseFactory.class);

    public static Response getSentResponse(String message) {
        return new Response(Status.SENT, message);
    }

    /**
     * This method has been deprecated; use getSentResponse instead
     */
    // TODO: Remove in 3.1
    @Deprecated
    public static Response getSuccessResponse(String message) {
        logger.error("The getSuccessResponse(message) method is deprecated and will soon be removed. Please use getSentResponse(message) instead.");
        return new Response(Status.SENT, message);
    }

    public static Response getErrorResponse(String message) {
        return new Response(Status.ERROR, message);
    }

    /**
     * This method has been deprecated; use getErrorResponse instead
     */
    // TODO: Remove in 3.1
    @Deprecated
    public static Response getFailureResponse(String message) {
        logger.error("The getFailureResponse(message) method is deprecated and will soon be removed. Please use getErrorResponse(message) instead.");
        return new Response(Status.ERROR, message);
    }

    public static Response getFilteredResponse(String message) {
        return new Response(Status.FILTERED, message);
    }

    /**
     * This method has been deprecated; use getQueuedResponse instead
     */
    // TODO: Remove in 3.1
    @Deprecated
    public static Response getQueudResponse(String message) {
        logger.error("The getQueudResponse(message) method is deprecated and will soon be removed. Please use getQueuedResponse(message) instead.");
        return new Response(Status.QUEUED, message);
    }

    public static Response getQueuedResponse(String message) {
        return new Response(Status.QUEUED, message);
    }

    // TODO: Remove in 3.1
    @Deprecated
    public static Response getResponse(String message) {
        logger.error("The getResponse(message) method is deprecated and will soon be removed. The UNKNOWN status has also been removed; this method will return a response with the SENT status instead.");
        return new Response(Status.SENT, message);
    }
}
