/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.util.HashSet;
import java.util.Set;


public class DataTypeConnectorTableModel extends SortableTreeTableModel {

    private AbstractSortableTreeTableNode root;
    private Set<Integer> destinations = new HashSet<Integer>();
    
    public DataTypeConnectorTableModel() {
        root = new AbstractSortableTreeTableNode() {
            @Override
            public Object getValueAt(int column) {
                return null;
            }

            @Override
            public int getColumnCount() {
                return 0;
            }
        };
        
        setRoot(root);
    }
    
    @Override
    public int getHierarchicalColumn() {
        return 1;
    }
    
    @Override
    public Class<?> getColumnClass(int column) {
        return null;
    }
    
    @Override
    public boolean isCellEditable(Object node, int column) {
    	boolean editable = false;
    	
        if (node instanceof DataTypeConnectorTableNode) {
        	DataTypeConnectorTableNode tableNode = (DataTypeConnectorTableNode) node;
        	
        	// Ask the cell whether it is editable
        	editable = tableNode.isEditable(column);
        	
        	// If the cell is an inbound column
        	if (column == DataTypesDialog.INBOUND_COLUMN) {
        		// Disable if the channel requires a particular data type or if the cell belongs to a destination connector
        		if (destinations.contains(tableNode.getContainerIndex()) || (tableNode.getContainerIndex() == 0 && PlatformUI.MIRTH_FRAME.channelEditPanel.getRequiredInboundDataType() != null)) {
        			editable = false;
        		}
        	}
        	// If the cell is an outbound column
        	if (column == DataTypesDialog.OUTBOUND_COLUMN) {
        		// Disable if the channel requires a particular data type
        		if (tableNode.getContainerIndex() == 0 && PlatformUI.MIRTH_FRAME.channelEditPanel.getRequiredOutboundDataType() != null) {
        			editable = false;
        		}
        	}
        }
        
        return editable;
    }
    
    /**
     * Sets the container indexes that are destination connectors
     */
    public void setDestinations(Set<Integer> destinations) {
    	this.destinations = destinations;
    }

    /**
     * Populate the connector tree table
     */
    public void addConnector(int containerIndex, Object[][] connector) {
    	Object[] transformer = connector[0];
    	
    	DataTypeConnectorTableNode connectorNode = new DataTypeConnectorTableNode(containerIndex, transformer);
    	insertNodeInto(connectorNode, root);
    	
    	for (int i = 1; i < connector.length; i++) {
    		insertNodeInto(new DataTypeConnectorTableNode(containerIndex + i, connector[i]), connectorNode);
    	}
    }

}