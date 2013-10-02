/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.transmission;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.mirth.connect.model.transmission.batch.BatchStreamReader;

public abstract class StreamHandler {

    protected InputStream inputStream;
    protected OutputStream outputStream;
    protected BatchStreamReader batchStreamReader;
    protected TransmissionModeProperties transmissionModeProperties;

    public StreamHandler(InputStream inputStream, OutputStream outputStream, BatchStreamReader batchStreamReader, TransmissionModeProperties transmissionModeProperties) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.batchStreamReader = batchStreamReader;
        this.transmissionModeProperties = transmissionModeProperties;
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

    public TransmissionModeProperties getTransmissionModeProperties() {
        return transmissionModeProperties;
    }

    public void setTransmissionModeProperties(TransmissionModeProperties transmissionModeProperties) {
        this.transmissionModeProperties = transmissionModeProperties;
    }

    /**
     * Returns the next message from the underlying input stream, or null if all
     * messages have been read already.
     * 
     * @throws IOException
     */
    public abstract byte[] read() throws IOException;

    /**
     * Notifies the handler whether or not the engine was able to commit the
     * bytes of the previous read to memory.
     * 
     * @param success
     *            - Determines whether the commit was successful or not.
     * @throws IOException
     */
    public void commit(boolean success) throws IOException {}

    /**
     * Writes the data to the underlying output stream.
     * 
     * @throws IOException
     */
    public abstract void write(byte[] data) throws IOException;
}
