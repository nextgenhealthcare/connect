/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import org.apache.log4j.Logger;

public class Attachment {
    private String id;
    private byte[] content;
    private String type;
    private Logger logger = Logger.getLogger(getClass());

    public Attachment() {}

    public Attachment(String id, byte[] content, String type) {
        this.id = id;
        this.content = content;
        this.type = type;
    }

    public String getAttachmentId() {
        return "${ATTACH:" + id + "}";
    }

    @Deprecated
    // TODO: Remove in 3.1
    public void setAttachmentId(String attachmentId) {
        logger.error("The Attachment.setAttachmentId(attachmentId) method is deprecated and will soon be removed. Please use setId(id) instead.");
        setId(attachmentId);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Deprecated
    // TODO: Remove in 3.1
    public byte[] getData() {
        logger.error("The Attachment.getData() method is deprecated and will soon be removed. Please use getContent() instead.");
        return getContent();
    }

    @Deprecated
    // TODO: Remove in 3.1
    public void setData(byte[] data) {
        logger.error("The Attachment.setData(data) method is deprecated and will soon be removed. Please use setContent(content) instead.");
        setContent(data);
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Deprecated
    // TODO: Remove in 3.1
    public int getSize() {
        logger.error("The Attachment.getSize() method is deprecated and will soon be removed. Please use getContent().length instead.");
        return content != null ? content.length : 0;
    }

    @Deprecated
    // TODO: Remove in 3.1
    public void setSize(int size) {
        logger.error("The Attachment.setSize(size) method is deprecated and will soon be removed. This method no longer does anything.");
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Deprecated
    // TODO: Remove in 3.1
    public String getMessageId() {
        logger.error("The Attachment.getMessageId() method is deprecated and will soon be removed. This method always returns an empty string.");
        return "";
    }

    @Deprecated
    // TODO: Remove in 3.1
    public void setMessageId(String messageId) {
        logger.error("The Attachment.setMessageId(messageId) method is deprecated and will soon be removed. This method no longer does anything.");
    }
}