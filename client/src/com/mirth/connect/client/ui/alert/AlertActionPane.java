/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.alert;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.TextFieldCellEditor;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthButton;
import com.mirth.connect.client.ui.components.MirthComboBoxTableCellEditor;
import com.mirth.connect.client.ui.components.MirthComboBoxTableCellRenderer;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.components.MirthTextArea;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.client.ui.components.MirthVariableList;
import com.mirth.connect.model.alert.AlertAction;
import com.mirth.connect.model.alert.AlertActionGroup;
import com.mirth.connect.model.alert.AlertActionProtocol;

public class AlertActionPane extends JPanel {

    private static String PROTOCOL_COLUMN_NAME = "Protocol";
    private static String RECIPIENT_COLUMN_NAME = "Recipient";
    private static int PROTOCOL_COLUMN_WIDTH = 65;
    
    private AlertActionGroup actionGroup;

    public AlertActionPane() {
        initComponents();

        makeActionTable();
    }

    private void makeActionTable() {
        actionTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        actionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        actionTable.getColumnExt(PROTOCOL_COLUMN_NAME).setCellEditor(new MirthComboBoxTableCellEditor(actionTable, AlertActionProtocol.values(), 1, false, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox comboBox = (JComboBox) e.getSource();
                if (comboBox.isPopupVisible()) {
                    PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
                }
            }

        }));

        actionTable.getColumnExt(PROTOCOL_COLUMN_NAME).setCellRenderer(new MirthComboBoxTableCellRenderer(AlertActionProtocol.values()));

        actionTable.getColumnExt(RECIPIENT_COLUMN_NAME).setCellEditor(new TextFieldCellEditor() {

            @Override
            public boolean isCellEditable(EventObject evt) {
                boolean editable = super.isCellEditable(evt);

                if (editable) {
                    removeActionButton.setEnabled(false);
                }

                return editable;
            }

            @Override
            protected boolean valueChanged(String value) {
                PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
                removeActionButton.setEnabled(true);
                return true;
            }
        });

        actionTable.setRowHeight(UIConstants.ROW_HEIGHT);
        actionTable.setSortable(false);
        actionTable.setOpaque(true);
        actionTable.setDragEnabled(false);
        actionTable.getTableHeader().setReorderingAllowed(false);
        actionTable.setShowGrid(true, true);
        actionTable.setAutoCreateColumnsFromModel(false);

        actionTable.getColumnExt(PROTOCOL_COLUMN_NAME).setMaxWidth(PROTOCOL_COLUMN_WIDTH);
        actionTable.getColumnExt(PROTOCOL_COLUMN_NAME).setMinWidth(PROTOCOL_COLUMN_WIDTH);
        actionTable.getColumnExt(PROTOCOL_COLUMN_NAME).setResizable(false);

        actionTable.getColumnExt(RECIPIENT_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);
        actionTable.getColumnExt(RECIPIENT_COLUMN_NAME).setResizable(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            actionTable.setHighlighters(highlighter);
        }
    }

    private void updateActionTable(List<AlertAction> alertActions) {
        ActionTableModel model = (ActionTableModel) actionTable.getModel();
        model.refreshData(alertActions);
        if (model.getRowCount() == 0) {
            actionTable.clearSelection();
            removeActionButton.setEnabled(false);
        } else {
            actionTable.getSelectionModel().setSelectionInterval(0, 0);
            removeActionButton.setEnabled(true);
        }
    }

    public AlertActionGroup getActionGroup() {
        return actionGroup;
    }

    public void setActionGroup(AlertActionGroup actionGroup) {
        this.actionGroup = actionGroup;
        
        subjectTextField.setText(this.actionGroup.getSubject());
        templateTextArea.setText(this.actionGroup.getTemplate());

        updateActionTable(this.actionGroup.getActions());
    }
    
    public void setVariableList(List<String> variables) {
        variableList.removeAll();
        variableList.setListData(variables.toArray());
        variableScrollPane.setViewportView(variableList);
    }
    
    private class ActionTableModel extends AbstractTableModel {
        
        private List<AlertAction> actions = new ArrayList<AlertAction>();
        private String[] columns = new String[] { "Protocol", "Recipient" };
        
        public ActionTableModel() {
            
        }
        
        public void refreshData(List<AlertAction> actions) {
            this.actions = actions;
            fireTableDataChanged();
        }
        
        public void addRow(AlertAction action) {
            int row = actions.size();
            actions.add(action);
            fireTableRowsInserted(row, row);
        }
        
        public void removeRow(int rowIndex) {
            actions.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }

        @Override
        public int getRowCount() {
            return actions.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return columns[columnIndex];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 1:
                    return String.class;
                case 2:
                    return String.class;
                default:
                    return Object.class;
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            AlertAction action = actions.get(rowIndex);
            
            switch (columnIndex) {
                case 0:
                    return action.getProtocol();
                case 1:
                    return action.getRecipient();
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            AlertAction action = actions.get(rowIndex);
            
            switch (columnIndex) {
                case 0:
                    action.setProtocol((AlertActionProtocol) aValue);
                    break;
                case 1:
                    action.setRecipient((String) aValue);
                    break;
                default:
                    break;
            }
            
            fireTableCellUpdated(rowIndex, columnIndex);
        }

    }

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);
        setLayout(new MigLayout("insets 0, flowy", "[][grow][]", "grow"));

        actionTable = new MirthTable();
        actionTable.setModel(new ActionTableModel());
        actionScrollPane = new JScrollPane(actionTable);
        actionScrollPane.setBackground(UIConstants.BACKGROUND_COLOR);

        addActionButton = new MirthButton("Add");
        addActionButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ((ActionTableModel) actionTable.getModel()).addRow(new AlertAction(AlertActionProtocol.EMAIL, ""));

                if (actionTable.getRowCount() == 1) {
                    actionTable.setRowSelectionInterval(0, 0);
                }

                removeActionButton.setEnabled(true);
                PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
            }

        });
        removeActionButton = new MirthButton("Remove");
        removeActionButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (actionTable.getSelectedModelIndex() != -1 && !actionTable.isEditing()) {
                    ActionTableModel model = (ActionTableModel) actionTable.getModel();

                    int selectedModelIndex = actionTable.getSelectedModelIndex();
                    int newViewIndex = actionTable.convertRowIndexToView(selectedModelIndex);
                    if (newViewIndex == (model.getRowCount() - 1)) {
                        newViewIndex--;
                    }

                    // must set lastModelRow to -1 so that when setting the new
                    // row selection below the old data won't try to be saved.
                    // lastEmailRow = -1;
                    model.removeRow(selectedModelIndex);

                    if (actionTable.getModel().getRowCount() != 0) {
                        actionTable.setRowSelectionInterval(newViewIndex, newViewIndex);
                    } else {
                        removeActionButton.setEnabled(false);
                    }

                    PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
                }
            }

        });

        actionPane = new JPanel();
        actionPane.setBackground(UIConstants.BACKGROUND_COLOR);
        actionPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Actions"));
        actionPane.setLayout(new MigLayout("insets 0, flowy", "[grow][]", "grow"));
        actionPane.add(actionScrollPane, "grow, wrap");
        actionPane.add(addActionButton, "aligny top, growx, split");
        actionPane.add(removeActionButton, "growx");

        subjectTextField = new MirthTextField();
        subjectTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                actionGroup.setSubject(subjectTextField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actionGroup.setSubject(subjectTextField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // TODO Auto-generated method stub
                
            }
            
        });

        subjectPane = new JPanel();
        subjectPane.setBackground(UIConstants.BACKGROUND_COLOR);
        subjectPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Subject (Only used for email messages)"));
        subjectPane.setLayout(new BorderLayout());
        subjectPane.add(subjectTextField);

        templateTextArea = new MirthTextArea();
        templateTextArea.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                actionGroup.setTemplate(templateTextArea.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actionGroup.setTemplate(templateTextArea.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // TODO Auto-generated method stub
                
            }
            
        });
        
        templateScrollPane = new JScrollPane(templateTextArea);
        templateScrollPane.setBackground(UIConstants.BACKGROUND_COLOR);

        templatePane = new JPanel();
        templatePane.setBackground(UIConstants.BACKGROUND_COLOR);
        templatePane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Template"));
        templatePane.setLayout(new BorderLayout());
        templatePane.add(templateScrollPane);

        variableList = new MirthVariableList();
        variableScrollPane = new JScrollPane(variableList);
        variableScrollPane.setBackground(UIConstants.BACKGROUND_COLOR);

        variablePane = new JPanel();
        variablePane.setBackground(UIConstants.BACKGROUND_COLOR);
        variablePane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Alert Variables"));
        variablePane.setLayout(new BorderLayout());
        variablePane.add(variableScrollPane);

        add(actionPane, "width 250:250:250, growy, wrap");

        add(subjectPane, "split, growx");
        add(templatePane, "grow, wrap");

        add(variablePane, "width 170:170:170, growy");
    }

    private JPanel actionPane;
    private JScrollPane actionScrollPane;
    private MirthTable actionTable;
    private MirthButton addActionButton;
    private MirthButton removeActionButton;
    private JPanel subjectPane;
    private MirthTextField subjectTextField;
    private JPanel templatePane;
    private JScrollPane templateScrollPane;
    private MirthTextArea templateTextArea;
    private JPanel variablePane;
    private JScrollPane variableScrollPane;
    private MirthVariableList variableList;

}
