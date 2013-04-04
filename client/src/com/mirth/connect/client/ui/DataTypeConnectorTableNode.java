/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;

public class DataTypeConnectorTableNode extends AbstractMutableTreeTableNode {
	
	private int containerIndex;
	private Object[] values;
	
	public DataTypeConnectorTableNode(int containerIndex, Object[] values) {
		this.containerIndex = containerIndex;
		this.values = values;
	}
	
	/**
	 * Get the container index that is used to retrieve the transformer container
	 */
	public int getContainerIndex() {
		return containerIndex;
	}
	
	@Override
    public boolean isEditable(int column) {
        if (column == DataTypesDialog.CONNECTOR_COLUMN) {
            return false;
        }
        
        return true;
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public Object getValueAt(int column) {
        return values[column];
    }
    
    @Override
    public void setValueAt(Object value, int column) {
    	values[column] = value;
    }
}
