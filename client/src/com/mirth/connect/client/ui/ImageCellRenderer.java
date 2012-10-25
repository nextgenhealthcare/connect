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
        
        // MIRTH-2186 Java sometimes calls this method with a hard-coded null value so tablecellrenderers must explicitly check for null.
        // Only observed so far on Mac OSX.
        // http://stackoverflow.com/questions/3054775/java-swing-jtable-strange-behavior-from-getaccessiblechild-method-resulting
        if (value != null) {
            CellData data = (CellData) value;
    
            setText(data.getText());
    
            setIcon(data.getIcon());
        }

        return this;
    }
}
