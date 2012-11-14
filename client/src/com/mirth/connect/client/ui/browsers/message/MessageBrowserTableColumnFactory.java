/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.browsers.message;

import java.text.SimpleDateFormat;

import javax.swing.table.TableModel;

import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;

import com.mirth.connect.client.ui.DateCellRenderer;
import com.mirth.connect.client.ui.NumberCellRenderer;

public class MessageBrowserTableColumnFactory extends ColumnFactory {
    @Override
    public TableColumnExt createAndConfigureTableColumn(TableModel model, int index) {
        TableColumnExt column = super.createAndConfigureTableColumn(model, index);

        switch (index) {
            case 0:
            case 5:
            case 6:
                column.setCellRenderer(new NumberCellRenderer());
                column.setMaxWidth(80);
                column.setMinWidth(80);
                break;
                
            case 3:
                DateCellRenderer dateCellRenderer = new DateCellRenderer();
                dateCellRenderer.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS"));
                column.setCellRenderer(dateCellRenderer);
                column.setMaxWidth(140);
                column.setMinWidth(140);
                break;
        }

        return column;
    }
}
