/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message.attachment;

import java.util.List;

public class AttachmentMessage {
    private List<Attachment> attachments;
    private String replacedMessage;

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public String getReplacedMessage() {
        return replacedMessage;
    }

    public void setReplacedMessage(String replacedMessage) {
        this.replacedMessage = replacedMessage;
    }
}
