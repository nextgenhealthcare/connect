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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.mirth.connect.client.ui.PlatformUI;


public class DataTypesCellRenderer implements TableCellRenderer {

    private boolean source;
    private boolean inbound;
    JComboBox comboBox;
    JPanel panel;
    JButton button;
    
    public DataTypesCellRenderer(String[] items, boolean source, boolean inbound) {
        comboBox = new JComboBox(items);
        
        button = new JButton();
        button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/mirth/connect/client/ui/images/wrench.png")));
        
        panel = new JPanel();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = new Insets(0, 0, 0, 0);
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder());
        panel.add(comboBox, constraints);
        panel.add(button);
        
        this.source = source;
        this.inbound = inbound;
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        boolean enabled = true;
        // The cell (combobox) is not editable if it's the source inbound data type and it is required to be XML
        // Or if it is a destination inbound data type
        if (inbound) {
            if ((source && PlatformUI.MIRTH_FRAME.channelEditPanel.requiresXmlDataType()) || !source) {
                enabled = false;
            }
        }
        comboBox.setForeground(table.getForeground());
        comboBox.setBackground(table.getBackground());
        comboBox.setEnabled(enabled);
        comboBox.setSelectedItem(value);
        
        button.setForeground(table.getForeground());
        button.setBackground(table.getBackground());
        
        return panel;
    }

}
