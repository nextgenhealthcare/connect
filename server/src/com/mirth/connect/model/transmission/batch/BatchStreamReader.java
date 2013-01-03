/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.transmission.batch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public abstract class BatchStreamReader {

    protected InputStream inputStream;

    public BatchStreamReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * This allows subclasses of StreamHandler to initialize anything it needs
     * to (e.g. mark the input stream, skip over data at the beginning, etc.).
     * This runs every time read() is called, and after the beginning byte
     * sequence check has occurred. Since the same StreamHandler object may be
     * used multiple times to return several messages in a potential batch, this
     * allows the subclass to reset anything it needs to before reading actually
     * begins.
     * 
     * @throws IOException
     */
    public void initialize() throws IOException {}

    /**
     * Overriding this method allows subclasses of StreamHandler to preprocess
     * the bytes returned from the underlying input stream. This can be used to
     * skip over data (e.g. HL7 batch segments, empty lines) that isn't deemed
     * relevant.
     * 
     * @return The next byte to be handled by the reader (or -1 in the case of
     *         an EOF).
     * @throws IOException
     */
    public int getNextByte() throws IOException {
        return inputStream.read();
    }

    /**
     * Overriding this method allows subclasses of StreamHandler to check the
     * bytes that have been read in so far, and decide whether all or part of
     * them constitutes an intermediate message as part of a batch. The bytes
     * returned may or may not be identical to the bytes thus far read in (for
     * example an intermediate message determined by a delimiter may not include
     * the delimiter itself).
     * 
     * The subclass has access to capturedBytes and endBytesBuffer, which
     * together compose all bytes so far returned from getNextByte. It also has
     * access to lastByte, which is the last byte returned from getNextByte
     * (this doesn't include EOFs or repeat bytes caused by cycling back through
     * endBytesBuffer).
     * 
     * @return A byte array representing the next intermediate message in the
     *         stream, or null if no intermediate message is yet detected.
     * @throws IOException
     */
    public byte[] checkForIntermediateMessage(ByteArrayOutputStream capturedBytes, List<Byte> endBytesBuffer, int lastByte) throws IOException {
        return null;
    }
}
