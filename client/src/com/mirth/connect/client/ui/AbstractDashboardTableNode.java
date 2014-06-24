package com.mirth.connect.client.ui;

import com.mirth.connect.model.DashboardStatus;

public abstract class AbstractDashboardTableNode extends AbstractSortableTreeTableNode {
    public abstract String getChannelId();

    public abstract DashboardStatus getStatus();
    
    public abstract void setStatus(DashboardStatus status);
    
    public abstract void setShowLifetimeStats(boolean showLifetimeStats);
}
