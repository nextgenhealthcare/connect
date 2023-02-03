package com.mirth.commons.encryption;

public class EncryptionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EncryptionException() {
        super();
    }

    public EncryptionException(Throwable t) {
        super(t);
    }

    public EncryptionException(String message) {
        super(message);
    }
}
