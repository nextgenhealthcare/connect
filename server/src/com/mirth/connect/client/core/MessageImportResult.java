package com.mirth.connect.client.core;

public class MessageImportResult {
    private int imported = 0;
    private int errored = 0;

    public int getImported() {
        return imported;
    }
    
    public void addImported(int addend) {
        imported += addend;
    }

    public int getErrored() {
        return errored;
    }
    
    public void addErrored(int addend) {
        errored += addend;
    }
}
