/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.userutil;

import com.mirth.connect.util.MirthJsonUtil;

/**
 * Provides JSON utility methods.
 */
public class JsonUtil {
    private JsonUtil() {}

    /**
     * Formats an JSON string with indented markup.
     * 
     * @param input
     *            The JSON string to format.
     * @return The formatted JSON string.
     */
    public static String prettyPrint(String input) {
        return MirthJsonUtil.prettyPrint(input);
    }

    /**
     * Escapes any special JSON characters in the input.
     * 
     * @param input
     *            The string to escape.
     * @return The escaped string.
     */
    public static String escape(String input) {
        return MirthJsonUtil.escape(input);
    }
}
