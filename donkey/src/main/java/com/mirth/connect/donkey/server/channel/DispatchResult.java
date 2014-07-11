/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import java.util.Calendar;

import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.Response;

public class DispatchResult {
    private long messageId;
    private Message processedMessage;
    private boolean markAsProcessed;
    private boolean removeContent;
    private boolean removeAttachments;
    private boolean lockAcquired;
    private Response selectedResponse;
    private ChannelException channelException;
    private Calendar responseDate;
    private boolean attemptedResponse;
    private String responseError;

    protected DispatchResult(long messageId, Message processedMessage, Response selectedResponse, boolean markAsProcessed, boolean removeContent, boolean removeAttachments, boolean lockAcquired) {
        this(messageId, processedMessage, selectedResponse, markAsProcessed, removeContent, removeAttachments, lockAcquired, null);
    }

    protected DispatchResult(long messageId, Message processedMessage, Response selectedResponse, boolean markAsProcessed, boolean removeContent, boolean removeAttachments, boolean lockAcquired, ChannelException channelException) {
        this.messageId = messageId;
        this.processedMessage = processedMessage;
        this.markAsProcessed = markAsProcessed;
        this.removeContent = removeContent;
        this.removeAttachments = removeAttachments;
        this.selectedResponse = selectedResponse;
        this.lockAcquired = lockAcquired;
        this.channelException = channelException;
        this.responseDate = Calendar.getInstance();
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

    public boolean isRemoveAttachments() {
        return removeAttachments;
    }

    public boolean isLockAcquired() {
        return lockAcquired;
    }

    protected void setLockAcquired(boolean lockAcquired) {
        this.lockAcquired = lockAcquired;
    }

    public Response getSelectedResponse() {
        return selectedResponse;
    }

    public ChannelException getChannelException() {
        return channelException;
    }

    public Calendar getResponseDate() {
        return responseDate;
    }

    public boolean isAttemptedResponse() {
        return attemptedResponse;
    }

    public void setAttemptedResponse(boolean attemptedResponse) {
        this.attemptedResponse = attemptedResponse;
    }

    public String getResponseError() {
        return responseError;
    }

    public void setResponseError(String responseError) {
        this.responseError = responseError;
    }
}
