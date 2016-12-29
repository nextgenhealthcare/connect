/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.rulebuilder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.text.BadLocationException;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.TextFieldCellEditor;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.editors.EditorPanel;
import com.mirth.connect.model.Rule;
import com.mirth.connect.plugins.rulebuilder.RuleBuilderRule.Condition;

import net.miginfocom.swing.MigLayout;

public class RuleBuilderPanel extends EditorPanel<Rule> {

    private static final int VALUE_COLUMN = 0;
    private static final String VALUE_COLUMN_NAME = "Value";

    private List<ActionListener> nameActionListeners = new ArrayList<ActionListener>();

    public RuleBuilderPanel() {
        initComponents();
        initLayout();
    }

    @Override
    public Rule getDefaults() {
        return new RuleBuilderRule();
    }

    @Override
    public Rule getProperties() {
        RuleBuilderRule props = new RuleBuilderRule();

        props.setField(fieldField.getText());
        props.setCondition(getSelectedCondition());
        props.setValues(getValues());

        return props;
    }

    @Override
    public void setProperties(Rule properties) {
        RuleBuilderRule props = (RuleBuilderRule) properties;

        fieldField.setText(props.getField());
        conditionRadioMap.get(props.getCondition()).setSelected(true);
        setValues(props.getValues());
        properties.setName(updateName());
    }

    @Override
    public String checkProperties(Rule properties, boolean highlight) {
        RuleBuilderRule props = (RuleBuilderRule) properties;
        String errors = "";

        if (StringUtils.isBlank(props.getField())) {
            errors += "The field cannot be blank.\n";
            if (highlight) {
                fieldField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return errors;
    }

    @Override
    public void resetInvalidProperties() {
        fieldField.setBackground(null);
    }

    @Override
    public void addNameActionListener(ActionListener actionListener) {
        nameActionListeners.add(actionListener);
    }

    public void setValues(List<String> values) {
        Object[][] tableData = new Object[values.size()][1];
        for (int i = 0; i < values.size(); i++) {
            tableData[i][VALUE_COLUMN] = values.get(i);
        }
        ((RefreshTableModel) valuesTable.getModel()).refreshDataVector(tableData);
    }

    public List<String> getValues() {
        List<String> values = new ArrayList<String>();

        for (int i = 0; i < valuesTable.getRowCount(); i++) {
            if (((String) valuesTable.getValueAt(i, VALUE_COLUMN)).length() > 0) {
                values.add((String) valuesTable.getValueAt(i, VALUE_COLUMN));
            }
        }

        return values;
    }

    /** Clears the selection in the table and sets the tasks appropriately */
    public void deselectRows() {
        valuesTable.clearSelection();
        deleteButton.setEnabled(false);
    }

    /** Get the currently selected destination index */
    public int getSelectedRow() {
        if (valuesTable.isEditing()) {
            return valuesTable.getEditingRow();
        } else {
            return valuesTable.getSelectedRow();
        }
    }

    private void setValuesEnabled(boolean enabled) {
        if (valuesTable.isEditing()) {
            valuesTable.getCellEditor().stopCellEditing();
        }
        valuesScrollPane.setEnabled(enabled);
        valuesTable.setEnabled(enabled);
        valuesLabel.setEnabled(enabled);
        newButton.setEnabled(enabled);

        deselectRows();
    }

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);

        behaviorLabel = new JLabel("Behavior:");
        acceptLabel = new JLabel("Accept");

        fieldLabel = new JLabel("Field:");

        fieldField = new JTextField();
        fieldField.getDocument().addDocumentListener(new DocumentListener() {
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

        conditionLabel = new JLabel("Condition:");

        conditionRadioMap = new LinkedHashMap<Condition, JRadioButton>();
        ButtonGroup conditionButtonGroup = new ButtonGroup();

        for (Condition condition : Condition.values()) {
            JRadioButton radio = new JRadioButton(condition.toString());
            radio.setBackground(getBackground());
            radio.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    conditionRadioActionPerformed(condition);
                }
            });
            conditionButtonGroup.add(radio);
            conditionRadioMap.put(condition, radio);
        }

        valuesLabel = new JLabel("Values:");

