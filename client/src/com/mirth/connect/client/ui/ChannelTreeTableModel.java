/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import static com.mirth.connect.client.ui.ChannelPanel.DATA_TYPE_COLUMN_NUMBER;
import static com.mirth.connect.client.ui.ChannelPanel.DEPLOYED_REVISION_DELTA_COLUMN_NUMBER;
import static com.mirth.connect.client.ui.ChannelPanel.DESCRIPTION_COLUMN_NUMBER;
import static com.mirth.connect.client.ui.ChannelPanel.ID_COLUMN_NUMBER;
import static com.mirth.connect.client.ui.ChannelPanel.LAST_DEPLOYED_COLUMN_NUMBER;
import static com.mirth.connect.client.ui.ChannelPanel.LAST_MODIFIED_COLUMN_NUMBER;
import static com.mirth.connect.client.ui.ChannelPanel.LOCAL_CHANNEL_ID_COLUMN_NUMBER;
import static com.mirth.connect.client.ui.ChannelPanel.NAME_COLUMN_NUMBER;
import static com.mirth.connect.client.ui.ChannelPanel.STATUS_COLUMN_NUMBER;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.tree.TreePath;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;

import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelGroup;
import com.mirth.connect.model.ChannelStatus;
import com.mirth.connect.plugins.ChannelColumnPlugin;

public class ChannelTreeTableModel extends SortableTreeTableModel {

    private ChannelTableNodeFactory nodeFactory;
    private boolean groupModeEnabled = true;
    private AtomicBoolean updating = new AtomicBoolean(false);

    public ChannelTreeTableModel() {
        setSortChildNodes(true);
    }

    public void setNodeFactory(ChannelTableNodeFactory nodeFactory) {
        this.nodeFactory = nodeFactory;
    }

    public boolean isGroupModeEnabled() {
        return groupModeEnabled;
    }

    public void setGroupModeEnabled(boolean groupModeEnabled) {
        this.groupModeEnabled = groupModeEnabled;
    }

    @Override
    public int getHierarchicalColumn() {
        return NAME_COLUMN_NUMBER;
    }

    @Override
    public Class<?> getColumnClass(int column) {
        column -= getColumnOffset();

        // @formatter:off
        switch (column) {
            case STATUS_COLUMN_NUMBER: return CellData.class;
            case DATA_TYPE_COLUMN_NUMBER: return String.class;
            case NAME_COLUMN_NUMBER: return ChannelTableNameEntry.class;
            case ID_COLUMN_NUMBER: return String.class;
            case LOCAL_CHANNEL_ID_COLUMN_NUMBER: return Long.class;
            case DESCRIPTION_COLUMN_NUMBER: return String.class;
            case DEPLOYED_REVISION_DELTA_COLUMN_NUMBER: return Integer.class;
            case LAST_DEPLOYED_COLUMN_NUMBER: return Calendar.class;
            case LAST_MODIFIED_COLUMN_NUMBER: return Calendar.class;
            default: return String.class;
        }
        // @formatter:on
    }

    public void update(List<ChannelGroupStatus> groupStatuses) {
        if (updating.getAndSet(true)) {
            return;
        }

        try {
            MutableTreeTableNode root = new AbstractSortableTreeTableNode() {
                @Override
                public Object getValueAt(int arg0) {
                    return null;
                }

                @Override
                public int getColumnCount() {
                    return 0;
                }
            };
            setRoot(root);

            int rootIndex = 0;

            if (groupModeEnabled) {
                for (ChannelGroupStatus groupStatus : groupStatuses) {
                    MutableTreeTableNode groupNode = nodeFactory.createNode(groupStatus);
                    insertNodeInto(groupNode, root, rootIndex++);

                    int groupIndex = 0;

                    for (ChannelStatus channelStatus : groupStatus.getChannelStatuses()) {
                        MutableTreeTableNode channelNode = nodeFactory.createNode(channelStatus);
                        insertNodeInto(channelNode, groupNode, groupIndex++);
                    }
                }
            } else {
                for (ChannelGroupStatus groupStatus : groupStatuses) {
                    for (ChannelStatus channelStatus : groupStatus.getChannelStatuses()) {
                        MutableTreeTableNode channelNode = nodeFactory.createNode(channelStatus);
                        insertNodeInto(channelNode, root, rootIndex++);
                    }
                }
            }
        } finally {
            updating.set(false);
        }
    }

