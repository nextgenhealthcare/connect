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
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.math.NumberUtils;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.TaskConstants;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.model.ServerConfiguration;
import com.mirth.connect.model.ServerSettings;
import com.mirth.connect.model.UpdateSettings;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.util.ImportConverter;

public class SettingsPanelServer extends AbstractSettingsPanel {
    
    public static final String TAB_NAME = "Server";

    public SettingsPanelServer(String tabName) {
        super(tabName);

        initComponents();

        addTask(TaskConstants.SETTINGS_SERVER_BACKUP, "Backup Config", "Backup your server configuration to an XML file. The backup includes channels, alerts, code templates, server properties, global scripts, and plugin properties.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_disk.png")));
        addTask(TaskConstants.SETTINGS_SERVER_RESTORE, "Restore Config", "Restore your server configuration from a server configuration XML file. This will remove and restore your channels, alerts, code templates, server properties, global scripts, and plugin properties.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_go.png")));

        provideUsageStatsMoreInfoLabel.setToolTipText(UIConstants.PRIVACY_TOOLTIP);
        provideUsageStatsMoreInfoLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        maxQueueSizeField.setDocument(new MirthFieldConstraints(8, false, false, true));
        smtpTimeoutField.setDocument(new MirthFieldConstraints(0, false, false, true));
    }

    public void doRefresh() {
        final String workingId = getFrame().startWorking("Loading " + getTabName() + " settings...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            ServerSettings serverSettings = null;
            UpdateSettings updateSettings = null;

            public Void doInBackground() {
                try {
                    if (getFrame().confirmLeave()) {
                        serverSettings = getFrame().mirthClient.getServerSettings();
                        updateSettings = getFrame().mirthClient.getUpdateSettings();
                    }
                } catch (ClientException e) {
                    getFrame().alertException(getFrame(), e.getStackTrace(), e.getMessage());
                }
                return null;
            }

            @Override
            public void done() {
                // null if it failed to get the server/update settings or if confirmLeave returned false
                if (serverSettings != null && updateSettings != null) {
                    setServerSettings(serverSettings);
                    setUpdateSettings(updateSettings);
                }
                getFrame().stopWorking(workingId);
            }
        };

        worker.execute();
    }

    public void doSave() {
        final ServerSettings serverSettings = getServerSettings();
        final UpdateSettings updateSettings = getUpdateSettings();

        // Integer maxQueueSize will be null if it was invalid
        if (serverSettings.getMaxQueueSize() == null) {
            getFrame().alertWarning(this, "Please enter a valid maximum queue size.");
            return;
        }
        
        // Integer smtpTimeput will be null if it was invalid
        if (serverSettings.getSmtpTimeout() == null) {
            getFrame().alertWarning(this, "Please enter a valid SMTP timeout.");
            return;
        }

        final String workingId = getFrame().startWorking("Saving " + getTabName() + " settings...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            public Void doInBackground() {
                try {
                    getFrame().mirthClient.setServerSettings(serverSettings);
                    getFrame().mirthClient.setUpdateSettings(updateSettings);
                } catch (ClientException e) {
                    getFrame().alertException(getFrame(), e.getStackTrace(), e.getMessage());
                }

                return null;
            }

            @Override
            public void done() {
                setSaveEnabled(false);
                getFrame().stopWorking(workingId);
            }
        };

        worker.execute();
    }

    /** Loads the current server settings into the Settings form */
    public void setServerSettings(ServerSettings serverSettings) {
        if (serverSettings.getSmtpHost() != null) {
            smtpHostField.setText(serverSettings.getSmtpHost());
        } else {
            smtpHostField.setText("");
        }

        if (serverSettings.getSmtpPort()!= null) {
            smtpPortField.setText(serverSettings.getSmtpPort());
        } else {
            smtpPortField.setText("");
        }
        
        if (serverSettings.getSmtpTimeout() != null) {
            smtpTimeoutField.setText(serverSettings.getSmtpTimeout().toString());
        } else {
            smtpTimeoutField.setText("");
        }

        if (serverSettings.getSmtpFrom() != null) {
            defaultFromAddressField.setText(serverSettings.getSmtpFrom());
        } else {
            defaultFromAddressField.setText("");
        }

        String smtpSecure = serverSettings.getSmtpSecure();
        if (smtpSecure != null && smtpSecure.equalsIgnoreCase("tls")) {
            secureConnectionTLSRadio.setSelected(true);
        } else if (smtpSecure != null && smtpSecure.equalsIgnoreCase("ssl")) {
            secureConnectionSSLRadio.setSelected(true);
        } else {
            secureConnectionNoneRadio.setSelected(true);
        }

        if (serverSettings.getSmtpAuth() != null && serverSettings.getSmtpAuth()) {
            requireAuthenticationYesRadio.setSelected(true);
            requireAuthenticationYesRadioActionPerformed(null);
        } else {
            requireAuthenticationNoRadio.setSelected(true);
            requireAuthenticationNoRadioActionPerformed(null);
        }

        if (serverSettings.getClearGlobalMap() != null && !serverSettings.getClearGlobalMap()) {
            clearGlobalMapNoRadio.setSelected(true);
        } else {
            clearGlobalMapYesRadio.setSelected(true);
        }

        if (serverSettings.getMaxQueueSize() != null) {
            maxQueueSizeField.setText(serverSettings.getMaxQueueSize().toString());
        } else {
            maxQueueSizeField.setText("");
        }

        if (serverSettings.getSmtpUsername() != null) {
            usernameField.setText(serverSettings.getSmtpUsername());
        } else {
            usernameField.setText("");
        }

        if (serverSettings.getSmtpPassword() != null) {
            passwordField.setText(serverSettings.getSmtpPassword());
        } else {
            passwordField.setText("");
        }
    }
    
    public void setUpdateSettings(UpdateSettings updateSettings) {
        if (updateSettings.getUpdatesEnabled() != null && !updateSettings.getUpdatesEnabled()) {
            checkForUpdatesNoRadio.setSelected(true);
        } else {
            checkForUpdatesYesRadio.setSelected(true);
        }

        if (updateSettings.getStatsEnabled() != null  && !updateSettings.getStatsEnabled()) {
            provideUsageStatsNoRadio.setSelected(true);
        } else {
            provideUsageStatsYesRadio.setSelected(true);
        }

        if (updateSettings.getUpdateUrl() != null) {
            updateUrlField.setText(updateSettings.getUpdateUrl());
        } else {
            updateUrlField.setText("");
        }
    }

    /** Saves the current settings from the settings form */
    public ServerSettings getServerSettings() {
        ServerSettings serverSettings = new ServerSettings();

        serverSettings.setClearGlobalMap(clearGlobalMapYesRadio.isSelected());
        
        // Set the max queue size Integer to null if it was invalid
        int maxQueueSize = NumberUtils.toInt(maxQueueSizeField.getText(), -1);
        if (maxQueueSize == -1) {
            serverSettings.setMaxQueueSize(null);
        } else {
            serverSettings.setMaxQueueSize(maxQueueSize);
        }
        
        serverSettings.setSmtpHost(smtpHostField.getText());
        serverSettings.setSmtpPort(smtpPortField.getText());
        
        // Set the SMTP timeout Integer to null if it was invalid
        int smtpTimeout = NumberUtils.toInt(smtpTimeoutField.getText(), -1);
        if (smtpTimeout == -1) {
            serverSettings.setSmtpTimeout(null);
        } else {
            serverSettings.setSmtpTimeout(smtpTimeout);
        }
        
        serverSettings.setSmtpFrom(defaultFromAddressField.getText());
        
        if (secureConnectionTLSRadio.isSelected()) {
            serverSettings.setSmtpSecure("tls");
        } else if (secureConnectionSSLRadio.isSelected()) {
            serverSettings.setSmtpSecure("ssl");
        } else {
            serverSettings.setSmtpSecure("none");
        }

        if (requireAuthenticationYesRadio.isSelected()) {
            serverSettings.setSmtpAuth(true);
            serverSettings.setSmtpUsername(usernameField.getText());
            serverSettings.setSmtpPassword(new String(passwordField.getPassword()));
        } else {
            serverSettings.setSmtpAuth(false);
            serverSettings.setSmtpUsername("");
            serverSettings.setSmtpPassword("");
        }

        return serverSettings;
    }
    
    public UpdateSettings getUpdateSettings() {
        UpdateSettings updateSettings = new UpdateSettings();

        updateSettings.setUpdatesEnabled(checkForUpdatesYesRadio.isSelected());
        updateSettings.setStatsEnabled(provideUsageStatsYesRadio.isSelected());
        updateSettings.setUpdateUrl(updateUrlField.getText());

        return updateSettings;
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

        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
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

        String content = getFrame().browseForFileString("XML");

        if (content != null) {
            try {
                ServerConfiguration configuration = ObjectXMLSerializer.getInstance().fromXML(content, ServerConfiguration.class);

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
        smtpTimeoutField = new com.mirth.connect.client.ui.components.MirthTextField();
        smtpTimeoutLabel = new javax.swing.JLabel();
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
        maxQueueSizeField = new com.mirth.connect.client.ui.components.MirthTextField();
        maxQueueSizeLabel = new javax.swing.JLabel();

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

        defaultFromAddressLabel.setText("Default From Address:");

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
        secureConnectionNoneRadio.setToolTipText("Toggles STARTTLS and SSL connections for global SMTP settings.");
        secureConnectionNoneRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        secureConnectionTLSRadio.setBackground(new java.awt.Color(255, 255, 255));
        secureConnectionTLSRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        secureConnectionButtonGroup.add(secureConnectionTLSRadio);
        secureConnectionTLSRadio.setText("STARTTLS");
        secureConnectionTLSRadio.setToolTipText("Toggles STARTTLS and SSL connections for global SMTP settings.");
        secureConnectionTLSRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        secureConnectionSSLRadio.setBackground(new java.awt.Color(255, 255, 255));
        secureConnectionSSLRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        secureConnectionButtonGroup.add(secureConnectionSSLRadio);
        secureConnectionSSLRadio.setText("SSL");
        secureConnectionSSLRadio.setToolTipText("Toggles STARTTLS and SSL connections for global SMTP settings.");
        secureConnectionSSLRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        smtpTimeoutField.setToolTipText("SMTP socket connection timeout in milliseconds used for global SMTP settings.");

        smtpTimeoutLabel.setText("Send Timeout (ms):");

        javax.swing.GroupLayout emailPanelLayout = new javax.swing.GroupLayout(emailPanel);
        emailPanel.setLayout(emailPanelLayout);
        emailPanelLayout.setHorizontalGroup(
            emailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(emailPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(emailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(passwordLabel)
                    .addComponent(usernameLabel)
                    .addComponent(requireAuthenticationLabel)
                    .addComponent(secureConnectionLabel)
                    .addComponent(defaultFromAddressLabel)
                    .addComponent(smtpPortLabel)
                    .addComponent(smtpHostLabel)
                    .addComponent(smtpTimeoutLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(emailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(smtpTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(smtpHostField, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(smtpPortField, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(defaultFromAddressField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                    .addComponent(usernameField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(passwordField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                    .addComponent(smtpTimeoutField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(smtpTimeoutLabel))
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        maxQueueSizeField.setToolTipText("<html>The maximum queue size allowed for each connector, or 0 for infinite.<br>Mirth Connect must be restarted for this setting to take effect.</html>");

        maxQueueSizeLabel.setText("Maximum Queue Size:");

        javax.swing.GroupLayout configurationPanelLayout = new javax.swing.GroupLayout(configurationPanel);
        configurationPanel.setLayout(configurationPanelLayout);
        configurationPanelLayout.setHorizontalGroup(
            configurationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configurationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(configurationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(checkForUpdatesLabel)
                    .addComponent(clearGlobalMapLabel)
                    .addComponent(provideUsageStatsLabel)
                    .addComponent(updateUrlLabel)
                    .addComponent(maxQueueSizeLabel))
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
                        .addComponent(checkForUpdatesNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(maxQueueSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configurationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxQueueSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxQueueSizeLabel))
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
    private com.mirth.connect.client.ui.components.MirthTextField maxQueueSizeField;
    private javax.swing.JLabel maxQueueSizeLabel;
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
    private com.mirth.connect.client.ui.components.MirthTextField smtpTimeoutField;
    private javax.swing.JLabel smtpTimeoutLabel;
    private com.mirth.connect.client.ui.components.MirthTextField updateUrlField;
    private javax.swing.JLabel updateUrlLabel;
    private com.mirth.connect.client.ui.components.MirthTextField usernameField;
    private javax.swing.JLabel usernameLabel;
    // End of variables declaration//GEN-END:variables
}
