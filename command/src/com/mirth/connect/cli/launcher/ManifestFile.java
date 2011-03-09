package com.mirth.connect.cli.launcher;

public class ManifestFile implements ManifestEntry {
    private String file;

    public ManifestFile(String file) {
        this.file = file;
    }

    public String getName() {
        return file;
    }
}