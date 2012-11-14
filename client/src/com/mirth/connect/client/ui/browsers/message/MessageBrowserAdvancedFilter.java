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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.ItemSelectionTable;
import com.mirth.connect.client.ui.components.ItemSelectionTableModel;
import com.mirth.connect.client.ui.components.MirthComboBoxCellRenderer;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.channel.MetaDataColumnException;
import com.mirth.connect.donkey.model.channel.MetaDataColumnType;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.filters.MessageFilter;
import com.mirth.connect.model.filters.elements.MetaDataSearchElement;
import com.mirth.connect.model.filters.elements.MetaDataSearchOperator;

public class MessageBrowserAdvancedFilter extends javax.swing.JDialog {
    private Frame parent;
    private static final String SOURCE_CONNECTOR_NAME = "Source";
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
        messageIdField.setDocument(new MirthFieldConstraints(19, false, false, true));
        importIdField.setDocument(new MirthFieldConstraints(19, false, false, true));
    }

    private void initContentSearchTable() {
        contentSearchTable.setModel(new DefaultTableModel(new Object [][] {}, new String[] { "Content Type", "Contains" }) {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return true;
            }
        });
        
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
        column.setCellEditor(new DefaultCellEditor(new JComboBox(ContentType.values())));
        column.setCellRenderer(new MirthComboBoxCellRenderer(ContentType.values()));
        column.setMinWidth(CONTENT_TYPE_COLUMN_WIDTH);
        column.setMaxWidth(CONTENT_TYPE_COLUMN_WIDTH);
        
        deleteContentSearchButton.setEnabled(false);
    }
    
    private void initMetaDataSearchTable() {
        metaDataSearchTable.setModel(new DefaultTableModel(new Object [][] {}, new String[] { "MetaData", "Operator", "Value", "Ignore Case" }) {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                if (columnIndex == 3 && cachedMetaDataColumns.get(getValueAt(rowIndex, 0)).getType() != MetaDataColumnType.STRING) {
                    return false;
                }
                
                return true;
            }
            
            @Override
            public void setValueAt(Object value, int row, int column) {
                int metaDataColumnIndex = findColumn("MetaData");
                int operatorColumnIndex = findColumn("Operator");
                int valueColumnIndex = findColumn("Value");
                
                if (column == valueColumnIndex) {
                    MetaDataColumn metaDataColumn = cachedMetaDataColumns.get(getValueAt(row, metaDataColumnIndex));
                    
                    if (StringUtils.isNotEmpty((String) value)) {
                        try {
                            metaDataColumn.getType().castMetaDataFromString((String) value);
                        } catch (MetaDataColumnException e) {
                            parent.alertError(parent, "Invalid value for column type " + metaDataColumn.getType().toString());
                        }
                    }
                } else if (column == metaDataColumnIndex) {
                    if (!value.equals(getValueAt(row, metaDataColumnIndex))) {
                        MetaDataColumn metaDataColumn = cachedMetaDataColumns.get(value);
                        MetaDataSearchOperator operator = MetaDataSearchOperator.getDefaultForColumnType(metaDataColumn.getType());
                        
                        super.setValueAt(operator, row, operatorColumnIndex);
                    }
                    
                    super.setValueAt("", row, valueColumnIndex);
                }
                super.setValueAt(value, row, column);
            }
        });
        
        metaDataSearchTable.setSortable(false);
        metaDataSearchTable.getTableHeader().setReorderingAllowed(false);
        
        metaDataSearchTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                deleteMetaDataSearchButton.setEnabled(getSelectedRow(metaDataSearchTable) != -1);
            }
        });

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            metaDataSearchTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }
        
        List<MetaDataColumn> metaDataColumns = messageBrowser.getChannel().getProperties().getMetaDataColumns();
        cachedMetaDataColumns.clear();
        
        String[] metaDataNames = new String[metaDataColumns.size()];
        for (int i = 0; i < metaDataColumns.size(); i++) {
            String columnName = metaDataColumns.get(i).getName();
            metaDataNames[i] = columnName;
            cachedMetaDataColumns.put(columnName, metaDataColumns.get(i));
        }
        
        TableColumn metaDataColumn = metaDataSearchTable.getColumnModel().getColumn(0);
        metaDataColumn.setCellEditor(new DefaultCellEditor(new JComboBox(metaDataNames)));
        metaDataColumn.setCellRenderer(new MirthComboBoxCellRenderer(metaDataNames));
        metaDataColumn.setMinWidth(METADATA_NAME_COLUMN_WIDTH);
        metaDataColumn.setMaxWidth(METADATA_NAME_COLUMN_WIDTH * 2);
        metaDataColumn.setPreferredWidth(METADATA_NAME_COLUMN_WIDTH);
        
        // Need to create this custom editor since the combo box values are dynamic based on metadata column type. 
        DefaultCellEditor operatorEditor = new DefaultCellEditor(new JComboBox()) {
            private JComboBox comboBox;
            
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                
                MetaDataColumn metaDataColumn = cachedMetaDataColumns.get(table.getValueAt(row, 0));
                
                comboBox = new JComboBox(MetaDataSearchOperator.valuesForColumnType(metaDataColumn.getType()));
                comboBox.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        stopCellEditing();
                    }
                    
                });
                
                comboBox.setSelectedItem(value);
                // Return the configured component
                return comboBox;
            }
            
            public Object getCellEditorValue() {
                return comboBox.getSelectedItem();
            }
        };
        
        TableColumn operatorColumn = metaDataSearchTable.getColumnModel().getColumn(1);
        operatorColumn.setCellEditor(operatorEditor);
        operatorColumn.setCellRenderer(new MirthComboBoxCellRenderer(MetaDataSearchOperator.values()));
        operatorColumn.setMinWidth(METADATA_OPERATOR_COLUMN_WIDTH);
        operatorColumn.setMaxWidth(METADATA_OPERATOR_COLUMN_WIDTH);
        
        TableColumn caseColumn = metaDataSearchTable.getColumnModel().getColumn(3);
        caseColumn.setMinWidth(METADATA_CASE_COLUMN_WIDTH);
        caseColumn.setMaxWidth(METADATA_CASE_COLUMN_WIDTH);
        
        deleteMetaDataSearchButton.setEnabled(false);
    }

    public void loadChannel(Channel channel) {
        Map<Integer, String> connectors = new HashMap<Integer, String>();
        connectors.put(0, SOURCE_CONNECTOR_NAME);
        
        for (Connector connector : channel.getDestinationConnectors()) {
            connectors.put(connector.getMetaDataId(), connector.getName());
        }

        connectorTable.setModel(new ItemSelectionTableModel<Integer, String>(connectors, null, "Connector", "Included"));
        
        initMetaDataSearchTable();
    }

    protected void applySelectionsToFilter(MessageFilter messageFilter) {
        messageFilter.setMetaDataIds(getSelectedMetaDataIds());

        String messageIdText = messageIdField.getText();
        
        if (!StringUtils.isEmpty(messageIdText)) {
            try {
                messageFilter.setMessageId(Long.parseLong(messageIdText));
            } catch (NumberFormatException e) {
                parent.alertError(parent.messageBrowser, "Invalid message ID");
            }
        }

        messageFilter.setServerId(getServerId());
        
        String importIdText = importIdField.getText();
        if (!StringUtils.isEmpty(importIdText)) {
            try {
                messageFilter.setImportId(Long.parseLong(importIdText));
            } catch (NumberFormatException e) {
                parent.alertError(parent.messageBrowser, "Invalid import ID");
            }
        }

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
        messageFilter.setContentSearch(getContentSearch());
        
        try {
            messageFilter.setMetaDataSearch(getMetaDataSearch());
        } catch (MetaDataColumnException e) {
            parent.alertError(parent.messageBrowser, "Invalid value for column: " + e.getMetaDataColumn().getName());
        }
    }

    private List<Integer> getSelectedMetaDataIds() {
    	List<Integer> selectedMetaDataIds = ((ItemSelectionTableModel<Integer, String>) connectorTable.getModel()).getSelectedKeys();
    	if (selectedMetaDataIds.size() == connectorTable.getRowCount()) {
    		return null;
    	}
        return selectedMetaDataIds;
    }

    private String getServerId() {
        String serverId = serverIdField.getText();
        return (serverId.length() == 0) ? null : serverId;
    }

    private Map<ContentType, String> getContentSearch() {
        Map<ContentType, String> contentSearch = new HashMap<ContentType, String>();
        DefaultTableModel model = ((DefaultTableModel) contentSearchTable.getModel());
        int rowCount = model.getRowCount();

        for (int i = 0; i < rowCount; i++) {
            ContentType contentType = (ContentType) model.getValueAt(i, 0);
            String searchText = (String) model.getValueAt(i, 1);
            
            if (searchText.length() > 0) {
                contentSearch.put(contentType, searchText);
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
                    metaDataSearch.add(new MetaDataSearchElement(metaDataName, operator, column.getType().castMetaDataFromString(searchText), ignoreCase));
                }
            }
    
            return metaDataSearch;
        }
    }
    
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
        
        cachedSettings.put("messageIdField", messageIdField.getText());
        cachedSettings.put("serverIdField", serverIdField.getText());
        cachedSettings.put("importIdField", importIdField.getText());
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
            connectorData[row] = (Boolean) connectorModel.getValueAt(row, 1);
        }
        
        cachedSettings.put("connectorTable", connectorData);
    }
    
    public void loadSelections() {
        DefaultTableModel contentSearchModel = ((DefaultTableModel) contentSearchTable.getModel());
        DefaultTableModel metaDataSearchModel = ((DefaultTableModel) metaDataSearchTable.getModel());
        ItemSelectionTableModel<Integer, String> connectorModel = ((ItemSelectionTableModel<Integer, String>) connectorTable.getModel());
        messageIdField.setText((String) cachedSettings.get("messageIdField"));
        serverIdField.setText((String) cachedSettings.get("serverIdField"));
        importIdField.setText((String) cachedSettings.get("importIdField"));
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
            connectorModel.setValueAt(connectorData[row], row, 1);
        }
        
        cachedSettings.clear();
    }
    
    public void resetSelections() {
        messageIdField.setText("");
        serverIdField.setText("");
        importIdField.setText("");
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
        
        if (StringUtils.isNotEmpty(messageIdField.getText()) ||
            StringUtils.isNotEmpty(serverIdField.getText()) ||
            StringUtils.isNotEmpty(importIdField.getText()) ||
            !sendAttemptsLower.getValue().equals(0) || 
            StringUtils.isNotEmpty(sendAttemptsUpper.getValue().toString()) ||
            attachmentCheckBox.isSelected() ||
            ((DefaultTableModel) contentSearchTable.getModel()).getRowCount() != 0 ||
            ((DefaultTableModel) metaDataSearchTable.getModel()).getRowCount() != 0 ||
            model.getSelectedKeys().size() != model.getRowCount()) {
             hasAdvancedCriteria = true;       
        }
        
        return hasAdvancedCriteria;
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
        messageIdField = new com.mirth.connect.client.ui.components.MirthTextField();
        serverIdField = new com.mirth.connect.client.ui.components.MirthTextField();
        serverIdLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        cancelButton = new com.mirth.connect.client.ui.components.MirthButton();
        okButton = new com.mirth.connect.client.ui.components.MirthButton();
        jSeparator1 = new javax.swing.JSeparator();
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
        importIdField = new com.mirth.connect.client.ui.components.MirthTextField();

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

        containerPanel.setBackground(new java.awt.Color(255, 255, 255));

        messageIdLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        messageIdLabel.setText("Message ID:");

        serverIdField.setToolTipText("<html>The GUID of the message in the Mirth Connect database.<br>This can be retrieved from the Meta Data tab in the Message Browser.</html>");

        serverIdLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        serverIdLabel.setText("Server ID:");

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("# of Send Attempts");

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("is Between:");

        jLabel3.setText("and");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jSeparator1)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

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

        jScrollPane6.setViewportView(connectorTable);

        connectorSelectAll.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
        connectorSelectAll.setForeground(java.awt.Color.blue);
        connectorSelectAll.setText("<html><u>Select All</u></html>");
        connectorSelectAll.setToolTipText("Select all connectors below.");
        connectorSelectAll.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        connectorSelectAll.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                connectorSelectAllMouseReleased(evt);
            }
        });

        connectorDeselectAll.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
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
        jLabel7.setText("/");

        addMetaDataSearchButton.setText("New");
        addMetaDataSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMetaDataSearchButtonActionPerformed(evt);
            }
        });

        jScrollPane7.setViewportView(metaDataSearchTable);

        deleteMetaDataSearchButton.setText("Delete");
        deleteMetaDataSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteMetaDataSearchButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Attachment Only:");

        attachmentCheckBox.setBackground(new java.awt.Color(255, 255, 255));

        importIdLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        importIdLabel.setText("Import ID:");

        importIdField.setToolTipText("<html>The GUID of the message in the Mirth Connect database.<br>This can be retrieved from the Meta Data tab in the Message Browser.</html>");

        javax.swing.GroupLayout containerPanelLayout = new javax.swing.GroupLayout(containerPanel);
        containerPanel.setLayout(containerPanelLayout);
        containerPanelLayout.setHorizontalGroup(
            containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(containerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane6)
                    .addGroup(containerPanelLayout.createSequentialGroup()
                        .addComponent(serverIdLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(serverIdField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(containerPanelLayout.createSequentialGroup()
                        .addComponent(messageIdLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(messageIdField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(containerPanelLayout.createSequentialGroup()
                        .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1)
                            .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(containerPanelLayout.createSequentialGroup()
                                .addComponent(sendAttemptsLower, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sendAttemptsUpper, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(attachmentCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 230, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, containerPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(addMetaDataSearchButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(deleteMetaDataSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, containerPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(connectorSelectAll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(connectorDeselectAll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, containerPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(addContentSearchButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(deleteContentSearchButton, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, containerPanelLayout.createSequentialGroup()
                        .addComponent(importIdLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(importIdField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(messageIdLabel)
                    .addComponent(messageIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(serverIdLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(importIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(importIdLabel))
                .addGap(7, 7, 7)
                .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(containerPanelLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel3)
                        .addComponent(sendAttemptsLower, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(sendAttemptsUpper, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(attachmentCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(containerPanelLayout.createSequentialGroup()
                        .addComponent(addContentSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteContentSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(containerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(containerPanelLayout.createSequentialGroup()
                        .addComponent(addMetaDataSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteMetaDataSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 114, Short.MAX_VALUE))
                    .addGroup(containerPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addGap(7, 7, 7)))
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
            .addGroup(layout.createSequentialGroup()
                .addComponent(containerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        loadSelections();
        setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        
        
        setVisible(false);
        
        // if the user had typed in a value in the content search table, close the cell editor so that any value that was entered will be included in the search
        TableCellEditor cellEditor = contentSearchTable.getCellEditor();
        
        if (cellEditor != null) {
            cellEditor.stopCellEditing();
        }
    }//GEN-LAST:event_okButtonActionPerformed

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
        
        model.addRow(new Object[]{ContentType.RAW, ""});
        
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
        
        List<MetaDataColumn> metaDataColumns = messageBrowser.getChannel().getProperties().getMetaDataColumns();
        if (metaDataColumns.size() > 0) {
            MetaDataColumn metaDataColumn = metaDataColumns.get(0);
            MetaDataSearchOperator operator = MetaDataSearchOperator.getDefaultForColumnType(metaDataColumn.getType());
            
            model.addRow(new Object[]{metaDataColumn.getName(), operator, "", false});
            
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

    private class ListItem {
        private Object key;
        private String label;

        public ListItem(Object key, String label) {
            this.key = key;
            this.label = label;
        }

        public Object getKey() {
            return key;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.mirth.connect.client.ui.components.MirthButton addContentSearchButton;
    private com.mirth.connect.client.ui.components.MirthButton addMetaDataSearchButton;
    private com.mirth.connect.client.ui.components.MirthCheckBox attachmentCheckBox;
    private com.mirth.connect.client.ui.components.MirthButton cancelButton;
    private javax.swing.JLabel connectorDeselectAll;
    private javax.swing.JLabel connectorSelectAll;
    private com.mirth.connect.client.ui.components.MirthTable connectorTable;
    private javax.swing.JPanel containerPanel;
    private com.mirth.connect.client.ui.components.MirthTable contentSearchTable;
    private com.mirth.connect.client.ui.components.MirthButton deleteContentSearchButton;
    private com.mirth.connect.client.ui.components.MirthButton deleteMetaDataSearchButton;
    private com.mirth.connect.client.ui.components.MirthTextField importIdField;
    private javax.swing.JLabel importIdLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JSeparator jSeparator1;
    private com.mirth.connect.client.ui.components.MirthTextField messageIdField;
    private javax.swing.JLabel messageIdLabel;
    private com.mirth.connect.client.ui.components.MirthTable metaDataSearchTable;
    private com.mirth.connect.client.ui.components.MirthTable mirthTable1;
    private com.mirth.connect.client.ui.components.MirthTable mirthTable2;
    private com.mirth.connect.client.ui.components.MirthTable mirthTable3;
    private com.mirth.connect.client.ui.components.MirthButton okButton;
    private javax.swing.JSpinner sendAttemptsLower;
    private com.mirth.connect.client.ui.components.MirthBlankableSpinner sendAttemptsUpper;
    private com.mirth.connect.client.ui.components.MirthTextField serverIdField;
    private javax.swing.JLabel serverIdLabel;
    // End of variables declaration//GEN-END:variables
}
