/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message;

public class ImmutableMessageContent {
    private MessageContent messageContent;

    public ImmutableMessageContent(MessageContent messageContent) {
        this.messageContent = messageContent;
    }

    public ContentType getContentType() {
        return messageContent.getContentType();
    }

    public String getContent() {
        return messageContent.getContent();
    }

    public long getMessageId() {
        return messageContent.getMessageId();
    }

    public int getMetaDataId() {
        return messageContent.getMetaDataId();
    }
    
    public String getDataType() {
        return messageContent.getDataType();
    }
}
