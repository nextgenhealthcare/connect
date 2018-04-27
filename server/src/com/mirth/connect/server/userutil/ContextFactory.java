/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.util.Set;

import com.mirth.connect.server.util.javascript.MirthContextFactory;

/**
 * Allows the user to retrieve information about the current JavaScript context.
 */
public class ContextFactory {

    private MirthContextFactory delegate;

    /**
     * Instantiates a new ContextFactory object.
     * 
     * @param delegate
     *            The underlying ContextFactory this class will delegate to.
     */
    public ContextFactory(MirthContextFactory delegate) {
        this.delegate = delegate;
    }

    /**
     * Returns the set of custom resource IDs that the current JavaScript context is using. If no
     * custom libraries are being used in the current JavaScript context, this will return an empty
     * set.
     * 
     * @return The set of custom resource IDs that the current JavaScript context is using.
     */
    public Set<String> getResourceIds() {
        return delegate.getResourceIds();
    }

    /**
     * Returns the application classloader that the current JavaScript context is using.
     * 
     * @return The application classloader that the current JavaScript context is using.
     */
    public ClassLoader getClassLoader() {
        return delegate.getApplicationClassLoader();
    }

    /**
     * Returns a classloader containing only the libraries contained in the custom resources, with
     * no parent classloader. If no custom libraries are being used in the current JavaScript
     * context, this will return null.
     * 
     * @return A classloader containing only the libraries contained in the custom resources, with
     *         no parent classloader.
     */
    public ClassLoader getIsolatedClassLoader() {
        return delegate.getIsolatedClassLoader();
    }
}