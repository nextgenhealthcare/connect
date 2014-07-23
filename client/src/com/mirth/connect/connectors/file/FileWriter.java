/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.ConnectorTypeDecoration;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.util.ConnectionTestResponse;

public class FileWriter extends ConnectorSettingsPanel {

    private Logger logger = Logger.getLogger(this.getClass());
    private Frame parent;

    public FileWriter() {
        this.parent = PlatformUI.MIRTH_FRAME;
        initComponents();

        parent.setupCharsetEncodingForConnector(charsetEncodingCombobox);
    }

    @Override
    public String getConnectorName() {
        return new FileDispatcherProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        FileDispatcherProperties properties = new FileDispatcherProperties();

        properties.setScheme(FileScheme.fromDisplayName((String) schemeComboBox.getSelectedItem()));

        if (schemeComboBox.getSelectedItem().equals(FileScheme.FILE.getDisplayName())) {
            properties.setHost(directoryField.getText().replace('\\', '/'));
        } else {
            properties.setHost(hostField.getText() + "/" + pathField.getText());
        }

        properties.setOutputPattern(fileNameField.getText());

        properties.setAnonymous(anonymousYes.isSelected());

        properties.setUsername(usernameField.getText());
        properties.setPassword(new String(passwordField.getPassword()));

        properties.setTimeout(timeoutField.getText());

        properties.setSecure(secureModeYes.isSelected());
        properties.setPassive(passiveModeYes.isSelected());
        properties.setValidateConnection(validateConnectionYes.isSelected());

        if (fileExistsAppendRadio.isSelected()) {
            properties.setOutputAppend(true);
            properties.setErrorOnExists(false);
        } else if (fileExistsErrorRadio.isSelected()) {
            properties.setOutputAppend(false);
            properties.setErrorOnExists(true);
        } else { // overwrite
            properties.setOutputAppend(false);
            properties.setErrorOnExists(false);
        }

        properties.setTemporary(tempFileYesRadio.isSelected());
        properties.setTemplate(fileContentsTextPane.getText());

        properties.setCharsetEncoding(parent.getSelectedEncodingForConnector(charsetEncodingCombobox));

        properties.setBinary(fileTypeBinary.isSelected());

        logger.debug("getProperties: properties=" + properties);

        return properties;
    }

    /**
     * Parses the scheme and URL to determine the values for the directory, host and path fields,
     * optionally storing them to the fields, highlighting field errors, or just testing for valid
     * values.
     * 
     * @param props
     *            The connector properties from which to take the values.
     * @param store
     *            If true, the parsed values are stored to the corresponding form controls.
     * @param highlight
     *            If true, fields for which the parsed values are invalid are highlighted.
     */
    public boolean setDirHostPath(FileDispatcherProperties props, boolean store, boolean highlight) {

        boolean valid = true;
        FileScheme scheme = props.getScheme();
        String hostPropValue = props.getHost();
        String directoryValue = "";
        String hostValue = "";
        String pathValue = "";
        if (scheme.equals(FileScheme.FILE)) {

            directoryValue = hostPropValue;
            if (directoryValue.length() <= 0) {
                if (highlight) {
                    directoryField.setBackground(UIConstants.INVALID_COLOR);
                }
                valid = false;
            }
        } else {

            int splitIndex = hostPropValue.indexOf('/');
            if (splitIndex != -1) {
                hostValue = hostPropValue.substring(0, splitIndex);
                pathValue = hostPropValue.substring(splitIndex + 1);
            } else {
                hostValue = hostPropValue;
            }

            if (hostValue.length() <= 0) {
                if (highlight) {
                    hostField.setBackground(UIConstants.INVALID_COLOR);
                }
                valid = false;
            }
        }

        if (store) {

            directoryField.setText(directoryValue);
            hostField.setText(hostValue);
            pathField.setText(pathValue);
        }

        return valid;
    }

