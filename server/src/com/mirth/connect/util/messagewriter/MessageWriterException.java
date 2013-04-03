package com.mirth.connect.util.messagewriter;

public class MessageWriterException extends Exception {
    public MessageWriterException(String message) {
        super(message);
    }

    public MessageWriterException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageWriterException(Throwable cause) {
        super(cause);
    }
}
