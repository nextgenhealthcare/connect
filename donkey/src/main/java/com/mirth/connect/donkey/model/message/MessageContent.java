/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message;

import java.io.Serializable;

public class MessageContent implements Serializable {
    private String channelId;
    private long messageId;
    private int metaDataId;
    private ContentType contentType;
    private String content;
    private String dataType;
    private String encryptedContent;

    public MessageContent() {}

    public MessageContent(String channelId, long messageId, int metaDataId, ContentType contentType, String content, String dataType, String encryptedContent) {
        this.channelId = channelId;
        this.messageId = messageId;
        this.metaDataId = metaDataId;
        this.contentType = contentType;
        this.content = content;
        this.dataType = dataType;
        this.encryptedContent = encryptedContent;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEncryptedContent() {
        return encryptedContent;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public void setEncryptedContent(String encryptedContent) {
        this.encryptedContent = encryptedContent;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public int getMetaDataId() {
        return metaDataId;
    }

    public void setMetaDataId(Integer metaDataId) {
        this.metaDataId = metaDataId;
    }
}
