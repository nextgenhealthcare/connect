/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file;

import java.util.ArrayList;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;

import org.apache.log4j.Logger;

import com.mirth.connect.client.ui.ConnectorTypeDecoration;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.client.ui.panels.reference.ReferenceListFactory;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.CodeTemplate.CodeSnippetType;
import com.mirth.connect.model.CodeTemplate.ContextType;
import com.mirth.connect.util.ConnectionTestResponse;

public class FileReader extends ConnectorSettingsPanel {

    private Logger logger = Logger.getLogger(this.getClass());
    private Frame parent;

    public FileReader() {
        this.parent = PlatformUI.MIRTH_FRAME;

        initComponents();

        afterProcessingActionComboBox.setModel(new DefaultComboBoxModel(new FileAction[] {
                FileAction.NONE, FileAction.MOVE, FileAction.DELETE }));
        errorReadingActionComboBox.setModel(new DefaultComboBoxModel(new FileAction[] {
                FileAction.NONE, FileAction.MOVE, FileAction.DELETE }));
        errorResponseActionComboBox.setModel(new DefaultComboBoxModel(new FileAction[] {
                FileAction.AFTER_PROCESSING, FileAction.MOVE, FileAction.DELETE }));
        fileAge.setDocument(new MirthFieldConstraints(0, false, false, true));
        fileSizeMinimumField.setDocument(new MirthFieldConstraints(0, false, false, true));
        fileSizeMaximumField.setDocument(new MirthFieldConstraints(0, false, false, true));
        // ast:encoding activation
        parent.setupCharsetEncodingForConnector(charsetEncodingCombobox);

        // This is required because of MIRTH-3305
        Map<String, ArrayList<CodeTemplate>> references = ReferenceListFactory.getInstance().getReferences();
        references.put(getConnectorName() + " Functions", getReferenceItems());
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

        properties.setDirectoryRecursion(directoryRecursionYesRadio.isSelected());
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

        properties.setAfterProcessingAction((FileAction) afterProcessingActionComboBox.getSelectedItem());
        properties.setMoveToDirectory(moveToDirectoryField.getText().replace('\\', '/'));
        properties.setMoveToFileName(moveToFileNameField.getText());
        properties.setErrorReadingAction((FileAction) errorReadingActionComboBox.getSelectedItem());
        properties.setErrorResponseAction((FileAction) errorResponseActionComboBox.getSelectedItem());
        properties.setErrorMoveToDirectory(errorMoveToDirectoryField.getText().replace('\\', '/'));
        properties.setErrorMoveToFileName(errorMoveToFileNameField.getText());

        properties.setCheckFileAge(checkFileAgeYes.isSelected());
        properties.setFileAge(fileAge.getText());

        properties.setFileSizeMinimum(fileSizeMinimumField.getText());
        properties.setFileSizeMaximum(fileSizeMaximumField.getText());
        properties.setIgnoreFileSizeMaximum(ignoreFileSizeMaximumCheckBox.isSelected());

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

        if (props.isDirectoryRecursion()) {
            directoryRecursionYesRadio.setSelected(true);
        } else {
            directoryRecursionNoRadio.setSelected(true);
        }

        if (props.isIgnoreDot()) {
            ignoreDotFilesYesRadio.setSelected(true);
        } else {
            ignoreDotFilesNoRadio.setSelected(true);
        }

        if (props.isAnonymous()) {
            anonymousYes.setSelected(true);
            anonymousYesActionPerformed(null);
        } else {
            anonymousNo.setSelected(true);
            anonymousNoActionPerformed(null);
            usernameField.setText(props.getUsername());
            passwordField.setText(props.getPassword());
        }

        timeoutField.setText(props.getTimeout());

        if (props.isSecure()) {
            secureModeYes.setSelected(true);
            if (scheme.equals(FileScheme.WEBDAV)) {
                hostLabel.setText("https://");
            }
        } else {
            secureModeNo.setSelected(true);
            if (scheme.equals(FileScheme.WEBDAV)) {
                hostLabel.setText("http://");
            }
        }

        if (props.isPassive()) {
            passiveModeYes.setSelected(true);
        } else {
            passiveModeNo.setSelected(true);
        }

        if (props.isValidateConnection()) {
            validateConnectionYes.setSelected(true);
        } else {
            validateConnectionNo.setSelected(true);
        }

        afterProcessingActionComboBox.setSelectedItem(props.getAfterProcessingAction());
        afterProcessingActionComboBoxActionPerformed(null);

        moveToDirectoryField.setText(props.getMoveToDirectory());
        moveToFileNameField.setText(props.getMoveToFileName());
        errorReadingActionComboBox.setSelectedItem(props.getErrorReadingAction());
        errorResponseActionComboBox.setSelectedItem(props.getErrorResponseAction());
        errorMoveToDirectoryField.setText(props.getErrorMoveToDirectory());
        errorMoveToFileNameField.setText(props.getErrorMoveToFileName());
        updateErrorFields();

        if (props.isCheckFileAge()) {
            checkFileAgeYes.setSelected(true);
            checkFileAgeYesActionPerformed(null);
        } else {
            checkFileAgeNo.setSelected(true);
            checkFileAgeNoActionPerformed(null);
        }

        fileAge.setText(props.getFileAge());

        fileSizeMinimumField.setText(props.getFileSizeMinimum());
        fileSizeMaximumField.setText(props.getFileSizeMaximum());
        ignoreFileSizeMaximumCheckBox.setSelected(props.isIgnoreFileSizeMaximum());
        ignoreFileSizeMaximumCheckBoxActionPerformed(null);

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
            fileTypeBinaryActionPerformed(null);
        } else {
            fileTypeASCII.setSelected(true);
            fileTypeASCIIActionPerformed(null);
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

        if (props.getFileSizeMinimum().length() == 0) {
            valid = false;
            if (highlight) {
                fileSizeMinimumField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (!props.isIgnoreFileSizeMaximum()) {
            if (props.getFileSizeMaximum().length() == 0) {
                valid = false;
                if (highlight) {
                    fileSizeMaximumField.setBackground(UIConstants.INVALID_COLOR);
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
        fileSizeMinimumField.setBackground(null);
        fileSizeMaximumField.setBackground(null);
        usernameField.setBackground(null);
        passwordField.setBackground(null);
        timeoutField.setBackground(null);
    }

    @Override
    public ArrayList<CodeTemplate> getReferenceItems() {
        ArrayList<CodeTemplate> referenceItems = new ArrayList<CodeTemplate>();

        referenceItems.add(new CodeTemplate("Get Original File Name", "Retrieves the name of the file read by the File Reader.", "sourceMap.get('originalFilename')", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));
        referenceItems.add(new CodeTemplate("Get Original File Directory", "Retrieves the parent directory of the file read by the File Reader.", "sourceMap.get('fileDirectory')", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));
        referenceItems.add(new CodeTemplate("Get Original File Size in Bytes", "Retrieves the size (in bytes) of the file read by the File Reader.", "sourceMap.get('fileSize')", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));
        referenceItems.add(new CodeTemplate("Get Original File Last Modified Timestamp", "Retrieves the last modified timestamp (in milliseconds since January 1st, 1970) of the file read by the File Reader.", "sourceMap.get('fileLastModified')", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));

        return referenceItems;
    }

    @Override
    public void doLocalDecoration(ConnectorTypeDecoration connectorTypeDecoration) {
        if (FileScheme.FTP.getDisplayName().equalsIgnoreCase((String) schemeComboBox.getSelectedItem())) {
            hostLabel.setText("ftp" + (connectorTypeDecoration != null ? "s" : "") + "://");
        }
    }

    @Override
    public void handleConnectorServiceResponse(String method, Object response) {
        if (method.equals(FileServiceMethods.METHOD_TEST_READ)) {
            ConnectionTestResponse connectionTestResponse = (ConnectionTestResponse) response;

            if (connectionTestResponse == null) {
                parent.alertError(parent, "Failed to invoke service.");
            } else if (connectionTestResponse.getType().equals(ConnectionTestResponse.Type.SUCCESS)) {
                parent.alertInformation(parent, connectionTestResponse.getMessage());
            } else {
                parent.alertWarning(parent, connectionTestResponse.getMessage());
            }
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup6 = new javax.swing.ButtonGroup();
        buttonGroup7 = new javax.swing.ButtonGroup();
        buttonGroup8 = new javax.swing.ButtonGroup();
        buttonGroup9 = new javax.swing.ButtonGroup();
        ignoreDotFilesButtonGroup = new javax.swing.ButtonGroup();
        directoryRecursionButtonGroup = new javax.swing.ButtonGroup();
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
        moveToFileNameField = new com.mirth.connect.client.ui.components.MirthTextField();
        moveToDirectoryField = new com.mirth.connect.client.ui.components.MirthTextField();
        moveToFileNameLabel = new javax.swing.JLabel();
        afterProcessingActionLabel = new javax.swing.JLabel();
        checkFileAgeLabel = new javax.swing.JLabel();
        fileAgeLabel = new javax.swing.JLabel();
        checkFileAgeYes = new com.mirth.connect.client.ui.components.MirthRadioButton();
        checkFileAgeNo = new com.mirth.connect.client.ui.components.MirthRadioButton();
        fileAge = new com.mirth.connect.client.ui.components.MirthTextField();
        sortFilesByLabel = new javax.swing.JLabel();
        sortBy = new com.mirth.connect.client.ui.components.MirthComboBox();
        charsetEncodingCombobox = new com.mirth.connect.client.ui.components.MirthComboBox();
        encodingLabel = new javax.swing.JLabel();
        fileTypeASCII = new com.mirth.connect.client.ui.components.MirthRadioButton();
        fileTypeBinary = new com.mirth.connect.client.ui.components.MirthRadioButton();
        fileTypeLabel = new javax.swing.JLabel();
        errorMoveToDirectoryLabel = new javax.swing.JLabel();
        errorMoveToDirectoryField = new com.mirth.connect.client.ui.components.MirthTextField();
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
        afterProcessingActionComboBox = new com.mirth.connect.client.ui.components.MirthComboBox();
        moveToFileLabel1 = new javax.swing.JLabel();
        errorReadingActionComboBox = new com.mirth.connect.client.ui.components.MirthComboBox();
        errorResponseActionLabel = new javax.swing.JLabel();
        errorResponseActionComboBox = new com.mirth.connect.client.ui.components.MirthComboBox();
        errorMoveToFileNameLabel = new javax.swing.JLabel();
        errorMoveToFileNameField = new com.mirth.connect.client.ui.components.MirthTextField();
        directoryRecursionLabel = new javax.swing.JLabel();
        directoryRecursionYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        directoryRecursionNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        fileSizeLabel = new javax.swing.JLabel();
        fileSizeMinimumField = new com.mirth.connect.client.ui.components.MirthTextField();
        fileSizeDashLabel = new javax.swing.JLabel();
        fileSizeMaximumField = new com.mirth.connect.client.ui.components.MirthTextField();
        ignoreFileSizeMaximumCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();

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

        moveToFileNameField.setToolTipText("<html>If successfully processed files should be renamed, enter the new name here.<br>The filename specified may include template substitutions from the list to the right.<br>If this field is left empty, successfully processed files will not be renamed.</html>");

        moveToDirectoryField.setToolTipText("<html>If successfully processed files should be moved to a different directory (folder), enter that directory here.<br>The directory name specified may include template substitutions from the list to the right.<br>If this field is left empty, successfully processed files will not be moved to a different directory.</html>");

        moveToFileNameLabel.setText("Move-to File Name:");

        afterProcessingActionLabel.setText("After Processing Action:");

        checkFileAgeLabel.setText("Check File Age:");

        fileAgeLabel.setText("File Age (ms):");

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

        charsetEncodingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Default", "UTF-8", "ISO-8859-1", "UTF-16 (le)", "UTF-16 (be)", "UTF-16 (bom)", "US-ASCII" }));
        charsetEncodingCombobox.setToolTipText("If File Type Text is selected, select the character set encoding (ASCII, UTF-8, etc.) to be used in reading the contents of each file.");
        charsetEncodingCombobox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                charsetEncodingComboboxActionPerformed(evt);
            }
        });

        encodingLabel.setText("Encoding:");

        fileTypeASCII.setBackground(new java.awt.Color(255, 255, 255));
        fileTypeASCII.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonGroup7.add(fileTypeASCII);
        fileTypeASCII.setSelected(true);
        fileTypeASCII.setText("Text");
        fileTypeASCII.setToolTipText("<html>Select Binary if files contain binary data; the contents will be Base64 encoded before processing.<br>Select Text if files contain text data; the contents will be encoded using the specified character set encoding.</html>");
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
        fileTypeBinary.setToolTipText("<html>Select Binary if files contain binary data; the contents will be Base64 encoded before processing.<br>Select Text if files contain text data; the contents will be encoded using the specified character set encoding.</html>");
        fileTypeBinary.setMargin(new java.awt.Insets(0, 0, 0, 0));
        fileTypeBinary.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileTypeBinaryActionPerformed(evt);
            }
        });

        fileTypeLabel.setText("File Type:");

        errorMoveToDirectoryLabel.setText("Error Move-to Directory:");

        errorMoveToDirectoryField.setToolTipText("<html>If files which cause processing errors should be moved to a different directory (folder), enter that directory here.<br>The directory name specified may include template substitutions from the list to the right.<br>If this field is left empty, files which cause processing errors will not be moved to a different directory.</html>");

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

        afterProcessingActionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "Move", "Delete" }));
        afterProcessingActionComboBox.setToolTipText("<html>Select Move to move and/or rename the file after successful processing.<br/>Select Delete to delete the file after successful processing.</html>");
        afterProcessingActionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                afterProcessingActionComboBoxActionPerformed(evt);
            }
        });

        moveToFileLabel1.setText("Error Reading Action:");

        errorReadingActionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "Move", "Delete" }));
        errorReadingActionComboBox.setToolTipText("<html>Select Move to move and/or rename files that have failed to be read in.<br/>Select Delete to delete files that have failed to be read in.</html>");
        errorReadingActionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                errorReadingActionComboBoxActionPerformed(evt);
            }
        });

        errorResponseActionLabel.setText("Error in Response Action:");

        errorResponseActionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "After Processing Action", "Move", "Delete" }));
        errorResponseActionComboBox.setToolTipText("<html>Select Move to move and/or rename the file if an ERROR response is returned.<br/>Select Delete to delete the file if an ERROR response is returned.<br/>If After Processing Action is selected, the After Processing Action will apply.<br/>This action is only available if Process Batch Files is disabled.</html>");
        errorResponseActionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                errorResponseActionComboBoxActionPerformed(evt);
            }
        });

        errorMoveToFileNameLabel.setText("Error Move-to File Name:");

        errorMoveToFileNameField.setToolTipText("<html>If files which cause processing errors should be renamed, enter the new name here.<br/>The filename specified may include template substitutions from the list to the right.<br/>If this field is left empty, files which cause processing errors will not be renamed.</html>");

        directoryRecursionLabel.setText("Include All Subdirectories:");

        directoryRecursionYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        directoryRecursionYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        directoryRecursionButtonGroup.add(directoryRecursionYesRadio);
        directoryRecursionYesRadio.setText("Yes");
        directoryRecursionYesRadio.setToolTipText("<html>Select Yes to traverse directories recursively and search for files in each one.</html>");
        directoryRecursionYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        directoryRecursionYesRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                directoryRecursionYesRadioActionPerformed(evt);
            }
        });

        directoryRecursionNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        directoryRecursionNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        directoryRecursionButtonGroup.add(directoryRecursionNoRadio);
        directoryRecursionNoRadio.setSelected(true);
        directoryRecursionNoRadio.setText("No");
        directoryRecursionNoRadio.setToolTipText("<html>Select No to only search for files in the selected directory/location, ignoring subdirectories.</html>");
        directoryRecursionNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        fileSizeLabel.setText("File Size (bytes):");

        fileSizeMinimumField.setToolTipText("<html>The minimum size (in bytes) of files to be accepted.</html>");

        fileSizeDashLabel.setText("-");

        fileSizeMaximumField.setToolTipText("<html>The maximum size (in bytes) of files to be accepted.<br/>This option has no effect if Ignore Maximum is checked.</html>");

        ignoreFileSizeMaximumCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        ignoreFileSizeMaximumCheckBox.setText("Ignore Maximum");
        ignoreFileSizeMaximumCheckBox.setToolTipText("<html>If checked, only the minimum file size will be checked against incoming files.</html>");
        ignoreFileSizeMaximumCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ignoreFileSizeMaximumCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hostLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(schemeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(directoryLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(filenameFilterLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(directoryRecursionLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(ignoreDotFilesLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(anonymousLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(usernameLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(passwordLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(timeoutLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(secureModeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(passiveModeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(validateConnectionLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(afterProcessingActionLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(moveToDirectoryLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(moveToFileNameLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(moveToFileLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(errorResponseActionLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(errorMoveToDirectoryLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(errorMoveToFileNameLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(checkFileAgeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(fileAgeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(fileSizeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(sortFilesByLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(fileTypeLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(encodingLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fileNameFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(filenameFilterRegexCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(directoryField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(directoryRecursionYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(directoryRecursionNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(ignoreDotFilesYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ignoreDotFilesNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(anonymousYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(anonymousNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(timeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(secureModeYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(secureModeNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(passiveModeYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(passiveModeNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(validateConnectionYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(validateConnectionNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(afterProcessingActionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(moveToDirectoryField, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(moveToFileNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(errorReadingActionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(errorResponseActionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(errorMoveToDirectoryField, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(errorMoveToFileNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(fileAge, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(checkFileAgeYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkFileAgeNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fileSizeMinimumField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fileSizeDashLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fileSizeMaximumField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ignoreFileSizeMaximumCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(sortBy, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fileTypeBinary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fileTypeASCII, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(charsetEncodingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(62, Short.MAX_VALUE))
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
                    .addComponent(directoryRecursionLabel)
                    .addComponent(directoryRecursionYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(directoryRecursionNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ignoreDotFilesLabel)
                    .addComponent(ignoreDotFilesYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ignoreDotFilesNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(anonymousLabel)
                    .addComponent(anonymousYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(anonymousNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                    .addComponent(timeoutLabel)
                    .addComponent(timeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                    .addComponent(afterProcessingActionLabel)
                    .addComponent(afterProcessingActionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(moveToDirectoryLabel)
                            .addComponent(moveToDirectoryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(moveToFileNameLabel)
                            .addComponent(moveToFileNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(moveToFileLabel1)
                            .addComponent(errorReadingActionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(errorResponseActionLabel)
                            .addComponent(errorResponseActionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(errorMoveToDirectoryLabel)
                            .addComponent(errorMoveToDirectoryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(errorMoveToFileNameLabel)
                            .addComponent(errorMoveToFileNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(checkFileAgeLabel)
                            .addComponent(checkFileAgeYes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(checkFileAgeNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(fileAgeLabel)
                            .addComponent(fileAge, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileSizeLabel)
                    .addComponent(fileSizeMinimumField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fileSizeDashLabel)
                    .addComponent(fileSizeMaximumField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ignoreFileSizeMaximumCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        decorateConnectorType();
    }//GEN-LAST:event_schemeComboBoxActionPerformed

    private void fileTypeASCIIActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_fileTypeASCIIActionPerformed
    {//GEN-HEADEREND:event_fileTypeASCIIActionPerformed
        encodingLabel.setEnabled(true);
        charsetEncodingCombobox.setEnabled(true);
    }//GEN-LAST:event_fileTypeASCIIActionPerformed

    private void fileTypeBinaryActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_fileTypeBinaryActionPerformed
    {//GEN-HEADEREND:event_fileTypeBinaryActionPerformed
        encodingLabel.setEnabled(false);
        charsetEncodingCombobox.setEnabled(false);
        charsetEncodingCombobox.setSelectedIndex(0);
    }//GEN-LAST:event_fileTypeBinaryActionPerformed

    private void testConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testConnectionActionPerformed
        invokeConnectorService(FileServiceMethods.METHOD_TEST_READ, "Testing connection...", "Failed to invoke service: ");
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

    private void afterProcessingActionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_afterProcessingActionComboBoxActionPerformed
        boolean enabled = (FileAction) afterProcessingActionComboBox.getSelectedItem() == FileAction.MOVE;
        moveToDirectoryLabel.setEnabled(enabled);
        moveToDirectoryField.setEnabled(enabled);
        moveToFileNameLabel.setEnabled(enabled);
        moveToFileNameField.setEnabled(enabled);
    }//GEN-LAST:event_afterProcessingActionComboBoxActionPerformed

    private void errorReadingActionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_errorReadingActionComboBoxActionPerformed
        updateErrorFields();
    }//GEN-LAST:event_errorReadingActionComboBoxActionPerformed

    private void errorResponseActionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_errorResponseActionComboBoxActionPerformed
        updateErrorFields();
    }//GEN-LAST:event_errorResponseActionComboBoxActionPerformed

    private void directoryRecursionYesRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_directoryRecursionYesRadioActionPerformed
        if (!parent.alertOption(parent, "<html>Including all subdirectories recursively is not recommended, especially if you are<br/>moving or deleting files.&nbsp;&nbsp;Are you sure you want to enable directory recursion?</html>")) {
            directoryRecursionNoRadio.setSelected(true);
        }
    }//GEN-LAST:event_directoryRecursionYesRadioActionPerformed

    private void ignoreFileSizeMaximumCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ignoreFileSizeMaximumCheckBoxActionPerformed
        fileSizeDashLabel.setEnabled(!ignoreFileSizeMaximumCheckBox.isSelected());
        fileSizeMaximumField.setEnabled(!ignoreFileSizeMaximumCheckBox.isSelected());
    }//GEN-LAST:event_ignoreFileSizeMaximumCheckBoxActionPerformed

    private void updateErrorFields() {
        FileAction readAction = (FileAction) errorReadingActionComboBox.getSelectedItem();
        FileAction responseAction = (FileAction) errorResponseActionComboBox.getSelectedItem();
        boolean enabled = readAction == FileAction.MOVE || responseAction == FileAction.MOVE;
        errorMoveToDirectoryLabel.setEnabled(enabled);
        errorMoveToDirectoryField.setEnabled(enabled);
        errorMoveToFileNameLabel.setEnabled(enabled);
        errorMoveToFileNameField.setEnabled(enabled);
    }

    private void processBatchFilesNoActionPerformed(java.awt.event.ActionEvent evt)// GEN-FIRST:event_processBatchFilesNoActionPerformed
    {// GEN-HEADEREND:event_processBatchFilesNoActionPerformed
        errorResponseActionComboBox.setEnabled(true);
        errorResponseActionLabel.setEnabled(true);
    }// GEN-LAST:event_processBatchFilesNoActionPerformed

    private void charsetEncodingComboboxActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_charsetEncodingComboboxActionPerformed
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
    private com.mirth.connect.client.ui.components.MirthComboBox afterProcessingActionComboBox;
    private javax.swing.JLabel afterProcessingActionLabel;
    private javax.swing.JLabel anonymousLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton anonymousNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton anonymousYes;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup6;
    private javax.swing.ButtonGroup buttonGroup7;
    private javax.swing.ButtonGroup buttonGroup8;
    private javax.swing.ButtonGroup buttonGroup9;
    private com.mirth.connect.client.ui.components.MirthComboBox charsetEncodingCombobox;
    private javax.swing.JLabel checkFileAgeLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton checkFileAgeNo;
    private com.mirth.connect.client.ui.components.MirthRadioButton checkFileAgeYes;
    private com.mirth.connect.client.ui.components.MirthTextField directoryField;
    private javax.swing.JLabel directoryLabel;
    private javax.swing.ButtonGroup directoryRecursionButtonGroup;
    private javax.swing.JLabel directoryRecursionLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton directoryRecursionNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton directoryRecursionYesRadio;
    private javax.swing.JLabel encodingLabel;
    private com.mirth.connect.client.ui.components.MirthTextField errorMoveToDirectoryField;
    private javax.swing.JLabel errorMoveToDirectoryLabel;
    private com.mirth.connect.client.ui.components.MirthTextField errorMoveToFileNameField;
    private javax.swing.JLabel errorMoveToFileNameLabel;
    private com.mirth.connect.client.ui.components.MirthComboBox errorReadingActionComboBox;
    private com.mirth.connect.client.ui.components.MirthComboBox errorResponseActionComboBox;
    private javax.swing.JLabel errorResponseActionLabel;
    private com.mirth.connect.client.ui.components.MirthTextField fileAge;
    private javax.swing.JLabel fileAgeLabel;
    private com.mirth.connect.client.ui.components.MirthTextField fileNameFilter;
    private javax.swing.JLabel fileSizeDashLabel;
    private javax.swing.JLabel fileSizeLabel;
    private com.mirth.connect.client.ui.components.MirthTextField fileSizeMaximumField;
    private com.mirth.connect.client.ui.components.MirthTextField fileSizeMinimumField;
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
    private com.mirth.connect.client.ui.components.MirthCheckBox ignoreFileSizeMaximumCheckBox;
    private javax.swing.JScrollPane jScrollPane1;
    private com.mirth.connect.client.ui.components.MirthVariableList mirthVariableList1;
    private com.mirth.connect.client.ui.components.MirthTextField moveToDirectoryField;
    private javax.swing.JLabel moveToDirectoryLabel;
    private javax.swing.JLabel moveToFileLabel1;
    private com.mirth.connect.client.ui.components.MirthTextField moveToFileNameField;
    private javax.swing.JLabel moveToFileNameLabel;
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
