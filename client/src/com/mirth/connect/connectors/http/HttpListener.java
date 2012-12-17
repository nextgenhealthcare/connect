/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.net.URI;
import java.util.EventObject;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.TextFieldCellEditor;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.client.ui.panels.connectors.ListenerSettingsPanel;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;

public class HttpListener extends ConnectorSettingsPanel {

    private final int NAME_COLUMN = 0;
    private final int VALUE_COLUMN = 1;
    private final String NAME_COLUMN_NAME = "Name";
    private final String VALUE_COLUMN_NAME = "Value";
    private int responseHeadersLastIndex = -1;
    
    private Frame parent;

    public HttpListener() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        httpUrlField.setEditable(false);
        parent.setupCharsetEncodingForConnector(charsetEncodingCombobox);
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

        properties.setBodyOnly(messageContentBodyOnlyRadio.isSelected());

        properties.setResponseContentType(responseContentTypeField.getText());
        properties.setCharset(parent.getSelectedEncodingForConnector(charsetEncodingCombobox));

        properties.setResponseStatusCode(responseStatusCodeField.getText());
        
        properties.setResponseHeaders(getResponseHeaders());

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        HttpReceiverProperties props = (HttpReceiverProperties) properties;

        contextPathField.setText(props.getContextPath());
        receiveTimeoutField.setText(props.getTimeout());

        updateHttpUrl();

        if (props.isBodyOnly()) {
            messageContentBodyOnlyRadio.setSelected(true);
        } else {
            messageContentHeadersQueryAndBodyRadio.setSelected(true);
        }

        responseContentTypeField.setText(props.getResponseContentType());

        parent.setPreviousSelectedEncodingForConnector(charsetEncodingCombobox, props.getCharset());

        responseStatusCodeField.setText(props.getResponseStatusCode());
        
        if (props.getResponseHeaders() != null) {
            setResponseHeaders(props.getResponseHeaders());
        } else {
            setResponseHeaders(new LinkedHashMap<String, String>());
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
        if (!props.getResponseConnectorProperties().getResponseVariable().equalsIgnoreCase("None")) {
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
    public boolean requiresXmlDataType() {
        return !((HttpReceiverProperties) getProperties()).isBodyOnly();
    }

    public void updateHttpUrl() {
        String server = "<server ip>";
        try {
            server = new URI(PlatformUI.SERVER_NAME).getHost();
        } catch (Exception e) {
            // ignore exceptions getting the server ip
        }

        // Display: http://server:port/contextpath/
        httpUrlField.setText("http://" + server + ":" + ((HttpReceiverProperties) getFilledProperties()).getListenerConnectorProperties().getPort() + (contextPathField.getText().startsWith("/") ? "" : "/") + contextPathField.getText() + ((StringUtils.isBlank(contextPathField.getText()) || contextPathField.getText().endsWith("/")) ? "" : "/"));
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

        responseHeadersTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[]{NAME_COLUMN_NAME, VALUE_COLUMN_NAME}) {

            boolean[] canEdit = new boolean[]{true, true};

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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        listenerAddressButtonGroup = new javax.swing.ButtonGroup();
        includeHeadersGroup = new javax.swing.ButtonGroup();
        messageContentBodyOnlyRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        messageContentHeadersQueryAndBodyRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
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

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        messageContentBodyOnlyRadio.setBackground(new java.awt.Color(255, 255, 255));
        messageContentBodyOnlyRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        includeHeadersGroup.add(messageContentBodyOnlyRadio);
        messageContentBodyOnlyRadio.setText("Body Only");
        messageContentBodyOnlyRadio.setToolTipText("<html>If selected, the message content will only include the body as a string.</html>");
        messageContentBodyOnlyRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        messageContentBodyOnlyRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                messageContentBodyOnlyRadioActionPerformed(evt);
            }
        });

