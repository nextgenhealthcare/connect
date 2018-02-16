/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.ws;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.XMLTokenMarker;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.ConnectorTypeDecoration;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.TextFieldCellEditor;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthComboBox;
import com.mirth.connect.client.ui.components.MirthEditableComboBox;
import com.mirth.connect.client.ui.components.MirthIconTextField;
import com.mirth.connect.client.ui.components.MirthPasswordField;
import com.mirth.connect.client.ui.components.MirthRadioButton;
import com.mirth.connect.client.ui.components.MirthSyntaxTextArea;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.client.ui.panels.connectors.ResponseHandler;
import com.mirth.connect.connectors.ws.DefinitionServiceMap.DefinitionPortMap;
import com.mirth.connect.connectors.ws.DefinitionServiceMap.PortInformation;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.model.Connector.Mode;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.util.ConnectionTestResponse;

public class WebServiceSender extends ConnectorSettingsPanel {

    protected static final ImageIcon ICON_LOCK_X = new ImageIcon(Frame.class.getResource("images/lock_x.png"));
    protected static final Color COLOR_SSL_NOT_CONFIGURED = new Color(0xFFF099);
    protected static final String SSL_TOOL_TIP = "<html>The default system certificate store will be used for this connection.<br/>As a result, certain security options are not available and mutual<br/>authentication (two-way authentication) is not supported.</html>";

    private final int ID_COLUMN_NUMBER = 0;
    private final int CONTENT_COLUMN_NUMBER = 1;
    private final int MIME_TYPE_COLUMN_NUMBER = 2;
    private final String ID_COLUMN_NAME = "ID";
    private final String CONTENT_COLUMN_NAME = "Content";
    private final String MIME_TYPE_COLUMN_NAME = "MIME Type";

    private final int NAME_COLUMN = 0;
    private final int VALUE_COLUMN = 1;
    private final String NAME_COLUMN_NAME = "Name";
    private final String VALUE_COLUMN_NAME = "Value";
    private int headerLastIndex = -1;

    ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
    private Frame parent;
    private DefinitionServiceMap currentServiceMap;

