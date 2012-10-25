/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.util.EventObject;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.TextFieldCellEditor;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.util.ConnectionTestResponse;

public class HttpSender extends ConnectorSettingsPanel {

    private final int NAME_COLUMN = 0;
    private final int VALUE_COLUMN = 1;
    private final String NAME_COLUMN_NAME = "Name";
    private final String VALUE_COLUMN_NAME = "Value";
    private int propertiesLastIndex = -1;
    private int headerLastIndex = -1;
    private Frame parent;

    public HttpSender() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();

        parent.setupCharsetEncodingForConnector(charsetEncodingCombobox);

        queryParametersPane.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deselectRows(queryParametersTable, queryParametersDeleteButton);
            }
        });
        headersPane.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deselectRows(headersTable, headersDeleteButton);
            }
        });
        queryParametersDeleteButton.setEnabled(false);
        headersDeleteButton.setEnabled(false);
    }

    @Override
    public String getConnectorName() {
        return new HttpDispatcherProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        HttpDispatcherProperties properties = new HttpDispatcherProperties();

        properties.setHost(urlField.getText());

        if (postButton.isSelected()) {
            properties.setMethod("post");
        } else if (getButton.isSelected()) {
            properties.setMethod("get");
        } else if (putButton.isSelected()) {
            properties.setMethod("put");
        } else if (deleteButton.isSelected()) {
            properties.setMethod("delete");
        }

        properties.setMultipart(multipartYesButton.isSelected());

        properties.setSocketTimeout(sendTimeoutField.getText());

        properties.setUseAuthentication(authenticationYesRadio.isSelected());

        if (authenticationTypeBasicRadio.isSelected()) {
            properties.setAuthenticationType("Basic");
        } else {
            properties.setAuthenticationType("Digest");
        }
        
        properties.setUsername(usernameField.getText());
        properties.setPassword(new String(passwordField.getPassword()));

        properties.setIncludeHeadersInResponse(responseContentHeadersAndBodyButton.isSelected());

        properties.setCharset(parent.getSelectedEncodingForConnector(charsetEncodingCombobox));

        properties.setParameters(getAdditionalProperties());
        properties.setHeaders(getHeaderProperties());

        properties.setContentType(contentTypeField.getText());
        properties.setContent(contentTextArea.getText());

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        HttpDispatcherProperties props = (HttpDispatcherProperties) properties;

        urlField.setText(props.getHost());

        if (props.getMethod().equalsIgnoreCase("post")) {
            postButton.setSelected(true);
            postButtonActionPerformed(null);
        } else if (props.getMethod().equalsIgnoreCase("get")) {
            getButton.setSelected(true);
            getButtonActionPerformed(null);
        } else if (props.getMethod().equalsIgnoreCase("put")) {
            putButton.setSelected(true);
            putButtonActionPerformed(null);
        } else if (props.getMethod().equalsIgnoreCase("delete")) {
            deleteButton.setSelected(true);
            deleteButtonActionPerformed(null);
        }

        if (props.isMultipart()) {
            multipartYesButton.setSelected(true);
        } else {
            multipartNoButton.setSelected(true);
        }

        checkMultipartEnabled();

        sendTimeoutField.setText(props.getSocketTimeout());

        if (props.isUseAuthentication()) {
            authenticationYesRadio.setSelected(true);
            authenticationYesRadioActionPerformed(null);
        } else {
            authenticationNoRadio.setSelected(true);
            authenticationNoRadioActionPerformed(null);
        }

        if (props.getAuthenticationType().equalsIgnoreCase("Basic")) {
            authenticationTypeBasicRadio.setSelected(true);
        } else if (props.getAuthenticationType().equalsIgnoreCase("Digest")) {
            authenticationTypeDigestRadio.setSelected(true);
        }

        usernameField.setText(props.getUsername());
        passwordField.setText(props.getPassword());

        if (props.isIncludeHeadersInResponse()) {
            responseContentHeadersAndBodyButton.setSelected(true);
        } else {
            responseContentBodyOnlyButton.setSelected(true);
        }

        if (props.getParameters() != null) {
            setAdditionalProperties(props.getParameters());
        } else {
            setAdditionalProperties(new LinkedHashMap<String, String>());
        }

        if (props.getHeaders() != null) {
            setHeaderProperties(props.getHeaders());
        } else {
            setHeaderProperties(new LinkedHashMap<String, String>());
        }

        contentTypeField.setText(props.getContentType());

        contentTextArea.setText(props.getContent());

        parent.setPreviousSelectedEncodingForConnector(charsetEncodingCombobox, props.getCharset());
    }

    @Override
    public ConnectorProperties getDefaults() {
        return new HttpDispatcherProperties();
    }
    
    public void setAdditionalProperties(Map<String, String> properties) {
        Object[][] tableData = new Object[properties.size()][2];

        queryParametersTable = new MirthTable();

        int j = 0;
        Iterator i = properties.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            tableData[j][NAME_COLUMN] = (String) entry.getKey();
            tableData[j][VALUE_COLUMN] = (String) entry.getValue();
            j++;
        }

        queryParametersTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[]{NAME_COLUMN_NAME, VALUE_COLUMN_NAME}) {

            boolean[] canEdit = new boolean[]{true, true};

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        queryParametersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                if (getSelectedRow(queryParametersTable) != -1) {
                    propertiesLastIndex = getSelectedRow(queryParametersTable);
                    queryParametersDeleteButton.setEnabled(true);
                } else {
                    queryParametersDeleteButton.setEnabled(false);
                }
            }
        });

        class HTTPTableCellEditor extends TextFieldCellEditor {
            boolean checkProperties;

            public HTTPTableCellEditor(boolean checkProperties) {
                super();
                this.checkProperties = checkProperties;
            }

            public boolean checkUniqueProperty(String property) {
                boolean exists = false;

                for (int i = 0; i < queryParametersTable.getRowCount(); i++) {
                    if (queryParametersTable.getValueAt(i, NAME_COLUMN) != null && ((String) queryParametersTable.getValueAt(i, NAME_COLUMN)).equalsIgnoreCase(property)) {
                        exists = true;
                    }
                }

                return exists;
            }

            @Override
            public boolean isCellEditable(EventObject evt) {
                boolean editable = super.isCellEditable(evt);
                
                if (editable) {
                    queryParametersDeleteButton.setEnabled(false);
                }

                return editable; 
            }

            @Override
            protected boolean valueChanged(String value) {
                queryParametersDeleteButton.setEnabled(true);
                
                if (checkProperties && (value.length() == 0 || checkUniqueProperty(value))) {
                    return false;
                }

                parent.setSaveEnabled(true);
                return true;
            }
        }

        queryParametersTable.getColumnModel().getColumn(queryParametersTable.getColumnModel().getColumnIndex(NAME_COLUMN_NAME)).setCellEditor(new HTTPTableCellEditor(true));
        queryParametersTable.getColumnModel().getColumn(queryParametersTable.getColumnModel().getColumnIndex(VALUE_COLUMN_NAME)).setCellEditor(new HTTPTableCellEditor(false));
        queryParametersTable.setCustomEditorControls(true);
        
        queryParametersTable.setSelectionMode(0);
        queryParametersTable.setRowSelectionAllowed(true);
        queryParametersTable.setRowHeight(UIConstants.ROW_HEIGHT);
        queryParametersTable.setDragEnabled(false);
        queryParametersTable.setOpaque(true);
        queryParametersTable.setSortable(false);
        queryParametersTable.getTableHeader().setReorderingAllowed(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            queryParametersTable.setHighlighters(highlighter);
        }

        queryParametersPane.setViewportView(queryParametersTable);
    }

    public void setHeaderProperties(Map<String, String> properties) {
        Object[][] tableData = new Object[properties.size()][2];

        headersTable = new MirthTable();

        int j = 0;
        Iterator i = properties.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            tableData[j][NAME_COLUMN] = (String) entry.getKey();
            tableData[j][VALUE_COLUMN] = (String) entry.getValue();
            j++;
        }

        headersTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[]{NAME_COLUMN_NAME, VALUE_COLUMN_NAME}) {

            boolean[] canEdit = new boolean[]{true, true};

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        headersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                if (getSelectedRow(headersTable) != -1) {
                    headerLastIndex = getSelectedRow(headersTable);
                    headersDeleteButton.setEnabled(true);
                } else {
                    headersDeleteButton.setEnabled(false);
                }
            }
        });

        class HTTPTableCellEditor extends TextFieldCellEditor {
            boolean checkProperties;

            public HTTPTableCellEditor(boolean checkProperties) {
                super();
                this.checkProperties = checkProperties;
            }

            public boolean checkUniqueProperty(String property) {
                boolean exists = false;

                for (int i = 0; i < headersTable.getRowCount(); i++) {
                    if (headersTable.getValueAt(i, NAME_COLUMN) != null && ((String) headersTable.getValueAt(i, NAME_COLUMN)).equalsIgnoreCase(property)) {
                        exists = true;
                    }
                }

                return exists;
            }

            @Override
            public boolean isCellEditable(EventObject evt) {
                boolean editable = super.isCellEditable(evt);
                
                if (editable) {
                    headersDeleteButton.setEnabled(false);
                }

                return editable; 
            }

            @Override
            protected boolean valueChanged(String value) {
                headersDeleteButton.setEnabled(true);
                
                if (checkProperties && (value.length() == 0 || checkUniqueProperty(value))) {
                    return false;
                }
                
                parent.setSaveEnabled(true);
                return true;
            }
        }

        headersTable.getColumnModel().getColumn(headersTable.getColumnModel().getColumnIndex(NAME_COLUMN_NAME)).setCellEditor(new HTTPTableCellEditor(true));
        headersTable.getColumnModel().getColumn(headersTable.getColumnModel().getColumnIndex(VALUE_COLUMN_NAME)).setCellEditor(new HTTPTableCellEditor(false));
        headersTable.setCustomEditorControls(true);
        
        headersTable.setSelectionMode(0);
        headersTable.setRowSelectionAllowed(true);
        headersTable.setRowHeight(UIConstants.ROW_HEIGHT);
        headersTable.setDragEnabled(false);
        headersTable.setOpaque(true);
        headersTable.setSortable(false);
        headersTable.getTableHeader().setReorderingAllowed(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            headersTable.setHighlighters(highlighter);
        }

        headersPane.setViewportView(headersTable);
    }

    public Map<String, String> getAdditionalProperties() {
        Map<String, String> properties = new LinkedHashMap<String, String>();

        for (int i = 0; i < queryParametersTable.getRowCount(); i++) {
            if (((String) queryParametersTable.getValueAt(i, NAME_COLUMN)).length() > 0) {
                properties.put(((String) queryParametersTable.getValueAt(i, NAME_COLUMN)), ((String) queryParametersTable.getValueAt(i, VALUE_COLUMN)));
            }
        }

        return properties;
    }

    public Map<String, String> getHeaderProperties() {
        Map<String, String> properties = new LinkedHashMap<String, String>();
        
        for (int i = 0; i < headersTable.getRowCount(); i++) {
            if (((String) headersTable.getValueAt(i, NAME_COLUMN)).length() > 0) {
                properties.put(((String) headersTable.getValueAt(i, NAME_COLUMN)), ((String) headersTable.getValueAt(i, VALUE_COLUMN)));
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
                if (((String) table.getValueAt(j, NAME_COLUMN)).equalsIgnoreCase(temp + i)) {
                    exists = true;
                }
            }
            if (!exists) {
                return temp + i;
            }
        }
        return "";
    }

    @Override
    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        HttpDispatcherProperties props = (HttpDispatcherProperties) properties;
        
        boolean valid = true;

        if (props.getHost().length() == 0) {
            valid = false;
            if (highlight) {
                urlField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (props.getSocketTimeout().length() == 0) {
            valid = false;
            if (highlight) {
                sendTimeoutField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (props.getMethod().equalsIgnoreCase("post") || props.getMethod().equalsIgnoreCase("put")) {
            if (props.getContentType().length() == 0) {
                valid = false;
                if (highlight) {
                    contentTypeField.setBackground(UIConstants.INVALID_COLOR);
                }
            }

            if (props.getContent().length() == 0) {
                valid = false;
                if (highlight) {
                    contentTextArea.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }

        return valid;
    }

    @Override
    public void resetInvalidProperties() {
        urlField.setBackground(null);
        sendTimeoutField.setBackground(null);
        contentTypeField.setBackground(null);
        contentTextArea.setBackground(null);
    }

    private void checkMultipartEnabled() {
        if (postButton.isSelected()) {
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

    private void setContentEnabled(boolean enabled) {
        contentTypeLabel.setEnabled(enabled);
        contentTypeField.setEnabled(enabled);
        contentLabel.setEnabled(enabled);
        contentTextArea.setEnabled(enabled);
    }
    
    private void setQueryParametersEnabled(boolean enabled) {
        queryParametersLabel.setEnabled(enabled);
        queryParametersPane.setEnabled(enabled);
        queryParametersTable.setEnabled(enabled);
        queryParametersNewButton.setEnabled(enabled);
        
        deselectRows(queryParametersTable, queryParametersDeleteButton);
    }

    private void setAuthenticationEnabled(boolean enabled) {
        authenticationTypeLabel.setEnabled(enabled);
        authenticationTypeBasicRadio.setEnabled(enabled);
        authenticationTypeDigestRadio.setEnabled(enabled);

        usernameLabel.setEnabled(enabled);
        usernameField.setEnabled(enabled);

        if (!enabled) {
            usernameField.setText("");
        }

        passwordLabel.setEnabled(enabled);
        passwordField.setEnabled(enabled);

        if (!enabled) {
            passwordField.setText("");
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
        responseContentButtonGroup = new javax.swing.ButtonGroup();
        usePersistantQueuesButtonGroup = new javax.swing.ButtonGroup();
        multipartButtonGroup = new javax.swing.ButtonGroup();
        authenticationButtonGroup = new javax.swing.ButtonGroup();
        authenticationTypeButtonGroup = new javax.swing.ButtonGroup();
        urlLabel = new javax.swing.JLabel();
        urlField = new com.mirth.connect.client.ui.components.MirthTextField();
        queryParametersNewButton = new javax.swing.JButton();
        queryParametersDeleteButton = new javax.swing.JButton();
        queryParametersPane = new javax.swing.JScrollPane();
        queryParametersTable = new com.mirth.connect.client.ui.components.MirthTable();
        queryParametersLabel = new javax.swing.JLabel();
        methodLabel = new javax.swing.JLabel();
        postButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        getButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        headersPane = new javax.swing.JScrollPane();
        headersTable = new com.mirth.connect.client.ui.components.MirthTable();
        headersLabel = new javax.swing.JLabel();
        headersNewButton = new javax.swing.JButton();
        headersDeleteButton = new javax.swing.JButton();
        responseContentLabel = new javax.swing.JLabel();
        responseContentHeadersAndBodyButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        responseContentBodyOnlyButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        putButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        deleteButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        testConnection = new javax.swing.JButton();
        multipartLabel = new javax.swing.JLabel();
        multipartYesButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        multipartNoButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        contentTextArea = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea(true,false);
        contentLabel = new javax.swing.JLabel();
        contentTypeField = new com.mirth.connect.client.ui.components.MirthTextField();
        contentTypeLabel = new javax.swing.JLabel();
        authenticationLabel = new javax.swing.JLabel();
        authenticationYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        authenticationNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        usernameField = new com.mirth.connect.client.ui.components.MirthTextField();
        usernameLabel = new javax.swing.JLabel();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new com.mirth.connect.client.ui.components.MirthPasswordField();
        authenticationTypeDigestRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        authenticationTypeBasicRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        authenticationTypeLabel = new javax.swing.JLabel();
        charsetEncodingLabel = new javax.swing.JLabel();
        charsetEncodingCombobox = new com.mirth.connect.client.ui.components.MirthComboBox();
        sendTimeoutField = new com.mirth.connect.client.ui.components.MirthTextField();
        sendTimeoutLabel = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        urlLabel.setText("URL:");

        urlField.setToolTipText("Enter the URL of the HTTP server to send each message to.");

        queryParametersNewButton.setText("New");
        queryParametersNewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                queryParametersNewButtonActionPerformed(evt);
            }
        });

        queryParametersDeleteButton.setText("Delete");
        queryParametersDeleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                queryParametersDeleteButtonActionPerformed(evt);
            }
        });

        queryParametersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Value"
            }
        ));
        queryParametersTable.setToolTipText("Query parameters are encoded as x=y pairs as part of the request URL, separated from it by a '?' and from each other by an '&'.");
        queryParametersPane.setViewportView(queryParametersTable);

        queryParametersLabel.setText("Query Parameters:");

        methodLabel.setText("Method:");

        postButton.setBackground(new java.awt.Color(255, 255, 255));
        postButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        methodButtonGroup.add(postButton);
        postButton.setText("POST");
        postButton.setToolTipText("Selects the HTTP operation used to send each message.");
        postButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        postButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                postButtonActionPerformed(evt);
            }
        });

        getButton.setBackground(new java.awt.Color(255, 255, 255));
        getButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        methodButtonGroup.add(getButton);
        getButton.setText("GET");
        getButton.setToolTipText("Selects the HTTP operation used to send each message.");
        getButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        getButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getButtonActionPerformed(evt);
            }
        });

        headersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Value"
            }
        ));
        headersTable.setToolTipText("Header parameters are encoded as HTTP headers in the HTTP request sent to the server.");
        headersPane.setViewportView(headersTable);

        headersLabel.setText("Headers:");

        headersNewButton.setText("New");
        headersNewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                headersNewButtonActionPerformed(evt);
            }
        });

        headersDeleteButton.setText("Delete");
        headersDeleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                headersDeleteButtonActionPerformed(evt);
            }
        });

        responseContentLabel.setText("Response Content:");

        responseContentHeadersAndBodyButton.setBackground(new java.awt.Color(255, 255, 255));
        responseContentHeadersAndBodyButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        responseContentButtonGroup.add(responseContentHeadersAndBodyButton);
        responseContentHeadersAndBodyButton.setText("Headers and Body as XML");
        responseContentHeadersAndBodyButton.setToolTipText("<html>If selected, the response content will include the HTTP headers and body as XML.</html>");
        responseContentHeadersAndBodyButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        responseContentBodyOnlyButton.setBackground(new java.awt.Color(255, 255, 255));
        responseContentBodyOnlyButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        responseContentButtonGroup.add(responseContentBodyOnlyButton);
        responseContentBodyOnlyButton.setText("Body Only");
        responseContentBodyOnlyButton.setToolTipText("<html>If selected, the response content will only include the body as a string.</html>");
        responseContentBodyOnlyButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        putButton.setBackground(new java.awt.Color(255, 255, 255));
        putButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        methodButtonGroup.add(putButton);
        putButton.setText("PUT");
        putButton.setToolTipText("Selects the HTTP operation used to send each message.");
        putButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        putButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                putButtonActionPerformed(evt);
            }
        });

        deleteButton.setBackground(new java.awt.Color(255, 255, 255));
        deleteButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        methodButtonGroup.add(deleteButton);
        deleteButton.setText("DELETE");
        deleteButton.setToolTipText("Selects the HTTP operation used to send each message.");
        deleteButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
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
        multipartYesButton.setToolTipText("Set to use multipart in the Content-Type header. Multipart can only be used with POST.");
        multipartYesButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        multipartNoButton.setBackground(new java.awt.Color(255, 255, 255));
        multipartNoButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        multipartButtonGroup.add(multipartNoButton);
        multipartNoButton.setText("No");
        multipartNoButton.setToolTipText("Set not to use multipart in the Content-Type header.");
        multipartNoButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        contentTextArea.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        contentTextArea.setToolTipText("The HTTP message body.");

        contentLabel.setText("Content:");

        contentTypeField.setToolTipText("The HTTP message body MIME type.");

        contentTypeLabel.setText("Content Type:");

        authenticationLabel.setText("Authentication:");

        authenticationYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        authenticationYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        authenticationButtonGroup.add(authenticationYesRadio);
        authenticationYesRadio.setText("Yes");
        authenticationYesRadio.setToolTipText("<html>Turning on authentication uses a username and password to communicate with the HTTP server.</html>");
        authenticationYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        authenticationYesRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                authenticationYesRadioActionPerformed(evt);
            }
        });

        authenticationNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        authenticationNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        authenticationButtonGroup.add(authenticationNoRadio);
        authenticationNoRadio.setText("No");
        authenticationNoRadio.setToolTipText("<html>Turning on authentication uses a username and password to communicate with the HTTP server.</html>");
        authenticationNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        authenticationNoRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                authenticationNoRadioActionPerformed(evt);
            }
        });

        usernameField.setToolTipText("The username used to connect to the HTTP server.");

        usernameLabel.setText("Username:");

        passwordLabel.setText("Password:");

        passwordField.setToolTipText("The password used to connect to the HTTP server.");

        authenticationTypeDigestRadio.setBackground(new java.awt.Color(255, 255, 255));
        authenticationTypeDigestRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        authenticationTypeButtonGroup.add(authenticationTypeDigestRadio);
        authenticationTypeDigestRadio.setText("Digest");
        authenticationTypeDigestRadio.setToolTipText("Use the digest authentication scheme.");
        authenticationTypeDigestRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        authenticationTypeBasicRadio.setBackground(new java.awt.Color(255, 255, 255));
        authenticationTypeBasicRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        authenticationTypeButtonGroup.add(authenticationTypeBasicRadio);
        authenticationTypeBasicRadio.setText("Basic");
        authenticationTypeBasicRadio.setToolTipText("Use the basic authentication scheme.");
        authenticationTypeBasicRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        authenticationTypeLabel.setText("Authentication Type:");

        charsetEncodingLabel.setText("Charset Encoding:");

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "default", "utf-8", "iso-8859-1", "utf-16 (le)", "utf-16 (be)", "utf-16 (bom)", "us-ascii" }));
        charsetEncodingCombobox.setToolTipText("<html>Select the character set encoding used by the sender of the message,<br> or Default to assume the default character set encoding for the JVM running Mirth.</html>");

        sendTimeoutField.setToolTipText("<html>Sets the socket timeout (SO_TIMEOUT) in milliseconds to be used when executing the method.<br>A timeout value of zero is interpreted as an infinite timeout.</html>");

        sendTimeoutLabel.setText("Send Timeout (ms):");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sendTimeoutLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(charsetEncodingLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(authenticationTypeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(contentTypeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(authenticationLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(usernameLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(passwordLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(contentLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(multipartLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(responseContentLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(methodLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(queryParametersLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(headersLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(urlLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(queryParametersPane)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(queryParametersDeleteButton)
                            .addComponent(queryParametersNewButton)))
                    .addComponent(contentTextArea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(contentTypeField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(headersPane))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(headersNewButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(headersDeleteButton)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(authenticationTypeBasicRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(authenticationTypeDigestRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(authenticationYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(authenticationNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(responseContentBodyOnlyButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(responseContentHeadersAndBodyButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(sendTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(urlField, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(testConnection))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(postButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(getButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(putButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(multipartYesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(multipartNoButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(38, 38, 38)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {queryParametersDeleteButton, queryParametersNewButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(urlLabel)
                    .addComponent(urlField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(testConnection))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(methodLabel)
                    .addComponent(postButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(getButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(putButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(multipartLabel)
                    .addComponent(multipartYesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(multipartNoButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sendTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sendTimeoutLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(responseContentHeadersAndBodyButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(responseContentLabel)
                    .addComponent(responseContentBodyOnlyButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(authenticationYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(authenticationNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(authenticationLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(authenticationTypeBasicRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(authenticationTypeDigestRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(authenticationTypeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usernameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(charsetEncodingLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(queryParametersLabel)
                            .addComponent(queryParametersNewButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(queryParametersDeleteButton))
                    .addComponent(queryParametersPane, javax.swing.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(headersLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(headersNewButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(headersDeleteButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(headersPane, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(contentTypeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(contentTypeLabel))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(contentLabel)
                    .addComponent(contentTextArea, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void headersDeleteButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_headersDeleteButtonActionPerformed
    {//GEN-HEADEREND:event_headersDeleteButtonActionPerformed
        if (getSelectedRow(headersTable) != -1 && !headersTable.isEditing()) {
            ((DefaultTableModel) headersTable.getModel()).removeRow(getSelectedRow(headersTable));

            if (headersTable.getRowCount() != 0) {
                if (headerLastIndex == 0) {
                    headersTable.setRowSelectionInterval(0, 0);
                } else if (headerLastIndex == headersTable.getRowCount()) {
                    headersTable.setRowSelectionInterval(headerLastIndex - 1, headerLastIndex - 1);
                } else {
                    headersTable.setRowSelectionInterval(headerLastIndex, headerLastIndex);
                }
            }

            parent.setSaveEnabled(true);
        }
    }//GEN-LAST:event_headersDeleteButtonActionPerformed

    private void headersNewButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_headersNewButtonActionPerformed
    {//GEN-HEADEREND:event_headersNewButtonActionPerformed
        ((DefaultTableModel) headersTable.getModel()).addRow(new Object[]{getNewPropertyName(headersTable), ""});
        headersTable.setRowSelectionInterval(headersTable.getRowCount() - 1, headersTable.getRowCount() - 1);
        parent.setSaveEnabled(true);
    }//GEN-LAST:event_headersNewButtonActionPerformed

private void postButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_postButtonActionPerformed
    setContentEnabled(true);
    checkMultipartEnabled();
    setQueryParametersEnabled(true);
}//GEN-LAST:event_postButtonActionPerformed

private void getButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getButtonActionPerformed
    setContentEnabled(false);
    checkMultipartEnabled();
    setQueryParametersEnabled(true);
}//GEN-LAST:event_getButtonActionPerformed

private void putButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_putButtonActionPerformed
    setContentEnabled(true);
    checkMultipartEnabled();
    setQueryParametersEnabled(true);
}//GEN-LAST:event_putButtonActionPerformed

private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
    setContentEnabled(false);
    checkMultipartEnabled();
    setQueryParametersEnabled(true);
}//GEN-LAST:event_deleteButtonActionPerformed

private void testConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testConnectionActionPerformed
    final String workingId = parent.startWorking("Testing connection...");

    SwingWorker worker = new SwingWorker<Void, Void>() {

        public Void doInBackground() {

            try {
                ConnectionTestResponse response = (ConnectionTestResponse) parent.mirthClient.invokeConnectorService(getConnectorName(), "testConnection", getProperties());

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
            parent.stopWorking(workingId);
        }
    };

    worker.execute();
}//GEN-LAST:event_testConnectionActionPerformed

private void queryParametersDeleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_queryParametersDeleteButtonActionPerformed
    if (getSelectedRow(queryParametersTable) != -1 && !queryParametersTable.isEditing()) {
        ((DefaultTableModel) queryParametersTable.getModel()).removeRow(getSelectedRow(queryParametersTable));

        if (queryParametersTable.getRowCount() != 0) {
            if (propertiesLastIndex == 0) {
                queryParametersTable.setRowSelectionInterval(0, 0);
            } else if (propertiesLastIndex == queryParametersTable.getRowCount()) {
                queryParametersTable.setRowSelectionInterval(propertiesLastIndex - 1, propertiesLastIndex - 1);
            } else {
                queryParametersTable.setRowSelectionInterval(propertiesLastIndex, propertiesLastIndex);
            }
        }

        parent.setSaveEnabled(true);
    }
}//GEN-LAST:event_queryParametersDeleteButtonActionPerformed

private void queryParametersNewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_queryParametersNewButtonActionPerformed
    ((DefaultTableModel) queryParametersTable.getModel()).addRow(new Object[]{getNewPropertyName(queryParametersTable), ""});
    queryParametersTable.setRowSelectionInterval(queryParametersTable.getRowCount() - 1, queryParametersTable.getRowCount() - 1);
    parent.setSaveEnabled(true);
}//GEN-LAST:event_queryParametersNewButtonActionPerformed

private void authenticationYesRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_authenticationYesRadioActionPerformed
    setAuthenticationEnabled(true);
}//GEN-LAST:event_authenticationYesRadioActionPerformed

private void authenticationNoRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_authenticationNoRadioActionPerformed
    setAuthenticationEnabled(false);
}//GEN-LAST:event_authenticationNoRadioActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup authenticationButtonGroup;
    private javax.swing.JLabel authenticationLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton authenticationNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton authenticationTypeBasicRadio;
    private javax.swing.ButtonGroup authenticationTypeButtonGroup;
    private com.mirth.connect.client.ui.components.MirthRadioButton authenticationTypeDigestRadio;
    private javax.swing.JLabel authenticationTypeLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton authenticationYesRadio;
    private com.mirth.connect.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private javax.swing.JLabel charsetEncodingLabel;
    private javax.swing.JLabel contentLabel;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea contentTextArea;
    private com.mirth.connect.client.ui.components.MirthTextField contentTypeField;
    private javax.swing.JLabel contentTypeLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton deleteButton;
    private com.mirth.connect.client.ui.components.MirthRadioButton getButton;
    private javax.swing.JButton headersDeleteButton;
    private javax.swing.JLabel headersLabel;
    private javax.swing.JButton headersNewButton;
    private javax.swing.JScrollPane headersPane;
    private com.mirth.connect.client.ui.components.MirthTable headersTable;
    private javax.swing.ButtonGroup methodButtonGroup;
    private javax.swing.JLabel methodLabel;
    private javax.swing.ButtonGroup multipartButtonGroup;
    private javax.swing.JLabel multipartLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton multipartNoButton;
    private com.mirth.connect.client.ui.components.MirthRadioButton multipartYesButton;
    private com.mirth.connect.client.ui.components.MirthPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton postButton;
    private com.mirth.connect.client.ui.components.MirthRadioButton putButton;
    private javax.swing.JButton queryParametersDeleteButton;
    private javax.swing.JLabel queryParametersLabel;
    private javax.swing.JButton queryParametersNewButton;
    private javax.swing.JScrollPane queryParametersPane;
    private com.mirth.connect.client.ui.components.MirthTable queryParametersTable;
    private com.mirth.connect.client.ui.components.MirthRadioButton responseContentBodyOnlyButton;
    private javax.swing.ButtonGroup responseContentButtonGroup;
    private com.mirth.connect.client.ui.components.MirthRadioButton responseContentHeadersAndBodyButton;
    private javax.swing.JLabel responseContentLabel;
    private com.mirth.connect.client.ui.components.MirthTextField sendTimeoutField;
    private javax.swing.JLabel sendTimeoutLabel;
    private javax.swing.JButton testConnection;
    private com.mirth.connect.client.ui.components.MirthTextField urlField;
    private javax.swing.JLabel urlLabel;
    private javax.swing.ButtonGroup usePersistantQueuesButtonGroup;
    private com.mirth.connect.client.ui.components.MirthTextField usernameField;
    private javax.swing.JLabel usernameLabel;
    // End of variables declaration//GEN-END:variables
}
