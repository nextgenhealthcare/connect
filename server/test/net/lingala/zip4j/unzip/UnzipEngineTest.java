package net.lingala.zip4j.unzip;

import static org.junit.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Strings;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.UnzipParameters;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.progress.ProgressMonitor;

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

    @Test(expected=ZipException.class)
    public void testUnzipWithMaliciousFile1() throws Exception {
        String maliciousFileName = "../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../tmp/evil.txt";
        File maliciousZipFile = createTempZipFile(maliciousFileName);
        
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
     * This case should not throw a ZipSlip related exception because we are passing in a newFileName which will be used for the destination file
     */
    @Test
    public void testUnzipWithMaliciousFileButValidNewFileName() throws Exception {
        String maliciousFileName = "../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../tmp/evil.txt";
        File maliciousZipFile = createTempZipFile(maliciousFileName);
        
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
    
    @Test
    public void testUnzipWithNormalFile1() throws Exception {
        String fileName = "good.txt";
        File zipFile = createTempZipFile(fileName);
        
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
        File zipFile = createTempZipFile(fileName);
        
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

    // TODO Refactor this method into a util class
    private File createTempZipFile(String fileName) throws Exception {
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
