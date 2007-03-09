package com.webreach.mirth.model;

public class Response {
	public enum Status {
		SUCCESS, FAIL, UKNOWN
	}
	private Status status;
	private String message;
	public Response(Status status, String message){
		this.status = status;
		this.message = message;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}

}
