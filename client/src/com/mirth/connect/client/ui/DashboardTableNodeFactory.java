package com.mirth.connect.client.ui;

import com.mirth.connect.model.DashboardStatus;

public interface DashboardTableNodeFactory {
    public AbstractDashboardTableNode createNode(String channelId, DashboardStatus status);
}
