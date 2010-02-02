package com.webreach.mirth.client.ui.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Base64;

public class FileUtil {

    public static final String CHARSET = "UTF-8";

    public static void write(File file, String data, boolean append) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file, append), CHARSET);

        try {
            writer.write(data);
            writer.flush();
        } finally {
            writer.close();
        }
    }

    public static String read(File file) throws IOException {
        FileInputStream stream = new FileInputStream(file);
        try {
            // This is the quickest way to read a file
            FileChannel fileChannel = stream.getChannel();
            MappedByteBuffer byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
            // Decode the bytes to a string using the given charset
            return Charset.forName(CHARSET).decode(byteBuffer).toString();
        } finally {
            stream.close();
        }
    }

    public static String readBinaryBase64(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();

        return new String(new Base64().encode(bytes));
    }
}
