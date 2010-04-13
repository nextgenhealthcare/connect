/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package com.mirth.connect.connectors.http;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.connectors.ConnectorClass;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.QueuedSenderProperties;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.util.ConnectionTestResponse;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class HttpSender extends ConnectorClass {

    private final int VARIABLE_COLUMN = 0;
    private final int VALUE_COLUMN = 1;
    private final String VARIABLE_COLUMN_NAME = "Variable";
    private final String VALUE_COLUMN_NAME = "Value";
    private final String PAYLOAD_KEY = "$payload";
    private int propertiesLastIndex = -1;
    private int headerLastIndex = -1;
    /** Creates new form HTTPWriter */
    private HashMap channelList;

    public HttpSender() {
        name = HttpSenderProperties.name;
        initComponents();
        propertiesPane.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deselectRows(propertiesTable, deleteButton);
            }
        });
        headerVariablesPane.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deselectRows(headerVariablesTable, headerDeleteButton);
            }
        });
        deleteButton.setEnabled(false);
        headerDeleteButton.setEnabled(false);
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        properties.put(HttpSenderProperties.DATATYPE, name);
        properties.put(HttpSenderProperties.HTTP_URL, httpURL.getText());

        if (post.isSelected()) {
            properties.put(HttpSenderProperties.HTTP_METHOD, "post");
        } else if (get.isSelected()) {
            properties.put(HttpSenderProperties.HTTP_METHOD, "get");
        } else if (put.isSelected()) {
            properties.put(HttpSenderProperties.HTTP_METHOD, "put");
        } else if (delete.isSelected()) {
            properties.put(HttpSenderProperties.HTTP_METHOD, "delete");
        }

        if (multipartYesButton.isSelected()) {
            properties.put(HttpSenderProperties.HTTP_MULTIPART, UIConstants.YES_OPTION);
        } else {
            properties.put(HttpSenderProperties.HTTP_MULTIPART, UIConstants.NO_OPTION);
        }

        if (includeResponseHeadersYesButton.isSelected()) {
            properties.put(HttpSenderProperties.HTTP_INCLUDE_HEADERS_IN_RESPONSE, UIConstants.YES_OPTION);
        } else {
            properties.put(HttpSenderProperties.HTTP_INCLUDE_HEADERS_IN_RESPONSE, UIConstants.NO_OPTION);
        }

        properties.put(HttpSenderProperties.HTTP_REPLY_CHANNEL_ID, channelList.get((String) channelNames.getSelectedItem()));

        properties.put(QueuedSenderProperties.RECONNECT_INTERVAL, reconnectInterval.getText());

        if (usePersistentQueuesYesRadio.isSelected()) {
            properties.put(QueuedSenderProperties.USE_PERSISTENT_QUEUES, UIConstants.YES_OPTION);
        } else {
            properties.put(QueuedSenderProperties.USE_PERSISTENT_QUEUES, UIConstants.NO_OPTION);
        }

        if (rotateMessages.isSelected()) {
            properties.put(QueuedSenderProperties.ROTATE_QUEUE, UIConstants.YES_OPTION);
        } else {
            properties.put(QueuedSenderProperties.ROTATE_QUEUE, UIConstants.NO_OPTION);
        }

        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        properties.put(HttpSenderProperties.HTTP_PARAMETERS, serializer.toXML(getAdditionalProperties()));
        properties.put(HttpSenderProperties.HTTP_HEADERS, serializer.toXML(getHeaderProperties()));
        return properties;
    }

    public void setProperties(Properties props) {
        resetInvalidProperties();

        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();

        httpURL.setText((String) props.get(HttpSenderProperties.HTTP_URL));

        if (((String) props.get(HttpSenderProperties.HTTP_METHOD)).equalsIgnoreCase("post")) {
            post.setSelected(true);
        } else if (((String) props.get(HttpSenderProperties.HTTP_METHOD)).equalsIgnoreCase("get")) {
            get.setSelected(true);
        } else if (((String) props.get(HttpSenderProperties.HTTP_METHOD)).equalsIgnoreCase("put")) {
            put.setSelected(true);
        } else if (((String) props.get(HttpSenderProperties.HTTP_METHOD)).equalsIgnoreCase("delete")) {
            delete.setSelected(true);
        }

        if (((String) props.get(HttpSenderProperties.HTTP_MULTIPART)).equals(UIConstants.YES_OPTION)) {
            multipartYesButton.setSelected(true);
        } else {
            multipartNoButton.setSelected(true);
        }

        checkMultipartEnabled();

        if (((String) props.get(HttpSenderProperties.HTTP_INCLUDE_HEADERS_IN_RESPONSE)).equals(UIConstants.YES_OPTION)) {
            includeResponseHeadersYesButton.setSelected(true);
        } else {
            includeResponseHeadersNoButton.setSelected(true);
        }

        ObjectXMLSerializer serializer = new ObjectXMLSerializer();

        if (((String) props.get(HttpSenderProperties.HTTP_PARAMETERS)).length() > 0) {
            setAdditionalProperties((Properties) serializer.fromXML((String) props.get(HttpSenderProperties.HTTP_PARAMETERS)));
        } else {
            setAdditionalProperties(new Properties());
        }

        if (((String) props.get(HttpSenderProperties.HTTP_HEADERS)).length() > 0) {
            setHeaderProperties((Properties) serializer.fromXML((String) props.get(HttpSenderProperties.HTTP_HEADERS)));
        } else {
            setHeaderProperties(new Properties());
        }

        reconnectInterval.setText((String) props.get(QueuedSenderProperties.RECONNECT_INTERVAL));

        if (((String) props.get(QueuedSenderProperties.USE_PERSISTENT_QUEUES)).equals(UIConstants.YES_OPTION)) {
            usePersistentQueuesYesRadio.setSelected(true);
            usePersistentQueuesYesRadioActionPerformed(null);
        } else {
            usePersistentQueuesNoRadio.setSelected(true);
            usePersistentQueuesNoRadioActionPerformed(null);
        }

        if (((String) props.get(QueuedSenderProperties.ROTATE_QUEUE)).equals(UIConstants.YES_OPTION)) {
            rotateMessages.setSelected(true);
        } else {
            rotateMessages.setSelected(false);
        }

        ArrayList<String> channelNameArray = new ArrayList<String>();
        channelList = new HashMap();
        channelList.put("None", "sink");
        channelNameArray.add("None");

        String selectedChannelName = "None";

        for (Channel channel : parent.channels.values()) {
            if (((String) props.get(HttpSenderProperties.HTTP_REPLY_CHANNEL_ID)).equalsIgnoreCase(channel.getId())) {
                selectedChannelName = channel.getName();
            }

            channelList.put(channel.getName(), channel.getId());
            channelNameArray.add(channel.getName());
        }
        channelNames.setModel(new javax.swing.DefaultComboBoxModel(channelNameArray.toArray()));

        channelNames.setSelectedItem(selectedChannelName);

        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
    }

    public Properties getDefaults() {
        return new HttpSenderProperties().getDefaults();
    }

    public void setAdditionalProperties(Properties properties) {
        Object[][] tableData = new Object[properties.size()][2];

        propertiesTable = new MirthTable();

        int j = 0;
        Iterator i = properties.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            tableData[j][VARIABLE_COLUMN] = (String) entry.getKey();
            tableData[j][VALUE_COLUMN] = (String) entry.getValue();
            j++;
        }

        propertiesTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[]{VARIABLE_COLUMN_NAME, VALUE_COLUMN_NAME}) {

            boolean[] canEdit = new boolean[]{true, true};

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        propertiesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                if (getSelectedRow(propertiesTable) != -1) {
                    propertiesLastIndex = getSelectedRow(propertiesTable);
                    deleteButton.setEnabled(true);
                } else {
                    deleteButton.setEnabled(false);
                }
            }
        });

        class HTTPTableCellEditor extends AbstractCellEditor implements TableCellEditor {

            JComponent component = new JTextField();
            Object originalValue;
            boolean checkProperties;

            public HTTPTableCellEditor(boolean checkProperties) {
                super();
                this.checkProperties = checkProperties;
            }

            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                // 'value' is value contained in the cell located at (rowIndex,
                // vColIndex)
                originalValue = value;

                if (isSelected) {
                    // cell (and perhaps other cells) are selected
                }

                // Configure the component with the specified value
                ((JTextField) component).setText((String) value);

                // Return the configured component
                return component;
            }

            public Object getCellEditorValue() {
                return ((JTextField) component).getText();
            }

            public boolean stopCellEditing() {
                String s = (String) getCellEditorValue();

                if (checkProperties && (s.length() == 0 || checkUniqueProperty(s))) {
                    super.cancelCellEditing();
                } else {
                    parent.enableSave();
                }

                deleteButton.setEnabled(true);

                return super.stopCellEditing();
            }

            public boolean checkUniqueProperty(String property) {
                boolean exists = false;

                for (int i = 0; i < propertiesTable.getRowCount(); i++) {
                    if (propertiesTable.getValueAt(i, VARIABLE_COLUMN) != null && ((String) propertiesTable.getValueAt(i, VARIABLE_COLUMN)).equalsIgnoreCase(property)) {
                        exists = true;
                    }
                }

                return exists;
            }

            /**
             * Enables the editor only for double-clicks.
             */
            public boolean isCellEditable(EventObject evt) {
                if (evt instanceof MouseEvent && ((MouseEvent) evt).getClickCount() >= 2) {
                    deleteButton.setEnabled(false);
                    return true;
                }
                return false;
            }
        }
        ;

        // Set the custom cell editor for the Destination Name column.
        propertiesTable.getColumnModel().getColumn(propertiesTable.getColumnModel().getColumnIndex(VARIABLE_COLUMN_NAME)).setCellEditor(new HTTPTableCellEditor(true));

        // Set the custom cell editor for the Destination Name column.
        propertiesTable.getColumnModel().getColumn(propertiesTable.getColumnModel().getColumnIndex(VALUE_COLUMN_NAME)).setCellEditor(new HTTPTableCellEditor(false));

        propertiesTable.setSelectionMode(0);
        propertiesTable.setRowSelectionAllowed(true);
        propertiesTable.setRowHeight(UIConstants.ROW_HEIGHT);
        propertiesTable.setDragEnabled(false);
        propertiesTable.setOpaque(true);
        propertiesTable.setSortable(false);
        propertiesTable.getTableHeader().setReorderingAllowed(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            propertiesTable.setHighlighters(highlighter);
        }

        propertiesPane.setViewportView(propertiesTable);
    }

    public void setHeaderProperties(Properties properties) {
        Object[][] tableData = new Object[properties.size()][2];

        headerVariablesTable = new MirthTable();

        int j = 0;
        Iterator i = properties.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            tableData[j][VARIABLE_COLUMN] = (String) entry.getKey();
            tableData[j][VALUE_COLUMN] = (String) entry.getValue();
            j++;
        }

        headerVariablesTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[]{VARIABLE_COLUMN_NAME, VALUE_COLUMN_NAME}) {

            boolean[] canEdit = new boolean[]{true, true};

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        headerVariablesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                if (getSelectedRow(headerVariablesTable) != -1) {
                    headerLastIndex = getSelectedRow(headerVariablesTable);
                    headerDeleteButton.setEnabled(true);
                } else {
                    headerDeleteButton.setEnabled(false);
                }
            }
        });

        class HTTPTableCellEditor extends AbstractCellEditor implements TableCellEditor {

            JComponent component = new JTextField();
            Object originalValue;
            boolean checkProperties;

            public HTTPTableCellEditor(boolean checkProperties) {
                super();
                this.checkProperties = checkProperties;
            }

            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                // 'value' is value contained in the cell located at (rowIndex,
                // vColIndex)
                originalValue = value;

                if (isSelected) {
                    // cell (and perhaps other cells) are selected
                }

                // Configure the component with the specified value
                ((JTextField) component).setText((String) value);

                // Return the configured component
                return component;
            }

            public Object getCellEditorValue() {
                return ((JTextField) component).getText();
            }

            public boolean stopCellEditing() {
                String s = (String) getCellEditorValue();

                if (checkProperties && (s.length() == 0 || checkUniqueProperty(s))) {
                    super.cancelCellEditing();
                } else {
                    parent.enableSave();
                }

                headerDeleteButton.setEnabled(true);

                return super.stopCellEditing();
            }

            public boolean checkUniqueProperty(String property) {
                boolean exists = false;

                for (int i = 0; i < headerVariablesTable.getRowCount(); i++) {
                    if (headerVariablesTable.getValueAt(i, VARIABLE_COLUMN) != null && ((String) headerVariablesTable.getValueAt(i, VARIABLE_COLUMN)).equalsIgnoreCase(property)) {
                        exists = true;
                    }
                }

                return exists;
            }

            /**
             * Enables the editor only for double-clicks.
             */
            public boolean isCellEditable(EventObject evt) {
                if (evt instanceof MouseEvent && ((MouseEvent) evt).getClickCount() >= 2) {
                    headerDeleteButton.setEnabled(false);
                    return true;
                }
                return false;
            }
        }
        ;

        // Set the custom cell editor for the Destination Name column.
        headerVariablesTable.getColumnModel().getColumn(headerVariablesTable.getColumnModel().getColumnIndex(VARIABLE_COLUMN_NAME)).setCellEditor(new HTTPTableCellEditor(true));

        // Set the custom cell editor for the Destination Name column.
        headerVariablesTable.getColumnModel().getColumn(headerVariablesTable.getColumnModel().getColumnIndex(VALUE_COLUMN_NAME)).setCellEditor(new HTTPTableCellEditor(false));

        headerVariablesTable.setSelectionMode(0);
        headerVariablesTable.setRowSelectionAllowed(true);
        headerVariablesTable.setRowHeight(UIConstants.ROW_HEIGHT);
        headerVariablesTable.setDragEnabled(false);
        headerVariablesTable.setOpaque(true);
        headerVariablesTable.setSortable(false);
        headerVariablesTable.getTableHeader().setReorderingAllowed(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            headerVariablesTable.setHighlighters(highlighter);
        }

        headerVariablesPane.setViewportView(headerVariablesTable);
    }

    public Map getAdditionalProperties() {
        Properties properties = new Properties();

        for (int i = 0; i < propertiesTable.getRowCount(); i++) {
            if (((String) propertiesTable.getValueAt(i, VARIABLE_COLUMN)).length() > 0) {
                properties.put(((String) propertiesTable.getValueAt(i, VARIABLE_COLUMN)), ((String) propertiesTable.getValueAt(i, VALUE_COLUMN)));
            }
        }

        return properties;
    }

    public Map getHeaderProperties() {
        Properties properties = new Properties();

        for (int i = 0; i < headerVariablesTable.getRowCount(); i++) {
            if (((String) headerVariablesTable.getValueAt(i, VARIABLE_COLUMN)).length() > 0) {
                properties.put(((String) headerVariablesTable.getValueAt(i, VARIABLE_COLUMN)), ((String) headerVariablesTable.getValueAt(i, VALUE_COLUMN)));
            }
        }

        return properties;
    }

    /** Clears the selection in the table and sets the tasks appropriately */
    public void deselectRows(MirthTable table, JButton button) {
        table.clearSelection();
        button.setEnabled(false);
    }

    /** Get the currently selected table index */
    public int getSelectedRow(MirthTable table) {
        if (table.isEditing()) {
            return table.getEditingRow();
        } else {
            return table.getSelectedRow();
        }
    }

    /**
     * Get the name that should be used for a new property so that it is unique.
     */
    private String getNewPropertyName(MirthTable table) {
        String temp = "Property ";

        for (int i = 1; i <= table.getRowCount() + 1; i++) {
            boolean exists = false;
            for (int j = 0; j < table.getRowCount(); j++) {
                if (((String) table.getValueAt(j, VARIABLE_COLUMN)).equalsIgnoreCase(temp + i)) {
                    exists = true;
                }
            }
            if (!exists) {
                return temp + i;
            }
        }
        return "";
    }

    public boolean checkProperties(Properties props, boolean highlight) {
        resetInvalidProperties();
        boolean valid = true;

        if (((String) props.getProperty(HttpSenderProperties.HTTP_URL)).length() == 0) {
            valid = false;
            if (highlight) {
                httpURL.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (((String) props.get(QueuedSenderProperties.USE_PERSISTENT_QUEUES)).equals(UIConstants.YES_OPTION) && ((String) props.get(QueuedSenderProperties.RECONNECT_INTERVAL)).length() == 0) {
            valid = false;
            if (highlight) {
                reconnectInterval.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return valid;
    }

    private void resetInvalidProperties() {
        httpURL.setBackground(null);
    }

    public String doValidate(Properties props, boolean highlight) {
        String error = null;

        if (!checkProperties(props, highlight)) {
            error = "Error in the form for connector \"" + getName() + "\".\n\n";
        }

        return error;
    }

    private void checkMultipartEnabled() {
        if (post.isSelected()) {
            multipartLabel.setEnabled(true);
            multipartYesButton.setEnabled(true);
            multipartNoButton.setEnabled(true);
        } else {
            multipartLabel.setEnabled(false);
            multipartYesButton.setEnabled(false);
            multipartNoButton.setEnabled(false);
            multipartNoButton.setSelected(true);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        methodButtonGroup = new javax.swing.ButtonGroup();
        responseHeadersButtonGroup = new javax.swing.ButtonGroup();
        buttonGroup1 = new javax.swing.ButtonGroup();
        multipartButtonGroup = new javax.swing.ButtonGroup();
        jLabel7 = new javax.swing.JLabel();
        httpURL = new com.mirth.connect.client.ui.components.MirthTextField();
        newButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        propertiesPane = new javax.swing.JScrollPane();
        propertiesTable = new com.mirth.connect.client.ui.components.MirthTable();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        post = new com.mirth.connect.client.ui.components.MirthRadioButton();
        get = new com.mirth.connect.client.ui.components.MirthRadioButton();
        channelNames = new com.mirth.connect.client.ui.components.MirthComboBox();
        URL1 = new javax.swing.JLabel();
        headerVariablesPane = new javax.swing.JScrollPane();
        headerVariablesTable = new com.mirth.connect.client.ui.components.MirthTable();
        jLabel3 = new javax.swing.JLabel();
        headerNewButton = new javax.swing.JButton();
        headerDeleteButton = new javax.swing.JButton();
        responseHeadersLabel = new javax.swing.JLabel();
        includeResponseHeadersYesButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        includeResponseHeadersNoButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        rotateMessages = new com.mirth.connect.client.ui.components.MirthCheckBox();
        usePersistentQueuesNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        usePersistentQueuesYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        jLabel36 = new javax.swing.JLabel();
        reconnectIntervalLabel = new javax.swing.JLabel();
        reconnectInterval = new com.mirth.connect.client.ui.components.MirthTextField();
        put = new com.mirth.connect.client.ui.components.MirthRadioButton();
        delete = new com.mirth.connect.client.ui.components.MirthRadioButton();
        testConnection = new javax.swing.JButton();
        multipartLabel = new javax.swing.JLabel();
        multipartYesButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        multipartNoButton = new com.mirth.connect.client.ui.components.MirthRadioButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jLabel7.setText("URL:");

        httpURL.setToolTipText("Enter the URL of the HTTP server to send each message to.");

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

        propertiesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Property", "Value"
            }
        ));
        propertiesTable.setToolTipText("Request variables are encoded as x=y pairs as part of the request URL, separated from it by a '?' and from each other by an '&'.");
        propertiesPane.setViewportView(propertiesTable);

        jLabel2.setText("Request Variables:");

        jLabel1.setText("HTTP Method:");

        post.setBackground(new java.awt.Color(255, 255, 255));
        post.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        methodButtonGroup.add(post);
        post.setText("POST");
        post.setToolTipText("Selects whether the HTTP operation used to send each message.");
        post.setMargin(new java.awt.Insets(0, 0, 0, 0));
        post.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                postActionPerformed(evt);
            }
        });

        get.setBackground(new java.awt.Color(255, 255, 255));
        get.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        methodButtonGroup.add(get);
        get.setText("GET");
        get.setToolTipText("Selects whether the HTTP operation used to send each message.");
        get.setMargin(new java.awt.Insets(0, 0, 0, 0));
        get.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getActionPerformed(evt);
            }
        });

        channelNames.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        channelNames.setToolTipText("<html>Selects a channel to send the response from the HTTP server as a new inbound message<br> or None to ignore the response from the HTTP server.</html>");

        URL1.setText("Send Response to:");

        headerVariablesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Property", "Value"
            }
        ));
        headerVariablesTable.setToolTipText("Header variables are encoded as HTTP headers in the HTTP request sent to the server.");
        headerVariablesPane.setViewportView(headerVariablesTable);

        jLabel3.setText("Header Variables:");

        headerNewButton.setText("New");
        headerNewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                headerNewButtonActionPerformed(evt);
            }
        });

        headerDeleteButton.setText("Delete");
        headerDeleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                headerDeleteButtonActionPerformed(evt);
            }
        });

        responseHeadersLabel.setText("Include Response Headers:");

        includeResponseHeadersYesButton.setBackground(new java.awt.Color(255, 255, 255));
        includeResponseHeadersYesButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        responseHeadersButtonGroup.add(includeResponseHeadersYesButton);
        includeResponseHeadersYesButton.setText("Yes");
        includeResponseHeadersYesButton.setToolTipText("<html>Only enabled if Send Response To selects a channel.<br>If Include is selected, the HTTP headers of the response received are included in the message sent to the selected channel.<br>If Exclude is selected, the HTTP headers are not included.</html>");
        includeResponseHeadersYesButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        includeResponseHeadersYesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                includeResponseHeadersYesButtonActionPerformed(evt);
            }
        });

        includeResponseHeadersNoButton.setBackground(new java.awt.Color(255, 255, 255));
        includeResponseHeadersNoButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        responseHeadersButtonGroup.add(includeResponseHeadersNoButton);
        includeResponseHeadersNoButton.setText("No");
        includeResponseHeadersNoButton.setToolTipText("<html>Only enabled if Send Response To selects a channel.<br>If Include is selected, the HTTP headers of the response received are included in the message sent to the selected channel.<br>If Exclude is selected, the HTTP headers are not included.</html>");
        includeResponseHeadersNoButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        rotateMessages.setBackground(new java.awt.Color(255, 255, 255));
        rotateMessages.setText("Rotate Messages in Queue");
        rotateMessages.setToolTipText("<html>If checked, upon unsuccessful re-try, it will rotate and put the queued message to the back of the queue<br> in order to prevent it from clogging the queue and to let the other subsequent messages in queue be processed.<br>If the order of messages processed is important, this should be unchecked.</html>");

        usePersistentQueuesNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        usePersistentQueuesNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(usePersistentQueuesNoRadio);
        usePersistentQueuesNoRadio.setSelected(true);
        usePersistentQueuesNoRadio.setText("No");
        usePersistentQueuesNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        usePersistentQueuesNoRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usePersistentQueuesNoRadioActionPerformed(evt);
            }
        });

        usePersistentQueuesYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        usePersistentQueuesYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(usePersistentQueuesYesRadio);
        usePersistentQueuesYesRadio.setText("Yes");
        usePersistentQueuesYesRadio.setToolTipText("<html>If checked, the connector will store any messages that are unable to be successfully processed in a file-based queue.<br>Messages will be automatically resent until the queue is manually cleared or the message is successfully sent.<br>The default queue location is (Mirth Directory)/.mule/queuestore/(ChannelID),<br> where (Mirth Directory) is the main Mirth install root and (ChannelID) is the unique id of the current channel.</html>");
        usePersistentQueuesYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        usePersistentQueuesYesRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usePersistentQueuesYesRadioActionPerformed(evt);
            }
        });

        jLabel36.setText("Use Persistent Queues:");

        reconnectIntervalLabel.setText("Reconnect Interval (ms):");

        put.setBackground(new java.awt.Color(255, 255, 255));
        put.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        methodButtonGroup.add(put);
        put.setText("PUT");
        put.setToolTipText("Selects whether the HTTP operation used to send each message.");
        put.setMargin(new java.awt.Insets(0, 0, 0, 0));
        put.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                putActionPerformed(evt);
            }
        });

        delete.setBackground(new java.awt.Color(255, 255, 255));
        delete.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        methodButtonGroup.add(delete);
        delete.setText("DELETE");
        delete.setToolTipText("Selects whether the HTTP operation used to send each message.");
        delete.setMargin(new java.awt.Insets(0, 0, 0, 0));
        delete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteActionPerformed(evt);
            }
        });

        testConnection.setText("Test Connection");
        testConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testConnectionActionPerformed(evt);
            }
        });

        multipartLabel.setText("Multipart:");

        multipartYesButton.setBackground(new java.awt.Color(255, 255, 255));
        multipartYesButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        multipartButtonGroup.add(multipartYesButton);
        multipartYesButton.setText("Yes");
        multipartYesButton.setToolTipText("Set to use multipart in the Content-Type header. It can only be used with POST.");
        multipartYesButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        multipartNoButton.setBackground(new java.awt.Color(255, 255, 255));
        multipartNoButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        multipartButtonGroup.add(multipartNoButton);
        multipartNoButton.setText("No");
        multipartNoButton.setToolTipText("Set not to use multipart in the Content-Type header.");
        multipartNoButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(multipartLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(URL1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(responseHeadersLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel36, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(reconnectIntervalLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(multipartYesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(multipartNoButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(includeResponseHeadersYesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(includeResponseHeadersNoButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(httpURL, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(testConnection))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(post, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(get, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(put, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(delete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(channelNames, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(usePersistentQueuesYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(usePersistentQueuesNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(rotateMessages, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(reconnectInterval, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(14, 14, 14))
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(headerVariablesPane, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(headerNewButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(headerDeleteButton)))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addComponent(propertiesPane, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(deleteButton)
                                        .addComponent(newButton))))
                            .addContainerGap()))))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {deleteButton, newButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(httpURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(testConnection))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(post, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(get, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(put, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(delete, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(multipartLabel)
                    .addComponent(multipartYesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(multipartNoButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(URL1)
                    .addComponent(channelNames, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(includeResponseHeadersYesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(includeResponseHeadersNoButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(responseHeadersLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel36)
                    .addComponent(usePersistentQueuesYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usePersistentQueuesNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rotateMessages, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reconnectInterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reconnectIntervalLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(newButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteButton))
                    .addComponent(propertiesPane, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(headerVariablesPane, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE)
                    .addComponent(jLabel3)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(headerNewButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(headerDeleteButton)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void headerDeleteButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_headerDeleteButtonActionPerformed
    {//GEN-HEADEREND:event_headerDeleteButtonActionPerformed
        if (getSelectedRow(headerVariablesTable) != -1 && !headerVariablesTable.isEditing()) {
            ((DefaultTableModel) headerVariablesTable.getModel()).removeRow(getSelectedRow(headerVariablesTable));

            if (headerVariablesTable.getRowCount() != 0) {
                if (headerLastIndex == 0) {
                    headerVariablesTable.setRowSelectionInterval(0, 0);
                } else if (headerLastIndex == headerVariablesTable.getRowCount()) {
                    headerVariablesTable.setRowSelectionInterval(headerLastIndex - 1, headerLastIndex - 1);
                } else {
                    headerVariablesTable.setRowSelectionInterval(headerLastIndex, headerLastIndex);
                }
            }

            parent.enableSave();
        }
    }//GEN-LAST:event_headerDeleteButtonActionPerformed

    private void headerNewButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_headerNewButtonActionPerformed
    {//GEN-HEADEREND:event_headerNewButtonActionPerformed
        ((DefaultTableModel) headerVariablesTable.getModel()).addRow(new Object[]{getNewPropertyName(headerVariablesTable), ""});
        headerVariablesTable.setRowSelectionInterval(headerVariablesTable.getRowCount() - 1, headerVariablesTable.getRowCount() - 1);
        parent.enableSave();
    }//GEN-LAST:event_headerNewButtonActionPerformed

private void usePersistentQueuesNoRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usePersistentQueuesNoRadioActionPerformed
    rotateMessages.setEnabled(false);
    reconnectInterval.setEnabled(false);
    reconnectIntervalLabel.setEnabled(false);
}//GEN-LAST:event_usePersistentQueuesNoRadioActionPerformed

private void usePersistentQueuesYesRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usePersistentQueuesYesRadioActionPerformed
    rotateMessages.setEnabled(true);
    reconnectInterval.setEnabled(true);
    reconnectIntervalLabel.setEnabled(true);
}//GEN-LAST:event_usePersistentQueuesYesRadioActionPerformed

private void includeResponseHeadersYesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_includeResponseHeadersYesButtonActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_includeResponseHeadersYesButtonActionPerformed

private void postActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_postActionPerformed
    Properties properties = (Properties) getAdditionalProperties();
    if (!properties.containsKey(PAYLOAD_KEY)) {
        properties.put(PAYLOAD_KEY, "");
        setAdditionalProperties(properties);
    }
    checkMultipartEnabled();
}//GEN-LAST:event_postActionPerformed

private void getActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getActionPerformed
    Properties properties = (Properties) getAdditionalProperties();
    if (properties.containsKey(PAYLOAD_KEY)) {
        properties.remove(PAYLOAD_KEY);
        setAdditionalProperties(properties);
    }
    checkMultipartEnabled();
}//GEN-LAST:event_getActionPerformed

private void putActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_putActionPerformed
    Properties properties = (Properties) getAdditionalProperties();
    if (!properties.containsKey(PAYLOAD_KEY)) {
        properties.put(PAYLOAD_KEY, "");
        setAdditionalProperties(properties);
    }
    checkMultipartEnabled();
}//GEN-LAST:event_putActionPerformed

private void deleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteActionPerformed
    Properties properties = (Properties) getAdditionalProperties();
    if (properties.containsKey(PAYLOAD_KEY)) {
        properties.remove(PAYLOAD_KEY);
        setAdditionalProperties(properties);
    }
    checkMultipartEnabled();
}//GEN-LAST:event_deleteActionPerformed

private void testConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testConnectionActionPerformed
    parent.setWorking("Testing connection...", true);

    SwingWorker worker = new SwingWorker<Void, Void>() {

        public Void doInBackground() {

            try {
                ConnectionTestResponse response = (ConnectionTestResponse) parent.mirthClient.invokeConnectorService(name, "testConnection", getProperties());

                if (response == null) {
                    throw new ClientException("Failed to invoke service.");
                } else if (response.getType().equals(ConnectionTestResponse.Type.SUCCESS)) {
                    parent.alertInformation(parent, response.getMessage());
                } else {
                    parent.alertWarning(parent, response.getMessage());
                }

                return null;
            } catch (ClientException e) {
                parent.alertError(parent, e.getMessage());
                return null;
            }
        }

        public void done() {
            parent.setWorking("", false);
        }
    };

    worker.execute();
}//GEN-LAST:event_testConnectionActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_deleteButtonActionPerformed
    {// GEN-HEADEREND:event_deleteButtonActionPerformed
        if (getSelectedRow(propertiesTable) != -1 && !propertiesTable.isEditing()) {
            ((DefaultTableModel) propertiesTable.getModel()).removeRow(getSelectedRow(propertiesTable));

            if (propertiesTable.getRowCount() != 0) {
                if (propertiesLastIndex == 0) {
                    propertiesTable.setRowSelectionInterval(0, 0);
                } else if (propertiesLastIndex == propertiesTable.getRowCount()) {
                    propertiesTable.setRowSelectionInterval(propertiesLastIndex - 1, propertiesLastIndex - 1);
                } else {
                    propertiesTable.setRowSelectionInterval(propertiesLastIndex, propertiesLastIndex);
                }
            }

            parent.enableSave();
        }
    }// GEN-LAST:event_deleteButtonActionPerformed

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_newButtonActionPerformed
    {// GEN-HEADEREND:event_newButtonActionPerformed
        ((DefaultTableModel) propertiesTable.getModel()).addRow(new Object[]{getNewPropertyName(propertiesTable), ""});
        propertiesTable.setRowSelectionInterval(propertiesTable.getRowCount() - 1, propertiesTable.getRowCount() - 1);
        parent.enableSave();
    }// GEN-LAST:event_newButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel URL1;
    private javax.swing.ButtonGroup buttonGroup1;
    private com.mirth.connect.client.ui.components.MirthComboBox channelNames;
    private com.mirth.connect.client.ui.components.MirthRadioButton delete;
    private javax.swing.JButton deleteButton;
    private com.mirth.connect.client.ui.components.MirthRadioButton get;
    private javax.swing.JButton headerDeleteButton;
    private javax.swing.JButton headerNewButton;
    private javax.swing.JScrollPane headerVariablesPane;
    private com.mirth.connect.client.ui.components.MirthTable headerVariablesTable;
    private com.mirth.connect.client.ui.components.MirthTextField httpURL;
    private com.mirth.connect.client.ui.components.MirthRadioButton includeResponseHeadersNoButton;
    private com.mirth.connect.client.ui.components.MirthRadioButton includeResponseHeadersYesButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel7;
    private javax.swing.ButtonGroup methodButtonGroup;
    private javax.swing.ButtonGroup multipartButtonGroup;
    private javax.swing.JLabel multipartLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton multipartNoButton;
    private com.mirth.connect.client.ui.components.MirthRadioButton multipartYesButton;
    private javax.swing.JButton newButton;
    private com.mirth.connect.client.ui.components.MirthRadioButton post;
    private javax.swing.JScrollPane propertiesPane;
    private com.mirth.connect.client.ui.components.MirthTable propertiesTable;
    private com.mirth.connect.client.ui.components.MirthRadioButton put;
    private com.mirth.connect.client.ui.components.MirthTextField reconnectInterval;
    private javax.swing.JLabel reconnectIntervalLabel;
    private javax.swing.ButtonGroup responseHeadersButtonGroup;
    private javax.swing.JLabel responseHeadersLabel;
    private com.mirth.connect.client.ui.components.MirthCheckBox rotateMessages;
    private javax.swing.JButton testConnection;
    private com.mirth.connect.client.ui.components.MirthRadioButton usePersistentQueuesNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton usePersistentQueuesYesRadio;
    // End of variables declaration//GEN-END:variables
}
