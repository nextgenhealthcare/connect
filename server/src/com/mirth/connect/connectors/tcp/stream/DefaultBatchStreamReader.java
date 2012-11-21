package com.mirth.connect.connectors.tcp.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class DefaultBatchStreamReader extends BatchStreamReader {
    
    public DefaultBatchStreamReader(InputStream inputStream) {
        super(inputStream);
    }

    public void initialize() throws IOException {}

    public int getNextByte() throws IOException {
        return inputStream.read();
    }

    public byte[] checkForIntermediateMessage(ByteArrayOutputStream capturedBytes, List<Byte> endBytesBuffer, int lastByte) throws IOException {
        return null;
    }
}
