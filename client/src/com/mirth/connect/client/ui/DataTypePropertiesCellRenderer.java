/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.mirth.connect.client.ui.components.MirthTriStateCheckBox;
import com.mirth.connect.model.datatype.PropertyEditorType;

public class DataTypePropertiesCellRenderer extends DefaultTableCellRenderer {
    
    private static final String DIFFERENT_VALUES = "<Different Values>";
	private JLabel label;
    private JLabel javascriptLabel;
    private MirthTriStateCheckBox checkBox;
    private JButton button;
    private JPanel panel;
    
    public DataTypePropertiesCellRenderer() {
        label = new JLabel();
        label.setOpaque(true);
        label.setBorder(BorderFactory.createEmptyBorder());
        
        javascriptLabel = new JLabel();
        javascriptLabel.setBorder(BorderFactory.createEmptyBorder());
        
        checkBox = new MirthTriStateCheckBox();

        button = new JButton("Edit");
        button.setAlignmentX(Component.RIGHT_ALIGNMENT);
        
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder());
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = new Insets(0, 0, 0, 0);
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        
        panel.add(javascriptLabel, constraints);
        panel.add(button);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Color backgroundColor = isSelected ? table.getSelectionBackground() : table.getBackground();
        
        checkBox.setBackground(backgroundColor);
        label.setBackground(backgroundColor);
        panel.setBackground(backgroundColor);
        
        if (value != null) {
            if (value instanceof DataTypeNodeDescriptor) {
                DataTypeNodeDescriptor nodeDescriptor = (DataTypeNodeDescriptor) value;
                if (nodeDescriptor.getEditorType() == PropertyEditorType.BOOLEAN) {
                	Boolean booleanValue = (Boolean) nodeDescriptor.getValue();
                    // Display a tri-state checkbox if the type is boolean
                	if (nodeDescriptor.isMultipleValues()) {
                		checkBox.setText(DIFFERENT_VALUES);
                		checkBox.setState(MirthTriStateCheckBox.PARTIAL);
                	} else if (booleanValue) {
                		checkBox.setText("");
                		checkBox.setState(MirthTriStateCheckBox.CHECKED);
                	} else {
                		checkBox.setText("");
                		checkBox.setState(MirthTriStateCheckBox.UNCHECKED);
                	}
                
                    return checkBox;
                } else if (nodeDescriptor.getEditorType() == PropertyEditorType.STRING) {
                	String stringValue = (String) nodeDescriptor.getValue();
                	if (nodeDescriptor.isMultipleValues()) {
                		stringValue = DIFFERENT_VALUES;
                	}

                    // Display a label if the type is a string.
                    label.setText(stringValue);
                    return label;
                } else if (nodeDescriptor.getEditorType() == PropertyEditorType.JAVASCRIPT) {
                	String stringValue = (String) nodeDescriptor.getValue();
                	if (nodeDescriptor.isMultipleValues()) {
                		stringValue = DIFFERENT_VALUES;
                	}
                	
                    // Display a label with a button if the type is Javascript
                    javascriptLabel.setText(stringValue);
                    return panel;
                }
            }
        } else {
            label.setText("");
        }
        
        return label;
    }

}
