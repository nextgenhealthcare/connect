/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class MirthButtonCellRenderer implements TableCellRenderer {

    JButton button = new JButton();

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // MIRTH-2186 Java sometimes calls this method with a hard-coded null value so tablecellrenderers must explicitly check for null.
        // Only observed so far on Mac OSX.
        // http://stackoverflow.com/questions/3054775/java-swing-jtable-strange-behavior-from-getaccessiblechild-method-resulting
        if (value != null) {
            button.setText(value.toString());
        }
        return button;
    }
}
