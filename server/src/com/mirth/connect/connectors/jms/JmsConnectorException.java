package com.mirth.connect.connectors.jms;

public class JmsConnectorException extends Exception {
    public JmsConnectorException(String message) {
        super(message);
    }

    public JmsConnectorException(Throwable cause) {
        super(cause);
    }

    public JmsConnectorException(String message, Throwable cause) {
        super(message, cause);
    }
}
