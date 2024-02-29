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

import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelStatus;
import com.mirth.connect.model.DashboardStatus;

public abstract class SearchFilter {

    public void filterChannelIds(Iterable<String> channelIds) {
        for (Iterator<String> it = channelIds.iterator(); it.hasNext();) {
            if (!acceptChannelId(it.next())) {
                it.remove();
            }
        }
    }

    public void filterChannelNames(Iterable<String> channelNames) {
        for (Iterator<String> it = channelNames.iterator(); it.hasNext();) {
            if (!acceptChannelName(it.next())) {
                it.remove();
            }
        }
    }

    public void filterChannels(Iterable<Channel> channels) {
        for (Iterator<Channel> it = channels.iterator(); it.hasNext();) {
            if (!acceptChannel(it.next())) {
                it.remove();
            }
        }
    }

    public void filterDashboardStatuses(Iterable<DashboardStatus> statuses) {
        for (Iterator<DashboardStatus> it = statuses.iterator(); it.hasNext();) {
            if (!acceptDashboardStatus(it.next())) {
                it.remove();
            }
        }
    }

    public void filterChannelStatuses(Iterable<ChannelStatus> statuses) {
        for (Iterator<ChannelStatus> it = statuses.iterator(); it.hasNext();) {
            if (!acceptChannelStatus(it.next())) {
                it.remove();
            }
        }
    }

    public abstract List<String> getValues();

    public abstract boolean acceptChannelId(String channelId);

    public abstract boolean acceptChannelName(String channelName);

    public abstract boolean acceptChannel(Channel channel);

    public abstract boolean acceptDashboardStatus(DashboardStatus status);

    public abstract boolean acceptChannelStatus(ChannelStatus status);

    public abstract String toDisplayString();
}