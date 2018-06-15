package com.mirth.connect.server.controllers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.junit.Test;

public class DefaultExtensionControllerTest {

    @Test(expected = ZipException.class)
    public void testExtractZipEntryZipSlipWithRelativePath() throws Exception {
        DefaultExtensionController extensionController = new DefaultExtensionController();
        
        File installTempDir = new File("tests/zipextraction");
        ZipEntry entry = new ZipEntry("../ZipSlip.txt");
        ZipFile zipFile = createTempZipFile("ZipSlip.txt");
        
        extensionController.extractZipEntry(entry, installTempDir, zipFile);
    }
    
    @Test
    public void testExtractZipEntryValidPath() throws Exception {
        File installTempDir = new File("tests/zipextraction/extractionTemp");
        File file2 = new File(installTempDir, "good.txt");
        if (file2.exists()) {
            file2.delete();
        }
        if (installTempDir.exists()) {
            installTempDir.delete();
        }
        installTempDir.mkdir();
        
        DefaultExtensionController extensionController = new DefaultExtensionController();
        
        ZipEntry entry = new ZipEntry("good.txt");
        ZipFile zipFile = createTempZipFile("good.txt");
        extensionController.extractZipEntry(entry, installTempDir, zipFile);
    }
    
    private ZipFile createTempZipFile(String fileName) throws Exception {
        File tempFile = File.createTempFile("temp_zip", ".zip"); //write to system defined temp
        FileOutputStream fos = new FileOutputStream(tempFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ZipOutputStream zos = new ZipOutputStream(bos);

        try {
            ZipEntry entry = new ZipEntry(fileName);
            zos.putNextEntry(entry);
            zos.write("file contents".getBytes());
            zos.closeEntry();
        }
        finally {
            zos.close();
        }

        return new ZipFile(tempFile);
    }
}