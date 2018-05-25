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

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.ConnectorTypeDecoration;
import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthComboBox;
import com.mirth.connect.client.ui.components.MirthPasswordField;
import com.mirth.connect.client.ui.components.MirthRadioButton;
import com.mirth.connect.client.ui.components.MirthSyntaxTextArea;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.client.ui.panels.connectors.ResponseHandler;
import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.model.Connector.Mode;
import com.mirth.connect.util.ConnectionTestResponse;

public class FileWriter extends ConnectorSettingsPanel {

    private Logger logger = Logger.getLogger(this.getClass());
    private Frame parent;

    private FileScheme selectedScheme;
    private SchemeProperties advancedProperties;

    public FileWriter() {
        this.parent = PlatformUI.MIRTH_FRAME;

        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        setLayout(new MigLayout("novisualpadding, hidemode 3, insets 0, fill, gapy 6", "[right,120][left]"));

        initComponents();
        initToolTips();
        initLayout();

        parent.setupCharsetEncodingForConnector(charsetEncodingComboBox);
    }

    @Override
    public String getConnectorName() {
        return new FileDispatcherProperties().getName();
    }

    @Override
    public ConnectorProperties getProperties() {
        FileDispatcherProperties properties = new FileDispatcherProperties();

        properties.setScheme((FileScheme) schemeComboBox.getSelectedItem());

        properties.setSchemeProperties(advancedProperties);

        if (schemeComboBox.getSelectedItem() == FileScheme.FILE) {
            properties.setHost(directoryField.getText().replace('\\', '/'));
        } else {
            properties.setHost(hostField.getText() + "/" + pathField.getText());
        }

        properties.setOutputPattern(fileNameField.getText());

        properties.setAnonymous(anonymousYesRadio.isSelected());

        properties.setUsername(usernameField.getText());
        properties.setPassword(new String(passwordField.getPassword()));

        properties.setTimeout(timeoutField.getText());

        properties.setSecure(secureModeYesRadio.isSelected());
        properties.setPassive(passiveModeYesRadio.isSelected());
        properties.setValidateConnection(validateConnectionYesRadio.isSelected());

        if (fileExistsAppendRadio.isSelected()) {
            properties.setOutputAppend(true);
            properties.setErrorOnExists(false);
        } else if (fileExistsErrorRadio.isSelected()) {
            properties.setOutputAppend(false);
            properties.setErrorOnExists(true);
        } else {
            properties.setOutputAppend(false);
            properties.setErrorOnExists(false);
        }

        properties.setTemporary(tempFileYesRadio.isSelected());
        properties.setTemplate(fileContentsTextPane.getText());

        properties.setCharsetEncoding(parent.getSelectedEncodingForConnector(charsetEncodingComboBox));

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

        selectedScheme = null;
        FileScheme scheme = props.getScheme();
        schemeComboBox.setSelectedItem(scheme);
        schemeComboBoxActionPerformed(null);

        advancedProperties = props.getSchemeProperties();
        setSummaryText();

        setDirHostPath(props, true, false);

        fileNameField.setText(props.getOutputPattern());

        if (props.isAnonymous()) {
            anonymousYesRadio.setSelected(true);
            anonymousNoRadio.setSelected(false);
            anonymousYesActionPerformed(null);
        } else {
            anonymousYesRadio.setSelected(false);
            anonymousNoRadio.setSelected(true);
            anonymousNoActionPerformed(null);
            usernameField.setText(props.getUsername());
            passwordField.setText(props.getPassword());
        }

        timeoutField.setText(props.getTimeout());

        if (props.isSecure()) {
            secureModeYesRadio.setSelected(true);
            secureModeNoRadio.setSelected(false);
            if (scheme == FileScheme.WEBDAV) {
                hostLabel.setText("https://");
            }
        } else {
            secureModeYesRadio.setSelected(false);
            secureModeNoRadio.setSelected(true);
            if (scheme == FileScheme.WEBDAV) {
                hostLabel.setText("http://");
            }
        }

        if (props.isPassive()) {
            passiveModeYesRadio.setSelected(true);
            passiveModeNoRadio.setSelected(false);
        } else {
            passiveModeYesRadio.setSelected(false);
            passiveModeNoRadio.setSelected(true);
        }

        if (props.isValidateConnection()) {
            validateConnectionYesRadio.setSelected(true);
            validateConnectionNoRadio.setSelected(false);
        } else {
            validateConnectionYesRadio.setSelected(false);
            validateConnectionNoRadio.setSelected(true);
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

        parent.setPreviousSelectedEncodingForConnector(charsetEncodingComboBox, props.getCharsetEncoding());

        fileContentsTextPane.setText(props.getTemplate());

        if (props.isBinary()) {
            fileTypeBinary.setSelected(true);
            fileTypeText.setSelected(false);
            fileTypeBinaryActionPerformed(null);
        } else {
            fileTypeBinary.setSelected(false);
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
        return new ConnectorTypeDecoration(Mode.DESTINATION);
    }

    @Override
    public void doLocalDecoration(ConnectorTypeDecoration connectorTypeDecoration) {
        if (FileScheme.FTP == schemeComboBox.getSelectedItem()) {
            hostLabel.setText("ftp" + (connectorTypeDecoration != null && connectorTypeDecoration.getHighlightColor() != null ? "s" : "") + "://");
        }
    }

    private void initComponents() {
        schemeLabel = new JLabel();
        schemeLabel.setText("Method:");
        schemeComboBox = new MirthComboBox<FileScheme>();
        schemeComboBox.setModel(new DefaultComboBoxModel<FileScheme>(FileScheme.values()));
        schemeComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                schemeComboBoxActionPerformed(evt);
            }
        });

