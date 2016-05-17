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
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.TreePath;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.jdesktop.swingx.treetable.TreeTableNode;

import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelGroup;
import com.mirth.connect.model.ChannelStatus;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.plugins.DashboardColumnPlugin;

public class DashboardTreeTableModel extends SortableTreeTableModel {

    private DashboardTableNodeFactory nodeFactory;
    private boolean groupModeEnabled = true;
    private MutableTreeTableNode groupRoot;
    private MutableTreeTableNode channelRoot;

    public DashboardTreeTableModel() {
        setSortChildNodes(true);
    }

    public void setNodeFactory(DashboardTableNodeFactory nodeFactory) {
        this.nodeFactory = nodeFactory;
    }

    public boolean isGroupModeEnabled() {
        return groupModeEnabled;
    }

    public MutableTreeTableNode getGroupRoot() {
        if (groupRoot == null) {
            initializeRoot();
        }
        return groupRoot;
    }

    public MutableTreeTableNode getChannelRoot() {
        if (channelRoot == null) {
            initializeRoot();
        }
        return channelRoot;
    }

    public void setGroupModeEnabled(boolean groupModeEnabled) {
        if (groupModeEnabled != this.groupModeEnabled) {
            this.groupModeEnabled = groupModeEnabled;
            if (getRoot() == null) {
                initializeRoot();
            } else {
                setRoot(groupModeEnabled ? groupRoot : channelRoot);
            }
        }
    }

    @Override
    public int getHierarchicalColumn() {
        return 1;
    }

    @Override
    public Class<?> getColumnClass(int column) {
        column -= getColumnOffset();

        // @formatter:off
        switch(column) {
            case 0: return CellData.class; // Status
            case 1: return String.class; // Name
            case 2: return Integer.class; // Revision
            case 3: return Calendar.class; // Last Deployed
            case 4: return Integer.class; // Received
            case 5: return Integer.class; // Filtered
            case 6: return Integer.class; // Queued
            case 7: return Integer.class; // Sent
            case 8: return Integer.class; // Errored
            case 9: return Integer.class; // Connection
            default: return String.class;
        }
        // @formatter:on
    }

    @Override
    public Object getChild(Object parent, int index) {
        if (!isValidTreeTableNode(parent)) {
            throw new IllegalArgumentException("parent must be a TreeTableNode managed by this model");
        }

        return ((TreeTableNode) parent).getChildAt(index);
    }

    @Override
    public int getChildCount(Object parent) {
        if (!isValidTreeTableNode(parent)) {
            throw new IllegalArgumentException("parent must be a TreeTableNode managed by this model");
        }

        return ((TreeTableNode) parent).getChildCount();
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        if (!isValidTreeTableNode(parent) || !isValidTreeTableNode(child)) {
            return -1;
        }

        return ((TreeTableNode) parent).getIndex((TreeTableNode) child);
    }

    @Override
    public Object getValueAt(Object node, int column) {
        if (!isValidTreeTableNode(node)) {
            throw new IllegalArgumentException("node must be a valid node managed by this model");
        }

        if (column < 0 || column >= getColumnCount()) {
            throw new IllegalArgumentException("column must be a valid index");
        }

        TreeTableNode ttn = (TreeTableNode) node;

        if (column >= ttn.getColumnCount()) {
            return null;
        }

        return ttn.getValueAt(column);
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        if (!isValidTreeTableNode(node)) {
            throw new IllegalArgumentException("node must be a valid node managed by this model");
        }

        if (column < 0 || column >= getColumnCount()) {
            throw new IllegalArgumentException("column must be a valid index");
        }

        TreeTableNode ttn = (TreeTableNode) node;

        if (column >= ttn.getColumnCount()) {
            return false;
        }

        return ttn.isEditable(column);
    }

    @Override
    public boolean isLeaf(Object node) {
        if (!isValidTreeTableNode(node)) {
            throw new IllegalArgumentException("node must be a TreeTableNode managed by this model");
        }

        return ((TreeTableNode) node).isLeaf();
    }