    public void addChannelToGroup(AbstractChannelTableNode targetGroupNode, String channelId) {
        if (updating.getAndSet(true)) {
            return;
        }

        try {
            if (!groupModeEnabled || !targetGroupNode.isGroupNode()) {
                return;
            }

            for (Enumeration<? extends MutableTreeTableNode> channelNodes = targetGroupNode.children(); channelNodes.hasMoreElements();) {
                AbstractChannelTableNode channelNode = (AbstractChannelTableNode) channelNodes.nextElement();

                // Don't do anything if the channel is already in this group
                if (StringUtils.equals(channelNode.getChannelStatus().getChannel().getId(), channelId)) {
                    return;
                }
            }

            MutableTreeTableNode root = (MutableTreeTableNode) getRoot();
            if (root == null) {
                return;
            }

            AbstractChannelTableNode previousGroupNode = null;

            for (Enumeration<? extends MutableTreeTableNode> groupNodes = root.children(); groupNodes.hasMoreElements();) {
                AbstractChannelTableNode groupNode = (AbstractChannelTableNode) groupNodes.nextElement();

                if (!StringUtils.equals(targetGroupNode.getGroupStatus().getGroup().getId(), groupNode.getGroupStatus().getGroup().getId())) {
                    for (int channelNodeIndex = 0; channelNodeIndex < groupNode.getChildCount(); channelNodeIndex++) {
                        AbstractChannelTableNode channelNode = (AbstractChannelTableNode) groupNode.getChildAt(channelNodeIndex);

                        if (StringUtils.equals(channelNode.getChannelStatus().getChannel().getId(), channelId)) {
                            previousGroupNode = groupNode;
                            removeNodeFromParent(channelNode);
                            insertNodeInto(channelNode, targetGroupNode);
                            break;
                        }
                    }
                }

                if (previousGroupNode != null) {
                    break;
                }
            }

            ChannelStatus targetChannelStatus = null;

            // Remove channel status from previous group status
            for (Iterator<ChannelStatus> it = previousGroupNode.getGroupStatus().getChannelStatuses().iterator(); it.hasNext();) {
                ChannelStatus channelStatus = it.next();
                if (StringUtils.equals(channelStatus.getChannel().getId(), channelId)) {
                    targetChannelStatus = channelStatus;
                    it.remove();
                }
            }

            // Remove channel from previous group 
            for (Iterator<Channel> it = previousGroupNode.getGroupStatus().getGroup().getChannels().iterator(); it.hasNext();) {
                Channel channel = it.next();
                if (StringUtils.equals(channel.getId(), channelId)) {
                    it.remove();
                }
            }

            // Add channel status to target group status
            targetGroupNode.getGroupStatus().getChannelStatuses().add(targetChannelStatus);
            // Add channel to tager group
            targetGroupNode.getGroupStatus().getGroup().getChannels().add(new Channel(channelId));

            previousGroupNode.updateStatusColumn();
            targetGroupNode.updateStatusColumn();
        } finally {
            updating.set(false);
        }
    }

