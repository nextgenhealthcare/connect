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
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.mirth.connect.model.ExtensionPoint;
import com.mirth.connect.model.ExtensionPointDefinition;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.plugins.ClientPanelPlugin;
import com.mirth.connect.plugins.DashboardPanelPlugin;

public class PluginPanel extends javax.swing.JPanel {

    public static final String EXTENSION_MANAGER = "Extension Manager";
    private Frame parent;
    private Map<String, ClientPanelPlugin> loadedPlugins;
    private Map<String, DashboardPanelPlugin> loadedDashboardPanelPlugins;
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

    //Extension point for ExtensionPoint.Type.CLIENT_PANEL
    @ExtensionPointDefinition(mode = ExtensionPoint.Mode.CLIENT, type = ExtensionPoint.Type.CLIENT_PANEL)
    public void loadPlugins() {
        loadedPlugins = new HashMap<String, ClientPanelPlugin>();

        Map<String, PluginMetaData> plugins = parent.getPluginMetaData();

        for (PluginMetaData metaData : plugins.values()) {
            try {
                if (metaData.isEnabled()) {
                    for (ExtensionPoint extensionPoint : metaData.getExtensionPoints()) {
                        if (extensionPoint.getMode() == ExtensionPoint.Mode.CLIENT && extensionPoint.getType() == ExtensionPoint.Type.CLIENT_PANEL && extensionPoint.getClassName() != null && extensionPoint.getClassName().length() > 0) {
                            String pluginName = extensionPoint.getName();
                            Class<?> clazz = Class.forName(extensionPoint.getClassName());
                            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
                            for (int i = 0; i < constructors.length; i++) {
                                Class<?> parameters[];
                                parameters = constructors[i].getParameterTypes();
                                // load plugin if the number of parameters is 1.
                                if (parameters.length == 1) {

                                    ClientPanelPlugin clientPlugin = (ClientPanelPlugin) constructors[i].newInstance(new Object[]{pluginName});

                                    clientPlugin.start();

                                    // add task pane before the "other" pane
                                    if (clientPlugin.getTaskPane() != null) {
                                        parent.setNonFocusable(clientPlugin.getTaskPane());
                                        clientPlugin.getTaskPane().setVisible(false);
                                        parent.taskPaneContainer.add(clientPlugin.getTaskPane(), parent.taskPaneContainer.getComponentCount() - 1);
                                    }

                                    if (clientPlugin.getComponent() != null) {
                                        if (pluginName.equals(EXTENSION_MANAGER) && tabs.getTabCount() > 0) {
                                            tabs.insertTab(pluginName, null, clientPlugin.getComponent(), null, 0);
                                        } else {
                                            tabs.addTab(pluginName, clientPlugin.getComponent());
                                        }
                                    }

                                    loadedPlugins.put(pluginName, clientPlugin);
                                    i = constructors.length;

                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                parent.alertException(this, e.getStackTrace(), e.getMessage());
            }
        }
    }

    public void stopPlugins() {
        for (ClientPanelPlugin plugin : loadedPlugins.values()) {
            plugin.stop();
        }

        loadedDashboardPanelPlugins = parent.getDashboardPanelPlugins();
        for (DashboardPanelPlugin plugin : loadedDashboardPanelPlugins.values()) {
            plugin.stop();
        }
    }
    
    public void resetPlugins() {
        for (ClientPanelPlugin plugin : loadedPlugins.values()) {
            plugin.reset();
        }

        loadedDashboardPanelPlugins = parent.getDashboardPanelPlugins();
        for (DashboardPanelPlugin plugin : loadedDashboardPanelPlugins.values()) {
            plugin.reset();
        }
    }

    public void loadDefaultPanel() {
        if (tabs.getTabCount() > 0) {
            if (loadedPlugins.containsKey(EXTENSION_MANAGER)) {
                loadPlugin(EXTENSION_MANAGER);
            } else {
                if (loadedPlugins.keySet().iterator().hasNext()) {
                    loadPlugin(loadedPlugins.keySet().iterator().next());
                }
            }
            tabs.setSelectedIndex(0);
        } else {
            setBlankPanel();
        }
    }

    public void loadPlugin(String pluginName) {
        ClientPanelPlugin plugin = loadedPlugins.get(pluginName);
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
    
    public Map<String, ClientPanelPlugin> getLoadedPlugins() {
        return loadedPlugins;
    }
    
    public Map<String, DashboardPanelPlugin> getLoadedDashboardPanelPlugins() {
        return loadedDashboardPanelPlugins;
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
