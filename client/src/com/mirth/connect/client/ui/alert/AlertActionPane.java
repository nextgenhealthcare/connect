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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

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
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.alert.AlertAction;
import com.mirth.connect.model.alert.AlertActionGroup;
import com.mirth.connect.model.alert.AlertActionProtocol;

public class AlertActionPane extends JPanel {

    private static final int PROTOCOL_COLUMN_INDEX = 0;
    private static final int RECIPIENT_COLUMN_INDEX = 1;
    private static final String PROTOCOL_COLUMN_NAME = "Protocol";
    private static final String RECIPIENT_COLUMN_NAME = "Recipient";
    private static int PROTOCOL_COLUMN_WIDTH = 65;

    private Map<String, String> channelMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

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

        actionTable.getColumnExt(RECIPIENT_COLUMN_INDEX).setCellRenderer(new RecipientCellRenderer(new DefaultTableCellRenderer(), new MirthComboBoxTableCellRenderer(new Object[] {})));

        TableCellEditor emailCellEditor = new TextFieldCellEditor() {
            @Override
            protected boolean valueChanged(String value) {
                PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
                return true;
            }
        };

        actionTable.getColumnExt(RECIPIENT_COLUMN_NAME).setCellEditor(new RecipientCellEditor(emailCellEditor, new MirthComboBoxTableCellEditor(actionTable, new Object[] {}, 1, false, null)));

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
        // Make sure there aren't any cell editors still active
        TableCellEditor cellEditor = actionTable.getCellEditor();

        if (cellEditor != null) {
            cellEditor.stopCellEditing();
        }

        // Clear old name->id map values
        channelMap.clear();

        // An id->name map is needed locally
        Map<String, String> channelNameMap = new HashMap<String, String>();
        
        if (PlatformUI.MIRTH_FRAME.channels != null) {
            for (Channel channel : PlatformUI.MIRTH_FRAME.channels.values()) {
                // Sort the channels by channel name
                channelMap.put(channel.getName(), channel.getId());
                channelNameMap.put(channel.getId(), channel.getName());
            }
        }

