package com.mirth.connect.donkey.model.message;

public abstract class Content {
    private boolean encrypted = false;

    public Content() {}

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public abstract Object getContent();
}
