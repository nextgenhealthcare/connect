/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file.filters;

import jcifs.smb.SmbFile;

public class SmbRegexFilenameFilter extends RegexFilenameFilter {
    public SmbRegexFilenameFilter(String pattern) {
        super(pattern);
    }

    public boolean accept(SmbFile dir, String filename) {
        return super.accept(null, filename);
    }
}
