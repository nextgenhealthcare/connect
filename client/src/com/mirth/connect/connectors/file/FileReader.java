/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.ConnectorTypeDecoration;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthCheckBox;
import com.mirth.connect.client.ui.components.MirthComboBox;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthPasswordField;
import com.mirth.connect.client.ui.components.MirthRadioButton;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.client.ui.components.MirthVariableList;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.client.ui.panels.connectors.ResponseHandler;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.util.ConnectionTestResponse;

public class FileReader extends ConnectorSettingsPanel {

    private Logger logger = Logger.getLogger(this.getClass());
    private Frame parent;

    private FileScheme selectedScheme;
    private SchemeProperties advancedProperties;

    public FileReader() {
        this.parent = PlatformUI.MIRTH_FRAME;

        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        initComponents();
        initLayout();

        afterProcessingActionComboBox.setModel(new DefaultComboBoxModel(new FileAction[] {
                FileAction.NONE, FileAction.MOVE, FileAction.DELETE }));
        errorReadingActionComboBox.setModel(new DefaultComboBoxModel(new FileAction[] {
                FileAction.NONE, FileAction.MOVE, FileAction.DELETE }));
        errorResponseActionComboBox.setModel(new DefaultComboBoxModel(new FileAction[] {
                FileAction.AFTER_PROCESSING, FileAction.MOVE, FileAction.DELETE }));
        fileAgeField.setDocument(new MirthFieldConstraints(0, false, false, true));
        fileSizeMinimumField.setDocument(new MirthFieldConstraints(0, false, false, true));
        fileSizeMaximumField.setDocument(new MirthFieldConstraints(0, false, false, true));

        parent.setupCharsetEncodingForConnector(charsetEncodingComboBox);
    }

    @Override
    public String getConnectorName() {
        return new FileReceiverProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        FileReceiverProperties properties = new FileReceiverProperties();

        properties.setScheme((FileScheme) schemeComboBox.getSelectedItem());

        properties.setSchemeProperties(advancedProperties);

        if (schemeComboBox.getSelectedItem() == FileScheme.FILE) {
            properties.setHost(directoryField.getText().replace('\\', '/'));
        } else {
            properties.setHost(hostField.getText() + "/" + pathField.getText());
        }

        properties.setDirectoryRecursion(directoryRecursionYesRadio.isSelected());
        properties.setIgnoreDot(ignoreDotFilesYesRadio.isSelected());

        properties.setAnonymous(anonymousYesRadio.isSelected());

        properties.setUsername(usernameField.getText());
        properties.setPassword(new String(passwordField.getPassword()));

        properties.setTimeout(timeoutField.getText());

        properties.setSecure(secureModeYesRadio.isSelected());
        properties.setPassive(passiveModeYesRadio.isSelected());
        properties.setValidateConnection(validateConnectionYesRadio.isSelected());

        properties.setAfterProcessingAction((FileAction) afterProcessingActionComboBox.getSelectedItem());
        properties.setMoveToDirectory(moveToDirectoryField.getText().replace('\\', '/'));
        properties.setMoveToFileName(moveToFileNameField.getText());
        properties.setErrorReadingAction((FileAction) errorReadingActionComboBox.getSelectedItem());
        properties.setErrorResponseAction((FileAction) errorResponseActionComboBox.getSelectedItem());
        properties.setErrorMoveToDirectory(errorMoveToDirectoryField.getText().replace('\\', '/'));
        properties.setErrorMoveToFileName(errorMoveToFileNameField.getText());

        properties.setCheckFileAge(checkFileAgeYesRadio.isSelected());
        properties.setFileAge(fileAgeField.getText());

        properties.setFileSizeMinimum(fileSizeMinimumField.getText());
        properties.setFileSizeMaximum(fileSizeMaximumField.getText());
        properties.setIgnoreFileSizeMaximum(ignoreFileSizeMaximumCheckBox.isSelected());

        if (((String) sortByComboBox.getSelectedItem()).equals("Name")) {
            properties.setSortBy(FileReceiverProperties.SORT_BY_NAME);
        } else if (((String) sortByComboBox.getSelectedItem()).equals("Size")) {
            properties.setSortBy(FileReceiverProperties.SORT_BY_SIZE);
        } else if (((String) sortByComboBox.getSelectedItem()).equals("Date")) {
            properties.setSortBy(FileReceiverProperties.SORT_BY_DATE);
        }

        properties.setCharsetEncoding(parent.getSelectedEncodingForConnector(charsetEncodingComboBox));
        properties.setFileFilter(fileNameFilterField.getText());
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

        selectedScheme = null;
        FileScheme scheme = props.getScheme();
        schemeComboBox.setSelectedItem(scheme);
        schemeComboBoxActionPerformed(null);

        advancedProperties = props.getSchemeProperties();
        setSummaryText();

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
            anonymousYesRadio.setSelected(true);
            anonymousYesActionPerformed(null);
        } else {
            anonymousNoRadio.setSelected(true);
            anonymousNoActionPerformed(null);
            usernameField.setText(props.getUsername());
            passwordField.setText(props.getPassword());
        }

        timeoutField.setText(props.getTimeout());

        if (props.isSecure()) {
            secureModeYesRadio.setSelected(true);
            if (scheme == FileScheme.WEBDAV) {
                hostLabel.setText("https://");
            }
        } else {
            secureModeNoRadio.setSelected(true);
            if (scheme == FileScheme.WEBDAV) {
                hostLabel.setText("http://");
            }
        }

        if (props.isPassive()) {
            passiveModeYesRadio.setSelected(true);
        } else {
            passiveModeNoRadio.setSelected(true);
        }

        if (props.isValidateConnection()) {
            validateConnectionYesRadio.setSelected(true);
        } else {
            validateConnectionNoRadio.setSelected(true);
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
            checkFileAgeYesRadio.setSelected(true);
            checkFileAgeYesActionPerformed(null);
        } else {
            checkFileAgeNoRadio.setSelected(true);
            checkFileAgeNoActionPerformed(null);
        }

