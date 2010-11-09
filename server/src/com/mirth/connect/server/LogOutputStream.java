/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.mirth.connect.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

/**
 * Base class to connect a logging system to the output and/or error stream of
 * then external process. The implementation parses the incoming data to
 * construct a line and passes the complete line to an user-defined
 * implementation.
 */
public class LogOutputStream extends OutputStream {

    private Logger logger = Logger.getLogger(this.getClass());

    /** Initial buffer size. */
    private static final int INTIAL_SIZE = 132;

    /** Carriage return */
    private static final int CR = 0x0d;

    /** Linefeed */
    private static final int LF = 0x0a;

    /** the internal buffer */
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(INTIAL_SIZE);

    private boolean skip = false;

    /**
     * Write the data to the buffer and flush the buffer, if a line separator is
     * detected.
     * 
     * @param cc
     *            data to log (byte).
     * @see java.io.OutputStream#write(int)
     */
    public void write(final int cc) throws IOException {
        final byte c = (byte) cc;
        if ((c == '\n') || (c == '\r')) {
            if (!skip) {
                processBuffer();
            }
        } else {
            buffer.write(cc);
        }
        skip = (c == '\r');
    }

    /**
     * Flush this log stream.
     * 
     * @see java.io.OutputStream#flush()
     */
    public void flush() {
        if (buffer.size() > 0) {
            processBuffer();
        }
    }

    /**
     * Writes all remaining data from the buffer.
     * 
     * @see java.io.OutputStream#close()
     */
    public void close() throws IOException {
        if (buffer.size() > 0) {
            processBuffer();
        }
        super.close();
    }

    /**
     * Write a block of characters to the output stream
     * 
     * @param b
     *            the array containing the data
     * @param off
     *            the offset into the array where data starts
     * @param len
     *            the length of block
     * @throws java.io.IOException
     *             if the data cannot be written into the stream.
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    public void write(final byte[] b, final int off, final int len) throws IOException {
        // find the line breaks and pass other chars through in blocks
        int offset = off;
        int blockStartOffset = offset;
        int remaining = len;
        while (remaining > 0) {
            while (remaining > 0 && b[offset] != LF && b[offset] != CR) {
                offset++;
                remaining--;
            }
            // either end of buffer or a line separator char
            int blockLength = offset - blockStartOffset;
            if (blockLength > 0) {
                buffer.write(b, blockStartOffset, blockLength);
            }
            while (remaining > 0 && (b[offset] == LF || b[offset] == CR)) {
                write(b[offset]);
                offset++;
                remaining--;
            }
            blockStartOffset = offset;
        }
    }

    /**
     * Converts the buffer to a string and sends it to <code>processLine</code>.
     */
    protected void processBuffer() {
        processLine(buffer.toString());
        buffer.reset();
    }

    /**
     * Logs a line to the log system of the user.
     * 
     * @param line
     *            the line to log.
     */
    protected void processLine(final String line) {
        logger.error(line);
    }
}