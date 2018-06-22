package com.mirth.connect.client.ui.components;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class MirthPasswordTableCellRenderer extends DefaultTableCellRenderer {

    JPasswordField passwordField = new JPasswordField();
    
    public MirthPasswordTableCellRenderer() {
        passwordField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        passwordField.setBackground(Color.WHITE);
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        passwordField.setText((String) value);
        
        if (isSelected) {
            passwordField.setBackground(table.getSelectionBackground());
        } else {
            passwordField.setBackground(table.getBackground());
        }
        
        return passwordField;
    }
}
