/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.browsers.message;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.MirthDialog;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.ItemSelectionTable;
import com.mirth.connect.client.ui.components.ItemSelectionTableModel;
import com.mirth.connect.client.ui.components.MirthBlankableSpinner;
import com.mirth.connect.client.ui.components.MirthButton;
import com.mirth.connect.client.ui.components.MirthCheckBox;
import com.mirth.connect.client.ui.components.MirthComboBoxTableCellEditor;
import com.mirth.connect.client.ui.components.MirthComboBoxTableCellRenderer;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.channel.MetaDataColumnException;
import com.mirth.connect.donkey.model.channel.MetaDataColumnType;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.model.filters.MessageFilter;
import com.mirth.connect.model.filters.elements.ContentSearchElement;
import com.mirth.connect.model.filters.elements.MetaDataSearchElement;
import com.mirth.connect.model.filters.elements.MetaDataSearchOperator;

import net.miginfocom.swing.MigLayout;

public class MessageBrowserAdvancedFilter extends MirthDialog {
    private Frame parent;
    private static final int CONTENT_TYPE_COLUMN_WIDTH = 120;
    private static final int METADATA_NAME_COLUMN_WIDTH = 140;
    private static final int METADATA_OPERATOR_COLUMN_WIDTH = 140;
    private static final int METADATA_CASE_COLUMN_WIDTH = 75;
    private static Map<String, Object> cachedSettings;
    private static Map<String, MetaDataColumn> cachedMetaDataColumns;

    private MessageBrowser messageBrowser;

    /** Creates new form MessageBrowserAdvancedFilter */
    public MessageBrowserAdvancedFilter(com.mirth.connect.client.ui.Frame parent, MessageBrowser messageBrowser, String title, boolean modal, boolean allowSearch) {
        super(parent, title, modal);
        this.parent = parent;
        this.messageBrowser = messageBrowser;
        initComponents();
        initComponentsManual();
        initContentSearchTable();
        initLayout();
        connectorTable = new ItemSelectionTable();
        cachedSettings = new HashMap<String, Object>();
        cachedMetaDataColumns = new HashMap<String, MetaDataColumn>();
        connectorScrollPane.setViewportView(connectorTable);

    }

    private void initComponentsManual() {
        // restrict the message ID and import ID fields to integer input only
        messageIdLowerField.setDocument(new MirthFieldConstraints(19, false, false, true));
        messageIdUpperField.setDocument(new MirthFieldConstraints(19, false, false, true));
        originalIdLowerField.setDocument(new MirthFieldConstraints(19, false, false, true));
        originalIdUpperField.setDocument(new MirthFieldConstraints(19, false, false, true));
        importIdLowerField.setDocument(new MirthFieldConstraints(19, false, false, true));
        importIdUpperField.setDocument(new MirthFieldConstraints(19, false, false, true));
        sendAttemptsLower.setModel(new SpinnerNumberModel(0, 0, null, 1));
    }

    private void initContentSearchTable() {
        contentSearchTable.setModel(new DefaultTableModel(new Object[][] {}, new String[] {
                "Content Type", "Contains" }) {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return true;
            }
        });

        contentSearchTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contentSearchTable.setDragEnabled(false);
        contentSearchTable.setSortable(false);
        contentSearchTable.getTableHeader().setReorderingAllowed(false);

        contentSearchTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                deleteContentSearchButton.setEnabled(getSelectedRow(contentSearchTable) != -1);
            }
        });

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            contentSearchTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        TableColumn column = contentSearchTable.getColumnModel().getColumn(0);
        column.setCellRenderer(new MirthComboBoxTableCellRenderer(ContentType.getDisplayValues()));
        column.setCellEditor(new MirthComboBoxTableCellEditor(contentSearchTable, ContentType.getDisplayValues(), 1, false, null));
        column.setMinWidth(CONTENT_TYPE_COLUMN_WIDTH);
        column.setMaxWidth(CONTENT_TYPE_COLUMN_WIDTH);

        deleteContentSearchButton.setEnabled(false);
    }

    private void initMetaDataSearchTable() {
        metaDataSearchTable.setModel(new DefaultTableModel(new Object[][] {}, new String[] {
                "Metadata", "Operator", "Value", "Ignore Case" }) {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                if (columnIndex == 3 && cachedMetaDataColumns.get(getValueAt(rowIndex, 0)).getType() != MetaDataColumnType.STRING) {
                    return false;
                }

                return true;
            }

            @Override
            public void setValueAt(Object value, int row, int column) {
                int metaDataColumnIndex = findColumn("Metadata");
                int operatorColumnIndex = findColumn("Operator");
                int valueColumnIndex = findColumn("Value");
                int ignoreCaseColumnIndex = findColumn("Ignore Case");

                if (column == valueColumnIndex) {
                    MetaDataColumn metaDataColumn = cachedMetaDataColumns.get(getValueAt(row, metaDataColumnIndex));

                    if (StringUtils.isNotEmpty((String) value)) {
                        try {
                            metaDataColumn.getType().castValue(value);
                        } catch (MetaDataColumnException e) {
                            parent.alertError(parent, "Invalid value for column type " + metaDataColumn.getType().toString());
                            return;
                        }
                    }
                } else if (column == metaDataColumnIndex) {
                    if (!value.equals(getValueAt(row, metaDataColumnIndex))) {
                        MetaDataSearchOperator operator = MetaDataSearchOperator.EQUAL;

                        super.setValueAt(operator, row, operatorColumnIndex);

                        MetaDataColumn metaDataColumn = cachedMetaDataColumns.get(value);
                        if (metaDataColumn.getType() != MetaDataColumnType.STRING) {
                            super.setValueAt(Boolean.FALSE, row, ignoreCaseColumnIndex);
                        }
                    }

                    super.setValueAt("", row, valueColumnIndex);
                }
                super.setValueAt(value, row, column);
            }
        });

        metaDataSearchTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        metaDataSearchTable.setDragEnabled(false);
        metaDataSearchTable.setSortable(false);
        metaDataSearchTable.getTableHeader().setReorderingAllowed(false);

        newMetaDataSearchButton.setEnabled(!messageBrowser.getMetaDataColumns().isEmpty());

        metaDataSearchTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                deleteMetaDataSearchButton.setEnabled(getSelectedRow(metaDataSearchTable) != -1);
            }
        });

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            metaDataSearchTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        List<MetaDataColumn> metaDataColumns = messageBrowser.getMetaDataColumns();
        cachedMetaDataColumns.clear();

        String[] metaDataNames = new String[metaDataColumns.size()];
        for (int i = 0; i < metaDataColumns.size(); i++) {
            String columnName = metaDataColumns.get(i).getName();
            metaDataNames[i] = columnName;
            cachedMetaDataColumns.put(columnName, metaDataColumns.get(i));
        }

        MirthComboBoxTableCellEditor metaDataEditor = new MirthComboBoxTableCellEditor(metaDataSearchTable, metaDataNames, 1, false, null);
        metaDataEditor.getComboBox().setAutoResizeDropdown(true);

        TableColumn metaDataColumn = metaDataSearchTable.getColumnModel().getColumn(0);
        // add applicable metadata columns to the pull-down list
        metaDataColumn.setCellRenderer(new MirthComboBoxTableCellRenderer(metaDataNames));
        metaDataColumn.setCellEditor(metaDataEditor);
        metaDataColumn.setMinWidth(METADATA_NAME_COLUMN_WIDTH);
        metaDataColumn.setMaxWidth(METADATA_NAME_COLUMN_WIDTH * 2);
        metaDataColumn.setPreferredWidth(METADATA_NAME_COLUMN_WIDTH);

        // Need to create this custom editor since the combo box values are dynamic based on metadata column type. 
        MirthComboBoxTableCellEditor operatorEditor = new MirthComboBoxTableCellEditor(metaDataSearchTable, MetaDataSearchOperator.values(), 1, false, null) {

            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

                MetaDataColumn metaDataColumn = cachedMetaDataColumns.get(table.getValueAt(row, 0));

                comboBox.setModel(new DefaultComboBoxModel(MetaDataSearchOperator.valuesForColumnType(metaDataColumn.getType())));

                return super.getTableCellEditorComponent(table, value, isSelected, row, column);
            }

        };

        TableColumn operatorColumn = metaDataSearchTable.getColumnModel().getColumn(1);
        operatorColumn.setCellRenderer(new MirthComboBoxTableCellRenderer(MetaDataSearchOperator.values()));
        operatorColumn.setCellEditor(operatorEditor);
        operatorColumn.setMinWidth(METADATA_OPERATOR_COLUMN_WIDTH);
        operatorColumn.setMaxWidth(METADATA_OPERATOR_COLUMN_WIDTH);

        TableColumn caseColumn = metaDataSearchTable.getColumnModel().getColumn(3);
        caseColumn.setMinWidth(METADATA_CASE_COLUMN_WIDTH);
        caseColumn.setMaxWidth(METADATA_CASE_COLUMN_WIDTH);
        
        deleteMetaDataSearchButton.setEnabled(false);
    }

    public void loadChannel() {
        connectorTable.setModel(createConnectorTableModel());

        initMetaDataSearchTable();
    }
    
    protected TableModel createConnectorTableModel() {
    	return new ItemSelectionTableModel<Integer, String>(messageBrowser.getConnectors(), null, "Current Connector Name", "Included", "Id");
    }
    
    protected TableModel getConnectorTableModel() {
    	return connectorTable.getModel();
    }

    public void setSelectedMetaDataIds(List<Integer> selectedMetaDataIds) {
        if (selectedMetaDataIds.get(0) != null) {
            ItemSelectionTableModel<Integer, String> connectorModel = (ItemSelectionTableModel<Integer, String>) connectorTable.getModel();
            connectorModel.unselectAllKeys();
            for (Integer metaDataId : selectedMetaDataIds) {
                connectorModel.selectKey(metaDataId);
            }
        }
    }

    public void applySelectionsToFilter(MessageFilter messageFilter) {
        List<Integer> selectedMetaDataIds = getMetaDataIds(true);

        // Included and Excluded metadata Ids will both be null if everything is selected.
        if (selectedMetaDataIds != null) {
            if (selectedMetaDataIds.contains(null)) {
                messageFilter.setExcludedMetaDataIds(getMetaDataIds(false));
            } else {
                messageFilter.setIncludedMetaDataIds(selectedMetaDataIds);
            }
        }

        String id = messageIdLowerField.getText();
        if (!StringUtils.isEmpty(id)) {
            messageFilter.setMinMessageId(Long.parseLong(id));
        }

        id = messageIdUpperField.getText();
        if (!StringUtils.isEmpty(id)) {
            messageFilter.setMaxMessageId(Long.parseLong(id));
        }

        id = originalIdLowerField.getText();
        if (!StringUtils.isEmpty(id)) {
            messageFilter.setOriginalIdLower(Long.parseLong(id));
        }

        id = originalIdUpperField.getText();
        if (!StringUtils.isEmpty(id)) {
            messageFilter.setOriginalIdUpper(Long.parseLong(id));
        }

        id = importIdLowerField.getText();
        if (!StringUtils.isEmpty(id)) {
            messageFilter.setImportIdLower(Long.parseLong(id));
        }

        id = importIdUpperField.getText();
        if (!StringUtils.isEmpty(id)) {
            messageFilter.setImportIdUpper(Long.parseLong(id));
        }

        messageFilter.setServerId(getServerId());

        Integer sendAttemptsLower = (Integer) this.sendAttemptsLower.getValue();
        Integer sendAttemptsUpper = this.sendAttemptsUpper.getIntegerValue();

        // There is no need to test this criteria if it is zero or less, because this should be the lowest value allowed.
        if (sendAttemptsLower <= 0) {
            sendAttemptsLower = null;
        }

        if (sendAttemptsLower != null && sendAttemptsUpper != null && sendAttemptsLower > sendAttemptsUpper) {
            sendAttemptsLower = null;
            sendAttemptsUpper = null;
        }

        messageFilter.setAttachment(attachmentCheckBox.isSelected());
        messageFilter.setError(errorCheckBox.isSelected());
        messageFilter.setSendAttemptsLower(sendAttemptsLower);
        messageFilter.setSendAttemptsUpper(sendAttemptsUpper);
        List<ContentSearchElement> contentSearch = getContentSearch();
        messageFilter.setContentSearch(contentSearch.isEmpty() ? null : contentSearch);

        try {
            messageFilter.setMetaDataSearch(getMetaDataSearch());
        } catch (MetaDataColumnException e) {
            parent.alertError(parent.messageBrowser, "Invalid value for column: " + e.getMetaDataColumn().getName());
        }
    }

    private List<Integer> getMetaDataIds(boolean selected) {
        List<Integer> selectedMetaDataIds = ((ItemSelectionTableModel<Integer, String>) connectorTable.getModel()).getKeys(selected);
        if (selectedMetaDataIds.size() == connectorTable.getRowCount()) {
            return null;
        }
        return selectedMetaDataIds;
    }

    private String getServerId() {
        String serverId = serverIdField.getText();
        return (serverId.length() == 0) ? null : serverId;
    }

    private List<ContentSearchElement> getContentSearch() {
        List<ContentSearchElement> contentSearch = new ArrayList<ContentSearchElement>();
        Map<ContentType, List<String>> contentSearchMap = new HashMap<ContentType, List<String>>();
        DefaultTableModel model = ((DefaultTableModel) contentSearchTable.getModel());
        int rowCount = model.getRowCount();

        for (int i = 0; i < rowCount; i++) {
            ContentType contentType = (ContentType) model.getValueAt(i, 0);
            String searchText = (String) model.getValueAt(i, 1);

            if (searchText.length() > 0) {
                List<String> searchList = contentSearchMap.get(contentType);

                if (searchList == null) {
                    searchList = new ArrayList<String>();
                    contentSearchMap.put(contentType, searchList);
                }
                searchList.add(searchText);
            }
        }

        for (ContentType contentType : ContentType.getDisplayValues()) {
            if (contentSearchMap.containsKey(contentType)) {
                contentSearch.add(new ContentSearchElement(contentType.getContentTypeCode(), contentSearchMap.get(contentType)));
            }
        }

        return contentSearch;
    }

    private List<MetaDataSearchElement> getMetaDataSearch() throws MetaDataColumnException {
        List<MetaDataSearchElement> metaDataSearch = new ArrayList<MetaDataSearchElement>();

        DefaultTableModel model = ((DefaultTableModel) metaDataSearchTable.getModel());
        int rowCount = model.getRowCount();

        if (rowCount == 0) {
            return null;
        } else {
            for (int i = 0; i < rowCount; i++) {
                String metaDataName = (String) model.getValueAt(i, 0);
                String operator = ((MetaDataSearchOperator) model.getValueAt(i, 1)).toFullString();
                String searchText = (String) model.getValueAt(i, 2);
                Boolean ignoreCase = (Boolean) model.getValueAt(i, 3);

                if (StringUtils.isNotEmpty(searchText)) {
                    MetaDataColumn column = cachedMetaDataColumns.get(metaDataName);
                    metaDataSearch.add(new MetaDataSearchElement(metaDataName, operator, column.getType().castValue(searchText), ignoreCase));
                }
            }

            return metaDataSearch;
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            saveSelections();
        }

        super.setVisible(visible);
    }

    public void saveSelections() {
        DefaultTableModel contentSearchModel = ((DefaultTableModel) contentSearchTable.getModel());
        DefaultTableModel metaDataSearchModel = ((DefaultTableModel) metaDataSearchTable.getModel());
        ItemSelectionTableModel<Integer, String> connectorModel = ((ItemSelectionTableModel<Integer, String>) connectorTable.getModel());

        cachedSettings.clear();

        cachedSettings.put("messageIdLowerField", messageIdLowerField.getText());
        cachedSettings.put("messageIdUpperField", messageIdUpperField.getText());
        cachedSettings.put("originalIdLowerField", originalIdLowerField.getText());
        cachedSettings.put("originalIdUpperField", originalIdUpperField.getText());
        cachedSettings.put("importIdLowerField", importIdLowerField.getText());
        cachedSettings.put("importIdUpperField", importIdUpperField.getText());
        cachedSettings.put("serverIdField", serverIdField.getText());
        cachedSettings.put("sendAttemptsLower", sendAttemptsLower.getValue());
        cachedSettings.put("sendAttemptsUpper", sendAttemptsUpper.getValue());
        cachedSettings.put("attachment", attachmentCheckBox.isSelected());
        cachedSettings.put("error", errorCheckBox.isSelected());

        Object[][] contentSearchData = new Object[contentSearchModel.getRowCount()][contentSearchModel.getColumnCount()];
        for (int row = 0; row < contentSearchModel.getRowCount(); row++) {
            for (int column = 0; column < contentSearchModel.getColumnCount(); column++) {
                contentSearchData[row][column] = contentSearchModel.getValueAt(row, column);
            }
        }
        cachedSettings.put("contentSearchTable", contentSearchData);

        Object[][] metaDataSearchData = new Object[metaDataSearchModel.getRowCount()][metaDataSearchModel.getColumnCount()];
        for (int row = 0; row < metaDataSearchModel.getRowCount(); row++) {
            for (int column = 0; column < metaDataSearchModel.getColumnCount(); column++) {
                metaDataSearchData[row][column] = metaDataSearchModel.getValueAt(row, column);
            }
        }
        cachedSettings.put("metaDataSearchTable", metaDataSearchData);

        Boolean[] connectorData = new Boolean[connectorModel.getRowCount()];
        for (int row = 0; row < connectorModel.getRowCount(); row++) {
            connectorData[row] = (Boolean) connectorModel.getValueAt(row, ItemSelectionTableModel.CHECKBOX_COLUMN);
        }

        cachedSettings.put("connectorTable", connectorData);
    }

    public void loadSelections() {
        DefaultTableModel contentSearchModel = ((DefaultTableModel) contentSearchTable.getModel());
        DefaultTableModel metaDataSearchModel = ((DefaultTableModel) metaDataSearchTable.getModel());
        ItemSelectionTableModel<Integer, String> connectorModel = ((ItemSelectionTableModel<Integer, String>) connectorTable.getModel());
        messageIdLowerField.setText((String) cachedSettings.get("messageIdLowerField"));
        messageIdUpperField.setText((String) cachedSettings.get("messageIdUpperField"));
        originalIdLowerField.setText((String) cachedSettings.get("originalIdLowerField"));
        originalIdUpperField.setText((String) cachedSettings.get("originalIdUpperField"));
        importIdLowerField.setText((String) cachedSettings.get("importIdLowerField"));
        importIdUpperField.setText((String) cachedSettings.get("importIdUpperField"));
        serverIdField.setText((String) cachedSettings.get("serverIdField"));
        sendAttemptsLower.setValue(cachedSettings.get("sendAttemptsLower"));
        sendAttemptsUpper.setValue(cachedSettings.get("sendAttemptsUpper"));
        attachmentCheckBox.setSelected((Boolean) cachedSettings.get("attachment"));
        errorCheckBox.setSelected((Boolean) cachedSettings.get("error"));

        contentSearchModel.setNumRows(0);
        Object[][] contentSearchData = (Object[][]) cachedSettings.get("contentSearchTable");
        for (int row = 0; row < contentSearchData.length; row++) {
            contentSearchModel.addRow(contentSearchData[row]);
        }

        metaDataSearchModel.setNumRows(0);
        Object[][] metaDataSearchData = (Object[][]) cachedSettings.get("metaDataSearchTable");
        for (int row = 0; row < metaDataSearchData.length; row++) {
            metaDataSearchModel.addRow(metaDataSearchData[row]);
        }

        Boolean[] connectorData = (Boolean[]) cachedSettings.get("connectorTable");
        for (int row = 0; row < connectorModel.getRowCount(); row++) {
            connectorModel.setValueAt(connectorData[row], row, ItemSelectionTableModel.CHECKBOX_COLUMN);
        }

        cachedSettings.clear();
    }

    public void resetSelections() {
        messageIdLowerField.setText("");
        messageIdUpperField.setText("");
        originalIdLowerField.setText("");
        originalIdUpperField.setText("");
        importIdLowerField.setText("");
        importIdUpperField.setText("");
        serverIdField.setText("");
        sendAttemptsLower.setValue(0);
        sendAttemptsUpper.setValue("");
        attachmentCheckBox.setSelected(false);
        errorCheckBox.setSelected(false);
        ((DefaultTableModel) contentSearchTable.getModel()).setNumRows(0);
        ((DefaultTableModel) metaDataSearchTable.getModel()).setNumRows(0);
        ((ItemSelectionTableModel<Integer, String>) connectorTable.getModel()).selectAllKeys();
    }

    public Boolean hasAdvancedCriteria() {
        Boolean hasAdvancedCriteria = false;

        ItemSelectionTableModel<Integer, String> model = ((ItemSelectionTableModel<Integer, String>) connectorTable.getModel());

        if (StringUtils.isNotEmpty(messageIdLowerField.getText()) || StringUtils.isNotEmpty(messageIdUpperField.getText()) || StringUtils.isNotEmpty(originalIdLowerField.getText()) || StringUtils.isNotEmpty(originalIdUpperField.getText()) || StringUtils.isNotEmpty(importIdLowerField.getText()) || StringUtils.isNotEmpty(importIdUpperField.getText()) || StringUtils.isNotEmpty(serverIdField.getText()) || !sendAttemptsLower.getValue().equals(0) || StringUtils.isNotEmpty(sendAttemptsUpper.getValue().toString()) || attachmentCheckBox.isSelected() || errorCheckBox.isSelected() || ((DefaultTableModel) contentSearchTable.getModel()).getRowCount() != 0 || ((DefaultTableModel) metaDataSearchTable.getModel()).getRowCount() != 0 || model.getKeys(true).size() != model.getRowCount()) {
            hasAdvancedCriteria = true;
        }

        return hasAdvancedCriteria;
    }

    private void stopEditing() {
        // if the user had typed in a value in the content search table, close the cell editor so that any value that was entered will be included in the search
        TableCellEditor cellEditor = contentSearchTable.getCellEditor();
        if (cellEditor != null) {
            cellEditor.stopCellEditing();
        }

        cellEditor = metaDataSearchTable.getCellEditor();
        if (cellEditor != null) {
            cellEditor.stopCellEditing();
        }
    }

    @Override
    public void onCloseAction() {
        formWindowClosing(null);
    }

    private void initComponents() {
   
        // initialize components for the connector scroll table
        connectorSelectAll = new JLabel();
        connectorSelectAll.setForeground(java.awt.Color.blue);
        connectorSelectAll.setText("<html><u>Select All</u></html>");
        connectorSelectAll.setToolTipText("Select all connectors below.");
        connectorSelectAll.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        connectorSelectAll.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                connectorSelectAllMouseReleased(evt);
            }
        });
        
        verticalSeparator = new JLabel();
        verticalSeparator.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
        verticalSeparator.setText("|");
        
        connectorDeselectAll = new JLabel();
        connectorDeselectAll.setForeground(java.awt.Color.blue);
        connectorDeselectAll.setText("<html><u>Deselect All</u></html>");
        connectorDeselectAll.setToolTipText("Deselect all connectors below.");
        connectorDeselectAll.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        connectorDeselectAll.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                connectorDeselectAllMouseReleased(evt);
            }
        });
        
        connectorTable = new MirthTable();
        connectorTable.setToolTipText("<html>\nInclude messages from the selected connectors. Connectors that were<br/>\nremoved from this channel are not available to select. Messages for removed<br/>\nconnectors will only be included if all connectors are selected. If a connector's<br/>\nname has changed, messages before the name change will still be included.\n</html>");

        connectorScrollPane = new JScrollPane();
        connectorScrollPane.setViewportView(connectorTable);
        
        // initialize components for the message filter
        messageFilterPanel = new JPanel();
        messageFilterPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        messageIdLabel = new JLabel("Message Id:");
        messageIdLowerField = new MirthTextField();
        messageDashLabel = new JLabel(" - ");
        messageIdUpperField = new MirthTextField();
        
        originalIdLabel = new JLabel("Original Id:");
        originalIdLowerField = new MirthTextField();
        originalDashLabel = new JLabel(" - ");
        originalIdUpperField = new MirthTextField();

        importIdLabel = new JLabel("Import Id:");
        importIdLowerField = new MirthTextField();
        importDashLabel = new JLabel(" - ");
        importIdUpperField = new MirthTextField();

        serverIdLabel = new JLabel("Server Id:");
        serverIdField = new MirthTextField();
        serverIdField.setToolTipText("<html>The GUID of the message in the Mirth Connect database.<br>This can be retrieved from the Meta Data tab in the Message Browser.</html>");

        sendAttemptsLabel = new JLabel("Send Attempts:");
        sendAttemptsLower = new javax.swing.JSpinner();

        sendAttemptsDashLabel = new JLabel(" - ");
        sendAttemptsUpper = new MirthBlankableSpinner(0, null);
      
        hasAttachmentLabel = new JLabel("Has Attachment:");
        attachmentCheckBox = new MirthCheckBox();
        attachmentCheckBox.setBackground(UIConstants.BACKGROUND_COLOR);
        attachmentCheckBox.setToolTipText("If checked, only messages with attachments will be included.");

        hasErrorLabel = new JLabel("Has Error:");
        errorCheckBox = new MirthCheckBox();
        errorCheckBox.setBackground(UIConstants.BACKGROUND_COLOR);
        errorCheckBox.setToolTipText("If checked, only messages with errors will be included.");
        
        // initialize the content type search
        contentSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        contentSplitPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        contentSplitPane.setBackground(UIConstants.BACKGROUND_COLOR);
        contentSplitPane.setDividerSize(0);
        contentSplitPane.setResizeWeight(1);

        contentSearchTable = new MirthTable();
        contentSearchTable.setToolTipText("<html> \nSearch specific message content. This process could take a long time<br/>\ndepending on the amount of message content currently stored. Any message<br/>\ncontent that was encrypted by this channel will not be searchable. </html>");
        contentSearchScrollPane = new JScrollPane();
        contentSearchScrollPane.setViewportView(contentSearchTable);

        newContentSearchButton = new MirthButton();
        newContentSearchButton.setText("New");
        newContentSearchButton.setMargin(new java.awt.Insets(0, 2, 0, 2));
        newContentSearchButton.setMaximumSize(new java.awt.Dimension(48, 21));
        newContentSearchButton.setMinimumSize(new java.awt.Dimension(48, 21));
        newContentSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addContentSearchButtonActionPerformed(evt);
            }
        });

        deleteContentSearchButton = new MirthButton();
        deleteContentSearchButton.setText("Delete");
        deleteContentSearchButton.setMargin(new java.awt.Insets(0, 2, 0, 2));
        deleteContentSearchButton.setMaximumSize(new java.awt.Dimension(48, 21));
        deleteContentSearchButton.setMinimumSize(new java.awt.Dimension(48, 21));
        deleteContentSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteContentSearchButtonActionPerformed(evt);
            }
        });
        
        contentLeftPanel = new JPanel();
        contentRightPanel = new JPanel();
        contentRightPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        // initialize the metadata search
        metaDataSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        metaDataSplitPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        metaDataSplitPane.setOneTouchExpandable(true);
        metaDataSplitPane.setDividerSize(0);
        metaDataSplitPane.setResizeWeight(1);

        metaDataSearchTable = new MirthTable();
        metaDataSearchTable.setToolTipText("<html>Search on custom metadata stored for this channel.<br/>Note that if Ignore Case is unchecked, case<br/>sensitivity depends on the database collation.</html>");
        metaDataSearchScrollPane = new JScrollPane();
        metaDataSearchScrollPane.setViewportView(metaDataSearchTable);

        newMetaDataSearchButton = new MirthButton();
        newMetaDataSearchButton.setText("New");
        newMetaDataSearchButton.setMargin(new java.awt.Insets(0, 2, 0, 2));
        newMetaDataSearchButton.setMaximumSize(new java.awt.Dimension(48, 21));
        newMetaDataSearchButton.setMinimumSize(new java.awt.Dimension(48, 21));
        newMetaDataSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMetaDataSearchButtonActionPerformed(evt);
            }
        });

        deleteMetaDataSearchButton = new MirthButton();
        deleteMetaDataSearchButton.setText("Delete");
        deleteMetaDataSearchButton.setMargin(new java.awt.Insets(0, 2, 0, 2));
        deleteMetaDataSearchButton.setMaximumSize(new java.awt.Dimension(48, 21));
        deleteMetaDataSearchButton.setMinimumSize(new java.awt.Dimension(48, 21));
        deleteMetaDataSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteMetaDataSearchButtonActionPerformed(evt);
            }
        });
        
        metaDataLeftPanel = new JPanel();
        metaDataRightPanel = new JPanel();
        metaDataRightPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        separator = new JSeparator();

        okButton = new JButton();
        okButton.setText("OK");
        okButton.setMargin(new java.awt.Insets(0, 2, 0, 2));
        okButton.setMaximumSize(new java.awt.Dimension(48, 21));
        okButton.setMinimumSize(new java.awt.Dimension(48, 21));
        okButton.setPreferredSize(new java.awt.Dimension(48, 21));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButton1ActionPerformed(evt);
            }
        });

        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        cancelButton.setMargin(new java.awt.Insets(0, 2, 0, 2));
        cancelButton.setMaximumSize(new java.awt.Dimension(48, 21));
        cancelButton.setMinimumSize(new java.awt.Dimension(48, 21));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        
        contentSplitPane.setLeftComponent(contentLeftPanel);
        contentSplitPane.setRightComponent(contentRightPanel);
        metaDataSplitPane.setLeftComponent(metaDataLeftPanel);
        metaDataSplitPane.setRightComponent(metaDataRightPanel);    
    }
    
    private void initLayout() {
        setLayout(new MigLayout("insets 12, novisualpadding, hidemode 3, fill, gap 6"));   
        setBackground(UIConstants.BACKGROUND_COLOR);
        getContentPane().setBackground(getBackground());
        setSize(new Dimension(560, 710));

        // Current Connector Name
        add(connectorSelectAll, "right, split 3");
        add(verticalSeparator);
        add(connectorDeselectAll, "wrap");
        
        add(connectorScrollPane, "h 135!, growx, wrap");
        
        // middle section filter
        messageFilterPanel.setLayout(new MigLayout("", "", ""));
        messageFilterPanel.add(messageIdLabel, "right");
        messageFilterPanel.add(messageIdLowerField, "w 110!");
        messageFilterPanel.add(messageDashLabel);
        messageFilterPanel.add(messageIdUpperField, "w 110!, wrap");
        
        messageFilterPanel.add(originalIdLabel, "right");
        messageFilterPanel.add(originalIdLowerField, "w 110!");
        messageFilterPanel.add(originalDashLabel);
        messageFilterPanel.add(originalIdUpperField, "w 110!, wrap");
        
        messageFilterPanel.add(importIdLabel, "right");
        messageFilterPanel.add(importIdLowerField, "w 110!");
        messageFilterPanel.add(importDashLabel);
        messageFilterPanel.add(importIdUpperField, "w 110!, wrap");

        messageFilterPanel.add(serverIdLabel, "right");
        messageFilterPanel.add(serverIdField, "spanx, pushx, growx, wrap");

        messageFilterPanel.add(sendAttemptsLabel, "right");
        messageFilterPanel.add(sendAttemptsLower, "w 110!");
        messageFilterPanel.add(sendAttemptsDashLabel);
        messageFilterPanel.add(sendAttemptsUpper, "w 110!, wrap");
      
        messageFilterPanel.add(hasAttachmentLabel, "right");
        messageFilterPanel.add(attachmentCheckBox, "wrap");

        messageFilterPanel.add(hasErrorLabel, "right");
        messageFilterPanel.add(errorCheckBox);

        add(messageFilterPanel, "left, wrap");
        
        // content type split
        contentLeftPanel.setLayout(new BorderLayout(2,2));
        contentLeftPanel.add(contentSearchScrollPane);
        contentRightPanel.setLayout(new MigLayout("insets 0 12 0, right, top, wrap", "[50!]0", "0[]5[]"));
        contentRightPanel.add(newContentSearchButton);
        contentRightPanel.add(deleteContentSearchButton);
        add(contentSplitPane, "push, grow, wrap");
        
        // metadata split panel
        metaDataLeftPanel.setLayout(new BorderLayout(2,2));
        metaDataLeftPanel.add(metaDataSearchScrollPane);
        metaDataRightPanel.setLayout(new MigLayout("insets 0 12 0, right, top, wrap", "[50!]0", "0[]5[]"));
        metaDataRightPanel.add(newMetaDataSearchButton);
        metaDataRightPanel.add(deleteMetaDataSearchButton);
        add(metaDataSplitPane, "push, grow");

        add(separator, "newline, growx");
        add(okButton, "right, newline, split 2");
        add(cancelButton, "right");
        
    }

    private void shiftValues(JList source, JList destination) {
        Object[] values = source.getSelectedValues();
        DefaultListModel sourceModel = (DefaultListModel) source.getModel();
        DefaultListModel destinationModel = (DefaultListModel) destination.getModel();

        for (Object value : values) {
            sourceModel.removeElement(value);
            destinationModel.addElement(value);
        }
    }

    private void addContentSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {
        DefaultTableModel model = ((DefaultTableModel) contentSearchTable.getModel());
        int row = model.getRowCount();

        model.addRow(new Object[] { ContentType.RAW, "" });

        contentSearchTable.setRowSelectionInterval(row, row);
    }

    private int getSelectedRow(MirthTable table) {
        if (table.isEditing()) {
            return table.getEditingRow();
        } else {
            return table.getSelectedRow();
        }
    }

    private void deleteContentSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedRow = getSelectedRow(contentSearchTable);

        if (selectedRow != -1 && !contentSearchTable.isEditing()) {
            ((DefaultTableModel) contentSearchTable.getModel()).removeRow(selectedRow);
        }

        int rowCount = contentSearchTable.getRowCount();

        if (rowCount > 0) {
            if (selectedRow >= rowCount) {
                selectedRow--;
            }

            contentSearchTable.setRowSelectionInterval(selectedRow, selectedRow);
        }
    }

    private void connectorSelectAllMouseReleased(java.awt.event.MouseEvent evt) {
        ((ItemSelectionTableModel<Integer, String>) connectorTable.getModel()).selectAllKeys();
    }

    private void connectorDeselectAllMouseReleased(java.awt.event.MouseEvent evt) {
        ((ItemSelectionTableModel<Integer, String>) connectorTable.getModel()).unselectAllKeys();
    }

    private void addMetaDataSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {
        DefaultTableModel model = ((DefaultTableModel) metaDataSearchTable.getModel());
        int row = model.getRowCount();

        List<MetaDataColumn> metaDataColumns = messageBrowser.getMetaDataColumns();
        if (metaDataColumns.size() > 0) {
            MetaDataColumn metaDataColumn = metaDataColumns.get(0);
            MetaDataSearchOperator operator = MetaDataSearchOperator.EQUAL;

            model.addRow(new Object[] { metaDataColumn.getName(), operator, "", false });

            metaDataSearchTable.setRowSelectionInterval(row, row);
        }
    }

    private void deleteMetaDataSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedRow = getSelectedRow(metaDataSearchTable);

        if (selectedRow != -1 && !metaDataSearchTable.isEditing()) {
            ((DefaultTableModel) metaDataSearchTable.getModel()).removeRow(selectedRow);
        }

        int rowCount = metaDataSearchTable.getRowCount();

        if (rowCount > 0) {
            if (selectedRow >= rowCount) {
                selectedRow--;
            }

            metaDataSearchTable.setRowSelectionInterval(selectedRow, selectedRow);
        }
    }

    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        stopEditing();
        loadSelections();
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        stopEditing();
        loadSelections();

        setVisible(false);
    }

    private void okButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        stopEditing();

        setVisible(false);
    }

    private MirthCheckBox attachmentCheckBox;
    private JButton cancelButton;
    private JLabel connectorDeselectAll;
    private JScrollPane connectorScrollPane;
    private JLabel connectorSelectAll;
    private MirthTable connectorTable;
    private JPanel contentLeftPanel;
    private JPanel contentRightPanel;
    private JScrollPane contentSearchScrollPane;
    private MirthTable contentSearchTable;
    private JSplitPane contentSplitPane;
    private MirthButton deleteContentSearchButton;
    private MirthButton deleteMetaDataSearchButton;
    private MirthCheckBox errorCheckBox;
    private JLabel hasAttachmentLabel;
    private JLabel hasErrorLabel;
    private JLabel importDashLabel;
    private JLabel importIdLabel;
    private MirthTextField importIdLowerField;
    private MirthTextField importIdUpperField;
    private JLabel messageDashLabel;
    private JPanel messageFilterPanel;
    private JLabel messageIdLabel;
    private MirthTextField messageIdLowerField;
    private MirthTextField messageIdUpperField;
    private JPanel metaDataLeftPanel;
    private JPanel metaDataRightPanel;
    private JSplitPane metaDataSplitPane;
    private JScrollPane metaDataSearchScrollPane;
    private MirthTable metaDataSearchTable;
    private MirthButton newContentSearchButton;
    private MirthButton newMetaDataSearchButton;
    private JButton okButton;
    private JLabel originalDashLabel;
    private JLabel originalIdLabel;
    private MirthTextField originalIdLowerField;
    private MirthTextField originalIdUpperField;
    private JLabel sendAttemptsDashLabel;
    private JLabel sendAttemptsLabel;
    private JSpinner sendAttemptsLower;
    private MirthBlankableSpinner sendAttemptsUpper;
    private JSeparator separator;
    private MirthTextField serverIdField;
    private JLabel serverIdLabel;
    private JLabel verticalSeparator;
}
