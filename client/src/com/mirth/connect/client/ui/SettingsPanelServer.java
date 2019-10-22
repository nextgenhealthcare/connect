/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.mail.internet.InternetAddress;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.TaskConstants;
import com.mirth.connect.client.ui.alert.DefaultAlertPanel;
import com.mirth.connect.client.ui.components.MirthCheckBox;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthPasswordField;
import com.mirth.connect.client.ui.components.MirthRadioButton;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.client.ui.util.DisplayUtil;
import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ServerConfiguration;
import com.mirth.connect.model.ServerSettings;
import com.mirth.connect.model.UpdateSettings;
import com.mirth.connect.model.alert.AlertStatus;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.util.DefaultMetaData;
import com.mirth.connect.util.ConnectionTestResponse;

public class SettingsPanelServer extends AbstractSettingsPanel {

    public static final String TAB_NAME = "Server";

    private List<MetaDataColumn> defaultMetaDataColumns;

    public SettingsPanelServer(String tabName) {
        super(tabName);

        initComponents();
        initLayout();

        addTask(TaskConstants.SETTINGS_SERVER_BACKUP, "Backup Config", "Backup your server configuration to an XML file. The backup includes channels, alerts, code templates, server properties, global scripts, and plugin properties.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_disk.png")));
        addTask(TaskConstants.SETTINGS_SERVER_RESTORE, "Restore Config", "Restore your server configuration from a server configuration XML file. This will remove and restore your channels, alerts, code templates, server properties, global scripts, and plugin properties.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/report_go.png")));
        addTask(TaskConstants.SETTINGS_CLEAR_ALL_STATS, "Clear All Statistics", "Reset the current and lifetime statistics for all channels.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/chart_bar_delete.png")));

        provideUsageStatsMoreInfoLabel.setToolTipText(UIConstants.PRIVACY_TOOLTIP);
        provideUsageStatsMoreInfoLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        queueBufferSizeField.setDocument(new MirthFieldConstraints(8, false, false, true));
        smtpTimeoutField.setDocument(new MirthFieldConstraints(0, false, false, false));

        defaultMetaDataColumns = new ArrayList<MetaDataColumn>();
    }

    public void doRefresh() {
        if (PlatformUI.MIRTH_FRAME.alertRefresh()) {
            return;
        }

        final String workingId = getFrame().startWorking("Loading " + getTabName() + " settings...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            ServerSettings serverSettings = null;
            UpdateSettings updateSettings = null;

            public Void doInBackground() {
                try {
                    serverSettings = getFrame().mirthClient.getServerSettings();
                    updateSettings = getFrame().mirthClient.getUpdateSettings();
                } catch (ClientException e) {
                    getFrame().alertThrowable(getFrame(), e);
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

    public boolean doSave() {
        final ServerSettings serverSettings = getServerSettings();
        final UpdateSettings updateSettings = getUpdateSettings();

        // Integer queueBufferSize will be null if it was invalid
        queueBufferSizeField.setBackground(null);
        if (serverSettings.getQueueBufferSize() == null) {
            queueBufferSizeField.setBackground(UIConstants.INVALID_COLOR);
            getFrame().alertWarning(this, "Please enter a valid queue buffer size.");
            return false;
        }

        try {
            String emailAddress = serverSettings.getSmtpFrom();
            if (StringUtils.isNotBlank(emailAddress)) {
                new InternetAddress(emailAddress).validate();
            }
        } catch (Exception e) {
            PlatformUI.MIRTH_FRAME.alertWarning(PlatformUI.MIRTH_FRAME, "The Default From Address is invalid: " + e.getMessage());
            return false;
        }

        final String workingId = getFrame().startWorking("Saving " + getTabName() + " settings...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            private boolean usingServerDefaultColor = true;

            public Void doInBackground() {
                try {
                    getFrame().mirthClient.setServerSettings(serverSettings);

                    String environmentName = environmentNameField.getText();
                    String serverName = serverNameField.getText();
                    StringBuilder titleText = new StringBuilder();
                    StringBuilder statusBarText = new StringBuilder();
                    statusBarText.append("Connected to: ");

                    if (!StringUtils.isBlank(environmentName)) {
                        titleText.append(environmentName + " - ");
                        statusBarText.append(environmentName);

                        if (!StringUtils.isBlank(serverName)) {
                            statusBarText.append(" - ");
                        } else {
                            statusBarText.append(" | ");
                        }

                        PlatformUI.ENVIRONMENT_NAME = environmentName;
                    }

                    if (!StringUtils.isBlank(serverName)) {
                        titleText.append(serverName);
                        statusBarText.append(serverName + " | ");
                        PlatformUI.SERVER_NAME = serverName;
                    } else {
                        titleText.append(PlatformUI.SERVER_URL);
                    }
                    titleText.append(" - " + UIConstants.TITLE_TEXT);
                    statusBarText.append(PlatformUI.SERVER_URL);
                    titleText.append(" - (" + PlatformUI.SERVER_VERSION + ")");
                    getFrame().setTitle(titleText.toString());
                    getFrame().statusBar.setServerText(statusBarText.toString());

                    getFrame().mirthClient.setUpdateSettings(updateSettings);
                } catch (Exception e) {
                    getFrame().alertThrowable(getFrame(), e);
                }

                try {
                    Color defaultBackgroundColor = serverSettings.getDefaultAdministratorBackgroundColor();
                    if (defaultBackgroundColor != null) {
                        PlatformUI.DEFAULT_BACKGROUND_COLOR = defaultBackgroundColor;
                    }

                    String backgroundColorStr = getFrame().mirthClient.getUserPreference(getFrame().getCurrentUser(getFrame()).getId(), UIConstants.USER_PREF_KEY_BACKGROUND_COLOR);
                    if (StringUtils.isNotBlank(backgroundColorStr)) {
                        Color backgroundColor = ObjectXMLSerializer.getInstance().deserialize(backgroundColorStr, Color.class);
                        if (backgroundColor != null) {
                            usingServerDefaultColor = false;
                        }
                    }
                } catch (Exception e) {
                    getFrame().alertThrowable(getFrame(), e);
                }

                return null;
            }

            @Override
            public void done() {
                if (usingServerDefaultColor) {
                    getFrame().setupBackgroundPainters(PlatformUI.DEFAULT_BACKGROUND_COLOR);
                }
                setSaveEnabled(false);
                getFrame().stopWorking(workingId);
            }
        };

        worker.execute();

        return true;
    }

    /** Loads the current server settings into the Settings form */
    public void setServerSettings(ServerSettings serverSettings) {
        if (serverSettings.getEnvironmentName() != null) {
            environmentNameField.setText(serverSettings.getEnvironmentName());
        } else {
            environmentNameField.setText("");
        }

        if (serverSettings.getServerName() != null) {
            serverNameField.setText(serverSettings.getServerName());
        } else {
            serverNameField.setText("");
        }

        if (serverSettings.getDefaultAdministratorBackgroundColor() != null) {
            defaultAdministratorColorButton.setBackground(serverSettings.getDefaultAdministratorBackgroundColor());
        } else {
            defaultAdministratorColorButton.setBackground(ServerSettings.DEFAULT_COLOR);
        }

        if (serverSettings.getSmtpHost() != null) {
            smtpHostField.setText(serverSettings.getSmtpHost());
        } else {
            smtpHostField.setText("");
        }

        if (serverSettings.getSmtpPort() != null) {
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

        if (serverSettings.getQueueBufferSize() != null) {
            queueBufferSizeField.setText(serverSettings.getQueueBufferSize().toString());
        } else {
            queueBufferSizeField.setText("");
        }

        // TODO: Change this to use a more complex custom metadata table rather than checkboxes
        List<MetaDataColumn> defaultMetaDataColumns = serverSettings.getDefaultMetaDataColumns();
        if (defaultMetaDataColumns != null) {
            this.defaultMetaDataColumns = new ArrayList<MetaDataColumn>(defaultMetaDataColumns);
        } else {
            this.defaultMetaDataColumns = new ArrayList<MetaDataColumn>(DefaultMetaData.DEFAULT_COLUMNS);
        }
        defaultMetaDataSourceCheckBox.setSelected(this.defaultMetaDataColumns.contains(DefaultMetaData.SOURCE_COLUMN));
        defaultMetaDataTypeCheckBox.setSelected(this.defaultMetaDataColumns.contains(DefaultMetaData.TYPE_COLUMN));
        defaultMetaDataVersionCheckBox.setSelected(this.defaultMetaDataColumns.contains(DefaultMetaData.VERSION_COLUMN));

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
        resetInvalidSettings();
    }

    public void setUpdateSettings(UpdateSettings updateSettings) {
        if (updateSettings.getStatsEnabled() != null && !updateSettings.getStatsEnabled()) {
            provideUsageStatsNoRadio.setSelected(true);
        } else {
            provideUsageStatsYesRadio.setSelected(true);
        }
    }

    /** Saves the current settings from the settings form */
    public ServerSettings getServerSettings() {
        ServerSettings serverSettings = new ServerSettings();

        serverSettings.setEnvironmentName(environmentNameField.getText());

        serverSettings.setServerName(serverNameField.getText());

        serverSettings.setDefaultAdministratorBackgroundColor(defaultAdministratorColorButton.getBackground());

        serverSettings.setClearGlobalMap(clearGlobalMapYesRadio.isSelected());

        // Set the queue buffer size Integer to null if it was invalid
        int queueBufferSize = NumberUtils.toInt(queueBufferSizeField.getText(), 0);
        if (queueBufferSize == 0) {
            serverSettings.setQueueBufferSize(null);
        } else {
            serverSettings.setQueueBufferSize(queueBufferSize);
        }

        // TODO: Change this to use a more complex custom metadata table rather than checkboxes
        // Until this is changed to a table, always add source/type/version in order
        List<MetaDataColumn> defaultMetaDataColumns = new ArrayList<MetaDataColumn>();
        if (defaultMetaDataSourceCheckBox.isSelected()) {
            defaultMetaDataColumns.add(DefaultMetaData.SOURCE_COLUMN);
        } else {
            this.defaultMetaDataColumns.remove(DefaultMetaData.SOURCE_COLUMN);
        }

        if (defaultMetaDataTypeCheckBox.isSelected()) {
            defaultMetaDataColumns.add(DefaultMetaData.TYPE_COLUMN);
        } else {
            this.defaultMetaDataColumns.remove(DefaultMetaData.TYPE_COLUMN);
        }

        if (defaultMetaDataVersionCheckBox.isSelected()) {
            defaultMetaDataColumns.add(DefaultMetaData.VERSION_COLUMN);
        } else {
            this.defaultMetaDataColumns.remove(DefaultMetaData.VERSION_COLUMN);
        }

        for (MetaDataColumn column : this.defaultMetaDataColumns) {
            if (!defaultMetaDataColumns.contains(column)) {
                defaultMetaDataColumns.add(column);
            }
        }
        serverSettings.setDefaultMetaDataColumns(this.defaultMetaDataColumns = defaultMetaDataColumns);

        serverSettings.setSmtpHost(smtpHostField.getText());
        serverSettings.setSmtpPort(smtpPortField.getText());
        serverSettings.setSmtpTimeout(smtpTimeoutField.getText());
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

        updateSettings.setStatsEnabled(provideUsageStatsYesRadio.isSelected());

        return updateSettings;
    }

    public void doBackup() {
        if (isSaveEnabled()) {
            int option = JOptionPane.showConfirmDialog(this, "Would you like to save the settings first?");

            if (option == JOptionPane.YES_OPTION) {
                if (!doSave()) {
                    return;
                }
            } else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
                return;
            }
        }

        final String backupDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        final File exportFile = getFrame().createFileForExport(backupDate.substring(0, 10) + " Mirth Backup.xml", "XML");

        if (exportFile != null) {
            final String workingId = getFrame().startWorking("Exporting server config...");

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                public Void doInBackground() {
                    ServerConfiguration configuration = null;

                    try {
                        configuration = getFrame().mirthClient.getServerConfiguration();
                    } catch (ClientException e) {
                        getFrame().alertThrowable(SettingsPanelServer.this, e);
                        return null;
                    }

                    // Update resource names
                    for (Channel channel : configuration.getChannels()) {
                        getFrame().updateResourceNames(channel, configuration.getResourceProperties().getList());
                    }

                    configuration.setDate(backupDate);
                    String backupXML = ObjectXMLSerializer.getInstance().serialize(configuration);

                    getFrame().exportFile(backupXML, exportFile, "Server Configuration");
                    return null;
                }

                public void done() {
                    getFrame().stopWorking(workingId);
                }
            };

            worker.execute();
        }
    }

    public void doRestore() {
        if (getFrame().isSaveEnabled()) {
            if (!getFrame().alertOkCancel(this, "Your new settings will first be saved.  Continue?")) {
                return;
            }
            if (!doSave()) {
                return;
            }
        }

        String content = getFrame().browseForFileString("XML");

        if (content != null) {
            try {
                if (!getFrame().promptObjectMigration(content, "server configuration")) {
                    return;
                }

                final ServerConfiguration configuration = ObjectXMLSerializer.getInstance().deserialize(content, ServerConfiguration.class);

                final JCheckBox deployChannelsCheckBox = new JCheckBox("Deploy all channels after import");
                deployChannelsCheckBox.setSelected(true);
                final JCheckBox overwriteConfigMap = new JCheckBox("Overwrite Configuration Map");
                overwriteConfigMap.setSelected(false);
                String warningMessage = "Import configuration from " + configuration.getDate() + "?\nWARNING: This will overwrite all current channels,\nalerts, server properties, and plugin properties.\n";
                Object[] params = { warningMessage, new JLabel(" "), deployChannelsCheckBox,
                        overwriteConfigMap };
                int option = JOptionPane.showConfirmDialog(this, params, "Select an Option", JOptionPane.YES_NO_OPTION);

                if (option == JOptionPane.YES_OPTION) {
                    final Set<String> alertIds = new HashSet<String>();
                    for (AlertStatus alertStatus : PlatformUI.MIRTH_FRAME.mirthClient.getAlertStatusList()) {
                        alertIds.add(alertStatus.getId());
                    }

                    final String workingId = getFrame().startWorking("Restoring server config...");

                    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                        private boolean updateAlerts = false;

                        public Void doInBackground() {
                            try {
                                getFrame().mirthClient.setServerConfiguration(configuration, deployChannelsCheckBox.isSelected(), overwriteConfigMap.isSelected());
                                getFrame().channelPanel.clearChannelCache();
                                doRefresh();
                                getFrame().alertInformation(SettingsPanelServer.this, "Your configuration was successfully restored.");
                                updateAlerts = true;
                            } catch (ClientException e) {
                                getFrame().alertThrowable(SettingsPanelServer.this, e);
                            }
                            return null;
                        }

                        public void done() {
                            if (getFrame().alertPanel == null) {
                                getFrame().alertPanel = new DefaultAlertPanel();
                            }

                            if (updateAlerts) {
                                getFrame().alertPanel.updateAlertDetails(alertIds);
                            }
                            getFrame().stopWorking(workingId);
                        }
                    };

                    worker.execute();
                }
            } catch (Exception e) {
                getFrame().alertError(this, "Invalid server configuration file.");
            }
        }
    }

    public void doClearAllStats() {
        String result = DisplayUtil.showInputDialog(this, "<html>This will reset all channel statistics (including lifetime statistics) for<br>all channels (including undeployed channels).<br><font size='1'><br></font>Type CLEAR and click the OK button to continue.</html>", "Clear All Statistics", JOptionPane.WARNING_MESSAGE);

        if (result != null) {
            if (!result.equals("CLEAR")) {
                getFrame().alertWarning(SettingsPanelServer.this, "You must type CLEAR to clear all statistics.");
                return;
            }

            final String workingId = getFrame().startWorking("Clearing all statistics...");

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                private Exception exception = null;

                public Void doInBackground() {
                    try {
                        getFrame().mirthClient.clearAllStatistics();
                    } catch (ClientException e) {
                        exception = e;
                        getFrame().alertThrowable(SettingsPanelServer.this, e);
                    }
                    return null;
                }

                public void done() {
                    getFrame().stopWorking(workingId);

                    if (exception == null) {
                        getFrame().alertInformation(SettingsPanelServer.this, "All current and lifetime statistics have been cleared for all channels.");
                    }
                }
            };

            worker.execute();
        }
    }

    private void resetInvalidSettings() {
        queueBufferSizeField.setBackground(null);
        smtpHostField.setBackground(null);
        smtpPortField.setBackground(null);
        smtpTimeoutField.setBackground(null);
        defaultFromAddressField.setBackground(null);
        usernameField.setBackground(null);
        passwordField.setBackground(null);
    }

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);

        generalPanel = new JPanel();
        generalPanel.setBackground(getBackground());
        generalPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(204, 204, 204)), "General", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 11)));

        environmentNameLabel = new JLabel("Environment name:");
        environmentNameField = new MirthTextField();
        environmentNameField.setToolTipText("<html>The name of this Mirth Connect environment. There is one environment name per Mirth Connect database.</html>");

        serverNameLabel = new JLabel("Server name:");
        serverNameField = new MirthTextField();
        serverNameField.setToolTipText("<html>The server name which will appear in the Administrator title, taskbar/dock<br>and desktop shortcut. This setting applies for all users on this server.</html>");

        defaultAdministratorColorLabel = new JLabel("Default Background Color:");

        defaultAdministratorColorButton = new JButton();
        defaultAdministratorColorButton.setBackground(ServerSettings.DEFAULT_COLOR);
        defaultAdministratorColorButton.setToolTipText("<html>The default Administrator GUI background color this server should use.<br/>Users can override this with their own custom background color.</html>");
        defaultAdministratorColorButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        defaultAdministratorColorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                Color color = JColorChooser.showDialog(PlatformUI.MIRTH_FRAME, "Edit Background Color", defaultAdministratorColorButton.getBackground());
                if (color != null) {
                    defaultAdministratorColorButton.setBackground(color);
                    getFrame().setSaveEnabled(true);
                }
            }
        });

        provideUsageStatsLabel = new JLabel("Provide usage statistics:");
        provideUsageStatsButtonGroup = new ButtonGroup();

        provideUsageStatsYesRadio = new MirthRadioButton("Yes");
        provideUsageStatsYesRadio.setBackground(getBackground());
        provideUsageStatsYesRadio.setToolTipText("<html>Toggles sending usage statistics to NextGen Healthcare.  These statistics <br>do not contain any PHI or channel/script implementations,<br> and help NextGen Healthcare determine which connectors or areas of<br>Mirth Connect are most widely used.</html>");
        provideUsageStatsButtonGroup.add(provideUsageStatsYesRadio);

        provideUsageStatsNoRadio = new MirthRadioButton("No");
        provideUsageStatsNoRadio.setBackground(getBackground());
        provideUsageStatsNoRadio.setToolTipText("<html>Toggles sending usage statistics to NextGen Healthcare.  These statistics <br>do not contain any PHI or channel/script implementations,<br> and help NextGen Healthcare determine which connectors or areas of<br>Mirth Connect are most widely used.</html>");
        provideUsageStatsButtonGroup.add(provideUsageStatsNoRadio);

        provideUsageStatsMoreInfoLabel = new JLabel("<html><font color=blue><u>More Info</u></font></html>");
        provideUsageStatsMoreInfoLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                provideUsageStatsMoreInfoLabelMouseClicked(evt);
            }
        });

        channelPanel = new JPanel();
        channelPanel.setBackground(getBackground());
        channelPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(204, 204, 204)), "Channel", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 11)));

        clearGlobalMapLabel = new JLabel("Clear global map on redeploy:");
        clearGlobalMapButtonGroup = new ButtonGroup();

        clearGlobalMapYesRadio = new MirthRadioButton("Yes");
        clearGlobalMapYesRadio.setBackground(getBackground());
        clearGlobalMapYesRadio.setSelected(true);
        clearGlobalMapYesRadio.setToolTipText("Toggles clearing the global map when redeploying all channels.");
        clearGlobalMapButtonGroup.add(clearGlobalMapYesRadio);

        clearGlobalMapNoRadio = new MirthRadioButton("No");
        clearGlobalMapNoRadio.setBackground(getBackground());
        clearGlobalMapNoRadio.setToolTipText("Toggles clearing the global map when redeploying all channels.");
        clearGlobalMapButtonGroup.add(clearGlobalMapNoRadio);

        queueBufferSizeLabel = new JLabel("Default Queue Buffer Size:");
        queueBufferSizeField = new MirthTextField();
        queueBufferSizeField.setToolTipText("The default source/destination queue buffer size to use for new channels.");

        defaultMetaDataLabel = new JLabel("Default Metadata Columns:");

        defaultMetaDataSourceCheckBox = new MirthCheckBox("Source");
        defaultMetaDataSourceCheckBox.setBackground(getBackground());
        defaultMetaDataSourceCheckBox.setToolTipText("<html>If checked, the Source metadata column will be added by<br/>default when a user creates a new channel. The user can<br/>choose to remove the column on the channel's Summary tab.</html>");

        defaultMetaDataTypeCheckBox = new MirthCheckBox("Type");
        defaultMetaDataTypeCheckBox.setBackground(getBackground());
        defaultMetaDataTypeCheckBox.setToolTipText("<html>If checked, the Type metadata column will be added by<br/>default when a user creates a new channel. The user can<br/>choose to remove the column on the channel's Summary tab.</html>");

        defaultMetaDataVersionCheckBox = new MirthCheckBox("Version");
        defaultMetaDataVersionCheckBox.setBackground(getBackground());
        defaultMetaDataVersionCheckBox.setToolTipText("<html>If checked, the Version metadata column will be added by<br/>default when a user creates a new channel. The user can<br/>choose to remove the column on the channel's Summary tab.</html>");

        emailPanel = new JPanel();
        emailPanel.setBackground(getBackground());
        emailPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(204, 204, 204)), "Email", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 11)));

        smtpHostLabel = new JLabel("SMTP Host:");
        smtpHostField = new MirthTextField();
        smtpHostField.setToolTipText("SMTP host used for global SMTP settings.");

        testEmailButton = new JButton("Send Test Email");
        testEmailButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                testEmailButtonActionPerformed(evt);
            }
        });

        smtpPortLabel = new JLabel("SMTP Port:");
        smtpPortField = new MirthTextField();
        smtpPortField.setToolTipText("SMTP port used for global SMTP settings.");

        smtpTimeoutLabel = new JLabel("Send Timeout (ms):");
        smtpTimeoutField = new MirthTextField();
        smtpTimeoutField.setToolTipText("SMTP socket connection timeout in milliseconds used for global SMTP settings.");

        defaultFromAddressLabel = new JLabel("Default From Address:");
        defaultFromAddressField = new MirthTextField();
        defaultFromAddressField.setToolTipText("Default \"from\" email address used for global SMTP settings.");

        secureConnectionLabel = new JLabel("Secure Connection:");
        secureConnectionButtonGroup = new ButtonGroup();

        secureConnectionNoneRadio = new MirthRadioButton("None");
        secureConnectionNoneRadio.setBackground(getBackground());
        secureConnectionNoneRadio.setSelected(true);
        secureConnectionNoneRadio.setToolTipText("Toggles STARTTLS and SSL connections for global SMTP settings.");
        secureConnectionButtonGroup.add(secureConnectionNoneRadio);

        secureConnectionTLSRadio = new MirthRadioButton("STARTTLS");
        secureConnectionTLSRadio.setBackground(getBackground());
        secureConnectionTLSRadio.setToolTipText("Toggles STARTTLS and SSL connections for global SMTP settings.");
        secureConnectionButtonGroup.add(secureConnectionTLSRadio);

        secureConnectionSSLRadio = new MirthRadioButton("SSL");
        secureConnectionSSLRadio.setBackground(getBackground());
        secureConnectionSSLRadio.setToolTipText("Toggles STARTTLS and SSL connections for global SMTP settings.");
        secureConnectionButtonGroup.add(secureConnectionSSLRadio);

        requireAuthenticationLabel = new JLabel("Require Authentication:");
        requireAuthenticationButtonGroup = new ButtonGroup();

        requireAuthenticationYesRadio = new MirthRadioButton("Yes");
        requireAuthenticationYesRadio.setBackground(getBackground());
        requireAuthenticationYesRadio.setSelected(true);
        requireAuthenticationYesRadio.setToolTipText("Toggles authentication for global SMTP settings.");
        requireAuthenticationYesRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                requireAuthenticationYesRadioActionPerformed(evt);
            }
        });
        requireAuthenticationButtonGroup.add(requireAuthenticationYesRadio);

        requireAuthenticationNoRadio = new MirthRadioButton("No");
        requireAuthenticationNoRadio.setBackground(getBackground());
        requireAuthenticationNoRadio.setToolTipText("Toggles authentication for global SMTP settings.");
        requireAuthenticationNoRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                requireAuthenticationNoRadioActionPerformed(evt);
            }
        });
        requireAuthenticationButtonGroup.add(requireAuthenticationNoRadio);

        usernameLabel = new JLabel("Username:");
        usernameField = new MirthTextField();
        usernameField.setToolTipText("Username for global SMTP settings.");

        passwordLabel = new JLabel("Password:");
        passwordField = new MirthPasswordField();
        passwordField.setToolTipText("Password for global SMTP settings.");
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 12, novisualpadding, hidemode 3, fill, gap 6", "", "[][][][grow]"));

        generalPanel.setLayout(new MigLayout("insets 12, novisualpadding, hidemode 3, fill, gap 6", "[]12[][grow]", ""));
        generalPanel.add(environmentNameLabel, "right");
        generalPanel.add(environmentNameField, "w 168!");
        generalPanel.add(serverNameLabel, "newline, right");
        generalPanel.add(serverNameField, "w 168!");
        generalPanel.add(defaultAdministratorColorLabel, "newline, right");
        generalPanel.add(defaultAdministratorColorButton, "h 22!, w 22!");
        generalPanel.add(provideUsageStatsLabel, "newline, right");
        generalPanel.add(provideUsageStatsYesRadio, "split 3");
        generalPanel.add(provideUsageStatsNoRadio);
        generalPanel.add(provideUsageStatsMoreInfoLabel, "gapbefore 12");
        add(generalPanel, "growx");

        channelPanel.setLayout(new MigLayout("insets 12, novisualpadding, hidemode 3, fill, gap 6", "[]12[][grow]", ""));
        channelPanel.add(clearGlobalMapLabel, "right");
        channelPanel.add(clearGlobalMapYesRadio, "split 2");
        channelPanel.add(clearGlobalMapNoRadio);
        channelPanel.add(queueBufferSizeLabel, "newline, right");
        channelPanel.add(queueBufferSizeField, "w 50!");
        channelPanel.add(defaultMetaDataLabel, "newline, right");
        channelPanel.add(defaultMetaDataSourceCheckBox, "split 3");
        channelPanel.add(defaultMetaDataTypeCheckBox);
        channelPanel.add(defaultMetaDataVersionCheckBox);
        add(channelPanel, "newline, growx");

        emailPanel.setLayout(new MigLayout("insets 12, novisualpadding, hidemode 3, fill, gap 6", "[]12[][grow]", "[][][][][][][][][grow]"));
        emailPanel.add(smtpHostLabel, "right");
        emailPanel.add(smtpHostField, "w 117!, split 2");
        emailPanel.add(testEmailButton);
        emailPanel.add(smtpPortLabel, "newline, right");
        emailPanel.add(smtpPortField, "w 50!");
        emailPanel.add(smtpTimeoutLabel, "newline, right");
        emailPanel.add(smtpTimeoutField, "w 75!");
        emailPanel.add(defaultFromAddressLabel, "newline, right");
        emailPanel.add(defaultFromAddressField, "w 117!");
        emailPanel.add(secureConnectionLabel, "newline, right");
        emailPanel.add(secureConnectionNoneRadio, "split 3");
        emailPanel.add(secureConnectionTLSRadio);
        emailPanel.add(secureConnectionSSLRadio);
        emailPanel.add(requireAuthenticationLabel, "newline, right");
        emailPanel.add(requireAuthenticationYesRadio, "split 2");
        emailPanel.add(requireAuthenticationNoRadio);
        emailPanel.add(usernameLabel, "newline, right");
        emailPanel.add(usernameField, "w 117!");
        emailPanel.add(passwordLabel, "newline, right");
        emailPanel.add(passwordField, "w 117!");
        add(emailPanel, "newline, growx");
    }

    private void provideUsageStatsMoreInfoLabelMouseClicked(MouseEvent evt) {
        BareBonesBrowserLaunch.openURL(UIConstants.PRIVACY_URL);
    }

    private void requireAuthenticationNoRadioActionPerformed(ActionEvent evt) {
        usernameField.setEnabled(false);
        passwordField.setEnabled(false);
        usernameLabel.setEnabled(false);
        passwordLabel.setEnabled(false);
    }

    private void requireAuthenticationYesRadioActionPerformed(ActionEvent evt) {
        usernameField.setEnabled(true);
        passwordField.setEnabled(true);
        usernameLabel.setEnabled(true);
        passwordLabel.setEnabled(true);
    }

    private void testEmailButtonActionPerformed(ActionEvent evt) {
        resetInvalidSettings();
        ServerSettings serverSettings = getServerSettings();
        StringBuilder invalidFields = new StringBuilder();

        if (StringUtils.isBlank(serverSettings.getSmtpHost())) {
            smtpHostField.setBackground(UIConstants.INVALID_COLOR);
            invalidFields.append("\"SMTP Host\" is required\n");
        }

        if (StringUtils.isBlank(serverSettings.getSmtpPort())) {
            smtpPortField.setBackground(UIConstants.INVALID_COLOR);
            invalidFields.append("\"SMTP Port\" is required\n");
        }

        if (StringUtils.isBlank(serverSettings.getSmtpTimeout())) {
            smtpTimeoutField.setBackground(UIConstants.INVALID_COLOR);
            invalidFields.append("\"Send Timeout\" is required\n");
        }

        if (StringUtils.isBlank(serverSettings.getSmtpFrom())) {
            defaultFromAddressField.setBackground(UIConstants.INVALID_COLOR);
            invalidFields.append("\"Default From Address\" is required\n");
        }

        if (serverSettings.getSmtpAuth()) {
            if (StringUtils.isBlank(serverSettings.getSmtpUsername())) {
                usernameField.setBackground(UIConstants.INVALID_COLOR);
                invalidFields.append("\"Username\" is required\n");
            }

            if (StringUtils.isBlank(serverSettings.getSmtpPassword())) {
                passwordField.setBackground(UIConstants.INVALID_COLOR);
                invalidFields.append("\"Password\" is required\n");
            }
        }

        String errors = invalidFields.toString();
        if (StringUtils.isNotBlank(errors)) {
            PlatformUI.MIRTH_FRAME.alertCustomError(PlatformUI.MIRTH_FRAME, errors, "Please fix the following errors before sending a test email:");
            return;
        }

        String sendToEmail = (String) DisplayUtil.showInputDialog(PlatformUI.MIRTH_FRAME, "Send test email to:", "Send Test Email", JOptionPane.INFORMATION_MESSAGE, null, null, serverSettings.getSmtpFrom());

        if (StringUtils.isNotBlank(sendToEmail)) {
            try {
                new InternetAddress(sendToEmail).validate();
            } catch (Exception error) {
                PlatformUI.MIRTH_FRAME.alertWarning(PlatformUI.MIRTH_FRAME, "The Send To Address is invalid: " + error.getMessage());
                return;
            }

            final Properties properties = new Properties();
            properties.put("port", serverSettings.getSmtpPort());
            properties.put("encryption", serverSettings.getSmtpSecure());
            properties.put("host", serverSettings.getSmtpHost());
            properties.put("timeout", serverSettings.getSmtpTimeout());
            properties.put("authentication", String.valueOf(serverSettings.getSmtpAuth()));
            properties.put("username", serverSettings.getSmtpUsername());
            properties.put("password", serverSettings.getSmtpPassword());
            properties.put("toAddress", sendToEmail);
            properties.put("fromAddress", serverSettings.getSmtpFrom());

            final String workingId = PlatformUI.MIRTH_FRAME.startWorking("Sending test email...");

            SwingWorker worker = new SwingWorker<Void, Void>() {

                public Void doInBackground() {

                    try {
                        ConnectionTestResponse response = (ConnectionTestResponse) PlatformUI.MIRTH_FRAME.mirthClient.sendTestEmail(properties);

                        if (response == null) {
                            PlatformUI.MIRTH_FRAME.alertError(PlatformUI.MIRTH_FRAME, "Failed to send email.");
                        } else if (response.getType().equals(ConnectionTestResponse.Type.SUCCESS)) {
                            PlatformUI.MIRTH_FRAME.alertInformation(PlatformUI.MIRTH_FRAME, response.getMessage());
                        } else {
                            PlatformUI.MIRTH_FRAME.alertWarning(PlatformUI.MIRTH_FRAME, response.getMessage());
                        }

                        return null;
                    } catch (Exception e) {
                        PlatformUI.MIRTH_FRAME.alertThrowable(PlatformUI.MIRTH_FRAME, e);
                        return null;
                    }
                }

                public void done() {
                    PlatformUI.MIRTH_FRAME.stopWorking(workingId);
                }
            };

            worker.execute();
        }
    }

    private JPanel generalPanel;
    private JLabel environmentNameLabel;
    private MirthTextField environmentNameField;
    private JLabel serverNameLabel;
    private MirthTextField serverNameField;
    private JLabel defaultAdministratorColorLabel;
    private JButton defaultAdministratorColorButton;

    private JLabel provideUsageStatsLabel;
    private ButtonGroup provideUsageStatsButtonGroup;
    private MirthRadioButton provideUsageStatsYesRadio;
    private MirthRadioButton provideUsageStatsNoRadio;
    private JLabel provideUsageStatsMoreInfoLabel;

    private JPanel channelPanel;
    private JLabel clearGlobalMapLabel;
    private ButtonGroup clearGlobalMapButtonGroup;
    private MirthRadioButton clearGlobalMapYesRadio;
    private MirthRadioButton clearGlobalMapNoRadio;
    private JLabel queueBufferSizeLabel;
    private MirthTextField queueBufferSizeField;
    private JLabel defaultMetaDataLabel;
    private MirthCheckBox defaultMetaDataSourceCheckBox;
    private MirthCheckBox defaultMetaDataTypeCheckBox;
    private MirthCheckBox defaultMetaDataVersionCheckBox;

    private JPanel emailPanel;
    private JLabel smtpHostLabel;
    private MirthTextField smtpHostField;
    private JButton testEmailButton;
    private JLabel smtpPortLabel;
    private MirthTextField smtpPortField;
    private JLabel smtpTimeoutLabel;
    private MirthTextField smtpTimeoutField;
    private JLabel defaultFromAddressLabel;
    private MirthTextField defaultFromAddressField;
    private JLabel secureConnectionLabel;
    private ButtonGroup secureConnectionButtonGroup;
    private MirthRadioButton secureConnectionNoneRadio;
    private MirthRadioButton secureConnectionSSLRadio;
    private MirthRadioButton secureConnectionTLSRadio;
    private JLabel requireAuthenticationLabel;
    private ButtonGroup requireAuthenticationButtonGroup;
    private MirthRadioButton requireAuthenticationYesRadio;
    private MirthRadioButton requireAuthenticationNoRadio;
    private JLabel usernameLabel;
    private MirthTextField usernameField;
    private JLabel passwordLabel;
    private MirthPasswordField passwordField;
}