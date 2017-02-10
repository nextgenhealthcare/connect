/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.editors;

import com.mirth.connect.model.Filter;
import com.mirth.connect.model.Rule;
import com.mirth.connect.model.Rule.Operator;

public class FilterTreeTableNode extends FilterTransformerTreeTableNode<Filter, Rule> {

    public FilterTreeTableNode(BaseEditorPane<Filter, Rule> editorPane, Rule element) {
        super(editorPane, element);
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public Object getValueAt(int column) {
        if (column == editorPane.operatorColumn) {
            return element.getOperator();
        }
        return super.getValueAt(column);
    }

    @Override
    public void setValueAt(Object value, int column) {
        if (column == editorPane.operatorColumn) {
            element.setOperator((Operator) value);
        }
        super.setValueAt(value, column);
    }
}