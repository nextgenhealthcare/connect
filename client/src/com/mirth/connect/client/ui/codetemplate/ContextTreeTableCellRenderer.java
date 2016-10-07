/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.codetemplate;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import org.apache.commons.lang3.tuple.Pair;
import org.jdesktop.swingx.treetable.TreeTableNode;

import com.mirth.connect.client.ui.components.MirthTriStateCheckBox;
import com.mirth.connect.model.codetemplates.ContextType;

public class ContextTreeTableCellRenderer extends MirthTriStateCheckBox implements TreeCellRenderer {

    public ContextTreeTableCellRenderer() {
        setBackground(null);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Pair<Integer, ?> pair = (Pair<Integer, ?>) ((TreeTableNode) value).getUserObject();
        if (pair != null) {
            setState(pair.getLeft());
            if (pair.getRight() instanceof String) {
                setText((String) pair.getRight());
            } else {
                setText(((ContextType) pair.getRight()).getDisplayName());
            }
        }
        return this;
    }
}