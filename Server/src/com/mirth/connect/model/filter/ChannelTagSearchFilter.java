/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.filter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelStatus;
import com.mirth.connect.model.ChannelTag;
import com.mirth.connect.model.DashboardStatus;

public class ChannelTagSearchFilter extends SearchFilter {

    private Set<String> channelIds = null;
    private List<String> filteredTagNames;
    private List<ChannelTag> channelTags = new ArrayList<ChannelTag>();

    public ChannelTagSearchFilter(Set<ChannelTag> channelTags, List<String> filteredTagNames) {
        this.filteredTagNames = filteredTagNames;

        List<String> lowercaseTagNames = new ArrayList<String>();
        for (String tagName : filteredTagNames) {
            lowercaseTagNames.add(tagName.toLowerCase());
        }

        for (ChannelTag tag : channelTags) {
            if (lowercaseTagNames.contains(tag.getName().toLowerCase())) {
                this.channelTags.add(new ChannelTag(tag));

                if (channelIds == null) {
                    channelIds = new HashSet<String>(tag.getChannelIds());
                } else {
                    for (Iterator<String> it = channelIds.iterator(); it.hasNext();) {
                        if (!tag.getChannelIds().contains(it.next())) {
                            it.remove();
                        }
                    }
                }
            }
        }

        if (channelIds == null) {
            channelIds = new HashSet<String>();
        }
    }

    @Override
    public List<String> getValues() {
        return filteredTagNames;
    }

    @Override
    public boolean acceptChannelId(String channelId) {
        return channelIds.contains(channelId);
    }

    @Override
    public boolean acceptChannelName(String channelName) {
        return true;
    }

    @Override
    public boolean acceptChannel(Channel channel) {
        return channelIds.contains(channel.getId());
    }

    @Override
    public boolean acceptDashboardStatus(DashboardStatus status) {
        return channelIds.contains(status.getChannelId());
    }

    @Override
    public boolean acceptChannelStatus(ChannelStatus status) {
        return channelIds.contains(status.getChannel().getId());
    }

    @Override
    public String toDisplayString() {
        StringBuilder builder = new StringBuilder();
        for (Iterator<ChannelTag> it = channelTags.iterator(); it.hasNext();) {
            builder.append(it.next().getName());
            if (it.hasNext()) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }
}