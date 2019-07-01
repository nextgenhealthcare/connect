/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.smtp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.CharsetEncodingInformation;
import com.mirth.connect.client.ui.ConnectorTypeDecoration;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.TextFieldCellEditor;
import com.mirth.connect.client.ui.UIConstants;
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

public class SmtpSender extends ConnectorSettingsPanel {

    private final int HEADERS_NAME_COLUMN = 0;
    private final int HEADERS_VALUE_COLUMN = 1;
    private final String HEADERS_NAME_COLUMN_NAME = "Name";
    private final String HEADERS_VALUE_COLUMN_NAME = "Value";
    private final int ATTACHMENTS_NAME_COLUMN = 0;
    private final int ATTACHMENTS_CONTENT_COLUMN = 1;
    private final int ATTACHMENTS_MIME_TYPE_COLUMN = 2;
    private final String ATTACHMENTS_NAME_COLUMN_NAME = "Name";
    private final String ATTACHMENTS_CONTENT_COLUMN_NAME = "Content";
    private final String ATTACHMENTS_MIME_TYPE_COLUMN_NAME = "MIME type";
    private int headersLastIndex = -1;
    private int attachmentsLastIndex = -1;
    private String errors;

    private Frame parent;

    public SmtpSender() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        initToolTips();
        initLayout();

