/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("dashboardChannelInfo")
public class DashboardChannelInfo implements Serializable {
    private List<DashboardStatus> dashboardStatuses;
    private Set<String> remainingChannelIds;
    private int deployedChannelCount;

    public DashboardChannelInfo(List<DashboardStatus> dashboardStatuses, Set<String> remainingChannelIds, int deployedChannelCount) {
        this.dashboardStatuses = dashboardStatuses;
        this.remainingChannelIds = remainingChannelIds;
        this.deployedChannelCount = deployedChannelCount;
    }

    public List<DashboardStatus> getDashboardStatuses() {
        return dashboardStatuses;
    }

    public void setDashboardStatuses(List<DashboardStatus> dashboardStatuses) {
        this.dashboardStatuses = dashboardStatuses;
    }

    public Set<String> getRemainingChannelIds() {
        return remainingChannelIds;
    }

    public void setRemainingChannelIds(Set<String> remainingChannelIds) {
        this.remainingChannelIds = remainingChannelIds;
    }

    public int getDeployedChannelCount() {
        return deployedChannelCount;
    }

    public void setDeployedChannelNames(int deployedChannelCount) {
        this.deployedChannelCount = deployedChannelCount;
    }
}
