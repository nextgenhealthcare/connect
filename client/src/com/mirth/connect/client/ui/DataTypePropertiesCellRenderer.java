package com.mirth.connect.client.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.mirth.connect.model.datatype.DataTypePropertyDescriptor;
import com.mirth.connect.model.datatype.PropertyEditorType;

public class DataTypePropertiesCellRenderer extends DefaultTableCellRenderer {
    
    private JLabel label;
    private JLabel javascriptLabel;
    private JCheckBox checkBox;
    private JButton button;
    private JPanel panel;
    
    public DataTypePropertiesCellRenderer() {
        label = new JLabel();
        label.setOpaque(true);
        label.setBorder(BorderFactory.createEmptyBorder());
        
        javascriptLabel = new JLabel();
        javascriptLabel.setBorder(BorderFactory.createEmptyBorder());
        
        checkBox = new JCheckBox();

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
            if (value instanceof DataTypePropertyDescriptor) {
                DataTypePropertyDescriptor propertyDescriptor = (DataTypePropertyDescriptor) value;
                if (propertyDescriptor.getEditorType() == PropertyEditorType.BOOLEAN) {
                    // Display a checkbox if the type is boolean
                    checkBox.setSelected((Boolean) propertyDescriptor.getValue());
                
                    return checkBox;
                } else if (propertyDescriptor.getEditorType() == PropertyEditorType.STRING) {
                    // Display a label if the type is a string.
                    label.setText((String) propertyDescriptor.getValue());
                    return label;
                } else if (propertyDescriptor.getEditorType() == PropertyEditorType.JAVASCRIPT) {
                    // Display a label with a button if the type is Javascript
                    javascriptLabel.setText((String) propertyDescriptor.getValue());
                    return panel;
                }
            }
        } else {
            label.setText("");
        }
        
        return label;
    }

}
