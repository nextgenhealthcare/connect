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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import net.lingala.zip4j.io.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.util.messagewriter.EncryptionType;

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
        createArchive(sourceFolder, destinationFile, archiver, null, null, null);
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
    public static void createArchive(File sourceFolder, File destinationFile, String archiver, String compressor, String password, EncryptionType encryptionType) throws CompressException {
        if (!sourceFolder.isDirectory() || !sourceFolder.exists()) {
            throw new CompressException("Invalid source folder: " + sourceFolder.getAbsolutePath());
        }

        logger.debug("Creating archive \"" + destinationFile.getAbsolutePath() + "\" from folder \"" + sourceFolder.getAbsolutePath() + "\"");
        OutputStream outputStream = null;
        OutputStream archiveOutputStream = null;

        try {
            /*
             * The commons-compress documentation recommends constructing a ZipArchiveOutputStream
             * with the archive file when using the ZIP archive format. See
             * http://commons.apache.org/proper/commons-compress/zip.html
             */
            if (archiver.equals(ArchiveStreamFactory.ZIP) && compressor == null) {
                archiveOutputStream = new ZipOutputStream(new FileOutputStream(destinationFile));
            } else {
                // if not using ZIP format, use the archiver/compressor stream factories to initialize the archive output stream
                outputStream = new BufferedOutputStream(new FileOutputStream(destinationFile));

                if (compressor != null) {
                    outputStream = new CompressorStreamFactory().createCompressorOutputStream(compressor, outputStream);
                }

                archiveOutputStream = new ArchiveStreamFactory().createArchiveOutputStream(archiver, outputStream);
            }

            createFolderArchive(sourceFolder, archiveOutputStream, sourceFolder.getAbsolutePath() + IOUtils.DIR_SEPARATOR, password, encryptionType);
        } catch (Exception e) {
            throw new CompressException(e);
        } finally {
            IOUtils.closeQuietly(archiveOutputStream);
            IOUtils.closeQuietly(outputStream);
            logger.debug("Finished creating archive \"" + destinationFile.getAbsolutePath() + "\"");
        }
    }

    private static void createFolderArchive(File folder, OutputStream outputStream, String rootFolder, String password, EncryptionType encryptionType) throws CompressException {
        byte[] buffer = new byte[BUFFER_SIZE];
        ZipOutputStream zipOutputStream = null;

        try {
            for (File file : folder.listFiles()) {
                if (file.isDirectory()) {
                    createFolderArchive(file, outputStream, rootFolder, password, encryptionType);
                } else {
                    try {
                        // extract/remove the rootFolder from the file's absolute path before adding it to the archive
                        String entryName = file.getAbsolutePath();

                        if (entryName.substring(0, rootFolder.length()).equals(rootFolder)) {
                            entryName = entryName.substring(rootFolder.length());
                        }

                        ArchiveOutputStream archiveOutputStream = null;
                        InputStream inputStream = new FileInputStream(file);
                        try {
                            logger.debug("Adding \"" + entryName + "\" to archive");

                            if (outputStream instanceof ArchiveOutputStream) {
                                archiveOutputStream = (ArchiveOutputStream) outputStream;
                                archiveOutputStream.putArchiveEntry(archiveOutputStream.createArchiveEntry(file, entryName));
                            } else if (outputStream instanceof ZipOutputStream) {
                                zipOutputStream = (ZipOutputStream) outputStream;

                                ZipParameters parameters = new ZipParameters();
                                parameters.setSourceExternalStream(true);
                                parameters.setFileNameInZip(entryName);
                                parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
                                parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

                                if (StringUtils.isNotBlank(password)) {
                                    parameters.setEncryptFiles(true);

                                    boolean isAes = encryptionType != EncryptionType.STANDARD;
                                    parameters.setEncryptionMethod(isAes ? Zip4jConstants.ENC_METHOD_AES : Zip4jConstants.ENC_METHOD_STANDARD);

                                    if (isAes) {
                                        parameters.setAesKeyStrength(encryptionType.getKeyStrength());
                                    }

                                    parameters.setPassword(password);
                                }

                                zipOutputStream.putNextEntry(null, parameters);
                            }

                            IOUtils.copyLarge(inputStream, outputStream, buffer);
                        } finally {
                            IOUtils.closeQuietly(inputStream);

                            if (outputStream instanceof ArchiveOutputStream) {
                                archiveOutputStream.closeArchiveEntry();
                            } else if (outputStream instanceof ZipOutputStream) {
                                zipOutputStream.closeEntry();
                            }
                        }
                    } catch (Exception e) {
                        throw new CompressException(e);
                    }
                }
            }
        } finally {
            if (zipOutputStream != null) {
                try {
                    zipOutputStream.finish();
                } catch (Exception e) {
                    throw new CompressException(e);
                }
            }
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
