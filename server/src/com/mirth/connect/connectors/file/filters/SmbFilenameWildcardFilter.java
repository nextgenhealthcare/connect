/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file.filters;

import java.util.regex.Pattern;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFilenameFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SmbFilenameWildcardFilter implements SmbFilenameFilter {
    private static transient Log logger = LogFactory.getLog(SmbFilenameFilter.class);
    private String pattern;
    private boolean isRegex;
    private String[] patterns;

    public SmbFilenameWildcardFilter(String pattern, boolean isRegex) {
        this.pattern = pattern;
        this.isRegex = isRegex;

        if (!isRegex) {
            this.patterns = pattern.trim().split("\\s*,\\s*");
        }
    }

    /**
     * UMOFilter condition decider method.
     * <p/>
     * Returns <code>boolean</code> <code>TRUE</code> if the file conforms to an
     * acceptable pattern or <code>FALSE</code> otherwise.
     * 
     * @param dir
     *            The directory to apply the filter to.
     * @param name
     *            The name of the file to apply the filter to.
     * @return indication of acceptance as boolean.
     */
    public boolean accept(SmbFile dir, String name) {
        if (name == null) {
            logger.warn("The filename and or directory was null");
            return false;
        } else if (isRegex) {
            return Pattern.compile(pattern).matcher(name).matches();
        } else {
            return accept(name);
        }
    }

    // WildcardFilter accept to match the old Mule logic
    private boolean accept(String name) {
        if (name == null) {
            return false;
        }
        String pattern = null;
        boolean match = false;
        for (int x = 0; x < patterns.length; x++) {
            pattern = patterns[x];

            if ("*".equals(pattern) || "**".equals(pattern)) {
                return true;
            }
            int i = pattern.indexOf("*");

            if (i == -1) {
                match = pattern.equals(name);
            } else {
                int i2 = pattern.indexOf("*", i + 1);
                if (i2 > 1) {
                    match = name.indexOf(pattern.substring(1, i2)) > -1;
                } else if (i == 0) {
                    match = name.endsWith(pattern.substring(1));
                } else {
                    match = name.startsWith(pattern.substring(0, i));
                }
            }
            if (match) {
                return true;
            }
        }
        return false;
    }
}
