package com.mirth.connect.connectors.tcp.stream;

import java.io.IOException;
import java.io.InputStream;

import com.mirth.connect.connectors.tcp.EOFCheckInputStream;

public class ER7BatchStreamHandler extends StreamHandler {

    private int previousByte;

    public ER7BatchStreamHandler(InputStream inputStream) {
        this(inputStream, new byte[0], new byte[0]);
    }

    public ER7BatchStreamHandler(InputStream inputStream, byte[] beginBytes, byte[] endBytes) {
        this(inputStream, beginBytes, endBytes, false);
    }

    public ER7BatchStreamHandler(InputStream inputStream, byte[] beginBytes, byte[] endBytes, boolean returnDataOnException) {
        super(new EOFCheckInputStream(inputStream, 3), null, beginBytes, endBytes, returnDataOnException);
    }

    @Override
    public void setInputStream(InputStream inputStream) {
        super.setInputStream(new EOFCheckInputStream(inputStream, 3));
    }

    @Override
    protected void initialize() throws IOException {
        super.initialize();
        previousByte = -1;
    }

    @Override
    protected int getNextByte() throws IOException {
        // If we're at the beginning of the stream, or if a line break was last read, check to see if we need to skip anything
        if (previousByte == -1 || previousByte == '\r' || previousByte == '\n') {
            skipSegments();
        }
        // Get the next byte from the input stream
        return super.getNextByte();
    }

    @Override
    protected byte[] checkForIntermediateMessage() throws IOException {
        if (previousByte == -1 || lastByte == '\r' || lastByte == '\n') {
            skipSegments();

            // Check for MSH segments at the beginning of the message or following a line break
            inputStream.mark(3);
            try {
                if (inputStream.read() == 'M' && inputStream.read() == 'S' && inputStream.read() == 'H') {
                    // If there are any bytes in the buffer, flush them out
                    for (Byte bufferByte : endBytesBuffer) {
                        capturedBytes.write(bufferByte);
                    }
                    return capturedBytes.toByteArray();
                }
            } finally {
                // Reset the input stream position regardless of whether a new message was found
                inputStream.reset();
            }
        }

        previousByte = lastByte;
        return null;
    }

    private void skipSegments() throws IOException {
        // Check for empty lines
        inputStream.mark(1);
        int tempByte;
        if ((tempByte = inputStream.read()) == '\r' || tempByte == '\n') {
            // Check for an LF after a CR
            if (tempByte == '\r') {
                inputStream.mark(1);
                if (inputStream.read() != '\n') {
                    inputStream.reset();
                }
            }

            // Recursively check again
            skipSegments();
            return;
        } else {
            inputStream.reset();
        }

        // Check for header/trailer segments
        inputStream.mark(3);
        if (((tempByte = inputStream.read()) == 'F' || tempByte == 'B') && ((tempByte = inputStream.read()) == 'H' || tempByte == 'T') && inputStream.read() == 'S') {
            // Header or trailer segment detected; skip to the end of the line
            do {
                inputStream.mark(1);
            } while ((tempByte = inputStream.read()) != -1 && tempByte != '\r' && tempByte != '\n' && !(getEndOfMessageBytes().length > 0 && tempByte == getEndOfMessageBytes()[0]));

            // If the first ending byte was detected, reset the stream
            if (getEndOfMessageBytes().length > 0 && tempByte == getEndOfMessageBytes()[0]) {
                inputStream.reset();
            }

            // Check for an LF after a CR
            if (tempByte == '\r') {
                inputStream.mark(1);
                if (inputStream.read() != '\n') {
                    inputStream.reset();
                }
            }

            // Now that we've skipped the segment, recursively check again
            if (tempByte != -1) {
                skipSegments();
            } else {
                streamDone = true;
            }
        } else {
            inputStream.reset();
        }
    }
}