    public void removeChannelFromGroup(AbstractChannelTableNode targetGroupNode, String channelId) {
        if (updating.getAndSet(true)) {
            return;
        }

        try {
            if (!groupModeEnabled || StringUtils.equals(targetGroupNode.getGroupStatus().getGroup().getId(), ChannelGroup.DEFAULT_ID) || !targetGroupNode.isGroupNode()) {
                return;
            }

            AbstractChannelTableNode targetChannelNode = null;
            for (Enumeration<? extends MutableTreeTableNode> channelNodes = targetGroupNode.children(); channelNodes.hasMoreElements();) {
                AbstractChannelTableNode channelNode = (AbstractChannelTableNode) channelNodes.nextElement();

                if (StringUtils.equals(channelNode.getChannelStatus().getChannel().getId(), channelId)) {
                    targetChannelNode = channelNode;
                    break;
                }
            }

            // Don't do anything if the channel isn't in this group
            if (targetChannelNode == null) {
                return;
            }

            MutableTreeTableNode root = (MutableTreeTableNode) getRoot();
            if (root == null) {
                return;
            }

            AbstractChannelTableNode defaultGroupNode = null;
            for (Enumeration<? extends MutableTreeTableNode> groupNodes = root.children(); groupNodes.hasMoreElements();) {
                AbstractChannelTableNode groupNode = (AbstractChannelTableNode) groupNodes.nextElement();

                if (!StringUtils.equals(targetGroupNode.getGroupStatus().getGroup().getId(), ChannelGroup.DEFAULT_ID)) {
                    defaultGroupNode = groupNode;
                    break;
                }
            }

            if (defaultGroupNode == null) {
                return;
            }

            removeNodeFromParent(targetChannelNode);
            insertNodeInto(targetChannelNode, defaultGroupNode);
            ChannelStatus targetChannelStatus = null;

            // Remove channel status from target group status
            for (Iterator<ChannelStatus> it = targetGroupNode.getGroupStatus().getChannelStatuses().iterator(); it.hasNext();) {
                ChannelStatus channelStatus = it.next();
                if (StringUtils.equals(channelStatus.getChannel().getId(), channelId)) {
                    targetChannelStatus = channelStatus;
                    it.remove();
                }
            }

            // Remove channel from target group 
            for (Iterator<Channel> it = targetGroupNode.getGroupStatus().getGroup().getChannels().iterator(); it.hasNext();) {
                Channel channel = it.next();
                if (StringUtils.equals(channel.getId(), channelId)) {
                    it.remove();
                }
            }

            // Add channel status to default group status
            defaultGroupNode.getGroupStatus().getChannelStatuses().add(targetChannelStatus);
            // Add channel to default group
            defaultGroupNode.getGroupStatus().getGroup().getChannels().add(new Channel(channelId));

            targetGroupNode.updateStatusColumn();
            defaultGroupNode.updateStatusColumn();
        } finally {
            updating.set(false);
        }
    }

    public AbstractChannelTableNode addNewGroup(ChannelGroup group) {
        if (updating.getAndSet(true)) {
            return null;
        }

        try {
            MutableTreeTableNode root = (MutableTreeTableNode) getRoot();
            if (root == null) {
                return null;
            }

            ChannelGroupStatus groupStatus = new ChannelGroupStatus(group, new ArrayList<ChannelStatus>());
            AbstractChannelTableNode groupNode = nodeFactory.createNode(groupStatus);
            insertNodeInto(groupNode, root);
            return groupNode;
        } finally {
            updating.set(false);
        }
    }

    public void removeGroup(AbstractChannelTableNode groupNode) {
        if (updating.getAndSet(true)) {
            return;
        }

        try {
            MutableTreeTableNode root = (MutableTreeTableNode) getRoot();
            if (root == null) {
                return;
            }

            removeNodeFromParent(groupNode);
        } finally {
            updating.set(false);
        }
    }

    public void refresh() {
        MutableTreeTableNode root = (MutableTreeTableNode) getRoot();

        if (root != null) {
            modelSupport.firePathChanged(new TreePath(root));
        }
    }

    private int getColumnOffset() {
        int counter = 0;

        for (ChannelColumnPlugin plugin : LoadedExtensions.getInstance().getChannelColumnPlugins().values()) {
            if (plugin.isDisplayFirst()) {
                counter++;
            }
        }
        return counter;
    }
}