    @Override
    public void setValueAt(Object value, Object node, int column) {
        if (!isValidTreeTableNode(node)) {
            throw new IllegalArgumentException("node must be a valid node managed by this model");
        }

        if (column < 0 || column >= getColumnCount()) {
            throw new IllegalArgumentException("column must be a valid index");
        }

        TreeTableNode ttn = (TreeTableNode) node;

        if (column < ttn.getColumnCount()) {
            ttn.setValueAt(value, column);

            modelSupport.firePathChanged(new TreePath(getPathToRoot(ttn)));
        }
    }

    @Override
    public TreeTableNode[] getPathToRoot(TreeTableNode aNode) {
        List<TreeTableNode> path = new ArrayList<TreeTableNode>();
        TreeTableNode node = aNode;

        while (node != groupRoot && node != channelRoot) {
            path.add(0, node);

            node = node.getParent();
        }

        if (node == groupRoot || node == channelRoot) {
            path.add(0, node);
        }

        return path.toArray(new TreeTableNode[0]);
    }

    private boolean isValidTreeTableNode(Object node) {
        boolean result = false;

        if (node instanceof TreeTableNode) {
            TreeTableNode ttn = (TreeTableNode) node;

            while (!result && ttn != null) {
                result = ttn == groupRoot || ttn == channelRoot;

                ttn = ttn.getParent();
            }
        }

        return result;
    }

    public void setShowLifetimeStats(boolean showLifetimeStats) {
        setShowLifetimeStats(showLifetimeStats, groupRoot);
        setShowLifetimeStats(showLifetimeStats, channelRoot);
    }

    private void setShowLifetimeStats(boolean showLifetimeStats, MutableTreeTableNode parent) {
        if (parent == groupRoot) {
            for (Enumeration<? extends MutableTreeTableNode> groupNodes = groupRoot.children(); groupNodes.hasMoreElements();) {
                AbstractDashboardTableNode groupNode = (AbstractDashboardTableNode) groupNodes.nextElement();
                setShowLifetimeStats(showLifetimeStats, groupNode);
                groupNode.setShowLifetimeStats(showLifetimeStats);
            }
        } else {
            for (int i = 0; i < parent.getChildCount(); i++) {
                AbstractDashboardTableNode node = (AbstractDashboardTableNode) parent.getChildAt(i);
                node.setShowLifetimeStats(showLifetimeStats);
                setShowLifetimeStats(showLifetimeStats, node);
            }
        }
    }

    private void initializeRoot() {
        if (getRoot() == null) {
            channelRoot = getNewRootNode();
            groupRoot = getNewRootNode();
            setRoot(groupModeEnabled ? groupRoot : channelRoot);
            addDefaultGroupNode();
        }
    }

    private AbstractDashboardTableNode addDefaultGroupNode() {
        AbstractDashboardTableNode defaultGroupNode = nodeFactory.createNode(new ChannelGroupStatus(ChannelGroup.getDefaultGroup(), new ArrayList<ChannelStatus>()));
        insertNodeInto(defaultGroupNode, groupRoot);
        return defaultGroupNode;
    }

    private MutableTreeTableNode getNewRootNode() {
        return new AbstractSortableTreeTableNode() {
            @Override
            public Object getValueAt(int arg0) {
                return null;
            }

            @Override
            public int getColumnCount() {
                return 0;
            }
        };
    }

    public void setStatuses(List<DashboardStatus> intermediateStatuses) {
        MutableTreeTableNode root = (MutableTreeTableNode) getRoot();

        if (root == null) {
            initializeRoot();
            add(intermediateStatuses, groupRoot);
            add(intermediateStatuses, channelRoot);
        } else {
            updateChannelNodes(new ArrayList<DashboardStatus>(intermediateStatuses), groupRoot);
            updateChannelNodes(new ArrayList<DashboardStatus>(intermediateStatuses), channelRoot);
        }
    }

