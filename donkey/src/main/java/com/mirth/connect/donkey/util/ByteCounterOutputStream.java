/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.util;

import java.io.OutputStream;

public class ByteCounterOutputStream extends OutputStream {
    private int count = 0;

    /**
     * Adds the number of bytes to be written to the counter.
     */
    @Override
    public void write(int b) {
        count++;
    }

    /**
     * Adds the number of bytes to be written to the counter.
     */
    @Override
    public void write(byte b[]) {
        count += b.length;
    }

    /**
     * Adds the number of bytes to be written to the counter.
     */
    @Override
    public void write(byte b[], int off, int len) {
        count += len;
    }

    /**
     * Gets the total byte count of all the data that would have been written to this output stream.
     */
    public int size() {
        return count;
    }
}
