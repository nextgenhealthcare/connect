package com.webreach.mirth.util;

public class ConnectionTestResponse {
    public enum Type {
        SUCCESS, TIME_OUT, FAILURE
    }

    private String message;
    private Type type;

    public ConnectionTestResponse(Type type, String message) {
        this.type = type;
        this.message = message;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
