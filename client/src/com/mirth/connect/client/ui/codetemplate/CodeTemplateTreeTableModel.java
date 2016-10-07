/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.codetemplate;

import java.util.Calendar;

import com.mirth.connect.client.ui.SortableTreeTableModel;
import com.mirth.connect.model.codetemplates.CodeTemplateProperties.CodeTemplateType;

public class CodeTemplateTreeTableModel extends SortableTreeTableModel {

    public CodeTemplateTreeTableModel() {
        setSortChildNodes(true);
    }

    @Override
    public int getHierarchicalColumn() {
        return CodeTemplatePanel.TEMPLATE_NAME_COLUMN;
    }

    @Override
    public Class<?> getColumnClass(int column) {
        // @formatter:off
        switch(column) {
            case CodeTemplatePanel.TEMPLATE_NAME_COLUMN: return String.class;
            case CodeTemplatePanel.TEMPLATE_ID_COLUMN: return String.class;
            case CodeTemplatePanel.TEMPLATE_TYPE_COLUMN: return CodeTemplateType.class;
            case CodeTemplatePanel.TEMPLATE_DESCRIPTION_COLUMN: return String.class;
            case CodeTemplatePanel.TEMPLATE_REVISION_COLUMN: return Integer.class;
            case CodeTemplatePanel.TEMPLATE_LAST_MODIFIED_COLUMN: return Calendar.class;
            default: return String.class;
        }
        // @formatter:on
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        return column == 0;
    }
}