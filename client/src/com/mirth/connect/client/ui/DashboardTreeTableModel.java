/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.TreePath;

import org.jdesktop.swingx.treetable.MutableTreeTableNode;

import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.plugins.DashboardColumnPlugin;

public class DashboardTreeTableModel extends SortableTreeTableModel {
    private DashboardTableNodeFactory nodeFactory;

    public void setNodeFactory(DashboardTableNodeFactory nodeFactory) {
        this.nodeFactory = nodeFactory;
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

    public void setShowLifetimeStats(boolean showLifetimeStats) {
        setShowLifetimeStats(showLifetimeStats, (MutableTreeTableNode) getRoot());
    }

    private void setShowLifetimeStats(boolean showLifetimeStats, MutableTreeTableNode parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            AbstractDashboardTableNode node = (AbstractDashboardTableNode) parent.getChildAt(i);
            node.setShowLifetimeStats(showLifetimeStats);
            setShowLifetimeStats(showLifetimeStats, node);
        }
    }

    private void initializeRoot() {
        MutableTreeTableNode root = (MutableTreeTableNode) getRoot();

        if (root == null) {
            root = new AbstractSortableTreeTableNode() {
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
        }
    }

    public void setStatuses(List<DashboardStatus> intermediateStatuses) {
        MutableTreeTableNode root = (MutableTreeTableNode) getRoot();

        if (root == null) {
            initializeRoot();
            add(intermediateStatuses, (MutableTreeTableNode) getRoot());
        } else {
            updateChannelNodes(intermediateStatuses, root);
        }
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
        removeLingeringChannelNodes(finishedStatuses, (MutableTreeTableNode) getRoot());
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
        int index = parent.getChildCount();

        // add each status as a new child node of parent
        for (DashboardStatus status : statuses) {
            MutableTreeTableNode newNode = nodeFactory.createNode(status.getChannelId(), status);
            insertNodeInto(newNode, parent, index++);

            // recursively add the children of this status entry as child nodes of the newly created node
            add(status.getChildStatuses(), newNode);
        }
    }

    private void updateChannelNodes(List<DashboardStatus> statuses, MutableTreeTableNode root) {
        // index the status entries by status key so that they can be retrieved quickly as we traverse the tree
        Map<String, DashboardStatus> statusMap = new LinkedHashMap<String, DashboardStatus>();

        for (DashboardStatus status : statuses) {
            statusMap.put(status.getKey(), status);
            for (DashboardStatus child : status.getChildStatuses()) {
                statusMap.put(child.getKey(), status);
            }
        }

        for (int i = 0; i < root.getChildCount(); i++) {
            AbstractDashboardTableNode node = (AbstractDashboardTableNode) root.getChildAt(i);

            if (statusMap.containsKey(node.getStatus().getKey())) {
                // Remove channels that do exist from the list of statuses so they will not be added again later.
                DashboardStatus status = statusMap.get(node.getStatus().getKey());
                statuses.remove(status);
                // Update the channel status
                node.setStatus(status);
                modelSupport.firePathChanged(new TreePath(getPathToRoot(node)));

                // Update the connector level statuses
                updateConnector(statusMap, node);
            }
        }

        // any remaining entries in the status list are new status entries that need to be added as nodes to the root node
        add(statuses, root);
    }

    private void removeLingeringChannelNodes(List<DashboardStatus> statuses, MutableTreeTableNode root) {
        // index the status entries by status key so that they can be retrieved quickly as we traverse the tree
        Map<String, DashboardStatus> statusMap = new LinkedHashMap<String, DashboardStatus>();

        for (DashboardStatus status : statuses) {
            statusMap.put(status.getKey(), status);
            for (DashboardStatus child : status.getChildStatuses()) {
                statusMap.put(child.getKey(), status);
            }
        }

        for (int i = 0; i < root.getChildCount(); i++) {
            AbstractDashboardTableNode node = (AbstractDashboardTableNode) root.getChildAt(i);

            if (!statusMap.containsKey(node.getStatus().getKey())) {
                // Remove channels that no longer exist
                removeNodeFromParent(node);
                i--;
            }
        }
    }

    private void updateConnector(Map<String, DashboardStatus> statusMap, AbstractDashboardTableNode channelNode) {
        Map<String, AbstractDashboardTableNode> nodeMap = new HashMap<String, AbstractDashboardTableNode>();

        DashboardStatus channelStatus = statusMap.get(channelNode.getStatus().getKey());

        // Remove connectors that no longer exist
        for (int i = 0; i < channelNode.getChildCount(); i++) {
            AbstractDashboardTableNode node = (AbstractDashboardTableNode) channelNode.getChildAt(i);

            if (!statusMap.containsKey(node.getStatus().getKey())) {
                removeNodeFromParent(node);
                i--;
            } else {
                nodeMap.put(node.getStatus().getKey(), node);
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
            if (oldConnectorNode != null && newConnectorStatus.getKey().equals(oldConnectorNode.getStatus().getKey())) {
                oldConnectorNode.setStatus(newConnectorStatus);
                modelSupport.firePathChanged(new TreePath(getPathToRoot(oldConnectorNode)));
            } else {
                AbstractDashboardTableNode node;

                if (nodeMap.containsKey(newConnectorStatus.getKey())) {
                    // If the key is already in the table, remove it because it is out of order.
                    node = nodeMap.get(newConnectorStatus.getKey());
                    removeNodeFromParent(node);

                    // Update the node's status before it gets readded.
                    node.setStatus(newConnectorStatus);
                } else {
                    // If the key is not in the table yet, create a node for it.
                    node = nodeFactory.createNode(newConnectorStatus.getChannelId(), newConnectorStatus);
                }

                // Insert the node
                insertNodeInto(node, channelNode, i);
            }
        }
    }
}
