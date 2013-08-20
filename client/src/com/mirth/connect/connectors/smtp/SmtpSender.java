/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.smtp;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.validator.routines.EmailValidator;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.TextFieldCellEditor;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
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

        parent.setupCharsetEncodingForConnector(charsetEncodingCombobox);
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

        properties.setCharsetEncoding(parent.getSelectedEncodingForConnector(charsetEncodingCombobox));

        properties.setHtml(htmlYes.isSelected());

        properties.setBody(bodyTextPane.getText());
        properties.setHeaders(getHeaders());
        properties.setAttachments(getAttachments());

        return properties;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        SmtpDispatcherProperties props = (SmtpDispatcherProperties) properties;

        smtpHostField.setText(props.getSmtpHost());
        smtpPortField.setText(props.getSmtpPort());
        sendTimeoutField.setText(props.getTimeout());

        if (props.getEncryption().equalsIgnoreCase("TLS")) {
            encryptionTls.setSelected(true);
        } else if (props.getEncryption().equalsIgnoreCase("SSL")) {
            encryptionSsl.setSelected(true);
        } else {
            encryptionNone.setSelected(true);
        }

        if (props.isAuthentication()) {
            useAuthenticationYesActionPerformed(null);
            useAuthenticationYes.setSelected(true);
        } else {
            useAuthenticationNoActionPerformed(null);
            useAuthenticationNo.setSelected(true);
        }

        usernameField.setText(props.getUsername());
        passwordField.setText(props.getPassword());
        toField.setText(props.getTo());
        fromField.setText(props.getFrom());
        subjectField.setText(props.getSubject());

        parent.setPreviousSelectedEncodingForConnector(charsetEncodingCombobox, props.getCharsetEncoding());

        if (props.isHtml()) {
            htmlYes.setSelected(true);
        } else {
            htmlNo.setSelected(true);
        }

        bodyTextPane.setText(props.getBody());

        setHeaders(props.getHeaders());

        setAttachments(props.getAttachments());
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
        } else if (!EmailValidator.getInstance().isValid(props.getTo())) {
            valid = false;
            if (highlight) {
                toField.setBackground(UIConstants.INVALID_COLOR);
            }

            errors.append("\"To\" must be a valid email address\n");
        }

        if (props.getFrom().length() == 0) {
            valid = false;
            if (highlight) {
                fromField.setBackground(UIConstants.INVALID_COLOR);
            }

            errors.append("\"From\" is required\n");
        } else if (!EmailValidator.getInstance().isValid(props.getFrom())) {
            valid = false;
            if (highlight) {
                fromField.setBackground(UIConstants.INVALID_COLOR);
            }

            errors.append("\"From\" must be a valid email address\n");
        }

        this.errors = errors.toString();

        return valid;
    }

    @Override
    public void resetInvalidProperties() {
        smtpHostField.setBackground(null);
        smtpPortField.setBackground(null);
        sendTimeoutField.setBackground(null);
        toField.setBackground(null);
        fromField.setBackground(null);
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

        headersTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[] {
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
            tableData[i][ATTACHMENTS_NAME_COLUMN] = attachments.get(i).getName();
            tableData[i][ATTACHMENTS_CONTENT_COLUMN] = attachments.get(i).getContent();
            tableData[i][ATTACHMENTS_MIME_TYPE_COLUMN] = attachments.get(i).getMimeType();
        }

        attachmentsTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[] {
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        htmlButtonGroup = new javax.swing.ButtonGroup();
        secureButtonGroup = new javax.swing.ButtonGroup();
        useAuthenticationButtonGroup = new javax.swing.ButtonGroup();
        smtpHostLabel = new javax.swing.JLabel();
        smtpPortLabel = new javax.swing.JLabel();
        usernameLabel = new javax.swing.JLabel();
        passwordLabel = new javax.swing.JLabel();
        toLabel = new javax.swing.JLabel();
        subjectLabel = new javax.swing.JLabel();
        bodyLabel = new javax.swing.JLabel();
        usernameField = new com.mirth.connect.client.ui.components.MirthTextField();
        smtpPortField = new com.mirth.connect.client.ui.components.MirthTextField();
        sendTimeoutLabel = new javax.swing.JLabel();
        sendTimeoutField = new com.mirth.connect.client.ui.components.MirthTextField();
        smtpHostField = new com.mirth.connect.client.ui.components.MirthTextField();
        toField = new com.mirth.connect.client.ui.components.MirthTextField();
        subjectField = new com.mirth.connect.client.ui.components.MirthTextField();
        passwordField = new com.mirth.connect.client.ui.components.MirthPasswordField();
        fromLabel = new javax.swing.JLabel();
        fromField = new com.mirth.connect.client.ui.components.MirthTextField();
        bodyTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea();
        htmlLabel = new javax.swing.JLabel();
        htmlYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        htmlNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        attachmentsLabel = new javax.swing.JLabel();
        attachmentsPane = new javax.swing.JScrollPane();
        attachmentsTable = new com.mirth.connect.client.ui.components.MirthTable();
        newAttachmentButton = new javax.swing.JButton();
        deleteAttachmentButton = new javax.swing.JButton();
        encryptionLabel = new javax.swing.JLabel();
        encryptionNone = new com.mirth.connect.client.ui.components.MirthRadioButton();
        encryptionTls = new com.mirth.connect.client.ui.components.MirthRadioButton();
        encryptionSsl = new com.mirth.connect.client.ui.components.MirthRadioButton();
        useAuthenticationYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        useAuthenticationLabel = new javax.swing.JLabel();
        useAuthenticationNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        sendTestEmailButton = new javax.swing.JButton();
        headersPane = new javax.swing.JScrollPane();
        headersTable = new com.mirth.connect.client.ui.components.MirthTable();
        newHeaderButton = new javax.swing.JButton();
        deleteHeaderButton = new javax.swing.JButton();
        headersLabel = new javax.swing.JLabel();
        charsetEncodingCombobox = new com.mirth.connect.client.ui.components.MirthComboBox();
        charsetEncodingLabel = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        smtpHostLabel.setText("SMTP Host:");

        smtpPortLabel.setText("SMTP Port:");

        usernameLabel.setText("Username:");

        passwordLabel.setText("Password:");

        toLabel.setText("To:");

        subjectLabel.setText("Subject:");

        bodyLabel.setText("Body:");

        usernameField.setToolTipText("If the SMTP server requires authentication to send a message, enter the username here.");

        smtpPortField.setToolTipText("<html>The port number of the SMTP server to send the email message to.<br>Generally, the default port of 25 is used.</html>");

        sendTimeoutLabel.setText("Send Timeout (ms):");

        sendTimeoutField.setToolTipText("<html>Enter the number of milliseconds for the SMTP socket connection timeout.</html>");

        smtpHostField.setToolTipText("<html>Enter the DNS domain name or IP address of the SMTP server to use to send the email messages.<br>Note that sending email to an SMTP server that is not expecting it may result in the IP of the box running Mirth being added to the server's \"blacklist.\"</html>");

        toField.setToolTipText("The name of the mailbox (person, usually) to which the email should be sent.");

        subjectField.setToolTipText("The text that should appear as the subject of the email, as seen by the receiver's email client.");

        passwordField.setToolTipText("If the SMTP server requires authentication to send a message, enter the password here.");

        fromLabel.setText("From:");

        fromField.setToolTipText("The name that should appear as the \"From address\" in the email.");

        bodyTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        htmlLabel.setText("HTML Body:");

        htmlYes.setBackground(new java.awt.Color(255, 255, 255));
        htmlYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        htmlButtonGroup.add(htmlYes);
        htmlYes.setText("Yes");
        htmlYes.setToolTipText("Selects whether HTML tags can be used in the email message body.");
        htmlYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        htmlNo.setBackground(new java.awt.Color(255, 255, 255));
        htmlNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        htmlButtonGroup.add(htmlNo);
        htmlNo.setText("No");
        htmlNo.setToolTipText("Selects whether HTML tags can be used in the email message body.");
        htmlNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        attachmentsLabel.setText("Attachments:");

        attachmentsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Content", "MIME type"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        attachmentsTable.setToolTipText("");
        attachmentsPane.setViewportView(attachmentsTable);

        newAttachmentButton.setText("New");
        newAttachmentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newAttachmentButtonActionPerformed(evt);
            }
        });

        deleteAttachmentButton.setText("Delete");
        deleteAttachmentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteAttachmentButtonActionPerformed(evt);
            }
        });

        encryptionLabel.setText("Encryption:");

        encryptionNone.setBackground(new java.awt.Color(255, 255, 255));
        encryptionNone.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        secureButtonGroup.add(encryptionNone);
        encryptionNone.setText("None");
        encryptionNone.setToolTipText("Selects whether STARTTLS or SSL should be used for optional connection security.");
        encryptionNone.setMargin(new java.awt.Insets(0, 0, 0, 0));

        encryptionTls.setBackground(new java.awt.Color(255, 255, 255));
        encryptionTls.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        secureButtonGroup.add(encryptionTls);
        encryptionTls.setText("STARTTLS");
        encryptionTls.setToolTipText("Selects whether STARTTLS or SSL should be used for optional connection security.");
        encryptionTls.setMargin(new java.awt.Insets(0, 0, 0, 0));

        encryptionSsl.setBackground(new java.awt.Color(255, 255, 255));
        encryptionSsl.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        secureButtonGroup.add(encryptionSsl);
        encryptionSsl.setText("SSL");
        encryptionSsl.setToolTipText("Selects whether STARTTLS or SSL should be used for optional connection security.");
        encryptionSsl.setMargin(new java.awt.Insets(0, 0, 0, 0));

        useAuthenticationYes.setBackground(new java.awt.Color(255, 255, 255));
        useAuthenticationYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        useAuthenticationButtonGroup.add(useAuthenticationYes);
        useAuthenticationYes.setText("Yes");
        useAuthenticationYes.setToolTipText("Use SMTP authentication.");
        useAuthenticationYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useAuthenticationYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useAuthenticationYesActionPerformed(evt);
            }
        });

        useAuthenticationLabel.setText("Use Authentication:");

        useAuthenticationNo.setBackground(new java.awt.Color(255, 255, 255));
        useAuthenticationNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        useAuthenticationButtonGroup.add(useAuthenticationNo);
        useAuthenticationNo.setText("No");
        useAuthenticationNo.setToolTipText("Do not use SMTP authentication.");
        useAuthenticationNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useAuthenticationNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useAuthenticationNoActionPerformed(evt);
            }
        });

        sendTestEmailButton.setText("Send Test Email");
        sendTestEmailButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendTestEmailButtonActionPerformed(evt);
            }
        });

        headersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        headersTable.setToolTipText("");
        headersPane.setViewportView(headersTable);

        newHeaderButton.setText("New");
        newHeaderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newHeaderButtonActionPerformed(evt);
            }
        });

        deleteHeaderButton.setText("Delete");
        deleteHeaderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteHeaderButtonActionPerformed(evt);
            }
        });

        headersLabel.setText("Headers:");

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "default", "utf-8", "iso-8859-1", "utf-16 (le)", "utf-16 (be)", "utf-16 (bom)", "us-ascii" }));
        charsetEncodingCombobox.setToolTipText("<html>Select the character set encoding used by the sender of the message,<br> or Default to assume the default character set encoding for the JVM running Mirth.</html>");

        charsetEncodingLabel.setText("Charset Encoding:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sendTimeoutLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(bodyLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(subjectLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(charsetEncodingLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(headersLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(fromLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(useAuthenticationLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(attachmentsLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(htmlLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(smtpHostLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(smtpPortLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(usernameLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(passwordLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(toLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(encryptionLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sendTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bodyTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(useAuthenticationYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(useAuthenticationNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(htmlYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(htmlNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(subjectField, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fromField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(toField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(smtpPortField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(smtpHostField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sendTestEmailButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(encryptionNone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(encryptionTls, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(encryptionSsl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(headersPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
                            .addComponent(attachmentsPane, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(newHeaderButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(deleteHeaderButton))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(newAttachmentButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(deleteAttachmentButton)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(smtpHostLabel)
                    .addComponent(smtpHostField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sendTestEmailButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(smtpPortLabel)
                    .addComponent(smtpPortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sendTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sendTimeoutLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(encryptionLabel)
                    .addComponent(encryptionNone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(encryptionTls, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(encryptionSsl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(useAuthenticationYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(useAuthenticationNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(useAuthenticationLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameLabel)
                    .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(toLabel)
                    .addComponent(toField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fromLabel)
                    .addComponent(fromField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(subjectLabel)
                    .addComponent(subjectField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(charsetEncodingLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(htmlLabel)
                    .addComponent(htmlYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(htmlNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bodyLabel)
                    .addComponent(bodyTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(headersPane, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(newHeaderButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteHeaderButton))
                    .addComponent(headersLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(attachmentsLabel)
                    .addComponent(attachmentsPane, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(newAttachmentButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteAttachmentButton)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void newAttachmentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newAttachmentButtonActionPerformed
        ((DefaultTableModel) attachmentsTable.getModel()).addRow(new Object[] {
                getNewUniqueName(attachmentsTable), "" });
        attachmentsTable.setRowSelectionInterval(attachmentsTable.getRowCount() - 1, attachmentsTable.getRowCount() - 1);
        parent.setSaveEnabled(true);
    }//GEN-LAST:event_newAttachmentButtonActionPerformed

    private void deleteAttachmentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteAttachmentButtonActionPerformed
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
    }//GEN-LAST:event_deleteAttachmentButtonActionPerformed

    private void useAuthenticationYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useAuthenticationYesActionPerformed
        usernameLabel.setEnabled(true);
        usernameField.setEnabled(true);

        passwordLabel.setEnabled(true);
        passwordField.setEnabled(true);
    }//GEN-LAST:event_useAuthenticationYesActionPerformed

    private void useAuthenticationNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useAuthenticationNoActionPerformed
        usernameLabel.setEnabled(false);
        usernameField.setEnabled(false);

        passwordLabel.setEnabled(false);
        passwordField.setEnabled(false);
    }//GEN-LAST:event_useAuthenticationNoActionPerformed

    private void sendTestEmailButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendTestEmailButtonActionPerformed
        if (!checkProperties(getProperties(), true)) {
            parent.alertCustomError(this.parent, errors, "Please fix the following errors before sending a test email:");
            return;
        }

        final String workingId = parent.startWorking("Sending test email...");

        SwingWorker worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {

                try {
                    ConnectionTestResponse response = (ConnectionTestResponse) parent.mirthClient.invokeConnectorService(parent.channelEditPanel.currentChannel.getId(), getConnectorName(), "sendTestEmail", getProperties());

                    if (response == null) {
                        parent.alertError(parent, "Failed to send email.");
                    } else if (response.getType().equals(ConnectionTestResponse.Type.SUCCESS)) {
                        parent.alertInformation(parent, response.getMessage());
                    } else {
                        parent.alertWarning(parent, response.getMessage());
                    }

                    return null;
                } catch (Exception e) {
                    parent.alertException(parent, e.getStackTrace(), e.getMessage());
                    return null;
                }
            }

            public void done() {
                parent.stopWorking(workingId);
            }
        };

        worker.execute();
    }//GEN-LAST:event_sendTestEmailButtonActionPerformed

    private void newHeaderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newHeaderButtonActionPerformed
        ((DefaultTableModel) headersTable.getModel()).addRow(new Object[] {
                getNewUniqueName(headersTable), "" });
        headersTable.setRowSelectionInterval(headersTable.getRowCount() - 1, headersTable.getRowCount() - 1);
        parent.setSaveEnabled(true);
    }//GEN-LAST:event_newHeaderButtonActionPerformed

    private void deleteHeaderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteHeaderButtonActionPerformed
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
    }//GEN-LAST:event_deleteHeaderButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel attachmentsLabel;
    private javax.swing.JScrollPane attachmentsPane;
    private com.mirth.connect.client.ui.components.MirthTable attachmentsTable;
    private javax.swing.JLabel bodyLabel;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea bodyTextPane;
    private com.mirth.connect.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private javax.swing.JLabel charsetEncodingLabel;
    private javax.swing.JButton deleteAttachmentButton;
    private javax.swing.JButton deleteHeaderButton;
    private javax.swing.JLabel encryptionLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton encryptionNone;
    private com.mirth.connect.client.ui.components.MirthRadioButton encryptionSsl;
    private com.mirth.connect.client.ui.components.MirthRadioButton encryptionTls;
    private com.mirth.connect.client.ui.components.MirthTextField fromField;
    private javax.swing.JLabel fromLabel;
    private javax.swing.JLabel headersLabel;
    private javax.swing.JScrollPane headersPane;
    private com.mirth.connect.client.ui.components.MirthTable headersTable;
    private javax.swing.ButtonGroup htmlButtonGroup;
    private javax.swing.JLabel htmlLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton htmlNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton htmlYes;
    private javax.swing.JButton newAttachmentButton;
    private javax.swing.JButton newHeaderButton;
    private com.mirth.connect.client.ui.components.MirthPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.ButtonGroup secureButtonGroup;
    private javax.swing.JButton sendTestEmailButton;
    private com.mirth.connect.client.ui.components.MirthTextField sendTimeoutField;
    private javax.swing.JLabel sendTimeoutLabel;
    private com.mirth.connect.client.ui.components.MirthTextField smtpHostField;
    private javax.swing.JLabel smtpHostLabel;
    private com.mirth.connect.client.ui.components.MirthTextField smtpPortField;
    private javax.swing.JLabel smtpPortLabel;
    private com.mirth.connect.client.ui.components.MirthTextField subjectField;
    private javax.swing.JLabel subjectLabel;
    private com.mirth.connect.client.ui.components.MirthTextField toField;
    private javax.swing.JLabel toLabel;
    private javax.swing.ButtonGroup useAuthenticationButtonGroup;
    private javax.swing.JLabel useAuthenticationLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton useAuthenticationNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton useAuthenticationYes;
    private com.mirth.connect.client.ui.components.MirthTextField usernameField;
    private javax.swing.JLabel usernameLabel;
    // End of variables declaration//GEN-END:variables
}
