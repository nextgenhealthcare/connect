/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import com.mirth.connect.model.Response;
import com.mirth.connect.model.Response.Status;

public class ResponseFactory {
	public static Response getFailureResponse(String message) {
		return new Response(Status.FAILURE, message);
	}

	public static Response getSuccessResponse(String message) {
		return new Response(Status.SUCCESS, message);
	}

	public static Response getFilteredResponse(String message) {
		return new Response(Status.FILTERED, message);
	}

	public static Response getQueudResponse(String message) {
		return new Response(Status.QUEUED, message);
	}

	public static Response getResponse(String message) {
		return new Response(Status.UNKNOWN, message);
	}
}
