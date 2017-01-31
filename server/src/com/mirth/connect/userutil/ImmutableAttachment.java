/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.userutil;

import com.mirth.connect.donkey.model.message.attachment.Attachment;

/**
 * This class represents an message attachment and is used to retrieve details such as the
 * replacement token or content type.
 */
public class ImmutableAttachment {

    private Attachment attachment;

    /**
     * Instantiates a new ImmutableAttachment object.
     * 
     * @param attachment
     *            The Attachment object that this object will reference for retrieving data.
     */
    public ImmutableAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    /**
     * Returns the unique replacement token for the attachment. This token should replace the
     * attachment content in the message string, and will be used to re-attach the attachment
     * content in the outbound message before it is sent to a downstream system.
     */
    public String getAttachmentId() {
        return attachment.getAttachmentId();
    }

    /**
     * Returns the unique ID for the attachment.
     */
    public String getId() {
        return attachment.getId();
    }

    /**
     * Returns the content of the attachment as a byte array.
     */
    public byte[] getContent() {
        return attachment.getContent();
    }

    /**
     * Returns the MIME type of the attachment.
     */
    public String getType() {
        return attachment.getType();
    }

    /**
     * Returns a boolean indicating whether the attachment content is encrypted.
     */
    public boolean isEncrypted() {
        return attachment.isEncrypted();
    }
}