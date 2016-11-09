/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.userutil;

import java.util.List;

/**
 * Convenience class to allow fluent building of lists.
 */
@SuppressWarnings("rawtypes")
public class Lists {

    /**
     * Instantiates a new {@link ListBuilder} using an ArrayList.
     * 
     * @return The new {@link ListBuilder} instance.
     */
    public static ListBuilder list() {
        return new ListBuilder();
    }

    /**
     * Instantiates a new {@link ListBuilder} using an ArrayList and the given element.
     * 
     * @param e
     *            element to be appended to this list
     * @return The new {@link ListBuilder} instance.
     */
    public static ListBuilder list(Object e) {
        return new ListBuilder(e);
    }

    /**
     * Instantiates a new {@link ListBuilder} using the given map.
     * 
     * @return The new {@link ListBuilder} instance.
     */
    public static ListBuilder list(List list) {
        return new ListBuilder(list);
    }
}