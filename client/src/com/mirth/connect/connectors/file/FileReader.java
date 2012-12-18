/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.util.ConnectionTestResponse;

public class FileReader extends ConnectorSettingsPanel {

    private Logger logger = Logger.getLogger(this.getClass());
    private Frame parent;

    public FileReader() {
        this.parent = PlatformUI.MIRTH_FRAME;

        initComponents();

        fileAge.setDocument(new MirthFieldConstraints(0, false, false, true));
        // ast:encoding activation
        parent.setupCharsetEncodingForConnector(charsetEncodingCombobox);
    }

    @Override
    public String getConnectorName() {
        return new FileReceiverProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        FileReceiverProperties properties = new FileReceiverProperties();

        properties.setScheme(FileScheme.fromDisplayName((String) schemeComboBox.getSelectedItem()));

        if (schemeComboBox.getSelectedItem().equals(FileScheme.FILE.getDisplayName())) {
            properties.setHost(directoryField.getText().replace('\\', '/'));
        } else {
            properties.setHost(hostField.getText() + "/" + pathField.getText());
        }

        properties.setIgnoreDot(ignoreDotFilesYesRadio.isSelected());

        if (anonymousYes.isSelected()) {
            properties.setAnonymous(true);
        } else {
            properties.setAnonymous(false);
        }

        properties.setUsername(usernameField.getText());
        properties.setPassword(new String(passwordField.getPassword()));

        properties.setTimeout(timeoutField.getText());

        properties.setSecure(secureModeYes.isSelected());
        properties.setPassive(passiveModeYes.isSelected());
        properties.setValidateConnection(validateConnectionYes.isSelected());

        properties.setMoveToPattern(moveToPattern.getText());
        properties.setMoveToDirectory(moveToDirectory.getText().replace('\\', '/'));
        properties.setMoveToErrorDirectory(errorMoveToDirectory.getText().replace('\\', '/'));

        properties.setAutoDelete(deleteAfterReadYes.isSelected());

        properties.setCheckFileAge(checkFileAgeYes.isSelected());
        properties.setFileAge(fileAge.getText());

        if (((String) sortBy.getSelectedItem()).equals("Name")) {
            properties.setSortBy(FileReceiverProperties.SORT_BY_NAME);
        } else if (((String) sortBy.getSelectedItem()).equals("Size")) {
            properties.setSortBy(FileReceiverProperties.SORT_BY_SIZE);
        } else if (((String) sortBy.getSelectedItem()).equals("Date")) {
            properties.setSortBy(FileReceiverProperties.SORT_BY_DATE);
        }

        properties.setCharsetEncoding(parent.getSelectedEncodingForConnector(charsetEncodingCombobox));
        properties.setFileFilter(fileNameFilter.getText());
        properties.setRegex(filenameFilterRegexCheckBox.isSelected());
        properties.setProcessBatch(processBatchFilesYes.isSelected());
        properties.setBinary(fileTypeBinary.isSelected());

        logger.debug("getProperties: properties=" + properties);

        return properties;
    }

