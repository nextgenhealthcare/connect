/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package com.mirth.connect.connectors.file;

import java.util.Properties;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.connectors.ConnectorClass;
import com.mirth.connect.util.ConnectionTestResponse;

/**
 * A form that extends from ConnectorClass. All methods implemented are
 * described in ConnectorClass.
 */
public class FileReader extends ConnectorClass {

    private Logger logger = Logger.getLogger(this.getClass());

    /** Creates new form FileReader */
    public FileReader() {
        name = FileReaderProperties.name;
        initComponents();
        pollingFrequency.setDocument(new MirthFieldConstraints(0, false, false, true));
        fileAge.setDocument(new MirthFieldConstraints(0, false, false, true));
        // ast:encoding activation
        parent.setupCharsetEncodingForConnector(charsetEncodingCombobox);
    }

    /** Converts the values of the form fields to a Properties */
    public Properties getProperties() {
        Properties properties = new Properties();
        properties.put(FileReaderProperties.DATATYPE, name);

        if (((String) schemeComboBox.getSelectedItem()).equals("file")) {
            properties.put(FileReaderProperties.FILE_SCHEME, FileReaderProperties.SCHEME_FILE);
        } else if (((String) schemeComboBox.getSelectedItem()).equals("ftp")) {
            properties.put(FileReaderProperties.FILE_SCHEME, FileReaderProperties.SCHEME_FTP);
        } else if (((String) schemeComboBox.getSelectedItem()).equals("sftp")) {
            properties.put(FileReaderProperties.FILE_SCHEME, FileReaderProperties.SCHEME_SFTP);
        } else if (((String) schemeComboBox.getSelectedItem()).equals("smb")) {
            properties.put(FileReaderProperties.FILE_SCHEME, FileReaderProperties.SCHEME_SMB);
        } else if (((String) schemeComboBox.getSelectedItem()).equals("webdav")) {
            properties.put(FileReaderProperties.FILE_SCHEME, FileReaderProperties.SCHEME_WEBDAV);
        } else {
            // This "can't happen"
            logger.error("Unrecognized this.schemeComboBox value '" + schemeComboBox.getSelectedItem() + "', using 'file' instead");
            properties.put(FileReaderProperties.FILE_SCHEME, FileReaderProperties.SCHEME_FILE);
        }

        if (schemeComboBox.getSelectedItem().equals("file")) {
            properties.put(FileReaderProperties.FILE_HOST, directoryField.getText().replace('\\', '/'));
        } else {
            properties.put(FileReaderProperties.FILE_HOST, hostField.getText() + "/" + pathField.getText());
        }
        
        if (ignoreDotFilesYesRadio.isSelected()) {
            properties.put(FileReaderProperties.FILE_IGNORE_DOT, UIConstants.YES_OPTION);
        } else {
            properties.put(FileReaderProperties.FILE_IGNORE_DOT, UIConstants.NO_OPTION);
        }

        if (anonymousYes.isSelected()) {
            properties.put(FileReaderProperties.FILE_ANONYMOUS, UIConstants.YES_OPTION);
            if (((String) schemeComboBox.getSelectedItem()).equals(FileReaderProperties.SCHEME_WEBDAV)) {
                properties.put(FileReaderProperties.FILE_USERNAME, "null");
                properties.put(FileReaderProperties.FILE_PASSWORD, "null");
            }
        } else {
            properties.put(FileReaderProperties.FILE_ANONYMOUS, UIConstants.NO_OPTION);
        }

        properties.put(FileReaderProperties.FILE_USERNAME, usernameField.getText());
        properties.put(FileReaderProperties.FILE_PASSWORD, new String(passwordField.getPassword()));

        properties.put(FileReaderProperties.FILE_TIMEOUT, timeoutField.getText());

        if (secureModeYes.isSelected()) {
            properties.put(FileReaderProperties.FILE_SECURE_MODE, UIConstants.YES_OPTION);
        } else {
            properties.put(FileReaderProperties.FILE_SECURE_MODE, UIConstants.NO_OPTION);
        }

        if (passiveModeYes.isSelected()) {
            properties.put(FileReaderProperties.FILE_PASSIVE_MODE, UIConstants.YES_OPTION);
        } else {
            properties.put(FileReaderProperties.FILE_PASSIVE_MODE, UIConstants.NO_OPTION);
        }

        if (validateConnectionYes.isSelected()) {
            properties.put(FileReaderProperties.FILE_VALIDATE_CONNECTION, UIConstants.YES_OPTION);
        } else {
            properties.put(FileReaderProperties.FILE_VALIDATE_CONNECTION, UIConstants.NO_OPTION);
        }

        properties.put(FileReaderProperties.FILE_MOVE_TO_PATTERN, moveToPattern.getText());
        properties.put(FileReaderProperties.FILE_MOVE_TO_DIRECTORY, moveToDirectory.getText().replace('\\', '/'));
        properties.put(FileReaderProperties.FILE_MOVE_TO_ERROR_DIRECTORY, errorMoveToDirectory.getText().replace('\\', '/'));

        if (deleteAfterReadYes.isSelected()) {
            properties.put(FileReaderProperties.FILE_DELETE_AFTER_READ, UIConstants.YES_OPTION);
        } else {
            properties.put(FileReaderProperties.FILE_DELETE_AFTER_READ, UIConstants.NO_OPTION);
        }

        if (checkFileAgeYes.isSelected()) {
            properties.put(FileReaderProperties.FILE_CHECK_FILE_AGE, UIConstants.YES_OPTION);
        } else {
            properties.put(FileReaderProperties.FILE_CHECK_FILE_AGE, UIConstants.NO_OPTION);
        }

        properties.put(FileReaderProperties.FILE_FILE_AGE, fileAge.getText());

        if (((String) sortBy.getSelectedItem()).equals("Name")) {
            properties.put(FileReaderProperties.FILE_SORT_BY, FileReaderProperties.SORT_BY_NAME);
        } else if (((String) sortBy.getSelectedItem()).equals("Size")) {
            properties.put(FileReaderProperties.FILE_SORT_BY, FileReaderProperties.SORT_BY_SIZE);
        } else if (((String) sortBy.getSelectedItem()).equals("Date")) {
            properties.put(FileReaderProperties.FILE_SORT_BY, FileReaderProperties.SORT_BY_DATE);
        }

        properties.put(FileReaderProperties.CONNECTOR_CHARSET_ENCODING, parent.getSelectedEncodingForConnector(charsetEncodingCombobox));

        properties.put(FileReaderProperties.FILE_FILTER, fileNameFilter.getText());

        if (filenameFilterRegexCheckBox.isSelected()) {
            properties.put(FileReaderProperties.FILE_REGEX, UIConstants.YES_OPTION);
        } else {
            properties.put(FileReaderProperties.FILE_REGEX, UIConstants.NO_OPTION);
        }

        if (processBatchFilesYes.isSelected()) {
            properties.put(FileReaderProperties.FILE_PROCESS_BATCH_FILES, UIConstants.YES_OPTION);
        } else {
            properties.put(FileReaderProperties.FILE_PROCESS_BATCH_FILES, UIConstants.NO_OPTION);
        }

        if (fileTypeBinary.isSelected()) {
            properties.put(FileReaderProperties.FILE_TYPE, UIConstants.YES_OPTION);
        } else {
            properties.put(FileReaderProperties.FILE_TYPE, UIConstants.NO_OPTION);
        }

        if (pollingIntervalButton.isSelected()) {
            properties.put(FileReaderProperties.FILE_POLLING_TYPE, "interval");
            properties.put(FileReaderProperties.FILE_POLLING_FREQUENCY, pollingFrequency.getText());
        } else {
            properties.put(FileReaderProperties.FILE_POLLING_TYPE, "time");
            properties.put(FileReaderProperties.FILE_POLLING_TIME, pollingTime.getDate());
        }

        logger.debug("getProperties: properties=" + properties);

        return properties;
    }