    public WebServiceSender() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        initToolTips();
        initLayout();
    }

    @Override
    public String getConnectorName() {
        return new WebServiceDispatcherProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        WebServiceDispatcherProperties properties = new WebServiceDispatcherProperties();

        properties.setWsdlUrl(wsdlUrlField.getText());
        properties.setService(StringUtils.trimToEmpty((String) serviceComboBox.getEditor().getItem()));
        properties.setPort(StringUtils.trimToEmpty((String) portComboBox.getEditor().getItem()));
        properties.setLocationURI(StringUtils.trimToEmpty((String) locationURIComboBox.getEditor().getItem()));
        properties.setSocketTimeout(socketTimeoutField.getText());
        properties.setSoapAction(soapActionField.getText());

        properties.setOneWay(invocationOneWayRadio.isSelected());

        if (operationComboBox.getSelectedIndex() != -1) {
            properties.setOperation((String) operationComboBox.getSelectedItem());
        }

        properties.setUseAuthentication(authenticationYesRadio.isSelected());

        properties.setUsername(usernameField.getText());
        properties.setPassword(new String(passwordField.getPassword()));

        properties.setEnvelope(soapEnvelopeTextArea.getText());

        ArrayList<String> operations = new ArrayList<String>();
        for (int i = 0; i < operationComboBox.getModel().getSize(); i++) {
            operations.add((String) operationComboBox.getModel().getElementAt(i));
        }

        properties.setWsdlDefinitionMap(currentServiceMap);
        properties.setHeaders(getHeaderProperties());

        properties.setUseMtom(useMtomYesRadio.isSelected());

        List<List<String>> attachments = getAttachments();
        properties.setAttachmentNames(attachments.get(0));
        properties.setAttachmentContents(attachments.get(1));
        properties.setAttachmentTypes(attachments.get(2));

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        WebServiceDispatcherProperties props = (WebServiceDispatcherProperties) properties;

        wsdlUrlField.setText(props.getWsdlUrl());
        soapActionField.setText(props.getSoapAction());
        urlFieldChanged();

        soapEnvelopeTextArea.setText(props.getEnvelope());
        socketTimeoutField.setText(props.getSocketTimeout());

        if (props.isUseAuthentication()) {
            authenticationYesRadio.setSelected(true);
            authenticationYesRadioActionPerformed(null);
        } else {
            authenticationNoRadio.setSelected(true);
            authenticationNoRadioActionPerformed(null);
        }

        usernameField.setText(props.getUsername());
        passwordField.setText(props.getPassword());

        if (props.isOneWay()) {
            invocationOneWayRadio.setSelected(true);
        } else {
            invocationTwoWayRadio.setSelected(true);
        }

        boolean enabled = parent.isSaveEnabled();

        currentServiceMap = props.getWsdlDefinitionMap();
        loadServiceMap();

        serviceComboBox.setSelectedItem(props.getService());
        portComboBox.setSelectedItem(props.getPort());
        locationURIComboBox.setSelectedItem(props.getLocationURI());
        operationComboBox.setSelectedItem(props.getOperation());
        updateGenerateEnvelopeButtonEnabled();

        parent.setSaveEnabled(enabled);

        if (props.getHeaders() != null) {
            setHeaderProperties(props.getHeaders());
        } else {
            setHeaderProperties(new LinkedHashMap<String, List<String>>());
        }

        List<List<String>> attachments = new ArrayList<List<String>>();

        attachments.add(props.getAttachmentNames());
        attachments.add(props.getAttachmentContents());
        attachments.add(props.getAttachmentTypes());

        setAttachments(attachments);

        if (props.isUseMtom()) {
            useMtomYesRadio.setSelected(true);
            useMtomYesRadioActionPerformed(null);
        } else {
            useMtomNoRadio.setSelected(true);
            useMtomNoRadioActionPerformed(null);
        }
    }

    @Override
    public ConnectorProperties getDefaults() {
        return new WebServiceDispatcherProperties();
    }

    @Override
    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        WebServiceDispatcherProperties props = (WebServiceDispatcherProperties) properties;

        boolean valid = true;

        if (props.getWsdlUrl().length() == 0) {
            valid = false;
            if (highlight) {
                wsdlUrlField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (props.getService().length() == 0) {
            valid = false;
            if (highlight) {
                serviceComboBox.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (props.getPort().length() == 0) {
            valid = false;
            if (highlight) {
                portComboBox.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (StringUtils.isBlank(props.getSocketTimeout())) {
            valid = false;
            if (highlight) {
                socketTimeoutField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (props.getEnvelope().length() == 0) {
            valid = false;
            if (highlight) {
                soapEnvelopeTextArea.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return valid;
    }

    @Override
    public void resetInvalidProperties() {
        wsdlUrlField.setBackground(null);
        urlFieldChanged();
        serviceComboBox.setBackground(new Color(0xDEDEDE));
        portComboBox.setBackground(new Color(0xDEDEDE));
        locationURIComboBox.setBackground(UIConstants.COMBO_BOX_BACKGROUND);
        socketTimeoutField.setBackground(null);
        soapEnvelopeTextArea.setBackground(null);
    }

    @Override
    public ConnectorTypeDecoration getConnectorTypeDecoration() {
        if (isUsingHttps(wsdlUrlField.getText()) || isUsingHttps(String.valueOf(locationURIComboBox.getSelectedItem()))) {
            return new ConnectorTypeDecoration(Mode.DESTINATION, "(SSL Not Configured)", ICON_LOCK_X, SSL_TOOL_TIP, sslWarningPanel, COLOR_SSL_NOT_CONFIGURED);
        } else {
            return new ConnectorTypeDecoration(Mode.DESTINATION);
        }
    }

    @Override
    public void doLocalDecoration(ConnectorTypeDecoration connectorTypeDecoration) {
        if (connectorTypeDecoration != null) {
            wsdlUrlField.setIcon(connectorTypeDecoration.getIcon());
            wsdlUrlField.setAlternateToolTipText(connectorTypeDecoration.getIconToolTipText());
            wsdlUrlField.setIconPopupMenuComponent(connectorTypeDecoration.getIconPopupComponent());
            wsdlUrlField.setBackground(connectorTypeDecoration.getHighlightColor());
            locationURIComboBox.setBackground(connectorTypeDecoration.getHighlightColor() != null ? connectorTypeDecoration.getHighlightColor() : UIConstants.COMBO_BOX_BACKGROUND);
        }
    }

    protected DefinitionServiceMap getCurrentServiceMap() {
        return currentServiceMap;
    }

    protected void setCurrentServiceMap(DefinitionServiceMap serviceMap) {
        currentServiceMap = serviceMap;
    }

    protected boolean canSetLocationURI() {
        return true;
    }

    protected void loadServiceMap() {
        // First reset the service/port/operation
        serviceComboBox.setModel(new DefaultComboBoxModel());
        portComboBox.setModel(new DefaultComboBoxModel());

        if (canSetLocationURI()) {
            locationURIComboBox.setModel(new DefaultComboBoxModel());
        }

        operationComboBox.setModel(new DefaultComboBoxModel(new String[] {
                WebServiceDispatcherProperties.WEBSERVICE_DEFAULT_DROPDOWN }));

        if (currentServiceMap != null) {
            serviceComboBox.setModel(new DefaultComboBoxModel(currentServiceMap.getMap().keySet().toArray()));

            // If at least one service exists, make sure to trigger the action performed handler
            if (serviceComboBox.getModel().getSize() > 0) {
                serviceComboBox.setSelectedIndex(0);
            }
        }
    }

    protected boolean isUsingHttps(String url) {
        if (StringUtils.isNotBlank(url)) {
            try {
                URI hostURI = new URI(url);
                String hostScheme = hostURI.getScheme();
                if (hostScheme != null && hostScheme.toLowerCase().equals("https")) {
                    return true;
                }
            } catch (URISyntaxException e) {
                if (url.toLowerCase().startsWith("https")) {
                    return true;
                }
            }
        }

        return false;
    }

    private void urlFieldChanged() {
        decorateConnectorType();
    }

    private boolean isDefaultOperations() {
        return (operationComboBox.getItemCount() == 1 && operationComboBox.getItemAt(0).equals(WebServiceDispatcherProperties.WEBSERVICE_DEFAULT_DROPDOWN));
    }

    public void setHeaderProperties(Map<String, List<String>> properties) {
        int size = 0;
        for (List<String> list : properties.values()) {
            size += list.size();
        }

        Object[][] tableData = new Object[size][2];

        int j = 0;
        Iterator<Entry<String, List<String>>> i = properties.entrySet().iterator();
        while (i.hasNext()) {
            Entry<String, List<String>> entry = i.next();
            for (String keyValue : (List<String>) entry.getValue()) {
                tableData[j][NAME_COLUMN] = (String) entry.getKey();
                tableData[j][VALUE_COLUMN] = keyValue;
                j++;
            }
        }

        ((RefreshTableModel) headersTable.getModel()).refreshDataVector(tableData);
    }

    public Map<String, List<String>> getHeaderProperties() {
        Map<String, List<String>> properties = new HashMap<String, List<String>>();

        for (int i = 0; i < headersTable.getRowCount(); i++) {
            String key = (String) headersTable.getValueAt(i, NAME_COLUMN);

            List<String> headers = properties.get(key);

            if (headers == null) {
                headers = new ArrayList<String>();
                properties.put(key, headers);
            }

            headers.add((String) headersTable.getValueAt(i, VALUE_COLUMN));
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

    private void setAttachments(List<List<String>> attachments) {
        List<String> attachmentIds = attachments.get(0);
        List<String> attachmentContents = attachments.get(1);
        List<String> attachmentTypes = attachments.get(2);

        Object[][] tableData = new Object[attachmentIds.size()][3];

        for (int i = 0; i < attachmentIds.size(); i++) {
            tableData[i][ID_COLUMN_NUMBER] = attachmentIds.get(i);
            tableData[i][CONTENT_COLUMN_NUMBER] = attachmentContents.get(i);
            tableData[i][MIME_TYPE_COLUMN_NUMBER] = attachmentTypes.get(i);
        }

        ((RefreshTableModel) attachmentsTable.getModel()).refreshDataVector(tableData);
    }

    private List<List<String>> getAttachments() {
        List<List<String>> attachments = new ArrayList<List<String>>();

        ArrayList<String> attachmentIds = new ArrayList<String>();
        ArrayList<String> attachmentContents = new ArrayList<String>();
        ArrayList<String> attachmentTypes = new ArrayList<String>();

        for (int i = 0; i < attachmentsTable.getModel().getRowCount(); i++) {
            if (((String) attachmentsTable.getModel().getValueAt(i, ID_COLUMN_NUMBER)).length() > 0) {
                attachmentIds.add((String) attachmentsTable.getModel().getValueAt(i, ID_COLUMN_NUMBER));
                attachmentContents.add((String) attachmentsTable.getModel().getValueAt(i, CONTENT_COLUMN_NUMBER));
                attachmentTypes.add((String) attachmentsTable.getModel().getValueAt(i, MIME_TYPE_COLUMN_NUMBER));
            }
        }

        attachments.add(attachmentIds);
        attachments.add(attachmentContents);
        attachments.add(attachmentTypes);

        return attachments;
    }

    public void stopCellEditing() {
        if (attachmentsTable.isEditing()) {
            attachmentsTable.getColumnModel().getColumn(attachmentsTable.convertColumnIndexToModel(attachmentsTable.getEditingColumn())).getCellEditor().stopCellEditing();
        }
    }

    /**
     * Get the name that should be used for a new user so that it is unique.
     */
    private String getNewAttachmentId(int size) {
        String temp = "Attachment";

        for (int i = 1; i <= size; i++) {
            boolean exists = false;

            for (int j = 0; j < size - 1; j++) {
                if (((String) attachmentsTable.getModel().getValueAt(j, attachmentsTable.getColumnModelIndex(ID_COLUMN_NAME))).equalsIgnoreCase(temp + i)) {
                    exists = true;
                }
            }

            if (!exists) {
                return temp + i;
            }
        }
        return "";
    }

    protected void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);

        wsdlUrlLabel = new JLabel("WSDL URL:");

        KeyListener keyListener = new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                urlFieldChanged();
            }
        };
        wsdlUrlField = new MirthIconTextField();
        wsdlUrlField.addKeyListener(keyListener);

        getOperationsButton = new JButton("Get Operations");
        getOperationsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                getOperationsButtonActionPerformed(evt);
            }
        });

        wsdlUrlTestConnectionButton = new JButton("Test Connection");
        wsdlUrlTestConnectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                testConnectionButtonActionPerformed(true);
            }
        });

        serviceLabel = new JLabel("Service:");
        serviceComboBox = new MirthEditableComboBox();
        serviceComboBox.setBackground(UIConstants.COMBO_BOX_BACKGROUND);
        serviceComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                serviceComboBoxActionPerformed(evt);
            }
        });

        portLabel = new JLabel("Port / Endpoint:");
        portComboBox = new MirthEditableComboBox();
        portComboBox.setBackground(UIConstants.COMBO_BOX_BACKGROUND);
        portComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                portComboBoxActionPerformed(evt);
            }
        });

        locationURILabel = new JLabel("Location URI:");
        locationURIComboBox = new MirthEditableComboBox();
        locationURIComboBox.setBackground(UIConstants.COMBO_BOX_BACKGROUND);

        locationURITestConnectionButton = new JButton("Test Connection");
        locationURITestConnectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                testConnectionButtonActionPerformed(false);
            }
        });

        socketTimeoutLabel = new JLabel("Socket Timeout (ms):");
        socketTimeoutField = new MirthTextField();

        authenticationLabel = new JLabel("Authentication:");
        ButtonGroup authenticationButtonGroup = new ButtonGroup();

        authenticationYesRadio = new MirthRadioButton("Yes");
        authenticationYesRadio.setBackground(getBackground());
        authenticationYesRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                authenticationYesRadioActionPerformed(evt);
            }
        });
        authenticationButtonGroup.add(authenticationYesRadio);

        authenticationNoRadio = new MirthRadioButton("No");
        authenticationNoRadio.setBackground(getBackground());
        authenticationNoRadio.setText("No");
        authenticationNoRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                authenticationNoRadioActionPerformed(evt);
            }
        });
        authenticationButtonGroup.add(authenticationNoRadio);

        usernameLabel = new JLabel("Username:");
        usernameField = new MirthTextField();

        passwordLabel = new JLabel("Password:");
        passwordField = new MirthPasswordField();

        invocationTypeLabel = new JLabel("Invocation Type:");
        ButtonGroup invocationButtonGroup = new ButtonGroup();

        invocationOneWayRadio = new MirthRadioButton("One-Way");
        invocationOneWayRadio.setBackground(getBackground());
        invocationButtonGroup.add(invocationOneWayRadio);

        invocationTwoWayRadio = new MirthRadioButton("Two-Way");
        invocationTwoWayRadio.setBackground(getBackground());
        invocationButtonGroup.add(invocationTwoWayRadio);

        operationLabel = new JLabel("Operation:");
        operationComboBox = new MirthComboBox();
        operationComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String soapAction = "";

                // Leave SOAP Action empty if Press Get Operations is selected
                if (!WebServiceDispatcherProperties.WEBSERVICE_DEFAULT_DROPDOWN.equals(operationComboBox.getSelectedItem())) {
                    String selectedOperation = (String) operationComboBox.getSelectedItem();

                    if (currentServiceMap != null) {
                        DefinitionPortMap portMap = currentServiceMap.getMap().get(serviceComboBox.getSelectedItem());

                        if (portMap != null) {
                            PortInformation portInfo = portMap.getMap().get(portComboBox.getSelectedItem());

                            if (portInfo != null && CollectionUtils.isNotEmpty(portInfo.getOperations()) && CollectionUtils.isNotEmpty(portInfo.getActions())) {
                                int index = portInfo.getOperations().indexOf(selectedOperation);

                                if (index >= 0 && index < portInfo.getActions().size()) {
                                    soapAction = portInfo.getActions().get(index);
                                }
                            }
                        }
                    }
                }

                soapActionField.setText(soapAction);
            }
        });

        generateEnvelopeButton = new JButton("Generate Envelope");
        generateEnvelopeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                generateEnvelopeActionPerformed(evt);
            }
        });

        soapActionLabel = new JLabel("SOAP Action:");
        soapActionField = new MirthIconTextField();
        soapActionField.setBackground(UIConstants.COMBO_BOX_BACKGROUND);
        soapActionField.addKeyListener(keyListener);

        soapEnvelopeLabel = new JLabel("SOAP Envelope:");

        soapEnvelopeTextArea = new MirthSyntaxTextArea(true);
        soapEnvelopeTextArea.setBorder(BorderFactory.createEtchedBorder());
        soapEnvelopeTextArea.setMinimumSize(new Dimension(26, 115));
        SyntaxDocument document = new SyntaxDocument();
        document.setTokenMarker(new XMLTokenMarker());
        soapEnvelopeTextArea.setDocument(document);

        headersLabel = new JLabel("Headers:");

        headersTable = new MirthTable();
        headersTable.setModel(new RefreshTableModel(new String[] { "Name", "Value" }, 0));

        headersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                if (getSelectedRow(headersTable) != -1) {
                    headerLastIndex = getSelectedRow(headersTable);
                    headersDeleteButton.setEnabled(true);
                } else {
                    headersDeleteButton.setEnabled(false);
                }
            }
        });

        class WebServiceTableCellEditor extends TextFieldCellEditor {
            boolean checkProperties;

            public WebServiceTableCellEditor(boolean checkProperties) {
                super();
                this.checkProperties = checkProperties;
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

        headersTable.getColumnModel().getColumn(headersTable.getColumnModel().getColumnIndex(NAME_COLUMN_NAME)).setCellEditor(new WebServiceTableCellEditor(true));
        headersTable.getColumnModel().getColumn(headersTable.getColumnModel().getColumnIndex(VALUE_COLUMN_NAME)).setCellEditor(new WebServiceTableCellEditor(false));
        headersTable.setCustomEditorControls(true);

        headersTable.setSelectionMode(0);
        headersTable.setRowSelectionAllowed(true);
        headersTable.setRowHeight(UIConstants.ROW_HEIGHT);
        headersTable.setDragEnabled(false);
        headersTable.setOpaque(true);
        headersTable.setSortable(false);
        headersTable.setEditable(true);
        headersTable.getTableHeader().setReorderingAllowed(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            headersTable.setHighlighters(highlighter);
        }

        headersScrollPane = new JScrollPane(headersTable);
        headersScrollPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                deselectRows(headersTable, headersDeleteButton);
            }
        });

        headersNewButton = new JButton("New");
        headersNewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                headersNewButtonActionPerformed(evt);
            }
        });

        headersDeleteButton = new JButton("Delete");
        headersDeleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                headersDeleteButtonActionPerformed(evt);
            }
        });

        useMtomLabel = new JLabel("Use MTOM:");
        ButtonGroup useMtomButtonGroup = new ButtonGroup();

        useMtomYesRadio = new MirthRadioButton("Yes");
        useMtomYesRadio.setBackground(getBackground());
        useMtomYesRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                useMtomYesRadioActionPerformed(evt);
            }
        });
        useMtomButtonGroup.add(useMtomYesRadio);

        useMtomNoRadio = new MirthRadioButton("No");
        useMtomNoRadio.setBackground(getBackground());
        useMtomNoRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                useMtomNoRadioActionPerformed(evt);
            }
        });
        useMtomButtonGroup.add(useMtomNoRadio);

        attachmentsLabel = new JLabel("Attachments:");

        attachmentsTable = new MirthTable();
        attachmentsTable.setModel(new RefreshTableModel(new String[] { "ID", "Content",
                "MIME Type" }, 0));

        attachmentsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                if (attachmentsTable.getSelectedModelIndex() != -1) {
                    attachmentsDeleteButton.setEnabled(true);
                } else {
                    attachmentsDeleteButton.setEnabled(false);
                }
            }
        });

        class AttachmentsTableCellEditor extends TextFieldCellEditor {

            boolean checkUnique;

            public AttachmentsTableCellEditor(boolean checkUnique) {
                super();
                this.checkUnique = checkUnique;
            }

            public boolean checkUnique(String value) {
                boolean exists = false;

                for (int i = 0; i < attachmentsTable.getModel().getRowCount(); i++) {
                    if (((String) attachmentsTable.getModel().getValueAt(i, ID_COLUMN_NUMBER)).equalsIgnoreCase(value)) {
                        exists = true;
                    }
                }

                return exists;
            }

            @Override
            public boolean isCellEditable(EventObject evt) {
                boolean editable = super.isCellEditable(evt);

                if (editable) {
                    attachmentsDeleteButton.setEnabled(false);
                }

                return editable;
            }

            @Override
            protected boolean valueChanged(String value) {
                attachmentsDeleteButton.setEnabled(true);

                if (checkUnique && (value.length() == 0 || checkUnique(value))) {
                    return false;
                }

                parent.setSaveEnabled(true);
                return true;
            }
        }

        attachmentsTable.getColumnModel().getColumn(attachmentsTable.getColumnModelIndex(ID_COLUMN_NAME)).setCellEditor(new AttachmentsTableCellEditor(true));
        attachmentsTable.getColumnModel().getColumn(attachmentsTable.getColumnModelIndex(CONTENT_COLUMN_NAME)).setCellEditor(new AttachmentsTableCellEditor(false));
        attachmentsTable.getColumnModel().getColumn(attachmentsTable.getColumnModelIndex(MIME_TYPE_COLUMN_NAME)).setCellEditor(new AttachmentsTableCellEditor(false));
        attachmentsTable.setCustomEditorControls(true);

        attachmentsTable.setSelectionMode(0);
        attachmentsTable.setRowSelectionAllowed(true);
        attachmentsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        attachmentsTable.setDragEnabled(true);
        attachmentsTable.setOpaque(true);
        attachmentsTable.setSortable(true);
        attachmentsTable.setEditable(true);

        attachmentsTable.setTransferHandler(new TransferHandler() {

            protected Transferable createTransferable(JComponent c) {
                try {
                    MirthTable table = ((MirthTable) (c));

                    if (table == null) {
                        return null;
                    }

                    int currRow = table.convertRowIndexToModel(table.getSelectedRow());

                    String text = "";
                    if (currRow >= 0 && currRow < table.getModel().getRowCount()) {
                        text = (String) table.getModel().getValueAt(currRow, ID_COLUMN_NUMBER);
                    }

                    text = "<inc:Include href=\"cid:" + text + "\" xmlns:inc=\"http://www.w3.org/2004/08/xop/include\"/>";

                    return new StringSelection(text);
                } catch (ClassCastException cce) {
                    return null;
                }
            }

            public int getSourceActions(JComponent c) {
                return COPY;
            }

            public boolean canImport(JComponent c, DataFlavor[] df) {
                return false;
            }
        });

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            attachmentsTable.setHighlighters(highlighter);
        }

        attachmentsScrollPane = new JScrollPane(attachmentsTable);

        attachmentsNewButton = new JButton("New");
        attachmentsNewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                newButtonActionPerformed(evt);
            }
        });

        attachmentsDeleteButton = new JButton("Delete");
        attachmentsDeleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        sslWarningPanel = new SSLWarningPanel();
    }

    protected void initToolTips() {
        wsdlUrlField.setToolTipText("Enter the full URL to the WSDL describing the web service method to be called, and then click the Get Operations button.");
        getOperationsButton.setToolTipText("<html>Clicking this button fetches the WSDL from the specified URL<br> and parses it to obtain a description of the data types and methods used by the web service to be called.<br>It replaces the values of all of the controls below by values taken from the WSDL.</html>");
        serviceComboBox.setToolTipText("<html>The service name for the WSDL defined above. This field<br/>is filled in automatically when the Get Operations button<br/>is clicked and does not usually need to be changed,<br/>unless multiple services are defined in the WSDL.</html>");
        portComboBox.setToolTipText("<html>The port / endpoint name for the service defined above.<br/>This field is filled in automatically when the Get Operations<br/>button is clicked and does not usually need to be changed,<br/>unless multiple endpoints are defined for the currently<br/>selected service in the WSDL.</html>");
        locationURIComboBox.setToolTipText("<html>The dispatch location for the port / endpoint defined above.<br/>This field is filled in automatically when the Get Operations<br/>button is clicked and does not usually need to be changed.<br/>If left blank, the default URI defined in the WSDL will be used.</html>");
        socketTimeoutField.setToolTipText("<html>Sets the connection and socket timeout (SO_TIMEOUT) in<br/>milliseconds to be used when invoking the web service.<br/>A timeout value of zero is interpreted as an infinite timeout.</html>");
        authenticationYesRadio.setToolTipText("<html>Turning on authentication uses a username and password to get the WSDL, if necessary,<br>and uses the username and password binding provider properties when calling the web service.</html>");
        authenticationNoRadio.setToolTipText("<html>Turning on authentication uses a username and password to get the WSDL, if necessary,<br>and uses the username and password binding provider properties when calling the web service.</html>");
        usernameField.setToolTipText("The username used to get the WSDL and call the web service.");
        passwordField.setToolTipText("The password used to get the WSDL and call the web service.");
        invocationOneWayRadio.setToolTipText("<html>Invoke the operation using the one-way invocation function.<br>This will not wait for any response, and should only be used if the<br>operation is defined as a one-way operation.</html>");
        invocationTwoWayRadio.setToolTipText("<html>Invoke the operation using the standard two-way invocation function.<br>This will wait for some response or acknowledgement to be returned.</html>");
        operationComboBox.setToolTipText("<html>Select the web service operation to be called from this list.<br>This is only used for generating the envelope</html>");
        generateEnvelopeButton.setToolTipText("<html>Clicking this button regenerates the contents of the SOAP Envelope control based on the<br>schema defined in the WSDL, discarding any changes that may have been made.<br>It also populates the SOAP Action field, if available.</html>");
        soapActionField.setToolTipText("<html>The SOAPAction HTTP request header field can be used to indicate the intent of the SOAP HTTP request.<br>This field is optional for most web services, and will be auto-populated when you select an operation.</html>");
        headersTable.setToolTipText("Header parameters are encoded as HTTP headers in the HTTP request sent to the server.");
        useMtomYesRadio.setToolTipText("<html>Enables MTOM on the SOAP Binding. If MTOM is enabled,<br>attachments can be added to the table below and dropped into the envelope.</html>");
        useMtomNoRadio.setToolTipText("<html>Does not enable MTOM on the SOAP Binding. If MTOM is enabled,<br>attachments can be added to the table below and dropped into the envelope.</html>");
        attachmentsTable.setToolTipText("<html>Attachments should be added with an ID, Base64 encoded content,<br>and a valid MIME type. Once an attachment is added<br>the row can be dropped into an argument in the envelope.</html>");
    }

    protected void initLayout() {
        setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill, gapy 6", "[]12[grow][]"));

        add(wsdlUrlLabel, "right");
        add(wsdlUrlField, "growx, sx, split 3");
        add(getOperationsButton);
        add(wsdlUrlTestConnectionButton);
        add(serviceLabel, "newline, right");
        add(serviceComboBox, "growx, sx");
        add(portLabel, "newline, right");
        add(portComboBox, "growx, sx");
        add(locationURILabel, "newline, right");
        add(locationURIComboBox, "growx, sx, split 2");
        add(locationURITestConnectionButton);
        add(socketTimeoutLabel, "newline, right");
        add(socketTimeoutField, "w 75!");
        add(authenticationLabel, "newline, right");
        add(authenticationYesRadio, "split 2");
        add(authenticationNoRadio);
        add(usernameLabel, "newline, right");
        add(usernameField, "w 125!");
        add(passwordLabel, "newline, right");
        add(passwordField, "w 125!");
        add(invocationTypeLabel, "newline, right");
        add(invocationOneWayRadio, "split 2");
        add(invocationTwoWayRadio);
        add(operationLabel, "newline, right");
        add(operationComboBox, "split 2");
        add(generateEnvelopeButton);
        add(soapActionLabel, "newline, right");
        add(soapActionField, "growx, sx");
        add(soapEnvelopeLabel, "newline, top, right");
        add(soapEnvelopeTextArea, "grow, push, sx");
        add(headersLabel, "newline, top, right");
        add(headersScrollPane, "top, growx, pushx, h 80!");
        add(headersNewButton, "top, flowy, split 2, w 50!");
        add(headersDeleteButton, "w 50!");
        add(useMtomLabel, "newline, right");
        add(useMtomYesRadio, "split 2");
        add(useMtomNoRadio);
        add(attachmentsLabel, "newline, top, right");
        add(attachmentsScrollPane, "top, growx, pushx, h 80!");
        add(attachmentsNewButton, "top, flowy, split 2, w 50!");
        add(attachmentsDeleteButton, "w 50!");
    }

    private void newButtonActionPerformed(ActionEvent evt) {
        stopCellEditing();
        ((DefaultTableModel) attachmentsTable.getModel()).addRow(new Object[] {
                getNewAttachmentId(attachmentsTable.getModel().getRowCount() + 1), "" });
        int newViewIndex = attachmentsTable.convertRowIndexToView(attachmentsTable.getModel().getRowCount() - 1);
        attachmentsTable.setRowSelectionInterval(newViewIndex, newViewIndex);

        attachmentsScrollPane.getViewport().setViewPosition(new Point(0, attachmentsTable.getRowHeight() * attachmentsTable.getModel().getRowCount()));
        parent.setSaveEnabled(true);
    }

    private void deleteButtonActionPerformed(ActionEvent evt) {
        stopCellEditing();

        int selectedModelIndex = attachmentsTable.getSelectedModelIndex();
        int newViewIndex = attachmentsTable.convertRowIndexToView(selectedModelIndex);
        if (newViewIndex == (attachmentsTable.getModel().getRowCount() - 1)) {
            newViewIndex--;
        }

        ((DefaultTableModel) attachmentsTable.getModel()).removeRow(selectedModelIndex);

        parent.setSaveEnabled(true);

        if (attachmentsTable.getModel().getRowCount() == 0) {
            attachmentsTable.clearSelection();
            attachmentsDeleteButton.setEnabled(false);
        } else {
            attachmentsTable.setRowSelectionInterval(newViewIndex, newViewIndex);
        }

        parent.setSaveEnabled(true);
    }

    private void getOperationsButtonActionPerformed(ActionEvent evt) {
        if (StringUtils.isNotBlank((String) serviceComboBox.getEditor().getItem()) || StringUtils.isNotBlank((String) portComboBox.getEditor().getItem()) || StringUtils.isNotBlank((String) locationURIComboBox.getEditor().getItem()) || !isDefaultOperations()) {
            if (!parent.alertOkCancel(parent, "This will replace your current service, port, location URI, and operation list. Press OK to continue.")) {
                return;
            }
        }

        // Reset all of the fields
        currentServiceMap = null;
        serviceComboBox.setModel(new DefaultComboBoxModel());
        portComboBox.setModel(new DefaultComboBoxModel());

        if (canSetLocationURI()) {
            locationURIComboBox.setModel(new DefaultComboBoxModel());
        }

        operationComboBox.setModel(new DefaultComboBoxModel(new String[] {
                WebServiceDispatcherProperties.WEBSERVICE_DEFAULT_DROPDOWN }));
        operationComboBox.setSelectedIndex(0);
        generateEnvelopeButton.setEnabled(false);

        ResponseHandler handler = new ResponseHandler() {
            @Override
            public void handle(Object response) {
                ResponseHandler handler = new ResponseHandler() {
                    @Override
                    public void handle(Object response) {
                        if (response != null) {
                            currentServiceMap = (DefinitionServiceMap) response;
                            loadServiceMap();

                            if (currentServiceMap != null) {
                                serviceComboBox.setSelectedItem(currentServiceMap.getMap().keySet().iterator().next());
                            }

                            parent.setSaveEnabled(true);
                        }
                    }
                };

                try {
                    WebServiceDispatcherProperties props = (WebServiceDispatcherProperties) getFilledProperties();
                    getServlet(WebServiceConnectorServletInterface.class, "Retrieving cached WSDL definition map...", "There was an error retrieving the cached WSDL definition map.\n\n", handler).getDefinition(getChannelId(), getChannelName(), props.getWsdlUrl(), props.getUsername(), props.getPassword());
                } catch (ClientException e) {
                    // Should not happen
                }
            }
        };

        try {
            getServlet(WebServiceConnectorServletInterface.class, "Getting operations...", "Error caching WSDL. Please check the WSDL URL and authentication settings.\n\n", handler).cacheWsdlFromUrl(getChannelId(), getChannelName(), (WebServiceDispatcherProperties) getFilledProperties());
        } catch (ClientException e) {
            // Should not happen
        }
    }

    protected boolean canTestConnection(boolean wsdlUrl) {
        if (wsdlUrl) {
            if (StringUtils.isBlank(wsdlUrlField.getText())) {
                parent.alertError(parent, "WSDL URL is blank.");
                return false;
            }
        } else if (StringUtils.isBlank(String.valueOf(locationURIComboBox.getSelectedItem()))) {
            parent.alertError(parent, "Location URI is blank.");
            return false;
        }

        return true;
    }

    protected WebServiceDispatcherProperties getTestConnectionPropeties() {
        return (WebServiceDispatcherProperties) getFilledProperties();
    }

    private void testConnectionButtonActionPerformed(boolean wsdlUrl) {
        if (!canTestConnection(wsdlUrl)) {
            return;
        }

        WebServiceDispatcherProperties properties = getTestConnectionPropeties();

        // Blank out the other property so that it isn't tested
        if (wsdlUrl) {
            properties.setLocationURI("");
        } else {
            properties.setWsdlUrl("");
        }

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
            getServlet(WebServiceConnectorServletInterface.class, "Testing connection...", "Error testing Web Service connection: ", handler).testConnection(getChannelId(), getChannelName(), properties);
        } catch (ClientException e) {
            // Should not happen
        }
    }

    private void authenticationYesRadioActionPerformed(ActionEvent evt) {
        usernameLabel.setEnabled(true);
        usernameField.setEnabled(true);

        passwordLabel.setEnabled(true);
        passwordField.setEnabled(true);
    }

    private void authenticationNoRadioActionPerformed(ActionEvent evt) {
        usernameLabel.setEnabled(false);
        usernameField.setEnabled(false);
        usernameField.setText("");

        passwordLabel.setEnabled(false);
        passwordField.setEnabled(false);
        passwordField.setText("");
    }

    private void useMtomYesRadioActionPerformed(ActionEvent evt) {
        attachmentsLabel.setEnabled(true);
        attachmentsScrollPane.setEnabled(true);
        attachmentsTable.setEnabled(true);
        attachmentsNewButton.setEnabled(true);

        attachmentsTable.setRowSelectionAllowed(true);
        if (attachmentsTable.getModel().getRowCount() > 0) {
            attachmentsTable.setRowSelectionInterval(0, 0);
            attachmentsDeleteButton.setEnabled(true);
        }

    }

    private void useMtomNoRadioActionPerformed(ActionEvent evt) {
        attachmentsLabel.setEnabled(false);
        attachmentsScrollPane.setEnabled(false);
        attachmentsTable.setEnabled(false);
        attachmentsNewButton.setEnabled(false);
        attachmentsDeleteButton.setEnabled(false);

        stopCellEditing();
        attachmentsTable.setRowSelectionAllowed(false);
        attachmentsTable.clearSelection();
    }

    private void generateEnvelopeActionPerformed(ActionEvent evt) {
        generateEnvelope();
    }

    protected void generateEnvelope() {
        generateEnvelope(((WebServiceDispatcherProperties) getFilledProperties()).getWsdlUrl(), getChannelId(), getChannelName(), true);
    }

    protected void generateEnvelope(String wsdlUrl, String channelId, String channelName, boolean buildOptional) {
        if (soapEnvelopeTextArea.getText().length() > 0 || soapActionField.getText().length() > 0) {
            if (!parent.alertOkCancel(parent, "This will replace your current SOAP envelope and SOAP action. Press OK to continue.")) {
                return;
            }
        }

        final WebServiceDispatcherProperties props = (WebServiceDispatcherProperties) getFilledProperties();
        props.setWsdlUrl(wsdlUrl);

        ResponseHandler isWsdlCachedHandler = new ResponseHandler() {
            @Override
            public void handle(Object response) {
                if (response != null) {
                    if ((Boolean) response) {
                        ResponseHandler generateEnvelopeHandler = new ResponseHandler() {
                            @Override
                            public void handle(Object response) {
                                setSoapEnvelopeText((String) response);

                                ResponseHandler getSoapActionHandler = new ResponseHandler() {
                                    @Override
                                    public void handle(Object response) {
                                        String soapAction = (String) response;
                                        if (soapAction != null) {
                                            soapActionField.setText(soapAction);
                                            parent.setSaveEnabled(true);
                                            urlFieldChanged();
                                        }
                                    }
                                };

                                try {
                                    getServlet(WebServiceConnectorServletInterface.class, "Retrieving SOAP action...", "There was an error retrieving the SOAP action.\n\n", getSoapActionHandler).getSoapAction(channelId, channelName, props.getWsdlUrl(), props.getUsername(), props.getPassword(), props.getService(), props.getPort(), props.getOperation());
                                } catch (ClientException e) {
                                    // Should not happen
                                }
                            }
                        };

                        try {
                            getServlet(WebServiceConnectorServletInterface.class, "Generating envelope...", "There was an error generating the envelope.\n\n", generateEnvelopeHandler).generateEnvelope(channelId, channelName, props.getWsdlUrl(), props.getUsername(), props.getPassword(), props.getService(), props.getPort(), props.getOperation(), buildOptional);
                        } catch (ClientException e) {
                            // Should not happen
                        }
                    } else {
                        parent.alertInformation(parent, "The WSDL is no longer cached on the server. Press \"Get Operations\" to fetch the latest WSDL.");
                    }
                }
            }
        };

        try {
            getServlet(WebServiceConnectorServletInterface.class, "Checking if WSDL is cached...", "Error checking if the wsdl is cached: ", isWsdlCachedHandler).isWsdlCached(channelId, channelName, props.getWsdlUrl(), props.getUsername(), props.getPassword());
        } catch (ClientException e) {
            // Should not happen
        }
    }

    protected void setSoapEnvelopeText(String text) {
        if (text != null) {
            soapEnvelopeTextArea.setText(text);
            parent.setSaveEnabled(true);
        }
    }

    private void serviceComboBoxActionPerformed(ActionEvent evt) {
        String selectedPort = (String) portComboBox.getEditor().getItem();

        if (currentServiceMap != null) {
            DefinitionPortMap portMap = currentServiceMap.getMap().get((String) serviceComboBox.getEditor().getItem());

            if (portMap != null) {
                portComboBox.setModel(new DefaultComboBoxModel(portMap.getMap().keySet().toArray()));
            } else {
                portComboBox.setModel(new DefaultComboBoxModel());
            }
        }

        if (StringUtils.isNotBlank(selectedPort)) {
            portComboBox.setSelectedItem(selectedPort);
        } else if (portComboBox.getModel().getSize() > 0) {
            portComboBox.setSelectedIndex(0);
        }
    }

    private void portComboBoxActionPerformed(ActionEvent evt) {
        if (currentServiceMap != null) {
            DefinitionPortMap portMap = currentServiceMap.getMap().get((String) serviceComboBox.getEditor().getItem());

            if (portMap != null) {
                PortInformation portInformation = portMap.getMap().get((String) portComboBox.getEditor().getItem());
                String selectedLocationURI = (String) locationURIComboBox.getEditor().getItem();

                if (portInformation != null) {
                    List<String> operationList = portInformation.getOperations();

                    if (CollectionUtils.isNotEmpty(operationList)) {
                        String selectedOperation = (String) operationComboBox.getSelectedItem();
                        operationComboBox.setModel(new DefaultComboBoxModel(operationList.toArray()));

                        if (operationList.contains(selectedOperation)) {
                            operationComboBox.setSelectedItem(selectedOperation);
                        } else {
                            operationComboBox.setSelectedIndex(0);
                        }

                        updateGenerateEnvelopeButtonEnabled();
                    } else {
                        operationComboBox.setModel(new DefaultComboBoxModel());
                    }

                    if (canSetLocationURI()) {
                        if (StringUtils.isNotBlank(portInformation.getLocationURI())) {
                            locationURIComboBox.setModel(new DefaultComboBoxModel(new String[] {
                                    portInformation.getLocationURI() }));
                        } else {
                            locationURIComboBox.setModel(new DefaultComboBoxModel());
                        }
                    }
                } else {
                    if (canSetLocationURI()) {
                        locationURIComboBox.setModel(new DefaultComboBoxModel());
                    }
                    operationComboBox.setModel(new DefaultComboBoxModel());
                }

                if (canSetLocationURI() && StringUtils.isNotBlank(selectedLocationURI)) {
                    locationURIComboBox.setSelectedItem(selectedLocationURI);
                }
            }
        }
    }

    protected void updateGenerateEnvelopeButtonEnabled() {
        generateEnvelopeButton.setEnabled(!isDefaultOperations());
    }

    private void headersNewButtonActionPerformed(ActionEvent evt) {
        ((DefaultTableModel) headersTable.getModel()).addRow(new Object[] {
                getNewPropertyName(headersTable), "" });
        headersTable.setRowSelectionInterval(headersTable.getRowCount() - 1, headersTable.getRowCount() - 1);
        parent.setSaveEnabled(true);
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

    protected JLabel wsdlUrlLabel;
    protected MirthIconTextField wsdlUrlField;
    protected JButton getOperationsButton;
    protected JButton wsdlUrlTestConnectionButton;
    protected JLabel serviceLabel;
    protected MirthEditableComboBox serviceComboBox;
    protected JLabel portLabel;
    protected MirthEditableComboBox portComboBox;
    protected JLabel locationURILabel;
    protected MirthEditableComboBox locationURIComboBox;
    protected JButton locationURITestConnectionButton;
    protected JLabel socketTimeoutLabel;
    protected MirthTextField socketTimeoutField;
    protected JLabel authenticationLabel;
    protected MirthRadioButton authenticationYesRadio;
    protected MirthRadioButton authenticationNoRadio;
    protected JLabel usernameLabel;
    protected MirthTextField usernameField;
    protected JLabel passwordLabel;
    protected MirthPasswordField passwordField;
    protected JLabel invocationTypeLabel;
    protected MirthRadioButton invocationOneWayRadio;
    protected MirthRadioButton invocationTwoWayRadio;
    protected JLabel operationLabel;
    protected MirthComboBox operationComboBox;
    protected JButton generateEnvelopeButton;
    protected JLabel soapActionLabel;
    protected MirthIconTextField soapActionField;
    protected JLabel soapEnvelopeLabel;
    protected MirthSyntaxTextArea soapEnvelopeTextArea;
    protected JLabel headersLabel;
    protected MirthTable headersTable;
    protected JScrollPane headersScrollPane;
    protected JButton headersNewButton;
    protected JButton headersDeleteButton;
    protected JLabel useMtomLabel;
    protected MirthRadioButton useMtomYesRadio;
    protected MirthRadioButton useMtomNoRadio;
    protected JLabel attachmentsLabel;
    protected MirthTable attachmentsTable;
    protected JScrollPane attachmentsScrollPane;
    protected JButton attachmentsNewButton;
    protected JButton attachmentsDeleteButton;
    protected SSLWarningPanel sslWarningPanel;
}
