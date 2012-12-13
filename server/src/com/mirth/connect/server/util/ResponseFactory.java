/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;

public class ResponseFactory {
    public static Response getSentResponse(String message) {
        return new Response(Status.SENT, message);
    }

    /**
     * This method has been deprecated; use getSentResponse instead
     */
    // TODO: Decide whether we want to remove/convert this
    @Deprecated
    public static Response getSuccessResponse(String message) {
        return new Response(Status.SENT, message);
    }

    public static Response getErrorResponse(String message) {
        return new Response(Status.ERROR, message);
    }

    /**
     * This method has been deprecated; use getErrorResponse instead
     */
    // TODO: Decide whether we want to remove/convert this
    @Deprecated
    public static Response getFailureResponse(String message) {
        return new Response(Status.ERROR, message);
    }

    public static Response getFilteredResponse(String message) {
        return new Response(Status.FILTERED, message);
    }

    public static Response getQueuedResponse(String message) {
        return new Response(Status.QUEUED, message);
    }
}
