/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.util.messagewriter;

import java.io.Serializable;

import com.mirth.connect.donkey.model.message.ContentType;

public class MessageWriterOptions implements Serializable {
    private ContentType contentType;
    private boolean destinationContent;
    private boolean encrypt;
    private String rootFolder;
    private String filePattern;
    private String archiver;
    private String compressor;

    public ContentType getContentType() {
        return contentType;
    }

    /**
     * @param contentType
     *            The ContentType that will be extracted from the message for writing. If null or if
     *            a ContentType is not provided, the MessageWriter will write the entire message in
     *            a serialized format.
     */
    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public boolean isDestinationContent() {
        return destinationContent;
    }

    /**
     * @param destinationContent
     *            If true, the content to write will be extracted from the destination message(s),
     *            rather than the source message (see setContentType())
     */
    public void setDestinationContent(boolean destinationContent) {
        this.destinationContent = destinationContent;
    }

    public boolean isEncrypt() {
        return encrypt;
    }

    /**
     * @param encrypt
     *            If true, the MessageWriter will encrypt message content before writing.
     */
    public void setEncrypt(boolean encrypt) {
        this.encrypt = encrypt;
    }

    public String getRootFolder() {
        return rootFolder;
    }

    /**
     * @param rootFolder
     *            The root folder to contain the written messages/sub-folders.
     */
    public void setRootFolder(String rootFolder) {
        this.rootFolder = rootFolder;
    }

    public String getFilePattern() {
        return filePattern;
    }

    /**
     * @param filePattern
     *            A string defining the folder/filename(s) for writing messages. It may contain
     *            variables to be replaced.
     */
    public void setFilePattern(String filePattern) {
        this.filePattern = filePattern;
    }

    public String getArchiver() {
        return archiver;
    }

    /**
     * @param archiver
     *            The archiver format to use to archive messages/folders that are written to the
     *            root folder. See org.apache.commons.compress.archivers.ArchiveStreamFactory.
     */
    public void setArchiver(String archiver) {
        this.archiver = archiver;
    }

    public String getCompressor() {
        return compressor;
    }

    /**
     * @param compressor
     *            The compressor format to use to compress the archive file. See
     *            org.apache.commons.compress.compressors.CompressorStreamFactory.
     */
    public void setCompressor(String compressor) {
        this.compressor = compressor;
    }
}
