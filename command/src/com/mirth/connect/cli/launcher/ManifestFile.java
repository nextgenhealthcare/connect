/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

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