        // Update the recipient column renderer and editor with the current channel names
        ((RecipientCellRenderer) actionTable.getColumnExt(RECIPIENT_COLUMN_INDEX).getCellRenderer()).setChannelCellRenderer(new MirthComboBoxTableCellRenderer(channelMap.keySet().toArray()));
        ((RecipientCellEditor) actionTable.getColumnExt(RECIPIENT_COLUMN_NAME).getCellEditor()).setChannelCellEditor(new MirthComboBoxTableCellEditor(actionTable, channelMap.keySet().toArray(), 1, false, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox comboBox = (JComboBox) e.getSource();
                if (comboBox.isPopupVisible() && actionTable.getCellEditor() != null) {
                    actionTable.getCellEditor().stopCellEditing();
                    PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
                }
            }

        }));

        // Generate the channel name values for the model
        List<String> channelNames = new ArrayList<String>();
        for (AlertAction action : alertActions) {
            String channelName = null;
            if (action.getProtocol() == AlertActionProtocol.CHANNEL && channelNameMap.containsKey(action.getRecipient())) {
                channelName = channelNameMap.get(action.getRecipient());
            }

            channelNames.add(channelName);
        }

        ActionTableModel model = (ActionTableModel) actionTable.getModel();
        model.refreshData(alertActions, channelNames);
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

    private class RecipientCellRenderer implements TableCellRenderer {

        public TableCellRenderer emailCellRenderer;
        public TableCellRenderer channelCellRenderer;
        private AlertActionProtocol protocol;

        public RecipientCellRenderer(TableCellRenderer emailCellRenderer, TableCellRenderer channelCellRenderer) {
            this.emailCellRenderer = emailCellRenderer;
            this.channelCellRenderer = channelCellRenderer;
        }

        public void setChannelCellRenderer(TableCellRenderer channelCellRenderer) {
            this.channelCellRenderer = channelCellRenderer;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            protocol = (AlertActionProtocol) table.getValueAt(row, PROTOCOL_COLUMN_INDEX);

            if (protocol == AlertActionProtocol.EMAIL) {
                return emailCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            } else if (protocol == AlertActionProtocol.CHANNEL) {
                return channelCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }

            return null;
        }

    }

    private class RecipientCellEditor extends AbstractCellEditor implements TableCellEditor {

        private TableCellEditor emailCellEditor;
        private TableCellEditor channelCellEditor;
        private AlertActionProtocol protocol;

        public RecipientCellEditor(TableCellEditor emailCellEditor, TableCellEditor channelCellEditor) {
            this.emailCellEditor = emailCellEditor;
            this.channelCellEditor = channelCellEditor;
        }

        public void setChannelCellEditor(TableCellEditor channelCellEditor) {
            this.channelCellEditor = channelCellEditor;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            protocol = (AlertActionProtocol) table.getValueAt(row, PROTOCOL_COLUMN_INDEX);

            if (protocol == AlertActionProtocol.EMAIL) {
                return emailCellEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
            } else if (protocol == AlertActionProtocol.CHANNEL) {
                return channelCellEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
            }

            return null;
        }

        @Override
        public Object getCellEditorValue() {
            if (protocol == AlertActionProtocol.EMAIL) {
                return emailCellEditor.getCellEditorValue();
            } else if (protocol == AlertActionProtocol.CHANNEL) {
                return channelCellEditor.getCellEditorValue();
            }

            return null;
        }

        @Override
        public boolean stopCellEditing() {
            if (protocol == AlertActionProtocol.EMAIL) {
                emailCellEditor.stopCellEditing();
            } else if (protocol == AlertActionProtocol.CHANNEL) {
                channelCellEditor.stopCellEditing();
            }

            return super.stopCellEditing();
        }
    }

    private class ActionTableModel extends AbstractTableModel {

        private List<AlertAction> actions = new ArrayList<AlertAction>();
        private List<String> channelNames = new ArrayList<String>();
        private String[] columns = new String[] { PROTOCOL_COLUMN_NAME, RECIPIENT_COLUMN_NAME };

        public ActionTableModel() {

        }

        public void refreshData(List<AlertAction> actions, List<String> channelNames) {
            this.actions = actions;
            this.channelNames = channelNames;
            fireTableDataChanged();
        }

        public void addRow(AlertAction action) {
            int row = actions.size();
            actions.add(action);
            channelNames.add(null);
            fireTableRowsInserted(row, row);
        }

        public void removeRow(int rowIndex) {
            actions.remove(rowIndex);
            channelNames.remove(rowIndex);
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
                case PROTOCOL_COLUMN_INDEX:
                    return action.getProtocol();
                case RECIPIENT_COLUMN_INDEX:
                    String recipient = null;
                    if (action.getProtocol() == AlertActionProtocol.EMAIL) {
                        recipient = action.getRecipient();
                    } else if (action.getProtocol() == AlertActionProtocol.CHANNEL) {
                        recipient = channelNames.get(rowIndex);
                    }
                    return recipient;
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            AlertAction action = actions.get(rowIndex);

            switch (columnIndex) {
                case PROTOCOL_COLUMN_INDEX:
                    AlertActionProtocol protocol = (AlertActionProtocol) aValue;
                    if (!protocol.equals(action.getProtocol())) {
                        action.setProtocol(protocol);
                        action.setRecipient(null);
                        channelNames.set(rowIndex, null);
                    }
                    break;
                case RECIPIENT_COLUMN_INDEX:
                    if (action.getProtocol() == AlertActionProtocol.EMAIL) {
                        action.setRecipient((String) aValue);
                    } else if (action.getProtocol() == AlertActionProtocol.CHANNEL) {
                        String channelName = (String) aValue;
                        channelNames.set(rowIndex, channelName);
                        action.setRecipient(channelName == null ? null : channelMap.get(channelName));
                    }
                    break;
                default:
                    break;
            }

            fireTableRowsUpdated(rowIndex, rowIndex);
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
                if (actionTable.getSelectedModelIndex() != -1) {
                    if (actionTable.isEditing()) {
                        actionTable.getCellEditor().stopCellEditing();
                    }

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
            public void changedUpdate(DocumentEvent e) {}
            
        });

        subjectPane = new JPanel();
        subjectPane.setBackground(UIConstants.BACKGROUND_COLOR);
        subjectPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Subject (only used for email messages)"));
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
            public void changedUpdate(DocumentEvent e) {}
            
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
