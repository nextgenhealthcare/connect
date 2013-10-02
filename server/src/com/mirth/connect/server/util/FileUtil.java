/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

import org.apache.commons.codec.binary.Base64;

/**
 * @deprecated
 * @see org.apache.commons.io.FileUtils
 */
public class FileUtil {
    
    /**
     * @deprecated
     * @see org.apache.commons.io.FileUtils#writeStringToFile(File, String)
     * @param fileName
     * @param append
     * @param data
     * @throws IOException
     */
	public static void write(String fileName, boolean append, String data) throws IOException {
		write(fileName, append, data.getBytes());
	}

	public static byte[] decode(String data) {
		return Base64.decodeBase64(data.getBytes());
	}

	public static String encode(byte[] data) {
		return new String(Base64.encodeBase64Chunked(data));
	}

	/**
	 * @deprecated
	 * @see org.apache.commons.io.FileUtils#writeByteArrayToFile(File, byte[])
	 * @param fileName
	 * @param append
	 * @param bytes
	 * @throws IOException
	 */
	public static void write(String fileName, boolean append, byte[] bytes) throws IOException {
		File file = new File(fileName);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file, append);
			fos.write(bytes);
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}

	/**
	 * Returns the contents of the file in a byte array.
	 * 
	 * @deprecated
	 * @see org.apache.commons.io.FileUtils#readFileToByteArray(File)
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static byte[] readBytes(String fileName) throws IOException {
		File file = new File(fileName);
		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			// File is too large
			throw new IOException("File too large " + file.getName());
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
		return bytes;
	}

	/**
	 * @deprecated
	 * @see org.apache.commons.io.FileUtils#readFileToString(File)
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static String read(String fileName) throws IOException {
		File file = new File(fileName);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		StringBuilder contents = new StringBuilder();
		String line = null;

		try {
			while ((line = reader.readLine()) != null) {
				contents.append(line + "\n");
			}
		} finally {
			reader.close();
		}

		return contents.toString();
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
