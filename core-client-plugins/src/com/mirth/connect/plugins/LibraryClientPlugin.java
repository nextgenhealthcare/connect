/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

public abstract class LibraryClientPlugin extends ResourceClientPlugin {

    public LibraryClientPlugin(String pluginName) {
        super(pluginName);
    }

    public boolean singleSelectionOnly() {
        return false;
    }

    public String[] getUnselectableTransportNames() {
        return new String[0];
    }
}