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

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.UnzipParameters;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.progress.ProgressMonitor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mirth.connect.util.ZipTestUtils;

public class UnzipEngineTest {

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
    public void testUnzipWithMaliciousFile1() throws Exception {
        String maliciousFileName = "../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../tmp/evil.txt";
        File maliciousZipFile = ZipTestUtils.createTempZipFile(maliciousFileName);

        ZipModel zipModel = new ZipModel();
        zipModel.setZipFile(maliciousZipFile.getAbsolutePath());
        FileHeader fileHeader = new FileHeader();
        fileHeader.setFileName(maliciousFileName);
        fileHeader.setCompressionMethod(8);
        UnzipEngine engine = new UnzipEngine(zipModel, fileHeader);

        ProgressMonitor progressMonitor = new ProgressMonitor();
        String outPath = "tests/zipextraction";
        String newFileName = null;
        UnzipParameters unzipParams = new UnzipParameters();

        engine.unzipFile(progressMonitor, outPath, newFileName, unzipParams);
    }

    /*
     * This case should not throw a ZipSlip related exception because we are passing in a
     * newFileName which will be used for the destination file
     */
    @Test
    public void testUnzipWithMaliciousFileButValidNewFileName() throws Exception {
        String maliciousFileName = "../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../tmp/evil.txt";
        File maliciousZipFile = ZipTestUtils.createTempZipFile(maliciousFileName);

        ZipModel zipModel = new ZipModel();
        zipModel.setZipFile(maliciousZipFile.getAbsolutePath());
        FileHeader fileHeader = new FileHeader();
        fileHeader.setFileName(maliciousFileName);
        fileHeader.setCompressionMethod(8);
        UnzipEngine engine = new UnzipEngine(zipModel, fileHeader);

        ProgressMonitor progressMonitor = new ProgressMonitor();
        String outPath = "tests/zipextraction";
        String newFileName = "evil.txt";
        UnzipParameters unzipParams = new UnzipParameters();

        engine.unzipFile(progressMonitor, outPath, newFileName, unzipParams);

        File outputFile = new File(outPath + File.separator + newFileName);
        assertTrue(outputFile.exists());
    }

    @Test(expected = ZipException.class)
    public void testUnzipWithMaliciousFileAndMaliciousNewFileName() throws Exception {
        String maliciousFileName = "../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../tmp/evil.txt";
        File maliciousZipFile = ZipTestUtils.createTempZipFile(maliciousFileName);

        ZipModel zipModel = new ZipModel();
        zipModel.setZipFile(maliciousZipFile.getAbsolutePath());
        FileHeader fileHeader = new FileHeader();
        fileHeader.setFileName(maliciousFileName);
        fileHeader.setCompressionMethod(8);
        UnzipEngine engine = new UnzipEngine(zipModel, fileHeader);

        ProgressMonitor progressMonitor = new ProgressMonitor();
        String outPath = "tests/zipextraction";
        String newFileName = maliciousFileName;
        UnzipParameters unzipParams = new UnzipParameters();

        engine.unzipFile(progressMonitor, outPath, newFileName, unzipParams);

        File outputFile = new File(outPath + File.separator + newFileName);
        assertTrue(outputFile.exists());
    }

    @Test
    public void testUnzipWithNormalFile1() throws Exception {
        String fileName = "good.txt";
        File zipFile = ZipTestUtils.createTempZipFile(fileName);

        ZipModel zipModel = new ZipModel();
        zipModel.setZipFile(zipFile.getAbsolutePath());
        FileHeader fileHeader = new FileHeader();
        fileHeader.setFileName(fileName);
        fileHeader.setCompressionMethod(8);
        UnzipEngine engine = new UnzipEngine(zipModel, fileHeader);

        ProgressMonitor progressMonitor = new ProgressMonitor();
        String outPath = "tests/zipextraction";
        String newFileName = null;
        UnzipParameters unzipParams = new UnzipParameters();

        engine.unzipFile(progressMonitor, outPath, newFileName, unzipParams);
        File outputFile = new File(outPath + File.separator + fileName);
        assertTrue(outputFile.exists());
    }

    @Test
    public void testUnzipWithNormalFile2() throws Exception {
        String fileName = "good.txt";
        File zipFile = ZipTestUtils.createTempZipFile(fileName);

        ZipModel zipModel = new ZipModel();
        zipModel.setZipFile(zipFile.getAbsolutePath());
        FileHeader fileHeader = new FileHeader();
        fileHeader.setFileName(fileName);
        fileHeader.setCompressionMethod(8);
        UnzipEngine engine = new UnzipEngine(zipModel, fileHeader);

        ProgressMonitor progressMonitor = new ProgressMonitor();
        String outPath = "tests/zipextraction";
        String newFileName = "good2.txt";
        UnzipParameters unzipParams = new UnzipParameters();

        engine.unzipFile(progressMonitor, outPath, newFileName, unzipParams);

        engine.unzipFile(progressMonitor, outPath, newFileName, unzipParams);
        File outputFile = new File(outPath + File.separator + newFileName);
        assertTrue(outputFile.exists());
    }

    @Test(expected = ZipException.class)
    public void testUnzipWithNormalFileButMaliciousNewFileName() throws Exception {
        String fileName = "good.txt";
        File zipFile = ZipTestUtils.createTempZipFile(fileName);

        ZipModel zipModel = new ZipModel();
        zipModel.setZipFile(zipFile.getAbsolutePath());
        FileHeader fileHeader = new FileHeader();
        fileHeader.setFileName(fileName);
        fileHeader.setCompressionMethod(8);
        UnzipEngine engine = new UnzipEngine(zipModel, fileHeader);

        ProgressMonitor progressMonitor = new ProgressMonitor();
        String outPath = "tests/zipextraction";
        String newFileName = "../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../tmp/evil.txt";
        UnzipParameters unzipParams = new UnzipParameters();

        engine.unzipFile(progressMonitor, outPath, newFileName, unzipParams);

        engine.unzipFile(progressMonitor, outPath, newFileName, unzipParams);
        File outputFile = new File(outPath + File.separator + newFileName);
        assertTrue(outputFile.exists());
    }
}
