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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class DateCellRenderer extends DefaultTableCellRenderer {

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public DateCellRenderer() {
        this(CENTER);
    }

    public DateCellRenderer(int alignment) {
        super();
        this.setHorizontalAlignment(alignment);
    }

    public DateFormat getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            setForeground(table.getSelectionForeground());
            super.setBackground(table.getSelectionBackground());
        } else {
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }
        if (value == null) {
            setText("--");
        } else {
            setText(dateFormat.format(((Calendar) value).getTime()));
        }
        return this;
    }
}