    /** Parses the scheme and URL to determine the values for the
     * directory, host and path fields, optionally storing them to
     * the fields, highlighting field errors, or just testing for
     * valid values.
     * 
     * @param props The connector properties from which to take the
     * values.
     * @param store If true, the parsed values are stored to the
     * corresponding form controls.
     * @param highlight If true, fields for which the parsed values
     * are invalid are highlighted.
     */
    public boolean setDirHostPath(Properties props, boolean store, boolean highlight) {

        boolean valid = true;
        Object schemeValue = props.get(FileReaderProperties.FILE_SCHEME);
        String hostPropValue = (String) props.get(FileReaderProperties.FILE_HOST);
        String directoryValue = "";
        String hostValue = "";
        String pathValue = "";
        if (schemeValue.equals(FileReaderProperties.SCHEME_FILE)) {

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

    /** Converts a Properties to values of the form fields */
    public void setProperties(Properties props) {
        logger.debug("setProperties: props=" + props);

        resetInvalidProperties();

        Object schemeValue = props.get(FileReaderProperties.FILE_SCHEME);
        if (schemeValue.equals(FileReaderProperties.SCHEME_FILE)) {
            schemeComboBox.setSelectedItem("file");
        } else if (schemeValue.equals(FileReaderProperties.SCHEME_FTP)) {
            schemeComboBox.setSelectedItem("ftp");
        } else if (schemeValue.equals(FileReaderProperties.SCHEME_SFTP)) {
            schemeComboBox.setSelectedItem("sftp");
        } else if (schemeValue.equals(FileReaderProperties.SCHEME_SMB)) {
            schemeComboBox.setSelectedItem("smb");
        } else if (schemeValue.equals(FileReaderProperties.SCHEME_WEBDAV)) {
            schemeComboBox.setSelectedItem("webdav");
        } else {
            // This "can't happen"
            logger.error("Unrecognized props[\"scheme\"] value '" + schemeValue + "', using 'file' instead");
            schemeComboBox.setSelectedItem("file");
        }

        schemeComboBoxActionPerformed(null);

        setDirHostPath(props, true, false);

        if (props.getProperty(FileReaderProperties.FILE_IGNORE_DOT).equalsIgnoreCase(UIConstants.YES_OPTION)) {
            ignoreDotFilesYesRadio.setSelected(true);
        } else {
            ignoreDotFilesNoRadio.setSelected(true);
        }
        
        if (((String) props.get(FileReaderProperties.FILE_ANONYMOUS)).equalsIgnoreCase(UIConstants.YES_OPTION)) {
            anonymousYes.setSelected(true);
            anonymousNo.setSelected(false);
            anonymousYesActionPerformed(null);
        } else {
            anonymousYes.setSelected(false);
            anonymousNo.setSelected(true);
            anonymousNoActionPerformed(null);
            usernameField.setText((String) props.get(FileReaderProperties.FILE_USERNAME));
            passwordField.setText((String) props.get(FileReaderProperties.FILE_PASSWORD));
        }

        timeoutField.setText((String) props.get(FileReaderProperties.FILE_TIMEOUT));

        if (((String) props.get(FileReaderProperties.FILE_SECURE_MODE)).equalsIgnoreCase(UIConstants.YES_OPTION)) {
            secureModeYes.setSelected(true);
            secureModeNo.setSelected(false);
            if (schemeValue.equals(FileReaderProperties.SCHEME_WEBDAV)) {
                hostLabel.setText("https://");
            }
        } else {
            secureModeYes.setSelected(false);
            secureModeNo.setSelected(true);
            if (schemeValue.equals(FileReaderProperties.SCHEME_WEBDAV)) {
                hostLabel.setText("http://");
            }
        }

        if (((String) props.get(FileReaderProperties.FILE_PASSIVE_MODE)).equalsIgnoreCase(UIConstants.YES_OPTION)) {
            passiveModeYes.setSelected(true);
            passiveModeNo.setSelected(false);
        } else {
            passiveModeYes.setSelected(false);
            passiveModeNo.setSelected(true);
        }

        if (((String) props.get(FileReaderProperties.FILE_VALIDATE_CONNECTION)).equalsIgnoreCase(UIConstants.YES_OPTION)) {
            validateConnectionYes.setSelected(true);
            validateConnectionNo.setSelected(false);
        } else {
            validateConnectionYes.setSelected(false);
            validateConnectionNo.setSelected(true);
        }

        moveToPattern.setText((String) props.get(FileReaderProperties.FILE_MOVE_TO_PATTERN));
        moveToDirectory.setText((String) props.get(FileReaderProperties.FILE_MOVE_TO_DIRECTORY));
        errorMoveToDirectory.setText((String) props.get(FileReaderProperties.FILE_MOVE_TO_ERROR_DIRECTORY));

        if (((String) props.get(FileReaderProperties.FILE_DELETE_AFTER_READ)).equalsIgnoreCase(UIConstants.YES_OPTION)) {
            deleteAfterReadYes.setSelected(true);
            deleteAfterReadNo.setSelected(false);
            deleteAfterReadYesActionPerformed(null);
        } else {
            deleteAfterReadYes.setSelected(false);
            deleteAfterReadNo.setSelected(true);
            deleteAfterReadNoActionPerformed(null);
        }
        if (((String) props.get(FileReaderProperties.FILE_CHECK_FILE_AGE)).equalsIgnoreCase(UIConstants.YES_OPTION)) {
            checkFileAgeYes.setSelected(true);
            checkFileAgeNo.setSelected(false);
            checkFileAgeYesActionPerformed(null);
        } else {
            checkFileAgeYes.setSelected(false);
            checkFileAgeNo.setSelected(true);
            checkFileAgeNoActionPerformed(null);
        }

        fileAge.setText((String) props.get(FileReaderProperties.FILE_FILE_AGE));

        if (props.get(FileReaderProperties.FILE_SORT_BY).equals(FileReaderProperties.SORT_BY_NAME)) {
            sortBy.setSelectedItem("Name");
        } else if (props.get(FileReaderProperties.FILE_SORT_BY).equals(FileReaderProperties.SORT_BY_SIZE)) {
            sortBy.setSelectedItem("Size");
        } else if (props.get(FileReaderProperties.FILE_SORT_BY).equals(FileReaderProperties.SORT_BY_DATE)) {
            sortBy.setSelectedItem("Date");
        }

        parent.setPreviousSelectedEncodingForConnector(charsetEncodingCombobox, (String) props.get(FileReaderProperties.CONNECTOR_CHARSET_ENCODING));

        fileNameFilter.setText((String) props.get(FileReaderProperties.FILE_FILTER));

        if (((String) props.get(FileReaderProperties.FILE_REGEX)).equalsIgnoreCase(UIConstants.YES_OPTION)) {
            filenameFilterRegexCheckBox.setSelected(true);
        } else {
            filenameFilterRegexCheckBox.setSelected(false);
        }

        if (((String) props.get(FileReaderProperties.FILE_TYPE)).equalsIgnoreCase(UIConstants.YES_OPTION)) {
            fileTypeBinary.setSelected(true);
            fileTypeASCII.setSelected(false);
            fileTypeBinaryActionPerformed(null);
        } else {
            fileTypeBinary.setSelected(false);
            fileTypeASCII.setSelected(true);
            fileTypeASCIIActionPerformed(null);
        }

        if (((String) props.get(FileReaderProperties.FILE_PROCESS_BATCH_FILES)).equalsIgnoreCase(UIConstants.YES_OPTION)) {
            processBatchFilesYes.setSelected(true);
            processBatchFilesNo.setSelected(false);
        } else {
            processBatchFilesYes.setSelected(false);
            processBatchFilesNo.setSelected(true);
        }

        if (((String) props.get(FileReaderProperties.FILE_POLLING_TYPE)).equalsIgnoreCase("interval")) {
            pollingIntervalButton.setSelected(true);
            pollingTimeButton.setSelected(false);
            pollingIntervalButtonActionPerformed(null);
            pollingFrequency.setText((String) props.get(FileReaderProperties.FILE_POLLING_FREQUENCY));
        } else {
            pollingIntervalButton.setSelected(false);
            pollingTimeButton.setSelected(true);
            pollingTimeButtonActionPerformed(null);
            pollingTime.setDate((String) props.get(FileReaderProperties.FILE_POLLING_TIME));
        }
    }

    /** Returns the default Properties */
    public Properties getDefaults() {
        return new FileReaderProperties().getDefaults();
    }

    /** Tests if the specified Properties are valid, optionally highlighting fields
     * with invalid entries.
     */
    public boolean checkProperties(Properties props, boolean highlight) {
        resetInvalidProperties();
        boolean valid = true;

        valid = setDirHostPath(props, false, highlight);

        if (((String) props.get(FileReaderProperties.FILE_FILTER)).length() == 0) {
            valid = false;
            if (highlight) {
                fileNameFilter.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(FileReaderProperties.FILE_POLLING_TYPE)).equalsIgnoreCase("interval") && ((String) props.get(FileReaderProperties.FILE_POLLING_FREQUENCY)).length() == 0) {
            valid = false;
            if (highlight) {
                pollingFrequency.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(FileReaderProperties.FILE_POLLING_TYPE)).equalsIgnoreCase("time") && ((String) props.get(FileReaderProperties.FILE_POLLING_TIME)).length() == 0) {
            valid = false;
            if (highlight) {
                pollingTime.setBackground(UIConstants.INVALID_COLOR);
            }
        }
        if (((String) props.get(FileReaderProperties.FILE_ANONYMOUS)).equals(UIConstants.NO_OPTION)) {
            if (((String) props.get(FileReaderProperties.FILE_USERNAME)).length() == 0) {
                valid = false;
                if (highlight) {
                    usernameField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
            if (((String) props.get(FileReaderProperties.FILE_PASSWORD)).length() == 0) {
                valid = false;
                if (highlight) {
                    passwordField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }

        Object scheme = props.get(FileReaderProperties.FILE_SCHEME);
        if (scheme.equals(FileReaderProperties.SCHEME_FTP) || scheme.equals(FileReaderProperties.SCHEME_SFTP) || scheme.equals(FileReaderProperties.SCHEME_SMB)) {
            if (((String) props.get(FileReaderProperties.FILE_TIMEOUT)).length() == 0) {
                valid = false;
                if (highlight) {
                    timeoutField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }
        
        if (((String) props.get(FileReaderProperties.FILE_CHECK_FILE_AGE)).equals(UIConstants.YES_OPTION)) {
            if (((String) props.get(FileReaderProperties.FILE_FILE_AGE)).length() == 0) {
                valid = false;
                if (highlight) {
                    fileAge.setBackground(UIConstants.INVALID_COLOR);
                }
            }
        }

        return valid;
    }

    /** Turns off all invalid property value highlighting */
    private void resetInvalidProperties() {
        directoryField.setBackground(null);
        hostField.setBackground(null);
        pathField.setBackground(null);
        fileNameFilter.setBackground(null);
        pollingFrequency.setBackground(null);
        pollingTime.setBackground(null);
        fileAge.setBackground(null);
        usernameField.setBackground(null);
        passwordField.setBackground(null);
        timeoutField.setBackground(null);
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
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
        pollingTypeLabel = new javax.swing.JLabel();
        pollingIntervalButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        pollingTimeButton = new com.mirth.connect.client.ui.components.MirthRadioButton();
        pollingFrequencyLabel = new javax.swing.JLabel();
        pollingFrequency = new com.mirth.connect.client.ui.components.MirthTextField();
        pollingTimeLabel = new javax.swing.JLabel();
        pollingTime = new com.mirth.connect.client.ui.components.MirthTimePicker();
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
        mirthVariableList1 = new com.mirth.connect.client.ui.components.MirthVariableList();
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

        pollingTypeLabel.setText("Polling Type:");

        pollingIntervalButton.setBackground(new java.awt.Color(255, 255, 255));
        pollingIntervalButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup4.add(pollingIntervalButton);
        pollingIntervalButton.setText("Interval");
        pollingIntervalButton.setToolTipText("Records that the time at which polling for files to be read will be specified as the time between polling attempts.");
        pollingIntervalButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        pollingIntervalButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pollingIntervalButtonActionPerformed(evt);
            }
        });

        pollingTimeButton.setBackground(new java.awt.Color(255, 255, 255));
        pollingTimeButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup4.add(pollingTimeButton);
        pollingTimeButton.setText("Time");
        pollingTimeButton.setToolTipText("Records that the time at which polling for files to be read will be specified as the time of day at which a polling attempt will occur each day.");
        pollingTimeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        pollingTimeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pollingTimeButtonActionPerformed(evt);
            }
        });

        pollingFrequencyLabel.setText("Polling Frequency (ms):");

        pollingFrequency.setToolTipText("If the Interval Polling Type is selected, enter the number of milliseconds between polling attempts here.");

        pollingTimeLabel.setText("Polling Time (daily):");

        pollingTime.setToolTipText("If the Time Polling Type is selected, enter the time of day for polling attempts here.");

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

        mirthVariableList1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        mirthVariableList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "DATE", "COUNT", "UUID", "SYSTIME", "ORIGINALNAME" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(anonymousLabel)
                    .addComponent(filenameFilterLabel)
                    .addComponent(directoryLabel)
                    .addComponent(hostLabel)
                    .addComponent(schemeLabel)
                    .addComponent(secureModeLabel)
                    .addComponent(passwordLabel)
                    .addComponent(validateConnectionLabel)
                    .addComponent(pollingTypeLabel)
                    .addComponent(pollingFrequencyLabel)
                    .addComponent(moveToFileLabel)
                    .addComponent(moveToDirectoryLabel)
                    .addComponent(errorMoveToDirectoryLabel)
                    .addComponent(usernameLabel)
                    .addComponent(pollingTimeLabel)
                    .addComponent(passiveModeLabel)
                    .addComponent(deleteAfterReadLabel)
                    .addComponent(processBatchFilesLabel)
                    .addComponent(checkFileAgeLabel)
                    .addComponent(fileAgeLabel)
                    .addComponent(sortFilesByLabel)
                    .addComponent(fileTypeLabel)
                    .addComponent(encodingLabel)
                    .addComponent(timeoutLabel)
                    .addComponent(ignoreDotFilesLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(ignoreDotFilesYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ignoreDotFilesNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(timeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fileAge, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sortBy, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                    .addComponent(pollingFrequency, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                        .addComponent(pollingIntervalButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pollingTimeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(checkFileAgeYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(checkFileAgeNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(errorMoveToDirectory, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(moveToPattern, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(moveToDirectory, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(deleteAfterReadYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(deleteAfterReadNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(pollingTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mirthVariableList1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
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
                    .addComponent(pollingTypeLabel)
                    .addComponent(pollingIntervalButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pollingTimeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pollingFrequencyLabel)
                    .addComponent(pollingFrequency, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pollingTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pollingTimeLabel))
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
                            .addComponent(checkFileAgeNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                            .addComponent(processBatchFilesNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(mirthVariableList1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void onSchemeChange(boolean enableHost, boolean anonymous, String scheme) {
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

        if (scheme.equals(FileReaderProperties.SCHEME_FTP)) {
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
        } else if (scheme.equals(FileReaderProperties.SCHEME_SFTP)) {
            timeoutLabel.setEnabled(true);
            timeoutField.setEnabled(true);
        } else if (scheme.equals(FileReaderProperties.SCHEME_WEBDAV)) {
            anonymousLabel.setEnabled(true);
            anonymousYes.setEnabled(true);
            anonymousNo.setEnabled(true);
            secureModeLabel.setEnabled(true);
            secureModeYes.setEnabled(true);
            secureModeNo.setEnabled(true);

            // set Passive Mode and validate connection to No.
            passiveModeNo.setSelected(true);
            validateConnectionNo.setSelected(true);
        } else if (scheme.equals(FileReaderProperties.SCHEME_SMB)) {
            timeoutLabel.setEnabled(true);
            timeoutField.setEnabled(true);
        }
    }

    private void schemeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_schemeComboBoxActionPerformed

        String text = (String) schemeComboBox.getSelectedItem();

        // if File is selected
        if (text.equals(FileReaderProperties.SCHEME_FILE)) {

            onSchemeChange(false, true, FileReaderProperties.SCHEME_FILE);
            hostField.setText("");
        } // else if FTP is selected
        else if (text.equals(FileReaderProperties.SCHEME_FTP)) {

            onSchemeChange(true, anonymousYes.isSelected(), FileReaderProperties.SCHEME_FTP);
            hostLabel.setText("ftp://");
        } // else if SFTP is selected
        else if (text.equals(FileReaderProperties.SCHEME_SFTP)) {

            onSchemeChange(true, false, FileReaderProperties.SCHEME_SFTP);
            hostLabel.setText("sftp://");
        } // else if SMB is selected
        else if (text.equals(FileReaderProperties.SCHEME_SMB)) {

            onSchemeChange(true, false, FileReaderProperties.SCHEME_SMB);
            hostLabel.setText("smb://");
        } // else if WEBDAV is selected
        else if (text.equals(FileReaderProperties.SCHEME_WEBDAV)) {

            onSchemeChange(true, anonymousYes.isSelected(), FileReaderProperties.SCHEME_WEBDAV);
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

    private void pollingTimeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_pollingTimeButtonActionPerformed
    {//GEN-HEADEREND:event_pollingTimeButtonActionPerformed
        pollingFrequencyLabel.setEnabled(false);
        pollingTimeLabel.setEnabled(true);
        pollingFrequency.setEnabled(false);
        pollingTime.setEnabled(true);

    }//GEN-LAST:event_pollingTimeButtonActionPerformed

    private void pollingIntervalButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_pollingIntervalButtonActionPerformed
    {//GEN-HEADEREND:event_pollingIntervalButtonActionPerformed
        pollingFrequencyLabel.setEnabled(true);
        pollingTimeLabel.setEnabled(false);
        pollingFrequency.setEnabled(true);
        pollingTime.setEnabled(false);
    }//GEN-LAST:event_pollingIntervalButtonActionPerformed

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
                ConnectionTestResponse response = (ConnectionTestResponse) parent.mirthClient.invokeConnectorService(name, "testRead", getProperties());

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
    private javax.swing.ButtonGroup buttonGroup4;
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
    private com.mirth.connect.client.ui.components.MirthTextField pollingFrequency;
    private javax.swing.JLabel pollingFrequencyLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton pollingIntervalButton;
    private com.mirth.connect.client.ui.components.MirthTimePicker pollingTime;
    private com.mirth.connect.client.ui.components.MirthRadioButton pollingTimeButton;
    private javax.swing.JLabel pollingTimeLabel;
    private javax.swing.JLabel pollingTypeLabel;
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