        valuesTable = new MirthTable();
        valuesTable.setModel(new RefreshTableModel(new String[] { VALUE_COLUMN_NAME }, 0) {
            boolean[] canEdit = new boolean[] { true };

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        valuesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                deleteButton.setEnabled(getSelectedRow() >= 0);
            }
        });

        class RegExTableCellEditor extends TextFieldCellEditor {
            @Override
            public boolean stopCellEditing() {
                deleteButton.setEnabled(true);
                return super.stopCellEditing();
            }

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
                return true;
            }
        }

        // Set the custom cell editor for the Destination Name column.
        valuesTable.getColumnModel().getColumn(valuesTable.getColumnModel().getColumnIndex(VALUE_COLUMN_NAME)).setCellEditor(new RegExTableCellEditor());
        valuesTable.setCustomEditorControls(true);

        valuesTable.setSelectionMode(0);
        valuesTable.setRowSelectionAllowed(true);
        valuesTable.setRowHeight(UIConstants.ROW_HEIGHT);
        valuesTable.setDragEnabled(false);
        valuesTable.setOpaque(true);
        valuesTable.setSortable(false);
        valuesTable.getTableHeader().setReorderingAllowed(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            valuesTable.setHighlighters(highlighter);
        }

        valuesTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent evt) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        updateName();
                    }
                });
            }
        });

        valuesScrollPane = new JScrollPane(valuesTable);
        valuesScrollPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                deselectRows();
            }
        });

        newButton = new JButton("New");
        newButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });

        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });
        deleteButton.setEnabled(false);
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 8, novisualpadding, hidemode 3, gap 6"));

        add(behaviorLabel, "right, gapafter 6");
        add(acceptLabel);
        add(fieldLabel, "newline, right, gapafter 6");
        add(fieldField, "sx, growx");
        add(conditionLabel, "newline, right, gapafter 6");
        for (JRadioButton radio : conditionRadioMap.values()) {
            add(radio, "split " + conditionRadioMap.size());
        }
        add(valuesLabel, "newline, top, right, gapafter 6");
        add(valuesScrollPane, "grow, push, sy");
        add(newButton, "top, flowy, split 2, sgx");
        add(deleteButton, "top, sgx");
    }

    private void conditionRadioActionPerformed(Condition condition) {
        setValuesEnabled(condition.isValuesEnabled());
        updateName(condition);
    }

    private Condition getSelectedCondition() {
        for (Entry<Condition, JRadioButton> entry : conditionRadioMap.entrySet()) {
            if (entry.getValue().isSelected()) {
                return entry.getKey();
            }
        }
        return Condition.values()[0];
    }

    private String updateName() {
        return updateName(fieldField.getText(), getSelectedCondition());
    }

    private String updateName(String fieldText) {
        return updateName(fieldText, getSelectedCondition());
    }

    private String updateName(Condition condition) {
        return updateName(fieldField.getText(), condition);
    }

    private String updateName(String fieldText, Condition condition) {
        StringBuilder builder = new StringBuilder("Accept message if \"").append(fieldText).append("\" ");

        if (condition.isValuesEnabled()) {
            boolean first = true;
            for (String value : getValues()) {
                if (StringUtils.isNotBlank(value)) {
                    if (first) {
                        builder.append(condition.getPresentTense()).append(' ');
                    } else {
                        builder.append(" or ");
                    }
                    builder.append(value);
                    first = false;
                }
            }

            if (first) {
                if (condition == Condition.EQUALS) {
                    builder.append("is blank");
                } else if (condition == Condition.NOT_EQUAL) {
                    builder.append("is not blank");
                } else {
                    builder.append(condition.getPresentTense()).append(" \"\"");
                }
            }
        } else {
            builder.append(condition.getPresentTense());
        }

        String name = builder.toString();

        for (ActionListener actionListener : nameActionListeners) {
            actionListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, name));
        }

        return name;
    }

    private void deleteButtonActionPerformed(ActionEvent evt) {
        int selectedRow = getSelectedRow();
        if (selectedRow >= 0 && !valuesTable.isEditing()) {
            ((RefreshTableModel) valuesTable.getModel()).removeRow(valuesTable.convertRowIndexToModel(selectedRow));

            if (valuesTable.getRowCount() > 0) {
                if (selectedRow < valuesTable.getRowCount()) {
                    valuesTable.setRowSelectionInterval(selectedRow, selectedRow);
                } else {
                    valuesTable.setRowSelectionInterval(valuesTable.getRowCount() - 1, valuesTable.getRowCount() - 1);
                }
            }
        }
    }

    private void newButtonActionPerformed(ActionEvent evt) {
        ((RefreshTableModel) valuesTable.getModel()).addRow(new Object[] { "" });
        valuesTable.setRowSelectionInterval(valuesTable.getRowCount() - 1, valuesTable.getRowCount() - 1);
    }

    private JLabel behaviorLabel;
    private JLabel acceptLabel;
    private JLabel fieldLabel;
    private JTextField fieldField;
    private JLabel conditionLabel;
    private Map<Condition, JRadioButton> conditionRadioMap;
    private JLabel valuesLabel;
    private MirthTable valuesTable;
    private JScrollPane valuesScrollPane;
    private JButton newButton;
    private JButton deleteButton;
}
