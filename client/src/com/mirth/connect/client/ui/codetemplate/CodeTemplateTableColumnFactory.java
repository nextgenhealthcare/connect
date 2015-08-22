/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.codetemplate;

import javax.swing.SwingConstants;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;

import com.mirth.connect.client.ui.DateCellRenderer;
import com.mirth.connect.client.ui.NumberCellRenderer;

public class CodeTemplateTableColumnFactory extends ColumnFactory {

    @Override
    public TableColumnExt createAndConfigureTableColumn(TableModel model, int modelIndex) {
        TableColumnExt column = super.createAndConfigureTableColumn(model, modelIndex);

        switch (modelIndex) {
            case CodeTemplatePanel.TEMPLATE_TYPE_COLUMN:
                column.setMinWidth(50);
                column.setMaxWidth(135);
                break;
            case CodeTemplatePanel.TEMPLATE_REVISION_COLUMN:
                column.setMaxWidth(60);
                column.setMinWidth(60);
                column.setCellRenderer(new NumberCellRenderer(SwingConstants.CENTER, true));
                break;
            case CodeTemplatePanel.TEMPLATE_LAST_MODIFIED_COLUMN:
                column.setMaxWidth(95);
                column.setMinWidth(95);
                column.setCellRenderer(new DateCellRenderer());
                break;
        }

        return column;
    }
}