    public void setGroupStatuses(Collection<ChannelGroupStatus> channelGroupStatuses) {
        Map<String, ChannelGroupStatus> groupStatusMap = new HashMap<String, ChannelGroupStatus>();
        for (ChannelGroupStatus groupStatus : channelGroupStatuses) {
            groupStatusMap.put(groupStatus.getGroup().getId(), groupStatus);
        }

        List<AbstractDashboardTableNode> tableGroupNodes = new ArrayList<AbstractDashboardTableNode>();
        AbstractDashboardTableNode defaultGroupNode = null;
        for (Enumeration<? extends MutableTreeTableNode> groupNodes = groupRoot.children(); groupNodes.hasMoreElements();) {
            AbstractDashboardTableNode groupNode = (AbstractDashboardTableNode) groupNodes.nextElement();
            if (StringUtils.equals(groupNode.getGroupStatus().getGroup().getId(), ChannelGroup.DEFAULT_ID)) {
                defaultGroupNode = groupNode;
            }
            tableGroupNodes.add(groupNode);
        }

        List<AbstractDashboardTableNode> groupNodesToRemove = new ArrayList<AbstractDashboardTableNode>();

        for (AbstractDashboardTableNode groupNode : tableGroupNodes) {
            ChannelGroup group = groupNode.getGroupStatus().getGroup();

            ChannelGroupStatus matchingGroupStatus = groupStatusMap.remove(group.getId());

            if (matchingGroupStatus != null) {
                ChannelGroup matchingGroup = matchingGroupStatus.getGroup();

                Set<String> channelIds = new HashSet<String>();
                for (Channel channel : matchingGroup.getChannels()) {
                    channelIds.add(channel.getId());
                }

                for (Channel channel : group.getChannels()) {
                    if (!channelIds.remove(channel.getId())) {
                        // Remove from the current group
                        AbstractDashboardTableNode channelNode = groupNode.getChannelNode(channel.getId());
                        if (channelNode != null) {
                            removeNodeFromParent(channelNode);

                            if (defaultGroupNode == null) {
                                defaultGroupNode = addDefaultGroupNode();
                            }
                            insertNodeInto(channelNode, defaultGroupNode);
                        }
                    }
                }

                for (String channelId : channelIds) {
                    // Move channel into the current group
                    AbstractDashboardTableNode channelNode = findChannelNode(channelId);
                    if (channelNode != null) {
                        removeNodeFromParent(channelNode);
                        insertNodeInto(channelNode, groupNode);
                    }
                }

                groupNode.setGroupStatus(matchingGroupStatus);
            } else {
                for (Channel channel : group.getChannels()) {
                    // Remove from the current group
                    AbstractDashboardTableNode channelNode = groupNode.getChannelNode(channel.getId());
                    if (channelNode != null) {
                        removeNodeFromParent(channelNode);
                        if (defaultGroupNode == null) {
                            defaultGroupNode = addDefaultGroupNode();
                        }
                        insertNodeInto(channelNode, defaultGroupNode);
                    }
                }

                // Remove the current group
                groupNodesToRemove.add(groupNode);
            }
        }

        for (ChannelGroupStatus groupStatus : groupStatusMap.values()) {
            // Add a new group node
            AbstractDashboardTableNode groupNode = nodeFactory.createNode(groupStatus);
            insertNodeInto(groupNode, groupRoot);

            for (Channel channel : groupStatus.getGroup().getChannels()) {
                // Move channel into the group
                if (defaultGroupNode == null) {
                    defaultGroupNode = addDefaultGroupNode();
                }
                AbstractDashboardTableNode channelNode = defaultGroupNode.getChannelNode(channel.getId());
                if (channelNode != null) {
                    removeNodeFromParent(channelNode);
                    insertNodeInto(channelNode, groupNode);
                }
            }

            groupNode.updateGroupNode();
        }

        for (AbstractDashboardTableNode groupNode : groupNodesToRemove) {
            removeNodeFromParent(groupNode);
        }
    }

    private AbstractDashboardTableNode findChannelNode(String channelId) {
        for (Enumeration<? extends MutableTreeTableNode> groupNodes = groupRoot.children(); groupNodes.hasMoreElements();) {
            AbstractDashboardTableNode channelNode = ((AbstractDashboardTableNode) groupNodes.nextElement()).getChannelNode(channelId);
            if (channelNode != null) {
                return channelNode;
            }
        }
        return null;
    }

    public void refresh() {
        MutableTreeTableNode root = (MutableTreeTableNode) getRoot();

        if (root != null) {
            modelSupport.firePathChanged(new TreePath(root));
        }
    }

