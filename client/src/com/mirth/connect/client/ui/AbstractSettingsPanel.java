/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.action.ActionFactory;
import org.jdesktop.swingx.action.BoundAction;

public abstract class AbstractSettingsPanel extends JPanel {

    private JXTaskPane taskPane = new JXTaskPane();
    private JPopupMenu popupMenu = new JPopupMenu();
    private Frame parent;
    private String tabName;
    private int saveIndex;

    public AbstractSettingsPanel(String tabName) {
        this.parent = PlatformUI.MIRTH_FRAME;
        this.tabName = tabName;

        taskPane.setTitle(tabName + " Tasks");
        taskPane.setName(tabName);
        taskPane.setFocusable(false);

        addTask("doRefresh", "Refresh", "Refresh " + tabName + " settings.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/arrow_refresh.png")));
        saveIndex = addTask("doSave", "Save", "Save " + tabName + " settings.", "", new ImageIcon(com.mirth.connect.client.ui.Frame.class.getResource("images/disk.png")));

        setVisibleTasks(saveIndex, saveIndex, false);

        this.addMouseListener(getPopupMenuMouseAdapter());
    }

    public MouseAdapter getPopupMenuMouseAdapter() {
        return new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        };
    }

    /**
     * Initializes the bound method call for the task taskPane actions and adds
     * them to the taskpane/popupmenu.
     */
    public int addTask(String callbackMethod, String displayName, String toolTip, String shortcutKey, ImageIcon icon) {
        BoundAction boundAction = ActionFactory.createBoundAction(callbackMethod, displayName, shortcutKey);

        if (icon != null) {
            boundAction.putValue(Action.SMALL_ICON, icon);
        }
        boundAction.putValue(Action.SHORT_DESCRIPTION, toolTip);
        boundAction.registerCallback(this, callbackMethod);

        Component component = taskPane.add(boundAction);
        parent.getComponentTaskMap().put(component, taskPane.getName() + "#" + callbackMethod);

        popupMenu.add(boundAction);

        return (taskPane.getContentPane().getComponentCount() - 1);
    }

    public void setVisibleTasks(int start, int end, boolean visible) {
        parent.setVisibleTasks(taskPane, popupMenu, start, end, visible);
    }

    public void setSaveEnabled(boolean enabled) {
        setVisibleTasks(saveIndex, saveIndex, enabled);
    }

    public boolean isSaveEnabled() {
        return taskPane.getContentPane().getComponent(saveIndex).isVisible();
    }

    public String getTabName() {
        return tabName;
    }

    public Frame getFrame() {
        return parent;
    }

    public JXTaskPane getTaskPane() {
        return taskPane;
    }
    
    public JPopupMenu getPopupMenu() {
        return popupMenu;
    }

    public abstract void doRefresh();

    public abstract void doSave();
}
