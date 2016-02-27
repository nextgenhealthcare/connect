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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdesktop.swingx.treetable.MutableTreeTableNode;

import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.plugins.DashboardColumnPlugin;

public class DashboardTableNode extends AbstractDashboardTableNode {
    private String channelId;
    private Object[] row;
    private ChannelGroupStatus groupStatus;
    private DashboardStatus dashboardStatus;
    private boolean showLifetimeStats = false;
    private Map<String, AbstractDashboardTableNode> channelNodes = new HashMap<String, AbstractDashboardTableNode>();

    public DashboardTableNode(ChannelGroupStatus groupStatus) {
        row = new Object[DashboardPanel.getNumberOfDefaultColumns() + LoadedExtensions.getInstance().getDashboardColumnPlugins().size()];
        setGroupStatus(groupStatus);
    }

    public DashboardTableNode(String channelId, DashboardStatus dashboardStatus) {
        this.channelId = channelId;
        row = new Object[DashboardPanel.getNumberOfDefaultColumns() + LoadedExtensions.getInstance().getDashboardColumnPlugins().size()];

        // The children of these nodes should not be sortable. Only the root node will be sortable
        setSortable(false);
        setDashboardStatus(dashboardStatus);
    }

    @Override
    public boolean isGroupNode() {
        return groupStatus != null;
    }

    @Override
    public String getChannelId() {
        return channelId;
    }

    @Override
    public ChannelGroupStatus getGroupStatus() {
        return groupStatus;
    }

    @Override
    public void setGroupStatus(ChannelGroupStatus groupStatus) {
        this.groupStatus = groupStatus;

        int i = 0;

        for (DashboardColumnPlugin plugin : LoadedExtensions.getInstance().getDashboardColumnPlugins().values()) {
            if (plugin.isDisplayFirst()) {
                row[i++] = plugin.getTableData(groupStatus.getGroup());
            }
        }

        int colOffset = i;
        row[i++] = new CellData(null, null);

        i += (DashboardPanel.getNumberOfDefaultColumns() - 1);

        for (DashboardColumnPlugin plugin : LoadedExtensions.getInstance().getDashboardColumnPlugins().values()) {
            if (!plugin.isDisplayFirst()) {
                row[i++] = plugin.getTableData(groupStatus.getGroup());
            }
        }

        List<DashboardStatus> childDashboardStatuses = getChildDashboardStatuses();
        updateGroupStatusRow(childDashboardStatuses, colOffset);

        row[colOffset + 1] = groupStatus.getGroup().getName();

        Long queued = 0L;
        for (DashboardStatus dashboardStatus : childDashboardStatuses) {
            queued += dashboardStatus.getQueued();
        }
        row[colOffset + 6] = queued;

        setStatistics(childDashboardStatuses, colOffset);
    }

