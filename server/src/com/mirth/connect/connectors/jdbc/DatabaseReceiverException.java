package com.mirth.connect.connectors.jdbc;

public class DatabaseReceiverException extends Exception {
    public DatabaseReceiverException(String message) {
        super(message);
    }

    public DatabaseReceiverException(Throwable cause) {
        super(cause);
    }

    public DatabaseReceiverException(String message, Throwable cause) {
        super(message, cause);
    }
}
