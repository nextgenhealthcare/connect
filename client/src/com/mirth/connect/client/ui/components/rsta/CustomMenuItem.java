/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.fife.ui.rtextarea.RecordableTextAction;

import com.mirth.connect.client.ui.components.rsta.actions.ActionInfo;

public class CustomMenuItem extends JMenuItem {

    private ActionInfo actionInfo;

    public CustomMenuItem(JComponent parent, Action action, ActionInfo actionInfo) {
        super(action);
        this.actionInfo = actionInfo;
        updateAccelerator();
        parent.getActionMap().put(actionInfo.getActionMapKey(), action);
    }

    public void updateAccelerator() {
        KeyStroke accelerator = MirthInputMap.getInstance().getKeyStroke(actionInfo);
        setAccelerator(accelerator);
        if (getAction() != null && getAction() instanceof RecordableTextAction) {
            ((RecordableTextAction) getAction()).setAccelerator(accelerator);
        }
    }
}