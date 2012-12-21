package com.mirth.connect.connectors.tcp.stream;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

public class FrameStreamHandler extends StreamHandler {

    private Logger logger = Logger.getLogger(this.getClass());
    private byte[] startOfMessageBytes;
    private byte[] endOfMessageBytes;
    private boolean returnDataOnException;

    private ByteArrayOutputStream capturedBytes; // The bytes captured so far by the reader, not including any in the end bytes buffer.
    private List<Byte> endBytesBuffer; // An interim buffer of bytes used to capture the ending byte sequence.
    private byte lastByte; // The last byte returned from getNextByte.
    private boolean streamDone; // This is true if an EOF has been read in, or if the ending byte sequence has been detected.

    private boolean checkStartOfMessageBytes;
    private int currentByte;

    public FrameStreamHandler(InputStream inputStream, OutputStream outputStream) {
        this(inputStream, outputStream, new DefaultBatchStreamReader(inputStream));
    }

    public FrameStreamHandler(InputStream inputStream, OutputStream outputStream, BatchStreamReader batchStreamHandler) {
        this(inputStream, outputStream, batchStreamHandler, new byte[0], new byte[0]);
    }

    public FrameStreamHandler(InputStream inputStream, OutputStream outputStream, BatchStreamReader batchStreamHandler, byte[] startOfMessageBytes, byte[] endOfMessageBytes) {
        this(inputStream, outputStream, batchStreamHandler, startOfMessageBytes, endOfMessageBytes, false);
    }

    public FrameStreamHandler(InputStream inputStream, OutputStream outputStream, BatchStreamReader batchStreamHandler, byte[] startOfMessageBytes, byte[] endOfMessageBytes, boolean returnDataOnException) {
        super(inputStream, outputStream, batchStreamHandler);
        this.startOfMessageBytes = startOfMessageBytes;
        this.endOfMessageBytes = endOfMessageBytes;
        this.returnDataOnException = returnDataOnException;
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
    public final byte[] read() throws IOException {
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
                        // The input stream ended before the begin bytes were detected, so return null
                        streamDone = true;
                        if (firstBytes.size() > 0) {
                            throw new FrameStreamHandlerException(true, startOfMessageBytes, ArrayUtils.toPrimitive(firstBytes.toArray(new Byte[0])));
                        } else {
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
        } catch (Exception e) {
            if (checkStartOfMessageBytes && firstBytes.size() > 0) {
                // At least some bytes have been read, but the start of message bytes were not detected
                throw new FrameStreamHandlerException(true, startOfMessageBytes, ArrayUtils.toPrimitive(firstBytes.toArray(new Byte[0])), e);
            }
            if (capturedBytes.size() + endBytesBuffer.size() > 0 && endOfMessageBytes.length > 0) {
                // At least some bytes have been captured, but the end of message bytes were not detected
                throw new FrameStreamHandlerException(false, endOfMessageBytes, getLastBytes(), e);
            }

            if (!returnDataOnException) {
                if (e instanceof IOException) {
                    // If an IOException occurred and we're not allowing data to return, throw the exception
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

        // Return the captured bytes if there are any
        if (capturedBytes.size() > 0) {
            if (endOfMessageBytes.length > 0) {
                // The end of message bytes were not detected
                throw new FrameStreamHandlerException(false, endOfMessageBytes, getLastBytes());
            }
            return capturedBytes.toByteArray();
        } else {
            return null;
        }
    }

    @Override
    public final void write(byte[] data) throws IOException {
        writeFrame(data);
    }

    private final void writeFrame(byte[] data) throws IOException {
        write(startOfMessageBytes, data, endOfMessageBytes);
    }

    private void write(byte[]... dataArrays) throws IOException {
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
                lastBytes.add(0, capturedByteArray[i]);
            }
        }

        return ArrayUtils.toPrimitive(lastBytes.toArray(new Byte[0]));
    }
}