    @Override
    public void setProperties(ConnectorProperties properties) {
        logger.debug("setProperties: props=" + properties);
        FileDispatcherProperties props = (FileDispatcherProperties) properties;

        FileScheme scheme = props.getScheme();
        schemeComboBox.setSelectedItem(props.getScheme().getDisplayName());

        schemeComboBoxActionPerformed(null);

        setDirHostPath(props, true, false);

        fileNameField.setText(props.getOutputPattern());

        if (props.isAnonymous()) {
            anonymousYes.setSelected(true);
            anonymousNo.setSelected(false);
            anonymousYesActionPerformed(null);
        } else {
            anonymousYes.setSelected(false);
            anonymousNo.setSelected(true);
            anonymousNoActionPerformed(null);
            usernameField.setText(props.getUsername());
            passwordField.setText(props.getPassword());
        }

        timeoutField.setText(props.getTimeout());

        if (props.isSecure()) {
            secureModeYes.setSelected(true);
            secureModeNo.setSelected(false);
            if (scheme.equals(FileScheme.WEBDAV)) {
                hostLabel.setText("https://");
            }
        } else {
            secureModeYes.setSelected(false);
            secureModeNo.setSelected(true);
            if (scheme.equals(FileScheme.WEBDAV)) {
                hostLabel.setText("http://");
            }
        }

        if (props.isPassive()) {
            passiveModeYes.setSelected(true);
            passiveModeNo.setSelected(false);
        } else {
            passiveModeYes.setSelected(false);
            passiveModeNo.setSelected(true);
        }

        if (props.isValidateConnection()) {
            validateConnectionYes.setSelected(true);
            validateConnectionNo.setSelected(false);
        } else {
            validateConnectionYes.setSelected(false);
            validateConnectionNo.setSelected(true);
        }

        if (props.isTemporary()) {
            tempFileYesRadio.setSelected(true);
        } else {
            tempFileNoRadio.setSelected(true);
        }

        if (props.isOutputAppend()) {
            fileExistsAppendRadio.setSelected(true);
            fileExistsAppendRadioActionPerformed(null);
        } else if (props.isErrorOnExists()) {
            fileExistsErrorRadio.setSelected(true);
            fileExistsErrorRadioActionPerformed(null);
        } else {
            fileExistsOverwriteRadio.setSelected(true);
            fileExistsOverwriteRadioActionPerformed(null);
        }

        parent.setPreviousSelectedEncodingForConnector(charsetEncodingCombobox, props.getCharsetEncoding());

        fileContentsTextPane.setText(props.getTemplate());

        if (props.isBinary()) {
            fileTypeBinary.setSelected(true);
            fileTypeASCII.setSelected(false);
            fileTypeBinaryActionPerformed(null);
        } else {
            fileTypeBinary.setSelected(false);
            fileTypeASCII.setSelected(true);
            fileTypeASCIIActionPerformed(null);
        }
    }

    @Override
    public ConnectorProperties getDefaults() {
        return new FileDispatcherProperties();
    }

    @Override
    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        FileDispatcherProperties props = (FileDispatcherProperties) properties;

        boolean valid = true;

        valid = setDirHostPath(props, false, highlight);

