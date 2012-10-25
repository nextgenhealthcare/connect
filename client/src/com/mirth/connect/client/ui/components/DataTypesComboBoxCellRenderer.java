/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import java.awt.Component;

import javax.swing.JTable;

import com.mirth.connect.client.ui.PlatformUI;


public class DataTypesComboBoxCellRenderer extends MirthComboBoxCellRenderer {

    boolean source;
    
    public DataTypesComboBoxCellRenderer(String[] items, boolean source) {
        super(items);
        this.source = source;
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        // The combobox should appear disabled if it's the 
        // source inbound data type and XML is required
        if (source && (row == 0) && PlatformUI.MIRTH_FRAME.channelEditPanel.requiresXmlDataType()) {
            component.setEnabled(false);
        } else {
            component.setEnabled(true);
        }
        
        return component;
    }

}
