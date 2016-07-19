/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util.messagewriter;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.model.CalendarToStringStyle;

public class MessageWriterOptions implements Serializable {
    private ContentType contentType;
    private boolean destinationContent;
    private boolean encrypt;
    private boolean includeAttachments;
    private String baseFolder;
    private String rootFolder;
    private String filePattern;
    private String archiveFileName;
    private String archiveFormat;
    private String compressFormat;
    private boolean passwordEnabled;
    private String password;
    private EncryptionType encryptionType;

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

    public boolean includeAttachments() {
        return includeAttachments;
    }

    /**
     * @param includeAttachments
     *            If true, the MessageWriter will include attachments before writing.
     */
    public void setIncludeAttachments(boolean includeAttachments) {
        this.includeAttachments = includeAttachments;
    }

    public String getBaseFolder() {
        return baseFolder;
    }

    /**
     * @param baseDir
     *            The base directory to use when resolving relative paths in the root folder.
     */
    public void setBaseFolder(String baseDir) {
        this.baseFolder = baseDir;
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

    public String getArchiveFileName() {
        return archiveFileName;
    }

    public void setArchiveFileName(String archiveFileName) {
        this.archiveFileName = archiveFileName;
    }

    public String getArchiveFormat() {
        return archiveFormat;
    }

    /**
     * @param archiveFormat
     *            The archiver format to use to archive messages/folders that are written to the
     *            root folder. See org.apache.commons.compress.archivers.ArchiveStreamFactory.
     */
    public void setArchiveFormat(String archiveFormat) {
        this.archiveFormat = archiveFormat;
    }

    public String getCompressFormat() {
        return compressFormat;
    }

    /**
     * @param compressor
     *            The compressor format to use to compress the archive file. See
     *            org.apache.commons.compress.compressors.CompressorStreamFactory.
     */
    public void setCompressFormat(String compressFormat) {
        this.compressFormat = compressFormat;
    }

    public boolean isPasswordEnabled() {
        return passwordEnabled;
    }

    public void setPasswordEnabled(boolean passwordEnabled) {
        this.passwordEnabled = passwordEnabled;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public EncryptionType getEncryptionType() {
        return encryptionType;
    }

    public void setEncryptionType(EncryptionType encryptionType) {
        this.encryptionType = encryptionType;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, CalendarToStringStyle.instance());
    }
}
