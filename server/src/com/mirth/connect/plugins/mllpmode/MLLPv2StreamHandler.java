/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.mllpmode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.util.Arrays;

import com.mirth.connect.model.transmission.TransmissionModeProperties;
import com.mirth.connect.model.transmission.batch.BatchStreamReader;
import com.mirth.connect.model.transmission.framemode.FrameStreamHandler;
import com.mirth.connect.util.TcpUtil;

public class MLLPv2StreamHandler extends FrameStreamHandler {

    private Logger logger = Logger.getLogger(this.getClass());

    private byte[] ackBytes;
    private byte[] nackBytes;
    private int maxRetries;
    private boolean committed;

    public MLLPv2StreamHandler(InputStream inputStream, OutputStream outputStream, BatchStreamReader batchStreamReader, TransmissionModeProperties transmissionModeProperties) {
        super(inputStream, outputStream, batchStreamReader, transmissionModeProperties);
        MLLPModeProperties props = (MLLPModeProperties) transmissionModeProperties;
        ackBytes = TcpUtil.stringToByteArray(props.getAckBytes());
        nackBytes = TcpUtil.stringToByteArray(props.getNackBytes());
        maxRetries = NumberUtils.toInt(props.getMaxRetries());
        committed = false;
    }

    @Override
    public void commit(boolean success) throws IOException {
        // In case batching is occurring, we only want to send the acknowledgement back once
        if (!committed) {
            // Write either an ACK or NACK depending on whether the engine was able to commit the data to memory
            super.write(success ? ackBytes : nackBytes);
            committed = true;
        }
    }

    @Override
    public void write(byte[] data) throws IOException {
        boolean done = false;
        Exception firstCause = null;
        int retryCount = 0;

        while (!done) {
            // Write the data as per normal MLLPv1
            super.write(data);

            try {
                // Attempt to retrieve an acknowledgement
                byte[] response = super.read();
                // Reset the streamDone flag so the input stream can be read from again
                reset();

                if (response == null) {
                    /*
                     * Nothing was captured but an exception also was not thrown, so assume this is
                     * due to a previous read encountering an EOF and marking the stream as done.
                     * Reset the FrameStreamHandler's flags and try once more.
                     */
                    response = super.read();
                }

                if (Arrays.areEqual(response, ackBytes)) {
                    done = true;
                } else if (Arrays.areEqual(response, nackBytes)) {
                    throw new MLLPv2StreamHandlerException("Negative commit acknowledgement received.");
                } else {
                    throw new MLLPv2StreamHandlerException("Invalid acknowledgement block received.");
                }
            } catch (IOException e) {
                if (firstCause == null) {
                    firstCause = e;
                }

                if (maxRetries > 0 && retryCount++ == maxRetries) {
                    throw new MLLPv2StreamHandlerException("Maximum retry count reached. First cause: " + firstCause.getMessage(), firstCause);
                }
            }
        }
    }
}
