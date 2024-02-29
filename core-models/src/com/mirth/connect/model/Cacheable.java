/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

public interface Cacheable<T extends Cacheable<T>> {

    public String getId();

    public String getName();

    public Integer getRevision();

    public T cloneIfNeeded();
}