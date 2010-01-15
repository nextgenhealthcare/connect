package com.webreach.mirth.connectors.file.filters;

import jcifs.smb.SmbFile;

public class SmbRegexFilenameFilter extends RegexFilenameFilter {
    public SmbRegexFilenameFilter(String pattern) {
        super(pattern);
    }

    public boolean accept(SmbFile dir, String filename) {
        return super.accept(null, filename);
    }
}
