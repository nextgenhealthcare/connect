/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.VariableListHandler.TransferMode;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.components.MirthVariableList;
import com.mirth.connect.model.Alert;
import com.mirth.connect.model.Channel;

/** The alert editor panel. */
public class AlertPanel extends javax.swing.JPanel {

    private Frame parent;
    public boolean isDeletingAlert = false;
    private boolean updatingAlertTable = false;
    private int lastAlertRow;
    private final String ALERT_NAME_COLUMN_NAME = "Name";
    private final String ALERT_STATUS_COLUMN_NAME = "Status";
    private final String APPLY_CHANNEL_NAME_COLUMN_NAME = "Channel Name";
    private final String APPLY_STATUS_COLUMN_NAME = "Applied";
    private final String APPLY_CHANNEL_ID_COLUMN_NAME = "Channel ID";
    private final String EMAIL_COLUMN_NAME = "Email Address";
    private final String ENABLED_TEXT = "Enabled";
    private final String DISABLED_TEXT = "Disabled";
    private final JPanel blankPanel;

    /**
     * Creates the Alert Editor panel. Calls initComponents() and sets up the
     * model, dropdowns, and mouse listeners.
     */
    public AlertPanel() {
        this.parent = PlatformUI.MIRTH_FRAME;
        lastAlertRow = -1;
        blankPanel = new JPanel();
        initComponents();
        errorList.setTransferMode(TransferMode.RAW);
        emailSubjectField.setDocument(new MirthFieldConstraints(998));
        makeAlertTable();
        makeApplyToChannelsTable();
        makeEmailsTable();
        setAlertErrorList();
        setAlertTemplateVariableList();
    }

    /**
     * Makes the alert table with a parameter that is true if a new alert should
     * be added as well.
     */
    public void makeAlertTable() {
        updateAlertTable();

        alertTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        // Set the custom cell editor for the Alert Name column.
        alertTable.getColumnModel().getColumn(alertTable.getColumnModelIndex(ALERT_NAME_COLUMN_NAME)).setCellEditor(new AlertTableCellEditor());
        alertTable.setCustomEditorControls(true);
        alertTable.getColumnExt(ALERT_STATUS_COLUMN_NAME).setCellRenderer(new ImageCellRenderer());

        alertTable.setSelectionMode(0);
        alertTable.setRowSelectionAllowed(true);
        alertTable.setRowHeight(UIConstants.ROW_HEIGHT);
        alertTable.setFocusable(false);
        alertTable.setSortable(true);
        alertTable.setOpaque(true);
        alertTable.setDragEnabled(false);

        alertTable.getColumnExt(ALERT_STATUS_COLUMN_NAME).setMaxWidth(UIConstants.MAX_WIDTH);
        alertTable.getColumnExt(ALERT_STATUS_COLUMN_NAME).setMinWidth(UIConstants.MIN_WIDTH);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            alertTable.setHighlighters(highlighter);
        }

        // This action is called when a new selection is made on the alert
        // table.
        alertTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                if (updatingAlertTable || isDeletingAlert || alertTable.isEditing()) {
                    return;
                }