        testConnectionButton = new JButton();
        testConnectionButton.setText("Test Write");
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

        hostLabel = new JLabel();
        hostLabel.setText("ftp://");
        hostField = new MirthTextField();
        pathLabel = new JLabel();
        pathLabel.setText("/");
        pathField = new MirthTextField();

        fileNameLabel = new JLabel();
        fileNameLabel.setText("File Name:");

        fileNameField = new MirthTextField();

        anonymousLabel = new JLabel();
        anonymousLabel.setText("Anonymous:");

        anonymousYesRadio = new MirthRadioButton();
        anonymousYesRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        anonymousYesRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        anonymousYesRadio.setText("Yes");
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

        passwordLabel = new JLabel();
        passwordLabel.setText("Password:");
        passwordField = new MirthPasswordField();

        timeoutLabel = new JLabel();
        timeoutLabel.setText("Timeout (ms):");
        timeoutField = new MirthTextField();

        secureModeLabel = new JLabel();
        secureModeLabel.setText("Secure Mode:");

        secureModeYesRadio = new MirthRadioButton();
        secureModeYesRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        secureModeYesRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        secureModeYesRadio.setText("Yes");
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
        passiveModeYesRadio.setMargin(new Insets(0, 0, 0, 0));

        passiveModeNoRadio = new MirthRadioButton();
        passiveModeNoRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        passiveModeNoRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        passiveModeNoRadio.setSelected(true);
        passiveModeNoRadio.setText("No");
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
        validateConnectionYesRadio.setMargin(new Insets(0, 0, 0, 0));

        validateConnectionNoRadio = new MirthRadioButton();
        validateConnectionNoRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        validateConnectionNoRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        validateConnectionNoRadio.setText("No");
        validateConnectionNoRadio.setMargin(new Insets(0, 0, 0, 0));

        validateConnectionButtonGroup = new ButtonGroup();
        validateConnectionButtonGroup.add(validateConnectionYesRadio);
        validateConnectionButtonGroup.add(validateConnectionNoRadio);

        fileExistsLabel = new JLabel();
        fileExistsLabel.setText("File Exists:");

