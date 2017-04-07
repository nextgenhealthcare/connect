/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.messagebuilder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.TextFieldCellEditor;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.editors.EditorPanel;
import com.mirth.connect.model.Step;

public class MessageBuilderPanel extends EditorPanel<Step> {

    private static final int REGEX_COLUMN = 0;
    private static final int REPLACEMENT_COLUMN = 1;
    private static final String REGEX_COLUMN_NAME = "Regular Expression";
    private static final String REPLACEMENT_COLUMN_NAME = "Replace With";

    public MessageBuilderPanel() {
        initComponents();
        initLayout();
    }

    @Override
    public Step getDefaults() {
        return new MessageBuilderStep();
    }

    @Override
    public Step getProperties() {
        MessageBuilderStep props = new MessageBuilderStep();

        props.setMessageSegment(messageSegmentField.getText().trim());
        props.setMapping(mappingField.getText().trim());
        props.setDefaultValue(defaultValueField.getText().trim());

        List<Pair<String, String>> replacements = new ArrayList<Pair<String, String>>();
        for (int i = 0; i < regularExpressionsTable.getModel().getRowCount(); i++) {
            String regex = (String) regularExpressionsTable.getModel().getValueAt(i, REGEX_COLUMN);
            if (StringUtils.isNotBlank(regex)) {
                replacements.add(new ImmutablePair<String, String>(regex, (String) regularExpressionsTable.getValueAt(i, REPLACEMENT_COLUMN)));
            }
        }
        props.setReplacements(replacements);

        return props;
    }

    @Override
    public void setProperties(Step properties) {
        MessageBuilderStep props = (MessageBuilderStep) properties;

        messageSegmentField.setText(props.getMessageSegment());
        mappingField.setText(props.getMapping());
        defaultValueField.setText(props.getDefaultValue());

        List<Pair<String, String>> replacements = props.getReplacements();
        if (replacements != null) {
            setRegexProperties(replacements);
        } else {
            setRegexProperties(new ArrayList<Pair<String, String>>());
        }
    }

    @Override
    public String checkProperties(Step properties, boolean highlight) {
        MessageBuilderStep props = (MessageBuilderStep) properties;
        String errors = "";

        if (StringUtils.isBlank(props.getMessageSegment())) {
            errors += "The message segment value cannot be blank.\n";
            if (highlight) {
                messageSegmentField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return errors;
    }

    @Override
    public void resetInvalidProperties() {
        messageSegmentField.setBackground(null);
    }

    @Override
    public void setNameActionListener(ActionListener actionListener) {}

    private void setRegexProperties(List<Pair<String, String>> properties) {
        Object[][] tableData = new Object[properties.size()][2];

        if (properties != null) {
            int i = 0;
            for (Pair<String, String> pair : properties) {
                tableData[i][REGEX_COLUMN] = pair.getLeft();
                tableData[i][REPLACEMENT_COLUMN] = pair.getRight();
                i++;
            }
        }

        ((RefreshTableModel) regularExpressionsTable.getModel()).refreshDataVector(tableData);
    }

    /** Clears the selection in the table and sets the tasks appropriately */
    public void deselectRows() {
        regularExpressionsTable.clearSelection();
        deleteButton.setEnabled(false);
    }

    /** Get the currently selected destination index */
    public int getSelectedRow() {
        if (regularExpressionsTable.isEditing()) {
            return regularExpressionsTable.getEditingRow();
        } else {
            return regularExpressionsTable.getSelectedRow();
        }
    }

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);

        messageSegmentLabel = new JLabel("Message Segment:");
        messageSegmentField = new JTextField();

        mappingLabel = new JLabel("Mapping:");
        mappingField = new JTextField();

        defaultValueLabel = new JLabel("Default Value:");
        defaultValueField = new JTextField();

        replacementLabel = new JLabel("String Replacement:");

        regularExpressionsTable = new MirthTable();
        regularExpressionsTable.setModel(new RefreshTableModel(new String[] { REGEX_COLUMN_NAME,
                REPLACEMENT_COLUMN_NAME }, 0) {

            boolean[] canEdit = new boolean[] { true, true };

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        regularExpressionsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                if (getSelectedRow() != -1) {
                    deleteButton.setEnabled(true);
                } else {
                    deleteButton.setEnabled(false);
                }
            }
        });

