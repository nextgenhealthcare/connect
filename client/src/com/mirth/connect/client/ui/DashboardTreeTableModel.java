/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.plugins.DashboardColumnPlugin;

public class DashboardTreeTableModel extends SortableTreeTableModel {
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
            case 9: return String.class; // Alerted
            case 10: return Integer.class; // Connection
            default: return String.class;
        }
        // @formatter:on
    }
    
    public void setShowOverallStats(boolean showOverallStats) {
        setShowOverallStats(showOverallStats, (MutableTreeTableNode) getRoot());
    }
    
    private void setShowOverallStats(boolean showOverallStats, MutableTreeTableNode parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            DashboardTableNode node = (DashboardTableNode) parent.getChildAt(i);
            node.setShowOverallStats(showOverallStats);
            setShowOverallStats(showOverallStats, node);
        }
    }
    
    public void setStatuses(List<DashboardStatus> statuses) {
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
            add(statuses, root);
        } else {
            update(statuses, root);
        }
    }
    
    private int getColumnOffset(){
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
            MutableTreeTableNode newNode = new DashboardTableNode(status.getChannelId(), status);
            insertNodeInto(newNode, parent, index++);

            // recursively add the children of this status entry as child nodes of the newly created node
            add(status.getChildStatuses(), newNode);
        }
    }

    private void update(List<DashboardStatus> statuses, MutableTreeTableNode parent) {
        // index the status entries by status key so that they can be retrieved quickly as we traverse the tree
        Map<String, DashboardStatus> statusMap = new LinkedHashMap<String, DashboardStatus>();

        for (DashboardStatus status : statuses) {
            statusMap.put(status.getKey(), status);
        }

        for (int i = 0; i < parent.getChildCount(); i++) {
            DashboardTableNode node = (DashboardTableNode) parent.getChildAt(i);

            // for each child node of parent, extract the updated status from statusMap
            DashboardStatus status = statusMap.remove(node.getStatus().getKey());

            if (status == null) {
                // if no status is found, then the node needs to be removed from the tree
                removeNodeFromParent(node);
                i--; // decrement i since the subsequent node indices have been shifted -1
            } else {
                // otherwise, replace the node's status with the new status and recursively update the node's child nodes
                node.setStatus(status);
                update(status.getChildStatuses(), node);
            }
        }

        // any remaining entries in statusMap are new status entries that need to be added as nodes to the parent node
        add(statusMap.values(), parent);
    }
}