    /**
     * Should be called at the end with the final list of dashboard statuses.
     * 
     * @param finishedStatuses
     */
    public void finishStatuses(List<DashboardStatus> finishedStatuses) {
        initializeRoot();
        removeLingeringChannelNodes(finishedStatuses, groupRoot);
        removeLingeringChannelNodes(finishedStatuses, channelRoot);
    }

    private int getColumnOffset() {
        int counter = 0;

        for (DashboardColumnPlugin plugin : LoadedExtensions.getInstance().getDashboardColumnPlugins().values()) {
            if (plugin.isDisplayFirst()) {
                counter++;
            }
        }
        return counter;
    }

    private void add(Collection<DashboardStatus> statuses, MutableTreeTableNode parent) {
        if (parent == groupRoot) {
            Map<String, AbstractDashboardTableNode> groupNodeMap = new HashMap<String, AbstractDashboardTableNode>();
            for (Enumeration<? extends MutableTreeTableNode> groupNodes = groupRoot.children(); groupNodes.hasMoreElements();) {
                AbstractDashboardTableNode groupNode = (AbstractDashboardTableNode) groupNodes.nextElement();

                for (Channel channel : groupNode.getGroupStatus().getGroup().getChannels()) {
                    groupNodeMap.put(channel.getId(), groupNode);
                }
            }

            AbstractDashboardTableNode defaultGroupNode = null;
            for (Enumeration<? extends MutableTreeTableNode> groupNodes = groupRoot.children(); groupNodes.hasMoreElements();) {
                AbstractDashboardTableNode groupNode = (AbstractDashboardTableNode) groupNodes.nextElement();
                if (StringUtils.equals(groupNode.getGroupStatus().getGroup().getId(), ChannelGroup.DEFAULT_ID)) {
                    defaultGroupNode = groupNode;
                    break;
                }
            }

            for (DashboardStatus status : statuses) {
                AbstractDashboardTableNode groupNode = groupNodeMap.get(status.getChannelId());
                if (groupNode != null) {
                    add(Collections.singleton(status), groupNode);
                    groupNode.updateGroupNode();
                } else {
                    if (defaultGroupNode == null) {
                        defaultGroupNode = addDefaultGroupNode();
                    }
                    add(Collections.singleton(status), defaultGroupNode);
                    defaultGroupNode.updateGroupNode();
                }
            }
        } else {
            int index = parent.getChildCount();

            // add each status as a new child node of parent
            for (DashboardStatus status : statuses) {
                MutableTreeTableNode newNode = nodeFactory.createNode(status.getChannelId(), status);
                insertNodeInto(newNode, parent, index++);

                // recursively add the children of this status entry as child nodes of the newly created node
                add(status.getChildStatuses(), newNode);
            }
        }
    }

    private void updateChannelNodes(List<DashboardStatus> statuses, MutableTreeTableNode root) {
        // index the status entries by status key so that they can be retrieved quickly as we traverse the tree
        Map<String, DashboardStatus> statusMap = getStatusMap(statuses);

        if (root == groupRoot) {
            for (Enumeration<? extends MutableTreeTableNode> groupNodes = groupRoot.children(); groupNodes.hasMoreElements();) {
                AbstractDashboardTableNode groupNode = (AbstractDashboardTableNode) groupNodes.nextElement();
                updateChannelNodes(statusMap, statuses, groupNode);
                groupNode.updateGroupNode();
            }
        } else {
            updateChannelNodes(statusMap, statuses, root);
        }

        // any remaining entries in the status list are new status entries that need to be added as nodes to the root node
        add(statuses, root);
    }

