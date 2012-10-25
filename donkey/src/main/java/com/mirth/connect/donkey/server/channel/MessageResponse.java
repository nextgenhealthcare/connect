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

public class MessageResponse {
    private long messageId;
    private Response response;
    private boolean markAsProcessed;
    private Message processedMessage;

    public MessageResponse(long messageId, Response response, boolean markAsProcessed, Message processedMessage) {
        this.messageId = messageId;
        this.response = response;
        this.markAsProcessed = markAsProcessed;
        this.setProcessedMessage(processedMessage);
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public boolean isMarkAsProcessed() {
        return markAsProcessed;
    }

    public void setMarkAsProcessed(boolean markAsProcessed) {
        this.markAsProcessed = markAsProcessed;
    }

    public Message getProcessedMessage() {
        return processedMessage;
    }

    public void setProcessedMessage(Message processedMessage) {
        this.processedMessage = processedMessage;
    }
}
