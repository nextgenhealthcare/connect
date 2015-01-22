/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta;

import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.fife.ui.rtextarea.RecordableTextAction;

public class CustomMenuItem extends JMenuItem {

    public CustomMenuItem(JComponent parent, Action action) {
        this(parent, action, KeyEvent.VK_UNDEFINED);
    }

    public CustomMenuItem(JComponent parent, Action action, int acceleratorKeyCode) {
        this(parent, action, acceleratorKeyCode, 0);
    }

    public CustomMenuItem(JComponent parent, Action action, int acceleratorKeyCode, int acceleratorModifiers) {
        super(action);
        if (acceleratorKeyCode != KeyEvent.VK_UNDEFINED) {
            KeyStroke accelerator = KeyStroke.getKeyStroke(acceleratorKeyCode, acceleratorModifiers);
            setAccelerator(accelerator);
            if (action instanceof RecordableTextAction) {
                ((RecordableTextAction) action).setAccelerator(accelerator);
            }
            String name = (String) action.getValue(Action.NAME);
            parent.getInputMap().put(accelerator, name);
            parent.getActionMap().put(name, action);
        }
    }

    @Override
    public void setToolTipText(String text) {
        // Ignore! Actions (e.g. undo/redo) set this when changing their text due to changing enabled state.
    }
}