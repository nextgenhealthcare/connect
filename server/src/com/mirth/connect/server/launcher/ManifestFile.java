package com.mirth.connect.server.launcher;

public class ManifestFile implements ManifestEntry {
    private String file;

    public ManifestFile(String file) {
        this.file = file;
    }

    public String getName() {
        return file;
    }
}