    /**
     * Parses the scheme and URL to determine the values for the directory, host
     * and path fields, optionally storing them to the fields, highlighting
     * field errors, or just testing for valid values.
     *
     * @param props The connector properties from which to take the values.
     * @param store If true, the parsed values are stored to the corresponding
     * form controls.
     * @param highlight If true, fields for which the parsed values are invalid
     * are highlighted.
     */
    private boolean setDirHostPath(FileReceiverProperties props, boolean store, boolean highlight) {

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
        FileReceiverProperties props = (FileReceiverProperties) properties;

        FileScheme scheme = props.getScheme();
        schemeComboBox.setSelectedItem(props.getScheme().getDisplayName());

        schemeComboBoxActionPerformed(null);

        setDirHostPath(props, true, false);

        if (props.isIgnoreDot()) {
            ignoreDotFilesYesRadio.setSelected(true);
        } else {
            ignoreDotFilesNoRadio.setSelected(true);
        }

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

        moveToPattern.setText(props.getMoveToPattern());
        moveToDirectory.setText(props.getMoveToDirectory());
        errorMoveToDirectory.setText(props.getMoveToErrorDirectory());

        if (props.isAutoDelete()) {
            deleteAfterReadYes.setSelected(true);
            deleteAfterReadNo.setSelected(false);
            deleteAfterReadYesActionPerformed(null);
        } else {
            deleteAfterReadYes.setSelected(false);
            deleteAfterReadNo.setSelected(true);
            deleteAfterReadNoActionPerformed(null);
        }

        if (props.isCheckFileAge()) {
            checkFileAgeYes.setSelected(true);
            checkFileAgeNo.setSelected(false);
            checkFileAgeYesActionPerformed(null);
        } else {
            checkFileAgeYes.setSelected(false);
            checkFileAgeNo.setSelected(true);
            checkFileAgeNoActionPerformed(null);
        }

        fileAge.setText(props.getFileAge());

        if (props.getSortBy().equals(FileReceiverProperties.SORT_BY_NAME)) {
            sortBy.setSelectedItem("Name");
        } else if (props.getSortBy().equals(FileReceiverProperties.SORT_BY_SIZE)) {
            sortBy.setSelectedItem("Size");
        } else if (props.getSortBy().equals(FileReceiverProperties.SORT_BY_DATE)) {
            sortBy.setSelectedItem("Date");
        }

        parent.setPreviousSelectedEncodingForConnector(charsetEncodingCombobox, props.getCharsetEncoding());

        fileNameFilter.setText(props.getFileFilter());

        if (props.isRegex()) {
            filenameFilterRegexCheckBox.setSelected(true);
        } else {
            filenameFilterRegexCheckBox.setSelected(false);
        }

        if (props.isBinary()) {
            fileTypeBinary.setSelected(true);
            fileTypeASCII.setSelected(false);
            fileTypeBinaryActionPerformed(null);
        } else {
            fileTypeBinary.setSelected(false);
            fileTypeASCII.setSelected(true);
            fileTypeASCIIActionPerformed(null);
        }

        if (props.isProcessBatch()) {
            processBatchFilesYes.setSelected(true);
            processBatchFilesNo.setSelected(false);
        } else {
            processBatchFilesYes.setSelected(false);
            processBatchFilesNo.setSelected(true);
        }
    }

    @Override
    public ConnectorProperties getDefaults() {
        return new FileReceiverProperties();
    }

