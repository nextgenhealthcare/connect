/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Strings;

public class StringUtil {

    private static final Pattern pattern = Pattern.compile("xmlns:?[^=]*=[\\\"\\\"][^\\\"\\\"]*[\\\"\\\"]");

    public static String convertLineBreaks(String text, String replacement) {
        int start = 0;
        int end = -1;

        StringBuilder builder = new StringBuilder(text.length());

        for (int i = 0; i < text.length(); i++) {
            boolean append = false;

            /*
             * When encountering a CR or LF, append the segment since the last start index to the
             * buffer. Then append the replacement string.
             */
            if (text.charAt(i) == '\r') {
                end = i;
                append = true;

                /*
                 * After a CR, check if the next immediate character is an LF. If so, skip that
                 * character.
                 */
                if (i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    i++;
                }
            } else if (text.charAt(i) == '\n') {
                end = i;
                append = true;
            }

            if (append) {
                builder.append(text.substring(start, end));
                builder.append(replacement);

                start = i + 1;
            }
        }

        if (start == 0) {
            // If no replacements were made, just return the original text.
            return text;
        } else {
            // Append the remainder of the message to the buffer.
            builder.append(text.substring(start, text.length()));

            return builder.toString();
        }
    }

    public static String stripNamespaces(String string) {
        return pattern.matcher(string).replaceAll("");
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

    /**
     * A glorified version of String.valueOf that calls Arrays.toString for arrays (and arrays
     * within maps).
     */
    public static String valueOf(Object object) {
        if (object != null) {
            if (object instanceof Object[]) {
                // Convert using Arrays.toString so that it will show up as "[a,b,c]", instead of the object hash code.
                return Arrays.toString((Object[]) object);
            } else if (object instanceof Map) {
                // Build a custom string representation of the map that also converts arrays using Arrays.toString. 
                Map<?, ?> map = (Map<?, ?>) object;
                StringBuilder builder = new StringBuilder("{");

                for (Iterator<?> it = map.entrySet().iterator(); it.hasNext();) {
                    Entry<?, ?> entry = (Entry<?, ?>) it.next();
                    Object key = entry.getKey();
                    Object value = entry.getValue();

                    builder.append(key == map ? "(this Map)" : valueOf(key));
                    builder.append('=');
                    builder.append(value == map ? "(this Map)" : valueOf(value));

                    if (it.hasNext()) {
                        builder.append(", ");
                    }
                }

                builder.append('}');
                return builder.toString();
            }
        }

        return String.valueOf(object);
    }

    /**
     * A version of equals that treats null and "" as the same value
     * 
     * @param str1
     * @param str2
     * @return
     */
    public static boolean equalsIgnoreNull(String str1, String str2) {
        if (Strings.isNullOrEmpty(str1) && Strings.isNullOrEmpty(str2)) {
            return true;
        }
        return StringUtils.equals(str1, str2);
    }
}
