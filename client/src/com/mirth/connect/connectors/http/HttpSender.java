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
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.ConnectorTypeDecoration;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.TextFieldCellEditor;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthCheckBox;
import com.mirth.connect.client.ui.components.MirthComboBox;
import com.mirth.connect.client.ui.components.MirthIconTextField;
import com.mirth.connect.client.ui.components.MirthPasswordField;
import com.mirth.connect.client.ui.components.MirthRadioButton;
import com.mirth.connect.client.ui.components.MirthSyntaxTextArea;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.client.ui.panels.connectors.ResponseHandler;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.model.Connector.Mode;
import com.mirth.connect.util.ConnectionTestResponse;

import net.miginfocom.swing.MigLayout;

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

        parent.setupCharsetEncodingForConnector(charsetEncodingCombobox, true);

        queryParametersPane.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent evt) {
                deselectRows(queryParametersTable, queryParametersDeleteButton);
            }
        });
        headersPane.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent evt) {
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
        
        initToolTips();
        initLayout();
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
        } else if (patchButton.isSelected()) {
            properties.setMethod("patch");
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

        properties.setParametersMap(getProperties(queryParametersTable));
        properties.setUseParametersVariable(useQueryParamsVariableRadio.isSelected());
        properties.setParametersVariable(queryParamsVariableField.getText());
        
        properties.setHeadersMap(getProperties(headersTable));
        properties.setUseHeadersVariable(useHeadersVariableRadio.isSelected());
        properties.setHeadersVariable(headersVariableField.getText());

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
        } else if (props.getMethod().equalsIgnoreCase("patch")) {
            patchButton.setSelected(true);
            patchButtonActionPerformed(null);
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

        if (props.getParametersMap() != null) {
            setParameters(props.getParametersMap());
        } else {
            setParameters(new LinkedHashMap<String, List<String>>());
        }
        if (props.isUseParametersVariable()) {
            useQueryParamsVariableRadio.setSelected(true);
        } else {
            useQueryParamsTableRadio.setSelected(true);
        }
        queryParamsVariableField.setText(props.getParametersVariable());
        useQueryParamsVariableFieldsEnabled(props.isUseParametersVariable());

        if (props.getHeadersMap() != null) {
            setHeaders(props.getHeadersMap());
        } else {
            setHeaders(new LinkedHashMap<String, List<String>>());
        }
        if (props.isUseHeadersVariable()) {
            useHeadersVariableRadio.setSelected(true);
        } else {
            useHeadersTableRadio.setSelected(true);
        }
        headersVariableField.setText(props.getHeadersVariable());
        useHeadersVariableFieldsEnabled(props.isUseHeadersVariable());

        contentTypeField.setText(props.getContentType());

        if (props.isDataTypeBinary()) {
            dataTypeBinaryRadio.setSelected(true);
            dataTypeBinaryRadioActionPerformed(null);
        } else {
            dataTypeTextRadio.setSelected(true);
            dataTypeTextRadioActionPerformed(null);
        }

        contentTextArea.setText(props.getContent());

        parent.setPreviousSelectedEncodingForConnector(charsetEncodingCombobox, props.getCharset(), true);

        checkContentEnabled();
    }

    @Override
    public ConnectorProperties getDefaults() {
        return new HttpDispatcherProperties();
    }

    public void setParameters(Map<String, List<String>> properties) {
        int size = 0;
        for (List<String> property : properties.values()) {
            size += property.size();
        }

        Object[][] tableData = new Object[size][2];

        queryParametersTable = new MirthTable();

        int j = 0;
        Iterator<Entry<String, List<String>>> i = properties.entrySet().iterator();
        while (i.hasNext()) {
            Entry<String, List<String>> entry = (Entry<String, List<String>>) i.next();

            for (String keyValue : (ArrayList<String>) entry.getValue()) {
                tableData[j][NAME_COLUMN] = (String) entry.getKey();
                tableData[j][VALUE_COLUMN] = keyValue;
                j++;
            }
        }

        queryParametersTable.setModel(new DefaultTableModel(tableData, new String[] {
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

                if (checkProperties && (value.length() == 0)) {
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

    public void setHeaders(Map<String, List<String>> properties) {
        int size = 0;
        for (List<String> property : properties.values()) {
            size += property.size();
        }

        Object[][] tableData = new Object[size][2];

        headersTable = new MirthTable();

        int j = 0;
        Iterator<Entry<String, List<String>>> i = properties.entrySet().iterator();
        while (i.hasNext()) {
            Entry<String, List<String>> entry = (Entry<String, List<String>>) i.next();
            for (String keyValue : (List<String>) entry.getValue()) {
                tableData[j][NAME_COLUMN] = (String) entry.getKey();
                tableData[j][VALUE_COLUMN] = keyValue;
                j++;
            }
        }

        headersTable.setModel(new DefaultTableModel(tableData, new String[] {
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

                if (checkProperties && (value.length() == 0)) {
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

    private Map<String, List<String>> getProperties(JTable table) {
        Map<String, List<String>> properties = new LinkedHashMap<String, List<String>>();

        for (int i = 0; i < table.getRowCount(); i++) {
            String key = (String) table.getValueAt(i, NAME_COLUMN);

            List<String> propertiesList = properties.get(key);

            if (propertiesList == null) {
                propertiesList = new ArrayList<String>();
                properties.put(key, propertiesList);
            }

            propertiesList.add((String) table.getValueAt(i, VALUE_COLUMN));
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

        if (props.getMethod().equalsIgnoreCase("post") || props.getMethod().equalsIgnoreCase("put") || props.getMethod().equalsIgnoreCase("patch")) {
            if (props.getContentType().length() == 0) {
                valid = false;
                if (highlight) {
                    contentTypeField.setBackground(UIConstants.INVALID_COLOR);
                }
            }

            if (isUsingFormUrlEncoded(props.getContentType())) {
                if (MapUtils.isEmpty(props.getParametersMap())) {
                    valid = false;
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
            return new ConnectorTypeDecoration(Mode.DESTINATION, "(SSL Not Configured)", ICON_LOCK_X, SSL_TOOL_TIP, sslWarningPanel, COLOR_SSL_NOT_CONFIGURED);
        } else {
            return new ConnectorTypeDecoration(Mode.DESTINATION);
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
        if (postButton.isSelected() || putButton.isSelected() || patchButton.isSelected()) {
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

    private void initComponents() {
        methodButtonGroup = new ButtonGroup();
        responseContentButtonGroup = new ButtonGroup();
        usePersistantQueuesButtonGroup = new ButtonGroup();
        multipartButtonGroup = new ButtonGroup();
        authenticationButtonGroup = new ButtonGroup();
        authenticationTypeButtonGroup = new ButtonGroup();
        parseMultipartButtonGroup = new ButtonGroup();
        includeMetadataButtonGroup = new ButtonGroup();
        proxyTypeButtonGroup = new ButtonGroup();
        dataTypeButtonGroup = new ButtonGroup();
        urlLabel = new JLabel();
        urlField = new MirthIconTextField();
        queryParametersNewButton = new JButton();
        queryParametersDeleteButton = new JButton();
        queryParametersPane = new JScrollPane();
        queryParametersTable = new MirthTable();
        queryParametersLabel = new JLabel();
        queryParamsVariableField = new MirthTextField();
        useQueryParamsTableRadio = new MirthRadioButton();
        useQueryParamsVariableRadio = new MirthRadioButton();
        methodLabel = new JLabel();
        postButton = new MirthRadioButton();
        getButton = new MirthRadioButton();
        headersPane = new JScrollPane();
        headersTable = new MirthTable();
        headersLabel = new JLabel();
        headersNewButton = new JButton();
        headersDeleteButton = new JButton();
        headersVariableField = new MirthTextField();
        useHeadersTableRadio = new MirthRadioButton();
        useHeadersVariableRadio = new MirthRadioButton();
        responseContentLabel = new JLabel();
        responseContentXmlBodyRadio = new MirthRadioButton();
        responseContentPlainBodyRadio = new MirthRadioButton();
        putButton = new MirthRadioButton();
        deleteButton = new MirthRadioButton();
        testConnection = new JButton();
        multipartLabel = new JLabel();
        multipartYesButton = new MirthRadioButton();
        multipartNoButton = new MirthRadioButton();
        contentTextArea = new MirthSyntaxTextArea(true);
        contentLabel = new JLabel();
        contentTypeField = new MirthTextField();
        contentTypeLabel = new JLabel();
        authenticationLabel = new JLabel();
        dataTypeBinaryRadio = new MirthRadioButton();
        authenticationNoRadio = new MirthRadioButton();
        usernameField = new MirthTextField();
        usernameLabel = new JLabel();
        passwordLabel = new JLabel();
        passwordField = new MirthPasswordField();
        authenticationTypeDigestRadio = new MirthRadioButton();
        authenticationTypeBasicRadio = new MirthRadioButton();
        authenticationTypeLabel = new JLabel();
        charsetEncodingLabel = new JLabel();
        charsetEncodingCombobox = new MirthComboBox<String>();
        sendTimeoutField = new MirthTextField();
        sendTimeoutLabel = new JLabel();
        parseMultipartLabel = new JLabel();
        parseMultipartYesRadio = new MirthRadioButton();
        parseMultipartNoRadio = new MirthRadioButton();
        includeMetadataLabel = new JLabel();
        includeMetadataYesRadio = new MirthRadioButton();
        includeMetadataNoRadio = new MirthRadioButton();
        useProxyServerLabel = new JLabel();
        useProxyServerYesRadio = new MirthRadioButton();
        useProxyServerNoRadio = new MirthRadioButton();
        proxyAddressLabel = new JLabel();
        proxyAddressField = new MirthTextField();
        proxyPortLabel = new JLabel();
        proxyPortField = new MirthTextField();
        authenticationPreemptiveCheckBox = new MirthCheckBox();
        dataTypeLabel = new JLabel();
        authenticationYesRadio = new MirthRadioButton();
        dataTypeTextRadio = new MirthRadioButton();
        responseBinaryMimeTypesLabel = new JLabel();
        responseBinaryMimeTypesField = new MirthTextField();
        responseBinaryMimeTypesRegexCheckBox = new MirthCheckBox();
        patchButton = new MirthRadioButton();

        setBackground(new Color(255, 255, 255));
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        urlLabel.setText("URL:");

        queryParametersNewButton.setText("New");
        queryParametersNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                queryParametersNewButtonActionPerformed(evt);
            }
        });

        queryParametersDeleteButton.setText("Delete");
        queryParametersDeleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                queryParametersDeleteButtonActionPerformed(evt);
            }
        });

        queryParametersTable.setModel(new DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Value"
            }
        ));
        queryParametersPane.setViewportView(queryParametersTable);

        queryParametersLabel.setText("Query Parameters:");

        useQueryParamsTableRadio.setText("Use Table");
        useQueryParamsTableRadio.setBackground(new Color(255, 255, 255));
        useQueryParamsTableRadio.addActionListener(event -> {
            useQueryParamsVariableFieldsEnabled(false);
        });
        
        useQueryParamsVariableRadio.setText("Use Map:");
        useQueryParamsVariableRadio.setBackground(new Color(255, 255, 255));
        useQueryParamsVariableRadio.addActionListener(event -> {
            useQueryParamsVariableFieldsEnabled(true);
        });
        
        ButtonGroup queryParamsButtonGroup = new ButtonGroup();
        queryParamsButtonGroup.add(useQueryParamsTableRadio);
        queryParamsButtonGroup.add(useQueryParamsVariableRadio);

        methodLabel.setText("Method:");

        postButton.setBackground(new Color(255, 255, 255));
        methodButtonGroup.add(postButton);
        postButton.setText("POST");
        postButton.setMargin(new Insets(0, 0, 0, 0));
        postButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                postButtonActionPerformed(evt);
            }
        });

        getButton.setBackground(new Color(255, 255, 255));
        methodButtonGroup.add(getButton);
        getButton.setText("GET");
        getButton.setMargin(new Insets(0, 0, 0, 0));
        getButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                getButtonActionPerformed(evt);
            }
        });

        headersTable.setModel(new DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Value"
            }
        ));
        headersPane.setViewportView(headersTable);

        headersLabel.setText("Headers:");

        headersNewButton.setText("New");
        headersNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                headersNewButtonActionPerformed(evt);
            }
        });

        headersDeleteButton.setText("Delete");
        headersDeleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                headersDeleteButtonActionPerformed(evt);
            }
        });
        useHeadersTableRadio.setText("Use Table");
        useHeadersTableRadio.setBackground(new Color(255, 255, 255));
        useHeadersTableRadio.addActionListener(event -> {
            useHeadersVariableFieldsEnabled(false);
        });
        
        useHeadersVariableRadio.setText("Use Map:");
        useHeadersVariableRadio.setBackground(new Color(255, 255, 255));
        useHeadersVariableRadio.addActionListener(event -> {
            useHeadersVariableFieldsEnabled(true);
        });
        
        ButtonGroup headersButtonGroup = new ButtonGroup();
        headersButtonGroup.add(useHeadersTableRadio);
        headersButtonGroup.add(useHeadersVariableRadio);
        
        responseContentLabel.setText("Response Content:");

        responseContentXmlBodyRadio.setBackground(new Color(255, 255, 255));
        responseContentButtonGroup.add(responseContentXmlBodyRadio);
        responseContentXmlBodyRadio.setText("XML Body");
        responseContentXmlBodyRadio.setMargin(new Insets(0, 0, 0, 0));
        responseContentXmlBodyRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                responseContentXmlBodyRadioActionPerformed(evt);
            }
        });

        responseContentPlainBodyRadio.setBackground(new Color(255, 255, 255));
        responseContentButtonGroup.add(responseContentPlainBodyRadio);
        responseContentPlainBodyRadio.setText("Plain Body");
        responseContentPlainBodyRadio.setMargin(new Insets(0, 0, 0, 0));
        responseContentPlainBodyRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                responseContentPlainBodyRadioActionPerformed(evt);
            }
        });

        putButton.setBackground(new Color(255, 255, 255));
        methodButtonGroup.add(putButton);
        putButton.setText("PUT");
        putButton.setMargin(new Insets(0, 0, 0, 0));
        putButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                putButtonActionPerformed(evt);
            }
        });

        deleteButton.setBackground(new Color(255, 255, 255));
        methodButtonGroup.add(deleteButton);
        deleteButton.setText("DELETE");
        deleteButton.setMargin(new Insets(0, 0, 0, 0));
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        testConnection.setText("Test Connection");
        testConnection.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                testConnectionActionPerformed(evt);
            }
        });

        multipartLabel.setText("Multipart:");

        multipartYesButton.setBackground(new Color(255, 255, 255));
        multipartButtonGroup.add(multipartYesButton);
        multipartYesButton.setText("Yes");
        multipartYesButton.setMargin(new Insets(0, 0, 0, 0));

        multipartNoButton.setBackground(new Color(255, 255, 255));
        multipartButtonGroup.add(multipartNoButton);
        multipartNoButton.setText("No");
        multipartNoButton.setMargin(new Insets(0, 0, 0, 0));

        contentTextArea.setBorder(BorderFactory.createEtchedBorder());

        contentLabel.setText("Content:");

        contentTypeLabel.setText("Content Type:");

        authenticationLabel.setText("Authentication:");

        dataTypeBinaryRadio.setBackground(new Color(255, 255, 255));
        dataTypeButtonGroup.add(dataTypeBinaryRadio);
        dataTypeBinaryRadio.setText("Binary");
        dataTypeBinaryRadio.setMargin(new Insets(0, 0, 0, 0));
        dataTypeBinaryRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                dataTypeBinaryRadioActionPerformed(evt);
            }
        });

        authenticationNoRadio.setBackground(new Color(255, 255, 255));
        authenticationButtonGroup.add(authenticationNoRadio);
        authenticationNoRadio.setText("No");
        authenticationNoRadio.setMargin(new Insets(0, 0, 0, 0));
        authenticationNoRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                authenticationNoRadioActionPerformed(evt);
            }
        });

        usernameLabel.setText("Username:");

        passwordLabel.setText("Password:");

        authenticationTypeDigestRadio.setBackground(new Color(255, 255, 255));
        authenticationTypeButtonGroup.add(authenticationTypeDigestRadio);
        authenticationTypeDigestRadio.setText("Digest");
        authenticationTypeDigestRadio.setMargin(new Insets(0, 0, 0, 0));

        authenticationTypeBasicRadio.setBackground(new Color(255, 255, 255));
        authenticationTypeButtonGroup.add(authenticationTypeBasicRadio);
        authenticationTypeBasicRadio.setText("Basic");
        authenticationTypeBasicRadio.setMargin(new Insets(0, 0, 0, 0));

        authenticationTypeLabel.setText("Authentication Type:");

        charsetEncodingLabel.setText("Charset Encoding:");

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "default", "utf-8", "iso-8859-1", "utf-16 (le)", "utf-16 (be)", "utf-16 (bom)", "us-ascii" }));

        sendTimeoutLabel.setText("Send Timeout (ms):");

        parseMultipartLabel.setText("Parse Multipart:");

        parseMultipartYesRadio.setBackground(new Color(255, 255, 255));
        parseMultipartButtonGroup.add(parseMultipartYesRadio);
        parseMultipartYesRadio.setText("Yes");
        parseMultipartYesRadio.setMargin(new Insets(0, 0, 0, 0));

        parseMultipartNoRadio.setBackground(new Color(255, 255, 255));
        parseMultipartButtonGroup.add(parseMultipartNoRadio);
        parseMultipartNoRadio.setText("No");
        parseMultipartNoRadio.setMargin(new Insets(0, 0, 0, 0));

        includeMetadataLabel.setText("Include Metadata:");

        includeMetadataYesRadio.setBackground(new Color(255, 255, 255));
        includeMetadataButtonGroup.add(includeMetadataYesRadio);
        includeMetadataYesRadio.setText("Yes");
        includeMetadataYesRadio.setMargin(new Insets(0, 0, 0, 0));

        includeMetadataNoRadio.setBackground(new Color(255, 255, 255));
        includeMetadataButtonGroup.add(includeMetadataNoRadio);
        includeMetadataNoRadio.setText("No");
        includeMetadataNoRadio.setMargin(new Insets(0, 0, 0, 0));

        useProxyServerLabel.setText("Use Proxy Server:");

        useProxyServerYesRadio.setBackground(new Color(255, 255, 255));
        proxyTypeButtonGroup.add(useProxyServerYesRadio);
        useProxyServerYesRadio.setText("Yes");
        useProxyServerYesRadio.setMargin(new Insets(0, 0, 0, 0));
        useProxyServerYesRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                useProxyServerYesRadioActionPerformed(evt);
            }
        });

        useProxyServerNoRadio.setBackground(new Color(255, 255, 255));
        proxyTypeButtonGroup.add(useProxyServerNoRadio);
        useProxyServerNoRadio.setText("No");
        useProxyServerNoRadio.setMargin(new Insets(0, 0, 0, 0));
        useProxyServerNoRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                useProxyServerNoRadioActionPerformed(evt);
            }
        });

        proxyAddressLabel.setText("Proxy Address:");

        proxyPortLabel.setText("Proxy Port:");

        authenticationPreemptiveCheckBox.setBackground(com.mirth.connect.client.ui.UIConstants.BACKGROUND_COLOR);
        authenticationPreemptiveCheckBox.setText("Preemptive");

        dataTypeLabel.setText("Data Type:");

        authenticationYesRadio.setBackground(new Color(255, 255, 255));
        authenticationButtonGroup.add(authenticationYesRadio);
        authenticationYesRadio.setText("Yes");
        authenticationYesRadio.setMargin(new Insets(0, 0, 0, 0));
        authenticationYesRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                authenticationYesRadioActionPerformed(evt);
            }
        });

        dataTypeTextRadio.setBackground(new Color(255, 255, 255));
        dataTypeButtonGroup.add(dataTypeTextRadio);
        dataTypeTextRadio.setText("Text");
        dataTypeTextRadio.setMargin(new Insets(0, 0, 0, 0));
        dataTypeTextRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                dataTypeTextRadioActionPerformed(evt);
            }
        });

        responseBinaryMimeTypesLabel.setText("Binary MIME Types:");

        responseBinaryMimeTypesField.setMinimumSize(new Dimension(200, 21));
        responseBinaryMimeTypesField.setPreferredSize(new Dimension(200, 21));

        responseBinaryMimeTypesRegexCheckBox.setBackground(new Color(255, 255, 255));
        responseBinaryMimeTypesRegexCheckBox.setText("Regular Expression");

        patchButton.setBackground(new Color(255, 255, 255));
        methodButtonGroup.add(patchButton);
        patchButton.setText("PATCH");
        patchButton.setMargin(new Insets(0, 0, 0, 0));
        patchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                patchButtonActionPerformed(evt);
            }
        });
    }
    
    private void initToolTips() {
        urlField.setToolTipText("Enter the URL of the HTTP server to send each message to.");
        queryParametersTable.setToolTipText("Query parameters are encoded as x=y pairs as part of the request URL, separated from it by a '?' and from each other by an '&'.");
        postButton.setToolTipText("Selects the HTTP operation used to send each message.");
        getButton.setToolTipText("Selects the HTTP operation used to send each message.");
        headersTable.setToolTipText("Header parameters are encoded as HTTP headers in the HTTP request sent to the server.");
        responseContentXmlBodyRadio.setToolTipText("<html>If selected, the response content will include the response body as serialized XML.</html>");
        responseContentPlainBodyRadio.setToolTipText("<html>If selected, the response content will only include the response body as a raw string.</html>");
        putButton.setToolTipText("Selects the HTTP operation used to send each message.");
        deleteButton.setToolTipText("Selects the HTTP operation used to send each message.");
        multipartYesButton.setToolTipText("Set to use multipart in the Content-Type header. Multipart can only be used with POST.");
        multipartNoButton.setToolTipText("Set not to use multipart in the Content-Type header.");
        contentTextArea.setToolTipText("The HTTP message body.");
        contentTypeField.setToolTipText("<html>The HTTP message body MIME type to use. If<br/>application/x-www-form-urlencoded is used,<br/>the query parameters specified above will be<br/>automatically encoded into the request body.</html>");
        dataTypeBinaryRadio.setToolTipText("<html>Select Binary if the outbound message is a Base64 string (will be decoded before it is sent out).<br/>Select Text if the outbound message is text (will be encoded with the specified character set encoding).</html>");
        authenticationNoRadio.setToolTipText("<html>Turning on authentication uses a username and password to communicate with the HTTP server.</html>");
        usernameField.setToolTipText("The username used to connect to the HTTP server.");
        passwordField.setToolTipText("The password used to connect to the HTTP server.");
        authenticationTypeDigestRadio.setToolTipText("Use the digest authentication scheme.");
        authenticationTypeBasicRadio.setToolTipText("Use the basic authentication scheme.");
        charsetEncodingCombobox.setToolTipText("<html>Select the character set encoding used by the sender of the message,<br> or Default to assume the default character set encoding for the JVM running Mirth Connect.</html>");
        sendTimeoutField.setToolTipText("<html>Sets the socket timeout (SO_TIMEOUT) in milliseconds to be used when executing the method.<br>A timeout value of zero is interpreted as an infinite timeout.</html>");
        parseMultipartYesRadio.setToolTipText("<html>Select Yes to automatically parse multipart responses into separate XML nodes.<br/>Select No to always keep the response body as a single XML node.</html>");
        parseMultipartNoRadio.setToolTipText("<html>Select Yes to automatically parse multipart responses into separate XML nodes.<br/>Select No to always keep the response body as a single XML node.</html>");
        includeMetadataYesRadio.setToolTipText("<html>Select Yes to include response metadata (status<br/>line and headers) in the XML content. Note that<br/>regardless of this setting, the same metadata<br/>will be available in the connector map.</html>");
        includeMetadataNoRadio.setToolTipText("<html>Select Yes to include response metadata (status<br/>line and headers) in the XML content. Note that<br/>regardless of this setting, the same metadata<br/>will be available in the connector map.</html>");
        useProxyServerYesRadio.setToolTipText("<html>If enabled, requests will be forwarded to the proxy<br/>server specified in the address/port fields below.</html>");
        useProxyServerNoRadio.setToolTipText("<html>If enabled, requests will be forwarded to the proxy<br/>server specified in the address/port fields below.</html>");
        proxyAddressField.setToolTipText("The domain name or IP address of the proxy server to connect to.");
        proxyPortField.setToolTipText("The port on which to connect to the proxy server.");
        authenticationPreemptiveCheckBox.setToolTipText("<html>If checked, the authorization header will be sent to the server with the initial<br/>request. Otherwise, the header will only be sent when the server requests it.<br/>When using digest authentication, an Authorization header containing the<br/>realm/nonce/algorithm/qop values must be included in the Headers table.</html>");
        authenticationYesRadio.setToolTipText("<html>Turning on authentication uses a username and password to communicate with the HTTP server.</html>");
        dataTypeTextRadio.setToolTipText("<html>Select Binary if the outbound message is a Base64 string (will be decoded before it is sent out).<br/>Select Text if the outbound message is text (will be encoded with the specified character set encoding).</html>");
        responseBinaryMimeTypesField.setToolTipText("<html>When a response comes in with a Content-Type header that<br/>matches one of these entries, the content will be encoded<br/>into a Base64 string. If Regular Expression is unchecked,<br/>specify multiple entries with commas. Otherwise, enter a<br/>valid regular expression to match MIME types against.</html>");
        responseBinaryMimeTypesRegexCheckBox.setToolTipText("<html>When a response comes in with a Content-Type header that<br/>matches one of these entries, the content will be encoded<br/>into a Base64 string. If Regular Expression is unchecked,<br/>specify multiple entries with commas. Otherwise, enter a<br/>valid regular expression to match MIME types against.</html>");
        patchButton.setToolTipText("Selects the HTTP operation used to send each message.");
        useQueryParamsTableRadio.setToolTipText("<html>The table below will be used to populate query parameters.</html>");
        useQueryParamsVariableRadio.setToolTipText("<html>The Java map specified by the following variable will be used to populate query parameters.<br/>The map must have String keys and either String or List&lt;String&gt; values.</html>");
        queryParamsVariableField.setToolTipText("<html>The variable of a Java map to use to populate query parameters.<br/>The map must have String keys and either String or List&lt;String&gt; values.</html>");  
        useHeadersTableRadio.setToolTipText("<html>The table below will be used to populate headers.</html>");
        useHeadersVariableRadio.setToolTipText("<html>The Java map specified by the following variable will be used to populate headers.<br/>The map must have String keys and either String or List&lt;String&gt; values.</html>");
        headersVariableField.setToolTipText("<html>The variable of a Java map to use to populate headers.<br/>The map must have String keys and either String or List&lt;String&gt; values.</html>");
    }
    
    private void initLayout() {
        setLayout(new MigLayout("insets 0 8 0 8, novisualpadding, hidemode 3, gap 12 6", "[][]6[]", "[][][][][][][][][][][][][][][][][grow][][grow][][][][grow]"));
        
        add(urlLabel, "right");
        add(urlField, "w 312!, sx, split 2");
        add(testConnection, "gapbefore 6");
        add(useProxyServerLabel, "newline, right");
        add(useProxyServerYesRadio, "split 2");
        add(useProxyServerNoRadio);
        add(proxyAddressLabel, "newline, right");
        add(proxyAddressField, "w 202!, sx");
        add(proxyPortLabel, "newline, right");
        add(proxyPortField, "w 56!, sx");
        add(methodLabel, "newline, right");
        add(postButton, "split 5");
        add(getButton);
        add(putButton);
        add(deleteButton);
        add(patchButton);
        add(multipartLabel, "newline, right");
        add(multipartYesButton, "split 2");
        add(multipartNoButton);
        add(sendTimeoutLabel, "newline, right");
        add(sendTimeoutField, "w 75!, sx");
        add(responseContentLabel, "newline, right");
        add(responseContentPlainBodyRadio, "split 2");
        add(responseContentXmlBodyRadio);
        add(parseMultipartLabel, "newline, right");
        add(parseMultipartYesRadio, "split 2");
        add(parseMultipartNoRadio);
        add(includeMetadataLabel, "newline, right");
        add(includeMetadataYesRadio, "split 2");
        add(includeMetadataNoRadio);
        add(responseBinaryMimeTypesLabel, "newline, right");
        add(responseBinaryMimeTypesField, "w 312!, sx, split 3");
        add(responseBinaryMimeTypesRegexCheckBox);
        add(authenticationLabel, "newline, right");
        add(authenticationYesRadio, "split 2");
        add(authenticationNoRadio);
        add(authenticationTypeLabel, "newline, right");
        add(authenticationTypeBasicRadio, "split 3");
        add(authenticationTypeDigestRadio);
        add(authenticationPreemptiveCheckBox);
        add(usernameLabel, "newline, right");
        add(usernameField, "w 125!, sx");
        add(passwordLabel, "newline, right");
        add(passwordField, "w 125!, sx");
        add(queryParametersLabel, "newline, right");
        add(useQueryParamsTableRadio, "split 3");
        add(useQueryParamsVariableRadio);
        add(queryParamsVariableField, "w 125!, sx");
        add(queryParametersPane, "newline, growx, pushx, growy, skip 1, span 2, h 84:84:150");
        add(queryParametersNewButton, "top, flowy, split 2, w 44!");
        add(queryParametersDeleteButton, "w 44!");
        add(headersLabel, "newline, right");
        add(useHeadersTableRadio, "split 3");
        add(useHeadersVariableRadio);
        add(headersVariableField, "w 125!, sx");
        add(headersPane, "newline, growx, pushx, growy, skip 1, span 2, h 84:84:150");
        add(headersNewButton, "top, flowy, split 2, w 44!");
        add(headersDeleteButton, "w 44!");
        add(contentTypeLabel, "newline, right");
        add(contentTypeField, "w 125!, sx");
        add(dataTypeLabel, "newline, right");
        add(dataTypeBinaryRadio, "split 2");
        add(dataTypeTextRadio);
        add(charsetEncodingLabel, "newline, right");
        add(charsetEncodingCombobox);
        add(contentLabel, "newline, top, right");
        add(contentTextArea, "grow, push, sx, h 132:");
    }

    private void headersDeleteButtonActionPerformed(ActionEvent evt) {
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
    }

    private void headersNewButtonActionPerformed(ActionEvent evt) {
        ((DefaultTableModel) headersTable.getModel()).addRow(new Object[] {
                getNewPropertyName(headersTable), "" });
        headersTable.setRowSelectionInterval(headersTable.getRowCount() - 1, headersTable.getRowCount() - 1);
        parent.setSaveEnabled(true);
    }

    private void postButtonActionPerformed(ActionEvent evt) {
        checkMultipartEnabled();
        checkContentEnabled();
        setQueryParametersEnabled(true);
    }

    private void getButtonActionPerformed(ActionEvent evt) {
        checkMultipartEnabled();
        checkContentEnabled();
        setQueryParametersEnabled(true);
    }

    private void putButtonActionPerformed(ActionEvent evt) {
        checkMultipartEnabled();
        checkContentEnabled();
        setQueryParametersEnabled(true);
    }

    private void deleteButtonActionPerformed(ActionEvent evt) {
        checkMultipartEnabled();
        checkContentEnabled();
        setQueryParametersEnabled(true);
    }

    private void patchButtonActionPerformed(ActionEvent evt) {
        checkMultipartEnabled();
        checkContentEnabled();
        setQueryParametersEnabled(true);
    }

    private void testConnectionActionPerformed(ActionEvent evt) {
        ResponseHandler handler = new ResponseHandler() {
            @Override
            public void handle(Object response) {
                ConnectionTestResponse connectionTestResponse = (ConnectionTestResponse) response;

                if (connectionTestResponse == null) {
                    parent.alertError(parent, "Failed to invoke service.");
                } else if (connectionTestResponse.getType().equals(ConnectionTestResponse.Type.SUCCESS)) {
                    parent.alertInformation(parent, connectionTestResponse.getMessage());
                } else {
                    parent.alertWarning(parent, connectionTestResponse.getMessage());
                }
            }
        };

        try {
            getServlet(HttpConnectorServletInterface.class, "Testing connection...", "Error testing HTTP connection: ", handler).testConnection(getChannelId(), getChannelName(), (HttpDispatcherProperties) getFilledProperties());
        } catch (ClientException e) {
            // Should not happen
        }
    }

    private void queryParametersDeleteButtonActionPerformed(ActionEvent evt) {
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
    }

    private void queryParametersNewButtonActionPerformed(ActionEvent evt) {
        ((DefaultTableModel) queryParametersTable.getModel()).addRow(new Object[] {
                getNewPropertyName(queryParametersTable), "" });
        queryParametersTable.setRowSelectionInterval(queryParametersTable.getRowCount() - 1, queryParametersTable.getRowCount() - 1);
        parent.setSaveEnabled(true);
    }

    private void authenticationYesRadioActionPerformed(ActionEvent evt) {
        setAuthenticationEnabled(true);
    }

    private void authenticationNoRadioActionPerformed(ActionEvent evt) {
        setAuthenticationEnabled(false);
    }

    private void responseContentPlainBodyRadioActionPerformed(ActionEvent evt) {
        parseMultipartLabel.setEnabled(false);
        parseMultipartYesRadio.setEnabled(false);
        parseMultipartNoRadio.setEnabled(false);
        includeMetadataLabel.setEnabled(false);
        includeMetadataYesRadio.setEnabled(false);
        includeMetadataNoRadio.setEnabled(false);
    }

    private void responseContentXmlBodyRadioActionPerformed(ActionEvent evt) {
        parseMultipartLabel.setEnabled(true);
        parseMultipartYesRadio.setEnabled(true);
        parseMultipartNoRadio.setEnabled(true);
        includeMetadataLabel.setEnabled(true);
        includeMetadataYesRadio.setEnabled(true);
        includeMetadataNoRadio.setEnabled(true);
    }

    private void useProxyServerYesRadioActionPerformed(ActionEvent evt) {
        proxyAddressLabel.setEnabled(true);
        proxyAddressField.setEnabled(true);
        proxyPortLabel.setEnabled(true);
        proxyPortField.setEnabled(true);
    }

    private void useProxyServerNoRadioActionPerformed(ActionEvent evt) {
        proxyAddressLabel.setEnabled(false);
        proxyAddressField.setEnabled(false);
        proxyPortLabel.setEnabled(false);
        proxyPortField.setEnabled(false);
    }

    private void dataTypeBinaryRadioActionPerformed(ActionEvent evt) {
        charsetEncodingLabel.setEnabled(false);
        charsetEncodingCombobox.setEnabled(false);
        charsetEncodingCombobox.setSelectedIndex(0);
    }

    private void dataTypeTextRadioActionPerformed(ActionEvent evt) {
        if (postButton.isSelected() || putButton.isSelected() || patchButton.isSelected()) {
            charsetEncodingLabel.setEnabled(true);
            charsetEncodingCombobox.setEnabled(true);
        }
    }
    
    private void useHeadersVariableFieldsEnabled(boolean useVariable) {
        headersVariableField.setEnabled(useVariable);
        headersTable.setEnabled(!useVariable);
        headersNewButton.setEnabled(!useVariable);
        headersDeleteButton.setEnabled(!useVariable && headersTable.getSelectedRow() > -1);
    }
    
    private void useQueryParamsVariableFieldsEnabled(boolean useVariable) {
        queryParamsVariableField.setEnabled(useVariable);
        queryParametersTable.setEnabled(!useVariable);
        queryParametersNewButton.setEnabled(!useVariable);
        queryParametersDeleteButton.setEnabled(!useVariable && queryParametersTable.getSelectedRow() > -1);
    }
    
    private ButtonGroup authenticationButtonGroup;
    private JLabel authenticationLabel;
    private MirthRadioButton authenticationNoRadio;
    private MirthCheckBox authenticationPreemptiveCheckBox;
    private MirthRadioButton authenticationTypeBasicRadio;
    private ButtonGroup authenticationTypeButtonGroup;
    private MirthRadioButton authenticationTypeDigestRadio;
    private JLabel authenticationTypeLabel;
    private MirthRadioButton authenticationYesRadio;
    private MirthComboBox<String> charsetEncodingCombobox;
    private JLabel charsetEncodingLabel;
    private JLabel contentLabel;
    private MirthSyntaxTextArea contentTextArea;
    private MirthTextField contentTypeField;
    private JLabel contentTypeLabel;
    private MirthRadioButton dataTypeBinaryRadio;
    private ButtonGroup dataTypeButtonGroup;
    private JLabel dataTypeLabel;
    private MirthRadioButton dataTypeTextRadio;
    private MirthRadioButton deleteButton;
    private MirthRadioButton getButton;
    private JButton headersDeleteButton;
    private JLabel headersLabel;
    private JButton headersNewButton;
    private JScrollPane headersPane;
    private MirthTable headersTable;
    protected MirthTextField headersVariableField;
    protected MirthRadioButton useHeadersTableRadio;
    protected MirthRadioButton useHeadersVariableRadio;
    private ButtonGroup includeMetadataButtonGroup;
    private JLabel includeMetadataLabel;
    private MirthRadioButton includeMetadataNoRadio;
    private MirthRadioButton includeMetadataYesRadio;
    private ButtonGroup methodButtonGroup;
    private JLabel methodLabel;
    private ButtonGroup multipartButtonGroup;
    private JLabel multipartLabel;
    private MirthRadioButton multipartNoButton;
    private MirthRadioButton multipartYesButton;
    private ButtonGroup parseMultipartButtonGroup;
    private JLabel parseMultipartLabel;
    private MirthRadioButton parseMultipartNoRadio;
    private MirthRadioButton parseMultipartYesRadio;
    private MirthPasswordField passwordField;
    private JLabel passwordLabel;
    private MirthRadioButton patchButton;
    private MirthRadioButton postButton;
    private MirthTextField proxyAddressField;
    private JLabel proxyAddressLabel;
    private MirthTextField proxyPortField;
    private JLabel proxyPortLabel;
    private ButtonGroup proxyTypeButtonGroup;
    private MirthRadioButton putButton;
    private JButton queryParametersDeleteButton;
    private JLabel queryParametersLabel;
    private JButton queryParametersNewButton;
    private JScrollPane queryParametersPane;
    private MirthTable queryParametersTable;
    protected MirthTextField queryParamsVariableField;
    protected MirthRadioButton useQueryParamsTableRadio;
    protected MirthRadioButton useQueryParamsVariableRadio;
    private MirthTextField responseBinaryMimeTypesField;
    private JLabel responseBinaryMimeTypesLabel;
    private MirthCheckBox responseBinaryMimeTypesRegexCheckBox;
    private ButtonGroup responseContentButtonGroup;
    private JLabel responseContentLabel;
    private MirthRadioButton responseContentPlainBodyRadio;
    private MirthRadioButton responseContentXmlBodyRadio;
    private MirthTextField sendTimeoutField;
    private JLabel sendTimeoutLabel;
    private JButton testConnection;
    private MirthIconTextField urlField;
    private JLabel urlLabel;
    private ButtonGroup usePersistantQueuesButtonGroup;
    private JLabel useProxyServerLabel;
    private MirthRadioButton useProxyServerNoRadio;
    private MirthRadioButton useProxyServerYesRadio;
    private MirthTextField usernameField;
    private JLabel usernameLabel;
}
