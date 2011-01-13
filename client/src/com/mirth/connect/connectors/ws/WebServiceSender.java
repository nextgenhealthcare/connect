/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package com.mirth.connect.connectors.ws;

import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.XMLTokenMarker;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.connectors.ConnectorClass;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.QueuedSenderProperties;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class WebServiceSender extends ConnectorClass {

    private final int ID_COLUMN_NUMBER = 0;
    private final int CONTENT_COLUMN_NUMBER = 1;
    private final int MIME_TYPE_COLUMN_NUMBER = 2;
    private final String ID_COLUMN_NAME = "ID";
    private final String CONTENT_COLUMN_NAME = "Content";
    private final String MIME_TYPE_COLUMN_NAME = "MIME Type";
    ObjectXMLSerializer serializer = new ObjectXMLSerializer();
    private HashMap<String, String> channelList;

    public WebServiceSender() {
        name = WebServiceSenderProperties.name;
        initComponents();

        reconnectIntervalField.setDocument(new MirthFieldConstraints(0, false, false, true));
        queuePollIntervalField.setDocument(new MirthFieldConstraints(0, false, false, true));

        SyntaxDocument document = new SyntaxDocument();
        document.setTokenMarker(new XMLTokenMarker());
        soapEnvelope.setDocument(document);
    }

    public Properties getProperties() {
        Properties properties = new Properties();

        properties.put(WebServiceSenderProperties.DATATYPE, name);

        // Queue properties
        properties.put(QueuedSenderProperties.QUEUE_POLL_INTERVAL, queuePollIntervalField.getText());
        properties.put(QueuedSenderProperties.RECONNECT_INTERVAL, reconnectIntervalField.getText());

        if (usePersistentQueuesYesRadio.isSelected()) {
            properties.put(QueuedSenderProperties.USE_PERSISTENT_QUEUES, UIConstants.YES_OPTION);
        } else {
            properties.put(QueuedSenderProperties.USE_PERSISTENT_QUEUES, UIConstants.NO_OPTION);
        }

        if (rotateMessagesCheckBox.isSelected()) {
            properties.put(QueuedSenderProperties.ROTATE_QUEUE, UIConstants.YES_OPTION);
        } else {
            properties.put(QueuedSenderProperties.ROTATE_QUEUE, UIConstants.NO_OPTION);
        }
        // End of queue properties

        properties.put(WebServiceSenderProperties.WEBSERVICE_WSDL_URL, wsdlUrlField.getText());
        properties.put(WebServiceSenderProperties.WEBSERVICE_SERVICE, serviceField.getText());
        properties.put(WebServiceSenderProperties.WEBSERVICE_PORT, portField.getText());
        properties.put(WebServiceSenderProperties.WEBSERVICE_SOAP_ACTION, soapActionField.getText());

        if (invocationOneWayRadio.isSelected()) {
            properties.put(WebServiceSenderProperties.WEBSERVICE_ONE_WAY, UIConstants.YES_OPTION);
        } else {
            properties.put(WebServiceSenderProperties.WEBSERVICE_ONE_WAY, UIConstants.NO_OPTION);
        }

        if (operationComboBox.getSelectedIndex() != -1) {
            properties.put(WebServiceSenderProperties.WEBSERVICE_OPERATION, (String) operationComboBox.getSelectedItem());
        }

        properties.put(WebServiceSenderProperties.WEBSERVICE_HOST, buildHost());

        if (authenticationYesRadio.isSelected()) {
            properties.put(WebServiceSenderProperties.WEBSERVICE_USE_AUTHENTICATION, UIConstants.YES_OPTION);
        } else {
            properties.put(WebServiceSenderProperties.WEBSERVICE_USE_AUTHENTICATION, UIConstants.NO_OPTION);
        }

        properties.put(WebServiceSenderProperties.WEBSERVICE_USERNAME, usernameField.getText());
        properties.put(WebServiceSenderProperties.WEBSERVICE_PASSWORD, new String(passwordField.getPassword()));

        properties.put(WebServiceSenderProperties.WEBSERVICE_REPLY_CHANNEL_ID, channelList.get((String) channelNames.getSelectedItem()));

        properties.put(WebServiceSenderProperties.WEBSERVICE_ENVELOPE, soapEnvelope.getText());

        ArrayList<String> operations = new ArrayList<String>();
        for (int i = 0; i < operationComboBox.getModel().getSize(); i++) {
            operations.add((String) operationComboBox.getModel().getElementAt(i));
        }

        properties.put(WebServiceSenderProperties.WEBSERVICE_WSDL_OPERATIONS, serializer.toXML(operations));

        if (useMtomYesRadio.isSelected()) {
            properties.put(WebServiceSenderProperties.WEBSERVICE_USE_MTOM, UIConstants.YES_OPTION);
        } else {
            properties.put(WebServiceSenderProperties.WEBSERVICE_USE_MTOM, UIConstants.NO_OPTION);
        }

        ArrayList<ArrayList<String>> attachments = getAttachments();
        properties.put(WebServiceSenderProperties.WEBSERVICE_ATTACHMENT_NAMES, serializer.toXML(attachments.get(0)));
        properties.put(WebServiceSenderProperties.WEBSERVICE_ATTACHMENT_CONTENTS, serializer.toXML(attachments.get(1)));
        properties.put(WebServiceSenderProperties.WEBSERVICE_ATTACHMENT_TYPES, serializer.toXML(attachments.get(2)));

        return properties;
    }

    public void setProperties(Properties props) {
        resetInvalidProperties();

        wsdlUrlField.setText((String) props.get(WebServiceSenderProperties.WEBSERVICE_WSDL_URL));
        serviceField.setText((String) props.get(WebServiceSenderProperties.WEBSERVICE_SERVICE));
        portField.setText((String) props.get(WebServiceSenderProperties.WEBSERVICE_PORT));
        soapActionField.setText((String) props.get(WebServiceSenderProperties.WEBSERVICE_SOAP_ACTION));

        soapEnvelope.setText((String) props.getProperty(WebServiceSenderProperties.WEBSERVICE_ENVELOPE));

        if (((String) props.get(WebServiceSenderProperties.WEBSERVICE_USE_AUTHENTICATION)).equals(UIConstants.YES_OPTION)) {
            authenticationYesRadio.setSelected(true);
            authenticationYesRadioActionPerformed(null);
        } else {
            authenticationNoRadio.setSelected(true);
            authenticationNoRadioActionPerformed(null);
        }

        usernameField.setText(props.getProperty(WebServiceSenderProperties.WEBSERVICE_USERNAME));
        passwordField.setText(props.getProperty(WebServiceSenderProperties.WEBSERVICE_PASSWORD));

        // Queue properties
        queuePollIntervalField.setText((String) props.get(QueuedSenderProperties.QUEUE_POLL_INTERVAL));
        reconnectIntervalField.setText((String) props.get(QueuedSenderProperties.RECONNECT_INTERVAL));

        if (((String) props.get(QueuedSenderProperties.USE_PERSISTENT_QUEUES)).equals(UIConstants.YES_OPTION)) {
            usePersistentQueuesYesRadio.setSelected(true);
            usePersistentQueuesYesRadioActionPerformed(null);
        } else {
            usePersistentQueuesNoRadio.setSelected(true);
            usePersistentQueuesNoRadioActionPerformed(null);
        }

        if (((String) props.get(QueuedSenderProperties.ROTATE_QUEUE)).equals(UIConstants.YES_OPTION)) {
            rotateMessagesCheckBox.setSelected(true);
        } else {
            rotateMessagesCheckBox.setSelected(false);
        }
        // End of queue properties

        ArrayList<String> channelNameArray = new ArrayList<String>();
        channelList = new HashMap<String, String>();
        channelList.put("None", "sink");
        channelNameArray.add("None");

        String selectedChannelName = "None";

        for (Channel channel : parent.channels.values()) {
            if (((String) props.get(WebServiceSenderProperties.WEBSERVICE_REPLY_CHANNEL_ID)).equalsIgnoreCase(channel.getId())) {
                selectedChannelName = channel.getName();
            }

            channelList.put(channel.getName(), channel.getId());
            channelNameArray.add(channel.getName());
        }
        channelNames.setModel(new javax.swing.DefaultComboBoxModel(channelNameArray.toArray()));

        boolean enabled = parent.isSaveEnabled();

        channelNames.setSelectedItem(selectedChannelName);

        if (((String) props.get(WebServiceSenderProperties.WEBSERVICE_ONE_WAY)).equals(UIConstants.YES_OPTION)) {
            invocationOneWayRadio.setSelected(true);
        } else {
            invocationTwoWayRadio.setSelected(true);
        }

        ArrayList<String> operations = new ArrayList<String>();
        if (((String) props.get(WebServiceSenderProperties.WEBSERVICE_WSDL_OPERATIONS)).length() > 0) {
            operations = (ArrayList<String>) serializer.fromXML((String) props.get(WebServiceSenderProperties.WEBSERVICE_WSDL_OPERATIONS));
        }

        operationComboBox.setModel(new javax.swing.DefaultComboBoxModel(operations.toArray()));
        enableOrDisableGenerateEnvelope(operations);

        operationComboBox.setSelectedItem(props.getProperty(WebServiceSenderProperties.WEBSERVICE_OPERATION));

        parent.setSaveEnabled(enabled);

        ArrayList<ArrayList<String>> attachments = new ArrayList<ArrayList<String>>();
        ArrayList<String> attachmentNames = new ArrayList<String>();
        ArrayList<String> attachmentContents = new ArrayList<String>();
        ArrayList<String> attachmentTypes = new ArrayList<String>();

        if (((String) props.get(WebServiceSenderProperties.WEBSERVICE_ATTACHMENT_NAMES)).length() > 0) {
            attachmentNames = (ArrayList<String>) serializer.fromXML((String) props.get(WebServiceSenderProperties.WEBSERVICE_ATTACHMENT_NAMES));
            attachmentContents = (ArrayList<String>) serializer.fromXML((String) props.get(WebServiceSenderProperties.WEBSERVICE_ATTACHMENT_CONTENTS));
            attachmentTypes = (ArrayList<String>) serializer.fromXML((String) props.get(WebServiceSenderProperties.WEBSERVICE_ATTACHMENT_TYPES));
        }

        attachments.add(attachmentNames);
        attachments.add(attachmentContents);
        attachments.add(attachmentTypes);

        setAttachments(attachments);

        if (((String) props.get(WebServiceSenderProperties.WEBSERVICE_USE_MTOM)).equals(UIConstants.YES_OPTION)) {
            useMtomYesRadio.setSelected(true);
            useMtomYesRadioActionPerformed(null);
        } else {
            useMtomNoRadio.setSelected(true);
            useMtomNoRadioActionPerformed(null);
        }
    }

    public Properties getDefaults() {
        return new WebServiceSenderProperties().getDefaults();
    }

    public boolean checkProperties(Properties props, boolean highlight) {
        resetInvalidProperties();
        boolean valid = true;

        if (((String) props.getProperty(WebServiceSenderProperties.WEBSERVICE_WSDL_URL)).length() == 0) {
            valid = false;
            if (highlight) {
                wsdlUrlField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (((String) props.getProperty(WebServiceSenderProperties.WEBSERVICE_SERVICE)).length() == 0) {
            valid = false;
            if (highlight) {
                serviceField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (((String) props.getProperty(WebServiceSenderProperties.WEBSERVICE_PORT)).length() == 0) {
            valid = false;
            if (highlight) {
                portField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (((String) props.getProperty(WebServiceSenderProperties.WEBSERVICE_ENVELOPE)).length() == 0) {
            valid = false;
            if (highlight) {
                soapEnvelope.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (((String) props.get(QueuedSenderProperties.USE_PERSISTENT_QUEUES)).equals(UIConstants.YES_OPTION)) {

            if (((String) props.get(QueuedSenderProperties.RECONNECT_INTERVAL)).length() == 0) {
                valid = false;
                if (highlight) {
                    reconnectIntervalField.setBackground(UIConstants.INVALID_COLOR);
                }
            }

            if (((String) props.get(QueuedSenderProperties.QUEUE_POLL_INTERVAL)).length() == 0) {
                valid = false;
                if (highlight) {
                    queuePollIntervalField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }

        return valid;
    }

    private void resetInvalidProperties() {
        wsdlUrlField.setBackground(null);
        serviceField.setBackground(new java.awt.Color(222, 222, 222));
        portField.setBackground(new java.awt.Color(222, 222, 222));
        soapEnvelope.setBackground(null);
        queuePollIntervalField.setBackground(null);
        reconnectIntervalField.setBackground(null);
    }

    public String doValidate(Properties props, boolean highlight) {
        String error = null;

        if (!checkProperties(props, highlight)) {
            error = "Error in the form for connector \"" + getName() + "\".\n\n";
        }

        return error;
    }

    private void enableOrDisableGenerateEnvelope(List<String> operations) {
        if ((operations.size() == 1) && operations.get(0).equals(WebServiceSenderProperties.WEBSERVICE_DEFAULT_DROPDOWN)) {
            generateEnvelope.setEnabled(false);
        } else {
            generateEnvelope.setEnabled(true);
        }
    }

    private boolean isWsdlCached() {
        String wsdlUrl = wsdlUrlField.getText().trim();
        
        if (wsdlUrl.equals("")) {
            return false;
        }

        boolean isWsdlCached = false;

        try {
            isWsdlCached = (Boolean) parent.mirthClient.invokeConnectorService(name, "isWsdlCached", wsdlUrl);
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

            parent.mirthClient.invokeConnectorService(name, "cacheWsdlFromUrl", cacheWsdlMap);

            return true;
        } catch (ClientException e) {
            parent.alertError(parent, "Error caching WSDL. Please check the WSDL URL and authentication settings.");
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
            returnObject = parent.mirthClient.invokeConnectorService(name, method, params);
        } catch (ClientException e) {
            if (method.equals("generateEnvelope")) {
                parent.alertError(parent, "There was an error generating the envelope. Mirth Connect \ncurrently does not support all WSDL types. Please use another \ntool, such as soapUI (soapui.org), to generate the envelope.");
            } else {
                parent.alertError(parent, "Error calling " + method + " with parameter: " + params.toString());
            }
        }

        return returnObject;
    }

    public String buildHost() {
        if (wsdlUrlField.getText().startsWith("http://")) {
            return wsdlUrlField.getText().substring(7);
        } else if (wsdlUrlField.getText().startsWith("https://")) {
            return wsdlUrlField.getText().substring(8);
        } else {
            return wsdlUrlField.getText();
        }
    }

    public void setAttachments(ArrayList<ArrayList<String>> attachments) {

        ArrayList<String> attachmentIds = attachments.get(0);
        ArrayList<String> attachmentContents = attachments.get(1);
        ArrayList<String> attachmentTypes = attachments.get(2);

        Object[][] tableData = new Object[attachmentIds.size()][3];

        attachmentsTable = new MirthTable();

        for (int i = 0; i < attachmentIds.size(); i++) {
            tableData[i][ID_COLUMN_NUMBER] = attachmentIds.get(i);
            tableData[i][CONTENT_COLUMN_NUMBER] = attachmentContents.get(i);
            tableData[i][MIME_TYPE_COLUMN_NUMBER] = attachmentTypes.get(i);
        }

        attachmentsTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[]{ID_COLUMN_NAME, CONTENT_COLUMN_NAME, MIME_TYPE_COLUMN_NAME}) {

            boolean[] canEdit = new boolean[]{true, true, true};

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

        class AttachmentsTableCellEditor extends AbstractCellEditor implements TableCellEditor {

            JComponent component = new JTextField();
            Object originalValue;
            boolean checkUnique;

            public AttachmentsTableCellEditor(boolean checkUnique) {
                super();
                this.checkUnique = checkUnique;
            }

            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                // 'value' is value contained in the cell located at (rowIndex, vColIndex)
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

                if (checkUnique && (s.length() == 0 || checkUnique(s))) {
                    super.cancelCellEditing();
                } else {
                    parent.setSaveEnabled(true);
                }

                deleteButton.setEnabled(true);

                return super.stopCellEditing();
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

        attachmentsTable.getColumnModel().getColumn(attachmentsTable.getColumnModelIndex(ID_COLUMN_NAME)).setCellEditor(new AttachmentsTableCellEditor(true));
        attachmentsTable.getColumnModel().getColumn(attachmentsTable.getColumnModelIndex(CONTENT_COLUMN_NAME)).setCellEditor(new AttachmentsTableCellEditor(false));
        attachmentsTable.getColumnModel().getColumn(attachmentsTable.getColumnModelIndex(MIME_TYPE_COLUMN_NAME)).setCellEditor(new AttachmentsTableCellEditor(false));

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

    public ArrayList<ArrayList<String>> getAttachments() {
        ArrayList<ArrayList<String>> attachments = new ArrayList<ArrayList<String>>();

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
        channelNames = new com.mirth.connect.client.ui.components.MirthComboBox();
        URL1 = new javax.swing.JLabel();
        rotateMessagesCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        usePersistentQueuesNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        usePersistentQueuesYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        reconnectIntervalField = new com.mirth.connect.client.ui.components.MirthTextField();
        reconnectIntervalLabel = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
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
        queuePollIntervalLabel = new javax.swing.JLabel();
        queuePollIntervalField = new com.mirth.connect.client.ui.components.MirthTextField();
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

        channelNames.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        channelNames.setToolTipText("Select None to ignore the response from the web service method, or select a channel to send to as a new inbound message.");

        URL1.setText("Send Response to:");

        rotateMessagesCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        rotateMessagesCheckBox.setText("Rotate Messages in Queue");
        rotateMessagesCheckBox.setToolTipText("<html>If checked, upon unsuccessful re-try, it will rotate and put the queued message to the back of the queue<br> in order to prevent it from clogging the queue and to let the other subsequent messages in queue be processed.<br>If the order of messages processed is important, this should be unchecked.</html>");

        usePersistentQueuesNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        usePersistentQueuesNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        userPersistentQueuesButtonGroup.add(usePersistentQueuesNoRadio);
        usePersistentQueuesNoRadio.setSelected(true);
        usePersistentQueuesNoRadio.setText("No");
        usePersistentQueuesNoRadio.setToolTipText("If checked, no queues will be used.");
        usePersistentQueuesNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        usePersistentQueuesNoRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usePersistentQueuesNoRadioActionPerformed(evt);
            }
        });

        usePersistentQueuesYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        usePersistentQueuesYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        userPersistentQueuesButtonGroup.add(usePersistentQueuesYesRadio);
        usePersistentQueuesYesRadio.setText("Yes");
        usePersistentQueuesYesRadio.setToolTipText("<html>If checked, the connector will store any messages that are unable to be successfully processed in a file-based queue.<br>Messages will be automatically resent until the queue is manually cleared or the message is successfully sent.<br>The default queue location is (Mirth Directory)/.mule/queuestore/(ChannelID),<br> where (Mirth Directory) is the main Mirth install root and (ChannelID) is the unique id of the current channel.</html>");
        usePersistentQueuesYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        usePersistentQueuesYesRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usePersistentQueuesYesRadioActionPerformed(evt);
            }
        });

        reconnectIntervalField.setToolTipText("<html>The amount of time that should elapse between retry attempts to send messages in the queue.</html>");

        reconnectIntervalLabel.setText("Reconnect Interval (ms):");

        jLabel36.setText("Use Persistent Queues:");

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

        queuePollIntervalLabel.setText("Queue Poll Interval (ms):");

        queuePollIntervalField.setToolTipText("<html>The amount of time that should elapse between polls of an empty queue to check for queued messages.</html>");

        soapActionField.setBackground(new java.awt.Color(222, 222, 222));
        soapActionField.setToolTipText("<html>The SOAPAction HTTP request header field can be used to indicate the intent of the SOAP HTTP request.<br>This field is optional for most web services, and will be auto-populated when you select an operation.</html>");

        soapActionLabel.setText("SOAP Action:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(authenticationLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(usernameLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(invocationTypeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(portLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(passwordLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(serviceLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(useMtomLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(reconnectIntervalLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(queuePollIntervalLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(URL1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(wsdlUrlLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel36, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(attachmentsLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(soapActionLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(wsdlUrlField, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(getOperationsButton))
                    .addComponent(serviceField, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                    .addComponent(portField, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                    .addComponent(soapActionField, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                    .addComponent(queuePollIntervalField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(authenticationYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(authenticationNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(operationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(generateEnvelope))
                            .addComponent(reconnectIntervalField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(usePersistentQueuesYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(usePersistentQueuesNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(rotateMessagesCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(channelNames, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(64, 64, 64))
                    .addComponent(soapEnvelope, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(invocationTwoWayRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(invocationOneWayRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(attachmentsPane, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(newButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(deleteButton)))
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
                    .addComponent(soapActionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(soapActionLabel))
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
                    .addComponent(channelNames, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(URL1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel36)
                    .addComponent(usePersistentQueuesYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usePersistentQueuesNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rotateMessagesCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(queuePollIntervalField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(queuePollIntervalLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reconnectIntervalLabel)
                    .addComponent(reconnectIntervalField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(soapEnvelope, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                    .addComponent(jLabel4))
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

private void usePersistentQueuesNoRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usePersistentQueuesNoRadioActionPerformed
    rotateMessagesCheckBox.setEnabled(false);
    queuePollIntervalLabel.setEnabled(false);
    queuePollIntervalField.setEnabled(false);
    reconnectIntervalField.setEnabled(false);
    reconnectIntervalLabel.setEnabled(false);
}//GEN-LAST:event_usePersistentQueuesNoRadioActionPerformed

private void usePersistentQueuesYesRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usePersistentQueuesYesRadioActionPerformed
    rotateMessagesCheckBox.setEnabled(true);
    queuePollIntervalLabel.setEnabled(true);
    queuePollIntervalField.setEnabled(true);
    reconnectIntervalField.setEnabled(true);
    reconnectIntervalLabel.setEnabled(true);
}//GEN-LAST:event_usePersistentQueuesYesRadioActionPerformed

private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
    stopCellEditing();
    ((DefaultTableModel) attachmentsTable.getModel()).addRow(new Object[]{getNewAttachmentId(attachmentsTable.getModel().getRowCount() + 1), ""});
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
    parent.setWorking("Getting operations...", true);

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
                enableOrDisableGenerateEnvelope(loadedMethods);

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

            parent.setWorking("", false);
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
    if (soapEnvelope.getText().length() > 0) {
        if (!parent.alertOkCancel(parent, "This will replace your current SOAP envelope with a generated envelope. Press OK to continue.")) {
            return;
        }
    }

    parent.setWorking("Generating envelope...", true);

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

            parent.setWorking("", false);
        }
    };
    worker.execute();
}//GEN-LAST:event_generateEnvelopeActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel URL1;
    private javax.swing.JLabel attachmentsLabel;
    private javax.swing.JScrollPane attachmentsPane;
    private com.mirth.connect.client.ui.components.MirthTable attachmentsTable;
    private javax.swing.ButtonGroup authenticationButtonGroup;
    private javax.swing.JLabel authenticationLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton authenticationNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton authenticationYesRadio;
    private com.mirth.connect.client.ui.components.MirthComboBox channelNames;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton generateEnvelope;
    private javax.swing.JButton getOperationsButton;
    private javax.swing.ButtonGroup invocationButtonGroup;
    private com.mirth.connect.client.ui.components.MirthRadioButton invocationOneWayRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton invocationTwoWayRadio;
    private javax.swing.JLabel invocationTypeLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JButton newButton;
    private com.mirth.connect.client.ui.components.MirthComboBox operationComboBox;
    private com.mirth.connect.client.ui.components.MirthPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private com.mirth.connect.client.ui.components.MirthTextField portField;
    private javax.swing.JLabel portLabel;
    private com.mirth.connect.client.ui.components.MirthTextField queuePollIntervalField;
    private javax.swing.JLabel queuePollIntervalLabel;
    private com.mirth.connect.client.ui.components.MirthTextField reconnectIntervalField;
    private javax.swing.JLabel reconnectIntervalLabel;
    private com.mirth.connect.client.ui.components.MirthCheckBox rotateMessagesCheckBox;
    private com.mirth.connect.client.ui.components.MirthTextField serviceField;
    private javax.swing.JLabel serviceLabel;
    private com.mirth.connect.client.ui.components.MirthTextField soapActionField;
    private javax.swing.JLabel soapActionLabel;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea soapEnvelope;
    private javax.swing.ButtonGroup useMtomButtonGroup;
    private javax.swing.JLabel useMtomLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton useMtomNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton useMtomYesRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton usePersistentQueuesNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton usePersistentQueuesYesRadio;
    private javax.swing.ButtonGroup userPersistentQueuesButtonGroup;
    private com.mirth.connect.client.ui.components.MirthTextField usernameField;
    private javax.swing.JLabel usernameLabel;
    private com.mirth.connect.client.ui.components.MirthTextField wsdlUrlField;
    private javax.swing.JLabel wsdlUrlLabel;
    // End of variables declaration//GEN-END:variables
}
