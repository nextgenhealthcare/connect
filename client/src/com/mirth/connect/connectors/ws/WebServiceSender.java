/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.ws;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.XMLTokenMarker;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.TextFieldCellEditor;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

public class WebServiceSender extends ConnectorSettingsPanel {

    private final int ID_COLUMN_NUMBER = 0;
    private final int CONTENT_COLUMN_NUMBER = 1;
    private final int MIME_TYPE_COLUMN_NUMBER = 2;
    private final String ID_COLUMN_NAME = "ID";
    private final String CONTENT_COLUMN_NAME = "Content";
    private final String MIME_TYPE_COLUMN_NAME = "MIME Type";
    ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
    private Frame parent;

    public WebServiceSender() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();

        SyntaxDocument document = new SyntaxDocument();
        document.setTokenMarker(new XMLTokenMarker());
        soapEnvelope.setDocument(document);
    }

    @Override
    public String getConnectorName() {
        return new WebServiceDispatcherProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        WebServiceDispatcherProperties properties = new WebServiceDispatcherProperties();

        properties.setWsdlUrl(wsdlUrlField.getText());
        properties.setService(serviceField.getText());
        properties.setPort(portField.getText());
        properties.setSoapAction(soapActionField.getText());

        properties.setOneWay(invocationOneWayRadio.isSelected());

        if (operationComboBox.getSelectedIndex() != -1) {
            properties.setOperation((String) operationComboBox.getSelectedItem());
        }

        properties.setUseAuthentication(authenticationYesRadio.isSelected());

        properties.setUsername(usernameField.getText());
        properties.setPassword(new String(passwordField.getPassword()));

        properties.setEnvelope(soapEnvelope.getText());

        ArrayList<String> operations = new ArrayList<String>();
        for (int i = 0; i < operationComboBox.getModel().getSize(); i++) {
            operations.add((String) operationComboBox.getModel().getElementAt(i));
        }

        properties.setWsdlOperations(operations);

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
        serviceField.setText(props.getService());
        portField.setText(props.getPort());
        soapActionField.setText(props.getSoapAction());

        soapEnvelope.setText(props.getEnvelope());

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

        List<String> operations = props.getWsdlOperations();

        operationComboBox.setModel(new javax.swing.DefaultComboBoxModel(operations.toArray()));
        generateEnvelope.setEnabled(!isDefaultOperations());

        operationComboBox.setSelectedItem(props.getOperation());

        parent.setSaveEnabled(enabled);

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
                serviceField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (props.getPort().length() == 0) {
            valid = false;
            if (highlight) {
                portField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (props.getEnvelope().length() == 0) {
            valid = false;
            if (highlight) {
                soapEnvelope.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return valid;
    }

    @Override
    public void resetInvalidProperties() {
        wsdlUrlField.setBackground(null);
        serviceField.setBackground(new java.awt.Color(222, 222, 222));
        portField.setBackground(new java.awt.Color(222, 222, 222));
        soapEnvelope.setBackground(null);
    }

    private boolean isDefaultOperations() {
        return (operationComboBox.getItemCount() == 1 && operationComboBox.getItemAt(0).equals(WebServiceDispatcherProperties.WEBSERVICE_DEFAULT_DROPDOWN));
    }

    private boolean isWsdlCached() {
        String wsdlUrl = wsdlUrlField.getText().trim();

        if (wsdlUrl.equals("")) {
            return false;
        }

        boolean isWsdlCached = false;

        try {
            isWsdlCached = (Boolean) parent.mirthClient.invokeConnectorService(parent.channelEditPanel.currentChannel.getId(), getConnectorName(), "isWsdlCached", wsdlUrl);
        } catch (ClientException e) {
            parent.alertError(parent, "Error checking if the wsdl is cached.");
        }

        return isWsdlCached;
    }

    private boolean cacheWsdl() {
        try {
            String wsdlUrl = wsdlUrlField.getText().trim();
            Map<String, String> cacheWsdlMap = new HashMap<String, String>();

            cacheWsdlMap.put("wsdlUrl", wsdlUrl);

            if (authenticationYesRadio.isSelected()) {
                cacheWsdlMap.put("username", usernameField.getText());
                cacheWsdlMap.put("password", new String(passwordField.getPassword()));
            }

            parent.mirthClient.invokeConnectorService(parent.channelEditPanel.currentChannel.getId(), getConnectorName(), "cacheWsdlFromUrl", cacheWsdlMap);

            return true;
        } catch (ClientException e) {
            parent.alertException(parent, e.getStackTrace(), "Error caching WSDL. Please check the WSDL URL and authentication settings.\n\n" + e.getMessage());
            return false;
        }
    }

    private Object invokeConnectorService(String method) {
        return invokeConnectorService(method, null, null);
    }

    private Object invokeConnectorService(String method, String paramName, String paramValue) {
        String wsdlUrl = wsdlUrlField.getText().trim();
        Object returnObject = null;
        Object params = null;

        if (paramName == null) {
            params = wsdlUrl;
        } else {
            Map<String, String> paramMap = new HashMap<String, String>();
            paramMap.put("id", wsdlUrl);
            paramMap.put(paramName, paramValue);

            params = paramMap;
        }

        try {
            returnObject = parent.mirthClient.invokeConnectorService(parent.channelEditPanel.currentChannel.getId(), getConnectorName(), method, params);
        } catch (ClientException e) {
            if (method.equals("generateEnvelope")) {
                parent.alertError(parent, "There was an error generating the envelope.");
            } else {
                parent.alertError(parent, "Error calling " + method + " with parameter: " + params.toString());
            }
        }

        return returnObject;
    }

    private String buildHost() {
        if (wsdlUrlField.getText().startsWith("http://") || wsdlUrlField.getText().startsWith("file://")) {
            return wsdlUrlField.getText().substring(7);
        } else if (wsdlUrlField.getText().startsWith("https://")) {
            return wsdlUrlField.getText().substring(8);
        } else {
            return wsdlUrlField.getText();
        }
    }

    private void setAttachments(List<List<String>> attachments) {

        List<String> attachmentIds = attachments.get(0);
        List<String> attachmentContents = attachments.get(1);
        List<String> attachmentTypes = attachments.get(2);

        Object[][] tableData = new Object[attachmentIds.size()][3];

        attachmentsTable = new MirthTable();

        for (int i = 0; i < attachmentIds.size(); i++) {
            tableData[i][ID_COLUMN_NUMBER] = attachmentIds.get(i);
            tableData[i][CONTENT_COLUMN_NUMBER] = attachmentContents.get(i);
            tableData[i][MIME_TYPE_COLUMN_NUMBER] = attachmentTypes.get(i);
        }

        attachmentsTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[] { ID_COLUMN_NAME, CONTENT_COLUMN_NAME, MIME_TYPE_COLUMN_NAME }) {

            boolean[] canEdit = new boolean[] { true, true, true };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        attachmentsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                if (attachmentsTable.getSelectedModelIndex() != -1) {
                    deleteButton.setEnabled(true);
                } else {
                    deleteButton.setEnabled(false);
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
                    deleteButton.setEnabled(false);
                }

                return editable;
            }

            @Override
            protected boolean valueChanged(String value) {
                deleteButton.setEnabled(true);

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

        attachmentsTable.setOpaque(true);
        attachmentsTable.setSortable(true);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            attachmentsTable.setHighlighters(highlighter);
        }

        attachmentsPane.setViewportView(attachmentsTable);
        deleteButton.setEnabled(false);
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        userPersistentQueuesButtonGroup = new javax.swing.ButtonGroup();
        authenticationButtonGroup = new javax.swing.ButtonGroup();
        invocationButtonGroup = new javax.swing.ButtonGroup();
        useMtomButtonGroup = new javax.swing.ButtonGroup();
        wsdlUrlLabel = new javax.swing.JLabel();
        wsdlUrlField = new com.mirth.connect.client.ui.components.MirthTextField();
        getOperationsButton = new javax.swing.JButton();
        operationComboBox = new com.mirth.connect.client.ui.components.MirthComboBox();
        jLabel1 = new javax.swing.JLabel();
        serviceLabel = new javax.swing.JLabel();
        portField = new com.mirth.connect.client.ui.components.MirthTextField();
        soapEnvelope = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea(true,false);
        portLabel = new javax.swing.JLabel();
        serviceField = new com.mirth.connect.client.ui.components.MirthTextField();
        jLabel4 = new javax.swing.JLabel();
        generateEnvelope = new javax.swing.JButton();
        attachmentsLabel = new javax.swing.JLabel();
        attachmentsPane = new javax.swing.JScrollPane();
        attachmentsTable = new com.mirth.connect.client.ui.components.MirthTable();
        newButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        authenticationLabel = new javax.swing.JLabel();
        authenticationYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        authenticationNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        usernameLabel = new javax.swing.JLabel();
        usernameField = new com.mirth.connect.client.ui.components.MirthTextField();
        passwordField = new com.mirth.connect.client.ui.components.MirthPasswordField();
        passwordLabel = new javax.swing.JLabel();
        invocationTypeLabel = new javax.swing.JLabel();
        invocationTwoWayRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        invocationOneWayRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        useMtomLabel = new javax.swing.JLabel();
        useMtomYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        useMtomNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        soapActionField = new com.mirth.connect.client.ui.components.MirthTextField();
        soapActionLabel = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        wsdlUrlLabel.setText("WSDL URL:");

        wsdlUrlField.setToolTipText("Enter the full URL to the WSDL describing the web service method to be called, and then click the Get Operations button.");

        getOperationsButton.setText("Get Operations");
        getOperationsButton.setToolTipText("<html>Clicking this button fetches the WSDL from the specified URL<br> and parses it to obtain a description of the data types and methods used by the web service to be called.<br>It replaces the values of all of the controls below by values taken from the WSDL.</html>");
        getOperationsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getOperationsButtonActionPerformed(evt);
            }
        });

        operationComboBox.setToolTipText("<html>Select the web service operation to be called from this list.<br>This is only used for generating the envelope</html>");

        jLabel1.setText("Operation:");

        serviceLabel.setText("Service:");

        portField.setBackground(new java.awt.Color(222, 222, 222));
        portField.setToolTipText("<html>The port name for the WSDL defined above.<br>This field is filled in automatically when the Get Operations button is clicked and does not need to be changed.</html>");

        soapEnvelope.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        portLabel.setText("Port:");

        serviceField.setBackground(new java.awt.Color(222, 222, 222));
        serviceField.setToolTipText("<html>The service name for the WSDL defined above.<br>This field is filled in automatically when the Get Operations button is clicked and does not need to be changed.</html>");

        jLabel4.setText("SOAP Envelope:");

        generateEnvelope.setText("Generate Envelope");
        generateEnvelope.setToolTipText("<html>Clicking this button regenerates the contents of the SOAP Envelope control based on the<br>schema defined in the WSDL, discarding any changes that may have been made.<br>It also populates the SOAP Action field, if available.</html>");
        generateEnvelope.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateEnvelopeActionPerformed(evt);
            }
        });

        attachmentsLabel.setText("Attachments:");

        attachmentsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Content", "MIME Type"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        attachmentsTable.setToolTipText("<html>Attachments should be added with an ID, Base64 encoded content,<br>and a valid MIME type. Once an attachment is added<br>the row can be dropped into an argument in the envelope.</html>");
        attachmentsPane.setViewportView(attachmentsTable);

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

        authenticationLabel.setText("Authentication:");

        authenticationYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        authenticationYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        authenticationButtonGroup.add(authenticationYesRadio);
        authenticationYesRadio.setText("Yes");
        authenticationYesRadio.setToolTipText("<html>Turning on authentication uses a username and password to get the WSDL, if necessary,<br>and uses the username and password binding provider properties when calling the web service.</html>");
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
        authenticationNoRadio.setToolTipText("<html>Turning on authentication uses a username and password to get the WSDL, if necessary,<br>and uses the username and password binding provider properties when calling the web service.</html>");
        authenticationNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        authenticationNoRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                authenticationNoRadioActionPerformed(evt);
            }
        });

        usernameLabel.setText("Username:");

        usernameField.setToolTipText("The username used to get the WSDL and call the web service.");

        passwordField.setToolTipText("The password used to get the WSDL and call the web service.");

        passwordLabel.setText("Password:");

        invocationTypeLabel.setText("Invocation Type:");

        invocationTwoWayRadio.setBackground(new java.awt.Color(255, 255, 255));
        invocationTwoWayRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        invocationButtonGroup.add(invocationTwoWayRadio);
        invocationTwoWayRadio.setText("Two-Way");
        invocationTwoWayRadio.setToolTipText("<html>Invoke the operation using the standard two-way invocation function.<br>This will wait for some response or acknowledgement to be returned.</html>");
        invocationTwoWayRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        invocationOneWayRadio.setBackground(new java.awt.Color(255, 255, 255));
        invocationOneWayRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        invocationButtonGroup.add(invocationOneWayRadio);
        invocationOneWayRadio.setText("One-Way");
        invocationOneWayRadio.setToolTipText("<html>Invoke the operation using the one-way invocation function.<br>This will not wait for any response, and should only be used if the<br>operation is defined as a one-way operation.</html>");
        invocationOneWayRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        useMtomLabel.setText("Use MTOM:");

        useMtomYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        useMtomYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        useMtomButtonGroup.add(useMtomYesRadio);
        useMtomYesRadio.setText("Yes");
        useMtomYesRadio.setToolTipText("<html>Enables MTOM on the SOAP Binding. If MTOM is enabled,<br>attachments can be added to the table below and dropped into the envelope.</html>");
        useMtomYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useMtomYesRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useMtomYesRadioActionPerformed(evt);
            }
        });

        useMtomNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        useMtomNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        useMtomButtonGroup.add(useMtomNoRadio);
        useMtomNoRadio.setSelected(true);
        useMtomNoRadio.setText("No");
        useMtomNoRadio.setToolTipText("<html>Does not enable MTOM on the SOAP Binding. If MTOM is enabled,<br>attachments can be added to the table below and dropped into the envelope.</html>");
        useMtomNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useMtomNoRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useMtomNoRadioActionPerformed(evt);
            }
        });

        soapActionField.setBackground(new java.awt.Color(222, 222, 222));
        soapActionField.setToolTipText("<html>The SOAPAction HTTP request header field can be used to indicate the intent of the SOAP HTTP request.<br>This field is optional for most web services, and will be auto-populated when you select an operation.</html>");

        soapActionLabel.setText("SOAP Action:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(authenticationLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(soapActionLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(attachmentsLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(useMtomLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(portLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(usernameLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(serviceLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(passwordLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(invocationTypeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(wsdlUrlLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(wsdlUrlField, javax.swing.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(getOperationsButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(attachmentsPane, javax.swing.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(newButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(deleteButton)))
                    .addComponent(serviceField, javax.swing.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
                    .addComponent(portField, javax.swing.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
                    .addComponent(soapEnvelope, javax.swing.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
                    .addComponent(soapActionField, javax.swing.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(authenticationYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(authenticationNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(operationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(generateEnvelope))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(invocationTwoWayRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(invocationOneWayRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(useMtomYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(useMtomNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(wsdlUrlLabel)
                    .addComponent(getOperationsButton)
                    .addComponent(wsdlUrlField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serviceLabel)
                    .addComponent(serviceField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(portLabel)
                    .addComponent(portField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(authenticationLabel)
                    .addComponent(authenticationYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(authenticationNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                    .addComponent(invocationTypeLabel)
                    .addComponent(invocationTwoWayRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(invocationOneWayRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(operationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(generateEnvelope))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(soapActionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(soapActionLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(soapEnvelope, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(useMtomLabel)
                    .addComponent(useMtomYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(useMtomNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(attachmentsLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(newButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteButton))
                    .addComponent(attachmentsPane, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        stopCellEditing();
        ((DefaultTableModel) attachmentsTable.getModel()).addRow(new Object[] { getNewAttachmentId(attachmentsTable.getModel().getRowCount() + 1), "" });
        int newViewIndex = attachmentsTable.convertRowIndexToView(attachmentsTable.getModel().getRowCount() - 1);
        attachmentsTable.setRowSelectionInterval(newViewIndex, newViewIndex);

        attachmentsPane.getViewport().setViewPosition(new Point(0, attachmentsTable.getRowHeight() * attachmentsTable.getModel().getRowCount()));
        parent.setSaveEnabled(true);
    }//GEN-LAST:event_newButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
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
            deleteButton.setEnabled(false);
        } else {
            attachmentsTable.setRowSelectionInterval(newViewIndex, newViewIndex);
        }

        parent.setSaveEnabled(true);
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void getOperationsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getOperationsButtonActionPerformed
        if (serviceField.getText().length() > 0 || portField.getText().length() > 0 || !isDefaultOperations()) {
            if (!parent.alertOkCancel(parent, "This will replace your current service, port, and operation list. Press OK to continue.")) {
                return;
            }
        }

        // Reset all of the fields
        serviceField.setText("");
        portField.setText("");
        operationComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { WebServiceDispatcherProperties.WEBSERVICE_DEFAULT_DROPDOWN }));
        operationComboBox.setSelectedIndex(0);
        generateEnvelope.setEnabled(false);

        // Get the new operations
        final String workingId = parent.startWorking("Getting operations...");

        SwingWorker worker = new SwingWorker<Void, Void>() {

            private List<String> loadedMethods = null;
            private String serviceName = null;
            private String portName = null;

            public Void doInBackground() {
                if (cacheWsdl()) {
                    loadedMethods = (List<String>) invokeConnectorService("getOperations");
                    serviceName = (String) invokeConnectorService("getService");
                    portName = (String) invokeConnectorService("getPort");
                }

                return null;
            }

            public void done() {
                if (loadedMethods != null) {
                    String[] methodNames = new String[loadedMethods.size()];
                    loadedMethods.toArray(methodNames);

                    operationComboBox.setModel(new javax.swing.DefaultComboBoxModel(methodNames));
                    generateEnvelope.setEnabled(!isDefaultOperations());

                    if (methodNames.length > 0) {
                        operationComboBox.setSelectedIndex(0);
                    }
                }

                if (serviceName != null) {
                    serviceField.setText(serviceName);
                }

                if (portName != null) {
                    portField.setText(portName);
                }

                parent.setSaveEnabled(true);

                parent.stopWorking(workingId);
            }
        };
        worker.execute();
    }//GEN-LAST:event_getOperationsButtonActionPerformed

    private void authenticationYesRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_authenticationYesRadioActionPerformed
        usernameLabel.setEnabled(true);
        usernameField.setEnabled(true);

        passwordLabel.setEnabled(true);
        passwordField.setEnabled(true);
    }//GEN-LAST:event_authenticationYesRadioActionPerformed

    private void authenticationNoRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_authenticationNoRadioActionPerformed
        usernameLabel.setEnabled(false);
        usernameField.setEnabled(false);
        usernameField.setText("");

        passwordLabel.setEnabled(false);
        passwordField.setEnabled(false);
        passwordField.setText("");
    }//GEN-LAST:event_authenticationNoRadioActionPerformed

    private void useMtomYesRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useMtomYesRadioActionPerformed
        attachmentsLabel.setEnabled(true);
        attachmentsPane.setEnabled(true);
        attachmentsTable.setEnabled(true);
        newButton.setEnabled(true);

        attachmentsTable.setRowSelectionAllowed(true);
        if (attachmentsTable.getModel().getRowCount() > 0) {
            attachmentsTable.setRowSelectionInterval(0, 0);
            deleteButton.setEnabled(true);
        }

    }//GEN-LAST:event_useMtomYesRadioActionPerformed

    private void useMtomNoRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useMtomNoRadioActionPerformed
        attachmentsLabel.setEnabled(false);
        attachmentsPane.setEnabled(false);
        attachmentsTable.setEnabled(false);
        newButton.setEnabled(false);
        deleteButton.setEnabled(false);

        stopCellEditing();
        attachmentsTable.setRowSelectionAllowed(false);
        attachmentsTable.clearSelection();
    }//GEN-LAST:event_useMtomNoRadioActionPerformed

    private void generateEnvelopeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateEnvelopeActionPerformed
        if (soapEnvelope.getText().length() > 0 || soapActionField.getText().length() > 0) {
            if (!parent.alertOkCancel(parent, "This will replace your current SOAP envelope and SOAP action. Press OK to continue.")) {
                return;
            }
        }

        final String workingId = parent.startWorking("Generating envelope...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            private String soapAction = null;
            private String generatedEnvelope = null;

            public Void doInBackground() {
                if (!isWsdlCached()) {
                    parent.alertInformation(parent, "The WSDL is no longer cached on the server. Press \"Get Operations\" to fetch the latest WSDL.");
                } else {
                    generatedEnvelope = (String) invokeConnectorService("generateEnvelope", "operation", (String) operationComboBox.getSelectedItem());
                    soapAction = (String) invokeConnectorService("getSoapAction", "operation", (String) operationComboBox.getSelectedItem());
                }

                return null;
            }

            public void done() {
                if (generatedEnvelope != null) {
                    soapEnvelope.setText(generatedEnvelope);
                    parent.setSaveEnabled(true);
                }

                if (soapAction != null) {
                    soapActionField.setText(soapAction);
                    parent.setSaveEnabled(true);
                }

                parent.stopWorking(workingId);
            }
        };
        worker.execute();
    }//GEN-LAST:event_generateEnvelopeActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel attachmentsLabel;
    private javax.swing.JScrollPane attachmentsPane;
    private com.mirth.connect.client.ui.components.MirthTable attachmentsTable;
    private javax.swing.ButtonGroup authenticationButtonGroup;
    private javax.swing.JLabel authenticationLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton authenticationNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton authenticationYesRadio;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton generateEnvelope;
    private javax.swing.JButton getOperationsButton;
    private javax.swing.ButtonGroup invocationButtonGroup;
    private com.mirth.connect.client.ui.components.MirthRadioButton invocationOneWayRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton invocationTwoWayRadio;
    private javax.swing.JLabel invocationTypeLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JButton newButton;
    private com.mirth.connect.client.ui.components.MirthComboBox operationComboBox;
    private com.mirth.connect.client.ui.components.MirthPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private com.mirth.connect.client.ui.components.MirthTextField portField;
    private javax.swing.JLabel portLabel;
    private com.mirth.connect.client.ui.components.MirthTextField serviceField;
    private javax.swing.JLabel serviceLabel;
    private com.mirth.connect.client.ui.components.MirthTextField soapActionField;
    private javax.swing.JLabel soapActionLabel;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea soapEnvelope;
    private javax.swing.ButtonGroup useMtomButtonGroup;
    private javax.swing.JLabel useMtomLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton useMtomNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton useMtomYesRadio;
    private javax.swing.ButtonGroup userPersistentQueuesButtonGroup;
    private com.mirth.connect.client.ui.components.MirthTextField usernameField;
    private javax.swing.JLabel usernameLabel;
    private com.mirth.connect.client.ui.components.MirthTextField wsdlUrlField;
    private javax.swing.JLabel wsdlUrlLabel;
    // End of variables declaration//GEN-END:variables
}
