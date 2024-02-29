/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package net.lingala.zip4j.unzip;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.UnzipParameters;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.progress.ProgressMonitor;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mirth.connect.util.ZipTestUtils;

public class UnzipTest {

    @Before
    public void createTestFolder() {
        File installTempDir = new File("tests/zipextraction");
        if (!installTempDir.exists()) {
            installTempDir.mkdir();
        } else {
            cleanupTestFolder();
        }
    }

    @After
    public void cleanupTestFolder() {
        File tempDir = new File("tests/zipextraction");
        if (tempDir.exists()) {
            for (File file : tempDir.listFiles()) {
                file.delete();
            }
        }
    }

    @Test(expected = ZipException.class)
    public void testExtractMaliciousFile1() throws Exception {
        String maliciousFileName = "../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../tmp/evil.txt";
        Pair<Unzip, FileHeader> pair = createUnzipPair(maliciousFileName);
        Unzip unzip = pair.getLeft();
        FileHeader fileHeader = pair.getRight();

        UnzipParameters unzipParams = new UnzipParameters();
        String outPath = "tests/zipextraction";
        ProgressMonitor progressMonitor = new ProgressMonitor();
        String newFileName = null;

        unzip.extractFile(fileHeader, outPath, unzipParams, newFileName, progressMonitor, false);
    }

    /*
     * Unzip.initExtractFile tests the fileName from the fileHeader, regardless if you pass in a
     * newFileName, so this should throw an exception.
     */
    @Test(expected = ZipException.class)
    public void testExtractMaliciousFile2() throws Exception {
        String maliciousFileName = "../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../tmp/evil.txt";
        Pair<Unzip, FileHeader> pair = createUnzipPair(maliciousFileName);
        Unzip unzip = pair.getLeft();
        FileHeader fileHeader = pair.getRight();

        UnzipParameters unzipParams = new UnzipParameters();
        String outPath = "tests/zipextraction";
        ProgressMonitor progressMonitor = new ProgressMonitor();
        String newFileName = "evil.txt";

        unzip.extractFile(fileHeader, outPath, unzipParams, newFileName, progressMonitor, false);
    }

    @Test
    public void testExtractNormalFile1() throws Exception {
        String fileName = "good.txt";
        Pair<Unzip, FileHeader> pair = createUnzipPair(fileName);
        Unzip unzip = pair.getLeft();
        FileHeader fileHeader = pair.getRight();

        UnzipParameters unzipParams = new UnzipParameters();
        String outPath = "tests/zipextraction";
        ProgressMonitor progressMonitor = new ProgressMonitor();
        String newFileName = null;

        unzip.extractFile(fileHeader, outPath, unzipParams, newFileName, progressMonitor, false);
        File outputFile = new File(outPath + File.separator + fileName);
        assertTrue(outputFile.exists());
    }

    @Test
    public void testExtractNormalFile2() throws Exception {
        String fileName = "good.txt";
        Pair<Unzip, FileHeader> pair = createUnzipPair(fileName);
        Unzip unzip = pair.getLeft();
        FileHeader fileHeader = pair.getRight();

        UnzipParameters unzipParams = new UnzipParameters();
        String outPath = "tests/zipextraction";
        ProgressMonitor progressMonitor = new ProgressMonitor();
        String newFileName = "good2.txt";

        unzip.extractFile(fileHeader, outPath, unzipParams, newFileName, progressMonitor, false);
        File outputFile = new File(outPath + File.separator + newFileName);
        assertTrue(outputFile.exists());
    }

    @Test(expected = ZipException.class)
    public void testExtractNormalFileWithMaliciousNewFileName() throws Exception {
        String fileName = "good.txt";
        Pair<Unzip, FileHeader> pair = createUnzipPair(fileName);
        Unzip unzip = pair.getLeft();
        FileHeader fileHeader = pair.getRight();

        UnzipParameters unzipParams = new UnzipParameters();
        String outPath = "tests/zipextraction";
        ProgressMonitor progressMonitor = new ProgressMonitor();
        String newFileName = "../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../tmp/evil.txt";

        unzip.extractFile(fileHeader, outPath, unzipParams, newFileName, progressMonitor, false);
        File outputFile = new File(outPath + File.separator + newFileName);
        assertTrue(outputFile.exists());
    }

    @Test(expected = ZipException.class)
    public void testExtractAllFromMaliciousFile() throws Exception {
        String maliciousFileName = "../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../tmp/evil.txt";
        Pair<Unzip, FileHeader> pair = createUnzipPair(maliciousFileName);
        Unzip unzip = pair.getLeft();

        UnzipParameters unzipParams = new UnzipParameters();
        String outPath = "tests/zipextraction";
        ProgressMonitor progressMonitor = new ProgressMonitor();

        unzip.extractAll(unzipParams, outPath, progressMonitor, false);
    }

    @Test
    public void testExtractAllFromNormalFile() throws Exception {
        String fileName = "good.txt";
        Pair<Unzip, FileHeader> pair = createUnzipPair(fileName);
        Unzip unzip = pair.getLeft();

        UnzipParameters unzipParams = new UnzipParameters();
        String outPath = "tests/zipextraction";
        ProgressMonitor progressMonitor = new ProgressMonitor();

        unzip.extractAll(unzipParams, outPath, progressMonitor, false);
        File outputFile = new File(outPath + File.separator + fileName);
        assertTrue(outputFile.exists());
    }

    private Pair<Unzip, FileHeader> createUnzipPair(String fileName) throws Exception {
        File zipFile = ZipTestUtils.createTempZipFile(fileName);
        ZipModel zipModel = new ZipModel();
        zipModel.setZipFile(zipFile.getAbsolutePath());
        CentralDirectory centralDirectory = new CentralDirectory();
        ArrayList<FileHeader> fileHeaders = new ArrayList<>();
        FileHeader fileHeader = new FileHeader();
        fileHeader.setFileName(fileName);
        fileHeader.setCompressionMethod(8);
        fileHeaders.add(fileHeader);
        centralDirectory.setFileHeaders(fileHeaders);
        zipModel.setCentralDirectory(centralDirectory);

        Unzip unzip = new Unzip(zipModel);

        return Pair.of(unzip, fileHeader);
    }

}
