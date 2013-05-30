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
import java.io.FileNotFoundException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.util.ArchiveUtils;

public class MessageWriterArchive implements MessageWriter {
    private MessageWriterVfs vfsWriter;
    private File rootFolder;
    private File archiveFile;
    private String archiver;
    private String compressor;
    private boolean messagesWritten;

    /**
     * Writes messages to the file-system using MessageWriterVfs, then moves the resulting
     * folders/files into the archive file.
     * 
     * @param vfsWriter
     * @param rootFolder
     * @param archiveFile
     * @param archiver
     *            The archiver type, see org.apache.commons.compress.archivers.ArchiveStreamFactory
     * @param compressor
     *            The compressor type, see
     *            org.apache.commons.compress.compressors.CompressorStreamFactory
     */
    public MessageWriterArchive(MessageWriterVfs vfsWriter, File rootFolder, File archiveFile, String archiver, String compressor) {
        this.vfsWriter = vfsWriter;
        this.rootFolder = rootFolder;
        this.archiveFile = archiveFile;
        this.archiver = archiver;
        this.compressor = compressor;
    }

    @Override
    public boolean write(Message message) throws MessageWriterException {
        boolean result = vfsWriter.write(message);

        if (!messagesWritten && result) {
            messagesWritten = true;
        }

        return result;
    }

    /**
     * Ends message writing and moves the written folders/files into the archive file.
     */
    @Override
    public void close() throws MessageWriterException {
        vfsWriter.close();

        if (messagesWritten) {
            try {
                File tempFile = new File(archiveFile.getParent() + IOUtils.DIR_SEPARATOR + "." + archiveFile.getName());
                
                try {
                    FileUtils.forceDelete(tempFile);
                } catch (FileNotFoundException e) {
                }
                
                ArchiveUtils.createArchive(rootFolder, tempFile, archiver, compressor);
                
                try {
                    FileUtils.forceDelete(archiveFile);
                } catch (FileNotFoundException e) {
                }
                
                FileUtils.moveFile(tempFile, archiveFile);
            } catch (Exception e) {
                throw new MessageWriterException(e);
            } finally {
                FileUtils.deleteQuietly(rootFolder);
            }
        }
    }
}
