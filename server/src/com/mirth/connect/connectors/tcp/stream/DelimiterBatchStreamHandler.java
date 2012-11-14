package com.mirth.connect.connectors.tcp.stream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DelimiterBatchStreamHandler extends StreamHandler {

    private byte[] delimiterBytes;
    private boolean includeDelimiter;

    public DelimiterBatchStreamHandler(InputStream inputStream, byte[] delimiterBytes, boolean includeDelimiter) {
        this(inputStream, delimiterBytes, includeDelimiter, new byte[] {}, new byte[] {});
    }

    public DelimiterBatchStreamHandler(InputStream inputStream, byte[] delimiterBytes, boolean includeDelimiter, byte[] beginBytes, byte[] endBytes) {
        this(inputStream, delimiterBytes, includeDelimiter, beginBytes, endBytes, false);
    }

    public DelimiterBatchStreamHandler(InputStream inputStream, byte[] delimiterBytes, boolean includeDelimiter, byte[] beginBytes, byte[] endBytes, boolean returnDataOnException) {
        super(new BufferedInputStream(inputStream, delimiterBytes.length), null, beginBytes, endBytes, returnDataOnException);
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
    protected byte[] checkForIntermediateMessage() throws IOException {
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
