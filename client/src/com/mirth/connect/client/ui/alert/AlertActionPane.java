/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.alert;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
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

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.core.ClientException;
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

public class AlertActionPane extends JPanel {

    private static final int PROTOCOL_COLUMN_INDEX = 0;
    private static final int RECIPIENT_COLUMN_INDEX = 1;
    private static final String PROTOCOL_COLUMN_NAME = "Protocol";
    private static final String RECIPIENT_COLUMN_NAME = "Recipient";
    private static final String DEFAULT_PROTOCOL = "Email";
    private static int PROTOCOL_COLUMN_WIDTH = 65;

    private volatile Map<String, Protocol> protocols = new LinkedHashMap<String, AlertActionPane.Protocol>();
    private AlertActionGroup actionGroup;

    public AlertActionPane() {
        initComponents();

        try {
            updateProtocols(PlatformUI.MIRTH_FRAME.mirthClient.getAlertProtocolOptions());
        } catch (ClientException e) {
            updateProtocols(new HashMap<String, Map<String, String>>());
            PlatformUI.MIRTH_FRAME.alertThrowable(PlatformUI.MIRTH_FRAME, e, "An error occurred while attempting to initialize the alert editor.", false);
        }

        makeActionTable();
    }

    private void makeActionTable() {
        actionTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        actionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        actionTable.getColumnExt(PROTOCOL_COLUMN_NAME).setCellEditor(new MirthComboBoxTableCellEditor(actionTable, protocols.keySet().toArray(), 1, false, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox comboBox = (JComboBox) e.getSource();
                if (comboBox.isPopupVisible()) {
                    PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
                }
            }

        }));

        actionTable.getColumnExt(PROTOCOL_COLUMN_NAME).setCellRenderer(new MirthComboBoxTableCellRenderer(protocols.keySet().toArray()));

        actionTable.getColumnExt(RECIPIENT_COLUMN_INDEX).setCellRenderer(new RecipientCellRenderer());

        actionTable.getColumnExt(RECIPIENT_COLUMN_NAME).setCellEditor(new RecipientCellEditor());

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

        for (Protocol protocol : protocols.values()) {
            if (protocol.hasOptions()) {
                MirthComboBoxTableCellEditor editor = new MirthComboBoxTableCellEditor(actionTable, protocol.getRecipientNames().toArray(), 1, true, new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JComboBox comboBox = (JComboBox) e.getSource();
                        if (comboBox.isPopupVisible() && actionTable.getCellEditor() != null) {
                            actionTable.getCellEditor().stopCellEditing();
                        }
                        PlatformUI.MIRTH_FRAME.setSaveEnabled(true);

                    }

                });

                editor.setEditable(true);
                editor.getComboBox().setCanEnableSave(false);
                editor.getComboBox().setAutoResizeDropdown(true);

                MirthComboBoxTableCellRenderer renderer = new MirthComboBoxTableCellRenderer(protocol.getRecipientNames().toArray());
                renderer.setEditable(true);

                // Update the recipient column renderer and editor with the current names
                ((RecipientCellRenderer) actionTable.getColumnExt(RECIPIENT_COLUMN_INDEX).getCellRenderer()).setCellRenderer(protocol.getProtocolName(), renderer);
                ((RecipientCellEditor) actionTable.getColumnExt(RECIPIENT_COLUMN_NAME).getCellEditor()).setCellEditor(protocol.getProtocolName(), editor);
            }
        }

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

    public void setActionGroup(AlertActionGroup actionGroup, Map<String, Map<String, String>> protocolOptions) {
        this.actionGroup = actionGroup;
        updateProtocols(protocolOptions);

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

        private Map<String, TableCellRenderer> cellRenderers = new HashMap<String, TableCellRenderer>();
        private String protocol;

        public RecipientCellRenderer() {
            for (Protocol protocol : protocols.values()) {
                if (protocol.hasOptions()) {
                    MirthComboBoxTableCellRenderer renderer = new MirthComboBoxTableCellRenderer(new Object[] {});
                    renderer.setEditable(true);

                    cellRenderers.put(protocol.getProtocolName(), renderer);
                } else {
                    cellRenderers.put(protocol.getProtocolName(), new DefaultTableCellRenderer());
                }
            }
        }

        public void setCellRenderer(String protocol, TableCellRenderer cellRenderer) {
            cellRenderers.put(protocol, cellRenderer);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            protocol = (String) table.getValueAt(row, PROTOCOL_COLUMN_INDEX);
            return cellRenderers.get(protocol).getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }

    }

    private class RecipientCellEditor extends AbstractCellEditor implements TableCellEditor {

        private Map<String, TableCellEditor> cellEditors = new HashMap<String, TableCellEditor>();
        private String protocol;

        @SuppressWarnings("serial")
        public RecipientCellEditor() {
            for (Protocol protocol : protocols.values()) {
                if (protocol.hasOptions()) {
                    MirthComboBoxTableCellEditor editor = new MirthComboBoxTableCellEditor(actionTable, new Object[] {}, 1, false, null);
                    editor.setEditable(true);

                    cellEditors.put(protocol.getProtocolName(), editor);
                } else {
                    cellEditors.put(protocol.getProtocolName(), new TextFieldCellEditor() {
                        @Override
                        protected boolean valueChanged(String value) {
                            PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
                            return true;
                        }
                    });
                }
            }
        }

        public void setCellEditor(String protocol, TableCellEditor cellEditor) {
            cellEditors.put(protocol, cellEditor);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            protocol = (String) table.getValueAt(row, PROTOCOL_COLUMN_INDEX);
            return cellEditors.get(protocol).getTableCellEditorComponent(table, value, isSelected, row, column);
        }

        @Override
        public Object getCellEditorValue() {
            return cellEditors.get(protocol).getCellEditorValue();
        }

        @Override
        public boolean stopCellEditing() {
            cellEditors.get(protocol).stopCellEditing();
            return super.stopCellEditing();
        }
    }

    private class ActionTableModel extends AbstractTableModel {

        private List<AlertAction> actions = new ArrayList<AlertAction>();
        private String[] columns = new String[] { PROTOCOL_COLUMN_NAME, RECIPIENT_COLUMN_NAME };

        public ActionTableModel() {

        }

        public void refreshData(List<AlertAction> actions) {
            /*
             * Remove any actions that belong to a protocol that is not currently installed (for
             * example: if the the user role plugin were to be uninstalled). This will modify the
             * AlertModel, so the next time the user saves the alert, these alert actions will be
             * permanently removed.
             */
            for (Iterator<AlertAction> iterator = actions.iterator(); iterator.hasNext();) {
                if (!protocols.containsKey(iterator.next().getProtocol())) {
                    iterator.remove();
                }
            }

            this.actions = actions;
            fireTableDataChanged();
        }

        public void addRow(AlertAction action) {
            // Skip if the action's protocol is not currently installed
            if (protocols.containsKey(action.getProtocol())) {
                int row = actions.size();
                actions.add(action);
                fireTableRowsInserted(row, row);
            }
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
                case PROTOCOL_COLUMN_INDEX:
                    return action.getProtocol();
                case RECIPIENT_COLUMN_INDEX:
                    return protocols.get(action.getProtocol()).getRecipientNameFromId(action.getRecipient());
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            AlertAction action = actions.get(rowIndex);

            switch (columnIndex) {
                case PROTOCOL_COLUMN_INDEX:
                    action.setProtocol((String) aValue);
                    action.setRecipient(null);
                    break;
                case RECIPIENT_COLUMN_INDEX:
                    String recipient = protocols.get(action.getProtocol()).getRecipientIdFromName((String) aValue);

                    if (StringUtils.isBlank(recipient)) {
                        recipient = (String) aValue;
                    }

                    action.setRecipient(recipient);
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
                ((ActionTableModel) actionTable.getModel()).addRow(new AlertAction(DEFAULT_PROTOCOL, ""));

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

        add(actionPane, "width 280:280:280, growy, wrap");

        add(subjectPane, "split, growx");
        add(templatePane, "grow, wrap");

        add(variablePane, "width 140:140:140, growy");
    }

    private void updateProtocols(Map<String, Map<String, String>> protocolOptions) {
        protocols = new LinkedHashMap<String, AlertActionPane.Protocol>();

        for (Entry<String, Map<String, String>> entry : protocolOptions.entrySet()) {
            protocols.put(entry.getKey(), new Protocol(entry.getKey(), entry.getValue()));
        }
    }

    private class Protocol {
        private String protocolName;
        private Map<String, String> options;
        private Map<String, String> ids;

        public Protocol(String protocolName, Map<String, String> options) {
            this.protocolName = protocolName;

            if (options != null) {
                this.options = options;
                ids = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

                for (Entry<String, String> entry : options.entrySet()) {
                    ids.put(entry.getValue(), entry.getKey());
                }
            }
        }

        String getProtocolName() {
            return protocolName;
        }

        boolean hasOptions() {
            return options != null;
        }

        Set<String> getRecipientNames() {
            if (options == null) {
                return null;
            } else {
                Set<String> names = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
                names.addAll(options.values());
                return names;
            }
        }

        String getRecipientIdFromName(String name) {
            if (name == null) {
                return null;
            } else if (options == null) {
                return name;
            } else {
                return ids.get(name);
            }
        }

        String getRecipientNameFromId(String id) {
            if (options == null || !options.containsKey(id)) {
                return id;
            } else {
                return options.get(id);
            }
        }
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
