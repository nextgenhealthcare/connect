/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import com.mirth.connect.model.DashboardStatus;

public abstract class AbstractDashboardTableNode extends AbstractSortableTreeTableNode {
    public abstract String getChannelId();

    public abstract boolean isGroupNode();

    public abstract ChannelGroupStatus getGroupStatus();

    public abstract void setGroupStatus(ChannelGroupStatus groupStatus);

    public abstract DashboardStatus getDashboardStatus();

    public abstract void setDashboardStatus(DashboardStatus dashboardStatus);

    public abstract void setShowLifetimeStats(boolean showLifetimeStats);

    public abstract AbstractDashboardTableNode getChannelNode(String channelId);

    public abstract void updateGroupNode();
}
