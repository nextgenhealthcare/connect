package com.webreach.mirth.connectors.file.filters;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

public class RegexFilenameFilter implements FilenameFilter {
    private String pattern;
    
    public RegexFilenameFilter(String pattern) {
        this.pattern = pattern;
    }
    
    public boolean accept(File dir, String filename) {
        return Pattern.compile(pattern).matcher(filename).matches();
    }

}