    private void updateGroupStatusRow(List<DashboardStatus> childDashboardStatuses, int colOffset) {
        DeployedState state = null;
        Set<DeployedState> states = new HashSet<DeployedState>();

        for (DashboardStatus dashboardStatus : childDashboardStatuses) {
            states.add(dashboardStatus.getState());
        }

        if (states.size() == 1) {
            state = states.iterator().next();
        } else if (states.size() == 2) {
            if (states.contains(DeployedState.STARTED) && states.contains(DeployedState.STARTING)) {
                state = DeployedState.STARTING;
            } else if (states.contains(DeployedState.STOPPED) && states.contains(DeployedState.STOPPING)) {
                state = DeployedState.STOPPING;
            } else if (states.contains(DeployedState.PAUSED) && states.contains(DeployedState.PAUSING)) {
                state = DeployedState.PAUSING;
            }
        }

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
                    boolean started = true;
                    for (DashboardStatus dashboardStatus : childDashboardStatuses) {
                        if (!isStarted(dashboardStatus.getChildStatuses())) {
                            started = false;
                        }
                    }

                    if (started) {
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

                case SYNCING:
                    row[colOffset] = new CellData(UIConstants.ICON_BULLET_ORANGE, "Syncing");
                    break;

                case UNKNOWN:
                    row[colOffset] = new CellData(UIConstants.ICON_BULLET_BLACK, "Unknown");
                    break;
            }
        } else if (states.isEmpty()) {
            row[colOffset] = new CellData(UIConstants.ICON_BULLET_BLACK, "N/A");
        } else {
            row[colOffset] = new CellData(UIConstants.ICON_BULLET_ORANGE, "Mixed");
        }
    }

    @Override
    public DashboardStatus getDashboardStatus() {
        return dashboardStatus;
    }

    @Override
    public void setDashboardStatus(DashboardStatus status) {
        if (!status.getChannelId().equals(channelId)) {
            throw new RuntimeException("Supplied status is for the wrong channel, this node is associated with channel ID " + channelId);
        }

        this.dashboardStatus = status;

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

                case SYNCING:
                    row[colOffset] = new CellData(UIConstants.ICON_BULLET_ORANGE, "Syncing");
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

        setStatistics(Collections.singletonList(dashboardStatus), colOffset);
    }

    private void setStatistics(List<DashboardStatus> dashboardStatuses, int colOffset) {
        Map<Status, Long> statistics = new HashMap<Status, Long>();
        statistics.put(Status.RECEIVED, 0L);
        statistics.put(Status.FILTERED, 0L);
        statistics.put(Status.SENT, 0L);
        statistics.put(Status.ERROR, 0L);

        for (DashboardStatus dashboardStatus : dashboardStatuses) {
            Map<Status, Long> dashboardStatusStatistics = showLifetimeStats ? dashboardStatus.getLifetimeStatistics() : dashboardStatus.getStatistics();

            statistics.put(Status.RECEIVED, statistics.get(Status.RECEIVED) + ((dashboardStatusStatistics.get(Status.RECEIVED) == null) ? 0L : dashboardStatusStatistics.get(Status.RECEIVED)));
            statistics.put(Status.FILTERED, statistics.get(Status.FILTERED) + ((dashboardStatusStatistics.get(Status.FILTERED) == null) ? 0L : dashboardStatusStatistics.get(Status.FILTERED)));
            statistics.put(Status.SENT, statistics.get(Status.SENT) + ((dashboardStatusStatistics.get(Status.SENT) == null) ? 0L : dashboardStatusStatistics.get(Status.SENT)));
            statistics.put(Status.ERROR, statistics.get(Status.ERROR) + ((dashboardStatusStatistics.get(Status.ERROR) == null) ? 0L : dashboardStatusStatistics.get(Status.ERROR)));
        }

        setStatistics(statistics, colOffset);
    }

    private void setStatistics(Map<Status, Long> statistics, int colOffset) {
        row[colOffset + 4] = statistics.get(Status.RECEIVED);
        row[colOffset + 5] = statistics.get(Status.FILTERED);
        row[colOffset + 7] = statistics.get(Status.SENT);
        row[colOffset + 8] = statistics.get(Status.ERROR);
    }

    public boolean isShowLifetimeStats() {
        return showLifetimeStats;
    }

    @Override
    public void setShowLifetimeStats(boolean showLifetimeStats) {
        this.showLifetimeStats = showLifetimeStats;
        int colOffset = getColumnOffset();

        if (isGroupNode()) {
            updateGroupStatistics(getChildDashboardStatuses(), colOffset);
        } else {
            setStatistics(Collections.singletonList(dashboardStatus), colOffset);
        }
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
    public void insert(MutableTreeTableNode child, int index) {
        AbstractDashboardTableNode channelNode = (AbstractDashboardTableNode) child;
        channelNodes.put(channelNode.getChannelId(), channelNode);
        super.insert(child, index);
    }

    @Override
    public void remove(int index) {
        AbstractDashboardTableNode channelNode = (AbstractDashboardTableNode) getChildAt(index);
        channelNodes.remove(channelNode.getChannelId());
        super.remove(index);
    }

    @Override
    public void remove(MutableTreeTableNode node) {
        AbstractDashboardTableNode channelNode = (AbstractDashboardTableNode) node;
        channelNodes.remove(channelNode.getChannelId());
        super.remove(node);
    }

    @Override
    public AbstractDashboardTableNode getChannelNode(String channelId) {
        return channelNodes.get(channelId);
    }

    @Override
    public void updateGroupNode() {
        List<DashboardStatus> childDashboardStatuses = getChildDashboardStatuses();
        int colOffset = getColumnOffset();

        updateGroupStatusRow(childDashboardStatuses, colOffset);
        updateGroupStatistics(childDashboardStatuses, colOffset);
    }

    private void updateGroupStatistics(List<DashboardStatus> childDashboardStatuses, int colOffset) {
        Long queued = 0L;
        for (DashboardStatus dashboardStatus : childDashboardStatuses) {
            queued += dashboardStatus.getQueued();
        }
        row[colOffset + 6] = queued;

        setStatistics(childDashboardStatuses, colOffset);
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

    @Override
    public String toString() {
        if (isGroupNode()) {
            return groupStatus.getGroup().getName();
        } else {
            return dashboardStatus.getName();
        }
    }

    private int getColumnOffset() {
        int i = 0;
        for (DashboardColumnPlugin plugin : LoadedExtensions.getInstance().getDashboardColumnPlugins().values()) {
            if (plugin.isDisplayFirst()) {
                i++;
            }
        }
        return i;
    }

    private List<DashboardStatus> getChildDashboardStatuses() {
        List<DashboardStatus> childDashboardStatuses = new ArrayList<DashboardStatus>();
        for (Enumeration<? extends MutableTreeTableNode> channelNodes = children(); channelNodes.hasMoreElements();) {
            childDashboardStatuses.add(((AbstractDashboardTableNode) channelNodes.nextElement()).getDashboardStatus());
        }
        return childDashboardStatuses;
    }
}
