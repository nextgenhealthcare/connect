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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.ConnectorTypeDecoration;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.MirthDialog;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.TextFieldCellEditor;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthCheckBox;
import com.mirth.connect.client.ui.components.MirthComboBox;
import com.mirth.connect.client.ui.components.MirthComboBoxTableCellEditor;
import com.mirth.connect.client.ui.components.MirthComboBoxTableCellRenderer;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthRadioButton;
import com.mirth.connect.client.ui.components.MirthSyntaxTextArea;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.client.ui.panels.connectors.ListenerSettingsPanel;
import com.mirth.connect.connectors.http.HttpStaticResource.ResourceType;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;

public class HttpListener extends ConnectorSettingsPanel {

    private final int NAME_COLUMN = 0;
    private final int VALUE_COLUMN = 1;
    private final String NAME_COLUMN_NAME = "Name";
    private final String VALUE_COLUMN_NAME = "Value";
    private int responseHeadersLastIndex = -1;
    private int staticResourcesLastIndex = -1;
    private boolean usingHttps = false;

    private enum StaticResourcesColumn {
        CONTEXT_PATH(0, "Context Path"), RESOURCE_TYPE(1, "Resource Type"), VALUE(2,
                "Value"), CONTENT_TYPE(3, "Content Type");

        private int index;
        private String name;

        private StaticResourcesColumn(int index, String name) {
            this.index = index;
            this.name = name;
        }

        public int getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }

        public static String[] getNames() {
            StaticResourcesColumn[] values = values();
            String[] names = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                names[i] = values[i].getName();
            }
            return names;
        }
    }

    private Frame parent;

    public HttpListener() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        initToolTips();
        initLayout();
        initComponentsManual();
        httpUrlField.setEditable(false);
        parent.setupCharsetEncodingForConnector(charsetEncodingCombobox);
    }

    @Override
    public String getConnectorName() {
        return new HttpReceiverProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        HttpReceiverProperties properties = (HttpReceiverProperties) getDefaults();
        properties.setContextPath(contextPathField.getText());
        properties.setTimeout(receiveTimeoutField.getText());
        properties.setXmlBody(messageContentXmlBodyRadio.isSelected());
        properties.setParseMultipart(parseMultipartYesRadio.isSelected());
        properties.setIncludeMetadata(includeMetadataYesRadio.isSelected());
        properties.setBinaryMimeTypes(binaryMimeTypesField.getText());
        properties.setBinaryMimeTypesRegex(binaryMimeTypesRegexCheckBox.isSelected());
        properties.setResponseContentType(responseContentTypeField.getText());
        properties.setResponseDataTypeBinary(responseDataTypeBinaryRadio.isSelected());
        properties.setCharset(parent.getSelectedEncodingForConnector(charsetEncodingCombobox));

        properties.setResponseStatusCode(responseStatusCodeField.getText());

        properties.setResponseHeadersMap(getResponseHeaders());
        properties.setUseHeadersVariable(useResponseHeadersVariableRadio.isSelected());
        properties.setResponseHeadersVariable(responseHeadersVariableField.getText());
        
        properties.setStaticResources(getStaticResources());

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        HttpReceiverProperties props = (HttpReceiverProperties) properties;

        contextPathField.setText(props.getContextPath());
        receiveTimeoutField.setText(props.getTimeout());

        updateHttpUrl();

        if (props.isXmlBody()) {
            messageContentXmlBodyRadio.setSelected(true);
            messageContentXmlBodyRadioActionPerformed(null);
        } else {
            messageContentPlainBodyRadio.setSelected(true);
            messageContentPlainBodyRadioActionPerformed(null);
        }

        if (props.isParseMultipart()) {
            parseMultipartYesRadio.setSelected(true);
        } else {
            parseMultipartNoRadio.setSelected(true);
        }

        if (props.isIncludeMetadata()) {
            includeMetadataYesRadio.setSelected(true);
        } else {
            includeMetadataNoRadio.setSelected(true);
        }

        binaryMimeTypesField.setText(props.getBinaryMimeTypes());
        binaryMimeTypesRegexCheckBox.setSelected(props.isBinaryMimeTypesRegex());

        responseContentTypeField.setText(props.getResponseContentType());

        if (props.isResponseDataTypeBinary()) {
            responseDataTypeBinaryRadio.setSelected(true);
            responseDataTypeBinaryRadioActionPerformed(null);
        } else {
            responseDataTypeTextRadio.setSelected(true);
            responseDataTypeTextRadioActionPerformed(null);
        }

        parent.setPreviousSelectedEncodingForConnector(charsetEncodingCombobox, props.getCharset());

        responseStatusCodeField.setText(props.getResponseStatusCode());

        if (props.getResponseHeadersMap() != null) {
            setResponseHeaders(props.getResponseHeadersMap());
        } else {
            setResponseHeaders(new LinkedHashMap<String, List<String>>());
        }

        if (props.isUseHeadersVariable()) {
            useResponseHeadersVariableRadio.setSelected(true);
        } else {
            useResponseHeadersTableRadio.setSelected(true);
        }
        responseHeadersVariableField.setText(props.getResponseHeadersVariable());
        useResponseHeadersVariableFieldsEnabled(props.isUseHeadersVariable());
        
        if (props.getStaticResources() != null) {
            setStaticResources(props.getStaticResources());
        } else {
            setStaticResources(new ArrayList<HttpStaticResource>(0));
        }
    }

    @Override
    public ConnectorProperties getDefaults() {
        return new HttpReceiverProperties();
    }

    @Override
    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        HttpReceiverProperties props = (HttpReceiverProperties) properties;

        boolean valid = true;

        if (props.getTimeout().length() == 0) {
            valid = false;
            if (highlight) {
                receiveTimeoutField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (!props.getSourceConnectorProperties().getResponseVariable().equalsIgnoreCase("None")) {
            if (props.getResponseContentType().length() == 0) {
                valid = false;
                if (highlight) {
                    responseContentTypeField.setBackground(UIConstants.INVALID_COLOR);
                }
            }

        }

        return valid;
    }

    @Override
    public void resetInvalidProperties() {
        receiveTimeoutField.setBackground(null);
        responseContentTypeField.setBackground(null);
    }

    @Override
    public void doLocalDecoration(ConnectorTypeDecoration connectorTypeDecoration) {
        usingHttps = connectorTypeDecoration != null;
        updateHttpUrl();
    }

    @Override
    public String getRequiredInboundDataType() {
        if (((HttpReceiverProperties) getProperties()).isXmlBody()) {
            return UIConstants.DATATYPE_XML;
        } else {
            return null;
        }
    }

    public void updateHttpUrl() {
        String server = "<server ip>";
        try {
            server = new URI(PlatformUI.SERVER_URL).getHost();
        } catch (Exception e) {
            // ignore exceptions getting the server ip
        }

        httpUrlLabel.setText(usingHttps ? "HTTPS URL:" : "HTTP URL:");

        // Display: http://server:port/contextpath/
        httpUrlField.setText("http" + (usingHttps ? "s" : "") + "://" + server + ":" + ((HttpReceiverProperties) getFilledProperties()).getListenerConnectorProperties().getPort() + (contextPathField.getText().startsWith("/") ? "" : "/") + contextPathField.getText() + ((StringUtils.isBlank(contextPathField.getText()) || contextPathField.getText().endsWith("/")) ? "" : "/"));
    }

    public void setResponseHeaders(Map<String, List<String>> responseHeaders) {
        int size = 0;
        for (List<String> property : responseHeaders.values()) {
            size += property.size();
        }

        Object[][] tableData = new Object[size][2];

        responseHeadersTable = new MirthTable();

        int j = 0;
        Iterator<Entry<String, List<String>>> i = responseHeaders.entrySet().iterator();
        while (i.hasNext()) {
            Entry<String, List<String>> entry = i.next();
            for (String keyValue : (List<String>) entry.getValue()) {
                tableData[j][NAME_COLUMN] = entry.getKey();
                tableData[j][VALUE_COLUMN] = keyValue;
                j++;
            }
        }

        responseHeadersTable.setModel(new DefaultTableModel(tableData, new String[] {
                NAME_COLUMN_NAME, VALUE_COLUMN_NAME }) {

            boolean[] canEdit = new boolean[] { true, true };

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        responseHeadersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                if (getSelectedRow(responseHeadersTable) != -1) {
                    responseHeadersLastIndex = getSelectedRow(responseHeadersTable);
                    responseHeadersDeleteButton.setEnabled(true);
                } else {
                    responseHeadersDeleteButton.setEnabled(false);
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
                    responseHeadersDeleteButton.setEnabled(false);
                }

                return editable;
            }

            @Override
            protected boolean valueChanged(String value) {
                responseHeadersDeleteButton.setEnabled(true);

                if (checkProperties && (value.length() == 0)) {
                    return false;
                }

                parent.setSaveEnabled(true);
                return true;
            }
        }

        responseHeadersTable.getColumnModel().getColumn(responseHeadersTable.getColumnModel().getColumnIndex(NAME_COLUMN_NAME)).setCellEditor(new HTTPTableCellEditor(true));
        responseHeadersTable.getColumnModel().getColumn(responseHeadersTable.getColumnModel().getColumnIndex(VALUE_COLUMN_NAME)).setCellEditor(new HTTPTableCellEditor(false));
        responseHeadersTable.setCustomEditorControls(true);

        responseHeadersTable.setSelectionMode(0);
        responseHeadersTable.setRowSelectionAllowed(true);
        responseHeadersTable.setRowHeight(UIConstants.ROW_HEIGHT);
        responseHeadersTable.setDragEnabled(false);
        responseHeadersTable.setOpaque(true);
        responseHeadersTable.setSortable(false);
        responseHeadersTable.getTableHeader().setReorderingAllowed(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            responseHeadersTable.setHighlighters(highlighter);
        }

        responseHeadersPane.setViewportView(responseHeadersTable);
        responseHeadersDeleteButton.setEnabled(false);
    }

    public Map<String, List<String>> getResponseHeaders() {
        Map<String, List<String>> properties = new LinkedHashMap<String, List<String>>();

        for (int i = 0; i < responseHeadersTable.getRowCount(); i++) {
            String key = (String) responseHeadersTable.getValueAt(i, NAME_COLUMN);

            List<String> headers = properties.get(key);

            if (headers == null) {
                headers = new ArrayList<String>();
                properties.put(key, headers);
            }

            headers.add((String) responseHeadersTable.getValueAt(i, VALUE_COLUMN));
        }

        return properties;
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
    public void updatedField(String field) {
        if (ListenerSettingsPanel.FIELD_PORT.equals(field)) {
            updateHttpUrl();
        }
    }

    private List<HttpStaticResource> getStaticResources() {
        List<HttpStaticResource> staticResources = new ArrayList<HttpStaticResource>();

        for (int i = 0; i < staticResourcesTable.getRowCount(); i++) {
            String contextPath = (String) staticResourcesTable.getValueAt(i, StaticResourcesColumn.CONTEXT_PATH.getIndex());
            ResourceType resourceType = ResourceType.fromString((String) staticResourcesTable.getValueAt(i, StaticResourcesColumn.RESOURCE_TYPE.getIndex()));
            String value = (String) staticResourcesTable.getValueAt(i, StaticResourcesColumn.VALUE.getIndex());
            String contentType = (String) staticResourcesTable.getValueAt(i, StaticResourcesColumn.CONTENT_TYPE.getIndex());
            staticResources.add(new HttpStaticResource(contextPath, resourceType, value, contentType));
        }

        return staticResources;
    }

    private void setStaticResources(List<HttpStaticResource> staticResources) {
        Object[][] tableData = new Object[staticResources.size()][4];

        for (int i = 0; i < staticResources.size(); i++) {
            HttpStaticResource staticResource = staticResources.get(i);
            tableData[i][0] = staticResource.getContextPath();
            tableData[i][1] = staticResource.getResourceType().toString();
            tableData[i][2] = staticResource.getValue();
            tableData[i][3] = staticResource.getContentType();
        }

        ((RefreshTableModel) staticResourcesTable.getModel()).refreshDataVector(tableData);
    }

    private String getBaseContextPath() {
        String contextPath = StringUtils.trimToEmpty(contextPathField.getText());
        if (!contextPath.startsWith("/")) {
            contextPath = "/" + contextPath;
        }
        while (contextPath.endsWith("/")) {
            contextPath = contextPath.substring(0, contextPath.length() - 1).trim();
        }
        return contextPath;
    }

    private String fixContentPath(String contextPath) {
        contextPath = StringUtils.trimToEmpty(contextPath);
        if (!contextPath.startsWith("/")) {
            contextPath = "/" + contextPath;
        }
        while (contextPath.endsWith("/") && contextPath.length() > 1) {
            contextPath = contextPath.substring(0, contextPath.length() - 1).trim();
        }
        return contextPath;
    }

    private void initComponentsManual() {
        staticResourcesTable.setModel(new RefreshTableModel(StaticResourcesColumn.getNames(), 0) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return true;
            }
        });

        staticResourcesTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        staticResourcesTable.setRowHeight(UIConstants.ROW_HEIGHT);
        staticResourcesTable.setFocusable(true);
        staticResourcesTable.setSortable(false);
        staticResourcesTable.setOpaque(true);
        staticResourcesTable.setDragEnabled(false);
        staticResourcesTable.getTableHeader().setReorderingAllowed(false);
        staticResourcesTable.setShowGrid(true, true);
        staticResourcesTable.setAutoCreateColumnsFromModel(false);
        staticResourcesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        staticResourcesTable.setRowSelectionAllowed(true);
        staticResourcesTable.setCustomEditorControls(true);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            staticResourcesTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        class ContextPathCellEditor extends TextFieldCellEditor {
            public ContextPathCellEditor() {
                getTextField().setDocument(new MirthFieldConstraints("^\\S*$"));
            }

            @Override
            protected boolean valueChanged(String value) {
                if (StringUtils.isEmpty(value) || value.equals("/")) {
                    return false;
                }

                if (value.equals(getOriginalValue())) {
                    return false;
                }

                for (int i = 0; i < staticResourcesTable.getRowCount(); i++) {
                    if (value.equals(fixContentPath((String) staticResourcesTable.getValueAt(i, StaticResourcesColumn.CONTEXT_PATH.getIndex())))) {
                        return false;
                    }
                }

                parent.setSaveEnabled(true);
                return true;
            }

            @Override
            public Object getCellEditorValue() {
                String value = fixContentPath((String) super.getCellEditorValue());
                String baseContextPath = getBaseContextPath();
                if (value.equals(baseContextPath)) {
                    return null;
                } else {
                    return fixContentPath(StringUtils.removeStartIgnoreCase(value, baseContextPath + "/"));
                }
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                String resourceContextPath = fixContentPath((String) value);
                setOriginalValue(resourceContextPath);
                getTextField().setText(getBaseContextPath() + resourceContextPath);
                return getTextField();
            }
        }
        ;

        staticResourcesTable.getColumnExt(StaticResourcesColumn.CONTEXT_PATH.getIndex()).setCellEditor(new ContextPathCellEditor());
        staticResourcesTable.getColumnExt(StaticResourcesColumn.CONTEXT_PATH.getIndex()).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                super.setValue(getBaseContextPath() + fixContentPath((String) value));
            }
        });

        class ResourceTypeCellEditor extends MirthComboBoxTableCellEditor implements ActionListener {
            private Object originalValue;

            public ResourceTypeCellEditor(JTable table, Object[] items, int clickCount, boolean focusable) {
                super(table, items, clickCount, focusable, null);
                for (ActionListener actionListener : comboBox.getActionListeners()) {
                    comboBox.removeActionListener(actionListener);
                }
                comboBox.addActionListener(this);
            }

            @Override
            public boolean stopCellEditing() {
                if (ObjectUtils.equals(getCellEditorValue(), originalValue)) {
                    cancelCellEditing();
                } else {
                    parent.setSaveEnabled(true);
                }
                return super.stopCellEditing();
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                originalValue = value;
                return super.getTableCellEditorComponent(table, value, isSelected, row, column);
            }

            @Override
            public void actionPerformed(ActionEvent evt) {
                ((AbstractTableModel) staticResourcesTable.getModel()).fireTableCellUpdated(staticResourcesTable.getSelectedRow(), StaticResourcesColumn.VALUE.getIndex());
                stopCellEditing();
                fireEditingStopped();
            }
        }

        String[] resourceTypes = ResourceType.stringValues();
        staticResourcesTable.getColumnExt(StaticResourcesColumn.RESOURCE_TYPE.getIndex()).setMinWidth(100);
        staticResourcesTable.getColumnExt(StaticResourcesColumn.RESOURCE_TYPE.getIndex()).setMaxWidth(100);
        staticResourcesTable.getColumnExt(StaticResourcesColumn.RESOURCE_TYPE.getIndex()).setCellEditor(new ResourceTypeCellEditor(staticResourcesTable, resourceTypes, 1, false));
        staticResourcesTable.getColumnExt(StaticResourcesColumn.RESOURCE_TYPE.getIndex()).setCellRenderer(new MirthComboBoxTableCellRenderer(resourceTypes));

        class ValueCellEditor extends AbstractCellEditor implements TableCellEditor {

            private JPanel panel;
            private JLabel label;
            private JTextField textField;
            private String text;
            private String originalValue;

            public ValueCellEditor() {
                panel = new JPanel(new MigLayout("insets 0 1 0 0, novisualpadding, hidemode 3"));
                panel.setBackground(UIConstants.BACKGROUND_COLOR);

                label = new JLabel();
                label.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseReleased(MouseEvent evt) {
                        new ValueDialog();
                        stopCellEditing();
                    }
                });
                panel.add(label, "grow, pushx, h 19!");

                textField = new JTextField();
                panel.add(textField, "grow, pushx, h 19!");
            }

            @Override
            public boolean isCellEditable(EventObject evt) {
                if (evt == null) {
                    return false;
                }
                if (evt instanceof MouseEvent) {
                    return ((MouseEvent) evt).getClickCount() >= 2;
                }
                return true;
            }

            @Override
            public Object getCellEditorValue() {
                if (label.isVisible()) {
                    return text;
                } else {
                    return textField.getText();
                }
            }

            @Override
            public boolean stopCellEditing() {
                if (ObjectUtils.equals(getCellEditorValue(), originalValue)) {
                    cancelCellEditing();
                } else {
                    parent.setSaveEnabled(true);
                }
                return super.stopCellEditing();
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                boolean custom = table.getValueAt(row, StaticResourcesColumn.RESOURCE_TYPE.getIndex()).equals(ResourceType.CUSTOM.toString());
                label.setVisible(custom);
                textField.setVisible(!custom);

                panel.setBackground(table.getSelectionBackground());
                label.setBackground(panel.getBackground());
                label.setMaximumSize(new Dimension(table.getColumnModel().getColumn(column).getWidth(), 19));

                String text = (String) value;
                this.text = text;
                originalValue = text;
                label.setText(text);
                textField.setText(text);

                return panel;
            }

            class ValueDialog extends MirthDialog {

                public ValueDialog() {
                    super(parent, true);
                    setTitle("Custom Value");
                    setPreferredSize(new Dimension(600, 500));
                    setLayout(new MigLayout("insets 12, novisualpadding, hidemode 3, fill", "", "[grow]7[]"));
                    setBackground(UIConstants.BACKGROUND_COLOR);
                    getContentPane().setBackground(getBackground());

                    final MirthSyntaxTextArea textArea = new MirthSyntaxTextArea();
                    textArea.setSaveEnabled(false);
                    textArea.setText(text);
                    textArea.setBorder(BorderFactory.createEtchedBorder());
                    add(textArea, "grow");

                    add(new JSeparator(), "newline, grow");

                    JPanel buttonPanel = new JPanel(new MigLayout("insets 0, novisualpadding, hidemode 3"));
                    buttonPanel.setBackground(getBackground());

                    JButton openFileButton = new JButton("Open File...");
                    openFileButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent evt) {
                            String content = parent.browseForFileString(null);
                            if (content != null) {
                                textArea.setText(content);
                            }
                        }
                    });
                    buttonPanel.add(openFileButton);

                    JButton okButton = new JButton("OK");
                    okButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent evt) {
                            text = textArea.getText();
                            label.setText(text);
                            textField.setText(text);
                            staticResourcesTable.getModel().setValueAt(text, staticResourcesTable.getSelectedRow(), StaticResourcesColumn.VALUE.getIndex());
                            parent.setSaveEnabled(true);
                            dispose();
                        }
                    });
                    buttonPanel.add(okButton);

                    JButton cancelButton = new JButton("Cancel");
                    cancelButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent evt) {
                            dispose();
                        }
                    });
                    buttonPanel.add(cancelButton);

                    add(buttonPanel, "newline, right");

                    pack();
                    setLocationRelativeTo(parent);
                    setVisible(true);
                }
            }
        }
        ;

        staticResourcesTable.getColumnExt(StaticResourcesColumn.VALUE.getIndex()).setCellEditor(new ValueCellEditor());

        staticResourcesTable.getColumnExt(StaticResourcesColumn.CONTENT_TYPE.getIndex()).setMinWidth(100);
        staticResourcesTable.getColumnExt(StaticResourcesColumn.CONTENT_TYPE.getIndex()).setMaxWidth(150);
        staticResourcesTable.getColumnExt(StaticResourcesColumn.CONTENT_TYPE.getIndex()).setCellEditor(new TextFieldCellEditor() {
            @Override
            protected boolean valueChanged(String value) {
                if (value.equals(getOriginalValue())) {
                    return false;
                }
                parent.setSaveEnabled(true);
                return true;
            }
        });

        staticResourcesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                if (getSelectedRow(staticResourcesTable) != -1) {
                    staticResourcesLastIndex = getSelectedRow(staticResourcesTable);
                    staticResourcesDeleteButton.setEnabled(true);
                } else {
                    staticResourcesDeleteButton.setEnabled(false);
                }
            }
        });

        contextPathField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent evt) {
                changedUpdate(evt);
            }

            @Override
            public void removeUpdate(DocumentEvent evt) {
                changedUpdate(evt);
            }

            @Override
            public void changedUpdate(DocumentEvent evt) {
                ((AbstractTableModel) staticResourcesTable.getModel()).fireTableDataChanged();
            }
        });

        staticResourcesDeleteButton.setEnabled(false);
    }

    private boolean checkStaticResourceContextPath(String contextPath) {
        for (int i = 0; i < staticResourcesTable.getRowCount(); i++) {
            if (contextPath.equals(staticResourcesTable.getValueAt(i, StaticResourcesColumn.CONTEXT_PATH.getIndex()))) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    protected void initComponents() {

        includeHeadersGroup = new ButtonGroup();
        parseMultipartButtonGroup = new ButtonGroup();
        includeMetadataButtonGroup = new ButtonGroup();
        responseDataTypeButtonGroup = new ButtonGroup();
        messageContentPlainBodyRadio = new MirthRadioButton();
        messageContentLabel = new JLabel();
        responseContentTypeField = new MirthTextField();
        responseContentTypeLabel = new JLabel();
        charsetEncodingCombobox = new MirthComboBox<String>();
        charsetEncodingLabel = new JLabel();
        contextPathLabel = new JLabel();
        contextPathField = new MirthTextField();
        receiveTimeoutLabel = new JLabel();
        receiveTimeoutField = new MirthTextField();
        httpUrlField = new JTextField();
        httpUrlLabel = new JLabel();
        headersLabel = new JLabel();
        responseHeadersPane = new JScrollPane();
        responseHeadersTable = new MirthTable();
        responseHeadersNewButton = new JButton();
        responseHeadersDeleteButton = new JButton();
        responseStatusCodeLabel = new JLabel();
        responseStatusCodeField = new MirthTextField();
        messageContentXmlBodyRadio = new MirthRadioButton();
        parseMultipartLabel = new JLabel();
        parseMultipartYesRadio = new MirthRadioButton();
        parseMultipartNoRadio = new MirthRadioButton();
        includeMetadataLabel = new JLabel();
        includeMetadataYesRadio = new MirthRadioButton();
        includeMetadataNoRadio = new MirthRadioButton();
        staticResourcesLabel = new JLabel();
        staticResourcesDeleteButton = new JButton();
        staticResourcesNewButton = new JButton();
        responseHeadersPane1 = new JScrollPane();
        staticResourcesTable = new MirthTable();
        responseDataTypeLabel = new JLabel();
        responseDataTypeBinaryRadio = new MirthRadioButton();
        responseDataTypeTextRadio = new MirthRadioButton();
        binaryMimeTypesLabel = new JLabel();
        binaryMimeTypesField = new MirthTextField();
        binaryMimeTypesRegexCheckBox = new MirthCheckBox();

        setBackground(new Color(255, 255, 255));
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        messageContentPlainBodyRadio.setBackground(new Color(255, 255, 255));
        includeHeadersGroup.add(messageContentPlainBodyRadio);
        messageContentPlainBodyRadio.setText("Plain Body");
        messageContentPlainBodyRadio.setMargin(new Insets(0, 0, 0, 0));
        messageContentPlainBodyRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                messageContentPlainBodyRadioActionPerformed(evt);
            }
        });

        messageContentLabel.setText("Message Content:");

        responseContentTypeLabel.setText("Response Content Type:");

        charsetEncodingCombobox.setModel(new DefaultComboBoxModel<String>(new String[] { "default",
                "utf-8", "iso-8859-1", "utf-16 (le)", "utf-16 (be)", "utf-16 (bom)", "us-ascii" }));

        charsetEncodingLabel.setText("Charset Encoding:");

        contextPathLabel.setText("Base Context Path:");

        contextPathField.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                contextPathFieldKeyReleased(evt);
            }
        });

        receiveTimeoutLabel.setText("Receive Timeout (ms):");

        httpUrlLabel.setText("HTTP URL:");

        headersLabel.setText("Response Headers:");

        responseHeadersTable.setModel(new DefaultTableModel(new Object[][] {

        }, new String[] { "Name", "Value" }));
        responseHeadersPane.setViewportView(responseHeadersTable);

        responseHeadersNewButton.setText("New");
        responseHeadersNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                responseHeadersNewButtonActionPerformed(evt);
            }
        });

        responseHeadersDeleteButton.setText("Delete");
        responseHeadersDeleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                responseHeadersDeleteButtonActionPerformed(evt);
            }
        });

        useResponseHeadersTableRadio = new MirthRadioButton("Use Table");
        useResponseHeadersTableRadio.setBackground(getBackground());
        useResponseHeadersTableRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                useResponseHeadersVariableFieldsEnabled(false);
            }
        });
        useResponseHeadersVariableRadio = new MirthRadioButton("Use Map:");
        useResponseHeadersVariableRadio.setBackground(getBackground());
        useResponseHeadersVariableRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                useResponseHeadersVariableFieldsEnabled(true);
            }
        });
        ButtonGroup headersSourceButtonGroup = new ButtonGroup();
        headersSourceButtonGroup.add(useResponseHeadersTableRadio);
        headersSourceButtonGroup.add(useResponseHeadersVariableRadio);     

        responseHeadersVariableField = new MirthTextField();
        
        responseStatusCodeLabel.setText("Response Status Code:");

        messageContentXmlBodyRadio.setBackground(new Color(255, 255, 255));
        includeHeadersGroup.add(messageContentXmlBodyRadio);
        messageContentXmlBodyRadio.setText("XML Body");
        messageContentXmlBodyRadio.setMargin(new Insets(0, 0, 0, 0));
        messageContentXmlBodyRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                messageContentXmlBodyRadioActionPerformed(evt);
            }
        });

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

        staticResourcesLabel.setText("Static Resources:");

        staticResourcesDeleteButton.setText("Delete");
        staticResourcesDeleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                staticResourcesDeleteButtonActionPerformed(evt);
            }
        });

        staticResourcesNewButton.setText("New");
        staticResourcesNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                staticResourcesNewButtonActionPerformed(evt);
            }
        });

        staticResourcesTable.setModel(new DefaultTableModel(new Object[][] {

        }, new String[] {

        }));
        responseHeadersPane1.setViewportView(staticResourcesTable);

        responseDataTypeLabel.setText("Response Data Type:");

        responseDataTypeBinaryRadio.setBackground(new Color(255, 255, 255));
        responseDataTypeButtonGroup.add(responseDataTypeBinaryRadio);
        responseDataTypeBinaryRadio.setText("Binary");
        responseDataTypeBinaryRadio.setMargin(new Insets(0, 0, 0, 0));
        responseDataTypeBinaryRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                responseDataTypeBinaryRadioActionPerformed(evt);
            }
        });

        responseDataTypeTextRadio.setBackground(new Color(255, 255, 255));
        responseDataTypeButtonGroup.add(responseDataTypeTextRadio);
        responseDataTypeTextRadio.setText("Text");
        responseDataTypeTextRadio.setMargin(new Insets(0, 0, 0, 0));
        responseDataTypeTextRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                responseDataTypeTextRadioActionPerformed(evt);
            }
        });

        binaryMimeTypesLabel.setText("Binary MIME Types:");

        binaryMimeTypesField.setMinimumSize(new java.awt.Dimension(200, 21));
        binaryMimeTypesField.setPreferredSize(new java.awt.Dimension(200, 21));

        binaryMimeTypesRegexCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        binaryMimeTypesRegexCheckBox.setText("Regular Expression");
    }
    
    protected void initToolTips() {
        messageContentPlainBodyRadio.setToolTipText("<html>If selected, the request body will be sent to the channel as a raw string.</html>");
        responseContentTypeField.setToolTipText("The MIME type to be used for the response.");
        charsetEncodingCombobox.setToolTipText("<html>Select the character set encoding to be used for the response to the sending system.<br>Set to Default to assume the default character set encoding for the JVM running Mirth Connect.</html>");
        contextPathField.setToolTipText("The context path for the HTTP Listener URL.");
        receiveTimeoutField.setToolTipText("Enter the maximum idle time in milliseconds for a connection.");
        httpUrlField.setToolTipText("<html>Displays the generated HTTP URL for the HTTP Listener.</html>");
        responseHeadersTable.setToolTipText("Response header parameters are encoded as HTTP headers in the response sent to the client.");
        responseStatusCodeField.setToolTipText("<html>Enter the status code for the HTTP response.  If this field is left blank a <br>default status code of 200 will be returned for a successful message, <br>and 500 will be returned for an errored message. If a \"Respond from\" <br>value is chosen, that response will be used to determine a successful <br>or errored response.<html>");
        messageContentXmlBodyRadio.setToolTipText("<html>If selected, the request body will be sent to the channel as serialized XML.</html>");
        parseMultipartYesRadio.setToolTipText("<html>Select Yes to automatically parse multipart requests into separate XML nodes.<br/>Select No to always keep the request body as a single XML node.</html>");
        parseMultipartNoRadio.setToolTipText("<html>Select Yes to automatically parse multipart requests into separate XML nodes.<br/>Select No to always keep the request body as a single XML node.</html>");
        includeMetadataYesRadio.setToolTipText("<html>Select Yes to include request metadata (method, context path, headers,<br/>query parameters) in the XML content. Note that regardless of this<br/>setting, the same metadata is always available in the source map.</html>");
        includeMetadataNoRadio.setToolTipText("<html>Select Yes to include request metadata (method, context path, headers,<br/>query parameters) in the XML content. Note that regardless of this<br/>setting, the same metadata is always available in the source map.</html>");
        staticResourcesTable.setToolTipText("<html>Values in this table are automatically sent back to any request<br/>with the matching context path. There are three resource types:<br/> - <b>File</b>: The value field specifies the path of the file to return.<br/> - <b>Directory</b>: Any file within the directory given by the value<br/>&nbsp;&nbsp;&nbsp;field may be requested, but subdirectories are not included.<br/> - <b>Custom</b>: The value field itself is returned as the response.<br/></html>");
        responseDataTypeBinaryRadio.setToolTipText("<html>If Binary is selected, responses will be decoded from Base64 into raw byte streams.<br/>If Text is selected, responses will be encoded with the specified character set encoding.</html>");
        responseDataTypeTextRadio.setToolTipText("<html>If Binary is selected, responses will be decoded from Base64 into raw byte streams.<br/>If Text is selected, responses will be encoded with the specified character set encoding.</html>");
        binaryMimeTypesField.setToolTipText("<html>When a response comes in with a Content-Type header that<br/>matches one of these entries, the content will be encoded<br/>into a Base64 string. If Regular Expression is unchecked,<br/>specify multiple entries with commas. Otherwise, enter a<br/>valid regular expression to match MIME types against.</html>");
        binaryMimeTypesRegexCheckBox.setToolTipText("<html>When a response comes in with a Content-Type header that<br/>matches one of these entries, the content will be encoded<br/>into a Base64 string. If Regular Expression is unchecked,<br/>specify multiple entries with commas. Otherwise, enter a<br/>valid regular expression to match MIME types against.</html>");
        useResponseHeadersTableRadio.setToolTipText("<html>The table below will be used to populate response headers.</html>");
        useResponseHeadersVariableRadio.setToolTipText("<html>The Java map specified by the following variable will be used to populate response headers.<br/>The map must have String keys and either String or List&lt;String&gt; values.</html>");
        responseHeadersVariableField.setToolTipText("<html>The variable of a Java map to use to populate response headers.<br/>The map must have String keys and either String or List&lt;String&gt; values.</html>");
    }

    protected void initLayout() {
        setLayout(new MigLayout("insets 0 8 0 8, novisualpadding, hidemode 3, gap 12 6", "[][]6[]", "[][][][][][][][][][][][][grow][grow]"));

        add(contextPathLabel, "right");
        add(contextPathField, "w 150!, sx");
        add(receiveTimeoutLabel, "newline, right");
        add(receiveTimeoutField, "w 100!, sx");
        add(messageContentLabel, "newline, right");
        add(messageContentPlainBodyRadio, "split 2");
        add(messageContentXmlBodyRadio);
        add(parseMultipartLabel, "newline, right");
        add(parseMultipartYesRadio, "split 2");
        add(parseMultipartNoRadio);
        add(includeMetadataLabel, "newline, right");
        add(includeMetadataYesRadio, "split 2");
        add(includeMetadataNoRadio);
        add(binaryMimeTypesLabel, "newline, right");
        add(binaryMimeTypesField, "w 300!, sx, split 3");
        add(binaryMimeTypesRegexCheckBox);
        add(httpUrlLabel, "newline, right");
        add(httpUrlField, "w 250!, sx");
        add(responseContentTypeLabel, "newline, right");
        add(responseContentTypeField, "w 125!, sx");
        add(responseDataTypeLabel, "newline, right");
        add(responseDataTypeBinaryRadio, "split 2");
        add(responseDataTypeTextRadio);
        add(charsetEncodingLabel, "newline, right");
        add(charsetEncodingCombobox);
        add(responseStatusCodeLabel, "newline, right");
        add(responseStatusCodeField, "w 125!");
        add(headersLabel, "newline, right");
        add(useResponseHeadersTableRadio, "split 3");
        add(useResponseHeadersVariableRadio);
        add(responseHeadersVariableField, "w 125!, sx");
        add(responseHeadersPane, "newline, growx, pushx, growy, skip 1, span 2, h 104:104:342");
        add(responseHeadersNewButton, "top, flowy, split 2, w 44!");
        add(responseHeadersDeleteButton, "w 44!");
        add(staticResourcesLabel, "newline, top, right");
        add(responseHeadersPane1, "growx, pushx, growy, span 2, h 104:104:342");
        add(staticResourcesNewButton, "top, flowy, split 2, w 44!");
        add(staticResourcesDeleteButton, "w 44!");
    }

    private void messageContentPlainBodyRadioActionPerformed(ActionEvent evt) {
        parent.channelEditPanel.checkAndSetSourceDataType();
        parseMultipartLabel.setEnabled(false);
        parseMultipartYesRadio.setEnabled(false);
        parseMultipartNoRadio.setEnabled(false);
        includeMetadataLabel.setEnabled(false);
        includeMetadataYesRadio.setEnabled(false);
        includeMetadataNoRadio.setEnabled(false);
    }

    private void contextPathFieldKeyReleased(java.awt.event.KeyEvent evt) {
        updateHttpUrl();
    }

    private void responseHeadersNewButtonActionPerformed(ActionEvent evt) {
        ((DefaultTableModel) responseHeadersTable.getModel()).addRow(new Object[] {
                getNewPropertyName(responseHeadersTable), "" });
        responseHeadersTable.setRowSelectionInterval(responseHeadersTable.getRowCount() - 1, responseHeadersTable.getRowCount() - 1);
        parent.setSaveEnabled(true);
    }

    private void responseHeadersDeleteButtonActionPerformed(ActionEvent evt) {
        if (getSelectedRow(responseHeadersTable) != -1 && !responseHeadersTable.isEditing()) {
            ((DefaultTableModel) responseHeadersTable.getModel()).removeRow(getSelectedRow(responseHeadersTable));

            if (responseHeadersTable.getRowCount() != 0) {
                if (responseHeadersLastIndex == 0) {
                    responseHeadersTable.setRowSelectionInterval(0, 0);
                } else if (responseHeadersLastIndex == responseHeadersTable.getRowCount()) {
                    responseHeadersTable.setRowSelectionInterval(responseHeadersLastIndex - 1, responseHeadersLastIndex - 1);
                } else {
                    responseHeadersTable.setRowSelectionInterval(responseHeadersLastIndex, responseHeadersLastIndex);
                }
            }

            parent.setSaveEnabled(true);
        }
    }

    private void messageContentXmlBodyRadioActionPerformed(ActionEvent evt) {
        parent.channelEditPanel.checkAndSetSourceDataType();
        parseMultipartLabel.setEnabled(true);
        parseMultipartYesRadio.setEnabled(true);
        parseMultipartNoRadio.setEnabled(true);
        includeMetadataLabel.setEnabled(true);
        includeMetadataYesRadio.setEnabled(true);
        includeMetadataNoRadio.setEnabled(true);
    }

    private void staticResourcesDeleteButtonActionPerformed(ActionEvent evt) {
        int selectedRow = getSelectedRow(staticResourcesTable);

        if (selectedRow != -1 && !staticResourcesTable.isEditing()) {
            ((DefaultTableModel) staticResourcesTable.getModel()).removeRow(selectedRow);

            if (staticResourcesTable.getRowCount() != 0) {
                if (staticResourcesLastIndex == 0) {
                    staticResourcesTable.setRowSelectionInterval(0, 0);
                } else if (staticResourcesLastIndex == staticResourcesTable.getRowCount()) {
                    staticResourcesTable.setRowSelectionInterval(staticResourcesLastIndex - 1, staticResourcesLastIndex - 1);
                } else {
                    staticResourcesTable.setRowSelectionInterval(staticResourcesLastIndex, staticResourcesLastIndex);
                }
            }

            parent.setSaveEnabled(true);
        }
    }

    private void staticResourcesNewButtonActionPerformed(ActionEvent evt) {
        int contextPathNumber = 1;
        String contextPath = "path" + contextPathNumber;
        while (!checkStaticResourceContextPath(contextPath)) {
            contextPath = "path" + (++contextPathNumber);
        }

        Object[] rowData = new Object[] { contextPath, ResourceType.FILE.toString(), "",
                ContentType.TEXT_PLAIN.getMimeType() };
        ((DefaultTableModel) staticResourcesTable.getModel()).addRow(rowData);
        staticResourcesTable.setRowSelectionInterval(staticResourcesTable.getRowCount() - 1, staticResourcesTable.getRowCount() - 1);
        parent.setSaveEnabled(true);
    }

    private void responseDataTypeBinaryRadioActionPerformed(ActionEvent evt) {
        charsetEncodingLabel.setEnabled(false);
        charsetEncodingCombobox.setEnabled(false);
        charsetEncodingCombobox.setSelectedIndex(0);
    }

    private void responseDataTypeTextRadioActionPerformed(ActionEvent evt) {
        charsetEncodingLabel.setEnabled(true);
        charsetEncodingCombobox.setEnabled(true);
    }
    
    private void useResponseHeadersVariableFieldsEnabled(boolean useTemplate) {
        responseHeadersVariableField.setEnabled(useTemplate);
        responseHeadersTable.setEnabled(!useTemplate);
        responseHeadersNewButton.setEnabled(!useTemplate);
        responseHeadersDeleteButton.setEnabled(!useTemplate && responseHeadersTable.getSelectedRow() > -1);
    }

    // Variables declaration - do not modify
    private MirthTextField binaryMimeTypesField;
    private JLabel binaryMimeTypesLabel;
    private MirthCheckBox binaryMimeTypesRegexCheckBox;
    private MirthComboBox<String> charsetEncodingCombobox;
    private JLabel charsetEncodingLabel;
    protected MirthTextField contextPathField;
    protected JLabel contextPathLabel;
    private JLabel headersLabel;
    protected JTextField httpUrlField;
    protected JLabel httpUrlLabel;
    private ButtonGroup includeHeadersGroup;
    private ButtonGroup includeMetadataButtonGroup;
    private JLabel includeMetadataLabel;
    private MirthRadioButton includeMetadataNoRadio;
    private MirthRadioButton includeMetadataYesRadio;
    private JLabel messageContentLabel;
    private MirthRadioButton messageContentPlainBodyRadio;
    private MirthRadioButton messageContentXmlBodyRadio;
    private ButtonGroup parseMultipartButtonGroup;
    private JLabel parseMultipartLabel;
    private MirthRadioButton parseMultipartNoRadio;
    private MirthRadioButton parseMultipartYesRadio;
    protected MirthTextField receiveTimeoutField;
    protected JLabel receiveTimeoutLabel;
    protected JLabel responseStatusCodeLabel;
    private MirthTextField responseContentTypeField;
    private JLabel responseContentTypeLabel;
    private MirthRadioButton responseDataTypeBinaryRadio;
    private ButtonGroup responseDataTypeButtonGroup;
    private JLabel responseDataTypeLabel;
    private MirthRadioButton responseDataTypeTextRadio;
    private JButton responseHeadersDeleteButton;
    private JButton responseHeadersNewButton;
    private JScrollPane responseHeadersPane;
    private JScrollPane responseHeadersPane1;
    private MirthTable responseHeadersTable;
    private MirthRadioButton useResponseHeadersTableRadio;
    private MirthRadioButton useResponseHeadersVariableRadio;
    private MirthTextField responseHeadersVariableField;
    private MirthTextField responseStatusCodeField;
    private JButton staticResourcesDeleteButton;
    private JLabel staticResourcesLabel;
    private JButton staticResourcesNewButton;
    private MirthTable staticResourcesTable;
    // End of variables declaration
}
