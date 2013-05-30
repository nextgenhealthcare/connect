/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

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
 * @see org.apache.commons.io.FileUtils
 */
public class FileUtil {
    
    /**
     * @see org.apache.commons.io.FileUtils#writeStringToFile(File, String)
     * @param fileName
     * @param append
     * @param data
     * @throws IOException
     */
	public static void write(String fileName, boolean append, String data) throws IOException {
	    FileUtils.writeStringToFile(new File(fileName), data, append);
	}

	public static byte[] decode(String data) {
		return Base64.decodeBase64(data.getBytes());
	}

	public static String encode(byte[] data) {
		return new String(Base64.encodeBase64Chunked(data));
	}

	/**
	 * @see org.apache.commons.io.FileUtils#writeByteArrayToFile(File, byte[])
	 * @param fileName
	 * @param append
	 * @param bytes
	 * @throws IOException
	 */
	public static void write(String fileName, boolean append, byte[] bytes) throws IOException {
	    FileUtils.writeByteArrayToFile(new File(fileName), bytes, append);
	}

	/**
	 * Returns the contents of the file in a byte array.
	 * 
	 * @see org.apache.commons.io.FileUtils#readFileToByteArray(File)
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static byte[] readBytes(String fileName) throws IOException {
	    return FileUtils.readFileToByteArray(new File(fileName));
	}

	/**
	 * @see org.apache.commons.io.FileUtils#readFileToString(File)
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static String read(String fileName) throws IOException {
	    return FileUtils.readFileToString(new File(fileName));
	}

    /**
     * deletes a specified File.  'delete' is a keyword in Rhino and E4X, thus can't call File.delete() method within Mirth directly.
     * @param file
     * @return
     * @throws SecurityException
     */
    public static boolean deleteFile(File file) throws SecurityException {
        return file.delete();
	}

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