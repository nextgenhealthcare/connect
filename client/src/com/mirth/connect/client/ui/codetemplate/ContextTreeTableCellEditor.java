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
import java.awt.LayoutManager;
import java.awt.event.ActionListener;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTree;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTreeTable;
import com.mirth.connect.client.ui.components.MirthTriStateCheckBox;
import com.mirth.connect.model.codetemplates.ContextType;

public class ContextTreeTableCellEditor extends DefaultCellEditor {

    private CheckBoxPanel panel;
    private MirthTriStateCheckBox checkBox;
    private ContextType contextType;

    public ContextTreeTableCellEditor(ActionListener saveEnableActionListener) {
        super(new MirthTriStateCheckBox());
        panel = new CheckBoxPanel(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
        checkBox = (MirthTriStateCheckBox) editorComponent;
        checkBox.addActionListener(saveEnableActionListener);
        panel.add(checkBox);
    }

    @Override
    public Object getCellEditorValue() {
        if (contextType != null) {
            return new MutablePair<Integer, ContextType>(checkBox.getState(), contextType);
        } else {
            return new MutablePair<Integer, String>(checkBox.getState(), checkBox.getText());
        }
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        Pair<Integer, ?> pair = (Pair<Integer, ?>) value;
        if (pair != null) {
            checkBox.setState(pair.getLeft());
            if (pair.getRight() instanceof String) {
                contextType = null;
                checkBox.setText((String) pair.getRight());
            } else {
                contextType = (ContextType) pair.getRight();
                checkBox.setText(contextType.getDisplayName());
            }
        }

        MirthTreeTable treeTable = (MirthTreeTable) table;
        JTree tree = (JTree) treeTable.getCellRenderer(0, treeTable.getHierarchicalColumn());
        panel.setOffset(tree.getRowBounds(row).x);
        panel.setBackground(row % 2 == 0 ? UIConstants.HIGHLIGHTER_COLOR : UIConstants.BACKGROUND_COLOR);
        checkBox.setBackground(panel.getBackground());
        return panel;
    }

    private class CheckBoxPanel extends JPanel {

        private int offset;

        public CheckBoxPanel(LayoutManager layout) {
            super(layout);
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        @Override
        public void setBounds(int x, int y, int width, int height) {
            super.setBounds(x + offset, y, width - offset, height);
        }
    }
}