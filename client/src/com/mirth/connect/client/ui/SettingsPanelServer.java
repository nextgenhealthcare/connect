/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Cursor;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.model.ServerConfiguration;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.util.ImportConverter;

public class SettingsPanelServer extends AbstractSettingsPanel {

    public SettingsPanelServer(String tabName) {
        super(tabName);

        initComponents();

        addTask("doBackup", "Backup Config", "Backup your server configuration to an XML file. The backup includes channels, alerts, code templates, server properties, global scripts, and plugin properties.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_disk.png")));
        addTask("doRestore", "Restore Config", "Restore your server configuration from a server configuration XML file. This will remove and restore your channels, alerts, code templates, server properties, global scripts, and plugin properties.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_go.png")));

        provideUsageStatsMoreInfoLabel.setToolTipText(UIConstants.PRIVACY_TOOLTIP);
        provideUsageStatsMoreInfoLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public void doRefresh() {
        getFrame().setWorking("Loading " + getTabName() + " settings...", true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            Properties serverProperties = null;

            public Void doInBackground() {
                try {
                    if (getFrame().confirmLeave()) {
                        serverProperties = getFrame().mirthClient.getServerProperties();
                    }
                } catch (ClientException e) {
                    getFrame().alertException(getFrame(), e.getStackTrace(), e.getMessage());
                }
                return null;
            }

            @Override
            public void done() {
                // null if it failed to get the server properties or if confirmLeave returned false
                if (serverProperties != null) {
                    setProperties(serverProperties);
                }
                getFrame().setWorking("", false);
            }
        };

        worker.execute();
    }

    public void doSave() {
        getFrame().setWorking("Saving " + getTabName() + " settings...", true);

        final Properties serverProperties = getProperties();

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                try {
                    getFrame().mirthClient.setServerProperties(serverProperties);
                } catch (ClientException e) {
                    getFrame().alertException(getFrame(), e.getStackTrace(), e.getMessage());
                }

                return null;
            }

            @Override
            public void done() {
                setSaveEnabled(false);
                getFrame().setWorking("", false);
            }
        };

        worker.execute();
    }

    /** Loads the current settings into the Settings form */
    public void setProperties(Properties serverProperties) {
        if (serverProperties.getProperty("smtp.host") != null) {
            smtpHostField.setText(serverProperties.getProperty("smtp.host"));
        } else {
            smtpHostField.setText("");
        }

        if (serverProperties.getProperty("smtp.port") != null) {
            smtpPortField.setText(serverProperties.getProperty("smtp.port"));
        } else {
            smtpPortField.setText("");
        }

        if (serverProperties.getProperty("smtp.from") != null) {
            defaultFromAddressField.setText(serverProperties.getProperty("smtp.from"));
        } else {
            defaultFromAddressField.setText("");
        }

        String smtpSecure = serverProperties.getProperty("smtp.secure");
        if (smtpSecure != null && smtpSecure.equalsIgnoreCase("tls")) {
            secureConnectionTLSRadio.setSelected(true);
        } else if (smtpSecure != null && smtpSecure.equalsIgnoreCase("ssl")) {
            secureConnectionSSLRadio.setSelected(true);
        } else {
            secureConnectionNoneRadio.setSelected(true);
        }

        if (serverProperties.getProperty("smtp.auth") != null) {
            if (serverProperties.getProperty("smtp.auth").equals(UIConstants.YES_OPTION)) {
                requireAuthenticationYesRadio.setSelected(true);
                requireAuthenticationYesRadioActionPerformed(null);
            } else {
                requireAuthenticationNoRadio.setSelected(true);
                requireAuthenticationNoRadioActionPerformed(null);
            }
        } else {
            requireAuthenticationNoRadio.setSelected(true);
            requireAuthenticationNoRadioActionPerformed(null);
        }

        if (serverProperties.getProperty("server.resetglobalvariables") != null) {
            if (serverProperties.getProperty("server.resetglobalvariables").equals(UIConstants.YES_OPTION)) {
                clearGlobalMapYesRadio.setSelected(true);
            } else {
                clearGlobalMapNoRadio.setSelected(true);
            }
        } else {
            clearGlobalMapYesRadio.setSelected(true);
        }

        if (serverProperties.getProperty("update.enabled") != null) {
            if (serverProperties.getProperty("update.enabled").equals(UIConstants.YES_OPTION)) {
                checkForUpdatesYesRadio.setSelected(true);
            } else {
                checkForUpdatesNoRadio.setSelected(true);
            }
        } else {
            checkForUpdatesYesRadio.setSelected(true);
        }

        if (serverProperties.getProperty("stats.enabled") != null) {
            if (serverProperties.getProperty("stats.enabled").equals(UIConstants.YES_OPTION)) {
                provideUsageStatsYesRadio.setSelected(true);
            } else {
                provideUsageStatsNoRadio.setSelected(true);
            }
        } else {
            provideUsageStatsYesRadio.setSelected(true);
        }

        if (serverProperties.getProperty("update.url") != null) {
            updateUrlField.setText(serverProperties.getProperty("update.url"));
        } else {
            updateUrlField.setText("");
        }

        if (serverProperties.getProperty("smtp.username") != null) {
            usernameField.setText(serverProperties.getProperty("smtp.username"));
        } else {
            usernameField.setText("");
        }

        if (serverProperties.getProperty("smtp.password") != null) {
            passwordField.setText(serverProperties.getProperty("smtp.password"));
        } else {
            passwordField.setText("");
        }
    }

    /** Saves the current settings from the settings form */
    public Properties getProperties() {
        Properties serverProperties = new Properties();

        if (clearGlobalMapNoRadio.isSelected()) {
            serverProperties.put("server.resetglobalvariables", UIConstants.NO_OPTION);
        } else {
            serverProperties.put("server.resetglobalvariables", UIConstants.YES_OPTION);
        }

        if (checkForUpdatesNoRadio.isSelected()) {
            serverProperties.put("update.enabled", UIConstants.NO_OPTION);
        } else {
            serverProperties.put("update.enabled", UIConstants.YES_OPTION);
        }

        if (provideUsageStatsNoRadio.isSelected()) {
            serverProperties.put("stats.enabled", UIConstants.NO_OPTION);
        } else {
            serverProperties.put("stats.enabled", UIConstants.YES_OPTION);
        }

        serverProperties.put("update.url", updateUrlField.getText());

        serverProperties.put("smtp.host", smtpHostField.getText());
        serverProperties.put("smtp.port", smtpPortField.getText());
        serverProperties.put("smtp.from", defaultFromAddressField.getText());

        if (secureConnectionTLSRadio.isSelected()) {
            serverProperties.put("smtp.secure", "tls");
        } else if (secureConnectionSSLRadio.isSelected()) {
            serverProperties.put("smtp.secure", "ssl");
        } else {
            serverProperties.put("smtp.secure", "none");
        }

        if (requireAuthenticationYesRadio.isSelected()) {
            serverProperties.put("smtp.auth", UIConstants.YES_OPTION);
            serverProperties.put("smtp.username", usernameField.getText());
            serverProperties.put("smtp.password", new String(passwordField.getPassword()));
        } else {
            serverProperties.put("smtp.auth", UIConstants.NO_OPTION);
            serverProperties.put("smtp.username", "");
            serverProperties.put("smtp.password", "");
        }

        return serverProperties;
    }

    public void doBackup() {
        if (isSaveEnabled()) {
            int option = JOptionPane.showConfirmDialog(this, "Would you like to save the settings first?");

            if (option == JOptionPane.YES_OPTION) {
                doSave();
            } else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
                return;
            }
        }

        String backupDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        ServerConfiguration configuration = null;
        try {
            configuration = getFrame().mirthClient.getServerConfiguration();
        } catch (ClientException e) {
            getFrame().alertException(this, e.getStackTrace(), e.getMessage());
            return;
        }

        configuration.setDate(backupDate);
        String backupXML = serializer.toXML(configuration);

        getFrame().exportFile(backupXML, backupDate.substring(0, 10) + " Mirth Backup.xml", "XML", "Server Configuration");
    }

    public void doRestore() {
        if (getFrame().isSaveEnabled()) {
            if (!getFrame().alertOkCancel(this, "Your new settings will first be saved.  Continue?")) {
                return;
            }
            doSave();
        }

        File backupFile = getFrame().importFile("XML");

        if (backupFile != null) {
            String backupXML = null;
            try {
                backupXML = FileUtils.readFileToString(backupFile, UIConstants.CHARSET);
            } catch (IOException ex) {
                getFrame().alertError(this, "File could not be read.");
                return;
            }
            try {
                ServerConfiguration configuration = ImportConverter.convertServerConfiguration(backupXML);

                if (getFrame().alertOption(this, "Import configuration from " + configuration.getDate() + "?\nWARNING: This will overwrite all current channels,\nalerts, server properties, and plugin properties.")) {
                    try {
                        getFrame().mirthClient.setServerConfiguration(configuration);
                        getFrame().clearChannelCache();
                        doRefresh();
                        getFrame().alertInformation(this, "Your configuration was successfully restored.");
                    } catch (ClientException e) {
                        getFrame().alertException(this, e.getStackTrace(), e.getMessage());
                    }
                }
            } catch (Exception e) {
                getFrame().alertError(this, "Invalid server configuration file.");
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

        requireAuthenticationButtonGroup = new javax.swing.ButtonGroup();
        clearGlobalMapButtonGroup = new javax.swing.ButtonGroup();
        checkForUpdatesButtonGroup = new javax.swing.ButtonGroup();
        provideUsageStatsButtonGroup = new javax.swing.ButtonGroup();
        secureConnectionButtonGroup = new javax.swing.ButtonGroup();
        emailPanel = new javax.swing.JPanel();
        passwordField = new com.mirth.connect.client.ui.components.MirthPasswordField();
        passwordLabel = new javax.swing.JLabel();
        usernameLabel = new javax.swing.JLabel();
        usernameField = new com.mirth.connect.client.ui.components.MirthTextField();
        requireAuthenticationYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        requireAuthenticationNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        requireAuthenticationLabel = new javax.swing.JLabel();
        defaultFromAddressLabel = new javax.swing.JLabel();
        defaultFromAddressField = new com.mirth.connect.client.ui.components.MirthTextField();
        smtpPortLabel = new javax.swing.JLabel();
        smtpPortField = new com.mirth.connect.client.ui.components.MirthTextField();
        smtpHostField = new com.mirth.connect.client.ui.components.MirthTextField();
        smtpHostLabel = new javax.swing.JLabel();
        secureConnectionLabel = new javax.swing.JLabel();
        secureConnectionNoneRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        secureConnectionTLSRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        secureConnectionSSLRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        configurationPanel = new javax.swing.JPanel();
        clearGlobalMapLabel = new javax.swing.JLabel();
        clearGlobalMapYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        clearGlobalMapNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        checkForUpdatesLabel = new javax.swing.JLabel();
        checkForUpdatesYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        checkForUpdatesNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        provideUsageStatsLabel = new javax.swing.JLabel();
        provideUsageStatsYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        provideUsageStatsNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        updateUrlField = new com.mirth.connect.client.ui.components.MirthTextField();
        updateUrlLabel = new javax.swing.JLabel();
        provideUsageStatsMoreInfoLabel = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        emailPanel.setBackground(new java.awt.Color(255, 255, 255));
        emailPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(204, 204, 204)), "Email", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        passwordField.setToolTipText("Password for global SMTP settings.");

        passwordLabel.setText("Password:");

        usernameLabel.setText("Username:");

        usernameField.setToolTipText("Username for global SMTP settings.");

        requireAuthenticationYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        requireAuthenticationYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        requireAuthenticationButtonGroup.add(requireAuthenticationYesRadio);
        requireAuthenticationYesRadio.setSelected(true);
        requireAuthenticationYesRadio.setText("Yes");
        requireAuthenticationYesRadio.setToolTipText("Toggles authentication for global SMTP settings.");
        requireAuthenticationYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        requireAuthenticationYesRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                requireAuthenticationYesRadioActionPerformed(evt);
            }
        });

        requireAuthenticationNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        requireAuthenticationNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        requireAuthenticationButtonGroup.add(requireAuthenticationNoRadio);
        requireAuthenticationNoRadio.setText("No");
        requireAuthenticationNoRadio.setToolTipText("Toggles authentication for global SMTP settings.");
        requireAuthenticationNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        requireAuthenticationNoRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                requireAuthenticationNoRadioActionPerformed(evt);
            }
        });

        requireAuthenticationLabel.setText("Require Authentication:");

        defaultFromAddressLabel.setText("Default from Address:");

        defaultFromAddressField.setToolTipText("Default \"from\" email address used for global SMTP settings.");

        smtpPortLabel.setText("SMTP Port:");

        smtpPortField.setToolTipText("SMTP port used for global SMTP settings.");

        smtpHostField.setToolTipText("SMTP host used for global SMTP settings.");

        smtpHostLabel.setText("SMTP Host:");

        secureConnectionLabel.setText("Secure Connection:");

        secureConnectionNoneRadio.setBackground(new java.awt.Color(255, 255, 255));
        secureConnectionNoneRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        secureConnectionButtonGroup.add(secureConnectionNoneRadio);
        secureConnectionNoneRadio.setSelected(true);
        secureConnectionNoneRadio.setText("None");
        secureConnectionNoneRadio.setToolTipText("Toggles TLS and SSL connections for global SMTP settings.");
        secureConnectionNoneRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        secureConnectionTLSRadio.setBackground(new java.awt.Color(255, 255, 255));
        secureConnectionTLSRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        secureConnectionButtonGroup.add(secureConnectionTLSRadio);
        secureConnectionTLSRadio.setText("TLS");
        secureConnectionTLSRadio.setToolTipText("Toggles TLS and SSL connections for global SMTP settings.");
        secureConnectionTLSRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        secureConnectionSSLRadio.setBackground(new java.awt.Color(255, 255, 255));
        secureConnectionSSLRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        secureConnectionButtonGroup.add(secureConnectionSSLRadio);
        secureConnectionSSLRadio.setText("SSL");
        secureConnectionSSLRadio.setToolTipText("Toggles TLS and SSL connections for global SMTP settings.");
        secureConnectionSSLRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout emailPanelLayout = new javax.swing.GroupLayout(emailPanel);
        emailPanel.setLayout(emailPanelLayout);
        emailPanelLayout.setHorizontalGroup(
            emailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(emailPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(emailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(passwordLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(usernameLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(requireAuthenticationLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(secureConnectionLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(defaultFromAddressLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(smtpPortLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(smtpHostLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(emailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(smtpHostField, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(smtpPortField, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(defaultFromAddressField, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                    .addGroup(emailPanelLayout.createSequentialGroup()
                        .addComponent(secureConnectionNoneRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(secureConnectionTLSRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(secureConnectionSSLRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(emailPanelLayout.createSequentialGroup()
                        .addComponent(requireAuthenticationYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(requireAuthenticationNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(usernameField, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                    .addComponent(passwordField, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE))
                .addContainerGap())
        );

        emailPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {defaultFromAddressField, passwordField, smtpHostField, usernameField});

        emailPanelLayout.setVerticalGroup(
            emailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(emailPanelLayout.createSequentialGroup()
                .addGroup(emailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(smtpHostField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(smtpHostLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(emailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(smtpPortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(smtpPortLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(emailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(defaultFromAddressLabel)
                    .addComponent(defaultFromAddressField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(emailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(secureConnectionLabel)
                    .addComponent(secureConnectionNoneRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(secureConnectionTLSRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(secureConnectionSSLRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(emailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(requireAuthenticationLabel)
                    .addComponent(requireAuthenticationYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(requireAuthenticationNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(emailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usernameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(emailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        configurationPanel.setBackground(new java.awt.Color(255, 255, 255));
        configurationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(204, 204, 204)), "Configuration", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        clearGlobalMapLabel.setText("Clear global map on redeploy:");

        clearGlobalMapYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        clearGlobalMapYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        clearGlobalMapButtonGroup.add(clearGlobalMapYesRadio);
        clearGlobalMapYesRadio.setSelected(true);
        clearGlobalMapYesRadio.setText("Yes");
        clearGlobalMapYesRadio.setToolTipText("Toggles clearing the global map when redeploying all channels.");
        clearGlobalMapYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        clearGlobalMapNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        clearGlobalMapNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        clearGlobalMapButtonGroup.add(clearGlobalMapNoRadio);
        clearGlobalMapNoRadio.setText("No");
        clearGlobalMapNoRadio.setToolTipText("Toggles clearing the global map when redeploying all channels.");
        clearGlobalMapNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        checkForUpdatesLabel.setText("Check for updates:");

        checkForUpdatesYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        checkForUpdatesYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        checkForUpdatesButtonGroup.add(checkForUpdatesYesRadio);
        checkForUpdatesYesRadio.setSelected(true);
        checkForUpdatesYesRadio.setText("Yes");
        checkForUpdatesYesRadio.setToolTipText("Toggles checking for software updates.");
        checkForUpdatesYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        checkForUpdatesNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        checkForUpdatesNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        checkForUpdatesButtonGroup.add(checkForUpdatesNoRadio);
        checkForUpdatesNoRadio.setText("No");
        checkForUpdatesNoRadio.setToolTipText("Toggles checking for software updates.");
        checkForUpdatesNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        provideUsageStatsLabel.setText("Provide usage statistics:");

        provideUsageStatsYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        provideUsageStatsYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        provideUsageStatsButtonGroup.add(provideUsageStatsYesRadio);
        provideUsageStatsYesRadio.setSelected(true);
        provideUsageStatsYesRadio.setText("Yes");
        provideUsageStatsYesRadio.setToolTipText("<html>Toggles sending usage statistics to Mirth.  These statistics <br>do not contain any PHI, and help Mirth determine which connectors <br>or areas of Mirth Connect are most widely used.</html>");
        provideUsageStatsYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        provideUsageStatsNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        provideUsageStatsNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        provideUsageStatsButtonGroup.add(provideUsageStatsNoRadio);
        provideUsageStatsNoRadio.setText("No");
        provideUsageStatsNoRadio.setToolTipText("<html>Toggles sending usage statistics to Mirth.  These statistics <br>do not contain any PHI, and help Mirth determine which connectors <br>or areas of Mirth Connect are most widely used.</html>");
        provideUsageStatsNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        updateUrlField.setToolTipText("The URL to use when checking for software updates.");

        updateUrlLabel.setText("Update URL:");

        provideUsageStatsMoreInfoLabel.setText("<html><font color=blue><u>More Info</u></font></html>");
        provideUsageStatsMoreInfoLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                provideUsageStatsMoreInfoLabelMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout configurationPanelLayout = new javax.swing.GroupLayout(configurationPanel);
        configurationPanel.setLayout(configurationPanelLayout);
        configurationPanelLayout.setHorizontalGroup(
            configurationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configurationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(configurationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkForUpdatesLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(clearGlobalMapLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(provideUsageStatsLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(updateUrlLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configurationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(updateUrlField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(configurationPanelLayout.createSequentialGroup()
                        .addComponent(provideUsageStatsYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(provideUsageStatsNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(provideUsageStatsMoreInfoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(configurationPanelLayout.createSequentialGroup()
                        .addComponent(clearGlobalMapYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(clearGlobalMapNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(configurationPanelLayout.createSequentialGroup()
                        .addComponent(checkForUpdatesYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkForUpdatesNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(31, Short.MAX_VALUE))
        );
        configurationPanelLayout.setVerticalGroup(
            configurationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configurationPanelLayout.createSequentialGroup()
                .addGroup(configurationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clearGlobalMapLabel)
                    .addComponent(clearGlobalMapYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clearGlobalMapNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configurationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkForUpdatesLabel)
                    .addComponent(checkForUpdatesYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkForUpdatesNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configurationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(provideUsageStatsLabel)
                    .addComponent(provideUsageStatsYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(provideUsageStatsNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(provideUsageStatsMoreInfoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configurationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(updateUrlField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(updateUrlLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(configurationPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(emailPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(configurationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(emailPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void provideUsageStatsMoreInfoLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_provideUsageStatsMoreInfoLabelMouseClicked
        BareBonesBrowserLaunch.openURL(UIConstants.PRIVACY_URL);
    }//GEN-LAST:event_provideUsageStatsMoreInfoLabelMouseClicked

    private void requireAuthenticationNoRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_requireAuthenticationNoRadioActionPerformed
        usernameField.setEnabled(false);
        passwordField.setEnabled(false);
        usernameLabel.setEnabled(false);
        passwordLabel.setEnabled(false);
    }//GEN-LAST:event_requireAuthenticationNoRadioActionPerformed

    private void requireAuthenticationYesRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_requireAuthenticationYesRadioActionPerformed
        usernameField.setEnabled(true);
        passwordField.setEnabled(true);
        usernameLabel.setEnabled(true);
        passwordLabel.setEnabled(true);
    }//GEN-LAST:event_requireAuthenticationYesRadioActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup checkForUpdatesButtonGroup;
    private javax.swing.JLabel checkForUpdatesLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton checkForUpdatesNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton checkForUpdatesYesRadio;
    private javax.swing.ButtonGroup clearGlobalMapButtonGroup;
    private javax.swing.JLabel clearGlobalMapLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton clearGlobalMapNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton clearGlobalMapYesRadio;
    private javax.swing.JPanel configurationPanel;
    private com.mirth.connect.client.ui.components.MirthTextField defaultFromAddressField;
    private javax.swing.JLabel defaultFromAddressLabel;
    private javax.swing.JPanel emailPanel;
    private com.mirth.connect.client.ui.components.MirthPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.ButtonGroup provideUsageStatsButtonGroup;
    private javax.swing.JLabel provideUsageStatsLabel;
    private javax.swing.JLabel provideUsageStatsMoreInfoLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton provideUsageStatsNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton provideUsageStatsYesRadio;
    private javax.swing.ButtonGroup requireAuthenticationButtonGroup;
    private javax.swing.JLabel requireAuthenticationLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton requireAuthenticationNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton requireAuthenticationYesRadio;
    private javax.swing.ButtonGroup secureConnectionButtonGroup;
    private javax.swing.JLabel secureConnectionLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton secureConnectionNoneRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton secureConnectionSSLRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton secureConnectionTLSRadio;
    private com.mirth.connect.client.ui.components.MirthTextField smtpHostField;
    private javax.swing.JLabel smtpHostLabel;
    private com.mirth.connect.client.ui.components.MirthTextField smtpPortField;
    private javax.swing.JLabel smtpPortLabel;
    private com.mirth.connect.client.ui.components.MirthTextField updateUrlField;
    private javax.swing.JLabel updateUrlLabel;
    private com.mirth.connect.client.ui.components.MirthTextField usernameField;
    private javax.swing.JLabel usernameLabel;
    // End of variables declaration//GEN-END:variables
}
