package com.mirth.connect.server.alert.action;

@SuppressWarnings("serial")
public class DispatchException extends Exception {
    public DispatchException(String message) {
        super(message);
    }

    public DispatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public DispatchException(Throwable cause) {
        super(cause);
    }
}
