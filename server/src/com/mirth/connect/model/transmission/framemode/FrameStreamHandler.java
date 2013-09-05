/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.transmission.framemode;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.model.transmission.StreamHandler;
import com.mirth.connect.model.transmission.TransmissionModeProperties;
import com.mirth.connect.model.transmission.batch.BatchStreamReader;
import com.mirth.connect.util.TcpUtil;

public class FrameStreamHandler extends StreamHandler {

    private Logger logger = Logger.getLogger(this.getClass());

    protected byte[] startOfMessageBytes;
    protected byte[] endOfMessageBytes;
    protected boolean returnDataOnException; // Determines whether data should be returned if an exception occurs.

    private ByteArrayOutputStream capturedBytes; // The bytes captured so far by the reader, not including any in the end bytes buffer.
    private List<Byte> endBytesBuffer; // An interim buffer of bytes used to capture the ending byte sequence.
    private byte lastByte; // The last byte returned from getNextByte.
    private boolean streamDone; // This is true if an EOF has been read in, or if the ending byte sequence has been detected.

    private boolean checkStartOfMessageBytes;
    private int currentByte;

    public FrameStreamHandler(InputStream inputStream, OutputStream outputStream, BatchStreamReader batchStreamReader, TransmissionModeProperties transmissionModeProperties) {
        super(inputStream, outputStream, batchStreamReader, transmissionModeProperties);
        FrameModeProperties frameModeProperties = (FrameModeProperties) transmissionModeProperties;
        this.startOfMessageBytes = TcpUtil.stringToByteArray(frameModeProperties.getStartOfMessageBytes());
        this.endOfMessageBytes = TcpUtil.stringToByteArray(frameModeProperties.getEndOfMessageBytes());
        // Only return data on exceptions if there are no end bytes defined
        this.returnDataOnException = endOfMessageBytes.length == 0;
        this.checkStartOfMessageBytes = true;
        this.streamDone = false;
    }

    public byte[] getStartOfMessageBytes() {
        return startOfMessageBytes;
    }

    public void setStartOfMessageBytes(byte[] startOfMessageBytes) {
        this.startOfMessageBytes = startOfMessageBytes;
    }

    public byte[] getEndOfMessageBytes() {
        return endOfMessageBytes;
    }

    public void setEndOfMessageBytes(byte[] endOfMessageBytes) {
        this.endOfMessageBytes = endOfMessageBytes;
    }

    public boolean isReturnDataOnException() {
        return returnDataOnException;
    }

    public void setReturnDataOnException(boolean returnDataOnException) {
        this.returnDataOnException = returnDataOnException;
    }

