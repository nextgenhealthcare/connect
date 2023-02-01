package com.mirth.commons.encryption;

import java.security.Provider;

public abstract class Encryptor {
    private Provider provider;
    private Output format = Output.BASE64;
    private boolean initialized = false;

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Output getFormat() {
        return format;
    }

    public void setFormat(Output format) {
        this.format = format;
    }

    public boolean isInitialized() {
        return initialized;
    }

    protected void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public abstract void initialize() throws EncryptionException;

    public abstract String decrypt(String message) throws EncryptionException;

    public abstract String encrypt(String message) throws EncryptionException;
}
