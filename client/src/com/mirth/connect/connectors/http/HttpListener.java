/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
import com.mirth.connect.client.ui.components.MirthComboBoxTableCellEditor;
import com.mirth.connect.client.ui.components.MirthComboBoxTableCellRenderer;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthSyntaxTextArea;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.client.ui.panels.connectors.ListenerSettingsPanel;
import com.mirth.connect.client.ui.panels.reference.ReferenceListFactory;
import com.mirth.connect.connectors.http.HttpStaticResource.ResourceType;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.CodeTemplate.CodeSnippetType;
import com.mirth.connect.model.CodeTemplate.ContextType;

public class HttpListener extends ConnectorSettingsPanel {

    private final int NAME_COLUMN = 0;
    private final int VALUE_COLUMN = 1;
    private final String NAME_COLUMN_NAME = "Name";
    private final String VALUE_COLUMN_NAME = "Value";
    private int responseHeadersLastIndex = -1;
    private int staticResourcesLastIndex = -1;
    private boolean usingHttps = false;

    private enum StaticResourcesColumn {
        CONTEXT_PATH(0, "Context Path"), RESOURCE_TYPE(1, "Resource Type"), VALUE(2, "Value"), CONTENT_TYPE(
                3, "Content Type");

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
        initComponentsManual();
        httpUrlField.setEditable(false);
        parent.setupCharsetEncodingForConnector(charsetEncodingCombobox);

        // This is required because of MIRTH-3305
        Map<String, ArrayList<CodeTemplate>> references = ReferenceListFactory.getInstance().getReferences();
        references.put(getConnectorName() + " Functions", getReferenceItems());
    }

