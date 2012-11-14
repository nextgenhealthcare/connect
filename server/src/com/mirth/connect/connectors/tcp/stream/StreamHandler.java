package com.mirth.connect.connectors.tcp.stream;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class StreamHandler {

    private Logger logger = Logger.getLogger(this.getClass());
    private byte[] startOfMessageBytes;
    private byte[] endOfMessageBytes;
    private boolean returnDataOnException;

    protected InputStream inputStream;
    protected OutputStream outputStream;

    protected ByteArrayOutputStream capturedBytes; // The bytes captured so far by the reader, not including any in the end bytes buffer.
    protected List<Byte> endBytesBuffer; // An interim buffer of bytes used to capture the ending byte sequence.
    protected byte lastByte; // The last byte returned from getNextByte.
    protected boolean streamDone; // This is true if an EOF has been read in, or if the ending byte sequence has been detected.

    private boolean checkStartOfMessageBytes;
    private int currentByte;

    public StreamHandler(InputStream inputStream) {
        this(inputStream, null);
    }

    public StreamHandler(InputStream inputStream, OutputStream outputStream) {
        this(inputStream, outputStream, new byte[0], new byte[0]);
    }

    public StreamHandler(InputStream inputStream, OutputStream outputStream, byte[] startOfMessageBytes, byte[] endOfMessageBytes) {
        this(inputStream, outputStream, startOfMessageBytes, endOfMessageBytes, false);
    }

    public StreamHandler(InputStream inputStream, OutputStream outputStream, byte[] startOfMessageBytes, byte[] endOfMessageBytes, boolean returnDataOnException) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
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

    /**
     * This allows subclasses of StreamHandler to initialize anything it needs
     * to (e.g. mark the input stream, skip over data at the beginning, etc.).
     * This runs every time getNextMessage is called, and after the beginning
     * byte sequence check has occurred. Since the same StreamHandler object may
     * be used multiple times to return several messages in a potential batch,
     * this allows the subclass to reset anything it needs to before reading
     * actually begins.
     * 
     * @throws IOException
     */
    protected void initialize() throws IOException {}

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
    protected int getNextByte() throws IOException {
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
    protected byte[] checkForIntermediateMessage() throws IOException {
        return null;
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
    public final byte[] getNextMessage() throws IOException {
        if (streamDone || inputStream == null) {
            return null;
        }

        capturedBytes = new ByteArrayOutputStream();
        // A List is used here to allow the buffer to simulate a "shifting window" of potential ending bytes.
        endBytesBuffer = new ArrayList<Byte>();

        try {
            // Skip to the beginning of the message
            if (checkStartOfMessageBytes) {
                int i = 0;

                while (i < startOfMessageBytes.length) {
                    currentByte = inputStream.read();

                    if (currentByte != -1) {
                        if (currentByte == startOfMessageBytes[i]) {
                            i++;
                        } else {
                            i = 0;
                        }
                    } else {
                        // The input stream ended before the begin bytes were detected, so return null
                        streamDone = true;
                        return null;
                    }
                }

                // Begin bytes were found
                checkStartOfMessageBytes = false;
            }

            // Allow the handler to initialize anything it needs to (e.g. mark the input stream)
            initialize();

            // Iterate while there are still bytes to read, or if we're checking for end bytes and its buffer is not empty
            while ((currentByte = getNextByte()) != -1 || (endOfMessageBytes.length > 0 && !endBytesBuffer.isEmpty())) {
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
                    byte[] returnBytes = checkForIntermediateMessage();
                    if (returnBytes != null) {
                        return returnBytes;
                    }
                }
            }
        } catch (Throwable e) {
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

        return capturedBytes.size() > 0 ? capturedBytes.toByteArray() : null;
    }

    public final void offerResponse(byte[] data) throws IOException {
        writeFrame(data);
    }

    public final void writeFrame(byte[] data) throws IOException {
        write(new byte[][] { startOfMessageBytes, data, endOfMessageBytes });
    }

    protected final void write(byte[][] dataArrays) throws IOException {
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
}
