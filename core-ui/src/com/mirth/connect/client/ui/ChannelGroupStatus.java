/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.util.ArrayList;
import java.util.List;

import com.mirth.connect.model.ChannelGroup;
import com.mirth.connect.model.ChannelStatus;

public class ChannelGroupStatus {

    private ChannelGroup group;
    private List<ChannelStatus> channelStatuses;

    public ChannelGroupStatus(ChannelGroup group, List<ChannelStatus> channelStatuses) {
        this.group = group;
        this.channelStatuses = channelStatuses;
    }

    public ChannelGroupStatus(ChannelGroupStatus groupStatus) {
        this.group = new ChannelGroup(groupStatus.getGroup());
        this.channelStatuses = new ArrayList<ChannelStatus>(groupStatus.getChannelStatuses());
    }

    public ChannelGroup getGroup() {
        return group;
    }

    public void setGroup(ChannelGroup group) {
        this.group = group;
    }

    public List<ChannelStatus> getChannelStatuses() {
        return channelStatuses;
    }

    public void setChannelStatuses(List<ChannelStatus> channelStatuses) {
        this.channelStatuses = channelStatuses;
    }
}