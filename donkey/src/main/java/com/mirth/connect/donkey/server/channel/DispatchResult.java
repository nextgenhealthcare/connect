/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.Response;

public class DispatchResult {
    private long messageId;
    private Message processedMessage;
    private boolean markAsProcessed;
    private boolean removeContent;
    private Response selectedResponse;
    private ChannelException channelException;
    
    public DispatchResult(long messageId, Message processedMessage, Response selectedResponse, boolean markAsProcessed, boolean removeContent) {
        this(messageId, processedMessage, selectedResponse, markAsProcessed, removeContent, null);
    }
    
    public DispatchResult(long messageId, Message processedMessage, Response selectedResponse, boolean markAsProcessed, boolean removeContent, ChannelException channelException) {
        this.messageId = messageId;
        this.processedMessage = processedMessage;
        this.markAsProcessed = markAsProcessed;
        this.removeContent = removeContent;
        this.selectedResponse = selectedResponse;
        this.channelException = channelException;
    }
    
    public long getMessageId() {
        return messageId;
    }

    public Message getProcessedMessage() {
        return processedMessage;
    }

    public boolean isMarkAsProcessed() {
        return markAsProcessed;
    }

    public boolean isRemoveContent() {
        return removeContent;
    }
    
    public Response getSelectedResponse() {
        return selectedResponse;
    }

    public ChannelException getChannelException() {
        return channelException;
    }
}
