/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.util;

import org.apache.commons.codec.binary.StringUtils;

public class StringUtil {
    private static final int CHUNK_SIZE = 1000000;

    /**
     * A memory efficient method for getting bytes from a string. It first calculates the required
     * byte array size to avoid any buffers growing out of control. The main purpose of the method
     * is to alleviate the use case when charset = "UTF-8". For some reason, using new
     * String(string, "UTF-8") requires memory many times the size of the string itself. There is a
     * performance decrease over the Java method of about 30-100%, so this method would ideally only
     * be used when memory is an issue.
     */
    public static byte[] getBytesUncheckedChunked(String string, String charset) {
        int offset = 0;
        int length = string.length();

        // Calculate the size of the string after byte encoding
        ByteCounterOutputStream outputStream = new ByteCounterOutputStream();
        while ((length - offset) > 0) {
            int segmentSize = Math.min(CHUNK_SIZE, length - offset);
            outputStream.write(StringUtils.getBytesUnchecked(string.substring(offset, offset + segmentSize), charset));
            offset += segmentSize;
        }

        // Create a byte array the size of the exact size required
        byte[] data = new byte[outputStream.size()];
        int position = 0;
        offset = 0;

        // Perform the conversion again and write the data to the byte array
        while ((length - offset) > 0) {
            int segmentSize = Math.min(CHUNK_SIZE, length - offset);
            byte[] segment = StringUtils.getBytesUnchecked(string.substring(offset, offset + segmentSize), charset);
            System.arraycopy(segment, 0, data, position, segment.length);
            offset += segmentSize;
            position += segment.length;
        }

        return data;
    }

    /**
     * Searches for a substring between a specific start/end position, without taking a substring
     * and creating a new String.
     */
    public static int indexOf(String string, String search, int startPos, int endPos) {
        for (int i = startPos; i < endPos && i + search.length() <= string.length(); i++) {
            boolean found = true;
            for (int j = 0; j < search.length(); j++) {
                if (string.charAt(i + j) != search.charAt(j)) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return i;
            }
        }
        return -1;
    }
}
