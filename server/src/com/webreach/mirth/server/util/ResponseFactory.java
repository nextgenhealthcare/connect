package com.webreach.mirth.server.util;

import com.webreach.mirth.model.Response;
import com.webreach.mirth.model.Response.Status;

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
