/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.editors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.model.FilterTransformerElement;
import com.mirth.connect.model.IteratorElement;

import net.miginfocom.swing.MigLayout;

public abstract class IteratorPanel<C extends FilterTransformerElement> extends EditorPanel<C> {

    private ActionListener nameActionListener;

    public IteratorPanel() {
        baseInitComponents();
        initToolTips();
        initLayout();
    }

    protected abstract IteratorElement<C> newIteratorElement();

    protected abstract String getName(String target);

    public abstract void setName(IteratorElement<C> properties);

    protected abstract void initComponents();

    protected abstract void addMiddleComponents();

    @Override
    @SuppressWarnings("unchecked")
    public C getProperties() {
        IteratorElement<C> props = newIteratorElement();

        props.getProperties().setTarget(targetField.getText());
        props.getProperties().setIndexVariable(indexVariableField.getText());

        List<String> prefixSubstitutions = new ArrayList<String>();
        for (int i = 0; i < prefixSubstitutionsTable.getModel().getRowCount(); i++) {
            String prefix = (String) prefixSubstitutionsTable.getModel().getValueAt(i, 0);
            if (StringUtils.isNotBlank(prefix)) {
                prefixSubstitutions.add(prefix);
            }
        }
        props.getProperties().setPrefixSubstitutions(prefixSubstitutions);

        return (C) props;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setProperties(C properties) {
        IteratorElement<C> props = (IteratorElement<C>) properties;

        targetField.setText(props.getProperties().getTarget());
        indexVariableField.setText(props.getProperties().getIndexVariable());

        Object[][] data = new Object[props.getProperties().getPrefixSubstitutions().size()][1];
        int i = 0;
        for (String prefix : props.getProperties().getPrefixSubstitutions()) {
            data[i][0] = prefix;
            i++;
        }
        ((RefreshTableModel) prefixSubstitutionsTable.getModel()).refreshDataVector(data);
        deselectRows();
    }

    @Override
    @SuppressWarnings("unchecked")
    public String checkProperties(C properties, boolean highlight) {
        IteratorElement<C> props = (IteratorElement<C>) properties;
        String errors = "";

        if (StringUtils.isBlank(props.getProperties().getTarget())) {
            errors += "The iteration target expression cannot be blank.\n";
            if (highlight) {
                targetField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (StringUtils.isBlank(props.getProperties().getIndexVariable())) {
            errors += "The iteration index variable cannot be blank.\n";
            if (highlight) {
                indexVariableField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return errors;
    }

    @Override
    public void resetInvalidProperties() {
        targetField.setBackground(null);
        indexVariableField.setBackground(null);
    }

    @Override
    public void setNameActionListener(ActionListener actionListener) {
        nameActionListener = actionListener;
    }

    protected void updateName() {
        updateName(targetField.getText());
    }

    protected void updateName(String target) {
        String name = getName(StringUtils.defaultIfBlank(target, "..."));
        if (nameActionListener != null) {
            nameActionListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, name));
        }
    }

    private void baseInitComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);

        targetLabel = new JLabel("Iterate On:");
        targetField = new JTextField();
        targetField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent evt) {
                documentChanged(evt);
            }

            @Override
            public void insertUpdate(DocumentEvent evt) {
                documentChanged(evt);
            }

            @Override
            public void changedUpdate(DocumentEvent evt) {
                documentChanged(evt);
            }

            private void documentChanged(DocumentEvent evt) {
                try {
                    updateName(evt.getDocument().getText(0, evt.getDocument().getLength()));
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });

        indexVariableLabel = new JLabel("Index Variable:");
        indexVariableField = new JTextField();

        prefixSubstitutionsLabel = new JLabel("Drag-and-Drop Substitutions:");

        prefixSubstitutionsTable = new MirthTable();
        prefixSubstitutionsTable.setModel(new RefreshTableModel(new String[] {
                "Substitution Prefix" }, 0));

        prefixSubstitutionsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                if (getSelectedRow() != -1) {
                    prefixSubstitutionsDeleteButton.setEnabled(true);
                } else {
                    prefixSubstitutionsDeleteButton.setEnabled(false);
                }
            }
        });

        prefixSubstitutionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        prefixSubstitutionsTable.setRowSelectionAllowed(true);
        prefixSubstitutionsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        prefixSubstitutionsTable.setDragEnabled(false);
        prefixSubstitutionsTable.setOpaque(true);
        prefixSubstitutionsTable.setSortable(false);
        prefixSubstitutionsTable.setEditable(true);
        prefixSubstitutionsTable.getTableHeader().setReorderingAllowed(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            prefixSubstitutionsTable.setHighlighters(highlighter);
        }

        prefixSubstitutionsScrollPane = new JScrollPane(prefixSubstitutionsTable);
        prefixSubstitutionsScrollPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                deselectRows();
            }
        });

        prefixSubstitutionsNewButton = new JButton("New");
        prefixSubstitutionsNewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                ((DefaultTableModel) prefixSubstitutionsTable.getModel()).addRow(new Object[] {
                        "" });
                prefixSubstitutionsTable.setRowSelectionInterval(prefixSubstitutionsTable.getRowCount() - 1, prefixSubstitutionsTable.getRowCount() - 1);
            }
        });

        prefixSubstitutionsDeleteButton = new JButton("Delete");
        prefixSubstitutionsDeleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                int selectedRow = getSelectedRow();
                if (selectedRow != -1 && !prefixSubstitutionsTable.isEditing()) {
                    ((RefreshTableModel) prefixSubstitutionsTable.getModel()).removeRow(prefixSubstitutionsTable.convertRowIndexToModel(selectedRow));

                    if (prefixSubstitutionsTable.getRowCount() > 0) {
                        if (selectedRow < prefixSubstitutionsTable.getRowCount()) {
                            prefixSubstitutionsTable.setRowSelectionInterval(selectedRow, selectedRow);
                        } else {
                            prefixSubstitutionsTable.setRowSelectionInterval(prefixSubstitutionsTable.getRowCount() - 1, prefixSubstitutionsTable.getRowCount() - 1);
                        }
                    }
                }
            }
        });
        prefixSubstitutionsDeleteButton.setEnabled(false);

        initComponents();
    }

    private void initToolTips() {
        targetField.setToolTipText("<html>Specify the element to iterate on.<br/>This may be a list of E4X XML nodes,<br/>or a Java / JavaScript array.</html>");
        indexVariableField.setToolTipText("Set the index variable to use for each iteration.");
        prefixSubstitutionsTable.setToolTipText("<html>When drag-and-dropping values into the<br/>children underneath this iterator, the<br/>index variable (e.g. \"[i]\") will be<br/>injected after any of these prefixes.</html>");
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 8, novisualpadding, hidemode 3, gap 6"));

        add(targetLabel, "right, gapafter 6");
        add(targetField, "growx, sx");
        add(indexVariableLabel, "newline, right, gapafter 6");
        add(indexVariableField, "sx, growx");

        addMiddleComponents();

        add(prefixSubstitutionsLabel, "newline, top, right, gapafter 6");
        add(prefixSubstitutionsScrollPane, "grow, push, sy");
        add(prefixSubstitutionsNewButton, "top, flowy, split 2, sgx");
        add(prefixSubstitutionsDeleteButton, "top, sgx");
    }

    private void deselectRows() {
        prefixSubstitutionsTable.clearSelection();
        prefixSubstitutionsDeleteButton.setEnabled(false);
    }

    private int getSelectedRow() {
        if (prefixSubstitutionsTable.isEditing()) {
            return prefixSubstitutionsTable.getEditingRow();
        } else {
            return prefixSubstitutionsTable.getSelectedRow();
        }
    }

    private JLabel targetLabel;
    private JTextField targetField;
    private JLabel indexVariableLabel;
    private JTextField indexVariableField;
    private JLabel prefixSubstitutionsLabel;
    private MirthTable prefixSubstitutionsTable;
    private JScrollPane prefixSubstitutionsScrollPane;
    private JButton prefixSubstitutionsNewButton;
    private JButton prefixSubstitutionsDeleteButton;
}