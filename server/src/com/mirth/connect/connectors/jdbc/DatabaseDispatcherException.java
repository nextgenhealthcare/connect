package com.mirth.connect.connectors.jdbc;

public class DatabaseDispatcherException extends Exception {
    public DatabaseDispatcherException(String message) {
        super(message);
    }

    public DatabaseDispatcherException(Throwable cause) {
        super(cause);
    }

    public DatabaseDispatcherException(String message, Throwable cause) {
        super(message, cause);
    }
}
