/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mirth.connect.util.ZipTestUtils;

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
        File installTempDir = new File("tests/zipextraction/");

        DefaultExtensionController extensionController = new DefaultExtensionController();

        ZipEntry entry = new ZipEntry("good.txt");
        ZipFile zipFile = createTempZipFile("good.txt");
        extensionController.extractZipEntry(entry, installTempDir, zipFile);

        File extractedFile = new File("tests/zipextraction/", "good.txt");
        assertTrue(extractedFile.exists());
    }

    @Before
    public void createTestFolder() {
        File installTempDir = new File("tests/zipextraction/");
        if (!installTempDir.exists()) {
            installTempDir.mkdir();
        } else {
            cleanupTestFolder();
        }
    }

    @After
    public void cleanupTestFolder() {
        File tempDir = new File("tests/zipextraction/");
        if (tempDir.exists()) {
            for (File file : tempDir.listFiles()) {
                file.delete();
            }
        }
    }

    private ZipFile createTempZipFile(String fileName) throws Exception {
        return new ZipFile(ZipTestUtils.createTempZipFile(fileName));
    }
}