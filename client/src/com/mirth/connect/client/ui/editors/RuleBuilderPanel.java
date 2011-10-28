/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.editors;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.TextFieldCellEditor;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.plugins.rulebuilder.RuleBuilderPlugin;

public class RuleBuilderPanel extends BasePanel {

    protected String label;
    protected MirthEditorPane parent;
    public final int VALUE_COLUMN = 0;
    public final String VALUE_COLUMN_NAME = "Value";
    private final RuleBuilderPlugin rulePlugin;
    private int lastIndex = -1;
    private String name = "";
    private String originalField = "";

    /** Creates new form MapperPanel */
    public RuleBuilderPanel(MirthEditorPane p, final RuleBuilderPlugin rulePlugin) {
        parent = p;
        this.rulePlugin = rulePlugin;
        initComponents();


        fieldTextField.getDocument().addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent arg0) {
                parent.modified = true;
                rulePlugin.updateName();
            }

            public void insertUpdate(DocumentEvent arg0) {
                parent.modified = true;
                rulePlugin.updateName();
            }

            public void removeUpdate(DocumentEvent arg0) {
                parent.modified = true;
                rulePlugin.updateName();
            }
        });

        doesNotEqual.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parent.modified = true;
                rulePlugin.updateName();
            }
        });
        doesNotExist.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parent.modified = true;

                rulePlugin.updateName();
            }
        });
        equals.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parent.modified = true;
                rulePlugin.updateName();
            }
        });
        exists.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parent.modified = true;
                rulePlugin.updateName();
            }
        });
        contains.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parent.modified = true;
                rulePlugin.updateName();
            }
        });
        doesNotContain.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parent.modified = true;
                rulePlugin.updateName();
            }
        });


        valuesScrollPane.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deselectRows();
            }
        });
        deleteButton.setEnabled(false);

    }

    public void updateTable() {
        if (parent.getSelectedRow() != -1 && !parent.getTableModel().getValueAt(parent.getSelectedRow(), parent.STEP_TYPE_COL).toString().equals("JavaScript")) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    parent.getTableModel().setValueAt(rulePlugin.getStepName(), parent.getSelectedRow(), parent.STEP_NAME_COL);
                    parent.updateTaskPane(parent.getTableModel().getValueAt(parent.getSelectedRow(), parent.STEP_TYPE_COL).toString());
                }
            });
        }
    }

    public Map<Object, Object> getData() {
        Map<Object, Object> m = new HashMap<Object, Object>();
        m.put("Field", fieldTextField.getText().trim());
        m.put("Name", name);
        m.put("OriginalField", originalField);

        if (equals.isSelected()) {
            m.put("Equals", UIConstants.YES_OPTION);
        } else if (doesNotEqual.isSelected()) {
            m.put("Equals", UIConstants.NO_OPTION);
        } else if (exists.isSelected()) {
            m.put("Equals", UIConstants.EXISTS_OPTION);
        } else if (doesNotExist.isSelected()) {
            m.put("Equals", UIConstants.DOES_NOT_EXIST_OPTION);
        } else if (contains.isSelected()) {
            m.put("Equals", UIConstants.CONTAINS_OPTION);
        } else if (doesNotContain.isSelected()) {
            m.put("Equals", UIConstants.DOES_NOT_CONTAIN_OPTION);
        }

        m.put("Values", getValues());

        return m;
    }

    public void setData(Map<Object, Object> data) {
        boolean modified = parent.modified;

        // Must set the text last so that the text field change action is
        // not called before the new button values are set.
        if (data != null) {
            if (((String) data.get("Equals")).equals(UIConstants.YES_OPTION)) {
                equalsActionPerformed(null);
                equals.setSelected(true);
            } else if (((String) data.get("Equals")).equals(UIConstants.NO_OPTION)) {
                doesNotEqualActionPerformed(null);
                doesNotEqual.setSelected(true);
            } else if (((String) data.get("Equals")).equals(UIConstants.EXISTS_OPTION)) {
                existsActionPerformed(null);
                exists.setSelected(true);
            } else if (((String) data.get("Equals")).equals(UIConstants.DOES_NOT_EXIST_OPTION)) {
                doesNotExistActionPerformed(null);
                doesNotExist.setSelected(true);
            } else if (((String) data.get("Equals")).equals(UIConstants.CONTAINS_OPTION)) {
                containsActionPerformed(null);
                contains.setSelected(true);
            } else if (((String) data.get("Equals")).equals(UIConstants.DOES_NOT_CONTAIN_OPTION)) {
                doesNotContainActionPerformed(null);
                doesNotContain.setSelected(true);
            }

            ArrayList<String> values = (ArrayList<String>) data.get("Values");
            if (values != null) {
                setValues(values);
            } else {
                setValues(new ArrayList<String>());
            }

            originalField = (String) data.get("OriginalField");
            name = (String) data.get("Name");
            fieldTextField.setText((String) data.get("Field"));
        } else {
            equals.setSelected(true);
            ArrayList<String> values = new ArrayList<String>();
            values.add("\"Example Value\"");
            setValues(values);
            fieldTextField.setText("");
        }

        parent.modified = modified;
    }

    public void setValues(ArrayList<String> values) {
        Object[][] tableData = new Object[values.size()][1];

        valuesTable = new MirthTable();

        for (int i = 0; i < values.size(); i++) {
            tableData[i][VALUE_COLUMN] = values.get(i);
        }

        valuesTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[]{VALUE_COLUMN_NAME}) {

            boolean[] canEdit = new boolean[]{true};

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        valuesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                if (getSelectedRow() != -1) {
                    lastIndex = getSelectedRow();
                    deleteButton.setEnabled(true);
                } else {
                    deleteButton.setEnabled(false);
                }

                rulePlugin.updateName();
            }
        });

        class RegExTableCellEditor extends TextFieldCellEditor {

            public boolean stopCellEditing() {
                parent.modified = true;
                deleteButton.setEnabled(true);
                boolean result = super.stopCellEditing();
                
                /*
                 * HACK: Cannot move this logic into valueChanged. We need to
                 * update the name after stopping cell editing, because the
                 * swingx update caused the ListSelectionListener to stop being
                 * called when there was only one row and it was edited.
                 */
                rulePlugin.updateName();

                return result;
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

        valuesScrollPane.setViewportView(valuesTable);
    }

    public ArrayList<String> getValues() {
        ArrayList<String> values = new ArrayList<String>();

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
        valuesScrollPane.setEnabled(enabled);
        valuesTable.setEnabled(enabled);
        valuesLabel.setEnabled(enabled);
        newButton.setEnabled(enabled);

        deselectRows();

        parent.modified = true;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        valuesScrollPane = new javax.swing.JScrollPane();
        valuesTable = new com.mirth.connect.client.ui.components.MirthTable();
        newButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        valuesLabel = new javax.swing.JLabel();
        fieldTextField = new javax.swing.JTextField();
        equals = new javax.swing.JRadioButton();
        doesNotEqual = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        exists = new javax.swing.JRadioButton();
        doesNotExist = new javax.swing.JRadioButton();
        acceptLabel = new javax.swing.JLabel();
        contains = new javax.swing.JRadioButton();
        doesNotContain = new javax.swing.JRadioButton();

        setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setText("Field:");

        valuesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Value"
            }
        ));
        valuesScrollPane.setViewportView(valuesTable);

        newButton.setText("New");
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });

        deleteButton.setText("Delete");
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        valuesLabel.setText("Values:");

        equals.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup1.add(equals);
        equals.setText("Equals");
        equals.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        equals.setMargin(new java.awt.Insets(0, 0, 0, 0));
        equals.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                equalsActionPerformed(evt);
            }
        });

        doesNotEqual.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup1.add(doesNotEqual);
        doesNotEqual.setText("Not Equal");
        doesNotEqual.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        doesNotEqual.setMargin(new java.awt.Insets(0, 0, 0, 0));
        doesNotEqual.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doesNotEqualActionPerformed(evt);
            }
        });

        jLabel2.setText("Condition:");

        jLabel3.setText("Behavior:");

        exists.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup1.add(exists);
        exists.setText("Exists");
        exists.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        exists.setMargin(new java.awt.Insets(0, 0, 0, 0));
        exists.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                existsActionPerformed(evt);
            }
        });

        doesNotExist.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup1.add(doesNotExist);
        doesNotExist.setText("Not Exist");
        doesNotExist.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        doesNotExist.setMargin(new java.awt.Insets(0, 0, 0, 0));
        doesNotExist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doesNotExistActionPerformed(evt);
            }
        });

        acceptLabel.setText("Accept");

        contains.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup1.add(contains);
        contains.setText("Contains");
        contains.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        contains.setMargin(new java.awt.Insets(0, 0, 0, 0));
        contains.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                containsActionPerformed(evt);
            }
        });

        doesNotContain.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroup1.add(doesNotContain);
        doesNotContain.setText("Not Contain");
        doesNotContain.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        doesNotContain.setMargin(new java.awt.Insets(0, 0, 0, 0));
        doesNotContain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doesNotContainActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(valuesLabel)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(exists)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(doesNotExist)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(equals)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(doesNotEqual)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(contains)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(doesNotContain))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(valuesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(newButton)
                            .addComponent(deleteButton)))
                    .addComponent(fieldTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
                    .addComponent(acceptLabel))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {deleteButton, newButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(acceptLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(fieldTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(equals)
                    .addComponent(doesNotEqual)
                    .addComponent(jLabel2)
                    .addComponent(exists)
                    .addComponent(doesNotExist)
                    .addComponent(contains)
                    .addComponent(doesNotContain))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(newButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteButton))
                    .addComponent(valuesLabel)
                    .addComponent(valuesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void doesNotExistActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_doesNotExistActionPerformed
    {//GEN-HEADEREND:event_doesNotExistActionPerformed
        setValuesEnabled(false);
    }//GEN-LAST:event_doesNotExistActionPerformed

    private void doesNotEqualActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_doesNotEqualActionPerformed
    {//GEN-HEADEREND:event_doesNotEqualActionPerformed
        setValuesEnabled(true);
    }//GEN-LAST:event_doesNotEqualActionPerformed

    private void equalsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_equalsActionPerformed
    {//GEN-HEADEREND:event_equalsActionPerformed
        setValuesEnabled(true);
    }//GEN-LAST:event_equalsActionPerformed

    private void existsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_existsActionPerformed
    {//GEN-HEADEREND:event_existsActionPerformed
        setValuesEnabled(false);
    }//GEN-LAST:event_existsActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_deleteButtonActionPerformed
    {//GEN-HEADEREND:event_deleteButtonActionPerformed
        if (getSelectedRow() != -1 && !valuesTable.isEditing()) {
            ((DefaultTableModel) valuesTable.getModel()).removeRow(getSelectedRow());

            if (valuesTable.getRowCount() != 0) {
                if (lastIndex == 0) {
                    valuesTable.setRowSelectionInterval(0, 0);
                } else if (lastIndex == valuesTable.getRowCount()) {
                    valuesTable.setRowSelectionInterval(lastIndex - 1, lastIndex - 1);
                } else {
                    valuesTable.setRowSelectionInterval(lastIndex, lastIndex);
                }
            }
            rulePlugin.updateName();
            parent.modified = true;
        }
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_newButtonActionPerformed
    {//GEN-HEADEREND:event_newButtonActionPerformed
        ((DefaultTableModel) valuesTable.getModel()).addRow(new Object[]{"", ""});
        valuesTable.setRowSelectionInterval(valuesTable.getRowCount() - 1, valuesTable.getRowCount() - 1);
        rulePlugin.updateName();
        parent.modified = true;
    }//GEN-LAST:event_newButtonActionPerformed

    private void containsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_containsActionPerformed
        setValuesEnabled(true);
    }//GEN-LAST:event_containsActionPerformed

    private void doesNotContainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doesNotContainActionPerformed
        setValuesEnabled(true);
    }//GEN-LAST:event_doesNotContainActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel acceptLabel;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JRadioButton contains;
    private javax.swing.JButton deleteButton;
    private javax.swing.JRadioButton doesNotContain;
    private javax.swing.JRadioButton doesNotEqual;
    private javax.swing.JRadioButton doesNotExist;
    private javax.swing.JRadioButton equals;
    private javax.swing.JRadioButton exists;
    private javax.swing.JTextField fieldTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JButton newButton;
    private javax.swing.JLabel valuesLabel;
    private javax.swing.JScrollPane valuesScrollPane;
    private com.mirth.connect.client.ui.components.MirthTable valuesTable;
    // End of variables declaration//GEN-END:variables
}