    @Override
    public boolean checkProperties(ConnectorProperties properties, boolean highlight) {
        FileReceiverProperties props = (FileReceiverProperties) properties;

        boolean valid = true;

        valid = setDirHostPath(props, false, highlight);

        if (props.getFileFilter().length() == 0) {
            valid = false;
            if (highlight) {
                fileNameFilter.setBackground(UIConstants.INVALID_COLOR);
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

        if (props.isCheckFileAge()) {
            if (props.getFileAge().length() == 0) {
                valid = false;
                if (highlight) {
                    fileAge.setBackground(UIConstants.INVALID_COLOR);
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
        fileNameFilter.setBackground(null);
        fileAge.setBackground(null);
        usernameField.setBackground(null);
        passwordField.setBackground(null);
        timeoutField.setBackground(null);
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
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        buttonGroup6 = new javax.swing.ButtonGroup();
        buttonGroup7 = new javax.swing.ButtonGroup();
        buttonGroup8 = new javax.swing.ButtonGroup();
        buttonGroup9 = new javax.swing.ButtonGroup();
        ignoreDotFilesButtonGroup = new javax.swing.ButtonGroup();
        schemeLabel = new javax.swing.JLabel();
        schemeComboBox = new com.mirth.connect.client.ui.components.MirthComboBox();
        directoryLabel = new javax.swing.JLabel();
        directoryField = new com.mirth.connect.client.ui.components.MirthTextField();
        hostLabel = new javax.swing.JLabel();
        hostField = new com.mirth.connect.client.ui.components.MirthTextField();
        pathLabel = new javax.swing.JLabel();
        pathField = new com.mirth.connect.client.ui.components.MirthTextField();
        filenameFilterLabel = new javax.swing.JLabel();
        fileNameFilter = new com.mirth.connect.client.ui.components.MirthTextField();
        moveToDirectoryLabel = new javax.swing.JLabel();
        moveToPattern = new com.mirth.connect.client.ui.components.MirthTextField();
        moveToDirectory = new com.mirth.connect.client.ui.components.MirthTextField();
        moveToFileLabel = new javax.swing.JLabel();
        deleteAfterReadLabel = new javax.swing.JLabel();
        checkFileAgeLabel = new javax.swing.JLabel();
        fileAgeLabel = new javax.swing.JLabel();
        deleteAfterReadYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        deleteAfterReadNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        checkFileAgeYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        checkFileAgeNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        fileAge = new com.mirth.connect.client.ui.components.MirthTextField();
        sortFilesByLabel = new javax.swing.JLabel();
        sortBy = new com.mirth.connect.client.ui.components.MirthComboBox();
        charsetEncodingCombobox = new com.mirth.connect.client.ui.components.MirthComboBox();
        encodingLabel = new javax.swing.JLabel();
        processBatchFilesLabel = new javax.swing.JLabel();
        processBatchFilesYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        processBatchFilesNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        fileTypeASCII = new com.mirth.connect.client.ui.components.MirthRadioButton();
        fileTypeBinary = new com.mirth.connect.client.ui.components.MirthRadioButton();
        fileTypeLabel = new javax.swing.JLabel();
        errorMoveToDirectoryLabel = new javax.swing.JLabel();
        errorMoveToDirectory = new com.mirth.connect.client.ui.components.MirthTextField();
        anonymousLabel = new javax.swing.JLabel();
        anonymousYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        anonymousNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        usernameLabel = new javax.swing.JLabel();
        usernameField = new com.mirth.connect.client.ui.components.MirthTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new com.mirth.connect.client.ui.components.MirthPasswordField();
        validateConnectionLabel = new javax.swing.JLabel();
        validateConnectionYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        validateConnectionNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        secureModeLabel = new javax.swing.JLabel();
        secureModeYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        secureModeNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        passiveModeLabel = new javax.swing.JLabel();
        testConnection = new javax.swing.JButton();
        passiveModeYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        passiveModeNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        filenameFilterRegexCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        timeoutField = new com.mirth.connect.client.ui.components.MirthTextField();
        timeoutLabel = new javax.swing.JLabel();
        ignoreDotFilesYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        ignoreDotFilesNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        ignoreDotFilesLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        mirthVariableList1 = new com.mirth.connect.client.ui.components.MirthVariableList();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        schemeLabel.setText("Method:");

        schemeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "file", "ftp", "sftp", "smb", "webdav" }));
        schemeComboBox.setToolTipText("The basic method used to access files to be read - file (local filesystem), FTP, SFTP, Samba share, or WebDAV");
        schemeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                schemeComboBoxActionPerformed(evt);
            }
        });

        directoryLabel.setText("Directory:");

        directoryField.setToolTipText("The directory (folder) in which the files to be read can be found.");

        hostLabel.setText("ftp://");

        hostField.setToolTipText("The name or IP address of the host (computer) on which the files to be read can be found.");

        pathLabel.setText("/");

        pathField.setToolTipText("The directory (folder) in which the files to be read can be found.");

        filenameFilterLabel.setText("Filename Filter Pattern:");

        fileNameFilter.setToolTipText("<html>The pattern which names of files must match in order to be read.<br>Files with names that do not match the pattern will be ignored.</html>");

