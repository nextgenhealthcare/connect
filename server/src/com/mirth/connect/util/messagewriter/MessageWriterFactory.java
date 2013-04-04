/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.util.messagewriter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.mirth.commons.encryption.Encryptor;

public class MessageWriterFactory {
    private final static String DATE_FORMAT = "yyyyMMddHHmmss";

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

    public MessageWriter getMessageWriter(MessageWriterOptions options, Encryptor encryptor, String channelId) throws MessageWriterException {
        String rootFolder = FilenameUtils.normalizeNoEndSeparator(options.getRootFolder());
        String filePattern = options.getFilePattern();

        if (filePattern.substring(0, 1).equals(IOUtils.DIR_SEPARATOR)) {
            filePattern = filePattern.substring(1);
        }

        MessageWriterVfs vfsWriter = new MessageWriterVfs("file://" + rootFolder, filePattern, options.getContentType(), options.isDestinationContent(), options.isEncrypt(), encryptor);

        if (options.getArchiver() == null) {
            return vfsWriter;
        }

        /*
         * If we are writing to an archive, make the vfsWriter write to a temporary folder that will
         * be removed once the archive file has been created
         */
        String tempFolder = rootFolder + IOUtils.DIR_SEPARATOR + UUID.randomUUID().toString();
        vfsWriter.setUri("file://" + tempFolder);

        // determine the archive's extension and file name
        String extension = getArchiveExtension(options.getArchiver(), options.getCompressor());
        File archiveFile = new File(rootFolder + IOUtils.DIR_SEPARATOR + channelId + "_" + new SimpleDateFormat(DATE_FORMAT).format(Calendar.getInstance().getTime()) + "." + extension);

        return new MessageWriterArchive(vfsWriter, new File(tempFolder), archiveFile, options.getArchiver(), options.getCompressor());
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
