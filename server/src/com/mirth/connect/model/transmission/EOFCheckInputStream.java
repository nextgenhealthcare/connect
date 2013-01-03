/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.transmission;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An EOFCheckInputStream adds functionality to another InputStream that detects
 * when a -1 is first read from the underlying stream, and doesn't allow any
 * subsequent reads after that (all calls to read() will result in -1). This is
 * useful for sockets that are persisted across multiple reads because it
 * prevents an unnecessary SocketTimeoutException from occurring. To continue
 * reading from a socket using this stream, you can either call reset() to a
 * previously marked position, or you can wrap the socket stream in a new
 * instance of EOFCheckInputStream.
 */
public class EOFCheckInputStream extends BufferedInputStream {

    private int eofPos = -1;

    public EOFCheckInputStream(InputStream in) {
        super(in);
    }

    public EOFCheckInputStream(InputStream in, int size) {
        super(in, size);
    }

    @Override
    public int read() throws IOException {
        // If an EOF has been detected and the current position is at the EOF position, then return -1
        if (eofPos == -1 || pos < eofPos) {
            int i = super.read();
            if (i == -1) {
                // Mark the EOF position
                eofPos = pos;
            }
            return i;
        } else {
            return -1;
        }
    }
}