    @Override
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
        batchStreamReader.setInputStream(inputStream);
    }

    public void reset() {
        checkStartOfMessageBytes = true;
        streamDone = false;
    }

    /**
     * Returns the next message from the stream (could be the entire stream
     * contents or part of a batch).
     * 
     * @return A byte array representing the next whole message in the stream
     *         (could be the entire stream contents or part of a batch), or null
     *         if the stream is done. If an IOException is caught while reading
     *         (e.g. a socket timeout) and returnDataOnException is true, then
     *         all bytes accumulated up to that point are returned.
     * @throws IOException
     *             If an IOException is caught while reading (e.g. a socket
     *             timeout) and returnDataOnException is false.
     */
    @Override
    public byte[] read() throws IOException {
        if (streamDone || inputStream == null) {
            return null;
        }

        capturedBytes = new ByteArrayOutputStream();
        List<Byte> firstBytes = new ArrayList<Byte>();
        // A List is used here to allow the buffer to simulate a "shifting window" of potential bytes.
        endBytesBuffer = new ArrayList<Byte>();

        try {
            // Skip to the beginning of the message
            if (checkStartOfMessageBytes) {
                int i = 0;

                while (i < startOfMessageBytes.length) {
                    currentByte = inputStream.read();
                    logger.trace("Checking for start of message bytes, currentByte: " + currentByte);

                    if (currentByte != -1) {
                        if (firstBytes.size() < startOfMessageBytes.length) {
                            firstBytes.add((byte) currentByte);
                        }

                        if (currentByte == (int) (startOfMessageBytes[i] & 0xFF)) {
                            i++;
                        } else {
                            i = 0;
                        }
                    } else {
                        streamDone = true;
                        if (firstBytes.size() > 0) {
                            throw new FrameStreamHandlerException(true, startOfMessageBytes, ArrayUtils.toPrimitive(firstBytes.toArray(new Byte[0])));
                        } else {
                            // The input stream ended before the begin bytes were detected, so return null
                            return null;
                        }
                    }
                }

                // Begin bytes were found
                checkStartOfMessageBytes = false;
            }

            // Allow the handler to initialize anything it needs to (e.g. mark the input stream)
            batchStreamReader.initialize();

            // Iterate while there are still bytes to read, or if we're checking for end bytes and its buffer is not empty
            while ((currentByte = batchStreamReader.getNextByte()) != -1 || (endOfMessageBytes.length > 0 && !endBytesBuffer.isEmpty())) {
                // If the input stream is done, get the byte from the buffer instead
                if (currentByte == -1) {
                    currentByte = endBytesBuffer.remove(0);
                    streamDone = true;
                } else {
                    lastByte = (byte) currentByte;
                }

                // Check to see if an end frame has been received
                if (endOfMessageBytes.length > 0 && !streamDone) {
                    if (endBytesBuffer.size() == endOfMessageBytes.length) {
                        // Shift the buffer window over one, popping the first element and writing it to the output stream
                        capturedBytes.write(endBytesBuffer.remove(0));
                    }

                    // Add the byte to the buffer
                    endBytesBuffer.add((byte) currentByte);

                    // Check to see if the current buffer window equals the ending byte sequence
                    boolean endBytesFound = true;
                    for (int i = 0; i <= endBytesBuffer.size() - 1; i++) {
                        if (endBytesBuffer.get(i) != endOfMessageBytes[i]) {
                            endBytesFound = false;
                            break;
                        }
                    }

                    if (endBytesFound) {
                        // Ending bytes sequence has been detected
                        streamDone = true;
                        return capturedBytes.toByteArray();
                    }
                } else {
                    // Add the byte to the main output stream
                    capturedBytes.write(currentByte);
                }

                if (!streamDone) {
                    // Allow subclass to check the current byte stream and return immediately
                    byte[] returnBytes = batchStreamReader.checkForIntermediateMessage(capturedBytes, endBytesBuffer, lastByte);
                    if (returnBytes != null) {
                        return returnBytes;
                    }
                }
            }
        } catch (Throwable e) {
            if (!returnDataOnException) {
                if (e instanceof IOException) {
                    // If an IOException occurred and we're not allowing data to return, throw the exception

                    if (checkStartOfMessageBytes && firstBytes.size() > 0) {
                        // At least some bytes have been read, but the start of message bytes were not detected
                        throw new FrameStreamHandlerException(true, startOfMessageBytes, ArrayUtils.toPrimitive(firstBytes.toArray(new Byte[0])), e);
                    }
                    if (capturedBytes.size() + endBytesBuffer.size() > 0 && endOfMessageBytes.length > 0) {
                        // At least some bytes have been captured, but the end of message bytes were not detected
                        throw new FrameStreamHandlerException(false, endOfMessageBytes, getLastBytes(), e);
                    }
                    throw (IOException) e;
                } else {
                    // If any other Throwable was caught, return null to indicate that we're done
                    return null;
                }
            }
        }

        // Flush the buffer to the main output stream
        for (Byte bufByte : endBytesBuffer) {
            capturedBytes.write(bufByte);
        }

        return capturedBytes.size() > 0 ? capturedBytes.toByteArray() : null;
    }

    @Override
    public void write(byte[] data) throws IOException {
        writeFrame(data);
    }

    protected void writeFrame(byte[] data) throws IOException {
        write(startOfMessageBytes, data, endOfMessageBytes);
    }

    protected void write(byte[]... dataArrays) throws IOException {
        if (dataArrays == null || outputStream == null) {
            return;
        }

        DataOutputStream dos = new DataOutputStream(outputStream);

        for (byte[] data : dataArrays) {
            if (data != null) {
                for (byte b : data) {
                    dos.writeByte(b);
                }
            }
        }

        try {
            dos.flush();
        } catch (SocketException e) {
            logger.debug("Socket closed while trying to flush.");
        }
    }

    private byte[] getLastBytes() {
        List<Byte> lastBytes = new ArrayList<Byte>();

        if (endBytesBuffer != null) {
            lastBytes.addAll(endBytesBuffer);
        }

        if (capturedBytes != null) {
            byte[] capturedByteArray = capturedBytes.toByteArray();
            for (int i = capturedByteArray.length - 1; i >= 0 && lastBytes.size() < endOfMessageBytes.length; i--) {
                lastBytes.add(capturedByteArray[i]);
            }
        }

        return ArrayUtils.toPrimitive(lastBytes.toArray(new Byte[0]));
    }
}
