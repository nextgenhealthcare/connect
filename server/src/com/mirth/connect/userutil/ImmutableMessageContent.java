/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.userutil;

import com.mirth.connect.donkey.model.message.MessageContent;

/**
 * This class represents content associated with a connector message.
 */
public class ImmutableMessageContent {
    private MessageContent messageContent;

    /**
     * Instantiates a new ImmutableMessageContent object.
     * 
     * @param messageContent
     *            The MessageContent object that this object will reference for
     *            retrieving data.
     */
    public ImmutableMessageContent(MessageContent messageContent) {
        this.messageContent = messageContent;
    }

    /**
     * Returns the ContentType of this message content (e.g. RAW, ENCODED).
     */
    public ContentType getContentType() {
        return ContentType.fromDonkeyContentType(messageContent.getContentType());
    }

    /**
     * Returns the actual content, as a string.
     */
    public String getContent() {
        return messageContent.getContent();
    }

    /**
     * Returns the sequential ID of the overall Message associated with this
     * message content.
     */
    public long getMessageId() {
        return messageContent.getMessageId();
    }

    /**
     * Returns the metadata ID of the connector associated with this message
     * content. Note that the source connector has a metadata ID of 0.
     */
    public int getMetaDataId() {
        return messageContent.getMetaDataId();
    }

    /**
     * Returns the data type (e.g. "HL7V2") of this message content.
     */
    public String getDataType() {
        return messageContent.getDataType();
    }
}