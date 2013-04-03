/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.util.prefs.Preferences;

import com.mirth.connect.client.ui.components.MirthFieldConstraints;

public class SettingsPanelAdministrator extends AbstractSettingsPanel {

    public static final String TAB_NAME = "Administrator";
    private static Preferences userPreferences;

    public SettingsPanelAdministrator(String tabName) {
        super(tabName);

        initComponents();
    }

    public void doRefresh() {
        if (getFrame().confirmLeave()) {
            dashboardRefreshIntervalField.setDocument(new MirthFieldConstraints(3, false, false, true));
            messageBrowserPageSizeField.setDocument(new MirthFieldConstraints(3, false, false, true));
            eventBrowserPageSizeField.setDocument(new MirthFieldConstraints(3, false, false, true));
            userPreferences = Preferences.userNodeForPackage(Mirth.class);
            int interval = userPreferences.getInt("intervalTime", 10);
            dashboardRefreshIntervalField.setText(interval + "");

            int messageBrowserPageSize = userPreferences.getInt("messageBrowserPageSize", 20);
            messageBrowserPageSizeField.setText(messageBrowserPageSize + "");

            int eventBrowserPageSize = userPreferences.getInt("eventBrowserPageSize", 100);
            eventBrowserPageSizeField.setText(eventBrowserPageSize + "");

            if (userPreferences.getBoolean("messageBrowserFormatXml", true)) {
                formatXmlYesRadio.setSelected(true);
            } else {
                formatXmlNoRadio.setSelected(true);
            }

            messageBrowserConnectorCheckBox.setSelected(userPreferences.getBoolean("messageBrowserVisibleColumnConnector", true));
            messageBrowserStatusCheckBox.setSelected(userPreferences.getBoolean("messageBrowserVisibleColumnStatus", true));
            messageBrowserReceivedDateCheckBox.setSelected(userPreferences.getBoolean("messageBrowserVisibleColumnReceived Date", true));
            messageBrowserOrigReceivedDateCheckBox.setSelected(userPreferences.getBoolean("messageBrowserVisibleColumnOrig. Received Date", true));
            messageBrowserServerIdCheckBox.setSelected(userPreferences.getBoolean("messageBrowserVisibleColumnServer Id", false));
            messageBrowserSendAttemptsCheckBox.setSelected(userPreferences.getBoolean("messageBrowserVisibleColumnSend Attempts", false));
            messageBrowserSendDateCheckBox.setSelected(userPreferences.getBoolean("messageBrowserVisibleColumnSend Date", false));
            messageBrowserResponseDateCheckBox.setSelected(userPreferences.getBoolean("messageBrowserVisibleColumnResponse Date", false));
            messageBrowserImportIdCheckBox.setSelected(userPreferences.getBoolean("messageBrowserVisibleColumnImport Id", false));
            messageBrowserErrorsCheckBox.setSelected(userPreferences.getBoolean("messageBrowserVisibleColumnErrors", false));
        }
    }

