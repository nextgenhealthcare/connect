/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.net.URL;
import java.util.List;
import java.util.Set;

import com.mirth.connect.model.LibraryProperties;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

public abstract class ContextFactoryController extends Controller {

    public abstract void initGlobalContextFactory();

    public abstract void updateResources(List<LibraryProperties> resources) throws Exception;

    public abstract MirthContextFactory getGlobalContextFactory();

    public abstract MirthContextFactory getGlobalScriptContextFactory() throws Exception;

    public abstract MirthContextFactory getContextFactory(Set<String> libraryResourceIds) throws Exception;

    public abstract void reloadResource(String resourceId) throws Exception;

    public abstract List<URL> getLibraries(String resourceId) throws Exception;
}