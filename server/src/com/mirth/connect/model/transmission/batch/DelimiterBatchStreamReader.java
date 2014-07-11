/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.transmission.batch;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.mirth.connect.donkey.server.message.batch.BatchStreamReader;

public class DelimiterBatchStreamReader extends BatchStreamReader {

    private byte[] delimiterBytes;
    private boolean includeDelimiter;

    public DelimiterBatchStreamReader(InputStream inputStream) {
        this(inputStream, new byte[0], false);
    }

    public DelimiterBatchStreamReader(InputStream inputStream, byte[] delimiterBytes, boolean includeDelimiter) {
        super(new BufferedInputStream(inputStream, delimiterBytes.length));
        this.delimiterBytes = delimiterBytes;
        this.includeDelimiter = includeDelimiter;
    }

    public byte[] getDelimiterBytes() {
        return delimiterBytes;
    }

    public void setDelimiterBytes(byte[] delimiterBytes) {
        this.delimiterBytes = delimiterBytes;
    }

    public boolean isIncludeDelimiter() {
        return includeDelimiter;
    }

    public void setIncludeDelimiter(boolean includeDelimiter) {
        this.includeDelimiter = includeDelimiter;
    }

    @Override
    public byte[] checkForIntermediateMessage(ByteArrayOutputStream capturedBytes, List<Byte> endBytesBuffer, int lastByte) throws IOException {
        inputStream.mark(delimiterBytes.length);

        boolean delimiterFound = true;
        for (byte delimiterByte : delimiterBytes) {
            if (inputStream.read() != delimiterByte) {
                delimiterFound = false;
                break;
            }
        }

        if (delimiterFound) {
            // If there are any bytes in the buffer, flush them out
            for (Byte bufferByte : endBytesBuffer) {
                capturedBytes.write(bufferByte);
            }

            // If we're including the delimiter, write it to the output stream
            if (includeDelimiter) {
                for (byte delimiterByte : delimiterBytes) {
                    capturedBytes.write(delimiterByte);
                }
            }

            return capturedBytes.toByteArray();
        } else {
            // No delimiter was found, so reset the input stream
            inputStream.reset();
        }

        return null;
    }
}
