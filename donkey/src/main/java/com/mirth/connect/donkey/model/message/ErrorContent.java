package com.mirth.connect.donkey.model.message;

public class ErrorContent {
    private String error = null;
    private transient boolean persisted = false;

    public ErrorContent() {

    }
    
    public ErrorContent(String error, boolean persisted) {
        this.error = error;
        this.persisted = persisted;
    }

    public String getError() {
        return error;
    }

    protected void setError(String error) {
        this.error = error;
    }

    public boolean isPersisted() {
        return persisted;
    }

    public void setPersisted(boolean persisted) {
        this.persisted = persisted;
    }
}