        fileAgeField.setText(props.getFileAge());

        fileSizeMinimumField.setText(props.getFileSizeMinimum());
        fileSizeMaximumField.setText(props.getFileSizeMaximum());
        ignoreFileSizeMaximumCheckBox.setSelected(props.isIgnoreFileSizeMaximum());
        ignoreFileSizeMaximumCheckBoxActionPerformed(null);

        if (props.getSortBy().equals(FileReceiverProperties.SORT_BY_NAME)) {
            sortByComboBox.setSelectedItem("Name");
        } else if (props.getSortBy().equals(FileReceiverProperties.SORT_BY_SIZE)) {
            sortByComboBox.setSelectedItem("Size");
        } else if (props.getSortBy().equals(FileReceiverProperties.SORT_BY_DATE)) {
            sortByComboBox.setSelectedItem("Date");
        }

        parent.setPreviousSelectedEncodingForConnector(charsetEncodingComboBox, props.getCharsetEncoding());

        fileNameFilterField.setText(props.getFileFilter());

        if (props.isRegex()) {
            filenameFilterRegexCheckBox.setSelected(true);
        } else {
            filenameFilterRegexCheckBox.setSelected(false);
        }

        if (props.isBinary()) {
            fileTypeBinary.setSelected(true);
            fileTypeBinaryActionPerformed(null);
        } else {
            fileTypeText.setSelected(true);
            fileTypeASCIIActionPerformed(null);
        }
    }

    private void setSummaryText() {
        if (advancedProperties != null) {
            summaryLabel.setEnabled(true);
            summaryField.setEnabled(true);
            summaryField.setText(advancedProperties.getSummaryText());
        } else {
            summaryLabel.setEnabled(false);
            summaryField.setEnabled(false);
            summaryField.setText("<None>");
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
                fileNameFilterField.setBackground(UIConstants.INVALID_COLOR);
            }
        }

        if (!props.isAnonymous() && (props.getScheme() != FileScheme.S3 || !((S3SchemeProperties) props.getSchemeProperties()).isUseDefaultCredentialProviderChain())) {
            if (props.getUsername().length() == 0) {
                valid = false;
                if (highlight) {
                    usernameField.setBackground(UIConstants.INVALID_COLOR);
                }
            }
            boolean ignorePassword = props.getScheme() == FileScheme.SFTP && !((SftpSchemeProperties) props.getSchemeProperties()).isPasswordAuth();
            if (!ignorePassword && props.getPassword().length() == 0) {
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
                    fileAgeField.setBackground(UIConstants.INVALID_COLOR);
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
        fileNameFilterField.setBackground(null);
        fileAgeField.setBackground(null);
        fileSizeMinimumField.setBackground(null);
        fileSizeMaximumField.setBackground(null);
        usernameField.setBackground(null);
        passwordField.setBackground(null);
        timeoutField.setBackground(null);
    }

    @Override
    public void doLocalDecoration(ConnectorTypeDecoration connectorTypeDecoration) {
        if (FileScheme.FTP == schemeComboBox.getSelectedItem()) {
            hostLabel.setText("ftp" + (connectorTypeDecoration != null ? "s" : "") + "://");
        }
    }

    private void initComponents() {
        schemeLabel = new JLabel();
        schemeLabel.setText("Method:");
        schemeComboBox = new MirthComboBox<FileScheme>();
        schemeComboBox.setModel(new DefaultComboBoxModel<FileScheme>(FileScheme.values()));
        schemeComboBox.setToolTipText("The basic method used to access files to be read - file (local filesystem), FTP, SFTP, S3, Samba share, or WebDAV");
        schemeComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                schemeComboBoxActionPerformed(evt);
            }
        });

        testConnectionButton = new JButton();
        testConnectionButton.setText("Test Read");
        testConnectionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                testConnectionActionPerformed(evt);
            }
        });

        advancedSettingsButton = new JButton(new ImageIcon(Frame.class.getResource("images/wrench.png")));
        advancedSettingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                advancedFileSettingsActionPerformed();
            }
        });

        summaryLabel = new JLabel("Advanced Options:");
        summaryField = new JLabel("");

        directoryLabel = new JLabel();
        directoryLabel.setText("Directory:");
        directoryField = new MirthTextField();
        directoryField.setToolTipText("The directory (folder) in which the files to be read can be found.");

        hostLabel = new JLabel();
        hostLabel.setText("ftp://");
        hostField = new MirthTextField();
        hostField.setToolTipText("The name or IP address of the host (computer) on which the files to be read can be found.");
        pathLabel = new JLabel();
        pathLabel.setText("/");
        pathField = new MirthTextField();
        pathField.setToolTipText("The directory (folder) in which the files to be read can be found.");

        filenameFilterLabel = new JLabel();
        filenameFilterLabel.setText("Filename Filter Pattern:");
        fileNameFilterField = new MirthTextField();
        fileNameFilterField.setToolTipText("<html>The pattern which names of files must match in order to be read.<br>Files with names that do not match the pattern will be ignored.</html>");
        filenameFilterRegexCheckBox = new MirthCheckBox();
        filenameFilterRegexCheckBox.setBackground(UIConstants.BACKGROUND_COLOR);
        filenameFilterRegexCheckBox.setText("Regular Expression");
        filenameFilterRegexCheckBox.setToolTipText("<html>If Regex is checked, the pattern is treated as a regular expression.<br>If Regex is not checked, it is treated as a pattern that supports wildcards and a comma separated list.</html>");

        directoryRecursionLabel = new JLabel();
        directoryRecursionLabel.setText("Include All Subdirectories:");

        directoryRecursionYesRadio = new MirthRadioButton();
        directoryRecursionYesRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        directoryRecursionYesRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        directoryRecursionYesRadio.setText("Yes");
        directoryRecursionYesRadio.setToolTipText("<html>Select Yes to traverse directories recursively and search for files in each one.</html>");
        directoryRecursionYesRadio.setMargin(new Insets(0, 0, 0, 0));
        directoryRecursionYesRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                directoryRecursionYesRadioActionPerformed(evt);
            }
        });

        directoryRecursionNoRadio = new MirthRadioButton();
        directoryRecursionNoRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        directoryRecursionNoRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        directoryRecursionNoRadio.setSelected(true);
        directoryRecursionNoRadio.setText("No");
        directoryRecursionNoRadio.setToolTipText("<html>Select No to only search for files in the selected directory/location, ignoring subdirectories.</html>");
        directoryRecursionNoRadio.setMargin(new Insets(0, 0, 0, 0));

        directoryRecursionButtonGroup = new ButtonGroup();
        directoryRecursionButtonGroup.add(directoryRecursionYesRadio);
        directoryRecursionButtonGroup.add(directoryRecursionNoRadio);

        ignoreDotFilesLabel = new JLabel();
        ignoreDotFilesLabel.setText("Ignore . files:");

        ignoreDotFilesYesRadio = new MirthRadioButton();
        ignoreDotFilesYesRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        ignoreDotFilesYesRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ignoreDotFilesYesRadio.setText("Yes");
        ignoreDotFilesYesRadio.setToolTipText("Select Yes to ignore all files starting with a period.");
        ignoreDotFilesYesRadio.setMargin(new Insets(0, 0, 0, 0));

        ignoreDotFilesNoRadio = new MirthRadioButton();
        ignoreDotFilesNoRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        ignoreDotFilesNoRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ignoreDotFilesNoRadio.setText("No");
        ignoreDotFilesNoRadio.setToolTipText("Select No to process files starting with a period.");
        ignoreDotFilesNoRadio.setMargin(new Insets(0, 0, 0, 0));

        ignoreDotFilesButtonGroup = new ButtonGroup();
        ignoreDotFilesButtonGroup.add(ignoreDotFilesYesRadio);
        ignoreDotFilesButtonGroup.add(ignoreDotFilesNoRadio);

        anonymousLabel = new JLabel();
        anonymousLabel.setText("Anonymous:");

        anonymousYesRadio = new MirthRadioButton();
        anonymousYesRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        anonymousYesRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        anonymousYesRadio.setText("Yes");
        anonymousYesRadio.setToolTipText("Connects to the file anonymously instead of using a username and password.");
        anonymousYesRadio.setMargin(new Insets(0, 0, 0, 0));
        anonymousYesRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                anonymousYesActionPerformed(evt);
            }
        });

        anonymousNoRadio = new MirthRadioButton();
        anonymousNoRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        anonymousNoRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        anonymousNoRadio.setSelected(true);
        anonymousNoRadio.setText("No");
        anonymousNoRadio.setToolTipText("Connects to the file using a username and password instead of anonymously.");
        anonymousNoRadio.setMargin(new Insets(0, 0, 0, 0));
        anonymousNoRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                anonymousNoActionPerformed(evt);
            }
        });

        anonymousButtonGroup = new ButtonGroup();
        anonymousButtonGroup.add(anonymousYesRadio);
        anonymousButtonGroup.add(anonymousNoRadio);

        usernameLabel = new JLabel();
        usernameLabel.setText("Username:");
        usernameField = new MirthTextField();
        usernameField.setToolTipText("The user name used to gain access to the server.");

        passwordLabel = new JLabel();
        passwordLabel.setText("Password:");
        passwordField = new MirthPasswordField();
        passwordField.setToolTipText("The password used to gain access to the server.");

        timeoutLabel = new JLabel();
        timeoutLabel.setText("Timeout (ms):");
        timeoutField = new MirthTextField();
        timeoutField.setToolTipText("The socket timeout (in ms) for connecting to the server.");

        secureModeLabel = new JLabel();
        secureModeLabel.setText("Secure Mode:");

        secureModeYesRadio = new MirthRadioButton();
        secureModeYesRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        secureModeYesRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        secureModeYesRadio.setText("Yes");
        secureModeYesRadio.setToolTipText("<html>Select Yes to connect to the server via HTTPS.<br>Select No to connect via HTTP.</html>");
        secureModeYesRadio.setMargin(new Insets(0, 0, 0, 0));
        secureModeYesRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                secureModeYesActionPerformed(evt);
            }
        });

        secureModeNoRadio = new MirthRadioButton();
        secureModeNoRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        secureModeNoRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        secureModeNoRadio.setSelected(true);
        secureModeNoRadio.setText("No");
        secureModeNoRadio.setToolTipText("<html>Select Yes to connect to the server via HTTPS.<br>Select No to connect via HTTP.</html>");
        secureModeNoRadio.setMargin(new Insets(0, 0, 0, 0));
        secureModeNoRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                secureModeNoActionPerformed(evt);
            }
        });

        secureModeButtonGroup = new ButtonGroup();
        secureModeButtonGroup.add(secureModeYesRadio);
        secureModeButtonGroup.add(secureModeNoRadio);

        passiveModeLabel = new JLabel();
        passiveModeLabel.setText("Passive Mode:");

        passiveModeYesRadio = new MirthRadioButton();
        passiveModeYesRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        passiveModeYesRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        passiveModeYesRadio.setText("Yes");
        passiveModeYesRadio.setToolTipText("<html>Select Yes to connect to the server in \"passive mode\".<br>Passive mode sometimes allows a connection through a firewall that normal mode does not.</html>");
        passiveModeYesRadio.setMargin(new Insets(0, 0, 0, 0));

        passiveModeNoRadio = new MirthRadioButton();
        passiveModeNoRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        passiveModeNoRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        passiveModeNoRadio.setSelected(true);
        passiveModeNoRadio.setText("No");
        passiveModeNoRadio.setToolTipText("Select Yes to connect to the server in \"normal mode\" as opposed to passive mode.");
        passiveModeNoRadio.setMargin(new Insets(0, 0, 0, 0));

        passiveModeButtonGroup = new ButtonGroup();
        passiveModeButtonGroup.add(passiveModeYesRadio);
        passiveModeButtonGroup.add(passiveModeNoRadio);

        validateConnectionLabel = new JLabel();
        validateConnectionLabel.setText("Validate Connection:");

        validateConnectionYesRadio = new MirthRadioButton();
        validateConnectionYesRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        validateConnectionYesRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        validateConnectionYesRadio.setText("Yes");
        validateConnectionYesRadio.setToolTipText("Select Yes to test the connection to the server before each operation.");
        validateConnectionYesRadio.setMargin(new Insets(0, 0, 0, 0));

        validateConnectionNoRadio = new MirthRadioButton();
        validateConnectionNoRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        validateConnectionNoRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        validateConnectionNoRadio.setText("No");
        validateConnectionNoRadio.setToolTipText("Select No to skip testing the connection to the server before each operation.");
        validateConnectionNoRadio.setMargin(new Insets(0, 0, 0, 0));

        validateConnectionButtonGroup = new ButtonGroup();
        validateConnectionButtonGroup.add(validateConnectionYesRadio);
        validateConnectionButtonGroup.add(validateConnectionNoRadio);

        afterProcessingActionLabel = new JLabel();
        afterProcessingActionLabel.setText("After Processing Action:");

        afterProcessingActionComboBox = new MirthComboBox();
        afterProcessingActionComboBox.setModel(new DefaultComboBoxModel(new String[] { "None",
                "Move", "Delete" }));
        afterProcessingActionComboBox.setToolTipText("<html>Select Move to move and/or rename the file after successful processing.<br/>Select Delete to delete the file after successful processing.</html>");
        afterProcessingActionComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                afterProcessingActionComboBoxActionPerformed(evt);
            }
        });

        moveToDirectoryLabel = new JLabel();
        moveToDirectoryLabel.setText("Move-to Directory:");
        moveToDirectoryField = new MirthTextField();
        moveToDirectoryField.setToolTipText("<html>If successfully processed files should be moved to a different directory (folder), enter that directory here.<br>The directory name specified may include template substitutions from the list to the right.<br>If this field is left empty, successfully processed files will not be moved to a different directory.</html>");

        moveToFileNameLabel = new JLabel();
        moveToFileNameLabel.setText("Move-to File Name:");
        moveToFileNameField = new MirthTextField();
        moveToFileNameField.setToolTipText("<html>If successfully processed files should be renamed, enter the new name here.<br>The filename specified may include template substitutions from the list to the right.<br>If this field is left empty, successfully processed files will not be renamed.</html>");

        errorReadingActionLabel = new JLabel();
        errorReadingActionLabel.setText("Error Reading Action:");

        errorReadingActionComboBox = new MirthComboBox();
        errorReadingActionComboBox.setModel(new DefaultComboBoxModel(new String[] { "None", "Move",
                "Delete" }));
        errorReadingActionComboBox.setToolTipText("<html>Select Move to move and/or rename files that have failed to be read in.<br/>Select Delete to delete files that have failed to be read in.</html>");
        errorReadingActionComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                errorReadingActionComboBoxActionPerformed(evt);
            }
        });

        errorResponseActionLabel = new JLabel();
        errorResponseActionLabel.setText("Error in Response Action:");

        errorResponseActionComboBox = new MirthComboBox();
        errorResponseActionComboBox.setModel(new DefaultComboBoxModel(new String[] {
                "After Processing Action", "Move", "Delete" }));
        errorResponseActionComboBox.setToolTipText("<html>Select Move to move and/or rename the file if an ERROR response is returned.<br/>Select Delete to delete the file if an ERROR response is returned.<br/>If After Processing Action is selected, the After Processing Action will apply.<br/>This action is only available if Process Batch Files is disabled.</html>");
        errorResponseActionComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                errorResponseActionComboBoxActionPerformed(evt);
            }
        });

        errorMoveToDirectoryLabel = new JLabel();
        errorMoveToDirectoryLabel.setText("Error Move-to Directory:");
        errorMoveToDirectoryField = new MirthTextField();
        errorMoveToDirectoryField.setToolTipText("<html>If files which cause processing errors should be moved to a different directory (folder), enter that directory here.<br>The directory name specified may include template substitutions from the list to the right.<br>If this field is left empty, files which cause processing errors will not be moved to a different directory.</html>");

        errorMoveToFileNameLabel = new JLabel();
        errorMoveToFileNameLabel.setText("Error Move-to File Name:");
        errorMoveToFileNameField = new MirthTextField();
        errorMoveToFileNameField.setToolTipText("<html>If files which cause processing errors should be renamed, enter the new name here.<br/>The filename specified may include template substitutions from the list to the right.<br/>If this field is left empty, files which cause processing errors will not be renamed.</html>");

        variableListScrollPane = new JScrollPane();
        variableListScrollPane.setBorder(null);
        variableListScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        variableListScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        mirthVariableList = new MirthVariableList();
        mirthVariableList.setBorder(BorderFactory.createEtchedBorder());
        mirthVariableList.setModel(new AbstractListModel() {
            String[] strings = { "channelName", "channelId", "DATE", "COUNT", "UUID", "SYSTIME",
                    "originalFilename" };

            public int getSize() {
                return strings.length;
            }

            public Object getElementAt(int i) {
                return strings[i];
            }
        });
        variableListScrollPane.setViewportView(mirthVariableList);

        checkFileAgeLabel = new JLabel();
        checkFileAgeLabel.setText("Check File Age:");

        checkFileAgeYesRadio = new MirthRadioButton();
        checkFileAgeYesRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        checkFileAgeYesRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        checkFileAgeYesRadio.setText("Yes");
        checkFileAgeYesRadio.setToolTipText("Select Yes to skip files that are created within the specified age below.");
        checkFileAgeYesRadio.setMargin(new Insets(0, 0, 0, 0));
        checkFileAgeYesRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                checkFileAgeYesActionPerformed(evt);
            }
        });

        checkFileAgeNoRadio = new MirthRadioButton();
        checkFileAgeNoRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        checkFileAgeNoRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        checkFileAgeNoRadio.setSelected(true);
        checkFileAgeNoRadio.setText("No");
        checkFileAgeNoRadio.setToolTipText("Select No to process files regardless of age.");
        checkFileAgeNoRadio.setMargin(new Insets(0, 0, 0, 0));
        checkFileAgeNoRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                checkFileAgeNoActionPerformed(evt);
            }
        });

        checkFileAgeButtonGroup = new ButtonGroup();
        checkFileAgeButtonGroup.add(checkFileAgeYesRadio);
        checkFileAgeButtonGroup.add(checkFileAgeNoRadio);

        fileAgeLabel = new JLabel();
        fileAgeLabel.setText("File Age (ms):");
        fileAgeField = new MirthTextField();
        fileAgeField.setToolTipText("If Check File Age Yes is selected, only the files created that are older than the specified value in milliseconds will be processed.");

        fileSizeLabel = new JLabel();
        fileSizeLabel.setText("File Size (bytes):");
        fileSizeMinimumField = new MirthTextField();
        fileSizeMinimumField.setToolTipText("<html>The minimum size (in bytes) of files to be accepted.</html>");
        fileSizeDashLabel = new JLabel();
        fileSizeDashLabel.setText("-");
        fileSizeMaximumField = new MirthTextField();
        fileSizeMaximumField.setToolTipText("<html>The maximum size (in bytes) of files to be accepted.<br/>This option has no effect if Ignore Maximum is checked.</html>");
        ignoreFileSizeMaximumCheckBox = new MirthCheckBox();
        ignoreFileSizeMaximumCheckBox.setBackground(UIConstants.BACKGROUND_COLOR);
        ignoreFileSizeMaximumCheckBox.setText("Ignore Maximum");
        ignoreFileSizeMaximumCheckBox.setToolTipText("<html>If checked, only the minimum file size will be checked against incoming files.</html>");
        ignoreFileSizeMaximumCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                ignoreFileSizeMaximumCheckBoxActionPerformed(evt);
            }
        });

        sortFilesByLabel = new JLabel();
        sortFilesByLabel.setText("Sort Files By:");
        sortByComboBox = new MirthComboBox();
        sortByComboBox.setModel(new DefaultComboBoxModel(new String[] { "Date", "Name", "Size" }));
        sortByComboBox.setToolTipText("<html>Selects the order in which files should be processed, if there are multiple files available to be processed.<br>Files can be processed by Date (oldest last modification date first), Size (smallest first) or name (a before z, etc.).</html>");

        fileTypeLabel = new JLabel();
        fileTypeLabel.setText("File Type:");

        fileTypeBinary = new MirthRadioButton();
        fileTypeBinary.setBackground(UIConstants.BACKGROUND_COLOR);
        fileTypeBinary.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        fileTypeBinary.setText("Binary");
        fileTypeBinary.setToolTipText("<html>Select Binary if files contain binary data; the contents will be Base64 encoded before processing.<br>Select Text if files contain text data; the contents will be encoded using the specified character set encoding.</html>");
        fileTypeBinary.setMargin(new Insets(0, 0, 0, 0));
        fileTypeBinary.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                fileTypeBinaryActionPerformed(evt);
            }
        });

        fileTypeText = new MirthRadioButton();
        fileTypeText.setBackground(UIConstants.BACKGROUND_COLOR);
        fileTypeText.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        fileTypeText.setSelected(true);
        fileTypeText.setText("Text");
        fileTypeText.setToolTipText("<html>Select Binary if files contain binary data; the contents will be Base64 encoded before processing.<br>Select Text if files contain text data; the contents will be encoded using the specified character set encoding.</html>");
        fileTypeText.setMargin(new Insets(0, 0, 0, 0));
        fileTypeText.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                fileTypeASCIIActionPerformed(evt);
            }
        });

        fileTypeButtonGroup = new ButtonGroup();
        fileTypeButtonGroup.add(fileTypeBinary);
        fileTypeButtonGroup.add(fileTypeText);

        encodingLabel = new JLabel();
        encodingLabel.setText("Encoding:");

        charsetEncodingComboBox = new MirthComboBox();
        charsetEncodingComboBox.setModel(new DefaultComboBoxModel(new String[] { "Default", "UTF-8",
                "ISO-8859-1", "UTF-16 (le)", "UTF-16 (be)", "UTF-16 (bom)", "US-ASCII" }));
        charsetEncodingComboBox.setToolTipText("If File Type Text is selected, select the character set encoding (ASCII, UTF-8, etc.) to be used in reading the contents of each file.");
    }

    private void initLayout() {
        setLayout(new MigLayout("novisualpadding, hidemode 3, insets 0, gapy 6", "[right,175]12[left]"));

        add(schemeLabel);
        add(schemeComboBox, "split 3, spanx");
        add(testConnectionButton);
        add(advancedSettingsButton, "h 22!, w 22!");

        add(summaryLabel, "newline");
        add(summaryField, "growx, spanx");

        add(directoryLabel, "newline");
        add(directoryField, "w 200!, spanx");

        add(hostLabel, "newline");
        add(hostField, "w 200!, split 3, spanx");
        add(pathLabel, "gapleft 14");
        add(pathField, "gapleft 14, w 200!");

        add(filenameFilterLabel, "newline");
        add(fileNameFilterField, "w 200!, split 2, spanx");
        add(filenameFilterRegexCheckBox, "gapleft 8");

        add(directoryRecursionLabel, "newline");
        add(directoryRecursionYesRadio, "split 2, spanx");
        add(directoryRecursionNoRadio);

        add(ignoreDotFilesLabel, "newline");
        add(ignoreDotFilesYesRadio, "split 2, spanx");
        add(ignoreDotFilesNoRadio);

        add(anonymousLabel, "newline");
        add(anonymousYesRadio, "split 2, spanx");
        add(anonymousNoRadio);

        add(usernameLabel, "newline");
        add(usernameField, "w 125!, spanx");

        add(passwordLabel, "newline");
        add(passwordField, "w 125!, spanx");

        add(timeoutLabel, "newline");
        add(timeoutField, "w 75!, spanx");

        add(secureModeLabel, "newline");
        add(secureModeYesRadio, "split 2, spanx");
        add(secureModeNoRadio);

        add(passiveModeLabel, "newline");
        add(passiveModeYesRadio, "split 2, spanx");
        add(passiveModeNoRadio);

        add(validateConnectionLabel, "newline");
        add(validateConnectionYesRadio, "split 2, spanx");
        add(validateConnectionNoRadio);

        add(afterProcessingActionLabel, "newline");
        add(afterProcessingActionComboBox, "w 55!, spanx");

        add(moveToDirectoryLabel, "newline");
        add(moveToDirectoryField, "w 250!");

        add(variableListScrollPane, "spany 6, left, growy");

        add(moveToFileNameLabel, "newline");
        add(moveToFileNameField, "w 250!");

        add(errorReadingActionLabel, "newline");
        add(errorReadingActionComboBox, "w 55!");

        add(errorResponseActionLabel, "newline");
        add(errorResponseActionComboBox, "w 140!");

        add(errorMoveToDirectoryLabel, "newline");
        add(errorMoveToDirectoryField, "w 250!");

        add(errorMoveToFileNameLabel, "newline");
        add(errorMoveToFileNameField, "w 250!");

        add(checkFileAgeLabel, "newline");
        add(checkFileAgeYesRadio, "split 2, spanx");
        add(checkFileAgeNoRadio);

        add(checkFileAgeLabel, "newline");
        add(checkFileAgeYesRadio, "split 2, spanx");
        add(checkFileAgeNoRadio);

        add(fileAgeLabel, "newline");
        add(fileAgeField, "w 75!, spanx");

        add(fileSizeLabel, "newline");
        add(fileSizeMinimumField, "w 75!, split 4, spanx");
        add(fileSizeDashLabel);
        add(fileSizeMaximumField, "w 75!");
        add(ignoreFileSizeMaximumCheckBox);

        add(sortFilesByLabel, "newline");
        add(sortByComboBox, "w 75!, spanx");

        add(fileTypeLabel, "newline");
        add(fileTypeBinary, "split 2, spanx");
        add(fileTypeText);

        add(encodingLabel, "newline");
        add(charsetEncodingComboBox, "w 125!, spanx");
    }

    private void anonymousNoActionPerformed(ActionEvent evt) {
        usernameLabel.setEnabled(true);
        usernameField.setEnabled(true);

        passwordLabel.setEnabled(true);
        passwordField.setEnabled(true);

        FileScheme scheme = (FileScheme) schemeComboBox.getSelectedItem();
        if (scheme == FileScheme.S3) {
            usernameField.setText("");
            passwordField.setText("");
        }
    }

    private void anonymousYesActionPerformed(ActionEvent evt) {
        usernameLabel.setEnabled(false);
        usernameField.setEnabled(false);

        passwordLabel.setEnabled(false);
        passwordField.setEnabled(false);

        FileScheme scheme = (FileScheme) schemeComboBox.getSelectedItem();
        if (scheme == FileScheme.S3) {
            usernameField.setText("");
            passwordField.setText("");
        } else {
            usernameField.setText("anonymous");
            passwordField.setText("anonymous");
        }
    }

    private void onSchemeChange(boolean enableHost, boolean anonymous, FileScheme scheme) {
        // act like the appropriate Anonymous button was selected.
        if (anonymous) {
            anonymousYesRadio.setSelected(true);
            anonymousYesActionPerformed(null);
        } else {
            anonymousNoRadio.setSelected(true);
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
        anonymousYesRadio.setEnabled(false);
        anonymousNoRadio.setEnabled(false);
        passiveModeLabel.setEnabled(false);
        passiveModeYesRadio.setEnabled(false);
        passiveModeNoRadio.setEnabled(false);
        validateConnectionLabel.setEnabled(false);
        validateConnectionYesRadio.setEnabled(false);
        validateConnectionNoRadio.setEnabled(false);
        secureModeLabel.setEnabled(false);
        secureModeYesRadio.setEnabled(false);
        secureModeNoRadio.setEnabled(false);
        timeoutLabel.setEnabled(false);
        timeoutField.setEnabled(false);
        advancedSettingsButton.setEnabled(false);
        advancedProperties = null;
        usernameLabel.setText("Username:");
        passwordLabel.setText("Password:");
        usernameField.setToolTipText("The user name used to gain access to the server.");
        passwordField.setToolTipText("The password used to gain access to the server.");
        moveToDirectoryLabel.setText("Move-to Directory:");
        errorMoveToDirectoryLabel.setText("Error Move-to Directory:");

        if (scheme == FileScheme.FTP) {
            anonymousLabel.setEnabled(true);
            anonymousYesRadio.setEnabled(true);
            anonymousNoRadio.setEnabled(true);
            passiveModeLabel.setEnabled(true);
            passiveModeYesRadio.setEnabled(true);
            passiveModeNoRadio.setEnabled(true);
            validateConnectionLabel.setEnabled(true);
            validateConnectionYesRadio.setEnabled(true);
            validateConnectionNoRadio.setEnabled(true);
            timeoutLabel.setEnabled(true);
            timeoutField.setEnabled(true);
        } else if (scheme == FileScheme.SFTP) {
            timeoutLabel.setEnabled(true);
            timeoutField.setEnabled(true);
            advancedSettingsButton.setEnabled(true);
            advancedProperties = new SftpSchemeProperties();
        } else if (scheme == FileScheme.S3) {
            anonymousLabel.setEnabled(true);
            anonymousYesRadio.setEnabled(true);
            anonymousNoRadio.setEnabled(true);
            timeoutLabel.setEnabled(true);
            timeoutField.setEnabled(true);
            advancedSettingsButton.setEnabled(true);
            advancedProperties = new S3SchemeProperties();
            usernameLabel.setText("AWS Access Key ID:");
            usernameField.setToolTipText("The access key ID used to authenticate to AWS S3. This is optional when using the default credential provider chain.");
            passwordLabel.setText("AWS Secret Access Key:");
            passwordField.setToolTipText("The secret access key used to authenticate to AWS S3. This is optional when using the default credential provider chain.");
            moveToDirectoryLabel.setText("Move-to S3 Bucket / Directory:");
            errorMoveToDirectoryLabel.setText("Error Move-to S3 Bucket / Directory:");
        } else if (scheme == FileScheme.WEBDAV) {
            anonymousLabel.setEnabled(true);
            anonymousYesRadio.setEnabled(true);
            anonymousNoRadio.setEnabled(true);
            secureModeLabel.setEnabled(true);
            secureModeYesRadio.setEnabled(true);
            secureModeNoRadio.setEnabled(true);

            // set Passive Mode and validate connection to No.
            passiveModeNoRadio.setSelected(true);
        } else if (scheme == FileScheme.SMB) {
            timeoutLabel.setEnabled(true);
            timeoutField.setEnabled(true);
        }

        setSummaryText();
    }

    private void advancedFileSettingsActionPerformed() {
        if (selectedScheme == FileScheme.SFTP) {
            AdvancedSettingsDialog dialog = new AdvancedSftpSettingsDialog((SftpSchemeProperties) advancedProperties);
            if (dialog.wasSaved()) {
                advancedProperties = dialog.getSchemeProperties();
                setSummaryText();
            }
        } else if (selectedScheme == FileScheme.S3) {
            AdvancedSettingsDialog dialog = new AdvancedS3SettingsDialog((S3SchemeProperties) advancedProperties, anonymousYesRadio.isSelected());
            if (dialog.wasSaved()) {
                advancedProperties = dialog.getSchemeProperties();
                setSummaryText();
            }
        }
    }

    private boolean isAdvancedDefault() {
        if (selectedScheme == FileScheme.SFTP) {
            return Objects.equals(advancedProperties, new SftpSchemeProperties());
        } else if (selectedScheme == FileScheme.S3) {
            return Objects.equals(advancedProperties, new S3SchemeProperties());
        }
        return true;
    }

    private void schemeComboBoxActionPerformed(ActionEvent evt) {
        FileScheme scheme = (FileScheme) schemeComboBox.getSelectedItem();

        if (scheme != selectedScheme) {
            if (selectedScheme != null && !isAdvancedDefault()) {
                if (JOptionPane.showConfirmDialog(parent, "Are you sure you would like to change the scheme mode and lose all of the current properties?", "Select an Option", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    schemeComboBox.setSelectedItem(selectedScheme);
                    return;
                }
            }

            // if File is selected
            if (scheme == FileScheme.FILE) {
                onSchemeChange(false, true, FileScheme.FILE);
                hostField.setText("");
            } // else if FTP is selected
            else if (scheme == FileScheme.FTP) {
                onSchemeChange(true, anonymousYesRadio.isSelected(), FileScheme.FTP);
                hostLabel.setText("ftp://");
            } // else if SFTP is selected
            else if (scheme == FileScheme.SFTP) {
                onSchemeChange(true, false, FileScheme.SFTP);
                hostLabel.setText("sftp://");
            } else if (scheme == FileScheme.S3) {
                onSchemeChange(true, true, FileScheme.S3);
                hostLabel.setText("S3 Bucket:");
            } // else if SMB is selected
            else if (scheme == FileScheme.SMB) {
                onSchemeChange(true, false, FileScheme.SMB);
                hostLabel.setText("smb://");
            } // else if WEBDAV is selected
            else if (scheme == FileScheme.WEBDAV) {
                onSchemeChange(true, anonymousYesRadio.isSelected(), FileScheme.WEBDAV);
                hostLabel.setText("https://");
            }

            decorateConnectorType();
        }

        selectedScheme = scheme;
    }

    private void fileTypeASCIIActionPerformed(ActionEvent evt) {
        encodingLabel.setEnabled(true);
        charsetEncodingComboBox.setEnabled(true);
    }

    private void fileTypeBinaryActionPerformed(ActionEvent evt) {
        encodingLabel.setEnabled(false);
        charsetEncodingComboBox.setEnabled(false);
        charsetEncodingComboBox.setSelectedIndex(0);
    }

    private void testConnectionActionPerformed(ActionEvent evt) {
        ResponseHandler handler = new ResponseHandler() {
            @Override
            public void handle(Object response) {
                ConnectionTestResponse connectionTestResponse = (ConnectionTestResponse) response;

                if (connectionTestResponse == null) {
                    parent.alertError(parent, "Failed to invoke service.");
                } else if (connectionTestResponse.getType().equals(ConnectionTestResponse.Type.SUCCESS)) {
                    parent.alertInformation(parent, connectionTestResponse.getMessage());
                } else {
                    parent.alertWarning(parent, connectionTestResponse.getMessage());
                }
            }
        };

        try {
            getServlet(FileConnectorServletInterface.class, "Testing connection...", "Failed to invoke service: ", handler).testRead(getChannelId(), getChannelName(), (FileReceiverProperties) getFilledProperties());
        } catch (ClientException e) {
            // Should not happen
        }
    }

    private void secureModeYesActionPerformed(ActionEvent evt) {
        // only WebDAV has access to here.
        // change host label to 'https://'
        hostLabel.setText("https://");
    }

    private void secureModeNoActionPerformed(ActionEvent evt) {
        // only WebDAV has access to here.
        // change host label to 'http://'
        hostLabel.setText("http://");
    }

    private void afterProcessingActionComboBoxActionPerformed(ActionEvent evt) {
        boolean enabled = (FileAction) afterProcessingActionComboBox.getSelectedItem() == FileAction.MOVE;
        moveToDirectoryLabel.setEnabled(enabled);
        moveToDirectoryField.setEnabled(enabled);
        moveToFileNameLabel.setEnabled(enabled);
        moveToFileNameField.setEnabled(enabled);
    }

    private void errorReadingActionComboBoxActionPerformed(ActionEvent evt) {
        updateErrorFields();
    }

    private void errorResponseActionComboBoxActionPerformed(ActionEvent evt) {
        updateErrorFields();
    }

    private void directoryRecursionYesRadioActionPerformed(ActionEvent evt) {
        if (!parent.alertOption(parent, "<html>Including all subdirectories recursively is not recommended, especially if you are<br/>moving or deleting files.&nbsp;&nbsp;Are you sure you want to enable directory recursion?</html>")) {
            directoryRecursionNoRadio.setSelected(true);
        }
    }

    private void ignoreFileSizeMaximumCheckBoxActionPerformed(ActionEvent evt) {
        fileSizeDashLabel.setEnabled(!ignoreFileSizeMaximumCheckBox.isSelected());
        fileSizeMaximumField.setEnabled(!ignoreFileSizeMaximumCheckBox.isSelected());
    }

    private void updateErrorFields() {
        FileAction readAction = (FileAction) errorReadingActionComboBox.getSelectedItem();
        FileAction responseAction = (FileAction) errorResponseActionComboBox.getSelectedItem();
        boolean enabled = readAction == FileAction.MOVE || responseAction == FileAction.MOVE;
        errorMoveToDirectoryLabel.setEnabled(enabled);
        errorMoveToDirectoryField.setEnabled(enabled);
        errorMoveToFileNameLabel.setEnabled(enabled);
        errorMoveToFileNameField.setEnabled(enabled);
    }

    private void checkFileAgeNoActionPerformed(ActionEvent evt) {
        fileAgeLabel.setEnabled(false);
        fileAgeField.setEnabled(false);
    }

    private void checkFileAgeYesActionPerformed(ActionEvent evt) {
        fileAgeLabel.setEnabled(true);
        fileAgeField.setEnabled(true);
    }

    private MirthComboBox afterProcessingActionComboBox;
    private JLabel afterProcessingActionLabel;
    private JLabel anonymousLabel;
    private MirthRadioButton anonymousNoRadio;
    private MirthRadioButton anonymousYesRadio;
    private ButtonGroup anonymousButtonGroup;
    private ButtonGroup checkFileAgeButtonGroup;
    private ButtonGroup fileTypeButtonGroup;
    private ButtonGroup secureModeButtonGroup;
    private MirthComboBox charsetEncodingComboBox;
    private JLabel checkFileAgeLabel;
    private MirthRadioButton checkFileAgeNoRadio;
    private MirthRadioButton checkFileAgeYesRadio;
    private MirthTextField directoryField;
    private JLabel directoryLabel;
    private ButtonGroup directoryRecursionButtonGroup;
    private JLabel directoryRecursionLabel;
    private MirthRadioButton directoryRecursionNoRadio;
    private MirthRadioButton directoryRecursionYesRadio;
    private JLabel encodingLabel;
    private MirthTextField errorMoveToDirectoryField;
    private JLabel errorMoveToDirectoryLabel;
    private MirthTextField errorMoveToFileNameField;
    private JLabel errorMoveToFileNameLabel;
    private MirthComboBox errorReadingActionComboBox;
    private MirthComboBox errorResponseActionComboBox;
    private JLabel errorResponseActionLabel;
    private MirthTextField fileAgeField;
    private JLabel fileAgeLabel;
    private MirthTextField fileNameFilterField;
    private JLabel fileSizeDashLabel;
    private JLabel fileSizeLabel;
    private MirthTextField fileSizeMaximumField;
    private MirthTextField fileSizeMinimumField;
    private MirthRadioButton fileTypeText;
    private MirthRadioButton fileTypeBinary;
    private JLabel fileTypeLabel;
    private JLabel filenameFilterLabel;
    private MirthCheckBox filenameFilterRegexCheckBox;
    private MirthTextField hostField;
    private JLabel hostLabel;
    private ButtonGroup ignoreDotFilesButtonGroup;
    private JLabel ignoreDotFilesLabel;
    private MirthRadioButton ignoreDotFilesNoRadio;
    private MirthRadioButton ignoreDotFilesYesRadio;
    private MirthCheckBox ignoreFileSizeMaximumCheckBox;
    private JScrollPane variableListScrollPane;
    private MirthVariableList mirthVariableList;
    private MirthTextField moveToDirectoryField;
    private JLabel moveToDirectoryLabel;
    private JLabel errorReadingActionLabel;
    private MirthTextField moveToFileNameField;
    private JLabel moveToFileNameLabel;
    private MirthPasswordField passwordField;
    private JLabel passwordLabel;
    private MirthTextField pathField;
    private JLabel pathLabel;
    private MirthComboBox<FileScheme> schemeComboBox;
    private JLabel schemeLabel;
    private MirthComboBox sortByComboBox;
    private JLabel sortFilesByLabel;
    private JButton testConnectionButton;
    private MirthTextField timeoutField;
    private JLabel timeoutLabel;
    private MirthTextField usernameField;
    private JLabel usernameLabel;

    private JButton advancedSettingsButton;
    private JLabel summaryLabel;
    private JLabel summaryField;

    private JLabel passiveModeLabel;
    private MirthRadioButton passiveModeNoRadio;
    private MirthRadioButton passiveModeYesRadio;
    private ButtonGroup passiveModeButtonGroup;

    private JLabel validateConnectionLabel;
    private MirthRadioButton validateConnectionNoRadio;
    private MirthRadioButton validateConnectionYesRadio;
    private ButtonGroup validateConnectionButtonGroup;

    private JLabel secureModeLabel;
    private MirthRadioButton secureModeNoRadio;
    private MirthRadioButton secureModeYesRadio;
}