        parent.setupCharsetEncodingForConnector(charsetEncodingComboBox);
    }

    @Override
    public String getConnectorName() {
        return new SmtpDispatcherProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        SmtpDispatcherProperties properties = new SmtpDispatcherProperties();

        properties.setSmtpHost(smtpHostField.getText());
        properties.setSmtpPort(smtpPortField.getText());
        properties.setOverrideLocalBinding(overrideLocalBindingYesRadio.isSelected());
        properties.setLocalAddress(localAddressField.getText());
        properties.setLocalPort(localPortField.getText());
        properties.setTimeout(sendTimeoutField.getText());

        if (encryptionTls.isSelected()) {
            properties.setEncryption("TLS");
        } else if (encryptionSsl.isSelected()) {
            properties.setEncryption("SSL");
        } else {
            properties.setEncryption("none");
        }

        properties.setAuthentication(useAuthenticationYes.isSelected());

        properties.setUsername(usernameField.getText());
        properties.setPassword(new String(passwordField.getPassword()));
        properties.setTo(toField.getText());
        properties.setFrom(fromField.getText());
        properties.setSubject(subjectField.getText());

        properties.setCharsetEncoding(parent.getSelectedEncodingForConnector(charsetEncodingComboBox));

        properties.setHtml(htmlYes.isSelected());

        properties.setBody(bodyTextPane.getText());
        
        properties.setUseHeadersVariable(useHeadersVariableRadio.isSelected());
        properties.setHeadersMap(getHeaders());
        properties.setHeadersVariable(headersVariableField.getText());
        
        properties.setUseAttachmentsVariable(useAttachmentsVariableRadio.isSelected());
        properties.setAttachmentsList(getAttachments());
        properties.setAttachmentsVariable(attachmentsVariableField.getText());

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        SmtpDispatcherProperties props = (SmtpDispatcherProperties) properties;

        smtpHostField.setText(props.getSmtpHost());
        smtpPortField.setText(props.getSmtpPort());
        if (props.isOverrideLocalBinding()) {
            overrideLocalBindingYesRadio.setSelected(true);
            overrideLocalBindingFieldsEnabled(true);
        } else {
            overrideLocalBindingNoRadio.setSelected(true);
            overrideLocalBindingFieldsEnabled(false);
        }
        
        if (props.isUseHeadersVariable()) {
            useHeadersVariableRadio.setSelected(true);
        } else {
            useHeadersTableRadio.setSelected(true);
        }
        headersVariableField.setText(props.getHeadersVariable());
        useHeadersVariableFieldsEnabled(props.isUseHeadersVariable());
        
        if (props.isUseAttachmentsVariable()) {
            useAttachmentsVariableRadio.setSelected(true);
        } else {
            useAttachmentsListRadio.setSelected(true);
        }
        attachmentsVariableField.setText(props.getAttachmentsVariable());
        useAttachmentsVariableFieldsEnabled(props.isUseAttachmentsVariable());

        localAddressField.setText(props.getLocalAddress());
        localPortField.setText(props.getLocalPort());
        sendTimeoutField.setText(props.getTimeout());

        if (props.getEncryption().equalsIgnoreCase("TLS")) {
            encryptionTls.setSelected(true);
        } else if (props.getEncryption().equalsIgnoreCase("SSL")) {
            encryptionSsl.setSelected(true);
        } else {
            encryptionNone.setSelected(true);
        }

        if (props.isAuthentication()) {
            setAuthenticationFieldsEnabled(true);
            useAuthenticationYes.setSelected(true);
        } else {
            setAuthenticationFieldsEnabled(false);
            useAuthenticationNo.setSelected(true);
        }

        usernameField.setText(props.getUsername());
        passwordField.setText(props.getPassword());
        toField.setText(props.getTo());
        fromField.setText(props.getFrom());
        subjectField.setText(props.getSubject());

        parent.setPreviousSelectedEncodingForConnector(charsetEncodingComboBox, props.getCharsetEncoding());

        if (props.isHtml()) {
            htmlYes.setSelected(true);
        } else {
            htmlNo.setSelected(true);
        }

        bodyTextPane.setText(props.getBody());

        setHeaders(props.getHeadersMap());

        setAttachments(props.getAttachmentsList());
    }

    @Override
    public ConnectorProperties getDefaults() {
        return new SmtpDispatcherProperties();
    }

    @Override
    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        SmtpDispatcherProperties props = (SmtpDispatcherProperties) properties;

        boolean valid = true;
        StringBuilder errors = new StringBuilder();

        if (props.getSmtpHost().length() == 0) {
            valid = false;
            if (highlight) {
                smtpHostField.setBackground(UIConstants.INVALID_COLOR);
            }

            errors.append("\"SMTP Host\" is required\n");
        }

        if (props.getSmtpPort().length() == 0) {
            valid = false;
            if (highlight) {
                smtpPortField.setBackground(UIConstants.INVALID_COLOR);
            }

            errors.append("\"SMTP Port\" is required\n");
        }

        if (props.isOverrideLocalBinding()) {
            if (props.getLocalAddress().length() <= 3) {
                valid = false;
                if (highlight) {
                    localAddressField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
            if (props.getLocalPort().length() == 0) {
                valid = false;
                if (highlight) {
                    localPortField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }

        if (props.getTimeout().length() == 0) {
            valid = false;
            if (highlight) {
                sendTimeoutField.setBackground(UIConstants.INVALID_COLOR);
            }

            errors.append("\"Send Timeout\" is required\n");
        }

        if (props.getTo().length() == 0) {
            valid = false;
            if (highlight) {
                toField.setBackground(UIConstants.INVALID_COLOR);
            }

            errors.append("\"To\" is required\n");
        }

        if (props.getFrom().length() == 0) {
            valid = false;
            if (highlight) {
                fromField.setBackground(UIConstants.INVALID_COLOR);
            }

            errors.append("\"From\" is required\n");
        }

        this.errors = errors.toString();

        return valid;
    }

    @Override
    public void resetInvalidProperties() {
        smtpHostField.setBackground(null);
        decorateConnectorType();
        smtpPortField.setBackground(null);
        sendTimeoutField.setBackground(null);
        toField.setBackground(null);
        fromField.setBackground(null);
    }

    @Override
    public ConnectorTypeDecoration getConnectorTypeDecoration() {
        return new ConnectorTypeDecoration(Mode.DESTINATION);
    }

    @Override
    public void doLocalDecoration(ConnectorTypeDecoration connectorTypeDecoration) {
        if (connectorTypeDecoration != null) {
            smtpHostField.setIcon(connectorTypeDecoration.getIcon());
            smtpHostField.setAlternateToolTipText(connectorTypeDecoration.getIconToolTipText());
            smtpHostField.setIconPopupMenuComponent(connectorTypeDecoration.getIconPopupComponent());
            smtpHostField.setBackground(connectorTypeDecoration.getHighlightColor());
        }
    }

    private void setHeaders(Map<String, String> headers) {
        Object[][] tableData = new Object[headers.size()][2];

        headersTable = new MirthTable();

        int i = 0;
        for (Entry<String, String> entry : headers.entrySet()) {
            tableData[i][HEADERS_NAME_COLUMN] = entry.getKey();
            tableData[i][HEADERS_VALUE_COLUMN] = entry.getValue();
            i++;
        }

        headersTable.setModel(new DefaultTableModel(tableData, new String[] {
                HEADERS_NAME_COLUMN_NAME, HEADERS_VALUE_COLUMN_NAME }) {

            boolean[] canEdit = new boolean[] { true, true };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        headersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                if (getSelectedRow(headersTable) != -1) {
                    headersLastIndex = getSelectedRow(headersTable);
                    deleteHeaderButton.setEnabled(true);
                } else {
                    deleteHeaderButton.setEnabled(false);
                }
            }
        });

        class HeadersTableCellEditor extends TextFieldCellEditor {

            boolean checkHeaders;

            public HeadersTableCellEditor(boolean checkHeaders) {
                super();
                this.checkHeaders = checkHeaders;
            }

            public boolean checkUniqueHeader(String headerName) {
                boolean exists = false;

                for (int i = 0; i < headersTable.getRowCount(); i++) {
                    if (headersTable.getValueAt(i, HEADERS_NAME_COLUMN) != null && ((String) headersTable.getValueAt(i, HEADERS_NAME_COLUMN)).equalsIgnoreCase(headerName)) {
                        exists = true;
                    }
                }

                return exists;
            }

            @Override
            public boolean isCellEditable(EventObject evt) {
                boolean editable = super.isCellEditable(evt);

                if (editable) {
                    deleteHeaderButton.setEnabled(false);
                }

                return editable;
            }

            @Override
            protected boolean valueChanged(String value) {
                deleteHeaderButton.setEnabled(true);

                if (checkHeaders && (value.length() == 0 || checkUniqueHeader(value))) {
                    return false;
                }

                parent.setSaveEnabled(true);
                return true;
            }
        }

        headersTable.getColumnModel().getColumn(headersTable.getColumnModel().getColumnIndex(HEADERS_NAME_COLUMN_NAME)).setCellEditor(new HeadersTableCellEditor(true));
        headersTable.getColumnModel().getColumn(headersTable.getColumnModel().getColumnIndex(HEADERS_VALUE_COLUMN_NAME)).setCellEditor(new HeadersTableCellEditor(false));
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

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new LinkedHashMap<String, String>();

        for (int i = 0; i < headersTable.getRowCount(); i++) {
            if (((String) headersTable.getValueAt(i, HEADERS_NAME_COLUMN)).length() > 0) {
                headers.put((String) headersTable.getValueAt(i, HEADERS_NAME_COLUMN), (String) headersTable.getValueAt(i, HEADERS_VALUE_COLUMN));
            }
        }

        return headers;
    }

    private void setAttachments(List<Attachment> attachments) {
        Object[][] tableData = new Object[attachments.size()][3];

        attachmentsTable = new MirthTable();

        for (int i = 0; i < attachments.size(); i++) {
            Attachment entry = attachments.get(i);
            tableData[i][ATTACHMENTS_NAME_COLUMN] = entry.getName();
            tableData[i][ATTACHMENTS_CONTENT_COLUMN] = entry.getContent();
            tableData[i][ATTACHMENTS_MIME_TYPE_COLUMN] = entry.getMimeType();
        }

        attachmentsTable.setModel(new DefaultTableModel(tableData, new String[] {
                ATTACHMENTS_NAME_COLUMN_NAME, ATTACHMENTS_CONTENT_COLUMN_NAME,
                ATTACHMENTS_MIME_TYPE_COLUMN_NAME }) {

            boolean[] canEdit = new boolean[] { true, true, true };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });

        attachmentsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                if (getSelectedRow(attachmentsTable) != -1) {
                    attachmentsLastIndex = getSelectedRow(attachmentsTable);
                    deleteAttachmentButton.setEnabled(true);
                } else {
                    deleteAttachmentButton.setEnabled(false);
                }
            }
        });

        class AttachmentsTableCellEditor extends TextFieldCellEditor {

            boolean checkAttachments;

            public AttachmentsTableCellEditor(boolean checkAttachments) {
                super();
                this.checkAttachments = checkAttachments;
            }

            public boolean checkUniqueAttachment(String attachmentName) {
                boolean exists = false;

                for (int i = 0; i < attachmentsTable.getRowCount(); i++) {
                    if (attachmentsTable.getValueAt(i, ATTACHMENTS_NAME_COLUMN) != null && ((String) attachmentsTable.getValueAt(i, ATTACHMENTS_NAME_COLUMN)).equalsIgnoreCase(attachmentName)) {
                        exists = true;
                    }
                }

                return exists;
            }

            @Override
            public boolean isCellEditable(EventObject evt) {
                boolean editable = super.isCellEditable(evt);

                if (editable) {
                    deleteAttachmentButton.setEnabled(false);
                }

                return editable;
            }

            @Override
            protected boolean valueChanged(String value) {
                deleteAttachmentButton.setEnabled(true);

                if (checkAttachments && (value.length() == 0 || checkUniqueAttachment(value))) {
                    return false;
                }

                parent.setSaveEnabled(true);
                return true;
            }
        }

        attachmentsTable.getColumnModel().getColumn(attachmentsTable.getColumnModel().getColumnIndex(ATTACHMENTS_NAME_COLUMN_NAME)).setCellEditor(new AttachmentsTableCellEditor(true));
        attachmentsTable.getColumnModel().getColumn(attachmentsTable.getColumnModel().getColumnIndex(ATTACHMENTS_CONTENT_COLUMN_NAME)).setCellEditor(new AttachmentsTableCellEditor(false));
        attachmentsTable.getColumnModel().getColumn(attachmentsTable.getColumnModel().getColumnIndex(ATTACHMENTS_MIME_TYPE_COLUMN_NAME)).setCellEditor(new AttachmentsTableCellEditor(false));
        attachmentsTable.setCustomEditorControls(true);

        attachmentsTable.setSelectionMode(0);
        attachmentsTable.setRowSelectionAllowed(true);
        attachmentsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        attachmentsTable.setDragEnabled(false);
        attachmentsTable.setOpaque(true);
        attachmentsTable.setSortable(false);
        attachmentsTable.getTableHeader().setReorderingAllowed(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            attachmentsTable.setHighlighters(highlighter);
        }

        attachmentsPane.setViewportView(attachmentsTable);
    }

    private List<Attachment> getAttachments() {
        List<Attachment> attachments = new ArrayList<Attachment>();

        for (int i = 0; i < attachmentsTable.getRowCount(); i++) {
            if (((String) attachmentsTable.getValueAt(i, ATTACHMENTS_NAME_COLUMN)).length() > 0) {
                Attachment attachment = new Attachment();
                attachment.setName((String) attachmentsTable.getValueAt(i, ATTACHMENTS_NAME_COLUMN));
                attachment.setContent((String) attachmentsTable.getValueAt(i, ATTACHMENTS_CONTENT_COLUMN));
                attachment.setMimeType((String) attachmentsTable.getValueAt(i, ATTACHMENTS_MIME_TYPE_COLUMN));
                attachments.add(attachment);
            }
        }

        return attachments;
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
    private String getNewUniqueName(MirthTable table) {
        String tempName;
        int nameColumn;

        if (table == attachmentsTable) {
            tempName = "Attachment ";
            nameColumn = ATTACHMENTS_NAME_COLUMN;
        } else if (table == headersTable) {
            tempName = "Header ";
            nameColumn = HEADERS_NAME_COLUMN;
        } else {
            return null;
        }

        for (int i = 1; i <= table.getRowCount() + 1; i++) {
            boolean exists = false;
            for (int j = 0; j < table.getRowCount(); j++) {
                if (((String) table.getValueAt(j, nameColumn)).equalsIgnoreCase(tempName + i)) {
                    exists = true;
                }
            }
            if (!exists) {
                return tempName + i;
            }
        }
        return "";
    }

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);

        smtpHostLabel = new JLabel("SMTP Host:");
        smtpHostField = new MirthIconTextField();

        sendTestEmailButton = new JButton("Send Test Email");
        sendTestEmailButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                sendTestEmailButtonActionPerformed();
            }
        });

        smtpPortLabel = new JLabel("SMTP Port:");
        smtpPortField = new MirthTextField();

        overrideLocalBindingLabel = new JLabel("Override Local Binding:");
        ButtonGroup overrideLocalBindingButtonGroup = new ButtonGroup();

        overrideLocalBindingYesRadio = new MirthRadioButton("Yes");
        overrideLocalBindingYesRadio.setBackground(getBackground());
        overrideLocalBindingYesRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                overrideLocalBindingFieldsEnabled(true);
            }
        });
        overrideLocalBindingButtonGroup.add(overrideLocalBindingYesRadio);

        overrideLocalBindingNoRadio = new MirthRadioButton("No");
        overrideLocalBindingNoRadio.setBackground(getBackground());
        overrideLocalBindingNoRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                overrideLocalBindingFieldsEnabled(false);
            }
        });
        overrideLocalBindingButtonGroup.add(overrideLocalBindingNoRadio);

        localAddressLabel = new JLabel("Local Address:");
        localAddressField = new MirthTextField();

        localPortLabel = new JLabel("Local Port:");
        localPortField = new MirthTextField();

        sendTimeoutLabel = new JLabel("Send Timeout (ms):");
        sendTimeoutField = new MirthTextField();

        encryptionLabel = new JLabel("Encryption:");
        ButtonGroup encryptionButtonGroup = new ButtonGroup();

        encryptionNone = new MirthRadioButton("None");
        encryptionNone.setBackground(getBackground());
        encryptionButtonGroup.add(encryptionNone);

        encryptionTls = new MirthRadioButton("STARTTLS");
        encryptionTls.setBackground(getBackground());
        encryptionButtonGroup.add(encryptionTls);

        encryptionSsl = new MirthRadioButton("SSL");
        encryptionSsl.setBackground(getBackground());
        encryptionButtonGroup.add(encryptionSsl);

        useAuthenticationLabel = new JLabel("Use Authentication:");
        ButtonGroup useAuthenticationButtonGroup = new ButtonGroup();

        useAuthenticationYes = new MirthRadioButton("Yes");
        useAuthenticationYes.setBackground(getBackground());
        useAuthenticationYes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                setAuthenticationFieldsEnabled(true);
            }
        });
        useAuthenticationButtonGroup.add(useAuthenticationYes);

        useAuthenticationNo = new MirthRadioButton("No");
        useAuthenticationNo.setBackground(getBackground());
        useAuthenticationNo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                setAuthenticationFieldsEnabled(false);
            }
        });
        useAuthenticationButtonGroup.add(useAuthenticationNo);

        usernameLabel = new JLabel("Username:");
        usernameField = new MirthTextField();

        passwordLabel = new JLabel("Password:");
        passwordField = new MirthPasswordField();

        toLabel = new JLabel("To:");
        toField = new MirthTextField();

        fromLabel = new JLabel("From:");
        fromField = new MirthTextField();

        subjectLabel = new JLabel("Subject:");
        subjectField = new MirthTextField();

        charsetEncodingLabel = new JLabel("Charset Encoding:");
        charsetEncodingComboBox = new MirthComboBox<CharsetEncodingInformation>();

        htmlLabel = new JLabel("HTML Body:");
        ButtonGroup htmlButtonGroup = new ButtonGroup();

        htmlYes = new MirthRadioButton("Yes");
        htmlYes.setBackground(getBackground());
        htmlButtonGroup.add(htmlYes);

        htmlNo = new MirthRadioButton("No");
        htmlNo.setBackground(getBackground());
        htmlButtonGroup.add(htmlNo);

        bodyLabel = new JLabel("Body:");
        bodyTextPane = new MirthSyntaxTextArea();
        bodyTextPane.setBorder(BorderFactory.createEtchedBorder());

        headersLabel = new JLabel("Headers:");

        headersTable = new MirthTable();
        headersPane = new JScrollPane(headersTable);

        newHeaderButton = new JButton("New");
        newHeaderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                newHeaderButtonActionPerformed();
            }
        });

        deleteHeaderButton = new JButton("Delete");
        deleteHeaderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                deleteHeaderButtonActionPerformed();
            }
        });

        useHeadersTableRadio = new MirthRadioButton("Use Table");
        useHeadersTableRadio.setBackground(getBackground());
        useHeadersTableRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                useHeadersVariableFieldsEnabled(false);
            }
        });
        useHeadersVariableRadio = new MirthRadioButton("Use Map:");
        useHeadersVariableRadio.setBackground(getBackground());
        useHeadersVariableRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                useHeadersVariableFieldsEnabled(true);
            }
        });
        ButtonGroup headersSourceButtonGroup = new ButtonGroup();
        headersSourceButtonGroup.add(useHeadersTableRadio);
        headersSourceButtonGroup.add(useHeadersVariableRadio);     

        headersVariableField = new MirthTextField();
        
        attachmentsLabel = new JLabel("Attachments:");

        attachmentsTable = new MirthTable();
        attachmentsPane = new JScrollPane(attachmentsTable);

        newAttachmentButton = new JButton("New");
        newAttachmentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                newAttachmentButtonActionPerformed();
            }
        });

        deleteAttachmentButton = new JButton("Delete");
        deleteAttachmentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                deleteAttachmentButtonActionPerformed();
            }
        });
        
        useAttachmentsListRadio = new MirthRadioButton("Use Table");
        useAttachmentsListRadio.setBackground(getBackground());
        useAttachmentsListRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                useAttachmentsVariableFieldsEnabled(false);
            }
        });
        useAttachmentsVariableRadio = new MirthRadioButton("Use List:");
        useAttachmentsVariableRadio.setBackground(getBackground());
        useAttachmentsVariableRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                useAttachmentsVariableFieldsEnabled(true);
            }
        });
        ButtonGroup attachmentSourceButtonGroup = new ButtonGroup();
        attachmentSourceButtonGroup.add(useAttachmentsListRadio);
        attachmentSourceButtonGroup.add(useAttachmentsVariableRadio);     

        attachmentsVariableField = new MirthTextField();
    }

    private void initToolTips() {
        smtpHostField.setToolTipText("<html>Enter the DNS domain name or IP address of the SMTP server to use to send the email messages.<br>Note that sending email to an SMTP server that is not expecting it may result in the IP of the box running Mirth Connect being added to the server's \"blacklist.\"</html>");
        smtpPortField.setToolTipText("<html>The port number of the SMTP server to send the email message to.<br>Generally, the default port of 25 is used.</html>");

        String toolTipText = "<html>Select Yes to override the local address and port that the client socket will be bound to.<br/>Select No to use the default value picked by the Socket class.<br/>A local port of zero (0) indicates that the OS should assign an ephemeral port automatically.<br/><br/>Note that if a specific (non-zero) local port is chosen, then after a socket is closed it's up to the<br/>underlying OS to release the port before the next socket creation, otherwise the bind attempt will fail.<br/></html>";
        overrideLocalBindingYesRadio.setToolTipText(toolTipText);
        overrideLocalBindingNoRadio.setToolTipText(toolTipText);

        localAddressField.setToolTipText("<html>The local address that the client socket will be bound to, if Override Local Binding is set to Yes.<br/></html>");
        localPortField.setToolTipText("<html>The local port that the client socket will be bound to, if Override Local Binding is set to Yes.<br/><br/>Note that if a specific (non-zero) local port is chosen, then after a socket is closed it's up to the<br/>underlying OS to release the port before the next socket creation, otherwise the bind attempt will fail.<br/></html>");
        sendTimeoutField.setToolTipText("<html>Enter the number of milliseconds for the SMTP socket connection timeout.</html>");

        toolTipText = "Selects whether STARTTLS or SSL should be used for optional connection security.";
        encryptionNone.setToolTipText(toolTipText);
        encryptionTls.setToolTipText(toolTipText);
        encryptionSsl.setToolTipText(toolTipText);

        useAuthenticationYes.setToolTipText("Use SMTP authentication.");
        useAuthenticationNo.setToolTipText("Do not use SMTP authentication.");
        usernameField.setToolTipText("If the SMTP server requires authentication to send a message, enter the username here.");
        passwordField.setToolTipText("If the SMTP server requires authentication to send a message, enter the password here.");
        toField.setToolTipText("The name of the mailbox (person, usually) to which the email should be sent.");
        fromField.setToolTipText("The name that should appear as the \"From address\" in the email.");
        subjectField.setToolTipText("The text that should appear as the subject of the email, as seen by the receiver's email client.");
        charsetEncodingComboBox.setToolTipText("<html>Select the character set encoding used by the sender of the message,<br> or Default to assume the default character set encoding for the JVM running Mirth Connect.</html>");

        toolTipText = "Selects whether HTML tags can be used in the email message body.";
        htmlYes.setToolTipText(toolTipText);
        htmlNo.setToolTipText(toolTipText);
        
        useHeadersTableRadio.setToolTipText("<html>The table below will be used to populate headers.</html>");
        useHeadersVariableRadio.setToolTipText("<html>The Java map specified by the following variable will be used to populate headers.<br/>The map must have String keys and either String or List&lt;String&gt; values.</html>");
        headersVariableField.setToolTipText("<html>The variable of a Java map to use to populate headers.<br/>The map must have String keys and either String or List&lt;String&gt; values.</html>");
        
        useAttachmentsListRadio.setToolTipText("<html>The table below will be used to populate attachments.</html>");
        useAttachmentsVariableRadio.setToolTipText("<html>The Java list specified by the following variable will be used to populate attachments.<br/>The list must contain AttachmentEntry values - anything else is ignored.</html>");
        attachmentsVariableField.setToolTipText("<html>The variable of a Java list to use to populate attachments.<br/>The list must contain AttachmentEntry values.</html>");
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 0 8 0 8, novisualpadding, hidemode 3, gap 12 6", "[][]6[]", "[][]4[]4[][][]4[]4[]4[][][][][][]4[]4[][][]"));

        add(smtpHostLabel, "right");
        add(smtpHostField, "w 200!, sx, split 2");
        add(sendTestEmailButton, "gapbefore 6");
        add(smtpPortLabel, "newline, right");
        add(smtpPortField, "w 50!, sx");
        add(overrideLocalBindingLabel, "newline, right");
        add(overrideLocalBindingYesRadio, "split 2");
        add(overrideLocalBindingNoRadio);
        add(localAddressLabel, "newline, right");
        add(localAddressField, "w 200!, sx");
        add(localPortLabel, "newline, right");
        add(localPortField, "w 50!, sx");
        add(sendTimeoutLabel, "newline, right");
        add(sendTimeoutField, "w 75!, sx");
        add(encryptionLabel, "newline, right");
        add(encryptionNone, "split 3");
        add(encryptionTls);
        add(encryptionSsl);
        add(useAuthenticationLabel, "newline, right");
        add(useAuthenticationYes, "split 2");
        add(useAuthenticationNo);
        add(usernameLabel, "newline, right");
        add(usernameField, "w 125!, sx");
        add(passwordLabel, "newline, right");
        add(passwordField, "w 125!, sx");
        add(toLabel, "newline, right");
        add(toField, "w 200!, sx");
        add(fromLabel, "newline, right");
        add(fromField, "w 200!, sx");
        add(subjectLabel, "newline, right");
        add(subjectField, "w 250!, sx");
        add(charsetEncodingLabel, "newline, right");
        add(charsetEncodingComboBox);
        add(htmlLabel, "newline, right");
        add(htmlYes, "split 2");
        add(htmlNo);
        add(bodyLabel, "newline, top, right");
        add(bodyTextPane, "grow, push, sx, h 89:");
        add(headersLabel, "newline, right");
        add(useHeadersTableRadio, "split 3");
        add(useHeadersVariableRadio);
        add(headersVariableField, "w 125");
        add(headersPane, "newline, growx, pushx, skip 1, span 2, h 85!");
        add(newHeaderButton, "top, flowy, split 2, w 44!");
        add(deleteHeaderButton, "w 44!");
        add(attachmentsLabel, "newline, right");
        add(useAttachmentsListRadio, "split 3");
        add(useAttachmentsVariableRadio);
        add(attachmentsVariableField, "w 125");
        add(attachmentsPane, "newline, growx, pushx, skip 1, span 2, h 85!");
        add(newAttachmentButton, "top, flowy, split 2, w 44!");
        add(deleteAttachmentButton, "w 44!");
    }

    private void newAttachmentButtonActionPerformed() {
        ((DefaultTableModel) attachmentsTable.getModel()).addRow(new Object[] {
                getNewUniqueName(attachmentsTable), "" });
        attachmentsTable.setRowSelectionInterval(attachmentsTable.getRowCount() - 1, attachmentsTable.getRowCount() - 1);
        parent.setSaveEnabled(true);
    }

    private void deleteAttachmentButtonActionPerformed() {
        if (getSelectedRow(attachmentsTable) != -1 && !attachmentsTable.isEditing()) {
            ((DefaultTableModel) attachmentsTable.getModel()).removeRow(getSelectedRow(attachmentsTable));

            if (attachmentsTable.getRowCount() != 0) {
                if (attachmentsLastIndex == 0) {
                    attachmentsTable.setRowSelectionInterval(0, 0);
                } else if (attachmentsLastIndex == attachmentsTable.getRowCount()) {
                    attachmentsTable.setRowSelectionInterval(attachmentsLastIndex - 1, attachmentsLastIndex - 1);
                } else {
                    attachmentsTable.setRowSelectionInterval(attachmentsLastIndex, attachmentsLastIndex);
                }
            }

            parent.setSaveEnabled(true);
        }
    }

    private void setAuthenticationFieldsEnabled(boolean useAuthentication) {
        usernameLabel.setEnabled(useAuthentication);
        usernameField.setEnabled(useAuthentication);

        passwordLabel.setEnabled(useAuthentication);
        passwordField.setEnabled(useAuthentication);
    }

    private void sendTestEmailButtonActionPerformed() {
        if (!checkProperties(getProperties(), true)) {
            parent.alertCustomError(this.parent, errors, "Please fix the following errors before sending a test email:");
            return;
        }

        ResponseHandler handler = new ResponseHandler() {
            @Override
            public void handle(Object response) {
                ConnectionTestResponse connectionTestResponse = (ConnectionTestResponse) response;
                if (connectionTestResponse == null) {
                    parent.alertError(parent, "Failed to send email.");
                } else if (connectionTestResponse.getType().equals(ConnectionTestResponse.Type.SUCCESS)) {
                    parent.alertInformation(parent, connectionTestResponse.getMessage());
                } else {
                    parent.alertWarning(parent, connectionTestResponse.getMessage());
                }
            }
        };

        try {
            getServlet(SmtpConnectorServletInterface.class, "Sending test email...", "Failed to send email.\n\n", handler).sendTestEmail(getChannelId(), getChannelName(), (SmtpDispatcherProperties) getFilledProperties());
        } catch (ClientException e) {
            // Should not happen
        }
    }

    private void newHeaderButtonActionPerformed() {
        ((DefaultTableModel) headersTable.getModel()).addRow(new Object[] {
                getNewUniqueName(headersTable), "" });
        headersTable.setRowSelectionInterval(headersTable.getRowCount() - 1, headersTable.getRowCount() - 1);
        parent.setSaveEnabled(true);
    }

    private void deleteHeaderButtonActionPerformed() {
        if (getSelectedRow(headersTable) != -1 && !headersTable.isEditing()) {
            ((DefaultTableModel) headersTable.getModel()).removeRow(getSelectedRow(headersTable));

            if (headersTable.getRowCount() != 0) {
                if (headersLastIndex == 0) {
                    headersTable.setRowSelectionInterval(0, 0);
                } else if (headersLastIndex == headersTable.getRowCount()) {
                    headersTable.setRowSelectionInterval(headersLastIndex - 1, headersLastIndex - 1);
                } else {
                    headersTable.setRowSelectionInterval(headersLastIndex, headersLastIndex);
                }
            }

            parent.setSaveEnabled(true);
        }
    }

    private void overrideLocalBindingFieldsEnabled(boolean isUseLocal) {
        localAddressField.setEnabled(isUseLocal);
        localAddressLabel.setEnabled(isUseLocal);
        localPortField.setEnabled(isUseLocal);
        localPortLabel.setEnabled(isUseLocal);
    }

    private void useAttachmentsVariableFieldsEnabled(boolean useVariable) {
        attachmentsVariableField.setEnabled(useVariable);
        attachmentsTable.setEnabled(!useVariable);
        newAttachmentButton.setEnabled(!useVariable);
        deleteAttachmentButton.setEnabled(!useVariable && attachmentsTable.getSelectedRow() > -1);
    }
    
    private void useHeadersVariableFieldsEnabled(boolean useVariable) {
        headersVariableField.setEnabled(useVariable);
        headersTable.setEnabled(!useVariable);
        newHeaderButton.setEnabled(!useVariable);
        deleteHeaderButton.setEnabled(!useVariable && headersTable.getSelectedRow() > -1);
    }

    private JLabel smtpHostLabel;
    private MirthIconTextField smtpHostField;
    private JButton sendTestEmailButton;
    private JLabel smtpPortLabel;
    private MirthTextField smtpPortField;
    private JLabel overrideLocalBindingLabel;
    private MirthRadioButton overrideLocalBindingYesRadio;
    private MirthRadioButton overrideLocalBindingNoRadio;
    private JLabel localAddressLabel;
    private MirthTextField localAddressField;
    private JLabel localPortLabel;
    private MirthTextField localPortField;
    private JLabel sendTimeoutLabel;
    private MirthTextField sendTimeoutField;
    public JLabel encryptionLabel;
    public MirthRadioButton encryptionNone;
    public MirthRadioButton encryptionTls;
    public MirthRadioButton encryptionSsl;
    private JLabel useAuthenticationLabel;
    private MirthRadioButton useAuthenticationYes;
    private MirthRadioButton useAuthenticationNo;
    private JLabel usernameLabel;
    private MirthTextField usernameField;
    private JLabel passwordLabel;
    private MirthPasswordField passwordField;
    private JLabel toLabel;
    private MirthTextField toField;
    private JLabel fromLabel;
    private MirthTextField fromField;
    private JLabel subjectLabel;
    private MirthTextField subjectField;
    private JLabel charsetEncodingLabel;
    private MirthComboBox<CharsetEncodingInformation> charsetEncodingComboBox;
    private JLabel htmlLabel;
    private MirthRadioButton htmlYes;
    private MirthRadioButton htmlNo;
    private JLabel bodyLabel;
    private MirthSyntaxTextArea bodyTextPane;
    private JLabel headersLabel;
    private MirthTable headersTable;
    private JScrollPane headersPane;
    private JButton newHeaderButton;
    private JButton deleteHeaderButton;
    private MirthRadioButton useHeadersTableRadio;
    private MirthRadioButton useHeadersVariableRadio;
    private MirthTextField headersVariableField;
    private JLabel attachmentsLabel;
    private MirthTable attachmentsTable;
    private JScrollPane attachmentsPane;
    private JButton newAttachmentButton;
    private JButton deleteAttachmentButton;
    private MirthRadioButton useAttachmentsListRadio;
    private MirthRadioButton useAttachmentsVariableRadio;
    private MirthTextField attachmentsVariableField;
}
