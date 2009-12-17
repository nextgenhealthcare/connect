/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.connectors.ws;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.prefs.Preferences;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import org.jdesktop.swingworker.SwingWorker;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.XMLTokenMarker;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.Mirth;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthTable;
import com.webreach.mirth.client.ui.util.FileUtil;
import com.webreach.mirth.connectors.ConnectorClass;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.QueuedSenderProperties;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class WebServiceSender extends ConnectorClass
{
    public final int PARAMETER_COLUMN = 0;
    public final int TYPE_COLUMN = 1;
    public final int VALUE_COLUMN = 2;

    private final int ID_COLUMN_NUMBER = 0;
    private final int CONTENT_COLUMN_NUMBER = 1;
    private final int MIME_TYPE_COLUMN_NUMBER = 2;
    private final String ID_COLUMN_NAME = "ID";
    private final String CONTENT_COLUMN_NAME = "Content";
    private final String MIME_TYPE_COLUMN_NAME = "MIME type";

    public final String PARAMETER_COLUMN_NAME = "Parameter";
    public final String TYPE_COLUMN_NAME = "Type";
    public final String VALUE_COLUMN_NAME = "Value";
    
    ObjectXMLSerializer serializer = new ObjectXMLSerializer();
    private HashMap<String, String> channelList;
    private int attachmentsLastIndex = -1;

    private String wsdlCacheId = "";
    
    public WebServiceSender()
    {
        name = WebServiceSenderProperties.name;
        initComponents();
        SyntaxDocument document = new SyntaxDocument();
        document.setTokenMarker(new XMLTokenMarker());
        soapEnvelope.setDocument(document);
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();

        properties.put(WebServiceSenderProperties.DATATYPE, name);
        
        properties.put(WebServiceSenderProperties.WEBSERVICE_WSDL_CACHE_ID, wsdlCacheId);

        // Queue properties
        properties.put(QueuedSenderProperties.RECONNECT_INTERVAL, reconnectInterval.getText());
        
        if (usePersistentQueuesYesRadio.isSelected())
            properties.put(QueuedSenderProperties.USE_PERSISTENT_QUEUES, UIConstants.YES_OPTION);
        else
            properties.put(QueuedSenderProperties.USE_PERSISTENT_QUEUES, UIConstants.NO_OPTION);

        if (rotateMessages.isSelected())
            properties.put(QueuedSenderProperties.ROTATE_QUEUE, UIConstants.YES_OPTION);
        else
            properties.put(QueuedSenderProperties.ROTATE_QUEUE, UIConstants.NO_OPTION);
        // End of queue properties
        
        properties.put(WebServiceSenderProperties.WEBSERVICE_WSDL_URL, wsdlUrl.getText());
        properties.put(WebServiceSenderProperties.WEBSERVICE_SERVICE, serviceField.getText());
        properties.put(WebServiceSenderProperties.WEBSERVICE_PORT, portField.getText());

        if (invocationOneWayRadio.isSelected())
            properties.put(WebServiceSenderProperties.WEBSERVICE_ONE_WAY, UIConstants.YES_OPTION);
        else
            properties.put(WebServiceSenderProperties.WEBSERVICE_ONE_WAY, UIConstants.NO_OPTION);

        if (operationComboBox.getSelectedIndex() != -1)
            properties.put(WebServiceSenderProperties.WEBSERVICE_OPERATION, (String) operationComboBox.getSelectedItem());

        properties.put(WebServiceSenderProperties.WEBSERVICE_HOST, buildHost());
        
        if (authenticationYesRadio.isSelected())
            properties.put(WebServiceSenderProperties.WEBSERVICE_USE_AUTHENTICATION, UIConstants.YES_OPTION);
        else
            properties.put(WebServiceSenderProperties.WEBSERVICE_USE_AUTHENTICATION, UIConstants.NO_OPTION);

        properties.put(WebServiceSenderProperties.WEBSERVICE_USERNAME, usernameField.getText());
        properties.put(WebServiceSenderProperties.WEBSERVICE_PASSWORD, new String(passwordField.getPassword()));

        properties.put(WebServiceSenderProperties.WEBSERVICE_REPLY_CHANNEL_ID, channelList.get((String) channelNames.getSelectedItem()));

        properties.put(WebServiceSenderProperties.WEBSERVICE_ENVELOPE, soapEnvelope.getText());

        ArrayList<String> operations = new ArrayList<String>();
        for (int i = 0; i < operationComboBox.getModel().getSize(); i++) {
            operations.add((String)operationComboBox.getModel().getElementAt(i));
        }

        properties.put(WebServiceSenderProperties.WEBSERVICE_WSDL_OPERATIONS, serializer.toXML(operations));

        if (useMtomYesRadio.isSelected())
            properties.put(WebServiceSenderProperties.WEBSERVICE_USE_MTOM, UIConstants.YES_OPTION);
        else
            properties.put(WebServiceSenderProperties.WEBSERVICE_USE_MTOM, UIConstants.NO_OPTION);

        ArrayList<ArrayList<String>> attachments = getAttachments();
        properties.put(WebServiceSenderProperties.WEBSERVICE_ATTACHMENT_NAMES, serializer.toXML(attachments.get(0)));
        properties.put(WebServiceSenderProperties.WEBSERVICE_ATTACHMENT_CONTENTS, serializer.toXML(attachments.get(1)));
        properties.put(WebServiceSenderProperties.WEBSERVICE_ATTACHMENT_TYPES, serializer.toXML(attachments.get(2)));
        
        return properties;
    }

    public void setProperties(Properties props)
    {
        resetInvalidProperties();

        wsdlCacheId = props.getProperty(WebServiceSenderProperties.WEBSERVICE_WSDL_URL);

        wsdlUrl.setText((String) props.get(WebServiceSenderProperties.WEBSERVICE_WSDL_URL));
        serviceField.setText((String) props.get(WebServiceSenderProperties.WEBSERVICE_SERVICE));
        portField.setText((String) props.get(WebServiceSenderProperties.WEBSERVICE_PORT));

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
        reconnectInterval.setText((String) props.get(QueuedSenderProperties.RECONNECT_INTERVAL));
        
        if (((String) props.get(QueuedSenderProperties.USE_PERSISTENT_QUEUES)).equals(UIConstants.YES_OPTION))
        {
            usePersistentQueuesYesRadio.setSelected(true);
            usePersistentQueuesYesRadioActionPerformed(null);
        }
        else
        {
            usePersistentQueuesNoRadio.setSelected(true);
            usePersistentQueuesNoRadioActionPerformed(null);
        }
        
        if (((String) props.get(QueuedSenderProperties.ROTATE_QUEUE)).equals(UIConstants.YES_OPTION))
            rotateMessages.setSelected(true);
        else
            rotateMessages.setSelected(false);
        // End of queue properties
        
        ArrayList<String> channelNameArray = new ArrayList<String>();
        channelList = new HashMap<String, String>();
        channelList.put("None", "sink");
        channelNameArray.add("None");
        
        String selectedChannelName = "None";
        
        for (Channel channel : parent.channels.values())
        {
        	if (((String) props.get(WebServiceSenderProperties.WEBSERVICE_REPLY_CHANNEL_ID)).equalsIgnoreCase(channel.getId()))
        		selectedChannelName = channel.getName();
        	
            channelList.put(channel.getName(), channel.getId());
            channelNameArray.add(channel.getName());
        }
        channelNames.setModel(new javax.swing.DefaultComboBoxModel(channelNameArray.toArray()));

        boolean visible = parent.channelEditTasks.getContentPane().getComponent(0).isVisible();

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

        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(visible);
        
        if (((String) props.get(WebServiceSenderProperties.WEBSERVICE_USE_MTOM)).equals(UIConstants.YES_OPTION)) {
            useMtomYesRadio.setSelected(true);
            useMtomYesRadioActionPerformed(null);
        } else {
            useMtomNoRadio.setSelected(true);
            useMtomNoRadioActionPerformed(null);
        }

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
    }
    
    public Properties getDefaults()
    {
        return new WebServiceSenderProperties().getDefaults();
    }
    
    public boolean checkProperties(Properties props, boolean highlight)
    {
        resetInvalidProperties();
        boolean valid = true;
        
        if (((String) props.getProperty(WebServiceSenderProperties.WEBSERVICE_ENVELOPE)).length() == 0)
        {
            valid = false;
            if (highlight)
            	soapEnvelope.setBackground(UIConstants.INVALID_COLOR);
        }
        
        if (((String) props.get(QueuedSenderProperties.USE_PERSISTENT_QUEUES)).equals(UIConstants.YES_OPTION) && ((String) props.get(QueuedSenderProperties.RECONNECT_INTERVAL)).length() == 0)
        {
            valid = false;
            if (highlight)
            	reconnectInterval.setBackground(UIConstants.INVALID_COLOR);
        }
        
        return valid;
    }

    private void resetInvalidProperties()
    {
        soapEnvelope.setBackground(null);
        reconnectInterval.setBackground(null);
    }
    
    public String doValidate(Properties props, boolean highlight)
    {
    	String error = null;
    	
    	if (!checkProperties(props, highlight))
    		error = "Error in the form for connector \"" + getName() + "\".\n\n";
    	
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
        if (wsdlCacheId.equals("")) {
            return false;
        }

        boolean isWsdlCached = false;
        
        try {
            isWsdlCached = (Boolean) parent.mirthClient.invokeConnectorService(name, "isWsdlCached", wsdlCacheId);
        } catch (ClientException e) {
            parent.alertError(parent, "Error checking if the wsdl is cached.");
        }
        
        return isWsdlCached;
    }

    private boolean cacheWsdl() {
        try {
            // if it's a local wsdl file, send over the file content.
            String wsdlPath = wsdlUrl.getText().trim();
            File wsdlFile = new File(wsdlPath);
            Map<String, String> cacheWsdlMap = new HashMap<String, String>();

            wsdlCacheId = UUID.randomUUID().toString();
            cacheWsdlMap.put("id", wsdlCacheId);

            if (wsdlFile.exists()) {
                try {
                    cacheWsdlMap.put("wsdlContents", FileUtil.read(wsdlFile));
                    parent.mirthClient.invokeConnectorService(name, "cacheWsdlFromFile", cacheWsdlMap);
                } catch (IOException e) {
                    // error reading in the file.
                    parent.alertError(parent, "Error reading in the WSDL file.");
                }
            } else {
                cacheWsdlMap.put("wsdlUrl", wsdlPath);

                if (authenticationYesRadio.isSelected()) {
                    cacheWsdlMap.put("username", usernameField.getText());
                    cacheWsdlMap.put("password", new String(passwordField.getPassword()));
                }

                parent.mirthClient.invokeConnectorService(name, "cacheWsdlFromUrl", cacheWsdlMap);
            }

            return true;
        } catch (ClientException e) {
            parent.alertError(parent, "Error caching WSDL. Please check the WSDL file or URL and authentication settings.");
            return false;
        }
    }

    private Object invokeConnectorService(String method) {
        return invokeConnectorService(method, null, null);
    }

    private Object invokeConnectorService(String method, String paramName, String paramValue) {
        Object returnObject = null;

        Object params = null;
        if (paramName == null) {
            params = wsdlCacheId;
        } else {
            Map<String, String> paramMap = new HashMap<String, String>();
            paramMap.put("id", wsdlCacheId);
            paramMap.put(paramName, paramValue);
            
            params = paramMap;
        }

        try {
            returnObject = parent.mirthClient.invokeConnectorService(name, method, params);
        } catch (ClientException e) {
            parent.alertError(parent, "Error calling " + method + " with parameter: " + params.toString());
        }

        return returnObject;
    }

    public String buildHost()
    {
        return "ws:" + serviceField.getText() + "?method=" + (String) operationComboBox.getSelectedItem();
    }

    public void setAttachments(ArrayList<ArrayList<String>> attachments)
    {
    	ArrayList<String> attachmentNames = attachments.get(0);
    	ArrayList<String> attachmentContents = attachments.get(1);
    	ArrayList<String> attachmentTypes = attachments.get(2);

        Object[][] tableData = new Object[attachmentNames.size()][3];

        attachmentsTable = new MirthTable();

        for (int i = 0; i < attachmentNames.size(); i++)
        {
        	tableData[i][ID_COLUMN_NUMBER] = attachmentNames.get(i);
            tableData[i][CONTENT_COLUMN_NUMBER] = attachmentContents.get(i);
            tableData[i][MIME_TYPE_COLUMN_NUMBER] = attachmentTypes.get(i);
        }

        attachmentsTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[] { ID_COLUMN_NAME, CONTENT_COLUMN_NAME, MIME_TYPE_COLUMN_NAME })
        {
            boolean[] canEdit = new boolean[] { true, true, true };

            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return canEdit[columnIndex];
            }
        });

        attachmentsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {
                if (getSelectedRow(attachmentsTable) != -1)
                {
                    attachmentsLastIndex = getSelectedRow(attachmentsTable);
                    deleteButton.setEnabled(true);
                }
                else
                    deleteButton.setEnabled(false);
            }
        });

        class AttachmentsTableCellEditor extends AbstractCellEditor implements TableCellEditor
        {
            JComponent component = new JTextField();

            Object originalValue;

            boolean checkAttachments;

            public AttachmentsTableCellEditor(boolean checkAttachments)
            {
                super();
                this.checkAttachments = checkAttachments;
            }

            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
            {
                // 'value' is value contained in the cell located at (rowIndex,
                // vColIndex)
                originalValue = value;

                if (isSelected)
                {
                    // cell (and perhaps other cells) are selected
                }

                // Configure the component with the specified value
                ((JTextField) component).setText((String) value);

                // Return the configured component
                return component;
            }

            public Object getCellEditorValue()
            {
                return ((JTextField) component).getText();
            }

            public boolean stopCellEditing()
            {
                String s = (String) getCellEditorValue();

                if (checkAttachments && (s.length() == 0 || checkUniqueAttachment(s)))
                    super.cancelCellEditing();
                else
                    parent.enableSave();

                deleteButton.setEnabled(true);

                return super.stopCellEditing();
            }

            public boolean checkUniqueAttachment(String attachmentName)
            {
                boolean exists = false;

                for (int i = 0; i < attachmentsTable.getRowCount(); i++)
                {
                    if (attachmentsTable.getValueAt(i, ID_COLUMN_NUMBER) != null && ((String) attachmentsTable.getValueAt(i, ID_COLUMN_NUMBER)).equalsIgnoreCase(attachmentName))
                        exists = true;
                }

                return exists;
            }

            /**
             * Enables the editor only for double-clicks.
             */
            public boolean isCellEditable(EventObject evt)
            {
                if (evt instanceof MouseEvent && ((MouseEvent) evt).getClickCount() >= 2)
                {
                    deleteButton.setEnabled(false);
                    return true;
                }
                return false;
            }
        };

        attachmentsTable.getColumnModel().getColumn(attachmentsTable.getColumnModel().getColumnIndex(ID_COLUMN_NAME)).setCellEditor(new AttachmentsTableCellEditor(true));
        attachmentsTable.getColumnModel().getColumn(attachmentsTable.getColumnModel().getColumnIndex(CONTENT_COLUMN_NAME)).setCellEditor(new AttachmentsTableCellEditor(false));
        attachmentsTable.getColumnModel().getColumn(attachmentsTable.getColumnModel().getColumnIndex(MIME_TYPE_COLUMN_NAME)).setCellEditor(new AttachmentsTableCellEditor(false));

        attachmentsTable.setSelectionMode(0);
        attachmentsTable.setRowSelectionAllowed(true);
        attachmentsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        attachmentsTable.setDragEnabled(false);
        attachmentsTable.setOpaque(true);
        attachmentsTable.setSortable(false);
        attachmentsTable.getTableHeader().setReorderingAllowed(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
        	Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
        	attachmentsTable.setHighlighters(highlighter);
        }

        attachmentsPane.setViewportView(attachmentsTable);
    }

    public ArrayList<ArrayList<String>> getAttachments() {
        ArrayList<ArrayList<String>> attachments = new ArrayList<ArrayList<String>>();

        ArrayList<String> attachmentNames = new ArrayList<String>();
        ArrayList<String> attachmentContents = new ArrayList<String>();
        ArrayList<String> attachmentTypes = new ArrayList<String>();

        for (int i = 0; i < attachmentsTable.getRowCount(); i++)
        {
            if (((String) attachmentsTable.getValueAt(i, ID_COLUMN_NUMBER)).length() > 0)
            {
                attachmentNames.add((String)attachmentsTable.getValueAt(i, ID_COLUMN_NUMBER));
                attachmentContents.add((String)attachmentsTable.getValueAt(i, CONTENT_COLUMN_NUMBER));
                attachmentTypes.add((String)attachmentsTable.getValueAt(i, MIME_TYPE_COLUMN_NUMBER));
            }
        }

        attachments.add(attachmentNames);
        attachments.add(attachmentContents);
        attachments.add(attachmentTypes);

        return attachments;
    }

    /** Clears the selection in the table and sets the tasks appropriately */
    public void deselectRows(MirthTable table, JButton button)
    {
        table.clearSelection();
        button.setEnabled(false);
    }

    /** Get the currently selected table index */
    public int getSelectedRow(MirthTable table)
    {
        if (table.isEditing())
            return table.getEditingRow();
        else
            return table.getSelectedRow();
    }

    /**
     * Get the name that should be used for a new property so that it is unique.
     */
    private String getNewAttachmentName(MirthTable table)
    {
        String temp = "Attachment ";

        for (int i = 1; i <= table.getRowCount() + 1; i++)
        {
            boolean exists = false;
            for (int j = 0; j < table.getRowCount(); j++)
            {
                if (((String) table.getValueAt(j, ID_COLUMN_NUMBER)).equalsIgnoreCase(temp + i))
                {
                    exists = true;
                }
            }
            if (!exists)
                return temp + i;
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
        URL = new javax.swing.JLabel();
        wsdlUrl = new com.webreach.mirth.client.ui.components.MirthTextField();
        getOperationsButton = new javax.swing.JButton();
        operationComboBox = new com.webreach.mirth.client.ui.components.MirthComboBox();
        jLabel1 = new javax.swing.JLabel();
        serviceLabel = new javax.swing.JLabel();
        portField = new com.webreach.mirth.client.ui.components.MirthTextField();
        soapEnvelope = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea(true,false);
        portLabel = new javax.swing.JLabel();
        serviceField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel4 = new javax.swing.JLabel();
        generateEnvelope = new javax.swing.JButton();
        channelNames = new com.webreach.mirth.client.ui.components.MirthComboBox();
        URL1 = new javax.swing.JLabel();
        rotateMessages = new com.webreach.mirth.client.ui.components.MirthCheckBox();
        usePersistentQueuesNoRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        usePersistentQueuesYesRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        reconnectInterval = new com.webreach.mirth.client.ui.components.MirthTextField();
        reconnectIntervalLabel = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        browseWSDLfileButton = new javax.swing.JButton();
        attachmentsLabel = new javax.swing.JLabel();
        attachmentsPane = new javax.swing.JScrollPane();
        attachmentsTable = new com.webreach.mirth.client.ui.components.MirthTable();
        newButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        authenticationLabel = new javax.swing.JLabel();
        authenticationYesRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        authenticationNoRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        usernameLabel = new javax.swing.JLabel();
        usernameField = new com.webreach.mirth.client.ui.components.MirthTextField();
        passwordField = new com.webreach.mirth.client.ui.components.MirthPasswordField();
        passwordLabel = new javax.swing.JLabel();
        invocationTypeLabel = new javax.swing.JLabel();
        invocationTwoWayRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        invocationOneWayRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        useMtomLabel = new javax.swing.JLabel();
        useMtomYesRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        useMtomNoRadio = new com.webreach.mirth.client.ui.components.MirthRadioButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        URL.setText("WSDL Path:");

        wsdlUrl.setToolTipText("Enter the full URL to the WSDL file describing the web service method to be called, and then click the Get Methods button.");

        getOperationsButton.setText("Get Operations");
        getOperationsButton.setToolTipText("<html>Clicking this button fetches the WSDL file from the specified URL<br> and parses it to obtain a description of the data types and methods used by the web service to be called.<br>It replaces the values of all of the controls below by values taken from the WSDL file.</html>");
        getOperationsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getOperationsButtonActionPerformed(evt);
            }
        });

        operationComboBox.setToolTipText("Select the web service method to be called from this list.");

        jLabel1.setText("Operation:");

        serviceLabel.setText("Service:");

        portField.setBackground(new java.awt.Color(222, 222, 222));
        portField.setEditable(false);
        portField.setToolTipText("<html>Enter the SOAP Action URI for the method to be called here.<br>This field is normally filled in automatically when the Get Methods button is clicked and does not need to be changed.</html>");

        soapEnvelope.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        portLabel.setText("Port:");

        serviceField.setBackground(new java.awt.Color(222, 222, 222));
        serviceField.setEditable(false);
        serviceField.setToolTipText("<html>Enter the Service Endpoint URI for the method to be called here.<br>This field is normally filled in automatically when the Get Methods button is clicked and does not need to be changed.</html>");

        jLabel4.setText("SOAP Envelope:");

        generateEnvelope.setText("Generate Envelope");
        generateEnvelope.setToolTipText("<html>Clicking this button regenerates the contents of the SOAP Envelope control based on the other controls,<br> discarding any changes that may have been made.</html>");
        generateEnvelope.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateEnvelopeActionPerformed(evt);
            }
        });

        channelNames.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        channelNames.setToolTipText("Select None to ignore the response from the web service method, or select a channel to send to as a new inbound message.");

        URL1.setText("Send Response to:");

        rotateMessages.setBackground(new java.awt.Color(255, 255, 255));
        rotateMessages.setText("Rotate Messages in Queue");
        rotateMessages.setToolTipText("<html>If checked, upon unsuccessful re-try, it will rotate and put the queued message to the back of the queue<br> in order to prevent it from clogging the queue and to let the other subsequent messages in queue be processed.<br>If the order of messages processed is important, this should be unchecked.</html>");

        usePersistentQueuesNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        usePersistentQueuesNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        userPersistentQueuesButtonGroup.add(usePersistentQueuesNoRadio);
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
        userPersistentQueuesButtonGroup.add(usePersistentQueuesYesRadio);
        usePersistentQueuesYesRadio.setText("Yes");
        usePersistentQueuesYesRadio.setToolTipText("<html>If checked, the connector will store any messages that are unable to be successfully processed in a file-based queue.<br>Messages will be automatically resent until the queue is manually cleared or the message is successfully sent.<br>The default queue location is (Mirth Directory)/.mule/queuestore/(ChannelID),<br> where (Mirth Directory) is the main Mirth install root and (ChannelID) is the unique id of the current channel.</html>");
        usePersistentQueuesYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        usePersistentQueuesYesRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usePersistentQueuesYesRadioActionPerformed(evt);
            }
        });

        reconnectIntervalLabel.setText("Reconnect Interval (ms):");

        jLabel36.setText("Use Persistent Queues:");

        browseWSDLfileButton.setText("Browse...");
        browseWSDLfileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseWSDLfileButtonActionPerformed(evt);
            }
        });

        attachmentsLabel.setText("Attachments:");

        attachmentsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Content", "MIME type"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        attachmentsTable.setToolTipText("Request variables are encoded as x=y pairs as part of the request URL, separated from it by a '?' and from each other by an '&'.");
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
        authenticationYesRadio.setToolTipText("Connects to the file anonymously instead of using a username and password.");
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
        authenticationNoRadio.setToolTipText("Connects to the file using a username and password instead of anonymously.");
        authenticationNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        authenticationNoRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                authenticationNoRadioActionPerformed(evt);
            }
        });

        usernameLabel.setText("Username:");

        usernameField.setToolTipText("The user name used to gain access to the server.");

        passwordField.setToolTipText("The password used to gain access to the server.");

        passwordLabel.setText("Password:");

        invocationTypeLabel.setText("Invocation Type:");

        invocationTwoWayRadio.setBackground(new java.awt.Color(255, 255, 255));
        invocationTwoWayRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        invocationButtonGroup.add(invocationTwoWayRadio);
        invocationTwoWayRadio.setText("Two-Way");
        invocationTwoWayRadio.setToolTipText("Connects to the file anonymously instead of using a username and password.");
        invocationTwoWayRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        invocationOneWayRadio.setBackground(new java.awt.Color(255, 255, 255));
        invocationOneWayRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        invocationButtonGroup.add(invocationOneWayRadio);
        invocationOneWayRadio.setText("One-Way");
        invocationOneWayRadio.setToolTipText("Connects to the file using a username and password instead of anonymously.");
        invocationOneWayRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        useMtomLabel.setText("Use MTOM:");

        useMtomYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        useMtomYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        useMtomButtonGroup.add(useMtomYesRadio);
        useMtomYesRadio.setText("Yes");
        useMtomYesRadio.setToolTipText("Connects to the file anonymously instead of using a username and password.");
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
        useMtomNoRadio.setToolTipText("Connects to the file using a username and password instead of anonymously.");
        useMtomNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useMtomNoRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useMtomNoRadioActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, useMtomLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, attachmentsLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel4)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel1)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, invocationTypeLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, reconnectIntervalLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel36)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, URL1)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, passwordLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, usernameLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, authenticationLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, portLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, serviceLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, URL))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(authenticationYesRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(authenticationNoRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(wsdlUrl, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseWSDLfileButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(getOperationsButton))
                    .add(serviceField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                    .add(portField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                    .add(usernameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(passwordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(operationComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 186, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(generateEnvelope))
                            .add(reconnectInterval, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 75, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layout.createSequentialGroup()
                                .add(usePersistentQueuesYesRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(usePersistentQueuesNoRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(rotateMessages, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(channelNames, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 250, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(64, 64, 64))
                    .add(soapEnvelope, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(invocationTwoWayRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(invocationOneWayRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(attachmentsPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(newButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(deleteButton)))
                    .add(layout.createSequentialGroup()
                        .add(useMtomYesRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(useMtomNoRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(URL)
                    .add(getOperationsButton)
                    .add(browseWSDLfileButton)
                    .add(wsdlUrl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(serviceLabel)
                    .add(serviceField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(8, 8, 8)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(portLabel)
                    .add(portField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(5, 5, 5)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(authenticationLabel)
                    .add(authenticationYesRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(authenticationNoRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(usernameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(usernameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(passwordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(passwordLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(channelNames, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(URL1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel36)
                    .add(usePersistentQueuesYesRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(usePersistentQueuesNoRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(rotateMessages, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(reconnectIntervalLabel)
                    .add(reconnectInterval, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(invocationTypeLabel)
                    .add(invocationTwoWayRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(invocationOneWayRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(operationComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(generateEnvelope))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(soapEnvelope, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                    .add(jLabel4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(useMtomLabel)
                    .add(useMtomYesRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(useMtomNoRadio, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(attachmentsLabel)
                    .add(layout.createSequentialGroup()
                        .add(newButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(deleteButton))
                    .add(attachmentsPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

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

private void browseWSDLfileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseWSDLfileButtonActionPerformed
	File wsdlXMLfile = parent.importFile("WSDL");
	
	if (wsdlXMLfile != null) {
		wsdlUrl.setText(wsdlXMLfile.getPath());
	}
}//GEN-LAST:event_browseWSDLfileButtonActionPerformed

private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
    ((DefaultTableModel) attachmentsTable.getModel()).addRow(new Object[] { getNewAttachmentName(attachmentsTable), "" });
    attachmentsTable.setRowSelectionInterval(attachmentsTable.getRowCount() - 1, attachmentsTable.getRowCount() - 1);
    parent.enableSave();
}//GEN-LAST:event_newButtonActionPerformed

private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
    if (getSelectedRow(attachmentsTable) != -1 && !attachmentsTable.isEditing()) {
        ((DefaultTableModel) attachmentsTable.getModel()).removeRow(getSelectedRow(attachmentsTable));

        if (attachmentsTable.getRowCount() != 0) {
            if (attachmentsLastIndex == 0)
                attachmentsTable.setRowSelectionInterval(0, 0);
            else if (attachmentsLastIndex == attachmentsTable.getRowCount())
                attachmentsTable.setRowSelectionInterval(attachmentsLastIndex - 1, attachmentsLastIndex - 1);
            else
                attachmentsTable.setRowSelectionInterval(attachmentsLastIndex, attachmentsLastIndex);
        }

        parent.enableSave();
    }
}//GEN-LAST:event_deleteButtonActionPerformed

private void getOperationsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getOperationsButtonActionPerformed
    parent.setWorking("Getting operations...", true);

    SwingWorker worker = new SwingWorker<Void, Void>()
    {
        private List<String> loadedMethods = null;
        private String serviceName = null;
        private String portName = null;
        
        public Void doInBackground()
        {
            if (cacheWsdl()) {
                loadedMethods = (List<String>) invokeConnectorService("getOperations");
                serviceName = (String) invokeConnectorService("getService");
                portName = (String) invokeConnectorService("getPort");
            }
            
            return null;
        }

        public void done()
        {
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
            
            parent.enableSave();
            
            parent.setWorking("", false);
        }
    };
    worker.execute();
}//GEN-LAST:event_getOperationsButtonActionPerformed

private void generateEnvelopeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateEnvelopeActionPerformed
    if (soapEnvelope.getText().length() > 0) {
        if (!parent.alertOkCancel(parent, "This will replace your current SOAP envelope with a generated envelope. Press OK to continue.")) {
            return;
        }
    }

    parent.setWorking("Generating envelope...", true);

    SwingWorker worker = new SwingWorker<Void, Void>()
    {
        private String generatedEnvelope = null;

        public Void doInBackground()
        {
            if (!isWsdlCached()) {
                parent.alertInformation(parent, "The WSDL is no longer cached on the server. Press \"Get Operations\" to fetch the latest WSDL.");
            } else {
                generatedEnvelope = (String) invokeConnectorService("generateEnvelope", "operation", (String)operationComboBox.getSelectedItem());
            }
            
            return null;
        }

        public void done()
        {
            if (generatedEnvelope != null) {
                soapEnvelope.setText(generatedEnvelope);
                parent.enableSave();
            }

            parent.setWorking("", false);
        }
    };
    worker.execute();
}//GEN-LAST:event_generateEnvelopeActionPerformed

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
    deleteButton.setEnabled(true);
}//GEN-LAST:event_useMtomYesRadioActionPerformed

private void useMtomNoRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useMtomNoRadioActionPerformed
    attachmentsLabel.setEnabled(false);
    attachmentsPane.setEnabled(false);
    attachmentsTable.setEnabled(false);
    newButton.setEnabled(false);
    deleteButton.setEnabled(false);
}//GEN-LAST:event_useMtomNoRadioActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel URL;
    private javax.swing.JLabel URL1;
    private javax.swing.JLabel attachmentsLabel;
    private javax.swing.JScrollPane attachmentsPane;
    private com.webreach.mirth.client.ui.components.MirthTable attachmentsTable;
    private javax.swing.ButtonGroup authenticationButtonGroup;
    private javax.swing.JLabel authenticationLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton authenticationNoRadio;
    private com.webreach.mirth.client.ui.components.MirthRadioButton authenticationYesRadio;
    private javax.swing.JButton browseWSDLfileButton;
    private com.webreach.mirth.client.ui.components.MirthComboBox channelNames;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton generateEnvelope;
    private javax.swing.JButton getOperationsButton;
    private javax.swing.ButtonGroup invocationButtonGroup;
    private com.webreach.mirth.client.ui.components.MirthRadioButton invocationOneWayRadio;
    private com.webreach.mirth.client.ui.components.MirthRadioButton invocationTwoWayRadio;
    private javax.swing.JLabel invocationTypeLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JButton newButton;
    private com.webreach.mirth.client.ui.components.MirthComboBox operationComboBox;
    private com.webreach.mirth.client.ui.components.MirthPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField portField;
    private javax.swing.JLabel portLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField reconnectInterval;
    private javax.swing.JLabel reconnectIntervalLabel;
    private com.webreach.mirth.client.ui.components.MirthCheckBox rotateMessages;
    private com.webreach.mirth.client.ui.components.MirthTextField serviceField;
    private javax.swing.JLabel serviceLabel;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea soapEnvelope;
    private javax.swing.ButtonGroup useMtomButtonGroup;
    private javax.swing.JLabel useMtomLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton useMtomNoRadio;
    private com.webreach.mirth.client.ui.components.MirthRadioButton useMtomYesRadio;
    private com.webreach.mirth.client.ui.components.MirthRadioButton usePersistentQueuesNoRadio;
    private com.webreach.mirth.client.ui.components.MirthRadioButton usePersistentQueuesYesRadio;
    private javax.swing.ButtonGroup userPersistentQueuesButtonGroup;
    private com.webreach.mirth.client.ui.components.MirthTextField usernameField;
    private javax.swing.JLabel usernameLabel;
    private com.webreach.mirth.client.ui.components.MirthTextField wsdlUrl;
    // End of variables declaration//GEN-END:variables

}
