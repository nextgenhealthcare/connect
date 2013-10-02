/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.transmission.batch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class DefaultBatchStreamReader extends BatchStreamReader {

    public DefaultBatchStreamReader(InputStream inputStream) {
        super(inputStream);
    }

    public void initialize() throws IOException {}

    public int getNextByte() throws IOException {
        return inputStream.read();
    }

    public byte[] checkForIntermediateMessage(ByteArrayOutputStream capturedBytes, List<Byte> endBytesBuffer, int lastByte) throws IOException {
        return null;
    }
}
