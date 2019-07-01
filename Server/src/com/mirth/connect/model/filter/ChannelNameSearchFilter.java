/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.filter;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelStatus;
import com.mirth.connect.model.DashboardStatus;

public class ChannelNameSearchFilter extends SearchFilter {

    private List<String> filteredNames;
    private boolean fuzzy;

    public ChannelNameSearchFilter(List<String> filteredNames, boolean fuzzy) {
        this.filteredNames = filteredNames;
        this.fuzzy = fuzzy;
    }

    @Override
    public List<String> getValues() {
        return filteredNames;
    }

    @Override
    public boolean acceptChannelId(String channelId) {
        return true;
    }

    @Override
    public boolean acceptChannelName(String channelName) {
        return accept(channelName);
    }

    @Override
    public boolean acceptChannel(Channel channel) {
        return acceptChannelName(channel.getName());
    }

    @Override
    public boolean acceptDashboardStatus(DashboardStatus status) {
        return accept(status.getName());
    }

    @Override
    public boolean acceptChannelStatus(ChannelStatus status) {
        return accept(status.getChannel().getName());
    }

    private boolean accept(String channelName) {
        for (String filteredName : filteredNames) {
            if (fuzzy) {
                if (!StringUtils.containsIgnoreCase(channelName, filteredName)) {
                    return false;
                }
            } else {
                if (!StringUtils.equals(channelName, filteredName)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toDisplayString() {
        StringBuilder builder = new StringBuilder();
        for (Iterator<String> it = filteredNames.iterator(); it.hasNext();) {
            builder.append(it.next());
            if (it.hasNext()) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }
}
