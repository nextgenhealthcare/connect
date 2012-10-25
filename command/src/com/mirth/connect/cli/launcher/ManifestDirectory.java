/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.cli.launcher;

public class ManifestDirectory implements ManifestEntry {
    private String dir;
    private String[] excludes = new String[0];

    public ManifestDirectory(String dir) {
        this.dir = dir;
    }

    public String getName() {
        return dir;
    }

    public String[] getExcludes() {
        return excludes;
    }

    public void setExcludes(String[] excludes) {
        this.excludes = excludes;
    }
}