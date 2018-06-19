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
            }
            finally {
                zos.close();
            }
        }

        return tempFile;
    }
}
