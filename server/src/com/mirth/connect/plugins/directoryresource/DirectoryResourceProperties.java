/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.directoryresource;

import com.mirth.connect.model.LibraryProperties;

public class DirectoryResourceProperties extends LibraryProperties {

    public static final String PLUGIN_POINT = "Directory Resource";
    public static final String TYPE = "Directory";

    private String directory;

    public DirectoryResourceProperties() {
        super(PLUGIN_POINT, TYPE);
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }
}