        messageContentHeadersQueryAndBodyRadio.setBackground(new java.awt.Color(255, 255, 255));
        messageContentHeadersQueryAndBodyRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        includeHeadersGroup.add(messageContentHeadersQueryAndBodyRadio);
        messageContentHeadersQueryAndBodyRadio.setText("Headers, Query, and Body as XML");
        messageContentHeadersQueryAndBodyRadio.setToolTipText("<html>If selected, the message content will include the request headers, query parameters, and body as XML.</html>");
        messageContentHeadersQueryAndBodyRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        messageContentHeadersQueryAndBodyRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                messageContentHeadersQueryAndBodyRadioActionPerformed(evt);
            }
        });

        messageContentLabel.setText("Message Content:");

        responseContentTypeField.setToolTipText("The MIME type to be used for the response.");

        responseContentTypeLabel.setText("Response Content Type:");

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "default", "utf-8", "iso-8859-1", "utf-16 (le)", "utf-16 (be)", "utf-16 (bom)", "us-ascii" }));
        charsetEncodingCombobox.setToolTipText("<html>Select the character set encoding to be used for the response to the sending system.<br>Set to Default to assume the default character set encoding for the JVM running Mirth.</html>");

        charsetEncodingLabel.setText("Charset Encoding:");

        contextPathLabel.setText("Context Path:");

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(headersLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(httpUrlLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(contextPathLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(charsetEncodingLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(receiveTimeoutLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(responseContentTypeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(receiveTimeoutLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(messageContentLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(httpUrlField, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(receiveTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contextPathField, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(responseContentTypeField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(responseStatusCodeField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(responseHeadersPane, javax.swing.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(messageContentBodyOnlyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(messageContentHeadersQueryAndBodyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(responseHeadersNewButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(responseHeadersDeleteButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                    .addComponent(messageContentHeadersQueryAndBodyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(messageContentBodyOnlyRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(httpUrlField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(httpUrlLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(responseContentTypeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(responseContentTypeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(charsetEncodingLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(responseStatusCodeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(receiveTimeoutLabel1))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(headersLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(responseHeadersNewButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(responseHeadersDeleteButton))
                    .addComponent(responseHeadersPane, javax.swing.GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void messageContentHeadersQueryAndBodyRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_messageContentHeadersQueryAndBodyRadioActionPerformed
        parent.channelEditPanel.checkAndSetXmlDataType();
    }//GEN-LAST:event_messageContentHeadersQueryAndBodyRadioActionPerformed

    private void messageContentBodyOnlyRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_messageContentBodyOnlyRadioActionPerformed
        parent.channelEditPanel.checkAndSetXmlDataType();
    }//GEN-LAST:event_messageContentBodyOnlyRadioActionPerformed

    private void contextPathFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_contextPathFieldKeyReleased
        updateHttpUrl();
    }//GEN-LAST:event_contextPathFieldKeyReleased

    private void responseHeadersNewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_responseHeadersNewButtonActionPerformed
        ((DefaultTableModel) responseHeadersTable.getModel()).addRow(new Object[]{getNewPropertyName(responseHeadersTable), ""});
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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.mirth.connect.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private javax.swing.JLabel charsetEncodingLabel;
    private com.mirth.connect.client.ui.components.MirthTextField contextPathField;
    private javax.swing.JLabel contextPathLabel;
    private javax.swing.JLabel headersLabel;
    private javax.swing.JTextField httpUrlField;
    private javax.swing.JLabel httpUrlLabel;
    private javax.swing.ButtonGroup includeHeadersGroup;
    private javax.swing.ButtonGroup listenerAddressButtonGroup;
    private com.mirth.connect.client.ui.components.MirthRadioButton messageContentBodyOnlyRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton messageContentHeadersQueryAndBodyRadio;
    private javax.swing.JLabel messageContentLabel;
    private com.mirth.connect.client.ui.components.MirthTextField receiveTimeoutField;
    private javax.swing.JLabel receiveTimeoutLabel;
    private javax.swing.JLabel receiveTimeoutLabel1;
    private com.mirth.connect.client.ui.components.MirthTextField responseContentTypeField;
    private javax.swing.JLabel responseContentTypeLabel;
    private javax.swing.JButton responseHeadersDeleteButton;
    private javax.swing.JButton responseHeadersNewButton;
    private javax.swing.JScrollPane responseHeadersPane;
    private com.mirth.connect.client.ui.components.MirthTable responseHeadersTable;
    private com.mirth.connect.client.ui.components.MirthTextField responseStatusCodeField;
    // End of variables declaration//GEN-END:variables
}