    @Override
    public String getConnectorName() {
        return new HttpReceiverProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        HttpReceiverProperties properties = new HttpReceiverProperties();
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

        properties.setResponseHeaders(getResponseHeaders());
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

        if (props.getResponseHeaders() != null) {
            setResponseHeaders(props.getResponseHeaders());
        } else {
            setResponseHeaders(new LinkedHashMap<String, String>());
        }

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
    public ArrayList<CodeTemplate> getReferenceItems() {
        ArrayList<CodeTemplate> referenceItems = new ArrayList<CodeTemplate>();

        referenceItems.add(new CodeTemplate("Get HTTP Request Method", "Retrieves the method (e.g. GET, POST) from an incoming HTTP request.", "sourceMap.get('method')", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));
        referenceItems.add(new CodeTemplate("Get HTTP Request Context Path", "Retrieves the context path from an incoming HTTP request.", "sourceMap.get('contextPath')", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));
        referenceItems.add(new CodeTemplate("Get HTTP Request Header", "Retrieves a header value from an incoming HTTP request.", "sourceMap.get('headers').get('Header-Name')", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));
        referenceItems.add(new CodeTemplate("Get HTTP Request Parameter", "Retrieves a query/form parameter from an incoming HTTP request. If multiple values exist for the parameter, an array will be returned.", "sourceMap.get('parameters').get('parameterName')", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));
        referenceItems.add(new CodeTemplate("Convert HTTP Payload to XML", "Serializes an HTTP request body into XML. Multipart requests will also automatically be parsed into separate XML nodes. The body may be passed in as a string or input stream.", "HTTPUtil.httpBodyToXml(httpBody, contentType)", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));

        return referenceItems;
    }

    @Override
    public void doLocalDecoration(ConnectorTypeDecoration connectorTypeDecoration) {
        usingHttps = connectorTypeDecoration != null;
        updateHttpUrl();
    }

    @Override
    public boolean requiresXmlDataType() {
        return ((HttpReceiverProperties) getProperties()).isXmlBody();
    }

    public void updateHttpUrl() {
        String server = "<server ip>";
        try {
            server = new URI(PlatformUI.SERVER_NAME).getHost();
        } catch (Exception e) {
            // ignore exceptions getting the server ip
        }

        httpUrlLabel.setText(usingHttps ? "HTTPS URL:" : "HTTP URL:");

        // Display: http://server:port/contextpath/
        httpUrlField.setText("http" + (usingHttps ? "s" : "") + "://" + server + ":" + ((HttpReceiverProperties) getFilledProperties()).getListenerConnectorProperties().getPort() + (contextPathField.getText().startsWith("/") ? "" : "/") + contextPathField.getText() + ((StringUtils.isBlank(contextPathField.getText()) || contextPathField.getText().endsWith("/")) ? "" : "/"));
    }

    public void setResponseHeaders(Map<String, String> responseHeaders) {
        Object[][] tableData = new Object[responseHeaders.size()][2];

        responseHeadersTable = new MirthTable();

        int j = 0;
        Iterator i = responseHeaders.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            tableData[j][NAME_COLUMN] = (String) entry.getKey();
            tableData[j][VALUE_COLUMN] = (String) entry.getValue();
            j++;
        }

        responseHeadersTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[] {
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

            public boolean checkUniqueProperty(String property) {
                boolean exists = false;

                for (int i = 0; i < responseHeadersTable.getRowCount(); i++) {
                    if (responseHeadersTable.getValueAt(i, NAME_COLUMN) != null && ((String) responseHeadersTable.getValueAt(i, NAME_COLUMN)).equalsIgnoreCase(property)) {
                        exists = true;
                    }
                }

                return exists;
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

                if (checkProperties && (value.length() == 0 || checkUniqueProperty(value))) {
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
    }

    public Map<String, String> getResponseHeaders() {
        LinkedHashMap<String, String> responseHeaders = new LinkedHashMap<String, String>();

        for (int i = 0; i < responseHeadersTable.getRowCount(); i++) {
            if (((String) responseHeadersTable.getValueAt(i, NAME_COLUMN)).length() > 0) {
                responseHeaders.put(((String) responseHeadersTable.getValueAt(i, NAME_COLUMN)), ((String) responseHeadersTable.getValueAt(i, VALUE_COLUMN)));
            }
        }

        return responseHeaders;
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
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        includeHeadersGroup = new javax.swing.ButtonGroup();
        parseMultipartButtonGroup = new javax.swing.ButtonGroup();
        includeMetadataButtonGroup = new javax.swing.ButtonGroup();
        responseDataTypeButtonGroup = new javax.swing.ButtonGroup();
        messageContentPlainBodyRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        messageContentLabel = new javax.swing.JLabel();
        responseContentTypeField = new com.mirth.connect.client.ui.components.MirthTextField();
        responseContentTypeLabel = new javax.swing.JLabel();
        charsetEncodingCombobox = new com.mirth.connect.client.ui.components.MirthComboBox();
        charsetEncodingLabel = new javax.swing.JLabel();
        contextPathLabel = new javax.swing.JLabel();
        contextPathField = new com.mirth.connect.client.ui.components.MirthTextField();
        receiveTimeoutLabel = new javax.swing.JLabel();
        receiveTimeoutField = new com.mirth.connect.client.ui.components.MirthTextField();
        httpUrlField = new javax.swing.JTextField();
        httpUrlLabel = new javax.swing.JLabel();
        headersLabel = new javax.swing.JLabel();
        responseHeadersPane = new javax.swing.JScrollPane();
        responseHeadersTable = new com.mirth.connect.client.ui.components.MirthTable();
        responseHeadersNewButton = new javax.swing.JButton();
        responseHeadersDeleteButton = new javax.swing.JButton();
        receiveTimeoutLabel1 = new javax.swing.JLabel();
        responseStatusCodeField = new com.mirth.connect.client.ui.components.MirthTextField();
        messageContentXmlBodyRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        parseMultipartLabel = new javax.swing.JLabel();
        parseMultipartYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        parseMultipartNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        includeMetadataLabel = new javax.swing.JLabel();
        includeMetadataYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        includeMetadataNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        staticResourcesLabel = new javax.swing.JLabel();
        staticResourcesDeleteButton = new javax.swing.JButton();
        staticResourcesNewButton = new javax.swing.JButton();
        responseHeadersPane1 = new javax.swing.JScrollPane();
        staticResourcesTable = new com.mirth.connect.client.ui.components.MirthTable();
        responseDataTypeLabel = new javax.swing.JLabel();
        responseDataTypeBinaryRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        responseDataTypeTextRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        binaryMimeTypesLabel = new javax.swing.JLabel();
        binaryMimeTypesField = new com.mirth.connect.client.ui.components.MirthTextField();
        binaryMimeTypesRegexCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        messageContentPlainBodyRadio.setBackground(new java.awt.Color(255, 255, 255));
        includeHeadersGroup.add(messageContentPlainBodyRadio);
        messageContentPlainBodyRadio.setText("Plain Body");
        messageContentPlainBodyRadio.setToolTipText("<html>If selected, the request body will be sent to the channel as a raw string.</html>");
        messageContentPlainBodyRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        messageContentPlainBodyRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                messageContentPlainBodyRadioActionPerformed(evt);
            }
        });

        messageContentLabel.setText("Message Content:");

        responseContentTypeField.setToolTipText("The MIME type to be used for the response.");

        responseContentTypeLabel.setText("Response Content Type:");

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "default", "utf-8", "iso-8859-1", "utf-16 (le)", "utf-16 (be)", "utf-16 (bom)", "us-ascii" }));
        charsetEncodingCombobox.setToolTipText("<html>Select the character set encoding to be used for the response to the sending system.<br>Set to Default to assume the default character set encoding for the JVM running Mirth.</html>");

        charsetEncodingLabel.setText("Charset Encoding:");

        contextPathLabel.setText("Base Context Path:");

        contextPathField.setToolTipText("The context path for the HTTP Listener URL.");
        contextPathField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                contextPathFieldKeyReleased(evt);
            }
        });

        receiveTimeoutLabel.setText("Receive Timeout (ms):");

        receiveTimeoutField.setToolTipText("Enter the maximum idle time in milliseconds for a connection.");

        httpUrlField.setToolTipText("<html>Displays the generated HTTP URL for the HTTP Listener.</html>");

        httpUrlLabel.setText("HTTP URL:");

        headersLabel.setText("Response Headers:");

        responseHeadersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Value"
            }
        ));
        responseHeadersTable.setToolTipText("Response header parameters are encoded as HTTP headers in the response sent to the client.");
        responseHeadersPane.setViewportView(responseHeadersTable);

        responseHeadersNewButton.setText("New");
        responseHeadersNewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                responseHeadersNewButtonActionPerformed(evt);
            }
        });

        responseHeadersDeleteButton.setText("Delete");
        responseHeadersDeleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                responseHeadersDeleteButtonActionPerformed(evt);
            }
        });

        receiveTimeoutLabel1.setText("Response Status Code:");

        responseStatusCodeField.setToolTipText("<html>Enter the status code for the HTTP response.  If this field is left blank a <br>default status code of 200 will be returned for a successful message, <br>and 500 will be returned for an errored message. If a \"Respond from\" <br>value is chosen, that response will be used to determine a successful <br>or errored response.<html>");

        messageContentXmlBodyRadio.setBackground(new java.awt.Color(255, 255, 255));
        includeHeadersGroup.add(messageContentXmlBodyRadio);
        messageContentXmlBodyRadio.setText("XML Body");
        messageContentXmlBodyRadio.setToolTipText("<html>If selected, the request body will be sent to the channel as serialized XML.</html>");
        messageContentXmlBodyRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        messageContentXmlBodyRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                messageContentXmlBodyRadioActionPerformed(evt);
            }
        });

        parseMultipartLabel.setText("Parse Multipart:");

        parseMultipartYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        parseMultipartButtonGroup.add(parseMultipartYesRadio);
        parseMultipartYesRadio.setText("Yes");
        parseMultipartYesRadio.setToolTipText("<html>Select Yes to automatically parse multipart requests into separate XML nodes.<br/>Select No to always keep the request body as a single XML node.</html>");
        parseMultipartYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        parseMultipartNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        parseMultipartButtonGroup.add(parseMultipartNoRadio);
        parseMultipartNoRadio.setText("No");
        parseMultipartNoRadio.setToolTipText("<html>Select Yes to automatically parse multipart requests into separate XML nodes.<br/>Select No to always keep the request body as a single XML node.</html>");
        parseMultipartNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        includeMetadataLabel.setText("Include Metadata:");

        includeMetadataYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        includeMetadataButtonGroup.add(includeMetadataYesRadio);
        includeMetadataYesRadio.setText("Yes");
        includeMetadataYesRadio.setToolTipText("<html>Select Yes to include request metadata (method, context path, headers,<br/>query parameters) in the XML content. Note that regardless of this<br/>setting, the same metadata is always available in the source map.</html>");
        includeMetadataYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        includeMetadataNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        includeMetadataButtonGroup.add(includeMetadataNoRadio);
        includeMetadataNoRadio.setText("No");
        includeMetadataNoRadio.setToolTipText("<html>Select Yes to include request metadata (method, context path, headers,<br/>query parameters) in the XML content. Note that regardless of this<br/>setting, the same metadata is always available in the source map.</html>");
        includeMetadataNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        staticResourcesLabel.setText("Static Resources:");

        staticResourcesDeleteButton.setText("Delete");
        staticResourcesDeleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                staticResourcesDeleteButtonActionPerformed(evt);
            }
        });

        staticResourcesNewButton.setText("New");
        staticResourcesNewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                staticResourcesNewButtonActionPerformed(evt);
            }
        });

        staticResourcesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        staticResourcesTable.setToolTipText("<html>Values in this table are automatically sent back to any request<br/>with the matching context path. There are three resource types:<br/> - <b>File</b>: The value field specifies the path of the file to return.<br/> - <b>Directory</b>: Any file within the directory given by the value<br/>&nbsp;&nbsp;&nbsp;field may be requested, but subdirectories are not included.<br/> - <b>Custom</b>: The value field itself is returned as the response.<br/></html>");
        responseHeadersPane1.setViewportView(staticResourcesTable);

        responseDataTypeLabel.setText("Response Data Type:");

        responseDataTypeBinaryRadio.setBackground(new java.awt.Color(255, 255, 255));
        responseDataTypeButtonGroup.add(responseDataTypeBinaryRadio);
        responseDataTypeBinaryRadio.setText("Binary");
        responseDataTypeBinaryRadio.setToolTipText("<html>If Binary is selected, responses will be decoded from Base64 into raw byte streams.<br/>If Text is selected, responses will be encoded with the specified character set encoding.</html>");
        responseDataTypeBinaryRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        responseDataTypeBinaryRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                responseDataTypeBinaryRadioActionPerformed(evt);
            }
        });

        responseDataTypeTextRadio.setBackground(new java.awt.Color(255, 255, 255));
        responseDataTypeButtonGroup.add(responseDataTypeTextRadio);
        responseDataTypeTextRadio.setText("Text");
        responseDataTypeTextRadio.setToolTipText("<html>If Binary is selected, responses will be decoded from Base64 into raw byte streams.<br/>If Text is selected, responses will be encoded with the specified character set encoding.</html>");
        responseDataTypeTextRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        responseDataTypeTextRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                responseDataTypeTextRadioActionPerformed(evt);
            }
        });

        binaryMimeTypesLabel.setText("Binary MIME Types:");

        binaryMimeTypesField.setToolTipText("<html>When a response comes in with a Content-Type header that<br/>matches one of these entries, the content will be encoded<br/>into a Base64 string. If Regular Expression is unchecked,<br/>specify multiple entries with commas. Otherwise, enter a<br/>valid regular expression to match MIME types against.</html>");
        binaryMimeTypesField.setMinimumSize(new java.awt.Dimension(200, 21));
        binaryMimeTypesField.setPreferredSize(new java.awt.Dimension(200, 21));

        binaryMimeTypesRegexCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        binaryMimeTypesRegexCheckBox.setText("Regular Expression");
        binaryMimeTypesRegexCheckBox.setToolTipText("<html>When a response comes in with a Content-Type header that<br/>matches one of these entries, the content will be encoded<br/>into a Base64 string. If Regular Expression is unchecked,<br/>specify multiple entries with commas. Otherwise, enter a<br/>valid regular expression to match MIME types against.</html>");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(receiveTimeoutLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(binaryMimeTypesLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(headersLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(httpUrlLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(responseDataTypeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(receiveTimeoutLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(contextPathLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(messageContentLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(staticResourcesLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(includeMetadataLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(charsetEncodingLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(responseContentTypeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(parseMultipartLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(responseHeadersPane, javax.swing.GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE)
                    .addComponent(responseHeadersPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(responseStatusCodeField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(httpUrlField, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(responseDataTypeBinaryRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(responseDataTypeTextRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(receiveTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(contextPathField, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(messageContentPlainBodyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(messageContentXmlBodyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(includeMetadataYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(includeMetadataNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(parseMultipartYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(parseMultipartNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(responseContentTypeField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(binaryMimeTypesField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(binaryMimeTypesRegexCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(responseHeadersNewButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(responseHeadersDeleteButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(staticResourcesNewButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(staticResourcesDeleteButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contextPathLabel)
                    .addComponent(contextPathField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(receiveTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(receiveTimeoutLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(messageContentLabel)
                    .addComponent(messageContentPlainBodyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(messageContentXmlBodyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                    .addComponent(binaryMimeTypesLabel)
                    .addComponent(binaryMimeTypesField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(binaryMimeTypesRegexCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(httpUrlLabel)
                    .addComponent(httpUrlField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(responseContentTypeLabel)
                    .addComponent(responseContentTypeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(responseDataTypeLabel)
                    .addComponent(responseDataTypeBinaryRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(responseDataTypeTextRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(charsetEncodingLabel)
                    .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(receiveTimeoutLabel1)
                    .addComponent(responseStatusCodeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(headersLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(responseHeadersNewButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(responseHeadersDeleteButton))
                    .addComponent(responseHeadersPane, javax.swing.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(staticResourcesLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(staticResourcesNewButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(staticResourcesDeleteButton))
                    .addComponent(responseHeadersPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void messageContentPlainBodyRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_messageContentPlainBodyRadioActionPerformed
        parent.channelEditPanel.checkAndSetXmlDataType();
        parseMultipartLabel.setEnabled(false);
        parseMultipartYesRadio.setEnabled(false);
        parseMultipartNoRadio.setEnabled(false);
        includeMetadataLabel.setEnabled(false);
        includeMetadataYesRadio.setEnabled(false);
        includeMetadataNoRadio.setEnabled(false);
    }//GEN-LAST:event_messageContentPlainBodyRadioActionPerformed

    private void contextPathFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_contextPathFieldKeyReleased
        updateHttpUrl();
    }//GEN-LAST:event_contextPathFieldKeyReleased

    private void responseHeadersNewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_responseHeadersNewButtonActionPerformed
        ((DefaultTableModel) responseHeadersTable.getModel()).addRow(new Object[] {
                getNewPropertyName(responseHeadersTable), "" });
        responseHeadersTable.setRowSelectionInterval(responseHeadersTable.getRowCount() - 1, responseHeadersTable.getRowCount() - 1);
        parent.setSaveEnabled(true);
    }//GEN-LAST:event_responseHeadersNewButtonActionPerformed

    private void responseHeadersDeleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_responseHeadersDeleteButtonActionPerformed
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
    }//GEN-LAST:event_responseHeadersDeleteButtonActionPerformed

    private void messageContentXmlBodyRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_messageContentXmlBodyRadioActionPerformed
        parent.channelEditPanel.checkAndSetXmlDataType();
        parseMultipartLabel.setEnabled(true);
        parseMultipartYesRadio.setEnabled(true);
        parseMultipartNoRadio.setEnabled(true);
        includeMetadataLabel.setEnabled(true);
        includeMetadataYesRadio.setEnabled(true);
        includeMetadataNoRadio.setEnabled(true);
    }//GEN-LAST:event_messageContentXmlBodyRadioActionPerformed

    private void staticResourcesDeleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_staticResourcesDeleteButtonActionPerformed
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
    }//GEN-LAST:event_staticResourcesDeleteButtonActionPerformed

    private void staticResourcesNewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_staticResourcesNewButtonActionPerformed
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
    }//GEN-LAST:event_staticResourcesNewButtonActionPerformed

    private void responseDataTypeBinaryRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_responseDataTypeBinaryRadioActionPerformed
        charsetEncodingLabel.setEnabled(false);
        charsetEncodingCombobox.setEnabled(false);
        charsetEncodingCombobox.setSelectedIndex(0);
    }//GEN-LAST:event_responseDataTypeBinaryRadioActionPerformed

    private void responseDataTypeTextRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_responseDataTypeTextRadioActionPerformed
        charsetEncodingLabel.setEnabled(true);
        charsetEncodingCombobox.setEnabled(true);
    }//GEN-LAST:event_responseDataTypeTextRadioActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.mirth.connect.client.ui.components.MirthTextField binaryMimeTypesField;
    private javax.swing.JLabel binaryMimeTypesLabel;
    private com.mirth.connect.client.ui.components.MirthCheckBox binaryMimeTypesRegexCheckBox;
    private com.mirth.connect.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private javax.swing.JLabel charsetEncodingLabel;
    private com.mirth.connect.client.ui.components.MirthTextField contextPathField;
    private javax.swing.JLabel contextPathLabel;
    private javax.swing.JLabel headersLabel;
    private javax.swing.JTextField httpUrlField;
    private javax.swing.JLabel httpUrlLabel;
    private javax.swing.ButtonGroup includeHeadersGroup;
    private javax.swing.ButtonGroup includeMetadataButtonGroup;
    private javax.swing.JLabel includeMetadataLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton includeMetadataNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton includeMetadataYesRadio;
    private javax.swing.JLabel messageContentLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton messageContentPlainBodyRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton messageContentXmlBodyRadio;
    private javax.swing.ButtonGroup parseMultipartButtonGroup;
    private javax.swing.JLabel parseMultipartLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton parseMultipartNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton parseMultipartYesRadio;
    private com.mirth.connect.client.ui.components.MirthTextField receiveTimeoutField;
    private javax.swing.JLabel receiveTimeoutLabel;
    private javax.swing.JLabel receiveTimeoutLabel1;
    private com.mirth.connect.client.ui.components.MirthTextField responseContentTypeField;
    private javax.swing.JLabel responseContentTypeLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton responseDataTypeBinaryRadio;
    private javax.swing.ButtonGroup responseDataTypeButtonGroup;
    private javax.swing.JLabel responseDataTypeLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton responseDataTypeTextRadio;
    private javax.swing.JButton responseHeadersDeleteButton;
    private javax.swing.JButton responseHeadersNewButton;
    private javax.swing.JScrollPane responseHeadersPane;
    private javax.swing.JScrollPane responseHeadersPane1;
    private com.mirth.connect.client.ui.components.MirthTable responseHeadersTable;
    private com.mirth.connect.client.ui.components.MirthTextField responseStatusCodeField;
    private javax.swing.JButton staticResourcesDeleteButton;
    private javax.swing.JLabel staticResourcesLabel;
    private javax.swing.JButton staticResourcesNewButton;
    private com.mirth.connect.client.ui.components.MirthTable staticResourcesTable;
    // End of variables declaration//GEN-END:variables
}
