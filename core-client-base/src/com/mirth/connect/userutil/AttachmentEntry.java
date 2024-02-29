/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.userutil;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * Used to store and retrieve details about message attachments such as the name, contents, and
 * MIME type. When using a variable to specify attachments, such as in an SMTP Sender or
 * Web Service Sender, the variable must reference a list of AttachmentEntry objects.
 */
public class AttachmentEntry implements Serializable {
    private String name;
    private String content;
    private String mimeType;

    /**
     * Instantiates a new AttachmentEntry with no name, content, or MIME type.
     */
    public AttachmentEntry() {}

    /**
     * Instantiates a new AttachmentEntry that copies the name, content, and MIME type
     * from a given AttachmentEntry object.
     * 
     * @param attachment
     *            The AttachmentEntry object to copy.         
     */
    public AttachmentEntry(AttachmentEntry attachment) {
        name = attachment.getName();
        content = attachment.getContent();
        mimeType = attachment.getMimeType();
    }

    /**
     * Instantiates a new AttachmentEntry with a name, content, and a MIME type.
     * 
     * @param name
     *            The name of the attachment entry.
     * @param content
     *            The content to store for the attachment entry.
     * @param mimeType
     *            The MIME type of the attachment entry.
     */
    public AttachmentEntry(String name, String content, String mimeType) {
        this.name = name;
        this.content = content;
        this.mimeType = mimeType;
    }
    
    /**
     * Returns the name of the attachment entry.
     * @return The name of the attachment entry.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the attachment entry.
     * @param name
     *            The name of the attachment entry.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the content of the attachment entry.
     * @return The content of the attachment entry.
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content of the attachment entry.
     * @param content
     *            The content of the attachment entry.
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Returns the MIME type of the attachment entry.
     * @return The MIME type of the attachment entry.
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Sets the MIME type of the attachment entry.
     * @param mimeType
     *            The MIME type of the attachment entry.
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
}
