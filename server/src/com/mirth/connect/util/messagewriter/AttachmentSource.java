package com.mirth.connect.util.messagewriter;

import java.util.List;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.attachment.Attachment;

public interface AttachmentSource {
    public List<Attachment> getMessageAttachments(Message message) throws ClientException;
}