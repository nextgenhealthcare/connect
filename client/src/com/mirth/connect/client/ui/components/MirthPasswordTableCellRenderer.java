/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

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
