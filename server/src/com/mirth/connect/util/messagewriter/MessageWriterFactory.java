/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util.messagewriter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.mirth.commons.encryption.Encryptor;
import com.mirth.connect.util.FilenameUtils;

public class MessageWriterFactory {
    public final static String ARCHIVE_DATE_PATTERN = "yyyy-MM-dd-HH-mm-ss";
    
    private static MessageWriterFactory instance;

    public static MessageWriterFactory getInstance() {
        if (instance == null) {
            synchronized (MessageWriterFactory.class) {
                instance = new MessageWriterFactory();
            }
        }

        return instance;
    }

    private MessageWriterFactory() {}

    public MessageWriter getMessageWriter(MessageWriterOptions options, Encryptor encryptor) throws MessageWriterException {
        String rootFolder = FilenameUtils.getAbsolutePath(new File(options.getBaseFolder()), options.getRootFolder());
        String filePattern = options.getFilePattern();

        if (filePattern.substring(0, 1).equals(IOUtils.DIR_SEPARATOR)) {
            filePattern = filePattern.substring(1);
        }

        MessageWriterVfs vfsWriter = new MessageWriterVfs("file://" + rootFolder, filePattern, options.getContentType(), options.isDestinationContent(), options.isEncrypt(), encryptor);

        if (options.getArchiveFormat() == null) {
            return vfsWriter;
        }
        
        if (options.getArchiveFileName() == null) {
            options.setArchiveFileName(new SimpleDateFormat(ARCHIVE_DATE_PATTERN).format(Calendar.getInstance().getTime()));
        }
        
        /*
         * If we are writing to an archive, make the vfsWriter write to a temporary folder that will
         * be removed once the archive file has been created
         */
        String tempFolder = rootFolder + IOUtils.DIR_SEPARATOR + "." + options.getArchiveFileName();
        FileUtils.deleteQuietly(new File(tempFolder));
        vfsWriter.setUri("file://" + tempFolder);

        File archiveFile = new File(rootFolder + IOUtils.DIR_SEPARATOR + options.getArchiveFileName() + "." + getArchiveExtension(options.getArchiveFormat(), options.getCompressFormat()));

        return new MessageWriterArchive(vfsWriter, new File(tempFolder), archiveFile, options.getArchiveFormat(), options.getCompressFormat());
    }

    private String getArchiveExtension(String archiver, String compressor) {
        if (compressor == null) {
            return archiver;
        }

        if (compressor.equals(CompressorStreamFactory.BZIP2)) {
            compressor = "bz2";
        }

        return archiver + "." + compressor;
    }
}