    public void doSave() {
        if (dashboardRefreshIntervalField.getText().length() == 0) {
            getFrame().alertWarning(this, "Please enter a valid interval time.");
            return;
        }
        if (messageBrowserPageSizeField.getText().length() == 0) {
            getFrame().alertWarning(this, "Please enter a valid message browser page size.");
            return;
        }
        if (eventBrowserPageSizeField.getText().length() == 0) {
            getFrame().alertWarning(this, "Please enter a valid event browser page size.");
            return;
        }

        int interval = Integer.parseInt(dashboardRefreshIntervalField.getText());
        int messageBrowserPageSize = Integer.parseInt(messageBrowserPageSizeField.getText());
        int eventBrowserPageSize = Integer.parseInt(eventBrowserPageSizeField.getText());

        if (interval <= 0) {
            getFrame().alertWarning(this, "Please enter an interval time that is larger than 0.");
        } else if (messageBrowserPageSize <= 0) {
            getFrame().alertWarning(this, "Please enter an message browser page size larger than 0.");
        } else if (eventBrowserPageSize <= 0) {
            getFrame().alertWarning(this, "Please enter an event browser page size larger than 0.");
        } else {
            userPreferences.putInt("intervalTime", interval);
            userPreferences.putInt("messageBrowserPageSize", messageBrowserPageSize);
            userPreferences.putInt("eventBrowserPageSize", eventBrowserPageSize);
            userPreferences.putBoolean("messageBrowserFormatXml", formatXmlYesRadio.isSelected());

            userPreferences.putBoolean("messageBrowserVisibleColumnConnector", messageBrowserConnectorCheckBox.isSelected());
            userPreferences.putBoolean("messageBrowserVisibleColumnStatus", messageBrowserStatusCheckBox.isSelected());
            userPreferences.putBoolean("messageBrowserVisibleColumnReceived Date", messageBrowserReceivedDateCheckBox.isSelected());
            userPreferences.putBoolean("messageBrowserVisibleColumnOrig. Received Date", messageBrowserOrigReceivedDateCheckBox.isSelected());
            userPreferences.putBoolean("messageBrowserVisibleColumnServer Id", messageBrowserServerIdCheckBox.isSelected());
            userPreferences.putBoolean("messageBrowserVisibleColumnSend Attempts", messageBrowserSendAttemptsCheckBox.isSelected());
            userPreferences.putBoolean("messageBrowserVisibleColumnSend Date", messageBrowserSendDateCheckBox.isSelected());
            userPreferences.putBoolean("messageBrowserVisibleColumnResponse Date", messageBrowserResponseDateCheckBox.isSelected());
            userPreferences.putBoolean("messageBrowserVisibleColumnImport Id", messageBrowserImportIdCheckBox.isSelected());
            userPreferences.putBoolean("messageBrowserVisibleColumnErrors", messageBrowserErrorsCheckBox.isSelected());

            getFrame().setSaveEnabled(false);
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

        formatXmlButtonGroup = new javax.swing.ButtonGroup();
        clientSettings = new javax.swing.JPanel();
        dashboardRefreshIntervalLabel = new javax.swing.JLabel();
        dashboardRefreshIntervalField = new com.mirth.connect.client.ui.components.MirthTextField();
        messageBrowserPageSizeField = new com.mirth.connect.client.ui.components.MirthTextField();
        messageBrowserPageSizeLabel = new javax.swing.JLabel();
        formatXmlLabel = new javax.swing.JLabel();
        formatXmlYesRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        formatXmlNoRadio = new com.mirth.connect.client.ui.components.MirthRadioButton();
        eventBrowserPageSizeLabel = new javax.swing.JLabel();
        eventBrowserPageSizeField = new com.mirth.connect.client.ui.components.MirthTextField();
        messageBrowserdefaultColumnsLabel = new javax.swing.JLabel();
        messageBrowserConnectorCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        messageBrowserStatusCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        messageBrowserReceivedDateCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        messageBrowserSendAttemptsCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        messageBrowserImportIdCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        messageBrowserServerIdCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        messageBrowserErrorsCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        messageBrowserSendDateCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        messageBrowserResponseDateCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();
        messageBrowserOrigReceivedDateCheckBox = new com.mirth.connect.client.ui.components.MirthCheckBox();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        clientSettings.setBackground(new java.awt.Color(255, 255, 255));
        clientSettings.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(204, 204, 204)), "Preferences", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        dashboardRefreshIntervalLabel.setText("Dashboard refresh interval (seconds):");

        dashboardRefreshIntervalField.setToolTipText("<html>Interval in seconds at which to refresh the Dashboard. Decrement this for <br>faster updates, and increment it for slower servers with more channels.</html>");

        messageBrowserPageSizeField.setToolTipText("Sets the default page size for browsers (message, event, etc.)");

        messageBrowserPageSizeLabel.setText("Message browser page size:");

        formatXmlLabel.setText("Format XML in message browser:");

        formatXmlYesRadio.setBackground(new java.awt.Color(255, 255, 255));
        formatXmlYesRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        formatXmlButtonGroup.add(formatXmlYesRadio);
        formatXmlYesRadio.setSelected(true);
        formatXmlYesRadio.setText("Yes");
        formatXmlYesRadio.setToolTipText("Pretty print messages in the message browser that are XML.");
        formatXmlYesRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        formatXmlNoRadio.setBackground(new java.awt.Color(255, 255, 255));
        formatXmlNoRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        formatXmlButtonGroup.add(formatXmlNoRadio);
        formatXmlNoRadio.setText("No");
        formatXmlNoRadio.setToolTipText("Pretty print messages in the message browser that are XML.");
        formatXmlNoRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        eventBrowserPageSizeLabel.setText("Event browser page size:");

        eventBrowserPageSizeField.setToolTipText("Sets the default page size for browsers (message, event, etc.)");

        messageBrowserdefaultColumnsLabel.setText("Message browser default columns:");

        messageBrowserConnectorCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        messageBrowserConnectorCheckBox.setText("Connector");

        messageBrowserStatusCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        messageBrowserStatusCheckBox.setText("Status");

        messageBrowserReceivedDateCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        messageBrowserReceivedDateCheckBox.setText("Received Date");

        messageBrowserSendAttemptsCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        messageBrowserSendAttemptsCheckBox.setText("Send Attempts");

        messageBrowserImportIdCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        messageBrowserImportIdCheckBox.setText("Import Id");

        messageBrowserServerIdCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        messageBrowserServerIdCheckBox.setText("Server Id");

        messageBrowserErrorsCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        messageBrowserErrorsCheckBox.setText("Errors");

        messageBrowserSendDateCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        messageBrowserSendDateCheckBox.setText("Send Date");

        messageBrowserResponseDateCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        messageBrowserResponseDateCheckBox.setText("Response Date");

        messageBrowserOrigReceivedDateCheckBox.setBackground(new java.awt.Color(255, 255, 255));
        messageBrowserOrigReceivedDateCheckBox.setText("Orig. Received Date");

        javax.swing.GroupLayout clientSettingsLayout = new javax.swing.GroupLayout(clientSettings);
        clientSettings.setLayout(clientSettingsLayout);
        clientSettingsLayout.setHorizontalGroup(
            clientSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(clientSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(clientSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(messageBrowserdefaultColumnsLabel)
                    .addComponent(eventBrowserPageSizeLabel)
                    .addComponent(formatXmlLabel)
                    .addComponent(messageBrowserPageSizeLabel)
                    .addComponent(dashboardRefreshIntervalLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(clientSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(messageBrowserOrigReceivedDateCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(messageBrowserServerIdCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(messageBrowserResponseDateCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(messageBrowserSendDateCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(messageBrowserErrorsCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(messageBrowserImportIdCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(messageBrowserReceivedDateCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(messageBrowserStatusCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(messageBrowserConnectorCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dashboardRefreshIntervalField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(messageBrowserPageSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(clientSettingsLayout.createSequentialGroup()
                        .addComponent(formatXmlYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(formatXmlNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(eventBrowserPageSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(messageBrowserSendAttemptsCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(181, Short.MAX_VALUE))
        );
        clientSettingsLayout.setVerticalGroup(
            clientSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(clientSettingsLayout.createSequentialGroup()
                .addGroup(clientSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dashboardRefreshIntervalLabel)
                    .addComponent(dashboardRefreshIntervalField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(clientSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(messageBrowserPageSizeLabel)
                    .addComponent(messageBrowserPageSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(clientSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(eventBrowserPageSizeLabel)
                    .addComponent(eventBrowserPageSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(clientSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(formatXmlLabel)
                    .addComponent(formatXmlYesRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(formatXmlNoRadio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(clientSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(clientSettingsLayout.createSequentialGroup()
                        .addComponent(messageBrowserConnectorCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(messageBrowserStatusCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(messageBrowserReceivedDateCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(messageBrowserdefaultColumnsLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(messageBrowserOrigReceivedDateCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(messageBrowserSendAttemptsCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(messageBrowserSendDateCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(messageBrowserResponseDateCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(messageBrowserErrorsCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(messageBrowserServerIdCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(messageBrowserImportIdCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(clientSettings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(clientSettings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel clientSettings;
    private com.mirth.connect.client.ui.components.MirthTextField dashboardRefreshIntervalField;
    private javax.swing.JLabel dashboardRefreshIntervalLabel;
    private com.mirth.connect.client.ui.components.MirthTextField eventBrowserPageSizeField;
    private javax.swing.JLabel eventBrowserPageSizeLabel;
    private javax.swing.ButtonGroup formatXmlButtonGroup;
    private javax.swing.JLabel formatXmlLabel;
    private com.mirth.connect.client.ui.components.MirthRadioButton formatXmlNoRadio;
    private com.mirth.connect.client.ui.components.MirthRadioButton formatXmlYesRadio;
    private com.mirth.connect.client.ui.components.MirthCheckBox messageBrowserConnectorCheckBox;
    private com.mirth.connect.client.ui.components.MirthCheckBox messageBrowserErrorsCheckBox;
    private com.mirth.connect.client.ui.components.MirthCheckBox messageBrowserImportIdCheckBox;
    private com.mirth.connect.client.ui.components.MirthCheckBox messageBrowserOrigReceivedDateCheckBox;
    private com.mirth.connect.client.ui.components.MirthTextField messageBrowserPageSizeField;
    private javax.swing.JLabel messageBrowserPageSizeLabel;
    private com.mirth.connect.client.ui.components.MirthCheckBox messageBrowserReceivedDateCheckBox;
    private com.mirth.connect.client.ui.components.MirthCheckBox messageBrowserResponseDateCheckBox;
    private com.mirth.connect.client.ui.components.MirthCheckBox messageBrowserSendAttemptsCheckBox;
    private com.mirth.connect.client.ui.components.MirthCheckBox messageBrowserSendDateCheckBox;
    private com.mirth.connect.client.ui.components.MirthCheckBox messageBrowserServerIdCheckBox;
    private com.mirth.connect.client.ui.components.MirthCheckBox messageBrowserStatusCheckBox;
    private javax.swing.JLabel messageBrowserdefaultColumnsLabel;
    // End of variables declaration//GEN-END:variables
}
