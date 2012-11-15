/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.List;

import com.mirth.commons.encryption.Encryptor;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.model.filters.MessageFilter;
import com.mirth.connect.util.export.MessageExportOptions;
import com.mirth.connect.util.export.MessageExporter.MessageExporterException;

public abstract class MessageController {
    public static MessageController getInstance() {
        return ControllerFactory.getFactory().createMessageController();
    }

    public abstract long getMaxMessageId(String channelId);
    
    public abstract List<Message> getMessages(MessageFilter filter, Channel channel, Boolean includeContent, Integer offset, Integer limit);
    
    public abstract Long getMessageCount(MessageFilter filter, Channel channel);
    
    public abstract Message getMessageContent(String channelId, Long messageId);
    
    public abstract List<Attachment> getMessageAttachmentIds(String channelId, Long messageId);
    
    public abstract Attachment getMessageAttachment(String channelId, String attachmentId);
    
    public abstract List<Attachment> getMessageAttachment(String channelId, Long messageId);

    public abstract int removeMessages(String channelId, MessageFilter filter);
    
    public abstract boolean clearMessages(String channelId) throws ControllerException;

    public abstract void reprocessMessages(String channelId, MessageFilter filter, boolean replace, List<Integer> reprocessMetaDataIds, int userId);

    public abstract void importMessage(String channelId, Message message) throws MessageImportException;
    
    public abstract int exportMessages(MessageExportOptions options) throws MessageExporterException;

    public abstract int pruneMessages(List<String> channelIds, int limit) throws MessagePrunerException;

    public abstract void decryptMessage(Message message, Encryptor encryptor);

    public abstract void encryptMessage(Message message, Encryptor encryptor);
}
