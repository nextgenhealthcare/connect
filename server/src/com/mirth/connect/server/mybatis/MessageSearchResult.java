/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.mybatis;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.donkey.model.message.Message;

public class MessageSearchResult {
    private Long messageId;
    private String serverId;
    private Calendar receivedDate;
    private boolean processed;
    private Long originalId;
    private Long importId;
    private String importChannelId;
    private String metaDataIds;
    private int metaDataId;
    private Set<Integer> metaDataIdSet;

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public Calendar getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Calendar receivedDate) {
        this.receivedDate = receivedDate;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public Long getOriginalId() {
        return originalId;
    }

    public void setOriginalId(Long originalId) {
        this.originalId = originalId;
    }

    public Long getImportId() {
        return importId;
    }

    public void setImportId(Long importId) {
        this.importId = importId;
    }

    public String getImportChannelId() {
        return importChannelId;
    }

    public void setImportChannelId(String importChannelId) {
        this.importChannelId = importChannelId;
    }

    public String getMetaDataIds() {
        return metaDataIds;
    }

    public void setMetaDataIds(String metaDataIds) {
        this.metaDataIds = metaDataIds;
        metaDataIdSet = new HashSet<Integer>();

        for (String piece : StringUtils.split(metaDataIds, ',')) {
            metaDataIdSet.add(Integer.parseInt(piece));
        }
    }

    public int getMetaDataId() {
        return metaDataId;
    }

    public void setMetaDataId(int metaDataId) {
        this.metaDataId = metaDataId;
    }

    public Message getMessage() {
        Message message = new Message();
        message.setMessageId(messageId);
        message.setReceivedDate(receivedDate);
        message.setServerId(serverId);
        message.setProcessed(processed);
        message.setOriginalId(originalId);
        message.setImportId(importId);
        message.setImportChannelId(importChannelId);

        return message;
    }

    public Set<Integer> getMetaDataIdSet() {
        return metaDataIdSet;
    }

    public void setMetaDataIdSet(Set<Integer> metaDataIdSet) {
        this.metaDataIdSet = metaDataIdSet;
    }
}
