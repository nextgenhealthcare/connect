/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.browsers.message;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.ItemSelectionTable;
import com.mirth.connect.client.ui.components.ItemSelectionTableModel;
import com.mirth.connect.client.ui.components.MirthComboBoxTableCellEditor;
import com.mirth.connect.client.ui.components.MirthComboBoxTableCellRenderer;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.channel.MetaDataColumnException;
import com.mirth.connect.donkey.model.channel.MetaDataColumnType;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.model.filters.MessageFilter;
import com.mirth.connect.model.filters.elements.ContentSearchElement;
import com.mirth.connect.model.filters.elements.MetaDataSearchElement;
import com.mirth.connect.model.filters.elements.MetaDataSearchOperator;

public class MessageBrowserAdvancedFilter extends javax.swing.JDialog {
    private Frame parent;
    private static final int CONTENT_TYPE_COLUMN_WIDTH = 120;
    private static final int METADATA_NAME_COLUMN_WIDTH = 140;
    private static final int METADATA_OPERATOR_COLUMN_WIDTH = 90;
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
        connectorTable = new ItemSelectionTable();
        cachedSettings = new HashMap<String, Object>();
        cachedMetaDataColumns = new HashMap<String, MetaDataColumn>();
        jScrollPane6.setViewportView(connectorTable);
    }

    private void initComponentsManual() {
        // restrict the message ID and import ID fields to integer input only
        messageIdLowerField.setDocument(new MirthFieldConstraints(19, false, false, true));
        messageIdUpperField.setDocument(new MirthFieldConstraints(19, false, false, true));
        importIdLowerField.setDocument(new MirthFieldConstraints(19, false, false, true));
        importIdUpperField.setDocument(new MirthFieldConstraints(19, false, false, true));
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
        column.setCellRenderer(new MirthComboBoxTableCellRenderer(ContentType.getMessageTypes()));
        column.setCellEditor(new MirthComboBoxTableCellEditor(contentSearchTable, ContentType.getMessageTypes(), 1, false, null));
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

        addMetaDataSearchButton.setEnabled(!messageBrowser.getMetaDataColumns().isEmpty());

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

        TableColumn metaDataColumn = metaDataSearchTable.getColumnModel().getColumn(0);
        metaDataColumn.setCellRenderer(new MirthComboBoxTableCellRenderer(metaDataNames));
        metaDataColumn.setCellEditor(new MirthComboBoxTableCellEditor(metaDataSearchTable, metaDataNames, 1, false, null));
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
        connectorTable.setModel(new ItemSelectionTableModel<Integer, String>(messageBrowser.getConnectors(), null, "Current Connector Name", "Included", "Id"));

        initMetaDataSearchTable();
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

    protected void applySelectionsToFilter(MessageFilter messageFilter) {
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
            messageFilter.setMessageIdLower(Long.parseLong(id));
        }

        id = messageIdUpperField.getText();
        if (!StringUtils.isEmpty(id)) {
            messageFilter.setMessageIdUpper(Long.parseLong(id));
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
        messageFilter.setSendAttemptsLower(sendAttemptsLower);
        messageFilter.setSendAttemptsUpper(sendAttemptsUpper);
        messageFilter.setContentSearch(getContentSearch(messageFilter.getQuickSearch()));

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

    private List<ContentSearchElement> getContentSearch(String quickSearch) {
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

        for (ContentType contentType : ContentType.getMessageTypes()) {
            if (contentSearchMap.containsKey(contentType)) {
                contentSearch.add(new ContentSearchElement(contentType.getContentTypeCode(), contentSearchMap.get(contentType)));
            } else if (quickSearch != null) {
                /*
                 * If quick search is active, always add the content type to the content search so
                 * the content is joined for quick search.
                 */
                contentSearch.add(new ContentSearchElement(contentType.getContentTypeCode(), new ArrayList<String>()));
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
        cachedSettings.put("importIdLowerField", importIdLowerField.getText());
        cachedSettings.put("importIdUpperField", importIdUpperField.getText());
        cachedSettings.put("serverIdField", serverIdField.getText());
        cachedSettings.put("sendAttemptsLower", sendAttemptsLower.getValue());
        cachedSettings.put("sendAttemptsUpper", sendAttemptsUpper.getValue());
        cachedSettings.put("attachment", attachmentCheckBox.isSelected());

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
        importIdLowerField.setText((String) cachedSettings.get("importIdLowerField"));
        importIdUpperField.setText((String) cachedSettings.get("importIdUpperField"));
        serverIdField.setText((String) cachedSettings.get("serverIdField"));
        sendAttemptsLower.setValue(cachedSettings.get("sendAttemptsLower"));
        sendAttemptsUpper.setValue(cachedSettings.get("sendAttemptsUpper"));
        attachmentCheckBox.setSelected((Boolean) cachedSettings.get("attachment"));

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
        importIdLowerField.setText("");
        importIdUpperField.setText("");
        serverIdField.setText("");
        sendAttemptsLower.setValue(0);
        sendAttemptsUpper.setValue("");
        attachmentCheckBox.setSelected(false);
        ((DefaultTableModel) contentSearchTable.getModel()).setNumRows(0);
        ((DefaultTableModel) metaDataSearchTable.getModel()).setNumRows(0);
        ((ItemSelectionTableModel<Integer, String>) connectorTable.getModel()).selectAllKeys();
    }

    public Boolean hasAdvancedCriteria() {
        Boolean hasAdvancedCriteria = false;

        ItemSelectionTableModel<Integer, String> model = ((ItemSelectionTableModel<Integer, String>) connectorTable.getModel());

        if (StringUtils.isNotEmpty(messageIdLowerField.getText()) || StringUtils.isNotEmpty(messageIdUpperField.getText()) || StringUtils.isNotEmpty(importIdLowerField.getText()) || StringUtils.isNotEmpty(importIdUpperField.getText()) || StringUtils.isNotEmpty(serverIdField.getText()) || !sendAttemptsLower.getValue().equals(0) || StringUtils.isNotEmpty(sendAttemptsUpper.getValue().toString()) || attachmentCheckBox.isSelected() || ((DefaultTableModel) contentSearchTable.getModel()).getRowCount() != 0 || ((DefaultTableModel) metaDataSearchTable.getModel()).getRowCount() != 0 || model.getKeys(true).size() != model.getRowCount()) {
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        mirthTable1 = new com.mirth.connect.client.ui.components.MirthTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        mirthTable2 = new com.mirth.connect.client.ui.components.MirthTable();
        jScrollPane5 = new javax.swing.JScrollPane();
        mirthTable3 = new com.mirth.connect.client.ui.components.MirthTable();
        containerPanel = new javax.swing.JPanel();
        messageIdLabel = new javax.swing.JLabel();
        messageIdLowerField = new com.mirth.connect.client.ui.components.MirthTextField();
        serverIdField = new com.mirth.connect.client.ui.components.MirthTextField();
        serverIdLabel = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        cancelButton = new javax.swing.JButton();
        okButton1 = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        contentSearchTable = new com.mirth.connect.client.ui.components.MirthTable();
        addContentSearchButton = new com.mirth.connect.client.ui.components.MirthButton();
        deleteContentSearchButton = new com.mirth.connect.client.ui.components.MirthButton();
        sendAttemptsUpper = new com.mirth.connect.client.ui.components.MirthBlankableSpinner();
        jScrollPane6 = new javax.swing.JScrollPane();
        connectorTable = new com.mirth.connect.client.ui.components.MirthTable();
        connectorSelectAll = new javax.swing.JLabel();
        connectorDeselectAll = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        sendAttemptsLower = new javax.swing.JSpinner();
        addMetaDataSearchButton = new com.mirth.connect.client.ui.components.MirthButton();
        jScrollPane7 = new javax.swing.JScrollPane();
        metaDataSearchTable = new com.mirth.connect.client.ui.components.MirthTable();
        deleteMetaDataSearchButton = new com.mirth.connect.client.ui.components.MirthButton();
        jLabel1 = new javax.swing.JLabel();
        attachmentCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        importIdLabel = new javax.swing.JLabel();
        importIdLowerField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel4 = new javax.swing.JLabel();
        messageIdUpperField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel6 = new javax.swing.JLabel();
        importIdUpperField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel8 = new javax.swing.JLabel();

        mirthTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(mirthTable1);

        mirthTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane3.setViewportView(mirthTable2);

        mirthTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane5.setViewportView(mirthTable3);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        containerPanel.setBackground(new java.awt.Color(255, 255, 255));

        messageIdLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        messageIdLabel.setText("Message Id:");

        serverIdField.setToolTipText("<html>The GUID of the message in the Mirth Connect database.<br>This can be retrieved from the Meta Data tab in the Message Browser.</html>");

        serverIdLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        serverIdLabel.setText("Server Id:");

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Send Attempts:");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        cancelButton.setText("Cancel");
        cancelButton.setMargin(new java.awt.Insets(0, 2, 0, 2));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton1.setText("OK");
        okButton1.setMargin(new java.awt.Insets(0, 2, 0, 2));
        okButton1.setMaximumSize(new java.awt.Dimension(48, 21));
        okButton1.setMinimumSize(new java.awt.Dimension(48, 21));
        okButton1.setPreferredSize(new java.awt.Dimension(48, 21));
        okButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 547, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(okButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, okButton1});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 6, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cancelButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(okButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {cancelButton, okButton1});

        contentSearchTable.setToolTipText("<html> \nSearch specific message content. This process could take a long time<br/>\ndepending on the amount of message content currently stored. Any message<br/>\ncontent that was encrypted by this channel will not be searchable. </html>");
        jScrollPane4.setViewportView(contentSearchTable);

        addContentSearchButton.setText("New");
        addContentSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addContentSearchButtonActionPerformed(evt);
            }
        });

        deleteContentSearchButton.setText("Delete");
        deleteContentSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteContentSearchButtonActionPerformed(evt);
            }
        });

        connectorTable.setToolTipText("<html>\nInclude messages from the selected connectors. Connectors that were<br/>\nremoved from this channel are not available to select. Messages for removed<br/>\nconnectors will only be included if all connectors are selected. If a connector's<br/>\nname has changed, messages before the name change will still be included.\n</html>");
        jScrollPane6.setViewportView(connectorTable);

        connectorSelectAll.setForeground(java.awt.Color.blue);
        connectorSelectAll.setText("<html><u>Select All</u></html>");
        connectorSelectAll.setToolTipText("Select all connectors below.");
        connectorSelectAll.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        connectorSelectAll.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                connectorSelectAllMouseReleased(evt);
            }
        });

        connectorDeselectAll.setForeground(java.awt.Color.blue);
        connectorDeselectAll.setText("<html><u>Deselect All</u></html>");
        connectorDeselectAll.setToolTipText("Deselect all connectors below.");
        connectorDeselectAll.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        connectorDeselectAll.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                connectorDeselectAllMouseReleased(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
        jLabel7.setText("|");

        addMetaDataSearchButton.setText("New");
        addMetaDataSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMetaDataSearchButtonActionPerformed(evt);
            }
        });

        metaDataSearchTable.setToolTipText("<html>Search on custom metadata stored for this channel.<br/>Note that if Ignore Case is unchecked, case<br/>sensitivity depends on the database collation.</html>");
        jScrollPane7.setViewportView(metaDataSearchTable);

        deleteMetaDataSearchButton.setText("Delete");
        deleteMetaDataSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteMetaDataSearchButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Has Attachment:");
        jLabel1.setToolTipText("<html>\nIf this is not checked, messages with and without attachments will be retrieved.<br/>\nIf this is checked, only messages with attachments will be retrieved.\n</html>");

        attachmentCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        attachmentCheckBox.setToolTipText("If checked, only messages with attachments will be included.");

        importIdLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        importIdLabel.setText("Import Id:");

        jLabel4.setText("-");

        jLabel6.setText("-");

        jLabel8.setText("-");

        javax.swing.GroupLayout containerPanelLayout = new javax.swing.GroupLayout(containerPanel);
        containerPanel.setLayout(containerPanelLayout);
        containerPanelLayout.setHorizontalGroup(
            containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(containerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane6)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, containerPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(connectorSelectAll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(connectorDeselectAll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, containerPanelLayout.createSequentialGroup()
                        .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane7, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(addContentSearchButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(deleteContentSearchButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(addMetaDataSearchButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(deleteMetaDataSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(containerPanelLayout.createSequentialGroup()
                        .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(serverIdLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(importIdLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(messageIdLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(containerPanelLayout.createSequentialGroup()
                                .addComponent(messageIdLowerField, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(messageIdUpperField, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(attachmentCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(containerPanelLayout.createSequentialGroup()
                                    .addComponent(sendAttemptsLower, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jLabel8)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(sendAttemptsUpper, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(serverIdField, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(containerPanelLayout.createSequentialGroup()
                                .addComponent(importIdLowerField, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(importIdUpperField, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 197, Short.MAX_VALUE)))
                .addContainerGap())
        );
        containerPanelLayout.setVerticalGroup(
            containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(containerPanelLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(connectorSelectAll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(connectorDeselectAll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(messageIdLabel)
                    .addComponent(messageIdLowerField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(messageIdUpperField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel6)
                        .addComponent(importIdUpperField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(containerPanelLayout.createSequentialGroup()
                        .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(importIdLowerField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(importIdLabel))
                        .addGap(5, 5, 5)
                        .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(serverIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(serverIdLabel))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(sendAttemptsLower, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sendAttemptsUpper, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(attachmentCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                    .addGroup(containerPanelLayout.createSequentialGroup()
                        .addComponent(addContentSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteContentSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(containerPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(containerPanelLayout.createSequentialGroup()
                        .addComponent(addMetaDataSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteMetaDataSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(containerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(containerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void shiftValues(JList source, JList destination) {
        Object[] values = source.getSelectedValues();
        DefaultListModel sourceModel = (DefaultListModel) source.getModel();
        DefaultListModel destinationModel = (DefaultListModel) destination.getModel();

        for (Object value : values) {
            sourceModel.removeElement(value);
            destinationModel.addElement(value);
        }
    }

    private void addContentSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addContentSearchButtonActionPerformed
        DefaultTableModel model = ((DefaultTableModel) contentSearchTable.getModel());
        int row = model.getRowCount();

        model.addRow(new Object[] { ContentType.RAW, "" });

        contentSearchTable.setRowSelectionInterval(row, row);
    }//GEN-LAST:event_addContentSearchButtonActionPerformed

    private int getSelectedRow(MirthTable table) {
        if (table.isEditing()) {
            return table.getEditingRow();
        } else {
            return table.getSelectedRow();
        }
    }

    private void deleteContentSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteContentSearchButtonActionPerformed
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
    }//GEN-LAST:event_deleteContentSearchButtonActionPerformed

    private void connectorSelectAllMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_connectorSelectAllMouseReleased
        ((ItemSelectionTableModel<Integer, String>) connectorTable.getModel()).selectAllKeys();
    }//GEN-LAST:event_connectorSelectAllMouseReleased

    private void connectorDeselectAllMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_connectorDeselectAllMouseReleased
        ((ItemSelectionTableModel<Integer, String>) connectorTable.getModel()).unselectAllKeys();
    }//GEN-LAST:event_connectorDeselectAllMouseReleased

    private void addMetaDataSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMetaDataSearchButtonActionPerformed
        DefaultTableModel model = ((DefaultTableModel) metaDataSearchTable.getModel());
        int row = model.getRowCount();

        List<MetaDataColumn> metaDataColumns = messageBrowser.getMetaDataColumns();
        if (metaDataColumns.size() > 0) {
            MetaDataColumn metaDataColumn = metaDataColumns.get(0);
            MetaDataSearchOperator operator = MetaDataSearchOperator.EQUAL;

            model.addRow(new Object[] { metaDataColumn.getName(), operator, "", false });

            metaDataSearchTable.setRowSelectionInterval(row, row);
        }
    }//GEN-LAST:event_addMetaDataSearchButtonActionPerformed

    private void deleteMetaDataSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteMetaDataSearchButtonActionPerformed
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
    }//GEN-LAST:event_deleteMetaDataSearchButtonActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        stopEditing();
        loadSelections();
    }//GEN-LAST:event_formWindowClosing

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        stopEditing();
        loadSelections();

        setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButton1ActionPerformed
        stopEditing();

        setVisible(false);
    }//GEN-LAST:event_okButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.mirth.connect.client.ui.components.MirthButton addContentSearchButton;
    private com.mirth.connect.client.ui.components.MirthButton addMetaDataSearchButton;
    private com.mirth.connect.client.ui.components.MirthCheckBox attachmentCheckBox;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel connectorDeselectAll;
    private javax.swing.JLabel connectorSelectAll;
    private com.mirth.connect.client.ui.components.MirthTable connectorTable;
    private javax.swing.JPanel containerPanel;
    private com.mirth.connect.client.ui.components.MirthTable contentSearchTable;
    private com.mirth.connect.client.ui.components.MirthButton deleteContentSearchButton;
    private com.mirth.connect.client.ui.components.MirthButton deleteMetaDataSearchButton;
    private javax.swing.JLabel importIdLabel;
    private com.mirth.connect.client.ui.components.MirthTextField importIdLowerField;
    private com.mirth.connect.client.ui.components.MirthTextField importIdUpperField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel messageIdLabel;
    private com.mirth.connect.client.ui.components.MirthTextField messageIdLowerField;
    private com.mirth.connect.client.ui.components.MirthTextField messageIdUpperField;
    private com.mirth.connect.client.ui.components.MirthTable metaDataSearchTable;
    private com.mirth.connect.client.ui.components.MirthTable mirthTable1;
    private com.mirth.connect.client.ui.components.MirthTable mirthTable2;
    private com.mirth.connect.client.ui.components.MirthTable mirthTable3;
    private javax.swing.JButton okButton1;
    private javax.swing.JSpinner sendAttemptsLower;
    private com.mirth.connect.client.ui.components.MirthBlankableSpinner sendAttemptsUpper;
    private com.mirth.connect.client.ui.components.MirthTextField serverIdField;
    private javax.swing.JLabel serverIdLabel;
    // End of variables declaration//GEN-END:variables
}
