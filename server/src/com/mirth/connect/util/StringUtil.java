/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.util;

import org.apache.commons.lang.StringUtils;

public class StringUtil {
    public static String convertLFtoCR(String str) {
        return str.replaceAll("\\r\\n|\\n", "\r");
    }

    // Four ways to specify character values and string values
    // 1. Literal
    // 2. Quoted literal (turns off escape processing except for standard escape sequences)
    // 3. Standard escape sequences (e.g. \n, \r, \t)
    // 4. Hex notation (e.g. 0xyy)
    public static String unescape(String s) {

        // If null or empty, return the string
        if (StringUtils.isEmpty(s)) {
            return s;
        }

        // If the value is bracket delimited in double quotes, remove the quotes and treat the rest as a literal
        if (s.length() >= 2 && s.substring(0, 1).equals("\"") && s.substring(s.length() - 1, s.length()).equals("\"")) {
            return s.substring(1, s.length() - 1);
        }

        // Standard escape sequence substitutions for non-printable characters (excludes printable characters: \ " ')
        s = s.replace("\\b", "\b");
        s = s.replace("\\t", "\t");
        s = s.replace("\\n", "\n");
        s = s.replace("\\f", "\f");
        s = s.replace("\\r", "\r");

        // Substitute hex sequences with single character (e.g. 0x0a -> \n)
        int n = 0;
        while ((n = s.indexOf("0x", n)) != -1 && s.length() >= n + 4) {
            char ch;
            try {
                ch = (char) Integer.parseInt(s.substring(n + 2, n + 4), 16);
            } catch (NumberFormatException e) {
                n += 2;
                continue;
            }
            if (n + 4 < s.length()) {
                s = s.substring(0, n) + ch + s.substring(n + 4);
            } else {
                s = s.substring(0, n) + ch;
                break;
            }

            n++;
        }

        return s;
    }
}
