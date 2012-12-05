package com.mirth.connect.server.util;

import java.io.IOException;
import java.io.OutputStream;

public class ByteCounterOutputStream extends OutputStream {
    private int count = 0;
    
    /**
     * Adds the number of bytes to be written to the counter.
     */
    @Override
    public void write(int b) throws IOException {
        count++;
    }
    
    /**
     * Adds the number of bytes to be written to the counter.
     */
    @Override
    public void write(byte b[]) throws IOException {
        count += b.length;
    }

    /**
     * Adds the number of bytes to be written to the counter.
     */
    @Override
    public void write(byte b[], int off, int len) throws IOException {
        count += len;
    }

    /**
     * Gets the total byte count of all the data that would have been written to this output stream.
     */
    public int size() {
        return count;
    }
}
