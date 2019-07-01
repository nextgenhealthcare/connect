/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.attachments.passthru;

import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.model.message.attachment.AttachmentException;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandler;
import com.mirth.connect.donkey.server.channel.Channel;

public class PassthruAttachmentHandler implements AttachmentHandler {
    @Override
    public void initialize(RawMessage message, Channel channel) throws AttachmentException {}

    @Override
    public Attachment nextAttachment() throws AttachmentException {
        return null;
    }

    @Override
    public String shutdown() throws AttachmentException {
        return null;
    }
}