        class RegExTableCellEditor extends TextFieldCellEditor {
            @Override
            public boolean isCellEditable(EventObject evt) {
                boolean editable = super.isCellEditable(evt);

                if (editable) {
                    deleteButton.setEnabled(false);
                }

                return editable;
            }

            @Override
            protected boolean valueChanged(String value) {
                deleteButton.setEnabled(true);
                return true;
            }
        }

        regularExpressionsTable.getColumnModel().getColumn(regularExpressionsTable.getColumnModel().getColumnIndex(REGEX_COLUMN_NAME)).setCellEditor(new RegExTableCellEditor());
        regularExpressionsTable.getColumnModel().getColumn(regularExpressionsTable.getColumnModel().getColumnIndex(REPLACEMENT_COLUMN_NAME)).setCellEditor(new RegExTableCellEditor());
        regularExpressionsTable.setCustomEditorControls(true);

        regularExpressionsTable.setSelectionMode(0);
        regularExpressionsTable.setRowSelectionAllowed(true);
        regularExpressionsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        regularExpressionsTable.setDragEnabled(false);
        regularExpressionsTable.setOpaque(true);
        regularExpressionsTable.setSortable(false);
        regularExpressionsTable.getTableHeader().setReorderingAllowed(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            regularExpressionsTable.setHighlighters(highlighter);
        }

        regularExpressionsScrollPane = new JScrollPane(regularExpressionsTable);
        regularExpressionsScrollPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                deselectRows();
            }
        });

        newButton = new JButton("New");
        newButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });

        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });
        deleteButton.setEnabled(false);
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 8, novisualpadding, hidemode 3, gap 6"));

        add(messageSegmentLabel, "right, gapafter 6");
        add(messageSegmentField, "growx, sx");
        add(mappingLabel, "newline, right, gapafter 6");
        add(mappingField, "sx, growx");
        add(defaultValueLabel, "newline, right, gapafter 6");
        add(defaultValueField, "sx, growx");
        add(replacementLabel, "newline, top, right, gapafter 6");
        add(regularExpressionsScrollPane, "grow, push, sy");
        add(newButton, "top, flowy, split 2, sgx");
        add(deleteButton, "top, sgx");
    }

    private void deleteButtonActionPerformed(ActionEvent evt) {
        int selectedRow = getSelectedRow();
        if (selectedRow != -1 && !regularExpressionsTable.isEditing()) {
            ((RefreshTableModel) regularExpressionsTable.getModel()).removeRow(regularExpressionsTable.convertRowIndexToModel(selectedRow));

            if (regularExpressionsTable.getRowCount() > 0) {
                if (selectedRow < regularExpressionsTable.getRowCount()) {
                    regularExpressionsTable.setRowSelectionInterval(selectedRow, selectedRow);
                } else {
                    regularExpressionsTable.setRowSelectionInterval(regularExpressionsTable.getRowCount() - 1, regularExpressionsTable.getRowCount() - 1);
                }
            }
        }
    }

    private void newButtonActionPerformed(ActionEvent evt) {
        ((DefaultTableModel) regularExpressionsTable.getModel()).addRow(new Object[] { "", "" });
        regularExpressionsTable.setRowSelectionInterval(regularExpressionsTable.getRowCount() - 1, regularExpressionsTable.getRowCount() - 1);
    }

    private JLabel messageSegmentLabel;
    private JTextField messageSegmentField;
    private JLabel mappingLabel;
    private JTextField mappingField;
    private JLabel defaultValueLabel;
    private JTextField defaultValueField;
    private JLabel replacementLabel;
    private MirthTable regularExpressionsTable;
    private JScrollPane regularExpressionsScrollPane;
    private JButton newButton;
    private JButton deleteButton;
}
