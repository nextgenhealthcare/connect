package com.mirth.connect.donkey.util.xstream;

public class SerializerException extends RuntimeException {
    public SerializerException(String message) {
        super(message);
    }

    public SerializerException(Throwable cause) {
        super(cause);
    }

    public SerializerException(String message, Throwable cause) {
        super(message, cause);
    }
}
