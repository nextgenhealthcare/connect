/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.IOUtils;

public class Base64Util {

    /**
     * Encodes binary data using the base64 algorithm and chunks the encoded output into 76
     * character blocks.
     * This method sets the initial output buffer to a value that is "guaranteed" to be slightly
     * larger than necessary.
     * Therefore the buffer should never need to be expanded, making the maximum memory requirements
     * much lower than
     * using Base64.encodeBase64Chunked.
     * 
     * @param bytes
     * @return
     * @throws IOException
     */
    public static byte[] encodeBase64(byte[] bytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        // Set the size of the buffer to minimize peak memory usage
        // A Base64 encoded message takes roughly 1.333 times the memory of the raw binary, so use 1.4 to be safe.
        ByteArrayOutputStream baos = new ByteArrayOutputStream((int) (bytes.length * 1.4));
        Base64OutputStream b64os = new Base64OutputStream(baos);

        // Perform the encoding
        IOUtils.copy(bais, b64os);

        // Free up any memory from the input
        b64os.close();

        return baos.toByteArray();
    }

    /**
     * Decodes base64 data.
     * This method sets the initial output buffer to a value that is "guaranteed" to be slightly
     * larger than necessary.
     * Therefore the buffer should never need to be expanded, making the maximum memory requirements
     * much lower than
     * using Base64.decodeBase64.
     * 
     * @param bytes
     * @return
     * @throws IOException
     */
    public static byte[] decodeBase64(byte[] bytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        // Set the size of the buffer to minimize peak memory usage
        // A binary message takes roughly .75 times the memory of the Base64 encoded message, so use 0.8 to be safe.
        ByteArrayOutputStream baos = new ByteArrayOutputStream((int) (bytes.length * 0.8));
        Base64OutputStream b64os = new Base64OutputStream(baos, false);

        // Perform the encoding
        IOUtils.copy(bais, b64os);

        // Free up any memory from the input
        b64os.close();

        return baos.toByteArray();
    }
}
