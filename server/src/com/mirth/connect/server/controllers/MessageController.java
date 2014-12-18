/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.model.MessageImportResult;
import com.mirth.connect.model.filters.MessageFilter;
import com.mirth.connect.util.MessageExporter.MessageExportException;
import com.mirth.connect.util.MessageImporter.MessageImportException;
import com.mirth.connect.util.MessageImporter.MessageImportInvalidPathException;
import com.mirth.connect.util.messagewriter.MessageWriterOptions;

public abstract class MessageController {
    public static MessageController getInstance() {
        return ControllerFactory.getFactory().createMessageController();
    }

    public abstract long getMaxMessageId(String channelId);

    public abstract long getMinMessageId(String channelId);

    public abstract List<Message> getMessages(MessageFilter filter, Channel channel, Boolean includeContent, Integer offset, Integer limit);

    public abstract Long getMessageCount(MessageFilter filter, Channel channel);

    public abstract Message getMessageContent(String channelId, Long messageId);

    public abstract List<Attachment> getMessageAttachmentIds(String channelId, Long messageId);

    public abstract Attachment getMessageAttachment(String channelId, String attachmentId);

    public abstract List<Attachment> getMessageAttachment(String channelId, Long messageId);

    public abstract void removeMessages(String channelId, MessageFilter filter);

    public abstract void reprocessMessages(String channelId, MessageFilter filter, boolean replace, Collection<Integer> reprocessMetaDataIds);

    public abstract void importMessage(String channelId, Message message) throws MessageImportException;

    public abstract MessageImportResult importMessagesServer(String channelId, String path, boolean includeSubfolders) throws MessageImportException, InterruptedException, MessageImportInvalidPathException;

    public abstract int exportMessages(final String channelId, final MessageFilter messageFilter, int pageSize, boolean includeAttachments, MessageWriterOptions options) throws MessageExportException, InterruptedException;

    public abstract void exportAttachment(String channelId, String attachmentId, String filePath, boolean binary) throws IOException;
}
