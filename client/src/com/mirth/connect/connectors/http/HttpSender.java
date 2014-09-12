/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EventObject;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.ContentType;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.ConnectorTypeDecoration;
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

    private static final ImageIcon ICON_LOCK_X = new ImageIcon(Frame.class.getResource("images/lock_x.png"));
    private static final Color COLOR_SSL_NOT_CONFIGURED = new Color(0xFFF099);
    private static final String SSL_TOOL_TIP = "<html>The default system certificate store will be used for this connection.<br/>As a result, certain security options are not available and mutual<br/>authentication (two-way authentication) is not supported.</html>";

    private final int NAME_COLUMN = 0;
    private final int VALUE_COLUMN = 1;
    private final String NAME_COLUMN_NAME = "Name";
    private final String VALUE_COLUMN_NAME = "Value";
    private int propertiesLastIndex = -1;
    private int headerLastIndex = -1;
    private Frame parent;
    private SSLWarningPanel sslWarningPanel;

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

        urlField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                urlFieldChanged();
            }
        });

        sslWarningPanel = new SSLWarningPanel();

        contentTypeField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkContentEnabled();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkContentEnabled();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkContentEnabled();
            }
        });
    }

    @Override
    public String getConnectorName() {
        return new HttpDispatcherProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        HttpDispatcherProperties properties = new HttpDispatcherProperties();

        properties.setHost(urlField.getText());
        properties.setUseProxyServer(useProxyServerYesRadio.isSelected());
        properties.setProxyAddress(proxyAddressField.getText());
        properties.setProxyPort(proxyPortField.getText());

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

        properties.setUsePreemptiveAuthentication(authenticationPreemptiveCheckBox.isSelected());

        properties.setUsername(usernameField.getText());
        properties.setPassword(new String(passwordField.getPassword()));

        properties.setResponseXmlBody(responseContentXmlBodyRadio.isSelected());
        properties.setResponseParseMultipart(parseMultipartYesRadio.isSelected());
        properties.setResponseIncludeMetadata(includeMetadataYesRadio.isSelected());
        properties.setResponseBinaryMimeTypes(responseBinaryMimeTypesField.getText());
        properties.setResponseBinaryMimeTypesRegex(responseBinaryMimeTypesRegexCheckBox.isSelected());

        properties.setDataTypeBinary(dataTypeBinaryRadio.isSelected());
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
        urlFieldChanged();

        if (props.isUseProxyServer()) {
            useProxyServerYesRadio.setSelected(true);
            useProxyServerYesRadioActionPerformed(null);
        } else {
            useProxyServerNoRadio.setSelected(true);
            useProxyServerNoRadioActionPerformed(null);
        }

        proxyAddressField.setText(props.getProxyAddress());
        proxyPortField.setText(props.getProxyPort());

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

        authenticationPreemptiveCheckBox.setSelected(props.isUsePreemptiveAuthentication());

        usernameField.setText(props.getUsername());
        passwordField.setText(props.getPassword());

        if (props.isResponseXmlBody()) {
            responseContentXmlBodyRadio.setSelected(true);
            responseContentXmlBodyRadioActionPerformed(null);
        } else {
            responseContentPlainBodyRadio.setSelected(true);
            responseContentPlainBodyRadioActionPerformed(null);
        }

        if (props.isResponseParseMultipart()) {
            parseMultipartYesRadio.setSelected(true);
        } else {
            parseMultipartNoRadio.setSelected(true);
        }

        if (props.isResponseIncludeMetadata()) {
            includeMetadataYesRadio.setSelected(true);
        } else {
            includeMetadataNoRadio.setSelected(true);
        }

        responseBinaryMimeTypesField.setText(props.getResponseBinaryMimeTypes());
        responseBinaryMimeTypesRegexCheckBox.setSelected(props.isResponseBinaryMimeTypesRegex());

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

        if (props.isDataTypeBinary()) {
            dataTypeBinaryRadio.setSelected(true);
            dataTypeBinaryRadioActionPerformed(null);
        } else {
            dataTypeTextRadio.setSelected(true);
            dataTypeTextRadioActionPerformed(null);
        }

        contentTextArea.setText(props.getContent());

        parent.setPreviousSelectedEncodingForConnector(charsetEncodingCombobox, props.getCharset());

        checkContentEnabled();
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

        queryParametersTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[] {
                NAME_COLUMN_NAME, VALUE_COLUMN_NAME }) {

            boolean[] canEdit = new boolean[] { true, true };

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

        headersTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[] {
                NAME_COLUMN_NAME, VALUE_COLUMN_NAME }) {

            boolean[] canEdit = new boolean[] { true, true };

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

        if (props.isUseProxyServer()) {
            if (StringUtils.isBlank(props.getProxyAddress())) {
                valid = false;
                if (highlight) {
                    proxyAddressField.setBackground(UIConstants.INVALID_COLOR);
                }
            }

            if (StringUtils.isBlank(props.getProxyPort())) {
                valid = false;
                if (highlight) {
                    proxyPortField.setBackground(UIConstants.INVALID_COLOR);
                }
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

            if (isUsingFormUrlEncoded(props.getContentType())) {
                if (MapUtils.isEmpty(props.getParameters())) {
                    valid = false;
                }
            } else {
                if (props.getContent().length() == 0) {
                    valid = false;
                    if (highlight) {
                        contentTextArea.setBackground(UIConstants.INVALID_COLOR);
                    }
                }
            }
        }

        return valid;
    }

    @Override
    public void resetInvalidProperties() {
        urlField.setBackground(null);
        urlFieldChanged();
        proxyAddressField.setBackground(null);
        proxyPortField.setBackground(null);
        sendTimeoutField.setBackground(null);
        contentTypeField.setBackground(null);
        contentTextArea.setBackground(null);
    }

    @Override
    public ConnectorTypeDecoration getConnectorTypeDecoration() {
        boolean usingHttps = false;

        try {
            URI hostURI = new URI(urlField.getText());
            String hostScheme = hostURI.getScheme();
            if (hostScheme != null && hostScheme.toLowerCase().equals("https")) {
                usingHttps = true;
            }
        } catch (URISyntaxException e) {
            if (urlField.getText().toLowerCase().startsWith("https")) {
                usingHttps = true;
            }
        }

        if (usingHttps) {
            return new ConnectorTypeDecoration("(SSL Not Configured)", ICON_LOCK_X, SSL_TOOL_TIP, sslWarningPanel, COLOR_SSL_NOT_CONFIGURED);
        } else {
            return new ConnectorTypeDecoration();
        }
    }

    @Override
    public void doLocalDecoration(ConnectorTypeDecoration connectorTypeDecoration) {
        if (connectorTypeDecoration != null) {
            urlField.setIcon(connectorTypeDecoration.getIcon());
            urlField.setAlternateToolTipText(connectorTypeDecoration.getIconToolTipText());
            urlField.setIconPopupMenuComponent(connectorTypeDecoration.getIconPopupComponent());
            urlField.setBackground(connectorTypeDecoration.getHighlightColor());
        }
    }

    @Override
    public void handleConnectorServiceResponse(String method, Object response) {
        ConnectionTestResponse connectionTestResponse = (ConnectionTestResponse) response;

        if (connectionTestResponse == null) {
            parent.alertError(parent, "Failed to invoke service.");
        } else if (connectionTestResponse.getType().equals(ConnectionTestResponse.Type.SUCCESS)) {
            parent.alertInformation(parent, connectionTestResponse.getMessage());
        } else {
            parent.alertWarning(parent, connectionTestResponse.getMessage());
        }
    }

    private void urlFieldChanged() {
        decorateConnectorType();
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

    private boolean isUsingFormUrlEncoded(String contentType) {
        return StringUtils.startsWithIgnoreCase(contentType, ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
    }

    private void checkContentEnabled() {
        if (postButton.isSelected() || putButton.isSelected()) {
            contentTypeLabel.setEnabled(true);
            contentTypeField.setEnabled(true);

            if (isUsingFormUrlEncoded(contentTypeField.getText())) {
                multipartLabel.setEnabled(false);
                multipartYesButton.setEnabled(false);
                multipartNoButton.setEnabled(false);
                multipartNoButton.setSelected(true);

                dataTypeLabel.setEnabled(false);
                dataTypeBinaryRadio.setEnabled(false);
                dataTypeTextRadio.setEnabled(false);
                dataTypeTextRadio.setSelected(true);

                contentLabel.setEnabled(false);
                contentTextArea.setEnabled(false);
            } else {
                multipartLabel.setEnabled(postButton.isSelected());
                multipartYesButton.setEnabled(postButton.isSelected());
                multipartNoButton.setEnabled(postButton.isSelected());

                dataTypeLabel.setEnabled(true);
                dataTypeBinaryRadio.setEnabled(true);
                dataTypeTextRadio.setEnabled(true);

                contentLabel.setEnabled(true);
                contentTextArea.setEnabled(true);
            }

            if (dataTypeBinaryRadio.isSelected()) {
                dataTypeBinaryRadioActionPerformed(null);
            } else {
                dataTypeTextRadioActionPerformed(null);
            }
        } else {
            multipartLabel.setEnabled(postButton.isSelected());
            multipartYesButton.setEnabled(postButton.isSelected());
            multipartNoButton.setEnabled(postButton.isSelected());

            contentTypeLabel.setEnabled(false);
            contentTypeField.setEnabled(false);
            dataTypeLabel.setEnabled(false);
            dataTypeBinaryRadio.setEnabled(false);
            dataTypeTextRadio.setEnabled(false);
            charsetEncodingLabel.setEnabled(false);
            charsetEncodingCombobox.setEnabled(false);
            contentLabel.setEnabled(false);
            contentTextArea.setEnabled(false);
        }
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
        authenticationPreemptiveCheckBox.setEnabled(enabled);

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
        parseMultipartButtonGroup = new javax.swing.ButtonGroup();
        includeMetadataButtonGroup = new javax.swing.ButtonGroup();
        proxyTypeButtonGroup = new javax.swing.ButtonGroup();
        dataTypeButtonGroup = new javax.swing.ButtonGroup();
        urlLabel = new javax.swing.JLabel();
        urlField = new com.mirth.connect.client.ui.components.MirthIconTextField();
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
        responseContentXmlBodyRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        responseContentPlainBodyRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
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
        dataTypeBinaryRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
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
        parseMultipartLabel = new javax.swing.JLabel();
        parseMultipartYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        parseMultipartNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        includeMetadataLabel = new javax.swing.JLabel();
        includeMetadataYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        includeMetadataNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        useProxyServerLabel = new javax.swing.JLabel();
        useProxyServerYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        useProxyServerNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        proxyAddressLabel = new javax.swing.JLabel();
        proxyAddressField = new com.mirth.connect.client.ui.components.MirthTextField();
        proxyPortLabel = new javax.swing.JLabel();
        proxyPortField = new com.mirth.connect.client.ui.components.MirthTextField();
        authenticationPreemptiveCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        dataTypeLabel = new javax.swing.JLabel();
        authenticationYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        dataTypeTextRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        responseBinaryMimeTypesLabel = new javax.swing.JLabel();
        responseBinaryMimeTypesField = new com.mirth.connect.client.ui.components.MirthTextField();
        responseBinaryMimeTypesRegexCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();

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

        responseContentXmlBodyRadio.setBackground(new java.awt.Color(255, 255, 255));
        responseContentButtonGroup.add(responseContentXmlBodyRadio);
        responseContentXmlBodyRadio.setText("XML Body");
        responseContentXmlBodyRadio.setToolTipText("<html>If selected, the response content will include the response body as serialized XML.</html>");
        responseContentXmlBodyRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        responseContentXmlBodyRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                responseContentXmlBodyRadioActionPerformed(evt);
            }
        });

        responseContentPlainBodyRadio.setBackground(new java.awt.Color(255, 255, 255));
        responseContentButtonGroup.add(responseContentPlainBodyRadio);
        responseContentPlainBodyRadio.setText("Plain Body");
        responseContentPlainBodyRadio.setToolTipText("<html>If selected, the response content will only include the response body as a raw string.</html>");
        responseContentPlainBodyRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        responseContentPlainBodyRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                responseContentPlainBodyRadioActionPerformed(evt);
            }
        });

        putButton.setBackground(new java.awt.Color(255, 255, 255));
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
        multipartButtonGroup.add(multipartYesButton);
        multipartYesButton.setText("Yes");
        multipartYesButton.setToolTipText("Set to use multipart in the Content-Type header. Multipart can only be used with POST.");
        multipartYesButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        multipartNoButton.setBackground(new java.awt.Color(255, 255, 255));
        multipartButtonGroup.add(multipartNoButton);
        multipartNoButton.setText("No");
        multipartNoButton.setToolTipText("Set not to use multipart in the Content-Type header.");
        multipartNoButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        contentTextArea.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        contentTextArea.setToolTipText("The HTTP message body.");

        contentLabel.setText("Content:");

        contentTypeField.setToolTipText("<html>The HTTP message body MIME type to use. If<br/>application/x-www-form-urlencoded is used,<br/>the query parameters specified above will be<br/>automatically encoded into the request body.</html>");

        contentTypeLabel.setText("Content Type:");

        authenticationLabel.setText("Authentication:");

        dataTypeBinaryRadio.setBackground(new java.awt.Color(255, 255, 255));
        dataTypeButtonGroup.add(dataTypeBinaryRadio);
        dataTypeBinaryRadio.setText("Binary");
        dataTypeBinaryRadio.setToolTipText("<html>Select Binary if the outbound message is a Base64 string (will be decoded before it is sent out).<br/>Select Text if the outbound message is text (will be encoded with the specified character set encoding).</html>");
        dataTypeBinaryRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        dataTypeBinaryRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dataTypeBinaryRadioActionPerformed(evt);
            }
        });

        authenticationNoRadio.setBackground(new java.awt.Color(255, 255, 255));
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
        authenticationTypeButtonGroup.add(authenticationTypeDigestRadio);
        authenticationTypeDigestRadio.setText("Digest");
        authenticationTypeDigestRadio.setToolTipText("Use the digest authentication scheme.");
        authenticationTypeDigestRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        authenticationTypeBasicRadio.setBackground(new java.awt.Color(255, 255, 255));
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

        parseMultipartLabel.setText("Parse Multipart:");

        parseMultipartYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        parseMultipartButtonGroup.add(parseMultipartYesRadio);
        parseMultipartYesRadio.setText("Yes");
        parseMultipartYesRadio.setToolTipText("<html>Select Yes to automatically parse multipart responses into separate XML nodes.<br/>Select No to always keep the response body as a single XML node.</html>");
        parseMultipartYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        parseMultipartNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        parseMultipartButtonGroup.add(parseMultipartNoRadio);
        parseMultipartNoRadio.setText("No");
        parseMultipartNoRadio.setToolTipText("<html>Select Yes to automatically parse multipart responses into separate XML nodes.<br/>Select No to always keep the response body as a single XML node.</html>");
        parseMultipartNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        includeMetadataLabel.setText("Include Metadata:");

        includeMetadataYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        includeMetadataButtonGroup.add(includeMetadataYesRadio);
        includeMetadataYesRadio.setText("Yes");
        includeMetadataYesRadio.setToolTipText("<html>Select Yes to include response metadata (status<br/>line and headers) in the XML content. Note that<br/>regardless of this setting, the same metadata<br/>will be available in the connector map.</html>");
        includeMetadataYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        includeMetadataNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        includeMetadataButtonGroup.add(includeMetadataNoRadio);
        includeMetadataNoRadio.setText("No");
        includeMetadataNoRadio.setToolTipText("<html>Select Yes to include response metadata (status<br/>line and headers) in the XML content. Note that<br/>regardless of this setting, the same metadata<br/>will be available in the connector map.</html>");
        includeMetadataNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        useProxyServerLabel.setText("Use Proxy Server:");

        useProxyServerYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        proxyTypeButtonGroup.add(useProxyServerYesRadio);
        useProxyServerYesRadio.setText("Yes");
        useProxyServerYesRadio.setToolTipText("<html>If enabled, requests will be forwarded to the proxy<br/>server specified in the address/port fields below.</html>");
        useProxyServerYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useProxyServerYesRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useProxyServerYesRadioActionPerformed(evt);
            }
        });

        useProxyServerNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        proxyTypeButtonGroup.add(useProxyServerNoRadio);
        useProxyServerNoRadio.setText("No");
        useProxyServerNoRadio.setToolTipText("<html>If enabled, requests will be forwarded to the proxy<br/>server specified in the address/port fields below.</html>");
        useProxyServerNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useProxyServerNoRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useProxyServerNoRadioActionPerformed(evt);
            }
        });

        proxyAddressLabel.setText("Proxy Address:");

        proxyAddressField.setToolTipText("The domain name or IP address of the proxy server to connect to.");

        proxyPortLabel.setText("Proxy Port:");

        proxyPortField.setToolTipText("The port on which to connect to the proxy server.");

        authenticationPreemptiveCheckBox.setBackground(com.mirth.connect.client.ui.UIConstants.BACKGROUND_COLOR);
        authenticationPreemptiveCheckBox.setText("Preemptive");
        authenticationPreemptiveCheckBox.setToolTipText("<html>If checked, the authorization header will be sent to the server with the initial<br/>request. Otherwise, the header will only be sent when the server requests it.<br/>When using digest authentication, an Authorization header containing the<br/>realm/nonce/algorithm/qop values must be included in the Headers table.</html>");

        dataTypeLabel.setText("Data Type:");

        authenticationYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        authenticationButtonGroup.add(authenticationYesRadio);
        authenticationYesRadio.setText("Yes");
        authenticationYesRadio.setToolTipText("<html>Turning on authentication uses a username and password to communicate with the HTTP server.</html>");
        authenticationYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        authenticationYesRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                authenticationYesRadioActionPerformed(evt);
            }
        });

        dataTypeTextRadio.setBackground(new java.awt.Color(255, 255, 255));
        dataTypeButtonGroup.add(dataTypeTextRadio);
        dataTypeTextRadio.setText("Text");
        dataTypeTextRadio.setToolTipText("<html>Select Binary if the outbound message is a Base64 string (will be decoded before it is sent out).<br/>Select Text if the outbound message is text (will be encoded with the specified character set encoding).</html>");
        dataTypeTextRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        dataTypeTextRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dataTypeTextRadioActionPerformed(evt);
            }
        });

        responseBinaryMimeTypesLabel.setText("Binary MIME Types:");

        responseBinaryMimeTypesField.setToolTipText("<html>When a response comes in with a Content-Type header that<br/>matches one of these entries, the content will be encoded<br/>into a Base64 string. If Regular Expression is unchecked,<br/>specify multiple entries with commas. Otherwise, enter a<br/>valid regular expression to match MIME types against.</html>");
        responseBinaryMimeTypesField.setMinimumSize(new java.awt.Dimension(200, 21));
        responseBinaryMimeTypesField.setPreferredSize(new java.awt.Dimension(200, 21));

        responseBinaryMimeTypesRegexCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        responseBinaryMimeTypesRegexCheckBox.setText("Regular Expression");
        responseBinaryMimeTypesRegexCheckBox.setToolTipText("<html>When a response comes in with a Content-Type header that<br/>matches one of these entries, the content will be encoded<br/>into a Base64 string. If Regular Expression is unchecked,<br/>specify multiple entries with commas. Otherwise, enter a<br/>valid regular expression to match MIME types against.</html>");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(parseMultipartLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(sendTimeoutLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(includeMetadataLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(useProxyServerLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(responseContentLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(usernameLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(passwordLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(authenticationTypeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(charsetEncodingLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(queryParametersLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(methodLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(authenticationLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(responseBinaryMimeTypesLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(contentTypeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(contentLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(headersLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(multipartLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(urlLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(proxyAddressLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(proxyPortLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(dataTypeLabel, javax.swing.GroupLayout.Alignment.TRAILING))
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
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(includeMetadataYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(includeMetadataNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(parseMultipartYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(parseMultipartNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(useProxyServerYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(useProxyServerNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(responseContentPlainBodyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(responseContentXmlBodyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(sendTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                                    .addComponent(proxyAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(proxyPortField, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(urlField, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(authenticationTypeBasicRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(authenticationTypeDigestRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(authenticationPreemptiveCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(authenticationYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(authenticationNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(testConnection))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(dataTypeBinaryRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dataTypeTextRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(responseBinaryMimeTypesField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(responseBinaryMimeTypesRegexCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
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
                    .addComponent(useProxyServerLabel)
                    .addComponent(useProxyServerYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(useProxyServerNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(proxyAddressLabel)
                    .addComponent(proxyAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(proxyPortLabel)
                    .addComponent(proxyPortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                    .addComponent(sendTimeoutLabel)
                    .addComponent(sendTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(responseContentLabel)
                    .addComponent(responseContentXmlBodyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(responseContentPlainBodyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(parseMultipartLabel)
                    .addComponent(parseMultipartYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(parseMultipartNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(includeMetadataLabel)
                    .addComponent(includeMetadataYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(includeMetadataNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(responseBinaryMimeTypesLabel)
                    .addComponent(responseBinaryMimeTypesField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(responseBinaryMimeTypesRegexCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(authenticationLabel)
                    .addComponent(authenticationNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(authenticationYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(authenticationTypeLabel)
                    .addComponent(authenticationTypeBasicRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(authenticationTypeDigestRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(authenticationPreemptiveCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameLabel)
                    .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(queryParametersLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(queryParametersNewButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(queryParametersDeleteButton))
                    .addComponent(queryParametersPane, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(headersLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(headersNewButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(headersDeleteButton))
                    .addComponent(headersPane, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contentTypeLabel)
                    .addComponent(contentTypeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dataTypeLabel)
                    .addComponent(dataTypeBinaryRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dataTypeTextRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(charsetEncodingLabel)
                    .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(contentLabel)
                    .addComponent(contentTextArea, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE))
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
        ((DefaultTableModel) headersTable.getModel()).addRow(new Object[] {
                getNewPropertyName(headersTable), "" });
        headersTable.setRowSelectionInterval(headersTable.getRowCount() - 1, headersTable.getRowCount() - 1);
        parent.setSaveEnabled(true);
    }//GEN-LAST:event_headersNewButtonActionPerformed

    private void postButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_postButtonActionPerformed
        checkMultipartEnabled();
        checkContentEnabled();
        setQueryParametersEnabled(true);
    }//GEN-LAST:event_postButtonActionPerformed

    private void getButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getButtonActionPerformed
        checkMultipartEnabled();
        checkContentEnabled();
        setQueryParametersEnabled(true);
    }//GEN-LAST:event_getButtonActionPerformed

    private void putButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_putButtonActionPerformed
        checkMultipartEnabled();
        checkContentEnabled();
        setQueryParametersEnabled(true);
    }//GEN-LAST:event_putButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        checkMultipartEnabled();
        checkContentEnabled();
        setQueryParametersEnabled(true);
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void testConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testConnectionActionPerformed
        invokeConnectorService("testConnection", "Testing connection...", "Error testing HTTP connection: ");
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
        ((DefaultTableModel) queryParametersTable.getModel()).addRow(new Object[] {
                getNewPropertyName(queryParametersTable), "" });
        queryParametersTable.setRowSelectionInterval(queryParametersTable.getRowCount() - 1, queryParametersTable.getRowCount() - 1);
        parent.setSaveEnabled(true);
    }//GEN-LAST:event_queryParametersNewButtonActionPerformed

    private void authenticationYesRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_authenticationYesRadioActionPerformed
        setAuthenticationEnabled(true);
    }//GEN-LAST:event_authenticationYesRadioActionPerformed

    private void authenticationNoRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_authenticationNoRadioActionPerformed
        setAuthenticationEnabled(false);
    }//GEN-LAST:event_authenticationNoRadioActionPerformed

    private void responseContentPlainBodyRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_responseContentPlainBodyRadioActionPerformed
        parseMultipartLabel.setEnabled(false);
        parseMultipartYesRadio.setEnabled(false);
        parseMultipartNoRadio.setEnabled(false);
        includeMetadataLabel.setEnabled(false);
        includeMetadataYesRadio.setEnabled(false);
        includeMetadataNoRadio.setEnabled(false);
    }//GEN-LAST:event_responseContentPlainBodyRadioActionPerformed

    private void responseContentXmlBodyRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_responseContentXmlBodyRadioActionPerformed
        parseMultipartLabel.setEnabled(true);
        parseMultipartYesRadio.setEnabled(true);
        parseMultipartNoRadio.setEnabled(true);
        includeMetadataLabel.setEnabled(true);
        includeMetadataYesRadio.setEnabled(true);
        includeMetadataNoRadio.setEnabled(true);
    }//GEN-LAST:event_responseContentXmlBodyRadioActionPerformed

    private void useProxyServerYesRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useProxyServerYesRadioActionPerformed
        proxyAddressLabel.setEnabled(true);
        proxyAddressField.setEnabled(true);
        proxyPortLabel.setEnabled(true);
        proxyPortField.setEnabled(true);
    }//GEN-LAST:event_useProxyServerYesRadioActionPerformed

    private void useProxyServerNoRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useProxyServerNoRadioActionPerformed
        proxyAddressLabel.setEnabled(false);
        proxyAddressField.setEnabled(false);
        proxyPortLabel.setEnabled(false);
        proxyPortField.setEnabled(false);
    }//GEN-LAST:event_useProxyServerNoRadioActionPerformed

    private void dataTypeBinaryRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dataTypeBinaryRadioActionPerformed
        charsetEncodingLabel.setEnabled(false);
        charsetEncodingCombobox.setEnabled(false);
        charsetEncodingCombobox.setSelectedIndex(0);
    }//GEN-LAST:event_dataTypeBinaryRadioActionPerformed

    private void dataTypeTextRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dataTypeTextRadioActionPerformed
        if (postButton.isSelected() || putButton.isSelected()) {
            charsetEncodingLabel.setEnabled(true);
            charsetEncodingCombobox.setEnabled(true);
        }
    }//GEN-LAST:event_dataTypeTextRadioActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup authenticationButtonGroup;
    private javax.swing.JLabel authenticationLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton authenticationNoRadio;
    private com.mirth.connect.client.ui.components.MirthCheckBox authenticationPreemptiveCheckBox;
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
    private com.mirth.connect.client.ui.components.MirthRadioButton dataTypeBinaryRadio;
    private javax.swing.ButtonGroup dataTypeButtonGroup;
    private javax.swing.JLabel dataTypeLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton dataTypeTextRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton deleteButton;
    private com.mirth.connect.client.ui.components.MirthRadioButton getButton;
    private javax.swing.JButton headersDeleteButton;
    private javax.swing.JLabel headersLabel;
    private javax.swing.JButton headersNewButton;
    private javax.swing.JScrollPane headersPane;
    private com.mirth.connect.client.ui.components.MirthTable headersTable;
    private javax.swing.ButtonGroup includeMetadataButtonGroup;
    private javax.swing.JLabel includeMetadataLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton includeMetadataNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton includeMetadataYesRadio;
    private javax.swing.ButtonGroup methodButtonGroup;
    private javax.swing.JLabel methodLabel;
    private javax.swing.ButtonGroup multipartButtonGroup;
    private javax.swing.JLabel multipartLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton multipartNoButton;
    private com.mirth.connect.client.ui.components.MirthRadioButton multipartYesButton;
    private javax.swing.ButtonGroup parseMultipartButtonGroup;
    private javax.swing.JLabel parseMultipartLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton parseMultipartNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton parseMultipartYesRadio;
    private com.mirth.connect.client.ui.components.MirthPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton postButton;
    private com.mirth.connect.client.ui.components.MirthTextField proxyAddressField;
    private javax.swing.JLabel proxyAddressLabel;
    private com.mirth.connect.client.ui.components.MirthTextField proxyPortField;
    private javax.swing.JLabel proxyPortLabel;
    private javax.swing.ButtonGroup proxyTypeButtonGroup;
    private com.mirth.connect.client.ui.components.MirthRadioButton putButton;
    private javax.swing.JButton queryParametersDeleteButton;
    private javax.swing.JLabel queryParametersLabel;
    private javax.swing.JButton queryParametersNewButton;
    private javax.swing.JScrollPane queryParametersPane;
    private com.mirth.connect.client.ui.components.MirthTable queryParametersTable;
    private com.mirth.connect.client.ui.components.MirthTextField responseBinaryMimeTypesField;
    private javax.swing.JLabel responseBinaryMimeTypesLabel;
    private com.mirth.connect.client.ui.components.MirthCheckBox responseBinaryMimeTypesRegexCheckBox;
    private javax.swing.ButtonGroup responseContentButtonGroup;
    private javax.swing.JLabel responseContentLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton responseContentPlainBodyRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton responseContentXmlBodyRadio;
    private com.mirth.connect.client.ui.components.MirthTextField sendTimeoutField;
    private javax.swing.JLabel sendTimeoutLabel;
    private javax.swing.JButton testConnection;
    private com.mirth.connect.client.ui.components.MirthIconTextField urlField;
    private javax.swing.JLabel urlLabel;
    private javax.swing.ButtonGroup usePersistantQueuesButtonGroup;
    private javax.swing.JLabel useProxyServerLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton useProxyServerNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton useProxyServerYesRadio;
    private com.mirth.connect.client.ui.components.MirthTextField usernameField;
    private javax.swing.JLabel usernameLabel;
    // End of variables declaration//GEN-END:variables
}
