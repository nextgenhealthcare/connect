package com.webreach.mirth.util;

public enum ConnectionTestResponse {
    SUCCESS, TIME_OUT, FAILURE;

    private String message;

    ConnectionTestResponse() {

    }

    ConnectionTestResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
