/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.libraryresource;

import com.mirth.connect.model.LibraryProperties;

public class LibraryResourceProperties extends LibraryProperties {

    public static final String PLUGIN_POINT = "Library Resource";
    public static final String TYPE = "Library";

    private String directory;

    public LibraryResourceProperties() {
        super(PLUGIN_POINT, TYPE);
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }
}