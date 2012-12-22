/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.tcp.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class StreamHandler {
    
    protected InputStream inputStream;
    protected OutputStream outputStream;
    protected BatchStreamReader batchStreamReader;
    
    public StreamHandler() {
        this(null, null);
    }
    
    public StreamHandler(InputStream inputStream, OutputStream outputStream) {
        this(inputStream, outputStream, new DefaultBatchStreamReader(inputStream));
    }
    
    public StreamHandler(InputStream inputStream, OutputStream outputStream, BatchStreamReader batchStreamReader) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.batchStreamReader = batchStreamReader;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public BatchStreamReader getBatchStreamReader() {
        return batchStreamReader;
    }

    public void setBatchStreamReader(BatchStreamReader batchStreamReader) {
        this.batchStreamReader = batchStreamReader;
    }

    public abstract byte[] read() throws IOException;
    
    public abstract void write(byte[] data) throws IOException;
}