                if (!evt.getValueIsAdjusting()) {
                    if (lastAlertRow != -1 && lastAlertRow != alertTable.getSelectedModelIndex() && lastAlertRow < alertTable.getModel().getRowCount()) {
                        saveAlert();
                    }

                    loadAlert();
                    refreshAlertTableRow();
                    checkVisibleAlertTasks();
                }
            }
        });

        // Mouse listener for trigger-button popup on the table.
        alertTable.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                checkAlertSelectionAndPopupMenu(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                checkAlertSelectionAndPopupMenu(evt);
            }
        });

        alertPane.setViewportView(alertTable);

        // Key Listener trigger for DEL
        alertTable.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    parent.doDeleteAlert();
                }
            }

            public void keyReleased(KeyEvent e) {}

            public void keyTyped(KeyEvent e) {}
        });
    }

    public void updateAlertTable() {
        Object[][] tableData = null;
        int tableSize = 0;

        if (parent.alerts != null) {
            tableSize = parent.alerts.size();

            tableData = new Object[tableSize][2];
            for (int i = 0; i < tableSize; i++) {
                Alert alert = parent.alerts.get(i);
                if (alert.isEnabled()) {
                    tableData[i][0] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_blue.png")), ENABLED_TEXT);
                } else {
                    tableData[i][0] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_black.png")), DISABLED_TEXT);
                }
                tableData[i][1] = alert.getName();
            }
        }

        if (alertTable != null) {
            RefreshTableModel model = (RefreshTableModel) alertTable.getModel();
            updatingAlertTable = true;
            model.refreshDataVector(tableData);
            updatingAlertTable = false;
        } else {
            alertTable = new MirthTable();
            alertTable.setModel(new RefreshTableModel(tableData, new String[] { ALERT_STATUS_COLUMN_NAME, ALERT_NAME_COLUMN_NAME }) {

                boolean[] canEdit = new boolean[] { false, true };

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit[columnIndex];
                }
            });
        }

        refreshAlertTableRow();
    }

    private void refreshAlertTableRow() {
        lastAlertRow = alertTable.getSelectedModelIndex();
    }

    public void setDefaultAlert() {
        lastAlertRow = -1;

        if (parent.alerts.size() > 0) {
            alertTable.setRowSelectionInterval(0, 0);
        } else {
            deselectAlertRows();
        }
    }

    /**
     * Checks to see what tasks should be available in the alert pane
     */
    public void checkVisibleAlertTasks() {
        int selected = alertTable.getSelectedModelIndex();

        if (selected == UIConstants.ERROR_CONSTANT) {
            parent.setVisibleTasks(parent.alertTasks, parent.alertPopupMenu, 5, 6, false);
        } else {
            parent.setVisibleTasks(parent.alertTasks, parent.alertPopupMenu, 5, 5, true);

            if (parent.alerts.get(selected).isEnabled()) {
                parent.setVisibleTasks(parent.alertTasks, parent.alertPopupMenu, 6, 6, false);
                parent.setVisibleTasks(parent.alertTasks, parent.alertPopupMenu, 7, 7, true);
            } else {
                parent.setVisibleTasks(parent.alertTasks, parent.alertPopupMenu, 6, 6, true);
                parent.setVisibleTasks(parent.alertTasks, parent.alertPopupMenu, 7, 7, false);
            }
        }
    }

    /**
     * Makes the alert table with a parameter that is true if a new alert should
     * be added as well.
     */
    public void makeApplyToChannelsTable() {
        updateApplyToChannelsTable(null);

        applyToChannelsTable.setDragEnabled(false);
        applyToChannelsTable.setRowSelectionAllowed(false);
        applyToChannelsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        applyToChannelsTable.setFocusable(false);
        applyToChannelsTable.setOpaque(true);
        applyToChannelsTable.getTableHeader().setReorderingAllowed(false);
        applyToChannelsTable.setSortable(true);

        // Sort by Channel Name column
        applyToChannelsTable.getRowSorter().toggleSortOrder(applyToChannelsTable.getColumnModelIndex(APPLY_CHANNEL_NAME_COLUMN_NAME));

        applyToChannelsTable.getColumnExt(APPLY_STATUS_COLUMN_NAME).setMaxWidth(50);
        applyToChannelsTable.getColumnExt(APPLY_STATUS_COLUMN_NAME).setMinWidth(50);

        applyToChannelsTable.getColumnExt(APPLY_CHANNEL_ID_COLUMN_NAME).setVisible(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            applyToChannelsTable.setHighlighters(highlighter);
        }

        applyToChannelsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (applyToChannelsTable.convertColumnIndexToModel(applyToChannelsTable.getSelectedColumn()) == 1) {
                    parent.setSaveEnabled(true);
                }
            }
        });

        // Mouse listener for trigger-button popup on the table.
        applyToChannelsTable.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mousePressed(java.awt.event.MouseEvent evt) {
                checkAlertSelectionAndPopupMenu(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                checkAlertSelectionAndPopupMenu(evt);
            }
        });

        applyToChannelsScrollPane.setViewportView(applyToChannelsTable);
    }

    public void updateApplyToChannelsTable(Alert alert) {
        Object[][] tableData = null;
        int tableSize = 0;

        if (alert != null && parent.alerts != null) {
            tableSize = parent.channels.size();
            tableData = new Object[tableSize][3];

            int i = 0;
            for (Channel channel : parent.channels.values()) {
                tableData[i][0] = channel.getName();
                if (alert.getChannels() != null && alert.getChannels().contains(channel.getId())) {
                    tableData[i][1] = Boolean.TRUE;
                } else {
                    tableData[i][1] = Boolean.FALSE;
                }
                tableData[i][2] = channel.getId();
                i++;
            }
        }

        if (alert != null && applyToChannelsTable != null) {
            RefreshTableModel model = (RefreshTableModel) applyToChannelsTable.getModel();
            model.refreshDataVector(tableData);
        } else {
            applyToChannelsTable = new MirthTable();
            applyToChannelsTable.setModel(new RefreshTableModel(tableData, new String[] { APPLY_CHANNEL_NAME_COLUMN_NAME, APPLY_STATUS_COLUMN_NAME, APPLY_CHANNEL_ID_COLUMN_NAME }) {

                boolean[] canEdit = new boolean[] { false, true, false };

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit[columnIndex];
                }
            });
        }
    }

    public void setAlertErrorList() {
        ArrayList<String> variables = new ArrayList<String>();
        variables.add("Any");
        variables.add("Server");
        variables.add("Client");
        variables.add("200: Filter");
        variables.add("300: Transformer");
        variables.add("301: Transformer conversion");
        variables.add("302: Custom transformer");
        variables.add("400: Connector");
        variables.add("401: Document connector");
        variables.add("402: SMTP connector");
        variables.add("403: File connector");
        variables.add("404: HTTP connector");
        variables.add("405: FTP connector");
        variables.add("406: JDBC Connector");
        variables.add("407: JMS Connector");
        variables.add("408: MLLP Connector");
        variables.add("409: SFTP Connector");
        variables.add("410: SOAP Connector");
        variables.add("411: TCP Connector");
        variables.add("412: VM Connector");
        variables.add("413: Email Connector");

        errorList.removeAll();
        errorList.setListData(variables.toArray());
        errorScrollPane.setViewportView(errorList);
    }

    public void setAlertTemplateVariableList() {
        ArrayList<String> variables = new ArrayList<String>();
        variables.add("error");
        variables.add("errorMessage");
        variables.add("systemTime");
        variables.add("channelName");
        variables.add("date");
        variables.add("globalMapVariable");

        templateVariableList.removeAll();
        templateVariableList.setListData(variables.toArray());
        templateVariablesPane.setViewportView(templateVariableList);
    }

    public boolean loadAlert() {
        int index = alertTable.getSelectedModelIndex();

        if (index == UIConstants.ERROR_CONSTANT) {
            return false;
        }

        boolean enabled = parent.isSaveEnabled();

        Alert current = parent.alerts.get(index);
        updateApplyToChannelsTable(current);
        updateEmailsTable(current);
        errorField.setText(current.getExpression());
        emailSubjectField.setText(current.getSubject());
        template.setText(current.getTemplate());

        int dividerLocation = split.getDividerLocation();
        split.setRightComponent(bottomPane);
        split.setDividerLocation(dividerLocation);

        parent.setSaveEnabled(enabled);

        return true;
    }

    public boolean saveAlert() {
        if (lastAlertRow == UIConstants.ERROR_CONSTANT) {
            return false;
        }

        int index = lastAlertRow;

        boolean enabled = parent.isSaveEnabled();

        Alert current = parent.alerts.get(index);
        current.setChannels(getChannels());
        current.setExpression(errorField.getText());

        stopAlertEditing();
        stopEmailEditing();

        current.setEmails(getEmails());
        current.setSubject(emailSubjectField.getText());
        current.setTemplate(template.getText());

        parent.setSaveEnabled(enabled);

        return true;
    }

    public List<String> getChannels() {
        ArrayList<String> channelList = new ArrayList<String>();

        for (int i = 0; i < applyToChannelsTable.getModel().getRowCount(); i++) {
            if (((Boolean) applyToChannelsTable.getModel().getValueAt(i, 1)).booleanValue()) {
                channelList.add((String) applyToChannelsTable.getModel().getValueAt(i, 2));
            }
        }
        return channelList;
    }

    /**
     * Get the name that should be used for a new alert so that it is unique.
     */
    private String getNewAlertName(int size) {
        String temp = "Alert ";

        for (int i = 1; i <= size; i++) {
            boolean exists = false;
            for (int j = 0; j < size - 1; j++) {
                if (((String) alertTable.getModel().getValueAt(j, alertTable.getColumnModelIndex(ALERT_NAME_COLUMN_NAME))).equalsIgnoreCase(temp + i)) {
                    exists = true;
                }
            }
            if (!exists) {
                return temp + i;
            }
        }
        return "";
    }

    /**
     * Shows the popup menu when the trigger button (right-click) has been
     * pushed.
     */
    private void checkAlertSelectionAndPopupMenu(java.awt.event.MouseEvent evt) {
        int row = alertTable.rowAtPoint(new Point(evt.getX(), evt.getY()));

        if (evt.isPopupTrigger()) {
            if (row != -1) {
                alertTable.setRowSelectionInterval(row, row);
            }
            parent.alertPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    /** Adds a new alert. */
    public void addAlert() {
        stopAlertEditing();
        saveAlert();

        RefreshTableModel model = (RefreshTableModel) alertTable.getModel();

        Alert alert = new Alert();

        try {
            alert.setId(parent.mirthClient.getGuid());
        } catch (ClientException e) {
            parent.alertException(this, e.getStackTrace(), e.getMessage());
        }

        alert.setName(getNewAlertName(model.getRowCount() + 1));
        alert.setEnabled(false);

        Object[] rowData = new Object[2];
        rowData[0] = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_black.png")), DISABLED_TEXT);
        rowData[1] = alert.getName();

        parent.alerts.add(alert);
        model.addRow(rowData);

        int newViewIndex = alertTable.convertRowIndexToView(alertTable.getModel().getRowCount() - 1);
        alertTable.setRowSelectionInterval(newViewIndex, newViewIndex);

        alertPane.getViewport().setViewPosition(new Point(0, alertTable.getRowHeight() * alertTable.getModel().getRowCount()));
        parent.setSaveEnabled(true);
    }

    public void enableAlert() {
        stopAlertEditing();
        parent.alerts.get(alertTable.getSelectedModelIndex()).setEnabled(true);

        CellData enabled = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_blue.png")), ENABLED_TEXT);
        alertTable.getModel().setValueAt(enabled, alertTable.getSelectedModelIndex(), alertTable.getColumnModelIndex(ALERT_STATUS_COLUMN_NAME));

        checkVisibleAlertTasks();
        parent.setSaveEnabled(true);
    }

    public void disableAlert() {
        stopAlertEditing();
        parent.alerts.get(alertTable.getSelectedModelIndex()).setEnabled(false);

        CellData disabled = new CellData(new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/bullet_black.png")), DISABLED_TEXT);
        alertTable.getModel().setValueAt(disabled, alertTable.getSelectedModelIndex(), alertTable.getColumnModelIndex(ALERT_STATUS_COLUMN_NAME));

        checkVisibleAlertTasks();
        parent.setSaveEnabled(true);
    }

    public void deleteAlert() {
        if (!parent.alertOption(this, "Are you sure you want to delete this alert?")) {
            return;
        }
        isDeletingAlert = true;

        stopAlertEditing();

        RefreshTableModel model = (RefreshTableModel) alertTable.getModel();

        int selectedModelIndex = alertTable.getSelectedModelIndex();
        int newViewIndex = alertTable.convertRowIndexToView(selectedModelIndex);
        if (newViewIndex == (model.getRowCount() - 1)) {
            newViewIndex--;
        }

        // must set lastModelRow to -1 so that when setting the new
        // row selection below the old data won't try to be saved.
        lastAlertRow = -1;
        parent.alerts.remove(selectedModelIndex);
        model.removeRow(selectedModelIndex);

        parent.setSaveEnabled(true);

        isDeletingAlert = false;

        if (parent.alerts.size() == 0) {
            deselectAlertRows();
        } else {
            alertTable.setRowSelectionInterval(newViewIndex, newViewIndex);
        }
    }

    public void makeEmailsTable() {
        updateEmailsTable(null);

        emailsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                if (emailsTable.getSelectedRow() != -1) {
                    removeButton.setEnabled(true);
                } else {
                    removeButton.setEnabled(false);
                }
            }
        });

        class EmailsTableCellEditor extends TextFieldCellEditor {

            @Override
            public boolean isCellEditable(EventObject evt) {
                boolean editable = super.isCellEditable(evt);

                if (editable) {
                    removeButton.setEnabled(false);
                }

                return editable;
            }

            @Override
            protected boolean valueChanged(String value) {
                parent.setSaveEnabled(true);
                removeButton.setEnabled(true);
                return true;
            }
        }

        emailsTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        emailsTable.getColumnModel().getColumn(emailsTable.getColumnModelIndex(EMAIL_COLUMN_NAME)).setCellEditor(new EmailsTableCellEditor());
        emailsTable.setCustomEditorControls(true);

        emailsTable.setSelectionMode(0);
        emailsTable.setRowSelectionAllowed(true);
        emailsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        emailsTable.setDragEnabled(false);
        emailsTable.setOpaque(true);
        emailsTable.setSortable(true);
        emailsTable.getTableHeader().setReorderingAllowed(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            emailsTable.setHighlighters(highlighter);
        }

        emailsScrollPane.setViewportView(emailsTable);
    }

    public void updateEmailsTable(Alert alert) {
        Object[][] tableData = null;

        if (alert != null) {
            tableData = new Object[alert.getEmails().size()][1];

            for (int i = 0; i < alert.getEmails().size(); i++) {
                tableData[i][0] = alert.getEmails().get(i);
            }
        }

        if (alert != null && emailsTable != null) {
            RefreshTableModel model = (RefreshTableModel) emailsTable.getModel();
            model.refreshDataVector(tableData);
            if (alert.getEmails().size() == 0) {
                deselectEmailRows();
            }
        } else {
            emailsTable = new MirthTable();
            emailsTable.setModel(new RefreshTableModel(tableData, new String[] { EMAIL_COLUMN_NAME }) {

                boolean[] canEdit = new boolean[] { true };

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit[columnIndex];
                }
            });
        }
    }

    public List<String> getEmails() {
        ArrayList<String> emails = new ArrayList<String>();

        for (int i = 0; i < emailsTable.getModel().getRowCount(); i++) {
            if (((String) emailsTable.getModel().getValueAt(i, 0)).length() > 0) {
                emails.add((String) emailsTable.getModel().getValueAt(i, 0));
            }
        }

        return emails;
    }

    /** Clears the selection in the table */
    public void deselectEmailRows() {
        emailsTable.clearSelection();
        removeButton.setEnabled(false);
    }

    /** Clears the selection in the table and sets the tasks appropriately */
    public void deselectAlertRows() {
        alertTable.clearSelection();
        parent.setVisibleTasks(parent.alertTasks, parent.alertPopupMenu, 5, 7, false);
        resetBlankPane();
    }

    public void resetBlankPane() {
        int dividerLocation = split.getDividerLocation();
        split.setRightComponent(blankPanel);
        split.setDividerLocation(dividerLocation);
    }

    public void stopAlertEditing() {
        if (alertTable.isEditing()) {
            alertTable.getColumnModel().getColumn(alertTable.getColumnModelIndex(ALERT_NAME_COLUMN_NAME)).getCellEditor().stopCellEditing();
        }
    }

    public void stopEmailEditing() {
        if (emailsTable.isEditing()) {
            emailsTable.getColumnModel().getColumn(emailsTable.getColumnModelIndex(EMAIL_COLUMN_NAME)).getCellEditor().stopCellEditing();
        }
    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        split = new javax.swing.JSplitPane();
        alertPane = new javax.swing.JScrollPane();
        alertTable = null;
        bottomPane = new javax.swing.JPanel();
        applyToChannelsPanel = new javax.swing.JPanel();
        applyToChannelsScrollPane = new javax.swing.JScrollPane();
        applyToChannelsTable = null;
        errorPane = new javax.swing.JPanel();
        errorField = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        emailsPane = new javax.swing.JPanel();
        emailsScrollPane = new javax.swing.JScrollPane();
        emailsTable = null;
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        templatePane = new javax.swing.JPanel();
        template = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        errorPanel = new javax.swing.JPanel();
        errorScrollPane = new javax.swing.JScrollPane();
        errorList = new MirthVariableList();
        templateVariablesPanel = new javax.swing.JPanel();
        templateVariablesPane = new javax.swing.JScrollPane();
        templateVariableList = new com.mirth.connect.client.ui.components.MirthVariableList();
        emailSubjectPanel = new javax.swing.JPanel();
        emailSubjectField = new com.mirth.connect.client.ui.components.MirthTextField();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        split.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        split.setDividerLocation(125);
        split.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        alertPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        alertPane.setViewportView(alertTable);

        split.setLeftComponent(alertPane);

        bottomPane.setBackground(new java.awt.Color(255, 255, 255));

        applyToChannelsPanel.setBackground(new java.awt.Color(255, 255, 255));
        applyToChannelsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0), "Apply to Channels"));

        applyToChannelsScrollPane.setViewportView(applyToChannelsTable);

        javax.swing.GroupLayout applyToChannelsPanelLayout = new javax.swing.GroupLayout(applyToChannelsPanel);
        applyToChannelsPanel.setLayout(applyToChannelsPanelLayout);
        applyToChannelsPanelLayout.setHorizontalGroup(
            applyToChannelsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(applyToChannelsScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
        );
        applyToChannelsPanelLayout.setVerticalGroup(
            applyToChannelsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(applyToChannelsScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
        );

        errorPane.setBackground(new java.awt.Color(255, 255, 255));
        errorPane.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0), "Error Matching Field"));

        errorField.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout errorPaneLayout = new javax.swing.GroupLayout(errorPane);
        errorPane.setLayout(errorPaneLayout);
        errorPaneLayout.setHorizontalGroup(
            errorPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(errorField, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
        );
        errorPaneLayout.setVerticalGroup(
            errorPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(errorField, javax.swing.GroupLayout.DEFAULT_SIZE, 67, Short.MAX_VALUE)
        );

        emailsPane.setBackground(new java.awt.Color(255, 255, 255));
        emailsPane.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0), "Emails to Receive Alerts"));

        emailsScrollPane.setViewportView(emailsTable);

        addButton.setText("Add");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        removeButton.setText("Remove");
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout emailsPaneLayout = new javax.swing.GroupLayout(emailsPane);
        emailsPane.setLayout(emailsPaneLayout);
        emailsPaneLayout.setHorizontalGroup(
            emailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, emailsPaneLayout.createSequentialGroup()
                .addComponent(emailsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(emailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(addButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(removeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        emailsPaneLayout.setVerticalGroup(
            emailsPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(emailsPaneLayout.createSequentialGroup()
                .addComponent(addButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(emailsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
        );

        templatePane.setBackground(new java.awt.Color(255, 255, 255));
        templatePane.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0), "Email Body"));

        template.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout templatePaneLayout = new javax.swing.GroupLayout(templatePane);
        templatePane.setLayout(templatePaneLayout);
        templatePaneLayout.setHorizontalGroup(
            templatePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(template, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
        );
        templatePaneLayout.setVerticalGroup(
            templatePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(template, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
        );

        errorPanel.setBackground(new java.awt.Color(255, 255, 255));
        errorPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0), "Error Codes"));

        errorList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        errorScrollPane.setViewportView(errorList);

        javax.swing.GroupLayout errorPanelLayout = new javax.swing.GroupLayout(errorPanel);
        errorPanel.setLayout(errorPanelLayout);
        errorPanelLayout.setHorizontalGroup(
            errorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(errorScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
        );
        errorPanelLayout.setVerticalGroup(
            errorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(errorScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 67, Short.MAX_VALUE)
        );

        templateVariablesPanel.setBackground(new java.awt.Color(255, 255, 255));
        templateVariablesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0), "Alert Variables"));

        templateVariableList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        templateVariablesPane.setViewportView(templateVariableList);

        javax.swing.GroupLayout templateVariablesPanelLayout = new javax.swing.GroupLayout(templateVariablesPanel);
        templateVariablesPanel.setLayout(templateVariablesPanelLayout);
        templateVariablesPanelLayout.setHorizontalGroup(
            templateVariablesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(templateVariablesPane, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
        );
        templateVariablesPanelLayout.setVerticalGroup(
            templateVariablesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(templateVariablesPane, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
        );

        emailSubjectPanel.setBackground(new java.awt.Color(255, 255, 255));
        emailSubjectPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0), "Email Subject"));

        javax.swing.GroupLayout emailSubjectPanelLayout = new javax.swing.GroupLayout(emailSubjectPanel);
        emailSubjectPanel.setLayout(emailSubjectPanelLayout);
        emailSubjectPanelLayout.setHorizontalGroup(
            emailSubjectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(emailSubjectField, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
        );
        emailSubjectPanelLayout.setVerticalGroup(
            emailSubjectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(emailSubjectField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout bottomPaneLayout = new javax.swing.GroupLayout(bottomPane);
        bottomPane.setLayout(bottomPaneLayout);
        bottomPaneLayout.setHorizontalGroup(
            bottomPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bottomPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(bottomPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, bottomPaneLayout.createSequentialGroup()
                        .addComponent(applyToChannelsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(emailsPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, bottomPaneLayout.createSequentialGroup()
                        .addGroup(bottomPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(errorPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(emailSubjectPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(templatePane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(bottomPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(templateVariablesPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(errorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        bottomPaneLayout.setVerticalGroup(
            bottomPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bottomPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(bottomPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(applyToChannelsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(emailsPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(bottomPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(errorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(errorPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(bottomPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(bottomPaneLayout.createSequentialGroup()
                        .addComponent(emailSubjectPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(templatePane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(templateVariablesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        split.setRightComponent(bottomPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(split, javax.swing.GroupLayout.DEFAULT_SIZE, 578, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(split, javax.swing.GroupLayout.DEFAULT_SIZE, 468, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_removeButtonActionPerformed
    {// GEN-HEADEREND:event_removeButtonActionPerformed
        stopEmailEditing();
        if (emailsTable.getSelectedModelIndex() != -1 && !emailsTable.isEditing()) {
            DefaultTableModel model = (DefaultTableModel) emailsTable.getModel();

            int selectedModelIndex = emailsTable.getSelectedModelIndex();
            int newViewIndex = emailsTable.convertRowIndexToView(selectedModelIndex);
            if (newViewIndex == (model.getRowCount() - 1)) {
                newViewIndex--;
            }

            // must set lastModelRow to -1 so that when setting the new
            // row selection below the old data won't try to be saved.
            // lastEmailRow = -1;
            model.removeRow(selectedModelIndex);

            if (emailsTable.getModel().getRowCount() != 0) {
                emailsTable.setRowSelectionInterval(newViewIndex, newViewIndex);
            }

            parent.setSaveEnabled(true);
        }
    }// GEN-LAST:event_removeButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_addButtonActionPerformed
    {// GEN-HEADEREND:event_addButtonActionPerformed
        stopEmailEditing();
        ((DefaultTableModel) emailsTable.getModel()).addRow(new Object[] { "" });
        emailsTable.setRowSelectionInterval(emailsTable.getRowCount() - 1, emailsTable.getRowCount() - 1);
        parent.setSaveEnabled(true);
    }// GEN-LAST:event_addButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JScrollPane alertPane;
    private com.mirth.connect.client.ui.components.MirthTable alertTable;
    private javax.swing.JPanel applyToChannelsPanel;
    private javax.swing.JScrollPane applyToChannelsScrollPane;
    private com.mirth.connect.client.ui.components.MirthTable applyToChannelsTable;
    private javax.swing.JPanel bottomPane;
    private com.mirth.connect.client.ui.components.MirthTextField emailSubjectField;
    private javax.swing.JPanel emailSubjectPanel;
    private javax.swing.JPanel emailsPane;
    private javax.swing.JScrollPane emailsScrollPane;
    private com.mirth.connect.client.ui.components.MirthTable emailsTable;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea errorField;
    private com.mirth.connect.client.ui.components.MirthVariableList errorList;
    private javax.swing.JPanel errorPane;
    private javax.swing.JPanel errorPanel;
    private javax.swing.JScrollPane errorScrollPane;
    private javax.swing.JButton removeButton;
    private javax.swing.JSplitPane split;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea template;
    private javax.swing.JPanel templatePane;
    private com.mirth.connect.client.ui.components.MirthVariableList templateVariableList;
    private javax.swing.JScrollPane templateVariablesPane;
    private javax.swing.JPanel templateVariablesPanel;
    // End of variables declaration//GEN-END:variables
}
