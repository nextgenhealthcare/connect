/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Properties;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.action.ActionFactory;
import org.jdesktop.swingx.action.BoundAction;

import com.mirth.connect.client.core.ClientException;

public abstract class ClientPanelPlugin extends ClientPlugin {

    private JComponent component = new JPanel();
    private JXTaskPane pane = new JXTaskPane();
    private JPopupMenu menu = new JPopupMenu();
    private int refreshIndex = -1;
    private int saveIndex = -1;
    private int reservedTasksCount = 0;

    public ClientPanelPlugin(String name, boolean refresh, boolean save) {
        this.name = name;
        getTaskPane().setTitle(name + " Tasks");
        getTaskPane().setName(name);
        getTaskPane().setFocusable(false);
        
        if (refresh) {
            addTask("doRefresh", "Refresh", "Refresh loaded plugins.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/arrow_refresh.png")));
            refreshIndex = reservedTasksCount;
            reservedTasksCount++;
        }

        if (save) {
            addTask("doSave", "Save", "Save plugin settings.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/disk.png")));
            saveIndex = reservedTasksCount;
            reservedTasksCount++;
        }
    }

    public void setComponent(JComponent component) {
        this.component = component;
    }

    public void setTaskPane(JXTaskPane pane) {
        this.pane = pane;
    }

    public void setPopupMenu(JPopupMenu menu) {
        this.menu = menu;
    }

    public JComponent getComponent() {
        return component;
    }

    public JXTaskPane getTaskPane() {
        return pane;
    }

    public JPopupMenu getPopupMenu() {
        return menu;
    }

    public Object invoke(String method, Object object) throws ClientException {
        return parent.mirthClient.invokePluginMethod(name, method, object);
    }

    public MouseAdapter getPopupMenuMouseAdapter() {
        return new MouseAdapter() {

            public void mousePressed(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    getPopupMenu().show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }

            public void mouseReleased(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    getPopupMenu().show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        };
    }

    /**
     * Initializes the bound method call for the task pane actions and adds them
     * to the taskpane/popupmenu.
     */
    public void addTask(String callbackMethod, String displayName, String toolTip, String shortcutKey, ImageIcon icon) {
        BoundAction boundAction = ActionFactory.createBoundAction(callbackMethod, displayName, shortcutKey);

        if (icon != null) {
            boundAction.putValue(Action.SMALL_ICON, icon);
        }
        boundAction.putValue(Action.SHORT_DESCRIPTION, toolTip);
        boundAction.registerCallback(this, callbackMethod);

        Component component = getTaskPane().add(boundAction);
        parent.getComponentTaskMap().put(component, getTaskPane().getName() + "#" + callbackMethod);
        
        getPopupMenu().add(boundAction);
    }

    public void setVisibleTasks(int start, int end, boolean visible) {
        parent.setVisibleTasks(getTaskPane(), getPopupMenu(), start, end, visible);
    }

    public void alertException(Component parentComponent, StackTraceElement[] strace, String message) {
        parent.alertException(parentComponent, strace, message);
    }

    public void alertWarning(Component parentComponent, String message) {
        parent.alertWarning(parentComponent, message);
    }

    public void alertInformation(Component parentComponent, String message) {
        parent.alertInformation(parentComponent, message);
    }

    public void alertError(Component parentComponent, String message) {
        parent.alertError(parentComponent, message);
    }

    public boolean alertOkCancel(Component parentComponent, String message) {
        return parent.alertOkCancel(parentComponent, message);
    }

    public boolean alertOption(Component parentComponent, String message) {
        return parent.alertOption(parentComponent, message);
    }

    public void setWorking(String message, boolean working) {
        parent.setWorking(message, working);
    }

    public Properties getPropertiesFromServer() throws ClientException {
        return parent.mirthClient.getPluginProperties(name);
    }

    public void setPropertiesToServer(Properties properties) throws ClientException {
        parent.mirthClient.setPluginProperties(name, properties);
    }

    public void doRefresh() {
    }

    public void doSave() {
    }

    public void enableRefresh() {
        setVisibleTasks(refreshIndex, refreshIndex, true);
    }

    public void disableRefresh() {
        setVisibleTasks(refreshIndex, refreshIndex, false);
    }

    public void enableSave() {
        parent.setSaveEnabled(true);
    }

    public void disableSave() {
        parent.setSaveEnabled(false);
    }

    public int getRefreshIndex() {
        return refreshIndex;
    }

    public int getSaveIndex() {
        return saveIndex;
    }

    public int getReservedTasksCount() {
        return reservedTasksCount;
    }

    public boolean confirmLeave() {
        return parent.confirmLeave();
    }

    public boolean pluginConfirmLeave() {
        int option = JOptionPane.showConfirmDialog(parent, "Would you like to save the plugin changes?");

        if (option == JOptionPane.YES_OPTION) {
            doSave();
        } else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
            return false;
        }

        return true;
    }

    // used for starting processes in the plugin when the program is started
    public abstract void start();

    // used for stopping processes in the plugin when the program is exited
    public abstract void stop();

    // used for setting actions to be called when the plugin tab is loaded
    public abstract void display();
}
