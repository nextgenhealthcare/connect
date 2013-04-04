/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class ArchiveUtils {
    /**
     * The buffer size to use when copying files into an archive
     */
    private final static int BUFFER_SIZE = 1048576;

    private static Logger logger = Logger.getLogger(ArchiveUtils.class);

    /**
     * Create an archive file from files/subfolders in a given source folder.
     * 
     * @param sourceFolder
     *            Subfolders and files inside this folder will be copied into the archive
     * @param destinationFile
     *            The destination archive file
     * @param archiver
     *            The archiver format, see
     *            org.apache.commons.compress.archivers.ArchiveStreamFactory
     * @throws CompressException
     */
    public static void createArchive(File sourceFolder, File destinationFile, String archiver) throws CompressException {
        createArchive(sourceFolder, destinationFile, archiver, null);
    }

    /**
     * Create an archive file from files/sub-folders in a given source folder.
     * 
     * @param sourceFolder
     *            Sub-folders and files inside this folder will be copied into the archive
     * @param destinationFile
     *            The destination archive file
     * @param archiver
     *            The archiver format, see
     *            org.apache.commons.compress.archivers.ArchiveStreamFactory
     * @param compressor
     *            The compressor format, see
     *            org.apache.commons.compress.compressors.CompressorStreamFactory
     * @throws CompressException
     */
    public static void createArchive(File sourceFolder, File destinationFile, String archiver, String compressor) throws CompressException {
        if (!sourceFolder.isDirectory() || !sourceFolder.exists()) {
            throw new CompressException("Invalid source folder: " + sourceFolder.getAbsolutePath());
        }

        logger.debug("Creating archive \"" + destinationFile.getAbsolutePath() + "\" from folder \"" + sourceFolder.getAbsolutePath() + "\"");
        OutputStream outputStream = null;
        ArchiveOutputStream archiveOutputStream = null;

        try {
            /*
             * The commons-compress documentation recommends constructing a ZipArchiveOutputStream
             * with the archive file when using the ZIP archive format. See
             * http://commons.apache.org/proper/commons-compress/zip.html
             */
            if (archiver.equals(ArchiveStreamFactory.ZIP) && compressor == null) {
                archiveOutputStream = new ZipArchiveOutputStream(destinationFile);
            } else {
                // if not using ZIP format, use the archiver/compressor stream factories to initialize the archive output stream
                outputStream = new BufferedOutputStream(new FileOutputStream(destinationFile));

                if (compressor != null) {
                    outputStream = new CompressorStreamFactory().createCompressorOutputStream(compressor, outputStream);
                }

                archiveOutputStream = new ArchiveStreamFactory().createArchiveOutputStream(archiver, outputStream);
            }

            createFolderArchive(sourceFolder, archiveOutputStream, sourceFolder.getAbsolutePath() + IOUtils.DIR_SEPARATOR);
        } catch (Exception e) {
            throw new CompressException(e);
        } finally {
            IOUtils.closeQuietly(archiveOutputStream);
            IOUtils.closeQuietly(outputStream);
            logger.debug("Finished creating archive \"" + destinationFile.getAbsolutePath() + "\"");
        }
    }

    /**
     * Recursively copies folders/files into the given ArchiveOutputStream.
     */
    private static void createFolderArchive(File folder, ArchiveOutputStream archiveOutputStream, String rootFolder) throws CompressException {
        byte[] buffer = new byte[BUFFER_SIZE];

        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                createFolderArchive(file, archiveOutputStream, rootFolder);
            } else {
                try {
                    // extract/remove the rootFolder from the file's absolute path before adding it to the archive
                    String entryName = file.getAbsolutePath();

                    if (entryName.substring(0, rootFolder.length()).equals(rootFolder)) {
                        entryName = entryName.substring(rootFolder.length());
                    }

                    archiveOutputStream.putArchiveEntry(archiveOutputStream.createArchiveEntry(file, entryName));
                    InputStream inputStream = new FileInputStream(file);

                    logger.debug("Adding \"" + entryName + "\" to archive");

                    try {
                        IOUtils.copyLarge(inputStream, archiveOutputStream, buffer);
                    } finally {
                        IOUtils.closeQuietly(inputStream);
                        archiveOutputStream.closeArchiveEntry();
                    }
                } catch (Exception e) {
                    throw new CompressException(e);
                }
            }
        }
    }

    /**
     * Extracts folders/files from an archive into the destinationFolder. Supports reading any type
     * of archive file supported by Apache's commons-compress.
     * 
     * @param archiveFile
     *            A compressed or uncompressed archive file
     * @param destinationFolder
     *            Destination folder, will be created if it doesn't exist.
     * @throws CompressException
     */
    public static void extractArchive(File archiveFile, File destinationFolder) throws CompressException {
        /**
         * Since we don't know what type of archive file we've received, try treating it as a zip
         * file first and extract using our zip-optimized extract method. If an exception occurs,
         * fall back to the generic method.
         */
        try {
            extractZipArchive(archiveFile, destinationFolder);
        } catch (CompressException e) {
            extractGenericArchive(archiveFile, destinationFolder);
        }
    }

    /**
     * Extracts an archive using generic stream factories provided by commons-compress.
     */
    private static void extractGenericArchive(File archiveFile, File destinationFolder) throws CompressException {
        try {
            InputStream inputStream = new BufferedInputStream(FileUtils.openInputStream(archiveFile));

            try {
                inputStream = new CompressorStreamFactory().createCompressorInputStream(inputStream);
            } catch (CompressorException e) {
                // a compressor was not recognized in the stream, in this case we leave the inputStream as-is
            }

            ArchiveInputStream archiveInputStream = new ArchiveStreamFactory().createArchiveInputStream(inputStream);
            ArchiveEntry entry;
            int inputOffset = 0;
            byte[] buffer = new byte[BUFFER_SIZE];

            try {
                while (null != (entry = archiveInputStream.getNextEntry())) {
                    File outputFile = new File(destinationFolder.getAbsolutePath() + IOUtils.DIR_SEPARATOR + entry.getName());

                    if (entry.isDirectory()) {
                        FileUtils.forceMkdir(outputFile);
                    } else {
                        FileOutputStream outputStream = null;

                        try {
                            outputStream = FileUtils.openOutputStream(outputFile);
                            int bytesRead;
                            int outputOffset = 0;

                            while ((bytesRead = archiveInputStream.read(buffer, inputOffset, BUFFER_SIZE)) > 0) {
                                outputStream.write(buffer, outputOffset, bytesRead);
                                inputOffset += bytesRead;
                                outputOffset += bytesRead;
                            }
                        } finally {
                            IOUtils.closeQuietly(outputStream);
                        }
                    }
                }
            } finally {
                IOUtils.closeQuietly(archiveInputStream);
            }
        } catch (Exception e) {
            throw new CompressException(e);
        }
    }

    /**
     * Extracts folders/files from a zip archive using zip-optimized code from commons-compress.
     */
    private static void extractZipArchive(File archiveFile, File destinationFolder) throws CompressException {
        ZipFile zipFile = null;

        try {
            zipFile = new ZipFile(archiveFile);
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            ZipArchiveEntry entry = null;
            byte[] buffer = new byte[BUFFER_SIZE];

            for (; entries.hasMoreElements(); entry = entries.nextElement()) {
                File outputFile = new File(destinationFolder.getAbsolutePath() + IOUtils.DIR_SEPARATOR + entry.getName());

                if (entry.isDirectory()) {
                    FileUtils.forceMkdir(outputFile);
                } else {
                    InputStream inputStream = zipFile.getInputStream(entry);
                    OutputStream outputStream = FileUtils.openOutputStream(outputFile);

                    try {
                        IOUtils.copyLarge(inputStream, outputStream, buffer);
                    } finally {
                        IOUtils.closeQuietly(inputStream);
                        IOUtils.closeQuietly(outputStream);
                    }
                }
            }
        } catch (Exception e) {
            throw new CompressException(e);
        } finally {
            ZipFile.closeQuietly(zipFile);
        }
    }

    public static class CompressException extends Exception {
        public CompressException(String message) {
            super(message);
        }

        public CompressException(Throwable cause) {
            super(cause);
        }
    }
}
