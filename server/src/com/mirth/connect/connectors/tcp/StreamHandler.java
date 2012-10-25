/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.tcp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamHandler {
    private byte[] beginBytes;
    private byte[] endBytes;

    public byte[] getBeginBytes() {
        return beginBytes;
    }

    public void setBeginBytes(byte[] beginBytes) {
        this.beginBytes = beginBytes;
    }

    public byte[] getEndBytes() {
        return endBytes;
    }

    public void setEndBytes(byte[] endBytes) {
        this.endBytes = endBytes;
    }

    public byte[] read(InputStream inputStream) throws IOException {
        return read(inputStream, false);
    }

    public byte[] read(InputStream inputStream, boolean returnDataOnException) throws IOException {
        int inputByte;
        int i = 0;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try {
            // skip to the beginning of the message
            while (i < beginBytes.length) {
                inputByte = inputStream.read();

                if (inputByte != -1) {
                    if (inputByte == beginBytes[i]) {
                        i++;
                    } else {
                        i = 0;
                    }
                } else {
                    return bytes.toByteArray();
                }
            }

            if (endBytes.length > 0) {
                i = 0;

                while (i < endBytes.length) {
                    inputByte = inputStream.read();

                    if (inputByte == endBytes[i]) {
                        i++;
                        buffer.write(inputByte);
                    } else if (i > 0) {
                        buffer.write(inputByte);
                        bytes.write(buffer.toByteArray());
                        buffer.reset();
                        i = 0;
                    } else {
                        bytes.write(inputByte);
                    }
                }
            } else {
                do {
                    inputByte = inputStream.read();

                    if (inputByte != -1) {
                        bytes.write(inputByte);
                    }
                } while (inputByte != -1);
            }
        } catch (IOException e) {
            if (!returnDataOnException) {
                throw e;
            }
        }

        return bytes.toByteArray();
    }

    public void write(OutputStream outputStream, byte[] bytes) throws IOException {
        if (beginBytes.length > 0) {
            outputStream.write(beginBytes);
        }
        outputStream.write(bytes);
        if (endBytes.length > 0) {
            outputStream.write(endBytes);
        }
        outputStream.flush();
    }
}