    private void updateChannelNodes(Map<String, DashboardStatus> statusMap, List<DashboardStatus> statuses, MutableTreeTableNode parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            AbstractDashboardTableNode node = (AbstractDashboardTableNode) parent.getChildAt(i);

            if (statusMap.containsKey(node.getDashboardStatus().getKey())) {
                // Remove channels that do exist from the list of statuses so they will not be added again later.
                DashboardStatus status = statusMap.get(node.getDashboardStatus().getKey());
                statuses.remove(status);
                // Update the channel status
                node.setDashboardStatus(status);
                modelSupport.firePathChanged(new TreePath(getPathToRoot(node)));

                // Update the connector level statuses
                updateConnector(statusMap, node);
            }
        }
    }

    private void removeLingeringChannelNodes(List<DashboardStatus> statuses, MutableTreeTableNode root) {
        // index the status entries by status key so that they can be retrieved quickly as we traverse the tree
        Map<String, DashboardStatus> statusMap = getStatusMap(statuses);

        if (root == groupRoot) {
            for (Enumeration<? extends MutableTreeTableNode> groupNodes = groupRoot.children(); groupNodes.hasMoreElements();) {
                AbstractDashboardTableNode groupNode = (AbstractDashboardTableNode) groupNodes.nextElement();
                removeLingeringChannelNodes(statusMap, statuses, groupNode);
                groupNode.updateGroupNode();
            }
        } else {
            removeLingeringChannelNodes(statusMap, statuses, root);
        }
    }

    private void removeLingeringChannelNodes(Map<String, DashboardStatus> statusMap, List<DashboardStatus> statuses, MutableTreeTableNode root) {
        for (int i = 0; i < root.getChildCount(); i++) {
            AbstractDashboardTableNode node = (AbstractDashboardTableNode) root.getChildAt(i);

            if (!statusMap.containsKey(node.getDashboardStatus().getKey())) {
                // Remove channels that no longer exist
                removeNodeFromParent(node);
                i--;
            }
        }
    }

    private void updateConnector(Map<String, DashboardStatus> statusMap, AbstractDashboardTableNode channelNode) {
        Map<String, AbstractDashboardTableNode> nodeMap = new HashMap<String, AbstractDashboardTableNode>();

        DashboardStatus channelStatus = statusMap.get(channelNode.getDashboardStatus().getKey());

        // Remove connectors that no longer exist
        for (int i = 0; i < channelNode.getChildCount(); i++) {
            AbstractDashboardTableNode node = (AbstractDashboardTableNode) channelNode.getChildAt(i);

            if (!statusMap.containsKey(node.getDashboardStatus().getKey())) {
                removeNodeFromParent(node);
                i--;
            } else {
                nodeMap.put(node.getDashboardStatus().getKey(), node);
            }
        }

        // At this point, the only connectors remaining in the table should have an update.
        // Iterate across all the new statuses
        for (int i = 0; i < channelStatus.getChildStatuses().size(); i++) {
            // The new connector status at the current pointer
            DashboardStatus newConnectorStatus = channelStatus.getChildStatuses().get(i);
            // The node containing the old status
            AbstractDashboardTableNode oldConnectorNode = null;

            if (i < channelNode.getChildCount()) {
                oldConnectorNode = (AbstractDashboardTableNode) channelNode.getChildAt(i);
            }

            // If the new connector key is equal to the old one, then update the node's status
            if (oldConnectorNode != null && newConnectorStatus.getKey().equals(oldConnectorNode.getDashboardStatus().getKey())) {
                oldConnectorNode.setDashboardStatus(newConnectorStatus);
                modelSupport.firePathChanged(new TreePath(getPathToRoot(oldConnectorNode)));
            } else {
                AbstractDashboardTableNode node;

                if (nodeMap.containsKey(newConnectorStatus.getKey())) {
                    // If the key is already in the table, remove it because it is out of order.
                    node = nodeMap.get(newConnectorStatus.getKey());
                    removeNodeFromParent(node);

                    // Update the node's status before it gets readded.
                    node.setDashboardStatus(newConnectorStatus);
                } else {
                    // If the key is not in the table yet, create a node for it.
                    node = nodeFactory.createNode(newConnectorStatus.getChannelId(), newConnectorStatus);
                }

                // Insert the node
                insertNodeInto(node, channelNode, i);
            }
        }
    }

    private Map<String, DashboardStatus> getStatusMap(List<DashboardStatus> statuses) {
        Map<String, DashboardStatus> statusMap = new LinkedHashMap<String, DashboardStatus>();

        for (DashboardStatus status : statuses) {
            statusMap.put(status.getKey(), status);
            for (DashboardStatus child : status.getChildStatuses()) {
                statusMap.put(child.getKey(), status);
            }
        }

        return statusMap;
    }
}
