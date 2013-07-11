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
    private boolean isRegex;
    private String[] wildcardPatterns;
    private Pattern regexPattern;

    public SmbFilenameWildcardFilter(String pattern, boolean isRegex) {
        this.isRegex = isRegex;

        if (isRegex) {
            regexPattern = Pattern.compile(pattern);
        } else {
            wildcardPatterns = pattern.trim().split("\\s*,\\s*");
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
            return regexPattern.matcher(name).matches();
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

        for (int x = 0; x < wildcardPatterns.length; x++) {
            pattern = wildcardPatterns[x];

            if ("*".equals(pattern) || "**".equals(pattern)) {
                return true;
            }

            int i = pattern.indexOf("*");

            if (i == -1) {
                // If there is no *, just check for equality
                match = pattern.equals(name);
            } else {
                String partialName = name;
                String partialPattern = pattern;
                boolean partialMatch = true;
                boolean first = true;

                while (i != -1) {
                    // Get the substring before the *
                    String prefix = partialPattern.substring(0, i);
                    // Get and save the substring after the *
                    partialPattern = partialPattern.substring(i + 1);

                    if (!prefix.isEmpty()) {
                        int prefixIndex = partialName.indexOf(prefix);

                        if (first && prefixIndex == 0) {
                            // See if the partial name starts with the prefix
                            partialName = partialName.substring(prefix.length());
                        } else if (!first && prefixIndex >= 0) {
                            // See if the partial name contains the prefix
                            partialName = partialName.substring(prefixIndex + prefix.length());
                        } else {
                            partialMatch = false;
                            break;
                        }
                    }

                    // Get the next *
                    i = partialPattern.indexOf("*");
                    first = false;
                }

                // Check for trailing partial patterns
                if (partialMatch && !partialPattern.isEmpty() && !partialName.endsWith(partialPattern)) {
                    partialMatch = false;
                }

                match = partialMatch;
            }

            if (match) {
                return true;
            }
        }

        return false;
    }
}
