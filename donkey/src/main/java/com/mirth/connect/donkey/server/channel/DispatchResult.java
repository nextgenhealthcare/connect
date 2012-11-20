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
    private boolean lockAcquired;
    private Response selectedResponse;
    private ChannelException channelException;
    
    protected DispatchResult(long messageId, Message processedMessage, Response selectedResponse, boolean markAsProcessed, boolean removeContent, boolean lockAcquired) {
        this(messageId, processedMessage, selectedResponse, markAsProcessed, removeContent, lockAcquired, null);
    }
    
    protected DispatchResult(long messageId, Message processedMessage, Response selectedResponse, boolean markAsProcessed, boolean removeContent, boolean lockAcquired, ChannelException channelException) {
        this.messageId = messageId;
        this.processedMessage = processedMessage;
        this.markAsProcessed = markAsProcessed;
        this.removeContent = removeContent;
        this.selectedResponse = selectedResponse;
        this.lockAcquired = lockAcquired;
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
    
    public boolean isLockAcquired() {
    	return lockAcquired;
    }
    
    public Response getSelectedResponse() {
        return selectedResponse;
    }

    public ChannelException getChannelException() {
        return channelException;
    }
}
