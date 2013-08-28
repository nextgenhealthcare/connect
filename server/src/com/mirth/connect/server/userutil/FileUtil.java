/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

/**
 * Provides file utility methods.
 * 
 * @see org.apache.commons.io.FileUtils
 */
public class FileUtil {
    private FileUtil() {}

    /**
     * Writes a string to a specified file, creating the file if it does not
     * exist.
     * 
     * @see org.apache.commons.io.FileUtils#writeStringToFile(File, String)
     * @param fileName
     *            - The pathname string of the file to write to.
     * @param append
     *            - If true, the data will be added to the end of the file
     *            rather than overwriting the file.
     * @param data
     *            - The content to write to the file.
     * @throws IOException
     */
    public static void write(String fileName, boolean append, String data) throws IOException {
        FileUtils.writeStringToFile(new File(fileName), data, append);
    }

    /**
     * Decodes a Base64 string into octets.
     * 
     * @param data
     *            - The Base64 string to decode.
     * @return - The decoded data, as a byte array.s
     */
    public static byte[] decode(String data) {
        return Base64.decodeBase64(data.getBytes());
    }

    /**
     * Encoded binary data into a Base64 string.
     * 
     * @param data
     *            - The binary data to encode (byte array).
     * @return - The encoded Base64 string.
     */
    public static String encode(byte[] data) {
        return new String(Base64.encodeBase64Chunked(data));
    }

    /**
     * Writes a byte array to a file, creating the file if it does not exist.
     * 
     * @see org.apache.commons.io.FileUtils#writeByteArrayToFile(File, byte[])
     * @param fileName
     *            - The pathname string of the file to write to.
     * @param append
     *            - If true, the data will be added to the end of the file
     *            rather than overwriting the file.
     * @param bytes
     *            - The binary content to write to the file.
     * @throws IOException
     */
    public static void write(String fileName, boolean append, byte[] bytes) throws IOException {
        FileUtils.writeByteArrayToFile(new File(fileName), bytes, append);
    }

    /**
     * Returns the contents of the file as a byte array.
     * 
     * @see org.apache.commons.io.FileUtils#readFileToByteArray(File)
     * @param fileName
     *            - The pathname string of the file to read from.
     * @return The byte array representation of the file.
     * @throws IOException
     */
    public static byte[] readBytes(String fileName) throws IOException {
        return FileUtils.readFileToByteArray(new File(fileName));
    }

    /**
     * Returns the contents of the file as a string, using the system default
     * charset encoding.
     * 
     * @see org.apache.commons.io.FileUtils#readFileToString(File)
     * @param fileName
     *            - The pathname string of the file to read from.
     * @return The string representation of the file.
     * @throws IOException
     */
    public static String read(String fileName) throws IOException {
        return FileUtils.readFileToString(new File(fileName));
    }

    /**
     * Deletes a specified File. In Rhino and E4X 'delete' is a keyword, so
     * File.delete() can't be called within Mirth directly.
     * 
     * @param file
     *            - The File to delete.
     * @return true if and only if the file or directory is successfully
     *         deleted; false otherwise
     * @throws SecurityException
     */
    public static boolean deleteFile(File file) throws SecurityException {
        return file.delete();
    }

    /**
     * Converts an RTF into plain text using the Swing RTFEditorKit.
     * 
     * @param message
     *            - The RTF message to convert.
     * @param replaceLinebreaksWith
     *            - If not null, any line breaks in the converted message will
     *            be replaced with this string.
     * @return - The converted plain text message.
     * @throws IOException
     * @throws BadLocationException
     */
    public static String rtfToPlainText(String message, String replaceLinebreaksWith) throws IOException, BadLocationException {

        String convertedPlainText;

        // Reading the RTF content string
        Reader in = new StringReader(message);

        // creating a default blank styled document
        DefaultStyledDocument styledDoc = new DefaultStyledDocument();

        // Creating a RTF Editor kit
        RTFEditorKit rtfKit = new RTFEditorKit();

        // Populating the contents in the blank styled document
        rtfKit.read(in, styledDoc, 0);

        // Getting the root document
        Document doc = styledDoc.getDefaultRootElement().getDocument();

        convertedPlainText = doc.getText(0, doc.getLength());
        if (replaceLinebreaksWith != null) {
            convertedPlainText = convertedPlainText.replaceAll("\\n", replaceLinebreaksWith);
        }

        return convertedPlainText;
    }
}