package com.mirth.connect.server.attachments;

import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.model.message.attachment.AttachmentException;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandlerProperties;
import com.mirth.connect.donkey.server.channel.Channel;

public class PassthruAttachmentHandler extends MirthAttachmentHandler {
    @Override
    public void initialize(String message, Channel channel) throws AttachmentException {}

    @Override
    public void initialize(byte[] bytes, Channel channel) throws AttachmentException {}

    @Override
    public Attachment nextAttachment() throws AttachmentException {
        return null;
    }

    @Override
    public String shutdown() throws AttachmentException {
        return null;
    }

    @Override
    public void setProperties(AttachmentHandlerProperties attachmentProperties) {}

    @Override
    public boolean canExtractAttachments() {
        return false;
    }
}
