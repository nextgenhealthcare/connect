package com.mirth.connect.connectors.vm;

public class VMResponse {

    private String message;
    private Throwable exception;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

}