        fileExistsAppendRadio = new MirthRadioButton();
        fileExistsAppendRadio.setBackground(new java.awt.Color(255, 255, 255));
        fileExistsAppendRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        fileExistsAppendRadio.setText("Append");
        fileExistsAppendRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        fileExistsAppendRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                fileExistsAppendRadioActionPerformed(evt);
            }
        });

        fileExistsOverwriteRadio = new MirthRadioButton();
        fileExistsOverwriteRadio.setBackground(new java.awt.Color(255, 255, 255));
        fileExistsOverwriteRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        fileExistsOverwriteRadio.setText("Overwrite");
        fileExistsOverwriteRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        fileExistsOverwriteRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                fileExistsOverwriteRadioActionPerformed(evt);
            }
        });

        fileExistsErrorRadio = new MirthRadioButton();
        fileExistsErrorRadio.setBackground(new java.awt.Color(255, 255, 255));
        fileExistsErrorRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        fileExistsErrorRadio.setSelected(true);
        fileExistsErrorRadio.setText("Error");
        fileExistsErrorRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        fileExistsErrorRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                fileExistsErrorRadioActionPerformed(evt);
            }
        });

        fileExistsButtonGroup = new ButtonGroup();
        fileExistsButtonGroup.add(fileExistsAppendRadio);
        fileExistsButtonGroup.add(fileExistsOverwriteRadio);
        fileExistsButtonGroup.add(fileExistsErrorRadio);

        tempFileLabel = new JLabel();
        tempFileLabel.setText("Create Temp File:");

        tempFileYesRadio = new MirthRadioButton();
        tempFileYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        tempFileYesRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tempFileYesRadio.setText("Yes");
        tempFileYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        tempFileNoRadio = new MirthRadioButton();
        tempFileNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        tempFileNoRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tempFileNoRadio.setSelected(true);
        tempFileNoRadio.setText("No");
        tempFileNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        tempFileButtonGroup = new ButtonGroup();
        tempFileButtonGroup.add(tempFileYesRadio);
        tempFileButtonGroup.add(tempFileNoRadio);

        fileTypeLabel = new JLabel();
        fileTypeLabel.setText("File Type:");

        fileTypeBinary = new MirthRadioButton();
        fileTypeBinary.setBackground(UIConstants.BACKGROUND_COLOR);
        fileTypeBinary.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        fileTypeBinary.setText("Binary");
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

        templateLabel = new JLabel();
        templateLabel.setText("Template:");

        fileContentsTextPane = new MirthSyntaxTextArea();
        fileContentsTextPane.setBorder(BorderFactory.createEtchedBorder());
    }

    private void initToolTips() {
        schemeComboBox.setToolTipText("The basic method used to write files with - file (local filesystem), FTP, SFTP, Samba share, or WebDAV");
        directoryField.setToolTipText("The directory (folder) to write the files to.");
        hostField.setToolTipText("The name or IP address of the host (computer) on which the files can be written.");
        pathField.setToolTipText("The directory (folder) to write the files to.");
        anonymousYesRadio.setToolTipText("Connects to the file anonymously instead of using a username and password.");
        anonymousNoRadio.setToolTipText("Connects to the file using a username and password instead of anonymously.");
        usernameField.setToolTipText("The user name used to gain access to the server.");
        passwordField.setToolTipText("The password used to gain access to the server.");
        timeoutField.setToolTipText("The socket timeout (in ms) for connecting to the server.");

        String toolTipText = "<html>Select Yes to connect to the server via HTTPS.<br>Select No to connect via HTTP.</html>";
        secureModeYesRadio.setToolTipText(toolTipText);
        secureModeNoRadio.setToolTipText(toolTipText);

        passiveModeYesRadio.setToolTipText("<html>Select Yes to connect to the server in \"passive mode\".<br>Passive mode sometimes allows a connection through a firewall that normal mode does not.</html>");
        passiveModeNoRadio.setToolTipText("Select Yes to connect to the server in \"normal mode\" as opposed to passive mode.");
        validateConnectionYesRadio.setToolTipText("Select Yes to test the connection to the server before each operation.");
        validateConnectionNoRadio.setToolTipText("Select No to skip testing the connection to the server before each operation.");

        toolTipText = "<html>If 'append' is selected, messages accepted by this destination will be appended to the file specified in the File Name.<br>If 'overwrite' is selected, messages accepted by this destination will replace any existing file of the same name.<br>If 'error' is selected and a file with the specified file name already exists, the message will error.</html>";
        fileExistsAppendRadio.setToolTipText(toolTipText);
        fileExistsOverwriteRadio.setToolTipText(toolTipText);
        fileExistsErrorRadio.setToolTipText(toolTipText);

        toolTipText = "<html>If 'yes' is selected, the file contents will first be written to a temp file and then renamed to the specified file name.<br>If 'no' is selected, the file contents will be written directly to the destination file.<br>Using a temp file is not an option if the specified file is being appended to.</html>";
        tempFileYesRadio.setToolTipText(toolTipText);
        tempFileNoRadio.setToolTipText(toolTipText);

        toolTipText = "<html>Select Binary if files contain binary data; the contents will be Base64 decoded before being written out.<br>Select Text if files contain text data; the contents will be written out using the specified character set encoding.</html>";
        fileTypeBinary.setToolTipText(toolTipText);
        fileTypeText.setToolTipText(toolTipText);

        charsetEncodingComboBox.setToolTipText("If File Type Text is selected, select the character set encoding (ASCII, UTF-8, etc.) to be used in writing the contents of each file.");
    }

    private void initLayout() {
        add(schemeLabel);
        add(schemeComboBox, "split 3");
        add(testConnectionButton);
        add(advancedSettingsButton, "h 22!, w 22!, wrap");

        add(summaryLabel);
        add(summaryField, "growx, wrap");

        add(directoryLabel);
        add(directoryField, "w 200!, wrap");

        add(hostLabel);
        add(hostField, "w 200!, split 3");
        add(pathLabel, "gapleft 14");
        add(pathField, "gapleft 14, w 200!, wrap");

        add(fileNameLabel);
        add(fileNameField, "w 200!, wrap");

        add(anonymousLabel);
        add(anonymousYesRadio, "split 2");
        add(anonymousNoRadio, "wrap");

        add(usernameLabel);
        add(usernameField, "w 125!, wrap");

        add(passwordLabel);
        add(passwordField, "w 125!, wrap");

        add(timeoutLabel);
        add(timeoutField, "w 75!, wrap");

        add(secureModeLabel);
        add(secureModeYesRadio, "split 2");
        add(secureModeNoRadio, "wrap");

        add(passiveModeLabel);
        add(passiveModeYesRadio, "split 2");
        add(passiveModeNoRadio, "wrap");

        add(validateConnectionLabel);
        add(validateConnectionYesRadio, "split 2");
        add(validateConnectionNoRadio, "wrap");

        add(fileExistsLabel);
        add(fileExistsAppendRadio, "split 3");
        add(fileExistsOverwriteRadio);
        add(fileExistsErrorRadio, "wrap");

        add(tempFileLabel);
        add(tempFileYesRadio, "split 2");
        add(tempFileNoRadio, "wrap");

        add(fileTypeLabel);
        add(fileTypeBinary, "split 2");
        add(fileTypeText, "wrap");

        add(encodingLabel);
        add(charsetEncodingComboBox, "w 125!, wrap");

        add(templateLabel, "aligny top");
        add(fileContentsTextPane, "w 425, h 105, grow, span, push");
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

    private void onSchemeChange(boolean enableHost, boolean anonymous, boolean allowAppend, FileScheme scheme) {
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

        anonymousLabel.setEnabled(false);
        anonymousYesRadio.setEnabled(false);
        anonymousNoRadio.setEnabled(false);
        passiveModeLabel.setEnabled(false);
        passiveModeYesRadio.setEnabled(false);
        passiveModeNoRadio.setEnabled(false);
        secureModeLabel.setEnabled(false);
        secureModeYesRadio.setEnabled(false);
        secureModeNoRadio.setEnabled(false);
        validateConnectionLabel.setEnabled(false);
        validateConnectionYesRadio.setEnabled(false);
        validateConnectionNoRadio.setEnabled(false);
        timeoutLabel.setEnabled(false);
        timeoutField.setEnabled(false);
        advancedSettingsButton.setEnabled(false);
        advancedProperties = null;
        usernameLabel.setText("Username:");
        passwordLabel.setText("Password:");
        usernameField.setToolTipText("The user name used to gain access to the server.");
        passwordField.setToolTipText("The password used to gain access to the server.");

        if (allowAppend) {
            fileExistsOverwriteRadio.setEnabled(true);
            fileExistsAppendRadio.setEnabled(true);
        } else {
            if (fileExistsAppendRadio.isSelected()) {
                fileExistsOverwriteRadio.setSelected(true);
                fileExistsAppendRadio.setSelected(false);
                fileExistsOverwriteRadioActionPerformed(null);
            }

            fileExistsAppendRadio.setEnabled(false);
        }

        tempFileLabel.setEnabled(true);
        tempFileYesRadio.setEnabled(true);
        tempFileNoRadio.setEnabled(true);

        if (scheme.equals(FileScheme.FTP)) {
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
        } else if (scheme.equals(FileScheme.SFTP)) {
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
            tempFileLabel.setEnabled(false);
            tempFileNoRadio.setSelected(true);
            tempFileYesRadio.setEnabled(false);
            tempFileNoRadio.setEnabled(false);
        } else if (scheme.equals(FileScheme.WEBDAV)) {
            anonymousLabel.setEnabled(true);
            anonymousYesRadio.setEnabled(true);
            anonymousNoRadio.setEnabled(true);
            secureModeLabel.setEnabled(true);
            secureModeYesRadio.setEnabled(true);
            secureModeNoRadio.setEnabled(true);

            // set Passive Mode and validate connection to No.
            passiveModeNoRadio.setSelected(true);
            validateConnectionNoRadio.setSelected(true);

        } else if (scheme.equals(FileScheme.SMB)) {
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
            AdvancedS3SettingsDialog dialog = new AdvancedS3SettingsDialog((S3SchemeProperties) advancedProperties, anonymousYesRadio.isSelected());
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

                onSchemeChange(false, true, true, FileScheme.FILE);
            } // else if FTP is selected
            else if (scheme == FileScheme.FTP) {

                onSchemeChange(true, anonymousYesRadio.isSelected(), true, FileScheme.FTP);
                hostLabel.setText("ftp://");
            } // else if SFTP is selected
            else if (scheme == FileScheme.SFTP) {

                onSchemeChange(true, false, true, FileScheme.SFTP);
                hostLabel.setText("sftp://");
            } else if (scheme == FileScheme.S3) {
                onSchemeChange(true, true, false, FileScheme.S3);
                hostLabel.setText("S3 Bucket:");
            } // else if SMB is selected
            else if (scheme == FileScheme.SMB) {

                onSchemeChange(true, false, true, FileScheme.SMB);
                hostLabel.setText("smb://");
            } // else if WEBDAV is selected
            else if (scheme == FileScheme.WEBDAV) {

                onSchemeChange(true, anonymousYesRadio.isSelected(), false, FileScheme.WEBDAV);
                if (secureModeYesRadio.isSelected()) {
                    hostLabel.setText("https://");
                } else {
                    hostLabel.setText("http://");
                }
            }

            decorateConnectorType();
        }

        selectedScheme = scheme;
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
            getServlet(FileConnectorServletInterface.class, "Testing connection...", "Failed to invoke service: ", handler).testWrite(getChannelId(), getChannelName(), (FileDispatcherProperties) getFilledProperties());
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

    private void fileExistsAppendRadioActionPerformed(ActionEvent evt) {
        tempFileNoRadio.setSelected(true);
        tempFileLabel.setEnabled(false);
        tempFileYesRadio.setEnabled(false);
        tempFileNoRadio.setEnabled(false);
    }

    private void fileExistsOverwriteRadioActionPerformed(ActionEvent evt) {
        FileScheme scheme = (FileScheme) schemeComboBox.getSelectedItem();
        if (scheme != FileScheme.S3) {
            tempFileLabel.setEnabled(true);
            tempFileYesRadio.setEnabled(true);
            tempFileNoRadio.setEnabled(true);
        }
    }

    private void fileExistsErrorRadioActionPerformed(ActionEvent evt) {
        FileScheme scheme = (FileScheme) schemeComboBox.getSelectedItem();
        if (scheme != FileScheme.S3) {
            tempFileLabel.setEnabled(true);
            tempFileYesRadio.setEnabled(true);
            tempFileNoRadio.setEnabled(true);
        }
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

    private JLabel anonymousLabel;
    private MirthRadioButton anonymousNoRadio;
    private MirthRadioButton anonymousYesRadio;
    private ButtonGroup anonymousButtonGroup;
    private ButtonGroup passiveModeButtonGroup;
    private ButtonGroup validateConnectionButtonGroup;
    private ButtonGroup fileExistsButtonGroup;
    private ButtonGroup fileTypeButtonGroup;
    private ButtonGroup secureModeButtonGroup;
    private ButtonGroup tempFileButtonGroup;
    private MirthComboBox charsetEncodingComboBox;
    private MirthTextField directoryField;
    private JLabel directoryLabel;
    private JLabel encodingLabel;
    private MirthSyntaxTextArea fileContentsTextPane;
    private MirthRadioButton fileExistsAppendRadio;
    private MirthRadioButton fileExistsErrorRadio;
    private JLabel fileExistsLabel;
    private MirthRadioButton fileExistsOverwriteRadio;
    private MirthTextField fileNameField;
    private JLabel fileNameLabel;
    private MirthRadioButton fileTypeText;
    private MirthRadioButton fileTypeBinary;
    private JLabel fileTypeLabel;
    private MirthTextField hostField;
    private JLabel hostLabel;
    private JLabel passiveModeLabel;
    private MirthRadioButton passiveModeNoRadio;
    private MirthRadioButton passiveModeYesRadio;
    private MirthPasswordField passwordField;
    private JLabel passwordLabel;
    private MirthTextField pathField;
    private JLabel pathLabel;
    private MirthComboBox<FileScheme> schemeComboBox;
    private JLabel schemeLabel;
    private JLabel secureModeLabel;
    private MirthRadioButton secureModeNoRadio;
    private MirthRadioButton secureModeYesRadio;
    private JLabel tempFileLabel;
    private MirthRadioButton tempFileNoRadio;
    private MirthRadioButton tempFileYesRadio;
    private JLabel templateLabel;
    private JButton testConnectionButton;
    private MirthTextField timeoutField;
    private JLabel timeoutLabel;
    private MirthTextField usernameField;
    private JLabel usernameLabel;
    private JLabel validateConnectionLabel;
    private MirthRadioButton validateConnectionNoRadio;
    private MirthRadioButton validateConnectionYesRadio;

    private JButton advancedSettingsButton;
    private JLabel summaryLabel;
    private JLabel summaryField;
}
