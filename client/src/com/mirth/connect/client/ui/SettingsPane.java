/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultSingleSelectionModel;
import javax.swing.SingleSelectionModel;

import com.mirth.connect.client.core.TaskConstants;
import com.mirth.connect.client.ui.tag.SettingsPanelTags;
import com.mirth.connect.plugins.SettingsPanelPlugin;

public class SettingsPane extends javax.swing.JPanel {

    private FrameBase parent;
    private AbstractSettingsPanel currentSettingsPanel = null;
    private Map<String, AbstractSettingsPanel> settingsPanelMap = new HashMap<String, AbstractSettingsPanel>();

    /** Creates new form PluginPanel */
    public SettingsPane() {
        parent = PlatformUI.MIRTH_FRAME;
        initComponents();

        loadPanel(new SettingsPanelServer(SettingsPanelServer.TAB_NAME));
        loadPanel(new SettingsPanelAdministrator(SettingsPanelAdministrator.TAB_NAME));
        loadPanel(new SettingsPanelTags(SettingsPanelTags.TAB_NAME));
        loadPanel(new SettingsPanelMap(SettingsPanelMap.TAB_NAME));
        loadPanel(new SettingsPanelDatabaseTasks(SettingsPanelDatabaseTasks.TAB_NAME));
        loadPanel(new SettingsPanelResources(SettingsPanelResources.TAB_NAME));
        loadPluginPanels();

        SingleSelectionModel model = new DefaultSingleSelectionModel() {

            public void setSelectedIndex(int index) {
                if (parent.confirmLeave()) {
                    setCurrentSettingsPanel(index);
                    super.setSelectedIndex(index);
                }
            }
        };
        tabbedPane.setModel(model);
    }

    public void loadPluginPanels() {
        for (SettingsPanelPlugin settingsPanelPlugin : LoadedExtensions.getInstance().getSettingsPanelPlugins().values()) {
            loadPanel(settingsPanelPlugin.getSettingsPanel());
        }
    }

    private void loadPanel(AbstractSettingsPanel settingsPanel) {
        // Only load the panel if the refresh task is authorized
        if (AuthorizationControllerFactory.getAuthorizationController().checkTask(settingsPanel.getTaskPane().getName(), TaskConstants.SETTINGS_REFRESH)) {
            // add task pane before the "other" pane
            parent.setNonFocusable(settingsPanel.getTaskPane());
            settingsPanel.getTaskPane().setVisible(false);
            parent.getTaskPaneContainer().add(settingsPanel.getTaskPane(), parent.getTaskPaneContainer().getComponentCount() - 1);

            // Add the tab
            tabbedPane.addTab(settingsPanel.getTabName(), settingsPanel);

            settingsPanelMap.put(settingsPanel.getTabName(), settingsPanel);
        }
    }

    private void setCurrentSettingsPanel(int index) {
        String tabName = tabbedPane.getTitleAt(index);
        currentSettingsPanel = settingsPanelMap.get(tabName);
        parent.setFocus(currentSettingsPanel.getTaskPane());
        currentSettingsPanel.doRefresh();
    }

    public void setSelectedSettingsPanel(int index) {
        // The custom SingleSelectionModel calls setCurrentSettingsPanel(index)
        // when tabbedPane.setSelectedIndex(0) is called.
        tabbedPane.setSelectedIndex(0);
    }

    public AbstractSettingsPanel getCurrentSettingsPanel() {
        return currentSettingsPanel;
    }

    public AbstractSettingsPanel getSettingsPanel(String tabName) {
        return settingsPanelMap.get(tabName);
    }

    // @formatter:off
    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPane = new javax.swing.JTabbedPane();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // @formatter:on

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
}
