/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import java.net.URL;
import java.util.List;

import com.mirth.connect.model.LibraryProperties;

public interface LibraryPlugin extends ResourcePlugin {

    public List<URL> getLibraries(LibraryProperties properties) throws Exception;

    public void update(LibraryProperties properties) throws Exception;

    public void remove(LibraryProperties properties) throws Exception;
}