        if (props.getOutputPattern().length() == 0) {
            valid = false;
            if (highlight) {
                fileNameField.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (props.getTemplate().length() == 0) {
            valid = false;
            if (highlight) {
                fileContentsTextPane.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (!props.isAnonymous()) {
            if (props.getUsername().length() == 0) {
                valid = false;
                if (highlight) {
                    usernameField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
            if (props.getPassword().length() == 0) {
                valid = false;
                if (highlight) {
                    passwordField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }

        FileScheme scheme = props.getScheme();
        if (scheme.equals(FileScheme.FTP) || scheme.equals(FileScheme.SFTP) || scheme.equals(FileScheme.SMB)) {
            if (props.getTimeout().length() == 0) {
                valid = false;
                if (highlight) {
                    timeoutField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }

        return valid;
    }

    @Override
    public void resetInvalidProperties() {
        directoryField.setBackground(null);
        hostField.setBackground(null);
        pathField.setBackground(null);
        fileNameField.setBackground(null);
        fileContentsTextPane.setBackground(null);
        usernameField.setBackground(null);
        passwordField.setBackground(null);
        timeoutField.setBackground(null);
    }

    @Override
    public ConnectorTypeDecoration getConnectorTypeDecoration() {
        return new ConnectorTypeDecoration();
    }

    @Override
    public void doLocalDecoration(ConnectorTypeDecoration connectorTypeDecoration) {
        if (FileScheme.FTP.getDisplayName().equalsIgnoreCase((String) schemeComboBox.getSelectedItem())) {
            hostLabel.setText("ftp" + (connectorTypeDecoration != null && connectorTypeDecoration.getHighlightColor() != null ? "s" : "") + "://");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        buttonGroup6 = new javax.swing.ButtonGroup();
        buttonGroup7 = new javax.swing.ButtonGroup();
        schemeLabel = new javax.swing.JLabel();
        schemeComboBox = new com.mirth.connect.client.ui.components.MirthComboBox();
        directoryLabel = new javax.swing.JLabel();
        directoryField = new com.mirth.connect.client.ui.components.MirthTextField();
        hostLabel = new javax.swing.JLabel();
        hostField = new com.mirth.connect.client.ui.components.MirthTextField();
        pathLabel = new javax.swing.JLabel();
        pathField = new com.mirth.connect.client.ui.components.MirthTextField();
        fileNameLabel = new javax.swing.JLabel();
        fileNameField = new com.mirth.connect.client.ui.components.MirthTextField();
        anonymousLabel = new javax.swing.JLabel();
        anonymousYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        anonymousNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        usernameLabel = new javax.swing.JLabel();
        usernameField = new com.mirth.connect.client.ui.components.MirthTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new com.mirth.connect.client.ui.components.MirthPasswordField();
        secureModeLabel = new javax.swing.JLabel();
        secureModeYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        secureModeNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        validateConnectionLabel = new javax.swing.JLabel();
        validateConnectionYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        validateConnectionNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        fileExistsLabel = new javax.swing.JLabel();
        fileExistsAppendRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        fileExistsOverwriteRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        fileTypeLabel = new javax.swing.JLabel();
        fileTypeBinary = new com.mirth.connect.client.ui.components.MirthRadioButton();
        fileTypeASCII = new com.mirth.connect.client.ui.components.MirthRadioButton();
        charsetEncodingCombobox = new com.mirth.connect.client.ui.components.MirthComboBox();
        encodingLabel = new javax.swing.JLabel();
        templateLabel = new javax.swing.JLabel();
        fileContentsTextPane = new com.mirth.connect.client.ui.components.MirthSyntaxTextArea(false,false);
        testConnection = new javax.swing.JButton();
        passiveModeLabel = new javax.swing.JLabel();
        passiveModeYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        passiveModeNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        timeoutField = new com.mirth.connect.client.ui.components.MirthTextField();
        timeoutLabel = new javax.swing.JLabel();
        fileExistsErrorRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        tempFileLabel = new javax.swing.JLabel();
        tempFileYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        tempFileNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        schemeLabel.setText("Method:");

        schemeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "file", "ftp", "sftp", "smb", "webdav" }));
        schemeComboBox.setToolTipText("The basic method used to access files to be written - file (local filesystem), FTP, SFTP, Samba share, or WebDAV.");
        schemeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                schemeComboBoxActionPerformed(evt);
            }
        });

        directoryLabel.setText("Directory:");

        directoryField.setToolTipText("The directory (folder) where the generated file should be written.");

        hostLabel.setText("ftp://");

        hostField.setToolTipText("The name or IP address of the host (computer) on which the files will be written.");

        pathLabel.setText("/");

        pathField.setToolTipText("The directory (folder) to which the files will be written.");

        fileNameLabel.setText("File Name:");

        fileNameField.setToolTipText("The file name to give to the generated file.");

        anonymousLabel.setText("Anonymous:");

        anonymousYes.setBackground(new java.awt.Color(255, 255, 255));
        anonymousYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup1.add(anonymousYes);
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
        buttonGroup1.add(anonymousNo);
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

        secureModeLabel.setText("Secure Mode:");

        secureModeYes.setBackground(new java.awt.Color(255, 255, 255));
        secureModeYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(secureModeYes);
        secureModeYes.setText("Yes");
        secureModeYes.setToolTipText("<html>Select Yes to connect to the server via HTTPS.<br>Select No to connect via HTTP.</html>");
        secureModeYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        secureModeYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                secureModeYesActionPerformed(evt);
            }
        });

        secureModeNo.setBackground(new java.awt.Color(255, 255, 255));
        secureModeNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(secureModeNo);
        secureModeNo.setSelected(true);
        secureModeNo.setText("No");
        secureModeNo.setToolTipText("<html>Select Yes to connect to the server via HTTPS.<br>Select No to connect via HTTP.</html>");
        secureModeNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        secureModeNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                secureModeNoActionPerformed(evt);
            }
        });

        validateConnectionLabel.setText("Validate Connection:");

        validateConnectionYes.setBackground(new java.awt.Color(255, 255, 255));
        validateConnectionYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup3.add(validateConnectionYes);
        validateConnectionYes.setText("Yes");
        validateConnectionYes.setToolTipText("Select Yes to test the connection to the server before each operation.");
        validateConnectionYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        validateConnectionNo.setBackground(new java.awt.Color(255, 255, 255));
        validateConnectionNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup3.add(validateConnectionNo);
        validateConnectionNo.setText("No");
        validateConnectionNo.setToolTipText("Select No to skip testing the connection to the server before each operation.");
        validateConnectionNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        fileExistsLabel.setText("File Exists:");

        fileExistsAppendRadio.setBackground(new java.awt.Color(255, 255, 255));
        fileExistsAppendRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup4.add(fileExistsAppendRadio);
        fileExistsAppendRadio.setText("Append");
        fileExistsAppendRadio.setToolTipText("<html>If 'append' is selected, messages accepted by this destination will be appended to the file specified in the File Name.<br>If 'overwrite' is selected, messages accepted by this destination will replace any existing file of the same name.<br>If 'error' is selected and a file with the specified file name already exists, the message will error.</html>");
        fileExistsAppendRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        fileExistsAppendRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileExistsAppendRadioActionPerformed(evt);
            }
        });

        fileExistsOverwriteRadio.setBackground(new java.awt.Color(255, 255, 255));
        fileExistsOverwriteRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup4.add(fileExistsOverwriteRadio);
        fileExistsOverwriteRadio.setText("Overwrite");
        fileExistsOverwriteRadio.setToolTipText("<html>If 'append' is selected, messages accepted by this destination will be appended to the file specified in the File Name.<br>If 'overwrite' is selected, messages accepted by this destination will replace any existing file of the same name.<br>If 'error' is selected and a file with the specified file name already exists, the message will error.</html>");
        fileExistsOverwriteRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        fileExistsOverwriteRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileExistsOverwriteRadioActionPerformed(evt);
            }
        });

        fileTypeLabel.setText("File Type:");

        fileTypeBinary.setBackground(new java.awt.Color(255, 255, 255));
        fileTypeBinary.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup5.add(fileTypeBinary);
        fileTypeBinary.setText("Binary");
        fileTypeBinary.setToolTipText("<html>If Binary is selected, messages are written as binary byte streams.<br>If Text is selected, messages are written and encoded with the specified character set encoding.</html>");
        fileTypeBinary.setMargin(new java.awt.Insets(0, 0, 0, 0));
        fileTypeBinary.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileTypeBinaryActionPerformed(evt);
            }
        });

        fileTypeASCII.setBackground(new java.awt.Color(255, 255, 255));
        fileTypeASCII.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup5.add(fileTypeASCII);
        fileTypeASCII.setSelected(true);
        fileTypeASCII.setText("Text");
        fileTypeASCII.setToolTipText("<html>If Binary is selected, messages are written as binary byte streams.<br>If Text is selected, messages are written and encoded with the specified character set encoding.</html>");
        fileTypeASCII.setMargin(new java.awt.Insets(0, 0, 0, 0));
        fileTypeASCII.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileTypeASCIIActionPerformed(evt);
            }
        });

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "default", "utf-8", "iso-8859-1", "utf-16 (le)", "utf-16 (be)", "utf-16 (bom)", "us-ascii" }));
        charsetEncodingCombobox.setToolTipText("<html>Select the character encoding system to use to write the files accepted by the destination connector.<br>Selecting Default uses the default character encoding for the JVM in which Mirth is running.<br>Selecting any other value selects the corresponding character encoding.</html>");

        encodingLabel.setText("Encoding:");

        templateLabel.setText("Template:");

        fileContentsTextPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        testConnection.setText("Test Write");
        testConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testConnectionActionPerformed(evt);
            }
        });

        passiveModeLabel.setText("Passive Mode:");

        passiveModeYes.setBackground(new java.awt.Color(255, 255, 255));
        passiveModeYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(passiveModeYes);
        passiveModeYes.setText("Yes");
        passiveModeYes.setToolTipText("Select Yes to connect to the server in \"passive mode\". Passive mode sometimes allows a connection through a firewall that normal mode does not.");
        passiveModeYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        passiveModeNo.setBackground(new java.awt.Color(255, 255, 255));
        passiveModeNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(passiveModeNo);
        passiveModeNo.setSelected(true);
        passiveModeNo.setText("No");
        passiveModeNo.setToolTipText("Select Yes to connect to the server in \"normal mode\" as opposed to passive mode.");
        passiveModeNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        timeoutField.setToolTipText("The socket timeout (in ms) for connecting to the server.");

        timeoutLabel.setText("Timeout (ms):");

        fileExistsErrorRadio.setBackground(new java.awt.Color(255, 255, 255));
        fileExistsErrorRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup4.add(fileExistsErrorRadio);
        fileExistsErrorRadio.setSelected(true);
        fileExistsErrorRadio.setText("Error");
        fileExistsErrorRadio.setToolTipText("<html>If 'append' is selected, messages accepted by this destination will be appended to the file specified in the File Name.<br>If 'overwrite' is selected, messages accepted by this destination will replace any existing file of the same name.<br>If 'error' is selected and a file with the specified file name already exists, the message will error.</html>");
        fileExistsErrorRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        fileExistsErrorRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileExistsErrorRadioActionPerformed(evt);
            }
        });

        tempFileLabel.setText("Create Temp File:");

        tempFileYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        tempFileYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup7.add(tempFileYesRadio);
        tempFileYesRadio.setText("Yes");
        tempFileYesRadio.setToolTipText("<html>If 'yes' is selected, the file contents will first be written to a temp file and then renamed to the specified file name.<br>If 'no' is selected, the file contents will be written directly to the destination file.<br>Using a temp file is not an option if the specified file is being appended to.</html>");
        tempFileYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        tempFileNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        tempFileNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup7.add(tempFileNoRadio);
        tempFileNoRadio.setSelected(true);
        tempFileNoRadio.setText("No");
        tempFileNoRadio.setToolTipText("<html>If 'yes' is selected, the file contents will first be written to a temp file and then renamed to the specified file name.<br>If 'no' is selected, the file contents will be written directly to the destination file.<br>Using a temp file is not an option if the specified file is being appended to.</html>");
        tempFileNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(tempFileLabel)
                    .addComponent(templateLabel)
                    .addComponent(encodingLabel)
                    .addComponent(fileTypeLabel)
                    .addComponent(anonymousLabel)
                    .addComponent(fileNameLabel)
                    .addComponent(hostLabel)
                    .addComponent(directoryLabel)
                    .addComponent(schemeLabel)
                    .addComponent(secureModeLabel)
                    .addComponent(passwordLabel)
                    .addComponent(validateConnectionLabel)
                    .addComponent(fileExistsLabel)
                    .addComponent(usernameLabel)
                    .addComponent(passiveModeLabel)
                    .addComponent(timeoutLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(timeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(schemeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(testConnection))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(hostField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pathLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pathField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(fileNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(anonymousYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(anonymousNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(secureModeYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(secureModeNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(validateConnectionYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(validateConnectionNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fileExistsAppendRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fileExistsOverwriteRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fileExistsErrorRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fileTypeBinary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fileTypeASCII, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(directoryField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(passiveModeYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(passiveModeNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(fileContentsTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tempFileYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tempFileNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(schemeLabel)
                    .addComponent(schemeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(testConnection))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(directoryLabel)
                    .addComponent(directoryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hostLabel)
                    .addComponent(hostField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pathLabel)
                    .addComponent(pathField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileNameLabel)
                    .addComponent(fileNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(anonymousLabel)
                    .addComponent(anonymousYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(anonymousNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usernameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(timeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(timeoutLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(secureModeLabel)
                    .addComponent(secureModeYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(secureModeNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passiveModeLabel)
                    .addComponent(passiveModeYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passiveModeNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(validateConnectionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(validateConnectionYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(validateConnectionNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileExistsLabel)
                    .addComponent(fileExistsAppendRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fileExistsOverwriteRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fileExistsErrorRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tempFileLabel)
                    .addComponent(tempFileYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tempFileNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileTypeLabel)
                    .addComponent(fileTypeBinary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fileTypeASCII, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(encodingLabel)
                    .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(templateLabel)
                    .addComponent(fileContentsTextPane, javax.swing.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE))
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

    private void onSchemeChange(boolean enableHost, boolean anonymous, boolean allowAppend, FileScheme scheme) {
        // act like the appropriate Anonymous button was selected.
        if (anonymous) {
            anonymousNo.setSelected(false);
            anonymousYes.setSelected(true);
            anonymousYesActionPerformed(null);
        } else {
            anonymousNo.setSelected(true);
            anonymousYes.setSelected(false);
            anonymousNoActionPerformed(null);
        }

        hostLabel.setEnabled(enableHost);
        hostField.setEnabled(enableHost);
        pathLabel.setEnabled(enableHost);
        pathField.setEnabled(enableHost);
        directoryLabel.setEnabled(!enableHost);
        directoryField.setEnabled(!enableHost);

        anonymousLabel.setEnabled(false);
        anonymousYes.setEnabled(false);
        anonymousNo.setEnabled(false);
        passiveModeLabel.setEnabled(false);
        passiveModeYes.setEnabled(false);
        passiveModeNo.setEnabled(false);
        secureModeLabel.setEnabled(false);
        secureModeYes.setEnabled(false);
        secureModeNo.setEnabled(false);
        validateConnectionLabel.setEnabled(false);
        validateConnectionYes.setEnabled(false);
        validateConnectionNo.setEnabled(false);
        timeoutLabel.setEnabled(false);
        timeoutField.setEnabled(false);

        if (allowAppend) {
            fileExistsOverwriteRadio.setEnabled(true);
            fileExistsAppendRadio.setEnabled(true);
            fileExistsLabel.setEnabled(true);
        } else {
            if (fileExistsAppendRadio.isSelected()) {
                fileExistsOverwriteRadio.setSelected(true);
                fileExistsAppendRadio.setSelected(false);
            }

            fileExistsOverwriteRadio.setEnabled(false);
            fileExistsAppendRadio.setEnabled(false);
            fileExistsLabel.setEnabled(false);
        }

        if (scheme.equals(FileScheme.FTP)) {
            anonymousLabel.setEnabled(true);
            anonymousYes.setEnabled(true);
            anonymousNo.setEnabled(true);
            passiveModeLabel.setEnabled(true);
            passiveModeYes.setEnabled(true);
            passiveModeNo.setEnabled(true);
            validateConnectionLabel.setEnabled(true);
            validateConnectionYes.setEnabled(true);
            validateConnectionNo.setEnabled(true);
            timeoutLabel.setEnabled(true);
            timeoutField.setEnabled(true);
        } else if (scheme.equals(FileScheme.SFTP)) {
            timeoutLabel.setEnabled(true);
            timeoutField.setEnabled(true);
        } else if (scheme.equals(FileScheme.WEBDAV)) {
            anonymousLabel.setEnabled(true);
            anonymousYes.setEnabled(true);
            anonymousNo.setEnabled(true);
            secureModeLabel.setEnabled(true);
            secureModeYes.setEnabled(true);
            secureModeNo.setEnabled(true);

            // set Passive Mode and validate connection to No.
            passiveModeNo.setSelected(true);
            validateConnectionNo.setSelected(true);

        } else if (scheme.equals(FileScheme.SMB)) {
            timeoutLabel.setEnabled(true);
            timeoutField.setEnabled(true);
        }
    }

    private void schemeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_schemeComboBoxActionPerformed
        String text = (String) schemeComboBox.getSelectedItem();

        // if File is selected
        if (text.equals(FileScheme.FILE.getDisplayName())) {

            onSchemeChange(false, true, true, FileScheme.FILE);
        } // else if FTP is selected
        else if (text.equals(FileScheme.FTP.getDisplayName())) {

            onSchemeChange(true, anonymousYes.isSelected(), true, FileScheme.FTP);
            hostLabel.setText("ftp://");
        } // else if SFTP is selected
        else if (text.equals(FileScheme.SFTP.getDisplayName())) {

            onSchemeChange(true, false, true, FileScheme.SFTP);
            hostLabel.setText("sftp://");
        } // else if SMB is selected
        else if (text.equals(FileScheme.SMB.getDisplayName())) {

            onSchemeChange(true, false, true, FileScheme.SMB);
            hostLabel.setText("smb://");
        } // else if WEBDAV is selected
        else if (text.equals(FileScheme.WEBDAV.getDisplayName())) {

            onSchemeChange(true, anonymousYes.isSelected(), false, FileScheme.WEBDAV);
            if (secureModeYes.isSelected()) {
                hostLabel.setText("https://");
            } else {
                hostLabel.setText("http://");
            }
        }

        decorateConnectorType();
    }//GEN-LAST:event_schemeComboBoxActionPerformed

    private void testConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testConnectionActionPerformed
        final String workingId = parent.startWorking("Testing connection...");

        SwingWorker worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {

                try {
                    ConnectionTestResponse response = (ConnectionTestResponse) parent.mirthClient.invokeConnectorService(parent.channelEditPanel.currentChannel.getId(), getConnectorName(), "testWrite", getProperties());

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
                parent.stopWorking(workingId);
            }
        };

        worker.execute();
    }//GEN-LAST:event_testConnectionActionPerformed

    private void secureModeYesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_secureModeYesActionPerformed
        // only WebDAV has access to here.
        // change host label to 'https://'
        hostLabel.setText("https://");
    }//GEN-LAST:event_secureModeYesActionPerformed

    private void secureModeNoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_secureModeNoActionPerformed
        // only WebDAV has access to here.
        // change host label to 'http://'
        hostLabel.setText("http://");
    }//GEN-LAST:event_secureModeNoActionPerformed

    private void fileExistsAppendRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileExistsAppendRadioActionPerformed
        tempFileNoRadio.setSelected(true);
        tempFileLabel.setEnabled(false);
        tempFileYesRadio.setEnabled(false);
        tempFileNoRadio.setEnabled(false);
    }//GEN-LAST:event_fileExistsAppendRadioActionPerformed

    private void fileExistsOverwriteRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileExistsOverwriteRadioActionPerformed
        tempFileLabel.setEnabled(true);
        tempFileYesRadio.setEnabled(true);
        tempFileNoRadio.setEnabled(true);
    }//GEN-LAST:event_fileExistsOverwriteRadioActionPerformed

    private void fileExistsErrorRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileExistsErrorRadioActionPerformed
        tempFileLabel.setEnabled(true);
        tempFileYesRadio.setEnabled(true);
        tempFileNoRadio.setEnabled(true);
    }//GEN-LAST:event_fileExistsErrorRadioActionPerformed

    private void fileTypeASCIIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileTypeASCIIActionPerformed
        encodingLabel.setEnabled(true);
        charsetEncodingCombobox.setEnabled(true);
    }//GEN-LAST:event_fileTypeASCIIActionPerformed

    private void fileTypeBinaryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileTypeBinaryActionPerformed
        encodingLabel.setEnabled(false);
        charsetEncodingCombobox.setEnabled(false);
        charsetEncodingCombobox.setSelectedIndex(0);
    }//GEN-LAST:event_fileTypeBinaryActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel anonymousLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton anonymousNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton anonymousYes;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.ButtonGroup buttonGroup6;
    private javax.swing.ButtonGroup buttonGroup7;
    private com.mirth.connect.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private com.mirth.connect.client.ui.components.MirthTextField directoryField;
    private javax.swing.JLabel directoryLabel;
    private javax.swing.JLabel encodingLabel;
    private com.mirth.connect.client.ui.components.MirthSyntaxTextArea fileContentsTextPane;
    private com.mirth.connect.client.ui.components.MirthRadioButton fileExistsAppendRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton fileExistsErrorRadio;
    private javax.swing.JLabel fileExistsLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton fileExistsOverwriteRadio;
    private com.mirth.connect.client.ui.components.MirthTextField fileNameField;
    private javax.swing.JLabel fileNameLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton fileTypeASCII;
    private com.mirth.connect.client.ui.components.MirthRadioButton fileTypeBinary;
    private javax.swing.JLabel fileTypeLabel;
    private com.mirth.connect.client.ui.components.MirthTextField hostField;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JLabel passiveModeLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton passiveModeNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton passiveModeYes;
    private com.mirth.connect.client.ui.components.MirthPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private com.mirth.connect.client.ui.components.MirthTextField pathField;
    private javax.swing.JLabel pathLabel;
    private com.mirth.connect.client.ui.components.MirthComboBox schemeComboBox;
    private javax.swing.JLabel schemeLabel;
    private javax.swing.JLabel secureModeLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton secureModeNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton secureModeYes;
    private javax.swing.JLabel tempFileLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton tempFileNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton tempFileYesRadio;
    private javax.swing.JLabel templateLabel;
    private javax.swing.JButton testConnection;
    private com.mirth.connect.client.ui.components.MirthTextField timeoutField;
    private javax.swing.JLabel timeoutLabel;
    private com.mirth.connect.client.ui.components.MirthTextField usernameField;
    private javax.swing.JLabel usernameLabel;
    private javax.swing.JLabel validateConnectionLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton validateConnectionNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton validateConnectionYes;
    // End of variables declaration//GEN-END:variables
}
