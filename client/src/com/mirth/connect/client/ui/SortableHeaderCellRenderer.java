/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public class SortableHeaderCellRenderer implements TableCellRenderer {
    private Icon ascendingSortIcon = UIManager.getIcon("Table.ascendingSortIcon");
    private Icon descendingSortIcon = UIManager.getIcon("Table.descendingSortIcon");
    private Icon currentIcon;
    private int currentModelIndex = -1;
    private TableCellRenderer delegate;

    public SortableHeaderCellRenderer(TableCellRenderer delegate) {
        this.delegate = delegate;
        currentIcon = ascendingSortIcon; // set default icon to ascending
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (c instanceof JLabel) {
            JLabel label = (JLabel) c;

            if (currentModelIndex != -1 && currentIcon != null) {
                TableColumnModel model = table.getColumnModel();
                if (currentModelIndex == model.getColumn(column).getModelIndex()) {
                    label.setHorizontalTextPosition(JLabel.LEFT);
                    label.setIcon(currentIcon);
                } else {
                    label.setIcon(null); // clear all previously set header icons
                }
            }
        }
        return c;
    }

    public void setColumnIndex(int columnIndex) {
        this.currentModelIndex = columnIndex;
    }

    public void setSortingIcon(SortOrder sortOrder) {
        if (sortOrder != SortOrder.UNSORTED) {
            if (sortOrder == SortOrder.ASCENDING) {
                currentIcon = ascendingSortIcon;
            } else {
                currentIcon = descendingSortIcon;
            }
        }
    }
}