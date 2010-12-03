/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.mirth.connect.plugins.ClientPanelPlugin;
import com.mirth.connect.plugins.DashboardPanelPlugin;

public class PluginPanel extends javax.swing.JPanel {

    public static final String EXTENSION_MANAGER = "Extension Manager";
    private Frame parent;
    private int oldTabIndex = -1;
    private boolean cancelTabChange = false;
    private ClientPanelPlugin currentPanelPlugin = null;

    /** Creates new form PluginPanel */
    public PluginPanel() {
        parent = PlatformUI.MIRTH_FRAME;
        initComponents();
        loadPlugins();

        ChangeListener changeListener = new ChangeListener() {

            public void stateChanged(ChangeEvent changeEvent) {
                JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();

                /*
                 * There is no way to cancel a tab change, so instead we
                 * save the old tab's index and set that tab as selected
                 * again if the tab change is canceled.  When setting the
                 * tab back we don't want to execute any tab changed actions,
                 * so we set a cancelTabChange flag.
                 * 
                 */
                if (cancelTabChange) {
                    cancelTabChange = false;
                } else {
                    if (!parent.confirmLeave()) {
                        cancelTabChange = true;
                        sourceTabbedPane.setSelectedIndex(oldTabIndex);
                    } else {
                        int index = sourceTabbedPane.getSelectedIndex();
                        oldTabIndex = index;
                        loadPlugin(sourceTabbedPane.getTitleAt(index));
                    }
                }
            }
        };
        tabs.addChangeListener(changeListener);
    }

    public void loadPlugins() {
        for (ClientPanelPlugin clientPanelPlugin : LoadedExtensions.getInstance().getClientPanelPlugins().values()) {

            clientPanelPlugin.start();

            // add task pane before the "other" pane
            if (clientPanelPlugin.getTaskPane() != null) {
                parent.setNonFocusable(clientPanelPlugin.getTaskPane());
                clientPanelPlugin.getTaskPane().setVisible(false);
                parent.taskPaneContainer.add(clientPanelPlugin.getTaskPane(), parent.taskPaneContainer.getComponentCount() - 1);
            }

            if (clientPanelPlugin.getComponent() != null) {
                if (clientPanelPlugin.getName().equals(EXTENSION_MANAGER) && tabs.getTabCount() > 0) {
                    tabs.insertTab(clientPanelPlugin.getName(), null, clientPanelPlugin.getComponent(), null, 0);
                } else {
                    tabs.addTab(clientPanelPlugin.getName(), clientPanelPlugin.getComponent());
                }
            }
        }
    }

    public void stopPlugins() {
        for (ClientPanelPlugin clientPanelPlugin : LoadedExtensions.getInstance().getClientPanelPlugins().values()) {
            clientPanelPlugin.stop();
        }

        for (DashboardPanelPlugin dashboardPanelPlugin : LoadedExtensions.getInstance().getDashboardPanelPlugins().values()) {
            dashboardPanelPlugin.stop();
        }
    }
    
    public void resetPlugins() {
        for (ClientPanelPlugin clientPanelPlugin : LoadedExtensions.getInstance().getClientPanelPlugins().values()) {
            clientPanelPlugin.reset();
        }

        for (DashboardPanelPlugin dashboardPanelPlugin : LoadedExtensions.getInstance().getDashboardPanelPlugins().values()) {
            dashboardPanelPlugin.reset();
        }
    }

    public void loadDefaultPanel() {
        if (tabs.getTabCount() > 0) {
            if (LoadedExtensions.getInstance().getClientPanelPlugins().containsKey(EXTENSION_MANAGER)) {
                loadPlugin(EXTENSION_MANAGER);
            } else {
                if (LoadedExtensions.getInstance().getClientPanelPlugins().keySet().iterator().hasNext()) {
                    loadPlugin(LoadedExtensions.getInstance().getClientPanelPlugins().keySet().iterator().next());
                }
            }
            tabs.setSelectedIndex(0);
        } else {
            setBlankPanel();
        }
    }

    public void loadPlugin(String pluginName) {
        ClientPanelPlugin plugin = LoadedExtensions.getInstance().getClientPanelPlugins().get(pluginName);
        currentPanelPlugin = plugin;

        if (plugin != null) {
            parent.setFocus(plugin.getTaskPane());
            plugin.display();
        } else {
            parent.setFocus(null);
        }
    }

    public ClientPanelPlugin getCurrentPanelPlugin() {
        return currentPanelPlugin;
    }
    
    public void setBlankPanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("No plugin panels to display."));
        panel.setBackground(Color.WHITE);
        tabs.addTab("No Plugins", panel);
        tabs.setSelectedIndex(0);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabs = new javax.swing.JTabbedPane();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabs, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabs, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane tabs;
    // End of variables declaration//GEN-END:variables
}
