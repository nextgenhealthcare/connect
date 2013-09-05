/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;

public class DataTypeColumnFactory extends ColumnFactory {

    public DataTypeColumnFactory() {

    }

    @Override
    public TableColumnExt createAndConfigureTableColumn(TableModel model, int modelIndex) {
        TableColumnExt column = super.createAndConfigureTableColumn(model, modelIndex);
        
        switch (modelIndex) {
            case 0:
                column.setCellRenderer(new DefaultTableCellRenderer());
                column.setMinWidth(UIConstants.MIN_WIDTH);
                break;

            case 1:
                column.setCellRenderer(new DataTypePropertiesCellRenderer());
                column.setCellEditor(new DataTypePropertiesCellEditor());
                column.setMinWidth(UIConstants.MIN_WIDTH);
                break;


            default:
                column.setCellRenderer(new DefaultTableCellRenderer());
                column.setMinWidth(UIConstants.MIN_WIDTH);
                break;
        }

        return column;
    }
}
