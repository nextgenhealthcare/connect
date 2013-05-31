/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.core;

import java.util.List;

import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.model.filters.MessageFilter;
import com.mirth.connect.util.PaginatedList;

public class PaginatedMessageList extends PaginatedList<Message> {
    private Long itemCount;
    private Client client;
    private MessageFilter messageFilter;
    private String channelId;
    private boolean includeContent;

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public MessageFilter getMessageFilter() {
        return messageFilter;
    }

    public void setMessageFilter(MessageFilter messageFilter) {
        this.messageFilter = messageFilter;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public boolean isIncludeContent() {
        return includeContent;
    }

    public void setIncludeContent(boolean includeContent) {
        this.includeContent = includeContent;
    }

    public void setItemCount(Long itemCount) {
        this.itemCount = itemCount;
    }

    @Override
    public Long getItemCount() {
        return itemCount;
    }

    @Override
    protected List<Message> getItems(int offset, int limit) throws ClientException {
        return client.getMessages(channelId, messageFilter, includeContent, offset, limit);
    }
}
