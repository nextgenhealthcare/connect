/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Component;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JPanel;

public abstract class AbstractFramePanel extends JPanel {

    /**
     * Notifies the parent frame to switch the current view to this panel.
     */
    public abstract void switchPanel();

    /**
     * Determines whether the save task is currently visible for this panel.
     */
    public abstract boolean isSaveEnabled();

    /**
     * Sets the visibility of the save task for this panel.
     */
    public abstract void setSaveEnabled(boolean enabled);

    /**
     * Determines whether the save action is applicable for the current state of the panel.
     */
    public abstract boolean changesHaveBeenMade();

    /**
     * Allows the panel to save whatever data it needs to.
     */
    public abstract void doContextSensitiveSave();

    /**
     * When the panel is about to be exited or hidden, this allows it to prompt the user first. If
     * true is returned then it's okay to leave the panel, otherwise nothing should be done.
     */
    public abstract boolean confirmLeave();

    /**
     * Allows the caller to add items to the task pane and popup menu.
     */
    public final void addAction(Action action, Set<String> options, String callbackMethod) {
        Component component = addAction(action, options);
        if (component != null) {
            PlatformUI.MIRTH_FRAME.getComponentTaskMap().put(component, callbackMethod);
        }
    }

    /**
     * Allows the caller to add items to the task pane and popup menu.
     * 
     * @return The component added to the task pane.
     */
    protected abstract Component addAction(Action action, Set<String> options);
}