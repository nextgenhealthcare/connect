/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package com.mirth.connect.connectors.uima;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.connectors.ConnectorClass;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.plugins.uima.UimaService;
import com.mirth.connect.plugins.uima.model.UimaPipeline;
import com.mirth.connect.util.ConnectionTestResponse;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class UimaSender extends ConnectorClass {

    private final int NAME_COLUMN = 0;
    private final int VALUE_COLUMN = 1;
    private final String NAME_COLUMN_NAME = "Name";
    private final String VALUE_COLUMN_NAME = "Value";
    private int propertiesLastIndex = -1;
    private HashMap channelList;
    private List<UimaPipelineInfo> pipelineInfoList;

    public UimaSender() {
        name = UimaSenderProperties.name;
        initComponents();

        cpcTimeoutField.setDocument(new MirthFieldConstraints(0, false, false, true));
        casProcessTimeoutField.setDocument(new MirthFieldConstraints(0, false, false, true));

        queryParametersPane.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deselectRows(extraPropertiesTable, queryParametersDeleteButton);
            }
        });
        
        queryParametersDeleteButton.setEnabled(false);
        
        pipelineInfoList = new ArrayList<UimaPipelineInfo>();
        pipelineInfoList.add(new UimaPipelineInfo("tcp://host:port", "Select a Pipeline"));
        try {
            List<UimaPipeline> tempPluginPipelineList = (List<UimaPipeline>) parent.mirthClient.invokePluginMethod("UIMA Service", UimaService.METHOD_GET_PIPELINES, null);
            for (UimaPipeline uimaPipeline : tempPluginPipelineList) {
                pipelineInfoList.add(new UimaPipelineInfo(uimaPipeline.getJmsUrl(), uimaPipeline.getName()));
            }
        } catch (ClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        properties.put(UimaSenderProperties.DATATYPE, name);
        properties.put(UimaSenderProperties.UIMA_HOST, "sink");
        properties.put(UimaSenderProperties.UIMA_JMS_URL, jmsUrlField.getText());

        properties.put(UimaSenderProperties.UIMA_PIPELINE, pipelineField.getText());

        
        properties.put(UimaSenderProperties.UIMA_META_TIMEOUT, metaTimeoutField.getText());
        properties.put(UimaSenderProperties.UIMA_CAS_POOL_SIZE, casPoolSizeField.getText());
        properties.put(UimaSenderProperties.UIMA_CAS_PROCESS_TIMEOUT, casProcessTimeoutField.getText());
        properties.put(UimaSenderProperties.UIMA_CPC_TIMEOUT, cpcTimeoutField.getText());

        properties.put(UimaSenderProperties.UIMA_SUCCESS_RESPONSE_CHANNEL_ID, channelList.get((String) successResponseChannelIdField.getSelectedItem()));
        properties.put(UimaSenderProperties.UIMA_ERROR_RESPONSE_CHANNEL_ID, channelList.get((String) errorResponseChannelIdField.getSelectedItem()));


        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        properties.put(UimaSenderProperties.UIMA_EXTRA_PROPERTIES, serializer.toXML(getAdditionalProperties()));
        
        properties.put(UimaSenderProperties.UIMA_TEMPLATE, templateField.getText());

        return properties;
    }

    public void setProperties(Properties props) {
        resetInvalidProperties();

        boolean enabled = parent.isSaveEnabled();

        jmsUrlField.setText((String) props.get(UimaSenderProperties.UIMA_JMS_URL));


        metaTimeoutField.setText(props.getProperty(UimaSenderProperties.UIMA_META_TIMEOUT));
        casPoolSizeField.setText(props.getProperty(UimaSenderProperties.UIMA_CAS_POOL_SIZE));
        casProcessTimeoutField.setText(props.getProperty(UimaSenderProperties.UIMA_CAS_PROCESS_TIMEOUT));
        cpcTimeoutField.setText(props.getProperty(UimaSenderProperties.UIMA_CPC_TIMEOUT));

        pipelineField.setText(props.getProperty(UimaSenderProperties.UIMA_PIPELINE));
        templateField.setText(props.getProperty(UimaSenderProperties.UIMA_TEMPLATE));

        ObjectXMLSerializer serializer = new ObjectXMLSerializer();

        if (((String) props.get(UimaSenderProperties.UIMA_EXTRA_PROPERTIES)).length() > 0) {
            setAdditionalProperties((Properties) serializer.fromXML((String) props.get(UimaSenderProperties.UIMA_EXTRA_PROPERTIES)));
        } else {
            setAdditionalProperties(new Properties());
        }



        ArrayList<String> channelNameArray = new ArrayList<String>();
        channelList = new HashMap();
        channelList.put("None", "sink");
        channelNameArray.add("None");

        String successSelectedChannelName = "None";
        String errorSelectedChannelName = "None";

        for (Channel channel : parent.channels.values()) {
            if (((String) props.get(UimaSenderProperties.UIMA_SUCCESS_RESPONSE_CHANNEL_ID)).equalsIgnoreCase(channel.getId())) {
                successSelectedChannelName = channel.getName();
            }
            if (((String) props.get(UimaSenderProperties.UIMA_ERROR_RESPONSE_CHANNEL_ID)).equalsIgnoreCase(channel.getId())) {
                errorSelectedChannelName = channel.getName();
            }

            channelList.put(channel.getName(), channel.getId());
            channelNameArray.add(channel.getName());
        }
        successResponseChannelIdField.setModel(new javax.swing.DefaultComboBoxModel(channelNameArray.toArray()));
        successResponseChannelIdField.setSelectedItem(successSelectedChannelName);

        errorResponseChannelIdField.setModel(new javax.swing.DefaultComboBoxModel(channelNameArray.toArray()));
        errorResponseChannelIdField.setSelectedItem(errorSelectedChannelName);
        
        
       
        pipelinesListField.setModel(new javax.swing.DefaultComboBoxModel(pipelineInfoList.toArray()));
        pipelinesListField.setSelectedItem(pipelineInfoList.get(0));

        parent.setSaveEnabled(enabled);
    }

    public Properties getDefaults() {
        return new UimaSenderProperties().getDefaults();
    }

    public void setAdditionalProperties(Properties properties) {
        Object[][] tableData = new Object[properties.size()][2];

        extraPropertiesTable = new MirthTable();

        int j = 0;
        Iterator i = properties.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            tableData[j][NAME_COLUMN] = (String) entry.getKey();
            tableData[j][VALUE_COLUMN] = (String) entry.getValue();
            j++;
        }

        extraPropertiesTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[]{NAME_COLUMN_NAME, VALUE_COLUMN_NAME}) {

            boolean[] canEdit = new boolean[]{true, true};

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        extraPropertiesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                if (getSelectedRow(extraPropertiesTable) != -1) {
                    propertiesLastIndex = getSelectedRow(extraPropertiesTable);
                    queryParametersDeleteButton.setEnabled(true);
                } else {
                    queryParametersDeleteButton.setEnabled(false);
                }
            }
        });

        class UimaTableCellEditor extends AbstractCellEditor implements TableCellEditor {

            JComponent component = new JTextField();
            Object originalValue;
            boolean checkProperties;

            public UimaTableCellEditor(boolean checkProperties) {
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
                    parent.setSaveEnabled(true);
                }

                queryParametersDeleteButton.setEnabled(true);

                return super.stopCellEditing();
            }

            public boolean checkUniqueProperty(String property) {
                boolean exists = false;

                for (int i = 0; i < extraPropertiesTable.getRowCount(); i++) {
                    if (extraPropertiesTable.getValueAt(i, NAME_COLUMN) != null && ((String) extraPropertiesTable.getValueAt(i, NAME_COLUMN)).equalsIgnoreCase(property)) {
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
                    queryParametersDeleteButton.setEnabled(false);
                    return true;
                }
                return false;
            }
        }
        ;

        // Set the custom cell editor for the Destination Name column.
        extraPropertiesTable.getColumnModel().getColumn(extraPropertiesTable.getColumnModel().getColumnIndex(NAME_COLUMN_NAME)).setCellEditor(new UimaTableCellEditor(true));

        // Set the custom cell editor for the Destination Name column.
        extraPropertiesTable.getColumnModel().getColumn(extraPropertiesTable.getColumnModel().getColumnIndex(VALUE_COLUMN_NAME)).setCellEditor(new UimaTableCellEditor(false));

        extraPropertiesTable.setSelectionMode(0);
        extraPropertiesTable.setRowSelectionAllowed(true);
        extraPropertiesTable.setRowHeight(UIConstants.ROW_HEIGHT);
        extraPropertiesTable.setDragEnabled(false);
        extraPropertiesTable.setOpaque(true);
        extraPropertiesTable.setSortable(false);
        extraPropertiesTable.getTableHeader().setReorderingAllowed(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            extraPropertiesTable.setHighlighters(highlighter);
        }

        queryParametersPane.setViewportView(extraPropertiesTable);
    }


    public Map getAdditionalProperties() {
        Properties properties = new Properties();

        for (int i = 0; i < extraPropertiesTable.getRowCount(); i++) {
            if (((String) extraPropertiesTable.getValueAt(i, NAME_COLUMN)).length() > 0) {
                properties.put(((String) extraPropertiesTable.getValueAt(i, NAME_COLUMN)), ((String) extraPropertiesTable.getValueAt(i, VALUE_COLUMN)));
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

    public boolean checkProperties(Properties props, boolean highlight) {
        resetInvalidProperties();
        boolean valid = true;

        if (((String) props.getProperty(UimaSenderProperties.UIMA_JMS_URL)).length() == 0) {
            valid = false;
            if (highlight) {
                jmsUrlField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (((String) props.getProperty(UimaSenderProperties.UIMA_PIPELINE)).length() == 0) {
            valid = false;
            if (highlight) {
                pipelineField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        
        if (((String) props.get(UimaSenderProperties.UIMA_CAS_PROCESS_TIMEOUT)).length() == 0) {
            valid = false;
            if (highlight) {
                casProcessTimeoutField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        
        if (((String) props.get(UimaSenderProperties.UIMA_CAS_POOL_SIZE)).length() == 0) {
            valid = false;
            if (highlight) {
                casPoolSizeField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        
        if (((String) props.get(UimaSenderProperties.UIMA_TEMPLATE)).length() == 0) {
            valid = false;
            if (highlight) {
                templateField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        


        return valid;
    }

    private void resetInvalidProperties() {
        jmsUrlField.setBackground(null);
        pipelineField.setBackground(null);
        casProcessTimeoutField.setBackground(null);
        cpcTimeoutField.setBackground(null);
        templateField.setBackground(null);
    }

    public String doValidate(Properties props, boolean highlight) {
        String error = null;

        if (!checkProperties(props, highlight)) {
            error = "Error in the form for connector \"" + getName() + "\".\n\n";
        }

        return error;
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
        usePersistantQueuesButtonGroup = new javax.swing.ButtonGroup();
        multipartButtonGroup = new javax.swing.ButtonGroup();
        authenticationButtonGroup = new javax.swing.ButtonGroup();
        authenticationTypeButtonGroup = new javax.swing.ButtonGroup();
        urlLabel = new javax.swing.JLabel();
        jmsUrlField = new com.mirth.connect.client.ui.components.MirthTextField();
        queryParametersNewButton = new javax.swing.JButton();
        queryParametersDeleteButton = new javax.swing.JButton();
        queryParametersPane = new javax.swing.JScrollPane();
        extraPropertiesTable = new com.mirth.connect.client.ui.components.MirthTable();
        queryParametersLabel = new javax.swing.JLabel();
        successResponseChannelIdField = new com.mirth.connect.client.ui.components.MirthComboBox();
        URL1 = new javax.swing.JLabel();
        reconnectIntervalLabel = new javax.swing.JLabel();
        cpcTimeoutField = new com.mirth.connect.client.ui.components.MirthTextField();
        testConnection = new javax.swing.JButton();
        templateField = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea(true,false);
        contentLabel = new javax.swing.JLabel();
        metaTimeoutField = new com.mirth.connect.client.ui.components.MirthTextField();
        usernameLabel = new javax.swing.JLabel();
        passwordLabel = new javax.swing.JLabel();
        pipelineField = new com.mirth.connect.client.ui.components.MirthTextField();
        sendTimeoutLabel = new javax.swing.JLabel();
        casProcessTimeoutField = new com.mirth.connect.client.ui.components.MirthTextField();
        queuePollIntervalLabel = new javax.swing.JLabel();
        URL2 = new javax.swing.JLabel();
        errorResponseChannelIdField = new com.mirth.connect.client.ui.components.MirthComboBox();
        casPoolSizeField = new com.mirth.connect.client.ui.components.MirthTextField();
        pipelinesListField = new com.mirth.connect.client.ui.components.MirthComboBox();
        pipelinesListLabel = new javax.swing.JLabel();
        selectPipelinesButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        urlLabel.setText("JMS Broker URL:");

        jmsUrlField.setToolTipText("Enter the URL of the HTTP server to send each message to.");

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
/*
        extraPropertiesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Value"
            }
        ));
        extraPropertiesTable.setToolTipText("Query parameters are encoded as x=y pairs as part of the request URL, separated from it by a '?' and from each other by an '&'.");
        */
        queryParametersPane.setViewportView(extraPropertiesTable);

        queryParametersLabel.setText("Properties:");

        successResponseChannelIdField.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        successResponseChannelIdField.setToolTipText("<html>Selects a channel to send the response from the HTTP server as a new inbound message<br> or None to ignore the response from the HTTP server.</html>");

        URL1.setText("Send Success Response to:");

        reconnectIntervalLabel.setText("CPC Timeout (ms):");

        cpcTimeoutField.setToolTipText("<html>The amount of time that should elapse between retry attempts to send messages in the queue.</html>");

        testConnection.setText("Test Connection");
        testConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testConnectionActionPerformed(evt);
            }
        });

        templateField.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        templateField.setToolTipText("The HTTP message body.");

        contentLabel.setText("Message:");

        metaTimeoutField.setToolTipText("The username used to connect to the HTTP server.");

        usernameLabel.setText("Meta Timeout (ms):");

        passwordLabel.setText("CAS Pool Size:");

        pipelineField.setToolTipText("<html>Sets the socket timeout (SO_TIMEOUT) in milliseconds to be used when executing the method.<br>A timeout value of zero is interpreted as an infinite timeout.</html>");

        sendTimeoutLabel.setText("Pipeline:");

        casProcessTimeoutField.setToolTipText("<html>The amount of time that should elapse between polls of an empty queue to check for queued messages.</html>");

        queuePollIntervalLabel.setText("Process Timeout (ms):");

        URL2.setText("Send Error Response to:");

        errorResponseChannelIdField.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        errorResponseChannelIdField.setToolTipText("<html>Selects a channel to send the response from the HTTP server as a new inbound message<br> or None to ignore the response from the HTTP server.</html>");

        casPoolSizeField.setToolTipText("The username used to connect to the HTTP server.");

        pipelinesListField.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        pipelinesListField.setToolTipText("<html>Selects a channel to send the response from the HTTP server as a new inbound message<br> or None to ignore the response from the HTTP server.</html>");

        pipelinesListLabel.setText("Pipelines:");

        selectPipelinesButton.setText("Select");
        selectPipelinesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectPipelinesButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(contentLabel)
                    .addComponent(passwordLabel)
                    .addComponent(usernameLabel)
                    .addComponent(reconnectIntervalLabel)
                    .addComponent(queuePollIntervalLabel)
                    .addComponent(URL1)
                    .addComponent(sendTimeoutLabel)
                    .addComponent(urlLabel)
                    .addComponent(URL2)
                    .addComponent(queryParametersLabel)
                    .addComponent(pipelinesListLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pipelinesListField, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(selectPipelinesButton)
                        .addGap(86, 86, 86))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(templateField, javax.swing.GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE)
                            .addContainerGap())
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(casProcessTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(errorResponseChannelIdField, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(successResponseChannelIdField, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(pipelineField, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jmsUrlField, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(testConnection)
                            .addGap(14, 14, 14))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addComponent(queryParametersPane, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(queryParametersDeleteButton)
                                .addComponent(queryParametersNewButton))
                            .addContainerGap())
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(casPoolSizeField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(metaTimeoutField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cpcTimeoutField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE))
                            .addContainerGap()))))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {queryParametersDeleteButton, queryParametersNewButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pipelinesListField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pipelinesListLabel)
                    .addComponent(selectPipelinesButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(urlLabel)
                    .addComponent(jmsUrlField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(testConnection))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sendTimeoutLabel)
                    .addComponent(pipelineField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(URL1)
                    .addComponent(successResponseChannelIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(errorResponseChannelIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(URL2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(queuePollIntervalLabel)
                    .addComponent(casProcessTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reconnectIntervalLabel)
                    .addComponent(cpcTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameLabel)
                    .addComponent(metaTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(casPoolSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(contentLabel)
                    .addComponent(templateField, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(queryParametersNewButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(queryParametersDeleteButton))
                    .addComponent(queryParametersPane, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                    .addComponent(queryParametersLabel))
                .addGap(167, 167, 167))
        );
    }// </editor-fold>//GEN-END:initComponents

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

private void queryParametersDeleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_queryParametersDeleteButtonActionPerformed
    if (getSelectedRow(extraPropertiesTable) != -1 && !extraPropertiesTable.isEditing()) {
        ((DefaultTableModel) extraPropertiesTable.getModel()).removeRow(getSelectedRow(extraPropertiesTable));

        if (extraPropertiesTable.getRowCount() != 0) {
            if (propertiesLastIndex == 0) {
                extraPropertiesTable.setRowSelectionInterval(0, 0);
            } else if (propertiesLastIndex == extraPropertiesTable.getRowCount()) {
                extraPropertiesTable.setRowSelectionInterval(propertiesLastIndex - 1, propertiesLastIndex - 1);
            } else {
                extraPropertiesTable.setRowSelectionInterval(propertiesLastIndex, propertiesLastIndex);
            }
        }

        parent.setSaveEnabled(true);
    }
}//GEN-LAST:event_queryParametersDeleteButtonActionPerformed

private void queryParametersNewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_queryParametersNewButtonActionPerformed
    ((DefaultTableModel) extraPropertiesTable.getModel()).addRow(new Object[]{getNewPropertyName(extraPropertiesTable), ""});
    extraPropertiesTable.setRowSelectionInterval(extraPropertiesTable.getRowCount() - 1, extraPropertiesTable.getRowCount() - 1);
    parent.setSaveEnabled(true);
}//GEN-LAST:event_queryParametersNewButtonActionPerformed

private void selectPipelinesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectPipelinesButtonActionPerformed
    if (!pipelineField.getText().equals("")) {
        if (!parent.alertOption(parent, "Are you sure you want to replace your current connection info?")) {
            return;
        }
    }

    UimaPipelineInfo pipelineInfo = (UimaPipelineInfo) pipelinesListField.getSelectedItem();
    
    pipelineField.setText(pipelineInfo.getPipeline());
    jmsUrlField.setText(pipelineInfo.getJmsUrl());
    jmsUrlField.grabFocus();
    parent.setSaveEnabled(true);
}//GEN-LAST:event_selectPipelinesButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel URL1;
    private javax.swing.JLabel URL2;
    private javax.swing.ButtonGroup authenticationButtonGroup;
    private javax.swing.ButtonGroup authenticationTypeButtonGroup;
    private com.mirth.connect.client.ui.components.MirthTextField casPoolSizeField;
    private com.mirth.connect.client.ui.components.MirthTextField casProcessTimeoutField;
    private javax.swing.JLabel contentLabel;
    private com.mirth.connect.client.ui.components.MirthTextField cpcTimeoutField;
    private com.mirth.connect.client.ui.components.MirthComboBox errorResponseChannelIdField;
    private com.mirth.connect.client.ui.components.MirthTable extraPropertiesTable;
    private com.mirth.connect.client.ui.components.MirthTextField jmsUrlField;
    private com.mirth.connect.client.ui.components.MirthTextField metaTimeoutField;
    private javax.swing.ButtonGroup methodButtonGroup;
    private javax.swing.ButtonGroup multipartButtonGroup;
    private javax.swing.JLabel passwordLabel;
    private com.mirth.connect.client.ui.components.MirthTextField pipelineField;
    private com.mirth.connect.client.ui.components.MirthComboBox pipelinesListField;
    private javax.swing.JLabel pipelinesListLabel;
    private javax.swing.JButton queryParametersDeleteButton;
    private javax.swing.JLabel queryParametersLabel;
    private javax.swing.JButton queryParametersNewButton;
    private javax.swing.JScrollPane queryParametersPane;
    private javax.swing.JLabel queuePollIntervalLabel;
    private javax.swing.JLabel reconnectIntervalLabel;
    private javax.swing.ButtonGroup responseHeadersButtonGroup;
    private javax.swing.JButton selectPipelinesButton;
    private javax.swing.JLabel sendTimeoutLabel;
    private com.mirth.connect.client.ui.components.MirthComboBox successResponseChannelIdField;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea templateField;
    private javax.swing.JButton testConnection;
    private javax.swing.JLabel urlLabel;
    private javax.swing.ButtonGroup usePersistantQueuesButtonGroup;
    private javax.swing.JLabel usernameLabel;
    // End of variables declaration//GEN-END:variables
}
