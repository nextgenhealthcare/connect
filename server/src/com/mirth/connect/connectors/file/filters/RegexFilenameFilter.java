/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file.filters;

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
