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
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.jdesktop.swingx.treetable.TreeTableNode;

import com.mirth.connect.client.ui.components.MirthTreeTable;

public class CodeTemplateTreeTableCellEditor extends DefaultCellEditor {

    private CodeTemplatePanel parent;
    private OffsetPanel panel;
    private JTextField field;

    public CodeTemplateTreeTableCellEditor(CodeTemplatePanel parent) {
        super(new JTextField());
        this.parent = parent;
        panel = new OffsetPanel(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
        field = (JTextField) editorComponent;
        panel.add(field, "grow, push");
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        MirthTreeTable treeTable = (MirthTreeTable) table;
        JTree tree = (JTree) treeTable.getCellRenderer(0, treeTable.getHierarchicalColumn());
        panel.setOffset(tree.getRowBounds(row).x);
        field.setText((String) value);
        return panel;
    }

    @Override
    public boolean isCellEditable(EventObject evt) {
        return evt != null && evt instanceof MouseEvent && ((MouseEvent) evt).getClickCount() >= 2;
    }

    @Override
    public boolean stopCellEditing() {
        String value = (String) getCellEditorValue();
        boolean valid = StringUtils.isNotBlank(value) && value.length() <= 255;

        if (valid) {
            int selectedRow = parent.getTreeTable().getSelectedRow();
            if (selectedRow >= 0) {
                TreePath selectedPath = parent.getTreeTable().getPathForRow(selectedRow);
                if (selectedPath != null) {
                    TreeTableNode selectedNode = (TreeTableNode) selectedPath.getLastPathComponent();

                    if (selectedNode instanceof CodeTemplateLibraryTreeTableNode) {
                        for (Enumeration<? extends MutableTreeTableNode> libraries = ((MutableTreeTableNode) parent.getFullModel().getRoot()).children(); libraries.hasMoreElements();) {
                            if (((CodeTemplateLibraryTreeTableNode) libraries.nextElement()).getLibrary().getName().equals(value)) {
                                valid = false;
                                break;
                            }
                        }
                    } else {
                        CodeTemplateLibraryTreeTableNode libraryNode = (CodeTemplateLibraryTreeTableNode) selectedNode.getParent();

                        for (Enumeration<? extends MutableTreeTableNode> codeTemplates = libraryNode.children(); codeTemplates.hasMoreElements();) {
                            CodeTemplateTreeTableNode codeTemplateNode = (CodeTemplateTreeTableNode) codeTemplates.nextElement();
                            if (codeTemplateNode.getCodeTemplate().getName().equals(value)) {
                                valid = false;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (valid) {
            parent.setSaveEnabled(true);

            TreePath selectedPath = parent.getTreeTable().getTreeSelectionModel().getSelectionPath();
            if (selectedPath != null && selectedPath.getLastPathComponent() instanceof CodeTemplateLibraryTreeTableNode) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        parent.updateLibrariesComboBox();
                    }
                });
            }
        } else {
            super.cancelCellEditing();
        }

        return super.stopCellEditing();
    }

    private class OffsetPanel extends JPanel {

        private int offset;

        public OffsetPanel(LayoutManager layout) {
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