/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.server.util;

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

public class FileUtil {
	public static void write(String fileName, boolean append, String data) throws IOException {
		write(fileName, append, data.getBytes());
	}

	public static byte[] decode(String data) throws IOException {
		return new Base64().decode(data.getBytes());
	}

	public static String encode(byte[] data) throws IOException {
		return new String(new Base64().encode(data));
	}

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

	// Returns the contents of the file in a byte array.
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
