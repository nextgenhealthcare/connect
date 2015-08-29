/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.codetemplate;

import com.mirth.connect.client.ui.AbstractSortableTreeTableNode;

public class CodeTemplateRootTreeTableNode extends AbstractSortableTreeTableNode {

    @Override
    public int getColumnCount() {
        return CodeTemplatePanel.TEMPLATE_NUM_COLUMNS;
    }

    @Override
    public Object getValueAt(int i) {
        return null;
    }
}