/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.util.List;
import java.util.Map;

import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.plugins.DashboardColumnPlugin;

public class DashboardTableNode extends AbstractDashboardTableNode {
    private String channelId;
    private Object[] row;
    private DashboardStatus status;
    private boolean showLifetimeStats = false;

    public DashboardTableNode(String channelId, DashboardStatus status) {
        this.channelId = channelId;
        row = new Object[DashboardPanel.getNumberOfDefaultColumns() + LoadedExtensions.getInstance().getDashboardColumnPlugins().size()];
        
        // The children of these nodes should not be sortable. Only the root node will be sortable
        setSortable(false);
        setStatus(status);
    }

    @Override
    public String getChannelId() {
        return channelId;
    }

    @Override
    public DashboardStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(DashboardStatus status) {
        if (!status.getChannelId().equals(channelId)) {
            throw new RuntimeException("Supplied status is for the wrong channel, this node is associated with channel ID " + channelId);
        }

        this.status = status;
        
        int i = 0;

        for (DashboardColumnPlugin plugin : LoadedExtensions.getInstance().getDashboardColumnPlugins().values()) {
            if (plugin.isDisplayFirst()) {
                row[i++] = plugin.getTableData(channelId, status.getMetaDataId());
            }
        }

        int colOffset = i;
        row[i++] = new CellData(null, null);

        i += (DashboardPanel.getNumberOfDefaultColumns() - 1);

        for (DashboardColumnPlugin plugin : LoadedExtensions.getInstance().getDashboardColumnPlugins().values()) {
            if (!plugin.isDisplayFirst()) {
                row[i++] = plugin.getTableData(channelId, status.getMetaDataId());
            }
        }

        DeployedState state = status.getState();

        if (state != null) {
            switch (state) {
                case DEPLOYING:
                    row[colOffset] = new CellData(UIConstants.ICON_BULLET_ORANGE, "Deploying");
                    break;

                case UNDEPLOYING:
                    row[colOffset] = new CellData(UIConstants.ICON_BULLET_ORANGE, "Undeploying");
                    break;

                case STARTING:
                    row[colOffset] = new CellData(UIConstants.ICON_BULLET_ORANGE, "Starting");
                    break;

                case STARTED:
                    if (isStarted(status.getChildStatuses())) {
                        row[colOffset] = new CellData(UIConstants.ICON_BULLET_GREEN, "Started");
                    } else {
                        row[colOffset] = new CellData(UIConstants.ICON_BULLET_ORANGE, "Started");
                    }
                    break;

                case STOPPING:
                    row[colOffset] = new CellData(UIConstants.ICON_BULLET_ORANGE, "Stopping");
                    break;

                case STOPPED:
                    row[colOffset] = new CellData(UIConstants.ICON_BULLET_RED, "Stopped");
                    break;

                case PAUSING:
                    row[colOffset] = new CellData(UIConstants.ICON_BULLET_ORANGE, "Pausing");
                    break;

                case PAUSED:
                    row[colOffset] = new CellData(UIConstants.ICON_BULLET_YELLOW, "Paused");
                    break;

                case UNKNOWN:
                    row[colOffset] = new CellData(UIConstants.ICON_BULLET_BLACK, "Unknown");
                    break;
            }
        }

        row[colOffset + 1] = status.getName();
        row[colOffset + 2] = status.getDeployedRevisionDelta();
        row[colOffset + 3] = status.getDeployedDate();
        
        row[colOffset + 6] = status.getQueued();
        
        setStatistics(colOffset);
    }
    
    private void setStatistics(int colOffset) {
        Map<Status, Long> statistics = showLifetimeStats ? status.getLifetimeStatistics() : status.getStatistics();
        
        if (statistics != null) {
            row[colOffset + 4] = (statistics.get(Status.RECEIVED) == null) ? 0L : statistics.get(Status.RECEIVED);
            row[colOffset + 5] = (statistics.get(Status.FILTERED) == null) ? 0L : statistics.get(Status.FILTERED);
            row[colOffset + 7] = (statistics.get(Status.SENT) == null) ? 0L : statistics.get(Status.SENT);
            row[colOffset + 8] = (statistics.get(Status.ERROR) == null) ? 0L : statistics.get(Status.ERROR);
        } else {
            row[colOffset + 4] = 0L;
            row[colOffset + 5] = 0L;
            row[colOffset + 7] = 0L;
            row[colOffset + 8] = 0L;
        }
    }
    
    public boolean isShowLifetimeStats() {
        return showLifetimeStats;
    }

    @Override
    public void setShowLifetimeStats(boolean showLifetimeStats) {
        this.showLifetimeStats = showLifetimeStats;
        int colOffset = 0;
        
        for (DashboardColumnPlugin plugin : LoadedExtensions.getInstance().getDashboardColumnPlugins().values()) {
            if (plugin.isDisplayFirst()) {
                colOffset++;
            }
        }

        setStatistics(colOffset);
    }

    private boolean isStarted(List<DashboardStatus> statuses) {
        for (DashboardStatus status : statuses) {
            DeployedState state = status.getState();
            
            if (state == null || state != DeployedState.STARTED || !isStarted(status.getChildStatuses())) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public int getColumnCount() {
        return row.length;
    }

    @Override
    public Object getValueAt(int i) {
        return row[i];
    }

    public void setValueAt(int i, Object value) {
        row[i] = value; 
    }
}
