/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/** A new renderer that for table cells that has both an image and a text value. */
public class ImageCellRenderer extends DefaultTableCellRenderer {
    
    public ImageCellRenderer() {
        this(LEFT);        
    }
    
    public ImageCellRenderer(int alignment) {
        super();
        this.setHorizontalAlignment(alignment);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            setForeground(table.getSelectionForeground());
            super.setBackground(table.getSelectionBackground());
        } else {
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }
        CellData data = (CellData) value;

        setText(data.getText());

        setIcon(data.getIcon());

        return this;
    }
}
