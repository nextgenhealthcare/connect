package com.mirth.connect.donkey.util;

import org.apache.commons.codec.binary.StringUtils;

public class StringUtil {
    private static final int CHUNK_SIZE = 1000000;

    /**
     * A memory efficient method for getting bytes from a string. It first calculates the required
     * byte array size to avoid any buffers growing out of control.
     * The main purpose of the method is to alleviate the use case when charset = "UTF-8". For some
     * reason, using new String(string, "UTF-8") requires
     * memory many times the size of the string itself. There is a performance decrease over the
     * Java method of about 30-100%, so this method would ideally only
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
}