        moveToDirectoryLabel.setText("Move-to Directory:");

        moveToPattern.setToolTipText("<html>If successfully processed files should be renamed, enter the new name here.<br>The filename specified may include template substitutions from the list to the right.<br>If this field is left empty, successfully processed files will not be renamed.</html>");

        moveToDirectory.setToolTipText("<html>If successfully processed files should be moved to a different directory (folder), enter that directory here.<br>The directory name specified may include template substitutions from the list to the right.<br>If this field is left empty, successfully processed files will not be moved to a different directory.</html>");

        moveToFileLabel.setText("Move-to File Name:");

        deleteAfterReadLabel.setText("Delete File After Read:");

        checkFileAgeLabel.setText("Check File Age:");

        fileAgeLabel.setText("File Age (ms):");

        deleteAfterReadYes.setBackground(new java.awt.Color(255, 255, 255));
        deleteAfterReadYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup5.add(deleteAfterReadYes);
        deleteAfterReadYes.setText("Yes");
        deleteAfterReadYes.setToolTipText("Select Yes to delete files after they are processed.");
        deleteAfterReadYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        deleteAfterReadYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteAfterReadYesActionPerformed(evt);
            }
        });

        deleteAfterReadNo.setBackground(new java.awt.Color(255, 255, 255));
        deleteAfterReadNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup5.add(deleteAfterReadNo);
        deleteAfterReadNo.setSelected(true);
        deleteAfterReadNo.setText("No");
        deleteAfterReadNo.setToolTipText("Select No to not delete files after they are processed.");
        deleteAfterReadNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        deleteAfterReadNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteAfterReadNoActionPerformed(evt);
            }
        });

        checkFileAgeYes.setBackground(new java.awt.Color(255, 255, 255));
        checkFileAgeYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(checkFileAgeYes);
        checkFileAgeYes.setText("Yes");
        checkFileAgeYes.setToolTipText("Select Yes to skip files that are created within the specified age below.");
        checkFileAgeYes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        checkFileAgeYes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkFileAgeYesActionPerformed(evt);
            }
        });

        checkFileAgeNo.setBackground(new java.awt.Color(255, 255, 255));
        checkFileAgeNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup6.add(checkFileAgeNo);
        checkFileAgeNo.setSelected(true);
        checkFileAgeNo.setText("No");
        checkFileAgeNo.setToolTipText("Select No to process files regardless of age.");
        checkFileAgeNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        checkFileAgeNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkFileAgeNoActionPerformed(evt);
            }
        });

        fileAge.setToolTipText("If Check File Age Yes is selected, only the files created that are older than the specified value in milliseconds will be processed.");

        sortFilesByLabel.setText("Sort Files By:");

        sortBy.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Date", "Name", "Size" }));
        sortBy.setToolTipText("<html>Selects the order in which files should be processed, if there are multiple files available to be processed.<br>Files can be processed by Date (oldest last modification date first), Size (smallest first) or name (a before z, etc.).</html>");
        sortBy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortByActionPerformed(evt);
            }
        });

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Default", "UTF-8", "ISO-8859-1", "UTF-16 (le)", "UTF-16 (be)", "UTF-16 (bom)", "US-ASCII" }));
        charsetEncodingCombobox.setToolTipText("If File Type ASCII is selected, select the character set encoding (ASCII, UTF-8, etc.) to be used in reading the contents of each file.");
        charsetEncodingCombobox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                charsetEncodingComboboxActionPerformed(evt);
            }
        });

        encodingLabel.setText("Encoding:");

        processBatchFilesLabel.setText("Process Batch Files:");

        processBatchFilesYes.setBackground(new java.awt.Color(255, 255, 255));
        processBatchFilesYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup8.add(processBatchFilesYes);
        processBatchFilesYes.setText("Yes");
        processBatchFilesYes.setToolTipText("Select Yes to process all messages in each file.");
        processBatchFilesYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        processBatchFilesNo.setBackground(new java.awt.Color(255, 255, 255));
        processBatchFilesNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup8.add(processBatchFilesNo);
        processBatchFilesNo.setSelected(true);
        processBatchFilesNo.setText("No");
        processBatchFilesNo.setToolTipText("Select No to process the entire contents of the file as a single message.");
        processBatchFilesNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        processBatchFilesNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                processBatchFilesNoActionPerformed(evt);
            }
        });

        fileTypeASCII.setBackground(new java.awt.Color(255, 255, 255));
        fileTypeASCII.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup7.add(fileTypeASCII);
        fileTypeASCII.setSelected(true);
        fileTypeASCII.setText("ASCII");
        fileTypeASCII.setToolTipText("Select No if files contain text (ASCII is a misnomer here).");
        fileTypeASCII.setMargin(new java.awt.Insets(0, 0, 0, 0));
        fileTypeASCII.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileTypeASCIIActionPerformed(evt);
            }
        });

        fileTypeBinary.setBackground(new java.awt.Color(255, 255, 255));
        fileTypeBinary.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup7.add(fileTypeBinary);
        fileTypeBinary.setText("Binary");
        fileTypeBinary.setToolTipText("Select Yes if files contain binary data which should be Base64 encoded before processing.");
        fileTypeBinary.setMargin(new java.awt.Insets(0, 0, 0, 0));
        fileTypeBinary.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileTypeBinaryActionPerformed(evt);
            }
        });

        fileTypeLabel.setText("File Type:");

        errorMoveToDirectoryLabel.setText("Error Move-to Directory:");

        errorMoveToDirectory.setToolTipText("<html>If files which cause processing errors should be moved to a different directory (folder), enter that directory here.<br>The directory name specified may include template substitutions from the list to the right.<br>If this field is left empty, files which cause processing errors will not be moved to a different directory.</html>");

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

        secureModeLabel.setText("Secure Mode:");

        secureModeYes.setBackground(new java.awt.Color(255, 255, 255));
        secureModeYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup9.add(secureModeYes);
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
        buttonGroup9.add(secureModeNo);
        secureModeNo.setSelected(true);
        secureModeNo.setText("No");
        secureModeNo.setToolTipText("<html>Select Yes to connect to the server via HTTPS.<br>Select No to connect via HTTP.</html>");
        secureModeNo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        secureModeNo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                secureModeNoActionPerformed(evt);
            }
        });

        passiveModeLabel.setText("Passive Mode:");

        testConnection.setText("Test Read");
        testConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testConnectionActionPerformed(evt);
            }
        });

        passiveModeYes.setBackground(new java.awt.Color(255, 255, 255));
        passiveModeYes.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(passiveModeYes);
        passiveModeYes.setText("Yes");
        passiveModeYes.setToolTipText("<html>Select Yes to connect to the server in \"passive mode\".<br>Passive mode sometimes allows a connection through a firewall that normal mode does not.</html>");
        passiveModeYes.setMargin(new java.awt.Insets(0, 0, 0, 0));

        passiveModeNo.setBackground(new java.awt.Color(255, 255, 255));
        passiveModeNo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup2.add(passiveModeNo);
        passiveModeNo.setSelected(true);
        passiveModeNo.setText("No");
        passiveModeNo.setToolTipText("Select Yes to connect to the server in \"normal mode\" as opposed to passive mode.");
        passiveModeNo.setMargin(new java.awt.Insets(0, 0, 0, 0));

        filenameFilterRegexCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        filenameFilterRegexCheckBox.setText("Regular Expression");
        filenameFilterRegexCheckBox.setToolTipText("<html>If Regex is checked, the pattern is treated as a regular expression.<br>If Regex is not checked, it is treated as a pattern that supports wildcards and a comma separated list.</html>");

        timeoutField.setToolTipText("The socket timeout (in ms) for connecting to the server.");

        timeoutLabel.setText("Timeout (ms):");

        ignoreDotFilesYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        ignoreDotFilesYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ignoreDotFilesButtonGroup.add(ignoreDotFilesYesRadio);
        ignoreDotFilesYesRadio.setText("Yes");
        ignoreDotFilesYesRadio.setToolTipText("Select Yes to ignore all files starting with a period.");
        ignoreDotFilesYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        ignoreDotFilesNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        ignoreDotFilesNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ignoreDotFilesButtonGroup.add(ignoreDotFilesNoRadio);
        ignoreDotFilesNoRadio.setSelected(true);
        ignoreDotFilesNoRadio.setText("No");
        ignoreDotFilesNoRadio.setToolTipText("Select No to process files starting with a period.");
        ignoreDotFilesNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        ignoreDotFilesLabel.setText("Ignore . files:");

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        mirthVariableList1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        mirthVariableList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "DATE", "COUNT", "UUID", "SYSTIME", "originalFilename" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(mirthVariableList1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(encodingLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(processBatchFilesLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(errorMoveToDirectoryLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(fileTypeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(ignoreDotFilesLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(moveToFileLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(deleteAfterReadLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(checkFileAgeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(fileAgeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(schemeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(hostLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(sortFilesByLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(directoryLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(passiveModeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(moveToDirectoryLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(timeoutLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(passwordLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(anonymousLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(usernameLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(filenameFilterLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(validateConnectionLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(secureModeLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(ignoreDotFilesYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ignoreDotFilesNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(timeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(hostField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pathLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pathField, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(schemeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(testConnection))
                    .addComponent(directoryField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fileNameFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(filenameFilterRegexCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(anonymousYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(anonymousNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(passiveModeYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(passiveModeNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                        .addComponent(fileTypeBinary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fileTypeASCII, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(processBatchFilesYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(processBatchFilesNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(deleteAfterReadYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteAfterReadNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(fileAge, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sortBy, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(checkFileAgeYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(checkFileAgeNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(errorMoveToDirectory, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(moveToPattern, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(moveToDirectory, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(21, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(schemeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(schemeLabel)
                    .addComponent(testConnection))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(directoryLabel)
                    .addComponent(directoryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hostLabel)
                    .addComponent(pathLabel)
                    .addComponent(hostField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pathField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filenameFilterLabel)
                    .addComponent(fileNameFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filenameFilterRegexCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ignoreDotFilesYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ignoreDotFilesNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ignoreDotFilesLabel))
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
                    .addComponent(validateConnectionLabel)
                    .addComponent(validateConnectionYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(validateConnectionNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(deleteAfterReadLabel)
                    .addComponent(deleteAfterReadYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteAfterReadNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(moveToDirectoryLabel)
                            .addComponent(moveToDirectory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(moveToFileLabel)
                            .addComponent(moveToPattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(errorMoveToDirectoryLabel)
                            .addComponent(errorMoveToDirectory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(checkFileAgeLabel)
                            .addComponent(checkFileAgeYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(checkFileAgeNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileAgeLabel)
                    .addComponent(fileAge, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sortFilesByLabel)
                    .addComponent(sortBy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(processBatchFilesLabel)
                    .addComponent(processBatchFilesYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(processBatchFilesNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

    private void onSchemeChange(boolean enableHost, boolean anonymous, FileScheme scheme) {
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

        // reset the other fields.
        anonymousLabel.setEnabled(false);
        anonymousYes.setEnabled(false);
        anonymousNo.setEnabled(false);
        passiveModeLabel.setEnabled(false);
        passiveModeYes.setEnabled(false);
        passiveModeNo.setEnabled(false);
        validateConnectionLabel.setEnabled(false);
        validateConnectionYes.setEnabled(false);
        validateConnectionNo.setEnabled(false);
        secureModeLabel.setEnabled(false);
        secureModeYes.setEnabled(false);
        secureModeNo.setEnabled(false);
        timeoutLabel.setEnabled(false);
        timeoutField.setEnabled(false);

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

            onSchemeChange(false, true, FileScheme.FILE);
            hostField.setText("");
        } // else if FTP is selected
        else if (text.equals(FileScheme.FTP.getDisplayName())) {

            onSchemeChange(true, anonymousYes.isSelected(), FileScheme.FTP);
            hostLabel.setText("ftp://");
        } // else if SFTP is selected
        else if (text.equals(FileScheme.SFTP.getDisplayName())) {

            onSchemeChange(true, false, FileScheme.SFTP);
            hostLabel.setText("sftp://");
        } // else if SMB is selected
        else if (text.equals(FileScheme.SMB.getDisplayName())) {

            onSchemeChange(true, false, FileScheme.SMB);
            hostLabel.setText("smb://");
        } // else if WEBDAV is selected
        else if (text.equals(FileScheme.WEBDAV.getDisplayName())) {

            onSchemeChange(true, anonymousYes.isSelected(), FileScheme.WEBDAV);
            if (secureModeYes.isSelected()) {
                hostLabel.setText("https://");
            } else {
                hostLabel.setText("http://");
            }
        }
    }//GEN-LAST:event_schemeComboBoxActionPerformed

    private void sortByActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortByActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_sortByActionPerformed

    private void deleteAfterReadYesActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_deleteAfterReadYesActionPerformed
    {//GEN-HEADEREND:event_deleteAfterReadYesActionPerformed
        moveToDirectory.setEnabled(false);
        moveToPattern.setEnabled(false);

        moveToDirectoryLabel.setEnabled(false);
        moveToFileLabel.setEnabled(false);

        moveToDirectory.setText("");
        moveToPattern.setText("");
    }//GEN-LAST:event_deleteAfterReadYesActionPerformed

    private void deleteAfterReadNoActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_deleteAfterReadNoActionPerformed
    {//GEN-HEADEREND:event_deleteAfterReadNoActionPerformed
        moveToDirectory.setEnabled(true);
        moveToPattern.setEnabled(true);

        moveToDirectoryLabel.setEnabled(true);
        moveToFileLabel.setEnabled(true);
    }//GEN-LAST:event_deleteAfterReadNoActionPerformed

    private void fileTypeASCIIActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_fileTypeASCIIActionPerformed
    {//GEN-HEADEREND:event_fileTypeASCIIActionPerformed
        encodingLabel.setEnabled(true);
        charsetEncodingCombobox.setEnabled(true);

        processBatchFilesLabel.setEnabled(true);
        processBatchFilesNo.setEnabled(true);
        processBatchFilesYes.setEnabled(true);
    }//GEN-LAST:event_fileTypeASCIIActionPerformed

    private void fileTypeBinaryActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_fileTypeBinaryActionPerformed
    {//GEN-HEADEREND:event_fileTypeBinaryActionPerformed
        encodingLabel.setEnabled(false);
        charsetEncodingCombobox.setEnabled(false);
        charsetEncodingCombobox.setSelectedIndex(0);

        processBatchFilesLabel.setEnabled(false);
        processBatchFilesNo.setSelected(true);
        processBatchFilesNo.setEnabled(false);
        processBatchFilesYes.setEnabled(false);
    }//GEN-LAST:event_fileTypeBinaryActionPerformed

private void testConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testConnectionActionPerformed
    final String workingId = parent.startWorking("Testing connection...");

    SwingWorker worker = new SwingWorker<Void, Void>() {

        public Void doInBackground() {

            try {
                ConnectionTestResponse response = (ConnectionTestResponse) parent.mirthClient.invokeConnectorService(getConnectorName(), "testRead", getProperties());

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

    private void processBatchFilesNoActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_processBatchFilesNoActionPerformed
    {// GEN-HEADEREND:event_processBatchFilesNoActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_processBatchFilesNoActionPerformed

    private void charsetEncodingComboboxActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_charsetEncodingComboboxActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_charsetEncodingComboboxActionPerformed

    private void checkFileAgeNoActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_checkFileAgeNoActionPerformed
    {// GEN-HEADEREND:event_checkFileAgeNoActionPerformed
        fileAgeLabel.setEnabled(false);
        fileAge.setEnabled(false);
    }// GEN-LAST:event_checkFileAgeNoActionPerformed

    private void checkFileAgeYesActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_checkFileAgeYesActionPerformed
    {// GEN-HEADEREND:event_checkFileAgeYesActionPerformed
        fileAgeLabel.setEnabled(true);
        fileAge.setEnabled(true);
    }// GEN-LAST:event_checkFileAgeYesActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel anonymousLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton anonymousNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton anonymousYes;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.ButtonGroup buttonGroup6;
    private javax.swing.ButtonGroup buttonGroup7;
    private javax.swing.ButtonGroup buttonGroup8;
    private javax.swing.ButtonGroup buttonGroup9;
    private com.mirth.connect.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private javax.swing.JLabel checkFileAgeLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton checkFileAgeNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton checkFileAgeYes;
    private javax.swing.JLabel deleteAfterReadLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton deleteAfterReadNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton deleteAfterReadYes;
    private com.mirth.connect.client.ui.components.MirthTextField directoryField;
    private javax.swing.JLabel directoryLabel;
    private javax.swing.JLabel encodingLabel;
    private com.mirth.connect.client.ui.components.MirthTextField errorMoveToDirectory;
    private javax.swing.JLabel errorMoveToDirectoryLabel;
    private com.mirth.connect.client.ui.components.MirthTextField fileAge;
    private javax.swing.JLabel fileAgeLabel;
    private com.mirth.connect.client.ui.components.MirthTextField fileNameFilter;
    private com.mirth.connect.client.ui.components.MirthRadioButton fileTypeASCII;
    private com.mirth.connect.client.ui.components.MirthRadioButton fileTypeBinary;
    private javax.swing.JLabel fileTypeLabel;
    private javax.swing.JLabel filenameFilterLabel;
    private com.mirth.connect.client.ui.components.MirthCheckBox filenameFilterRegexCheckBox;
    private com.mirth.connect.client.ui.components.MirthTextField hostField;
    private javax.swing.JLabel hostLabel;
    private javax.swing.ButtonGroup ignoreDotFilesButtonGroup;
    private javax.swing.JLabel ignoreDotFilesLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton ignoreDotFilesNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton ignoreDotFilesYesRadio;
    private javax.swing.JScrollPane jScrollPane1;
    private com.mirth.connect.client.ui.components.MirthVariableList mirthVariableList1;
    private com.mirth.connect.client.ui.components.MirthTextField moveToDirectory;
    private javax.swing.JLabel moveToDirectoryLabel;
    private javax.swing.JLabel moveToFileLabel;
    private com.mirth.connect.client.ui.components.MirthTextField moveToPattern;
    private javax.swing.JLabel passiveModeLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton passiveModeNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton passiveModeYes;
    private com.mirth.connect.client.ui.components.MirthPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private com.mirth.connect.client.ui.components.MirthTextField pathField;
    private javax.swing.JLabel pathLabel;
    private javax.swing.JLabel processBatchFilesLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton processBatchFilesNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton processBatchFilesYes;
    private com.mirth.connect.client.ui.components.MirthComboBox schemeComboBox;
    private javax.swing.JLabel schemeLabel;
    private javax.swing.JLabel secureModeLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton secureModeNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton secureModeYes;
    private com.mirth.connect.client.ui.components.MirthComboBox sortBy;
    private javax.swing.JLabel sortFilesByLabel;
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
