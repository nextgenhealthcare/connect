/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.tasks;

import java.util.Calendar;
import java.util.List;

public class ReprocessingTask {
    private String channelId;
    private Long messageId;
    private List<Integer> metaDataIds;
    private boolean replaceMessage;
    private Calendar dateCreated;
    private boolean completed;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public List<Integer> getMetaDataIds() {
        return metaDataIds;
    }

    public void setMetaDataIds(List<Integer> metaDataIds) {
        this.metaDataIds = metaDataIds;
    }

    public boolean isReplaceMessage() {
        return replaceMessage;
    }

    public void setReplaceMessage(boolean replaceMessage) {
        this.replaceMessage = replaceMessage;
    }

    public Calendar getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Calendar dateCreated) {
        this.dateCreated = dateCreated;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
