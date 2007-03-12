package com.webreach.mirth.server.util;

import com.webreach.mirth.model.Response;
import com.webreach.mirth.model.Response.Status;

public class ResponseFactory {
	public Response getFailureResponse(String message) {
		return new Response(Status.FAILURE, message);
	}

	public Response getSuccessResponse(String message) {
		return new Response(Status.SUCCESS, message);
	}

	public Response getFilteredResponse(String message) {
		return new Response(Status.FILTERED, message);
	}

	public Response getQueudResponse(String message) {
		return new Response(Status.QUEUED, message);
	}

	public Response getResponse(String message) {
		return new Response(Status.UNKNOWN, message);
	}
}
