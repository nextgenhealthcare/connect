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

import com.mirth.connect.model.ServerEvent;
import com.mirth.connect.model.filters.EventFilter;
import com.mirth.connect.util.PaginatedList;

public class PaginatedEventList extends PaginatedList<ServerEvent> {
    private Long itemCount;
    private Client client;
    private EventFilter eventFilter;

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public EventFilter getEventFilter() {
        return eventFilter;
    }

    public void setEventFilter(EventFilter eventFilter) {
        this.eventFilter = eventFilter;
    }

    public void setItemCount(Long itemCount) {
        this.itemCount = itemCount;
    }

    @Override
    public Long getItemCount() {
        return itemCount;
    }

    @Override
    protected List<ServerEvent> getItems(int offset, int limit) throws ClientException {
        return client.getEvents(eventFilter, offset, limit);
    }
}
