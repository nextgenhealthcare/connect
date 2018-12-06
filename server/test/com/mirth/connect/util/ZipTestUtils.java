/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.google.common.base.Strings;

public class ZipTestUtils {

    public static File createTempZipFile(String fileName) throws Exception {
        File tempFile = File.createTempFile("temp_zip", ".zip"); //write to system defined temp
        FileOutputStream fos = new FileOutputStream(tempFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ZipOutputStream zos = new ZipOutputStream(bos);

        if (!Strings.isNullOrEmpty(fileName)) {
            try {
                ZipEntry entry = new ZipEntry(fileName);
                zos.putNextEntry(entry);
                zos.write("file contents".getBytes());
                zos.closeEntry();
            } finally {
                zos.close();
            }
        }

        return tempFile;
    }
}
