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

package com.webreach.mirth.connectors.file;

import java.util.Properties;

import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.connectors.ConnectorClass;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class FileWriter extends ConnectorClass
{
    /** Creates new form FileWriter */

    public FileWriter()
    {
        name = FileWriterProperties.name;
        initComponents();
        parent.setupCharsetEncodingForConnector(charsetEncodingCombobox);
    }

    /** Converts the value of the host and directory form fields to the FILE_DIRECTORY property value */
    private String getHostDirectory() {
        if (scheme.getSelectedItem().equals("file")) {
            return directoryField.getText().replace('\\', '/');
        }
        else {
            return hostField.getText() + "/" + directoryField.getText();
        }
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        properties.put(FileWriterProperties.DATATYPE, name);
        
        if (((String) scheme.getSelectedItem()).equals("file"))
            properties.put(FileWriterProperties.FILE_SCHEME, FileWriterProperties.SCHEME_FILE);
        else if (((String) scheme.getSelectedItem()).equals("ftp"))
            properties.put(FileWriterProperties.FILE_SCHEME, FileWriterProperties.SCHEME_FTP);
        else if (((String) scheme.getSelectedItem()).equals("sftp"))
            properties.put(FileWriterProperties.FILE_SCHEME, FileWriterProperties.SCHEME_SFTP);
        
        properties.put(FileWriterProperties.FILE_DIRECTORY, getHostDirectory());
        properties.put(FileWriterProperties.FILE_NAME, fileNameField.getText());

        if (anonymousYes.isSelected())
            properties.put(FileWriterProperties.FILE_ANONYMOUS, UIConstants.YES_OPTION);
        else
            properties.put(FileWriterProperties.FILE_ANONYMOUS, UIConstants.NO_OPTION);

        properties.put(FileWriterProperties.FILE_USERNAME, usernameField.getText());
        properties.put(FileWriterProperties.FILE_PASSWORD, new String(passwordField.getPassword()));

        if (passiveModeYes.isSelected())
            properties.put(FileWriterProperties.FILE_PASSIVE_MODE, UIConstants.YES_OPTION);
        else
            properties.put(FileWriterProperties.FILE_PASSIVE_MODE, UIConstants.NO_OPTION);

        if (validateConnectionYes.isSelected())
            properties.put(FileWriterProperties.FILE_VALIDATE_CONNECTION, UIConstants.YES_OPTION);
        else
            properties.put(FileWriterProperties.FILE_VALIDATE_CONNECTION, UIConstants.NO_OPTION);

        if (appendToFileYes.isSelected())
            properties.put(FileWriterProperties.FILE_APPEND, UIConstants.YES_OPTION);
        else
            properties.put(FileWriterProperties.FILE_APPEND, UIConstants.NO_OPTION);

        properties.put(FileWriterProperties.FILE_CONTENTS, fileContentsTextPane.getText());

        properties.put(FileWriterProperties.CONNECTOR_CHARSET_ENCODING, parent.getSelectedEncodingForConnector(charsetEncodingCombobox));

        if (fileTypeBinary.isSelected())
            properties.put(FileWriterProperties.FILE_TYPE, UIConstants.YES_OPTION);
        else
            properties.put(FileWriterProperties.FILE_TYPE, UIConstants.NO_OPTION);

        return properties;
    }

    /** Converts FILE_DIRECTORY to host and directory form field values */
    public void setHostDirectory(String src) {
        
        int splitIndex = src.indexOf('/');
        String hostValue = "";
        String directoryValue = "";
        if (splitIndex != -1)
        {
        	hostValue = src.substring(0, splitIndex);
        	directoryValue = src.substring(splitIndex + 1);
        }
        else
        {
        	hostValue = src;
        }
        
        hostField.setText(hostValue);
        directoryField.setText(directoryValue);
    }

    public void setProperties(Properties props)
    {
        resetInvalidProperties();

        if (props.get(FileWriterProperties.FILE_SCHEME).equals(FileWriterProperties.SCHEME_FILE))
            scheme.setSelectedItem("file");
        else if (props.get(FileWriterProperties.FILE_SCHEME).equals(FileWriterProperties.SCHEME_FTP))
            scheme.setSelectedItem("ftp");
        else if (props.get(FileWriterProperties.FILE_SCHEME).equals(FileWriterProperties.SCHEME_SFTP))
            scheme.setSelectedItem("sftp");
        schemeActionPerformed(null);
        
        setHostDirectory((String) props.get(FileWriterProperties.FILE_DIRECTORY));
        
        fileNameField.setText((String) props.get(FileWriterProperties.FILE_NAME));

        if (((String) props.get(FileWriterProperties.FILE_ANONYMOUS)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {
            anonymousYes.setSelected(true);
            anonymousYesActionPerformed(null);
        }
        else
        {
            anonymousNo.setSelected(true);
            anonymousNoActionPerformed(null);
            usernameField.setText((String) props.get(FileWriterProperties.FILE_USERNAME));
            passwordField.setText((String) props.get(FileWriterProperties.FILE_PASSWORD));
        }

        if (((String) props.get(FileWriterProperties.FILE_PASSIVE_MODE)).equalsIgnoreCase(UIConstants.YES_OPTION))
            passiveModeYes.setSelected(true);
        else
            passiveModeNo.setSelected(true);

        if (((String) props.get(FileWriterProperties.FILE_VALIDATE_CONNECTION)).equalsIgnoreCase(UIConstants.YES_OPTION))
            validateConnectionYes.setSelected(true);
        else
            validateConnectionNo.setSelected(true);

        if (((String) props.get(FileWriterProperties.FILE_APPEND)).equalsIgnoreCase(UIConstants.YES_OPTION))
            appendToFileYes.setSelected(true);
        else
            appendToFileNo.setSelected(true);

        parent.setPreviousSelectedEncodingForConnector(charsetEncodingCombobox, (String) props.get(FileWriterProperties.CONNECTOR_CHARSET_ENCODING));

        fileContentsTextPane.setText((String) props.get(FileWriterProperties.FILE_CONTENTS));

        if (((String) props.get(FileWriterProperties.FILE_TYPE)).equalsIgnoreCase(UIConstants.YES_OPTION))
        {
            fileTypeBinary.setSelected(true);
            fileTypeBinaryActionPerformed(null);
        }
        else
        {
            fileTypeASCII.setSelected(true);
            fileTypeASCIIActionPerformed(null);
        }
    }

    public Properties getDefaults()
    {
        return new FileWriterProperties().getDefaults();
    }

    public boolean checkProperties(Properties props, boolean highlight)
    {
        resetInvalidProperties();
        boolean valid = true;

        if (((String) props.get(FileWriterProperties.FILE_DIRECTORY)).length() == 0)
        {
            valid = false;
            if (highlight)
            	directoryField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(FileWriterProperties.FILE_NAME)).length() == 0)
        {
            valid = false;
            if (highlight)
            	fileNameField.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(FileWriterProperties.FILE_CONTENTS)).length() == 0)
        {
            valid = false;
            if (highlight)
            	fileContentsTextPane.setBackground(UIConstants.INVALID_COLOR);
        }
        if (((String) props.get(FileWriterProperties.FILE_ANONYMOUS)).equals(UIConstants.NO_OPTION))
        {
            if (((String) props.get(FileWriterProperties.FILE_USERNAME)).length() == 0)
            {
                valid = false;
                if (highlight)
                	usernameField.setBackground(UIConstants.INVALID_COLOR);
            }
            if (((String) props.get(FileWriterProperties.FILE_PASSWORD)).length() == 0)
            {
                valid = false;
                if (highlight)
                	passwordField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        return valid;
    }

    private void resetInvalidProperties()
    {
        directoryField.setBackground(null);
        fileNameField.setBackground(null);
        fileContentsTextPane.setBackground(null);
        usernameField.setBackground(null);
        passwordField.setBackground(null);
    }
    
    public String doValidate(Properties props, boolean highlight)
    {
    	String error = null;
    	
    	if (!checkProperties(props, highlight))
    		error = "Error in the form for connector \"" + getName() + "\".\n\n";
    	
    	return error;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        directoryField = new com.webreach.mirth.client.ui.components.MirthTextField();
        fileNameField = new com.webreach.mirth.client.ui.components.MirthTextField();
        jLabel4 = new javax.swing.JLabel();
        appendToFileYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        appendToFileNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        fileContentsTextPane = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea(false,false);
        charsetEncodingCombobox = new com.webreach.mirth.client.ui.components.MirthComboBox();
        encodingLabel = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        fileTypeBinary = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        fileTypeASCII = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        scheme = new com.webreach.mirth.client.ui.components.MirthComboBox();
        hostLabel = new javax.swing.JLabel();
        hostField = new com.webreach.mirth.client.ui.components.MirthTextField();
        anonymousLabel = new javax.swing.JLabel();
        anonymousYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        anonymousNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        usernameLabel = new javax.swing.JLabel();
        usernameField = new com.webreach.mirth.client.ui.components.MirthTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new com.webreach.mirth.client.ui.components.MirthPasswordField();
        passiveModeLabel = new javax.swing.JLabel();
        passiveModeYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        passiveModeNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        validateConnectionLabel = new javax.swing.JLabel();
        validateConnectionYes = new com.webreach.mirth.client.ui.components.MirthRadioButton();
        validateConnectionNo = new com.webreach.mirth.client.ui.components.MirthRadioButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jLabel1.setText("/");

        jLabel2.setText("File Name:");

        jLabel3.setText("Template:");

        jLabel4.setText("Append to file:");

        appendToFileYes.setBackground(new java.awt.Color(255, 255, 255));
        appendToFileYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(appendToFileYes);
        appendToFileYes.setText("Yes");
        appendToFileYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        appendToFileNo.setBackground(new java.awt.Color(255, 255, 255));
        appendToFileNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(appendToFileNo);
        appendToFileNo.setSelected(true);
        appendToFileNo.setText("No");
        appendToFileNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        fileContentsTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "default", "utf-8", "iso-8859-1", "utf-16 (le)", "utf-16 (be)", "utf-16 (bom)", "us-ascii" }));

        encodingLabel.setText("Encoding:");

        jLabel5.setText("File Type:");

        fileTypeBinary.setBackground(new java.awt.Color(255, 255, 255));
        fileTypeBinary.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup3.add(fileTypeBinary);
        fileTypeBinary.setText("Binary");
        fileTypeBinary.setMargin(new java.awt.Insets(0, 0, 0, 0));

        fileTypeASCII.setBackground(new java.awt.Color(255, 255, 255));
        fileTypeASCII.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup3.add(fileTypeASCII);
        fileTypeASCII.setSelected(true);
        fileTypeASCII.setText("ASCII");
        fileTypeASCII.setMargin(new java.awt.Insets(0, 0, 0, 0));

        scheme.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "File", "FTP", "SFTP" }));
        scheme.setToolTipText("The basic method used to access files to be read - file (local filesystem), FTP, or SFTP.");
        scheme.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                schemeActionPerformed(evt);
            }
        });

        hostLabel.setText("://");

        hostField.setToolTipText("The name or IP address of the host (computer) on which the files to be read can be found.");

        anonymousLabel.setText("Anonymous:");

        anonymousYes.setBackground(new java.awt.Color(255, 255, 255));
        anonymousYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        anonymousYes.setText("Yes");
        anonymousYes.setToolTipText("Connects to the file anonymously instead of using a username and password.");
        anonymousYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        anonymousYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                anonymousYesActionPerformed(evt);
            }
        });

        anonymousNo.setBackground(new java.awt.Color(255, 255, 255));
        anonymousNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        anonymousNo.setSelected(true);
        anonymousNo.setText("No");
        anonymousNo.setToolTipText("Connects to the file using a username and password instead of anonymously.");
        anonymousNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        anonymousNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                anonymousNoActionPerformed(evt);
            }
        });

        usernameLabel.setText("Username:");

        usernameField.setToolTipText("The user name used to gain access to the server.");

        passwordLabel.setText("Password:");

        passwordField.setToolTipText("The password used to gain access to the server.");
        passwordField.setFont(new java.awt.Font("Tahoma", 0, 11));

        passiveModeLabel.setText("Passive Mode:");

        passiveModeYes.setBackground(new java.awt.Color(255, 255, 255));
        passiveModeYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        passiveModeYes.setText("Yes");
        passiveModeYes.setToolTipText("Select Yes to connect to the server in \"passive mode\". Passive mode sometimes allows a connection through a firewall that normal mode does not.");
        passiveModeYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        passiveModeNo.setBackground(new java.awt.Color(255, 255, 255));
        passiveModeNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        passiveModeNo.setSelected(true);
        passiveModeNo.setText("No");
        passiveModeNo.setToolTipText("Select Yes to connect to the server in \"normal mode\" as opposed to passive mode.");
        passiveModeNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        validateConnectionLabel.setText("Validate Connection:");

        validateConnectionYes.setBackground(new java.awt.Color(255, 255, 255));
        validateConnectionYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        validateConnectionYes.setText("Yes");
        validateConnectionYes.setToolTipText("Select Yes to test the connection to the server before each operation.");
        validateConnectionYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        validateConnectionNo.setBackground(new java.awt.Color(255, 255, 255));
        validateConnectionNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        validateConnectionNo.setSelected(true);
        validateConnectionNo.setText("No");
        validateConnectionNo.setToolTipText("Select No to skip testing the connection to the server before each operation.");
        validateConnectionNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(encodingLabel)
                    .add(jLabel5)
                    .add(jLabel4)
                    .add(passiveModeLabel)
                    .add(passwordLabel)
                    .add(usernameLabel)
                    .add(anonymousLabel)
                    .add(jLabel2)
                    .add(layout.createSequentialGroup()
                        .add(scheme, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(hostLabel))
                    .add(validateConnectionLabel)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(fileContentsTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(fileTypeBinary, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fileTypeASCII, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(260, 260, 260))
                    .add(layout.createSequentialGroup()
                        .add(appendToFileYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(appendToFileNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(286, 286, 286))
                    .add(layout.createSequentialGroup()
                        .add(validateConnectionYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(validateConnectionNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(309, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(passiveModeYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(passiveModeNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(286, 286, 286))
                    .add(layout.createSequentialGroup()
                        .add(passwordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(usernameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(anonymousYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(anonymousNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(286, 286, 286))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(hostField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 167, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jLabel1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(directoryField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE))
                            .add(fileNameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE))
                        .add(6, 6, 6))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(hostLabel)
                    .add(scheme, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(hostField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1)
                    .add(directoryField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(fileNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(anonymousLabel)
                    .add(anonymousYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(anonymousNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(usernameLabel)
                    .add(usernameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(passwordLabel)
                    .add(passwordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(passiveModeLabel)
                    .add(passiveModeYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(passiveModeNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(validateConnectionYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(validateConnectionNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(validateConnectionLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(appendToFileYes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(appendToFileNo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(fileTypeBinary, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(fileTypeASCII, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(encodingLabel)
                    .add(charsetEncodingCombobox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(fileContentsTextPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                    .add(jLabel3))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void anonymousNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_anonymousNoActionPerformed
        usernameLabel.setEnabled(true);
        usernameField.setEnabled(true);
        
        passwordLabel.setEnabled(true);
        passwordField.setEnabled(true);
    }//GEN-LAST:event_anonymousNoActionPerformed

    private void anonymousYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_anonymousYesActionPerformed
        usernameLabel.setEnabled(false);
        usernameField.setEnabled(false);
        usernameField.setText("anonymous");
        
        passwordLabel.setEnabled(false);
        passwordField.setEnabled(false);
        passwordField.setText("anonymous");
    }//GEN-LAST:event_anonymousYesActionPerformed

    private void onSchemeChange(boolean enableHost, boolean enableOthers, boolean anonymous) {
        
            // act like the appropriate Anonymous button was selected.
        if (anonymous) {
            
            anonymousYes.doClick();
        }
        else {
            
            anonymousNo.doClick();
        }
            
        hostLabel.setEnabled(enableHost);
        hostField.setEnabled(enableHost);

        anonymousLabel.setEnabled(enableOthers);
        anonymousYes.setEnabled(enableOthers);
        anonymousNo.setEnabled(enableOthers);
        passiveModeLabel.setEnabled(enableOthers);
        passiveModeYes.setEnabled(enableOthers);
        passiveModeNo.setEnabled(enableOthers);
        validateConnectionLabel.setEnabled(enableOthers);
        validateConnectionYes.setEnabled(enableOthers);
        validateConnectionNo.setEnabled(enableOthers);
    }

    private void schemeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_schemeActionPerformed
        String text = (String) scheme.getSelectedItem();
        
        // if File is selected
        if (text.equals("File")) {
            
            onSchemeChange(false, false, true);
        }
        // else if FTP is selected
        else if (text.equals("FTP")) {
            
            onSchemeChange(true, true, anonymousYes.isSelected());
        }
        // else if SFTP is selected
        else if (text.equals("SFTP")) {
            
            onSchemeChange(true, false, false);
        }
    }//GEN-LAST:event_schemeActionPerformed

    private void fileTypeASCIIActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_fileTypeASCIIActionPerformed
    {// GEN-HEADEREND:event_fileTypeASCIIActionPerformed
        encodingLabel.setEnabled(true);
        charsetEncodingCombobox.setEnabled(true);
    }// GEN-LAST:event_fileTypeASCIIActionPerformed

    private void fileTypeBinaryActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_fileTypeBinaryActionPerformed
    {// GEN-HEADEREND:event_fileTypeBinaryActionPerformed
        encodingLabel.setEnabled(false);
        charsetEncodingCombobox.setEnabled(false);
        charsetEncodingCombobox.setSelectedIndex(0);
    }// GEN-LAST:event_fileTypeBinaryActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel anonymousLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton anonymousNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton anonymousYes;
    private com.webreach.mirth.client.ui.components.MirthRadioButton appendToFileNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton appendToFileYes;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private com.webreach.mirth.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private com.webreach.mirth.client.ui.components.MirthTextField directoryField;
    private javax.swing.JLabel encodingLabel;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea fileContentsTextPane;
    private com.webreach.mirth.client.ui.components.MirthTextField fileNameField;
    private com.webreach.mirth.client.ui.components.MirthRadioButton fileTypeASCII;
    private com.webreach.mirth.client.ui.components.MirthRadioButton fileTypeBinary;
    private com.webreach.mirth.client.ui.components.MirthTextField hostField;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel passiveModeLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton passiveModeNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton passiveModeYes;
    private com.webreach.mirth.client.ui.components.MirthPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private com.webreach.mirth.client.ui.components.MirthComboBox scheme;
    private com.webreach.mirth.client.ui.components.MirthTextField usernameField;
    private javax.swing.JLabel usernameLabel;
    private javax.swing.JLabel validateConnectionLabel;
    private com.webreach.mirth.client.ui.components.MirthRadioButton validateConnectionNo;
    private com.webreach.mirth.client.ui.components.MirthRadioButton validateConnectionYes;
    // End of variables declaration//GEN-END:variables

}
