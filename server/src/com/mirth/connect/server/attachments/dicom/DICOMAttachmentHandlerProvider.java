/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.attachments.dicom;

import com.mirth.connect.donkey.model.message.attachment.AttachmentHandler;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandlerProperties;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.server.attachments.MirthAttachmentHandlerProvider;
import com.mirth.connect.server.controllers.MessageController;

public class DICOMAttachmentHandlerProvider extends MirthAttachmentHandlerProvider {

    public DICOMAttachmentHandlerProvider(MessageController messageController) {
        super(messageController);
    }

    @Override
    public void setProperties(Channel channel, AttachmentHandlerProperties attachmentProperties) {}

    @Override
    public boolean canExtractAttachments() {
        return true;
    }

    @Override
    public byte[] replaceOutboundAttachment(byte[] content) {
        return content;
    }

    @Override
    public AttachmentHandler getHandler() {
        return new DICOMAttachmentHandler();
    }
}