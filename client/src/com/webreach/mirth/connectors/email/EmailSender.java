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

package com.webreach.mirth.connectors.email;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Properties;
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

import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

import com.webreach.mirth.client.ui.Mirth;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthTable;
import com.webreach.mirth.connectors.ConnectorClass;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class EmailSender extends ConnectorClass
{
    private final int NAME_COLUMN = 0;
    private final int CONTENT_COLUMN = 1;
    private final int MIME_TYPE_COLUMN = 2;
    private final String NAME_COLUMN_NAME = "Name";
    private final String CONTENT_COLUMN_NAME = "Content";
    private final String MIME_TYPE_COLUMN_NAME = "MIME type";
    private int attachmentsLastIndex = -1;
    
    /** Creates new form EmailSender */
    public EmailSender()
    {
        name = EmailSenderProperties.name;
        initComponents();
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(EmailSenderProperties.DATATYPE, name);
        properties.put(EmailSenderProperties.EMAIL_ADDRESS, SMTPServerHostField.getText());
        properties.put(EmailSenderProperties.EMAIL_PORT, SMTPServerPortField.getText());
        properties.put(EmailSenderProperties.EMAIL_USERNAME, emailUsernameField.getText());
        properties.put(EmailSenderProperties.EMAIL_PASSWORD, new String(emailPasswordField.getPassword()));
        properties.put(EmailSenderProperties.EMAIL_TO, emailToField.getText());
        properties.put(EmailSenderProperties.EMAIL_FROM, emailFromField.getText());
        properties.put(EmailSenderProperties.EMAIL_SUBJECT, emailSubjectField.getText());
        
        if (contentTypeHTMLButton.isSelected())
            properties.put(EmailSenderProperties.EMAIL_CONTENT_TYPE, "text/html");
        else
        	properties.put(EmailSenderProperties.EMAIL_CONTENT_TYPE, "text/plain");
        
        properties.put(EmailSenderProperties.EMAIL_BODY, emailBodyTextPane.getText());
        
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        ArrayList<ArrayList<String>> attachments = getAttachments();
        properties.put(EmailSenderProperties.EMAIL_ATTACHMENT_NAMES, serializer.toXML(attachments.get(0)));
        properties.put(EmailSenderProperties.EMAIL_ATTACHMENT_CONTENTS, serializer.toXML(attachments.get(1)));
        properties.put(EmailSenderProperties.EMAIL_ATTACHMENT_TYPES, serializer.toXML(attachments.get(2)));
        
        return properties;
    }

    public void setProperties(Properties props)
    {
        resetInvalidProperties();
        
        SMTPServerHostField.setText((String) props.get(EmailSenderProperties.EMAIL_ADDRESS));
        SMTPServerPortField.setText((String) props.get(EmailSenderProperties.EMAIL_PORT));
        emailUsernameField.setText((String) props.get(EmailSenderProperties.EMAIL_USERNAME));
        emailPasswordField.setText((String) props.get(EmailSenderProperties.EMAIL_PASSWORD));
        emailToField.setText((String) props.get(EmailSenderProperties.EMAIL_TO));
        emailFromField.setText((String) props.get(EmailSenderProperties.EMAIL_FROM));
        emailSubjectField.setText((String) props.get(EmailSenderProperties.EMAIL_SUBJECT));
        
        if (((String) props.get(EmailSenderProperties.EMAIL_CONTENT_TYPE)).equalsIgnoreCase("text/html"))
        	contentTypeHTMLButton.setSelected(true);
        else
        	contentTypePlainButton.setSelected(true);
        
        emailBodyTextPane.setText((String) props.get(EmailSenderProperties.EMAIL_BODY));
        
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        
        ArrayList<ArrayList<String>> attachments = new ArrayList<ArrayList<String>>();
        ArrayList<String> attachmentNames = new ArrayList<String>();
    	ArrayList<String> attachmentContents = new ArrayList<String>();
    	ArrayList<String> attachmentTypes = new ArrayList<String>();
        
        if (((String) props.get(EmailSenderProperties.EMAIL_ATTACHMENT_NAMES)).length() > 0) {
        	attachmentNames = (ArrayList<String>) serializer.fromXML((String) props.get(EmailSenderProperties.EMAIL_ATTACHMENT_NAMES));
        	attachmentContents = (ArrayList<String>) serializer.fromXML((String) props.get(EmailSenderProperties.EMAIL_ATTACHMENT_CONTENTS));
        	attachmentTypes = (ArrayList<String>) serializer.fromXML((String) props.get(EmailSenderProperties.EMAIL_ATTACHMENT_TYPES));
        }
        
    	attachments.add(attachmentNames);
    	attachments.add(attachmentContents);
    	attachments.add(attachmentTypes);
    	
    	setAttachments(attachments);
    }

    public Properties getDefaults()
    {
        return new EmailSenderProperties().getDefaults();
    }

    public boolean checkProperties(Properties props, boolean highlight)
    {
        resetInvalidProperties();
        boolean valid = true;
        
        if (((String) props.get(EmailSenderProperties.EMAIL_ADDRESS)).length() == 0)
        {
            valid = false;
            if (highlight)
            	SMTPServerHostField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(EmailSenderProperties.EMAIL_PORT)).length() == 0)
        {
            valid = false;
            if (highlight)
            	SMTPServerPortField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(EmailSenderProperties.EMAIL_TO)).length() == 0)
        {
            valid = false;
            if (highlight)
            	emailToField.setBackground(UIConstants.INVALID_COLOR);
        }
        
        return valid;
    }
    
    private void resetInvalidProperties()
    {
        SMTPServerHostField.setBackground(null);
        SMTPServerPortField.setBackground(null);
        emailToField.setBackground(null);
    }
    
    public String doValidate(Properties props, boolean highlight)
    {
    	String error = null;
    	
    	if (!checkProperties(props, highlight))
    		error = "Error in the form for connector \"" + getName() + "\".\n\n";
    	
    	return error;
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
        	tableData[i][NAME_COLUMN] = attachmentNames.get(i);
            tableData[i][CONTENT_COLUMN] = attachmentContents.get(i);
            tableData[i][MIME_TYPE_COLUMN] = attachmentTypes.get(i);
        }
        
        attachmentsTable.setModel(new javax.swing.table.DefaultTableModel(tableData, new String[] { NAME_COLUMN_NAME, CONTENT_COLUMN_NAME, MIME_TYPE_COLUMN_NAME })
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
                    if (attachmentsTable.getValueAt(i, NAME_COLUMN) != null && ((String) attachmentsTable.getValueAt(i, NAME_COLUMN)).equalsIgnoreCase(attachmentName))
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
        
        attachmentsTable.getColumnModel().getColumn(attachmentsTable.getColumnModel().getColumnIndex(NAME_COLUMN_NAME)).setCellEditor(new AttachmentsTableCellEditor(true));
        attachmentsTable.getColumnModel().getColumn(attachmentsTable.getColumnModel().getColumnIndex(CONTENT_COLUMN_NAME)).setCellEditor(new AttachmentsTableCellEditor(false));
        attachmentsTable.getColumnModel().getColumn(attachmentsTable.getColumnModel().getColumnIndex(MIME_TYPE_COLUMN_NAME)).setCellEditor(new AttachmentsTableCellEditor(false));
        
        attachmentsTable.setSelectionMode(0);
        attachmentsTable.setRowSelectionAllowed(true);
        attachmentsTable.setRowHeight(UIConstants.ROW_HEIGHT);
        attachmentsTable.setDragEnabled(false);
        attachmentsTable.setOpaque(true);
        attachmentsTable.setSortable(false);
        attachmentsTable.getTableHeader().setReorderingAllowed(false);
        
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
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
            if (((String) attachmentsTable.getValueAt(i, NAME_COLUMN)).length() > 0)
            {
                attachmentNames.add((String)attachmentsTable.getValueAt(i, NAME_COLUMN));
                attachmentContents.add((String)attachmentsTable.getValueAt(i, CONTENT_COLUMN));
                attachmentTypes.add((String)attachmentsTable.getValueAt(i, MIME_TYPE_COLUMN));
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
                if (((String) table.getValueAt(j, NAME_COLUMN)).equalsIgnoreCase(temp + i))
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        emailUsernameField = new com.webreach.mirth.client.ui.components.MirthTextField();
        SMTPServerPortField = new com.webreach.mirth.client.ui.components.MirthTextField();
        SMTPServerHostField = new com.webreach.mirth.client.ui.components.MirthTextField();
        emailToField = new com.webreach.mirth.client.ui.components.MirthTextField();
        emailSubjectField = new com.webreach.mirth.client.ui.components.MirthTextField();
        emailPasswordField = new com.webreach.mirth.client.ui.components.MirthPasswordField();
        jLabel8 = new javax.swing.JLabel();
        emailFromField = new com.webreach.mirth.client.ui.components.MirthTextField();
        emailBodyTextPane = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea();
        contentTypeLabel = new javax.swing.JLabel();
        contentTypeHTMLButton = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        contentTypePlainButton = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        jLabel9 = new javax.swing.JLabel();
        attachmentsPane = new javax.swing.JScrollPane();
        attachmentsTable = new com.webreach.mirth.client.ui.components.MirthTable();
        newButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jLabel1.setText("SMTP Server Host:");

        jLabel2.setText("SMTP Server Port:");

        jLabel3.setText("Username:");

        jLabel4.setText("Password:");

        jLabel5.setText("To:");

        jLabel6.setText("Subject:");

        jLabel7.setText("Body:");

        emailUsernameField.setToolTipText("If the SMTP server requires authentication to send a message, enter the username here.");

        SMTPServerPortField.setToolTipText("<html>The port number of the SMTP server to send the email message to.<br>Generally, the default port of 25 is used.</html>");

        SMTPServerHostField.setToolTipText("<html>Enter the DNS domain name or IP address of the SMTP server to use to send the email messages.<br>Note that sending email to an SMTP server that is not expecting it may result in the IP of the box running Mirth being added to the server's \"blacklist.\"</html>");

        emailToField.setToolTipText("The name of the mailbox (person, usually) to which the email should be sent.");

        emailSubjectField.setToolTipText("The text that should appear as the subject of the email, as seen by the receiver's email client.");

        emailPasswordField.setToolTipText("If the SMTP server requires authentication to send a message, enter the password here.");

        jLabel8.setText("From:");

        emailFromField.setToolTipText("The name that should appear as the \"From address\" in the email.");

        emailBodyTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        contentTypeLabel.setText("Content Type:");

        contentTypeHTMLButton.setBackground(new java.awt.Color(255, 255, 255));
        contentTypeHTMLButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(contentTypeHTMLButton);
        contentTypeHTMLButton.setText("HTML");
        contentTypeHTMLButton.setToolTipText("Selects whether the HTTP operation used to send each message.");
        contentTypeHTMLButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        contentTypePlainButton.setBackground(new java.awt.Color(255, 255, 255));
        contentTypePlainButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(contentTypePlainButton);
        contentTypePlainButton.setText("Plain");
        contentTypePlainButton.setToolTipText("Selects whether the HTTP operation used to send each message.");
        contentTypePlainButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel9.setText("Attachments:");

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

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(40, 40, 40)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel3)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel2)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel1)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel4)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel5)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel8)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel6)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, contentTypeLabel)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel7)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel9))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(attachmentsPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                    .add(newButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(deleteButton)))
                            .add(emailBodyTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(134, 134, 134)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(emailPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(emailUsernameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(emailToField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                            .add(emailFromField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(emailSubjectField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(134, 134, 134)
                        .add(contentTypeHTMLButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(contentTypePlainButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(134, 134, 134)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(SMTPServerHostField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                            .add(SMTPServerPortField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(SMTPServerHostField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(SMTPServerPortField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(emailUsernameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(emailPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(emailToField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(emailFromField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(emailSubjectField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(contentTypeLabel)
                    .add(contentTypeHTMLButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(contentTypePlainButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel7)
                    .add(emailBodyTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel9)
                    .add(layout.createSequentialGroup()
                        .add(newButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(deleteButton))
                    .add(attachmentsPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
    ((DefaultTableModel) attachmentsTable.getModel()).addRow(new Object[] { getNewAttachmentName(attachmentsTable), "" });
    attachmentsTable.setRowSelectionInterval(attachmentsTable.getRowCount() - 1, attachmentsTable.getRowCount() - 1);
    parent.enableSave();
}//GEN-LAST:event_newButtonActionPerformed

private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
    if (getSelectedRow(attachmentsTable) != -1 && !attachmentsTable.isEditing())
    {
        ((DefaultTableModel) attachmentsTable.getModel()).removeRow(getSelectedRow(attachmentsTable));

        if (attachmentsTable.getRowCount() != 0)
        {
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.webreach.mirth.client.ui.components.MirthTextField SMTPServerHostField;
    private com.webreach.mirth.client.ui.components.MirthTextField SMTPServerPortField;
    private javax.swing.JScrollPane attachmentsPane;
    private com.webreach.mirth.client.ui.components.MirthTable attachmentsTable;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private com.webreach.mirth.client.ui.components.MirthRadioButton contentTypeHTMLButton;
    private javax.swing.JLabel contentTypeLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton contentTypePlainButton;
    private javax.swing.JButton deleteButton;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea emailBodyTextPane;
    private com.webreach.mirth.client.ui.components.MirthTextField emailFromField;
    private com.webreach.mirth.client.ui.components.MirthPasswordField emailPasswordField;
    private com.webreach.mirth.client.ui.components.MirthTextField emailSubjectField;
    private com.webreach.mirth.client.ui.components.MirthTextField emailToField;
    private com.webreach.mirth.client.ui.components.MirthTextField emailUsernameField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JButton newButton;
    // End of variables declaration//GEN-END:variables

}
